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

package androidx.pdf.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object TestUtils {
    private val TEMP_FILE_NAME = "temp"
    private val TEMP_FILE_TYPE = ".pdf"

    fun openFile(context: Context, filename: String): Uri {
        val inputStream = context.assets.open(filename)
        return saveStream(context, inputStream)
    }

    private fun saveStream(context: Context, inputStream: InputStream): Uri {
        val tempFile = File.createTempFile(TEMP_FILE_NAME, TEMP_FILE_TYPE, context.cacheDir)
        FileOutputStream(tempFile).use { outputStream -> inputStream.copyTo(outputStream) }
        return Uri.fromFile(tempFile)
    }
}
