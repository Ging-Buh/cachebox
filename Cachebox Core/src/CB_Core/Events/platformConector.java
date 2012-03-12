package CB_Core.Events;

import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.MessageBox.MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MsgBox.OnClickListener;

public class platformConector
{
	/**
	 * Interface definition for a callback to be invoked when a platform must show a massage.
	 */
	public interface OnShowMessageListner
	{
		void MsgShow(String msg);

		void MsgShow(String msg, OnClickListener listener);

		void MsgShow(String msg, String title, OnClickListener listener);

		void MsgShow(String msg, String title, MessageBoxButtons buttons, OnClickListener listener);

		void MsgShow(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnClickListener listener);
	}

	private static OnShowMessageListner massageShow;

	public static void setOnShowMassageListneer(OnShowMessageListner listner)
	{
		massageShow = listner;
	}

	public static class Msg
	{

		public static void Show(String msg)
		{
			if (massageShow != null)
			{
				massageShow.MsgShow(msg, null);
			}

		}

		public static void Show(String msg, MsgBox.OnClickListener listener)
		{
			if (massageShow != null)
			{
				massageShow.MsgShow(msg, listener);
			}
		}

		public static void Show(String msg, String title, OnClickListener listener)
		{
			if (massageShow != null)
			{
				massageShow.MsgShow(msg, title, listener);
			}
		}

		public static void Show(String msg, String title, MessageBoxButtons buttons, OnClickListener listener)
		{
			if (massageShow != null)
			{
				massageShow.MsgShow(msg, title, buttons, listener);
			}
		}

		public static void Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnClickListener listener)
		{
			if (massageShow != null)
			{
				massageShow.MsgShow(msg, title, buttons, icon, listener);
			}
		}

	}

}
