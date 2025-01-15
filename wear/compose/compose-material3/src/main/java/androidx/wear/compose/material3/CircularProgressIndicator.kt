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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.tokens.MotionTokens
import androidx.wear.compose.materialcore.isSmallScreen
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.min
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Material Design circular progress indicator. Progress changes are animated.
 *
 * Example of a full screen [CircularProgressIndicator]. Note that the padding
 * [CircularProgressIndicatorDefaults.FullScreenPadding] should be applied:
 *
 * @sample androidx.wear.compose.material3.samples.FullScreenProgressIndicatorSample
 *
 * Example of progress showing overflow value (more than 1) by [CircularProgressIndicator]:
 *
 * @sample androidx.wear.compose.material3.samples.OverflowProgressIndicatorSample
 *
 * Example of progress indicator wrapping media control by [CircularProgressIndicator]:
 *
 * @sample androidx.wear.compose.material3.samples.MediaButtonProgressIndicatorSample
 *
 * Example of a [CircularProgressIndicator] with small progress values:
 *
 * @sample androidx.wear.compose.material3.samples.SmallValuesProgressIndicatorSample
 *
 * Progress indicators express the proportion of completion of an ongoing task.
 *
 * @param progress The progress of this progress indicator where 0.0 represents no progress and 1.0
 *   represents completion. Progress changes will be animated.
 * @param modifier Modifier to be applied to the CircularProgressIndicator.
 * @param allowProgressOverflow When progress overflow is allowed, values smaller than 0.0 will be
 *   coerced to 0, while values larger than 1.0 will be wrapped around and shown as overflow with a
 *   different track color [ProgressIndicatorColors.overflowTrackBrush]. For example values 1.2, 2.2
 *   etc will be shown as 20% progress with the overflow color. When progress overflow is not
 *   allowed, progress values will be coerced into the range 0..1.
 * @param startAngle The starting position of the progress arc, measured clockwise in degrees (0
 *   to 360) from the 3 o'clock position. For example, 0 and 360 represent 3 o'clock, 90 and 180
 *   represent 6 o'clock and 9 o'clock respectively. Default is 270 degrees
 *   [CircularProgressIndicatorDefaults.StartAngle] (top of the screen).
 * @param endAngle The ending position of the progress arc, measured clockwise in degrees (0 to 360)
 *   from the 3 o'clock position. For example, 0 and 360 represent 3 o'clock, 90 and 180 represent 6
 *   o'clock and 9 o'clock respectively. By default equal to [startAngle].
 * @param colors [ProgressIndicatorColors] that will be used to resolve the indicator and track
 *   color for this progress indicator in different states.
 * @param strokeWidth The stroke width for the progress indicator. The recommended values are
 *   [CircularProgressIndicatorDefaults.largeStrokeWidth] and
 *   [CircularProgressIndicatorDefaults.smallStrokeWidth].
 * @param gapSize The size (in Dp) of the gap between the ends of the progress indicator and the
 *   track. The stroke endcaps are not included in this distance.
 * @param enabled controls the enabled state. Although this component is not clickable, it can be
 *   contained within a clickable component. When enabled is `false`, this component will appear
 *   visually disabled.
 */
@Composable
public fun CircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    allowProgressOverflow: Boolean = false,
    startAngle: Float = CircularProgressIndicatorDefaults.StartAngle,
    endAngle: Float = startAngle,
    colors: ProgressIndicatorColors = ProgressIndicatorDefaults.colors(),
    strokeWidth: Dp = CircularProgressIndicatorDefaults.largeStrokeWidth,
    gapSize: Dp = CircularProgressIndicatorDefaults.calculateRecommendedGapSize(strokeWidth),
    enabled: Boolean = true,
) {
    if (allowProgressOverflow) {
        AnimatedCircularProgressIndicatorWithOverflowImpl(
            progress,
            modifier,
            startAngle,
            endAngle,
            colors,
            strokeWidth,
            gapSize,
            enabled
        )
    } else {
        AnimatedCircularProgressIndicatorImpl(
            progress,
            modifier,
            startAngle,
            endAngle,
            colors,
            strokeWidth,
            gapSize,
            enabled
        )
    }
}

