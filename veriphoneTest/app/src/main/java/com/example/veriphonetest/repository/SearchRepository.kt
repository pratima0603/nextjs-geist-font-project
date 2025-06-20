package com.example.veriphonetest.repository

import com.example.veriphonetest.model.Product
import com.example.veriphonetest.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun searchProducts(query: String): Result<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchProducts(query)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
