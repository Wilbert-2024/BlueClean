package com.example.afinal.DB.repositorio

import com.example.afinal.DB.conexionBD.Conexion
import com.example.afinal.DB.modal.PuntoReferencia_Modal

object PuntoReferencia_Repositorio {

    private val Coleccion = "Puntos_referencia"

    fun agregar(barrio: String, datos: PuntoReferencia_Modal.Datos) {
        Conexion.db.collection(Coleccion).document(barrio).set(datos)
    }

    fun obtenerTodo(onSuccess: (Map<String, List<PuntoReferencia_Modal.Lugar>>) -> Unit, onError: ((Exception) -> Unit)? = null){
        Conexion.db.collection(Coleccion).get()
            .addOnSuccessListener { resultadodetodo ->
                val mapaBarrio = resultadodetodo.documents.associate { doc ->
                    val datos = doc.toObject(PuntoReferencia_Modal.Datos::class.java)
                    doc.id to (datos?.Lugares?: emptyList())
                }
                onSuccess(mapaBarrio)
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }


    fun obtenerNombreDeZona(barrio: String, onSuccess: (List<String>) -> Unit){
        Conexion.db.collection(Coleccion).document(barrio).get()
            .addOnSuccessListener { doc ->
                val datos = doc.toObject(PuntoReferencia_Modal.Datos::class.java)
                val nombres = datos?.Lugares?.map { it.Nombre }?: emptyList()
                onSuccess(nombres)
            }
            .addOnFailureListener { onSuccess(emptyList()) }
    }

    // Nueva: Obtiene los objetos Lugar completos (Nombre + ruta_id) para un barrio
    fun obtenerLugaresPorBarrio(barrio: String, onSuccess: (List<PuntoReferencia_Modal.Lugar>) -> Unit){
        Conexion.db.collection(Coleccion).document(barrio).get()
            .addOnSuccessListener { doc ->
                val datos = doc.toObject(PuntoReferencia_Modal.Datos::class.java)
                onSuccess(datos?.Lugares ?: emptyList())
            }
            .addOnFailureListener { onSuccess(emptyList()) }
    }


    fun obtenerRutaPorBarrio(barrio: String, onSuccess: (List<String>) -> Unit, onError: ((Exception) -> Unit)? = null){
        Conexion.db.collection(Coleccion).document(barrio).get()
            .addOnSuccessListener { documento ->

                if (documento.exists()){
                    val datos = documento.toObject(PuntoReferencia_Modal.Datos::class.java)

                    val rutaId = datos?.Lugares?.map {it.ruta_id}  ?.filter { it.isNotBlank() } ?.distinct()?: emptyList()
                    onSuccess(rutaId)

                } else {onSuccess(emptyList())}
            }
            .addOnFailureListener { exception ->
                onError?.invoke(exception)
            }
    }

    fun obtenerBarriosYcoordenadas(barrio: String, rutaId: String, onSuccess: (List<PuntoReferencia_Modal.Lugar>) -> Unit){
        Conexion.db.collection(Coleccion).document(barrio).get()
            .addOnSuccessListener { doc ->
                val datos = doc.toObject(PuntoReferencia_Modal.Datos::class.java)
                val lugaresCoordenadas = datos?.Lugares?.filter {
                    it.ruta_id.equals(rutaId, ignoreCase = true)
                } ?: emptyList()
                onSuccess(lugaresCoordenadas)
            }
            .addOnFailureListener { onSuccess(emptyList()) }
    }

    // Nueva: Busca en TODOS los barrios aquellos lugares que pertenezcan a una ruta_id específica
    fun obtenerTodosLosLugaresDeUnaRuta(rutaId: String, onSuccess: (List<PuntoReferencia_Modal.Lugar>) -> Unit){
        Conexion.db.collection(Coleccion).get()
            .addOnSuccessListener { result ->
                val todosLosLugares = mutableListOf<PuntoReferencia_Modal.Lugar>()
                for (doc in result.documents) {
                    val datos = doc.toObject(PuntoReferencia_Modal.Datos::class.java)
                    val filtrados = datos?.Lugares?.filter { it.ruta_id.equals(rutaId, ignoreCase = true) }
                    if (filtrados != null) todosLosLugares.addAll(filtrados)
                }
                onSuccess(todosLosLugares)
            }
            .addOnFailureListener { onSuccess(emptyList()) }
    }







}