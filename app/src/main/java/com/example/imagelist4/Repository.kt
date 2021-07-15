package com.example.imagelist4

import android.os.AsyncTask
import android.view.Display
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.*


// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class Repository(private val modelDao: ModelDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    val allWords: Flow<List<Model>> = modelDao.getAll()



    private var mAsyncTaskDao: ModelDao? = null




    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(model: Model) {
        modelDao.insert(model)
    }

    fun delete(model: Model) {
        deleteWordAsyncTask(modelDao).execute(model)
    }

    suspend fun update(model:Model){
        modelDao.update(model)
    }

    suspend fun deleteAll() {
        modelDao.deleteAll()
    }



    private class deleteWordAsyncTask internal constructor(dao: ModelDao) :
        AsyncTask<Model?, Void?, Void?>() {
        private val mAsyncTaskDao: ModelDao


        fun deleteModel(word: Model?) {
            deleteWordAsyncTask(mAsyncTaskDao).execute(word)
        }

        override fun doInBackground(vararg params: Model?): Void? {
            params[0]?.let { mAsyncTaskDao.deleteModel(it) }
            return null
        }


        init {
            mAsyncTaskDao = dao
        }


    }



}
