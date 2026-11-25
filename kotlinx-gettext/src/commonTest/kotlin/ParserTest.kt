/*
 * Copyright 2022 Victor Kropp
 * Modifications copyright 2026 SSimco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ssimco.kotlinx.gettext

import io.github.ssimco.kotlinx.gettext.BinaryOp.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun zero() {
        assertEquals(NumberLiteral(0), PluralRule("0"))
    }

    @Test
    fun notZero() {
        assertEquals(BinaryExpression(N, NotEquals, NumberLiteral(0)), PluralRule("n != 0"))
    }

    @Test
    fun gtZero() {
        assertEquals(BinaryExpression(N, Greater, NumberLiteral(0)), PluralRule("n > 0"))
    }

    @Test
    fun gteZero() {
        assertEquals(BinaryExpression(N, GreaterOrEquals, NumberLiteral(0)), PluralRule("n >= 0"))
    }

    @Test
    fun ltTen() {
        assertEquals(BinaryExpression(N, Less, NumberLiteral(10)), PluralRule("n < 10"))
    }

    @Test
    fun leTen() {
        assertEquals(BinaryExpression(N, LessOrEquals, NumberLiteral(10)), PluralRule("n <= 10"))
    }

    @Test
    fun reminder() {
        assertEquals(BinaryExpression(N, Remainder, NumberLiteral(10)), PluralRule("n % 10"))
    }

    @Test
    fun bracesNotZero() {
        assertEquals(BinaryExpression(N, NotEquals, NumberLiteral(0)), PluralRule("(n != 0)"))
    }

    @Test
    fun bracesGtZero() {
        assertEquals(BinaryExpression(N, Greater, NumberLiteral(0)), PluralRule("(n > 0)"))
    }

    @Test
    fun bracesReminder() {
        assertEquals(BinaryExpression(N, Remainder, NumberLiteral(10)), PluralRule("(n % 10)"))
    }

    @Test
    fun andFalse() {
        assertEquals(BinaryExpression(N, And, NumberLiteral(0)), PluralRule("n && 0"))
    }

    @Test
    fun andTrue() {
        assertEquals(BinaryExpression(N, And, NumberLiteral(1)), PluralRule("n && 1"))
    }

    @Test
    fun orFalse() {
        assertEquals(BinaryExpression(N, Or, NumberLiteral(0)), PluralRule("n || 0"))
    }

    @Test
    fun orTrue() {
        assertEquals(BinaryExpression(N, Or, NumberLiteral(1)), PluralRule("n || 1"))
    }

    @Test
    fun ternary() {
        assertEquals(TernaryExpression(N, NumberLiteral(123), NumberLiteral(321)), PluralRule("n ? 123 : 321"))
    }

    @Test
    fun ternaryOrCondition() {
        assertEquals(TernaryExpression(
            BinaryExpression(
                BinaryExpression(N, Equals, NumberLiteral(1)),
                Or,
                BinaryExpression(
                    BinaryExpression(N, Remainder, NumberLiteral(10)),
                    Equals,
                    NumberLiteral(1)
                ),
            ),
            NumberLiteral(0), NumberLiteral(1)
        ), PluralRule("(n==1 || n%10==1) ? 0 : 1"))
    }

    @Test
    fun parens() {
        assertEquals(TernaryExpression(
                BinaryExpression(BinaryExpression(N, Remainder, NumberLiteral(10)), Equals, NumberLiteral(1)),
                NumberLiteral(0),
                NumberLiteral(1)
            ), PluralRule("((n%10==1) ? 0 : (1))")
        )
    }

    @Test
    fun ru() {
        PluralRule("((n%10==1 && n%100!=11) ? 0 : ((n%10 >= 2 && n%10 <=4 && (n%100 < 12 || n%100 > 14)) ? 1 : ((n%10 == 0 || (n%10 >= 5 && n%10 <=9)) || (n%100 >= 11 && n%100 <= 14)) ? 2 : 3))")
    }

    @Test
    fun unexpectedCharacter() {
        assertEquals(EmptyRule, PluralRule("(x != 1)"))
    }
}
