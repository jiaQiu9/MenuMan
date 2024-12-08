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

    // Fetch a random quote from Firebase Firestore (Offline)
    fun fetchQuoteFromFirebase() {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()

            try {
                // Switch to IO thread for Firebase fetch
                val documents = withContext(Dispatchers.IO) {
                    db.collection("quotes").get().await() // Using await() to get the result
                }

                if (documents.isEmpty) {
                    _quote.value = "No quote found in Firebase"
                } else {
                    // Convert documents to a list and select a random quote
                    val quotesList = documents.toObjects(Quote::class.java)
                    if (quotesList.isNotEmpty()) {
                        val randomIndex = Random.nextInt(quotesList.size)
                        val randomQuote = quotesList[randomIndex]

                        _quote.value = "Quote: \"${randomQuote.quote}\""
                        _name.value = "Name: ${randomQuote.name}"
                        _category.value = "Category: ${randomQuote.category}"
                    } else {
                        _quote.value = "No quote found in Firebase"
                    }
                }
            } catch (e: Exception) {
                _quote.value = "Error: ${e.message}"
                Log.e("FirestoreFetch", "Error fetching quotes: ${e.message}")
            }
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
