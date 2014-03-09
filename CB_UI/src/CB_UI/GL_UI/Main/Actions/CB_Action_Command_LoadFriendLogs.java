package CB_UI.GL_UI.Main.Actions;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Views.LogView;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_Utils.Events.ProgresssChangedEventList;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Command_LoadFriendLogs extends CB_ActionCommand
{

	public CB_Action_Command_LoadFriendLogs()
	{
		super("LoadLogs", MenuID.AID_LOADLOGS);

	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal());
	}

	private CancelWaitDialog pd;

	@Override
	public void Execute()
	{
		// pd = ProgressDialog.Show(Translation.Get("LoadLogs"), DownloadAnimation.GetINSTANCE(), ChkStatRunnable);
		pd = CancelWaitDialog.ShowWait(Translation.Get("LoadLogs"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
		{

			@Override
			public void isCanceld()
			{
				cancelThread = true;
			}
		}, ChkStatRunnable);
	}

	int ChangedCount = 0;
	int result = 0;
	boolean cancelThread = false;
	private RunnableReadyHandler ChkStatRunnable = new RunnableReadyHandler(new Runnable()
	{
		@Override
		public void run()
		{
			result = 0;

			cancelThread = false;

			float progress = 0;
			ArrayList<LogEntry> logList = new ArrayList<LogEntry>();

			do
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
					// thread abgebrochen
					cancelThread = true;
				}

				if (!cancelThread)
				{
					logList.clear();
					result = GroundspeakAPI.GetGeocacheLogsByCache(GlobalCore.getSelectedCache(), logList, false);
					if (result == -1) break;// API Error
					if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
					{
						GL.that.Toast(ConnectionError.INSTANCE);
						break;
					}
					if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
					{
						GL.that.Toast(ApiUnavailable.INSTANCE);
						break;
					}
				}

				ProgresssChangedEventList.Call("", (int) progress);

			}
			while (false && !cancelThread);

			if ((result == 0) && (!cancelThread) && (logList.size() > 0))
			{
				Database.Data.beginTransaction();

				Iterator<LogEntry> iterator = logList.iterator();
				LogDAO dao = new LogDAO();
				do
				{
					ChangedCount++;
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{
						cancelThread = true;
					}
					LogEntry writeTmp = iterator.next();
					dao.WriteToDatabase(writeTmp);
				}
				while (iterator.hasNext() && !cancelThread);

				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();
				if (LogView.that != null)
				{
					LogView.that.resetInitial();
				}

			}
			pd.close();

		}
	})
	{

		@Override
		public void RunnableReady(boolean canceld)
		{
			String sCanceld = canceld ? Translation.Get("isCanceld") + GlobalCore.br : "";

			if (result != -1)
			{
				/*
				 * // Reload result from DB synchronized (Database.Data.Query) { String sqlWhere =
				 * GlobalCore.LastFilter.getSqlWhere(Config.GcLogin.getValue()); CacheListDAO cacheListDAO = new CacheListDAO();
				 * cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere); }
				 * 
				 * CachListChangedEventList.Call();
				 */synchronized (Database.Data.Query)
				{
					GL_MsgBox.Show(sCanceld + Translation.Get("LogsLoaded") + " " + ChangedCount, Translation.Get("LoadLogs"),
							MessageBoxIcon.None);
				}

			}
		}
	};
}
