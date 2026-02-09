package com.example.autopark

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AutoParkApplication : Application() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        
        // Initialize sample data in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                com.example.autopark.data.SampleDataInitializer.initializeSampleData(firestore)
            } catch (e: Exception) {
                // Log error but don't crash app
                android.util.Log.e("AutoParkApp", "Error initializing sample data", e)
            }
        }
    }
}