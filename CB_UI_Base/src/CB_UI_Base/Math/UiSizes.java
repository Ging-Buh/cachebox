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

package CB_UI_Base.Math;

import CB_UI_Base.settings.CB_UI_Base_Settings;

/**
 * Enthält die Größen einzelner Controls
 *
 * @author Longri
 */
public class UiSizes extends UI_Size_Base {

    private static UiSizes uiSizes;
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
    CB_RectF ButtonRectF;
    CB_RectF WideButtonRectF;
    private boolean isInitialized;

    private UiSizes() {
        isInitialized = false;
    }

    public static UiSizes getInstance() {
        if (uiSizes == null) uiSizes = new UiSizes();
        return uiSizes;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Size initialize(DevicesSizes devicesSizes) {
        this.devicesSizes = devicesSizes;
        this.windowWidth = devicesSizes.Window.width;
        this.windowHeight = devicesSizes.Window.height;
        this.scale = devicesSizes.Density; // res.getDisplayMetrics().density;

        mClickToleranz = (int) (17 * scale);

        calcBase = 533.333 * scale;

        margin = (int) (10 * scale);

        float NormalTextSize = CB_UI_Base_Settings.FONT_SIZE_NORMAL.getValue() * 3.2f;

        ButtonWidth = (int) (NormalTextSize * scale);
        ButtonHeight = ButtonWidth;
        WideButtonWidth = (windowWidth - 4 * margin) / 3;

        RefWidth = windowWidth;

        scaledFontSize_normal = (int) (10 * scale);
        scaledFontSize_big = (int) (scaledFontSize_normal * 1.1);
        ScaledFontSize_small = (int) (scaledFontSize_normal * 0.9);
        ScaledFontSize_supersmall = (int) (ScaledFontSize_small * 0.8);
        scaledFontSize_btn = (int) (11 * scale);

        iconSize = (int) (10 * scale);

        IconContextMenuHeight = (int) (calcBase / 11.1);

        QuickButtonRef = 320;
        QuickButtonList = new Size((int) (QuickButtonRef * scale - (13.3333f * scale)), (int) (((QuickButtonRef * scale) / 5) - 4 * scale));

        scaledIconSize = (int) (10 * scale);

        CornerSize = (int) (10 * scale);
        CacheInfoHeight = (int) (40 * scale);
        infoSliderHeight = (int) (30 * scale);

        spaceWidth = (int) (scaledFontSize_normal * 0.9);
        tabWidth = (int) (scaledFontSize_normal * 0.6);
        halfCornerSize = CornerSize / 2;

        float ItemHeight = devicesSizes.Density * 63;

        CacheListItemSize = new Size(RefWidth, (int) ItemHeight);
        CacheListDrawRec = CacheListItemSize.getBounds(5, 2, -5, -2);
        StrengthHeightMultipler = (int) (calcBase / 600);

        arrowScaleList = (int) (10 * scale);
        arrowScaleMap = (int) (10 * scale);
        TB_icon_Size = (int) (10 * scale);
        ButtonRectF = new CB_RectF(0, 0, ButtonWidth, ButtonHeight);
        WideButtonRectF = new CB_RectF(0, 0, WideButtonWidth, ButtonHeight);

        isInitialized = true;
        return new Size(windowWidth, windowHeight);
    }

    public int getTbIconSize() {
        return TB_icon_Size;
    }

    public int getArrowScaleList() {
        return arrowScaleList;
    }

    public int getArrowScaleMap() {
        return arrowScaleMap;
    }

    public int getQuickButtonListHeight() {
        return QuickButtonList.height;
    }

    public int getQuickButtonListWidth() {
        return QuickButtonList.width;
    }

    public int getCacheInfoHeight() {
        return CacheInfoHeight;
    }

    public int getInfoSliderHeight() {
        return infoSliderHeight;
    }

    public Size getCacheListItemSize() {
        return CacheListItemSize;
    }

    public CB_Rect getCacheListItemRec() {
        return CacheListDrawRec;
    }

    public int getIconAddCorner() {
        return iconSize + CornerSize;
    }

    public int getStrengthHeight() {
        return StrengthHeightMultipler;
    }

    public int getIconContextMenuHeight() {
        return IconContextMenuHeight;
    }

    public int getCornerSize() {
        return CornerSize;
    }

    public int getScaledIconSize() {
        return scaledIconSize;
    }

    public int getSpaceWidth() {
        return spaceWidth;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public int getHalfCornerSize() {
        return halfCornerSize;
    }

    public CB_RectF getButtonRectF() {
        return ButtonRectF;
    }

    public CB_RectF getWideButtonRectF() {
        return WideButtonRectF;
    }
}
