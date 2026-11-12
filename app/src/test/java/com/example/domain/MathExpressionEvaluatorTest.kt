package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathExpressionEvaluatorTest {
  @Test
  fun evaluatesBrazilianDecimalsAndOperators() {
    assertEquals(6440L, MathExpressionEvaluator.evaluateToCents("45,90 + 18,50").getOrThrow())
    assertEquals(4000L, MathExpressionEvaluator.evaluateToCents("120 / 3").getOrThrow())
    assertEquals(4000L, MathExpressionEvaluator.evaluateToCents("120 ÷ 3").getOrThrow())
    assertEquals(13500L, MathExpressionEvaluator.evaluateToCents("150 × 0,9").getOrThrow())
    assertEquals(11000L, MathExpressionEvaluator.evaluateToCents("100 + 10").getOrThrow())
  }

  @Test
  fun evaluatesPercentagesAndPrecedence() {
    assertEquals(11000L, MathExpressionEvaluator.evaluateToCents("100 + 100 * 10%").getOrThrow())
    assertEquals(2500L, MathExpressionEvaluator.evaluateToCents("(15 + 10) * 1").getOrThrow())
  }

  @Test
  fun rejectsDivisionByZeroAndEmpty() {
    assertTrue(MathExpressionEvaluator.evaluateToCents("100 / 0").isFailure)
    assertTrue(MathExpressionEvaluator.evaluateToCents("   ").isFailure)
  }
}
