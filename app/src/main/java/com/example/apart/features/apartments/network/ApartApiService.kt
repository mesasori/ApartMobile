package com.example.apart.features.apartments.network

import android.util.Log
import com.example.apart.features.apartments.data.ApartmentModelBackend
import com.example.apart.features.apartments.data.PostBodyModel
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApartApiService {
    @GET("/crud/")
    suspend fun fetchApartments(): List<ApartmentModelBackend>

    @POST("/action/get_aparts")
    suspend fun fetchApartments(@Body body: PostBodyModel): Call<List<ApartmentModelBackend>>

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