package org.oicar.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val retrofit: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5194/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}