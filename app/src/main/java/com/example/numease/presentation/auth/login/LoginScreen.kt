package com.example.numease.presentation.auth.login


import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.numease.presentation.viewmodel.AuthState
import com.example.numease.presentation.viewmodel.AuthViewModel
import androidx.compose.material3.CircularProgressIndicator
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToRouter: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // 1. Quản lý Focus để ẩn bàn phím
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Lắng nghe trạng thái Authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToRouter()
            viewModel.resetState()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            // 2. Xử lý khoảng cách với StatusBar (pin, wifi)
            .statusBarsPadding()
            // 3. Click ra ngoài thì ẩn bàn phím
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                // 4. Cho phép vuốt khi nội dung dài hoặc bàn phím hiện lên
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // --- TIÊU ĐỀ ---
            Text(
                text = "Chào mừng trở lại!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Đăng nhập để tiếp tục cùng NumEase",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- BÁO LỖI ---
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // --- INPUT FIELDS ---
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Quên mật khẩu?")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NÚT ĐĂNG NHẬP ---
            Button(
                onClick = {
                    focusManager.clearFocus() // Ẩn phím khi bấm nút
                    viewModel.loginWithEmail()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
//                    CircularProgressIndicator(size = 24.dp, color = Color.White)
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Đăng nhập")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("HOẶC", color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(24.dp))

            // --- NÚT GOOGLE ---
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.loginWithGoogle()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Tiếp tục với Google")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- ĐĂNG KÝ ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa có tài khoản?")
                TextButton(onClick = onNavigateToRegister) {
                    Text("Đăng ký ngay")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}