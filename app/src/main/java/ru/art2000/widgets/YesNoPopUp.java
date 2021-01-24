package ru.art2000.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.art2000.publicidconverterev.R;

public class YesNoPopUp extends RelativeLayout {

    public TextView messageTV;
    public NiceButton confirmButton;
    public NiceButton cancelButton;
    public RelativeLayout containerRL;
    private Drawable confirmSrc;
    private Drawable cancelSrc;
    public final int yesOnly = 1;
    public final int yes_no = 0;
    public final int noOnly = -1;
    private int strokeColor = Color.parseColor("#757575");
    private int strokeWidth = dip2px(1);
    private int messageTextColor = Color.parseColor("#ff000000");
    private int backColor = Color.parseColor("#fffafafa");
    private int type;
    private String messageText;
    private String defaultMessageText = "Do Androids Dream of Electric Sheep?";

    public YesNoPopUp(Context context) {
        super(context);
        createLayout();
        setupElements();
    }

    public YesNoPopUp(Context context, AttributeSet attrs) {
        super(context, attrs);
        createLayout();
        loadStyleAttrs(attrs);
        setupElements();
    }

    public void createLayout(){
        containerRL = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams contLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        contLayoutParams.setMargins(dip2px(10), dip2px(10), dip2px(10), dip2px(10));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            containerRL.setElevation(dip2px(2));
        cancelButton = new NiceButton(getContext());
        confirmButton = new NiceButton(getContext());
        messageTV = new TextView(getContext());
        RelativeLayout.LayoutParams gravityLeft = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        gravityLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        RelativeLayout.LayoutParams gravityRight = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        gravityRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        containerRL.addView(cancelButton, gravityLeft);
        containerRL.addView(confirmButton, gravityRight);
        RelativeLayout.LayoutParams gravityCenter = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        gravityCenter.addRule(RelativeLayout.CENTER_IN_PARENT);
        containerRL.addView(messageTV, gravityCenter);
        addView(containerRL, contLayoutParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupElements(){
        GradientDrawable back = new GradientDrawable();
        back.setShape(GradientDrawable.RECTANGLE);
        back.setColor(backColor);
        setType(type);
        back.setStroke(strokeWidth, strokeColor);
        cancelButton.setupStroke(strokeColor, strokeWidth, NiceButton.width.px);
        confirmButton.setupStroke(strokeColor, strokeWidth, NiceButton.width.px);
        if (cancelSrc != null)
            cancelButton.setImageDrawable(cancelSrc);
        if (confirmSrc != null)
            confirmButton.setImageDrawable(confirmSrc);
        confirmButton.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        back.setCornerRadius(confirmButton.getMeasuredHeight()/2);
        messageTV.setText(messageText);
        messageTV.setTextColor(messageTextColor);
        containerRL.setBackgroundDrawable(back);
        containerRL.setOnTouchListener((v, event) ->
                true);
    }

    private void loadStyleAttrs(AttributeSet attrs){
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.YesNoPopUp,
                0, 0);
        try {
            messageText = defaultMessageText;
            String tmp = typedArray.getString(R.styleable.YesNoPopUp_popUpText);
            if (tmp != null)
                messageText = tmp;
            messageTextColor = typedArray.getColor(R.styleable.YesNoPopUp_popUpTextColor,
                    messageTextColor);
            type = typedArray.getInt(R.styleable.YesNoPopUp_popUpType, 0);
            backColor = typedArray.getColor(R.styleable.YesNoPopUp_popUpBackground, backColor);
            strokeColor = typedArray.getColor(R.styleable.YesNoPopUp_popUpStrokeColor, strokeColor);
            strokeWidth = (int) typedArray.getDimension(R.styleable.YesNoPopUp_popUpStrokeWidth,
                    strokeWidth);
            confirmSrc = typedArray.getDrawable(R.styleable.YesNoPopUp_confirmBtnIcon);
            cancelSrc = typedArray.getDrawable(R.styleable.YesNoPopUp_cancelBtnIcon);
        } finally {
            typedArray.recycle();
        }
    }

    public void setType(int type){
        switch (type){
            case yesOnly:
                cancelButton.setVisibility(GONE);
                confirmButton.setVisibility(VISIBLE);
                break;
            case noOnly:
                cancelButton.setVisibility(VISIBLE);
                confirmButton.setVisibility(GONE);
                break;
            case yes_no:
            default:
                confirmButton.setVisibility(VISIBLE);
                cancelButton.setVisibility(VISIBLE);
                break;
        }
    }

    public void hide(){
        setVisibility(GONE);
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void setConfirmImage(Drawable src){
        confirmButton.setImageDrawable(src);
    }

    public void setCancelImage(Drawable src){
        cancelButton.setImageDrawable(src);
    }

    public void setPopUpText(String text){
        if (!text.isEmpty())
            messageTV.setText(text);
        else
            messageTV.setText(defaultMessageText);
    }

    private int dip2px(float dip){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
    }

    public void setPopUpClickListener(PopUpClickListener listener){
        confirmButton.setOnClickListener(v ->
                listener.onConfirmButtonClick());
        cancelButton.setOnClickListener(v ->
                listener.onCancelButtonClick());
    }

    public static class PopUpClickListener {

        public void onConfirmButtonClick(){

        }

        public void onCancelButtonClick(){

        }

    }
}

