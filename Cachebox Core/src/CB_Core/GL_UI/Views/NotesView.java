package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.Math.CB_RectF;

public class NotesView extends CB_View_Base
{

	public NotesView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.get22());
		lblDummy.setText("Dummy NotesView");
		this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		// TODO Rufe ANDROID VIEW auf
	}
}
