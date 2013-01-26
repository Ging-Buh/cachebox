package CB_Core.GL_UI.GL_Listener;

import CB_Core.Config;
import CB_Core.Events.KeyCodes;
import CB_Core.Events.platformConector;
import CB_Core.Events.platformConector.KeyEventListner;
import CB_Core.GL_UI.Main.MainViewBase;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.splash;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Tab_GL_Listner extends GL
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
			if (Config.settings.DebugSpriteBatchCountBuffer.getValue())
			{
				// for Debug set to max!
				batch = new SpriteBatch(10000);
			}
			else
			{
				batch = new SpriteBatch(SPRITE_BATCH_BUFFER);
			}
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

		if (mActivity == null)
		{
			mActivity = new MainViewBase(0, 0, width, height, "Dialog");
			mActivity.setClickable(true);
		}

		platformConector.setKeyEventListner(new KeyEventListner()
		{

			@Override
			public boolean onKeyPressed(Character character)
			{
				if (DialogIsShown && character == KeyCodes.KEYCODE_BACK)
				{
					closeDialog(mDialog);
					return true; // behandelt!
				}

				if (ActivityIsShown && character == KeyCodes.KEYCODE_BACK)
				{
					closeActivity();
					return true; // behandelt!
				}

				// WeiterLeiten an EditTextView, welches den Focus Hat
				if (keyboardFocus != null && keyboardFocus.keyTyped(character)) return true;

				return false;

			}

			@Override
			public boolean keyUp(int KeyCode)
			{
				// WeiterLeiten an EditTextView, welches den Focus Hat
				if (keyboardFocus != null && keyboardFocus.keyUp(KeyCode)) return true;
				return false;
			}

			@Override
			public boolean keyDown(int keycode)
			{
				// WeiterLeiten an EditTextView, welches den Focus Hat
				if (keyboardFocus != null && keyboardFocus.keyDown(keycode)) return true;
				return false;
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
