package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.LocationBreadcrumbEntity
import com.example.data.local.entity.SafePlaceEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY priority ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY priority ASC")
    suspend fun getAllContactsList(): List<EmergencyContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<EmergencyContactEntity>)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY activatedAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE status IN ('ACTIVE', 'ACKNOWLEDGED', 'RESPONDING') ORDER BY activatedAt DESC")
    fun getActiveIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE trackingToken = :token LIMIT 1")
    fun getIncidentByToken(token: String): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents WHERE incidentCode = :code LIMIT 1")
    fun getIncidentByCode(code: String): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents WHERE incidentCode = :code LIMIT 1")
    suspend fun getIncidentByCodeOnce(code: String): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("UPDATE incidents SET status = :status, resolvedAt = :resolvedAt WHERE incidentCode = :code")
    suspend fun updateIncidentStatus(code: String, status: String, resolvedAt: Long? = null)

    @Query("UPDATE incidents SET status = :status, acknowledgedAt = :time WHERE incidentCode = :code")
    suspend fun markAcknowledged(code: String, status: String = "ACKNOWLEDGED", time: Long = System.currentTimeMillis())

    @Query("UPDATE incidents SET status = :status, respondingAt = :time, responderNotes = :notes WHERE incidentCode = :code")
    suspend fun markResponding(code: String, status: String = "RESPONDING", time: Long = System.currentTimeMillis(), notes: String = "")

    @Query("UPDATE incidents SET currentLatitude = :lat, currentLongitude = :lng, accuracy = :accuracy, speed = :speed, battery = :battery, addressName = :address WHERE incidentCode = :code")
    suspend fun updateLocation(code: String, lat: Double, lng: Double, accuracy: Float, speed: Float, battery: Int, address: String)
}

@Dao
interface LocationBreadcrumbDao {
    @Query("SELECT * FROM location_breadcrumbs WHERE incidentCode = :incidentCode ORDER BY timestamp ASC")
    fun getBreadcrumbsForIncident(incidentCode: String): Flow<List<LocationBreadcrumbEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreadcrumb(breadcrumb: LocationBreadcrumbEntity)

    @Query("DELETE FROM location_breadcrumbs WHERE incidentCode = :incidentCode")
    suspend fun deleteForIncident(incidentCode: String)
}

@Dao
interface SafePlaceDao {
    @Query("SELECT * FROM safe_places ORDER BY category, name")
    fun getAllSafePlaces(): Flow<List<SafePlaceEntity>>

    @Query("SELECT * FROM safe_places WHERE category = :category ORDER BY name")
    fun getSafePlacesByCategory(category: String): Flow<List<SafePlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<SafePlaceEntity>)

    @Query("SELECT COUNT(*) FROM safe_places")
    suspend fun getCount(): Int
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
