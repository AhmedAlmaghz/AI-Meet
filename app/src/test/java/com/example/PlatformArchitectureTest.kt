package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.NetworkEvent
import com.aistudio.meetingplatform.core.recovery.OfflineRecoveryManager
import com.aistudio.meetingplatform.core.recovery.QueuedOperation
import com.aistudio.meetingplatform.core.security.SecurityManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformArchitectureTest {

    private lateinit var eventBus: EventBus
    private lateinit var offlineRecoveryManager: OfflineRecoveryManager
    private lateinit var securityManager: SecurityManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        eventBus = EventBus()
        offlineRecoveryManager = OfflineRecoveryManager(eventBus)
        securityManager = SecurityManager(context)
    }

    @Test
    fun `test event bus emits network events`() = runTest {
        offlineRecoveryManager.simulateConnectionStatus(false)
        assertEquals(false, offlineRecoveryManager.isOnline.value)
    }

    @Test
    fun `test offline recovery queues operations when offline`() = runTest {
        offlineRecoveryManager.simulateConnectionStatus(false)
        
        var executed = false
        val op = QueuedOperation("op_1", "Send Chat Message")
        offlineRecoveryManager.enqueue(op) {
            executed = true
        }

        assertEquals(1, offlineRecoveryManager.pendingOperations.value.size)
        assertEquals(false, executed)
    }

    @Test
    fun `test security manager sanitizes PII and stores tokens`() {
        val secretToken = "jwt_secure_token_abc123"
        securityManager.secureStoreToken("auth_token", secretToken)
        assertEquals(secretToken, securityManager.retrieveToken("auth_token"))

        val dirtyLog = "User failed login with email john.doe@enterprise.com at IP 10.0.0.1"
        val cleanLog = securityManager.sanitizePII(dirtyLog)
        assertEquals("User failed login with email [REDACTED_EMAIL] at IP 10.0.0.1", cleanLog)
    }
}
