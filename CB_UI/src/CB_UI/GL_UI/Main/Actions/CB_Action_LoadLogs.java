package CB_UI.GL_UI.Main.Actions;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchGC;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;

public class CB_Action_LoadLogs extends CB_Action {

    public CB_Action_LoadLogs() {
	super("LoadLogs", MenuID.AID_LOADLOGS);

    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal());
    }

    private CancelWaitDialog wd;

    @Override
    public void Execute() {
	// pd = ProgressDialog.Show(Translation.Get("LoadLogs"), DownloadAnimation.GetINSTANCE(), ChkStatRunnable);

	if (GlobalCore.getSelectedCache() == null) {
	    GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
	    return;
	}

	wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

	    @Override
	    public void isCanceld() {

	    }
	}, new cancelRunnable() {

	    @Override
	    public void run() {
		String GcCode = GlobalCore.getSelectedCache().getGcCode();

		SearchGC searchC = new SearchGC(GcCode);
		searchC.number = 1;
		searchC.available = false;

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		CB_UI.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, GlobalCore.getSelectedCache().getGPXFilename_ID(), this);

		try {
		    GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}

		// Reload result from DB
		synchronized (Database.Data.Query) {
		    String sqlWhere = FilterInstances.LastFilter.getSqlWhere(Config.GcLogin.getValue());
		    CacheListDAO cacheListDAO = new CacheListDAO();
		    cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
		}

		CacheListChangedEventList.Call();
		Cache selCache = Database.Data.Query.GetCacheByGcCode(GcCode);
		GlobalCore.setSelectedCache(selCache);
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			GL.that.RunOnGL(new IRunOnGL() {

			    @Override
			    public void run() {
				if (TabMainView.logView != null)
				    TabMainView.logView.onShow();
				GL.that.renderOnce();
			    }
			});
		    }
		});

		wd.close();
	    }

	    @Override
	    public boolean cancel() {
		// TODO Handle cancel
		return false;
	    }
	});

    }

    int ChangedCount = 0;
    int result = 0;

}
