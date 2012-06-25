package de.cachebox_test.Views;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.GL_Listener.GL_Listener_Interface;
import CB_Core.Log.Logger;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.surfaceview.DefaultGLSurfaceView;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewCupcake;

import de.cachebox_test.R;
import de.cachebox_test.Events.ViewOptionsMenu;

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
		// GL_Listener.onStart();
	}

	@Override
	public void OnHide()
	{
		Logger.DEBUG("OnHide");
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

	// @Override
	// public boolean onTouchEvent(MotionEvent event)
	// {
	// final float ex = event.getX();
	// final float ey = event.getY();
	// // Weitergabe der Toucheingabe an den Gl_Listener
	// // ToDo: noch nicht fertig!!!!!!!!!!!!!
	// Thread thread = new Thread(new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// glListener.onTouchDown((int) ex, (int) ey, 0, 0);
	// }
	// });
	// thread.run();
	//
	// return true;
	// }

	public final static int GLSURFACE_VIEW20 = 0;
	public final static int GLSURFACE_CUPCAKE = 1;
	public final static int GLSURFACE_DEFAULT = 2;
	public final static int GLSURFACE_GLSURFACE = 3;

	private static int mAktSurfaceType = -1;

	/**
	 * Setzt den OpenGl Surface Type!
	 * 
	 * @param Type
	 *            </br> GLSURFACE_VIEW20=0 </br> GLSURFACE_CUPCAKE = 1 </br> GLSURFACE_DEFAULT = 2 </br> GLSURFACE_GLSURFACE = 3
	 */
	public static void setSurfaceType(int Type)
	{
		mAktSurfaceType = Type;
	}

	public static int getSurfaceType()
	{
		return mAktSurfaceType;
	}

	@Override
	public void RequestRender(String requestName)
	{

		// Logger.LogCat("RequestRender von : " + requestName);

		switch (mAktSurfaceType)
		{
		case GLSURFACE_VIEW20:
			((GLSurfaceView20) ViewGl).requestRender();
			break;
		case GLSURFACE_CUPCAKE:
			((GLSurfaceViewCupcake) ViewGl).requestRender();
			break;
		case GLSURFACE_DEFAULT:
			((DefaultGLSurfaceView) ViewGl).requestRender();
			break;
		case GLSURFACE_GLSURFACE:
			((GLSurfaceView) ViewGl).requestRender();
			break;
		}
	}

	@Override
	public void RenderDirty()
	{
		Logger.LogCat("Set: RenderDirty");
		try
		{
			switch (mAktSurfaceType)
			{
			case GLSURFACE_VIEW20:
				((GLSurfaceView20) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_WHEN_DIRTY);
				break;
			case GLSURFACE_CUPCAKE:
				((GLSurfaceViewCupcake) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_WHEN_DIRTY);
				break;
			case GLSURFACE_DEFAULT:
				((DefaultGLSurfaceView) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_WHEN_DIRTY);
				break;
			case GLSURFACE_GLSURFACE:
				((GLSurfaceView) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_WHEN_DIRTY);
				break;
			}
		}
		catch (Exception ex)
		{
			String s = ex.getMessage();
		}
	}

	@Override
	public void RenderContinous()
	{
		Logger.LogCat("Set: RenderContinous");
		switch (mAktSurfaceType)
		{
		case GLSURFACE_VIEW20:
			((GLSurfaceView20) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
			break;
		case GLSURFACE_CUPCAKE:
			((GLSurfaceViewCupcake) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
			break;
		case GLSURFACE_DEFAULT:
			((DefaultGLSurfaceView) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
			break;
		case GLSURFACE_GLSURFACE:
			((GLSurfaceView) ViewGl).setRenderMode(GLSurfaceViewCupcake.RENDERMODE_CONTINUOUSLY);
			break;
		}
	}
}
