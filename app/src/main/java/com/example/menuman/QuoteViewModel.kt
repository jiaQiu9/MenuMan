package com.example.menuman

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.lang.Exception
import kotlin.random.Random

class QuoteViewModel : ViewModel() {

    // Store the fetched quote
    private val _quote = mutableStateOf<String>("Loading...")
    val quote: State<String> get() = _quote

    private val _name = mutableStateOf<String>("Loading author ... ")
    val name: State<String> get() = _name

    private val _category = mutableStateOf<String>("Loading Category ... ")
    val category: State<String> get() = _category

    // Fetch offline holy shit this was annoying
    fun fetchQuoteFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Quotes")
            .get()
            .addOnSuccessListener { result ->
                // check result for debugging
                //holy shit this was annoying
                Log.d("Firestore", "Number of documents: ${result.size()}")

                if (result.isEmpty) {
                    _quote.value = "No quote found in Firebase"
                } else {
                    // Convert values of fields to a list
                    val quotesList = result.toObjects(Quote::class.java)
                    //print out the amount of quotes
                    Log.d("Firestore", "Quotes: $quotesList")

                    if (quotesList.isNotEmpty()) {
                        //get a random integer from the size of the list
                        val randomIndex = Random.nextInt(quotesList.size)
                        //common sense
                        val randomQuote = quotesList[randomIndex]
                        //assign the field values into the quote state of each
                        _quote.value = "Quote: \"${randomQuote.quote}\""
                        _name.value = "Name: ${randomQuote.name}"
                        _category.value = "Category: ${randomQuote.category}"
                    } else {
                        _quote.value = "no quotes inside the colletion"
                    }
                }
            }
            .addOnFailureListener { e ->
                _quote.value = "fuck: ${e.message}"
                Log.e("FirestoreError", "Im cooked: ${e.message}", e)
            }
    }

    // Fetch a random quote from an online API
    fun fetchRandomQuoteFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.apiService.getRandomQuote()

                if (response.isNotEmpty()) {
                    val fetchedQuote = response[0]
                    _quote.value = "Quote: \"${fetchedQuote.quote}\""
                    _name.value = "Name: ${fetchedQuote.name}"
                    _category.value = "Category: ${fetchedQuote.category}"
                } else {
                    _quote.value = "No quote found from API"
                }
            } catch (e: HttpException) {
                _quote.value = "HTTP Error: ${e.message()}"
            } catch (e: Exception) {
                _quote.value = "Failed to fetch from API: ${e.message}"
            }
        }
    }

    // Fetch quote based on network availability
    fun fetchQuote(isNetworkAvailable: Boolean) {
        if (isNetworkAvailable) {
            // Fetch from API if network is available
            fetchRandomQuoteFromApi()
        } else {
            // Fetch from Firebase if offline
            fetchQuoteFromFirebase()
        }
    }
}