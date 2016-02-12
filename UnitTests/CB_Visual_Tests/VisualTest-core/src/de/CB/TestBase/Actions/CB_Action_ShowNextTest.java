package de.CB.TestBase.Actions;

import java.util.ArrayList;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import de.CB.TestBase.Views.MainView;

public class CB_Action_ShowNextTest extends CB_Action_ShowView {

	final static int AID_SHOW_NEXT_TEST = 99999999;
	public static TestCaseBase actTest;
	public final static ArrayList<TestCaseBase> testList = new ArrayList<TestCaseBase>();

	public CB_Action_ShowNextTest() {
		super("Next Test", AID_SHOW_NEXT_TEST);
		testList.addAll(MultiTestList.INSTANCE);
	}

	public static int actTestIndex = -1;

	@Override
	public void Execute() {

		if (testList.size() == 0)
			return; // no more tests available
		actTestIndex++;

		if (actTestIndex > testList.size() - 1) {
			actTestIndex--;
			GL.that.Toast("No more Tests");
			return;
		}

		actTest = testList.get(actTestIndex);

		// Disable Rotate Buttons
		MainView.that.disableRotateButton();
		actTest.work();

		if ((tab != null))
			tab.ShowView(actTest);

		// dispose last
		if (actTestIndex > 0)
			testList.get(actTestIndex - 1).dispose();
		System.gc();
	}

	@Override
	public CB_View_Base getView() {
		return MainView.mapView;
	}

	public int size() {
		return testList.size();
	}

}
