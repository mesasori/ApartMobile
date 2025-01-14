package com.example.apart.features.apartments.repository

import android.util.Log
import com.example.apart.features.apartments.data.ApartmentModelBackend
import com.example.apart.features.apartments.data.PostBodyModel
import com.example.apart.features.apartments.network.ApartApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.coroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.coroutineContext


class ApartmentRepository(
    private val apartApi: ApartApiService
) {
    fun getApartments(): Flow<List<ApartmentModelBackend>> = flow {
        try {
            val loadedList = apartApi.fetchApartments()
            Log.i("APARTMENTS REPOSITORY", loadedList.toString())
            emit(loadedList)
        } catch (e: Error) {
            Log.e("APARTMENT REPOSITORY", e.message.toString())
        }
    }.flowOn(Dispatchers.IO)

}

sealed interface NetworkRequestState {
    data class Error(val error: kotlin.Error): NetworkRequestState

    data class Success(val data: List<ApartmentModelBackend>): NetworkRequestState
}
