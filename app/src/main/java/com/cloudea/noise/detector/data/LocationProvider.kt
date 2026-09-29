package com.cloudea.noise.detector.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val cityLabel: String,
)

/** 用框架 LocationManager + Geocoder，不引入 Google Play Services */
class LocationProvider(private val context: Context) {

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun current(): LocationInfo? {
        if (!hasPermission()) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val location = withTimeoutOrNull(12_000) { locate(lm) } ?: return null
        val city = reverseGeocode(location.latitude, location.longitude)
        return LocationInfo(location.latitude, location.longitude, city)
    }

    @SuppressLint("MissingPermission")
    private suspend fun locate(lm: LocationManager): Location? {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return null

        // 先用新鲜的「最后已知位置」秒回，避免干等
        val now = System.currentTimeMillis()
        for (p in providers) {
            val cached = runCatching { lm.getLastKnownLocation(p) }.getOrNull()
            if (cached != null && now - cached.time < 10 * 60 * 1000) return cached
        }

        val provider = providers.first()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            suspendCancellableCoroutine { cont ->
                lm.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
            }
        } else {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        runCatching { lm.removeUpdates(this) }
                        if (cont.isActive) cont.resume(location)
                    }

                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
            }
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        runCatching {
            if (!Geocoder.isPresent()) return@runCatching ""
            val geocoder = Geocoder(context, Locale.getDefault())
            val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (cont.isActive) cont.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            if (cont.isActive) cont.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
            }
            address?.let { it.locality ?: it.subAdminArea ?: it.adminArea ?: "" } ?: ""
        }.getOrDefault("")
    }

    companion object {
        fun formatCoord(lat: Double, lng: Double): String {
            val ns = if (lat >= 0) "N" else "S"
            val ew = if (lng >= 0) "E" else "W"
            return "%.1f°%s %.1f°%s".format(Locale.US, abs(lat), ns, abs(lng), ew)
        }

        fun displayLabel(info: LocationInfo?): String {
            if (info == null) return "定位不可用"
            val coord = formatCoord(info.latitude, info.longitude)
            return if (info.cityLabel.isBlank()) coord else "${info.cityLabel} · $coord"
        }
    }
}
