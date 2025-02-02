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

package androidx.wear.compose.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ButtonGroupTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun supports_testtag() {
        rule.setContentWithTheme {
            ButtonGroup(modifier = Modifier.testTag(TEST_TAG)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        rule.onNodeWithTag(TEST_TAG).assertExists()
    }

    @Test
    fun two_items_equally_sized_by_default() =
        verifyWidths(
            2,
            expectedWidths = { availableSpace -> arrayOf(availableSpace / 2, availableSpace / 2) }
        )

    @Test
    fun two_items_one_double_size() =
        verifyWidths(
            2,
            expectedWidths = { availableSpace ->
                arrayOf(availableSpace / 3, availableSpace / 3 * 2)
            },
            minWidthAndWeights = arrayOf(50.dp to 1f, 50.dp to 2f)
        )

    @Test
    fun respects_min_width() =
        verifyWidths(
            2,
            expectedWidths = { availableSpace -> arrayOf(30.dp, availableSpace - 30.dp) },
            size = 100.dp,
            minWidthAndWeights = arrayOf(30.dp to 1f, 30.dp to 10f)
        )

    @Test
    fun three_equal_buttons() =
        verifyWidths(3, expectedWidths = { availableSpace -> Array(3) { availableSpace / 3 } })

    @Test
    fun three_buttons_one_two_one() =
        verifyWidths(
            3,
            expectedWidths = { availableSpace ->
                arrayOf(availableSpace / 4, availableSpace / 2, availableSpace / 4)
            },
            minWidthAndWeights = arrayOf(50.dp to 1f, 50.dp to 2f, 50.dp to 1f)
        )

    @Test
    fun modifier_order_ignored() {
        val size = 300.dp
        rule.setContentWithTheme {
            ButtonGroup(
                modifier = Modifier.size(size),
                contentPadding = PaddingValues(0.dp),
                spacing = 0.dp
            ) {
                Box(Modifier.weight(1f).minWidth(60.dp).testTag("${TEST_TAG}0"))
                Box(Modifier.minWidth(60.dp).weight(1f).testTag("${TEST_TAG}1"))
                Box(Modifier.weight(2f).minWidth(60.dp).testTag("${TEST_TAG}2"))
                Box(Modifier.minWidth(60.dp).weight(2f).testTag("${TEST_TAG}3"))
            }
        }

        // Items 0 & 1 should be 60.dp, 2 & 3 should be 90.dp
        listOf(60.dp, 60.dp, 90.dp, 90.dp).forEachIndexed { index, dp ->
            rule.onNodeWithTag(TEST_TAG + index.toString()).assertWidthIsEqualTo(dp)
        }
    }

    private fun verifyWidths(
        numItems: Int,
        expectedWidths: (Dp) -> Array<Dp>,
        size: Dp = 300.dp,
        spacing: Dp = 10.dp,
        minWidthAndWeights: Array<Pair<Dp, Float>> = Array(numItems) { 48.dp to 1f },
    ) {
        val horizontalPadding = 10.dp
        val actualExpectedWidths =
            expectedWidths(size - horizontalPadding * 2 - spacing * (numItems - 1))

        require(numItems == actualExpectedWidths.size)
        require(numItems == minWidthAndWeights.size)

        rule.setContentWithTheme {
            ButtonGroup(
                modifier = Modifier.size(size),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                spacing = spacing
            ) {
                repeat(numItems) { ix ->
                    Box(
                        modifier =
                            Modifier.testTag(TEST_TAG + (ix + 1).toString())
                                .fillMaxSize()
                                .weight(minWidthAndWeights[ix].second)
                                .minWidth(minWidthAndWeights[ix].first)
                    )
                }
            }
        }

        repeat(numItems) {
            rule
                .onNodeWithTag(TEST_TAG + (it + 1).toString())
                .assertWidthIsEqualTo(actualExpectedWidths[it])
        }
    }
}
