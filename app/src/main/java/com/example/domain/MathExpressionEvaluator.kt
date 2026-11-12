package com.example.domain

import kotlin.math.roundToLong

object MathExpressionEvaluator {
  /**
   * Evaluates arithmetic expressions supporting +, -, *, ×, /, ÷, %, parentheses,
   * and Brazilian (comma) or US (dot) decimal separators.
   * Returns amount in integer cents (>= 0 or signed if negative).
   */
  fun evaluateToCents(rawExpression: String): Result<Long> {
    val sanitized = normalizeExpression(rawExpression)
    if (sanitized.isBlank()) {
      return Result.failure(IllegalArgumentException("Expressão vazia"))
    }
    return try {
      val parser = Parser(sanitized)
      val value = parser.parseExpression()
      if (!parser.isAtEnd()) {
        return Result.failure(IllegalArgumentException("Caractere inválido na expressão"))
      }
      if (value.isNaN() || value.isInfinite()) {
        return Result.failure(IllegalArgumentException("Operação matemática inválida"))
      }
      Result.success((value * 100.0).roundToLong())
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun normalizeExpression(input: String): String {
    val noSpaces = input
      .replace(" ", "")
      .replace("R$", "", ignoreCase = true)
      .replace("×", "*")
      .replace("x", "*", ignoreCase = true)
      .replace("÷", "/")
      .replace("−", "-")

    // Handle Brazilian number format:
    // If string has both '.' and ',' (e.g., 1.250,50), strip '.' and replace ',' with '.'
    // Otherwise replace ',' with '.'
    val builder = StringBuilder()
    val tokens = Regex("([0-9.,]+|[^0-9.,])").findAll(noSpaces)
    for (match in tokens) {
      val token = match.value
      if (token.any { it.isDigit() }) {
        val normalizedNum = if (token.contains(",") && token.contains(".")) {
          token.replace(".", "").replace(",", ".")
        } else {
          token.replace(",", ".")
        }
        builder.append(normalizedNum)
      } else {
        builder.append(token)
      }
    }
    return builder.toString()
  }

  private class Parser(private val src: String) {
    private var pos = 0

    fun isAtEnd(): Boolean = pos >= src.length

    fun parseExpression(): Double {
      var left = parseTerm()
      while (pos < src.length) {
        when (src[pos]) {
          '+' -> {
            pos++
            left += parseTerm()
          }
          '-' -> {
            pos++
            left -= parseTerm()
          }
          else -> break
        }
      }
      return left
    }

    private fun parseTerm(): Double {
      var left = parseFactor()
      while (pos < src.length) {
        when (src[pos]) {
          '*' -> {
            pos++
            left *= parseFactor()
          }
          '/' -> {
            pos++
            val divisor = parseFactor()
            if (divisor == 0.0) throw ArithmeticException("Divisão por zero")
            left /= divisor
          }
          else -> break
        }
      }
      return left
    }

    private fun parseFactor(): Double {
      if (pos >= src.length) throw IllegalArgumentException("Expressão incompleta")
      var value = when (src[pos]) {
        '+' -> {
          pos++
          parseFactor()
        }
        '-' -> {
          pos++
          -parseFactor()
        }
        '(' -> {
          pos++
          val inner = parseExpression()
          if (pos >= src.length || src[pos] != ')') {
            throw IllegalArgumentException("Parêntese não fechado")
          }
          pos++
          inner
        }
        else -> parseNumber()
      }
      while (pos < src.length && src[pos] == '%') {
        pos++
        value /= 100.0
      }
      return value
    }

    private fun parseNumber(): Double {
      val start = pos
      var dotSeen = false
      while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) {
        if (src[pos] == '.') {
          if (dotSeen) throw IllegalArgumentException("Número inválido")
          dotSeen = true
        }
        pos++
      }
      if (start == pos) throw IllegalArgumentException("Número esperado")
      return src.substring(start, pos).toDouble()
    }
  }
}
