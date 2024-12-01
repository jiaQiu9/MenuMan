package com.example.menuman

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RecipeRetrofitInstance {
    private const val BASE_URL = "https://api.spoonacular.com"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: RecipeApiService by lazy {
        retrofit.create(RecipeApiService::class.java)
    }
}