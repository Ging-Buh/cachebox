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

package CB_Core.Math;

//import de.cachebox_test.R;

/**
 * Enth�lt die Gr��en einzelner Controls
 * 
 * @author Longri
 */
public class UiSizes extends UI_Size_Base
{

	public static UiSizes that;

	public UiSizes()
	{
		super();
		that = this;
	}

	Size QuickButtonList;
	int CacheInfoHeight;

	int scaledIconSize;
	int CornerSize;
	int infoSliderHeight;
	int spaceWidth;
	int tabWidth;
	int halfCornerSize;
	Size CacheListItemSize;
	// private Rect CacheListDrawRec;
	CB_Rect CacheListDrawRec;
	int StrengthHeightMultipler;
	int arrowScaleList;
	int arrowScaleMap;
	int TB_icon_Size;

	int QuickButtonRef;

	@Override
	public void instanzeInitial()
	{
		QuickButtonRef = 320;
		QuickButtonList = new Size((int) (QuickButtonRef * scale - (13.3333f * scale)), (int) (((QuickButtonRef * scale) / 5) - 4 * scale));

		scaledIconSize = (int) ((calcBase / ui.IconSize) * scale);

		CornerSize = scaledRefSize_normal;
		CacheInfoHeight = (int) (scaledRefSize_normal * 8.5);
		infoSliderHeight = (int) (scaledRefSize_normal * 2.4);

		spaceWidth = (int) (scaledFontSize_normal * 0.9);
		tabWidth = (int) (scaledFontSize_normal * 0.6);
		halfCornerSize = (int) CornerSize / 2;

		float ItemHeight = ui.Density * 63;

		CacheListItemSize = new Size(RefWidth, (int) ItemHeight);
		CacheListDrawRec = CacheListItemSize.getBounds(5, 2, -5, -2);
		StrengthHeightMultipler = (int) (calcBase / 600);

		arrowScaleList = ui.ArrowSizeList;
		arrowScaleMap = ui.ArrowSizeMap;
		TB_icon_Size = ui.TB_IconSize;

	}

	public int getTbIconSize()
	{
		return TB_icon_Size;
	}

	public int getArrowScaleList()
	{
		return arrowScaleList;
	}

	public int getArrowScaleMap()
	{
		return arrowScaleMap;
	}

	public int getQuickButtonListHeight()
	{
		return QuickButtonList.height;
	}

	public int getQuickButtonListWidth()
	{
		return QuickButtonList.width;
	}

	public int getCacheInfoHeight()
	{
		return CacheInfoHeight;
	}

	public int getInfoSliderHeight()
	{
		return infoSliderHeight;
	}

	public Size getCacheListItemSize()
	{
		return CacheListItemSize;
	}

	public CB_Rect getCacheListItemRec()
	{
		return CacheListDrawRec;
	}

	public int getIconAddCorner()
	{
		return iconSize + CornerSize;
	}

	public int getStrengthHeight()
	{
		return StrengthHeightMultipler;
	}

	public int getIconContextMenuHeight()
	{
		return IconContextMenuHeight;
	}

	public int getCornerSize()
	{
		return CornerSize;
	}

	public int getScaledIconSize()
	{
		return scaledIconSize;
	}

	public int getSpaceWidth()
	{
		return spaceWidth;
	}

	public int getTabWidth()
	{
		return tabWidth;
	}

	public int getHalfCornerSize()
	{
		return halfCornerSize;
	}

}