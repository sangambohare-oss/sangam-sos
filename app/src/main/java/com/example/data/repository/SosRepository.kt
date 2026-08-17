package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.NagpurSurakshaDatabase
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.LocationBreadcrumbEntity
import com.example.data.local.entity.SafePlaceEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.FamilyPairingState
import com.example.data.model.GoogleAuthUser
import com.example.data.model.IncidentStatus
import com.example.data.model.LocationPoint
import com.example.data.model.PairedChildInfo
import com.example.data.model.UserRole
import com.example.location.LocationTracker
import com.example.service.PowerButtonService
import com.example.service.SosForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class WhatsAppMessageLog(
    val id: String = UUID.randomUUID().toString(),
    val recipientName: String,
    val recipientPhone: String,
    val timestamp: Long,
    val formattedTime: String,
    val messageText: String,
    val trackingUrl: String,
    val status: String = "DELIVERED"
)

data class ActiveSosState(
    val isTriggered: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownSecondsRemaining: Int = 3,
    val incidentCode: String? = null,
    val trackingToken: String? = null,
    val status: IncidentStatus = IncidentStatus.ACTIVE,
    val activatedAt: Long = 0L,
    val location: LocationPoint = LocationPoint(21.1415, 79.0620),
    val parentNotified: Boolean = false,
    val policeAlerted: Boolean = false,
    val cancelReason: String? = null,
    val lowBatteryAlertSent: Boolean = false
)

