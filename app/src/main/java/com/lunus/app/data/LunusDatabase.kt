package com.lunus.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        SubjectEntity::class,
        SourceEntity::class,
        DraftCardEntity::class,
        CardEntity::class,
        ModelProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LunusDatabase : RoomDatabase() {
    abstract fun lunusDao(): LunusDao

    companion object {
        @Volatile
        private var instance: LunusDatabase? = null

        fun get(context: Context): LunusDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LunusDatabase::class.java,
                    "lunus.db"
                )
                    .addMigrations(Migration1To2, Migration2To3)
                    .build()
                    .also { instance = it }
            }
        }
    }
}

private object Migration1To2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS model_profiles (
                id TEXT NOT NULL PRIMARY KEY,
                displayName TEXT NOT NULL,
                modelPath TEXT,
                runtimeStatus TEXT NOT NULL,
                lastLatencyMs INTEGER,
                lastError TEXT,
                updatedAt TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

private object Migration2To3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE model_profiles ADD COLUMN provider TEXT NOT NULL DEFAULT 'LocalFile'")
        db.execSQL("ALTER TABLE model_profiles ADD COLUMN variant TEXT NOT NULL DEFAULT 'CustomLocal'")
    }
}

@Dao
interface LunusDao {
    @Query("SELECT * FROM subjects ORDER BY createdAt ASC")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM sources ORDER BY createdAt ASC")
    fun observeSources(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM draft_cards ORDER BY createdAt ASC")
    fun observeDrafts(): Flow<List<DraftCardEntity>>

    @Query("SELECT * FROM cards ORDER BY createdAt ASC")
    fun observeCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM model_profiles WHERE id = :id LIMIT 1")
    fun observeModelProfile(id: String = "local-llm"): Flow<ModelProfileEntity?>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun subjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrafts(drafts: List<DraftCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModelProfile(profile: ModelProfileEntity)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query(
        """
        UPDATE draft_cards
        SET front = :front, back = :back, topic = :topic
        WHERE id = :id
        """
    )
    suspend fun updateDraft(id: String, front: String, back: String, topic: String)

    @Delete
    suspend fun deleteDraft(draft: DraftCardEntity)
}

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: String
)

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val body: String,
    val createdAt: String
)

@Entity(tableName = "draft_cards")
data class DraftCardEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val sourceId: String,
    val front: String,
    val back: String,
    val topic: String,
    val sourceSnippet: String,
    val createdAt: String
)

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val front: String,
    val back: String,
    val topic: String,
    val attempts: Int,
    val correctStreak: Int,
    val lastReviewedAt: String?,
    val createdAt: String
)

@Entity(tableName = "model_profiles")
data class ModelProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val provider: String,
    val variant: String,
    val modelPath: String?,
    val runtimeStatus: String,
    val lastLatencyMs: Long?,
    val lastError: String?,
    val updatedAt: String
)
