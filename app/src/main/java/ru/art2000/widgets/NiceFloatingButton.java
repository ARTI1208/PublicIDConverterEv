package ru.art2000.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ru.art2000.publicidconverterev.R;

public class NiceFloatingButton extends FrameLayout {

    NiceButton button = new NiceButton(getContext());
    private float dp4 = new Utils().dip2px(getContext(), 4);
    private int dip4 = new Utils().dip2px(getContext(), 4);
    private int dip20 = new Utils().dip2px(getContext(), 20);

    public NiceFloatingButton(Context context) {
        super(context);
    }

    public NiceFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewParent parent = getParent();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setBackgroundColor(Color.parseColor("#ff99ee44"));
        button.setImageDrawable(getResources().getDrawable(R.drawable.ic_manager_cancel));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setElevation(dp4);
        }

        button.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        button.setMinimumHeight(button.getMeasuredHeight() + dip4);

        LinearLayout ll = new LinearLayout(getContext());
        LinearLayout.LayoutParams llparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llparams.gravity = Gravity.RIGHT;
        llparams.gravity = Gravity.BOTTOM;
        ll.setGravity(Gravity.RIGHT);

        ll.setPadding(0, 0, dip20, dip20);
        ll.setLayoutParams(llparams);
        ll.addView(button);
        setBackgroundColor(Color.parseColor("#ffee9944"));
        addView(ll);
    }

}
