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

package androidx.xr.compose.testing

import androidx.annotation.RestrictTo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.util.fastAny
import androidx.xr.compose.subspace.node.SubspaceSemanticsNode

/**
 * Verifies that the node is focusable.
 *
 * @return matcher that matches the node if it is focusable.
 * @see SemanticsProperties.Focused
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun isFocusable(): SubspaceSemanticsMatcher = hasKey(SemanticsProperties.Focused)

/**
 * Verifies that the node is not focusable.
 *
 * @return matcher that matches the node if it is not focusable.
 * @see SemanticsProperties.Focused
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun isNotFocusable(): SubspaceSemanticsMatcher =
    SubspaceSemanticsMatcher.keyNotDefined(SemanticsProperties.Focused)

/**
 * Verifies that the node is focused.
 *
 * @return matcher that matches the node if it is focused.
 * @see SemanticsProperties.Focused
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun isFocused(): SubspaceSemanticsMatcher =
    SubspaceSemanticsMatcher.expectValue(SemanticsProperties.Focused, true)

/**
 * Verifies that the node is not focused.
 *
 * @return matcher that matches the node if it is not focused.
 * @see SemanticsProperties.Focused
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun isNotFocused(): SubspaceSemanticsMatcher =
    SubspaceSemanticsMatcher.expectValue(SemanticsProperties.Focused, false)

/**
 * Verifies the node's content description.
 *
 * @param value Value to match as one of the items in the list of content descriptions.
 * @param substring Whether to use substring matching.
 * @param ignoreCase Whether case should be ignored.
 * @return true if the node's content description contains the given [value].
 * @see SemanticsProperties.ContentDescription
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasContentDescription(
    value: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
): SubspaceSemanticsMatcher {
    return if (substring) {
        SubspaceSemanticsMatcher(
            "${SemanticsProperties.ContentDescription.name} contains '$value' " +
                "(ignoreCase: $ignoreCase)"
        ) {
            it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { item ->
                item.contains(value, ignoreCase)
            } ?: false
        }
    } else {
        SubspaceSemanticsMatcher(
            "${SemanticsProperties.ContentDescription.name} = '$value' (ignoreCase: $ignoreCase)"
        ) {
            it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { item ->
                item.equals(value, ignoreCase)
            } ?: false
        }
    }
}

/**
 * Verifies the node's test tag.
 *
 * @param testTag Value to match.
 * @return true if the node is annotated by the given test tag.
 * @see SemanticsProperties.TestTag
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasTestTag(testTag: String): SubspaceSemanticsMatcher =
    SubspaceSemanticsMatcher.expectValue(SemanticsProperties.TestTag, testTag)

/**
 * Verifies that the node is the root semantics node.
 *
 * There is always one root in every node tree, added implicitly by Compose.
 *
 * @return true if the node is the root semantics node.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun isRoot(): SubspaceSemanticsMatcher = SubspaceSemanticsMatcher("isRoot") { it.isRoot }

/**
 * Verifies the node's parent.
 *
 * @param matcher The matcher to use to check the parent.
 * @return true if the node's parent satisfies the given matcher.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasParent(matcher: SubspaceSemanticsMatcher): SubspaceSemanticsMatcher {
    return SubspaceSemanticsMatcher("hasParentThat(${matcher.description})") {
        it.parent?.run { matcher.matches(this) } ?: false
    }
}

/**
 * Verifies the node's children.
 *
 * @param matcher The matcher to use to check the children.
 * @return true if the node has at least one child that satisfies the given matcher.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasAnyChild(matcher: SubspaceSemanticsMatcher): SubspaceSemanticsMatcher {
    return SubspaceSemanticsMatcher("hasAnyChildThat(${matcher.description})") {
        matcher.matchesAny(it.children)
    }
}

/**
 * Verifies the node's siblings.
 *
 * @param matcher The matcher to use to check the siblings. Sibling is defined as a any other node
 *   that shares the same parent.
 * @return true if the node has at least one sibling that satisfies the given matcher.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasAnySibling(matcher: SubspaceSemanticsMatcher): SubspaceSemanticsMatcher {
    return SubspaceSemanticsMatcher("hasAnySiblingThat(${matcher.description})") {
        val node = it
        it.parent?.run { matcher.matchesAny(this.children.filter { child -> child.id != node.id }) }
            ?: false
    }
}

/**
 * Verifies the node's ancestors.
 *
 * @param matcher The matcher to use to check the ancestors. Example: For the following tree
 *
 * ```
 * |-X
 * |-A
 *   |-B
 *     |-C1
 *     |-C2
 * ```
 *
 * In case of C1, we would check the matcher against A and B.
 *
 * @return true if the node has at least one ancestor that satisfies the given matcher.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasAnyAncestor(matcher: SubspaceSemanticsMatcher): SubspaceSemanticsMatcher {
    return SubspaceSemanticsMatcher("hasAnyAncestorThat(${matcher.description})") {
        matcher.matchesAny(it.ancestors)
    }
}

/**
 * Verifies the node's descendants.
 *
 * @param matcher The matcher to use to check the descendants. Example: For the following tree
 *
 * ```
 * |-X
 * |-A
 *   |-B
 *     |-C1
 *     |-C2
 * ```
 *
 * In case of A, we would check the matcher against B, C1 and C2.
 *
 * @return true if the node has at least one descendant that satisfies the given matcher.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public fun hasAnyDescendant(matcher: SubspaceSemanticsMatcher): SubspaceSemanticsMatcher {
    fun checkIfSubtreeMatches(
        matcher: SubspaceSemanticsMatcher,
        node: SubspaceSemanticsNode,
    ): Boolean {
        if (matcher.matchesAny(node.children)) {
            return true
        }

        return node.children.fastAny { checkIfSubtreeMatches(matcher, it) }
    }

    return SubspaceSemanticsMatcher("hasAnyDescendantThat(${matcher.description})") {
        checkIfSubtreeMatches(matcher, it)
    }
}

private val SubspaceSemanticsNode.ancestors: Iterable<SubspaceSemanticsNode>
    get() =
        object : Iterable<SubspaceSemanticsNode> {
            override fun iterator(): Iterator<SubspaceSemanticsNode> {
                return object : Iterator<SubspaceSemanticsNode> {
                    var next = parent

                    override fun hasNext(): Boolean {
                        return next != null
                    }

                    override fun next(): SubspaceSemanticsNode {
                        return next!!.also { next = it.parent }
                    }
                }
            }
        }

private fun hasKey(key: SemanticsPropertyKey<*>): SubspaceSemanticsMatcher =
    SubspaceSemanticsMatcher.keyIsDefined(key)
