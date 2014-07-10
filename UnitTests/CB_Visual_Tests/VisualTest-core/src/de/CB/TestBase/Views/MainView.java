package de.CB.TestBase.Views;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.CB_ActionButton;
import CB_UI_Base.GL_UI.Main.CB_Button;
import CB_UI_Base.GL_UI.Main.CB_ButtonList;
import CB_UI_Base.GL_UI.Main.CB_TabView;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_Utils.Config_Core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;

import de.CB.TestBase.Settings;
import de.CB.TestBase.Actions.CB_Action_ShowMap;
import de.CB.TestBase.Actions.CB_Action_ShowNextTest;
import de.CB.TestBase.Actions.CB_Action_ShowPrevTest;
import de.CB.TestBase.Actions.MultiTestList;
import de.CB.TestBase.Res.ResourceCache;

/**
 * @author Longri
 */
public class MainView extends MainViewBase
{

	public static MainView that;
	public static String actTheme = "";
	public static CB_TabView TAB;

	public static MapView mapView;

	public static CB_Action_ShowMap actionShowMap = new CB_Action_ShowMap();

	public static CB_Action_ShowNextTest actionShowNextTest = new CB_Action_ShowNextTest();
	public static CB_Action_ShowPrevTest actionShowPrevTest = new CB_Action_ShowPrevTest();

	private final boolean NextClicked = false;

	static CB_Button btn1;
	static CB_Button btn2;
	static CB_Button btn5;
	static CB_Button btn6;
	static CB_Button btn7;

	// ######## Button Actions ###########

	// ######## Views ###########

	public static float Width()
	{
		if (that != null) return that.getWidth();
		return 0;
	}

	public static float Height()
	{
		if (that != null) return that.getHeight();
		return 0;
	}

