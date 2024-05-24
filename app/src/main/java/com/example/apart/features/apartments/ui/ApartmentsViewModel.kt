package com.example.apart.features.apartments.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.features.apartments.data.ApartmentApiState
import com.example.apart.features.apartments.data.ApartmentHolderItem
import com.example.apart.features.apartments.data.Error
import com.example.apart.features.apartments.data.Status
import com.example.apart.features.apartments.network.ApartApiService
import com.example.apart.features.apartments.repository.ApartmentRepository
import com.example.apart.utils.Result
import com.example.apart.utils.asResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApartmentsViewModel : ViewModel() {
    private val apiService = ApartApiService.getInstance()
    private val apartmentRepository: ApartmentRepository = ApartmentRepository(apiService)

    private val _list = MutableStateFlow(ApartmentApiState(Status.LOADING, listOf(), Error.NONE))
    val list = _list.asStateFlow()

    fun loadImages() {
        _list.value = ApartmentApiState.loading()
        viewModelScope.launch {
            apartmentRepository.getApartments()
                .catch {
                    var error = Error.NONE
                    it.message?.let { message ->
                        Log.d("ApartmentsViewModel", it.toString())
                        error = if (message.contains("HTTP")) Error.REQUEST
                        else Error.INTERNET
                    }

                    _list.value = ApartmentApiState.error(error)
                }
                .collect {
                    _list.value = ApartmentApiState.success(it.data.shuffled())
                }
        }
    }

    init {
        loadImages()
    }
}

sealed interface ApartmentsUiState {
    object Loading : ApartmentsUiState

    data class Success(
        val data: List<ApartmentHolderItem>
    ) : ApartmentsUiState

    data class Error(
        val throwable: Throwable?
    ) : ApartmentsUiState
}