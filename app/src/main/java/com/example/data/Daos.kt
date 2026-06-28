package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun clearRoomChat(roomId: String)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM saved_rooms ORDER BY lastJoinedTimestamp DESC")
    fun getSavedRooms(): Flow<List<SavedRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoom(room: SavedRoomEntity)

    @Query("DELETE FROM saved_rooms WHERE id = :roomId")
    suspend fun deleteRoomById(roomId: String)
}
