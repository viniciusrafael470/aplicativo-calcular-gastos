package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.PaymentMethod
import com.example.domain.Formatters
import com.example.domain.MathExpressionEvaluator
import com.example.domain.YearMonthKey
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

data class ExpenseDayGroup(
  val dayKey: String,
  val headerLabel: String,
  val dayTotalCents: Long,
  val items: List<ExpenseEntity>,
)

data class CategorySummary(
  val category: ExpenseCategory,
  val totalCents: Long,
  val percentage: Float,
  val count: Int,
)

data class PaymentSummary(
  val method: PaymentMethod,
  val totalCents: Long,
  val percentage: Float,
  val count: Int,
)

data class MonthTrendPoint(
  val yearMonth: YearMonthKey,
  val label: String,
  val totalCents: Long,
  val isSelected: Boolean,
)

data class QuickCalcItem(
  val id: Long,
  val label: String,
  val amountCents: Long,
)

data class CalculatorState(
  val expression: String = "",
  val previewCents: Long? = null,
  val expressionError: String? = null,
  val items: List<QuickCalcItem> = emptyList(),
  val adjustmentPercent: Int = 0, // negative = discount, positive = fee/tip
  val splitPeople: Int = 1,
) {
  val itemsSubtotalCents: Long
    get() = items.sumOf { it.amountCents }

  val baseTotalCents: Long
    get() = if (items.isNotEmpty()) {
      itemsSubtotalCents + (previewCents?.takeIf { expression.isNotBlank() } ?: 0L)
    } else {
      previewCents ?: 0L
    }

  val adjustmentCents: Long
    get() = ((baseTotalCents * adjustmentPercent) / 100.0).roundToLong()

  val finalTotalCents: Long
    get() = (baseTotalCents + adjustmentCents).coerceAtLeast(0L)

  val perPersonCents: Long
    get() = if (splitPeople > 0) (finalTotalCents.toDouble() / splitPeople).roundToLong() else finalTotalCents
}

