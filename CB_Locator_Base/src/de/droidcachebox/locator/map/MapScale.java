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
package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.text.NumberFormat;

import de.droidcachebox.InvalidateTextureListeners;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.log.Log;

public class MapScale extends CB_View_Base implements InvalidateTextureListeners.InvalidateTextureListener {
    private static final String log = "MapScale";
    private final static NumberFormat nf = NumberFormat.getInstance();
    private final int[] scaleNumUnits = new int[]{4, 3, 4, 3, 4, 5, 3};
    private final float[] scaleSteps = new float[]{1, 1.5f, 2, 3, 4, 5, 7.5f};
    private final MapViewBase mapInstanz;
    /**
     * Anzahl der Schritte auf dem Maßstab
     */
    private int scaleUnits = 5; // must always be between 3 and 5
    /**
     * Länge des Maßstabs in Metern
     */
    private double scaleLength = 1000;
    private float pixelsPerMeter;
    private int generatedZomm = -1;
    private BitmapFontCache fontCache;
    private final float wantedWidth;
    private Drawable CachedScaleDrawable;
    private float drawableWidth = 0;
    private String distanceString;
    private final boolean imperialUnits;

    public MapScale(CB_RectF rec, String Name, MapViewBase mapInstanz, boolean useImperialUnits) {
        super(rec, Name);
        imperialUnits = useImperialUnits;
        wantedWidth = rec.getWidth();
        this.mapInstanz = mapInstanz;
        CachedScaleDrawable = null;
        fontCache = new BitmapFontCache(Fonts.getNormal());
        fontCache.setColor(COLOR.getFontColor());
        fontCache.setText("", 0, 0);
        InvalidateTextureListeners.getInstance().addListener(this);
    }

    @Override
    protected void initialize() {
        generatedZomm = -1;
        zoomChanged();
    }

    @Override
    protected void skinIsChanged() {
        handleInvalidateTexture();
        zoomChanged();
    }

    /**
     * Nachdem Zoom verändert wurde müssen einige Werte neu berechnet werden
     */
    public void zoomChanged() {
        if (mapInstanz.pixelsPerMeter <= 0)
            return;
        if (mapInstanz.getCurrentZoom() == generatedZomm)
            return;

        try {

            pixelsPerMeter = mapInstanz.pixelsPerMeter;

            int multiplyer = 1;
            double scaleSize = 0;
            int idx = 0;
            while (scaleSize < (wantedWidth * 0.45)) {
                scaleLength = multiplyer * scaleSteps[idx] * ((imperialUnits) ? 1.6093 : 1);
                scaleUnits = scaleNumUnits[idx];

                scaleSize = pixelsPerMeter * scaleLength;

                idx++;
                if (idx == scaleNumUnits.length) {
                    idx = 0;
                    multiplyer *= 10;
                }
            }
        } catch (Exception exc) {
            Log.err(log, "MapView.zoomChanged()", "", exc);
        }

        if (imperialUnits) {
            nf.setMaximumFractionDigits(2);
            distanceString = nf.format(scaleLength / 1609.3) + "mi";
        } else if (scaleLength <= 500) {
            nf.setMaximumFractionDigits(0);
            distanceString = nf.format(scaleLength) + "m";
        } else {
            double length = scaleLength / 1000;
            nf.setMaximumFractionDigits(0);
            distanceString = nf.format(length) + "km";
        }

        ZoomChanged();
    }

    void ZoomChanged() {
        // todo is called, when scaleUnits is not yet initialized
        pixelsPerMeter = mapInstanz.pixelsPerMeter;
        drawableWidth = (int) (scaleLength * pixelsPerMeter);
        if (fontCache == null) {
            fontCache = new BitmapFontCache(Fonts.getNormal());
            fontCache.setColor(COLOR.getFontColor());
            fontCache.setText("", 0, 0);
        }

        try {
            GlyphLayout bounds = fontCache.setText(distanceString, 0, fontCache.getFont().isFlipped() ? 0 : fontCache.getFont().getCapHeight());
            this.setWidth((float) (drawableWidth + (bounds.width * 1.3)));
            CachedScaleDrawable = Sprites.MapScale[scaleUnits - 3]; // 0 = 3 Striche, 1 = 4 Striche, 2 = 5 Striche
            float margin = (this.getHeight() - bounds.height) / 1.6f;
            fontCache.setPosition(this.getWidth() - bounds.width - margin, margin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zeichnet den Maßstab. pixelsPerKm muss durch zoomChanged initialisiert sein!
     */
    @Override
    protected void render(Batch batch) {
        if (pixelsPerMeter <= 0)
            return;
        if (mapInstanz.getCurrentZoom() != generatedZomm) {
            zoomChanged();
            generatedZomm = mapInstanz.getCurrentZoom();
        }
        if (CachedScaleDrawable == null)
            zoomChanged();

        try {
            if (CachedScaleDrawable != null)
                CachedScaleDrawable.draw(batch, 0, 0, drawableWidth, this.getHeight());
            if (fontCache != null)
                fontCache.draw(batch);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void handleInvalidateTexture() {
        if (CachedScaleDrawable != null) {
            CachedScaleDrawable = null;
        }
        generatedZomm = -1;

        fontCache.setColor(COLOR.getFontColor());
    }

}