/**
 * [CircularProgressIndicatorStatic] is a non-animating circular progress indicator. Prefer to use
 * [CircularProgressIndicator] directly instead of this overload in order to access the recommended
 * animations, but this overload can be used when custom animations are required.
 *
 * Example of [CircularProgressIndicatorStatic] with custom progress animation:
 *
 * @sample androidx.wear.compose.material3.samples.CircularProgressIndicatorStaticSample
 * @param progress The progress of this progress indicator where 0.0 represents no progress and 1.0
 *   represents completion.
 * @param modifier Modifier to be applied to the CircularProgressIndicator.
 * @param allowProgressOverflow When progress overflow is allowed, values smaller than 0.0 will be
 *   coerced to 0, while values larger than 1.0 will be wrapped around and shown as overflow with a
 *   different track color [ProgressIndicatorColors.overflowTrackBrush]. For example values 1.2, 2.2
 *   etc will be shown as 20% progress with the overflow color. When progress overflow is not
 *   allowed, progress values will be coerced into the range 0..1.
 * @param startAngle The starting position of the progress arc, measured clockwise in degrees (0
 *   to 360) from the 3 o'clock position. For example, 0 and 360 represent 3 o'clock, 90 and 180
 *   represent 6 o'clock and 9 o'clock respectively. Default is 270 degrees
 *   [CircularProgressIndicatorDefaults.StartAngle] (top of the screen).
 * @param endAngle The ending position of the progress arc, measured clockwise in degrees (0 to 360)
 *   from the 3 o'clock position. For example, 0 and 360 represent 3 o'clock, 90 and 180 represent 6
 *   o'clock and 9 o'clock respectively. By default equal to [startAngle].
 * @param colors [ProgressIndicatorColors] that will be used to resolve the indicator and track
 *   color for this progress indicator in different states.
 * @param strokeWidth The stroke width for the progress indicator. The recommended values are
 *   [CircularProgressIndicatorDefaults.largeStrokeWidth] and
 *   [CircularProgressIndicatorDefaults.smallStrokeWidth].
 * @param gapSize The size (in Dp) of the gap between the ends of the progress indicator and the
 *   track. The stroke endcaps are not included in this distance.
 * @param enabled controls the enabled state. Although this component is not clickable, it can be
 *   contained within a clickable component. When enabled is `false`, this component will appear
 *   visually disabled.
 * @param overflowColorAlphaFraction Alpha fraction to apply to the overflow color. This can be used
 *   to implement animated transition from progress color to overflow color. For no animation should
 *   be set to 1.
 * @param targetProgress Target value if the progress value is to be animated. Used to determine if
 *   the min progress values should be enforced. For no animation this should be null.
 */
@Composable
public fun CircularProgressIndicatorStatic(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    allowProgressOverflow: Boolean = false,
    startAngle: Float = CircularProgressIndicatorDefaults.StartAngle,
    endAngle: Float = startAngle,
    colors: ProgressIndicatorColors = ProgressIndicatorDefaults.colors(),
    strokeWidth: Dp = CircularProgressIndicatorDefaults.largeStrokeWidth,
    gapSize: Dp = CircularProgressIndicatorDefaults.calculateRecommendedGapSize(strokeWidth),
    enabled: Boolean = true,
    overflowColorAlphaFraction: Float = 1f,
    targetProgress: (() -> Float)? = null,
): Unit {
    // Canvas internally uses Spacer.drawBehind.
    // Using Spacer.drawWithCache to optimize the stroke allocations.
    Spacer(
        modifier
            .clearAndSetSemantics {}
            .fillMaxSize()
            .focusable()
            .drawWithCache {
                onDrawWithContent {
                    val fullSweep = 360f - ((startAngle - endAngle) % 360 + 360) % 360
                    val strokePx = strokeWidth.toPx()
                    val gapSizePx = gapSize.toPx()
                    val minSize = min(size.height, size.width)
                    // Sweep angle between two progress indicator segments.
                    val gapSweep =
                        asin((strokePx + gapSizePx) / (minSize - strokePx)).toDegrees() * 2f
                    val currentProgress = progress()
                    val hasOverflow = allowProgressOverflow && currentProgress > 1f
                    val wrappedProgress = wrapProgress(currentProgress, allowProgressOverflow)
                    var progressSweep = fullSweep * wrappedProgress

                    // If progress sweep or remaining track sweep is smaller than gap sweep, it
                    // will be shown as a small dot. This dot should never be shown as
                    // static value, only in progress animation transitions.
                    val target = if (targetProgress != null) targetProgress() else currentProgress
                    val wrappedTargetProgress = wrapProgress(target, allowProgressOverflow)
                    val isValidTarget =
                        target.isFullInt() ||
                            (!allowProgressOverflow && target == 1 + GapExtraProgress) ||
                            wrappedTargetProgress * fullSweep in gapSweep..fullSweep - gapSweep
                    if (
                        !wrappedProgress.isFullInt() &&
                            !isValidTarget &&
                            floor(currentProgress) == floor(target)
                    ) {
                        progressSweep = progressSweep.coerceIn(gapSweep, fullSweep - gapSweep)
                    }

                    if (hasOverflow) {
                        // Draw the overflow track background.
                        drawIndicatorSegment(
                            startAngle = startAngle + progressSweep,
                            sweep = fullSweep - progressSweep,
                            gapSweep = gapSweep,
                            brush = colors.overflowTrackBrush(enabled, overflowColorAlphaFraction),
                            strokeWidth = strokePx,
                        )
                    } else {
                        // Draw the track background.
                        drawIndicatorSegment(
                            startAngle = startAngle + progressSweep,
                            sweep = fullSweep - progressSweep,
                            gapSweep = gapSweep,
                            brush = colors.trackBrush(enabled),
                            strokeWidth = strokePx,
                        )
                    }

                    if (!allowProgressOverflow && startAngle == endAngle && wrappedProgress == 1f) {
                        // Draw the full circle with merged gap.
                        val gapFraction =
                            (1f + GapExtraProgress - currentProgress).absoluteValue /
                                GapExtraProgress
                        drawIndicatorSegment(
                            startAngle = startAngle,
                            sweep = progressSweep,
                            gapSweep = gapFraction,
                            brush = colors.indicatorBrush(enabled),
                            strokeWidth = strokePx,
                        )
                    } else {
                        // Draw the indicator.
                        drawIndicatorSegment(
                            startAngle = startAngle,
                            sweep = progressSweep,
                            gapSweep = gapSweep,
                            brush = colors.indicatorBrush(enabled),
                            strokeWidth = strokePx,
                        )
                    }
                }
            }
    )
}

