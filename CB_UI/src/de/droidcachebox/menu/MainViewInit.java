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

import static de.droidcachebox.settings.AllSettings.DatabaseName;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.io.IOException;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.Categories;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
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
public class MainViewInit extends MainViewBase {
    private static final String log = "MainViewInit";
    private TextureAtlas atlas;
    private ProgressBar progress;
    private Image CB_Logo;
    private Image Mapsforge_Logo;
    private Image LibGdx_Logo;
    private Image GC_Logo;
    private CB_Label descTextView;
    private int step = 0;
    private boolean switcher = false;
    private boolean breakForWait = false;

    public MainViewInit(CB_RectF rec) {
        super(rec);
    }

    @Override
    protected void initialize() {
        GL.that.restartRendering();
        switcher = !switcher;
        if (switcher && !breakForWait) {
            // one step per render cycle
            switch (step) {
                case 0:
                    atlas = new TextureAtlas(Gdx.files.internal("skins/default/day/SplashPack.spp.atlas"));
                    setBackground(new SpriteDrawable(atlas.createSprite("splash-back")));

                    break;
                case 1:
                    ini_Progressbar();
                    progress.setProgress(10, "Read Config");
                    break;
                case 2:
                    ini_Config();
                    progress.setProgress(15, "Load Translations");
                    break;
                case 3:
                    ini_Translations();
                    progress.setProgress(20, "Load Sprites");
                    break;
                case 4:
                    ini_Sprites();
                    progress.setProgress(30, "check directories");
                    break;
                case 5:
                    ini_Dirs();
                    progress.setProgress(40, "Select DB");
                    break;
                case 6:
                    ini_SelectDB();
                    progress.setProgress(60, "Load Caches");
                    break;
                case 7:
                    ini_CacheDB();
                    progress.setProgress(80, "initial Map layer");
                    break;
                case 8:
                    progress.setProgress(100, "Run");
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
            resetIsInitialized();
    }

    /**
     * Step 1 <br>
     * add Progressbar
     */
    private void ini_Progressbar() {

        float ref = UiSizes.getInstance().getWindowHeight() / 13f;
        CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref, ref * 5, (ref * 5) / 4.11f);
        CB_Logo = new Image(CB_LogoRec, "CB_Logo", false);
        CB_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("cachebox-logo")));
        CB_Label dummy = new CB_Label();
        this.initRow();
        this.addLast(dummy);
        this.addNext(dummy);
        this.addNext(CB_Logo, FIXED);
        this.addLast(dummy);

        String VersionString = GlobalCore.getInstance().getVersionString();
        descTextView = new CB_Label(VersionString + br + br + GlobalCore.splashMsg, null, null, WrapType.MULTILINE).setHAlignment(HAlignment.CENTER);
        descTextView.setHeight(descTextView.getTextHeight());
        this.addLast(descTextView);

        Drawable progressBack = new NinePatchDrawable(atlas.createPatch(IconName.btnNormal.name()));
        Drawable progressFill = new NinePatchDrawable(atlas.createPatch("progress"));
        float progressHeight = Math.max(progressBack.getBottomHeight() + progressBack.getTopHeight(), ref / 1.5f);
        progress = new ProgressBar(new CB_RectF(0, 0, this.getWidth(), progressHeight), "Splash.ProgressBar");
        progress.setBackground(progressBack);
        progress.setProgressFill(progressFill);
        this.addChild(progress);

        float logoCalcRef = ref * 1.5f;

        CB_RectF rec_GC_Logo = new CB_RectF(20, 50, logoCalcRef, logoCalcRef);
        CB_RectF rec_Mapsforge_Logo = new CB_RectF(200, 50, logoCalcRef, logoCalcRef / 1.142f);
        CB_RectF rec_FX2_Logo = new CB_RectF(rec_Mapsforge_Logo);
        CB_RectF rec_LibGdx_Logo = new CB_RectF(20, 50, logoCalcRef * 4.17f * 0.8f, logoCalcRef * 0.8f);
        CB_RectF rec_OSM = new CB_RectF(rec_Mapsforge_Logo);
        CB_RectF rec_Route = new CB_RectF(rec_Mapsforge_Logo);

        rec_FX2_Logo.setX(400);

