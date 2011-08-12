package de.droidcachebox.Views.FilterSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import CB_Core.Config;
import CB_Core.Types.GpxFilename;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.FilterSettings.CategorieListView.CategorieEntry;
import de.droidcachebox.Views.FilterSettings.FilterSetListView;
import de.droidcachebox.Views.FilterSettings.FilterSetListView.FilterSetEntry;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.View;

public class CategorieListViewItem extends View {
	public CategorieEntry categorieEntry;
   
    private static int width;
    private static int height = 0;
    private Context mContext;
    private boolean BackColorChanger = false;
    private StaticLayout layoutEntryName;
    private StaticLayout layoutEntryDate;
    private StaticLayout layoutEntryCount;
    private Resources mRes;
    private ArrayList<CategorieListViewItem> mChildList = new ArrayList<CategorieListViewItem>();
    
    private static TextPaint textPaint;
  
    
    public CategorieListViewItem(Context context, CategorieEntry fne, Boolean BackColorId) {
		super(context);
		mContext=context;
		mRes = mContext.getResources();
        this.categorieEntry = fne;
        BackColorChanger = BackColorId;
        
        if(textPaint==null)
        {
        	textPaint = new TextPaint();
        	textPaint.setTextSize(Sizes.getScaledFontSize_normal());
        	textPaint.setColor(Global.getColor(R.attr.TextColor));
        	textPaint.setAntiAlias(true);
        }
     
	}

    public CategorieEntry getCategorieEntry(){return categorieEntry;}
    
    public CategorieListViewItem addChild(CategorieListViewItem item)
	{
		mChildList.add(item);
		return item;
	}
    
    public void toggleChildeViewState()
    {
    	if (mChildList!=null && mChildList.size()>0)
    	{
    		int newState = (mChildList.get(0).getVisibility()== View.VISIBLE)? View.GONE : View.VISIBLE;
    		
    		for(CategorieListViewItem tmp : mChildList)
    		{
    			tmp.setVisibility(newState);
    		}
    	}
    	
    }
    
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       
		
		
		 width = PresetListView.windowW;
		
		 height = Sizes.getIconSize() + Sizes.getCornerSize()*4; 
		 
 setMeasuredDimension(width, height);
	            
	}


