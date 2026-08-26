# Implementación de Alarmas y Notificaciones Reales

Este plan detalla los pasos para convertir el diálogo de configuración de recordatorios en un sistema funcional que programe alarmas en Android y muestre notificaciones al usuario.

## User Review Required

> [!IMPORTANT]
> El usuario deberá conceder permiso de notificaciones cuando la app se lo solicite al iniciar.
> En versiones recientes de Android (13+), si no se acepta el permiso, las alarmas se programarán pero la notificación no será visible.

## Proposed Changes

### [Sistema Base] Configuración y Permisos

#### [MODIFY] [AndroidManifest.xml](file:///C:/zAPKkotli/final/app/src/main/AndroidManifest.xml)
- Agregar permisos: `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`.
- Registrar el `AlarmReceiver` para capturar los eventos del sistema.

#### [NEW] [AlarmReceiver.kt](file:///C:/zAPKkotli/final/app/src/main/java/com/example/afinal/servicios/AlarmReceiver.kt)
- Implementar el receptor que procesa la alarma y dispara la notificación visual usando `NotificationCompat`.

#### [NEW] [GestionAlarmas.kt](file:///C:/zAPKkotli/final/app/src/main/java/com/example/afinal/servicios/GestionAlarmas.kt)
- Lógica para calcular el `Calendar` exacto de la alarma.
- Uso de `AlarmManager` para programar la ejecución.
- Persistencia de la configuración en `SharedPreferences` (reutilizando la lógica de `datosEnMemoria`).

### [Interfaz y Ciclo de Vida]

#### [MODIFY] [MainActivity.kt](file:///C:/zAPKkotli/final/app/src/main/java/com/example/afinal/MainActivity.kt)
- Crear el canal de notificaciones en `onCreate`.
- Implementar la solicitud de permisos de notificaciones para Android 13+.

#### [MODIFY] [Alarma.kt](file:///C:/zAPKkotli/final/app/src/main/java/com/example/afinal/Alarma.kt)
- Vincular el botón "Guardar recordatorios" con `GestionAlarmas.programarRecordatorios`.

## Verification Plan

### Automated Tests
- No se proponen tests automáticos en esta fase.

### Manual Verification
1. Abrir el diálogo de configuración.
2. Cambiar la hora a una cercana (ej. 2 minutos después del tiempo actual).
3. Guardar.
4. Cerrar la app o bloquear el teléfono.
5. Verificar que la notificación aparezca puntualmente con sonido/vibración.
