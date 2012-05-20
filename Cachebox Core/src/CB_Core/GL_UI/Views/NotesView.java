package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.Math.CB_RectF;

public class NotesView extends CB_View_Base
{

	public NotesView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy NotesView");
		setBackground(SpriteCache.ListBack);

		if (GlobalCore.platform == Plattform.Desktop) this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		// TODO Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.NOTES_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}
}