/**
 * Indeterminate Material Design circular progress indicator.
 *
 * Indeterminate progress indicator expresses an unspecified wait time and spins indefinitely.
 *
 * Example of indeterminate circular progress indicator:
 *
 * @sample androidx.wear.compose.material3.samples.IndeterminateProgressIndicatorSample
 * @param modifier Modifier to be applied to the CircularProgressIndicator.
 * @param colors [ProgressIndicatorColors] that will be used to resolve the indicator and track
 *   color for this progress indicator.
 * @param strokeWidth The stroke width for the progress indicator. The recommended values is
 *   [CircularProgressIndicatorDefaults.IndeterminateStrokeWidth].
 * @param gapSize The size (in Dp) of the gap between the ends of the progress indicator and the
 *   track. The stroke endcaps are not included in this distance.
 */
@Composable
public fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    colors: ProgressIndicatorColors = ProgressIndicatorDefaults.colors(),
    strokeWidth: Dp = CircularProgressIndicatorDefaults.IndeterminateStrokeWidth,
    gapSize: Dp = CircularProgressIndicatorDefaults.calculateRecommendedGapSize(strokeWidth),
) {
    val stroke =
        with(LocalDensity.current) { Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round) }

    val infiniteTransition = rememberInfiniteTransition()
    // A global rotation that does a 360 degrees rotation in 6 seconds.
    val globalRotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = CircularGlobalRotationDegreesTarget,
            animationSpec = circularIndeterminateGlobalRotationAnimationSpec
        )

    // An additional rotation that moves by 90 degrees in 500ms and then rest for 1 second.
    val additionalRotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = CircularAdditionalRotationDegreesTarget,
            animationSpec = circularIndeterminateRotationAnimationSpec
        )

    // Indicator progress animation that will be changing the progress up and down as the indicator
    // rotates.
    val progressAnimation =
        infiniteTransition.animateFloat(
            initialValue = CircularIndeterminateMinProgress,
            targetValue = CircularIndeterminateMaxProgress,
            animationSpec = circularIndeterminateProgressAnimationSpec
        )

    Canvas(
        modifier.size(CircularProgressIndicatorDefaults.IndeterminateCircularIndicatorDiameter)
    ) {
        val sweep = progressAnimation.value * 360f
        val adjustedGapSize = gapSize + strokeWidth
        val gapSizeSweep = (adjustedGapSize.value / (PI * size.width.toDp().value).toFloat()) * 360f

        rotate(globalRotation.value + additionalRotation.value) {
            drawCircularIndicator(
                sweep + min(sweep, gapSizeSweep),
                360f - sweep - min(sweep, gapSizeSweep) * 2,
                colors.trackBrush,
                stroke
            )
            drawCircularIndicator(startAngle = 0f, sweep, colors.indicatorBrush, stroke)
        }
    }
}

