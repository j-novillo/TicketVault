package com.ticketvault.metrics

/**
 * Cálculo de métricas agregadas sobre tickets guardados.
 * Implementación pendiente (épica Métricas).
 */
data class MonthlyExpense(
    val month: String,
    val total: Double,
)

interface MetricsCalculator {
    suspend fun getMonthlyExpenses(): List<MonthlyExpense>
}
