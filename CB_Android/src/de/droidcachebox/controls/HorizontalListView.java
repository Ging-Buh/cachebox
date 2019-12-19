package de.droidcachebox.controls;

/*
 * HorizontalListView.java v1.5
 *
 *
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

/**
 * nur noch ein Dummy um die Größe und damit das Layout zu erhalten
 *
 * @author Longri
 */
public class HorizontalListView extends AdapterView<ListAdapter> {

    public HorizontalListView(Context context) {
        super(context);
        initView();
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // GL größen müssen neu berechnet werden

    }

    private synchronized void initView() {

    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {

    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {

    }

    @Override
    public ListAdapter getAdapter() {
        return null;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {

    }

    @Override
    public View getSelectedView() {

        return null;
    }

    @Override
    public void setSelection(int position) {

    }

    @Override
    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), DownSlider.getAktQuickButtonHeight());
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

    public void setHeight(int newheight) {

        setMeasuredDimension(this.getWidth(), newheight);
    }

}
