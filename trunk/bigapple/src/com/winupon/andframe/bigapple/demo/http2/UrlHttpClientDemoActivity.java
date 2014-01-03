/* 
 * @(#)UrlHttpClientDemoActivity.java    Created on 2014-1-2
 * Copyright (c) 2014 ZDSoft Networks, Inc. All rights reserved.
 * $Id$
 */
package com.winupon.andframe.bigapple.demo.http2;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.winupon.andframe.bigapple.R;
import com.winupon.andframe.bigapple.bitmap.CompatibleAsyncTask;
import com.winupon.andframe.bigapple.http2.urlhttpclient.DownloadCallBack;
import com.winupon.andframe.bigapple.http2.urlhttpclient.URLHttpClient;
import com.winupon.andframe.bigapple.ioc.AnActivity;
import com.winupon.andframe.bigapple.ioc.InjectView;
import com.winupon.andframe.bigapple.utils.log.LogUtils;

/**
 * UrlHttpClient测试demo
 * 
 * @author xuan
 * @version $Revision: 1.0 $, $Date: 2014-1-2 下午6:52:41 $
 */
public class UrlHttpClientDemoActivity extends AnActivity {
    @InjectView(R.id.buttonGet)
    private Button buttonGet;

    @InjectView(R.id.buttonPost)
    private Button buttonPost;

    @InjectView(R.id.buttonDownload)
    private Button buttonDownload;

    @InjectView(R.id.textView)
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_http2_main);

        // Get方法测试
        buttonGet.setText("get方法测试");
        buttonGet.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CompatibleAsyncTask<Object, Object, String>() {
                    @Override
                    protected void onPreExecute() {
                        textView.setText("正在努力get中");
                    }

                    @Override
                    protected String doInBackground(Object... params) {
                        URLHttpClient client = new URLHttpClient();
                        String ret = null;
                        try {
                            ret = client.get("http://www.baidu.com");
                        }
                        catch (Exception e) {
                            LogUtils.e("", e);
                            ret = "请求出错啦！";
                        }

                        return ret;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        textView.setText(result);
                    }
                }.execute();
            }
        });

        // post方法测试
        buttonPost.setText("post方法测试");
        buttonPost.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CompatibleAsyncTask<Object, Object, String>() {
                    @Override
                    protected void onPreExecute() {
                        textView.setText("正在努力get中");
                    }

                    @Override
                    protected String doInBackground(Object... params) {
                        URLHttpClient client = new URLHttpClient();
                        String ret = null;
                        try {
                            ret = client.post("http://www.renren.com");
                        }
                        catch (Exception e) {
                            LogUtils.e("", e);
                            ret = "请求出错啦！";
                        }

                        return ret;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        textView.setText(result);
                    }
                }.execute();
            }
        });

        // download方法测试
        buttonDownload.setText("download方法测试");
        buttonDownload.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CompatibleAsyncTask<Object, Long, String>() {
                    @Override
                    protected void onPreExecute() {
                        textView.setText("正在努力download中");
                    }

                    @Override
                    protected String doInBackground(Object... params) {
                        URLHttpClient client = new URLHttpClient();
                        String filePath = null;
                        try {
                            filePath = Environment.getExternalStorageDirectory().getPath() + "/bigappletest/11.jpg";
                            client.download("http://img2.pcpop.com/ArticleImages/0x0/0/118/000118652.jpg", new File(
                                    filePath), new DownloadCallBack() {
                                @Override
                                public void onStart(long count) {
                                }

                                @Override
                                public void onLoading(long count, long current) {
                                    publishProgress(count, current);
                                }

                                @Override
                                public void onEnd() {
                                }
                            });
                        }
                        catch (Exception e) {
                            LogUtils.e("", e);
                            filePath = "请求出错啦！";
                        }

                        return "下载好的文件放在：" + filePath;
                    }

                    @Override
                    protected void onProgressUpdate(Long... values) {
                        textView.setText("下载进度如下" + values[1] + "/" + values[0]);
                    };

                    @Override
                    protected void onPostExecute(String result) {
                        textView.setText(result);
                    }
                }.execute();
            }
        });
    }

}