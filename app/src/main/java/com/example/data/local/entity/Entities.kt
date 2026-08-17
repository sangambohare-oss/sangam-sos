package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String,
    val phone: String,
    val whatsappNumber: String,
    val priority: Int = 1
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentCode: String,
    val userName: String,
    val userPhone: String,
    val status: String, // ACTIVE, ACKNOWLEDGED, RESPONDING, RESOLVED, CANCELLED
    val activatedAt: Long,
    val acknowledgedAt: Long? = null,
    val respondingAt: Long? = null,
    val resolvedAt: Long? = null,
    val currentLatitude: Double,
    val currentLongitude: Double,
    val accuracy: Float,
    val speed: Float,
    val battery: Int,
    val trackingToken: String,
    val addressName: String,
    val responderNotes: String = ""
)

@Entity(tableName = "location_breadcrumbs")
data class LocationBreadcrumbEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentCode: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float,
    val battery: Int,
    val timestamp: Long,
    val address: String
)

@Entity(tableName = "safe_places")
data class SafePlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // POLICE, HOSPITAL, FIRE, WOMEN_HELP
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phone: String,
    val area: String
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val name: String = "Chaitali Damodar",
    val phone: String = "+91 98765 43210",
    val email: String = "fs22ai030chaitalidamodar@gmail.com",
    val bloodGroup: String = "O+",
    val medicalNotes: String = "No known allergies",
    val powerButtonTriggerEnabled: Boolean = true,
    val countdownSeconds: Int = 3,
    val userRole: String = "UNASSIGNED", // UNASSIGNED, PARENT, CHILD
    val googleId: String = "g-user-478106",
    val googlePhotoUrl: String? = null,
    val isGoogleSignedIn: Boolean = true,
    val activePairingCode: String = "NAG-9284",
    val isPaired: Boolean = false,
    val pairedPartnerName: String = "Rajesh Damodar (Parent)",
    val pairedPartnerPhone: String = "+91 98230 11223",
    val pairedPartnerEmail: String = "rajesh.damodar@gmail.com"
)
