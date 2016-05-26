package com.aitorvs.android.fingerlock;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;

/**
 * @author Aidan Follestad (afollestad)
 */
class Utils{

    public static int resolveColor(Context context, @AttrRes int attr) {
        return resolveColor(context, attr, 0);
    }

    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }
}