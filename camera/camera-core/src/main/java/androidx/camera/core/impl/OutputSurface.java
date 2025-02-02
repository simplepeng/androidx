/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.camera.core.impl;

import android.util.Size;
import android.view.Surface;

import com.google.auto.value.AutoValue;

import org.jspecify.annotations.NonNull;

/**
 * A class wrapping output surface information for initializing {@link SessionProcessor}.
 */
@AutoValue
public abstract class OutputSurface {
    /**
     * Creates an OutputSurface instance.
     */
    public static @NonNull OutputSurface create(
            @NonNull Surface surface, @NonNull Size size, int imageFormat) {
        return new AutoValue_OutputSurface(surface, size, imageFormat);
    }

    /**
     * Gets the {@link Surface}.
     */
    public abstract @NonNull Surface getSurface();

    /**
     * Gets the size of the {@link Surface}.
     */
    public abstract @NonNull Size getSize();

    /**
     * Gets the image format of the {@link Surface}.
     */
    public abstract int getImageFormat();
}
