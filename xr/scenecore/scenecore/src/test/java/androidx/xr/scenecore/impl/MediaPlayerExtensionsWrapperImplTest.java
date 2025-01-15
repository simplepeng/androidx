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

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.media.MediaPlayer;

import androidx.xr.extensions.media.SpatializerExtensions;
import androidx.xr.extensions.media.XrSpatialAudioExtensions;
import androidx.xr.extensions.node.Node;
import androidx.xr.scenecore.JxrPlatformAdapter;
import androidx.xr.scenecore.JxrPlatformAdapter.MediaPlayerExtensionsWrapper;
import androidx.xr.scenecore.testing.FakeXrExtensions;
import androidx.xr.scenecore.testing.FakeXrExtensions.FakeMediaPlayerExtensions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MediaPlayerExtensionsWrapperImplTest {
    FakeXrExtensions mFakeXrExtensions;
    XrSpatialAudioExtensions mSpatialAudioExtensions;
    FakeMediaPlayerExtensions mFakeMediaPlayerExtensions;

    @Before
    public void setUp() {
        mFakeXrExtensions = new FakeXrExtensions();
        mSpatialAudioExtensions = mFakeXrExtensions.fakeSpatialAudioExtensions;
        mFakeMediaPlayerExtensions =
                (FakeMediaPlayerExtensions) mSpatialAudioExtensions.getMediaPlayerExtensions();
    }

    @Test
    public void setPointSourceAttr_callsExtensionsSetPointSourceAttr() {
        MediaPlayer mediaPlayer = new MediaPlayer();

        Node fakeNode = new FakeXrExtensions().createNode();
        AndroidXrEntity entity = mock(AndroidXrEntity.class);
        when(entity.getNode()).thenReturn(fakeNode);

        JxrPlatformAdapter.PointSourceAttributes expectedRtAttr =
                new JxrPlatformAdapter.PointSourceAttributes(entity);

        MediaPlayerExtensionsWrapper wrapper =
                new MediaPlayerExtensionsWrapperImpl(mFakeMediaPlayerExtensions);
        wrapper.setPointSourceAttributes(mediaPlayer, expectedRtAttr);

        assertThat(mFakeMediaPlayerExtensions.getPointSourceAttributes().getNode())
                .isEqualTo(fakeNode);
    }

    @Test
    public void setSoundFieldAttr_callsExtensionsSetSoundFieldAttr() {
        MediaPlayer mediaPlayer = new MediaPlayer();

        int expectedAmbisonicOrder = SpatializerExtensions.AMBISONICS_ORDER_THIRD_ORDER;
        JxrPlatformAdapter.SoundFieldAttributes expectedRtAttr =
                new JxrPlatformAdapter.SoundFieldAttributes(
                        JxrPlatformAdapter.SpatializerConstants.AMBISONICS_ORDER_THIRD_ORDER);

        MediaPlayerExtensionsWrapper wrapper =
                new MediaPlayerExtensionsWrapperImpl(mFakeMediaPlayerExtensions);
        wrapper.setSoundFieldAttributes(mediaPlayer, expectedRtAttr);

        assertThat(mFakeMediaPlayerExtensions.getSoundFieldAttributes().getAmbisonicsOrder())
                .isEqualTo(expectedAmbisonicOrder);
    }
}