/** Animated circular progress indicator implementation without overflow support. */
@Composable
private fun AnimatedCircularProgressIndicatorImpl(
    progress: () -> Float,
    modifier: Modifier,
    startAngle: Float,
    endAngle: Float,
    colors: ProgressIndicatorColors,
    strokeWidth: Dp,
    gapSize: Dp,
    enabled: Boolean = true,
) {
    val progressAnimationSpec = determinateCircularProgressAnimationSpec
    var shouldAnimateProgress by remember { mutableStateOf(false) }
    val updatedProgress by rememberUpdatedState(progress)
    val animatedProgress = remember {
        Animatable(coercedProgressWithGap(updatedProgress(), startAngle == endAngle))
    }

    LaunchedEffect(Unit) {
        snapshotFlow(updatedProgress).collectLatest {
            if (shouldAnimateProgress) {
                val targetProgress = coercedProgressWithGap(it, startAngle == endAngle)
                // Animate the progress arc.
                animatedProgress.animateTo(targetProgress, animationSpec = progressAnimationSpec)
            } else {
                shouldAnimateProgress = true
            }
        }
    }

    CircularProgressIndicatorStatic(
        progress = { animatedProgress.value },
        modifier = modifier,
        allowProgressOverflow = false,
        startAngle = startAngle,
        endAngle = endAngle,
        colors = colors,
        strokeWidth = strokeWidth,
        gapSize = gapSize,
        enabled = enabled,
        targetProgress = { animatedProgress.targetValue }
    )
}

/** Animated circular progress indicator implementation with overflow support. */
@Composable
private fun AnimatedCircularProgressIndicatorWithOverflowImpl(
    progress: () -> Float,
    modifier: Modifier,
    startAngle: Float,
    endAngle: Float = startAngle,
    colors: ProgressIndicatorColors,
    strokeWidth: Dp,
    gapSize: Dp,
    enabled: Boolean = true,
) {
    val progressAnimationSpec = determinateCircularProgressAnimationSpec
    val colorAnimationSpec = progressOverflowColorAnimationSpec

    var shouldAnimateProgress by remember { mutableStateOf(false) }
    val updatedProgress by rememberUpdatedState(progress)
    val initialProgress = updatedProgress()
    var lastProgress = initialProgress
    val animatedProgress = remember { Animatable(initialProgress) }
    val animatedOverflowColor = remember { Animatable(if (initialProgress > 1f) 1f else 0f) }

    LaunchedEffect(Unit) {
        snapshotFlow(updatedProgress).collectLatest {
            val newProgress = it

            val actualProgressAnimationSpec =
                if ((newProgress - lastProgress).absoluteValue <= 1f) progressAnimationSpec
                else createOverflowProgressAnimationSpec(newProgress, lastProgress)

            if (!shouldAnimateProgress) {
                shouldAnimateProgress = true
            } else if (newProgress > 1f) {
                // Animate the progress arc.
                animatedProgress.animateTo(newProgress, actualProgressAnimationSpec) {
                    // Start overflow color transition when progress crosses 1 (full circle).
                    if (animatedProgress.value.equalsWithTolerance(1f)) {
                        launch { animatedOverflowColor.animateTo(1f, colorAnimationSpec) }
                    }
                }
            } else if (newProgress <= 1f) {
                // Reverse overflow color transition - animate the progress and color at the
                // same time.
                launch {
                    awaitAll(
                        async {
                            animatedProgress.animateTo(newProgress, actualProgressAnimationSpec)
                        },
                        async { animatedOverflowColor.animateTo(0f, colorAnimationSpec) }
                    )
                }
            }

            lastProgress = newProgress
        }
    }

    CircularProgressIndicatorStatic(
        progress = { animatedProgress.value },
        modifier = modifier,
        allowProgressOverflow = true,
        startAngle = startAngle,
        endAngle = endAngle,
        colors = colors,
        strokeWidth = strokeWidth,
        gapSize = gapSize,
        enabled = enabled,
        overflowColorAlphaFraction = animatedOverflowColor.value,
        targetProgress = { animatedProgress.targetValue }
    )
}

