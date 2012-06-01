package CB_Core.Events;

import CB_Core.GL_UI.MenuID;
import CB_Core.GL_UI.ViewID;

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

		void menuItemClicked(MenuID ID);

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

	public static void menuItemClicked(MenuID ID)
	{
		if (showViewListner != null)
		{
			showViewListner.menuItemClicked(ID);
		}
	}

	public interface IHardwarStateListner
	{
		boolean isOnline();

		boolean isGPSon();
	}

	private static IHardwarStateListner hardwareListner;

	public static void setisOnlineListner(IHardwarStateListner listner)
	{
		hardwareListner = listner;
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

	public static interface trackListListner
	{
		public String[] getTracks();
	}

	private static trackListListner mTrackListner;

	public static void setGetTrackListner(trackListListner listner)
	{
		mTrackListner = listner;
	}

	public static String[] getTrackList()
	{
		if (mTrackListner != null) return mTrackListner.getTracks();
		return null;
	}

}
