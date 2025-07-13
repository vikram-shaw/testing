package com.example.rush.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RunningViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RunningViewModel::class.java)) {
            return RunningViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}