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

/**
 * Any binary expression used in plural rule
 */
public data class BinaryExpression(
    /**
     * Left operand.
     */
    public val left: PluralRuleExpression,
    /**
     * Operator applied to the operands.
     */
    public val op: BinaryOp,
    /**
     * Right operand.
     */
    public val right: PluralRuleExpression,
) : PluralRuleExpression {

    override fun evaluate(n: Int): Int {
        return when(op) {
            BinaryOp.NotEquals -> if (left.evaluate(n) != right.evaluate(n)) 1 else 0
            BinaryOp.Equals -> if (left.evaluate(n) == right.evaluate(n)) 1 else 0
            BinaryOp.Less -> if (left.evaluate(n) < right.evaluate(n)) 1 else 0
            BinaryOp.LessOrEquals -> if (left.evaluate(n) <= right.evaluate(n)) 1 else 0
            BinaryOp.Greater -> if (left.evaluate(n) > right.evaluate(n)) 1 else 0
            BinaryOp.GreaterOrEquals -> if (left.evaluate(n) >= right.evaluate(n)) 1 else 0
            BinaryOp.Remainder -> left.evaluate(n) % right.evaluate(n)
            BinaryOp.And -> if ((left.evaluate(n) != 0) && (right.evaluate(n) != 0)) 1 else 0
            BinaryOp.Or -> if ((left.evaluate(n) != 0) || (right.evaluate(n) != 0)) 1 else 0
        }
    }

    override fun toString(): String {
        return "$left $op $right"
    }
}