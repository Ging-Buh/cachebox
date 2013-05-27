package CB_Core.GL_UI.Main.Actions;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Controls.Animation.DownloadAnimation;
import CB_Core.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_Core.GL_UI.Controls.PopUps.ConnectionError;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.interfaces.RunnableReadyHandler;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Command_chkState extends CB_ActionCommand
{

	public CB_Action_Command_chkState()
	{
		super("chkState", MenuID.AID_CHK_STATE);

	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.GCLive_35.ordinal());
	}

	private ProgressDialog pd;

	@Override
	public void Execute()
	{
		pd = ProgressDialog.Show(Translation.Get("chkState"), DownloadAnimation.GetINSTANCE(), ChkStatRunnable);
	}

	int ChangedCount = 0;
	int result = 0;

	private RunnableReadyHandler ChkStatRunnable = new RunnableReadyHandler(new Runnable()
	{
		final int BlockSize = 100; // die API läst nur maximal 100 zu!

		@Override
		public void run()
		{
			ArrayList<Cache> chkList = new ArrayList<Cache>();

			synchronized (Database.Data.Query)
			{
				if (Database.Data.Query == null || Database.Data.Query.size() == 0) return;

				Iterator<Cache> cIterator = Database.Data.Query.iterator();

				ChangedCount = 0;

				if (cIterator != null && cIterator.hasNext())
				{
					do
					{
						chkList.add(cIterator.next());
					}
					while (cIterator.hasNext());
				}

			}
			float ProgressInkrement = 100.0f / (chkList.size() / BlockSize);

			// in Blöcke Teilen

			int start = 0;
			int stop = BlockSize;
			ArrayList<Cache> addedReturnList = new ArrayList<Cache>();

			result = 0;
			ArrayList<Cache> chkList100;

			boolean cancelThread = false;

			float progress = 0;

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
				chkList100 = new ArrayList<Cache>();
				if (!cancelThread)
				{

					if (chkList == null || chkList.size() == 0)
					{
						break;
					}

					Iterator<Cache> Iterator2 = chkList.iterator();

					int index = 0;
					do
					{
						if (index >= start && index <= stop)
						{
							chkList100.add(Iterator2.next());
						}
						else
						{
							Iterator2.next();
						}
						index++;
					}
					while (Iterator2.hasNext());

					// result = GroundspeakAPI.GetGeocacheStatus("WERTWEE", chkList100);
					result = GroundspeakAPI.GetGeocacheStatus(Config.GetAccessToken(), chkList100);
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
					addedReturnList.addAll(chkList100);
					start += BlockSize + 1;
					stop += BlockSize + 1;
				}

				progress += ProgressInkrement;

				ProgresssChangedEventList.Call("", (int) progress);

			}
			while (chkList100.size() == BlockSize + 1);

			if (result == 0)
			{
				Database.Data.beginTransaction();

				Iterator<Cache> iterator = addedReturnList.iterator();
				CacheDAO dao = new CacheDAO();
				do
				{
					Cache writeTmp = iterator.next();
					if (dao.UpdateDatabaseCacheState(writeTmp)) ChangedCount++;
				}
				while (iterator.hasNext());

				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();

			}
			else
			{
				pd.close();
			}
		}
	})
	{

		@Override
		public void RunnableReady(boolean canceld)
		{
			if (result != -1)
			{
				GL.that.closeAllDialogs();
				synchronized (Database.Data.Query)
				{
					GL_MsgBox.Show(Translation.Get("CachesUpdatet") + " " + ChangedCount + "/" + Database.Data.Query.size(),
							Translation.Get("chkState"), MessageBoxIcon.None);
				}

			}
		}
	};
}
