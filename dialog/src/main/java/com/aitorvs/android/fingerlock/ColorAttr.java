package com.aitorvs.android.fingerlock;
/*
 * Copyright (C) 23/05/16 aitorvs
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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;

class ColorAttr{

    public static int getColor(Context context, @AttrRes int attr) {
        return getColor(context, attr, 0);
    }

    public static int getColor(Context context, @AttrRes int attr, int colorDefault) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return typedArray.getColor(0, colorDefault);
        } finally {
            typedArray.recycle();
        }
    }
}