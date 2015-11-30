package CB_UI.GL_UI.Main.Actions;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CacheListChangedEventList;
import CB_Core.Types.Cache;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_Utils.Events.ProgresssChangedEventList;

public class CB_Action_Command_chkState extends CB_ActionCommand {

    public CB_Action_Command_chkState() {
	super("chkState", MenuID.AID_CHK_STATE);

    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal());
    }

    private ProgressDialog pd;

    @Override
    public void Execute() {
	pd = ProgressDialog.Show(Translation.Get("chkState"), DownloadAnimation.GetINSTANCE(), ChkStatRunnable);
    }

    int ChangedCount = 0;
    int result = 0;
    private boolean cancel = false;
    private RunnableReadyHandler ChkStatRunnable = new RunnableReadyHandler() {
	final int BlockSize = 100; // die API lässt nur maximal 100 zu!

	@Override
	public void run() {
	    cancel = false;
	    ArrayList<Cache> chkList = new ArrayList<Cache>();

	    synchronized (Database.Data.Query) {
		if (Database.Data.Query == null || Database.Data.Query.size() == 0)
		    return;
		ChangedCount = 0;
		for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
		    chkList.add(Database.Data.Query.get(i));
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

	    do {
		try {
		    Thread.sleep(10);
		} catch (InterruptedException e) {
		    // thread abgebrochen
		    cancelThread = true;
		}
		chkList100 = new ArrayList<Cache>();
		if (!cancelThread) {

		    if (chkList == null || chkList.size() == 0) {
			break;
		    }

		    Iterator<Cache> Iterator2 = chkList.iterator();

		    int index = 0;
		    do {
			if (index >= start && index <= stop) {
			    chkList100.add(Iterator2.next());
			} else {
			    Iterator2.next();
			}
			index++;
		    } while (Iterator2.hasNext());

		    // result = GroundspeakAPI.GetGeocacheStatus("WERTWEE", chkList100);
		    result = GroundspeakAPI.GetGeocacheStatus(chkList100, this);
		    if (result == -1)
			break;// API Error
		    if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
			GL.that.Toast(ConnectionError.INSTANCE);
			break;
		    }
		    if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
			GL.that.Toast(ApiUnavailable.INSTANCE);
			break;
		    }
		    addedReturnList.addAll(chkList100);
		    start += BlockSize + 1;
		    stop += BlockSize + 1;
		}

		progress += ProgressInkrement;

		ProgresssChangedEventList.Call("", (int) progress);

	    } while (chkList100.size() == BlockSize + 1 && !cancelThread);

	    if (result == 0 && !cancelThread) {
		Database.Data.beginTransaction();

		Iterator<Cache> iterator = addedReturnList.iterator();
		CacheDAO dao = new CacheDAO();
		do {
		    try {
			Thread.sleep(10);
		    } catch (InterruptedException e) {
			cancelThread = true;
		    }
		    Cache writeTmp = iterator.next();
		    if (dao.UpdateDatabaseCacheState(writeTmp))
			ChangedCount++;
		} while (iterator.hasNext() && !cancelThread);

		Database.Data.setTransactionSuccessful();
		Database.Data.endTransaction();

	    }
	    pd.close();

	}

	@Override
	public boolean cancel() {
	    return cancel;
	}

	@Override
	public void RunnableReady(boolean canceld) {
	    String sCanceld = canceld ? Translation.Get("isCanceld") + GlobalCore.br : "";

	    if (result != -1) {

		// Reload result from DB
		synchronized (Database.Data.Query) {
		    String sqlWhere = FilterProperties.LastFilter.getSqlWhere(Config.GcLogin.getValue());
		    CacheListDAO cacheListDAO = new CacheListDAO();
		    cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
		    cacheListDAO = null;
		}

		CacheListChangedEventList.Call();
		synchronized (Database.Data.Query) {
		    GL_MsgBox.Show(sCanceld + Translation.Get("CachesUpdatet") + " " + ChangedCount + "/" + Database.Data.Query.size(), Translation.Get("chkState"), MessageBoxIcon.None);
		}

	    }
	}
    };
}
