package com.example.menuman

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.random.Random

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

    private val _dbingredients = mutableStateOf("Loading dbingredients")
    val dbingredients:State<String> get() = _dbingredients


    fun fetchRecipeFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Recipes")
            .get()
            .addOnSuccessListener { result ->
                // check result for debugging
                //holy shit this was annoying
                Log.d("Firestore", "Number of documents: ${result.size()}")

                if (result.isEmpty) {
                    _title.value = "No title found in Firebase"
                } else {
                    // Convert values of fields to a list
                    val titlesList =result.toObjects(Dbrecipe::class.java)
                    //print out the amount of titles
                    Log.d("Firestore", "titles: $titlesList")

                    if (titlesList.isNotEmpty()) {
                        //get a random integer from the size of the list
                        val randomIndex = Random.nextInt(titlesList.size)
                        //common sense
                        val randomtitle = titlesList[randomIndex]
                        //assign the field values into the title state of each
                        _title.value = "title: \"${randomtitle.title}\""
                        _instructions.value = "instructions: ${randomtitle.instructions}"
                        _dbingredients.value = "Ingredients: ${randomtitle.dbingredients}"
                        _ingredients.value = randomtitle.dbingredients.split(",").map { it.trim() }
                    } else {
                        _title.value = "no titles inside the colletion"
                    }
                }
            }
            .addOnFailureListener { e ->
                _title.value = "fuck: ${e.message}"
                Log.e("FirestoreError", "Im cooked: ${e.message}", e)
            }
    }
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
    fun fetchRecipe(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            getRandomRecipe()
        } else {
            fetchRecipeFromFirebase()
        }
    }
}


