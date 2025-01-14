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

package androidx.pdf.content

import android.graphics.RectF
import androidx.annotation.RestrictTo

/**
 * Represents a continuous stream of text in a page of a PDF document in the order of viewing.
 *
 * @param bounds: Bounds for the text content
 * @param text: Text content within the bounds.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PdfPageTextContent(public val bounds: List<RectF>, public val text: String)
