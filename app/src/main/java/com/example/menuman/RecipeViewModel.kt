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
//    private val _recipe = mutableStateOf<String>("Loading...")
//    val recipe: State<String> get() = _recipe

    private var isRecipeFetched = false


    private val _title = mutableStateOf("Loading title ... ")
    val title: State<String> get() = _title

    private val _instructions = mutableStateOf("Loading instructions ... ")
    val instructions:State<String> get() = _instructions

    private val _ingredients = mutableStateOf(listOf<String>())
    val ingredients: State<List<String>> get() = _ingredients



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
                    // Format the extended ingredients
                    val ingredientsList = fetchedRecipe.extendedIngredients.map { ingredient ->
                        "-${ingredient.id}. ${ingredient.name} (${ingredient.amount} ${ingredient.unit}) ${ingredient.image}"
                    }

                    _title.value= fetchedRecipe.title
                    _instructions.value=cleanInstructions
                    _ingredients.value=ingredientsList
                } else {
                    _title.value = "No recipe found"
                    _instructions.value="No instructions found"
                    _ingredients.value= emptyList()
                }
            } catch (e: HttpException) {
                //_recipe.value = "HTTP Error: Code ${e.code()}, Message: ${e.message()}"
                _title.value="HTTP Error: ${e.message()}"
                _instructions.value="HTTP Error: ${e.message()}"
                _ingredients.value=emptyList()
            } catch (e: Exception) {
                // _recipe.value = "Failed to fetch recipe: ${e.localizedMessage}"
                _title.value="Failed to fetch recipe: ${e.localizedMessage}"
                _instructions.value="Failed to fetch recipe: ${e.localizedMessage}"
                _ingredients.value=emptyList()
            }
        }
    }
}
