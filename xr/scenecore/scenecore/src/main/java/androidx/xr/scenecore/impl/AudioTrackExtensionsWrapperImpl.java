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

package androidx.xr.scenecore.impl;

import android.media.AudioTrack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.xr.extensions.media.AudioTrackExtensions;
import androidx.xr.scenecore.JxrPlatformAdapter.AudioTrackExtensionsWrapper;
import androidx.xr.scenecore.JxrPlatformAdapter.Entity;
import androidx.xr.scenecore.JxrPlatformAdapter.PointSourceAttributes;
import androidx.xr.scenecore.JxrPlatformAdapter.SoundFieldAttributes;

/** Implementation of the {@link AudioTrackExtensionsWrapper} */
final class AudioTrackExtensionsWrapperImpl implements AudioTrackExtensionsWrapper {

    private final AudioTrackExtensions mExtensions;

    private final EntityManager mEntityManager;

    AudioTrackExtensionsWrapperImpl(AudioTrackExtensions extensions, EntityManager entityManager) {
        mExtensions = extensions;
        mEntityManager = entityManager;
    }

    @Nullable
    @Override
    public PointSourceAttributes getPointSourceAttributes(@NonNull AudioTrack audioTrack) {
        androidx.xr.extensions.media.PointSourceAttributes extAttributes =
                mExtensions.getPointSourceAttributes(audioTrack);

        if (extAttributes == null) {
            return null;
        }

        Entity entity = mEntityManager.getEntityForNode(extAttributes.getNode());

        if (entity == null) {
            return null;
        }

        return new PointSourceAttributes(entity);
    }

    @Nullable
    @Override
    public SoundFieldAttributes getSoundFieldAttributes(@NonNull AudioTrack audioTrack) {
        androidx.xr.extensions.media.SoundFieldAttributes extAttributes =
                mExtensions.getSoundFieldAttributes(audioTrack);

        if (extAttributes == null) {
            return null;
        }

        return new SoundFieldAttributes(extAttributes.getAmbisonicsOrder());
    }

    @Override
    public int getSpatialSourceType(@NonNull AudioTrack audioTrack) {
        return MediaUtils.convertExtensionsToSourceType(
                mExtensions.getSpatialSourceType(audioTrack));
    }

    @Override
    @NonNull
    public AudioTrack.Builder setPointSourceAttributes(
            @NonNull AudioTrack.Builder builder, @NonNull PointSourceAttributes attributes) {
        androidx.xr.extensions.media.PointSourceAttributes extAttributes =
                MediaUtils.convertPointSourceAttributesToExtensions(attributes);

        return mExtensions.setPointSourceAttributes(builder, extAttributes);
    }

    @Override
    @NonNull
    public AudioTrack.Builder setSoundFieldAttributes(
            @NonNull AudioTrack.Builder builder, @NonNull SoundFieldAttributes attributes) {
        androidx.xr.extensions.media.SoundFieldAttributes extAttributes =
                MediaUtils.convertSoundFieldAttributesToExtensions(attributes);

        return mExtensions.setSoundFieldAttributes(builder, extAttributes);
    }
}
