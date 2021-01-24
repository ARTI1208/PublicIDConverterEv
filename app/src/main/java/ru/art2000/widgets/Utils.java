package ru.art2000.widgets;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils {
    public int dip2px(Context ctx, float dip){
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
    }
}
