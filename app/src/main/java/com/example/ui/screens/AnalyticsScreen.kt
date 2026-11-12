package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ExpenseCategory
import com.example.domain.Formatters
import com.example.domain.YearMonthKey
import com.example.ui.viewmodel.CategorySummary
import com.example.ui.viewmodel.ExpenseDashboardUiState
import com.example.ui.viewmodel.MonthTrendPoint
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
  uiState: ExpenseDashboardUiState,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onSelectMonth: (YearMonthKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      MonthSelectorHeader(
        monthLabel = Formatters.formatMonthYear(uiState.selectedYearMonth),
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth,
      )
    }

    // 6-month trend card
    item {
      MonthlyTrendChartCard(
        points = uiState.sixMonthTrend,
        onSelectMonth = onSelectMonth,
      )
    }

    if (uiState.allMonthExpenses.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 1.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.PieChart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(44.dp),
            )
            Text(
              text = "Sem dados neste mês",
              style = MaterialTheme.typography.titleLarge,
            )
            Text(
              text = "Adicione gastos em ${Formatters.formatMonthYear(uiState.selectedYearMonth)} para visualizar gráficos por categoria e forma de pagamento.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      // Fixed vs Variable quick comparison
      item {
        FixedVsVariableCard(
          totalCents = uiState.totalSpentCents,
          recurringCents = uiState.recurringTotalCents,
        )
      }

      // Donut Chart & Category Breakdown
      item {
        CategoryDonutCard(
          totalSpentCents = uiState.totalSpentCents,
          summaries = uiState.categorySummaries,
        )
      }

      // Payment Methods
      if (uiState.paymentSummaries.isNotEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = "Por forma de pagamento",
                style = MaterialTheme.typography.titleLarge,
              )
              uiState.paymentSummaries.forEach { summary ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = "${summary.method.label} (${summary.count})",
                      style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                      text = "${Formatters.formatCurrencyCents(summary.totalCents)} • ${(summary.percentage * 100).roundToInt()}%",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(8.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant),
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth(summary.percentage.coerceIn(0.02f, 1f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Top 5 Highest Expenses
      if (uiState.topExpenses.isNotEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = "Maiores gastos do mês",
                style = MaterialTheme.typography.titleLarge,
              )
              uiState.topExpenses.forEachIndexed { index, expense ->
                val cat = ExpenseCategory.fromName(expense.category)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Text(
                    text = "${index + 1}º",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(24.dp),
                  )
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(cat.accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = cat.label,
                      tint = cat.accentColor,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = expense.title,
                      style = MaterialTheme.typography.titleSmall,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = "${cat.label} • ${Formatters.formatFullDate(expense.dateMillis)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Text(
                    text = Formatters.formatCurrencyCents(expense.amountCents),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MonthlyTrendChartCard(
  points: List<MonthTrendPoint>,
  onSelectMonth: (YearMonthKey) -> Unit,
) {
  val maxCents = (points.maxOfOrNull { it.totalCents } ?: 0L).coerceAtLeast(1L)
  val primaryColor = MaterialTheme.colorScheme.primary
  val inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
  val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("analytics_trend_card"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "Evolução dos últimos 6 meses",
          style = MaterialTheme.typography.titleLarge,
        )
        Text(
          text = "Toque em um mês para ver os detalhes",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
      ) {
        points.forEach { point ->
          val targetFraction = if (point.totalCents == 0L) {
            0.04f
          } else {
            (point.totalCents.toFloat() / maxCents.toFloat()).coerceIn(0.08f, 1f)
          }
          val animatedFraction by animateFloatAsState(
            targetValue = targetFraction,
            animationSpec = tween(durationMillis = 500),
            label = "bar_${point.yearMonth.asKey()}",
          )

          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSelectMonth(point.yearMonth) }
              .padding(vertical = 4.dp)
              .testTag("trend_bar_${point.yearMonth.asKey()}"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = if (point.totalCents > 0L) Formatters.formatCompactCurrencyCents(point.totalCents) else "–",
              style = MaterialTheme.typography.labelSmall,
              color = if (point.isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              maxLines = 1,
            )

            Canvas(
              modifier = Modifier
                .width(26.dp)
                .height(104.dp),
            ) {
              val corner = CornerRadius(8.dp.toPx(), 8.dp.toPx())
              drawRoundRect(
                color = trackColor,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = corner,
              )
              val barHeight = size.height * animatedFraction
              drawRoundRect(
                color = if (point.isSelected) primaryColor else inactiveColor,
                topLeft = Offset(0f, size.height - barHeight),
                size = Size(size.width, barHeight),
                cornerRadius = corner,
              )
            }

            Text(
              text = point.label,
              style = if (point.isSelected) {
                MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
              } else {
                MaterialTheme.typography.labelSmall
              },
              color = if (point.isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FixedVsVariableCard(
  totalCents: Long,
  recurringCents: Long,
) {
  val variableCents = (totalCents - recurringCents).coerceAtLeast(0L)
  val recurringPct = if (totalCents > 0L) (recurringCents * 100f / totalCents).roundToInt() else 0

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = "Gastos fixos ($recurringPct%)",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
          text = Formatters.formatCurrencyCents(recurringCents),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = "Gastos variáveis (${100 - recurringPct}%)",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
          text = Formatters.formatCurrencyCents(variableCents),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }
    }
  }
}

@Composable
private fun CategoryDonutCard(
  totalSpentCents: Long,
  summaries: List<CategorySummary>,
) {
  val sweepProgress by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 650),
    label = "donut_sweep",
  )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("analytics_donut_card"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Distribuição por categoria",
        style = MaterialTheme.typography.titleLarge,
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(210.dp),
        contentAlignment = Alignment.Center,
      ) {
        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(modifier = Modifier.size(186.dp)) {
          val strokeWidth = 26.dp.toPx()
          val diameter = size.minDimension - strokeWidth
          val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
          val arcSize = Size(diameter, diameter)

          drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth),
          )

          var startAngle = -90f
          summaries.forEach { summary ->
            val sweep = summary.percentage * 360f * sweepProgress
            if (sweep > 0.5f) {
              drawArc(
                color = summary.category.accentColor,
                startAngle = startAngle,
                sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
              )
              startAngle += sweep
            }
          }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = Formatters.formatCompactCurrencyCents(totalSpentCents),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        summaries.forEach { summary ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(summary.category.accentColor.copy(alpha = 0.14f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = summary.category.icon,
                contentDescription = summary.category.label,
                tint = summary.category.accentColor,
                modifier = Modifier.size(18.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "${summary.category.label} (${summary.count})",
                  style = MaterialTheme.typography.titleSmall,
                )
                Text(
                  text = "${Formatters.formatCurrencyCents(summary.totalCents)} • ${(summary.percentage * 100).roundToInt()}%",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              Spacer(modifier = Modifier.height(5.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(7.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth(summary.percentage.coerceIn(0.02f, 1f))
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(summary.category.accentColor),
                )
              }
            }
          }
        }
      }
    }
  }
}
