package com.example.numease.presentation.student.map



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.repository.MapRepository
import com.example.numease.manager.ChildSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Lớp data ẩn dùng nội bộ để tính toán Category và Level gửi xuống DB


@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: MapRepository,
    val childSessionManager: ChildSessionManager // Quản lý phiên của bé
) : ViewModel() {

    // State gửi sang UI
    private val _mapNodes = MutableStateFlow<List<MapNodeUI>>(emptyList())
    val mapNodes: StateFlow<List<MapNodeUI>> = _mapNodes.asStateFlow()

    private val _totalStars = MutableStateFlow(0)
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()

    init {
        loadMapData()
    }

    fun loadMapData() {
        val child = childSessionManager.activeChild.value ?: return

        viewModelScope.launch {
            // 1. Lấy dữ liệu từ DB
            val records = repository.getLevelRecords(child.id!!)

            // Tính tổng sao hiển thị ở góc trên cùng
            _totalStars.value = records.sumOf { it.stars }

            // 2. Xác định Cửa tiếp theo bé phải chơi
            // (Ví dụ: Qua 5 cửa thì cửa tiếp theo là 6)
            val currentUnlockedLevel = records.size + 1

            // 3. Render các cửa đã qua + 15 Cửa tương lai (Tạo cảm giác vô tận)
            val totalNodesToRender = currentUnlockedLevel + 14

            val uiNodes = mutableListOf<MapNodeUI>()

            for (levelId in 1..totalNodesToRender) {
                // Sinh thông số cấu hình cửa này
                val def = generateNodeDefinition(levelId)

                // Kiểm tra xem bé đã có điểm ở cửa này chưa
                val record = records.find {
                    it.categoryId == def.categoryId && it.level == def.levelInDb
                }

                // Gán trạng thái
                val state = when {
                    record != null -> NodeState.COMPLETED
                    levelId == currentUnlockedLevel -> NodeState.CURRENT
                    else -> NodeState.LOCKED
                }

                // Tạo Object UI và thêm vào mảng
                uiNodes.add(
                    MapNodeUI(
                        levelId = levelId,
                        zoneName = def.zoneName,
                        state = state,
                        stars = record?.stars ?: 0
                    )
                )
            }

            // Gửi dữ liệu đã xử lý xong sang UI
            _mapNodes.value = uiNodes
        }
    }

    // Hàm gọi khi User bấm vào 1 nút trên bản đồ để lấy chính xác thông tin gửi cho ExerciseScreen
    fun getNodeDefinition(levelId: Int): MapNodeDef {
        return generateNodeDefinition(levelId)
    }

    // ==========================================
    // THUẬT TOÁN SINH BẢN ĐỒ VÔ TẬN
    // ==========================================
    private fun generateNodeDefinition(levelId: Int): MapNodeDef {
        val index = levelId - 1

        // Chu kỳ mới 15 Cửa: 3 Đếm -> 3 Thả -> 3 So sánh -> 3 Cộng -> 3 Trừ
        val cycle = index / 15
        val step = index % 15

        // Xác định Category ID dựa trên vị trí Cửa trong chu kỳ
        val categoryId = when (step) {
            in 0..2 -> 1   // Đếm số
            in 3..5 -> 3   // Kéo thả
            in 6..8 -> 2   // So sánh
            in 9..11 -> 4  // Phép cộng (Mới)
            else -> 5      // Phép trừ (Mới)
        }

        // Cách tính level:
        // Vòng 1 sẽ đánh level 1, 2, 3.
        // Vòng 2 sẽ đánh level 4, 5, 6 (tăng độ khó dần lên).
        val levelInDb = (cycle * 3) + (step % 3) + 1

        // Gắn Biển Báo cho từng Khu Vực (Chỉ hiện ở cửa đầu tiên của mỗi Khu)
        val zoneName = when (step) {
            0 -> "🌲 Rừng Đếm Số (Vòng ${cycle + 1})"
            3 -> "🥕 Trại Kéo Thả (Vòng ${cycle + 1})"
            6 -> "🌊 Biển So Sánh (Vòng ${cycle + 1})"
            9 -> "➕ Thung Lũng Phép Cộng (Vòng ${cycle + 1})"
            12 -> "➖ Sa Mạc Phép Trừ (Vòng ${cycle + 1})"
            else -> null
        }

        return MapNodeDef(levelId, categoryId, levelInDb, zoneName)
    }
}