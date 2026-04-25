package com.example.numease.data.repository

import com.example.numease.data.model.Exercise
import com.example.numease.data.model.LevelRecord
import com.example.numease.data.model.SessionAnswer
import com.example.numease.data.model.StudySession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject


class ExerciseRepository @Inject constructor(
    private val postgrest: Postgrest
) {
    // 1. Lấy bài tập theo Đảo và Cửa
    suspend fun getExercises(categoryId: Int, level: Int): List<Exercise> {
        return postgrest.from("exercises").select {
            filter {
                eq("category_id", categoryId)
                eq("level", level)
            }
        }.decodeList<Exercise>()
    }

    // 2. Lấy danh sách ID các bài tập đã làm đúng (Né lặp câu hỏi)
    // 2. Lấy danh sách ID các bài tập đã làm đúng (Né lặp câu hỏi)
    suspend fun getAnsweredExerciseIds(childId: String, categoryId: Int): List<String> {
        // ĐÃ SỬA: Bỏ 'columns = Columns.list("id")' để lấy toàn bộ object
        val sessions = postgrest.from("study_sessions").select {
            filter {
                eq("child_profile_id", childId)
                eq("category_id", categoryId)
            }
        }.decodeList<StudySession>()

        val sessionIds = sessions.mapNotNull { it.id }
        if (sessionIds.isEmpty()) return emptyList()

        // ĐÃ SỬA: Bỏ 'columns = Columns.list("exercise_id")' để lấy toàn bộ object
        val answers = postgrest.from("session_answers").select {
            filter {
                isIn("session_id", sessionIds)
                eq("is_correct", true)
            }
        }.decodeList<SessionAnswer>()

        return answers.map { it.exerciseId }.distinct()
    }
    // 3. Đẩy toàn bộ kết quả lên Supabase khi hoàn thành màn chơi
    suspend fun saveFullSessionResults(
        session: StudySession,
        answers: List<SessionAnswer>,
        level: Int, // Lấy từ route truyền vào để update kỷ lục
        starsEarned: Int
    ) {
        // A. Insert Session và lấy về object có chứa ID do DB sinh ra
        val createdSession = postgrest.from("study_sessions")
            .insert(session) { select() }
            .decodeSingle<StudySession>()

        val sessionId = createdSession.id ?: return

        // B. Gắn sessionId vào từng câu trả lời và Insert 1 lượt (Bulk Insert)
        val finalAnswers = answers.map { it.copy(sessionId = sessionId) }
        postgrest.from("session_answers").insert(finalAnswers)

        // C. Cập nhật Kỷ lục Sao (Upsert)
        // Kiểm tra xem đã có kỷ lục cũ chưa
        val existingRecords = postgrest.from("level_records").select {
            filter {
                eq("child_profile_id", session.childProfileId)
                eq("category_id", session.categoryId)
                eq("level", level)
            }
        }.decodeList<LevelRecord>()

        if (existingRecords.isEmpty()) {
            // Chưa có -> Insert mới
            postgrest.from("level_records").insert(
                LevelRecord(
                    childProfileId = session.childProfileId,
                    categoryId = session.categoryId,
                    level = level,
                    stars = starsEarned
                )
            )
        } else {
            // Đã có -> Chỉ Update nếu sao mới cao hơn sao cũ
            val oldStar = existingRecords.first().stars
            if (starsEarned > oldStar) {
                postgrest.from("level_records").update(
                    { set("stars", starsEarned) }
                ) {
                    filter { eq("id", existingRecords.first().id!!) }
                }
            }
        }
    }

    // 1. Tìm ID của phiên chơi gần nhất cho một màn cụ thể
    suspend fun getLatestSessionId(childId: String, categoryId: Int, level: Int): String? {
        return try {
            val session = postgrest.from("study_sessions").select {
                filter {
                    eq("child_profile_id", childId)
                    eq("category_id", categoryId)
                    eq("level", level) // Giả sử bạn thêm cột level vào study_sessions hoặc lọc qua logic khác
                }
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(1)
            }.decodeSingleOrNull<StudySession>()
            session?.id
        } catch (e: Exception) {
            null
        }
    }

    // 2. Lấy danh sách bài tập dựa trên danh sách ID cụ thể
    suspend fun getExercisesByIds(ids: List<String>): List<Exercise> {
        return postgrest.from("exercises").select {
            filter {
                isIn("id", ids)
            }
        }.decodeList<Exercise>()
    }

    // 3. Lấy các câu trả lời của một phiên cụ thể để biết bé đã làm những câu nào
    suspend fun getExerciseIdsFromSession(sessionId: String): List<String> {
        val answers = postgrest.from("session_answers").select {
            filter { eq("session_id", sessionId) }
        }.decodeList<SessionAnswer>()
        return answers.map { it.exerciseId }
    }
}