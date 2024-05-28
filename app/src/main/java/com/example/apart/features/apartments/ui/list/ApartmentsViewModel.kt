package com.example.apart.features.apartments.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apart.features.apartments.data.ApartmentHolderItem
import com.example.apart.features.apartments.network.ApartApiService
import com.example.apart.features.apartments.repository.ApartmentRepository
import com.example.apart.utils.Result
import com.example.apart.utils.asResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApartmentsViewModel : ViewModel() {
    private val apiService = ApartApiService.getInstance()
    private val apartmentRepository: ApartmentRepository = ApartmentRepository(apiService)
    private val _uiState = MutableStateFlow(ApartmentsUiState.Loading)
    val uiState: StateFlow<ApartmentsUiState> = _uiState.asStateFlow()

    fun update() {
        viewModelScope.launch(Dispatchers.IO) {
            apartmentRepository
                .getApartments()
                .asResult()
                .map { result ->
                    when (result) {
                        is Result.Error -> ApartmentsUiState.Error(result.exception)
                        is Result.Loading -> ApartmentsUiState.Loading
                        is Result.Success -> ApartmentsUiState.Success(result.data.map { it.toHolderItem() })
                    }
                }.collect {
                    _uiState.update { it }
                }
        }
    }

    init {
        update()
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