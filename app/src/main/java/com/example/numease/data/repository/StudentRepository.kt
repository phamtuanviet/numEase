package com.example.numease.data.repository

import com.example.numease.data.model.StudySession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class StarRecord(val stars: Int)

class StudentRepository @Inject constructor(
    private val postgrest: Postgrest
) {
    suspend fun getTotalStars(childProfileId: String): Int {
        return try {
            val records = postgrest.from("level_records")
                .select(columns = Columns.list("stars")) {
                    filter {
                        eq("child_profile_id", childProfileId)
                    }
                }.decodeList<StarRecord>()

            // Tính tổng số sao
            records.sumOf { it.stars }
        } catch (e: Exception) {
            e.printStackTrace()
            0 // Nếu có lỗi (như chưa có dữ liệu mạng), tạm trả về 0
        }
    }

    suspend fun getRecentSessions(childProfileId: String, limit: Int = 10): List<StudySession> {
        return try {
            postgrest.from("study_sessions").select {
                filter {
                    eq("child_profile_id", childProfileId)
                }
                // Sắp xếp theo thời gian mới nhất
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(limit.toLong())
            }.decodeList<StudySession>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}


