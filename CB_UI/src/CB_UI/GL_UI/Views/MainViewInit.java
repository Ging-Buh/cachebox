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
package CB_UI.GL_UI.Views;

import CB_Core.*;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheListDAO;
import CB_Core.Types.Categories;
import CB_Core.Types.Waypoint;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.SelectDB;
import CB_UI.GL_UI.Activitys.SelectDB.IReturnListener;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Settings.SettingString;
import CB_Utils.Util.FileList;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.io.IOException;

/**
 * @author ging-buh
 * @author Longri
 */
public class MainViewInit extends MainViewBase {
    private static final String log = "MainViewInit";
    TextureAtlas atlas;
    ProgressBar progress;
    Image CB_Logo, OSM_Logo, Route_Logo, Mapsforge_Logo, LibGdx_Logo, GC_Logo;
    Label descTextView;
    int step = 0;
    boolean switcher = false;
    boolean breakForWait = false;

    public MainViewInit(CB_RectF rec) {
        super(rec);
    }

    @Override
    protected void Initial() {
        GL.that.RestartRender();
        switcher = !switcher;
        if (switcher && !breakForWait) {
            // in jedem Render Vorgang einen Step ausführen
            switch (step) {
                case 0:
                    atlas = new TextureAtlas(Gdx.files.internal("skins/default/day/SplashPack.spp"));
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
                    ini_MapPacks();
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
            resetInitial();
    }

    /**
     * Step 1 <br>
     * add Progressbar
     */
    private void ini_Progressbar() {

        float ref = UI_Size_Base.that.getWindowHeight() / 13;
        CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref, ref * 5, (ref * 5) / 4.11f);
        CB_Logo = new Image(CB_LogoRec, "CB_Logo", false);
        CB_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("cachebox-logo")));
        Label dummy = new Label();
        this.initRow();
        this.addLast(dummy);
        this.addNext(dummy);
        this.addNext(CB_Logo, FIXED);
        this.addLast(dummy);

        String VersionString = GlobalCore.getInstance().getVersionString();
        descTextView = new Label(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.splashMsg, null, null, WrapType.MULTILINE).setHAlignment(HAlignment.CENTER);
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

