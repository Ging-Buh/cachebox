package de.cachebox_test.Views;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.GL_Listener.GL_Listener_Interface;
import CB_Core.Log.Logger;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;

import de.cachebox_test.R;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.AllContextMenuCallHandler;

public class ViewGL extends RelativeLayout implements ViewOptionsMenu, GL_Listener_Interface
{
	public GL_Listener glListener;

	public static View ViewGl;

	public ViewGL(Context context, LayoutInflater inflater, View glView, GL_Listener glListener)
	{
		super(context);
		ViewGl = glView;
		GL_Listener.listenerInterface = this;
		this.glListener = glListener;
		try
		{

			RelativeLayout mapviewLayout = (RelativeLayout) inflater.inflate(R.layout.mapviewgl, null, false);
			this.addView(mapviewLayout);

			mapviewLayout.addView(glView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
		catch (Exception ex)
		{
			int i = 0;
			i++;
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 0:
			return true;

		}
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
		AllContextMenuCallHandler.showMapViewGLContextMenu();
	}

	@Override
	public int GetMenuId()
	{
		return 0;
	}

	@Override
	public void OnShow()
	{
		Logger.DEBUG("OnShow");
		glListener.onStart();
		// GL_Listener.onStart();
	}

	@Override
	public void OnHide()
	{
		Logger.DEBUG("OnHide");
		glListener.onStop();
		// GL_Listner.onStop();
	}

	@Override
	public void OnFree()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int GetContextMenuId()
	{
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{
		return false;
	}

	public void Initialize()
	{
		glListener.Initialize();
	}

	public void InitializeMap()
	{
		// glListener.InitializeMap();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float ex = event.getX();
		float ey = event.getY();
		// Weitergabe der Toucheingabe an den Gl_Listener
		// ToDo: noch nicht fertig!!!!!!!!!!!!!
		glListener.onTouchDown((int) ex, (int) ey, 0, 0);

		return true;
	}

	// public Layer GetCurrentLayer()
	// {
	// return glListener.CurrentLayer;
	// }

	@Override
	public void RequestRender()
	{
		((GLSurfaceViewCupcake) ViewGl).requestRender();
	}

	@Override
	public void RenderDirty()
	{
		Logger.DEBUG("Set: RenderDirty");
		try
		{
			((GLSurfaceViewCupcake) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_WHEN_DIRTY);
		}
		catch (Exception ex)
		{
			String s = ex.getMessage();
		}
	}

	@Override
	public void RenderContinous()
	{
		Logger.DEBUG("Set: RenderContinous");
		((GLSurfaceViewCupcake) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
	}
}
