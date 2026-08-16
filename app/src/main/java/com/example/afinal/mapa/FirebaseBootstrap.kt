package com.example.afinal.mapa

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseBootstrap {
    const val DATABASE_URL = "REEMPLAZA_CON_TU_FIREBASE_DATABASE_URL"

    private const val FIREBASE_APP_ID = "REEMPLAZA_CON_TU_FIREBASE_APP_ID"
    private const val FIREBASE_API_KEY = "REEMPLAZA_CON_TU_FIREBASE_API_KEY"
    private const val FIREBASE_PROJECT_ID = "REEMPLAZA_CON_TU_FIREBASE_PROJECT_ID"

    fun ensureInitialized(context: Context) {
        val appContext = context.applicationContext
        if (FirebaseApp.getApps(appContext).any { it.name == FirebaseApp.DEFAULT_APP_NAME }) {
            return
        }

        val options = FirebaseOptions.Builder()
            .setApplicationId(FIREBASE_APP_ID)
            .setApiKey(FIREBASE_API_KEY)
            .setDatabaseUrl(DATABASE_URL)
            .setProjectId(FIREBASE_PROJECT_ID)
            .build()

        FirebaseApp.initializeApp(appContext, options)
    }
}
