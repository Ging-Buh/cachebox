package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.View.MeasureSpec;

public class FieldNoteViewItem extends View {
	private FieldNoteEntry fieldnote;
    private int mAscent;
    private int width;
    private int height;
    private int rightBorder;
    private final int CornerSize =20;
    private boolean BackColorChanger = false;
       
    /// <summary>
    /// Höhe einer Zeile auf dem Zielgerät
    /// </summary>
    private int lineHeight = 50;
    private int imgSize = 50;

    public FieldNoteViewItem(Context context, FieldNoteEntry fieldnote, Boolean BackColorId) {
		super(context);

        this.fieldnote = fieldnote;
        BackColorChanger = BackColorId;
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       
        
        
        // Berechne Höhe so das 7 Einträge in die Liste passen
        this.height = (int) FieldNotesView.windowH / 7;
        this.imgSize = (int) (this.height / 1.2);
        this.lineHeight = (int) this.height / 3;
        this.rightBorder =(int) (this.height * 1.5);
        
        setMeasuredDimension(measureWidth(widthMeasureSpec),this.height);
              //  measureHeight(heightMeasureSpec));
	}

    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) Global.Paints.Day.ListBackground.measureText(fieldnote.CacheName) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        width = specSize;
        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) Global.Paints.Day.ListBackground.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + Global.Paints.Day.ListBackground.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
     
        return result;
    }

    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean selected = false;
        if (this.fieldnote == FieldNotesView.aktFieldNote)
        	selected = true;
        
        int BackgroundColor;
        if (BackColorChanger)
        {
        	BackgroundColor = (selected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (selected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        Paint BackPaint = new Paint();
        BackPaint.setAntiAlias(true);
       
        final Rect rect = new Rect(7, 7, width-7, height-7);
        final RectF rectF = new RectF(rect);
        
        final Rect outerRect = new Rect(5, 5, width-5, height-5);
        final RectF OuterRectF = new RectF(outerRect);
        BackPaint.setColor(BackgroundColor);
        canvas.drawRoundRect( rectF,CornerSize-2,CornerSize-2, BackPaint);
        canvas.drawText(fieldnote.CacheName, 70, 20, new Paint());
        canvas.drawText(fieldnote.comment, 70, 35, new Paint());
        canvas.drawText(fieldnote.gcCode, 70, 50, new Paint());
        canvas.drawText("#" + fieldnote.foundNumber, 70, 65, new Paint());
        canvas.drawText(fieldnote.timestamp.toLocaleString(), 200, 70, new Paint());
        canvas.drawText("Id:" + fieldnote.Id, 250, 35, new Paint());
        canvas.drawText("CacheId:" + fieldnote.CacheId, 250, 50, new Paint());

        if (fieldnote.typeIcon >= 0) 
        	Global.PutImageTargetHeight(canvas, Global.LogIcons[fieldnote.typeIcon], 10, 20, 50);
        
        
        
    }
}
