package com.misuper.backend.modules.statistics.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class SpendingByCategory(
    val categoryId: String,
    val categoryName: String,
    @Contextual val total: BigDecimal,
    val percentage: Double
)

@Serializable
data class SpendingByStore(
    val storeId: String? = null,
    val storeName: String,
    @Contextual val total: BigDecimal,
    val percentage: Double
)

@Serializable
data class MonthlySummary(
    val year: Int,
    val month: Int,
    @Contextual val total: BigDecimal,
    val purchaseCount: Int
)

@Serializable
data class BudgetProgress(
    val budgetId: String,
    val budgetName: String,
    @Contextual val budgetAmount: BigDecimal,
    @Contextual val spent: BigDecimal,
    val percentageUsed: Double,
    val period: String
)

@Serializable
data class SpendingByImportance(
    val importance: String,
    @Contextual val total: BigDecimal,
    val percentage: Double,
    val purchaseCount: Int,
    val itemCount: Int
)

@Serializable
data class StoreFrequency(
    val storeId: String? = null,
    val storeName: String,
    val purchaseCount: Int,
    @Contextual val totalSpent: BigDecimal,
    val percentage: Double
)

@Serializable
data class GroupStatsResponse(
    val totalSpent: String,
    val totalPurchases: Int,
    val averagePerPurchase: String,
    val spendingByCategory: List<SpendingByCategory>,
    val spendingByStore: List<SpendingByStore>,
    val monthlySummary: List<MonthlySummary>
)

@Serializable
data class MostPurchasedProduct(
    val productName: String,
    val count: Int,
    @Contextual val totalSpent: BigDecimal
)

@Serializable
data class MemberSpending(
    val userId: String,
    val userName: String,
    @Contextual val totalSpent: BigDecimal,
    val percentage: Double,
    val purchaseCount: Int
)
