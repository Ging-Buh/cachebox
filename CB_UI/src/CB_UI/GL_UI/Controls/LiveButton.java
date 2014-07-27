package CB_UI.GL_UI.Controls;

import CB_Core.Api.LiveMapQue;
import CB_Core.Api.LiveMapQue.QueStateChanged;
import CB_Locator.Coordinate;
import CB_UI.Config;
import CB_UI.GL_UI.SpriteCache;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.GL_Listener.GL;

import com.badlogic.gdx.graphics.g2d.Batch;

public class LiveButton extends ImageButton implements QueStateChanged
{

	private static final int Duration = 2000;
	private static final int Frames = 8;
	private boolean state = false;
	private int Animation;

	public LiveButton()
	{
		super("");
		this.name = "LiveButton";
		this.setClickable(true);
	}

	@Override
	public void Initial()
	{
		drawableNormal = null;
		drawablePressed = null;
		drawableDisabled = null;
		drawableFocused = null;
	}

	public void setState(boolean newState)
	{
		state = newState;
		Config.LiveMapEnabeld.setValue(newState);
		Config.AcceptChanges();
		switchImage();
	}

	private void switchImage()
	{
		if (state)
		{
			if (LiveMapQue.DownloadIsActive)
			{
				try
				{
					this.setImage(SpriteCache.LiveBtn.get(1 + Animation));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				this.setImage(SpriteCache.LiveBtn.get(0));
			}
		}
		else
		{
			this.setImage(SpriteCache.LiveBtn.get(1));
		}
		GL.that.renderOnce();
	}

	private int lastAnimation = 0;

	@Override
	public void render(Batch batch)
	{
		Animation = (1 + ((int) (GL.that.getStateTime() * 1000) % Duration) / (Duration / Frames));
		if (lastAnimation != Animation)
		{
			lastAnimation = Animation;
			switchImage();
		}
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{

		setState(!state);
		if (state)
		{
			if (MapView.that != null)
			{
				Coordinate center = MapView.that.center;
				LiveMapQue.quePosition(center);
			}
		}
		return true;
	}

	@Override
	public void stateChanged()
	{
		switchImage();
		if (state)
		{
			if (LiveMapQue.DownloadIsActive)
			{
				GL.that.addRenderView(this, GL.FRAME_RATE_ACTION);
			}
			else
			{
				GL.that.removeRenderView(this);
			}
		}
		GL.that.renderOnce();
	}
}
