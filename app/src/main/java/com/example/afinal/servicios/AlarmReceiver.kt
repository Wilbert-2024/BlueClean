package com.example.afinal.servicios

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.afinal.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "¡Alarma recibida! Procesando notificación...")
        val titulo = intent.getStringExtra("titulo") ?: "Recordatorio de Recolección"
        val mensaje = intent.getStringExtra("mensaje") ?: "¡El camión recolector está cerca! No olvides sacar tus bolsas."
        
        mostrarNotificacion(context, titulo, mensaje)
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val sonidoUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        val notification = NotificationCompat.Builder(context, "canal_recoleccion_v3")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(sonidoUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
