package com.angcyo.contactspicker.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.angcyo.contactspicker.R;

/**
 * 用来控制状态栏的padding
 * Created by angcyo on 2016-11-05.
 */

public class ToastLayout extends RelativeLayout {

    public ToastLayout(Context context) {
        super(context);
    }

    public ToastLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToastLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ToastLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            setClipToPadding(false);
            setClipChildren(false);
            setPadding(getPaddingLeft(),
                    statusBarHeight,
                    getPaddingRight(), getPaddingBottom());
            int height = statusBarHeight +
                    getResources().getDimensionPixelSize(R.dimen.action_bar_height);
            setMinimumHeight(height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();//MeasureSpec.getSize(heightMeasureSpec);

        int shadowHeight = getResources().getDimensionPixelSize(R.dimen.base_toast_shadow_height);
        setMeasuredDimension(width, height + shadowHeight);
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int heightSize = getMeasuredHeight();
//        int height;
//
//        Context context = getContext();
////        if (context instanceof Activity) {
////            if (ResUtil.isLayoutFullscreen((Activity) context)) {
//
////            }
////        }
////        setMeasuredDimension(widthMeasureSpec, height);
//    }
}
