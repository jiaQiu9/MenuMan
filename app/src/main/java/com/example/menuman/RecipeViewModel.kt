package com.example.menuman

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RecipeViewModel: ViewModel() {
    private val _recipe = mutableStateOf<String>("Loading...")
    val recipe: State<String> get() = _recipe

    fun getRandomRecipe(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RecipeRetrofitInstance.apiService.getRandomRecipe()

                if (response.isNotEmpty()) {
                    val fetchedRecipe = response[0]
                    _recipe.value = "Title: \"${fetchedRecipe.title}\"\nInstructions: ${fetchedRecipe.instruction}"
                } else {
                    _recipe.value = "No recipe found"
                }
            } catch (e: HttpException) {
                _recipe.value = "HTTP Error: ${e.message()}"
            } catch (e: Exception) {
                _recipe.value = "Failed to fetch recipe: ${e.message}"
            }
        }
    }
}