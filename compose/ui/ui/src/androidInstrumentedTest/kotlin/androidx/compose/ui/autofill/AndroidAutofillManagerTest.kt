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

package androidx.compose.ui.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.node.elementOf
import androidx.compose.ui.node.requestAutofill
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDataType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.onAutofillText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.TestActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Ignore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions

@SdkSuppress(minSdkVersion = 26)
@RequiresApi(Build.VERSION_CODES.O)
@RunWith(AndroidJUnit4::class)
class AndroidAutofillManagerTest {
    @get:Rule val rule = createAndroidComposeRule<TestActivity>()

    private val height = 200.dp
    private val width = 200.dp

    @OptIn(ExperimentalComposeUiApi::class)
    private val previousFlagValue = ComposeUiFlags.isSemanticAutofillEnabled

    @Before
    fun enableAutofill() {
        @OptIn(ExperimentalComposeUiApi::class)
        ComposeUiFlags.isSemanticAutofillEnabled = true
    }

    @After
    fun teardown() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = rule.activity
        while (!activity.isDestroyed) {
            instrumentation.runOnMainSync {
                if (!activity.isDestroyed) {
                    activity.finish()
                }
            }
        }
        @OptIn(ExperimentalComposeUiApi::class)
        ComposeUiFlags.isSemanticAutofillEnabled = previousFlagValue
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_initialization() {
        val am: PlatformAutofillManager = mock()
        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(Modifier.semantics { contentType = ContentType.Username }.size(height, width))
        }

