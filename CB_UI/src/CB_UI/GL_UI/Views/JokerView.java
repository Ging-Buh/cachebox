package CB_UI.GL_UI.Views;

import CB_UI.GlobalCore;
import CB_UI.Plattform;
import CB_UI.Events.platformConector;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.Fonts;
import CB_UI.GL_UI.SpriteCache;
import CB_UI.GL_UI.ViewConst;
import CB_UI.GL_UI.Controls.Label;
import CB_UI.Math.CB_RectF;

public class JokerView extends CB_View_Base
{

	public JokerView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy JokerView");
		setBackground(SpriteCache.ListBack);

		if (GlobalCore.platform == Plattform.Desktop) this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		// Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.JOKER_VIEW, this.Pos.x, this.Pos.y, this.width, this.height);
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.JOKER_VIEW);
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
