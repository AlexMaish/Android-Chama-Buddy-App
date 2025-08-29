package com.example.chamabuddy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chamabuddy.domain.model.Penalty
import kotlinx.coroutines.flow.Flow

@Dao
interface PenaltyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(penalty: Penalty)

    @Query("SELECT * FROM penalties WHERE groupId = :groupId")
    fun getPenaltiesForGroup(groupId: String): kotlinx.coroutines.flow.Flow<List<Penalty>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM penalties WHERE groupId = :groupId")
    fun getTotalForGroup(groupId: String): Flow<Double>

    @Query("UPDATE penalties SET is_synced = 1 WHERE penaltyId = :penaltyId")
    suspend fun markAsSynced(penaltyId: String)

    @Query("SELECT * FROM penalties WHERE is_synced = 0")
    suspend fun getUnsyncedPenalties(): List<Penalty>

    @Query("SELECT * FROM penalties WHERE penaltyId = :penaltyId LIMIT 1")
    suspend fun getPenaltyById(penaltyId: String): Penalty?

    @Query("SELECT * FROM penalties WHERE groupId = :groupId AND memberId = :memberId AND description = :description AND amount = :amount AND date = :date AND is_deleted = 0")
    suspend fun findSimilarPenalty(groupId: String, memberId: String, description: String, amount: Double, date: Long): Penalty?

    // 🔹 Soft delete
    @Query("UPDATE penalties SET is_deleted = 1, deleted_at = :timestamp WHERE penaltyId = :penaltyId")
    suspend fun markAsDeleted(penaltyId: String, timestamp: Long)

    // 🔹 Get all soft-deleted penalties
    @Query("SELECT * FROM penalties WHERE is_deleted = 1")
    suspend fun getDeletedPenalties(): List<Penalty>

    // 🔹 Permanently delete
    @Query("DELETE FROM penalties WHERE penaltyId = :penaltyId")
    suspend fun permanentDelete(penaltyId: String)

    @Update
    suspend fun update(penalty: Penalty)
}