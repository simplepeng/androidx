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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppFunctionSystemExceptionsTest {
    @Test
    fun testErrorCategory_SystemError() {
        assertThat(AppFunctionSystemUnknownException().errorCode)
            .isEqualTo(AppFunctionException.ERROR_SYSTEM_ERROR)
        assertThat(AppFunctionSystemUnknownException().errorCategory)
            .isEqualTo(AppFunctionException.ERROR_CATEGORY_SYSTEM)

        assertThat(AppFunctionCancelledException().errorCode)
            .isEqualTo(AppFunctionException.ERROR_CANCELLED)
        assertThat(AppFunctionCancelledException().errorCategory)
            .isEqualTo(AppFunctionException.ERROR_CATEGORY_SYSTEM)
    }
}
