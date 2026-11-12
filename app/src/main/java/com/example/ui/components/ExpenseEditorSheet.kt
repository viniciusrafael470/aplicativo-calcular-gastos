package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ExpenseCategory
import com.example.data.ExpenseEntity
import com.example.data.PaymentMethod
import com.example.domain.Formatters
import com.example.domain.MathExpressionEvaluator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseEditorSheet(
  editingExpense: ExpenseEntity?,
  initialAmountCents: Long? = null,
  onDismiss: () -> Unit,
  onSave: (
    existingId: Long?,
    title: String,
    amountCents: Long,
    category: ExpenseCategory,
    paymentMethod: PaymentMethod,
    dateMillis: Long,
    notes: String,
    isRecurring: Boolean,
  ) -> Unit,
  onDelete: ((ExpenseEntity) -> Unit)? = null,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var title by remember(editingExpense) { mutableStateOf(editingExpense?.title.orEmpty()) }
  var amountText by remember(editingExpense, initialAmountCents) {
    val cents = editingExpense?.amountCents ?: initialAmountCents ?: 0L
    mutableStateOf(if (cents > 0L) Formatters.formatEditableAmount(cents) else "")
  }
  var selectedCategory by remember(editingExpense) {
    mutableStateOf(
      editingExpense?.let { ExpenseCategory.fromName(it.category) } ?: ExpenseCategory.ALIMENTACAO
    )
  }
  var selectedPayment by remember(editingExpense) {
    mutableStateOf(
      editingExpense?.let { PaymentMethod.fromName(it.paymentMethod) } ?: PaymentMethod.PIX
    )
  }
  var dateMillis by remember(editingExpense) {
    mutableLongStateOf(editingExpense?.dateMillis ?: System.currentTimeMillis())
  }
  var notes by remember(editingExpense) { mutableStateOf(editingExpense?.notes.orEmpty()) }
  var isRecurring by remember(editingExpense) { mutableStateOf(editingExpense?.isRecurring ?: false) }

  var showInlineCalculator by remember { mutableStateOf(false) }
  var showDatePicker by remember { mutableStateOf(false) }
  var titleError by remember { mutableStateOf<String?>(null) }
  var amountError by remember { mutableStateOf<String?>(null) }

  val evaluatedPreviewCents = remember(amountText) {
    if (amountText.isBlank()) null else MathExpressionEvaluator.evaluateToCents(amountText).getOrNull()
  }

  val titleSuggestions = listOf("Supermercado", "Almoço", "Uber", "Café", "Farmácia", "Internet")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .navigationBarsPadding()
        .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (editingExpense == null) {
            stringResource(R.string.add_expense)
          } else {
            stringResource(R.string.edit_expense)
          },
          style = MaterialTheme.typography.headlineSmall,
        )
        if (editingExpense != null && onDelete != null) {
          IconButton(
            onClick = { onDelete(editingExpense) },
            modifier = Modifier.testTag("editor_delete_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteOutline,
              contentDescription = stringResource(R.string.cd_delete_expense),
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      // Amount input with inline calculator toggle
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
          value = amountText,
          onValueChange = {
            amountText = it
            amountError = null
          },
          label = { Text("Valor ou expressão (ex: 45,90 + 12)") },
          placeholder = { Text("0,00") },
          prefix = { Text("R$ ", fontWeight = FontWeight.SemiBold) },
          trailingIcon = {
            IconButton(
              onClick = { showInlineCalculator = !showInlineCalculator },
              modifier = Modifier.testTag("editor_toggle_calc_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Calculate,
                contentDescription = stringResource(R.string.cd_toggle_calculator),
                tint = if (showInlineCalculator) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              )
            }
          },
          singleLine = true,
          isError = amountError != null,
          supportingText = {
            when {
              amountError != null -> Text(amountError!!)
              evaluatedPreviewCents != null && (amountText.contains("+") ||
                amountText.contains("-") ||
                amountText.contains("*") ||
                amountText.contains("×") ||
                amountText.contains("/") ||
                amountText.contains("÷")) -> {
                Text(
                  text = "= ${Formatters.formatCurrencyCents(evaluatedPreviewCents)}",
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_amount_input"),
        )

        AnimatedVisibility(visible = showInlineCalculator) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              val rows = listOf(
                listOf("7", "8", "9", "÷"),
                listOf("4", "5", "6", "×"),
                listOf("1", "2", "3", "-"),
                listOf(",", "0", "⌫", "+"),
              )
              rows.forEach { rowTokens ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  rowTokens.forEach { token ->
                    val isOp = token in listOf("÷", "×", "-", "+", "⌫")
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = if (isOp) {
                        MaterialTheme.colorScheme.primaryContainer
                      } else {
                        MaterialTheme.colorScheme.surface
                      },
                      modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                          amountError = null
                          amountText = when (token) {
                            "⌫" -> if (amountText.isNotEmpty()) amountText.dropLast(1) else ""
                            else -> amountText + token
                          }
                        }
                        .testTag("inline_calc_key_$token"),
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Text(
                          text = token,
                          style = MaterialTheme.typography.titleMedium,
                          color = if (isOp) {
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
                  onClick = { amountText = "" },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("inline_calc_clear"),
                ) {
                  Text("Limpar")
                }
                Button(
                  onClick = {
                    val result = MathExpressionEvaluator.evaluateToCents(amountText).getOrNull()
                    if (result != null && result > 0L) {
                      amountText = Formatters.formatEditableAmount(result)
                      amountError = null
                    } else {
                      amountError = "Expressão inválida"
                    }
                  },
                  modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .testTag("inline_calc_equals"),
                ) {
                  Text("Calcular (=)")
                }
              }
            }
          }
        }
      }

      // Description field + quick suggestions
      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          titleError = null
        },
        label = { Text("Descrição") },
        placeholder = { Text("Ex: Almoço no centro") },
        singleLine = true,
        isError = titleError != null,
        supportingText = titleError?.let { msg -> { Text(msg) } },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("editor_title_input"),
      )

      if (editingExpense == null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          titleSuggestions.forEach { suggestion ->
            FilterChip(
              selected = title.equals(suggestion, ignoreCase = true),
              onClick = {
                title = suggestion
                titleError = null
                selectedCategory = when (suggestion) {
                  "Supermercado", "Almoço", "Café" -> ExpenseCategory.ALIMENTACAO
                  "Uber" -> ExpenseCategory.TRANSPORTE
                  "Farmácia" -> ExpenseCategory.SAUDE
                  "Internet" -> ExpenseCategory.CONTAS
                  else -> ExpenseCategory.OUTROS
                }
              },
              label = { Text(suggestion) },
              modifier = Modifier.testTag("suggestion_chip_$suggestion"),
            )
          }
        }
      }

      // Category selection
      Text(
        text = "Categoria",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        ExpenseCategory.entries.forEach { category ->
          val selected = selectedCategory == category
          FilterChip(
            selected = selected,
            onClick = { selectedCategory = category },
            leadingIcon = {
              Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else category.accentColor,
              )
            },
            label = { Text(category.label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.testTag("editor_category_${category.name.lowercase()}"),
          )
        }
      }

      // Payment Method
      Text(
        text = "Forma de pagamento",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        PaymentMethod.entries.forEach { method ->
          FilterChip(
            selected = selectedPayment == method,
            onClick = { selectedPayment = method },
            label = { Text(method.label) },
            modifier = Modifier.testTag("editor_payment_${method.name.lowercase()}"),
          )
        }
      }

      // Date selector
      Text(
        text = "Data",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val todayHeader = Formatters.formatDateHeader(dateMillis)
        FilterChip(
          selected = todayHeader == "Hoje",
          onClick = { dateMillis = System.currentTimeMillis() },
          label = { Text("Hoje") },
          modifier = Modifier.testTag("editor_date_today"),
        )
        FilterChip(
          selected = todayHeader == "Ontem",
          onClick = {
            dateMillis = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
          },
          label = { Text("Ontem") },
          modifier = Modifier.testTag("editor_date_yesterday"),
        )
        OutlinedButton(
          onClick = { showDatePicker = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .height(48.dp)
            .testTag("editor_date_pick_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(Formatters.formatFullDate(dateMillis))
        }
      }

      // Recurring switch
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isRecurring = !isRecurring }
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Gasto fixo mensal",
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = "Destaca contas recorrentes nas análises",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = isRecurring,
            onCheckedChange = { isRecurring = it },
            modifier = Modifier.testTag("editor_recurring_switch"),
          )
        }
      }

      // Notes
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Observação (opcional)") },
        placeholder = { Text("Ex: Parcela 2/6, dividido com amigos…") },
        maxLines = 3,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("editor_notes_input"),
      )

      Spacer(modifier = Modifier.height(4.dp))

      Button(
        onClick = {
          val parsedCents = MathExpressionEvaluator.evaluateToCents(amountText).getOrNull()
          var valid = true
          if (parsedCents == null || parsedCents <= 0L) {
            amountError = "Informe um valor maior que zero"
            valid = false
          }
          if (title.trim().isEmpty()) {
            titleError = "Informe uma descrição para o gasto"
            valid = false
          }
          if (valid && parsedCents != null) {
            onSave(
              editingExpense?.id,
              title,
              parsedCents,
              selectedCategory,
              selectedPayment,
              dateMillis,
              notes,
              isRecurring,
            )
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("editor_save_button"),
      ) {
        Text(
          text = if (editingExpense == null) "Salvar gasto" else "Atualizar gasto",
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }

  if (showDatePicker) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            pickerState.selectedDateMillis?.let { utcMillis ->
              // Adjust UTC midnight from DatePicker to local midday so day matches user's timezone
              val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = utcMillis
              }
              val localCal = Calendar.getInstance().apply {
                set(
                  utcCal.get(Calendar.YEAR),
                  utcCal.get(Calendar.MONTH),
                  utcCal.get(Calendar.DAY_OF_MONTH),
                  12,
                  0,
                  0,
                )
              }
              dateMillis = localCal.timeInMillis
            }
            showDatePicker = false
          },
          modifier = Modifier.testTag("date_picker_confirm"),
        ) {
          Text("Confirmar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancelar")
        }
      },
    ) {
      DatePicker(state = pickerState)
    }
  }
}
