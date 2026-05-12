package com.example.numease.presentation.admin.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class ParentUser(
    val id: String,
    val email: String? = "Chưa cập nhật",
    val role: String? = "PARENT", // Khớp với Database của bạn
    @SerialName("created_at") val createdAt: String? = null,
    var children: List<ChildProfile> = emptyList(),
    @SerialName("is_banned") val isBanned: Boolean = false,
)

// Đặt tên Enum trùng với Role trong DB để query trực tiếp
enum class AccountTab(val title: String) {
    ADMIN("Admin"),
    PARENT("Phụ huynh"),
    STUDENT("Học sinh")
}

@HiltViewModel
class ManageParentsViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AccountTab.ADMIN)
    val currentTab = _currentTab.asStateFlow()

    private val _parents = MutableStateFlow<List<ParentUser>>(emptyList())
    val parents = _parents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _totalCount = MutableStateFlow(0L)
    val totalCount = _totalCount.asStateFlow()

    private val pageSize = 10

    init {
        loadData()
    }

    fun onTabSelected(tab: AccountTab) {
        if (_currentTab.value != tab) {
            _currentTab.value = tab
            _searchQuery.value = ""
            _currentPage.value = 0
            loadData()
        }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
        loadData()
    }

    fun nextPage() {
        if ((_currentPage.value + 1) * pageSize < _totalCount.value) {
            _currentPage.value += 1
            loadData()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            loadData()
        }
    }

    // TẢI DỮ LIỆU TỪ 1 BẢNG DUY NHẤT "user_profiles" CHO CẢ 3 TAB
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val from = _currentPage.value * pageSize
                val to = from + pageSize - 1
                val queryText = _searchQuery.value.trim()

                // Lấy Role trực tiếp từ tên của Tab (ADMIN, PARENT, STUDENT)
                val roleFilter = _currentTab.value.name

                val response = postgrest.from("user_profiles").select {
                    filter {
                        eq("role", roleFilter)
                        if (queryText.isNotEmpty()) {
                            ilike("email", "%$queryText%")
                        }
                    }
                    range(from.toLong(), to.toLong())
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }

                _parents.value = response.decodeList<ParentUser>()
                _totalCount.value = response.countOrNull() ?: 0

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Giữ nguyên hàm này để phục vụ tính năng Mở rộng (Expand) cho thẻ Phụ huynh
    fun loadChildrenForParent(parentId: String, onResult: (List<ChildProfile>) -> Unit) {
        viewModelScope.launch {
            try {
                val children = postgrest.from("child_profiles").select {
                    filter { eq("account_id", parentId) }
                }.decodeList<ChildProfile>()
                onResult(children)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun promoteToAdmin(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Update cột role thành ADMIN
                postgrest.from("user_profiles").update(
                    { set("role", "ADMIN") }
                ) { filter { eq("id", userId) } }

                loadData() // Tải lại danh sách để UI tự cập nhật
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hàm Khóa / Mở khóa tài khoản
    fun toggleBanUser(userId: String, currentBanStatus: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Đảo ngược trạng thái khóa (đang false thành true, và ngược lại)
                postgrest.from("user_profiles").update(
                    { set("is_banned", !currentBanStatus) }
                ) { filter { eq("id", userId) } }

                loadData()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}