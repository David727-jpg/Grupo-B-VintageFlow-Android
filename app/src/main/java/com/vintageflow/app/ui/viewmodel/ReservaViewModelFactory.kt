package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vintageflow.app.data.repository.ReservaRepository

class ReservaViewModelFactory(
    private val repository: ReservaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReservaViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
