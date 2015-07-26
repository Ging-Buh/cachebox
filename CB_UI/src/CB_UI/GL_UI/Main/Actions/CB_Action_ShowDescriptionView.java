/* 
 * Copyright (C) 2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI.GL_UI.Main.Actions;

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchGC;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.DescriptionView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowDescriptionView extends CB_Action_ShowView {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Action_ShowDescriptionView.class);

    public CB_Action_ShowDescriptionView() {
	super("Description", MenuID.AID_SHOW_DESCRIPTION);
    }

    @Override
    public void Execute() {
	if ((TabMainView.descriptionView == null) && (tabMainView != null) && (tab != null))
	    TabMainView.descriptionView = new DescriptionView(tab.getContentRec(), "DescriptionView");

	if ((TabMainView.descriptionView != null) && (tab != null))
	    tab.ShowView(TabMainView.descriptionView);
    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.doc_2.ordinal());
    }

    @Override
    public CB_View_Base getView() {
	return TabMainView.descriptionView;
    }

    @Override
    public boolean HasContextMenu() {
	return true;
    }

    CancelWaitDialog wd = null;

    @Override
    public Menu getContextMenu() {
	Menu cm = new Menu("CacheListContextMenu");

	cm.addItemClickListner(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		switch (((MenuItem) v).getMenuItemId()) {
		case MenuID.MI_FAVORIT:
		    if (GlobalCore.getSelectedCache() == null) {
			GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
			return true;
		    }

		    GlobalCore.getSelectedCache().setFavorit(!GlobalCore.getSelectedCache().isFavorite());
		    CacheDAO dao = new CacheDAO();
		    dao.UpdateDatabase(GlobalCore.getSelectedCache());

		    // Update Query
		    Database.Data.Query.GetCacheById(GlobalCore.getSelectedCache().Id).setFavorite(GlobalCore.getSelectedCache().isFavorite());

		    // Update View
		    if (TabMainView.descriptionView != null)
			TabMainView.descriptionView.onShow();

		    CachListChangedEventList.Call();
		    return true;
		case MenuID.MI_RELOAD_CACHE:
		    ReloadSelectedCache();
		    return true;
		case MenuID.MI_TOGGLE_DEC__TXT_HTML:
		    TabMainView.descriptionView.toggleTxt_Html();
		    return true;
		}
		return false;
	    }

	});

	MenuItem mi;

	boolean isSelected = (GlobalCore.ifCacheSelected());

	mi = cm.addItem(MenuID.MI_FAVORIT, "Favorite", SpriteCacheBase.Icons.get(IconName.favorit_42.ordinal()));
	mi.setCheckable(true);
	if (isSelected) {
	    mi.setChecked(GlobalCore.getSelectedCache().isFavorite());
	} else {
	    mi.setEnabled(false);
	}

	boolean selectedCacheIsNoGC = false;

	if (isSelected)
	    selectedCacheIsNoGC = !GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
	mi = cm.addItem(MenuID.MI_RELOAD_CACHE, "ReloadCacheAPI", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
	if (!isSelected)
	    mi.setEnabled(false);
	if (selectedCacheIsNoGC)
	    mi.setEnabled(false);

	if (TabMainView.descriptionView != null) {
	    if (TabMainView.descriptionView.getTxtOnly()) {
		mi = cm.addItem(MenuID.MI_TOGGLE_DEC__TXT_HTML, "showHtml", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
	    } else {
		mi = cm.addItem(MenuID.MI_TOGGLE_DEC__TXT_HTML, "showTxtOnly", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
	    }
	}

	return cm;
    }

    public void ReloadSelectedCache() {
	if (GlobalCore.getSelectedCache() == null) {
	    GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
	    return;
	}

	wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListner() {

	    @Override
	    public void isCanceld() {
		// TODO handle cancel
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

		String result = CB_UI.Api.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, GlobalCore.getSelectedCache().GPXFilename_ID, this);

		if (result.length() > 0) {

		    log.debug("result:" + result);

		    try {
			GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }

		    // Reload result from DB
		    synchronized (Database.Data.Query) {
			String sqlWhere = FilterProperties.LastFilter.getSqlWhere(Config.GcLogin.getValue());
			CacheListDAO cacheListDAO = new CacheListDAO();
			cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
		    }

		    CachListChangedEventList.Call();
		    Cache selCache = Database.Data.Query.GetCacheByGcCode(GcCode);
		    GlobalCore.setSelectedCache(selCache);
		    GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
			    GL.that.RunOnGL(new IRunOnGL() {

				@Override
				public void run() {
				    if (TabMainView.descriptionView != null) {
					TabMainView.descriptionView.forceReload();
					TabMainView.descriptionView.onShow();
				    }
				    GL.that.renderOnce();
				}
			    });
			}
		    });

		}

		wd.close();
	    }

	    @Override
	    public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
	    }
	});
    }

}
