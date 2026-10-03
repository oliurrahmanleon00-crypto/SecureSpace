package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.CryptoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SecureSpace", appName)
  }

  @Test
  fun `verify hardware keystore master key generation`() {
    val key = CryptoEngine.ensureMasterKey()
    assertNotNull(key)
    assertEquals("AES", key.algorithm)
  }

  @Test
  fun `verify aes-gcm authenticated encryption and decryption cycle`() {
    val plaintext = "Knox-Grade Enterprise Sandbox Payload 2026"
    val encrypted = CryptoEngine.encryptString(plaintext)
    assertNotEquals(plaintext, encrypted)

    val decrypted = CryptoEngine.decryptString(encrypted)
    assertEquals(plaintext, decrypted)
  }
}
