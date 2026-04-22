package com.example.numease.presentation.auth.verify

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
class VerifyEmailViewModel @Inject constructor(
    private val auth : Auth,
    savedStateHandle: SavedStateHandle // Dùng để lấy email từ Navigation Argument
) : ViewModel() {

    // Lấy email được truyền từ màn hình Đăng ký sang
    // (Lưu ý: "email" phải khớp với tên biến trong Route ở bước 4)
    val userEmail: String = checkNotNull(savedStateHandle["email"])

    private val _state = MutableStateFlow(VerifyEmailState())
    val state: StateFlow<VerifyEmailState> = _state.asStateFlow()

    fun onOtpChange(code: String) {
        // Chỉ cho phép nhập tối đa 6 ký tự
        if (code.length <= 6) {
            _state.update { it.copy(otpCode = code, error = null) }
        }
    }

    fun verifyCode() {
        val currentState = _state.value
        if (currentState.otpCode.length < 6) {
            _state.update { it.copy(error = "Vui lòng nhập đủ mã 6 số") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Gọi hàm xác thực OTP của Supabase
                auth.verifyEmailOtp(
                    type = OtpType.Email.SIGNUP,
                    email = userEmail,
                    token = currentState.otpCode
                )
                // Thành công: Supabase sẽ tự động cập nhật Session và báo cho AuthViewModel tổng
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

    // Hàm hỗ trợ gửi lại mã (Resend)
    fun resendCode() {
        viewModelScope.launch {
            try {
                auth.resendEmail(OtpType.Email.SIGNUP, userEmail)
                // Có thể thêm 1 state để hiển thị Toast "Đã gửi lại mã"
            } catch (e: Exception) {
                _state.update { it.copy(error = "Lỗi khi gửi lại mã: ${e.message}") }
            }
        }
    }
}