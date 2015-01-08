package CB_UI.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UiSizes;

public class SpoilerView extends CB_View_Base
{

	public SpoilerView(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	@Override
	public void onShow()
	{
		// Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.SPOILER_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		Timer t = new Timer();
		TimerTask tt = new TimerTask()
		{
			@Override
			public void run()
			{
				SpoilerView.this.onResized(SpoilerView.this);
			}
		};
		t.schedule(tt, 70);
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		float infoHeight = -(UiSizes.that.getInfoSliderHeight());
		CB_RectF world = this.getWorldRec();
		platformConector.setContentSize((int) world.getX(), (int) ((GL_UISizes.SurfaceSize.getHeight() - world.getMaxY() + infoHeight)), (int) (GL_UISizes.SurfaceSize.getWidth() - world.getMaxX()), (int) world.getY());
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.SPOILER_VIEW);
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

}
