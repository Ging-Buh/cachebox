package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.Views.TestViews.Test_H_ListView;
import CB_Core.GL_UI.Views.TestViews.Test_V_ListView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

public class TabMainView extends MainViewBase
{

	public TabMainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

		addTestListViews();

		addPhoneTab();

	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	private void addPhoneTab()
	{
		// nur ein Tab

		CB_RectF rec = this.copy();
		rec.setWidth(460);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		// mit fünf Buttons

		CB_Button btn1 = new CB_Button(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), "Button1");
		CB_Button btn2 = new CB_Button(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), "Button2");
		CB_Button btn3 = new CB_Button(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), "Button3");
		CB_Button btn4 = new CB_Button(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), "Button4");
		CB_Button btn5 = new CB_Button(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), "Button5");

		CB_Button btn6 = new CB_Button(new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight), "Test 6");

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);
		btnList.addButton(btn6);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

	}

	private void addTabletTabs()
	{
		// zwei Tabs

	}

	/**
	 * nur zum Testen der ListViews. kann Zeitnah gelöscht werden!
	 */
	private void addTestListViews()
	{
		Test_V_ListView tlV = new Test_V_ListView(new CB_RectF(1000, 0, 100, 600), "test V");
		this.addChild(tlV);

		Test_H_ListView tlH = new Test_H_ListView(new CB_RectF(400, 625, 600, 100), "test H");
		this.addChild(tlH);
	}

}
