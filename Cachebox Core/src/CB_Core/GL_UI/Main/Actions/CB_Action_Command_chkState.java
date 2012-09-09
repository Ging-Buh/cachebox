package CB_Core.GL_UI.Main.Actions;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.interfaces.RunnableReadyHandler;
import CB_Core.Types.Cache;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Command_chkState extends CB_ActionCommand
{

	public CB_Action_Command_chkState()
	{
		super("chkState", AID_CHK_STATE);

	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(35);
	}

	private ProgressDialog pd;

	@Override
	public void Execute()
	{
		pd = ProgressDialog.Show("Title", ChkStatRunnable);
	}

	long startTime = 0;

	private RunnableReadyHandler ChkStatRunnable = new RunnableReadyHandler(new Runnable()
	{
		final int BlockSize = 100; // die API läst nur maximal 100 zu!

		@Override
		public void run()
		{
			ArrayList<Cache> chkList = new ArrayList<Cache>();
			Iterator<Cache> cIterator = Database.Data.Query.iterator();

			startTime = System.currentTimeMillis();

			do
			{
				chkList.add(cIterator.next());
			}
			while (cIterator.hasNext());

			float ProgressInkrement = 100.0f / (chkList.size() / BlockSize);

			// in Blöcke Teilen

			int start = 0;
			int stop = BlockSize;
			ArrayList<Cache> addedReturnList = new ArrayList<Cache>();

			int result = 0;
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

					result = GroundspeakAPI.GetGeocacheStatus(Config.GetAccessToken(), chkList100);
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
					dao.UpdateDatabaseCacheState(writeTmp);
				}
				while (iterator.hasNext());

				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();

			}
			else
			{
				GL_MsgBox.Show(GlobalCore.Translations.Get("errorAPI"), GlobalCore.Translations.Get("Error"), MessageBoxIcon.Error);
			}
		}
	})
	{

		@Override
		public void RunnableReady(boolean canceld)
		{
			pd.close();
			GL_MsgBox.Show("Ready :" + (System.currentTimeMillis() - startTime));
		}
	};
}
