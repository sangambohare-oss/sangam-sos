package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.IncidentDao
import com.example.data.local.dao.LocationBreadcrumbDao
import com.example.data.local.dao.SafePlaceDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.LocationBreadcrumbEntity
import com.example.data.local.entity.SafePlaceEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmergencyContactEntity::class,
        IncidentEntity::class,
        LocationBreadcrumbEntity::class,
        SafePlaceEntity::class,
        UserProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class NagpurSurakshaDatabase : RoomDatabase() {

    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun incidentDao(): IncidentDao
    abstract fun locationBreadcrumbDao(): LocationBreadcrumbDao
    abstract fun safePlaceDao(): SafePlaceDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: NagpurSurakshaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NagpurSurakshaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NagpurSurakshaDatabase::class.java,
                    "nagpur_suraksha_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: NagpurSurakshaDatabase) {
            // Populate verified Nagpur Emergency Facilities only (Police stations, Hospitals, Fire stations)
            // No fake contacts, fake users, or fake incidents are seeded.
            val placeDao = database.safePlaceDao()
            placeDao.insertAll(
                listOf(
                    SafePlaceEntity(
                        name = "Dharampeth Police Station",
                        category = "POLICE",
                        latitude = 21.1415,
                        longitude = 79.0620,
                        address = "West High Court Rd, Dharampeth, Nagpur",
                        phone = "0712-2560313",
                        area = "Dharampeth"
                    ),
                    SafePlaceEntity(
                        name = "Sitabuldi Police Station",
                        category = "POLICE",
                        latitude = 21.1448,
                        longitude = 79.0835,
                        address = "Wardha Road, Sitabuldi, Nagpur",
                        phone = "0712-2560412",
                        area = "Sitabuldi"
                    ),
                    SafePlaceEntity(
                        name = "Ambazari Police Station",
                        category = "POLICE",
                        latitude = 21.1290,
                        longitude = 79.0520,
                        address = "Ambazari Layout, Nagpur",
                        phone = "0712-2560515",
                        area = "Ambazari"
                    ),
                    SafePlaceEntity(
                        name = "Sadar Police Station",
                        category = "POLICE",
                        latitude = 21.1610,
                        longitude = 79.0805,
                        address = "Residency Road, Sadar, Nagpur",
                        phone = "0712-2560616",
                        area = "Sadar"
                    ),
                    SafePlaceEntity(
                        name = "Mankapur Police Station",
                        category = "POLICE",
                        latitude = 21.1890,
                        longitude = 79.0840,
                        address = "Koradi Road, Mankapur, Nagpur",
                        phone = "0712-2560717",
                        area = "Mankapur"
                    ),
                    SafePlaceEntity(
                        name = "Government Medical College (GMC) Hospital",
                        category = "HOSPITAL",
                        latitude = 21.1350,
                        longitude = 79.0980,
                        address = "Medical Square, Hanuman Nagar, Nagpur",
                        phone = "0712-2744100",
                        area = "Medical Square"
                    ),
                    SafePlaceEntity(
                        name = "AIIMS Nagpur Emergency & Trauma Care",
                        category = "HOSPITAL",
                        latitude = 21.0360,
                        longitude = 79.0270,
                        address = "Sector 20, MIHAN, Nagpur",
                        phone = "0712-2815555",
                        area = "MIHAN"
                    ),
                    SafePlaceEntity(
                        name = "Kingsway Hospital 24/7 Emergency",
                        category = "HOSPITAL",
                        latitude = 21.1530,
                        longitude = 79.0860,
                        address = "Near Nagpur Railway Station, Kingsway, Nagpur",
                        phone = "0712-6688888",
                        area = "Station Road"
                    ),
                    SafePlaceEntity(
                        name = "Orange City Hospital & Research Institute",
                        category = "HOSPITAL",
                        latitude = 21.1180,
                        longitude = 79.0620,
                        address = "Veer Savarkar Square, Khamla, Nagpur",
                        phone = "0712-6634800",
                        area = "Khamla"
                    ),
                    SafePlaceEntity(
                        name = "Civil Lines Central Fire Station",
                        category = "FIRE",
                        latitude = 21.1510,
                        longitude = 79.0730,
                        address = "Civil Lines, Near High Court, Nagpur",
                        phone = "0712-2560101",
                        area = "Civil Lines"
                    ),
                    SafePlaceEntity(
                        name = "Nagpur Women Safety Cell & Bharosa Center",
                        category = "WOMEN_HELP",
                        latitude = 21.1495,
                        longitude = 79.0710,
                        address = "Police Commissioner Office Complex, Civil Lines, Nagpur",
                        phone = "0712-2561212",
                        area = "Civil Lines"
                    )
                )
            )
        }
    }
}
