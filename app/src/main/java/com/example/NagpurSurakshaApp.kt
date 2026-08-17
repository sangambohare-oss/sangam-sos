package com.example

import android.app.Application
import com.example.data.local.NagpurSurakshaDatabase
import com.example.data.repository.SosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NagpurSurakshaApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy {
        NagpurSurakshaDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        SosRepository(this, database, applicationScope)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NagpurSurakshaApp
            private set
    }
}
