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

package androidx.work.integration.testapp;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * A {@link androidx.work.Worker} which requests itself to be scheduled.
 */
public class RecursiveWorker extends Worker {

    public static String TAG = "RecursiveWorker";

    public RecursiveWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @Override
    public @NonNull Result doWork() {
        OneTimeWorkRequest newRequest = new OneTimeWorkRequest.Builder(RecursiveWorker.class)
                .addTag(TAG)
                .setInitialDelay(100, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(newRequest);
        return Result.success();
    }
}
