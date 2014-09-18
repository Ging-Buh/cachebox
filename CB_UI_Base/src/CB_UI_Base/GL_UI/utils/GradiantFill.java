package CB_UI_Base.GL_UI.utils;

import java.util.ArrayList;
import java.util.Collections;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GradiantFill
{

	static public class GradiantStop implements Comparable<GradiantStop>
	{
		public Color StopColor;
		public float Stop;

		public GradiantStop(Color stopColor, float stop)
		{
			StopColor = stopColor;
			Stop = stop;
		}

		@Override
		public int compareTo(GradiantStop stop)
		{
			float dist1 = this.Stop;
			float dist2 = stop.Stop;
			return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
		}
	}

	private class GradiantStopList extends ArrayList<GradiantStop>
	{
		private static final long serialVersionUID = -7471890376233678292L;
		private float minDistance;

		public float getMinDistance()
		{
			return minDistance;
		}

		@Override
		public boolean add(GradiantStop stop)
		{
			boolean ret = super.add(stop);

			Collections.sort(this);

			minDistance = Float.MAX_VALUE;

			float lastStop = 0;

			for (GradiantStop tmp : this)
			{
				if (tmp.Stop != 0)
				{
					minDistance = Math.min(minDistance, tmp.Stop - lastStop);
				}
				lastStop = tmp.Stop;
			}

			return ret;
		}

	}

	private float Direction;

	private TextureRegion mTextureRegion;
	private Pixmap mPixmap;
	private Texture mTexture;

	public TextureRegion getTexture()
	{
		return mTextureRegion;
	}

	public GradiantStopList stops = new GradiantStopList();

	public GradiantFill(Color color1, Color color2, float direction)
	{
		stops.add(new GradiantStop(color1, 0f));
		stops.add(new GradiantStop(color2, 1f));
		Direction = direction;

		regenarateTexture();
	}

	public void setStartColor(Color color)
	{
		stops.get(0).StopColor = color;
		regenarateTexture();
	}

	public void setEndColor(Color color)
	{
		stops.get(stops.size()).StopColor = color;
		regenarateTexture();
	}

	public void setDirection(float direction)
	{
		Direction = direction;
		regenarateTexture();
	}

	public void addStop(GradiantStop stop)
	{
		stops.add(stop);
		regenarateTexture();
	}

	private void regenarateTexture()
	{

		if (stops.size() <= 1) return;

		disposeTexture();

		ArrayList<Color> colorArray = new ArrayList<Color>();

		// calc line steps
		int lineSteps = (int) ((255 * (1 / stops.getMinDistance())) * (stops.size() - 1));

		for (int i = 0; i < stops.size() - 1; i++)
		{
			colorArray.addAll(getColorsFromStep(stops.get(i), stops.get(i + 1), lineSteps));
		}

		int w = colorArray.size();
		int h = 1;
		mPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

		int index = 0;

		for (Color color : colorArray)
		{

			mPixmap.setColor(color);
			mPixmap.drawLine(index, 0, index, 1);
			index++;
		}

		mTexture = new Texture(mPixmap);
		mPixmap.dispose();
		mPixmap = null;
		mTextureRegion = new TextureRegion(mTexture, colorArray.size(), 1);

	}

	private ArrayList<Color> getColorsFromStep(GradiantStop stop1, GradiantStop stop2, int lineSteps)
	{
		float steps = (stop2.Stop - stop1.Stop) * lineSteps;

		float R1 = stop1.StopColor.r * 255;
		float G1 = stop1.StopColor.g * 255;
		float B1 = stop1.StopColor.b * 255;
		float A1 = stop1.StopColor.a * 255;

		float R2 = stop2.StopColor.r * 255;
		float G2 = stop2.StopColor.g * 255;
		float B2 = stop2.StopColor.b * 255;
		float A2 = stop2.StopColor.a * 255;

		float R_stepValue = (R2 - R1) / steps;
		float G_stepValue = (G2 - G1) / steps;
		float B_stepValue = (B2 - B1) / steps;
		float A_stepValue = (A2 - A1) / steps;

		ArrayList<Color> list = new ArrayList<Color>();

		list.add(stop1.StopColor);

		for (int i = 0; i < steps; i++)
		{
			R1 += R_stepValue;
			G1 += G_stepValue;
			B1 += B_stepValue;
			A1 += A_stepValue;

			Color c = new Color(R1 / 255, G1 / 255, B1 / 255, A1 / 255);
			list.add(c);
		}

		return list;
	}

	public float getDirection()
	{
		return Direction;
	}

	public void dispose()
	{
		disposeTexture();
	}

	private void disposeTexture()
	{
		GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (mPixmap != null) mPixmap.dispose();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					if (mTexture != null) mTexture.dispose();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				mPixmap = null;
				mTexture = null;
				mTextureRegion = null;
			}
		});

	}

}
