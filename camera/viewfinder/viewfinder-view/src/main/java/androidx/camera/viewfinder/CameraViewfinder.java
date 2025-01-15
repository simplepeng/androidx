/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.camera.viewfinder;

import static androidx.camera.viewfinder.core.ViewfinderSurfaceRequest.MIRROR_MODE_HORIZONTAL;
import static androidx.camera.viewfinder.internal.utils.TransformUtils.createTransformInfo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorRes;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import androidx.camera.viewfinder.core.ImplementationMode;
import androidx.camera.viewfinder.core.ScaleType;
import androidx.camera.viewfinder.core.ViewfinderSurfaceRequest;
import androidx.camera.viewfinder.internal.quirk.DeviceQuirks;
import androidx.camera.viewfinder.internal.quirk.SurfaceViewNotCroppedByParentQuirk;
import androidx.camera.viewfinder.internal.quirk.SurfaceViewStretchedQuirk;
import androidx.camera.viewfinder.internal.utils.Logger;
import androidx.camera.viewfinder.internal.utils.Threads;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Base viewfinder widget that can display the camera feed for Camera2.
 *
 * <p> It internally uses either a {@link TextureView} or {@link SurfaceView} to display the
 * camera feed, and applies required transformations on them to correctly display the viewfinder,
 * this involves correcting their aspect ratio, scale and rotation.
 */
public final class CameraViewfinder extends FrameLayout {

    private static final String TAG = "CameraViewFinder";

    @ColorRes private static final int DEFAULT_BACKGROUND_COLOR = android.R.color.black;
    private static final ImplementationMode DEFAULT_IMPL_MODE = ImplementationMode.EXTERNAL;

    // Synthetic access
    @SuppressWarnings("WeakerAccess")
    final @NonNull ViewfinderTransformation mViewfinderTransformation =
            new ViewfinderTransformation();

    @SuppressWarnings("WeakerAccess")
    private final @NonNull DisplayRotationListener mDisplayRotationListener =
            new DisplayRotationListener();

    private final @NonNull Looper mRequiredLooper = Looper.myLooper();

    @NonNull ImplementationMode mImplementationMode;

    // Synthetic access
    @SuppressWarnings("WeakerAccess")
    @Nullable ViewfinderImplementation mImplementation;

    // Synthetic access
    @SuppressWarnings("WeakerAccess")
    @Nullable ViewfinderSurfaceRequest mCurrentSurfaceRequest;

