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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.xr.extensions.XrExtensions;
import androidx.xr.extensions.node.Node;
import androidx.xr.extensions.space.Bounds;
import androidx.xr.extensions.space.SpatialState;
import androidx.xr.runtime.math.Pose;
import androidx.xr.runtime.math.Vector3;
import androidx.xr.scenecore.JxrPlatformAdapter.ActivitySpace;
import androidx.xr.scenecore.JxrPlatformAdapter.Dimensions;
import androidx.xr.scenecore.JxrPlatformAdapter.Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Implementation of a RealityCore ActivitySpaceImpl.
 *
 * <p>This is used to create an entity that contains the task node.
 */
@SuppressWarnings({"deprecation", "UnnecessarilyFullyQualified"}) // TODO(b/373435470): Remove
final class ActivitySpaceImpl extends SystemSpaceEntityImpl implements ActivitySpace {

    private static final String TAG = "ActivitySpaceImpl";

    private final Set<OnBoundsChangedListener> mBoundsListeners =
            Collections.synchronizedSet(new HashSet<>());

    private final Supplier<SpatialState> mSpatialStateProvider;
    private final AtomicReference<Dimensions> mBounds = new AtomicReference<>();

    ActivitySpaceImpl(
            Node taskNode,
            XrExtensions extensions,
            EntityManager entityManager,
            Supplier<SpatialState> spatialStateProvider,
            ScheduledExecutorService executor) {
        super(taskNode, extensions, entityManager, executor);

        mSpatialStateProvider = spatialStateProvider;
    }

    /** Returns the identity pose since this entity defines the origin of the activity space. */
    @Override
    public Pose getPoseInActivitySpace() {
        return new Pose();
    }

    /** Returns the identity pose since we assume the activity space is the world space root. */
    @Override
    public Pose getActivitySpacePose() {

        return new Pose();
    }

    @Override
    public Vector3 getActivitySpaceScale() {
        return new Vector3(1.0f, 1.0f, 1.0f);
    }

    @Override
    public void setParent(Entity parent) {
        Log.e(TAG, "Cannot set parent for the ActivitySpace.");
    }

    @Override
    public void setScale(Vector3 scale) {
        // TODO(b/349391097): make this behavior consistent with AnchorEntityImpl
        Log.e(TAG, "Cannot set scale for the ActivitySpace.");
    }

    @SuppressWarnings("ObjectToString")
    @Override
    public void dispose() {
        Log.i(TAG, "Disposing " + this);
        super.dispose();
    }

    @Override
    public Dimensions getBounds() {
        // The bounds are kept in sync with the Extensions in the onBoundsChangedEvent callback. We
        // only
        // invoke getSpatialState if they've never been set.
        return mBounds.updateAndGet(
                oldBounds -> {
                    if (oldBounds == null) {
                        Bounds bounds = mSpatialStateProvider.get().getBounds();
                        return new Dimensions(bounds.width, bounds.height, bounds.depth);
                    }
                    return oldBounds;
                });
    }

    @Override
    public void addOnBoundsChangedListener(@NonNull OnBoundsChangedListener listener) {
        mBoundsListeners.add(listener);
    }

    @Override
    public void removeOnBoundsChangedListener(@NonNull OnBoundsChangedListener listener) {
        mBoundsListeners.remove(listener);
    }

    /**
     * This method is called by the Runtime when the bounds of the Activity change. We dispatch the
     * event upwards to the JXRCoreSession via ActivitySpace.
     *
     * <p>Note that this call happens on the Activity's UI thread, so we should be careful not to
     * block it.
     */
    public void onBoundsChanged(Bounds newBounds) {
        Dimensions newDimensions =
                mBounds.updateAndGet(
                        oldBounds ->
                                new Dimensions(newBounds.width, newBounds.height, newBounds.depth));
        for (OnBoundsChangedListener listener : mBoundsListeners) {
            listener.onBoundsChanged(newDimensions);
        }
    }
}
