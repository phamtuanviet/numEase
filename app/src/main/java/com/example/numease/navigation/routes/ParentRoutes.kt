package com.example.numease.navigation.routes

import kotlinx.serialization.Serializable

// Tuyến đường gốc của biểu đồ Phụ huynh

// Màn hình chính (Dashboard tổng quan)
@Serializable
object ParentHomeRoute

// Màn hình Danh sách quản lý bé (Thêm, sửa, xóa)
@Serializable
object ManageChildrenRoute

// Màn hình Form Thêm/Sửa thông tin 1 bé (Nếu childId = null -> Thêm mới)
@Serializable
data class AddEditChildRoute(val childId: String? = null)

// Màn hình Thống kê chi tiết của 1 bé cụ thể
@Serializable
data class ChildStatsRoute(val childId: String)