/*
 * Copyright (C) 2011-2014 team-cachebox.de
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
package CB_UI_Base.GL_UI;

import CB_Utils.Util.HSV_Color;
import com.badlogic.gdx.graphics.Color;

/**
 * Hold the loaded colors from Skin!
 *
 * @author Longri
 */
public class COLOR {
    private final static HSV_Color TRANSPARENT = new HSV_Color(0, 0, 0, 0);

    private static HSV_Color day_fontColor;
    private static HSV_Color day_fontColorDisable;
    private static HSV_Color day_fontColorHighLight;
    private static HSV_Color day_fontColorLink;
    private static HSV_Color day_darknesColor;
    private static HSV_Color day_crossColor;
    private static HSV_Color day_MenuBackColor;
    private static HSV_Color day_popup_menu_info_back;
    private static HSV_Color day_popup_menu_border;
    private static HSV_Color day_popup_menu_icon_back;

    private static HSV_Color night_fontColor;
    private static HSV_Color night_fontColorDisable;
    private static HSV_Color night_fontColorHighLight;
    private static HSV_Color night_fontColorLink;
    private static HSV_Color night_darknesColor;
    private static HSV_Color night_crossColor;
    private static HSV_Color night_MenuBackColor;
    private static HSV_Color night_popup_menu_info_back;
    private static HSV_Color night_popup_menu_border;
    private static HSV_Color night_popup_menu_icon_back;
    public static void loadColors() {

        day_fontColor = getDayColor("font-color");
        day_fontColorDisable = getDayColor("font-color-disable");
        day_fontColorHighLight = getDayColor("font-color-highlight");
        day_fontColorLink = getDayColor("font-color-link");
        day_darknesColor = getDayColor("darknes");
        day_crossColor = getDayColor("cross");
        day_MenuBackColor = getDayColor("menu-back-color");
        day_popup_menu_info_back = getDayColor("popup-menu-info-back");
        day_popup_menu_border = getDayColor("popup-menu-border");
        day_popup_menu_icon_back = getDayColor("popup-menu-icon-back");

        night_fontColor = getNightColor("font-color");
        night_fontColorDisable = getNightColor("font-color-disable");
        night_fontColorHighLight = getNightColor("font-color-highlight");
        night_fontColorLink = getNightColor("font-color-link");
        night_darknesColor = getNightColor("darknes");
        night_crossColor = getNightColor("cross");
        night_MenuBackColor = getNightColor("menu-back-color");
        night_popup_menu_info_back = getNightColor("popup-menu-info-back");
        night_popup_menu_border = getNightColor("popup-menu-border");
        night_popup_menu_icon_back = getNightColor("popup-menu-icon-back");
    }

    private static HSV_Color getDayColor(String name) {

        Color ret = null;
        try {
            ret = CB_Skin.getInstance().getDaySkin().getColor(name);
        } catch (Exception e) {
        }

        if (ret == null) // use default from APK
        {
            ret = CB_Skin.getInstance().getDefaultDaySkin().getColor(name);
        }
        return new HSV_Color(ret);
    }

    private static HSV_Color getNightColor(String name) {

        Color ret = null;
        try {
            ret = CB_Skin.getInstance().getNightSkin().getColor(name);
        } catch (Exception e) {
        }

        if (ret == null) // use default from APK
        {
            ret = CB_Skin.getInstance().getDefaultNightSkin().getColor(name);
        }
        return new HSV_Color(ret);
    }

    public static HSV_Color getMenuBackColor() {
        return CB_Skin.getInstance().getNightMode() ? night_MenuBackColor : day_MenuBackColor;
    }

    public static HSV_Color getFontColor() {
        return CB_Skin.getInstance().getNightMode() ? night_fontColor : day_fontColor;
    }

    public static HSV_Color getDisableFontColor() {
        return CB_Skin.getInstance().getNightMode() ? night_fontColorDisable : day_fontColorDisable;
    }

    public static HSV_Color getHighLightFontColor() {
        return CB_Skin.getInstance().getNightMode() ? night_fontColorHighLight : day_fontColorHighLight;
    }

    public static HSV_Color getLinkFontColor() {
        return CB_Skin.getInstance().getNightMode() ? night_fontColorLink : day_fontColorLink;
    }

    public static HSV_Color getDarknesColor() {
        return CB_Skin.getInstance().getNightMode() ? night_darknesColor : day_darknesColor;
    }

    public static HSV_Color getCrossColor() {
        return CB_Skin.getInstance().getNightMode() ? night_crossColor : day_crossColor;
    }

    public static HSV_Color getPopUpInfoBackColor() {
        return CB_Skin.getInstance().getNightMode() ? night_popup_menu_info_back : day_popup_menu_info_back;
    }

    public static HSV_Color getPopUpMenuBorderColor() {
        return CB_Skin.getInstance().getNightMode() ? night_popup_menu_border : day_popup_menu_border;
    }

    public static HSV_Color getPopUpMenuIconBackColor() {
        return CB_Skin.getInstance().getNightMode() ? night_popup_menu_icon_back : day_popup_menu_icon_back;
    }

    public static HSV_Color getTransparent() {
        return TRANSPARENT;
    }
}
