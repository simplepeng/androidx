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

package androidx.pdf.view

import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import androidx.pdf.PdfDocument
import androidx.pdf.content.PageSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** Owns and updates all mutable state related to content selection in [PdfView] */
internal class SelectionStateManager(
    private val pdfDocument: PdfDocument,
    private val backgroundScope: CoroutineScope,
    private val handleTouchTargetSizePx: Int,
    initialSelection: SelectionModel? = null,
) {
    /** The current [Selection] */
    var selectionModel: SelectionModel? = initialSelection
        @VisibleForTesting internal set

    /** Replay at few values in case of an UI signal issued while [PdfView] is not collecting */
    private val _selectionUiSignalBus = MutableSharedFlow<SelectionUiSignal>(replay = 3)

    /**
     * This [SharedFlow] serves as an event bus of sorts to signal our host [PdfView] to update its
     * UI in a decoupled way
     */
    val selectionUiSignalBus: SharedFlow<SelectionUiSignal>
        get() = _selectionUiSignalBus

    private var setSelectionJob: Job? = null

    private var draggingState: DraggingState? = null

    /**
     * Potentially updates the location of a drag handle given the [action] and [location] of a
     * [MotionEvent] within the [PdfView]. If a drag handle is moved, the current selection is
     * updated asynchronously.
     *
     * @param currentZoom is used only to scale the size of the drag handle's touch target based on
     *   the zoom factor
     */
    fun maybeDragSelectionHandle(action: Int, location: PdfPoint?, currentZoom: Float): Boolean {
        return when (action) {
            MotionEvent.ACTION_DOWN -> {
                location ?: return false // We can't handle an ACTION_DOWN without a location
                maybeHandleActionDown(location, currentZoom)
            }
            MotionEvent.ACTION_MOVE -> {
                maybeHandleActionMove(location)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> maybeHandleGestureEnd()
            else -> false
        }
    }

    /** Asynchronously attempts to select the nearest block of text to [pdfPoint] */
    fun maybeSelectWordAtPoint(pdfPoint: PdfPoint) {
        _selectionUiSignalBus.tryEmit(SelectionUiSignal.ToggleActionMode(show = false))
        _selectionUiSignalBus.tryEmit(
            SelectionUiSignal.PlayHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        )
        updateSelectionAsync(pdfPoint, pdfPoint)
    }

    /** Synchronously resets all state of this manager */
    fun clearSelection() {
        draggingState = null
        setSelectionJob?.cancel()
        setSelectionJob = null
        _selectionUiSignalBus.tryEmit(SelectionUiSignal.ToggleActionMode(show = false))
        _selectionUiSignalBus.tryEmit(SelectionUiSignal.Invalidate)
        selectionModel = null
    }

    fun maybeShowActionMode() {
        if (selectionModel != null) {
            _selectionUiSignalBus.tryEmit(SelectionUiSignal.ToggleActionMode(show = true))
        }
    }

    fun maybeHideActionMode() {
        _selectionUiSignalBus.tryEmit(SelectionUiSignal.ToggleActionMode(show = false))
    }

    /**
     * Updates the selection to include all text on the 0-indexed [pageNum]. [pageSize] is used to
     * tell [PdfDocument] to select the whole page.
     */
    // TODO(b/386398335) Update this to accept a range of pages for select all, once we support
    // multi-page selections
    // TODO(b/386417152) Update this to use index-based selection once that's supported by
    // PdfDocument
    fun selectAllTextOnPageAsync(pageNum: Int, pageSize: Point) {
        updateSelectionAsync(
            PdfPoint(pageNum, PointF(0F, 0F)),
            PdfPoint(pageNum, PointF(pageSize.x.toFloat(), pageSize.y.toFloat()))
        )
    }

    private fun maybeHandleActionDown(location: PdfPoint, currentZoom: Float): Boolean {
        val currentSelection = selectionModel ?: return false
        val start = currentSelection.startBoundary.location
        val end = currentSelection.endBoundary.location
        val touchTargetContentSize = handleTouchTargetSizePx / currentZoom

        if (location.pageNum == start.pageNum) {
            val startPoint = start.pagePoint
            // Touch target is below and behind the start position, like the start handle
            val startTarget =
                RectF(
                    startPoint.x - touchTargetContentSize,
                    startPoint.y,
                    startPoint.x,
                    startPoint.y + touchTargetContentSize
                )
            if (startTarget.contains(location.pagePoint.x, location.pagePoint.y)) {
                draggingState =
                    DraggingState(
                        currentSelection.endBoundary,
                        currentSelection.startBoundary,
                        location.pagePoint
                    )
                // Play haptic feedback when the user starts dragging the handles
                _selectionUiSignalBus.tryEmit(
                    SelectionUiSignal.PlayHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                )
                return true
            }
        }
        if (location.pageNum == end.pageNum) {
            val endPoint = end.pagePoint
            // Touch target is below and ahead of the end position, like the end handle
            val endTarget =
                RectF(
                    endPoint.x,
                    endPoint.y,
                    endPoint.x + touchTargetContentSize,
                    endPoint.y + touchTargetContentSize
                )
            if (endTarget.contains(location.pagePoint.x, location.pagePoint.y)) {
                draggingState =
                    DraggingState(
                        currentSelection.startBoundary,
                        currentSelection.endBoundary,
                        location.pagePoint
                    )
                // Play haptic feedback when the user starts dragging the handles
                _selectionUiSignalBus.tryEmit(
                    SelectionUiSignal.PlayHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                )
                return true
            }
        }
        return false
    }

    private fun maybeHandleActionMove(location: PdfPoint?): Boolean {
        val prevDraggingState = draggingState ?: return false
        // location == null means the user dragged the handle just outside the bounds of any PDF
        // page.
        // TODO(b/386398335) Properly handle multi-page selections
        if (location == null || location.pageNum != prevDraggingState.dragging.location.pageNum) {
            // When the user drags outside the page, or to another page, we should still "capture"
            // the gesture (i.e. return true) to prevent spurious scrolling while the user is
            // attempting to adjust the selection. Return false if no drag is in progress.
            // See b/385291020
            return draggingState != null
        }
        val dx = location.pagePoint.x - prevDraggingState.downPoint.x
        val dy = location.pagePoint.y - prevDraggingState.downPoint.y
        val newEndPoint = prevDraggingState.dragging.location.translateBy(dx, dy)
        updateSelectionAsync(prevDraggingState.fixed.location, newEndPoint)
        // Hide the action mode while the user is actively dragging the handles
        _selectionUiSignalBus.tryEmit(SelectionUiSignal.ToggleActionMode(show = false))
        return true
    }

    private fun maybeHandleGestureEnd(): Boolean {
        val result = draggingState != null
        draggingState = null
        // If this gesture actually ended a handle drag operation, trigger haptic feedback and
        // reveal the action mode
        if (result) {
            _selectionUiSignalBus.tryEmit(
                SelectionUiSignal.PlayHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
            )
            _selectionUiSignalBus.tryEmit(SelectionUiSignal.ToggleActionMode(show = true))
        }
        return result
    }

    private fun PdfPoint.translateBy(dx: Float, dy: Float): PdfPoint {
        return PdfPoint(this.pageNum, PointF(this.pagePoint.x + dx, this.pagePoint.y + dy))
    }

    private fun updateSelectionAsync(start: PdfPoint, end: PdfPoint) {
        val prevJob = setSelectionJob
        setSelectionJob =
            backgroundScope
                .launch {
                    prevJob?.cancelAndJoin()
                    // TODO(b/386398335) Adapt this logic to support selections that span multiple
                    // pages
                    val newSelection =
                        pdfDocument.getSelectionBounds(
                            start.pageNum,
                            start.pagePoint,
                            end.pagePoint
                        )
                    if (newSelection != null && newSelection.hasBounds) {
                        selectionModel = SelectionModel.fromSinglePageSelection(newSelection)
                        _selectionUiSignalBus.tryEmit(SelectionUiSignal.Invalidate)
                        // Show the action mode if the user is not actively dragging the handles
                        if (draggingState == null) {
                            _selectionUiSignalBus.emit(
                                SelectionUiSignal.ToggleActionMode(show = true)
                            )
                        }
                    }
                }
                .also { it.invokeOnCompletion { setSelectionJob = null } }
    }

    /**
     * Returns true if this [PageSelection] has selected content with bounds, and if its start and
     * end boundaries include their location. Any selection without this information cannot be
     * displayed in the UI, and we expect this information to be present.
     *
     * [androidx.pdf.content.SelectionBoundary] is overloaded as both an input to selection and an
     * output from it, and here we are interacting with it as an output. In the output case, it
     * should always specify its [androidx.pdf.content.SelectionBoundary.point]
     */
    private val PageSelection.hasBounds: Boolean
        get() {
            return this.selectedTextContents.any { it.bounds.isNotEmpty() } &&
                this.start.point != null &&
                this.stop.point != null
        }
}

/** Signals to [PdfView] to update the UI in regards to a change in selection state */
internal sealed interface SelectionUiSignal {
    /** [PdfView] should invalidate itself to reflect a change in selection */
    object Invalidate : SelectionUiSignal

    /**
     * [PdfView] should play haptic feedback to indicate the start or end of a change in selection
     *
     * @param level should be a value from [android.view.HapticFeedbackConstants] indicating the
     *   type of haptic feedback to play
     */
    class PlayHapticFeedback(val level: Int) : SelectionUiSignal

    /** [PdfView] should show or hide the selection action mode */
    class ToggleActionMode(val show: Boolean) : SelectionUiSignal
}

/** Value class to hold state related to dragging a selection handle */
private data class DraggingState(
    val fixed: UiSelectionBoundary,
    val dragging: UiSelectionBoundary,
    val downPoint: PointF
)
