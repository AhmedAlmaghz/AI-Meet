package com.aistudio.meetingplatform.core.recovery

import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.NetworkEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

data class QueuedOperation(
    val id: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class OfflineRecoveryManager @Inject constructor(
    private val eventBus: EventBus
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _pendingOperations = MutableStateFlow<List<QueuedOperation>>(emptyList())
    val pendingOperations: StateFlow<List<QueuedOperation>> = _pendingOperations.asStateFlow()

    private val actionQueue = ConcurrentLinkedQueue<Pair<QueuedOperation, suspend () -> Unit>>()

    fun simulateConnectionStatus(online: Boolean) {
        if (_isOnline.value == online) return
        _isOnline.value = online
        scope.launch {
            if (online) {
                eventBus.emit(NetworkEvent.ConnectionRecovered)
                replayQueuedActions()
            } else {
                eventBus.emit(NetworkEvent.ConnectionLost)
            }
        }
    }

    fun enqueue(operation: QueuedOperation, action: suspend () -> Unit) {
        if (_isOnline.value) {
            scope.launch { action() }
        } else {
            actionQueue.add(operation to action)
            updatePendingState()
        }
    }

    private suspend fun replayQueuedActions() {
        while (!actionQueue.isEmpty()) {
            val item = actionQueue.poll()
            if (item != null) {
                try {
                    item.second.invoke()
                } catch (e: Exception) {
                    // Log or re-enqueue depending on policy
                }
            }
        }
        updatePendingState()
    }

    private fun updatePendingState() {
        _pendingOperations.value = actionQueue.map { it.first }
    }
}
