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

package androidx.xr.compose.platform

import android.util.CloseGuard
import androidx.annotation.RestrictTo
import androidx.xr.compose.subspace.node.SubspaceSemanticsNode

/**
 * Manager for all [SpatialComposeScene]s that are created when the [SceneManager] is running.
 *
 * This is used by the testing framework to keep track of all scene compositions that were created
 * for the purpose of finding the semantic roots.
 */
@Suppress("NotCloseable")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public object SceneManager : AutoCloseable {
    private val registeredScenes: MutableList<SpatialComposeScene> = mutableListOf()
    private var isRunning = false
    private val guard = CloseGuard()

    /**
     * Start keeping track of the scenes that are created. Scenes created before [SceneManager] is
     * running will not be tracked.
     */
    public fun start() {
        isRunning = true
        guard.open("stop")
    }

    /**
     * Stop tracking the created scenes and clear the set of scenes that [SceneManager] was keeping
     * track of.
     */
    public fun stop() {
        guard.close()
        isRunning = false
        registeredScenes.clear()
    }

    /** Alias to [SceneManager.stop] To implement the [AutoCloseable] interface. */
    override fun close() {
        stop()
    }

    internal fun onSceneCreated(scene: SpatialComposeScene) {
        if (isRunning) {
            registeredScenes.add(scene)
        }
    }

    internal fun onSceneDisposed(scene: SpatialComposeScene) {
        if (isRunning) {
            registeredScenes.remove(scene)
        }
    }

    /**
     * Returns all root subspace semantics nodes of all registered scenes.
     *
     * [SceneManager.start] should be called before attempting to get the root subspace semantics
     * nodes. This will throw an [IllegalStateException] if the [SceneManager] is not in a running
     * state.
     */
    public fun getAllRootSubspaceSemanticsNodes(): List<SubspaceSemanticsNode> {
        check(isRunning) { "SceneManager is not started. Call SceneManager.start() first." }
        return registeredScenes.map { SubspaceSemanticsNode(it.rootElement.compositionOwner.root) }
    }

    public fun getSceneCount(): Int {
        return registeredScenes.size
    }
}
