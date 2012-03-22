package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Menu.CB_AllContextMenuHandler;
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
		lblDummy.setFont(Fonts.get22());
		lblDummy.setText("Dummy AboutView");
		this.addChild(lblDummy);

		Button btn = new Button(new CB_RectF(100, 100, 100, 50), "");
		btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				CB_AllContextMenuHandler.showBtnMiscContextMenu();
				return true;
			}
		});
		this.addChild(btn);
	}

	@Override
	public void onShow()
	{
		// TODO Rufe ANDROID VIEW auf
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		lblDummy.setRec(CB_RectF.ScaleCenter(rec, 0.8f));
	}

}
