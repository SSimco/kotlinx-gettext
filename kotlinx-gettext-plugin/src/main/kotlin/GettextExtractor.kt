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

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

class GettextExtractor(
    private val messageCollector: MessageCollector,
    private val poEntries: MutableList<PoEntry>,
    private val keywords: List<KeywordSpec>,
    private val relativePath: String,
    private val fileEntry: IrFileEntry,
) : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitCall(expression: IrCall) {
        // Visit nested calls first, so their entries precede the enclosing call's entry.
        expression.acceptChildrenVoid(this)

        val info = keywords.firstOrNull { it.matches(expression) }?.process(expression, "$relativePath:${fileEntry.getLineNumber(expression.startOffset) + 1}")
        if (info != null) {
            poEntries.add(info)
            messageCollector.report(CompilerMessageSeverity.OUTPUT, "[gettext] ${info.references[0]} \"${info.text}\"")
        }
    }
}