package com.example.numease.presentation.admin.children


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import io.github.jan.supabase.postgrest.query.Columns
@Serializable
data class ChildWithParent(
    val id: String,
    val name: String,
    val age: Int,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,

    // ĐÃ SỬA: Khớp đúng với tên cột trong DB của bạn
    @SerialName("account_id") val accountId: String,

    // Thuộc tính chứa kết quả Join (vẫn có thể đặt tên biến là parent cho dễ hiểu)
    val parent: ParentEmail? = null
)

@Serializable
data class ParentEmail(
    // Thêm dấu ? và gán giá trị mặc định
    val email: String? = ""
)

@HiltViewModel
class ManageChildrenAdminViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildWithParent>>(emptyList())
    val children = _children.asStateFlow()

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
        loadChildren()
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
        loadChildren()
    }

    fun loadChildren() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val from = _currentPage.value * pageSize
                val to = from + pageSize - 1

                // ĐÃ SỬA: Dùng Columns.raw() để bọc chuỗi Join lại
                val response = postgrest.from("child_profiles").select(
                    columns = Columns.raw("*, parent:user_profiles!inner(email)")
                ) {
                    if (_searchQuery.value.isNotEmpty()) {
                        // Lọc theo alias "parent" mà chúng ta đã đặt tên ở trên
                        filter { ilike("parent.email", "%${_searchQuery.value}%") }
                    }
                    range(from.toLong(), to.toLong())
                    count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                }

                _children.value = response.decodeList<ChildWithParent>()
                _totalCount.value = response.countOrNull() ?: 0
            } catch (e: Exception) {
                android.util.Log.e("ManageChildrenVM", "Lỗi: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun nextPage() { if ((_currentPage.value + 1) * pageSize < _totalCount.value) { _currentPage.value += 1; loadChildren() } }
    fun prevPage() { if (_currentPage.value > 0) { _currentPage.value -= 1; loadChildren() } }
}