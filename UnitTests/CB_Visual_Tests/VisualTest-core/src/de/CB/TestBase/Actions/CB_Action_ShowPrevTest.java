package de.CB.TestBase.Actions;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import de.CB.TestBase.Views.MainView;

public class CB_Action_ShowPrevTest extends CB_Action_ShowView {

	final static int AID_SHOW_PREV_TEST = 99999998;

	public CB_Action_ShowPrevTest() {
		super("Prev Test", AID_SHOW_PREV_TEST);
	}

	@Override
	public void Execute() {

		if (CB_Action_ShowNextTest.testList.size() == 0)
			return; // no more tests available
		CB_Action_ShowNextTest.actTestIndex--;

		if (CB_Action_ShowNextTest.actTestIndex < 0) {
			CB_Action_ShowNextTest.actTestIndex++;
			GL.that.Toast("No more previeus Tests");
			return;
		}

		CB_Action_ShowNextTest.actTest = CB_Action_ShowNextTest.testList.get(CB_Action_ShowNextTest.actTestIndex);

		// Disable Rotate Buttons
		MainView.that.disableRotateButton();
		CB_Action_ShowNextTest.actTest.work();

		if ((tab != null))
			tab.ShowView(CB_Action_ShowNextTest.actTest);

		// dispose last
		if (CB_Action_ShowNextTest.actTestIndex < CB_Action_ShowNextTest.testList.size() - 1)
			CB_Action_ShowNextTest.testList.get(CB_Action_ShowNextTest.actTestIndex + 1).dispose();
		System.gc();
	}

	@Override
	public CB_View_Base getView() {
		return MainView.mapView;
	}

	public int size() {
		return CB_Action_ShowNextTest.testList.size();
	}

}
