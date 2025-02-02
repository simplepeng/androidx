/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.benchmark.benchmark

import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.ExperimentalBlackHoleApi
import androidx.benchmark.TestDefinition
import androidx.benchmark.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalBenchmarkConfigApi::class, ExperimentalBlackHoleApi::class)
@LargeTest
@RunWith(AndroidJUnit4::class)
class MeasureRepeatedSampleBenchmark {
    /** Proof of concept of top-level benchmark function, without a JUnit Rule */
    @Test
    fun increment() {
        println("increment")
        var i: Int = 0
        measureRepeated(
            TestDefinition(
                "androidx.benchmark.benchmark.MeasureRepeatedSampleBenchmark",
                "MeasureRepeatedSampleBenchmark",
                "increment"
            )
        ) {
            i++
        }
    }
}
