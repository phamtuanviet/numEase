package com.example.numease.data.repository


import com.example.numease.data.model.LevelRecord // Thay bằng import model của bạn
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class MapRepository @Inject constructor(
    private val postgrest: Postgrest
) {
    // Lấy toàn bộ kỷ lục số sao của một bé
    suspend fun getLevelRecords(childId: String): List<LevelRecord> {
        return try {
            postgrest.from("level_records")
                .select {
                    filter { eq("child_profile_id", childId) }
                }.decodeList<LevelRecord>()
        } catch (e: Exception) {
            e.printStackTrace()
            // Nếu lỗi mạng hoặc chưa có dữ liệu, trả về list rỗng
            emptyList()
        }
    }
}