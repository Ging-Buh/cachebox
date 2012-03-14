package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.Views.TestViews.Test_H_ListView;
import CB_Core.GL_UI.Views.TestViews.Test_V_ListView;
import CB_Core.Math.CB_RectF;

public class TabMainView extends MainViewBase
{

	public TabMainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		Test_V_ListView tlV = new Test_V_ListView(new CB_RectF(0, 0, 400, 600), "test V");
		this.addChild(tlV);

		Test_H_ListView tlH = new Test_H_ListView(new CB_RectF(100, 625, 1000, 100), "test H");
		this.addChild(tlH);
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
