package com.skycast.weather.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 定位封装。只用 FusedLocationProvider，不引第三方库。
 */
class LocationProvider(context: Context) {

    private val appContext = context.applicationContext
    private val client by lazy {
        LocationServices.getFusedLocationProviderClient(appContext)
    }

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * 取最近一次已知位置，失败则请求一次新位置。
     * 调用前必须确认 [hasPermission]。
     */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): Result<Pair<Double, Double>> =
        suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { last ->
                    if (last != null && cont.isActive) {
                        cont.resume(Result.success(last.latitude to last.longitude))
                        return@addOnSuccessListener
                    }
                    if (!cont.isActive) return@addOnSuccessListener
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { fresh ->
                            if (!cont.isActive) return@addOnSuccessListener
                            if (fresh != null) {
                                cont.resume(Result.success(fresh.latitude to fresh.longitude))
                            } else {
                                cont.resume(
                                    Result.failure(IllegalStateException("无法获取当前位置")),
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            if (cont.isActive) cont.resume(Result.failure(e))
                        }
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resume(Result.failure(e))
                }
        }
}
