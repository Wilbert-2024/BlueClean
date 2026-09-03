package com.example.afinal.servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

object GestorNotificacionesLlegada {
    private const val TAG = "GestorNotivLlegada"
    private var notificacion10MinEnviada = false
    private var notificacion5MinEnviada = false

    fun reiniciarSesion() {
        notificacion10MinEnviada = false
        notificacion5MinEnviada = false
    }

    fun verificarYEnviar(context: Context?, minutos: Int) {
        if (context == null) return
        Log.d(TAG, "Verificando notificaciones de llegada -> Minutos: $minutos | 10m: $notificacion10MinEnviada | 5m: $notificacion5MinEnviada")
        if (minutos <= 0) return

        val appContext = context.applicationContext

        if (minutos <= 5 && !notificacion5MinEnviada) {
            notificacion5MinEnviada = true
            notificacion10MinEnviada = true
            disparar(appContext, "¡Camión muy cerca!", "El camión recolector está a 5 minutos o menos de tu ubicación.")
        } else if (minutos <= 10 && !notificacion10MinEnviada) {
            notificacion10MinEnviada = true
            disparar(appContext, "¡Camión cerca!", "El camión recolector está a 10 minutos o menos de tu ubicación.")
        }
    }

    private fun disparar(context: Context, titulo: String, mensaje: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "canal_recoleccion_v3"
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val channel = NotificationChannel(channelId, "Recordatorios de Recolección", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Avisos importantes con sonido de alarma"
                    enableLights(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                    setSound(soundUri, null)
                }
                notificationManager.createNotificationChannel(channel)
            }

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

            val notifId = System.currentTimeMillis().toInt()
            notificationManager.notify(notifId, notification)
            Log.d(TAG, "¡Notificación push del sistema enviada con éxito! ID: $notifId")
        } catch (e: Exception) {
            Log.e(TAG, "Error crítico al enviar notificación push: ${e.message}", e)
        }
    }
}
