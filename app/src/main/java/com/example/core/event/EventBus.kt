package com.example.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

object EventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    /**
     * Publishes a new event to the global event stream.
     */
    suspend fun publish(event: AppEvent) {
        _events.emit(event)
    }

    /**
     * Publishes an event synchronously (non-suspending) using tryEmit.
     */
    fun tryPublish(event: AppEvent): Boolean {
        return _events.tryEmit(event)
    }

    /**
     * Subscribes and filters events of a specific type.
     */
    inline fun <reified T : AppEvent> subscribe() = events.filterIsInstance<T>()
}
