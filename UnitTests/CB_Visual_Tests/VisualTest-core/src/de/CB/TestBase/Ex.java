package de.CB.TestBase;

import CB_UI_Base.GL_UI.Main.MainViewBase;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputProcessor;


public class Ex extends CB_UI_Base.GL_UI.GL_Listener.GL implements ApplicationListener, InputProcessor
{
	public Ex(int initalWidth, int initialHeight, MainViewBase splash, MainViewBase mainView)
	{
		super(initalWidth, initialHeight, splash, mainView);

	}

	@Override
	public void create()
	{
		super.create();
	}

	@Override
	public void Initialize()
	{
		super.Initialize();
	}
}
