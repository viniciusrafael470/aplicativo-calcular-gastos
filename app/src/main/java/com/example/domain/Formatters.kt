package com.example.domain

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class YearMonthKey(val year: Int, val month: Int) {
  fun asKey(): String = String.format(Locale.US, "%04d-%02d", year, month)

  fun previous(): YearMonthKey =
    if (month == 1) YearMonthKey(year - 1, 12) else YearMonthKey(year, month - 1)

  fun next(): YearMonthKey =
    if (month == 12) YearMonthKey(year + 1, 1) else YearMonthKey(year, month + 1)

  fun rangeMillis(): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month - 1)
      set(Calendar.DAY_OF_MONTH, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis - 1L
    return start to end
  }

  fun daysInMonth(): Int {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month - 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  companion object {
    fun current(): YearMonthKey {
      val cal = Calendar.getInstance()
      return YearMonthKey(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + 1,
      )
    }

    fun fromMillis(millis: Long): YearMonthKey {
      val cal = Calendar.getInstance().apply { timeInMillis = millis }
      return YearMonthKey(
        year = cal.get(Calendar.YEAR),
        month = cal.get(Calendar.MONTH) + 1,
      )
    }
  }
}

object Formatters {
  private val ptBrLocale = Locale("pt", "BR")

  private val monthNames = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
  )

  private val shortMonthNames = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez",
  )

  fun formatCurrencyCents(cents: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(ptBrLocale)
    return formatter.format(cents / 100.0)
  }

  fun formatEditableAmount(cents: Long): String {
    val absCents = abs(cents)
    val integerPart = absCents / 100
    val decimalPart = absCents % 100
    return String.format(ptBrLocale, "%d,%02d", integerPart, decimalPart)
  }

  fun formatCompactCurrencyCents(cents: Long): String {
    val reais = cents / 100.0
    return when {
      abs(reais) >= 1000.0 -> String.format(ptBrLocale, "R$ %.1fk", reais / 1000.0)
      else -> String.format(ptBrLocale, "R$ %.0f", reais)
    }
  }

  fun formatMonthYear(ym: YearMonthKey): String {
    val name = monthNames.getOrElse(ym.month - 1) { "" }
    return "$name ${ym.year}"
  }

  fun formatShortMonth(ym: YearMonthKey): String =
    shortMonthNames.getOrElse(ym.month - 1) { "" }

  fun formatDayKey(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
      Locale.US,
      "%04d-%02d-%02d",
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH) + 1,
      cal.get(Calendar.DAY_OF_MONTH),
    )
  }

  fun formatDateHeader(millis: Long): String {
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
      isSameDay(target, today) -> "Hoje"
      isSameDay(target, yesterday) -> "Ontem"
      else -> {
        val sdf = SimpleDateFormat("dd 'de' MMM", ptBrLocale)
        sdf.format(Date(millis))
      }
    }
  }

  fun formatFullDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", ptBrLocale)
    return sdf.format(Date(millis))
  }

  private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
      a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
