package ru.art2000.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ru.art2000.publicidconverterev.R;

public class NiceButton extends ImageButton {

    private int background = Color.parseColor("#fffafafa");
    enum width {
        dp, px
    }
    private GradientDrawable backgroundDrawable;
    private GradientDrawable backgroundDrawablePressed;
    float elev;

    private StateListDrawable backgroundSelector;
    private int strokeColor = Color.parseColor("#757575");
    private int strokeWidth = 0;
    private boolean elevate = false;


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (elevate) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
            params.setMargins(dip2px(6), dip2px(6), dip2px(6), dip2px(6));
            setLayoutParams(params);
        }
    }

    public int dip2px(float dip){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
    }

    public void NiceButtonCreator(){
        setPadding(dip2px(10), dip2px(10), dip2px(10), dip2px(10));
        applyTransformation();
    }

    public NiceButton(Context context){
        super(context);
        NiceButtonCreator();
    }

    public void applyTransformation(){
        if (elevate)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setElevation(dip2px(4));
            }
        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.OVAL);
        backgroundDrawablePressed = new GradientDrawable();
        backgroundDrawablePressed.setShape(GradientDrawable.OVAL);
        backgroundDrawable.setColor(background);
        backgroundDrawablePressed.setColor(getPressedColor(background));
        backgroundDrawable.setStroke(strokeWidth, strokeColor);
        backgroundDrawablePressed.setStroke(strokeWidth, strokeColor);
        backgroundSelector = new StateListDrawable();
        backgroundSelector.addState(
                new int[]{android.R.attr.state_pressed},
                backgroundDrawablePressed
        );
        backgroundSelector.addState(
                new int[]{-android.R.attr.state_pressed},
                backgroundDrawable
        );
        setBackgroundDrawable(backgroundSelector);
    }

    public void setStrokeWidth(float dipStrokeWidth){
        strokeWidth = dip2px(dipStrokeWidth);
        applyTransformation();
    }

    public void setStrokeWidthPx(int pxStrokeWidth){
        strokeWidth = pxStrokeWidth;
        applyTransformation();
    }

    public void setStrokeColor(int strokeColor){
        this.strokeColor = strokeColor;
        applyTransformation();
    }

    public void setBackgroundColor(int backgroundColor){
        background = backgroundColor;
        applyTransformation();
    }

    public void setupStroke(int strokeColor, float strokeWidth, width widthType){
        setStrokeColor(strokeColor);
        switch (widthType){
            case dp:
                setStrokeWidth(strokeWidth);
                break;
            case px:
                setStrokeWidthPx((int) strokeWidth);
                break;
            default:
                Log.e("Wrong width type", "dp (0) or px (1) required");
                break;
        }
    }

    public NiceButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.NiceButton,
                0, 0);

        try {
            background = typedArray.getColor(R.styleable.NiceButton_backColor, background);
            strokeColor = typedArray.getColor(R.styleable.NiceButton_strokeColor, strokeColor);
            strokeWidth = (int) typedArray.getDimension(R.styleable.NiceButton_strokeWidth, strokeWidth);
            elevate = typedArray.getBoolean(R.styleable.NiceButton_elevate, elevate);
        } finally {
            typedArray.recycle();
        }

        NiceButtonCreator();

    }

    public int getPressedColor(int normalColor){
        int tmp;
        int red = Color.red(normalColor);
        int green = Color.green(normalColor);
        int blue = Color.blue(normalColor);
        if (red > 20)
            red -= 20;
        if (green > 20)
            green -= 20;
        if (blue > 20)
            blue -= 20;
        tmp = Color.rgb(red, green, blue);
        return tmp;
    }

}
