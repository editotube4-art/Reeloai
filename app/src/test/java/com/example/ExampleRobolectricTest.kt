package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.repository.AppRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("ReeloAI", appName)
  }

  @Test
  fun `database initialization and seeding`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val dao = database.appDao()
    val repository = AppRepository(dao)

    // Test seeding does not throw any exceptions (proves schema matches Entities configuration perfectly)
    repository.populateInitialDataIfEmpty()
    val adSettings = dao.getAdSettings()
    assertNotNull("Default ad settings should be populated and not null after seeding", adSettings)
    assertEquals("ca-app-pub-3940256099942544~3347511713", adSettings?.admobAppId)
  }
}

