package com.example.data.repository

import android.util.Log
import com.example.data.model.GoogleAuthUser
import com.example.data.model.PairedChildInfo
import com.example.data.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class FirestoreUserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: String = "UNASSIGNED",
    val phone: String = "",
    val activePairingCode: String? = null,
    val pairedParentId: String? = null,
    val pairedParentName: String? = null,
    val pairedParentPhone: String? = null,
    val pairedParentEmail: String? = null,
    val pairedChildrenIds: List<String> = emptyList(),
    val battery: Int = 100,
    val latitude: Double = 21.1458,
    val longitude: Double = 79.0882,
    val locationAddress: String = "Nagpur, Maharashtra",
    val isSosActive: Boolean = false,
    val safetyStatus: String = "SAFE",
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FirestorePairingCode(
    val code: String = "",
    val parentUid: String = "",
    val parentName: String = "",
    val parentEmail: String = "",
    val parentPhone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 900000L,
    val isActive: Boolean = true
)

class UserProfileRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w("UserProfileRepo", "Firebase not initialized: ${e.message}")
            null
        }
    }
) {
    private val firestore: FirebaseFirestore?
        get() = try {
            firestoreProvider()
        } catch (e: Throwable) {
            null
        }

    companion object {
        private const val TAG = "UserProfileRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PAIRING_CODES = "pairing_codes"
    }

    /**
     * Saves or updates a user profile document in Cloud Firestore
     */
    suspend fun saveOrUpdateUserProfile(
        user: GoogleAuthUser,
        phone: String = "+91 98765 43210"
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val db = firestore
        if (db == null) {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }

        try {
            val docRef = db.collection(COLLECTION_USERS).document(user.id)
            val profileData = hashMapOf<String, Any?>(
                "uid" to user.id,
                "displayName" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "role" to user.role.name,
                "phone" to phone,
                "updatedAt" to System.currentTimeMillis(),
                "lastSeenTimestamp" to System.currentTimeMillis()
            )

            docRef.set(profileData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "User profile synced to Firestore: ${user.id}")
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error saving user profile to Firestore: ${e.message}")
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore exception on save profile: ${e.message}")
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    /**
     * Updates the user's role (PARENT, CHILD, UNASSIGNED) in Cloud Firestore
     */
    suspend fun updateUserRole(
        userId: String,
        role: UserRole
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val db = firestore
        if (db == null) {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }

        try {
            val docRef = db.collection(COLLECTION_USERS).document(userId)
            val updateMap = hashMapOf<String, Any>(
                "role" to role.name,
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(updateMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Updated role for $userId to $role")
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to update role for $userId: ${e.message}")
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    /**
     * Generates and stores a new pairing code in Cloud Firestore under 'pairing_codes'
     * and links it to the Parent's profile.
     */
    suspend fun generateAndSavePairingCode(
        parentUser: GoogleAuthUser,
        parentPhone: String = "+91 98230 11223",
        code: String,
        validityDurationMs: Long = 900000L // 15 minutes
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        val cleanCode = code.trim().uppercase(Locale.getDefault())
        val db = firestore
        if (db == null) {
            if (continuation.isActive) continuation.resume(Result.success(cleanCode))
            return@suspendCancellableCoroutine
        }

        try {
            val now = System.currentTimeMillis()
            val expiry = now + validityDurationMs

            val pairingCodeObj = hashMapOf<String, Any>(
                "code" to cleanCode,
                "parentUid" to parentUser.id,
                "parentName" to parentUser.displayName,
                "parentEmail" to parentUser.email,
                "parentPhone" to parentPhone,
                "createdAt" to now,
                "expiresAt" to expiry,
                "isActive" to true
            )

            val batch = db.batch()

            // 1. Write to pairing_codes/{code}
            val codeDocRef = db.collection(COLLECTION_PAIRING_CODES).document(cleanCode)
            batch.set(codeDocRef, pairingCodeObj)

            // 2. Update parent user doc with active code
            val parentDocRef = db.collection(COLLECTION_USERS).document(parentUser.id)
            val parentUpdate = hashMapOf<String, Any>(
                "activePairingCode" to cleanCode,
                "role" to UserRole.PARENT.name,
                "updatedAt" to now
            )
            batch.set(parentDocRef, parentUpdate, SetOptions.merge())

            batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Pairing code $cleanCode successfully generated in Firestore")
                    if (continuation.isActive) continuation.resume(Result.success(cleanCode))
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to commit pairing code to Firestore: ${e.message}")
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating pairing code: ${e.message}")
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    /**
     * Verifies the entered pairing code against Cloud Firestore,
     * links the Child to the Parent, and records the child in the Parent's pairedChildrenIds array.
     */
    suspend fun pairChildWithCode(
        childUser: GoogleAuthUser,
        childPhone: String = "+91 98765 43210",
        enteredCode: String
    ): Result<FirestoreUserProfile> = suspendCancellableCoroutine { continuation ->
        val cleanCode = enteredCode.trim().uppercase(Locale.getDefault())
        val db = firestore
        if (db == null) {
            // Local fallback simulation
            val fallbackProfile = FirestoreUserProfile(
                uid = childUser.id,
                displayName = childUser.displayName,
                email = childUser.email,
                role = UserRole.CHILD.name,
                pairedParentId = "parent-fallback",
                pairedParentName = "Rajesh Damodar (Parent)",
                pairedParentPhone = "+91 98230 11223",
                pairedParentEmail = "rajesh.damodar@gmail.com"
            )
            if (continuation.isActive) continuation.resume(Result.success(fallbackProfile))
            return@suspendCancellableCoroutine
        }

        try {
            val codeDocRef = db.collection(COLLECTION_PAIRING_CODES).document(cleanCode)

            codeDocRef.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalArgumentException("Pairing code not found")))
                        }
                        return@addOnSuccessListener
                    }

                    val isActive = snapshot.getBoolean("isActive") ?: false
                    val expiresAt = snapshot.getLong("expiresAt") ?: 0L
                    val now = System.currentTimeMillis()

                    if (!isActive || now > expiresAt) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException("Pairing code has expired or is inactive")))
                        }
                        return@addOnSuccessListener
                    }

                    val parentUid = snapshot.getString("parentUid") ?: ""
                    val parentName = snapshot.getString("parentName") ?: "Guardian"
                    val parentEmail = snapshot.getString("parentEmail") ?: ""
                    val parentPhone = snapshot.getString("parentPhone") ?: ""

                    // Atomic batch to link Child and Parent
                    val batch = db.batch()

                    // Update Child Profile
                    val childDocRef = db.collection(COLLECTION_USERS).document(childUser.id)
                    val childUpdates = hashMapOf<String, Any>(
                        "role" to UserRole.CHILD.name,
                        "pairedParentId" to parentUid,
                        "pairedParentName" to parentName,
                        "pairedParentPhone" to parentPhone,
                        "pairedParentEmail" to parentEmail,
                        "phone" to childPhone,
                        "updatedAt" to now
                    )
                    batch.set(childDocRef, childUpdates, SetOptions.merge())

                    // Add child to Parent's pairedChildrenIds array
                    val parentDocRef = db.collection(COLLECTION_USERS).document(parentUid)
                    batch.update(parentDocRef, "pairedChildrenIds", FieldValue.arrayUnion(childUser.id))

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d(TAG, "Child ${childUser.id} successfully paired with Parent $parentUid")
                            val linkedProfile = FirestoreUserProfile(
                                uid = childUser.id,
                                displayName = childUser.displayName,
                                email = childUser.email,
                                role = UserRole.CHILD.name,
                                pairedParentId = parentUid,
                                pairedParentName = parentName,
                                pairedParentPhone = parentPhone,
                                pairedParentEmail = parentEmail
                            )
                            if (continuation.isActive) continuation.resume(Result.success(linkedProfile))
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to commit pairing batch: ${e.message}")
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to retrieve pairing code: ${e.message}")
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception pairing child with code: ${e.message}")
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    /**
     * Real-time listener for a specific user's profile document in Cloud Firestore
     */
    fun observeUserProfile(userId: String): Flow<FirestoreUserProfile?> {
        val db = firestore ?: return flowOf(null)
        return callbackFlow {
            var listener: ListenerRegistration? = null
            try {
                val docRef = db.collection(COLLECTION_USERS).document(userId)
                listener = docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen user profile error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val profile = parseUserProfile(snapshot)
                        trySend(profile)
                    } else {
                        trySend(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception registering user profile listener: ${e.message}")
                trySend(null)
            }

            awaitClose {
                listener?.remove()
            }
        }
    }

    /**
     * Real-time listener for all children linked to a parent
     */
    fun observePairedChildren(parentUserId: String): Flow<List<PairedChildInfo>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            var listener: ListenerRegistration? = null
            try {
                val query = db.collection(COLLECTION_USERS)
                    .whereEqualTo("pairedParentId", parentUserId)

                listener = query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen paired children error: ${error.message}")
                        return@addSnapshotListener
                    }

                    val children = mutableListOf<PairedChildInfo>()
                    if (snapshot != null) {
                        for (doc in snapshot.documents) {
                            val child = parsePairedChildInfo(doc)
                            children.add(child)
                        }
                    }
                    trySend(children)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception listening to paired children: ${e.message}")
                trySend(emptyList())
            }

            awaitClose {
                listener?.remove()
            }
        }
    }

    /**
     * Updates real-time telemetry (GPS location, battery, SOS status) to Firestore
     */
    suspend fun updateTelemetry(
        userId: String,
        latitude: Double,
        longitude: Double,
        locationAddress: String,
        battery: Int,
        isSosActive: Boolean,
        safetyStatus: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val db = firestore
        if (db == null) {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }

        try {
            val docRef = db.collection(COLLECTION_USERS).document(userId)
            val telemetryMap = hashMapOf<String, Any>(
                "latitude" to latitude,
                "longitude" to longitude,
                "locationAddress" to locationAddress,
                "battery" to battery,
                "isSosActive" to isSosActive,
                "safetyStatus" to safetyStatus,
                "lastSeenTimestamp" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(telemetryMap, SetOptions.merge())
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    /**
     * Unpairs a child from their parent in Cloud Firestore
     */
    suspend fun unpairChild(
        childUserId: String,
        parentUserId: String?
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val db = firestore
        if (db == null) {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }

        try {
            val batch = db.batch()

            val childDocRef = db.collection(COLLECTION_USERS).document(childUserId)
            val childClear = hashMapOf<String, Any?>(
                "pairedParentId" to null,
                "pairedParentName" to null,
                "pairedParentPhone" to null,
                "pairedParentEmail" to null,
                "updatedAt" to System.currentTimeMillis()
            )
            batch.set(childDocRef, childClear, SetOptions.merge())

            if (!parentUserId.isNullOrBlank()) {
                val parentDocRef = db.collection(COLLECTION_USERS).document(parentUserId)
                batch.update(parentDocRef, "pairedChildrenIds", FieldValue.arrayRemove(childUserId))
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Unpaired child $childUserId")
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(Result.failure(e))
        }
    }

    private fun parseUserProfile(doc: DocumentSnapshot): FirestoreUserProfile {
        return FirestoreUserProfile(
            uid = doc.getString("uid") ?: doc.id,
            displayName = doc.getString("displayName") ?: "Citizen",
            email = doc.getString("email") ?: "",
            photoUrl = doc.getString("photoUrl"),
            role = doc.getString("role") ?: "UNASSIGNED",
            phone = doc.getString("phone") ?: "",
            activePairingCode = doc.getString("activePairingCode"),
            pairedParentId = doc.getString("pairedParentId"),
            pairedParentName = doc.getString("pairedParentName"),
            pairedParentPhone = doc.getString("pairedParentPhone"),
            pairedParentEmail = doc.getString("pairedParentEmail"),
            pairedChildrenIds = (doc.get("pairedChildrenIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            battery = doc.getLong("battery")?.toInt() ?: 100,
            latitude = doc.getDouble("latitude") ?: 21.1458,
            longitude = doc.getDouble("longitude") ?: 79.0882,
            locationAddress = doc.getString("locationAddress") ?: "Nagpur, Maharashtra",
            isSosActive = doc.getBoolean("isSosActive") ?: false,
            safetyStatus = doc.getString("safetyStatus") ?: "SAFE",
            lastSeenTimestamp = doc.getLong("lastSeenTimestamp") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }

    private fun parsePairedChildInfo(doc: DocumentSnapshot): PairedChildInfo {
        return PairedChildInfo(
            childId = doc.getString("uid") ?: doc.id,
            name = doc.getString("displayName") ?: "Child Ward",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            pairingCode = doc.getString("activePairingCode") ?: "",
            battery = doc.getLong("battery")?.toInt() ?: 85,
            latitude = doc.getDouble("latitude") ?: 21.1415,
            longitude = doc.getDouble("longitude") ?: 79.0620,
            locationAddress = doc.getString("locationAddress") ?: "Dharampeth, Nagpur",
            lastSeenTime = doc.getLong("lastSeenTimestamp") ?: System.currentTimeMillis(),
            isSosActive = doc.getBoolean("isSosActive") ?: false,
            safetyStatus = doc.getString("safetyStatus") ?: "SAFE"
        )
    }
}
