package com.example.menuman

// data structure for the recipe api response,
// it is a list because the response is returning a list of reicpes
data class RecipeResponse(
    val recipes: List<Recipe>
)
