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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.SelectDB;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.main.MainViewBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.map.LayerManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileList;
import de.droidcachebox.utils.log.Log;

import java.io.IOException;

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
            // in jedem Render Vorgang einen Step ausführen
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
                    progress.setProgress(30, "check directoiries");
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
                    initLayers();
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
        descTextView = new CB_Label(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.splashMsg, null, null, WrapType.MULTILINE).setHAlignment(HAlignment.CENTER);
        descTextView.setHeight(descTextView.getTextHeight());
        this.addLast(descTextView);

        Drawable ProgressBack = new NinePatchDrawable(atlas.createPatch(IconName.btnNormal.name()));
        Drawable ProgressFill = new NinePatchDrawable(atlas.createPatch("progress"));

        float ProgressHeight = Math.max(ProgressBack.getBottomHeight() + ProgressBack.getTopHeight(), ref / 1.5f);

        progress = new ProgressBar(new CB_RectF(0, 0, this.getWidth(), ProgressHeight), "Splash.ProgressBar");

        progress.setBackground(ProgressBack);
        progress.setProgressFill(ProgressFill);
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
        // Log.info(log, "ini_Config");
        // Database.Settings.startUp(Config.WorkPath + "/User/Config.db3");
        // Config.settings.ReadFromDB();
        // // now must reinitial UiSizes with reading settings values
        // GL_UISizes.initial(UI_Size_Base.that.getWindowWidth(), UI_Size_Base.that.getWindowHeight());
        // Config.AcceptChanges();
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
            String altValue = Config.Sel_LanguagePath.getValue();
            if (altValue.contains(Config.mWorkPath)) {
                String newValue = altValue.replace(Config.mWorkPath + "/", "");
                Config.Sel_LanguagePath.setValue(newValue);
                Config.AcceptChanges();
            }

            if (altValue.startsWith("/")) {
                String newValue = altValue.substring(1);
                Config.Sel_LanguagePath.setValue(newValue);
                Config.AcceptChanges();
            }

            Translation trans = new Translation(Config.mWorkPath, FileType.Internal);
            try {
                trans.loadTranslation(Config.Sel_LanguagePath.getValue());
            } catch (Exception e) {
                trans.loadTranslation(Config.Sel_LanguagePath.getDefaultValue());
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
        ini_Dir(Config.PocketQueryFolder.getValue());
        ini_Dir(Config.TileCacheFolder.getValue());
        ini_Dir(Config.mWorkPath + "/User");
        ini_Dir(Config.TrackFolder.getValue());
        ini_Dir(Config.UserImageFolder.getValue());
        ini_Dir(Config.mWorkPath + "/repository");
        ini_Dir(Config.DescriptionImageFolder.getValue());
        ini_Dir(Config.MapPackFolder.getValue());
        ini_Dir(Config.SpoilerFolder.getValue());

        // prevent mediascanner to parse all the images in the cachebox folder
        File nomedia = FileFactory.createFile(Config.mWorkPath, ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException ignored) {
            }
        }
    }

    private void ini_Dir(String Folder) {
        File ff = FileFactory.createFile(Folder);
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
            fileList = new FileList(Config.mWorkPath, "DB3");
        } catch (Exception ex) {
            Log.err(log, "slpash.Initial()", "search number of DB3 files", ex);
        }
        if (fileList != null) {
            if ((fileList.size() > 1) && Config.MultiDBAsk.getValue() && !GlobalCore.restartAfterKill) {
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
        Log.info(log, "ini_CacheDB");

        Database.Data.startUp(Config.mWorkPath + "/" + Config.DatabaseName.getValue());

        Config.settings.ReadFromDB();

        FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());

        CoreData.categories = new Categories();
        Database.Data.updateCacheCountForGPXFilenames();

        synchronized (Database.Data.cacheList) {
            Database.Data.cacheList = CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Config.showAllWaypoints.getValue());
        }

        CacheListChangedListeners.getInstance().cacheListChanged();

        Database.Drafts.startUp(Config.mWorkPath + "/User/FieldNotes.db3");

    }

    /**
     * Step 7 <br>
     * chk installed map packs/layers
     */
    private void initLayers() {
        Log.info(log, "ini_MapPacks");
        LayerManager.getInstance().initLayers();
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
            synchronized (Database.Data.cacheList) {
                Cache c = Database.Data.cacheList.getCacheByGcCodeFromCacheList(GlobalCore.restartCache);
                if (c != null) {
                    if (GlobalCore.restartWaypoint != null) {
                        WaypointDAO dao = new WaypointDAO();
                        CB_List<Waypoint> waypoints = dao.getWaypointsFromCacheID(c.generatedId, true);
                        if (waypoints != null) {
                            Waypoint w = null;

                            for (int i = 0, n = waypoints.size(); i < n; i++) {
                                Waypoint wp = waypoints.get(i);
                                if (wp.getGcCode().equalsIgnoreCase(GlobalCore.restartWaypoint)) {
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
        this.removeChildsDirekt();

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
