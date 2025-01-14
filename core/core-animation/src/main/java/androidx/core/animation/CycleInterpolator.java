/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.core.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.FloatRange;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Repeats the animation for a specified number of cycles. The
 * rate of change follows a sinusoidal pattern.
 *
 */
public class CycleInterpolator implements Interpolator {

    /**
     * Creates a new instance of {@link CycleInterpolator}.
     *
     * @param cycles  The cycles to repeat.
     */
    public CycleInterpolator(float cycles) {
        mCycles = cycles;
    }

    /**
     * Creates a new instance of {@link CycleInterpolator} from XML.
     *
     * @param context The context.
     * @param attrs The {@link AttributeSet} from XML.
     */
    public CycleInterpolator(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    CycleInterpolator(Resources resources, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, AndroidResources.STYLEABLE_CYCLE_INTERPOLATOR,
                    0, 0);
        } else {
            a = resources.obtainAttributes(attrs, AndroidResources.STYLEABLE_CYCLE_INTERPOLATOR);
        }

        mCycles = a.getFloat(AndroidResources.STYLEABLE_CYCLE_INTERPOLATOR_CYCLES, 1.0f);
        a.recycle();
    }

    @Override
    @FloatRange(from = 0, to = 1)
    public float getInterpolation(@FloatRange(from = 0, to = 1) float input) {
        return (float) Math.sin(2 * mCycles * Math.PI * input);
    }

    private float mCycles;

}
