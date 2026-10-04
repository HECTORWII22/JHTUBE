package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CompressionConfig
import com.example.data.model.ResolutionPreset
import com.example.data.model.VideoCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JHTube", appName)
  }

  @Test
  fun `compression calculation saves space`() {
    val config = CompressionConfig(
      targetResolution = ResolutionPreset.RES_360P,
      codec = VideoCodec.H264,
      videoBitrateKbps = 600,
      audioBitrateKbps = 64
    )
    val durationSeconds = 120L // 2 minutes
    val estimatedBytes = config.calculateEstimatedSizeBytes(durationSeconds)
    assertTrue("Estimated bytes should be greater than zero", estimatedBytes > 0)

    val originalBytes = 35 * 1024 * 1024L // 35 MB original 1080p
    val savingsPct = config.calculateSavingsPercentage(originalBytes, durationSeconds)
    assertTrue("Savings percentage should be significant (>30%)", savingsPct > 30)
  }

  @Test
  fun `simpleCache manager initializes and reports cache size`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cache = com.example.util.ExoPlayerCacheManager.getSimpleCache(context)
    org.junit.Assert.assertNotNull(cache)

    val cacheSize = com.example.util.ExoPlayerCacheManager.getCacheSizeBytes(context)
    assertTrue("Cache size bytes should be non-negative", cacheSize >= 0)
  }
}
