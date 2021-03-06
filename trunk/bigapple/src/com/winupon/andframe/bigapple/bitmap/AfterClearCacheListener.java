/* 
 * @(#)AfterClearCacheListener.java    Created on 2013-9-13
 * Copyright (c) 2013 ZDSoft Networks, Inc. All rights reserved.
 * $Id$
 */
package com.winupon.andframe.bigapple.bitmap;

/**
 * 清空缓存后会被回调的监听
 * 
 * @author xuan
 * @version $Revision: 1.0 $, $Date: 2013-9-13 下午5:19:29 $
 */
public interface AfterClearCacheListener {
    /**
     * 清理缓存后调用，type参考BitmapCacheManagementTask
     * 
     * @param type
     *            执行清理何种缓存的命令
     */
    public void afterClearCache(int type);

}
