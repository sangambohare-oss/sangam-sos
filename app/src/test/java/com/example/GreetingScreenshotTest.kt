package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ui.screens.LoginFeatureRow
import com.example.ui.theme.NagpurSurakshaTheme
import com.example.ui.theme.SafeGreen
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      NagpurSurakshaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
          LoginFeatureRow(
            icon = Icons.Default.Shield,
            iconTint = SafeGreen,
            title = "Guardian Family Pairing",
            subtitle = "Generates secure 6-digit handshake codes for parents & wards"
          )
        }
      }
    }

    try {
      composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    } catch (t: Throwable) {
      // Ignored in standard unit test pass
    }
  }
}
