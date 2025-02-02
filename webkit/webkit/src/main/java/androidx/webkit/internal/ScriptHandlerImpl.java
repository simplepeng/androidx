/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.webkit.internal;

import androidx.webkit.ScriptHandler;

import org.chromium.support_lib_boundary.ScriptHandlerBoundaryInterface;
import org.chromium.support_lib_boundary.util.BoundaryInterfaceReflectionUtil;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationHandler;

/**
 * Internal implementation of {@link androidx.webkit.ScriptHandler}.
 */
public class ScriptHandlerImpl implements ScriptHandler {
    private final ScriptHandlerBoundaryInterface mBoundaryInterface;

    private ScriptHandlerImpl(@NonNull ScriptHandlerBoundaryInterface boundaryInterface) {
        mBoundaryInterface = boundaryInterface;
    }

    /**
     * Removes the corresponding script from WebView.
     */
    @Override
    public void remove() {
        // If this method is called, the feature must exist, so no need to check feature
        // DOCUMENT_START_JAVASCRIPT.
        mBoundaryInterface.remove();
    }

    /**
     * Create an AndroidX ScriptHandler from the given InvocationHandler.
     */
    public static @NonNull ScriptHandlerImpl toScriptHandler(
            /* ScriptHandler */ @NonNull InvocationHandler invocationHandler) {
        final ScriptHandlerBoundaryInterface boundaryInterface =
                BoundaryInterfaceReflectionUtil.castToSuppLibClass(
                        ScriptHandlerBoundaryInterface.class, invocationHandler);
        return new ScriptHandlerImpl(boundaryInterface);
    }
}
