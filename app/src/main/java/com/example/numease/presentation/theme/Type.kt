package com.example.numease.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.numease.R
val NunitoFontFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_black, FontWeight.Black)
    // Nếu bạn tải thêm file bold, semibold thì cứ add thêm vào đây
)

// 2. Cài đặt Nunito làm font mặc định cho Typography
val Typography = Typography(
    // Dùng cho tiêu đề lớn (như Tên App ở Splash)
    displayLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        letterSpacing = 1.5.sp
    ),
    // Dùng cho Text thông thường trong app
    bodyLarge = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Dùng cho Slogan hoặc nhãn phụ
    titleMedium = TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 1.sp
    )
    /* Bạn có thể ghi đè thêm bodyMedium, labelLarge... tùy nhu cầu */
)