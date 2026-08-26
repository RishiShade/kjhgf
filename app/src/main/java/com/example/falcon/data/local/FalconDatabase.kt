package com.example.falcon.data.local

import androidx.room.*
import com.example.falcon.model.ActivityLogItem
import com.example.falcon.model.ConversationMessage
import com.example.falcon.model.MemoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMemories(query: String): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecentMemoriesSync(): List<MemoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem): Long

    @Update
    suspend fun updateMemory(memory: MemoryItem)

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAll()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp DESC LIMIT 30")
    suspend fun getRecentMessagesSync(): List<ConversationMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessage): Long

    @Query("DELETE FROM conversation_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM conversation_messages")
    suspend fun clearAll()
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogItem>>

    @Query("SELECT * FROM activity_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getLogsByCategory(category: String): Flow<List<ActivityLogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogItem): Long

    @Query("DELETE FROM activity_logs")
    suspend fun clearAll()
}

@Database(
    entities = [MemoryItem::class, ConversationMessage::class, ActivityLogItem::class],
    version = 1,
    exportSchema = false
)
abstract class FalconDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun conversationDao(): ConversationDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: FalconDatabase? = null

        fun getInstance(context: android.content.Context): FalconDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FalconDatabase::class.java,
                    "falcon_neural_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
