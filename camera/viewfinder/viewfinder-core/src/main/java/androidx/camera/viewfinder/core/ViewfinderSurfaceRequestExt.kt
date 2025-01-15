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
@file:JvmName("ViewfinderSurfaceRequestUtil")

package androidx.camera.viewfinder.core

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import androidx.camera.viewfinder.core.ViewfinderSurfaceRequest.Companion.MIRROR_MODE_HORIZONTAL
import androidx.camera.viewfinder.core.ViewfinderSurfaceRequest.Companion.MIRROR_MODE_NONE

/**
 * Populates [ViewfinderSurfaceRequest.Builder] from [CameraCharacteristics].
 *
 * The [CameraCharacteristics] will be used to populate information including lens facing, sensor
 * orientation and [ImplementationMode]. If the hardware level is legacy, the [ImplementationMode]
 * will be set to [ImplementationMode.EMBEDDED].
 */
fun ViewfinderSurfaceRequest.Builder.populateFromCharacteristics(
    cameraCharacteristics: CameraCharacteristics
): ViewfinderSurfaceRequest.Builder {
    val lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)!!
    val mirrorMode =
        if (lensFacing == CameraMetadata.LENS_FACING_FRONT) MIRROR_MODE_HORIZONTAL
        else MIRROR_MODE_NONE
    setOutputMirrorMode(mirrorMode)
    setSourceOrientation(cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!)
    if (
        cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
            CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
    ) {
        setImplementationMode(ImplementationMode.EMBEDDED)
    }
    return this
}
