/* 
 * Copyright (C) 2014 team-cachebox.de
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

import CB_UI_Base.Global;
import CB_UI_Base.settings.CB_UI_Base_Settings;

public abstract class UI_Size_Base {
    public static UI_Size_Base that;

    public UI_Size_Base() {
	that = this;
    }

    protected Size Button;
    protected int scaledFontSize_normal;
    protected int iconSize;
    protected int windowWidth;
    protected int windowHeight;
    protected int scaledFontSize_big;
    protected int scaledFontSize_btn;
    protected int ScaledFontSize_small;
    protected int ScaledFontSize_supersmall;
    protected int IconContextMenuHeight;
    protected float scale;
    protected int margin;
    protected double calcBase;
    protected int RefWidth;
    protected int mClickToleranz;
    protected DevicesSizes devicesSizes;

    public abstract void instanzeInitial();

    public Size initial(DevicesSizes devicesSizes) {
	this.devicesSizes = devicesSizes;
	this.windowWidth = devicesSizes.Window.width;
	this.windowHeight = devicesSizes.Window.height;
	this.scale = devicesSizes.Density; // res.getDisplayMetrics().density;

	mClickToleranz = (int) (17 * scale);

	calcBase = 533.333 * scale;

	margin = (int) (10 * scale);

	float NormalTextSize = CB_UI_Base_Settings.FONT_SIZE_NORMAL.getValue() * 3.2f;

	int b = (int) (NormalTextSize * scale);
	Button = new Size(b, b);

	if (Global.isTab) {
	    // RefWidth LeftWidth on Tab
	    // must have place for 5 Buttons

	    b *= 1.5f; // Bud the bottom button are a little bit higher!

	    RefWidth = ((b + margin) * 5) + margin;
	} else {
	    RefWidth = windowWidth;
	}

	GL_UISizes.writeDebug("Button", Button.asFloat());

	scaledFontSize_normal = (int) (10 * scale);
	scaledFontSize_big = (int) (scaledFontSize_normal * 1.1);
	ScaledFontSize_small = (int) (scaledFontSize_normal * 0.9);
	ScaledFontSize_supersmall = (int) (ScaledFontSize_small * 0.8);
	scaledFontSize_btn = (int) (11 * scale);

	iconSize = (int) (10 * scale);

	IconContextMenuHeight = (int) (calcBase / 11.1);

	instanzeInitial();

	return new Size(windowWidth, windowHeight);

    }

    public int getMargin() {
	return margin;
    }

    public int getWindowHeight() {
	return windowHeight;
    }

    public int getWindowWidth() {
	return windowWidth;
    }

    public int getButtonHeight() {
	return Button.height;
    }

    public int getButtonWidth() {
	return Button.width;
    }

    public int getButtonWidthWide() {
	return (int) (Button.width * 1.6);
    }

    public SizeF getChkBoxSize() {
	float h = Button.height * 0.88f;
	return new SizeF(h, h);
    }

    public int getScaledFontSize() {
	return scaledFontSize_normal;
    }

    public int getScaledFontSize_btn() {
	return scaledFontSize_btn;
    }

    public int getScaledFontSize_big() {
	return scaledFontSize_big;
    }

    public int getScaledFontSize_small() {
	return ScaledFontSize_small;
    }

    public int getScaledFontSize_supersmall() {
	return ScaledFontSize_supersmall;
    }

    public int getIconSize() {
	return iconSize;
    }

    public float getScale() {
	return scale;
    }

    public float getSmallestWidth() {
	return Math.min(windowHeight, windowWidth);
    }

    public int getClickToleranz() {
	return mClickToleranz;
    }

    public void setScale(float scale) {
	this.scale = scale;
    }

}