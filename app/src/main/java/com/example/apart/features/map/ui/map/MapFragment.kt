package com.example.apart.features.map.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.DrawableContainer.DrawableContainerState
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.apart.R
import com.example.apart.databinding.FragmentMapBinding
import com.example.apart.utils.GeoObjectHolder
import com.example.apart.features.map.ui.details.GeoDetailsDialogFragment
import com.example.apart.features.map.ui.places.PlacesDialogFragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.SizeChangedListener
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: Map
    private lateinit var mapWindow: MapWindow

    private val suggestAdapter = SuggestsListAdapter()
    private lateinit var editQueryTextWatcher: TextWatcher

    private val cameraListener = CameraListener { _, _, reason, _ ->
        if (reason == CameraUpdateReason.GESTURES) {
            viewModel.setVisibleRegion(map.visibleRegion)
        }
    }

    private val searchResultPlacemarkTapListener = MapObjectTapListener { mapObject, _ ->
        val selectedObject = mapObject.userData as? GeoObject
        GeoObjectHolder.tappedObject = selectedObject
        showGeoDetailsDialogFragment()
        true
    }

    private val geoObjectTapListener = GeoObjectTapListener {
        val point = it.geoObject.geometry.firstOrNull()?.point ?: return@GeoObjectTapListener true

        var curZoom = 0f
        map.cameraPosition.run {
            val position = CameraPosition(point, zoom, azimuth, tilt)
            map.move(position, MOVE_ANIMATION, null)
            curZoom = zoom
        }

        val selectionMetadata =
            it.geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)

        map.selectGeoObject(selectionMetadata)
        viewModel.searchByPoint(point, curZoom.toInt())

        true
    }

    private val sizeChangedListener = SizeChangedListener { _, _, _ -> updateFocusRect() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapWindow = binding.mapview.mapWindow
        map = mapWindow.map

        map.move(START_POSITION)
        map.addCameraListener(cameraListener)
        map.addTapListener(geoObjectTapListener)
        viewModel.setVisibleRegion(map.visibleRegion)
        binding.mapview.mapWindow.addSizeChangedListener(sizeChangedListener)

        updateFocusRect()

        binding.apply {
            listSuggests.adapter = suggestAdapter

            buttonSearch.setOnClickListener { viewModel.startSearch() }
            buttonReset.setOnClickListener { viewModel.reset() }
            buttonZoomPlus.setOnClickListener { changeZoomByStep(ZOOM_STEP) }
            buttonZoomMinus.setOnClickListener { changeZoomByStep(-ZOOM_STEP) }
            buttonPlaces.setOnClickListener { showPlacesDialogFragment() }

            editQueryTextWatcher = editQuery.doAfterTextChanged { text ->
                if (text.toString() == viewModel.uiState.value.query) return@doAfterTextChanged
                viewModel.setQueryText(text.toString())
            }

            editQuery.setOnEditorActionListener { _, _, _ ->
                viewModel.startSearch()
                true
            }
        }

        viewModel.uiState
            .flowWithLifecycle(lifecycle)
            .onEach {
                suggestAdapter.items =
                    (it.suggestState as? SuggestState.Success)?.items ?: emptyList()

                if (it.suggestState is SuggestState.Error) {
                    Toast.makeText(
                        requireContext(),
                        "Suggest error, check your network connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val successSearchState = it.searchState as? SearchState.Success
                val searchItems = successSearchState?.items ?: emptyList()
                updateSearchResponsePlacemarks(searchItems)
                if (successSearchState?.zoomToItems == true) {
                    Log.d("Zoom to item", "zoom")
                    focusCamera(
                        searchItems.map { item -> item.point },
                        successSearchState.itemsBoundingBox
                    )
                }

                if (it.searchState is SearchState.Error) {
                    Toast.makeText(
                        requireContext(),
                        "Suggest error, check your network connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.apply {
                    editQuery.apply {
                        if (text.toString() != it.query) {
                            removeTextChangedListener(editQueryTextWatcher)
                            setText(it.query)
                            addTextChangedListener(editQueryTextWatcher)
                        }
                    }
                    buttonSearch.isEnabled =
                        it.query.isNotEmpty() && it.searchState == SearchState.Off
                    if (buttonSearch.isEnabled) {
                        buttonSearch.visibility = View.VISIBLE
                        decorationView.visibility = View.VISIBLE
                        val drawables = editQuery.compoundDrawables
                        for (d in drawables) {
                            if (d != null) {
                                Log.d("MapFragment", d.alpha.toString())
                                d.alpha = 0
                            }
                        }
                    }
                    else {
                        buttonSearch.visibility = View.GONE
                        decorationView.visibility = View.GONE
                        val drawables = editQuery.compoundDrawables
                        for (d in drawables) {
                            if (d != null) d.alpha = 255
                        }
                    }

                    buttonReset.isEnabled =
                        it.query.isNotEmpty() || it.searchState !is SearchState.Off

                    editQuery.isEnabled = it.searchState is SearchState.Off
                }
            }
            .launchIn(lifecycleScope)

        viewModel.subscribeForSuggest().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
        viewModel.subscribeForSearch().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
    }

    private fun updateSearchResponsePlacemarks(items: List<SearchResponseItem>) {
        map.mapObjects.clear()

        val bitmap =
            getBitmapFromVectorDrawable(requireActivity(), R.drawable.baseline_location_pin_24)

        val itemsToShow = if (!viewModel.showOnlyOnePlacemark()) items else {
            if (items.isEmpty()) items
            else listOf(items[0])
        }

        itemsToShow.forEach {
            map.mapObjects.addPlacemark().apply {
                geometry = it.point
                setIcon(
                    ImageProvider.fromBitmap(bitmap),
                    IconStyle().apply { scale = PLACEMARK_SCALE })
                addTapListener(searchResultPlacemarkTapListener)
                userData = it.geoObject
            }
        }
    }

    private fun focusCamera(points: List<Point>, boundingBox: BoundingBox) {
        if (points.isEmpty()) return

        val position = if (points.size == 1) {
            map.cameraPosition.run {
                CameraPosition(points.first(), zoom, azimuth, tilt)
            }
        } else {
            map.cameraPosition(Geometry.fromBoundingBox(boundingBox))
        }

        map.move(position, MOVE_ANIMATION, null)
    }

    private fun updateFocusRect() {
        val horizontal = resources.getDimension(R.dimen.window_horizontal_padding)
        val vertical = resources.getDimension(R.dimen.window_vertical_padding)
        val window = binding.mapview.mapWindow

        window.focusRect = ScreenRect(
            ScreenPoint(horizontal, vertical),
            ScreenPoint(window.width() - horizontal, window.height() - vertical),
        )
    }

    private fun showGeoDetailsDialogFragment() {
        val detailsFragment = GeoDetailsDialogFragment()
        detailsFragment.show(this@MapFragment.parentFragmentManager, null)
    }

    private fun showPlacesDialogFragment() {
        val placesFragment = PlacesDialogFragment()
        placesFragment.show(this@MapFragment.parentFragmentManager, null)
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        lateinit var bitmap: Bitmap
        drawable?.let {
            bitmap = Bitmap.createBitmap(
                it.intrinsicWidth,
                it.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            it.setBounds(0, 0, canvas.width, canvas.height)
            it.draw(canvas)
        }
        return bitmap
    }

    private fun changeZoomByStep(value: Float) {
        with(map.cameraPosition) {
            map.move(
                CameraPosition(target, zoom + value, azimuth, tilt),
                ZOOM_ANIMATION,
                null
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this.requireActivity())
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        GeoObjectHolder.tappedObject = null
        GeoObjectHolder.selectedObject = null
    }

    companion object {
        private val START_POSITION = CameraPosition(Point(55.753284, 37.622034), 13.0f, 0f, 0f)

        private val MOVE_ANIMATION = Animation(Animation.Type.SMOOTH, 0.5f)

        // Parameters
        private const val PLACEMARK_SCALE = 1.5f
        private const val ZOOM_STEP = 1.0f
        private val ZOOM_ANIMATION = Animation(Animation.Type.LINEAR, 0.2f)
    }
}