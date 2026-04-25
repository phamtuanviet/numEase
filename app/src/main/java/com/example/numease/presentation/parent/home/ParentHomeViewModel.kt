package com.example.numease.presentation.parent.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import com.example.numease.data.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentHomeViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : ViewModel() {

    private val _parentName = MutableStateFlow("Phụ huynh")
    val parentName: StateFlow<String> = _parentName.asStateFlow()

    // Lưu danh sách các bé để xử lý điều hướng Thống kê
    private val _childrenList = MutableStateFlow<List<ChildProfile>>(emptyList())
    val childrenList: StateFlow<List<ChildProfile>> = _childrenList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchParentData()
    }

    private fun fetchParentData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUserOrNull() ?: return@launch

                // Lấy tên phụ huynh
                val profile = postgrest.from("user_profiles").select {
                    filter { eq("id", user.id) }
                }.decodeSingleOrNull<UserProfile>()

                if (profile != null) {
                    _parentName.value = profile?.name ?: "Anh / Chị"
                }

                // Lấy danh sách các bé
                val children = postgrest.from("child_profiles").select {
                    filter { eq("account_id", user.id) }
                }.decodeList<ChildProfile>()

                _childrenList.value = children

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}