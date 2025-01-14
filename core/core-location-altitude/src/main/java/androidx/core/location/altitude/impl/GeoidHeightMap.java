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

package androidx.core.location.altitude.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import androidx.annotation.GuardedBy;
import androidx.core.location.altitude.impl.db.AltitudeConverterDatabase;
import androidx.core.location.altitude.impl.db.MapParamsEntity;
import androidx.core.location.altitude.impl.db.TilesEntity;
import androidx.core.location.altitude.impl.proto.ByteString;
import androidx.core.location.altitude.impl.proto.MapParamsProto;
import androidx.core.location.altitude.impl.proto.S2TileProto;
import androidx.core.util.Preconditions;
import androidx.room.Room;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Manages a mapping of geoid heights associated with S2 cells, referred to as MAP CELLS.
 *
 * <p>Tiles are used extensively to reduce the number of entries needed to be stored in memory and
 * on disk. A tile associates geoid heights with all map cells of a common parent at a specified S2
 * level.
 *
 * <p>Since bilinear interpolation considers at most four map cells at a time, at most four tiles
 * are simultaneously stored in memory. These tiles, referred to as CACHE TILES, are each keyed by
 * its common parent's S2 cell ID, referred to as a CACHE KEY.
 *
 * <p>Absent cache tiles needed for interpolation are constructed from larger tiles stored on disk.
 * The latter tiles, referred to as DISK TILES, are each keyed by its common parent's S2 cell token,
 * referred to as a DISK TOKEN.
 */
final class GeoidHeightMap {

    private static final Object sLock = new Object();

    @GuardedBy("sLock")
    private static @Nullable MapParamsProto sParams;

    /** Defines the resource database for {@link AltitudeConverter}. */
    @GuardedBy("sLock")
    private static @Nullable AltitudeConverterDatabase sDatabase;


    /** Defines a cache large enough to hold all cache tiles needed for interpolation. */
    private final LruCache<Long, S2TileProto> mCacheTiles = new LruCache<>(4);

    public static @NonNull AltitudeConverterDatabase getDatabase(@NonNull Context context) {
        synchronized (sLock) {
            if (sDatabase == null) {
                sDatabase = Room.databaseBuilder(context.getApplicationContext(),
                        AltitudeConverterDatabase.class, "geoid-height-map.db").createFromAsset(
                        "database/geoid-height-map-v0.db").build();
            }
            return sDatabase;
        }
    }

    /**
     * Returns the singleton parameter instance for a spherically projected geoid height map and its
     * corresponding tile management.
     */
    public static @NonNull MapParamsProto getParams(@NonNull Context context) throws IOException {
        synchronized (sLock) {
            if (sParams == null) {
                MapParamsEntity current = getDatabase(context).mapParamsDao().getCurrent();
                if (current == null) {
                    throw new IOException("Unable to load map parameters from raw assets.");
                }
                sParams = current.value();
            }
            return sParams;
        }
    }

    private static long getCacheKey(@NonNull MapParamsProto params, long s2CellId) {
        return S2CellIdUtils.getParent(s2CellId, params.getCacheTileS2Level());
    }

    private static @NonNull String getDiskToken(@NonNull MapParamsProto params, long s2CellId) {
        return S2CellIdUtils.getToken(
                S2CellIdUtils.getParent(s2CellId, params.getDiskTileS2Level()));
    }

