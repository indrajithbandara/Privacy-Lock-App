package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.components.SecureAuthDialog
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun unenrolledKeypadDoesNotSubmitCredentials() {
        var verificationCount = 0
        compose.setContent {
            MyApplicationTheme {
                SecureAuthDialog(isOpen = true, title = "Authentication test", randomizeKeypad = false,
                    isPinConfigured = false, vibrateOnKeyPress = false, onVerify = {
                        verificationCount++
                        "FAILED"
                    }, onDismiss = {}, onSuccess = {})
            }
        }
        repeat(6) { compose.onNodeWithTag("keypad_btn_7").performClick() }
        compose.runOnIdle { assertEquals(0, verificationCount) }
    }
}
