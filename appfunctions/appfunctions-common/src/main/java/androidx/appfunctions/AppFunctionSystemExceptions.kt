/*
 * Copyright 2025 The Android Open Source Project
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

package androidx.appfunctions

import android.os.Bundle
import androidx.appfunctions.AppFunctionException.Companion.ERROR_CATEGORY_SYSTEM
import androidx.core.util.Preconditions

/**
 * Thrown when an internal unexpected error comes from the system.
 *
 * <p>For example, the AppFunctionService implementation is not found by the system.
 *
 * <p>Reports errors of the category [ERROR_CATEGORY_SYSTEM].
 */
public abstract class AppFunctionSystemException
internal constructor(errorCode: Int, errorMessage: String? = null, extras: Bundle) :
    AppFunctionException(errorCode, errorMessage, extras) {
    init {
        Preconditions.checkArgument(errorCategory == ERROR_CATEGORY_SYSTEM)
    }
}

/**
 * Thrown when an internal unexpected error comes from the system.
 *
 * <p>For example, the AppFunctionService implementation is not found by the system.
 *
 * <p>This error is in the [ERROR_CATEGORY_SYSTEM] category.
 */
public class AppFunctionSystemUnknownException
internal constructor(errorMessage: String? = null, extras: Bundle) :
    AppFunctionSystemException(ERROR_SYSTEM_ERROR, errorMessage, extras) {

    public constructor(errorMessage: String? = null) : this(errorMessage, Bundle.EMPTY)
}

/**
 * Thrown when an operation was cancelled.
 *
 * <p>This error is in the [ERROR_CATEGORY_SYSTEM] category.
 */
public class AppFunctionCancelledException
internal constructor(errorMessage: String? = null, extras: Bundle) :
    AppFunctionSystemException(ERROR_CANCELLED, errorMessage, extras) {

    public constructor(errorMessage: String? = null) : this(errorMessage, Bundle.EMPTY)
}
