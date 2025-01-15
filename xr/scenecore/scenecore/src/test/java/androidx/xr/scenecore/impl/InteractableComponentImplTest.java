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
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;

import androidx.xr.extensions.node.Vec3;
import androidx.xr.runtime.math.Pose;
import androidx.xr.scenecore.JxrPlatformAdapter.Entity;
import androidx.xr.scenecore.JxrPlatformAdapter.InputEventListener;
import androidx.xr.scenecore.JxrPlatformAdapter.InteractableComponent;
import androidx.xr.scenecore.impl.perception.PerceptionLibrary;
import androidx.xr.scenecore.impl.perception.Session;
import androidx.xr.scenecore.testing.FakeImpressApi;
import androidx.xr.scenecore.testing.FakeScheduledExecutorService;
import androidx.xr.scenecore.testing.FakeXrExtensions;
import androidx.xr.scenecore.testing.FakeXrExtensions.FakeInputEvent;
import androidx.xr.scenecore.testing.FakeXrExtensions.FakeNode;

import com.google.androidxr.splitengine.SplitEngineSubspaceManager;
import com.google.ar.imp.view.splitengine.ImpSplitEngineRenderer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.concurrent.Executor;

@RunWith(RobolectricTestRunner.class)
public class InteractableComponentImplTest {
    private final ActivityController<Activity> mActivityController =
            Robolectric.buildActivity(Activity.class);
    private final Activity mActivity = mActivityController.create().start().get();
    private final FakeScheduledExecutorService mFakeExecutor = new FakeScheduledExecutorService();
    private final PerceptionLibrary mPerceptionLibrary = mock(PerceptionLibrary.class);
    private final FakeXrExtensions mFakeExtensions = new FakeXrExtensions();
    private final FakeImpressApi mFakeImpressApi = new FakeImpressApi();
    private JxrPlatformAdapterAxr mFakeRuntime;
    SplitEngineSubspaceManager mSplitEngineSubspaceManager =
            Mockito.mock(SplitEngineSubspaceManager.class);
    ImpSplitEngineRenderer mSplitEngineRenderer = Mockito.mock(ImpSplitEngineRenderer.class);

    private Entity createTestEntity() {
        when(mPerceptionLibrary.initSession(eq(mActivity), anyInt(), eq(mFakeExecutor)))
                .thenReturn(immediateFuture(mock(Session.class)));
        mFakeRuntime =
                JxrPlatformAdapterAxr.create(
                        mActivity,
                        mFakeExecutor,
                        mFakeExtensions,
                        mFakeImpressApi,
                        new EntityManager(),
                        mPerceptionLibrary,
                        mSplitEngineSubspaceManager,
                        mSplitEngineRenderer,
                        /* useSplitEngine= */ false);
        return mFakeRuntime.createEntity(new Pose(), "test", mFakeRuntime.getActivitySpace());
    }

    @Test
    public void addInteractableComponent_addsListenerToNode() {
        Entity entity = createTestEntity();
        Executor executor = directExecutor();
        InputEventListener inputEventListener = mock(InputEventListener.class);
        InteractableComponent interactableComponent =
                new InteractableComponentImpl(executor, inputEventListener);
        assertThat(entity.addComponent(interactableComponent)).isTrue();
        FakeNode node = (FakeNode) ((AndroidXrEntity) entity).getNode();

        assertThat(node.getListener()).isNotNull();
        assertThat(node.getExecutor()).isEqualTo(mFakeExecutor);

        FakeInputEvent inputEvent = new FakeInputEvent();
        inputEvent.setOrigin(new Vec3(0, 0, 0));
        inputEvent.setDirection(new Vec3(1, 1, 1));
        node.sendInputEvent(inputEvent);
        mFakeExecutor.runAll();

        assertThat(((AndroidXrEntity) entity).mInputEventListenerMap).isNotEmpty();
        verify(inputEventListener).onInputEvent(any());
    }

    @Test
    public void removeInteractableComponent_removesListenerFromNode() {
        Entity entity = createTestEntity();
        Executor executor = directExecutor();
        InputEventListener inputEventListener = mock(InputEventListener.class);
        InteractableComponent interactableComponent =
                new InteractableComponentImpl(executor, inputEventListener);
        assertThat(entity.addComponent(interactableComponent)).isTrue();
        FakeNode node = (FakeNode) ((AndroidXrEntity) entity).getNode();

        assertThat(node.getListener()).isNotNull();
        assertThat(node.getExecutor()).isEqualTo(mFakeExecutor);

        FakeInputEvent inputEvent = new FakeInputEvent();
        inputEvent.setOrigin(new Vec3(0, 0, 0));
        inputEvent.setDirection(new Vec3(1, 1, 1));
        node.sendInputEvent(inputEvent);
        mFakeExecutor.runAll();

        assertThat(((AndroidXrEntity) entity).mInputEventListenerMap).isNotEmpty();
        verify(inputEventListener).onInputEvent(any());

        entity.removeComponent(interactableComponent);
        assertThat(node.getListener()).isNull();
        assertThat(node.getExecutor()).isNull();
    }

    @Test
    public void interactableComponent_canAttachOnlyOnce() {
        Entity entity = createTestEntity();
        Entity entity2 = createTestEntity();
        Executor executor = directExecutor();
        InputEventListener inputEventListener = mock(InputEventListener.class);
        InteractableComponent interactableComponent =
                new InteractableComponentImpl(executor, inputEventListener);
        assertThat(entity.addComponent(interactableComponent)).isTrue();
        assertThat(entity2.addComponent(interactableComponent)).isFalse();
    }

    @Test
    public void interactableComponent_canAttachAgainAfterDetach() {
        Entity entity = createTestEntity();
        Executor executor = directExecutor();
        InputEventListener inputEventListener = mock(InputEventListener.class);
        InteractableComponent interactableComponent =
                new InteractableComponentImpl(executor, inputEventListener);
        assertThat(entity.addComponent(interactableComponent)).isTrue();
        entity.removeComponent(interactableComponent);
        assertThat(entity.addComponent(interactableComponent)).isTrue();
    }

    @Test
    public void interactableComponent_enablesColliderForGltfEntity() {
        GltfEntityImplSplitEngine gltfEntity = mock(GltfEntityImplSplitEngine.class);
        Executor executor = directExecutor();
        InputEventListener inputEventListener = mock(InputEventListener.class);
        InteractableComponent interactableComponent =
                new InteractableComponentImpl(executor, inputEventListener);
        assertThat(interactableComponent.onAttach(gltfEntity)).isTrue();
        verify(gltfEntity).setColliderEnabled(true);
    }
}
