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

import CB_Core.Import.UnZip;
import CB_Locator.LocatorSettings;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.TrackRecorder;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.*;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Log.Log;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.http.Download;
import CB_Utils.http.Webb;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.rendertheme.*;
import org.mapsforge.map.rendertheme.rule.CB_RenderThemeHandler;

import java.io.ByteArrayInputStream;
import java.util.*;

import static CB_Locator.Map.MapViewBase.INITIAL_WP_LIST;

/**
 * @author Longri
 */
public class CB_Action_ShowMap extends CB_Action_ShowView {
    private static final String log = "CB_Action_ShowMap";
    private static CB_Action_ShowMap that;
    public MapView normalMapView;
    private Menu mRenderThemesSelectionMenu;
    private HashMap<String, String> RenderThemes;
    private OptionMenu mapViewThemeMenu;
    private String themesPath;
    private FZKThemesInfo fzkThemesInfo;
    private Array<FZKThemesInfo> fzkThemesInfoList = new Array<>();

    private CB_Action_ShowMap() {
        super("Map", MenuID.AID_SHOW_MAP);
        normalMapView = new MapView(ViewManager.leftTab.getContentRec(), MapMode.Normal);
        normalMapView.SetZoom(Config.lastZoomLevel.getValue());
    }

    public static CB_Action_ShowMap getInstance() {
        if (that == null) that = new CB_Action_ShowMap();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(normalMapView);
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
        Menu icm = new Menu("MapViewContextMenuTitle");
        icm.addMenuItem("Layer", null, this::showMapLayerMenu);
        MenuItem mi = icm.addMenuItem("Renderthemes", null, this::showModusSelectionMenu);
        if (LocatorSettings.RenderThemesFolder.getValue().length() == 0) {
            mi.setEnabled(false);
        }
        icm.addMenuItem("overlays", null, this::showMapOverlayMenu);
        icm.addMenuItem("view", null, this::showMapViewLayerMenu);
        icm.addCheckableMenuItem("AlignToCompass", normalMapView.GetAlignToCompass(), () -> normalMapView.SetAlignToCompass(!normalMapView.GetAlignToCompass()));
        icm.addMenuItem("CenterWP", null, () -> normalMapView.createWaypointAtCenter());
        icm.addMenuItem("RecTrack", null, this::showMenuTrackRecording);
        return icm;
    }

    private void showMapLayerMenu() {
        Menu icm = new Menu("MapViewLayerMenuTitle");

        // Sorting (perhaps use an arraylist of layers without the overlay layers)
        Collections.sort(ManagerBase.Manager.getLayers(), (layer1, layer2) -> layer1.Name.toLowerCase().compareTo(layer2.Name.toLowerCase()));

        String[] curentLayerNames = MapView.mapTileLoader.getCurrentLayer().getNames();
        for (Layer layer : ManagerBase.Manager.getLayers()) {
            if (!layer.isOverlay()) {
                //set icon (Online, Mapsforge or Freizeitkarte)
                Sprite sprite = null;
                switch (layer.getMapType()) {
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
                        // case BITMAP:
                        break;
                }

                MenuItem mi = icm.addMenuItem("", layer.Name,
                        (sprite == null) ? null : new SpriteDrawable(sprite),
                        (v, x, y, pointer, button) -> {
                            icm.close();
                            Layer layer1 = (Layer) v.getData();
                            selectLayer(layer1);
                            showLanguageSelectionMenu(layer1);
                            return true;
                        }); // == friendlyName == FileName !!! ohne Translation
                mi.setData(layer);
                mi.setCheckable(true);
                for (String str : curentLayerNames) {
                    if (str.equals(layer.Name)) {
                        mi.setChecked(true);
                        break;
                    }
                }
            }
        }

        icm.Show();
    }

