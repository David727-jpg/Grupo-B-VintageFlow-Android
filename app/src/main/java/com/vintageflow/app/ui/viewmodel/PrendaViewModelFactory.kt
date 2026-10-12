package com.vintageflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vintageflow.app.data.repository.PrendaRepository

class PrendaViewModelFactory(
    private val repository: PrendaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(PrendaViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return PrendaViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido"
        )
    }
}