    /**
     * Adds to {@code values} values in the unit interval [0, 1] for the map cells identified by
     * {@code s2CellIds}. Returns true if values are present for all IDs; otherwise, returns false
     * and adds NaNs for absent values.
     */
    private static boolean getUnitIntervalValues(@NonNull MapParamsProto params,
            @NonNull TileFunction tileFunction,
            long @NonNull [] s2CellIds, double @NonNull [] values) throws IOException {
        int len = s2CellIds.length;

        S2TileProto[] tiles = new S2TileProto[len];
        for (int i = 0; i < len; i++) {
            long cacheKey = getCacheKey(params, s2CellIds[i]);
            tiles[i] = tileFunction.getTile(cacheKey);
            values[i] = Double.NaN;
        }

        for (int i = 0; i < len; i++) {
            if (tiles[i] == null || !Double.isNaN(values[i])) {
                continue;
            }

            mergeByteBufferValues(params, s2CellIds, tiles, i, values);
            mergeByteJpegValues(params, s2CellIds, tiles, i, values);
            mergeBytePngValues(params, s2CellIds, tiles, i, values);
        }

        boolean allFound = true;
        for (int i = 0; i < len; i++) {
            if (Double.isNaN(values[i])) {
                allFound = false;
            } else {
                values[i] = (((int) values[i]) & 0xFF) / 255.0;
            }
        }
        return allFound;
    }

    @SuppressWarnings("ReferenceEquality")
    private static void mergeByteBufferValues(@NonNull MapParamsProto params,
            long @NonNull [] s2CellIds,
            S2TileProto @NonNull [] tiles,
            int tileIndex, double @NonNull [] values) {
        ByteString byteString = tiles[tileIndex].getByteBuffer();
        if (byteString.isEmpty()) {
            return;
        }

        ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
        int tileS2Level =
                params.getMapS2Level() - Integer.numberOfTrailingZeros(byteBuffer.limit()) / 2;
        int numBitsLeftOfTile = 2 * tileS2Level + 3;

        for (int i = tileIndex; i < tiles.length; i++) {
            if (tiles[i] != tiles[tileIndex]) {
                continue;
            }

            long maskedS2CellId = s2CellIds[i] & (-1L >>> numBitsLeftOfTile);
            int numBitsRightOfMap = 2 * (S2CellIdUtils.MAX_LEVEL - params.getMapS2Level()) + 1;
            int bufferIndex = (int) (maskedS2CellId >>> numBitsRightOfMap);
            values[i] = Double.isNaN(values[i]) ? 0 : values[i];
            values[i] += ((int) byteBuffer.get(bufferIndex)) & 0xFF;
        }
    }

    private static void mergeByteJpegValues(@NonNull MapParamsProto params,
            long @NonNull [] s2CellIds,
            S2TileProto @NonNull [] tiles,
            int tileIndex, double @NonNull [] values) throws IOException {
        mergeByteImageValues(params, tiles[tileIndex].getByteJpeg(), s2CellIds, tiles, tileIndex,
                values);
    }

    private static void mergeBytePngValues(@NonNull MapParamsProto params,
            long @NonNull [] s2CellIds,
            S2TileProto @NonNull [] tiles,
            int tileIndex, double @NonNull [] values) throws IOException {
        mergeByteImageValues(params, tiles[tileIndex].getBytePng(), s2CellIds, tiles, tileIndex,
                values);
    }

    @SuppressWarnings("ReferenceEquality")
    private static void mergeByteImageValues(@NonNull MapParamsProto params,
            @NonNull ByteString byteString,
            long @NonNull [] s2CellIds,
            S2TileProto @NonNull [] tiles, int tileIndex, double @NonNull [] values)
            throws IOException {
        if (byteString.isEmpty()) {
            return;
        }
        Bitmap bitmap;
        try (InputStream inputStream = byteString.newInput()) {
            bitmap = BitmapFactory.decodeStream(inputStream);
        }
        if (bitmap == null) {
            return;
        }

        for (int i = tileIndex; i < tiles.length; i++) {
            if (tiles[i] != tiles[tileIndex]) {
                continue;
            }

            values[i] = Double.isNaN(values[i]) ? 0 : values[i];
            values[i] += bitmap.getPixel(getIndexX(params, s2CellIds[i], bitmap.getWidth()),
                    getIndexY(params, s2CellIds[i], bitmap.getHeight())) & 0xFF;
        }
    }

