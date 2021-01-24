package ru.art2000.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import ru.art2000.publicidconverterev.R;

public class Floatin extends NiceButton {

    ViewGroup parent;
    int leftBtnID;
    int rightBtnID;
    int defLeftMargin = 0;
    int defTopMargin = 0;
    int defRightMargin = dip2px(18);
    int defBottomMargin = dip2px(28);
    int leftViewMargin;
    int rightViewMargin = dip2px(30);


    public Floatin(Context context) {
        super(context);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {

        super.onVisibilityChanged(changedView, visibility);
        parent = (ViewGroup) getParent();
//        if (parent.getChildAt(parent.getChildCount() - 1).getId() == getId()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
            if (parent == null)
                return;
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChildAt(i).getVisibility() == VISIBLE) {
                    leftBtnID = parent.getChildAt(i).getId();
                    Log.d("left found", String.valueOf(i));
                    break;
                }
            }
            for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                if (parent.getChildAt(i).getVisibility() == VISIBLE) {
                    rightBtnID = parent.getChildAt(i).getId();
                    Log.d("right found", String.valueOf(i));
                    break;
                }
            }

            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChildAt(i).getId() == leftBtnID) {
                    params.setMargins(leftViewMargin, defTopMargin, defRightMargin, defBottomMargin);
//                    findViewById(leftBtnID).setBackgroundColor(Color.parseColor("#ff009944"));
                    Log.d("left", String.valueOf(i));
                } else if (parent.getChildAt(i).getId() == rightBtnID) {
                    params.setMargins(defLeftMargin, defTopMargin, rightViewMargin, defBottomMargin);
//                    findViewById(rightBtnID).setBackgroundColor(Color.parseColor("#ffee9944"));
                    Log.d("right", String.valueOf(i));
                } else {
                    params.setMargins(defLeftMargin, defTopMargin, defRightMargin, defBottomMargin);
//                    findViewById(parent.getChildAt(i).getId()).setBackgroundColor(Color.parseColor("#ff99ee44"));
                    Log.d("other", String.valueOf(i));
                }
//            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public Floatin(Context context, AttributeSet attrs) {
        super(context, attrs);
        leftViewMargin = dip2px(2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(dip2px(4));
            leftViewMargin = (int) (getElevation() + dip2px(2));
        }
        bringToFront();
    }
}