    private final OnLayoutChangeListener mOnLayoutChangeListener =
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                boolean isSizeChanged =
                        right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop;
                if (isSizeChanged) {
                    redrawViewfinder();
                }
            };

    // Synthetic access
    @SuppressWarnings("WeakerAccess")
    final ViewfinderSurfaceProvider mSurfaceProvider = new ViewfinderSurfaceProvider() {

        @Override
        @AnyThread
        public void onSurfaceRequested(
                @NonNull ViewfinderSurfaceRequest surfaceRequest
        ) {
            if (!Threads.isMainThread()) {
                // In short term, throwing exception to guarantee onSurfaceRequest is
                //  called on main thread. In long term, user should be able to specify an
                //  executor to run this function.
                throw new IllegalStateException("onSurfaceRequested must be called on the main  "
                        + "thread");
            }
            Logger.d(TAG, "Surface requested by Viewfinder.");

            if (surfaceRequest.getImplementationMode() != null) {
                mImplementationMode = surfaceRequest.getImplementationMode();
            }

            mImplementation = shouldUseTextureView(mImplementationMode)
                    ? new TextureViewImplementation(
                            CameraViewfinder.this, mViewfinderTransformation)
                    : new SurfaceViewImplementation(
                            CameraViewfinder.this, mViewfinderTransformation);

            mImplementation.onSurfaceRequested(surfaceRequest);

            Display display = getDisplay();
            if (display != null) {
                mViewfinderTransformation.setTransformationInfo(
                        createTransformInfo(surfaceRequest.getResolution(),
                                display,
                                surfaceRequest.getOutputMirrorMode()
                                        == MIRROR_MODE_HORIZONTAL,
                                surfaceRequest.getSourceOrientation()),
                        surfaceRequest.getResolution(),
                        surfaceRequest.getOutputMirrorMode()
                                == MIRROR_MODE_HORIZONTAL);
                redrawViewfinder();
            }
        }
    };

    @UiThread
    public CameraViewfinder(@NonNull Context context) {
        this(context, null);
    }

    @UiThread
    public CameraViewfinder(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @UiThread
    public CameraViewfinder(@NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @UiThread
    public CameraViewfinder(@NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.Viewfinder, defStyleAttr, defStyleRes);
        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.Viewfinder, attrs,
                attributes, defStyleAttr, defStyleRes);

        try {
            final int scaleTypeId = attributes.getInteger(
                    R.styleable.Viewfinder_scaleType,
                    mViewfinderTransformation.getScaleType().getId());
            setScaleType(ScaleType.fromId(scaleTypeId));

            int implementationModeId =
                    attributes.getInteger(R.styleable.Viewfinder_implementationMode,
                            DEFAULT_IMPL_MODE.getId());
            mImplementationMode = ImplementationMode.fromId(
                    implementationModeId);
        } finally {
            attributes.recycle();
        }

        // Set background only if it wasn't already set. A default background prevents the content
        // behind the viewfinder from being visible before the viewfinder starts streaming.
        if (getBackground() == null) {
            setBackgroundColor(ContextCompat.getColor(getContext(), DEFAULT_BACKGROUND_COLOR));
        }
    }

    /**
     * Returns the {@link ImplementationMode}.
     *
     * <p> For each {@link ViewfinderSurfaceRequest} sent to
     * {@link CameraViewfinder}, the
     * {@link ImplementationMode} set in the
     * {@link ViewfinderSurfaceRequest} will be used first.
     * If it's not set, the {@code app:implementationMode} in the layout xml will be used. If
     * it's not set in the layout xml, the default value
     * {@link ImplementationMode#EXTERNAL}
     * will be used. Each {@link ViewfinderSurfaceRequest} sent
     * to {@link CameraViewfinder} can override the
     * {@link ImplementationMode} once it has set the
     * {@link ImplementationMode}.
     *
     * @return The {@link ImplementationMode} for
     * {@link CameraViewfinder}.
     */
    @UiThread
    public @NonNull ImplementationMode getSurfaceImplementationMode() {
        checkUiThread();
        return mImplementationMode;
    }

    /**
     * Applies a {@link ScaleType} to the viewfinder.
     *
     * <p> This value can also be set in the layout XML file via the {@code app:scaleType}
     * attribute.
     *
     * <p> The default value is {@link ScaleType#FILL_CENTER}.
     *
     * <p> This method should be called after {@link CameraViewfinder} is inflated and can be
     * called before or after
     * {@link CameraViewfinder#requestSurfaceAsync(ViewfinderSurfaceRequest)}. The
     * {@link ScaleType} to set will be effective immediately after the method is called.
     *
     * @param scaleType The {@link ScaleType} to apply to the viewfinder.
     * @attr name app:scaleType
     */
    @UiThread
    public void setScaleType(final @NonNull ScaleType scaleType) {
        checkUiThread();
        mViewfinderTransformation.setScaleType(scaleType);
        redrawViewfinder();
    }

    /**
     * Returns the {@link ScaleType} currently applied to the viewfinder.
     *
     * <p> The default value is {@link ScaleType#FILL_CENTER}.
     *
     * @return The {@link ScaleType} currently applied to the viewfinder.
     */
    @UiThread
    public @NonNull ScaleType getScaleType() {
        checkUiThread();
        return mViewfinderTransformation.getScaleType();
    }

    /**
     * Requests surface by sending a
     * {@link ViewfinderSurfaceRequest}.
     *
     * <p> Only one request can be handled at the same time. If requesting a surface with
     * the same {@link ViewfinderSurfaceRequest}, the previous
     * requested surface will be returned. If requesting a surface with a new
     * {@link ViewfinderSurfaceRequest}, the previous
     * requested surface will be released and a new surface will be requested.
     *
     * <p> The result is a {@link ListenableFuture} of {@link Surface}, which provides the
     * functionality to attach listeners and propagate exceptions.
     *
     * <pre>{@code
     * ViewfinderSurfaceRequest request = new ViewfinderSurfaceRequest(
     *     new Size(width, height), cameraManager.getCameraCharacteristics(cameraId));
     *
     * ListenableFuture<Surface> surfaceListenableFuture =
     *     mCameraViewFinder.requestSurfaceAsync(request);
     *
     * Futures.addCallback(surfaceListenableFuture, new FutureCallback<Surface>() {
     *     {@literal @}Override
     *     public void onSuccess({@literal @}Nullable Surface surface) {
     *         if (surface != null) {
     *             createCaptureSession(surface);
     *         }
     *     }
     *
     *     {@literal @}Override
     *     public void onFailure(Throwable t) {}
     * }, ContextCompat.getMainExecutor(getContext()));
     * }</pre>
     *
     * @param surfaceRequest The {@link ViewfinderSurfaceRequest}
     *                       to get a surface.
     * @return The requested surface.
     *
     * @see ViewfinderSurfaceRequest
     */
    @UiThread
    public @NonNull ListenableFuture<Surface> requestSurfaceAsync(
            @NonNull ViewfinderSurfaceRequest surfaceRequest) {
        checkUiThread();

        if (mCurrentSurfaceRequest != null
                && surfaceRequest.equals(mCurrentSurfaceRequest)) {
            return mCurrentSurfaceRequest.getSurfaceAsync();
        }

        if (mCurrentSurfaceRequest != null) {
            mCurrentSurfaceRequest.markSurfaceSafeToRelease();
        }

        ListenableFuture<Surface> surfaceListenableFuture =
                surfaceRequest.getSurfaceAsync();
        mCurrentSurfaceRequest = surfaceRequest;

        provideSurfaceIfReady();

        return surfaceListenableFuture;
    }

    /**
     * Returns a {@link Bitmap} representation of the content displayed on the
     * {@link CameraViewfinder}, or {@code null} if the camera viewfinder hasn't started yet.
     * <p>
     * The returned {@link Bitmap} uses the {@link Bitmap.Config#ARGB_8888} pixel format and its
     * dimensions are the same as this view's.
     * <p>
     * <strong>Do not</strong> invoke this method from a drawing method
     * ({@link View#onDraw(Canvas)} for instance).
     * <p>
     * If an error occurs during the copy, an empty {@link Bitmap} will be returned.
     *
     * @return A {@link Bitmap.Config#ARGB_8888} {@link Bitmap} representing the content
     * displayed on the {@link CameraViewfinder}, or null if the camera viewfinder hasn't started
     * yet.
     */
    @UiThread
    public @Nullable Bitmap getBitmap() {
        checkUiThread();
        return mImplementation == null ? null : mImplementation.getBitmap();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addOnLayoutChangeListener(mOnLayoutChangeListener);
        if (mImplementation != null) {
            mImplementation.onAttachedToWindow();
        }
        startListeningToDisplayChange();

        // TODO: need to handle incomplete surface request if request is received before view
        //  attached to window.
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnLayoutChangeListener(mOnLayoutChangeListener);
        if (mImplementation != null) {
            mImplementation.onDetachedFromWindow();
        }
        if (mCurrentSurfaceRequest != null) {
            mCurrentSurfaceRequest.markSurfaceSafeToRelease();
            mCurrentSurfaceRequest = null;
        }
        stopListeningToDisplayChange();
    }

    @VisibleForTesting
    static boolean shouldUseTextureView(
            final @NonNull ImplementationMode implementationMode
    ) {
        boolean hasSurfaceViewQuirk = DeviceQuirks.get(SurfaceViewStretchedQuirk.class) != null
                ||  DeviceQuirks.get(SurfaceViewNotCroppedByParentQuirk.class) != null;
        if (Build.VERSION.SDK_INT <= 24 || hasSurfaceViewQuirk) {
            // Force to use TextureView when the device is running android 7.0 and below, legacy
            // level or SurfaceView has quirks.
            Logger.d(TAG, "Implementation mode to set is not supported, forcing to use "
                    + "TextureView, because transform APIs are not supported on these devices.");
            return true;
        }
        switch (implementationMode) {
            case EMBEDDED:
                return true;
            case EXTERNAL:
                return false;
            default:
                throw new IllegalArgumentException(
                        "Invalid implementation mode: " + implementationMode);
        }
    }

    // Synthetic access
    @SuppressWarnings("WeakerAccess")
    void redrawViewfinder() {
        if (mImplementation != null) {
            mImplementation.redrawViewfinder();
        }
    }

    private boolean provideSurfaceIfReady() {
        final ViewfinderSurfaceRequest surfaceRequest =
                mCurrentSurfaceRequest;
        final ViewfinderSurfaceProvider surfaceProvider = mSurfaceProvider;
        if (surfaceProvider != null && surfaceRequest != null) {
            surfaceProvider.onSurfaceRequested(surfaceRequest);
            return true;
        }
        return false;
    }

    /**
     * Checks if the current thread is the same UI thread on which the class was constructed.
     *
     * @see <a href = go/android-api-guidelines/concurrency#uithread></a>
     */
    private void checkUiThread() {
        // Ignore mRequiredLooper == null because this can be called from the super
        // class constructor before the class's own constructor has run.
        if (mRequiredLooper != null && Looper.myLooper() != mRequiredLooper) {
            Throwable throwable = new Throwable(
                    "A method was called on thread '" + Thread.currentThread().getName()
                            + "'. All methods must be called on the same thread. (Expected Looper "
                            + mRequiredLooper + ", but called on " + Looper.myLooper() + ".");
            throw new RuntimeException(throwable);
        }
    }

    private void startListeningToDisplayChange() {
        DisplayManager displayManager = getDisplayManager();
        if (displayManager == null) {
            return;
        }
        displayManager.registerDisplayListener(mDisplayRotationListener,
                new Handler(Looper.getMainLooper()));
    }

    private void stopListeningToDisplayChange() {
        DisplayManager displayManager = getDisplayManager();
        if (displayManager == null) {
            return;
        }
        displayManager.unregisterDisplayListener(mDisplayRotationListener);
    }

    private @Nullable DisplayManager getDisplayManager() {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        return (DisplayManager) context.getApplicationContext()
                .getSystemService(Context.DISPLAY_SERVICE);
    }
    /**
     * Listener for display rotation changes.
     *
     * <p> When the device is rotated 180° from side to side, the activity is not
     * destroyed and recreated. This class is necessary to make sure preview's target rotation
     * gets updated when that happens.
     */
    // Synthetic access
    @SuppressWarnings("WeakerAccess")
    class DisplayRotationListener implements DisplayManager.DisplayListener {
        @Override
        public void onDisplayAdded(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
        }

        @Override
        public void onDisplayChanged(int displayId) {
            Display display = getDisplay();
            if (display != null && display.getDisplayId() == displayId) {
                ViewfinderSurfaceRequest surfaceRequest =
                        mCurrentSurfaceRequest;
                if (surfaceRequest != null) {
                    mViewfinderTransformation.updateTransformInfo(
                            createTransformInfo(surfaceRequest.getResolution(),
                                    display,
                                    surfaceRequest.getOutputMirrorMode()
                                            == MIRROR_MODE_HORIZONTAL,
                                    surfaceRequest.getSourceOrientation()));
                    redrawViewfinder();
                }
            }
        }
    }
}
