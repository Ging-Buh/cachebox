package de.droidcachebox.Custom_Controls.QuickButtonList;

import android.graphics.drawable.Drawable;
import de.droidcachebox.Enums.Actions;

/**
 * Stellt ein Item der Quick Button List dar
 * @author Longri
 *
 */
public class QuickButtonItem 
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
	public QuickButtonItem(Actions Action, Drawable Icon,String Desc)
	{
		mAction=Action;
		mButtonIcon=Icon;
		mActionDesc=Desc;
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
	
}
