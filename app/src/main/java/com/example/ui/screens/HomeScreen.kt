package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.PaymentMethod
import com.example.domain.Formatters
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.ExpenseDashboardUiState

@Composable
fun HomeScreen(
  uiState: ExpenseDashboardUiState,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onOpenBudgetDialog: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onCategoryFilterClick: (ExpenseCategory?) -> Unit,
  onAddExpenseClick: () -> Unit,
  onEditExpenseClick: (ExpenseEntity) -> Unit,
  onDeleteExpenseClick: (ExpenseEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Month Switcher Header
    item {
      MonthSelectorHeader(
        monthLabel = Formatters.formatMonthYear(uiState.selectedYearMonth),
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth,
        onOpenBudgetDialog = onOpenBudgetDialog,
      )
    }

    // Hero Emerald Total Card
    item {
      HeroMonthlySummaryCard(
        uiState = uiState,
        onOpenBudgetDialog = onOpenBudgetDialog,
      )
    }

    // Search & Category Filters
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text(stringResource(R.string.search_expenses_hint)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.testTag("clear_search_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Close,
                  contentDescription = stringResource(R.string.cd_clear_search),
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_search_input"),
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item {
            FilterChip(
              selected = uiState.selectedCategoryFilter == null,
              onClick = { onCategoryFilterClick(null) },
              label = { Text(stringResource(R.string.all_categories)) },
              modifier = Modifier.testTag("filter_category_all"),
            )
          }
          items(ExpenseCategory.entries, key = { it.name }) { category ->
            val selected = uiState.selectedCategoryFilter == category
            FilterChip(
              selected = selected,
              onClick = { onCategoryFilterClick(category) },
              leadingIcon = {
                Icon(
                  imageVector = category.icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else category.accentColor,
                )
              },
              label = { Text(category.label) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("filter_category_${category.name.lowercase()}"),
            )
          }
        }
      }
    }

    // Empty State or Grouped Expense List
    if (!uiState.isLoading && uiState.dayGroups.isEmpty()) {
      item {
        EmptyExpensesCard(
          hasAnyMonthExpenses = uiState.allMonthExpenses.isNotEmpty(),
          onAddExpenseClick = onAddExpenseClick,
          onClearFilters = {
            onSearchQueryChange("")
            onCategoryFilterClick(null)
          },
        )
      }
    } else {
      uiState.dayGroups.forEach { group ->
        item(key = "header_${group.dayKey}") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = group.headerLabel,
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = Formatters.formatCurrencyCents(group.dayTotalCents),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        items(group.items, key = { it.id }) { expense ->
          ExpenseRowCard(
            expense = expense,
            onClick = { onEditExpenseClick(expense) },
            onDelete = { onDeleteExpenseClick(expense) },
          )
        }
      }
    }
  }
}

@Composable
fun MonthSelectorHeader(
  monthLabel: String,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onOpenBudgetDialog: (() -> Unit)? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
      ) {
        IconButton(
          onClick = onPreviousMonth,
          modifier = Modifier
            .size(48.dp)
            .testTag("month_previous_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_previous_month),
          )
        }
        Text(
          text = monthLabel,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(horizontal = 6.dp),
        )
        IconButton(
          onClick = onNextMonth,
          modifier = Modifier
            .size(48.dp)
            .testTag("month_next_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_next_month),
          )
        }
      }
    }

    if (onOpenBudgetDialog != null) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .clickable(onClick = onOpenBudgetDialog)
          .testTag("open_budget_dialog_button"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Meta",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
    }
  }
}

