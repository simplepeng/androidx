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

package android.support.mediacompat.client;

import static android.support.mediacompat.testlib.util.TestUtil.assertBundleEquals;

import static org.junit.Assert.assertEquals;

import android.net.Uri;
import android.os.Bundle;

import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;

import org.junit.Test;

/** Tests for {@link android.support.v4.media.MediaDescriptionCompat}. */
@SuppressWarnings("deprecation")
@SmallTest
public class MediaDescriptionCompatTest {

    @SdkSuppress(minSdkVersion = 21)
    @Test
    public void roundTripViaFrameworkObject_returnsEqualMediaUriAndExtras() {
        Uri mediaUri = Uri.parse("androidx://media/uri");
        android.support.v4.media.MediaDescriptionCompat originalDescription =
                new android.support.v4.media.MediaDescriptionCompat.Builder()
                        .setMediaUri(mediaUri)
                        .setExtras(createExtras())
                        .build();

        android.support.v4.media.MediaDescriptionCompat restoredDescription =
                android.support.v4.media.MediaDescriptionCompat.fromMediaDescription(
                        originalDescription.getMediaDescription());

        // Test second round-trip as MediaDescriptionCompat keeps an internal reference to a
        // previously restored platform instance.
        android.support.v4.media.MediaDescriptionCompat restoredDescription2 =
                android.support.v4.media.MediaDescriptionCompat.fromMediaDescription(
                        restoredDescription.getMediaDescription());

        assertEquals(mediaUri, restoredDescription.getMediaUri());
        assertBundleEquals(createExtras(), restoredDescription.getExtras());
        assertEquals(mediaUri, restoredDescription2.getMediaUri());
        assertBundleEquals(createExtras(), restoredDescription2.getExtras());
    }

    @SdkSuppress(minSdkVersion = 21)
    @Test
    public void getMediaDescription_withMediaUri_doesNotTouchExtras() {
        android.support.v4.media.MediaDescriptionCompat originalDescription =
                new android.support.v4.media.MediaDescriptionCompat.Builder()
                        .setMediaUri(Uri.EMPTY)
                        .setExtras(createExtras())
                        .build();
        originalDescription.getMediaDescription();
        assertBundleEquals(createExtras(), originalDescription.getExtras());
    }

    private static Bundle createExtras() {
        Bundle extras = new Bundle();
        extras.putString("key1", "value1");
        extras.putString("key2", "value2");
        return extras;
    }
}
