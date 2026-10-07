package com.industri.transportqa

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketCode: String,
    val passengerName: String,
    val destination: String
)

@Dao
interface TicketDao {
    @Insert
    suspend fun insertTicket(ticket: TicketEntity)

    @Query("SELECT * FROM tickets WHERE ticketCode = :code")
    suspend fun getTicketByCode(code: String): TicketEntity?
}

@Database(entities = [TicketEntity::class], version = 1, exportSchema = false)
abstract class TestAppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
}

@RunWith(AndroidJUnit4::class)
class TicketDaoTest {
    private lateinit var db: TestAppDatabase
    private lateinit var dao: TicketDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // inMemoryDatabaseBuilder: Database murni hidup di RAM, musnah setelah test selesai
        db = Room.inMemoryDatabaseBuilder(context, TestAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.ticketDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertDanAmbilTiket_harusTersimpanPersis() = runBlocking {
        val tiketBaru = TicketEntity("TKT-8801", "Budi Santoso", "Surabaya Gubeng")
        dao.insertTicket(tiketBaru)

        val retrieved = dao.getTicketByCode("TKT-8801")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.passengerName).isEqualTo("Budi Santoso")
    }
}
