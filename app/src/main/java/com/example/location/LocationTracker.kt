package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.example.data.model.LocationPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocationTracker(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _currentLocation = MutableStateFlow(
        LocationPoint(
            latitude = 21.1415,
            longitude = 79.0620,
            accuracy = 6f,
            speed = 0f,
            battery = getDeviceBatteryPercentage(),
            timestamp = System.currentTimeMillis(),
            address = "Dharampeth, Nagpur"
        )
    )
    val currentLocation: StateFlow<LocationPoint> = _currentLocation.asStateFlow()

    private var androidLocationListener: LocationListener? = null
    private var simulationJob: Job? = null
    private var isSimulating: Boolean = false

    // Nagpur Waypoints for realistic emergency movement simulation
    private val nagpurWaypoints = listOf(
        Pair(21.1415, 79.0620) to "West High Court Road, Dharampeth",
        Pair(21.1430, 79.0655) to "Coffee House Square, Dharampeth",
        Pair(21.1442, 79.0698) to "Law College Square, Amravati Road",
        Pair(21.1450, 79.0740) to "Bole Petrol Pump, VIP Road",
        Pair(21.1448, 79.0835) to "Sitabuldi Interchange, Nagpur",
        Pair(21.1495, 79.0710) to "Civil Lines, Near High Court",
        Pair(21.1530, 79.0860) to "Kingsway Station Road, Nagpur"
    )
    private var currentWaypointIndex = 0

    @SuppressLint("MissingPermission")
    fun startTracking(coroutineScope: CoroutineScope, useSimulation: Boolean = false) {
        isSimulating = useSimulation
        if (useSimulation) {
            startSimulation(coroutineScope)
            return
        }

        try {
            if (locationManager == null) {
                startSimulation(coroutineScope)
                return
            }

            androidLocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    updateLocationFromAndroid(location)
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            var registered = false
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    try {
                        locationManager.requestLocationUpdates(
                            provider,
                            3000L,
                            2f,
                            androidLocationListener!!,
                            Looper.getMainLooper()
                        )
                        registered = true
                        val lastKnown = locationManager.getLastKnownLocation(provider)
                        if (lastKnown != null) {
                            updateLocationFromAndroid(lastKnown)
                        }
                    } catch (e: SecurityException) {
                        Log.d("LocationTracker", "Permission not granted for $provider")
                    } catch (t: Throwable) {
                        Log.d("LocationTracker", "Provider $provider update failure")
                    }
                }
            }

            if (!registered) {
                startSimulation(coroutineScope)
            }
        } catch (t: Throwable) {
            startSimulation(coroutineScope)
        }
    }

    private fun updateLocationFromAndroid(location: Location) {
        val battery = getDeviceBatteryPercentage()
        _currentLocation.value = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = if (location.hasAccuracy()) location.accuracy else 8f,
            speed = if (location.hasSpeed()) location.speed * 3.6f else 0f, // km/h
            battery = battery,
            timestamp = System.currentTimeMillis(),
            address = getApproximateNagpurArea(location.latitude, location.longitude)
        )
    }

    fun startSimulation(scope: CoroutineScope) {
        simulationJob?.cancel()
        simulationJob = scope.launch(Dispatchers.Default) {
            var battery = getDeviceBatteryPercentage()
            while (isActive) {
                val (coord, address) = nagpurWaypoints[currentWaypointIndex % nagpurWaypoints.size]
                // Add micro-jitter for live GPS realism
                val jitterLat = coord.first + ((Math.random() - 0.5) * 0.0003)
                val jitterLng = coord.second + ((Math.random() - 0.5) * 0.0003)
                val simulatedSpeed = (12f + (Math.random() * 8f)).toFloat()

                _currentLocation.value = LocationPoint(
                    latitude = jitterLat,
                    longitude = jitterLng,
                    accuracy = (5f + (Math.random() * 4f)).toFloat(),
                    speed = simulatedSpeed,
                    battery = battery,
                    timestamp = System.currentTimeMillis(),
                    address = address
                )

                delay(4000L)
                currentWaypointIndex++
                if (currentWaypointIndex % 5 == 0 && battery > 10) {
                    battery -= 1 // Slow battery drain simulation
                }
            }
        }
    }

    fun stopTracking() {
        androidLocationListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (ignored: Throwable) {}
            androidLocationListener = null
        }
        simulationJob?.cancel()
        simulationJob = null
    }

    fun getDeviceBatteryPercentage(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 78
        return if (level in 1..100) level else 78
    }

    private fun getApproximateNagpurArea(lat: Double, lng: Double): String {
        return when {
            lat in 21.135..21.148 && lng in 79.055..79.070 -> "Dharampeth, Nagpur"
            lat in 21.140..21.150 && lng in 79.075..79.090 -> "Sitabuldi, Nagpur"
            lat in 21.145..21.160 && lng in 79.065..79.080 -> "Civil Lines, Nagpur"
            lat in 21.155..21.170 && lng in 79.075..79.090 -> "Sadar, Nagpur"
            lat in 21.120..21.135 && lng in 79.045..79.065 -> "Ambazari / VNIT, Nagpur"
            lat in 21.090..21.120 && lng in 79.050..79.090 -> "Wardha Road / Khamla, Nagpur"
            lat in 21.175..21.200 && lng in 79.070..79.100 -> "Mankapur, Nagpur"
            else -> "Nagpur Metro Area (Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)})"
        }
    }
}
