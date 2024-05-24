package com.example.apart.features.apartments.network

import android.util.Log
import com.example.apart.features.apartments.data.ApartmentModelBackend
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET

interface ApartApiService {
    @GET("/crud")
    suspend fun fetchApartments(): List<ApartmentModelBackend>

    companion object {
        fun getInstance(): ApartApiService {
            Log.d("ApiService", "Created")
            val r = Retrofit.Builder()
                .baseUrl("http://localhost:8000")
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
            Log.e("API SERVICe", r.toString())
            return r.create()
        }
    }
}