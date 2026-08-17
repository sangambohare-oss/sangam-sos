package com.example.data.model

enum class UserRole {
    UNASSIGNED,
    PARENT,
    CHILD,
    CITIZEN
}

data class GoogleAuthUser(
    val id: String = "g-user-478106",
    val displayName: String = "Chaitali Damodar",
    val email: String = "fs22ai030chaitalidamodar@gmail.com",
    val photoUrl: String? = null,
    val isSignedIn: Boolean = true,
    val role: UserRole = UserRole.UNASSIGNED
)

data class PairedChildInfo(
    val childId: String,
    val name: String,
    val email: String,
    val phone: String,
    val pairingCode: String,
    val battery: Int = 85,
    val latitude: Double = 21.1415,
    val longitude: Double = 79.0620,
    val locationAddress: String = "Dharampeth, Nagpur",
    val lastSeenTime: Long = System.currentTimeMillis(),
    val isSosActive: Boolean = false,
    val safetyStatus: String = "SAFE" // SAFE, SOS_TRIGGERED, CHECK_IN_PENDING
)

data class FamilyPairingState(
    val activePairingCode: String = "NAG-9284",
    val codeExpiresAt: Long = System.currentTimeMillis() + 900000L, // 15 mins
    val pairedChildren: List<PairedChildInfo> = listOf(
        PairedChildInfo(
            childId = "child-01",
            name = "Aarav Sharma (Child)",
            email = "aarav.sharma@nagpur.edu",
            phone = "+91 98765 43210",
            pairingCode = "NAG-9284",
            battery = 88,
            latitude = 21.1415,
            longitude = 79.0620,
            locationAddress = "Dharampeth, Near Law College Ground, Nagpur",
            lastSeenTime = System.currentTimeMillis() - 60000L,
            isSosActive = false,
            safetyStatus = "SAFE"
        )
    ),
    val pairedParentName: String? = "Rajesh Damodar (Parent)",
    val pairedParentEmail: String? = "fs22ai030chaitalidamodar@gmail.com",
    val pairedParentPhone: String? = "+91 98230 11223",
    val isChildPairedWithParent: Boolean = true,
    val lastCheckInPingTimestamp: Long? = null
)
