package com.winupon.andframe.bigapple.bitmap.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.winupon.andframe.bigapple.bitmap.BitmapDisplayConfig;

/**
 * 默认实现，加载完图片后的回调显示接口
 * 
 * @author xuan
 * @version $Revision: 1.0 $, $Date: 2013-8-1 下午6:16:04 $
 */
public class SimpleImageLoadCallBack implements ImageLoadCallBack {
    @Override
    public void loadCompleted(ImageView imageView, Bitmap bitmap, BitmapDisplayConfig config) {
        Animation animation = config.getAnimation();
        if (animation == null) {
            // fadeInDisplay(imageView, bitmap);
            imageView.setImageBitmap(bitmap);// 默认就不加动画了
        }
        else {
            animationDisplay(imageView, bitmap, animation);
        }
    }

    @Override
    public void loadFailed(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    // 默认效果渐入展示
    private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
        final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                new ColorDrawable(android.R.color.transparent), new BitmapDrawable(imageView.getResources(), bitmap) });
        imageView.setImageDrawable(td);
        td.startTransition(300);
    }

    // 播放自定义动画展示
    private void animationDisplay(ImageView imageView, Bitmap bitmap, Animation animation) {
        animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());// 设置播放开始时间
        imageView.setImageBitmap(bitmap);
        imageView.startAnimation(animation);
    }

}
