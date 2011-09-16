package de.droidcachebox.Views;

import CB_Core.GlobalCore;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Ui.Sizes;

import CB_Core.Types.Cache;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class CacheListViewItem extends View {
    private Cache cache;
    
    private boolean BackColorChanger = false;

    
    /**
     * True wenn die Liste schnell gescrollt wird.
     * Wird gesetzt vom CachelistView.OnScrollListener
     */
    public static boolean isFastScrolling=false;
    
	public CacheListViewItem(Context context, Cache cache, Boolean BackColorId) 
	{
		super(context);
        this.cache = cache;
        BackColorChanger = BackColorId;
    }

	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
        setMeasuredDimension(Sizes.getCacheListItemSize().width, Sizes.getCacheListItemSize().height);
	}
    
  static double fakeBearing =0;

    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        
        Boolean GlobalSelected = cache == GlobalCore.SelectedCache();
        int BackgroundColor;
        if (BackColorChanger)
        {
        	BackgroundColor = (GlobalSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (GlobalSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        CacheDraw.DrawInfo(cache,canvas, Sizes.getCacheListItemRec(), BackgroundColor, DrawStyle.all , isFastScrolling);
        
        
    }

}
