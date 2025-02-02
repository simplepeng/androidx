/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.camera.camera2.internal.compat.params;

import android.hardware.camera2.params.DynamicRangeProfiles;

import androidx.camera.core.DynamicRange;
import androidx.core.util.Preconditions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

class DynamicRangesCompatBaseImpl implements DynamicRangesCompat.DynamicRangeProfilesCompatImpl {

    static final DynamicRangesCompat COMPAT_INSTANCE =
            new DynamicRangesCompat(new DynamicRangesCompatBaseImpl());

    private static final Set<DynamicRange> SDR_ONLY = Collections.singleton(DynamicRange.SDR);
    @Override
    public @NonNull Set<DynamicRange> getDynamicRangeCaptureRequestConstraints(
            @NonNull DynamicRange dynamicRange) {
        Preconditions.checkArgument(DynamicRange.SDR.equals(dynamicRange),
                "DynamicRange is not supported: " + dynamicRange);
        return SDR_ONLY;
    }

    @Override
    public @NonNull Set<DynamicRange> getSupportedDynamicRanges() {
        return SDR_ONLY;
    }

    @Override
    public boolean isExtraLatencyPresent(@NonNull DynamicRange dynamicRange) {
        Preconditions.checkArgument(DynamicRange.SDR.equals(dynamicRange),
                "DynamicRange is not supported: " + dynamicRange);
        return false;
    }

    @Override
    public @Nullable DynamicRangeProfiles unwrap() {
        return null;
    }
}
