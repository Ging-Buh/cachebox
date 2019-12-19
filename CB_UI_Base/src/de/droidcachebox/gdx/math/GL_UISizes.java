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
package de.droidcachebox.gdx.math;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.AbstractGlobal;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.utils.log.Log;

/**
 * Diese Klasse Kapselt die Werte, welche in der OpenGL Map benötigt werden. Auch die Benutzen Fonts werden hier gespeichert, da die Grösse
 * hier berechnet wird.
 *
 * @author Longri
 */
public class GL_UISizes implements SizeChangedEvent {
    private static final String log = "GL_UISizes";
    /**
     * das Rechteck, welches die Größe und Position aller GL_View's auf der linken Seite darstellt. Dieses Rechteck ist immer Gültig! Das
     * Rechteck UI_Reight hat die Gleiche Größe und Position wie UI_Left, wenn es sich nicht um ein Tablet Layout handelt.
     */
    public static CB_RectF uiLeft;
    /**
     * Das Rechteck, welches die Größe und Position aller GL_View's auf der rechten Seite darstellt, wenn es sich um ein Tablet Layout
     * handelt. Wenn es sich nicht um ein Tablet Layout handelt, hat dieses Rechteck die selbe Größe und Position wie UI_Left.
     */
    public static CB_RectF uiRight;
    /**
     * Die Höhe des Schattens des Info Panels. Diese muss Berechnet werden, da sie für die Berechnung der Inhalt Positionen gebraucht wird.
     */
    public static float infoShadowHeight;
    public static Vector2 infoLine1;
    public static Vector2 infoLine2;
    /**
     * Dpi Faktor, welcher über die Settings eingestellt werden kann und mit dem HandyDisplay Wert vorbelegt ist. (HD2= 1.5)
     */
    public static float dpi;
    /**
     * DPI Wert des Displays, kann nicht über die Settings verändert werden
     */
    public static float defaultDPI = 1;
    /*
     * Die Font Größe wird über den DPI Faktor berechnet und kann über den FontFaktor zusätzlich beeinflusst werden.
    public static float fontFaktor;
     */
    /**
     * Das Rechteck in dem das Info Panel dargestellt wird.
     */
    public static CB_RectF info;
    /**
     * Das Rechteck in dem der ToggleButton dargestellt wird.
     */
    public static CB_RectF toggle;
    /**
     * Das Rechteck in dem die Zoom Buttons dargestellt wird.
     */
    public static CB_RectF zoomBtn;
    /**
     * Die Größe des Compass Icons. Welche Abhängig von der Höhe des Info Panels ist.
     */
    public static CB_RectF compass;
    /**
     * Halbe Compass grösse welche den Mittelpunkt darstellt.
     */
    public static float halfCompass;
    /**
     * Die Größe des zur Verfügung stehenden Bereiches von Gdx.graphics
     */
    public static CB_RectF surfaceSize;
    /**
     * Größe des position Markers
     */
    public static float posMarkerSize;
    /**
     * Halbe Größe des Position Markers, welche den Mittelpunkt darstellt
     */
    public static float halfPosMarkerSize;
    /**
     * Array der drei möglichen Grössen eines WP Icons
     */
    public static SizeF[] wayPointSizes;
    /**
     * Array der drei möglichen Grössen eines WP Underlay
     */
    public static SizeF[] underlaySizes;
    /**
     * Größe der Cache Info Bubble
     */
    public static SizeF bubble;
    /**
     * halbe breite der Info Bubble, welche den Mitttelpunkt darstellt
     */
    public static float halfBubble;
    /**
     * Korektur Wert zwichen Bubble und deren Content
     */
    public static SizeF bubbleCorrect;
    /**
     * Größe des Target Arrows
     */
    public static SizeF targetArrow;
    public static float margin;
    public static CB_RectF mainBtnSize;
    static float frameHeight = -1;
    // /**
    // * Die Größe der D/T Wertungs Stars
    // */
    // public static SizeF DT_Size;
    static float scale = 0;
    /**
     * Ist false solange die Größen nicht berechnet sind. Diese müssen nur einmal berechnet Werden, oder wenn ein Faktor (DPI oder
     * FontFaktor) in den Settings geändert Wurde.
     */
    private static boolean isInitialized = false;

    /**
     * Initialisiert die Größen und Positionen der UI-Elemente der OpenGL Map, anhand der übergebenen Größe und des Eingestellten DPI Faktors.
     *
     */
    public static void initial(float width, float height) {

        Log.debug(log, "Initial UISizes => " + width + "/" + height);
        Log.debug(log, "DPI = " + dpi);


        if (CB_UI_Base_Settings.mapViewDPIFaktor.getValue() == 1) {
            CB_UI_Base_Settings.mapViewDPIFaktor.setValue(AbstractGlobal.displayDensity);
        }


        if (dpi != CB_UI_Base_Settings.mapViewDPIFaktor.getValue()) {
            dpi = CB_UI_Base_Settings.mapViewDPIFaktor.getValue();
            isInitialized = false; // sizes must be recalculated
        }

        if (surfaceSize == null) {
            surfaceSize = new CB_RectF(0, 0, width, height);
            GL_UISizes tmp = new GL_UISizes();
            surfaceSize.addListener(tmp);
        } else {
            if (surfaceSize.setSize(width, height)) {
                // Surface grösse hat sich geändert, die Positionen der UI-Elemente müssen neu Berechnet werden.
                calcSizes();
                calcPos();
            }
        }

        if (info == null)
            info = new CB_RectF();
        if (toggle == null)
            toggle = new CB_RectF();
        if (zoomBtn == null)
            zoomBtn = new CB_RectF();
        if (compass == null)
            compass = new CB_RectF();
        if (infoLine1 == null)
            infoLine1 = new Vector2();
        if (infoLine2 == null)
            infoLine2 = new Vector2();
        if (bubble == null)
            bubble = new SizeF();
        if (bubbleCorrect == null)
            bubbleCorrect = new SizeF();

        if (!isInitialized) {
            calcSizes();

            CB_UI_Base_Settings.nightMode.addSettingChangedListener(() -> Fonts.setNightMode(CB_UI_Base_Settings.nightMode.getValue()));

            try {
                Fonts.loadFonts();
            } catch (Exception ex) {
                Log.err(log, "Initialize: Load fonts", ex);
            }

            calcPos();

            isInitialized = true;

        }
    }

