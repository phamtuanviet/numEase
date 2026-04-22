package com.example.numease.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.Auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreateNewPasswordViewModel @Inject constructor(
    private val auth: Auth // Dùng thẳng Auth theo NetworkModule đã chia nhỏ
) : ViewModel() {

    private val _state = MutableStateFlow(CreateNewPasswordState())
    val state: StateFlow<CreateNewPasswordState> = _state.asStateFlow()

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun resetPassword() {
        val currentState = _state.value

        if (currentState.password.isBlank() || currentState.confirmPassword.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập đầy đủ thông tin") }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(error = "Mật khẩu phải có ít nhất 6 ký tự") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Sửa updateUser thành modifyUser theo chuẩn Supabase Kotlin SDK
                auth.modifyUser {
                    password = currentState.password
                }

                // Đổi thành công, lập tức đăng xuất cái session tạm thời đi
                auth.signOut()

                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi cập nhật mật khẩu: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.value = CreateNewPasswordState()
    }
}