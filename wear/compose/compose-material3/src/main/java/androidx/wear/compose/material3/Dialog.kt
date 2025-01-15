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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.wear.compose.foundation.LocalReduceMotion
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material3.MotionScheme.Companion.standard
import kotlinx.coroutines.flow.collectLatest

/**
 * A base dialog component used by [AlertDialog] and [ConfirmationDialog] variations. This dialog
 * provides a full-screen experience with custom entry/exit animations.
 *
 * Dialogs provide important prompts in a user flow. They can require an action, communicate
 * information, or help users accomplish a task.
 *
 * The caller should consider whether timeText or scrollIndicator are needed on this dialog, in this
 * case they should provide that in their content by using ScreenScaffold (with suitable scrollState
 * if that's required).
 *
 * @param visible A boolean value that determines whether the dialog should be displayed.
 * @param onDismissRequest A lambda function to be called when the dialog is dismissed by swiping
 *   right.
 * @param modifier Modifier to be applied to the dialog content.
 * @param properties An optional [DialogProperties] object for configuring the dialog's behavior.
 * @param content A composable function that defines the content of the dialog.
 */
@Composable
public fun Dialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    val showState by rememberUpdatedState(visible)
    // Transitions for dialog animation.
    var transitionState by remember {
        mutableStateOf(MutableTransitionState(DialogVisibility.Hide))
    }
    val shouldShow by remember {
        derivedStateOf { showState || transitionState.currentState == DialogVisibility.Display }
    }
    val transition = rememberTransition(transitionState)

    val scaffoldState = LocalScaffoldState.current
    val backgroundAnimatable = remember { Animatable(1f) }

    val backgroundAnimationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>().faster(50f)

    val isReduceMotionEnabled = LocalReduceMotion.current.enabled()

    if (!isReduceMotionEnabled) {
        LaunchedEffect(Unit) {
            snapshotFlow { showState }
                .collectLatest {
                    if (it) {
                        backgroundAnimatable.animateTo(0.85f, backgroundAnimationSpec) {
                            scaffoldState.parentScale.floatValue = value
                        }
                    } else {
                        backgroundAnimatable.animateTo(1f, backgroundAnimationSpec) {
                            scaffoldState.parentScale.floatValue = value
                        }
                    }
                }
        }
    }

    if (shouldShow) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
        ) {
            // Disable System dialog animations
            val view = LocalView.current
            val dialogWindowProvider = view.parent as DialogWindowProvider
            dialogWindowProvider.window.setWindowAnimations(android.R.style.Animation)
            dialogWindowProvider.window.setDimAmount(0f)

            val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

            val contentAlpha by animateContentAlpha(transition)
            val scale by animateDialogScale(transition)

            SwipeToDismissBox(
                state = swipeToDismissBoxState,
                modifier =
                    modifier.graphicsLayer {
                        alpha = contentAlpha
                        scaleX = scale
                        scaleY = scale
                    },
                onDismissed = {
                    onDismissRequest()
                    // Reset state for the next time this dialog is shown.
                    transitionState = MutableTransitionState(DialogVisibility.Hide)
                }
            ) { isBackground ->
                if (!isBackground) {
                    Box(
                        modifier =
                            Modifier.matchParentSize()
                                .background(MaterialTheme.colorScheme.background)
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                    ) {
                        content()
                    }
                }
            }
        }
        LaunchedEffect(visible) {
            if (visible) {
                // a) Fade out previous screen contents b) Scale down dialog contents from 125%
                transitionState.targetState = DialogVisibility.Display
            } else {
                // a) Fade out dialog contents b) Scale up dialog contents.
                transitionState.targetState = DialogVisibility.Hide
            }
        }
    }

    // We want to be sure that background is scaled back to 1f after dialog is disposed.
    DisposableEffect(Unit) { onDispose { scaffoldState.parentScale.floatValue = 1f } }
}

@Composable
private fun animateContentAlpha(transition: Transition<DialogVisibility>): State<Float> {
    val dialogAlphaAnimationSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>().faster(50f)
    return transition.animateFloat(
        transitionSpec = {
            when (transition.targetState) {
                DialogVisibility.Display -> dialogAlphaAnimationSpec
                DialogVisibility.Hide -> standard().fastEffectsSpec()
            }
        },
        label = "background-scrim-alpha"
    ) { stage ->
        when (stage) {
            DialogVisibility.Hide -> 0f
            DialogVisibility.Display -> 1f
        }
    }
}

@Composable
private fun animateDialogScale(transition: Transition<DialogVisibility>): State<Float> {
    val dialogAnimationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>().faster(50f)
    return transition.animateFloat(
        transitionSpec = {
            when (transition.targetState) {
                DialogVisibility.Display -> dialogAnimationSpec
                DialogVisibility.Hide -> dialogAnimationSpec
            }
        },
        label = "scale"
    ) { stage ->
        when (stage) {
            DialogVisibility.Hide -> 1.25f
            DialogVisibility.Display -> 1.0f
        }
    }
}

private enum class DialogVisibility {
    Hide,
    Display
}
