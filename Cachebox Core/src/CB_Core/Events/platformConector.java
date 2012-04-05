package CB_Core.Events;

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

	public interface isOnlineListner
	{
		boolean isOnline();
	}

	private static isOnlineListner IsOnline;

	public static void setisOnlineListner(isOnlineListner listner)
	{
		IsOnline = listner;
	}

	public static boolean isOnline()
	{
		if (IsOnline == null)
		{
			return IsOnline.isOnline();
		}

		return false;
	}

}
