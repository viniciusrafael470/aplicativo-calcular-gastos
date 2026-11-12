package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.Formatters
import com.example.domain.MathExpressionEvaluator
import com.example.domain.YearMonthKey

@Composable
fun BudgetDialog(
  currentBudgetCents: Long,
  yearMonth: YearMonthKey,
  onDismiss: () -> Unit,
  onSave: (limitCents: Long, applyGlobally: Boolean) -> Unit,
) {
  var amountText by remember(currentBudgetCents) {
    mutableStateOf(
      if (currentBudgetCents > 0L) Formatters.formatEditableAmount(currentBudgetCents) else ""
    )
  }
  var applyGlobally by remember { mutableStateOf(true) }
  var errorText by remember { mutableStateOf<String?>(null) }

  val presets = listOf(150_000L, 250_000L, 400_000L, 600_000L)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Orçamento de ${Formatters.formatMonthYear(yearMonth)}",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Defina um limite mensal para acompanhar seus gastos com tranquilidade.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
          value = amountText,
          onValueChange = {
            amountText = it
            errorText = null
          },
          label = { Text("Limite mensal (R$)") },
          placeholder = { Text("Ex: 3000,00") },
          prefix = { Text("R$ ") },
          singleLine = true,
          isError = errorText != null,
          supportingText = errorText?.let { msg -> { Text(msg) } },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_amount_input"),
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          presets.forEach { presetCents ->
            FilterChip(
              selected = MathExpressionEvaluator.evaluateToCents(amountText).getOrNull() == presetCents,
              onClick = {
                amountText = Formatters.formatEditableAmount(presetCents)
                errorText = null
              },
              label = { Text(Formatters.formatCompactCurrencyCents(presetCents)) },
              modifier = Modifier.testTag("budget_preset_$presetCents"),
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        ) {
          Checkbox(
            checked = applyGlobally,
            onCheckedChange = { applyGlobally = it },
            modifier = Modifier.testTag("budget_apply_globally_checkbox"),
          )
          Text(
            text = "Usar como meta padrão nos outros meses",
            style = MaterialTheme.typography.bodyMedium,
          )
        }

        if (currentBudgetCents > 0L) {
          Spacer(modifier = Modifier.height(2.dp))
          TextButton(
            onClick = { onSave(0L, applyGlobally) },
            modifier = Modifier.testTag("budget_clear_button"),
          ) {
            Text(
              text = "Remover limite mensal",
              color = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsed = MathExpressionEvaluator.evaluateToCents(amountText).getOrNull()
          if (parsed == null || parsed <= 0L) {
            errorText = "Informe um valor válido maior que zero"
          } else {
            onSave(parsed, applyGlobally)
          }
        },
        modifier = Modifier.testTag("budget_save_button"),
      ) {
        Text("Salvar meta")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("budget_cancel_button"),
      ) {
        Text("Cancelar")
      }
    },
  )
}
