/*
 * Copyright (C) 2014-2015 team-cachebox.de
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
package de.droidcachebox.menu;

import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;
import static de.droidcachebox.settings.AllSettings.DatabaseName;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.io.IOException;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Categories;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.main.MainViewBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.menuBtn1.executes.SelectDB;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileList;
import de.droidcachebox.utils.log.Log;

/**
 * @author ging-buh
 * @author Longri
 */
public class MainView extends MainViewBase {
    private static final String sClass = "MainView";
    private TextureAtlas atlas;
    private ProgressBar progress;
    private int step = 0;
    private boolean waitUntilDBSelected = false;

    public MainView(CB_RectF rec) {
        super(rec);
    }

    @Override
    protected void renderInit() {
        GL.that.restartRendering();
        if (!waitUntilDBSelected) {
            // one step per render cycle
            switch (step) {
                case 0:
                    atlas = new TextureAtlas(Gdx.files.internal("skins/default/day/SplashPack.spp.atlas"));
                    setBackground(new SpriteDrawable(atlas.createSprite("splash-back")));
                    break;
                case 1:
                    ini_Progressbar();
                    progress.setValues(10, "Read Config");
                    break;
                case 2:
                    ini_Config();
                    progress.setValues(15, "Load Translations");
                    break;
                case 3:
                    ini_Translations();
                    progress.setValues(20, "Load Sprites");
                    break;
                case 4:
                    ini_Sprites();
                    progress.setValues(30, "check directories");
                    break;
                case 5:
                    ini_Dirs();
                    progress.setValues(40, "Select DB");
                    break;
                case 6:
                    ini_SelectDB();
                    progress.setValues(60, "Load Caches");
                    break;
                case 7:
                    ini_CacheDB();
                    progress.setValues(80, "initial Map layer");
                    break;
                case 8:
                    progress.setValues(100, "Run");
                    break;
                case 100:
                    ini_TabMainView();
                    break;
                default:
                    step = 99;
            }
            step++;
        }

        if (step <= 100)
            resetRenderInitDone();
    }

