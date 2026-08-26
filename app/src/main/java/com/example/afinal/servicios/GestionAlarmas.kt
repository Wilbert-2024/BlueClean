package com.example.afinal.servicios

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object GestionAlarmas {
    private const val PREFS_NAME = "config_alarmas"

    fun programarRecordatorios(
        context: Context,
        activo: Boolean,
        horaNoche: String,
        nocheActiva: Boolean,
        horaDia: String,
        diaActivo: Boolean
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putBoolean("activo", activo)
        prefs.putString("horaNoche", horaNoche)
        prefs.putBoolean("nocheActiva", nocheActiva)
        prefs.putString("horaDia", horaDia)
        prefs.putBoolean("diaActivo", diaActivo)
        prefs.apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelarAlarmas(context)

        if (!activo) return

        if (nocheActiva) {
            programar(context, alarmManager, 101, horaNoche, "Recordatorio: Noche anterior", "Prepara tus bolsas para mañana.")
        }

        if (diaActivo) {
            programar(context, alarmManager, 102, horaDia, "Recordatorio: Día de recolección", "El camión pasará pronto. ¡Saca la basura!")
        }
    }

    private fun programar(context: Context, am: AlarmManager, id: Int, horaStr: String, titulo: String, mensaje: String) {
        val partes = horaStr.split(":")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, partes[0].toInt())
            set(Calendar.MINUTE, partes[1].toInt())
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) { add(Calendar.DATE, 1) }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("titulo", titulo)
            putExtra("mensaje", mensaje)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelarAlarmas(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        listOf(101, 102).forEach { id ->
            val pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pi != null) am.cancel(pi)
        }
    }
}
