package com.example.afinal.mapa

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.afinal.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManejadorMapa(private val activity: Activity) {

    companion object {
        private const val ZOOM_DEFAULT = 18f
    }

    private var rutaActiva: String? = null
    private val database: FirebaseDatabase
    private var map: GoogleMap? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private var marcadorUsuario: Marker? = null
    private var circuloPrecision: Circle? = null
    private var ultimaUsuario: LatLng? = null

    private val rutasDibujadas = mutableMapOf<String, Polyline>()
    private val marcadoresBusPorRuta = mutableMapOf<String, Marker>()
    private val iconoActualPorRuta = mutableMapOf<String, Int>()
    private val listenersFirebase = mutableMapOf<String, ValueEventListener>()

    init {
        FirebaseBootstrap.ensureInitialized(activity)
        database = FirebaseDatabase.getInstance(FirebaseBootstrap.DATABASE_URL)
    }

    fun estaMapaInicializado() = map != null

    fun setGoogleMap(googleMap: GoogleMap) {
        this.map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
    }

    fun iniciarLogicaMapa(rutaId: String, imagenTransport: Int) {
        if (map == null) return

        rutaActiva?.let { ocultarRutaYMarcadorPorId(it) }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        if (tienePermisoUbicacion()) {
            iniciarSeguimientoUsuario()
        } else {
            mostrarToastPermisoDenegado()
        }

        escucharBusEnFirebase(rutaId, imagenTransport)
        cargarRutaEnMapa(rutaId)

        rutaActiva = rutaId
    }

    fun detener() {
        if (::fusedLocationClient.isInitialized) {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        }
        locationCallback = null
        locationRequest = null

        listenersFirebase.keys.toList().forEach { rutaId ->
            val referencia = database.getReference("ubicacion/$rutaId/coordenadas")
            listenersFirebase[rutaId]?.let { referencia.removeEventListener(it) }
        }
        listenersFirebase.clear()
    }

    private fun iniciarSeguimientoUsuario() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                actualizarUbicacionUsuario(loc)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            mostrarToastPermisoDenegado()
        }
    }

    private fun actualizarUbicacionUsuario(location: Location) {
        val pos = LatLng(location.latitude, location.longitude)
        val icono = UtilidadesMapa.redimensionarIcono(R.drawable.ic_person, 72, 72, activity)

        if (marcadorUsuario == null) {
            marcadorUsuario = map?.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("Tu estas aqui")
                    .icon(icono)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
            )
        } else {
            marcadorUsuario?.position = pos
            ultimaUsuario?.let {
                val rot = UtilidadesMapa.calcularRotacion(it, pos)
                animarRotacion(marcadorUsuario!!, rot)
            }
        }

        if (circuloPrecision == null) {
            circuloPrecision = map?.addCircle(
                CircleOptions()
                    .center(pos)
                    .radius(location.accuracy.toDouble())
                    .strokeColor(0x660000FF)
                    .fillColor(0x300000FF)
            )
        } else {
            circuloPrecision?.center = pos
            circuloPrecision?.radius = location.accuracy.toDouble()
        }

        ultimaUsuario = pos
    }

    private fun escucharBusEnFirebase(rutaId: String, imagenTransport: Int) {
        val referencia = database.getReference("ubicacion/$rutaId/coordenadas")

        listenersFirebase[rutaId]?.let {
            referencia.removeEventListener(it)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitud").getValue(Double::class.java)
                val lng = snapshot.child("longitud").getValue(Double::class.java)
                if (lat != null && lng != null) {
                    val pos = LatLng(lat, lng)
                    val icono = iconoTransporte(imagenTransport)

                    val marcadorExistente = marcadoresBusPorRuta[rutaId]
                    if (marcadorExistente == null || iconoActualPorRuta[rutaId] != imagenTransport) {
                        marcadorExistente?.remove()
                        val nuevoMarcador = map?.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title("Bus en tiempo real")
                                .icon(icono)
                                .anchor(0.5f, 0.5f)
                                .flat(true)
                        )
                        if (nuevoMarcador != null) {
                            marcadoresBusPorRuta[rutaId] = nuevoMarcador
                            iconoActualPorRuta[rutaId] = imagenTransport
                        }
                    } else {
                        marcadorExistente.position = pos
                    }
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, ZOOM_DEFAULT))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Error al cargar ubicacion del bus", Toast.LENGTH_SHORT).show()
            }
        }

        referencia.addValueEventListener(listener)
        listenersFirebase[rutaId] = listener
    }

    private fun cargarRutaEnMapa(rutaId: String) {
        database.getReference("ubicacion/$rutaId/RutaCoor/Puntos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pts = mutableListOf<LatLng>()
                    for (p in snapshot.children) {
                        val lat = p.child("lat").getValue(Double::class.java)
                        val lng = p.child("lng").getValue(Double::class.java)
                        if (lat != null && lng != null) pts.add(LatLng(lat, lng))
                    }
                    if (pts.isNotEmpty()) {
                        rutasDibujadas[rutaId]?.remove()
                        val polyline = map?.addPolyline(
                            PolylineOptions()
                                .addAll(pts)
                                .color(Color.BLUE)
                                .width(7f)
                        )
                        if (polyline != null) {
                            rutasDibujadas[rutaId] = polyline
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(activity, "Error al cargar ruta", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun ocultarRutaPorId(rutaId: String) {
        rutasDibujadas[rutaId]?.remove()
        rutasDibujadas.remove(rutaId)
    }

    fun ocultarMarcadorBusPorRuta(rutaId: String) {
        marcadoresBusPorRuta[rutaId]?.remove()
        marcadoresBusPorRuta.remove(rutaId)
        iconoActualPorRuta.remove(rutaId)
    }

    fun ocultarRutaYMarcadorPorId(rutaId: String) {
        ocultarRutaPorId(rutaId)
        ocultarMarcadorBusPorRuta(rutaId)

        val referencia = database.getReference("ubicacion/$rutaId/coordenadas")
        listenersFirebase[rutaId]?.let {
            referencia.removeEventListener(it)
            listenersFirebase.remove(rutaId)
        }
    }

    private fun animarRotacion(marcador: Marker, nuevaRotacion: Float) {
        val handler = Handler(Looper.getMainLooper())
        val start = marcador.rotation
        val delta = (nuevaRotacion - start + 360) % 360
        val duracion = 300L
        val startTime = System.currentTimeMillis()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val t = (elapsed.toFloat() / duracion).coerceAtMost(1f)
                val rot = (start + delta * t) % 360
                marcador.rotation = rot
                if (t < 1f) handler.postDelayed(this, 16)
            }
        })
    }

    private fun tienePermisoUbicacion(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun mostrarToastPermisoDenegado() {
        Toast.makeText(activity, "Permiso de ubicacion no concedido", Toast.LENGTH_SHORT).show()
    }

    private fun iconoTransporte(opcion: Int) = when (opcion) {
        1 -> UtilidadesMapa.redimensionarIcono(R.drawable.ic_bus, 96, 96, activity)
        2 -> UtilidadesMapa.redimensionarIcono(R.drawable.ic_bus2, 96, 96, activity)
        else -> UtilidadesMapa.redimensionarIcono(R.drawable.ic_bus, 96, 96, activity)
    }
}
