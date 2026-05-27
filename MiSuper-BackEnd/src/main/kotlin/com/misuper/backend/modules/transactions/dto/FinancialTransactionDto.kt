package com.misuper.backend.modules.transactions.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class CreateFinancialTransactionRequest(
    val groupId: String,
    val type: String,
    val category: String,
    val amount: Double,
    val description: String? = null,
    val transactionDate: String? = null
)

@Serializable
data class FinancialTransactionResponse(
    val id: String,
    val groupId: String,
    val userId: String,
    val userName: String,
    val type: String,
    val category: String,
    @Contextual val amount: BigDecimal,
    val description: String? = null,
    @Contextual val transactionDate: LocalDateTime,
    @Contextual val createdAt: LocalDateTime
)

@Serializable
data class FinancialSummaryResponse(
    @Contextual val income: BigDecimal,
    @Contextual val expense: BigDecimal,
    @Contextual val balance: BigDecimal
)
