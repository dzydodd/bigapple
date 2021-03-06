/* 
 * @(#)BitmapCacheManager.java    Created on 2013-12-31
 * Copyright (c) 2013 ZDSoft Networks, Inc. All rights reserved.
 * $Id$
 */
package com.winupon.andframe.bigapple.bitmap.core;

import android.os.AsyncTask;

import com.winupon.andframe.bigapple.bitmap.AfterClearCacheListener;
import com.winupon.andframe.bigapple.utils.log.LogUtils;

/**
 * 磁盘缓存和内存缓存的管理器，只要支持实现了缓存的清理操作
 * 
 * @author xuan
 * @version $Revision: 1.0 $, $Date: 2013-12-31 上午9:21:47 $
 */
public class BitmapCacheManager {
    private final BitmapCache bitmapCache;

    private BitmapCacheManager(BitmapCache bitmapCache) {
        this.bitmapCache = bitmapCache;
    }

    /**
     * 缓存管理器是多例状态
     * 
     * @param bitmapCache
     * @return
     */
    public static BitmapCacheManager getInstance(BitmapCache bitmapCache) {
        if (null == bitmapCache) {
            throw new NullPointerException("bitmapCache == null");
        }

        return new BitmapCacheManager(bitmapCache);
    }

    /**
     * 异步刷新缓存类
     * 
     * @author xuan
     * @version $Revision: 1.0 $, $Date: 2014-7-10 下午8:01:16 $
     */
    private class BitmapCacheManagementTask extends AsyncTask<Object, Void, Integer> {
        private AfterClearCacheListener afterClearCacheListener;// 清理缓存后回调

        public static final int MESSAGE_INIT_MEMORY_CACHE = 0;
        public static final int MESSAGE_INIT_DISK_CACHE = 1;
        public static final int MESSAGE_FLUSH = 2;
        public static final int MESSAGE_CLOSE = 3;
        public static final int MESSAGE_CLEAR = 4;
        public static final int MESSAGE_CLEAR_MEMORY = 5;
        public static final int MESSAGE_CLEAR_DISK = 6;
        public static final int MESSAGE_CLEAR_BY_KEY = 7;
        public static final int MESSAGE_CLEAR_MEMORY_BY_KEY = 8;
        public static final int MESSAGE_CLEAR_DISK_BY_KEY = 9;

        @Override
        protected Integer doInBackground(Object... params) {
            Integer type = (Integer) params[0];
            if (null != params[2]) {
                afterClearCacheListener = (AfterClearCacheListener) params[2];
            }

            try {
                switch (type) {
                case MESSAGE_INIT_MEMORY_CACHE:
                    bitmapCache.initMemoryCache();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    bitmapCache.initDiskCache();
                    break;
                case MESSAGE_FLUSH:
                    bitmapCache.clearMemoryCache();
                    bitmapCache.flush();
                    break;
                case MESSAGE_CLOSE:
                    bitmapCache.clearMemoryCache();
                    bitmapCache.close();
                case MESSAGE_CLEAR:
                    bitmapCache.clearCache();
                    break;
                case MESSAGE_CLEAR_MEMORY:
                    bitmapCache.clearMemoryCache();
                    break;
                case MESSAGE_CLEAR_DISK:
                    bitmapCache.clearDiskCache();
                    break;
                case MESSAGE_CLEAR_BY_KEY:
                    bitmapCache.clearCache(String.valueOf(params[1]));
                    break;
                case MESSAGE_CLEAR_MEMORY_BY_KEY:
                    bitmapCache.clearMemoryCache(String.valueOf(params[1]));
                    break;
                case MESSAGE_CLEAR_DISK_BY_KEY:
                    bitmapCache.clearDiskCache(String.valueOf(params[1]));
                    break;
                }
            }
            catch (Exception e) {
                LogUtils.e("", e);
            }

            return type;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (null != afterClearCacheListener) {
                afterClearCacheListener.afterClearCache(result);
            }
        }
    }

    /**
     * 初始化内存缓存
     */
    public void initMemoryCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_MEMORY_CACHE, null, null);
    }

    /**
     * 初始化磁盘缓存
     */
    public void initDiskCache() {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_INIT_DISK_CACHE, null, null);
    }

    /**
     * 清理内存缓存和磁盘缓存
     * 
     * @param listener
     *            清理之后回调
     */
    public void clearCache(AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR, null, listener);
    }

    /**
     * 清理内存缓存
     * 
     * @param listener
     *            清理之后回调
     */
    public void clearMemoryCache(AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY, null, listener);
    }

    /**
     * 清理磁盘缓存
     * 
     * @param listener
     *            清理之后回调
     */
    public void clearDiskCache(AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK, null, listener);
    }

    /**
     * 清理指定内存缓存和磁盘缓存
     * 
     * @param uri
     *            图片地址
     * @param listener
     *            清理之后回调
     */
    public void clearCache(String uri, AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_BY_KEY, uri, listener);
    }

    /**
     * 清理内存缓存
     * 
     * @param uri
     *            图片地址
     * @param listener
     *            清理之后回调
     */
    public void clearMemoryCache(String uri, AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_MEMORY_BY_KEY, uri, listener);
    }

    /**
     * 清理磁盘缓存
     * 
     * @param uri
     *            图片地址
     * @param listener
     *            清理之后回调
     */
    public void clearDiskCache(String uri, AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLEAR_DISK_BY_KEY, uri, listener);
    }

    /**
     * flush内存缓存和磁盘缓存
     * 
     * @param listener
     *            清理之后回调
     */
    public void flushCache(AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_FLUSH, null, listener);
    }

    /**
     * 关闭内存缓存和磁盘缓存，清理后缓存不可使用，需要重新初始化
     * 
     * @param listener
     */
    public void closeCache(AfterClearCacheListener listener) {
        new BitmapCacheManagementTask().execute(BitmapCacheManagementTask.MESSAGE_CLOSE, null, listener);
    }

}
