/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.transition;

import android.view.View;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Base class for <code>TransitionPropagation</code>s that care about
 * View Visibility and the center position of the View.
 */
public abstract class VisibilityPropagation extends TransitionPropagation {

    /**
     * The property key used for {@link android.view.View#getVisibility()}.
     */
    private static final String PROPNAME_VISIBILITY = "android:visibilityPropagation:visibility";

    /**
     * The property key used for the center of the View in screen coordinates. This is an
     * int[2] with the index 0 taking the x coordinate and index 1 taking the y coordinate.
     */
    private static final String PROPNAME_VIEW_CENTER = "android:visibilityPropagation:center";

    private static final String[] VISIBILITY_PROPAGATION_VALUES = {
            PROPNAME_VISIBILITY,
            PROPNAME_VIEW_CENTER,
    };

    @Override
    public void captureValues(@NonNull TransitionValues transitionValues) {
        View view = transitionValues.view;
        Integer visibility = (Integer) transitionValues.values.get(Visibility.PROPNAME_VISIBILITY);
        if (visibility == null) {
            visibility = view.getVisibility();
        }
        transitionValues.values.put(PROPNAME_VISIBILITY, visibility);
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        loc[0] += Math.round(view.getTranslationX());
        loc[0] += view.getWidth() / 2;
        loc[1] += Math.round(view.getTranslationY());
        loc[1] += view.getHeight() / 2;
        transitionValues.values.put(PROPNAME_VIEW_CENTER, loc);
    }

    @Override
    public String @Nullable [] getPropagationProperties() {
        return VISIBILITY_PROPAGATION_VALUES;
    }

    /**
     * Returns {@link android.view.View#getVisibility()} for the View at the time the values
     * were captured.
     * @param values The TransitionValues captured at the start or end of the Transition.
     * @return {@link android.view.View#getVisibility()} for the View at the time the values
     * were captured.
     */
    public int getViewVisibility(@Nullable TransitionValues values) {
        if (values == null) {
            return View.GONE;
        }
        Integer visibility = (Integer) values.values.get(PROPNAME_VISIBILITY);
        if (visibility == null) {
            return View.GONE;
        }
        return visibility;
    }

    /**
     * Returns the View's center x coordinate, relative to the screen, at the time the values
     * were captured.
     * @param values The TransitionValues captured at the start or end of the Transition.
     * @return the View's center x coordinate, relative to the screen, at the time the values
     * were captured.
     */
    public int getViewX(@Nullable TransitionValues values) {
        return getViewCoordinate(values, 0);
    }

    /**
     * Returns the View's center y coordinate, relative to the screen, at the time the values
     * were captured.
     * @param values The TransitionValues captured at the start or end of the Transition.
     * @return the View's center y coordinate, relative to the screen, at the time the values
     * were captured.
     */
    public int getViewY(@Nullable TransitionValues values) {
        return getViewCoordinate(values, 1);
    }

    private static int getViewCoordinate(@Nullable TransitionValues values, int coordinateIndex) {
        if (values == null) {
            return -1;
        }

        int[] coordinates = (int[]) values.values.get(PROPNAME_VIEW_CENTER);
        if (coordinates == null) {
            return -1;
        }

        return coordinates[coordinateIndex];
    }

}
