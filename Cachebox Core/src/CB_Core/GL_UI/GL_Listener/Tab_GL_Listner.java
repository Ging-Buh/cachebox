package CB_Core.GL_UI.GL_Listener;

import CB_Core.Events.KeyCodes;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.KeyEventListner;
import CB_Core.GL_UI.Main.MainViewBase;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.splash;
import CB_Core.GL_UI.libGdx_Controls.LibGdx_Host_Control;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Tab_GL_Listner extends GL_Listener
{

	public Tab_GL_Listner(int initalWidth, int initialHeight)
	{
		super(initalWidth, initialHeight);
	}

	@Override
	public void Initialize()
	{
		// Logger.LogCat("GL_Listner => Initialize TabMainView");

		if (batch == null)
		{
			batch = new SpriteBatch();
		}

		if (child == null)
		{
			// child = new TabMainView(0, 0, width, height, "TabMainView");
			child = new splash(0, 0, width, height, "SplashView");

			child.setClickable(true);
		}

		if (mDialog == null)
		{
			mDialog = new MainViewBase(0, 0, width, height, "Dialog");
			mDialog.setClickable(true);
		}

		platformConector.setKeyEventListner(new KeyEventListner()
		{

			@Override
			public boolean onKeyPressed(Character character)
			{
				if (DialogIsShown && character == KeyCodes.KEYCODE_BACK)
				{
					closeDialog();
					return true; // behandelt!
				}

				// WeiterLeiten an VirtualStage!
				return LibGdx_Host_Control.keyTyped(character);

			}

			@Override
			public boolean keyUp(int KeyCode)
			{
				return LibGdx_Host_Control.keyUp(KeyCode);
			}

			@Override
			public boolean keyDown(int keycode)
			{
				return LibGdx_Host_Control.keyDown(keycode);
			}
		});

	}

	public void switchToTabMainView()
	{
		MainViewBase altSplash = child;
		child = new TabMainView(0, 0, width, height, "TabMainView");

		altSplash.dispose();
		altSplash = null;

	}

}
