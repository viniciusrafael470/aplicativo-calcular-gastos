package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CatBills
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatHome
import com.example.ui.theme.CatLeisure
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport

enum class ExpenseCategory(
  val label: String,
  val icon: ImageVector,
  val accentColor: Color,
) {
  ALIMENTACAO("Alimentação", Icons.Rounded.Restaurant, CatFood),
  TRANSPORTE("Transporte", Icons.Rounded.DirectionsCar, CatTransport),
  MORADIA("Moradia", Icons.Rounded.Home, CatHome),
  CONTAS("Contas", Icons.Rounded.ReceiptLong, CatBills),
  LAZER("Lazer", Icons.Rounded.Celebration, CatLeisure),
  SAUDE("Saúde", Icons.Rounded.Favorite, CatHealth),
  COMPRAS("Compras", Icons.Rounded.ShoppingBag, CatShopping),
  EDUCACAO("Educação", Icons.Rounded.School, CatEducation),
  OUTROS("Outros", Icons.Rounded.AccountBalanceWallet, CatOther);

  companion object {
    fun fromName(name: String): ExpenseCategory =
      entries.firstOrNull { it.name == name } ?: OUTROS
  }
}

enum class PaymentMethod(val label: String) {
  PIX("Pix"),
  CREDITO("Crédito"),
  DEBITO("Débito"),
  DINHEIRO("Dinheiro");

  companion object {
    fun fromName(name: String): PaymentMethod =
      entries.firstOrNull { it.name == name } ?: PIX
  }
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val amountCents: Long,
  val category: String,
  val paymentMethod: String,
  val dateMillis: Long,
  val notes: String = "",
  val isRecurring: Boolean = false,
  val createdAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
  @PrimaryKey val yearMonthKey: String,
  val limitCents: Long,
  val updatedAtMillis: Long = System.currentTimeMillis(),
)