        Route_Logo = new Image(rec_OSM, "Route_Logo", false);
        Route_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("openrouteservice_logo")));

        OSM_Logo = new Image(rec_Route, "OSM_Logo", false);
        OSM_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("osm_logo")));

        float yPos = descTextView.getY() - GC_Logo.getHeight();
        float xPos = (this.getWidth() - ref - GC_Logo.getWidth() - Mapsforge_Logo.getWidth()) / 2;

        GC_Logo.setPos(xPos, yPos);
        xPos += GC_Logo.getWidth() + ref;

        Mapsforge_Logo.setPos(xPos, yPos);
        xPos += Mapsforge_Logo.getWidth() + ref;

        yPos -= GC_Logo.getHeight();// + refHeight;
        LibGdx_Logo.setPos(this.getHalfWidth() - LibGdx_Logo.getHalfWidth(), yPos);

        yPos -= GC_Logo.getHeight();//
        xPos = (this.getWidth() - (ref) - Route_Logo.getWidth() - OSM_Logo.getWidth()) / 2;

        Route_Logo.setPos(xPos, yPos);

        xPos += Route_Logo.getWidth() + ref;
        OSM_Logo.setPos(xPos, yPos);

        this.addChild(GC_Logo);
        this.addChild(Mapsforge_Logo);
        this.addChild(LibGdx_Logo);
        this.addChild(Route_Logo);
        this.addChild(OSM_Logo);

    }

    /**
     * Step 2 <br>
     * Load Config DB3
     */
    private void ini_Config() {
        // Log.debug(log, "ini_Config");
        // Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");
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
        if (!Translation.isInitial()) {
            Log.debug(log, "ini_Translations");

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

            FileType fileType = FileType.Internal;

            new Translation(Config.mWorkPath, fileType);
            try {
                Translation.LoadTranslation(Config.Sel_LanguagePath.getValue());
            } catch (Exception e) {
                try {
                    Translation.LoadTranslation(Config.Sel_LanguagePath.getDefaultValue());
                } catch (IOException e1) {
                    Log.err(log,"ini_Translations", e1);
                }
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
        Log.debug(log, "ini_Dirs");
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
            } catch (IOException e) {

                e.printStackTrace();
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
        Log.debug(log, "ini_SelectDB");
        // search number of DB3 files
        FileList fileList = null;
        try {
            fileList = new FileList(Config.mWorkPath, "DB3");
        } catch (Exception ex) {
            Log.err(log, "slpash.Initial()", "search number of DB3 files", ex);
        }
        if ((fileList.size() > 1) && Config.MultiDBAsk.getValue() && !GlobalCore.restartAfterKill) {
            breakForWait = true;
            SelectDB selectDBDialog = new SelectDB(this, "SelectDbDialog", true);
            selectDBDialog.setReturnListener(new IReturnListener() {
                @Override
                public void back() {
                    returnFromSelectDB();
                }
            });
            selectDBDialog.show();
            selectDBDialog = null;
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
        Log.debug(log, "ini_CacheDB");
        // chk if exist filter preset splitter "#" and Replace
        String ConfigPreset = Config.UserFilter.getValue();
        if (ConfigPreset.endsWith("#")) {
            // Preset implements old splitter, replaced!

            ConfigPreset = ConfigPreset.substring(0, ConfigPreset.length() - 1) + SettingString.STRING_SPLITTER;

            boolean replace = true;
            while (replace) {
                String newConfigPreset = ReplaceSplitter(ConfigPreset);
                if (newConfigPreset == null)
                    replace = false;
                else
                    ConfigPreset = newConfigPreset;
            }
            ;
            Config.UserFilter.setValue(ConfigPreset);
            Config.AcceptChanges();
        }

        Database.Data.StartUp(Config.mWorkPath + "/" + Config.DatabaseName.getValue());

        Config.settings.ReadFromDB();

        FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());

        CoreSettingsForward.Categories = new Categories();
        Database.Data.GPXFilenameUpdateCacheCount();

        synchronized (Database.Data.Query) {
            CacheListDAO cacheListDAO = new CacheListDAO();
            cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
            cacheListDAO = null;
        }

        CacheListChangedEventList.Call();

        Database.FieldNotes.StartUp(Config.mWorkPath + "/User/FieldNotes.db3");

    }

    private String ReplaceSplitter(String ConfigPreset) {
        try {
            int pos = ConfigPreset.indexOf("#");
            int pos2 = ConfigPreset.indexOf(";", pos);

            String PresetName = (String) ConfigPreset.subSequence(pos + 1, pos2);
            if (!PresetName.contains(",")) {
                String s1 = (String) ConfigPreset.subSequence(0, pos);
                String s2 = (String) ConfigPreset.subSequence(pos2, ConfigPreset.length());

                ConfigPreset = s1 + SettingString.STRING_SPLITTER + PresetName + s2;
                return ConfigPreset;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    /**
     * Step 7 <br>
     * chk installed map packs/layers
     */
    private void ini_MapPacks() {
        Log.debug(log, "ini_MapPacks");
        ManagerBase.Manager.initMapPacks();
    }

    /**
     * Last Step <br>
     * Show TabMainView
     */
    private void ini_TabMainView() {
        Log.debug(log, "ini_TabMainView");
        GL.that.removeRenderView(this);
        GL.that.switchToMainView();

        if (GlobalCore.restartCache != null) {
            synchronized (Database.Data.Query) {
                Cache c = Database.Data.Query.GetCacheByGcCode(GlobalCore.restartCache);
                if (c != null) {
                    if (GlobalCore.restartWaypoint != null) {
                        WaypointDAO dao = new WaypointDAO();
                        CB_List<Waypoint> waypoints = dao.getWaypointsFromCacheID(c.Id, true);
                        if (waypoints != null) {
                            Waypoint w = null;

                            for (int i = 0, n = waypoints.size(); i < n; i++) {
                                Waypoint wp = waypoints.get(i);
                                if (wp.getGcCode().equalsIgnoreCase(GlobalCore.restartWaypoint)) {
                                    w = wp;
                                }
                            }
                            Log.debug(log, "ini_TabMainView: Set selectedCache to" + c.getGcCode() + " from restartCache + WP.");
                            GlobalCore.setSelectedWaypoint(c, w);
                        } else {
                            Log.debug(log, "ini_TabMainView: Set selectedCache to" + c.getGcCode() + " from restartCache.");
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
