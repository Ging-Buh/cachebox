package CB_Core.GL_UI.Controls;

import java.text.NumberFormat;

import CB_Core.Config;
import CB_Core.UnitFormatter;
import CB_Core.Events.invalidateTextureEvent;
import CB_Core.Events.invalidateTextureEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MapScale extends CB_View_Base implements invalidateTextureEvent
{
	private BitmapFontCache fontCache;
	float sollwidth = 0;
	private MapView mapInstanz;

	public MapScale(CB_RectF rec, String Name, MapView mapInstanz)
	{
		super(rec, Name);
		sollwidth = rec.getWidth();
		this.mapInstanz = mapInstanz;
		CachedScaleSprite = null;
		invalidateTextureEventList.Add(this);
	}

	@Override
	protected void Initial()
	{
		generatedZomm = -1;
		zoomChanged();
	}

	@Override
	protected void SkinIsChanged()
	{
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

	/**
	 * Nachdem Zoom verändert wurde müssen einige Werte neu berechnet werden
	 */
	public void zoomChanged()
	{
		if (mapInstanz.pixelsPerMeter <= 0) return;
		if (mapInstanz.getAktZoom() == generatedZomm) return;

		try
		{
			int[] scaleNumUnits = new int[]
				{ 4, 3, 4, 3, 4, 5, 3 };
			float[] scaleSteps = new float[]
				{ 1, 1.5f, 2, 3, 4, 5, 7.5f };

			pixelsPerMeter = mapInstanz.pixelsPerMeter;

			int multiplyer = 1;
			double scaleSize = 0;
			int idx = 0;
			while (scaleSize < (sollwidth * 0.45))
			{
				scaleLength = multiplyer * scaleSteps[idx] * ((UnitFormatter.ImperialUnits) ? 1.6093 : 1);
				scaleUnits = scaleNumUnits[idx];

				scaleSize = pixelsPerMeter * scaleLength;

				idx++;
				if (idx == scaleNumUnits.length)
				{
					idx = 0;
					multiplyer *= 10;
				}
			}
		}
		catch (Exception exc)
		{
			Logger.Error("MapView.zoomChanged()", "", exc);
		}

		String distanceString;

		if (UnitFormatter.ImperialUnits)
		{
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			distanceString = nf.format(scaleLength / 1609.3) + "mi";
		}
		else if (scaleLength <= 500)
		{
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(0);
			distanceString = nf.format(scaleLength) + "m";
		}
		else
		{
			double length = scaleLength / 1000;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(0);
			distanceString = nf.format(length) + "km";
		}

		int pos = 0;
		int start = 1;
		boolean N = Config.settings.nightMode.getValue();

		Color[] brushes = new Color[2];

		brushes[0] = (N ? Color.RED : Color.BLACK);
		brushes[1] = (N ? Color.BLACK : Color.WHITE);

		int w = getNextHighestPO2((int) width);
		int h = getNextHighestPO2((int) height);
		// Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
		Pixmap p = new Pixmap(w, h, Pixmap.Format.RGB565);

		for (int i = 1; i <= scaleUnits; i++)
		{
			pos = (int) (scaleLength * ((double) i / scaleUnits) * pixelsPerMeter);

			int colorChanger = i % 2;

			p.setColor(brushes[colorChanger]);

			p.fillRectangle(start, 0, (int) pos, ((int) height) - 2);

			start = pos;
		}

		fontCache = new BitmapFontCache(Fonts.getNormal());

		TextBounds bounds = fontCache.setText(distanceString, 0, fontCache.getFont().isFlipped() ? 0 : fontCache.getFont().getCapHeight());

		this.setWidth((float) (pos + (bounds.width * 1.3)));

		p.setColor(brushes[0]);
		p.drawRectangle(0, 0, (int) (pos), ((int) height) - 1);

		Texture tex = new Texture(p);

		CachedScaleSprite = new Sprite(tex, (int) pos, (int) this.getHeight());
		p.dispose();

		float margin = (this.getHeight() - bounds.height) / 1.6f;
		fontCache.setPosition(this.width - bounds.width - margin, margin);

	}

	Sprite CachedScaleSprite;

	/**
	 * Zeichnet den Maßstab. pixelsPerKm muss durch zoomChanged initialisiert sein!
	 */
	@Override
	protected void render(SpriteBatch batch)
	{
		if (pixelsPerMeter <= 0) return;
		if (CachedScaleSprite == null) zoomChanged();
		if (CachedScaleSprite != null) CachedScaleSprite.draw(batch);
		if (fontCache != null) fontCache.draw(batch);

	}

	@Override
	public void invalidateTexture()
	{
		if (CachedScaleSprite != null)
		{
			CachedScaleSprite.getTexture().dispose();
			CachedScaleSprite = null;
		}
		generatedZomm = -1;
	}

}
