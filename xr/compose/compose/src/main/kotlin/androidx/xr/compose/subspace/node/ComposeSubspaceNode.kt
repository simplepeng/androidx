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

package androidx.xr.compose.subspace.node

import androidx.xr.compose.subspace.layout.CoreEntity
import androidx.xr.compose.subspace.layout.MeasurePolicy
import androidx.xr.compose.subspace.layout.SubspaceModifier

/**
 * Represents a Composable Subspace node in the Compose hierarchy.
 *
 * This interface is inspired by [androidx.compose.ui.node.ComposeUiNode].
 */
internal interface ComposeSubspaceNode {

    /** The [MeasurePolicy] used to define the measure and layout behavior of this node. */
    public var measurePolicy: MeasurePolicy

    /** The [SubspaceModifier] applied to this node. */
    public var modifier: SubspaceModifier

    /** The optional [CoreEntity] associated with this node. */
    public var coreEntity: CoreEntity?

    /** An optional name for this node, useful for debugging and identification purposes. */
    public var name: String?

    public companion object {
        /**
         * Constructor function for creating a new [ComposeSubspaceNode].
         *
         * @return an instance of a [ComposeSubspaceNode].
         */
        public val Constructor: () -> ComposeSubspaceNode = SubspaceLayoutNode.Constructor

        /**
         * Sets the [MeasurePolicy] for the given [ComposeSubspaceNode].
         *
         * @param measurePolicy the [MeasurePolicy] to be applied.
         */
        public val SetMeasurePolicy: ComposeSubspaceNode.(MeasurePolicy) -> Unit = {
            this.measurePolicy = it
        }

        /**
         * Sets the [CoreEntity] for the given [ComposeSubspaceNode].
         *
         * @param coreEntity the [CoreEntity] to be associated, or null.
         */
        public val SetCoreEntity: ComposeSubspaceNode.(CoreEntity?) -> Unit = {
            this.coreEntity = it
        }

        /**
         * Sets the [SubspaceModifier] for the given [ComposeSubspaceNode].
         *
         * Note: [SetCoreEntity] should be called before.
         *
         * @param modifier the [SubspaceModifier] to be applied.
         */
        public val SetModifier: ComposeSubspaceNode.(SubspaceModifier) -> Unit = {
            this.modifier = it
        }

        /**
         * Sets the name for the given [ComposeSubspaceNode].
         *
         * @param name the name to be assigned.
         */
        public val SetName: ComposeSubspaceNode.(String) -> Unit = { this.name = it }
    }
}