/** Contains default values for [CircularProgressIndicator]. */
public object CircularProgressIndicatorDefaults {
    /** Large stroke width for circular progress indicator. */
    public val largeStrokeWidth: Dp
        @Composable get() = if (isSmallScreen()) 8.dp else 12.dp

    /** Small stroke width for circular progress indicator. */
    public val smallStrokeWidth: Dp
        @Composable get() = if (isSmallScreen()) 5.dp else 8.dp

    /**
     * The default angle used for the start of the progress indicator arc.
     *
     * This can be customized with `startAngle` parameter on [CircularProgressIndicator].
     */
    public val StartAngle: Float = 270f

    /**
     * Returns recommended size of the gap based on `strokeWidth`.
     *
     * The absolute value can be customized with `gapSize` parameter on [CircularProgressIndicator].
     */
    public fun calculateRecommendedGapSize(strokeWidth: Dp): Dp = strokeWidth / 3f

    /** Padding used for displaying [CircularProgressIndicator] full screen. */
    public val FullScreenPadding: Dp = PaddingDefaults.edgePadding

    /** Diameter of the indicator circle for indeterminate progress. */
    internal val IndeterminateCircularIndicatorDiameter: Dp = 24.dp

    /** Default stroke width for indeterminate [CircularProgressIndicator]. */
    public val IndeterminateStrokeWidth: Dp = 3.dp
}

private fun coercedProgressWithGap(progress: Float, isFullCircle: Boolean): Float {
    val coercedProgress = progress.coerceIn(0f, 1f)
    return if (isFullCircle && coercedProgress == 1f) {
        // Add extra value to progress for full circle merge animation
        1f + GapExtraProgress
    } else {
        coercedProgress
    }
}

// The indeterminate circular indicator easing constants for its motion
internal val CircularProgressEasing = MotionTokens.EasingStandard
internal const val CircularIndeterminateMinProgress = 0.1f
internal const val CircularIndeterminateMaxProgress = 0.87f

internal const val CircularAnimationProgressDuration = 6000
internal const val CircularAnimationAdditionalRotationDelay = 1500
internal const val CircularAnimationAdditionalRotationDuration = 300
internal const val CircularAdditionalRotationDegreesTarget = 360f
internal const val CircularGlobalRotationDegreesTarget = 1080f

/** A global animation spec for indeterminate circular progress indicator. */
internal val circularIndeterminateGlobalRotationAnimationSpec =
    infiniteRepeatable<Float>(
        animation = tween(CircularAnimationProgressDuration, easing = LinearEasing)
    )

/**
 * An animation spec for indeterminate circular progress indicators that infinitely rotates a 360
 * degrees.
 */
internal val circularIndeterminateRotationAnimationSpec =
    infiniteRepeatable(
        animation =
            keyframes {
                durationMillis = CircularAnimationProgressDuration // 6000ms
                90f at
                    CircularAnimationAdditionalRotationDuration using
                    MotionTokens
                        .EasingEmphasizedDecelerate // MotionTokens.EasingEmphasizedDecelerateCubicBezier // 300ms
                90f at CircularAnimationAdditionalRotationDelay // hold till 1500ms
                180f at
                    CircularAnimationAdditionalRotationDuration +
                        CircularAnimationAdditionalRotationDelay // 1800ms
                180f at CircularAnimationAdditionalRotationDelay * 2 // hold till 3000ms
                270f at
                    CircularAnimationAdditionalRotationDuration +
                        CircularAnimationAdditionalRotationDelay * 2 // 3300ms
                270f at CircularAnimationAdditionalRotationDelay * 3 // hold till 4500ms
                360f at
                    CircularAnimationAdditionalRotationDuration +
                        CircularAnimationAdditionalRotationDelay * 3 // 4800ms
                360f at CircularAnimationProgressDuration // hold till 6000ms
            }
    )

/** An animation spec for indeterminate circular progress indicators progress motion. */
internal val circularIndeterminateProgressAnimationSpec =
    infiniteRepeatable(
        animation =
            keyframes {
                durationMillis = CircularAnimationProgressDuration // 6000ms
                CircularIndeterminateMaxProgress at
                    CircularAnimationProgressDuration / 2 using
                    CircularProgressEasing // 3000ms
                CircularIndeterminateMinProgress at CircularAnimationProgressDuration
            }
    )

// extra progress added for the gap merge animation
internal const val GapExtraProgress = 0.05f
