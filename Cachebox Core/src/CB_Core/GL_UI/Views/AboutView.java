package CB_Core.GL_UI.Views;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.Math.CB_RectF;

public class AboutView extends CB_View_Base
{
	Label lblDummy;

	AboutView Me;

	public AboutView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		Me = this;
		lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy AboutView");
		this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		// TODO Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.ABOUT_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.ABOUT_VIEW);
	}

	@Override
	protected void Initial()
	{
		this.setBackground(SpriteCache.AboutBack);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		lblDummy.setRec(CB_RectF.ScaleCenter(rec, 0.8f));
	}

}
