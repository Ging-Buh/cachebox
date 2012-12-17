package CB_Core.Events;

import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.GpsStatus;

public class platformConector
{
	/**
	 * Interface definition for a callback to be invoked when a platform must show a view.
	 */
	public interface IShowViewListner
	{
		void show(ViewID viewID, int x, int y, int width, int height);

		void hide(ViewID viewID);

		void showForDialog();

		void hideForDialog();

		void dayNightSwitched();

		void firstShow();
	}

	private static IShowViewListner showViewListner;

	public static void setShowViewListner(IShowViewListner listner)
	{
		showViewListner = listner;
	}

	public static void showView(ViewID viewID, float x, float y, float width, float height)
	{
		if (showViewListner != null)
		{
			GL.that.clearRenderViews();
			showViewListner.show(viewID, (int) x, (int) y, (int) width, (int) height);
		}
	}

	public static void FirstShow()
	{
		if (showViewListner != null)
		{
			showViewListner.firstShow();
		}
	}

	public static void DayNightSwitched()
	{
		if (showViewListner != null)
		{
			showViewListner.dayNightSwitched();
		}
	}

	public static void hideView(ViewID viewID)
	{
		if (showViewListner != null)
		{
			showViewListner.hide(viewID);
		}
	}

	public static void showForDialog()
	{
		if (showViewListner != null)
		{
			GL.that.clearRenderViews();
			showViewListner.showForDialog();
		}
	}

	public static void hideForDialog()
	{
		if (showViewListner != null)
		{
			showViewListner.hideForDialog();
		}
	}

	public interface IHardwarStateListner
	{
		boolean isOnline();

		boolean isGPSon();

		void vibrate();

		float getCompassHeading();

		GpsStatus getGpsStatus();
	}

	private static IHardwarStateListner hardwareListner;

	public static void setisOnlineListner(IHardwarStateListner listner)
	{
		hardwareListner = listner;
	}

	public static void vibrate()
	{
		if (hardwareListner != null)
		{
			hardwareListner.vibrate();
		}

	}

	public static boolean isOnline()
	{
		if (hardwareListner != null)
		{
			return hardwareListner.isOnline();
		}

		return false;
	}

	public static boolean isGPSon()
	{
		if (hardwareListner != null)
		{
			return hardwareListner.isGPSon();
		}

		return false;
	}

	public static GpsStatus getGpsStatus()
	{
		if (hardwareListner != null)
		{
			return hardwareListner.getGpsStatus();
		}

		return null;
	}

	public static float getCompassHeading()
	{
		if (hardwareListner != null)
		{
			return hardwareListner.getCompassHeading();
		}

		return -1;
	}

	private static KeyEventListner mKeyListner;

	public interface KeyEventListner
	{
		public boolean onKeyPressed(Character character);

		public boolean keyUp(int KeyCode);

		public boolean keyDown(int keycode);
	}

	public static void setKeyEventListner(KeyEventListner listner)
	{
		mKeyListner = listner;
	}

	public static boolean sendKey(Character character)
	{
		if (mKeyListner != null)
		{
			return mKeyListner.onKeyPressed(character);
		}

		return false;
	}

	public static boolean sendKeyDown(int KeyCode)
	{
		if (mKeyListner != null)
		{
			return mKeyListner.keyDown(KeyCode);
		}

		return false;
	}

	public static boolean sendKeyUp(int KeyCode)
	{
		if (mKeyListner != null)
		{
			return mKeyListner.keyUp(KeyCode);
		}

		return false;
	}

	// ------ get File from Fiele Dialog ------

	public interface IgetFileReturnListner
	{
		public void getFieleReturn(String Path);
	}

	public interface IgetFileListner
	{
		public void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListner returnListner);
	}

	private static IgetFileListner getFileListner;

	public static void setGetFileListner(IgetFileListner listner)
	{
		getFileListner = listner;
	}

	public static void getFile(String initialPath, String extension, String TitleText, String ButtonText,
			IgetFileReturnListner returnListner)
	{
		if (getFileListner != null) getFileListner.getFile(initialPath, extension, TitleText, ButtonText, returnListner);
	}

	// ----------------------------------------

	// ------ get folder from Folder Dialog ------

	public interface IgetFolderReturnListner
	{
		public void getFolderReturn(String Path);
	}

	public interface IgetFolderListner
	{
		public void getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner);
	}

	private static IgetFolderListner getfolderListner;

	public static void setGetFolderListner(IgetFolderListner listner)
	{
		getfolderListner = listner;
	}

	public static void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner)
	{
		if (getfolderListner != null) getfolderListner.getfolder(initialPath, TitleText, ButtonText, returnListner);
	}

	// ----------------------------------------

	// ------ Quitt ------

	public interface IQuit
	{
		public void Quit();
	}

	static IQuit quitListner;

	public static void setQuitListner(IQuit listner)
	{
		quitListner = listner;
	}

	public static void callQuitt()
	{
		if (quitListner != null) quitListner.Quit();
	}

	// ----------------------------------------

	// ------ GetApiKey ------

	public interface IGetApiKey
	{
		public void GetApiKey();
	}

	static IGetApiKey GetApiKeyListner;

	public static void setGetApiKeyListner(IGetApiKey listner)
	{
		GetApiKeyListner = listner;
	}

	public static void callGetApiKeyt()
	{
		if (GetApiKeyListner != null) GetApiKeyListner.GetApiKey();
	}

	// ----------------------------------------

	// ------ setScreenLockTime ------

	public interface IsetScreenLockTime
	{
		public void setScreenLockTime(int value);
	}

	static IsetScreenLockTime setScreenLockTimeListner;

	public static void setsetScreenLockTimeListner(IsetScreenLockTime listner)
	{
		setScreenLockTimeListner = listner;
	}

	public static void callsetScreenLockTimet(int value)
	{
		if (setScreenLockTimeListner != null) setScreenLockTimeListner.setScreenLockTime(value);
	}

	// ----------------------------------------

	// ------ setKeybordFocus ------

	public interface IsetKeybordFocus
	{
		public void setKeybordFocus(boolean value);
	}

	static IsetKeybordFocus setKeybordFocusListner;

	public static void setsetKeybordFocusListner(IsetKeybordFocus listner)
	{
		setKeybordFocusListner = listner;
	}

	public static void callsetKeybordFocus(boolean value)
	{
		if (setKeybordFocusListner != null) setKeybordFocusListner.setKeybordFocus(value);
	}
	// ----------------------------------------

}
