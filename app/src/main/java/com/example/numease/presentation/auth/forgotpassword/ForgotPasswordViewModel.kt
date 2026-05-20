package com.example.numease.presentation.auth.forgotpassword


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val auth: Auth
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun sendRecoveryEmail() {
        val email = _state.value.email.trim()
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(error = "Vui lòng nhập một địa chỉ email hợp lệ.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Yêu cầu Supabase gửi email khôi phục
                auth.resetPasswordForEmail(email)

                // Thành công: Chỉ chuyển khi không có Exception văng ra
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Thất bại: Email không tồn tại, quá giới hạn, hoặc lỗi mạng
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = translateAuthError(e)
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetState() {
        _state.value = ForgotPasswordState()
    }

    // "Phiên dịch" lỗi từ Supabase sang Tiếng Việt
    private fun translateAuthError(exception: Exception): String {
        val message =
            exception.message?.lowercase() ?: return "Đã có lỗi xảy ra. Vui lòng thử lại sau."

        return when {
            message.contains("user not found") -> "Email này chưa được đăng ký trong hệ thống."
            message.contains("rate limit") -> "Bạn thao tác quá nhiều lần, vui lòng thử lại sau vài phút."
            message.contains("network") || message.contains("host") -> "Lỗi kết nối mạng. Vui lòng kiểm tra Internet."
            else -> "Không thể gửi yêu cầu: Khớp email thất bại."
        }
    }
}