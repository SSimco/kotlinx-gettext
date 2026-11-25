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
 * A ternary expression, like in C or Java.
 *
 * If [condition] evaluates to a non-zero value, the result is the evaluation result of the [left] part,
 * otherwise it is the result of the evaluation of the [right] part.
 */
public data class TernaryExpression(
    /**
     * Condition that selects the [left] or [right] branch.
     */
    public val condition: PluralRuleExpression,
    /**
     * Expression evaluated when [condition] is non-zero.
     */
    public val left: PluralRuleExpression,
    /**
     * Expression evaluated when [condition] is zero.
     */
    public val right: PluralRuleExpression,
) : PluralRuleExpression {
    override fun evaluate(n: Int): Int {
        return if (condition.evaluate(n) != 0) left.evaluate(n) else right.evaluate(n)
    }

    override fun toString(): String {
        return "$condition ? $left : $right"
    }
}