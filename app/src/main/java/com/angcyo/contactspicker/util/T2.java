package com.angcyo.contactspicker.util;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angcyo.contactspicker.R;

import java.lang.reflect.Field;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/01/14 09:53
 * 修改人员：Robi
 * 修改时间：2017/01/14 09:53
 * 修改备注：
 * Version: 1.0.0
 */
public class T2 {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_OK = 1;
    public static final int TYPE_INFO = 2;
    public static final int TYPE_ERROR = 3;
    private static Toast toast;

    public static void show(Context context, CharSequence charSequence) {
        View layout;
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
            initToast(toast);
            layout = LayoutInflater.from(context).inflate(R.layout.base_toast, null);
            ((TextView) layout.findViewById(R.id.base_toast_text_view)).setText(charSequence);
            toast.setView(layout);
            toast.setGravity(Gravity.TOP, 0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toast.getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        } else {
            layout = toast.getView();
        }

        TextView titleView = find(layout, R.id.base_toast_text_view);
        ImageView imageView = find(layout, R.id.base_toast_image_view);

        titleView.setText(charSequence);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.base_ok);
        toast.show();
    }

    private static <T> T find(View view, int id) {
        return (T) view.findViewById(id);
    }

    private static void initToast(Toast toast) {
        try {
            Field mTN = toast.getClass().getDeclaredField("mTN");
            mTN.setAccessible(true);
            Object mTNObj = mTN.get(toast);

            Field mParams = mTNObj.getClass().getDeclaredField("mParams");
            mParams.setAccessible(true);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams.get(mTNObj);
            params.width = -1;
            params.height = -2;// (int) dpToPx(context, T_HEIGHT);
//            params.gravity = Gravity.TOP;
            params.windowAnimations = R.style.BaseToastAnimation;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
