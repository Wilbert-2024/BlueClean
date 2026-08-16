package com.example.afinal.mapa

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseBootstrap {
    const val DATABASE_URL = "https://ubicacion-a4f61-default-rtdb.firebaseio.com"

    private const val FIREBASE_APP_ID = "1:990833293006:android:6710876df704e6989d829f"
    private const val FIREBASE_API_KEY = "AIzaSyDct9LJPAXfMdpHDH2fPl7fXnCNT4S2erQ"
    private const val FIREBASE_PROJECT_ID = "ubicacion-a4f61"

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