@Composable
private fun HeroMonthlySummaryCard(
  uiState: ExpenseDashboardUiState,
  onOpenBudgetDialog: () -> Unit,
) {
  val hasBudget = uiState.budgetLimitCents > 0L
  val rawProgress = if (hasBudget) {
    (uiState.totalSpentCents.toFloat() / uiState.budgetLimitCents.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }
  val animatedProgress by animateFloatAsState(
    targetValue = rawProgress,
    animationSpec = tween(durationMillis = 550),
    label = "budget_progress",
  )

  val budgetRatio = if (hasBudget) {
    uiState.totalSpentCents.toFloat() / uiState.budgetLimitCents.toFloat()
  } else {
    0f
  }
  val progressColor = when {
    budgetRatio >= 1.0f -> StatusDanger
    budgetRatio >= 0.8f -> StatusWarning
    else -> StatusSafe
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(26.dp))
      .background(
        brush = Brush.linearGradient(
          colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd),
        ),
      ),
  ) {
    // Subtle decorative Canvas circles in the background
    Canvas(modifier = Modifier.matchParentSize()) {
      drawCircle(
        color = Color.White.copy(alpha = 0.06f),
        radius = size.height * 0.65f,
        center = Offset(size.width * 0.92f, size.height * 0.15f),
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.04f),
        radius = size.height * 0.45f,
        center = Offset(size.width * 0.12f, size.height * 0.95f),
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = stringResource(R.string.monthly_total_label),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.82f),
          )
          Text(
            text = Formatters.formatCurrencyCents(uiState.totalSpentCents),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            modifier = Modifier.testTag("hero_total_amount"),
          )
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color.White.copy(alpha = 0.16f),
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpenBudgetDialog)
            .testTag("hero_budget_pill"),
        ) {
          Text(
            text = if (hasBudget) {
              "Meta ${Formatters.formatCompactCurrencyCents(uiState.budgetLimitCents)}"
            } else {
              "+ Definir meta"
            },
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      AnimatedVisibility(visible = hasBudget) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp),
          ) {
            val y = size.height / 2f
            drawLine(
              color = Color.White.copy(alpha = 0.22f),
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = size.height,
              cap = StrokeCap.Round,
            )
            if (animatedProgress > 0f) {
              drawLine(
                color = progressColor,
                start = Offset(0f, y),
                end = Offset(size.width * animatedProgress, y),
                strokeWidth = size.height,
                cap = StrokeCap.Round,
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            val pctText = "${(budgetRatio * 100).toInt()}% utilizado"
            Text(
              text = pctText,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f),
            )
            val remainingText = if (uiState.remainingBudgetCents >= 0L) {
              "${stringResource(R.string.budget_remaining)}: ${Formatters.formatCurrencyCents(uiState.remainingBudgetCents)}"
            } else {
              "${stringResource(R.string.budget_exceeded)}: ${Formatters.formatCurrencyCents(-uiState.remainingBudgetCents)}"
            }
            Text(
              text = remainingText,
              style = MaterialTheme.typography.labelMedium,
              color = if (uiState.remainingBudgetCents >= 0L) Color.White else StatusDanger,
            )
          }
        }
      }

      // 3-stat quick metrics strip
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.18f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          HeroStatColumn(
            label = stringResource(R.string.daily_average),
            value = Formatters.formatCompactCurrencyCents(uiState.dailyAverageCents),
          )
          HeroStatColumn(
            label = stringResource(R.string.highest_expense),
            value = uiState.highestExpense?.let {
              Formatters.formatCompactCurrencyCents(it.amountCents)
            } ?: "R$ 0",
          )
          HeroStatColumn(
            label = stringResource(R.string.expense_count),
            value = "${uiState.allMonthExpenses.size}",
          )
        }
      }
    }
  }
}

@Composable
private fun HeroStatColumn(label: String, value: String) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White.copy(alpha = 0.72f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall,
      color = Color.White,
    )
  }
}

@Composable
private fun ExpenseRowCard(
  expense: ExpenseEntity,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val category = ExpenseCategory.fromName(expense.category)
  val payment = PaymentMethod.fromName(expense.paymentMethod)

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag("expense_card_${expense.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(category.accentColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = category.icon,
          contentDescription = category.label,
          tint = category.accentColor,
          modifier = Modifier.size(22.dp),
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = expense.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          if (expense.isRecurring) {
            Icon(
              imageVector = Icons.Rounded.Repeat,
              contentDescription = "Gasto recorrente",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(15.dp),
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = category.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = payment.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        if (expense.notes.isNotBlank()) {
          Text(
            text = expense.notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = Formatters.formatCurrencyCents(expense.amountCents),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(48.dp)
          .testTag("delete_expense_${expense.id}"),
      ) {
        Icon(
          imageVector = Icons.Rounded.DeleteOutline,
          contentDescription = stringResource(R.string.cd_delete_expense),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun EmptyExpensesCard(
  hasAnyMonthExpenses: Boolean,
  onAddExpenseClick: () -> Unit,
  onClearFilters: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_empty_expenses_1794511223712),
        contentDescription = stringResource(R.string.cd_empty_wallet),
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(160.dp, 120.dp)
          .clip(RoundedCornerShape(18.dp)),
      )

      Text(
        text = if (hasAnyMonthExpenses) {
          stringResource(R.string.empty_search_title)
        } else {
          stringResource(R.string.empty_expenses_title)
        },
        style = MaterialTheme.typography.titleLarge,
      )

      Text(
        text = if (hasAnyMonthExpenses) {
          stringResource(R.string.empty_search_subtitle)
        } else {
          stringResource(R.string.empty_expenses_subtitle)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (hasAnyMonthExpenses) {
        TextButton(
          onClick = onClearFilters,
          modifier = Modifier.testTag("empty_clear_filters_button"),
        ) {
          Text("Limpar filtros")
        }
      } else {
        Button(
          onClick = onAddExpenseClick,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .height(48.dp)
            .testTag("empty_add_expense_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Registrar primeiro gasto")
        }
      }
    }
  }
}
