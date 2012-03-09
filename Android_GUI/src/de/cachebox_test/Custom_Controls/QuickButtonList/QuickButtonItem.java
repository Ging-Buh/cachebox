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

package de.cachebox_test.Custom_Controls.QuickButtonList;

import CB_Core.GlobalCore;
import CB_Core.Math.UiSizes;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.Enums.Actions;
import de.cachebox_test.Ui.ActivityUtils;

/**
 * Stellt ein Item der Quick Button List dar
 * 
 * @author Longri
 */
public class QuickButtonItem extends View
{
	private Actions mAction;
	private Drawable mButtonIcon;
	private String mActionDesc;

	/**
	 * Constructor
	 * 
	 * @param Action
	 *            Action Enum
	 * @param Icon
	 *            Action icon
	 * @param Desc
	 *            Action Beschreibung
	 */
	public QuickButtonItem(Context context, Actions Action, Drawable Icon, String Desc)
	{
		super(context);
		mAction = Action;
		mButtonIcon = Icon;
		mActionDesc = Desc;
	}

	public QuickButtonItem(Context context, Actions Action, int Height)
	{
		super(context);
		mAction = Action;
		mButtonIcon = Actions.getDrawable(Action);
		mActionDesc = Actions.getName(Action);
		height = width = Height;

		setMeasuredDimension(this.width, this.height);

	}

	/**
	 * Gibt die Action dieses Items wieder
	 * 
	 * @return Actions
	 */
	public Actions getAction()
	{
		return mAction;
	}

	/**
	 * Gibt das Icon dieses Items wieder
	 * 
	 * @return Drawable
	 */
	public Drawable getIcon()
	{
		return mButtonIcon;
	}

	/**
	 * Gibt die Beschreibung dieses Items wieder
	 * 
	 * @return String
	 */
	public String getDesc()
	{
		return mActionDesc;
	}

	private int width;
	private int height;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		width = UiSizes.getQuickButtonWidth();
		height = UiSizes.getQuickButtonHeight();
		setMeasuredDimension(this.width, this.height);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// Draw Button Mask
		ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[1], 1, 1, height - 2);

		// draw icon or name

		if (this.mAction.ordinal() == Actions.AutoResort.ordinal())
		{// Wenn die Action ID = AutoResort ist, muss erst der Zustand On/Off
			// abgefragt werden
			if (Global.autoResort)
			{
				ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[15], 14, 12, height - 24);
			}
			else
			{
				ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[16], 14, 12, height - 24);
			}
		}
		else if (this.mAction.ordinal() == Actions.Spoiler.ordinal())
		{// Wenn die Action ID = Spoiler ist, muss erst die existence von
			// Spoilern abgefragt werden
			if (GlobalCore.SelectedCache() != null && GlobalCore.SelectedCache().SpoilerExists())
			{
				Global.BtnIcons[18].clearColorFilter();
				ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[18], 14, 12, height - 24);
			}
			else
			{
				ActivityUtils.PutImageTargetHeightColor(canvas, Global.BtnIcons[18], 14, 12, height - 24,
						Global.getColor(R.attr.TextColor_disable), Mode.SRC_IN);
			}
		}
		else if (this.mAction.ordinal() == Actions.Hint.ordinal())
		{// Wenn die Action ID = Hint ist, muss erst die existence von
			// Hint abgefragt werden
			if (GlobalCore.SelectedCache() != null && !GlobalCore.SelectedCache().hint.equals(""))
			{
				Global.BtnIcons[19].clearColorFilter();
				ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[19], 14, 12, height - 24);
			}
			else
			{
				ActivityUtils.PutImageTargetHeightColor(canvas, Global.BtnIcons[19], 14, 12, height - 24,
						Global.getColor(R.attr.TextColor_disable), Mode.SRC_IN);
			}
		}
		else if (this.mAction.ordinal() == Actions.WaypointView.ordinal())
		{// nur scharf, wenn es Wegpunkte gibt
			if (GlobalCore.SelectedCache() != null && !GlobalCore.SelectedCache().waypoints.isEmpty())
			{
				Global.BtnIcons[3].clearColorFilter();
				ActivityUtils.PutImageTargetHeight(canvas, Global.BtnIcons[3], 14, 12, height - 24);
			}
			else
			{
				ActivityUtils.PutImageTargetHeightColor(canvas, Global.BtnIcons[3], 14, 12, height - 24,
						Global.getColor(R.attr.TextColor_disable), Mode.SRC_IN);
			}
		}
		else
		{
			if (mButtonIcon != null)
			{
				ActivityUtils.PutImageTargetHeight(canvas, mButtonIcon, 14, 12, height - 24);
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