data class ExpenseDashboardUiState(
  val selectedYearMonth: YearMonthKey = YearMonthKey.current(),
  val searchQuery: String = "",
  val selectedCategoryFilter: ExpenseCategory? = null,
  val dayGroups: List<ExpenseDayGroup> = emptyList(),
  val allMonthExpenses: List<ExpenseEntity> = emptyList(),
  val totalSpentCents: Long = 0L,
  val recurringTotalCents: Long = 0L,
  val budgetLimitCents: Long = 0L,
  val remainingBudgetCents: Long = 0L,
  val dailyAverageCents: Long = 0L,
  val remainingDailyAllowanceCents: Long = 0L,
  val highestExpense: ExpenseEntity? = null,
  val categorySummaries: List<CategorySummary> = emptyList(),
  val paymentSummaries: List<PaymentSummary> = emptyList(),
  val topExpenses: List<ExpenseEntity> = emptyList(),
  val sixMonthTrend: List<MonthTrendPoint> = emptyList(),
  val isLoading: Boolean = true,
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: ExpenseRepository by lazy {
    val db = AppDatabase.getInstance(application)
    ExpenseRepository(db.expenseDao())
  }

  private val selectedYearMonth = MutableStateFlow(YearMonthKey.current())
  private val searchQuery = MutableStateFlow("")
  private val selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)

  private val _calculatorState = MutableStateFlow(CalculatorState())
  val calculatorState: StateFlow<CalculatorState> = _calculatorState

  private var nextCalcItemId = 1L

  private val filterTuple = combine(
    selectedYearMonth,
    searchQuery,
    selectedCategoryFilter,
  ) { ym, query, category ->
    Triple(ym, query, category)
  }

  val uiState: StateFlow<ExpenseDashboardUiState> = combine(
    repository.allExpenses,
    repository.allBudgets,
    filterTuple,
  ) { expenses, budgets, (ym, query, categoryFilter) ->
    val (startMillis, endMillis) = ym.rangeMillis()
    val monthExpenses = expenses.filter { it.dateMillis in startMillis..endMillis }
    val totalSpent = monthExpenses.sumOf { it.amountCents }
    val recurringTotal = monthExpenses.filter { it.isRecurring }.sumOf { it.amountCents }

    val monthBudget = budgets.firstOrNull { it.yearMonthKey == ym.asKey() }?.limitCents
      ?: budgets.firstOrNull { it.yearMonthKey == DEFAULT_BUDGET_KEY }?.limitCents
      ?: 0L

    val remainingBudget = if (monthBudget > 0L) monthBudget - totalSpent else 0L

    val currentYm = YearMonthKey.current()
    val daysElapsed = when {
      ym == currentYm -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
      else -> ym.daysInMonth()
    }
    val totalDaysInMonth = ym.daysInMonth()
    val daysLeft = if (ym == currentYm) (totalDaysInMonth - daysElapsed + 1).coerceAtLeast(1) else totalDaysInMonth
    val dailyAverage = if (totalSpent > 0L) totalSpent / daysElapsed else 0L
    val remainingDailyAllowance = if (monthBudget > 0L && remainingBudget > 0L) {
      remainingBudget / daysLeft
    } else {
      0L
    }

    val filtered = monthExpenses.filter { item ->
      val matchesCat = categoryFilter == null || item.category == categoryFilter.name
      val q = query.trim()
      val matchesQuery = q.isEmpty() ||
        item.title.contains(q, ignoreCase = true) ||
        item.notes.contains(q, ignoreCase = true) ||
        ExpenseCategory.fromName(item.category).label.contains(q, ignoreCase = true)
      matchesCat && matchesQuery
    }

    val dayGroups = filtered
      .groupBy { Formatters.formatDayKey(it.dateMillis) }
      .entries
      .sortedByDescending { it.key }
      .map { (dayKey, items) ->
        ExpenseDayGroup(
          dayKey = dayKey,
          headerLabel = Formatters.formatDateHeader(items.first().dateMillis),
          dayTotalCents = items.sumOf { it.amountCents },
          items = items,
        )
      }

    val categorySummaries = ExpenseCategory.entries.mapNotNull { cat ->
      val catItems = monthExpenses.filter { it.category == cat.name }
      if (catItems.isEmpty()) return@mapNotNull null
      val catSum = catItems.sumOf { it.amountCents }
      val pct = if (totalSpent > 0L) (catSum.toFloat() / totalSpent.toFloat()) else 0f
      CategorySummary(
        category = cat,
        totalCents = catSum,
        percentage = pct,
        count = catItems.size,
      )
    }.sortedByDescending { it.totalCents }

    val paymentSummaries = PaymentMethod.entries.mapNotNull { method ->
      val methodItems = monthExpenses.filter { it.paymentMethod == method.name }
      if (methodItems.isEmpty()) return@mapNotNull null
      val sum = methodItems.sumOf { it.amountCents }
      val pct = if (totalSpent > 0L) (sum.toFloat() / totalSpent.toFloat()) else 0f
      PaymentSummary(
        method = method,
        totalCents = sum,
        percentage = pct,
        count = methodItems.size,
      )
    }.sortedByDescending { it.totalCents }

    val sixMonths = buildList {
      var cursor = ym
      repeat(6) {
        add(cursor)
        cursor = cursor.previous()
      }
    }.reversed().map { monthKey ->
      val (s, e) = monthKey.rangeMillis()
      val sum = expenses.filter { it.dateMillis in s..e }.sumOf { it.amountCents }
      MonthTrendPoint(
        yearMonth = monthKey,
        label = Formatters.formatShortMonth(monthKey),
        totalCents = sum,
        isSelected = monthKey == ym,
      )
    }

    ExpenseDashboardUiState(
      selectedYearMonth = ym,
      searchQuery = query,
      selectedCategoryFilter = categoryFilter,
      dayGroups = dayGroups,
      allMonthExpenses = monthExpenses,
      totalSpentCents = totalSpent,
      recurringTotalCents = recurringTotal,
      budgetLimitCents = monthBudget,
      remainingBudgetCents = remainingBudget,
      dailyAverageCents = dailyAverage,
      remainingDailyAllowanceCents = remainingDailyAllowance,
      highestExpense = monthExpenses.maxByOrNull { it.amountCents },
      categorySummaries = categorySummaries,
      paymentSummaries = paymentSummaries,
      topExpenses = monthExpenses.sortedByDescending { it.amountCents }.take(5),
      sixMonthTrend = sixMonths,
      isLoading = false,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000L),
    initialValue = ExpenseDashboardUiState(),
  )

  fun previousMonth() {
    selectedYearMonth.update { it.previous() }
  }

  fun nextMonth() {
    selectedYearMonth.update { it.next() }
  }

  fun selectMonth(ym: YearMonthKey) {
    selectedYearMonth.value = ym
  }

  fun updateSearchQuery(query: String) {
    searchQuery.value = query
  }

  fun toggleCategoryFilter(category: ExpenseCategory?) {
    selectedCategoryFilter.update { current ->
      if (current == category) null else category
    }
  }

  fun saveExpense(
    existingId: Long?,
    title: String,
    amountCents: Long,
    category: ExpenseCategory,
    paymentMethod: PaymentMethod,
    dateMillis: Long,
    notes: String,
    isRecurring: Boolean,
  ) {
    viewModelScope.launch {
      val cleanTitle = title.trim()
      val cleanNotes = notes.trim()
      if (existingId != null && existingId > 0L) {
        repository.updateExpense(
          ExpenseEntity(
            id = existingId,
            title = cleanTitle,
            amountCents = amountCents,
            category = category.name,
            paymentMethod = paymentMethod.name,
            dateMillis = dateMillis,
            notes = cleanNotes,
            isRecurring = isRecurring,
          )
        )
      } else {
        repository.insertExpense(
          ExpenseEntity(
            title = cleanTitle,
            amountCents = amountCents,
            category = category.name,
            paymentMethod = paymentMethod.name,
            dateMillis = dateMillis,
            notes = cleanNotes,
            isRecurring = isRecurring,
          )
        )
      }
      // Keep user on the month of the saved expense if it differs
      selectedYearMonth.value = YearMonthKey.fromMillis(dateMillis)
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
    }
  }

  fun restoreExpense(expense: ExpenseEntity) {
    viewModelScope.launch {
      repository.insertExpense(expense.copy(id = 0L))
    }
  }

  fun saveBudget(limitCents: Long, applyGlobally: Boolean) {
    viewModelScope.launch {
      val ymKey = selectedYearMonth.value.asKey()
      repository.upsertBudget(ymKey, limitCents)
      if (applyGlobally) {
        repository.upsertBudget(DEFAULT_BUDGET_KEY, limitCents)
      }
    }
  }

  // --- Calculator & Splitter logic ---

  fun updateCalcExpression(newExpr: String) {
    val result = if (newExpr.isBlank()) null else MathExpressionEvaluator.evaluateToCents(newExpr)
    _calculatorState.update { state ->
      state.copy(
        expression = newExpr,
        previewCents = result?.getOrNull(),
        expressionError = if (newExpr.isNotBlank() && result?.isFailure == true) {
          null // Don't nag while typing an incomplete expression like "12+"
        } else {
          null
        },
      )
    }
  }

  fun appendCalcToken(token: String) {
    val current = _calculatorState.value.expression
    val updated = when (token) {
      "C" -> ""
      "⌫" -> if (current.isNotEmpty()) current.dropLast(1) else ""
      else -> current + token
    }
    updateCalcExpression(updated)
  }

  fun evaluateCalcExpression() {
    val current = _calculatorState.value.expression
    if (current.isBlank()) return
    val result = MathExpressionEvaluator.evaluateToCents(current)
    result.fold(
      onSuccess = { cents ->
        _calculatorState.update {
          it.copy(
            expression = Formatters.formatEditableAmount(cents),
            previewCents = cents,
            expressionError = null,
          )
        }
      },
      onFailure = {
        _calculatorState.update {
          it.copy(expressionError = "Expressão inválida")
        }
      },
    )
  }

  fun addCalcItem(labelInput: String) {
    val expr = _calculatorState.value.expression
    val result = MathExpressionEvaluator.evaluateToCents(expr)
    result.fold(
      onSuccess = { cents ->
        if (cents <= 0L) {
          _calculatorState.update { it.copy(expressionError = "Digite um valor maior que zero") }
          return
        }
        val cleanLabel = labelInput.trim().ifEmpty { "Item ${_calculatorState.value.items.size + 1}" }
        val newItem = QuickCalcItem(
          id = nextCalcItemId++,
          label = cleanLabel,
          amountCents = cents,
        )
        _calculatorState.update { state ->
          state.copy(
            items = state.items + newItem,
            expression = "",
            previewCents = null,
            expressionError = null,
          )
        }
      },
      onFailure = {
        _calculatorState.update { it.copy(expressionError = "Digite um valor válido antes de adicionar") }
      },
    )
  }

  fun removeCalcItem(id: Long) {
    _calculatorState.update { state ->
      state.copy(items = state.items.filterNot { it.id == id })
    }
  }

  fun clearAllCalculator() {
    _calculatorState.value = CalculatorState()
  }

  fun updateAdjustmentPercent(percent: Int) {
    _calculatorState.update { it.copy(adjustmentPercent = percent.coerceIn(-50, 50)) }
  }

  fun updateSplitPeople(count: Int) {
    _calculatorState.update { it.copy(splitPeople = count.coerceIn(1, 20)) }
  }

  fun saveCalculatedExpense(
    title: String,
    category: ExpenseCategory,
    paymentMethod: PaymentMethod,
    savePerPersonShare: Boolean,
  ): Boolean {
    val calc = _calculatorState.value
    val amountToSave = if (savePerPersonShare && calc.splitPeople > 1) {
      calc.perPersonCents
    } else {
      calc.finalTotalCents
    }
    if (amountToSave <= 0L) return false

    val notesSummary = buildString {
      if (calc.items.isNotEmpty()) {
        append(calc.items.joinToString(", ") { "${it.label}: ${Formatters.formatCurrencyCents(it.amountCents)}" })
      }
      if (calc.adjustmentPercent != 0) {
        if (isNotEmpty()) append(" • ")
        val sign = if (calc.adjustmentPercent > 0) "+" else ""
        append("Ajuste $sign${calc.adjustmentPercent}%")
      }
      if (calc.splitPeople > 1) {
        if (isNotEmpty()) append(" • ")
        append(
          if (savePerPersonShare) {
            "Minha parte (1/${calc.splitPeople} de ${Formatters.formatCurrencyCents(calc.finalTotalCents)})"
          } else {
            "Total dividido em ${calc.splitPeople} pessoas (${Formatters.formatCurrencyCents(calc.perPersonCents)}/cada)"
          }
        )
      }
    }

    val cleanTitle = title.trim().ifEmpty { "Gasto calculado" }
    saveExpense(
      existingId = null,
      title = cleanTitle,
      amountCents = amountToSave,
      category = category,
      paymentMethod = paymentMethod,
      dateMillis = System.currentTimeMillis(),
      notes = notesSummary,
      isRecurring = false,
    )
    clearAllCalculator()
    return true
  }

  companion object {
    const val DEFAULT_BUDGET_KEY = "DEFAULT"
  }
}
