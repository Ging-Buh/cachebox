package CB_Core.GL_UI.Controls;

import CB_Core.Events.GpsStateChangeEvent;
import CB_Core.Events.GpsStateChangeEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.GPS;
import CB_Core.Locator.GpsStatus;
import CB_Core.Locator.GpsStrength;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SatBarChart extends CB_View_Base implements GpsStateChangeEvent
{

	private GpsStatus mGpsStatus;
	private Image[] balken = null;

	public SatBarChart(CB_RectF rec, String Name)
	{
		super(rec, Name);

	}

	public void setGpsStatus(GpsStatus status)
	{
		mGpsStatus = status;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (redraw) setSatStrength();

	}

	private void setSatStrength()
	{
		float minH = (SpriteCache.bar.getBottomHeight() / 2) + SpriteCache.bar.getTopHeight();

		float w = (this.width / 14);
		boolean small = SpriteCache.bar.getMinWidth() > w * 1.2f;
		if (small)
		{
			w = (this.width / 12);
		}

		if (balken == null)
		{

			float iniHeight = small ? SpriteCache.barSmall.getTopHeight() : SpriteCache.bar.getTopHeight();

			w += 1;
			balken = new Image[14];
			balken[0] = new Image(new CB_RectF(0, 0, w, iniHeight), "");
			balken[1] = new Image(new CB_RectF(balken[0].getMaxX() - 1, 0, w, iniHeight), "");
			balken[2] = new Image(new CB_RectF(balken[1].getMaxX() - 1, 0, w, iniHeight), "");
			balken[3] = new Image(new CB_RectF(balken[2].getMaxX() - 1, 0, w, iniHeight), "");
			balken[4] = new Image(new CB_RectF(balken[3].getMaxX() - 1, 0, w, iniHeight), "");
			balken[5] = new Image(new CB_RectF(balken[4].getMaxX() - 1, 0, w, iniHeight), "");
			balken[6] = new Image(new CB_RectF(balken[5].getMaxX() - 1, 0, w, iniHeight), "");
			balken[7] = new Image(new CB_RectF(balken[6].getMaxX() - 1, 0, w, iniHeight), "");
			balken[8] = new Image(new CB_RectF(balken[7].getMaxX() - 1, 0, w, iniHeight), "");
			balken[9] = new Image(new CB_RectF(balken[8].getMaxX() - 1, 0, w, iniHeight), "");
			balken[10] = new Image(new CB_RectF(balken[9].getMaxX() - 1, 0, w, iniHeight), "");
			balken[11] = new Image(new CB_RectF(balken[10].getMaxX() - 1, 0, w, iniHeight), "");
			if (!small) balken[12] = new Image(new CB_RectF(balken[11].getMaxX() - 1, 0, w, iniHeight), "");
			if (!small) balken[13] = new Image(new CB_RectF(balken[12].getMaxX() - 1, 0, w, iniHeight), "");

			for (Image tmp : balken)
			{
				if (tmp != null)
				{
					tmp.setDrawable(small ? SpriteCache.barSmall_0 : SpriteCache.bar_0);
					this.addChild(tmp);
				}
			}
		}

		int count = 0;
		if (GPS.getSatList() != null)
		{
			for (GpsStrength tmp : GPS.getSatList())
			{
				// balken höhe festlegen
				if (balken[count] != null)
				{
					float barHeight = Math.min((tmp.getStrength() * 3 / 100) * this.height, this.height);

					if (barHeight < minH)
					{
						barHeight = small ? SpriteCache.barSmall.getTopHeight() : SpriteCache.bar.getTopHeight();
						balken[count].setDrawable(small ? SpriteCache.barSmall_0 : SpriteCache.bar_0);
					}
					else
					{
						balken[count].setDrawable(small ? SpriteCache.barSmall : SpriteCache.bar);
					}

					balken[count].setHeight(barHeight);

					// // balken farbe festlegen
					if (tmp.getFixed())
					{
						balken[count].setColor(Color.GREEN);
					}
					else
					{
						balken[count].setColor(Color.RED);
					}
				}

				count++;
				if (count >= 13) break;
			}
		}

		// restliche balken ausschalten!
		if (count < 14)
		{
			for (int i = count; i <= 13; i++)
			{
				if (balken[i] != null) balken[i].setColor(Color.LIGHT_GRAY);
			}
		}

		redraw = false;
		GL.that.renderOnce("MeasureCoord");

	}

	private boolean redraw = true;

	@Override
	public void GpsStateChanged()
	{
		redraw = true;
		GL.that.renderOnce("GPS-State Change");
	}

	@Override
	public void dispose()
	{
		GpsStateChangeEventList.Remove(this);
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	public void onShow()
	{
		super.onShow();
		GpsStateChangeEventList.Add(this);
	}

	@Override
	public void onHide()
	{
		super.onHide();
		GpsStateChangeEventList.Remove(this);
	}
}
