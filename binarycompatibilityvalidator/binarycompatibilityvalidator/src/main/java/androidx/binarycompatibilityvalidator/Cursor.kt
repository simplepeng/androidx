/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.binarycompatibilityvalidator

class Cursor
private constructor(private val lines: List<String>, rowIndex: Int = 0, columnIndex: Int = 0) {
    constructor(text: String) : this(text.split("\n"))

    var rowIndex: Int = rowIndex
        private set

    var columnIndex: Int = columnIndex
        private set

    val currentLine: String
        get() = lines[rowIndex].slice(columnIndex until lines[rowIndex].length)

    fun hasNextRow() = rowIndex < (lines.size - 1)

    /** Check if we have passed the last line in [lines] and there is nothing left to parse */
    fun isFinished() = rowIndex >= lines.size

    fun nextLine() {
        rowIndex++
        columnIndex = 0
        if (!isFinished()) {
            skipInlineWhitespace()
        }
    }

    fun parseSymbol(
        pattern: Regex,
        peek: Boolean = false,
        skipInlineWhitespace: Boolean = true
    ): String? {
        val match = pattern.find(currentLine)
        return match?.value?.also {
            if (!peek) {
                val offset = it.length + currentLine.indexOf(it)
                setColumn(columnIndex + offset)
                if (skipInlineWhitespace) {
                    skipInlineWhitespace()
                }
            }
        }
    }

    fun parseValidIdentifier(peek: Boolean = false): String? =
        parseSymbol(validIdentifierRegex, peek)

    fun parseWord(peek: Boolean = false): String? = parseSymbol(wordRegex, peek)

    fun copy() = Cursor(lines, rowIndex, columnIndex)

    private fun hasNextColumn(): Boolean {
        return columnIndex < lines[rowIndex].length - 1
    }

    private fun setColumn(index: Int) {
        columnIndex = index
    }

    internal fun skipInlineWhitespace() {
        while (currentLine.firstOrNull()?.isWhitespace() == true) {
            setColumn(columnIndex + 1)
        }
    }
}

private val validIdentifierRegex = Regex("^\\$?[a-zA-Z_][a-zA-Z0-9_]+")
private val wordRegex = Regex("[a-zA-Z]+")
