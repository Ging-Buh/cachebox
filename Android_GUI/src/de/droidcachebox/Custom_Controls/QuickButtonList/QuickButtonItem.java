/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.droidcachebox.Custom_Controls.QuickButtonList;

import CB_Core.GlobalCore;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Enums.Actions;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;

/**
 * Stellt ein Item der Quick Button List dar
 * @author Longri
 *
 */
public class QuickButtonItem extends View 
{
	private Actions mAction;
	private Drawable mButtonIcon;
	private String mActionDesc;
	
	/**
	 * Constructor
	 * @param Action Action Enum
	 * @param Icon Action icon
	 * @param Desc Action Beschreibung
	 */
	public QuickButtonItem(Context context, Actions Action, Drawable Icon,String Desc)
	{
		super(context);
		mAction=Action;
		mButtonIcon=Icon;
		mActionDesc=Desc;
	}
	
	public QuickButtonItem(Context context, Actions Action,int Height) 
	{
		super(context);
		mAction=Action;
		mButtonIcon=Actions.getDrawable(Action);
		mActionDesc=Actions.getName(Action);
		height = width = Height; 
	      
        setMeasuredDimension(this.width,this.height);
		
	}

	/**
	 * Gibt die Action dieses Items wieder
	 * @return Actions
	 */
	public Actions getAction(){return mAction;}
	
	/**
	 * Gibt das Icon dieses Items wieder
	 * @return Drawable
	 */
	public Drawable getIcon(){return mButtonIcon;}
	
	/**
	 * Gibt die Beschreibung dieses Items wieder
	 * @return String
	 */
	public String getDesc(){return mActionDesc;}
	
	
	private int width;
	private int height;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		width = Sizes.getButtonWidth(); 
		height = Sizes.getButtonHeight();
        setMeasuredDimension(this.width, this.height);
	}

 
	
	  @Override
	    protected void onDraw(Canvas canvas) 
	  {
	       // Draw Button Mask 
	       ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[1], 1, 1, height-2);
	       
	       //draw icon or name
	       
	       
	       if(this.mAction.ordinal()== Actions.AutoResort.ordinal())
	       {// Wenn die Action ID = AutoResort ist, muss erst der Zustand On/Off abgefragt werden
	    	   if(Global.autoResort)
	    	   {
	    		   ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[15], 14, 12, height-24);
	    	   }
	    	   else
	    	   {
	    		   ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[16], 14, 12, height-24);
	    	   }
	       }
	       else
	       {
	    	   if(mButtonIcon!=null)
		       {
		    	   ActivityUtils.PutImageTargetHeight(canvas, mButtonIcon, 14, 12, height-24); 
		       }
		       else
		       {
		    	   
		       }
	       }
	       
	       
	  }

	public int getActionId()
	{
		return Actions.GetIndex(mAction);
	}
	
}
