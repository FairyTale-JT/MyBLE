package com.example.cdha.utils;

import android.content.Context;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class ToastUtils {
    /**
     *错误Toast：
     */
    public static void showError(Context context,String text) {
        Toasty.error(context,text, Toast.LENGTH_SHORT,true).show();
    }

    /**
     *成功Toast：
     */
    public static void showSuccess(Context context,String text) {
        Toasty.success(context,text,Toast.LENGTH_SHORT,true).show();
    }

    /**
     *信息Toast：
     */
    public static void showInfo(Context context,String text) {
        Toasty.info(context,text,Toast.LENGTH_SHORT,true).show();
    }

    /**
     *警告Toast：
     */
    public static void showWarning(Context context,String text) {
        Toasty.warning(context,text,Toast.LENGTH_SHORT,true).show();
    }
}
