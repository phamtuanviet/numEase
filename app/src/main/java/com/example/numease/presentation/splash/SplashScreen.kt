import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.numease.R // Thay bằng package R của bạn
import com.example.numease.presentation.viewmodel.AuthState
import com.example.numease.presentation.viewmodel.AuthViewModel
import androidx.compose.ui.text.font.FontFamily

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToRouter: () -> Unit
) {
    // 1. Lấy trạng thái Auth hiện tại
    val authState by authViewModel.authState.collectAsState()

    // 2. Biến trạng thái để chạy Animation và đếm thời gian tối thiểu
    var startAnimation by remember { mutableStateOf(false) }
    var isMinDelayPassed by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Hiệu ứng mờ dần (Fade-in)
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), // Fade in trong 1 giây
        label = "splash_fade_in"
    )

    // 3. Khởi động Timer và Animation ngay khi mở màn hình
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1500) // Đảm bảo Splash Screen hiện ít nhất 1.5 giây
        isMinDelayPassed = true
    }

    // 4. Lắng nghe cả 2 điều kiện: Hết thời gian chờ VÀ AuthState đã load xong
    LaunchedEffect(authState, isMinDelayPassed) {
        if (isMinDelayPassed) {
            when (authState) {
                is AuthState.Authenticated -> {
                    onNavigateToRouter()
                }
                is AuthState.Unauthenticated -> {
                    onNavigateToAuth()
                }
                is AuthState.Banned -> {
                    // TÀI KHOẢN BỊ KHÓA SẼ RƠI VÀO ĐÂY
                    Log.d("SplashScreen_Flow", "🚫 Tài khoản bị khóa. Đá ra màn Login.")

                    // Hiện thông báo cho User
                    Toast.makeText(context, "Tài khoản của bạn đã bị khóa bởi Quản trị viên!", Toast.LENGTH_LONG).show()

                    // Điều hướng về Login
                    onNavigateToAuth()
                }
                is AuthState.Loading -> {
                    Log.d("SplashScreen_Flow", "⏳ AuthState vẫn đang Loading...")
                }
            }
        }
    }

    // 5. Giao diện người dùng
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo App
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "NumEase Logo",
            modifier = Modifier
                .size(150.dp)
                .alpha(alphaAnim)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tên App - Gọi thẳng style displayLarge từ Theme
        Text(
            text = "NumEase",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(alphaAnim)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Slogan - Gọi thẳng style titleMedium từ Theme
        Text(
            text = "Học Toán Thật Vui",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.alpha(alphaAnim)
        )
    }
}