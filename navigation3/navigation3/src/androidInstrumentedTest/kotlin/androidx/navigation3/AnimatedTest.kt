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

package androidx.navigation3

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.kruth.assertThat
import androidx.navigation3.NavDisplay.DEFAULT_TRANSITION_DURATION_MILLISECOND
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AnimatedTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testNavHostAnimations() {
        lateinit var backstack: MutableList<Any>

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            backstack = remember { mutableStateListOf(first) }
            NavDisplay(backstack) {
                when (it) {
                    first -> NavRecord(first) { Text(first) }
                    second -> NavRecord(second) { Text(second) }
                    else -> error("Invalid key passed")
                }
            }
        }

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitForIdle()
        assertThat(composeTestRule.onNodeWithText(first).isDisplayed()).isTrue()

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.runOnIdle { backstack.add(second) }

        // advance half way between animations
        composeTestRule.mainClock.advanceTimeBy(
            DEFAULT_TRANSITION_DURATION_MILLISECOND.toLong() / 2
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(first).assertExists()
        composeTestRule.onNodeWithText(second).assertExists()

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(first).assertDoesNotExist()
        composeTestRule.onNodeWithText(second).assertExists()
    }

    @Test
    fun testNavHostAnimationsCustom() {
        lateinit var backstack: MutableList<Any>

        composeTestRule.mainClock.autoAdvance = false
        val customDuration = DEFAULT_TRANSITION_DURATION_MILLISECOND * 2

        composeTestRule.setContent {
            backstack = remember { mutableStateListOf(first) }
            NavDisplay(backstack) {
                when (it) {
                    first ->
                        NavRecord(
                            first,
                        ) {
                            Text(first)
                        }
                    second ->
                        NavRecord(
                            second,
                            featureMap =
                                NavDisplay.transition(
                                    enter = fadeIn(tween(customDuration)),
                                    exit = fadeOut(tween(customDuration))
                                )
                        ) {
                            Text(second)
                        }
                    else -> error("Invalid key passed")
                }
            }
        }

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(first).assertIsDisplayed()

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.runOnIdle { backstack.add(second) }

        // advance past the default duration but not the custom duration
        composeTestRule.mainClock.advanceTimeBy(
            ((DEFAULT_TRANSITION_DURATION_MILLISECOND * 3 / 2)).toLong()
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(first).assertExists()
        composeTestRule.onNodeWithText(second).assertExists()

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(first).assertDoesNotExist()
        composeTestRule.onNodeWithText(second).assertExists()
    }
}

private const val first = "first"
private const val second = "second"
