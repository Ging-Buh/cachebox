package CB_UI.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_UI.GlobalCore;
import CB_UI_Base.Plattform;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.Math.CB_RectF;

public class DescriptionView extends CB_View_Base
{

	public DescriptionView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy DescriptionView");
		setBackground(SpriteCacheBase.ListBack);

		if (GlobalCore.platform == Plattform.Desktop) this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		// Rufe ANDROID VIEW auf
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				platformConector.showView(ViewConst.DESCRIPTION_VIEW, DescriptionView.this.Pos.x, DescriptionView.this.Pos.y,
						DescriptionView.this.width, DescriptionView.this.height);
			}
		};
		timer.schedule(task, 100);

	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.DESCRIPTION_VIEW);
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
