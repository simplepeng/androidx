/*
 * Copyright 2019 The Android Open Source Project
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

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A interface to return an ordered collection of {@link CaptureStage}.
 */
public interface CaptureBundle {
    /**
     * Returns a list of {@link CaptureStage} in order of how they are to be issued.
     */
    @Nullable List<CaptureStage> getCaptureStages();
}