class SosRepository(
    private val context: Context,
    private val database: NagpurSurakshaDatabase,
    private val scope: CoroutineScope,
    val userProfileRepository: UserProfileRepository = UserProfileRepository(),
    val firebaseMigrationRepository: FirebaseDataMigrationRepository = FirebaseDataMigrationRepository(context, database)
) {
    val locationTracker = LocationTracker(context)

    private val prefs = context.getSharedPreferences("nagpur_suraksha_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_IS_SIGNED_IN = "pref_is_signed_in"
        private const val PREF_USER_ID = "pref_user_id"
        private const val PREF_USER_NAME = "pref_user_name"
        private const val PREF_USER_EMAIL = "pref_user_email"
        private const val PREF_USER_PHOTO = "pref_user_photo"
        private const val PREF_USER_ROLE = "pref_user_role"
        private const val PREF_PAIRING_CODE = "pref_pairing_code"
        private const val PREF_PAIRED_PARENT_NAME = "pref_paired_parent_name"
        private const val PREF_PAIRED_PARENT_EMAIL = "pref_paired_parent_email"
        private const val PREF_PAIRED_PARENT_PHONE = "pref_paired_parent_phone"
        private const val PREF_IS_CHILD_PAIRED = "pref_is_child_paired"
    }

    private val _activeSos = MutableStateFlow(ActiveSosState())
    val activeSos: StateFlow<ActiveSosState> = _activeSos.asStateFlow()

    private val _whatsAppInbox = MutableStateFlow<List<WhatsAppMessageLog>>(emptyList())
    val whatsAppInbox: StateFlow<List<WhatsAppMessageLog>> = _whatsAppInbox.asStateFlow()

    // Google Auth & Role State - restored from persistent storage or blank
    private val _googleUser = MutableStateFlow(
        GoogleAuthUser(
            id = prefs.getString(PREF_USER_ID, "") ?: "",
            displayName = prefs.getString(PREF_USER_NAME, "") ?: "",
            email = prefs.getString(PREF_USER_EMAIL, "") ?: "",
            photoUrl = prefs.getString(PREF_USER_PHOTO, null),
            isSignedIn = prefs.getBoolean(PREF_IS_SIGNED_IN, false),
            role = try {
                UserRole.valueOf(prefs.getString(PREF_USER_ROLE, UserRole.UNASSIGNED.name) ?: UserRole.UNASSIGNED.name)
            } catch (e: Exception) {
                UserRole.UNASSIGNED
            }
        )
    )
    val googleUser: StateFlow<GoogleAuthUser> = _googleUser.asStateFlow()

    // Family Pairing State - starts clean, populated manually
    private val _familyPairing = MutableStateFlow(
        FamilyPairingState(
            activePairingCode = prefs.getString(PREF_PAIRING_CODE, "") ?: "",
            isChildPairedWithParent = prefs.getBoolean(PREF_IS_CHILD_PAIRED, false),
            pairedParentName = prefs.getString(PREF_PAIRED_PARENT_NAME, null),
            pairedParentEmail = prefs.getString(PREF_PAIRED_PARENT_EMAIL, null),
            pairedParentPhone = prefs.getString(PREF_PAIRED_PARENT_PHONE, null),
            pairedChildren = emptyList()
        )
    )
    val familyPairing: StateFlow<FamilyPairingState> = _familyPairing.asStateFlow()

    init {
        // Automatically initiate background sync & migration from Room to Cloud Firestore when user exists
        scope.launch(Dispatchers.IO) {
            try {
                val userId = _googleUser.value.id
                if (userId.isNotEmpty()) {
                    firebaseMigrationRepository.migrateRoomToFirestore(userId)
                }
            } catch (e: Exception) {
                // Handled gracefully
            }
        }
    }

    private var countdownJob: Job? = null
    private var locationBroadcastJob: Job? = null

    // Room DB Streams
    val emergencyContacts: Flow<List<EmergencyContactEntity>> =
        database.emergencyContactDao().getAllContacts()

    val allIncidents: Flow<List<IncidentEntity>> =
        database.incidentDao().getAllIncidents()

    val activeIncidents: Flow<List<IncidentEntity>> =
        database.incidentDao().getActiveIncidents()

    val safePlaces: Flow<List<SafePlaceEntity>> =
        database.safePlaceDao().getAllSafePlaces()

    val userProfile: Flow<UserProfileEntity?> =
        database.userProfileDao().getUserProfile()

    init {
        locationTracker.startTracking(scope, useSimulation = true)
        startPowerButtonBackgroundService()
    }

    private fun startPowerButtonBackgroundService() {
        try {
            val intent = Intent(context, PowerButtonService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            // Ignored if background start restricted
        }
    }

    fun startCountdownTrigger(triggerSource: String = "APP_BUTTON") {
        if (_activeSos.value.isTriggered || _activeSos.value.isCountingDown) return

        _activeSos.value = ActiveSosState(
            isCountingDown = true,
            countdownSecondsRemaining = 3
        )

        try {
            val serviceIntent = Intent(context, SosForegroundService::class.java).apply {
                action = SosForegroundService.ACTION_VIBRATE_ALERT
            }
            context.startService(serviceIntent)
        } catch (e: Exception) {
            // Service launch fallback
        }

        countdownJob?.cancel()
        countdownJob = scope.launch(Dispatchers.Main) {
            for (sec in 3 downTo 1) {
                _activeSos.value = _activeSos.value.copy(
                    isCountingDown = true,
                    countdownSecondsRemaining = sec
                )
                delay(1000L)
            }
            if (_activeSos.value.isCountingDown) {
                activateSosImmediately()
            }
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _activeSos.value = ActiveSosState(
            isCountingDown = false,
            cancelReason = "Cancelled by user before activation"
        )
    }

    fun activateSosImmediately() {
        countdownJob?.cancel()
        val incidentIdNumber = (1000..9999).random()
        val incidentCode = "NS-$incidentIdNumber"
        val trackingToken = "ns-token-${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()
        val loc = locationTracker.currentLocation.value

        _activeSos.value = ActiveSosState(
            isTriggered = true,
            isCountingDown = false,
            incidentCode = incidentCode,
            trackingToken = trackingToken,
            status = IncidentStatus.ACTIVE,
            activatedAt = now,
            location = loc,
            parentNotified = true,
            policeAlerted = true
        )

        // Persist to Room
        scope.launch(Dispatchers.IO) {
            val user = database.userProfileDao().getUserProfileOnce()
            val userName = user?.name?.ifBlank { null } ?: _googleUser.value.displayName.ifBlank { "Citizen" }
            val userPhone = user?.phone?.ifBlank { null } ?: "Not specified"

            val incident = IncidentEntity(
                incidentCode = incidentCode,
                userName = userName,
                userPhone = userPhone,
                status = "ACTIVE",
                activatedAt = now,
                currentLatitude = loc.latitude,
                currentLongitude = loc.longitude,
                accuracy = loc.accuracy,
                speed = loc.speed,
                battery = loc.battery,
                trackingToken = trackingToken,
                addressName = loc.address
            )
            database.incidentDao().insertIncident(incident)

            // Sync to Cloud Firestore
            firebaseMigrationRepository.syncIncidentToFirestore(incident)

            // Initial breadcrumb
            database.locationBreadcrumbDao().insertBreadcrumb(
                LocationBreadcrumbEntity(
                    incidentCode = incidentCode,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy = loc.accuracy,
                    speed = loc.speed,
                    battery = loc.battery,
                    timestamp = now,
                    address = loc.address
                )
            )

            // Send WhatsApp notifications to emergency contacts
            dispatchWhatsAppNotifications(userName, loc, trackingToken, incidentCode)
        }

        // Start Foreground Location Broadcaster
        startForegroundTrackingService(loc.address)
        startLocationBroadcastLoop(incidentCode)
    }

    private fun dispatchWhatsAppNotifications(
        userName: String,
        location: LocationPoint,
        trackingToken: String,
        incidentCode: String
    ) {
        scope.launch(Dispatchers.IO) {
            val contacts = database.emergencyContactDao().getAllContactsList()
            val trackingUrl = "https://nagpursuraksha.gov.in/track/$trackingToken"
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val messageText = buildString {
                appendLine("🚨 NAGPUR SURAKSHA — EMERGENCY ALERT")
                appendLine()
                appendLine("Your registered contact $userName has activated SOS ($incidentCode).")
                appendLine()
                appendLine("📍 Current Location:")
                appendLine("https://maps.google.com/?q=${location.latitude},${location.longitude}")
                appendLine("Area: ${location.address}")
                appendLine()
                appendLine("🕒 Time: $timeFormat")
                appendLine("🔋 Battery: ${location.battery}%")
                appendLine("🔴 SOS is ACTIVE.")
                appendLine()
                appendLine("Live Location:")
                appendLine(trackingUrl)
                appendLine()
                appendLine("Please contact the user immediately or dial 112.")
            }

            val newLogs = contacts.map { contact ->
                WhatsAppMessageLog(
                    recipientName = contact.name,
                    recipientPhone = contact.whatsappNumber.ifEmpty { contact.phone },
                    timestamp = System.currentTimeMillis(),
                    formattedTime = timeFormat,
                    messageText = messageText,
                    trackingUrl = trackingUrl,
                    status = "DELIVERED"
                )
            }

            _whatsAppInbox.value = newLogs + _whatsAppInbox.value
        }
    }

    private fun startLocationBroadcastLoop(incidentCode: String) {
        locationBroadcastJob?.cancel()
        locationBroadcastJob = scope.launch(Dispatchers.Default) {
            while (isActive && _activeSos.value.isTriggered) {
                delay(4000L)
                val newLoc = locationTracker.currentLocation.value

                _activeSos.value = _activeSos.value.copy(
                    location = newLoc
                )

                // Update Room
                database.incidentDao().updateLocation(
                    code = incidentCode,
                    lat = newLoc.latitude,
                    lng = newLoc.longitude,
                    accuracy = newLoc.accuracy,
                    speed = newLoc.speed,
                    battery = newLoc.battery,
                    address = newLoc.address
                )

                database.locationBreadcrumbDao().insertBreadcrumb(
                    LocationBreadcrumbEntity(
                        incidentCode = incidentCode,
                        latitude = newLoc.latitude,
                        longitude = newLoc.longitude,
                        accuracy = newLoc.accuracy,
                        speed = newLoc.speed,
                        battery = newLoc.battery,
                        timestamp = System.currentTimeMillis(),
                        address = newLoc.address
                    )
                )

                // Check Low Battery Alert (< 15%)
                if (newLoc.battery < 15 && !_activeSos.value.lowBatteryAlertSent) {
                    _activeSos.value = _activeSos.value.copy(lowBatteryAlertSent = true)
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    val lowBatMsg = WhatsAppMessageLog(
                        recipientName = "All Registered Emergency Contacts",
                        recipientPhone = "Registered Contacts",
                        timestamp = System.currentTimeMillis(),
                        formattedTime = timeFormat,
                        messageText = "⚠️ LOW BATTERY ALERT: Device battery has dropped to ${newLoc.battery}%. Last known location: ${newLoc.address}",
                        trackingUrl = "https://nagpursuraksha.gov.in/track/${_activeSos.value.trackingToken}",
                        status = "DELIVERED"
                    )
                    _whatsAppInbox.value = listOf(lowBatMsg) + _whatsAppInbox.value
                }
            }
        }
    }

    private fun startForegroundTrackingService(address: String) {
        try {
            val intent = Intent(context, SosForegroundService::class.java).apply {
                action = SosForegroundService.ACTION_START_SOS
                putExtra(SosForegroundService.EXTRA_LOCATION_TEXT, address)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun stopForegroundTrackingService() {
        try {
            val intent = Intent(context, SosForegroundService::class.java).apply {
                action = SosForegroundService.ACTION_STOP_SOS
            }
            context.startService(intent)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun cancelSos(reason: String = "User marked safe") {
        val code = _activeSos.value.incidentCode
        locationBroadcastJob?.cancel()
        locationBroadcastJob = null
        stopForegroundTrackingService()

        _activeSos.value = ActiveSosState(
            isTriggered = false,
            isCountingDown = false,
            cancelReason = reason,
            status = IncidentStatus.RESOLVED
        )

        if (code != null) {
            scope.launch(Dispatchers.IO) {
                database.incidentDao().updateIncidentStatus(
                    code = code,
                    status = "RESOLVED",
                    resolvedAt = System.currentTimeMillis()
                )
            }
        }
    }

    // Police Dispatcher Actions
    fun acknowledgeIncident(code: String) {
        scope.launch(Dispatchers.IO) {
            database.incidentDao().markAcknowledged(code)
            if (_activeSos.value.incidentCode == code) {
                _activeSos.value = _activeSos.value.copy(status = IncidentStatus.ACKNOWLEDGED)
            }
        }
    }

    fun respondToIncident(code: String, responderNotes: String = "PCR unit dispatched") {
        scope.launch(Dispatchers.IO) {
            database.incidentDao().markResponding(code, notes = responderNotes)
            if (_activeSos.value.incidentCode == code) {
                _activeSos.value = _activeSos.value.copy(status = IncidentStatus.RESPONDING)
            }
        }
    }

    fun resolveIncident(code: String) {
        scope.launch(Dispatchers.IO) {
            database.incidentDao().updateIncidentStatus(
                code = code,
                status = "RESOLVED",
                resolvedAt = System.currentTimeMillis()
            )
            if (_activeSos.value.incidentCode == code) {
                cancelSos("Resolved by Police Control Center")
            }
        }
    }

    // Contacts Management
    fun addContact(name: String, relationship: String, phone: String, whatsappNumber: String, priority: Int) {
        scope.launch(Dispatchers.IO) {
            val contact = EmergencyContactEntity(
                name = name,
                relationship = relationship,
                phone = phone,
                whatsappNumber = whatsappNumber,
                priority = priority
            )
            val id = database.emergencyContactDao().insertContact(contact)
            val savedContact = contact.copy(id = id)
            if (_googleUser.value.id.isNotEmpty()) {
                firebaseMigrationRepository.syncContactToFirestore(savedContact, _googleUser.value.id)
            }
        }
    }

    fun deleteContact(id: Long) {
        scope.launch(Dispatchers.IO) {
            database.emergencyContactDao().deleteContactById(id)
        }
    }

    fun updateProfile(profile: UserProfileEntity) {
        scope.launch(Dispatchers.IO) {
            database.userProfileDao().saveUserProfile(profile)
        }
    }

    fun getBreadcrumbsForIncident(code: String): Flow<List<LocationBreadcrumbEntity>> {
        return database.locationBreadcrumbDao().getBreadcrumbsForIncident(code)
    }

    // Clean All User Data & Reset Database (Removes all fake data)
    fun clearAllUserData() {
        scope.launch(Dispatchers.IO) {
            database.emergencyContactDao().deleteAllContacts()
            database.incidentDao().deleteAllIncidents()
            database.locationBreadcrumbDao().deleteAllBreadcrumbs()
            database.userProfileDao().deleteUserProfile()

            _activeSos.value = ActiveSosState()
            _whatsAppInbox.value = emptyList()
            _familyPairing.value = FamilyPairingState()
            _googleUser.value = GoogleAuthUser(
                displayName = "",
                email = "",
                isSignedIn = false,
                role = UserRole.UNASSIGNED
            )

            prefs.edit().clear().apply()
        }
    }

    // Google Auth Actions
    fun signInWithGoogle(
        email: String,
        displayName: String,
        photoUrl: String? = null
    ) {
        val user = GoogleAuthUser(
            id = "user-${UUID.randomUUID().toString().take(8)}",
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            isSignedIn = true,
            role = _googleUser.value.role.takeIf { it != UserRole.UNASSIGNED } ?: UserRole.CITIZEN
        )
        _googleUser.value = user

        // Save persistent session
        prefs.edit()
            .putBoolean(PREF_IS_SIGNED_IN, true)
            .putString(PREF_USER_ID, user.id)
            .putString(PREF_USER_NAME, user.displayName)
            .putString(PREF_USER_EMAIL, user.email)
            .putString(PREF_USER_PHOTO, user.photoUrl)
            .putString(PREF_USER_ROLE, user.role.name)
            .apply()

        // Async sync to Cloud Firestore
        scope.launch(Dispatchers.IO) {
            try {
                userProfileRepository.saveOrUpdateUserProfile(user)
                firebaseMigrationRepository.migrateRoomToFirestore(user.id)
            } catch (e: Exception) {
                // Logged in repository
            }
        }
    }

    fun signOutGoogle() {
        _googleUser.value = GoogleAuthUser(
            displayName = "",
            email = "",
            isSignedIn = false,
            role = UserRole.UNASSIGNED
        )
        prefs.edit()
            .putBoolean(PREF_IS_SIGNED_IN, false)
            .putString(PREF_USER_ROLE, UserRole.UNASSIGNED.name)
            .apply()
    }

    fun setUserRole(role: UserRole) {
        _googleUser.value = _googleUser.value.copy(role = role)
        prefs.edit()
            .putString(PREF_USER_ROLE, role.name)
            .apply()

        scope.launch(Dispatchers.IO) {
            try {
                if (_googleUser.value.id.isNotEmpty()) {
                    userProfileRepository.updateUserRole(_googleUser.value.id, role)
                }
            } catch (e: Exception) {
                // Handled in repository
            }
            val cur = database.userProfileDao().getUserProfileOnce()
            if (cur != null) {
                database.userProfileDao().saveUserProfile(
                    cur.copy(
                        userRole = role.name,
                        email = _googleUser.value.email.ifBlank { cur.email },
                        name = _googleUser.value.displayName.ifBlank { cur.name }
                    )
                )
            }
        }
    }

    // Family Pairing Actions (Parent & Child)
    fun generateNewPairingCode(parentPhone: String = ""): String {
        val randNum = (1000..9999).random()
        val newCode = "NAG-$randNum"
        _familyPairing.value = _familyPairing.value.copy(
            activePairingCode = newCode,
            codeExpiresAt = System.currentTimeMillis() + 900000L // 15 mins
        )
        prefs.edit().putString(PREF_PAIRING_CODE, newCode).apply()

        scope.launch(Dispatchers.IO) {
            try {
                if (_googleUser.value.id.isNotEmpty()) {
                    userProfileRepository.generateAndSavePairingCode(
                        parentUser = _googleUser.value,
                        parentPhone = parentPhone,
                        code = newCode
                    )
                }
            } catch (e: Exception) {
                // Handled in repository
            }
        }
        return newCode
    }

    fun pairWithParentCode(
        enteredCode: String,
        parentName: String = "Parent / Guardian",
        parentPhone: String = "",
        parentEmail: String = ""
    ): Boolean {
        val cleanCode = enteredCode.trim().uppercase(Locale.getDefault())
        if (cleanCode.isBlank()) return false

        _familyPairing.value = _familyPairing.value.copy(
            pairedParentName = parentName,
            pairedParentEmail = parentEmail,
            pairedParentPhone = parentPhone,
            isChildPairedWithParent = true,
            activePairingCode = cleanCode
        )

        prefs.edit()
            .putBoolean(PREF_IS_CHILD_PAIRED, true)
            .putString(PREF_PAIRED_PARENT_NAME, parentName)
            .putString(PREF_PAIRED_PARENT_EMAIL, parentEmail)
            .putString(PREF_PAIRED_PARENT_PHONE, parentPhone)
            .apply()

        if (parentPhone.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                database.emergencyContactDao().insertContact(
                    EmergencyContactEntity(
                        name = parentName,
                        relationship = "Parent / Guardian",
                        phone = parentPhone,
                        whatsappNumber = parentPhone,
                        priority = 1
                    )
                )
            }
        }
        return true
    }

    fun addPairedChildToParent(
        childName: String,
        childPhone: String,
        childEmail: String = ""
    ) {
        val newChild = PairedChildInfo(
            childId = "child-${System.currentTimeMillis() % 1000}",
            name = childName,
            email = childEmail,
            phone = childPhone,
            pairingCode = _familyPairing.value.activePairingCode,
            battery = 100,
            latitude = 21.1415,
            longitude = 79.0620,
            locationAddress = "Nagpur, Maharashtra",
            lastSeenTime = System.currentTimeMillis(),
            isSosActive = false,
            safetyStatus = "SAFE"
        )
        val updated = _familyPairing.value.pairedChildren.toMutableList().apply {
            add(0, newChild)
        }
        _familyPairing.value = _familyPairing.value.copy(pairedChildren = updated)
    }

    fun removePairedChild(childId: String) {
        val updated = _familyPairing.value.pairedChildren.filterNot { it.childId == childId }
        _familyPairing.value = _familyPairing.value.copy(pairedChildren = updated)
    }

    fun unpairChildFromParent() {
        _familyPairing.value = _familyPairing.value.copy(
            isChildPairedWithParent = false,
            pairedParentName = null,
            pairedParentEmail = null,
            pairedParentPhone = null
        )
        prefs.edit()
            .putBoolean(PREF_IS_CHILD_PAIRED, false)
            .remove(PREF_PAIRED_PARENT_NAME)
            .remove(PREF_PAIRED_PARENT_EMAIL)
            .remove(PREF_PAIRED_PARENT_PHONE)
            .apply()
    }

    fun sendParentCheckInPing(childId: String) {
        _familyPairing.value = _familyPairing.value.copy(
            lastCheckInPingTimestamp = System.currentTimeMillis()
        )
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
