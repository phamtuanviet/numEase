package com.example.numease.presentation.admin.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.UserProfile
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
    val email: String? = "Chưa cập nhật", // Để null an toàn, phòng trường hợp DB chưa sync kịp
    val role: String? = "USER",          // Thêm role theo đúng DB của bạn
    @SerialName("created_at") val createdAt: String? = null,

    // Thuộc tính này không có trong DB, ta dùng annotation @Transient hoặc gán mặc định để nó không bắt parse
    var children: List<ChildProfile> = emptyList()
)
@HiltViewModel
class ManageParentsViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _parents = MutableStateFlow<List<ParentUser>>(emptyList())
    val parents = _parents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentPage = MutableStateFlow(0) // Trang bắt đầu từ 0
    val currentPage = _currentPage.asStateFlow()

    private val _totalCount = MutableStateFlow(0L)
    val totalCount = _totalCount.asStateFlow()

    private val pageSize = 10

    init {
        loadParents()
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0 // Reset về trang đầu khi tìm kiếm
        loadParents()
    }

    fun nextPage() {
        if ((_currentPage.value + 1) * pageSize < _totalCount.value) {
            _currentPage.value += 1
            loadParents()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            loadParents()
        }
    }

    fun loadParents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val from = _currentPage.value * pageSize
                val to = from + pageSize - 1

                val response = postgrest.from("user_profiles").select {
                    if (_searchQuery.value.isNotEmpty()) {
                        filter { ilike("email", "%${_searchQuery.value}%") }
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

    // Hàm lấy danh sách con của 1 phụ huynh cụ thể
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
}