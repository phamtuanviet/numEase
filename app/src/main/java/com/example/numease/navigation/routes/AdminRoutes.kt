package com.example.numease.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
object AdminHomeRoute

// Màn hình Quản lý Ngân hàng Câu hỏi & Bài tập
@Serializable
object ManageContentRoute

// Màn hình Quản lý Người dùng (Phụ huynh, Học sinh, Phân quyền)
@Serializable
object ManageUsersRoute

@Serializable
data class ManageLevelsRoute(
    val categoryId: Int,
    val categoryName: String,
    val categoryCode: String
)


// Màn hình xem danh sách câu hỏi của 1 Level
@Serializable
data class ManageQuestionsRoute(val categoryId: Int, val categoryCode: String, val level: Int)

// Màn hình Form thêm câu hỏi (Tạm thời làm riêng cho COUNTING)
@Serializable
data class AddCountingQuestionRoute(val categoryId: Int, val categoryCode: String, val level: Int)

@Serializable
data class AddComparingQuestionRoute(val categoryId: Int, val categoryCode: String, val level: Int)

@Serializable
data class AddDragDropQuestionRoute(val categoryId: Int, val categoryCode: String, val level: Int)

// Dùng chung cho cả Phép cộng và Phép trừ
@Serializable
data class AddCalculationQuestionRoute(val categoryId: Int, val categoryCode: String, val level: Int)

@Serializable
object ManageParentsRoute

// Màn hình danh sách hồ sơ Học sinh (Dành riêng cho Admin)
@Serializable
object ManageChildrenAdminRoute

@Serializable
data class ChildDetailAdminRoute(val childId: String)



@Serializable
data class EditCountingQuestionRoute(val exerciseId: String, val categoryId: Int, val level: Int)

@Serializable
data class EditComparingQuestionRoute(val exerciseId: String, val categoryId: Int, val level: Int)

@Serializable
data class EditCalculationQuestionRoute(val exerciseId: String, val categoryId: Int, val categoryCode: String, val level: Int)

@Serializable
data class EditDragDropQuestionRoute(val exerciseId: String, val categoryId: Int, val level: Int)