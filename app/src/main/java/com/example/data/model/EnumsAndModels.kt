package com.example.data.model

enum class IncidentStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESPONDING,
    RESOLVED,
    CANCELLED
}

enum class PlaceCategory {
    POLICE,
    HOSPITAL,
    FIRE,
    WOMEN_HELP
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 8f,
    val speed: Float = 0f,
    val battery: Int = 85,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = "Dharampeth, Nagpur"
)
