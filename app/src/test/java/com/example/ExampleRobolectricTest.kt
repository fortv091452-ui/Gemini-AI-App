package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BrowserManager
import com.example.data.WolfiusSystemManager
import org.junit.Assert.assertEquals
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
        assertEquals("Wolfius Gemini", appName)
    }

    @Test
    fun `verify memory stats readable`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val stats = WolfiusSystemManager.getMemoryStats(context)
        assertNotNull(stats)
        assertTrue(stats.totalMemMb >= 0)
    }

    @Test
    fun `verify foss browsers list configured`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val browsers = BrowserManager.getFossBrowsers(context)
        assertTrue(browsers.isNotEmpty())
        assertTrue(browsers.any { it.name.contains("Lightning") })
    }

    @Test
    fun `verify prestigio device profile`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = WolfiusSystemManager.getDeviceProfile(context, forcePrestigioPreset = true)
        assertTrue(profile.modelName.contains("Prestigio"))
        assertTrue(profile.isPrestigioProfileActive)
    }
}