	public MainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		that = this;
		mainView = this;
	}

	@Override
	public void Initial()
	{
		// Override default Settings and load only Mapsforge Maps
		{
			Settings.MapPackFolder.ForceDefaultChange(Config_Core.WorkPath + "/maps");

			ManagerBase.Manager.initialMapPacks();

			ArrayList<Layer> MapsforgeOnlyList = new ArrayList<Layer>();

			for (Layer layer : ManagerBase.Manager.getLayers())
			{
				if (layer.isMapsForge) MapsforgeOnlyList.add(layer);
			}

			ManagerBase.Manager.getLayers().clear();

			for (Layer layer : MapsforgeOnlyList)
			{
				ManagerBase.Manager.getLayers().add(layer);
			}
		}

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight());
		rec.setPos(0, 0);

		TAB = new ThisTabView(rec, "Phone Tab");

		btn1 = new CB_Button(btnRec, "Button1", ResourceCache.btnSpritesHome);
		btn2 = new CB_Button(btnRec, "Button2", ResourceCache.btnSpritesHome);
		btn5 = new CB_Button(btnRec, "Button5", ResourceCache.btnSpritesHome);
		btn6 = new CB_Button(btnRec, "Button5", ResourceCache.btnSpritesHome);
		btn7 = new CB_Button(btnRec, "Button5", ResourceCache.btnSpritesHome);

		// set Button Overlays
		{
			btn1.setText("PREV");
			btn2.setText("NEXT");
			btn5.setText("EXTIT");
			btn6.setText("CCW");
			btn7.setText("CW");

			// btn1.addOverlayDrawable(ResourceCache.getSpriteDrawable("LoadMap"));
			// btn2.addOverlayDrawable(ResourceCache.getSpriteDrawable("LoadTheme"));
			// btn3.addOverlayDrawable(ResourceCache.getSpriteDrawable("ConfigTheme"));
			// btn4.addOverlayDrawable(ResourceCache.getSpriteDrawable("SaveTheme"));
			// btn5.addOverlayDrawable(ResourceCache.getSpriteDrawable("misc"));

		}

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		// btnList.addButton(btn3);
		// btnList.addButton(btn4);
		btnList.addButton(btn5);
		btnList.addButton(btn6);
		btnList.addButton(btn7);

		disableRotateButton();

		TAB.addButtonList(btnList);

		this.addChild(TAB);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, TAB);
		actionShowNextTest.setTab(this, TAB);
		actionShowPrevTest.setTab(this, TAB);
		// override App Name
		actionClose.OverrideAppName("Graphic-Test");

		btn1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base arg0, int arg1, int arg2, int arg3, int arg4)
			{
				actionShowPrevTest.Execute();
				return true;
			}
		});

		btn2.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base arg0, int arg1, int arg2, int arg3, int arg4)
			{
				actionShowNextTest.Execute();
				return true;
			}
		});

		// btn3.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public boolean onClick(GL_View_Base arg0, int arg1, int arg2, int arg3, int arg4)
		// {
		// if (Global.editor == null)
		// {
		// GL_MsgBox.Show("No Theme Loaded", "Error", MessageBoxIcon.Error);
		// return true;
		// }
		//
		// new ConfigListActivity().show();
		//
		// return true;
		// }
		// });

		// btn4.setOnClickListener(new OnClickListener()
		// {
		//
		// @Override
		// public boolean onClick(GL_View_Base arg0, int arg1, int arg2, int arg3, int arg4)
		// {
		// if (Global.editor != null)
		// {
		// try
		//
		// {
		// Global.editor.save();
		// }
		// catch (IOException e)
		// {
		// GL_MsgBox.Show("Error with saving Theme" + Global.br + e.getMessage(), "Error", MessageBoxIcon.Error);
		// return true;
		// }
		//
		// GL_MsgBox.Show("Theme saved!", null, MessageBoxIcon.None);
		//
		// }
		// else
		// {
		// GL_MsgBox.Show("No Theme loaded!", "Error", MessageBoxIcon.Error);
		// }
		// return true;
		// }
		// });

		btn5.addAction(new CB_ActionButton(actionClose, false));

		// actionShowMap.Execute();
		actionShowNextTest.Execute();

	}

	public void disableRotateButton()
	{
		btn6.disable();
		btn7.disable();
	}

	public void enableRotateButton(OnClickListener listner)
	{

		btn6.setOnClickListener(listner);
		btn7.setOnClickListener(listner);
		btn6.enable();
		btn7.enable();
	}

	static class ThisTabView extends CB_TabView
	{

		public ThisTabView(CB_RectF rec, String Name)
		{
			super(rec, Name);
		}

		@Override
		public void ShowView(CB_View_Base view)
		{
			super.ShowView(view);

			Timer timer = new Timer();
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					GL.that.RunOnGL(new IRunOnGL()
					{

						@Override
						public void run()
						{

							btn1.setText("PREV");
							btn2.setText("NEXT");
							btn5.setText("EXTIT");
						}
					});
				}
			};
			timer.schedule(task, 200);

		}

	}

	private boolean vorward = true;

	@Override
	public void renderChilds(Batch batch, ParentInfo parentInfo)
	{

		GL_View_Base.disableScissor=true;
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

		GL.that.renderOnce();
		super.renderChilds(batch, parentInfo);

		if (actionShowNextTest.size() > 0)
		{
			if (CB_Action_ShowNextTest.actTest == null)
			{
				actionShowNextTest.CallExecute();
				return;
			}
			if (!wait && CB_Action_ShowNextTest.actTest.getIsReady())
			{
				if (MultiTestList.FastTest)
				{
					if (vorward)
					{
						if (CB_Action_ShowNextTest.actTestIndex < CB_Action_ShowNextTest.testList.size() - 1)
						{
							callNext();
						}
						else
						{
							vorward = !vorward;
							callPrev();
						}

					}
					else
					{
						if (CB_Action_ShowNextTest.actTestIndex > 0)
						{
							callPrev();
						}
						else
						{
							vorward = !vorward;
							callNext();
						}
					}
				}
			}
		}
//		batch.flush();
	}

	private boolean wait = false;

	private void callNext()
	{
		if (wait) return;
		wait = true;
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				GL.that.RunOnGL(new IRunOnGL()
				{

					@Override
					public void run()
					{
						actionShowNextTest.CallExecute();
						wait = false;
					}
				});
			}
		};
		timer.schedule(task, 10000);

	}

	private void callPrev()
	{
		if (wait) return;
		wait = true;
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				GL.that.RunOnGL(new IRunOnGL()
				{

					@Override
					public void run()
					{
						actionShowPrevTest.CallExecute();
						wait = false;
					}
				});
			}
		};
		timer.schedule(task, 10000);
	}

}
