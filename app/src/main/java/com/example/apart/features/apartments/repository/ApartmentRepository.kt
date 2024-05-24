package com.example.apart.features.apartments.repository

import android.util.Log
import com.example.apart.features.apartments.data.ApartmentApiState
import com.example.apart.features.apartments.data.ApartmentModelBackend
import com.example.apart.features.apartments.network.ApartApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class ApartmentRepository(
    private val apartApi: ApartApiService
){
    suspend fun getApartments(): Flow<ApartmentApiState> = flow {
        try {
            Log.d("Repository", "Before loaded list")
            val loadedList = apartApi.fetchApartments()
            Log.d("Repository", loadedList.joinToString())
            emit(ApartmentApiState.success(loadedList))
        } catch (e: Exception) {
            throw e
        }

    }.flowOn(Dispatchers.IO)

}
