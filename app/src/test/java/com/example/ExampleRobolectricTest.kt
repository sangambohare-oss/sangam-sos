package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.NagpurSurakshaDatabase
import com.example.data.model.UserRole
import com.example.data.repository.SosRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nagpur Suraksha", appName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test SOS activation and incident creation`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val db = NagpurSurakshaDatabase.getDatabase(context, testScope)
        val repository = SosRepository(context, db, testScope)

        // Trigger SOS immediately
        repository.activateSosImmediately()

        val activeSos = repository.activeSos.value
        assertTrue(activeSos.isTriggered)
        assertNotNull(activeSos.incidentCode)
        assertNotNull(activeSos.trackingToken)

        // Verify distance calculation for Sitabuldi to Dharampeth (~2.5km)
        val dist = repository.calculateDistanceKm(21.1460, 79.0870, 21.1415, 79.0620)
        assertTrue(dist > 1.0 && dist < 5.0)

        // Safe Cancel
        repository.cancelSos("Test user safe")
        assertEquals(false, repository.activeSos.value.isTriggered)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test Google SignIn and Parent-Child Pairing flow`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val db = NagpurSurakshaDatabase.getDatabase(context, testScope)
        val repository = SosRepository(context, db, testScope)

        // 1. Google Sign-In
        repository.signInWithGoogle(
            email = "rajesh.damodar@gmail.com",
            displayName = "Rajesh Damodar",
            photoUrl = null
        )
        val signedInUser = repository.googleUser.value
        assertTrue(signedInUser.isSignedIn)
        assertEquals("Rajesh Damodar", signedInUser.displayName)

        // 2. Select PARENT role
        repository.setUserRole(UserRole.PARENT)
        assertEquals(UserRole.PARENT, repository.googleUser.value.role)

        // 3. Generate New Pairing Code
        val newCode = repository.generateNewPairingCode()
        assertTrue(newCode.startsWith("NAG-"))
        assertEquals(newCode, repository.familyPairing.value.activePairingCode)

        // 4. Child enters code and pairs
        val pairResult = repository.pairWithParentCode(
            enteredCode = newCode,
            childName = "Chaitali Damodar",
            childPhone = "+91 98765 43210"
        )
        assertTrue(pairResult)
        assertTrue(repository.familyPairing.value.isChildPairedWithParent)
        assertEquals("Rajesh Damodar (Parent)", repository.familyPairing.value.pairedParentName)

        // 5. Test invalid code rejection
        val invalidResult = repository.pairWithParentCode(
            enteredCode = "INVALID-CODE",
            childName = "Test",
            childPhone = "123"
        )
        assertFalse(invalidResult)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test Firebase data migration repository`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val db = NagpurSurakshaDatabase.getDatabase(context, testScope)
        val migrationRepo = com.example.data.repository.FirebaseDataMigrationRepository(context, db)

        val result = migrationRepo.migrateRoomToFirestore("test-user-id")
        assertNotNull(result)
        assertTrue(result.totalRecordsMigrated > 0)
        assertEquals(com.example.data.repository.MigrationStatus.SUCCESS, result.status)
    }
}
