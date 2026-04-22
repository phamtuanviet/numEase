package com.example.numease.navigation.routes

import kotlinx.serialization.Serializable

// ==========================================
// CÁC GRAPH CHÍNH (Đại diện cho các luồng lớn)
// (Bạn có thể để ở đây để AppNavigation dễ gọi,
// hoặc tách về các file Feature tương ứng đều được)
// ==========================================
@Serializable object AuthGraph

@Serializable data object OnboardingGraph
@Serializable object ParentGraph
@Serializable object StudentGraph
@Serializable object AdminGraph

@Serializable object ProfileSelectionRoute
// ==========================================
// CÁC ĐƯỜNG DẪN CỐT LÕI TẦNG APP (Core Routes)
// ==========================================

/**
 * Màn hình đầu tiên khi mở App.
 * Nhiệm vụ: Hiển thị Logo và check phiên đăng nhập (Session).
 */
@Serializable
object SplashRoute

@Serializable
object RoleRouterRoute

/**
 * Trạm trung chuyển (Router) vô hình.
 * Nhiệm vụ: Đứng giữa để phân luồng người dùng vào đúng màn hình (Admin / Phụ huynh / Học sinh)
 * dựa trên Role (từ Supabase) và ViewMode (từ DataStore).
 */
