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

package com.android.extensions.xr;

import android.content.Intent;

import androidx.annotation.RestrictTo;

/**
 * The main extensions class that creates or provides instances of various XR Extensions components.
 */
@SuppressWarnings({"unchecked", "deprecation", "all"})
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class XrExtensions {

    public XrExtensions() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Get the current version of the {@link com.android.extensions.xr.XrExtensions XrExtensions}
     * API.
     */
    public int getApiVersion() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously creates a node that can host a 2D panel or 3D subspace.
     *
     * @return A {@link com.android.extensions.xr.node.Node Node}.
     */
    public com.android.extensions.xr.node.Node createNode() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously creates a new transaction that can be used to update multiple {@link
     * com.android.extensions.xr.node.Node Node}'s data and transformation in the 3D space.
     *
     * @return A {@link com.android.extensions.xr.node.NodeTransaction NodeTransaction} that can be
     *     used to queue the updates and submit to backend at once.
     */
    public com.android.extensions.xr.node.NodeTransaction createNodeTransaction() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously creates a subspace.
     *
     * @param splitEngineBridge The splitEngineBridge.
     * @param subspaceId The unique identifier of the subspace.
     * @return A {@link com.android.extensions.xr.subspace.Subspace Subspace} that can be used to
     *     render 3D content in.
     */
    public com.android.extensions.xr.subspace.Subspace createSubspace(
            com.android.extensions.xr.splitengine.SplitEngineBridge splitEngineBridge,
            int subspaceId) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Loads and caches the glTF model in the SpaceFlinger.
     *
     * @param asset The input stream data of the glTF model.
     * @param regionSizeBytes The size of the memory region where the model is stored (in bytes).
     * @param regionOffsetBytes The offset from the beginning of the memory region (in bytes).
     * @param url The URL of the asset to be loaded. This string is only used for caching purposes.
     * @return A {@link java.util.concurrent.CompletableFuture CompletableFuture} that either
     *     contains the {@link com.android.extensions.xr.asset.GltfModelToken GltfModelToken}
     *     representing the loaded model or 'null' if the asset could not be loaded successfully.
     * @deprecated JXR Core doesn't need this anymore as it does the same with Split Engine.
     */
    @Deprecated
    public java.util.concurrent.CompletableFuture<com.android.extensions.xr.asset.GltfModelToken>
            loadGltfModel(
                    java.io.InputStream asset,
                    int regionSizeBytes,
                    int regionOffsetBytes,
                    java.lang.String url) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Views a 3D asset.
     *
     * @param activity The activity which relinquishes control in order to display the model..
     * @param gltfModel The model to display.
     * @return A {@link java.util.concurrent.CompletableFuture CompletableFuture} that notifies the
     *     caller when the session has completed.
     * @deprecated JXR Core doesn't need this anymore as it does the same with Split Engine.
     */
    @Deprecated
    public java.util.concurrent.CompletableFuture<
                    com.android.extensions.xr.XrExtensions.SceneViewerResult>
            displayGltfModel(
                    android.app.Activity activity,
                    com.android.extensions.xr.asset.GltfModelToken gltfModel) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Loads and caches the environment in the SpaceFlinger.
     *
     * @param asset The input stream data of the EXR or JPEG environment.
     * @param regionSizeBytes The size of the memory region where the environment is stored (in
     *     bytes).
     * @param regionOffsetBytes The offset from the beginning of the memory region (in bytes).
     * @param url The URL of the asset to be loaded. This string is only used for caching purposes.
     * @return A {@link java.util.concurrent.CompletableFuture CompletableFuture} that either
     *     contains the {@link com.android.extensions.xr.asset.EnvironmentToken EnvironmentToken}
     *     representing the loaded environment or 'null' if the asset could not be loaded
     *     successfully.
     * @deprecated JXR Core doesn't need this anymore as it does the same with Split Engine.
     */
    @Deprecated
    public java.util.concurrent.CompletableFuture<com.android.extensions.xr.asset.EnvironmentToken>
            loadEnvironment(
                    java.io.InputStream asset,
                    int regionSizeBytes,
                    int regionOffsetBytes,
                    java.lang.String url) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Loads and caches the environment in the SpaceFlinger.
     *
     * @param asset The input stream data of the EXR or JPEG environment.
     * @param regionSizeBytes The size of the memory region where the environment is stored (in
     *     bytes).
     * @param regionOffsetBytes The offset from the beginning of the memory region (in bytes).
     * @param url The URL of the asset to be loaded.
     * @param textureWidth The target width of the final texture which will be downsampled/upsampled
     *     from the original image.
     * @param textureHeight The target height of the final texture which will be
     *     downsampled/upsampled from the original image.
     * @return A {@link java.util.concurrent.CompletableFuture CompletableFuture} that either
     *     contains the {@link com.android.extensions.xr.asset.EnvironmentToken EnvironmentToken}
     *     representing the loaded environment or 'null' if the asset could not be loaded
     *     successfully.
     * @deprecated JXR Core doesn't need this anymore as it does the same with Split Engine.
     */
    @Deprecated
    public java.util.concurrent.CompletableFuture<com.android.extensions.xr.asset.EnvironmentToken>
            loadEnvironment(
                    java.io.InputStream asset,
                    int regionSizeBytes,
                    int regionOffsetBytes,
                    java.lang.String url,
                    int textureWidth,
                    int textureHeight) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Loads and caches the Impress scene in the SpaceFlinger.
     *
     * @param asset The input stream data of the textproto Impress scene.
     * @param regionSizeBytes The size of the memory region where the Impress scene is stored (in
     *     bytes).
     * @param regionOffsetBytes The offset from the beginning of the memory region (in bytes).
     * @return A {@link java.util.concurrent.CompletableFuture CompletableFuture} that either
     *     contains the {@link com.android.extensions.xr.asset.SceneToken SceneToken} representing
     *     the loaded Impress scene or 'null' if the asset could not be loaded successfully.
     * @deprecated JXR Core doesn't need this anymore as it does the same with Split Engine.
     */
    @Deprecated
    public java.util.concurrent.CompletableFuture<com.android.extensions.xr.asset.SceneToken>
            loadImpressScene(
                    java.io.InputStream asset, int regionSizeBytes, int regionOffsetBytes) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously returns a {@link com.android.extensions.xr.splitengine.SplitEngineBridge
     * SplitEngineBridge}.
     *
     * @return A {@link com.android.extensions.xr.splitengine.SplitEngineBridge SplitEngineBridge}.
     */
    public com.android.extensions.xr.splitengine.SplitEngineBridge createSplitEngineBridge() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously returns the implementation of the {@link
     * com.android.extensions.xr.media.XrSpatialAudioExtensions XrSpatialAudioExtensions} component.
     *
     * @return The {@link com.android.extensions.xr.media.XrSpatialAudioExtensions
     *     XrSpatialAudioExtensions}.
     */
    public com.android.extensions.xr.media.XrSpatialAudioExtensions getXrSpatialAudioExtensions() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Attaches the given {@code sceneNode} as the presentation for the given {@code activity} in
     * the space, and asks the system to attach the 2D content of the {@code activity} into the
     * given {@code windowNode}.
     *
     * <p>The {@code sceneNode} will only be visible if the {@code activity} is visible as in a
     * lifecycle state between {@link android.app.Activity#onStart() Activity#onStart()} and {@link
     * android.app.Activity#onStop() Activity#onStop()} and is SPATIAL_UI_CAPABLE too.
     *
     * <p>One activity can only attach one scene node. When a new scene node is attached for the
     * same {@code activity}, the previous one will be detached.
     *
     * @param activity the owner activity of the {@code sceneNode}.
     * @param sceneNode the node to show as the presentation of the {@code activity}.
     * @param windowNode a leash node to allow the app to control the position and size of the
     *     activity's main window.
     * @deprecated Use the new interface with a callback.
     */
    @Deprecated
    public void attachSpatialScene(
            android.app.Activity activity,
            com.android.extensions.xr.node.Node sceneNode,
            com.android.extensions.xr.node.Node windowNode) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Attaches the given {@code sceneNode} as the presentation for the given {@code activity} in
     * the space, and asks the system to attach the 2D content of the {@code activity} into the
     * given {@code windowNode}.
     *
     * <p>The {@code sceneNode} will only be visible if the {@code activity} is visible as in a
     * lifecycle state between {@link android.app.Activity#onStart() Activity#onStart()} and {@link
     * android.app.Activity#onStop() Activity#onStop()} and is SPATIAL_UI_CAPABLE too.
     *
     * <p>One activity can only attach one scene node. When a new scene node is attached for the
     * same {@code activity}, the previous one will be detached.
     *
     * @param activity the owner activity of the {@code sceneNode}.
     * @param sceneNode the node to show as the presentation of the {@code activity}.
     * @param windowNode a leash node to allow the app to control the position and size of the
     *     activity's main window.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_SUCCESS_NOT_VISIBLE: The
     *     request has been accepted, but will not immediately change the spatial state. A spatial
     *     state callback with an updated SpatialState won't run until the activity becomes
     *     SPATIAL_UI_CAPABLE. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The request has
     *     been ignored because the activity is already in the requested state.
     *     XrExtensionResult.XR_RESULT_ERROR_NOT_ALLOWED: The request has been rejected because the
     *     activity does not have the required capability (e.g. called by an embedded guest
     *     activity.) XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error
     *     has happened.
     * @param executor the executor the callback will be called on.
     */
    public void attachSpatialScene(
            android.app.Activity activity,
            com.android.extensions.xr.node.Node sceneNode,
            com.android.extensions.xr.node.Node windowNode,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Detaches the {@code sceneNode} that was previously attached for the {@code activity} via
     * {@link #attachSpatialScene}.
     *
     * <p>When an {@link android.app.Activity Activity} is destroyed, it must call this method to
     * detach the scene node that was attached for itself.
     *
     * @param activity the owner activity of the {@code sceneNode}.
     * @deprecated Use the new interface with a callback.
     */
    @Deprecated
    public void detachSpatialScene(android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Detaches the {@code sceneNode} that was previously attached for the {@code activity} via
     * {@link #attachSpatialScene}.
     *
     * <p>When an {@link android.app.Activity Activity} is destroyed, it must call this method to
     * detach the scene node that was attached for itself.
     *
     * @param activity the owner activity of the {@code sceneNode}.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_SUCCESS_NOT_VISIBLE: The
     *     request has been accepted, but will not immediately change the spatial state. A spatial
     *     state callback with an updated SpatialState won't run until the activity becomes
     *     SPATIAL_UI_CAPABLE. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The request has
     *     been ignored because the activity is already in the requested state.
     *     XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error has
     *     happened.
     * @param executor the executor the callback will be called on.
     */
    public void detachSpatialScene(
            android.app.Activity activity,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Resizes the main window of the given activity to the requested size.
     *
     * @param activity the activity whose main window should be resized.
     * @param width the new main window width in pixels.
     * @param height the new main window height in pixels.
     * @deprecated Use the new interface with a callback.
     */
    @Deprecated
    public void setMainWindowSize(android.app.Activity activity, int width, int height) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Resizes the main window of the given activity to the requested size.
     *
     * @param activity the activity whose main window should be resized.
     * @param width the new main window width in pixels.
     * @param height the new main window height in pixels.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_SUCCESS_NOT_VISIBLE: The
     *     request has been accepted, but will not immediately change the spatial state. A spatial
     *     state callback with an updated SpatialState won't run until the activity becomes
     *     SPATIAL_UI_CAPABLE. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The request has
     *     been ignored because the activity is already in the requested state.
     *     XrExtensionResult.XR_RESULT_ERROR_NOT_ALLOWED: The request has been rejected because the
     *     activity does not have the required capability (e.g. called by an embedded guest
     *     activity.) XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error
     *     has happened.
     * @param executor the executor the callback will be called on.
     */
    public void setMainWindowSize(
            android.app.Activity activity,
            int width,
            int height,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets the main window of the given activity to the curvature radius. Note that it's allowed
     * only for the activity in full space mode.
     *
     * @param activity the activity of the main window to which the curvature should be applied.
     * @param curvatureRadius the panel curvature radius. It is measured in "radius * 1 /
     *     curvature". A value of 0.0f means that the panel will be flat.
     * @deprecated Use Split Engine to create a curved panel.
     */
    @Deprecated
    public void setMainWindowCurvatureRadius(android.app.Activity activity, float curvatureRadius) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Attaches an environment node for a given activity to make it visible.
     *
     * <p>SysUI will attach the environment node to the task node when the activity gains the
     * APP_ENVIRONMENTS_CAPABLE capability.
     *
     * <p>This method can be called multiple times, SysUI will attach the new environment node and
     * detach the old environment node if it exists.
     *
     * @param activity the activity that provides the environment node to attach.
     * @param environmentNode the environment node provided by the activity to be attached.
     * @deprecated Use the new interface with a callback.
     */
    @Deprecated
    public void attachSpatialEnvironment(
            android.app.Activity activity, com.android.extensions.xr.node.Node environmentNode) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Attaches an environment node for a given activity to make it visible.
     *
     * <p>SysUI will attach the environment node to the task node when the activity gains the
     * APP_ENVIRONMENTS_CAPABLE capability.
     *
     * <p>This method can be called multiple times, SysUI will attach the new environment node and
     * detach the old environment node if it exists.
     *
     * @param activity the activity that provides the environment node to attach.
     * @param environmentNode the environment node provided by the activity to be attached.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_SUCCESS_NOT_VISIBLE: The
     *     request has been accepted, but will not immediately change the spatial state. A spatial
     *     state callback with an updated SpatialState won't run until the activity becomes
     *     APP_ENVIRONMENTS_CAPABLE. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The
     *     request has been ignored because the activity is already in the requested state.
     *     XrExtensionResult.XR_RESULT_ERROR_NOT_ALLOWED: The request has been rejected because the
     *     activity does not have the required capability (e.g. called by an embedded guest
     *     activity.) XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error
     *     has happened.
     * @param executor the executor the callback will be called on.
     */
    public void attachSpatialEnvironment(
            android.app.Activity activity,
            com.android.extensions.xr.node.Node environmentNode,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Detaches the environment node and its sub tree for a given activity to make it invisible.
     *
     * <p>This method will detach and cleanup the environment node and its subtree passed from the
     * activity.
     *
     * @param activity the activity with which SysUI will detach and clean up the environment node
     *     tree.
     * @deprecated Use the new interface with a callback.
     */
    @Deprecated
    public void detachSpatialEnvironment(android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Detaches the environment node and its sub tree for a given activity to make it invisible.
     *
     * <p>This method will detach and cleanup the environment node and its subtree passed from the
     * activity.
     *
     * @param activity the activity with which SysUI will detach and clean up the environment node
     *     tree.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_SUCCESS_NOT_VISIBLE: The
     *     request has been accepted, but will not immediately change the spatial state. A spatial
     *     state callback with an updated SpatialState won't run until the activity becomes
     *     APP_ENVIRONMENTS_CAPABLE. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The
     *     request has been ignored because the activity is already in the requested state.
     *     XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error has
     *     happened.
     * @param executor the executor the callback will be called on.
     */
    public void detachSpatialEnvironment(
            android.app.Activity activity,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets a callback to receive {@link com.android.extensions.xr.space.SpatialStateEvent
     * SpatialStateEvent} for the given {@code activity}.
     *
     * <p>One activity can only set one callback. When a new callback is set for the same {@code
     * activity}, the previous one will be cleared.
     *
     * <p>The callback will be triggered immediately with the current state when it is set, for each
     * of the possible events.
     *
     * @param activity the activity for the {@code callback} to listen to.
     * @param callback the callback to set.
     * @param executor the executor that the callback will be called on.
     * @see #clearSpatialStateCallback
     * @deprecated Use registerSpatialStateCallback instead.
     */
    @Deprecated
    public void setSpatialStateCallbackDeprecated(
            android.app.Activity activity,
            com.android.extensions.xr.function.Consumer<
                            com.android.extensions.xr.space.SpatialStateEvent>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously registers a callback to receive {@link android.view.xr.SpatialState
     * SpatialState} for the {@code activity}.
     *
     * <p>One activity can only set one callback. When a new callback is set for the same {@code
     * activity}, the previous one will be cleared.
     *
     * <p>The {@code executor}'s execute() method will soon be called to run the callback with the
     * current state when it is available, but it never happens directly from within this call.
     *
     * <p>This API throws IllegalArgumentException if it is called by an embedded (guest) activity.
     *
     * @param activity the activity for the {@code callback} to listen to.
     * @param callback the callback to set.
     * @param executor the executor that the callback will be called on.
     * @see #clearSpatialStateCallback
     */
    public void setSpatialStateCallback(
            android.app.Activity activity,
            com.android.extensions.xr.function.Consumer<
                            com.android.extensions.xr.space.SpatialState>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously clears the {@link com.android.extensions.xr.space.SpatialStateEvent
     * SpatialStateEvent} callback that was previously set to the {@code activity} via {@link
     * #setSpatialStateCallback}.
     *
     * <p>When an {@link android.app.Activity Activity} is destroyed, it must call this method to
     * clear the callback that was set for itself.
     *
     * @param activity the activity for the {@code callback} to listen to.
     */
    public void clearSpatialStateCallback(android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously creates an {@link com.android.extensions.xr.space.ActivityPanel ActivityPanel}
     * to be embedded inside the given {@code host} activity.
     *
     * <p>Caller must make sure the {@code host} can embed {@link
     * com.android.extensions.xr.space.ActivityPanel ActivityPanel}. See {@link getSpatialState}.
     * When embedding is possible, SpatialState's {@link
     * com.android.extensions.xr.space.SpatialCapabilities SpatialCapabilities} has {@code
     * SPATIAL_ACTIVITY_EMBEDDING_CAPABLE}.
     *
     * <p>For the {@link com.android.extensions.xr.space.ActivityPanel ActivityPanel} to be shown in
     * the scene, caller needs to attach the {@link
     * com.android.extensions.xr.space.ActivityPanel#getNode() ActivityPanel#getNode()} to the scene
     * node attached through {@link #attachSpatialScene}.
     *
     * <p>This API throws IllegalArgumentException if it is called by an embedded (guest) activity.
     *
     * @param host the host activity to embed the {@link
     *     com.android.extensions.xr.space.ActivityPanel ActivityPanel}.
     * @param launchParameters the parameters to define the initial state of the {@link
     *     com.android.extensions.xr.space.ActivityPanel ActivityPanel}.
     * @return the {@link com.android.extensions.xr.space.ActivityPanel ActivityPanel} created.
     * @throws java.lang.IllegalStateException if the {@code host} is not allowed to embed {@link
     *     com.android.extensions.xr.space.ActivityPanel ActivityPanel}.
     */
    public com.android.extensions.xr.space.ActivityPanel createActivityPanel(
            android.app.Activity host,
            com.android.extensions.xr.space.ActivityPanelLaunchParameters launchParameters) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously checks if an activity can be the host to embed an {@link
     * com.android.extensions.xr.space.ActivityPanel ActivityPanel}.
     *
     * <p>Activity inside an {@link com.android.extensions.xr.space.ActivityPanel ActivityPanel}
     * cannot be the host.
     *
     * @param activity the activity to check.
     * @see #createActivityPanel
     * @return true if the embedding is allowed.
     * @deprecated Use {@link getSpatialState} instead. When embedding is possible, SpatialState's
     *     {@link com.android.extensions.xr.space.SpatialCapabilities SpatialCapabilities} has
     *     {@code SPATIAL_ACTIVITY_EMBEDDING_CAPABLE}.
     */
    @Deprecated
    public boolean canEmbedActivityPanel(android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Requests to put an activity in full space mode when it has focus.
     *
     * @param activity the activity that requires to enter full space mode.
     * @return true when the request was sent (when the activity has focus).
     * @deprecated Use requestFullSpaceMode with 3 arguments.
     */
    @Deprecated
    public boolean requestFullSpaceMode(android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Requests to put an activity in home space mode when it has focus.
     *
     * @param activity the activity that requires to enter home space mode.
     * @return true when the request was sent (when the activity has focus).
     * @deprecated Use requestFullSpaceMode with 3 arguments.
     */
    @Deprecated
    public boolean requestHomeSpaceMode(android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Requests to put an activity in a different mode when it has focus.
     *
     * @param activity the activity that requires to enter full space mode.
     * @param requestEnter when true, activity is put in full space mode. Home space mode otherwise.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The
     *     request has been ignored because the activity is already in the requested mode.
     *     XrExtensionResult.XR_RESULT_ERROR_NOT_ALLOWED: The request has been rejected because the
     *     activity does not have the required capability (e.g. not the top activity in a top task
     *     in the desktop, called by an embedded guest activity.)
     *     XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error has
     *     happened.
     * @param executor the executor the callback will be called on.
     */
    public void requestFullSpaceMode(
            android.app.Activity activity,
            boolean requestEnter,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously sets the full space mode flag to the given {@link android.os.Bundle Bundle}.
     *
     * <p>The {@link android.os.Bundle Bundle} then could be used to launch an {@link
     * android.app.Activity Activity} with requesting to enter full space mode through {@link
     * android.app.Activity#startActivity Activity#startActivity}. If there's a bundle used for
     * customizing how the {@link android.app.Activity Activity} should be started by {@link
     * ActivityOptions.toBundle} or {@link androidx.core.app.ActivityOptionsCompat.toBundle}, it's
     * suggested to use the bundle to call this method.
     *
     * <p>The flag will be ignored when no {@link Intent.FLAG_ACTIVITY_NEW_TASK} is set in the
     * bundle, or it is not started from a focused Activity context.
     *
     * @param bundle the input bundle to set with the full space mode flag.
     * @return the input {@code bundle} with the full space mode flag set.
     */
    public android.os.Bundle setFullSpaceStartMode(android.os.Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously sets the inherit full space mode environvment flag to the given {@link
     * android.os.Bundle Bundle}.
     *
     * <p>The {@link android.os.Bundle Bundle} then could be used to launch an {@link
     * android.app.Activity Activity} with requesting to enter full space mode while inherit the
     * existing environment through {@link android.app.Activity#startActivity
     * Activity#startActivity}. If there's a bundle used for customizing how the {@link
     * android.app.Activity Activity} should be started by {@link ActivityOptions.toBundle} or
     * {@link androidx.core.app.ActivityOptionsCompat.toBundle}, it's suggested to use the bundle to
     * call this method.
     *
     * <p>When launched, the activity will be in full space mode and also inherits the environment
     * from the launching activity. If the inherited environment needs to be animated, the launching
     * activity has to continue updating the environment even after the activity is put into the
     * stopped state.
     *
     * <p>The flag will be ignored when no {@link Intent.FLAG_ACTIVITY_NEW_TASK} is set in the
     * intent, or it is not started from a focused Activity context.
     *
     * <p>The flag will also be ignored when there is no environment to inherit or the activity has
     * its own environment set already.
     *
     * <p>For security reasons, Z testing for the new activity is disabled, and the activity is
     * always drawn on top of the inherited environment. Because Z testing is disabled, the activity
     * should not spatialize itself, and should not curve its panel too much either.
     *
     * @param bundle the input bundle to set with the inherit full space mode environment flag.
     * @return the input {@code bundle} with the inherit full space mode flag set.
     */
    public android.os.Bundle setFullSpaceStartModeWithEnvironmentInherited(
            android.os.Bundle bundle) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets panel curvature radius to the given {@link android.os.Bundle Bundle}.
     *
     * <p>The {@link android.os.Bundle Bundle} then could be used to launch an {@link
     * android.app.Activity Activity} with requesting to a custom curvature radius for the main
     * panel through {@link android.app.Activity#startActivity Activity#startActivity}. If there's a
     * bundle used for customizing how the {@link android.app.Activity Activity} should be started
     * by {@link ActivityOptions.toBundle} or {@link
     * androidx.core.app.ActivityOptionsCompat.toBundle}, it's suggested to use the bundle to call
     * this method.
     *
     * <p>The curvature radius must be used together with {@link
     * #setFullSpaceModeWithEnvironmentInherited(android.os.Bundle)}. Otherwise, it will be ignored.
     *
     * @param bundle the input bundle to set with the inherit full space mode environment flag.
     * @param panelCurvatureRadius the panel curvature radius. It is measured in "radius * 1 /
     *     curvature". A value of 0.0f means the panel is flat.
     * @return the input {@code bundle} with the inherit full space mode flag set.
     * @deprecated Use Split Engine to create a curved panel.
     */
    @Deprecated
    public android.os.Bundle setMainPanelCurvatureRadius(
            android.os.Bundle bundle, float panelCurvatureRadius) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously returns system config information.
     *
     * @return A {@link com.android.extensions.xr.Config Config} object.
     */
    public com.android.extensions.xr.Config getConfig() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Hit-tests a ray against the virtual scene. If the ray hits an object in the scene,
     * information about the hit will be passed to the callback. If nothing is hit, the hit distance
     * will be infinite. Note that attachSpatialScene() must be called before calling this method.
     * Otherwise, an IllegalArgumentException is thrown.
     *
     * @param activity the requesting activity.
     * @param origin the origin of the ray to test, in the activity's task coordinates.
     * @param direction the direction of the ray to test, in the activity's task coordinates.
     * @param callback the callback that will be called with the hit test result.
     * @param executor the executor the callback will be called on.
     */
    public void hitTest(
            android.app.Activity activity,
            com.android.extensions.xr.node.Vec3 origin,
            com.android.extensions.xr.node.Vec3 direction,
            com.android.extensions.xr.function.Consumer<
                            com.android.extensions.xr.space.HitTestResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously returns the OpenXR reference space type.
     *
     * @return the OpenXR reference space type used as world space for the shared scene.
     * @see <a href="https://registry.khronos.org/OpenXR/specs/1.1/html/xrspec.html#spaces-reference-spaces">
     *     OpenXR specs</a>
     */
    public int getOpenXrWorldReferenceSpaceType() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously creates a new ReformOptions instance.
     *
     * @param callback the callback that will be called with reform events.
     * @param executor the executor the callback will be called on.
     * @return the new builder instance.
     */
    public com.android.extensions.xr.node.ReformOptions createReformOptions(
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.node.ReformEvent>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously makes a View findable via findViewById().
     *
     * <p>This is done without it being a child of the given group.
     *
     * @param view the view to add as findable.
     * @param group a group that is part of the hierarchy that findViewById() will be called on.
     */
    public void addFindableView(android.view.View view, android.view.ViewGroup group) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously removes a findable view from the given group.
     *
     * @param view the view to remove as findable.
     * @param group the group to remove the findable view from.
     */
    public void removeFindableView(android.view.View view, android.view.ViewGroup group) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the surface tracking node for a view, if there is one.
     *
     * <p>The surface tracking node is centered on the Surface that the view is attached to, and is
     * sized to match the surface's size. Note that the view's position in the surface can be
     * retrieved via View.getLocationInSurface().
     *
     * @param view the view.
     * @return the surface tracking node, or null if no such node exists.
     */
    public com.android.extensions.xr.node.Node getSurfaceTrackingNode(android.view.View view) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets a preferred main panel aspect ratio for home space mode.
     *
     * <p>The ratio is only applied to the activity. If the activity launches another activity in
     * the same task, the ratio is not applied to the new activity. Also, while the activity is in
     * full space mode, the preference is temporarily removed.
     *
     * @param activity the activity to set the preference.
     * @param preferredRatio the aspect ratio determined by taking the panel's width over its
     *     height. A value <= 0.0f means there are no preferences.
     * @deprecated Use the new interface with a callback.
     */
    @Deprecated
    public void setPreferredAspectRatio(android.app.Activity activity, float preferredRatio) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Sets a preferred main panel aspect ratio for an activity that is not SPATIAL_UI_CAPABLE.
     *
     * <p>The ratio is only applied to the activity. If the activity launches another activity in
     * the same task, the ratio is not applied to the new activity. Also, while the activity is
     * SPATIAL_UI_CAPABLE, the preference is temporarily removed. While the activity is
     * SPATIAL_UI_CAPABLE, use ReformOptions API instead.
     *
     * @param activity the activity to set the preference.
     * @param preferredRatio the aspect ratio determined by taking the panel's width over its
     *     height. A value <= 0.0f means there are no preferences.
     * @param callback the callback that will be called with the result. XrExtensionResult will
     *     indicate either of the following: XrExtensionResult.XR_RESULT_SUCCESS: The request has
     *     been accepted, and the client can expect that a spatial state callback with an updated
     *     SpatialState will run shortly. XrExtensionResult.XR_RESULT_SUCCESS_NOT_VISIBLE: The
     *     request has been accepted, but will not immediately change the spatial state. A spatial
     *     state callback with an updated SpatialState won't run until the activity loses the
     *     SPATIAL_UI_CAPABLE capability. XrExtensionResult.XR_RESULT_IGNORED_ALREADY_APPLIED: The
     *     request has been ignored because the activity is already in the requested state.
     *     XrExtensionResult.XR_RESULT_ERROR_NOT_ALLOWED: The request has been rejected because the
     *     activity does not have the required capability (e.g. called by an embedded guest
     *     activity.) XrExtensionResult.XR_RESULT_ERROR_SYSTEM: A unrecoverable service side error
     *     has happened.
     * @param executor the executor the callback will be called on.
     */
    public void setPreferredAspectRatio(
            android.app.Activity activity,
            float preferredRatio,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.XrExtensionResult>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Gets the spatial capabilities of the activity.
     *
     * @param activity the activity to get the capabilities.
     * @param callback the callback to run. If the activity is not found in SysUI, the callback runs
     *     with a null SpatialCapabilities.
     * @param executor the executor that the callback will be called on.
     * @deprecated Use getSpatialState synchronous getter.
     */
    @Deprecated
    public void getSpatialCapabilities(
            android.app.Activity activity,
            com.android.extensions.xr.function.Consumer<
                            com.android.extensions.xr.space.SpatialCapabilities>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Gets the bounds of the activity.
     *
     * @param activity the activity to get the bounds.
     * @param callback the callback to run. If the activity is not found in SysUI, the callback runs
     *     with a null Bounds.
     * @param executor the executor that the callback will be called on.
     * @deprecated Use getSpatialState synchronous getter.
     */
    @Deprecated
    public void getBounds(
            android.app.Activity activity,
            com.android.extensions.xr.function.Consumer<com.android.extensions.xr.space.Bounds>
                    callback,
            java.util.concurrent.Executor executor) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Synchronously gets the spatial state of the activity.
     *
     * <p>Do not call the API from the Binder thread. That may cause a deadlock.
     *
     * <p>This API throws IllegalArgumentException if it is called by an embedded (guest) activity,
     * and also throws RuntimeException if the calling thread is interrupted.
     *
     * @param activity the activity to get the capabilities.
     * @return the state of the activity.
     */
    public com.android.extensions.xr.space.SpatialState getSpatialState(
            android.app.Activity activity) {
        throw new RuntimeException("Stub!");
    }

    /**
     * The result of a displayGltfModel request.
     *
     * @deprecated JXR Core doesn't need this anymore as it does the same with Split Engine.
     */
    @SuppressWarnings({"unchecked", "deprecation", "all"})
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @Deprecated
    public class SceneViewerResult {

        @Deprecated
        public SceneViewerResult() {
            throw new RuntimeException("Stub!");
        }
    }
}
