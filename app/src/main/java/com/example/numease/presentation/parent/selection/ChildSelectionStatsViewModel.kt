package com.example.numease.presentation.parent.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.numease.data.model.ChildProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChildSelectionStatsViewModel @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildProfile>>(emptyList())
    val children = _children.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = auth.currentUserOrNull() ?: return@launch
                val list = postgrest.from("child_profiles").select {
                    filter { eq("account_id", user.id) }
                }.decodeList<ChildProfile>()
                _children.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}