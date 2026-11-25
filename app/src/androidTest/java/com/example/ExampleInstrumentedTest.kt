package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals("com.aistudio.privacylock.pnvxld", appContext.packageName)
  }

  @Test
  fun debugBrandingPreservesApplicationIdentity() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals("Untouched", context.getString(R.string.app_name))
    assertEquals("Untouched: Privacy Lock", context.getString(R.string.product_name))
    assertEquals("Your apps. Your boundaries.", context.getString(R.string.product_tagline))
    assertNotNull(context.packageManager.getApplicationIcon(context.packageName))
    assertEquals("com.aistudio.privacylock.pnvxld", context.packageName)
  }
}
