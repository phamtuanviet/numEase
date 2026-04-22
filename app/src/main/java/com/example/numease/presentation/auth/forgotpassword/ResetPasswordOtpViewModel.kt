package com.example.numease.presentation.auth.forgotpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordOtpViewModel @Inject constructor(
    private val auth: Auth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Lấy email từ tham số điều hướng (Navigation arguments)
    val userEmail: String = checkNotNull(savedStateHandle["email"])

    private val _state = MutableStateFlow(ResetPasswordOtpState())
    val state: StateFlow<ResetPasswordOtpState> = _state.asStateFlow()

    fun onOtpChange(code: String) {
        if (code.length <= 6) {
            _state.update { it.copy(otpCode = code, error = null) }
        }
    }

    fun verifyRecoveryOtp() {
        val currentState = _state.value
        if (currentState.otpCode.length < 6) {
            _state.update { it.copy(error = "Vui lòng nhập đủ mã 6 số") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Xác thực OTP dành riêng cho việc Khôi phục mật khẩu (RECOVERY)
                auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = userEmail,
                    token = currentState.otpCode
                )

                // Thành công: Người dùng đã được cấp quyền tạm thời
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Mã xác nhận không đúng hoặc đã hết hạn"
                    )
                }
            }
        }
    }

    fun resendRecoveryEmail() {
        viewModelScope.launch {
            try {
                // Để gửi lại mã, ta gọi lại hàm reset giống ở Phần 1
                auth.resetPasswordForEmail(userEmail)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Lỗi khi gửi lại mã: ${e.message}") }
            }
        }
    }

    fun resetState() {
        _state.value = ResetPasswordOtpState()
    }
}