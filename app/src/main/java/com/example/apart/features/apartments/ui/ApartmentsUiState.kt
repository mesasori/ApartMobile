package com.example.apart.features.apartments.ui

import com.example.apart.features.apartments.data.ApartmentHolderItem

sealed interface ApartmentsUiState {
    object Loading : ApartmentsUiState

    data class Success(
        val data: List<ApartmentHolderItem>
    ) : ApartmentsUiState

    data class Error(
        val throwable: Throwable?
    ) : ApartmentsUiState
}