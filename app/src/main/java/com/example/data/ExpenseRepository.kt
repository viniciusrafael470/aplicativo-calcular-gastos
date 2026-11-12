package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
  val allExpenses: Flow<List<ExpenseEntity>> = dao.observeAllExpenses()
  val allBudgets: Flow<List<MonthlyBudgetEntity>> = dao.observeAllBudgets()

  suspend fun insertExpense(expense: ExpenseEntity): Long = dao.insertExpense(expense)

  suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

  suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

  suspend fun upsertBudget(yearMonthKey: String, limitCents: Long) {
    dao.upsertBudget(
      MonthlyBudgetEntity(
        yearMonthKey = yearMonthKey,
        limitCents = limitCents,
        updatedAtMillis = System.currentTimeMillis(),
      )
    )
  }
}