    /** Returns the X index for an S2 cell within an S2 tile image of specified width. */
    private static int getIndexX(@NonNull MapParamsProto params, long s2CellId, int width) {
        return getIndexXOrY(params, S2CellIdUtils.getI(s2CellId), width);
    }

    /** Returns the Y index for an S2 cell within an S2 tile image of specified height. */
    private static int getIndexY(@NonNull MapParamsProto params, long s2CellId, int height) {
        return getIndexXOrY(params, S2CellIdUtils.getJ(s2CellId), height);
    }

    private static int getIndexXOrY(@NonNull MapParamsProto params, int iOrJ, int widthOrHeight) {
        return (iOrJ >> (S2CellIdUtils.MAX_LEVEL - params.getMapS2Level())) % widthOrHeight;
    }

    /**
     * Throws an {@link IllegalArgumentException} if the {@code s2CellIds} has an invalid length or
     * ID.
     */
    private static void validate(@NonNull MapParamsProto params, long @NonNull [] s2CellIds) {
        Preconditions.checkArgument(s2CellIds.length == 4);
        for (long s2CellId : s2CellIds) {
            Preconditions.checkArgument(S2CellIdUtils.getLevel(s2CellId) == params.getMapS2Level());
        }
    }

    /**
     * Returns the geoid heights in meters associated with the map cells identified by
     * {@code s2CellIds}. Throws an {@link IOException} if a geoid height cannot be calculated for
     * an ID.
     */
    public double @NonNull [] readGeoidHeights(@NonNull MapParamsProto params,
            @NonNull Context context, long @NonNull [] s2CellIds) throws IOException {
        validate(params, s2CellIds);
        double[] heightsMeters = new double[s2CellIds.length];
        if (getGeoidHeights(params, mCacheTiles::get, s2CellIds, heightsMeters)) {
            return heightsMeters;
        }

        TileFunction loadedTiles = loadFromCacheAndDisk(params, context, s2CellIds);
        if (getGeoidHeights(params, loadedTiles, s2CellIds, heightsMeters)) {
            return heightsMeters;
        }
        throw new IOException("Unable to calculate geoid heights from raw assets.");
    }

    /**
     * Same as {@link #readGeoidHeights(MapParamsProto, Context, long[])} except that data will not
     * be loaded from raw assets. Returns the heights if present for all IDs; otherwise, returns
     * null.
     */
    public double @Nullable [] readGeoidHeights(@NonNull MapParamsProto params,
            long @NonNull [] s2CellIds) throws IOException {
        validate(params, s2CellIds);
        double[] heightsMeters = new double[s2CellIds.length];
        if (getGeoidHeights(params, mCacheTiles::get, s2CellIds, heightsMeters)) {
            return heightsMeters;
        }
        return null;
    }

    /**
     * Adds to {@code heightsMeters} the geoid heights in meters associated with the map cells
     * identified by {@code s2CellIds}. Returns true if heights are present for all IDs; otherwise,
     * returns false and adds NaNs for absent heights.
     */
    private boolean getGeoidHeights(@NonNull MapParamsProto params,
            @NonNull TileFunction tileFunction, long @NonNull [] s2CellIds,
            double @NonNull [] heightsMeters) throws IOException {
        boolean allFound = getUnitIntervalValues(params, tileFunction, s2CellIds, heightsMeters);
        for (int i = 0; i < heightsMeters.length; i++) {
            // NaNs are properly preserved.
            heightsMeters[i] *= params.getModelAMeters();
            heightsMeters[i] += params.getModelBMeters();
        }
        return allFound;
    }

