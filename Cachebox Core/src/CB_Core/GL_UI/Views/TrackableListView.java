package CB_Core.GL_UI.Views;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.Math.CB_RectF;

public class TrackableListView extends CB_View_Base
{

	public TrackableListView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy TrackableListView");
		this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		platformConector.showView(ViewConst.TRACK_LIST_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void Initial()
	{
		platformConector.hideView(ViewConst.TRACK_LIST_VIEW);

	}

}
