package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.infrastructure.database.HazirDatabase
import com.example.infrastructure.database.SavedAddressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: HazirDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, HazirDatabase::class.java)
        .allowMainThreadQueries()
        .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hazir", appName)
  }

  @Test
  fun `test saved address insertion and selection`() = runBlocking {
    val dao = db.savedAddressDao()
    val addr1 = SavedAddressEntity(
        userId = "test_user",
        label = "Home",
        address = "123 Street",
        isDefault = true
    )
    val addr2 = SavedAddressEntity(
        userId = "test_user",
        label = "Office",
        address = "456 Office Rd",
        isDefault = false
    )

    dao.insertAddress(addr1)
    dao.insertAddress(addr2)

    val addresses = dao.getSavedAddresses("test_user")
    assertEquals(2, addresses.size)
    assertEquals("Home", addresses[0].label)
    assertTrue(addresses[0].isDefault)

    val flowAddresses = dao.getSavedAddressesFlow("test_user").first()
    assertEquals(2, flowAddresses.size)
  }

  @Test
  fun `test set default address clears previous default`() = runBlocking {
    val dao = db.savedAddressDao()
    val id1 = dao.insertAddress(
        SavedAddressEntity(userId = "user_2", label = "Home", address = "123 Street", isDefault = true)
    ).toInt()
    val id2 = dao.insertAddress(
        SavedAddressEntity(userId = "user_2", label = "Office", address = "456 Office Rd", isDefault = false)
    ).toInt()

    dao.setDefaultAddress("user_2", id2)

    val list = dao.getSavedAddresses("user_2")
    val defaultAddr = list.find { it.isDefault }
    assertEquals(id2, defaultAddr?.id)

    val oldDefault = list.find { it.id == id1 }
    assertEquals(false, oldDefault?.isDefault)
  }
}
