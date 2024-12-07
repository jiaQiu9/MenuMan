package com.example.menuman
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import retrofit2.HttpException

class QuoteViewModel : ViewModel(){

    // Store the fetched quote
    private val _quote = mutableStateOf<String>("Loading...")
    val quote: State<String> get() = _quote

    private val _author = mutableStateOf<String>("Loading author ... ")
    val author: State<String> get() = _author

    private val _category = mutableStateOf<String>("Loading Category ... ")
    val category: State<String> get()= _category

    fun fetchRandomQuote() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.apiService.getRandomQuote()

                if (response.isNotEmpty()) {
                    val fetchedQuote = response[0]
                    _quote.value = "Quote: \"${fetchedQuote.quote}\""
                    _author.value= "Author: ${fetchedQuote.author}"
                    _category.value= "Category: ${fetchedQuote.category}"
                } else {
                    _quote.value = "No quote found"
                }
            } catch (e: HttpException) {
                _quote.value = "HTTP Error: ${e.message()}"
            } catch (e: Exception) {
                _quote.value = "Failed to fetch quote: ${e.message}"
            }
        }
    }

}