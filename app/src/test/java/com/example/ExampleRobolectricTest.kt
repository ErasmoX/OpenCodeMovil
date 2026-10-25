package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.network.HealthCheckResult
import com.example.network.LocalEngineClient
import com.example.network.OpenRouterClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OpenCodeMovil", appName)
  }

  @Test
  fun `verify default models list is loaded`() {
    val client = OpenRouterClient()
    val models = client.defaultModels
    assertTrue(models.isNotEmpty())
    assertTrue(models.any { it.id.contains("deepseek") })
    assertTrue(models.any { it.id.contains("claude") })
  }

  @Test
  fun `verify local engine health check failure handling without server`() = runBlocking {
    val engineClient = LocalEngineClient()
    val result = engineClient.checkHealth("http://127.0.0.1:39999")
    assertTrue(result is HealthCheckResult.Failure)
  }
}