        // Upon initialization, we send `notifyViewVisibility` for all of the components that appear
        // onscreen. For other tests, we'll call `clearInvocations(am)` to avoid testing this call.
        rule.runOnIdle { verify(am, times(1)).notifyViewVisibilityChanged(any(), any(), eq(true)) }
    }

    @Test
    @SmallTest
    fun autofillManager_doNotCallCommit_nodesAppeared() {
        val am: PlatformAutofillManager = mock()
        var isVisible by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            if (isVisible) {
                Box(Modifier.semantics { contentType = ContentType.Username }.size(height, width))
            }
        }

        rule.runOnIdle { isVisible = true }

        // `commit` should not be called when an autofillable component appears onscreen.
        rule.runOnIdle { verify(am, never()).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_doNotCallCommit_autofillTagsAdded() {
        val am: PlatformAutofillManager = mock()
        var hasContentType by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                modifier =
                    Modifier.then(
                            if (hasContentType) {
                                Modifier.semantics { contentType = ContentType.Username }
                            } else {
                                Modifier
                            }
                        )
                        .size(height, width)
            )
        }

        rule.runOnIdle { hasContentType = true }

        // `commit` should not be called a component becomes relevant to autofill.
        rule.runOnIdle { verify(am, times(0)).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_callCommit_nodesDisappeared() {
        val am: PlatformAutofillManager = mock()
        var revealFirstUsername by mutableStateOf(true)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            if (revealFirstUsername) {
                Box(Modifier.semantics { contentType = ContentType.Username }.size(height, width))
            }
        }

        rule.runOnIdle { revealFirstUsername = false }

        // `commit` should be called when an autofill-able component leaves the screen.
        rule.runOnIdle { verify(am, times(1)).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_callCommit_nodesDisappearedAndAppeared() {
        val am: PlatformAutofillManager = mock()
        var revealFirstUsername by mutableStateOf(true)
        var revealSecondUsername by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            if (revealFirstUsername) {
                Box(Modifier.semantics { contentType = ContentType.Username }.size(height, width))
            }
            if (revealSecondUsername) {
                Box(Modifier.semantics { contentType = ContentType.Username }.size(height, width))
            }
        }

        rule.runOnIdle { revealFirstUsername = false }
        rule.runOnIdle { revealSecondUsername = true }

        // `commit` should be called when an autofill-able component leaves onscreen, even when
        // another, different autofill-able component is added.
        rule.runOnIdle { verify(am, times(1)).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_doNotCallCommit_nonAutofillRelatedNodesAddedAndDisappear() {
        val am: PlatformAutofillManager = mock()
        var isVisible by mutableStateOf(true)
        var semanticsExist by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            if (isVisible) {
                Box(
                    modifier =
                        Modifier.then(
                                if (semanticsExist) {
                                    Modifier.semantics { contentDescription = "contentDescription" }
                                } else {
                                    Modifier
                                }
                            )
                            .size(height, width)
                )
            }
        }

        rule.runOnIdle { semanticsExist = true }
        rule.runOnIdle { isVisible = false }

        // Adding in semantics not related to autofill should not trigger commit
        rule.runOnIdle { verify(am, never()).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_callCommit_nodesBecomeAutofillRelatedAndDisappear() {
        val am: PlatformAutofillManager = mock()
        var isVisible by mutableStateOf(true)
        var hasContentType by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            if (isVisible) {
                Box(
                    modifier =
                        Modifier.then(
                                if (hasContentType) {
                                    Modifier.semantics { contentType = ContentType.Username }
                                } else {
                                    Modifier
                                }
                            )
                            .size(height, width)
                )
            }
        }

        rule.runOnIdle { hasContentType = true }
        rule.runOnIdle { isVisible = false }

        // `commit` should be called when component becomes autofillable, then leaves the screen.
        rule.runOnIdle { verify(am, times(1)).commit() }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged() {
        val am: PlatformAutofillManager = mock()
        var changeText by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        editableText = AnnotatedString(if (changeText) "1234" else "****")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { changeText = true }

        rule.runOnIdle { verify(am).notifyValueChanged(any(), any(), any()) }
    }

    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged_fromEmpty() {
        val am: PlatformAutofillManager = mock()
        var changeText by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        editableText = AnnotatedString(if (changeText) "1234" else "")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { changeText = true }

        rule.runOnIdle { verify(am).notifyValueChanged(any(), any(), any()) }
    }

    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged_toEmpty() {
        val am: PlatformAutofillManager = mock()
        var changeText by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        editableText = AnnotatedString(if (changeText) "" else "1234")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { changeText = true }

        rule.runOnIdle { verify(am).notifyValueChanged(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged_editableTextAdded() {
        val am: PlatformAutofillManager = mock()
        var hasEditableText by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        if (hasEditableText) editableText = AnnotatedString("1234")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { hasEditableText = true }

        // TODO: This does not send notifyValueChanged, but will we could add a test to verify that
        //  it sends notifyVisibilityChanged after aosp/3391719 lands.
        rule.runOnIdle { verify(am, never()).notifyValueChanged(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged_editableTextRemoved() {
        val am: PlatformAutofillManager = mock()
        var hasEditableText by mutableStateOf(true)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        if (hasEditableText) editableText = AnnotatedString("1234")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { hasEditableText = false }

        // TODO: This does not send notifyValueChanged, but will we could add a test to verify that
        //  it sends notifyVisibilityChanged after aosp/3391719 lands.
        rule.runOnIdle { verify(am, never()).notifyValueChanged(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged_addedEmptyEditableText() {
        val am: PlatformAutofillManager = mock()
        var hasEditableText by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        if (hasEditableText) editableText = AnnotatedString("")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { hasEditableText = true }

        // TODO: This does not send notifyValueChanged, but will we could add a test to verify that
        //  it sends notifyVisibilityChanged after aosp/3391719 lands.
        rule.runOnIdle { verify(am, never()).notifyValueChanged(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyValueChanged_removedEmptyEditableText() {
        val am: PlatformAutofillManager = mock()
        var hasEditableText by mutableStateOf(true)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                        if (hasEditableText) editableText = AnnotatedString("")
                    }
                    .size(height, width)
            )
        }

        rule.runOnIdle { hasEditableText = false }

        // TODO: This does not send notifyValueChanged, but will we could add a test to verify that
        //  it sends notifyVisibilityChanged after aosp/3391719 lands.
        rule.runOnIdle { verify(am, never()).notifyValueChanged(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyViewEntered_previousFocusFalse() {
        val am: PlatformAutofillManager = mock()
        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        testTag = "username"
                        onAutofillText { true }
                    }
                    .size(height, width)
                    .focusable()
            )
        }

        rule.onNodeWithTag("username").requestFocus()

        rule.runOnIdle { verify(am).notifyViewEntered(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notAutofillable_notifyViewEntered_previousFocusFalse() {
        val am: PlatformAutofillManager = mock()
        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        testTag = "username"
                        contentType = ContentType.Username
                        contentDataType = ContentDataType.Text
                    }
                    .size(height, width)
                    .focusable()
            )
        }
        clearInvocations(am)

        rule.onNodeWithTag("username").requestFocus()

        rule.runOnIdle { verifyNoMoreInteractions(am) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyViewEntered_previousFocusNull() {
        val am: PlatformAutofillManager = mock()
        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                modifier =
                    Modifier.semantics {
                            testTag = "username"
                            onAutofillText { true }
                        }
                        .size(height, width)
                        .focusable()
            )
        }

        rule.onNodeWithTag("username").requestFocus()

        rule.runOnIdle { verify(am).notifyViewEntered(any(), any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyViewExited_previousFocusTrue() {
        val am: PlatformAutofillManager = mock()
        lateinit var focusManager: FocusManager
        rule.setContent {
            focusManager = LocalFocusManager.current
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                Modifier.semantics {
                        testTag = "username"
                        onAutofillText { true }
                    }
                    .size(height, width)
                    .focusable()
            )
        }
        rule.onNodeWithTag("username").requestFocus()
        rule.runOnIdle { clearInvocations(am) }

        rule.runOnIdle { focusManager.clearFocus() }

        rule.runOnIdle { verify(am).notifyViewExited(any(), any()) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyViewExited_previouslyFocusedItemNotAutofillable() {
        val am: PlatformAutofillManager = mock()
        lateinit var focusManager: FocusManager
        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            focusManager = LocalFocusManager.current
            Box(Modifier.semantics { testTag = "username" }.size(height, width).focusable())
        }
        clearInvocations(am)

        rule.onNodeWithTag("username").requestFocus()
        rule.runOnIdle { focusManager.clearFocus() }

        rule.runOnIdle { verifyZeroInteractions(am) }
    }

    @Ignore // TODO(b/383198004): Add support for notifyVisibilityChanged.
    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 27)
    fun autofillManager_notifyVisibilityChanged_disappeared() {
        val am: PlatformAutofillManager = mock()
        var isVisible by mutableStateOf(true)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                modifier =
                    Modifier.then(if (isVisible) Modifier else Modifier.alpha(0f))
                        // visibility is related to commit, so we must have a contentType set
                        .semantics { contentType = ContentType.Username }
                        .size(width, height)
                        .focusable()
            )
        }

        clearInvocations(am)

        rule.runOnIdle { isVisible = false }

        // After switching the flag, the autofill manager is then notified that the box has
        // become transparent.
        rule.runOnIdle { verify(am).notifyViewVisibilityChanged(any(), any(), eq(false)) }
    }

    @Ignore // TODO(b/383198004): Add support for notifyVisibilityChanged.
    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 27)
    fun autofillManager_notifyVisibilityChanged_appeared() {
        val am: PlatformAutofillManager = mock()
        var isVisible by mutableStateOf(false)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            Box(
                modifier =
                    Modifier.then(if (isVisible) Modifier else Modifier.alpha(0f))
                        // visibility is related to commit, so we must have a contentType set
                        .semantics { contentType = ContentType.Username }
                        .size(width, height)
                        .focusable()
            )
        }

        clearInvocations(am)

        rule.runOnIdle { isVisible = true }

        // After switching the flag, the autofill manager is then notified that the box has
        // become opaque.
        rule.runOnIdle { verify(am).notifyViewVisibilityChanged(any(), any(), eq(true)) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 27)
    fun autofillManager_notifyVisibilityChanged_lazyScroll() {
        // Arrange.
        val am: PlatformAutofillManager = mock()
        lateinit var lazyListState: LazyListState

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            lazyListState = rememberLazyListState()
            with(LocalDensity.current) {
                LazyRow(state = lazyListState, modifier = Modifier.size(10.toDp())) {
                    items(2) {
                        Box(
                            Modifier.size(10.toDp())
                                // visibility is related to commit, so we must have a contentType
                                // set
                                .semantics { contentType = ContentType.Username }
                                .focusable()
                        )
                    }
                }
            }
        }
        clearInvocations(am)

        // Act.
        rule.runOnIdle { lazyListState.requestScrollToItem(1) }

        // After scrolling, one element should be removed and one should be added.
        rule.runOnIdle { verify(am).notifyViewVisibilityChanged(any(), any(), eq(false)) }
        rule.runOnIdle { verify(am).notifyViewVisibilityChanged(any(), any(), eq(true)) }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyCommit() {
        val am: PlatformAutofillManager = mock()
        val forwardTag = "forward_button_tag"
        var autofillManager: AutofillManager?

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            autofillManager = LocalAutofillManager.current
            Box(
                modifier =
                    Modifier.clickable { autofillManager?.commit() }
                        .size(height, width)
                        .testTag(forwardTag)
            )
        }

        rule.onNodeWithTag(forwardTag).performClick()

        rule.runOnIdle { verify(am).commit() }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_notifyCancel() {
        val am: PlatformAutofillManager = mock()
        val backTag = "back_button_tag"
        var autofillManager: AutofillManager?

        rule.setContent {
            autofillManager = LocalAutofillManager.current
            (autofillManager as AndroidAutofillManager).platformAutofillManager = am
            Box(
                modifier =
                    Modifier.clickable { autofillManager?.cancel() }
                        .size(height, width)
                        .testTag(backTag)
            )
        }
        rule.onNodeWithTag(backTag).performClick()

        rule.runOnIdle { verify(am).cancel() }
    }

    @Test
    @SmallTest
    fun autofillManager_lazyColumnScroll_callsCommit() {
        lateinit var state: LazyListState
        lateinit var coroutineScope: CoroutineScope
        val am: PlatformAutofillManager = mock()
        val count = 100

        rule.setContent {
            coroutineScope = rememberCoroutineScope()
            state = rememberLazyListState()
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am

            with(LocalDensity.current) {
                LazyColumn(Modifier.fillMaxWidth().height(50.dp), state) {
                    item {
                        Box(
                            Modifier.semantics { contentType = ContentType.Username }
                                .size(10.toDp())
                        )
                    }
                    items(count) { Box(Modifier.size(10.toDp())) }
                }
            }
        }

        rule.runOnIdle { coroutineScope.launch { state.scrollToItem(10) } }

        rule.runOnIdle { verify(am).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_columnScroll_doesNotCallCommit() {
        lateinit var scrollState: ScrollState
        lateinit var coroutineScope: CoroutineScope
        val am: PlatformAutofillManager = mock()

        rule.setContent {
            coroutineScope = rememberCoroutineScope()
            scrollState = rememberScrollState()
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am

            with(LocalDensity.current) {
                Column(Modifier.fillMaxWidth().height(50.dp).verticalScroll(scrollState)) {
                    Row {
                        Box(
                            Modifier.semantics { contentType = ContentType.Username }
                                .size(10.toDp())
                        )
                    }
                    repeat(50) { Box(Modifier.size(10.toDp())) }
                }
            }
        }

        rule.runOnIdle { coroutineScope.launch { scrollState.scrollTo(scrollState.maxValue / 2) } }

        // Scrolling past an element in a column is not enough to call commit
        rule.runOnIdle { verify(am, never()).commit() }
    }

    @Test
    @SmallTest
    fun autofillManager_column_nodesDisappearingCallsCommit() {
        lateinit var scrollState: ScrollState
        val am: PlatformAutofillManager = mock()
        var autofillComponentsVisible by mutableStateOf(true)

        rule.setContent {
            (LocalAutofillManager.current as AndroidAutofillManager).platformAutofillManager = am
            scrollState = rememberScrollState()

            with(LocalDensity.current) {
                Column(Modifier.fillMaxWidth().height(50.dp).verticalScroll(scrollState)) {
                    if (autofillComponentsVisible) {
                        Row {
                            Box(
                                Modifier.semantics { contentType = ContentType.Username }
                                    .size(10.toDp())
                            )
                        }
                    }
                    repeat(50) { Box(Modifier.size(10.toDp())) }
                }
            }
        }

        rule.runOnIdle { autofillComponentsVisible = false }

        // A column disappearing will call commit
        rule.runOnIdle { verify(am).commit() }
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = 26)
    fun autofillManager_requestAutofill() {
        val am: PlatformAutofillManager = mock()
        val semanticsModifier = TestSemanticsModifier { testTag = "TestTag" }
        var autofillManager: AutofillManager?

        rule.setContent {
            autofillManager = LocalAutofillManager.current
            (autofillManager as AndroidAutofillManager).platformAutofillManager = am
            Box(Modifier.elementOf(semanticsModifier))
        }

        // Act
        rule.runOnIdle { semanticsModifier.requestAutofill() }

        // Assert
        rule.runOnIdle { verify(am).requestAutofill(any(), any(), any()) }
    }

    private class TestSemanticsModifier(
        private val onApplySemantics: SemanticsPropertyReceiver.() -> Unit
    ) : SemanticsModifierNode, Modifier.Node() {

        override fun SemanticsPropertyReceiver.applySemantics() {
            contentType = ContentType.Username
            onApplySemantics.invoke(this)
        }
    }
}
