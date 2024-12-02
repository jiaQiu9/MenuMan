package com.example.menuman

import android.os.Build
import android.text.Html
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RecipeViewModel : ViewModel() {
    private val _recipe = mutableStateOf<String>("Loading...")
    val recipe: State<String> get() = _recipe
    private var isRecipeFetched = false

    fun getRandomRecipe() {
        if (isRecipeFetched) return
        isRecipeFetched = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RecipeRetrofitInstance.apiService.getRandomRecipe()
                if (response.recipes.isNotEmpty()) {
                    val fetchedRecipe = response.recipes[0]
                    val cleanInstructions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(fetchedRecipe.instructions, Html.FROM_HTML_MODE_LEGACY).toString()
                    } else {
                        Html.fromHtml(fetchedRecipe.instructions).toString()
                    }
                    _recipe.value = "Title: \"${fetchedRecipe.title}\"\nInstructions: ${fetchedRecipe.instructions}"
                } else {
                    _recipe.value = "No recipe found"
                }
            } catch (e: HttpException) {
                _recipe.value = "HTTP Error: Code ${e.code()}, Message: ${e.message()}"
            } catch (e: Exception) {
                _recipe.value = "Failed to fetch recipe: ${e.localizedMessage}"
            }
        }
    }
}
