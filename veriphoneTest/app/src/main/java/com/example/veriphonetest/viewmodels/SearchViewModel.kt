package com.example.veriphonetest.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veriphonetest.model.Product
import com.example.veriphonetest.repository.SearchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.UnknownHostException

sealed class SearchUiState {
    object Empty : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val products: List<Product>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel(
    private val repository: SearchRepository = SearchRepository()
) : ViewModel() {

    // Input: User text stream
    private val _searchQuery = MutableStateFlow("")
    
    // Output: UI state for search results
    private val _searchResults = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val searchResults: StateFlow<SearchUiState> = _searchResults.asStateFlow()

    init {
        // Process the incoming query stream with debounce
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait for 300ms of inactivity
                .distinctUntilChanged() // Only emit if the query has changed
                .onEach { query -> 
                    if (query.trim().isEmpty()) {
                        _searchResults.value = SearchUiState.Empty
                    }
                }
                .filter { it.trim().isNotEmpty() }
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = SearchUiState.Loading
                
                repository.searchProducts(query).fold(
                    onSuccess = { products ->
                        _searchResults.value = if (products.isEmpty()) {
                            SearchUiState.Empty
                        } else {
                            SearchUiState.Success(products)
                        }
                    },
                    onFailure = { error ->
                        val errorMessage = when (error) {
                            is UnknownHostException -> "No internet connection"
                            else -> error.message ?: "An unknown error occurred"
                        }
                        _searchResults.value = SearchUiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                _searchResults.value = SearchUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}
