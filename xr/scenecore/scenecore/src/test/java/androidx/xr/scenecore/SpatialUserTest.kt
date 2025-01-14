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

package androidx.xr.scenecore

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyInt
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpatialUserTest {

    private val mockRuntime = mock<JxrPlatformAdapter>()
    lateinit var spatialUser: SpatialUser

    @Before
    fun setUp() {
        whenever(mockRuntime.headActivityPose).thenReturn(mock())
        whenever(mockRuntime.getCameraViewActivityPose(anyInt())).thenReturn(mock())
        spatialUser = SpatialUser.create(mockRuntime)
    }

    @Test
    fun getHeadActivityPose_returnsNullIfNoRtActivityPose() {
        whenever(mockRuntime.headActivityPose).thenReturn(null)
        val head = spatialUser.head
        assertThat(head).isNull()
    }

    @Test
    fun getHeadActivityPose_returnsNullThenHeadWhenAvailable() {
        whenever(mockRuntime.headActivityPose).thenReturn(null)
        var head = spatialUser.head
        assertThat(head).isNull()

        whenever(mockRuntime.headActivityPose).thenReturn(mock())
        head = spatialUser.head
        assertThat(head).isNotNull()
    }

    @Test
    fun getHeadActivityPose_returnsHeadActivityPose() {
        val head = spatialUser.head
        assertThat(head).isNotNull()
    }

    @Test
    fun getHeadActivityPoseTwice_returnsSameHeadActivityPose() {
        val head1 = spatialUser.head
        val head2 = spatialUser.head

        assertThat(head1).isEqualTo(head2)
    }

    @Test
    fun getNullCameraViews_returnsNullCameraViews() {
        whenever(mockRuntime.getCameraViewActivityPose(anyInt())).thenReturn(null)
        val leftView = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)
        assertThat(leftView).isNull()

        val rightView = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)
        assertThat(rightView).isNull()
    }

    @Test
    fun getCameraViews_returnsNullThenCameraViewsWhenAvailable() {
        whenever(mockRuntime.getCameraViewActivityPose(anyInt())).thenReturn(null)
        var leftView = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)
        assertThat(leftView).isNull()

        var rightView = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)
        assertThat(rightView).isNull()

        whenever(mockRuntime.getCameraViewActivityPose(anyInt())).thenReturn(mock())
        leftView = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)
        assertThat(leftView).isNotNull()

        rightView = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)
        assertThat(rightView).isNotNull()
    }

    @Test
    fun getCameraViews_returnsCameraView() {
        val leftView = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)
        assertThat(leftView).isNotNull()

        val rightView = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)
        assertThat(rightView).isNotNull()
    }

    @Test
    fun getCameraViewsTwice_returnsSameCameraView() {
        val leftView1 = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)
        val leftView2 = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)

        assertThat(leftView1).isEqualTo(leftView2)

        val rightView1 = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)
        val rightView2 = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)

        assertThat(rightView1).isEqualTo(rightView2)
    }

    @Test
    fun getCameraViews_returnsCameraViews() {
        val cameraViews = spatialUser.getCameraViews()

        val leftView = spatialUser.getCameraView(CameraView.CameraType.LEFT_EYE)
        val rightView = spatialUser.getCameraView(CameraView.CameraType.RIGHT_EYE)

        assertThat(cameraViews).containsExactly(leftView, rightView)
    }

    @Test
    fun getCameraViews_returnsEmptyListIfNullCamera() {
        val mockRuntimeNoCamera = mock<JxrPlatformAdapter>()
        whenever(mockRuntimeNoCamera.headActivityPose).thenReturn(mock())
        whenever(mockRuntimeNoCamera.getCameraViewActivityPose(anyInt())).thenReturn(null)
        val spatialUserNoCamera = SpatialUser.create(mockRuntimeNoCamera)

        val cameraViews = spatialUserNoCamera.getCameraViews()

        assertThat(cameraViews).isEmpty()
    }
}
