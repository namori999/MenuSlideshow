package com.example.imagelist4

import androidx.lifecycle.*
import kotlinx.coroutines.launch


open class ModelViewModel(private val repository: Repository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.


    val allWords: LiveData<List<Model>> = repository.allWords.asLiveData()



    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(model: Model) = viewModelScope.launch {
        repository.insert(model)

    }

    fun delete(model: Model) = viewModelScope.launch {
        repository.delete(model)

    }

    fun update(model:Model) = viewModelScope.launch {
        repository.update(model)
    }






    class ModelViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ModelViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ModelViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}