    private void selectLayer(Layer layer) {
        if (layer.Name.equals(normalMapView.getCurrentLayer().Name)) {
            normalMapView.clearAdditionalLayers();
        } else {
            // if current layer is a Mapsforge map, it is possible to add the selected Mapsforge map to the current layer. We ask the User!
            if (MapView.mapTileLoader.getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
                MessageBox msgBox = MessageBox.show(
                        Translation.get("AddOrChangeMap"),
                        Translation.get("Layer"),
                        MessageBoxButtons.YesNoCancel,
                        MessageBoxIcon.Question,
                        (which, data) -> {
                            Layer layer1 = (Layer) data;
                            switch (which) {
                                case MessageBox.BUTTON_POSITIVE:
                                    // add the selected map to the curent layer
                                    normalMapView.addAdditionalLayer(layer1);
                                    break;
                                case MessageBox.BUTTON_NEUTRAL:
                                    // switch curent layer to selected
                                    normalMapView.setCurrentLayer(layer1);
                                    break;
                                default:
                                    normalMapView.removeAdditionalLayer();
                            }
                            return true;
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
                    final Menu lsm = new Menu("MapViewLayerSelectLanguageTitle");
                    for (String lang : layer.languages) {
                        lsm.addMenuItem("", lang, null, (v, x, y, pointer, button) -> {
                            lsm.close();
                            String selectedLanguage = ((MenuItem) v).getTitle();
                            Config.PreferredMapLanguage.setValue(selectedLanguage);
                            Config.AcceptChanges();
                            return true;
                        });
                    }
                    lsm.Show();
                    hasLanguage = true;
                }
        }
        return hasLanguage;
    }

    private void showMapOverlayMenu() {
        final OptionMenu icm = new OptionMenu("MapViewOverlayMenuTitle");
        icm.setSingleSelection();
        for (Layer layer : ManagerBase.Manager.getLayers()) {
            if (layer.isOverlay()) {
                MenuItem mi = icm.addMenuItem(layer.FriendlyName, "", null,
                        (v, x, y, pointer, button) -> {
                            Layer layer1 = (Layer) v.getData();
                            if (layer1 == MapView.mapTileLoader.getCurrentOverlayLayer()) {
                                // switch off Overlay
                                normalMapView.SetCurrentOverlayLayer(null);
                            } else {
                                normalMapView.SetCurrentOverlayLayer(layer1);
                            }
                            icm.tickCheckBoxes((MenuItem) v);
                            return true;
                        });
                mi.setCheckable(true);
                mi.setChecked(layer == MapView.mapTileLoader.getCurrentOverlayLayer());
                mi.setData(layer);
            }
        }
        icm.Show();
    }

    private void showMapViewLayerMenu() {
        OptionMenu menuMapElements = new OptionMenu("MapViewLayerMenuTitle");
        menuMapElements.addCheckableMenuItem("ShowAtOriginalPosition", Config.ShowAtOriginalPosition.getValue(), () -> toggleSettingWithReload(Config.ShowAtOriginalPosition));
        menuMapElements.addCheckableMenuItem("HideFinds", Config.MapHideMyFinds.getValue(), () -> toggleSettingWithReload(Config.MapHideMyFinds));
        menuMapElements.addCheckableMenuItem("MapShowInfoBar", Config.MapShowInfo.getValue(), () -> toggleSetting(Config.MapShowInfo));
        menuMapElements.addCheckableMenuItem("ShowAllWaypoints", Config.ShowAllWaypoints.getValue(), () -> toggleSetting(Config.ShowAllWaypoints));
        menuMapElements.addCheckableMenuItem("ShowRatings", Config.MapShowRating.getValue(), () -> toggleSetting(Config.MapShowRating));
        menuMapElements.addCheckableMenuItem("ShowDT", Config.MapShowDT.getValue(), () -> toggleSetting(Config.MapShowDT));
        menuMapElements.addCheckableMenuItem("ShowTitle", Config.MapShowTitles.getValue(), () -> toggleSetting(Config.MapShowTitles));
        menuMapElements.addCheckableMenuItem("ShowDirectLine", Config.ShowDirektLine.getValue(), () -> toggleSetting(Config.ShowDirektLine));
        menuMapElements.addCheckableMenuItem("MenuTextShowAccuracyCircle", Config.ShowAccuracyCircle.getValue(), () -> toggleSetting(Config.ShowAccuracyCircle));
        menuMapElements.addCheckableMenuItem("ShowCenterCross", Config.ShowMapCenterCross.getValue(), () -> toggleSetting(Config.ShowMapCenterCross));
        menuMapElements.Show();
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

    private void showMenuTrackRecording() {
        Menu cm2 = new Menu("TrackRecordMenuTitle");
        cm2.addMenuItem("start", null, TrackRecorder::StartRecording).setEnabled(!TrackRecorder.recording);
        if (TrackRecorder.pauseRecording)
            cm2.addMenuItem("continue", null, TrackRecorder::PauseRecording).setEnabled(TrackRecorder.recording);
        else
            cm2.addMenuItem("pause", null, TrackRecorder::PauseRecording).setEnabled(TrackRecorder.recording);
        cm2.addMenuItem("stop", null, TrackRecorder::StopRecording).setEnabled(TrackRecorder.recording | TrackRecorder.pauseRecording);
        cm2.Show();
    }

    private HashMap<String, String> getRenderThemes() {
        HashMap<String, String> files = new HashMap<>();
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
        mapViewThemeMenu = new OptionMenu("MapViewThemeMenuTitle");
        mapViewThemeMenu.addMenuItem("RenderThemesDay", null, () -> showRenderThemesSelectionMenu(0));
        mapViewThemeMenu.addMenuItem("RenderThemesNight", null, () -> showRenderThemesSelectionMenu(1));
        mapViewThemeMenu.addMenuItem("RenderThemesCarDay", null, () -> showRenderThemesSelectionMenu(2));
        mapViewThemeMenu.addMenuItem("RenderThemesCarNight", null, () -> showRenderThemesSelectionMenu(3));

        themesPath = LocatorSettings.RenderThemesFolder.getValue();
        // only download, if writable
        boolean isWritable = false;
        if (themesPath.length() > 0)
            isWritable = FileIO.canWrite(themesPath);
        if (isWritable) {
            mapViewThemeMenu.addDivider();

            final String target = themesPath + "/Elevate4.zip";
            if (themesPath.length() > 0) {
                mapViewThemeMenu.addMenuItem("Download", "\n OpenAndroMaps", Sprites.getSprite(IconName.mapsforge_logo.name()),
                        (v, x, y, pointer, button) -> {
                            GL.that.postAsync(() -> {
                                ((MenuItem) v).setDisabled(false);
                                GL.that.renderOnce();
                                Download.Download("http://download.openandromaps.org/themes/Elevate4.zip", target);
                                try {
                                    UnZip.extractFolder(target, false);
                                } catch (Exception ex) {
                                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                                }
                                Gdx.files.absolute(target).delete();
                                ((MenuItem) v).setDisabled(true);
                                GL.that.renderOnce();
                            });
                            return true;
                        });
            }

            addDownloadFZKRenderThemes();
        } else {
            if (!Config.RememberAsk_RenderThemePathWritable.getValue()) {
                mapViewThemeMenu.addDivider();
                mapViewThemeMenu.addMenuItem("Download", null, () -> {
                    // MakeRenderThemePathWritable
                    MessageBox.show(Translation.get("MakeRenderThemePathWritable"), Translation.get("Download"), MessageBoxButtons.YesNo, MessageBoxIcon.Hand, (btnNumber, data) -> {
                        if (btnNumber == 1) { // change path
                            Config.RenderThemesFolder.setValue(Config.RenderThemesFolder.getDefaultValue());
                            Config.AcceptChanges();
                        }
                        return true;
                    }, Config.RememberAsk_RenderThemePathWritable);
                });
            }
        }

        mapViewThemeMenu.Show();
        return true;
    }

    private void addDownloadFZKRenderThemes() {
        GL.that.postAsync(() -> {
            if (fzkThemesInfoList.size == 0) {
                String repository_freizeitkarte_android = Webb.create()
                        .get("http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml")
                        .readTimeout(Config.socket_timeout.getValue())
                        .ensureSuccess()
                        .asString()
                        .getBody();
                java.util.Map<String, String> values = new HashMap<>();
                System.setProperty("sjxp.namespaces", "false");
                Array<IRule<Map<String, String>>> ruleList = createRepositoryRules(new Array<>());
                XMLParser<Map<String, String>> parserCache = new XMLParser<>(ruleList.toArray(IRule.class));
                parserCache.parse(new ByteArrayInputStream(repository_freizeitkarte_android.getBytes()), values);
            }

            for (FZKThemesInfo fzkThemesInfo : fzkThemesInfoList) {
                mapViewThemeMenu.addMenuItem("Download", "\n" + fzkThemesInfo.Description, Sprites.getSprite(Sprites.IconName.freizeit.name()),
                        (v, x, y, pointer, button) -> {
                            GL.that.postAsync(() -> {
                                ((MenuItem) v).setDisabled(false);
                                GL.that.renderOnce();
                                String zipFile = fzkThemesInfo.Url.substring(fzkThemesInfo.Url.lastIndexOf("/") + 1);
                                String target = themesPath + "/" + zipFile;

                                Download.Download(fzkThemesInfo.Url, target);
                                try {
                                    UnZip.extractFolder(target);
                                } catch (Exception ex) {
                                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                                }
                                Gdx.files.absolute(target).delete();
                                ((MenuItem) v).setDisabled(true);
                                GL.that.renderOnce();
                            });
                            return true;
                        });
            }
        });
    }

    private Array<IRule<java.util.Map<String, String>>> createRepositoryRules(Array<IRule<java.util.Map<String, String>>> ruleList) {
        ruleList.add(new DefaultRule<Map<String, String>>(IRule.Type.CHARACTER, "/Freizeitkarte/Theme/Name") {
            @Override
            public void handleParsedCharacters(XMLParser<java.util.Map<String, String>> parser, String text, java.util.Map<String, String> values) {
                fzkThemesInfo.Name = text;
            }
        });

        if (Config.Sel_LanguagePath.getValue().contains("/de/")) {
            ruleList.add(new DefaultRule<java.util.Map<String, String>>(IRule.Type.CHARACTER, "/Freizeitkarte/Theme/DescriptionGerman") {
                @Override
                public void handleParsedCharacters(XMLParser<java.util.Map<String, String>> parser, String text, java.util.Map<String, String> values) {
                    fzkThemesInfo.Description = text;
                }
            });
        } else {
            ruleList.add(new DefaultRule<java.util.Map<String, String>>(IRule.Type.CHARACTER, "/Freizeitkarte/Theme/DescriptionEnglish") {
                @Override
                public void handleParsedCharacters(XMLParser<java.util.Map<String, String>> parser, String text, java.util.Map<String, String> values) {
                    fzkThemesInfo.Description = text;
                }
            });
        }

        ruleList.add(new DefaultRule<java.util.Map<String, String>>(IRule.Type.CHARACTER, "/Freizeitkarte/Theme/Url") {
            @Override
            public void handleParsedCharacters(XMLParser<java.util.Map<String, String>> parser, String text, java.util.Map<String, String> values) {
                fzkThemesInfo.Url = text;
            }
        });

        ruleList.add(new DefaultRule<java.util.Map<String, String>>(IRule.Type.CHARACTER, "/Freizeitkarte/Theme/Size") {
            @Override
            public void handleParsedCharacters(XMLParser<java.util.Map<String, String>> parser, String text, java.util.Map<String, String> values) {
                fzkThemesInfo.Size = Integer.parseInt(text);
            }
        });

        ruleList.add(new DefaultRule<java.util.Map<String, String>>(IRule.Type.CHARACTER, "/Freizeitkarte/Theme/Checksum") {
            @Override
            public void handleParsedCharacters(XMLParser<java.util.Map<String, String>> parser, String text, java.util.Map<String, String> values) {
                fzkThemesInfo.MD5 = text;
            }
        });

        ruleList.add(new DefaultRule<java.util.Map<String, String>>(IRule.Type.TAG, "/Freizeitkarte/Theme") {
            @Override
            public void handleTag(XMLParser<java.util.Map<String, String>> parser, boolean isStartTag, java.util.Map<String, String> values) {
                if (isStartTag) {
                    fzkThemesInfo = new FZKThemesInfo();
                } else {
                    fzkThemesInfoList.add(fzkThemesInfo);
                }
            }
        });
        return ruleList;
    }

    private boolean showRenderThemesSelectionMenu(int which) {

        mRenderThemesSelectionMenu = new Menu("MapViewThemeMenuTitle");
        RenderThemes = getRenderThemes();

        addRenderTheme(ManagerBase.INTERNAL_THEME_DEFAULT, ManagerBase.INTERNAL_THEME_DEFAULT, which);
        ArrayList<String> themes = new ArrayList<>(RenderThemes.keySet());
        Collections.sort(themes, (s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase()));
        for (String theme : themes) {
            addRenderTheme(theme, RenderThemes.get(theme), which);
        }
        addRenderTheme(ManagerBase.INTERNAL_THEME_CAR, ManagerBase.INTERNAL_THEME_CAR, which);
        addRenderTheme(ManagerBase.INTERNAL_THEME_OSMARENDER, ManagerBase.INTERNAL_THEME_OSMARENDER, which);

        // for showStyleSelection to work
        RenderThemes.put(ManagerBase.INTERNAL_THEME_DEFAULT, ManagerBase.INTERNAL_THEME_DEFAULT);
        RenderThemes.put(ManagerBase.INTERNAL_THEME_CAR, ManagerBase.INTERNAL_THEME_CAR);
        RenderThemes.put(ManagerBase.INTERNAL_THEME_OSMARENDER, ManagerBase.INTERNAL_THEME_OSMARENDER);

        mRenderThemesSelectionMenu.Show();
        return true;
    }

    private void addRenderTheme(String theme, String PaN, int which) {
        MenuItem mi = mRenderThemesSelectionMenu.addMenuItem("", theme, null,
                (v, x, y, pointer, button) -> {
                    mRenderThemesSelectionMenu.close();
                    showStyleSelection((int) v.getData(), ((MenuItem) v).getTitle());
                    return true;
                }); // ohne Translation
        mi.setData(which);
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
        mi.setCheckable(true);
        if (compare.equals(PaN)) {
            mi.setChecked(true);
        }
    }

    private void showStyleSelection(int which, String selected) {
        String theTheme = RenderThemes.get(selected);
        final Menu menuStyle = new Menu("Styles");

        // getThemeStyles works only for External Themes
        // Internal Themes have no XmlRenderThemeMenuCallback
        HashMap<String, String> ThemeStyles = getThemeStyles(theTheme);
        String ThemeStyle = "";
        for (String style : ThemeStyles.keySet()) {
            MenuItem mi = menuStyle.addMenuItem("", style, null, (v, x, y, pointer, button) -> {
                menuStyle.close();
                MenuItem clickedItem = (MenuItem) v;
                String[] values = ((String) clickedItem.getData()).split("\\|");
                HashMap<String, String> StyleOverlays = getStyleOverlays(values[2], values[0]);
                String ConfigStyle = getStyleFromConfig(values[1]);
                if (!ConfigStyle.startsWith(values[0])) {
                    // Config one is not for this layer
                    ConfigStyle = "";
                }
                showOverlaySelection((String) clickedItem.getData(), StyleOverlays, ConfigStyle);
                return true;
            }); // ohne Translation
            ThemeStyle = ThemeStyles.get(style);
            mi.setData(ThemeStyle + "|" + which + "|" + theTheme);
            //mi.setCheckable(true);
        }

        if (ThemeStyles.size() > 1) {
            menuStyle.Show();
        } else if (ThemeStyles.size() == 1) {
            HashMap<String, String> StyleOverlays = getStyleOverlays(theTheme, ThemeStyle);
            String ConfigStyle = getStyleFromConfig("" + which);
            if (!ConfigStyle.startsWith(ThemeStyle)) {
                // Config one is not for this layer
                ConfigStyle = "";
            }
            showOverlaySelection(ThemeStyle + "|" + which + "|" + theTheme, StyleOverlays, ConfigStyle);
        } else {
            // there is no style (p.ex. internal Theme)
            // style of Config will be ignored while setting of Theme
            setConfig("|" + which + "|" + theTheme);
            Config.AcceptChanges();
        }
    }

    private HashMap<String, String> getThemeStyles(String selectedTheme) {
        if (selectedTheme.length() > 0) {
            try {
                GetStylesCallback getStylesCallBack = new GetStylesCallback();
                XmlRenderTheme renderTheme = new ExternalRenderTheme(selectedTheme, getStylesCallBack);
                try {
                    // parse RenderTheme to get XmlRenderThemeMenuCallback getCategories called
                    CB_RenderThemeHandler.getRenderTheme(ManagerBase.Manager.getGraphicFactory(ManagerBase.Manager.DISPLAY_MODEL.getScaleFactor()), new DisplayModel(), renderTheme);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
                return getStylesCallBack.getStyles();
            } catch (Exception ignored) {
            }
        }
        return new HashMap<>();
    }

    private void showOverlaySelection(String values, HashMap<String, String> StyleOverlays, String ConfigStyle) {
        final Menu menuStyleOverlay = new OptionMenu("MapViewThemeStyleMenuTitle");
        for (String overlay : StyleOverlays.keySet()) {
            MenuItem mi = menuStyleOverlay.addMenuItem("", overlay, null, (v, x, y, pointer, button) -> {
                MenuItem clickedItem = (MenuItem) v;
                menuStyleOverlay.tickCheckBoxes(clickedItem); // the new value of clickedItem must be set first
                String[] clickedValues = ((String) clickedItem.getData()).split("\\|");
                if (clickedItem.isChecked()) {
                    clickedValues[3] = "+" + clickedValues[3].substring(1);
                } else {
                    clickedValues[3] = "-" + clickedValues[3].substring(1);
                }
                clickedItem.setData(clickedValues[0] + "|" + clickedValues[1] + "|" + clickedValues[2] + "|" + clickedValues[3]);
                menuStyleOverlay.setData(concatValues(clickedValues, menuStyleOverlay));
                menuStyleOverlay.Show();
                return true;
            }); // ohne Translation

            String overlayID = StyleOverlays.get(overlay);
            boolean overlayEnabled = overlayID.startsWith("+");
            if (!(ConfigStyle.contains(overlayID))) {
                if (ConfigStyle.contains(overlayID.substring(1))) {
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

        menuStyleOverlay.mMsgBoxClickListener = (which, data) -> {
            setConfig((String) data);
            Config.AcceptChanges();
            return true;
        };

        if (StyleOverlays.size() > 0) {
            menuStyleOverlay.setData(concatValues(values.split("\\|"), menuStyleOverlay));
            menuStyleOverlay.Show();
        } else {
            // save the values, there is perhaps no overlay
            setConfig(values);
            Config.AcceptChanges();
        }
    }

    private String concatValues(String[] values, Menu lOverlay) {
        StringBuilder result = new StringBuilder(values[0]);
        for (MenuItemBase mitm : lOverlay.mItems) {
            String[] data = ((String) mitm.getData()).split("\\|");
            result.append("\t").append(data[3]);
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
            default:
                return Config.MapsforgeCarNightStyle.getValue();
        }
    }

    private void setConfig(String _StyleAndTheme) {
        String[] values = _StyleAndTheme.split("\\|");
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
            } catch (Exception ignored) {
            }
        }
        return new HashMap<>();
    }

    interface OverlaysCallback extends XmlRenderThemeMenuCallback {
        HashMap<String, String> getOverlays();

        void setLayer(String layer);
    }

    private class GetStylesCallback implements XmlRenderThemeMenuCallback {
        private HashMap<String, String> styles;

        @Override
        public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
            styles = new HashMap<>();
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
                styles = new HashMap<>();
            }
            return styles;
        }
    }

    private class GetOverlaysCallback implements OverlaysCallback {
        String selectedLayer;
        private HashMap<String, String> overlays;

        @Override
        public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
            overlays = new HashMap<>();
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

    private class FZKThemesInfo {
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        public String MD5;
    }

}
