package com.example.numease.presentation.student.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.CalculationContent
import com.example.numease.data.model.ComparingContent
import com.example.numease.data.model.CountingContent
import com.example.numease.data.model.DragDropContent
import com.example.numease.data.model.Exercise
import com.example.numease.data.model.SessionAnswer
import com.example.numease.data.model.StudySession
import com.example.numease.data.repository.ExerciseRepository
import com.example.numease.manager.ChildSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import kotlin.collections.filter


sealed class ExerciseUiState {
    object Loading : ExerciseUiState()
    data class Playing(
        val exercises: List<Exercise>,
        val currentIndex: Int,
        val correctCount: Int
    ) : ExerciseUiState()
    data class Finished(val earnedStars: Int) : ExerciseUiState()
    data class Error(val message: String) : ExerciseUiState()
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository,
    private val childSessionManager: ChildSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    // Quản lý trạng thái cục bộ trong lúc chơi
    private val localAnswers = mutableListOf<SessionAnswer>()
    private var sessionStartTime: Long = 0L
    private var questionStartTime: Long = 0L

    // Lưu lại thông tin route để dùng khi tính điểm
    private var currentCategoryId: Int = 0
    private var currentLevel: Int = 0

    fun startSession(categoryId: Int, levelInDb: Int) {
        val childId = childSessionManager.activeChild.value?.id ?: return
        this.currentCategoryId = categoryId
        this.currentLevel = levelInDb

        viewModelScope.launch {
            try {
                _uiState.value = ExerciseUiState.Loading
                localAnswers.clear()

                // BƯỚC 1: Kiểm tra xem bé đã từng chơi màn này chưa
                val latestSessionId = repository.getLatestSessionId(childId, categoryId, levelInDb)

                val selectedExercises: List<Exercise>

                if (latestSessionId != null) {
                    // --- TRƯỜNG HỢP CHƠI LẠI (REPLAY) ---
                    // Lấy lại đúng những câu hỏi của phiên chơi gần nhất
                    val previousExerciseIds = repository.getExerciseIdsFromSession(latestSessionId)
                    selectedExercises = repository.getExercisesByIds(previousExerciseIds)
                } else {
                    // --- TRƯỜNG HỢP CHƠI MỚI (NEW PLAY) ---
                    val allExercises = repository.getExercises(categoryId, levelInDb)

                    // Lọc bỏ những câu đã từng làm ĐÚNG ở CÁC MÀN KHÁC (nếu cần)
                    // hoặc cứ bốc ngẫu nhiên 5 câu mới từ kho của Level này
                    val playedIds = repository.getAnsweredExerciseIds(childId, categoryId)
                    var unplayedExercises = allExercises.filter { it.id !in playedIds }

                    if (unplayedExercises.size < 5) unplayedExercises = allExercises

                    selectedExercises = unplayedExercises.shuffled().take(5)
                }

                if (selectedExercises.isEmpty()) {
                    _uiState.value = ExerciseUiState.Error("Không thể tải câu hỏi!")
                    return@launch
                }

                // Đánh dấu thời gian bắt đầu
                sessionStartTime = System.currentTimeMillis()
                questionStartTime = System.currentTimeMillis()

                _uiState.value = ExerciseUiState.Playing(
                    exercises = selectedExercises,
                    currentIndex = 0,
                    correctCount = 0
                )

            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error("Lỗi: ${e.message}")
            }
        }
    }

    // Hàm submit này nhận kiểu Any để xài chung cho số (Đếm), String (So sánh)
    fun submitAnswer(selectedAnswer: Any) {
        val currentState = _uiState.value
        if (currentState !is ExerciseUiState.Playing) return

        val currentExercise = currentState.exercises[currentState.currentIndex]
        val timeTaken = ((System.currentTimeMillis() - questionStartTime) / 1000).toInt()

        // So sánh đáp án dựa theo Type
        val isCorrect = when (val content = currentExercise.content) {
            is CountingContent -> selectedAnswer == content.correctAnswer
            is ComparingContent -> selectedAnswer == content.correctAnswer
            is DragDropContent -> selectedAnswer == 1 // Sẽ xử lý logic drag drop sau
            is CalculationContent -> selectedAnswer == content.correctAnswer

        }

        // Lưu tạm vào mảng local (chưa đẩy lên DB)
        // Dùng JsonPrimitive để chuyển Int/String thành JsonElement theo chuẩn Model
        localAnswers.add(
            SessionAnswer(
                sessionId = "", // Sẽ được gắn sau khi tạo Session
                exerciseId = currentExercise.id,
                isCorrect = isCorrect,
                timeTakenSeconds = timeTaken,
                userAnswer = JsonPrimitive(selectedAnswer.toString())
            )
        )

        val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex < currentState.exercises.size) {
            // Sang câu mới
            questionStartTime = System.currentTimeMillis()
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                correctCount = newCorrectCount
            )
        } else {
            // Hết bài -> Tổng hợp và gửi lên server
            finishSession(newCorrectCount, currentState.exercises.size)
        }
    }

    private fun finishSession(correctCount: Int, totalQuestions: Int) {
        val childId = childSessionManager.activeChild.value?.id ?: return

        viewModelScope.launch {
            try {
                _uiState.value = ExerciseUiState.Loading

                val totalDuration = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
                val accuracyRate = if (totalQuestions > 0) (correctCount.toDouble() / totalQuestions) * 100 else 0.0

                // Tính số sao
                val starsEarned = when (correctCount) {
                    5 -> 3
                    3, 4 -> 2
                    else -> 1
                }

                // Đóng gói StudySession theo đúng Model mới của bạn
                val finalSession = StudySession(
                    childProfileId = childId,
                    categoryId = currentCategoryId,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctCount,
                    accuracy = accuracyRate,
                    durationSeconds = totalDuration
                )

                // Gửi toàn bộ 1 cục lên Repository xử lý
                repository.saveFullSessionResults(
                    session = finalSession,
                    answers = localAnswers,
                    level = currentLevel,
                    starsEarned = starsEarned
                )

                _uiState.value = ExerciseUiState.Finished(earnedStars = starsEarned)

            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error("Lỗi lưu kết quả: ${e.message}")
            }
        }
    }
}