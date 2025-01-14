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

package androidx.camera.camera2.interop;

import android.hardware.camera2.CaptureRequest;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.camera.camera2.impl.Camera2ImplConfig;
import androidx.camera.core.ExtendableBuilder;
import androidx.camera.core.impl.Config;
import androidx.camera.core.impl.MutableConfig;
import androidx.camera.core.impl.MutableOptionsBundle;
import androidx.camera.core.impl.OptionsBundle;
import androidx.camera.core.impl.ReadableConfig;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A bundle of Camera2 capture request options.
 */
@ExperimentalCamera2Interop
public class CaptureRequestOptions implements ReadableConfig {

    private final Config mConfig;

    /**
     * Creates a CaptureRequestOptions for reading Camera2 capture request options from the
     * given config.
     *
     * @param config The config that potentially contains Camera2 capture request options.
     */
    @RestrictTo(Scope.LIBRARY)
    public CaptureRequestOptions(@NonNull Config config) {
        mConfig = config;
    }

    /**
     * Returns a value for the given {@link CaptureRequest.Key} or null if it hasn't been set.
     *
     * @param key            The key to retrieve.
     * @param <ValueT>       The type of the value.
     * @return The stored value or null if the value does not exist in this
     * configuration.
     */
    public <ValueT> @Nullable ValueT getCaptureRequestOption(
            CaptureRequest.@NonNull Key<ValueT> key) {
        @SuppressWarnings(
                "unchecked") // Type should have been only set via Builder#setCaptureRequestOption()
                Option<ValueT> opt = (Option<ValueT>) Camera2ImplConfig.createCaptureRequestOption(
                key);
        return mConfig.retrieveOption(opt, null);
    }

    /**
     * Returns a value for the given {@link CaptureRequest.Key}.
     *
     * @param key            The key to retrieve.
     * @param valueIfMissing The value to return if this configuration option has not been set.
     * @param <ValueT>       The type of the value.
     * @return The stored value or <code>valueIfMissing</code> if the value does not exist in this
     * configuration.
     */
    @RestrictTo(Scope.LIBRARY)
    public <ValueT> @Nullable ValueT getCaptureRequestOption(
            CaptureRequest.@NonNull Key<ValueT> key, @Nullable ValueT valueIfMissing) {
        @SuppressWarnings(
                "unchecked") // Type should have been only set via Builder#setCaptureRequestOption()
                Option<ValueT> opt = (Option<ValueT>) Camera2ImplConfig.createCaptureRequestOption(
                key);
        return mConfig.retrieveOption(opt, valueIfMissing);
    }

    /**
     * Returns the {@link Config} object associated with this {@link CaptureRequestOptions}.
     *
     */
    @RestrictTo(Scope.LIBRARY)
    @Override
    public @NonNull Config getConfig() {
        return mConfig;
    }

    /**
     * Builder for creating {@link CaptureRequestOptions} instance.
     */
    public static final class Builder implements ExtendableBuilder<CaptureRequestOptions> {

        private final MutableOptionsBundle mMutableOptionsBundle = MutableOptionsBundle.create();

        /**
         * Generates a Builder from another Config object.
         *
         * @param config An immutable configuration to pre-populate this builder.
         * @return The new Builder.
         */
        @RestrictTo(Scope.LIBRARY)
        public static CaptureRequestOptions.@NonNull Builder from(@NonNull Config config) {
            CaptureRequestOptions.Builder bundleBuilder = new CaptureRequestOptions.Builder();
            config.findOptions(
                    Camera2ImplConfig.CAPTURE_REQUEST_ID_STEM,
                    option -> {
                        // Erase the type of the option. Capture request options should only be
                        // set via Camera2Interop so that the type of the key and value should
                        // always match.
                        @SuppressWarnings("unchecked")
                        Config.Option<Object> objectOpt = (Config.Option<Object>) option;
                        bundleBuilder.getMutableConfig().insertOption(objectOpt,
                                config.getOptionPriority(objectOpt),
                                config.retrieveOption(objectOpt));
                        return true;
                    });
            return bundleBuilder;
        }

        /**
         * {@inheritDoc}
         *
         */
        @RestrictTo(Scope.LIBRARY)
        @Override
        public @NonNull MutableConfig getMutableConfig() {
            return mMutableOptionsBundle;
        }

        /**
         * Inserts new capture request option with specific {@link CaptureRequest.Key} setting.
         */
        public <ValueT> CaptureRequestOptions.@NonNull Builder setCaptureRequestOption(
                CaptureRequest.@NonNull Key<ValueT> key, @NonNull ValueT value) {
            Option<Object> opt = Camera2ImplConfig.createCaptureRequestOption(key);
            mMutableOptionsBundle.insertOption(opt, value);
            return this;
        }

        /**
         * Removes a capture request option with specific {@link CaptureRequest.Key} setting.
         */
        public <ValueT> CaptureRequestOptions.@NonNull Builder clearCaptureRequestOption(
                CaptureRequest.@NonNull Key<ValueT> key) {
            Config.Option<Object> opt = Camera2ImplConfig.createCaptureRequestOption(key);
            mMutableOptionsBundle.removeOption(opt);
            return this;
        }


        /**
         * Builds an immutable {@link CaptureRequestOptions} from the current state.
         *
         * @return A {@link CaptureRequestOptions} populated with the current state.
         */
        @Override
        public @NonNull CaptureRequestOptions build() {
            return new CaptureRequestOptions(OptionsBundle.from(mMutableOptionsBundle));
        }
    }
}
