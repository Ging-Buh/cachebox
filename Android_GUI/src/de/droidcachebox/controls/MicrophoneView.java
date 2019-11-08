package de.droidcachebox.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.utils.ActivityUtils;

public final class MicrophoneView extends View {

    Handler handler = new Handler();
    /*
     * Private Member
     */
    private int height;
    private int width;
    private int[] colorArray;
    private int counter = 0;
    private boolean switchOff = false;
    Runnable task = new Runnable() {

        @Override
        public void run() {
            Redraw();
        }
    };

    public MicrophoneView(Context context) {
        super(context);
        iniColorArray();
    }

    public MicrophoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        iniColorArray();
    }

    public MicrophoneView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        iniColorArray();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = measure(widthMeasureSpec);
        this.height = measure(heightMeasureSpec);

        setMeasuredDimension(this.width, this.height);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);

        result = specSize;
        return result;
    }

    private void iniColorArray() {
        colorArray = new int[42];
        colorArray[0] = Color.argb(0, 255, 0, 0);
        for (int i = 10; i < 255; i += 10) {
            colorArray[i / 10] = Color.argb(i, 255, 0, 0);
        }

        for (int i = 25; i < 40; i++) {
            colorArray[i] = Color.argb(255, 255, 0, 0);
        }

        for (int i = 40; i < 60; i += 10) {
            colorArray[36 + (i / 10)] = Color.argb(255 - i, 255, 0, 0);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {

        ActivityUtils.PutImageTargetHeightColor(canvas, Global.Icons[29], 0, 0, this.height, colorArray[counter]);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Log.d("Cachebox", "Size changed to " + w + "x" + h);
    }

    public void SetOn() {
        this.setVisibility(View.VISIBLE);

        handler.postDelayed(task, 10);
        switchOff = false;

    }

    private void Redraw() {
        counter++;
        if (counter > colorArray.length - 1)
            counter = 0;
        this.invalidate();
        if (!switchOff)
            handler.postDelayed(task, 10);
    }

    public void SetOff() {
        this.setVisibility(View.GONE);
        switchOff = true;
    }

}