    private @NonNull TileFunction loadFromCacheAndDisk(@NonNull MapParamsProto params,
            @NonNull Context context, long @NonNull [] s2CellIds) throws IOException {
        int len = s2CellIds.length;

        // Enable batch loading by finding all cache keys upfront.
        long[] cacheKeys = new long[len];
        for (int i = 0; i < len; i++) {
            cacheKeys[i] = getCacheKey(params, s2CellIds[i]);
        }

        // Attempt to load tiles from cache.
        S2TileProto[] loadedTiles = new S2TileProto[len];
        String[] diskTokens = new String[len];
        for (int i = 0; i < len; i++) {
            if (diskTokens[i] != null) {
                continue;
            }
            loadedTiles[i] = mCacheTiles.get(cacheKeys[i]);
            diskTokens[i] = getDiskToken(params, cacheKeys[i]);

            // Batch across common cache key.
            for (int j = i + 1; j < len; j++) {
                if (cacheKeys[j] == cacheKeys[i]) {
                    loadedTiles[j] = loadedTiles[i];
                    diskTokens[j] = diskTokens[i];
                }
            }
        }

        // Attempt to load tiles from disk.
        for (int i = 0; i < len; i++) {
            if (loadedTiles[i] != null) {
                continue;
            }

            TilesEntity entity = getDatabase(context).tilesDao().get(diskTokens[i]);
            if (entity == null) {
                throw new IOException("Unable to read disk tile of disk token: " + diskTokens[i]);
            }
            mergeFromDiskTile(params, entity.tile(), cacheKeys, diskTokens, i, loadedTiles);
        }

        return cacheKey -> {
            for (int i = 0; i < cacheKeys.length; i++) {
                if (cacheKeys[i] == cacheKey) {
                    return loadedTiles[i];
                }
            }
            return null;
        };
    }

    private void mergeFromDiskTile(@NonNull MapParamsProto params, @NonNull S2TileProto diskTile,
            long @NonNull [] cacheKeys, String @NonNull [] diskTokens, int diskTokenIndex,
            S2TileProto @NonNull [] loadedTiles) throws IOException {
        int len = cacheKeys.length;
        int numMapCellsPerCacheTile =
                1 << (2 * (params.getMapS2Level() - params.getCacheTileS2Level()));

        // Reusable arrays.
        long[] s2CellIds = new long[numMapCellsPerCacheTile];
        double[] values = new double[numMapCellsPerCacheTile];
        byte[] bytes = new byte[numMapCellsPerCacheTile];

        // Each cache key identifies a different sub-tile of the same disk tile.
        TileFunction diskTileFunction = cacheKey -> diskTile;
        for (int i = diskTokenIndex; i < len; i++) {
            if (!Objects.equals(diskTokens[i], diskTokens[diskTokenIndex])
                    || loadedTiles[i] != null) {
                continue;
            }

            // Find all map cells within the current cache tile.
            long s2CellId = S2CellIdUtils.getTraversalStart(cacheKeys[i], params.getMapS2Level());
            for (int j = 0; j < numMapCellsPerCacheTile; j++) {
                s2CellIds[j] = s2CellId;
                s2CellId = S2CellIdUtils.getTraversalNext(s2CellId);
            }

            if (!getUnitIntervalValues(params, diskTileFunction, s2CellIds, values)) {
                throw new IOException("Corrupted disk tile of disk token: " + diskTokens[i]);
            }

            for (int j = 0; j < numMapCellsPerCacheTile; j++) {
                bytes[j] = (byte) Math.round(values[j] * 0xFF);
            }
            loadedTiles[i] =
                    S2TileProto.newBuilder().setByteBuffer(ByteString.copyFrom(bytes)).build();

            // Batch across common cache key.
            for (int j = i + 1; j < len; j++) {
                if (cacheKeys[j] == cacheKeys[i]) {
                    loadedTiles[j] = loadedTiles[i];
                }
            }

            // Side load into tile cache.
            mCacheTiles.put(cacheKeys[i], loadedTiles[i]);
        }
    }

    /** Defines a function-like object to retrieve tiles for cache keys. */
    private interface TileFunction {

        @Nullable S2TileProto getTile(long cacheKey);
    }
}
