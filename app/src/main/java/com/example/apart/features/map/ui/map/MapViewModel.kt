package com.example.apart.features.map.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.utils.toBoundingBox
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.SuggestItem
import com.yandex.mapkit.search.SuggestOptions
import com.yandex.mapkit.search.SuggestResponse
import com.yandex.mapkit.search.SuggestSession
import com.yandex.mapkit.search.SuggestType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class MapViewModel : ViewModel() {
    private val searchManager =
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    private var searchSession: Session? = null
    private val suggestSession: SuggestSession = searchManager.createSuggestSession()
    private var zoomToSearchResult = false
    private var showFirstPlacemark = false

    private val region = MutableStateFlow<VisibleRegion?>(null)

    @OptIn(FlowPreview::class)
    private val throttledRegion = region.debounce(1.seconds)
    private val query = MutableStateFlow("")
    private val searchState = MutableStateFlow<SearchState>(SearchState.Off)
    private val suggestState = MutableStateFlow<SuggestState>(SuggestState.Off)

    val uiState: StateFlow<MapUiState> = combine(
        query,
        searchState,
        suggestState,
    ) { query, searchState, suggestState ->
        MapUiState(
            query = query,
            searchState = searchState,
            suggestState = suggestState,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, MapUiState())

    fun setQueryText(value: String) {
        query.value = value
    }

    fun setVisibleRegion(region: VisibleRegion) {
        this.region.value = region
    }

    fun startSearch(searchText: String? = null) {
        val text = searchText ?: query.value
        if (query.value.isEmpty()) return
        val region = region.value?.let {
            VisibleRegionUtils.toPolygon(it)
        } ?: return

        submitSearch(text, region)
    }

    fun showOnlyOnePlacemark() = showFirstPlacemark

    fun reset() {
        searchSession?.cancel()
        searchSession = null
        searchState.value = SearchState.Off
        resetSuggest()
        query.value = ""
        showFirstPlacemark = false
    }

    fun subscribeForSuggest(): Flow<*> {
        return combine(
            query,
            throttledRegion,
            searchState,
        ) { query, region, searchState ->
            if (query.isNotEmpty() && region != null && searchState == SearchState.Off) {
                submitSuggest(query, region.toBoundingBox())
            } else {
                resetSuggest()
            }
        }
    }

    fun subscribeForSearch(): Flow<*> {
        return throttledRegion.filter { it != null }
            .filter { searchState.value is SearchState.Success }
            .mapNotNull { it }
            .onEach { region ->
                searchSession?.let {
                    it.setSearchArea(VisibleRegionUtils.toPolygon(region))
                    it.resubmit(searchSessionListener)
                    searchState.value = SearchState.Loading
                    zoomToSearchResult = false
                    showFirstPlacemark = false
                }
            }
    }

    fun searchByPoint(point: Point, zoom: Int) {
        searchSession?.cancel()
        searchSession = searchManager.submit(point, zoom ,SearchOptions(), searchSessionListener)
        searchState.value = SearchState.Loading
        zoomToSearchResult = false
        showFirstPlacemark = true
    }

    private fun submitUriSearch(uri: String) {
        searchSession?.cancel()
        searchSession = searchManager.searchByURI(
            uri,
            SearchOptions(),
            searchSessionListener
        )
        searchState.value = SearchState.Loading
        zoomToSearchResult = true
        showFirstPlacemark = false
    }

    private val searchSessionListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val items = response.collection.children.mapNotNull {
                val point = it.obj?.geometry?.firstOrNull()?.point ?: return@mapNotNull null
                SearchResponseItem(point, it.obj)
            }
            val boundingBox = response.metadata.boundingBox ?: return

            searchState.value = SearchState.Success(
                items,
                zoomToSearchResult,
                boundingBox,
            )
        }

        override fun onSearchError(p0: com.yandex.runtime.Error) {
            searchState.value = SearchState.Error
        }
    }

    private fun submitSearch(query: String, geometry: Geometry) {
        searchSession?.cancel()
        searchSession = searchManager.submit(
            query,
            geometry,
            SearchOptions().apply {
                resultPageSize = 32
            },
            searchSessionListener
        )
        searchState.value = SearchState.Loading
        zoomToSearchResult = true
        showFirstPlacemark = false
    }

    private val suggestSessionListener = object : SuggestSession.SuggestListener {
        override fun onResponse(responce: SuggestResponse) {
            val suggestItems = responce.items.take(SUGGEST_NUMBER_LIMIT)
                .map {
                    SuggestHolderItem(
                        title = it.title,
                        subtitle = it.subtitle,
                    ) {
                        setQueryText(it.displayText ?: "")

                        if (it.action == SuggestItem.Action.SEARCH) {
                            val uri = it.uri
                            if (uri != null) {
                                submitUriSearch(uri)
                            } else {
                                startSearch(it.searchText)
                            }
                        }
                    }
                }
            suggestState.value = SuggestState.Success(suggestItems)
        }

        override fun onError(p0: com.yandex.runtime.Error) {
            suggestState.value = SuggestState.Error
        }
    }

    private fun submitSuggest(
        query: String,
        box: BoundingBox,
        options: SuggestOptions = SUGGEST_OPTIONS,
    ) {
        suggestSession.suggest(query, box, options, suggestSessionListener)
        suggestState.value = SuggestState.Loading
    }

    private fun resetSuggest() {
        suggestSession.reset()
        suggestState.value = SuggestState.Off
    }

    companion object {
        private const val SUGGEST_NUMBER_LIMIT = 20
        private val SUGGEST_OPTIONS = SuggestOptions().setSuggestTypes(
            SuggestType.GEO.value
                    or SuggestType.BIZ.value
                    or SuggestType.TRANSIT.value
        )
    }
}