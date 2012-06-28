package CB_Core.GL_UI.Controls;

import CB_Core.Events.GpsStateChangeEvent;
import CB_Core.Events.GpsStateChangeEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Locator.GPS;
import CB_Core.Locator.GpsStatus;
import CB_Core.Locator.GpsStrength;
import CB_Core.Math.CB_Rect;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SatBarChart extends CB_View_Base implements GpsStateChangeEvent
{

	private GpsStatus mGpsStatus;

	public SatBarChart(CB_RectF rec, String Name)
	{
		super(rec, Name);

		GpsStateChangeEventList.Add(this);
	}

	public void setGpsStatus(GpsStatus status)
	{
		mGpsStatus = status;
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (drawing != null) drawing.draw(batch);

		if (redraw) setSatStrength();

	}

	Sprite drawing;

	private void setSatStrength()
	{
		CB_Rect[] balken = null;
		if (balken == null)
		{
			int w = (int) (this.width / 10);

			balken = new CB_Rect[10];
			balken[0] = new CB_Rect(0, 0, w, 10);
			balken[1] = new CB_Rect(balken[0].getMaxX() + 3, 0, w, 10);
			balken[2] = new CB_Rect(balken[1].getMaxX() + 3, 0, w, 10);
			balken[3] = new CB_Rect(balken[2].getMaxX() + 3, 0, w, 10);
			balken[4] = new CB_Rect(balken[3].getMaxX() + 3, 0, w, 10);
			balken[5] = new CB_Rect(balken[4].getMaxX() + 3, 0, w, 10);
			balken[6] = new CB_Rect(balken[5].getMaxX() + 3, 0, w, 10);
			balken[7] = new CB_Rect(balken[6].getMaxX() + 3, 0, w, 10);
			balken[8] = new CB_Rect(balken[7].getMaxX() + 3, 0, w, 10);
			balken[9] = new CB_Rect(balken[8].getMaxX() + 3, 0, w, 10);
		}

		int w = getNextHighestPO2((int) this.getWidth());
		int h = getNextHighestPO2((int) this.getHeight());
		Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);

		int count = 0;
		if (GPS.getSatList() != null)
		{
			for (GpsStrength tmp : GPS.getSatList())
			{

				// balken höhe festlegen

				balken[count].setHeight((int) (this.height * (tmp.getStrength() / 30)));

				// // balken farbe festlegen
				if (tmp.getFixed())
				{
					p.setColor(Color.GREEN);
				}
				else
				{
					p.setColor(Color.GRAY);
				}

				p.fillRectangle(balken[count].getX(), balken[count].getY(), balken[count].getWidth(), balken[count].getHeight());

				count++;
				if (count >= 9) break;
			}
		}

		// restliche balken ausschalten!
		if (count < 10)
		{
			for (int i = count; i <= 9; i++)
			{
				p.setColor(Color.LIGHT_GRAY);

				p.fillRectangle(balken[i].getX(), balken[i].getY(), balken[i].getWidth(), 5);
			}
		}

		Texture tex = new Texture(p);

		drawing = new Sprite(tex, (int) this.getWidth(), (int) this.getHeight());
		drawing.setX(0);
		drawing.setY(0);
		drawing.flip(false, true);
		p.dispose();
		redraw = false;
		GL_Listener.glListener.renderOnce("MesureCoord");

	}

	private boolean redraw = true;

	@Override
	public void GpsStateChanged()
	{
		redraw = true;
	}

	@Override
	public void dispose()
	{
		GpsStateChangeEventList.Remove(this);
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