    /**
     * Step 1 <br>
     * add Progressbar
     */
    private void ini_Progressbar() {

        float ref = UiSizes.getInstance().getWindowHeight() / 13f;
        CB_RectF CB_LogoRec = new CB_RectF(getHalfWidth() - (ref * 2.5f), getHeight() - ((ref * 5) / 4.11f) - ref, ref * 5, (ref * 5) / 4.11f);
        Image CB_Logo = new Image(CB_LogoRec, "CB_Logo", false);
        CB_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("cachebox-logo")));
        CB_Label dummy = new CB_Label();
        initRow();
        addLast(dummy);
        addNext(dummy);
        addNext(CB_Logo, FIXED);
        addLast(dummy);

        String version = GlobalCore.getInstance().getVersionString();
        CB_Label descTextView = new CB_Label(version + br + br + GlobalCore.splashMsg, null, null, WrapType.MULTILINE).setHAlignment(HAlignment.CENTER);
        descTextView.setHeight(descTextView.getTextHeight());
        addLast(descTextView);

        // cause Sprites are not yet initialised, do it here manually
        Drawable progressBack = new NinePatchDrawable(atlas.createPatch(Sprites.IconName.btnNormal.name()));
        Drawable progressFill = new NinePatchDrawable(atlas.createPatch("progress"));
        progress = new ProgressBar();
        progress.setBackground(progressBack);
        progress.setProgressFill(progressFill);
        initRow(BOTTOMUp);
        addLast(progress);

        float logoCalcRef = ref * 1.5f;
        CB_RectF rec_GC_Logo = new CB_RectF(20, 50, logoCalcRef, logoCalcRef);
        CB_RectF rec_Mapsforge_Logo = new CB_RectF(200, 50, logoCalcRef, logoCalcRef / 1.142f);
        CB_RectF rec_FX2_Logo = new CB_RectF(rec_Mapsforge_Logo);
        CB_RectF rec_LibGdx_Logo = new CB_RectF(20, 50, logoCalcRef * 4.17f * 0.8f, logoCalcRef * 0.8f);
        CB_RectF rec_OSM = new CB_RectF(rec_Mapsforge_Logo);
        CB_RectF rec_Route = new CB_RectF(rec_Mapsforge_Logo);
        rec_FX2_Logo.setX(400);

        Image GC_Logo = new Image(rec_GC_Logo, "GC_Logo", false);
        GC_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("gc_live")));
        if (isAccessTokenInvalid())
            GC_Logo.setInvisible();

        Image mapsforge_Logo = new Image(rec_Mapsforge_Logo, "Mapsforge_Logo", false);
        mapsforge_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("mapsforge_logo")));

        Image libGdx_Logo = new Image(rec_LibGdx_Logo, "LibGdx_Logo", false);
        libGdx_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("libgdx")));

        Image route_Logo = new Image(rec_OSM, "Route_Logo", false);
        route_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("openrouteservice_logo")));

        Image OSM_Logo = new Image(rec_Route, "OSM_Logo", false);
        OSM_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("osm_logo")));

        float yPos = descTextView.getY() - GC_Logo.getHeight();
        float xPos = (getWidth() - ref - GC_Logo.getWidth() - mapsforge_Logo.getWidth()) / 2;

        GC_Logo.setPos(xPos, yPos);
        xPos += GC_Logo.getWidth() + ref;

        mapsforge_Logo.setPos(xPos, yPos);

        yPos -= GC_Logo.getHeight();// + refHeight;
        libGdx_Logo.setPos(getHalfWidth() - libGdx_Logo.getHalfWidth(), yPos);

        yPos -= GC_Logo.getHeight();//
        xPos = (getWidth() - (ref) - route_Logo.getWidth() - OSM_Logo.getWidth()) / 2;

        route_Logo.setPos(xPos, yPos);

        xPos += route_Logo.getWidth() + ref;
        OSM_Logo.setPos(xPos, yPos);

        addChild(GC_Logo);
        addChild(libGdx_Logo);
        addChild(mapsforge_Logo);
        // addChild(Route_Logo);
        addChild(OSM_Logo);

    }

    /**
     * Step 2 <br>
     * Load Config DB3
     */
    private void ini_Config() {
    }

    /**
     * Step 3 <br>
     * Load Translations
     */
    private void ini_Translations() {
        if (!Translation.isInitialized()) {
            Log.debug(sClass, "ini_Translations");

            // Load from Assets changes
            // delete work path from settings value
            String altValue = Settings.Sel_LanguagePath.getValue();
            if (altValue.contains(GlobalCore.workPath)) {
                String newValue = altValue.replace(GlobalCore.workPath + "/", "");
                Settings.Sel_LanguagePath.setValue(newValue);
                Settings.getInstance().acceptChanges();
            }

            if (altValue.startsWith("/")) {
                String newValue = altValue.substring(1);
                Settings.Sel_LanguagePath.setValue(newValue);
                Settings.getInstance().acceptChanges();
            }

            Translation trans = new Translation(GlobalCore.workPath);
            try {
                trans.loadTranslation(Settings.Sel_LanguagePath.getValue());
            } catch (Exception e) {
                trans.loadTranslation(Settings.Sel_LanguagePath.getDefaultValue());
            }
        }
    }

    /**
     * Step 4 <br>
     * Load Sprites
     */
    private void ini_Sprites() {
        Log.debug(sClass, "ini_Sprites");
        Sprites.loadSprites(false);
        if (!Sprites.loaded)
            Log.err(sClass, "Error ini_Sprites");
    }

    /**
     * Step 5 <br>
     * chk directories
     */
    private void ini_Dirs() {
        Log.debug(sClass, "ini_Dirs");
        ini_Dir(Settings.PocketQueryFolder.getValue());
        ini_Dir(Settings.tileCacheFolder.getValue());
        ini_Dir(GlobalCore.workPath + "/User");
        ini_Dir(Settings.TrackFolder.getValue());
        ini_Dir(Settings.UserImageFolder.getValue());
        ini_Dir(GlobalCore.workPath + "/repository");
        ini_Dir(Settings.DescriptionImageFolder.getValue());
        ini_Dir(Settings.MapPackFolder.getValue());
        ini_Dir(Settings.SpoilerFolder.getValue());

        // prevent media_scanner to parse all the images in the cachebox folder
        AbstractFile nomedia = FileFactory.createFile(GlobalCore.workPath, ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException ignored) {
            }
        }
    }

    private void ini_Dir(String Folder) {
        AbstractFile ff = FileFactory.createFile(Folder);
        if (!ff.exists()) {
            ff.mkdir();
        }
    }

    /**
     * Step 5 <br>
     * show select DB Dialog
     */
    private void ini_SelectDB() {
        Log.debug(sClass, "ini_SelectDB");
        // search number of DB3 files
        FileList fileList = null;
        try {
            fileList = new FileList(GlobalCore.workPath, "DB3");
        } catch (Exception ex) {
            Log.err(sClass, "getting DB3 fileList", ex);
        }
        if (fileList != null) {
            if ((fileList.size() > 1) && Settings.MultiDBAsk.getValue() && !GlobalCore.restartAfterKill) {
                waitUntilDBSelected = true;
                SelectDB selectDB = new SelectDB(this, "SelectDbDialog", true);
                selectDB.setReturnListener(this::returnFromSelectDB);
                selectDB.show();
            }
        }
    }

    private void returnFromSelectDB() {
        waitUntilDBSelected = false;
    }

    /**
     * Step 6<br>
     * Load Cache DB3
     */
    private void ini_CacheDB() {
        Log.debug(sClass, "ini_DB " + DatabaseName.getValue());
        CBDB.getInstance().startUp(GlobalCore.workPath + "/" + DatabaseName.getValue());
        Log.debug(sClass, "ini_DB " + Settings.DatabaseName.getValue());
        Settings.getInstance().readFromDB();

        FilterInstances.setLastFilter(new FilterProperties(Settings.lastFilter.getValue()));
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());

        CoreData.categories = new Categories();
        CachesDAO cachesDAO = new CachesDAO();
        cachesDAO.updateCacheCountForGPXFilenames();

        synchronized (CBDB.cacheList) {
            cachesDAO.readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
        }

        CacheListChangedListeners.getInstance().fire(sClass);

        DraftsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/FieldNotes.db3");

    }

    /**
     * Last Step <br>
     * Show ViewManager
     */
    private void ini_TabMainView() {
        Log.debug(sClass, "ini_TabMainView");
        GL.that.removeRenderView(this);
        GL.that.switchToMainView();

        if (GlobalCore.restartCache != null) {
            synchronized (CBDB.cacheList) {
                Cache c = CBDB.cacheList.getCacheByGcCodeFromCacheList(GlobalCore.restartCache);
                if (c != null) {
                    if (GlobalCore.restartWayPoint != null) {
                        CB_List<Waypoint> waypoints = WaypointDAO.getInstance().getWaypointsFromCacheID(c.generatedId, true);
                        if (waypoints != null) {
                            Waypoint w = null;

                            for (int i = 0, n = waypoints.size(); i < n; i++) {
                                Waypoint wp = waypoints.get(i);
                                if (wp.getWaypointCode().equalsIgnoreCase(GlobalCore.restartWayPoint)) {
                                    w = wp;
                                }
                            }
                            Log.debug(sClass, "ini_TabMainView: Set selectedCache to" + c.getGeoCacheCode() + " from restartCache + WP.");
                            GlobalCore.setSelectedWaypoint(c, w);
                        } else {
                            Log.debug(sClass, "ini_TabMainView: Set selectedCache to" + c.getGeoCacheCode() + " from restartCache.");
                            GlobalCore.setSelectedCache(c);
                        }
                    }
                }

            }

        }
        GL.that.setAllIsInitialized(true);
    }

}
