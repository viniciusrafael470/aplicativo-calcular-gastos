package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY dateMillis DESC, createdAtMillis DESC")
  fun observeAllExpenses(): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Update
  suspend fun updateExpense(expense: ExpenseEntity)

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteExpenseById(id: Long)

  @Query("SELECT * FROM monthly_budgets")
  fun observeAllBudgets(): Flow<List<MonthlyBudgetEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertBudget(budget: MonthlyBudgetEntity)
}
