package com.example.util

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object ExoPlayerCacheManager {
    // 150 MB local segment cache limit for 2G / low-data offline optimization
    private const val MAX_CACHE_SIZE = 150 * 1024 * 1024L
    private var simpleCache: SimpleCache? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null

    @Synchronized
    fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "exoplayer_segments").apply {
                if (!exists()) mkdirs()
            }
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
            val dbProvider = StandaloneDatabaseProvider(context.applicationContext)
            databaseProvider = dbProvider
            simpleCache = SimpleCache(cacheDir, evictor, dbProvider)
        }
        return simpleCache!!
    }

    fun buildCacheDataSourceFactory(
        context: Context,
        httpDataSourceFactory: DefaultHttpDataSource.Factory
    ): DataSource.Factory {
        val cache = getSimpleCache(context)
        val upstreamFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getCacheSizeBytes(context: Context): Long {
        return try {
            getSimpleCache(context).cacheSpace
        } catch (_: Exception) {
            0L
        }
    }

    fun clearCache(context: Context) {
        try {
            val cache = getSimpleCache(context)
            val keys = cache.keys
            for (key in keys) {
                cache.removeResource(key)
            }
        } catch (_: Exception) {}
    }
}
