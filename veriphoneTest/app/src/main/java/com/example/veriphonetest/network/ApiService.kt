package com.example.veriphonetest.network

import com.example.veriphonetest.model.Product
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("products/search")
    suspend fun searchProducts(
        @Query("query") query: String
    ): List<Product>
}
