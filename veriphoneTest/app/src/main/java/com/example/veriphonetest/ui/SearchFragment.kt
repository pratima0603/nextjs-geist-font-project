package com.example.veriphonetest.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.veriphonetest.databinding.FragmentSearchBinding
import com.example.veriphonetest.ui.adapter.ProductAdapter
import com.example.veriphonetest.viewmodels.SearchUiState
import com.example.veriphonetest.viewmodels.SearchViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private val productAdapter = ProductAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchInput()
        observeSearchResults()
    }

    private fun setupRecyclerView() {
        binding.productsRecyclerView.adapter = productAdapter
    }

    private fun setupSearchInput() {
        binding.searchEditText.apply {
            addTextChangedListener { editable ->
                viewModel.onQueryChanged(editable?.toString() ?: "")
            }
            
            // Set IME action to search
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collect { state ->
                    updateUiState(state)
                }
            }
        }
    }

    private fun updateUiState(state: SearchUiState) {
        binding.apply {
            progressBar.isVisible = state is SearchUiState.Loading
            emptyStateText.isVisible = state is SearchUiState.Empty
            productsRecyclerView.isVisible = state is SearchUiState.Success
            
            when (state) {
                is SearchUiState.Success -> {
                    productAdapter.submitList(state.products)
                }
                is SearchUiState.Error -> {
                    showError(state.message)
                }
                is SearchUiState.Empty -> {
                    emptyStateText.text = if (searchEditText.text.isNullOrEmpty()) {
                        getString(R.string.search_hint)
                    } else {
                        getString(R.string.no_results_found)
                    }
                }
                else -> Unit
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun showError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
