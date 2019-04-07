/*
 * Copyright (C) 2014 team-cachebox.de
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

import CB_Locator.LocatorSettings;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.TrackRecorder;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.*;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Log.Log;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.rendertheme.*;
import org.mapsforge.map.rendertheme.rule.CB_RenderThemeHandler;

import java.util.*;

import static CB_Locator.Map.MapViewBase.INITIAL_WP_LIST;
import static CB_UI_Base.GL_UI.Menu.MenuID.MI_SHOW_AT_ORIGINAL_POSITION;

/**
 * @author Longri
 */
public class CB_Action_ShowMap extends CB_Action_ShowView {
    private static final String log = "CB_Action_ShowMap";
    private static final int START = 1;
    private static final int PAUSE = 2;
    private static final int STOP = 3;
    private static CB_Action_ShowMap that;
    private int menuID;
    private Menu mRenderThemesSelectionMenu;
    public MapView normalMapView;

    private CB_Action_ShowMap() {
        super("Map", MenuID.AID_SHOW_MAP);
        normalMapView = new MapView(TabMainView.leftTab.getContentRec(), MapMode.Normal);
        normalMapView.SetZoom(Config.lastZoomLevel.getValue());
    }

    public static CB_Action_ShowMap getInstance() {
        if (that == null) that = new CB_Action_ShowMap();
        return that;
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(normalMapView);
    }