//Draw Methods
    
    // static Member
   
    private static final SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    // private Member
    int left;
    int top ;
    int BackgroundColor;
    
    private static Rect lPinBounds;
    private static Rect rBounds;
    private static Rect rChkBounds;
    private static int halfSize=0;
    
    @Override
    protected void onDraw(Canvas canvas) 
    {
        
        //initial
    	left = Sizes.getCornerSize();
        top = Sizes.getCornerSize();
        
        if(rBounds == null || rChkBounds == null || lPinBounds==null)
    	{
    		rBounds = new Rect(width-height-7, 7, width-7, height-7);// = right Button bounds
    		halfSize= rBounds.width()/4;
    		int corrRecSize = (rBounds.width()-rBounds.height())/2;
    		rChkBounds = new Rect(rBounds.left + halfSize,rBounds.top + halfSize-corrRecSize, rBounds.right - halfSize, rBounds.bottom - halfSize +corrRecSize );
    		rChkBounds.offset(0, halfSize-Sizes.getCornerSize());
    		lPinBounds = new Rect(rChkBounds);
    		lPinBounds.offset(-(width - (halfSize *2) - rChkBounds.width()), 0);
    	}
       
        
        if(layoutEntryName==null)
        {        	
        	int innerWidth = (width - (Sizes.getIconAddCorner()+ rChkBounds.width()))+halfSize;
        	int innerWidthName= innerWidth-rBounds.width(); 
        	GpxFilename file = categorieEntry.getFile();
        	
        	String Name="";
        	String Date="";
        	String Count="";
        	int Collaps=0;
        	if(file!= null)
        	{
        		Name=file.GpxFileName;
        		Date=postFormater.format(file.Imported);
        		Count=String.valueOf(file.CacheCount);
        	}
        	else 
        	{
        		Name=categorieEntry.getCatName();
        		Date=postFormater.format(categorieEntry.getCat().LastImported());
        		Count=String.valueOf(categorieEntry.getCat().CacheCount());
        		Collaps=Sizes.getCornerSize();
        	}
        	innerWidth+=Collaps;
        	innerWidthName+=Collaps;
        	Count+=" Caches";
        	
            layoutEntryName = new StaticLayout(Name, textPaint, innerWidthName, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            layoutEntryDate = new StaticLayout(Date, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            layoutEntryCount = new StaticLayout(Count, textPaint, innerWidth, Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, false);
        }
		
        textPaint.setColor(Global.getColor(R.attr.TextColor));
       
        
        boolean selected = false;
        if (this.categorieEntry == CategorieListView.aktCategorieEntry)
        	selected = true;
        
       
		
        if (BackColorChanger)
        {
        	BackgroundColor = (selected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (selected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        if(this.categorieEntry.getItemType()!= FilterSetListView.COLLABSE_BUTTON_ITEM)
        {
        ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(5, 5, width-5, height-5), 2, 
     		   Global.getColor(R.attr.ListSeparator), BackgroundColor, 
     		  Sizes.getCornerSize());
        }
        
        switch (this.categorieEntry.getItemType())
        {
        case FilterSetListView.COLLABSE_BUTTON_ITEM:drawCollabseButtonItem(canvas);break;
        case FilterSetListView.CHECK_ITEM:drawChkItem(canvas);break;
        case FilterSetListView.THREE_STATE_ITEM:drawThreeStateItem(canvas);break;
        
        }
       //draw Name 
        ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left, top);
        
      //draw Count
        ActivityUtils.drawStaticLayout(canvas, layoutEntryCount, left, top);
          
        //draw Import Date
        top += 52;
        ActivityUtils.drawStaticLayout(canvas, layoutEntryDate, left, top);
        
        
        
    }
    
    
    private static Drawable btnBack;
    
    private void drawCollabseButtonItem(Canvas canvas)
    {
    	if(btnBack==null)
    	{
    		boolean n = Config.GetBool("nightMode");
    		btnBack = mRes.getDrawable(n? R.drawable.day_btn_default_normal : R.drawable.night_btn_default_normal);
    		
    		
    		Rect bounds = new Rect(3, 7, width-3, height-7);
    		btnBack.setBounds(bounds);
    		
    	}
    	
    	left+=70;
    	
    	btnBack.draw(canvas);
    	ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, 
	     		   Global.getColor(R.attr.ListSeparator), Color.TRANSPARENT, 
	     		  Sizes.getCornerSize());
    	
    	int ChkState = this.categorieEntry.getCat().getChek();
    	
    	if (ChkState==1)ActivityUtils.drawIconBounds(canvas,Global.Icons[27],rChkBounds);
    	if (ChkState==-1)ActivityUtils.drawIconBounds(canvas,Global.Icons[39],rChkBounds);
    	
    	drawPin(canvas);
    }
    
    private void drawPin(Canvas canvas) 
    {
    	if(this.getCategorieEntry().getCat().pinned)
    	{
    		ActivityUtils.drawIconBounds(canvas,Global.Icons[37],lPinBounds);
    	}
    	else
    	{
    		ActivityUtils.drawIconBounds(canvas,Global.Icons[38],lPinBounds);
    	}
    			
	}

	private void drawChkItem(Canvas canvas)
    {
    	drawIcon(canvas);
    	drawRightChkBox(canvas);
    	if(this.categorieEntry.getState()==1)
    	{
    		ActivityUtils.drawIconBounds(canvas,Global.Icons[27],rChkBounds);
    	}
    	
    }
    
    private void drawThreeStateItem(Canvas canvas)
    {
    	drawIcon(canvas);
    	drawRightChkBox(canvas);
    	if(this.categorieEntry.getState()==1)
    	{
    		ActivityUtils.drawIconBounds(canvas,Global.Icons[27],rChkBounds);
    	}
    	else if(this.categorieEntry.getState()==-1)
    	{
    		ActivityUtils.drawIconBounds(canvas,Global.Icons[28],rChkBounds);
    	}
    }
    
    

    
    
    
    private void drawIcon(Canvas canvas)
    {
    	if(categorieEntry.getIcon()!=null)
    	ActivityUtils.PutImageTargetHeight(canvas, categorieEntry.getIcon(), left , top , Sizes.getIconSize());
    	left += Sizes.getIconAddCorner();

    	
    }
    
    private void drawRightChkBox(Canvas canvas)
    {
    	
    	ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, 
	     		   Global.getColor(R.attr.ListSeparator), BackgroundColor, 
	     		  Sizes.getCornerSize());
    }

	public void plusClick() 
	{
		this.categorieEntry.plusClick();
		
	}
	public void minusClick() 
	{
		this.categorieEntry.minusClick();
	}
	public void stateClick() 
	{
		this.categorieEntry.stateClick();
	}

	public void setValue(int value) 
	{
		
		this.categorieEntry.setState(value);
		
	}
	
	public void setValue(float value) 
	{
		this.categorieEntry.setState(value);
		
	}

	public int getChecked() 
	{
		return categorieEntry.getState();
	}

	public float getValue() 
	{
		return (float) categorieEntry.getNumState();
	}

	public CategorieListViewItem getChild(int i) 
	{
		return mChildList.get(i);
	}

	public void setValue(boolean b) 
	{
		this.categorieEntry.setState(b? 1:0);
	}

	public int getChildLength() 
	{
		return mChildList.size();
	}

	public boolean getBoolean() 
	{
		if(categorieEntry.getState()==0)
			return false;
		
		return true;
	}
    
}
