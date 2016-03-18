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
package CB_Locator.Map;

import java.text.NumberFormat;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import CB_UI_Base.Events.invalidateTextureEvent;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Log.Log;

public class MapScale extends CB_View_Base implements invalidateTextureEvent {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(MapScale.class);
	private BitmapFontCache fontCache;
	private float sollwidth = 0;
	private final MapViewBase mapInstanz;
	private Drawable CachedScaleDrawable;
	private float drawableWidth = 0;
	private String distanceString;
	private boolean imperialunits = false;
	private final static NumberFormat nf = NumberFormat.getInstance();

	public MapScale(CB_RectF rec, String Name, MapViewBase mapInstanz, boolean useImperialUnits) {
		super(rec, Name);
		imperialunits = useImperialUnits;
		sollwidth = rec.getWidth();
		this.mapInstanz = mapInstanz;
		CachedScaleDrawable = null;
		fontCache = new BitmapFontCache(Fonts.getNormal());
		fontCache.setColor(COLOR.getFontColor());
		fontCache.setText("", 0, 0);
		invalidateTextureEventList.Add(this);
	}

	@Override
	protected void Initial() {
		generatedZomm = -1;
		zoomChanged();
	}

	@Override
	protected void SkinIsChanged() {
		invalidateTexture();
		zoomChanged();
	}

	/**
	 * Anzahl der Schritte auf dem Maßstab
	 */
	int scaleUnits = 10;

	/**
	 * Länge des Maßstabs in Metern
	 */
	double scaleLength = 1000;

	float pixelsPerMeter;

	int generatedZomm = -1;

	final int[] scaleNumUnits = new int[] { 4, 3, 4, 3, 4, 5, 3 };
	final float[] scaleSteps = new float[] { 1, 1.5f, 2, 3, 4, 5, 7.5f };

	/**
	 * Nachdem Zoom verändert wurde müssen einige Werte neu berechnet werden
	 */
	public void zoomChanged() {
		if (mapInstanz.pixelsPerMeter <= 0)
			return;
		if (mapInstanz.getAktZoom() == generatedZomm)
			return;

		try {

			pixelsPerMeter = mapInstanz.pixelsPerMeter;

			int multiplyer = 1;
			double scaleSize = 0;
			int idx = 0;
			while (scaleSize < (sollwidth * 0.45)) {
				scaleLength = multiplyer * scaleSteps[idx] * ((imperialunits) ? 1.6093 : 1);
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

		if (imperialunits) {
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

	public void ZoomChanged() {
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
			CachedScaleDrawable = Sprites.MapScale[scaleUnits - 3];
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
		if (mapInstanz.getAktZoom() != generatedZomm) {
			zoomChanged();
			generatedZomm = mapInstanz.getAktZoom();
		}
		if (CachedScaleDrawable == null)
			zoomChanged();

		try {
			if (CachedScaleDrawable != null)
				CachedScaleDrawable.draw(batch, 0, 0, drawableWidth, this.getHeight());
			if (fontCache != null)
				fontCache.draw(batch);
		} catch (Exception e) {
		}
	}

	@Override
	public void invalidateTexture() {
		if (CachedScaleDrawable != null) {
			CachedScaleDrawable = null;
		}
		generatedZomm = -1;

		fontCache.setColor(COLOR.getFontColor());
	}

}