    @Override
    public CB_View_Base getView() {
        return normalMapView;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.map.name());
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("menu_mapviewgl");
        icm.addItem(MenuID.MI_LAYER, "Layer");
        MenuItem mi = icm.addItem(MenuID.MI_RENDERTHEMES, "Renderthemes");
        if (LocatorSettings.RenderThemesFolder.getValue().length() == 0) {
            mi.setEnabled(false);
        }
        icm.addItem(MenuID.MI_MAPVIEW_OVERLAY_VIEW, "overlays");
        icm.addItem(MenuID.MI_MAPVIEW_VIEW, "view");
        icm.addCheckableItem(MenuID.MI_ALIGN_TO_COMPSS, "AlignToCompass", normalMapView.GetAlignToCompass());
        icm.addItem(MenuID.MI_CENTER_WP, "CenterWP");
        icm.addItem(MenuID.MI_TREC_REC, "RecTrack");
        icm.addOnClickListener((v, x, y, pointer, button) -> {
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_LAYER:
                    showMapLayerMenu();
                    return true;
                case MenuID.MI_RENDERTHEMES:
                    if (v.isEnabled()) {
                        return showModusSelectionMenu();
                    } else
                        return false;
                case MenuID.MI_MAPVIEW_OVERLAY_VIEW:
                    showMapOverlayMenu();
                    return true;
                case MenuID.MI_MAPVIEW_VIEW:
                    showMapViewLayerMenu();
                    return true;
                case MenuID.MI_ALIGN_TO_COMPSS:
                    normalMapView.SetAlignToCompass(!normalMapView.GetAlignToCompass());
                    return true;
                case MenuID.MI_CENTER_WP:
                        normalMapView.createWaypointAtCenter();
                    return true;
                case MenuID.MI_TREC_REC:
                    showMenuTrackRecording();
                    return true;
                default:
                    return false;
            }
        });
        return icm;
    }

    private void showMapLayerMenu() {
        Menu icm = new Menu("MapViewShowLayerContextMenu");

        // Sorting (perhaps use an arraylist of layers without the overlay layers)
        Collections.sort(ManagerBase.Manager.getLayers(), (layer1, layer2) -> layer1.Name.toLowerCase().compareTo(layer2.Name.toLowerCase()));

        int menuID = 0;
        String[] curentLayerNames = MapView.mapTileLoader.getCurrentLayer().getNames();
        for (Layer layer : ManagerBase.Manager.getLayers()) {
            if (!layer.isOverlay()) {
                MenuItem mi = icm.addItem(menuID++, "", layer.Name); // == friendlyName == FileName !!! ohne Translation
                mi.setData(layer);
                mi.setCheckable(true);

                //set icon (Online, Mapsforge or Freizeitkarte)
                Sprite sprite = null;
                switch (layer.getMapType()) {
                    case BITMAP:
                        break;
                    case FREIZEITKARTE:
                        sprite = Sprites.getSprite(IconName.freizeit.name());
                        break;
                    case MAPSFORGE:
                        sprite = Sprites.getSprite(IconName.mapsforge_logo.name());
                        break;
                    case ONLINE:
                        sprite = Sprites.getSprite(IconName.download.name());
                        break;
                    default:
                        break;
                }

                if (sprite != null)
                    mi.setIcon(new SpriteDrawable(sprite));

                for (String str : curentLayerNames) {
                    if (str.equals(layer.Name)) {
                        mi.setChecked(true);
                        break;
                    }
                }
            }
        }

        icm.addOnClickListener((v, x, y, pointer, button) -> {
            Layer layer = (Layer) v.getData();
            selectLayer(layer);
            showLanguageSelectionMenu(layer);
            return true;
        });

        icm.Show();
    }

    private void selectLayer(Layer layer) {
        if (layer.Name.equals(normalMapView.getCurrentLayer().Name)) {
            normalMapView.clearAdditionalLayers();
        } else {
            // if current layer is a Mapsforge map, it is possible to add the selected Mapsforge map to the current layer. We ask the User!
            if (MapView.mapTileLoader.getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
                MessageBox msgBox = MessageBox.show(Translation.get("AddOrChangeMap"), Translation.get("Layer"), MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question, new OnMsgBoxClickListener() {
                    @Override
                    public boolean onClick(int which, Object data) {
                        Layer layer = (Layer) data;
                        switch (which) {
                            case MessageBox.BUTTON_POSITIVE:
                                // add the selected map to the curent layer
                                normalMapView.addAdditionalLayer(layer);
                                break;
                            case MessageBox.BUTTON_NEUTRAL:
                                // switch curent layer to selected
                                normalMapView.setCurrentLayer(layer);
                                break;
                            default:
                                normalMapView.removeAdditionalLayer();
                        }
                        return true;
                    }
                });
                msgBox.setButtonText(1, "+");
                msgBox.setButtonText(2, "=");
                msgBox.setButtonText(3, "-");
                msgBox.setData(layer);
            } else {
                normalMapView.setCurrentLayer(layer);
            }
        }
    }

    private boolean showLanguageSelectionMenu(Layer layer) {
        boolean hasLanguage = false;
        if (layer.isMapsForge()) {
            if (layer.languages != null)
                if (layer.languages.length > 1) {
                    final Menu lsm = new Menu("lsm");
                    lsm.setTitle("Sprachauswahl");
                    int menuID = 0;
                    for (String lang : layer.languages) {
                        //MenuItem mi =
                        lsm.addItem(menuID++, "", lang); // ohne Translation
                        //mi.setData(which);
                    }
                    lsm.addOnClickListener((v, x, y, pointer, button) -> {
                        String selectedLanguage = ((MenuItem) v).getTitle();
                        Config.PreferredMapLanguage.setValue(selectedLanguage);
                        Config.AcceptChanges();
                        return true;
                    });
                    lsm.Show();
                    hasLanguage = true;
                }
        }
        return hasLanguage;
    }

    private void showMapOverlayMenu() {
        final OptionMenu icm = new OptionMenu("icm");

        int menuID = 0;
        for (Layer layer : ManagerBase.Manager.getLayers()) {
            if (layer.isOverlay()) {
                MenuItem mi = icm.addCheckableItem(menuID++, layer.FriendlyName, layer == MapView.mapTileLoader.getCurrentOverlayLayer());
                mi.setData(layer);
            }
        }

        icm.addOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                Layer layer = (Layer) ((MenuItem) v).getData();
                if (layer == MapView.mapTileLoader.getCurrentOverlayLayer()) {
                    // switch off Overlay
                    normalMapView.SetCurrentOverlayLayer(null);
                } else {
                    normalMapView.SetCurrentOverlayLayer(layer);
                }
                // Refresh menu
                icm.close();
                showMapOverlayMenu();
                return true;
            }
        });

        icm.Show();
    }

    private void showMapViewLayerMenu() {
        OptionMenu icm = new OptionMenu("MapViewShowLayerContextMenu");

        icm.addCheckableItem(MI_SHOW_AT_ORIGINAL_POSITION, "ShowAtOriginalPosition", Config.ShowAtOriginalPosition.getValue());
        icm.addCheckableItem(MenuID.MI_HIDE_FINDS, "HideFinds", Config.MapHideMyFinds.getValue());
        icm.addCheckableItem(MenuID.MI_MAP_SHOW_INFO, "MapShowCompass", Config.MapShowInfo.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_ALL_WAYPOINTS, "ShowAllWaypoints", Config.ShowAllWaypoints.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_RATINGS, "ShowRatings", Config.MapShowRating.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_DT, "ShowDT", Config.MapShowDT.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_TITLE, "ShowTitle", Config.MapShowTitles.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_DIRECT_LINE, "ShowDirectLine", Config.ShowDirektLine.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_ACCURACY_CIRCLE, "MenuTextShowAccuracyCircle", Config.ShowAccuracyCircle.getValue());
        icm.addCheckableItem(MenuID.MI_SHOW_CENTERCROSS, "ShowCenterCross", Config.ShowMapCenterCross.getValue());

        icm.addOnClickListener( (v, x, y, pointer, button) -> {
            switch (((MenuItem) v).getMenuItemId()) {
                case MI_SHOW_AT_ORIGINAL_POSITION:
                    toggleSettingWithReload(Config.ShowAtOriginalPosition);
                    return true;
                case MenuID.MI_HIDE_FINDS:
                    toggleSettingWithReload(Config.MapHideMyFinds);
                    return true;
                case MenuID.MI_MAP_SHOW_INFO:
                    toggleSetting(Config.MapShowInfo);
                    return true;
                case MenuID.MI_SHOW_ALL_WAYPOINTS:
                    toggleSetting(Config.ShowAllWaypoints);
                    return true;
                case MenuID.MI_SHOW_RATINGS:
                    toggleSetting(Config.MapShowRating);
                    return true;
                case MenuID.MI_SHOW_DT:
                    toggleSetting(Config.MapShowDT);
                    return true;
                case MenuID.MI_SHOW_TITLE:
                    toggleSetting(Config.MapShowTitles);
                    return true;
                case MenuID.MI_SHOW_DIRECT_LINE:
                    toggleSetting(Config.ShowDirektLine);
                    return true;
                case MenuID.MI_SHOW_ACCURACY_CIRCLE:
                    toggleSetting(Config.ShowAccuracyCircle);
                    return true;
                case MenuID.MI_SHOW_CENTERCROSS:
                    toggleSetting(Config.ShowMapCenterCross);
                    return true;
                default:
                    return false;
            }
        });
        icm.Show();
    }

    private void showMenuTrackRecording() {
        MenuItem mi;
        Menu cm2 = new Menu("TrackRecordContextMenu");
        cm2.addOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switch (((MenuItem) v).getMenuItemId()) {
                    case START:
                        TrackRecorder.StartRecording();
                        return true;
                    case PAUSE:
                        TrackRecorder.PauseRecording();
                        return true;
                    case STOP:
                        TrackRecorder.StopRecording();
                        return true;
                }
                return false;
            }
        });
        mi = cm2.addItem(START, "start");
        mi.setEnabled(!TrackRecorder.recording);

        if (TrackRecorder.pauseRecording)
            mi = cm2.addItem(PAUSE, "continue");
        else
            mi = cm2.addItem(PAUSE, "pause");

        mi.setEnabled(TrackRecorder.recording);

        mi = cm2.addItem(STOP, "stop");
        mi.setEnabled(TrackRecorder.recording | TrackRecorder.pauseRecording);

        cm2.Show();
    }

    private void toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        normalMapView.setNewSettings(MapView.INITIAL_SETTINGS_WITH_OUT_ZOOM);
    }

    private void toggleSettingWithReload(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        normalMapView.setNewSettings(INITIAL_WP_LIST);
    }

    private HashMap<String, String> getRenderThemes() {
        HashMap<String, String> files = new HashMap<String, String>();
        String directory = LocatorSettings.RenderThemesFolder.getValue();
        if (directory.length() > 0) {
            files.putAll(getDirsRenderThemes(directory));
        }
        return files;
    }

    private HashMap<String, String> getDirsRenderThemes(String directory) {
        HashMap<String, String> files = new HashMap<>();
        File dir = FileFactory.createFile(directory);
        String[] dirFiles = dir.list();
        if (dirFiles != null && dirFiles.length > 0) {
            for (String tmp : dirFiles) {
                File f = FileFactory.createFile(directory + "/" + tmp);
                if (f.isDirectory()) {
                    files.putAll(getDirsRenderThemes(f.getAbsolutePath()));
                } else {
                    String ttt = tmp.toLowerCase();
                    if (ttt.endsWith("xml")) {
                        files.put(FileIO.GetFileNameWithoutExtension(tmp), f.getAbsolutePath());
                    }
                }
            }
        }
        return files;
    }

    private boolean showModusSelectionMenu() {
        final Menu lRenderThemesMenu = new OptionMenu("RenderThemesMenu");
        lRenderThemesMenu.addItem(0, "RenderThemesDay");
        lRenderThemesMenu.addItem(1, "RenderThemesNight");
        lRenderThemesMenu.addItem(2, "RenderThemesCarDay");
        lRenderThemesMenu.addItem(3, "RenderThemesCarNight");

        lRenderThemesMenu.addOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                return showRenderThemesSelectionMenu(((MenuItem) v).getMenuItemId());
            }
        });

        lRenderThemesMenu.Show();
        return true;
    }

    private void addRenderTheme(String theme, String PaN, int which) {
        MenuItem mi = mRenderThemesSelectionMenu.addItem(menuID++, "", theme); // ohne Translation
        mi.setData(which);
        mi.setCheckable(true);
        String compare = ""; // Theme is saved with path
        switch (which) {
            case 0:
                compare = Config.MapsforgeDayTheme.getValue();
                break;
            case 1:
                compare = Config.MapsforgeNightTheme.getValue();
                break;
            case 2:
                compare = Config.MapsforgeCarDayTheme.getValue();
                break;
            case 3:
                compare = Config.MapsforgeCarNightTheme.getValue();
                break;
        }
        if (compare.equals(PaN)) {
            mi.setChecked(true);
        }
    }

    private boolean showRenderThemesSelectionMenu(int which) {

        mRenderThemesSelectionMenu = new Menu("RenderThemesSubMenu");
        final HashMap<String, String> RenderThemes = getRenderThemes();
        int menuID = 0;
        addRenderTheme(ManagerBase.INTERNAL_THEME_DEFAULT, ManagerBase.INTERNAL_THEME_DEFAULT, which);
        ArrayList<String> themes = new ArrayList<String>();
        for (String theme : RenderThemes.keySet()) themes.add(theme);
        Collections.sort(themes, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
        });
        for (String theme : themes) {
            addRenderTheme(theme, RenderThemes.get(theme), which);
        }
        addRenderTheme(ManagerBase.INTERNAL_THEME_CAR, ManagerBase.INTERNAL_THEME_CAR, which);
        addRenderTheme(ManagerBase.INTERNAL_THEME_OSMARENDER, ManagerBase.INTERNAL_THEME_OSMARENDER, which);

        // for showStyleSelection to work
        RenderThemes.put(ManagerBase.INTERNAL_THEME_DEFAULT, ManagerBase.INTERNAL_THEME_DEFAULT);
        RenderThemes.put(ManagerBase.INTERNAL_THEME_CAR, ManagerBase.INTERNAL_THEME_CAR);
        RenderThemes.put(ManagerBase.INTERNAL_THEME_OSMARENDER, ManagerBase.INTERNAL_THEME_OSMARENDER);

        mRenderThemesSelectionMenu.addOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                showStyleSelection((int) ((MenuItem) v).getData(), RenderThemes.get(((MenuItem) v).getTitle()));
                return true;
            }
        });

        mRenderThemesSelectionMenu.Show();
        return true;
    }

    private void showStyleSelection(int which, String selectedTheme) {
        final Menu lStyle = new Menu("Style");

        int menuID = 0;
        // getThemeStyles works only for External Themes
        // Internal Themes have no XmlRenderThemeMenuCallback
        HashMap<String, String> ThemeStyles = getThemeStyles(selectedTheme);
        String ThemeStyle = "";
        for (String style : ThemeStyles.keySet()) {
            MenuItem mi = lStyle.addItem(menuID++, "", style); // ohne Translation
            ThemeStyle = ThemeStyles.get(style);
            mi.setData(ThemeStyle + "|" + which + "|" + selectedTheme);
            //mi.setCheckable(true);
        }

        lStyle.addOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                MenuItem mi = (MenuItem) v;
                String values[] = ((String) mi.getData()).split("\\|");
                HashMap<String, String> StyleOverlays = getStyleOverlays(values[2], values[0]);
                String ConfigStyle = getStyleFromConfig(values[1]);
                if (!ConfigStyle.startsWith(values[0])) {
                    // Config one is not for this layer
                    ConfigStyle = "";
                }
                showOverlaySelection((String) mi.getData(), StyleOverlays, ConfigStyle);
                return true;
            }
        });

        if (ThemeStyles.size() > 1) {
            lStyle.Show();
        } else if (ThemeStyles.size() == 1) {
            HashMap<String, String> StyleOverlays = getStyleOverlays(selectedTheme, ThemeStyle);
            String ConfigStyle = getStyleFromConfig("" + which);
            if (!ConfigStyle.startsWith(ThemeStyle)) {
                // Config one is not for this layer
                ConfigStyle = "";
            }
            showOverlaySelection(ThemeStyle + "|" + which + "|" + selectedTheme, StyleOverlays, ConfigStyle);
        } else {
            // there is no style (p.ex. internal Theme)
            // style of Config will be ignored while setting of Theme
            setConfig("|" + which + "|" + selectedTheme);
            Config.AcceptChanges();
        }
    }

    private HashMap<String, String> getThemeStyles(String selectedTheme) {
        if (selectedTheme.length() > 0) {
            try {
                XmlRenderThemeMenuCallback getStylesCallBack = new GetStylesCallback();
                XmlRenderTheme renderTheme = new ExternalRenderTheme(selectedTheme, getStylesCallBack);
                try {
                    // parse RenderTheme to get XmlRenderThemeMenuCallback getCategories called
                    CB_RenderThemeHandler.getRenderTheme(ManagerBase.Manager.getGraphicFactory(ManagerBase.Manager.DISPLAY_MODEL.getScaleFactor()), new DisplayModel(), renderTheme);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
                return ((GetStylesCallback) getStylesCallBack).getStyles();
            } catch (Exception e) {
            }
        }
        return new HashMap<String, String>();
    }

    private void showOverlaySelection(String values, HashMap<String, String> StyleOverlays, String ConfigStyle) {
        final Menu lOverlay = new OptionMenu("StyleOverlay");

        int menuID = 0;
        for (String overlay : StyleOverlays.keySet()) {
            MenuItem mi = lOverlay.addItem(menuID++, "", overlay); // ohne Translation
            String overlayID = StyleOverlays.get(overlay);
            boolean overlayEnabled = overlayID.startsWith("+");
            if (!(ConfigStyle.indexOf(overlayID) > -1)) {
                if (ConfigStyle.indexOf(overlayID.substring(1)) > -1) {
                    overlayEnabled = !overlayEnabled;
                }
            }
            if (overlayEnabled)
                overlayID = "+" + overlayID.substring(1);
            else
                overlayID = "-" + overlayID.substring(1);
            mi.setData(values + "|" + overlayID);
            mi.setCheckable(true);
            mi.setChecked(overlayEnabled);
        }

        lOverlay.addOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                MenuItem mi = (MenuItem) v;
                String values[] = ((String) mi.getData()).split("\\|");
                if (mi.isChecked()) {
                    values[3] = "+" + values[3].substring(1);
                } else {
                    values[3] = "-" + values[3].substring(1);
                }
                mi.setData(values[0] + "|" + values[1] + "|" + values[2] + "|" + values[3]);
                lOverlay.setData(concatValues(values, lOverlay));
                lOverlay.Show();
                return true;
            }
        });

        lOverlay.mMsgBoxClickListener = new OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                setConfig((String) data);
                Config.AcceptChanges();
                return true;
            }
        };

        if (StyleOverlays.size() > 0) {
            lOverlay.setData(concatValues(values.split("\\|"), lOverlay));
            lOverlay.Show();
        } else {
            // save the values, there is perhaps no overlay
            setConfig(values);
            Config.AcceptChanges();
        }
    }

    private String concatValues(String values[], Menu lOverlay) {
        String result = values[0];
        for (MenuItemBase mitm : lOverlay.mItems) {
            String data[] = ((String) mitm.getData()).split("\\|");
            result = result + "\t" + data[3];
        }
        return result + "|" + values[1] + "|" + values[2];
    }

    private String getStyleFromConfig(String which) {
        switch (which) {
            case "0":
                return Config.MapsforgeDayStyle.getValue();
            case "1":
                return Config.MapsforgeNightStyle.getValue();
            case "2":
                return Config.MapsforgeCarDayStyle.getValue();
            case "3":
                return Config.MapsforgeCarNightStyle.getValue();
        }
        return "";
    }

    private void setConfig(String _StyleAndTheme) {
        String values[] = ((String) _StyleAndTheme).split("\\|");
        switch (values[1]) {
            case "0":
                Config.MapsforgeDayStyle.setValue(values[0]);
                Config.MapsforgeDayTheme.setValue(values[2]);
                break;
            case "1":
                Config.MapsforgeNightStyle.setValue(values[0]);
                Config.MapsforgeNightTheme.setValue(values[2]);
                break;
            case "2":
                Config.MapsforgeCarDayStyle.setValue(values[0]);
                Config.MapsforgeCarDayTheme.setValue(values[2]);
                break;
            case "3":
                Config.MapsforgeCarNightStyle.setValue(values[0]);
                Config.MapsforgeCarNightTheme.setValue(values[2]);
                break;
        }
    }

    private HashMap<String, String> getStyleOverlays(String selectedTheme, String selectedLayer) {
        if (selectedTheme.length() > 0) {
            try {
                OverlaysCallback getOverlaysCallback = new GetOverlaysCallback();
                XmlRenderTheme renderTheme = new ExternalRenderTheme(selectedTheme, getOverlaysCallback);
                getOverlaysCallback.setLayer(selectedLayer);
                try {
                    // parse RenderTheme to get XmlRenderThemeMenuCallback getCategories called
                    CB_RenderThemeHandler.getRenderTheme(ManagerBase.Manager.getGraphicFactory(ManagerBase.Manager.DISPLAY_MODEL.getScaleFactor()), new DisplayModel(), renderTheme);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
                return getOverlaysCallback.getOverlays();
            } catch (Exception e) {
            }
        }
        return new HashMap<String, String>();
    }

    interface OverlaysCallback extends XmlRenderThemeMenuCallback {
        public HashMap<String, String> getOverlays();

        public void setLayer(String layer);
    }

    private class GetStylesCallback implements XmlRenderThemeMenuCallback {
        private HashMap<String, String> styles;

        @Override
        public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
            styles = new HashMap<String, String>();
            Map<String, XmlRenderThemeStyleLayer> styleLayers = style.getLayers();

            for (XmlRenderThemeStyleLayer styleLayer : styleLayers.values()) {
                if (styleLayer.isVisible()) {
                    styles.put(styleLayer.getTitle(Translation.get("Language2Chars").toLowerCase()), styleLayer.getId());
                }
            }

            return null;
        }

        public HashMap<String, String> getStyles() {
            if (styles == null) {
                styles = new HashMap<String, String>();
            }
            return styles;
        }
    }

    private class GetOverlaysCallback implements OverlaysCallback {
        public String selectedLayer;
        private HashMap<String, String> overlays;

        @Override
        public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
            overlays = new HashMap<String, String>();
            XmlRenderThemeStyleLayer selected_Layer = style.getLayer(selectedLayer);
            for (XmlRenderThemeStyleLayer overlay : selected_Layer.getOverlays()) {
                if (overlay.isEnabled()) {
                    overlays.put(overlay.getTitle(Translation.get("Language2Chars")), "+" + overlay.getId());
                } else {
                    overlays.put(overlay.getTitle(Translation.get("Language2Chars")), "-" + overlay.getId());
                }
            }

            return null;
        }

        @Override
        public HashMap<String, String> getOverlays() {
            return overlays;
        }

        @Override
        public void setLayer(String layer) {
            selectedLayer = layer;
        }
    }

}
