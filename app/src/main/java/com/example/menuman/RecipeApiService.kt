package com.example.menuman

import retrofit2.http.GET
import retrofit2.http.Headers

interface RecipeApiService {
    @Headers(

        //"Accept: application/json"
        "x-rapidapi-host: spoonacular-recipe-food-nutrition-v1.p.rapidapi.com",
        "x-rapidapi-key: "
    )
    @GET("recipes/random")
    suspend fun getRandomRecipe(): RecipeResponse
}