    /**
     * Berechnet die Positionen der UI-Elemente
     */
    private static void calcPos() {
        Log.debug(log, "GL_UISizes.calcPos()");

        float w = uiLeft.getWidth();
        float h = uiLeft.getHeight();

        info.setPos(new Vector2(margin, (h - margin - info.getHeight())));

        float CompassMargin = (info.getHeight() - compass.getWidth()) / 2;

        compass.setPos(new Vector2(info.getX() + CompassMargin, info.getY() + infoShadowHeight + CompassMargin));

        toggle.setPos(new Vector2((w - margin - toggle.getWidth()), h - margin - toggle.getHeight()));

        zoomBtn.setPos(new Vector2((w - margin - zoomBtn.getWidth()), margin));

        infoLine1.x = compass.getMaxX() + margin;
        GlyphLayout bounds;
        if (Fonts.getNormal() != null) {
            bounds = new GlyphLayout();
            bounds.setText(Fonts.getSmall(), "52° 34,806N ");
        } else {
            bounds = new GlyphLayout();
            bounds.height = 20;
            bounds.width = 100;
        }

        infoLine2.x = info.getX() + info.getWidth() - bounds.width - (margin * 2);

        float t1 = info.getHeight() / 4;
        infoLine1.y = info.getMaxY() - t1;
        infoLine2.y = info.getY() + t1 + bounds.height;

    }

    /**
     * Berechnet die Größen der UI-Elemente
     */
    private static void calcSizes() {
        Log.debug(log, "GL_UISizes.calcSizes()");
        // größe der Frames berechnen
        int frameLeftwidth = UiSizes.getInstance().RefWidth;
        // private static int BottomButtonHeight = convertDip2Pix(65);
        int MainButtonSideLength = Math.round(Math.min(frameLeftwidth / 5.8f, convertDip2Pix(63)));
        mainBtnSize = new CB_RectF(0, 0, MainButtonSideLength, MainButtonSideLength);

        margin = (float) (6.6666667 * dpi);

        frameHeight = UiSizes.getInstance().getWindowHeight() - convertDip2Pix(35) - MainButtonSideLength;

        uiLeft = new CB_RectF(0, convertDip2Pix(65), frameLeftwidth, frameHeight);
        uiRight = new CB_RectF(uiLeft);


        infoShadowHeight = (float) (3.333333 * defaultDPI);
        info.setSize((UiSizes.getInstance().RefWidth - (UiSizes.getInstance().getButtonHeight() * 1.1f) - (margin * 3)), UiSizes.getInstance().getButtonHeight() * 1.1f);
        compass.setSize((float) (44.6666667 * dpi), (float) (44.6666667 * dpi));
        halfCompass = compass.getHeight() / 2;
        toggle.setSize(UiSizes.getInstance().getButtonHeight() * 1.1f, UiSizes.getInstance().getButtonHeight() * 1.1f);
        zoomBtn.setSize((158 * defaultDPI), 48 * defaultDPI);
        posMarkerSize = (float) (46.666667 * dpi);
        halfPosMarkerSize = posMarkerSize / 2;

        targetArrow = new SizeF((float) (12.6 * dpi), (float) (38.4 * dpi));

        underlaySizes = new SizeF[]{new SizeF(13 * dpi, 13 * dpi), new SizeF(14 * dpi, 14 * dpi), new SizeF(21 * dpi, 21 * dpi)};
        wayPointSizes = new SizeF[]{new SizeF(13 * dpi, 13 * dpi), new SizeF(20 * dpi, 20 * dpi), new SizeF(32 * dpi, 32 * dpi)};

        bubble.setSize((float) 273.3333334 * defaultDPI, (float) 113.333334 * defaultDPI);
        halfBubble = bubble.getWidth() / 2;
        bubbleCorrect.setSize((float) (6.6666667 * dpi), (float) 26.66667 * dpi);

        // DT_Size = new SizeF(37 * DPI, (37 * DPI * 0.2f));

    }

    /*
    public static void writeDebug(String name, CB_RectF rec) {
        Log.debug(log, name + "   ------ x/y/W/H =  " + rec.getX() + "/" + rec.getY() + "/" + rec.getWidth() + "/" + rec.getHeight());
    }

    public static void writeDebug(String name, float size) {
        Log.debug(log, name + "   ------ size =  " + size);
    }

    public static void writeDebug(String name, SizeF sizeF) {
        Log.debug(log, name + "   ------ W/H =  " + sizeF.getWidth() + "/" + sizeF.getHeight());
    }

    public static void writeDebug(String name, SizeF[] SizeArray) {
        for (int i = 0; i < SizeArray.length; i++) {
            writeDebug(name + "[" + i + "]", SizeArray[i]);
        }
    }
     */

    private static int convertDip2Pix(float dips) {
        // Converting dips to pixels
        if (scale == 0)
            scale = UiSizes.getInstance().getScale();
        return Math.round(dips * scale);
    }

    @Override
    public void sizeChanged() {
        Log.debug(log, "GL_UISizes.sizeChanged()");
        calcPos();

    }

}
