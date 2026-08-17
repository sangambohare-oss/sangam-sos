package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.LocationBreadcrumbEntity
import com.example.data.local.entity.SafePlaceEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.FamilyPairingState
import com.example.data.model.GoogleAuthUser
import com.example.data.model.LocationPoint
import com.example.data.model.UserRole
import com.example.data.repository.ActiveSosState
import com.example.data.repository.FirebaseDataMigrationRepository
import com.example.data.repository.MigrationState
import com.example.data.repository.SosRepository
import com.example.data.repository.WhatsAppMessageLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: SosRepository
) : ViewModel() {

    val firebaseMigrationRepository: FirebaseDataMigrationRepository = repository.firebaseMigrationRepository

    val activeSos: StateFlow<ActiveSosState> = repository.activeSos
    val currentLocation: StateFlow<LocationPoint> = repository.locationTracker.currentLocation
    val whatsAppInbox: StateFlow<List<WhatsAppMessageLog>> = repository.whatsAppInbox
    val googleUser: StateFlow<GoogleAuthUser> = repository.googleUser
    val familyPairing: StateFlow<FamilyPairingState> = repository.familyPairing
    val migrationState: StateFlow<MigrationState> = firebaseMigrationRepository.migrationState

    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> =
        repository.emergencyContacts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeIncidents: StateFlow<List<IncidentEntity>> =
        repository.activeIncidents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allIncidents: StateFlow<List<IncidentEntity>> =
        repository.allIncidents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val safePlaces: StateFlow<List<SafePlaceEntity>> =
        repository.safePlaces.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProfile: StateFlow<UserProfileEntity?> =
        repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Hardware Simulation State (Power Button 3x)
    private val _powerClickCount = MutableStateFlow(0)
    val powerClickCount: StateFlow<Int> = _powerClickCount.asStateFlow()

    // Police Dashboard Selected Incident
    private val _selectedIncident = MutableStateFlow<IncidentEntity?>(null)
    val selectedIncident: StateFlow<IncidentEntity?> = _selectedIncident.asStateFlow()

    // Search filter in Safe Places
    private val _safePlaceSearchQuery = MutableStateFlow("")
    val safePlaceSearchQuery: StateFlow<String> = _safePlaceSearchQuery.asStateFlow()

    private val _selectedPlaceCategory = MutableStateFlow("ALL")
    val selectedPlaceCategory: StateFlow<String> = _selectedPlaceCategory.asStateFlow()

    fun setSafePlaceFilter(query: String, category: String) {
        _safePlaceSearchQuery.value = query
        _selectedPlaceCategory.value = category
    }

    fun triggerSosCountdown(source: String = "MANUAL_TAP") {
        repository.startCountdownTrigger(source)
    }

    fun cancelSosCountdown() {
        repository.cancelCountdown()
    }

    fun triggerSosImmediate() {
        repository.activateSosImmediately()
    }

    fun cancelActiveSos(reason: String = "User safe") {
        repository.cancelSos(reason)
    }

    fun acknowledgeIncident(code: String) {
        repository.acknowledgeIncident(code)
    }

    fun respondIncident(code: String, notes: String) {
        repository.respondToIncident(code, notes)
    }

    fun resolveIncident(code: String) {
        repository.resolveIncident(code)
    }

    fun selectIncident(incident: IncidentEntity?) {
        _selectedIncident.value = incident
    }

    fun addContact(name: String, relationship: String, phone: String, whatsappNumber: String, priority: Int) {
        repository.addContact(name, relationship, phone, whatsappNumber, priority)
    }

    fun deleteContact(id: Long) {
        repository.deleteContact(id)
    }

    fun updateProfile(profile: UserProfileEntity) {
        repository.updateProfile(profile)
    }

    fun clearAllData() {
        repository.clearAllUserData()
    }

    fun simulatePowerButtonClick() {
        _powerClickCount.value += 1
        if (_powerClickCount.value >= 3) {
            _powerClickCount.value = 0
            triggerSosCountdown("POWER_BUTTON_3X")
        }
    }

    fun resetPowerClickCount() {
        _powerClickCount.value = 0
    }

    // Google Sign-In & Auth
    fun signInWithGoogle(
        email: String,
        displayName: String,
        photoUrl: String? = null
    ) {
        repository.signInWithGoogle(email, displayName, photoUrl)
    }

    fun signOutGoogle() {
        repository.signOutGoogle()
    }

    fun setUserRole(role: UserRole) {
        repository.setUserRole(role)
    }

    // Family Pairing (Parent & Child)
    fun generateNewPairingCode(parentPhone: String = ""): String {
        return repository.generateNewPairingCode(parentPhone)
    }

    fun pairWithParentCode(
        enteredCode: String,
        parentName: String = "Parent / Guardian",
        parentPhone: String = "",
        parentEmail: String = ""
    ): Boolean {
        return repository.pairWithParentCode(enteredCode, parentName, parentPhone, parentEmail)
    }

    fun addPairedChild(name: String, phone: String, email: String = "") {
        repository.addPairedChildToParent(name, phone, email)
    }

    fun removePairedChild(childId: String) {
        repository.removePairedChild(childId)
    }

    fun unpairChild() {
        repository.unpairChildFromParent()
    }

    fun sendCheckInPing(childId: String) {
        repository.sendParentCheckInPing(childId)
    }

    fun getBreadcrumbs(code: String): Flow<List<LocationBreadcrumbEntity>> {
        return repository.getBreadcrumbsForIncident(code)
    }

    fun calculateDistance(placeLat: Double, placeLng: Double): Double {
        val cur = currentLocation.value
        return repository.calculateDistanceKm(cur.latitude, cur.longitude, placeLat, placeLng)
    }

    // Firebase Data Migration
    fun migrateDataToFirebase() {
        viewModelScope.launch {
            val userId = googleUser.value.id
            if (userId.isNotEmpty()) {
                firebaseMigrationRepository.migrateRoomToFirestore(userId)
            }
        }
    }

    class Factory(private val repository: SosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