        GC_Logo = new Image(rec_GC_Logo, "GC_Logo", false);
        GC_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("gc_live")));

        Mapsforge_Logo = new Image(rec_Mapsforge_Logo, "Mapsforge_Logo", false);
        Mapsforge_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("mapsforge_logo")));

        LibGdx_Logo = new Image(rec_LibGdx_Logo, "LibGdx_Logo", false);
        LibGdx_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("libgdx")));

        Image route_Logo = new Image(rec_OSM, "Route_Logo", false);
        route_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("openrouteservice_logo")));

        Image OSM_Logo = new Image(rec_Route, "OSM_Logo", false);
        OSM_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("osm_logo")));

        float yPos = descTextView.getY() - GC_Logo.getHeight();
        float xPos = (this.getWidth() - ref - GC_Logo.getWidth() - Mapsforge_Logo.getWidth()) / 2;

        GC_Logo.setPos(xPos, yPos);
        xPos += GC_Logo.getWidth() + ref;

        Mapsforge_Logo.setPos(xPos, yPos);

        yPos -= GC_Logo.getHeight();// + refHeight;
        LibGdx_Logo.setPos(this.getHalfWidth() - LibGdx_Logo.getHalfWidth(), yPos);

        yPos -= GC_Logo.getHeight();//
        xPos = (this.getWidth() - (ref) - route_Logo.getWidth() - OSM_Logo.getWidth()) / 2;

        route_Logo.setPos(xPos, yPos);

        xPos += route_Logo.getWidth() + ref;
        OSM_Logo.setPos(xPos, yPos);

        this.addChild(GC_Logo);
        this.addChild(LibGdx_Logo);
        this.addChild(Mapsforge_Logo);
        // this.addChild(Route_Logo);
        this.addChild(OSM_Logo);

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
            Log.info(log, "ini_Translations");

            // Load from Assets changes
            // delete work path from settings value
            String altValue = Settings.Sel_LanguagePath.getValue();
            if (altValue.contains(GlobalCore.workPath)) {
                String newValue = altValue.replace(GlobalCore.workPath + "/", "");
                Settings.Sel_LanguagePath.setValue(newValue);
                ViewManager.that.acceptChanges();
            }

            if (altValue.startsWith("/")) {
                String newValue = altValue.substring(1);
                Settings.Sel_LanguagePath.setValue(newValue);
                ViewManager.that.acceptChanges();
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
        Log.info(log, "ini_Sprites");
        Sprites.loadSprites(false);
        if (!Sprites.loaded)
            Log.err(log, "Error ini_Sprites");
    }

    /**
     * Step 5 <br>
     * chk directories
     */
    private void ini_Dirs() {
        Log.info(log, "ini_Dirs");
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
        Log.info(log, "ini_SelectDB");
        // search number of DB3 files
        FileList fileList = null;
        try {
            fileList = new FileList(GlobalCore.workPath, "DB3");
        } catch (Exception ex) {
            Log.err(log, "getting DB3 fileList", ex);
        }
        if (fileList != null) {
            if ((fileList.size() > 1) && Settings.MultiDBAsk.getValue() && !GlobalCore.restartAfterKill) {
                breakForWait = true;
                SelectDB selectDBDialog = new SelectDB(this, "SelectDbDialog", true);
                selectDBDialog.setReturnListener(this::returnFromSelectDB);
                selectDBDialog.show();
            }
        }
    }

    private void returnFromSelectDB() {
        breakForWait = false;
        switcher = true;
    }

    /**
     * Step 6<br>
     * Load Cache DB3
     */
    private void ini_CacheDB() {

        Log.debug(log, "\r\nini_CacheDB " + Settings.DatabaseName.getValue());
        Log.debug(log, "\r\nini_CacheDB " + DatabaseName.getValue());
        CBDB.getInstance().startUp(GlobalCore.workPath + "/" + DatabaseName.getValue());
        Settings.getInstance().readFromDB();
        Log.debug(log, "\r\nini_CacheDB " + Settings.DatabaseName.getValue());

        FilterInstances.setLastFilter(new FilterProperties(Settings.FilterNew.getValue()));
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());

        CoreData.categories = new Categories();
        CacheDAO.getInstance().updateCacheCountForGPXFilenames();

        synchronized (CBDB.getInstance().cacheList) {
            CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
        }

        CacheListChangedListeners.getInstance().cacheListChanged();

        DraftsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/FieldNotes.db3");

    }

    /**
     * Last Step <br>
     * Show ViewManager
     */
    private void ini_TabMainView() {
        Log.info(log, "ini_TabMainView");
        GL.that.removeRenderView(this);
        GL.that.switchToMainView();

        if (GlobalCore.restartCache != null) {
            synchronized (CBDB.getInstance().cacheList) {
                Cache c = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList(GlobalCore.restartCache);
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
                            Log.info(log, "ini_TabMainView: Set selectedCache to" + c.getGeoCacheCode() + " from restartCache + WP.");
                            GlobalCore.setSelectedWaypoint(c, w);
                        } else {
                            Log.info(log, "ini_TabMainView: Set selectedCache to" + c.getGeoCacheCode() + " from restartCache.");
                            GlobalCore.setSelectedCache(c);
                        }
                    }
                }

            }

        }
        GL.that.setAllIsInitialized(true);
    }

    @Override
    public void dispose() {
        this.removeChildsDirect();

        if (descTextView != null)
            descTextView.dispose();
        if (GC_Logo != null)
            GC_Logo.dispose();
        if (LibGdx_Logo != null)
            LibGdx_Logo.dispose();
        if (Mapsforge_Logo != null)
            Mapsforge_Logo.dispose();
        if (CB_Logo != null)
            CB_Logo.dispose();
        if (progress != null)
            progress.dispose();
        if (atlas != null)
            atlas.dispose();

        descTextView = null;
        GC_Logo = null;
        LibGdx_Logo = null;
        Mapsforge_Logo = null;
        CB_Logo = null;
        progress = null;
        atlas = null;

    }

}
