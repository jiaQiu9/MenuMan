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

    fun fetchRandomQuote() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.apiService.getRandomQuote()

                if (response.isNotEmpty()) {
                    val fetchedQuote = response[0]
                    _quote.value = "Quote: \"${fetchedQuote.quote}\"\nAuthor: ${fetchedQuote.author}"
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