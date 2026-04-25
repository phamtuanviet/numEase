package com.example.numease.presentation.parent.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.StudySession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageChildrenViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildProfile>>(emptyList())
    val children: StateFlow<List<ChildProfile>> = _children.asStateFlow()

    private val _currentChildStats = MutableStateFlow<List<StudySession>>(emptyList())
    val currentChildStats: StateFlow<List<StudySession>> = _currentChildStats.asStateFlow()

    val showFormDialog = MutableStateFlow(false)
    val editingChildId = MutableStateFlow<String?>(null) // null = Thêm mới

    val formName = MutableStateFlow("")
    val formGender = MutableStateFlow("MALE")
    val formAge = MutableStateFlow("")
    val isFormLoading = MutableStateFlow(false)

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            try {
                val user = auth.currentUserOrNull() ?: return@launch
                val list = postgrest.from("child_profiles").select {
                    filter { eq("account_id", user.id) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }.decodeList<ChildProfile>()

                _children.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Hàm này sẽ được gọi mỗi khi phụ huynh lướt (swipe) sang thẻ của bé khác
    fun loadStatsForChild(childId: String?) {
        if (childId == null) {
            _currentChildStats.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val sessions = postgrest.from("study_sessions").select {
                    filter { eq("child_profile_id", childId) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(10)
                }.decodeList<StudySession>()

                // Đảo ngược để vẽ biểu đồ từ cũ (trái) sang mới (phải)
                _currentChildStats.value = sessions.reversed()
            } catch (e: Exception) {
                e.printStackTrace()
                _currentChildStats.value = emptyList()
            }
        }
    }

    fun openForm(child: ChildProfile?) {
        if (child != null) {
            editingChildId.value = child.id
            formName.value = child.name
            formGender.value = child.gender ?: "MALE"
            formAge.value = "6" // Giả sử load từ DB
        } else {
            editingChildId.value = null
            formName.value = ""
            formGender.value = "MALE"
            formAge.value = ""
        }
        showFormDialog.value = true
    }

    fun closeForm() {
        showFormDialog.value = false
    }

    // Hàm saveChild trong ViewModel
    fun saveChild() {
        val user = auth.currentUserOrNull() ?: return
        if (formName.value.isBlank() || formAge.value.isBlank()) return

        viewModelScope.launch {
            isFormLoading.value = true
            try {
                // Sử dụng Model chuẩn để tránh lỗi Any Serializer
                if (editingChildId.value == null) {
                    // TẠO OBJECT MỚI THEO MODEL CHILDPROFILE
                    val newChild = ChildProfile(
                        accountId = user.id,
                        name = formName.value,
                        age = formAge.value.toInt(),
                        gender = formGender.value,
                        currentLevel = 1
                    )
                    postgrest.from("child_profiles").insert(newChild)
                } else {
                    // CẬP NHẬT
                    postgrest.from("child_profiles").update(
                        {
                            set("name", formName.value)
                            set("gender", formGender.value)
                            set("age", formAge.value.toInt())
                        }
                    ) {
                        filter { eq("id", editingChildId.value!!) }
                    }
                }

                closeForm()
                loadChildren() // Tải lại danh sách, LaunchedEffect ở Screen sẽ đưa bé về trang 0
            } catch (e: Exception) {
                android.util.Log.e("SaveChildError", "Lỗi: ${e.message}")
            } finally {
                isFormLoading.value = false
            }
        }
    }
}