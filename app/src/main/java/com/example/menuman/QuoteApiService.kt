package com.example.menuman
import retrofit2.http.GET
import retrofit2.http.Headers

interface QuoteApiService {
    @Headers(
        "x-rapidapi-key: ce618b884fmsh57af0c27908b4c5p18e0a4jsn2384fc72663c",  // Replace YOUR_API_KEY with your actual RapidAPI key
        "x-rapidapi-host: quotes-by-api-ninjas.p.rapidapi.com"
    )
    @GET("v1/quotes")
    suspend fun getRandomQuote(): List<Quote>
}