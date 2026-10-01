package app.riyaspullur.personalmoneymanagement.core.util

import java.math.BigDecimal
import java.math.RoundingMode

object Calculator {
    fun evaluate(expression: String): BigDecimal? {
        return try {
            val sanitized = expression.replace(" ", "")
            if (sanitized.isEmpty()) return null
            
            // Basic regex-based parser for +, -, *, /
            // For a production app, use a proper expression parser library or a shunting-yard algorithm
            // Here we implement a simple sequential evaluator for basic arithmetic
            
            val numbers = sanitized.split(Regex("[+\\-*/]")).filter { it.isNotEmpty() }.map { BigDecimal(it) }
            val operators = sanitized.filter { it in "+-*/" }
            
            if (numbers.isEmpty()) return null
            if (operators.isEmpty()) return numbers[0]
            
            var result = numbers[0]
            for (i in operators.indices) {
                val nextNum = numbers[i + 1]
                result = when (operators[i]) {
                    '+' -> result.add(nextNum)
                    '-' -> result.subtract(nextNum)
                    '*' -> result.multiply(nextNum)
                    '/' -> if (nextNum != BigDecimal.ZERO) result.divide(nextNum, 4, RoundingMode.HALF_UP) else result
                    else -> result
                }
            }
            result.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        } catch (e: Exception) {
            null
        }
    }
}
