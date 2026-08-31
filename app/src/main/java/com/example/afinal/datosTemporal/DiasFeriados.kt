package com.example.afinal.datosTemporal

object DiasFeriados {
    data class FeriadoTemporal(val dia: Int, val mes: Int, val anio: Int, val nombre: String)

    val lista = listOf(
        FeriadoTemporal(1, 1, 0, "Año Nuevo"),
        FeriadoTemporal(1, 5, 0, "Día Internacional de los Trabajadores"),
        FeriadoTemporal(19, 7, 0, "Día de la Revolución Popular Sandinista"),
        FeriadoTemporal(1, 8, 0, "Día de Santo Domingo de Guzmán (Managua)"),
        FeriadoTemporal(10, 8, 0, "Día de Santo Domingo de Guzmán (Managua)"),
        FeriadoTemporal(14, 9, 0, "Batalla de San Jacinto"),
        FeriadoTemporal(15, 9, 0, "Independencia de Nicaragua"),
        FeriadoTemporal(8, 12, 0, "Día de la Inmaculada Concepción de María"),
        FeriadoTemporal(25, 12, 0, "Navidad"),
        // Feriados variables ejemplo (Semana Santa 2026 - Referencial)
         )
}
