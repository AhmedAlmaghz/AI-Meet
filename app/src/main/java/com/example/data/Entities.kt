package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarRes: Int,
    val messageText: String,
    val timestamp: Long,
    val isPrivate: Boolean,
    val recipientId: String?
)

@Entity(tableName = "saved_rooms")
data class SavedRoomEntity(
    @PrimaryKey val id: String,
    val title: String,
    val topic: String,
    val category: String,
    val hostName: String,
    val participantCount: Int,
    val isClassroom: Boolean,
    val lastJoinedTimestamp: Long = System.currentTimeMillis()
)
