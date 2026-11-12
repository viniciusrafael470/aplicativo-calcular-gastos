package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ExpenseCategory
import com.example.data.PaymentMethod
import com.example.domain.Formatters
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.viewmodel.CalculatorState
import com.example.ui.viewmodel.ExpenseDashboardUiState

@Composable
fun CalculatorScreen(
  calcState: CalculatorState,
  dashboardState: ExpenseDashboardUiState,
  onAppendToken: (String) -> Unit,
  onEvaluateExpression: () -> Unit,
  onAddItem: (String) -> Unit,
  onRemoveItem: (Long) -> Unit,
  onClearAll: () -> Unit,
  onAdjustmentChange: (Int) -> Unit,
  onSplitPeopleChange: (Int) -> Unit,
  onSaveCalculatedExpense: (
    title: String,
    category: ExpenseCategory,
    paymentMethod: PaymentMethod,
    savePerPersonShare: Boolean,
  ) -> Unit,
  onOpenBudgetDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var itemLabel by remember { mutableStateOf("") }
  var showSaveDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Summary & Split Result Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .background(
            brush = Brush.linearGradient(
              colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd),
            ),
          )
          .padding(20.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Calculadora de Gastos",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White.copy(alpha = 0.88f),
            )
            if (calcState.items.isNotEmpty() || calcState.expression.isNotEmpty() || calcState.adjustmentPercent != 0) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable(onClick = onClearAll)
                  .testTag("calc_clear_all_button"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = "Limpar tudo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = "Zerar",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                  )
                }
              }
            }
          }

          Text(
            text = Formatters.formatCurrencyCents(calcState.finalTotalCents),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            modifier = Modifier.testTag("calc_final_total_text"),
          )

          if (calcState.splitPeople > 1) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color.Black.copy(alpha = 0.2f),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Dividido por ${calcState.splitPeople} pessoas",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.White.copy(alpha = 0.88f),
                )
                Text(
                  text = "${Formatters.formatCurrencyCents(calcState.perPersonCents)} / pessoa",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.testTag("calc_per_person_text"),
                )
              }
            }
          }

          if (calcState.finalTotalCents > 0L) {
            Button(
              onClick = { showSaveDialog = true },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("calc_save_as_expense_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Lançar nos meus gastos")
            }
          }
        }
      }
    }

    // Expression Keypad + Add Item
    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = itemLabel,
            onValueChange = { itemLabel = it },
            label = { Text("Nome do item (opcional)") },
            placeholder = { Text("Ex: Café, Pizza, Uber…") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("calc_item_label_input"),
          )

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalAlignment = Alignment.End,
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Text(
                text = calcState.expression.ifEmpty { "0" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("calc_expression_display"),
              )
              Text(
                text = when {
                  calcState.expressionError != null -> calcState.expressionError
                  calcState.previewCents != null -> "= ${Formatters.formatCurrencyCents(calcState.previewCents)}"
                  else -> "Use os botões abaixo para somar ou dividir"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (calcState.expressionError != null) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.primary
                },
              )
            }
          }

          val rows = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf(",", "0", "⌫", "+"),
          )
          rows.forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              row.forEach { token ->
                val isOperator = token in listOf("÷", "×", "-", "+", "⌫")
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = if (isOperator) {
                    MaterialTheme.colorScheme.primaryContainer
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onAppendToken(token) }
                    .testTag("calc_key_$token"),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = token,
                      style = MaterialTheme.typography.titleLarge,
                      color = if (isOperator) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                    )
                  }
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = { onAppendToken("C") },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("calc_key_clear"),
            ) {
              Text("C")
            }
            OutlinedButton(
              onClick = onEvaluateExpression,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("calc_key_equals"),
            ) {
              Text("=")
            }
            Button(
              onClick = {
                onAddItem(itemLabel)
                itemLabel = ""
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .weight(2f)
                .height(48.dp)
                .testTag("calc_add_to_list_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Somar na lista")
            }
          }
        }
      }
    }

    // Running Tally List
    if (calcState.items.isNotEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 1.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Itens somados (${calcState.items.size})",
                style = MaterialTheme.typography.titleMedium,
              )
              Text(
                text = Formatters.formatCurrencyCents(calcState.itemsSubtotalCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
            }

            calcState.items.forEach { item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                  .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = item.label,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.weight(1f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = Formatters.formatCurrencyCents(item.amountCents),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                )
                IconButton(
                  onClick = { onRemoveItem(item.id) },
                  modifier = Modifier
                    .size(48.dp)
                    .testTag("calc_remove_item_${item.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remover item",
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            }
          }
        }
      }
    }

    // Discount / Tip & Split Controls
    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = "Desconto, taxa e divisão",
            style = MaterialTheme.typography.titleMedium,
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Ajuste percentual (${if (calcState.adjustmentPercent > 0) "+" else ""}${calcState.adjustmentPercent}%)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val percents = listOf(-15, -10, -5, 0, 10, 15)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              percents.forEach { pct ->
                val label = when {
                  pct > 0 -> "+$pct%"
                  else -> "$pct%"
                }
                FilterChip(
                  selected = calcState.adjustmentPercent == pct,
                  onClick = { onAdjustmentChange(pct) },
                  label = { Text(label) },
                  modifier = Modifier.testTag("calc_pct_$pct"),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Column {
                Text(
                  text = "Dividir conta",
                  style = MaterialTheme.typography.titleSmall,
                )
                Text(
                  text = "${calcState.splitPeople} ${if (calcState.splitPeople == 1) "pessoa" else "pessoas"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .clickable(enabled = calcState.splitPeople > 1) {
                    onSplitPeopleChange(calcState.splitPeople - 1)
                  }
                  .testTag("calc_split_minus"),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Diminuir pessoas",
                  )
                }
              }
              Text(
                text = "${calcState.splitPeople}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 6.dp),
              )
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .clickable(enabled = calcState.splitPeople < 20) {
                    onSplitPeopleChange(calcState.splitPeople + 1)
                  }
                  .testTag("calc_split_plus"),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Aumentar pessoas",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }
              }
            }
          }
        }
      }
    }

    // Daily Budget Allowance Helper Card
    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "Orçamento de ${Formatters.formatMonthYear(dashboardState.selectedYearMonth)}",
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = if (dashboardState.budgetLimitCents > 0L) {
                if (dashboardState.remainingBudgetCents > 0L) {
                  "Você ainda pode gastar cerca de ${Formatters.formatCurrencyCents(dashboardState.remainingDailyAllowanceCents)} por dia até o fim do mês."
                } else {
                  "Limite mensal atingido. Ajuste sua meta ou revise os lançamentos."
                }
              } else {
                "Defina uma meta mensal para calcular quanto você pode gastar por dia."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          TextButton(
            onClick = onOpenBudgetDialog,
            modifier = Modifier.testTag("calc_open_budget_button"),
          ) {
            Text(if (dashboardState.budgetLimitCents > 0L) "Editar" else "Definir")
          }
        }
      }
    }
  }

  if (showSaveDialog) {
    SaveCalculatedExpenseDialog(
      calcState = calcState,
      onDismiss = { showSaveDialog = false },
      onConfirm = { title, category, paymentMethod, savePerPerson ->
        onSaveCalculatedExpense(title, category, paymentMethod, savePerPerson)
        showSaveDialog = false
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SaveCalculatedExpenseDialog(
  calcState: CalculatorState,
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    category: ExpenseCategory,
    paymentMethod: PaymentMethod,
    savePerPersonShare: Boolean,
  ) -> Unit,
) {
  var title by remember {
    mutableStateOf(
      if (calcState.items.size == 1) calcState.items.first().label else "Compra calculada"
    )
  }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.ALIMENTACAO) }
  var selectedPayment by remember { mutableStateOf(PaymentMethod.PIX) }
  var savePerPerson by remember { mutableStateOf(calcState.splitPeople > 1) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Lançar gasto calculado") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Descrição") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("calc_save_title_input"),
        )

        if (calcState.splitPeople > 1) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Salvar apenas minha parte",
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = if (savePerPerson) {
                  Formatters.formatCurrencyCents(calcState.perPersonCents)
                } else {
                  "Total: ${Formatters.formatCurrencyCents(calcState.finalTotalCents)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Switch(
              checked = savePerPerson,
              onCheckedChange = { savePerPerson = it },
              modifier = Modifier.testTag("calc_save_per_person_switch"),
            )
          }
        } else {
          Text(
            text = "Valor: ${Formatters.formatCurrencyCents(calcState.finalTotalCents)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        Text(
          text = "Categoria",
          style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          ExpenseCategory.entries.forEach { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { selectedCategory = category },
              label = { Text(category.label) },
            )
          }
        }

        Text(
          text = "Pagamento",
          style = MaterialTheme.typography.labelLarge,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PaymentMethod.entries.forEach { method ->
            FilterChip(
              selected = selectedPayment == method,
              onClick = { selectedPayment = method },
              label = { Text(method.label) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(title, selectedCategory, selectedPayment, savePerPerson) },
        modifier = Modifier.testTag("calc_save_confirm_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Salvar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancelar")
      }
    },
  )
}
