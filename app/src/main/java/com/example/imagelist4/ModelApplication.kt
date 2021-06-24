package com.example.imagelist4

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ModelApplication: Application()  {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { ModelRoomDatabase.getDatabase(this , applicationScope) }
    val repository by lazy { Repository(database.modelDao()) }

}