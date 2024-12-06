package com.example.menuman

import retrofit2.http.GET
import retrofit2.http.Headers

interface RecipeApiService {
    @Headers(

        //"Accept: application/json"
        "x-rapidapi-host: spoonacular-recipe-food-nutrition-v1.p.rapidapi.com",
        "x-rapidapi-key: ce618b884fmsh57af0c27908b4c5p18e0a4jsn2384fc72663c"
    )
    @GET("recipes/random")
    suspend fun getRandomRecipe(): RecipeResponse
}