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

package de.droidcachebox.main.menuBtn3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import de.droidcachebox.Config;
import de.droidcachebox.TrackRecorder;
import de.droidcachebox.ex_import.UnZip;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.SearchCoordinates;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.*;
import de.droidcachebox.gdx.views.MapView;
import de.droidcachebox.gdx.views.MapView.MapMode;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.LocatorSettings;
import de.droidcachebox.locator.map.*;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;
import org.mapsforge.map.rendertheme.*;
import org.mapsforge.map.rendertheme.rule.RenderThemeHandler;

import java.io.ByteArrayInputStream;
import java.util.*;

import static de.droidcachebox.locator.LocatorBasePlatFormMethods.getMapsForgeGraphicFactory;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;
import static de.droidcachebox.locator.map.MapsForgeLayer.*;

/**
 * @author Longri
 */
public class ShowMap extends AbstractShowAction {
    private static final String log = "ShowMap";
    private static ShowMap that;
    public MapView normalMapView;
    private HashMap<String, String> RenderThemes;
    private String themesPath;
    private FZKThemesInfo fzkThemesInfo;
    private Array<FZKThemesInfo> fzkThemesInfoList = new Array<>();
    private ThemeIsFor whichCase;
    private Menu availableFZKThemesMenu;
    private SearchCoordinates searchCoordinates;

    private ShowMap() {
        super("Map", MenuID.AID_SHOW_MAP);
        normalMapView = new MapView(ViewManager.leftTab.getContentRec(), MapMode.Normal);
        normalMapView.SetZoom(Config.lastZoomLevel.getValue());
    }

    public static ShowMap getInstance() {
        if (that == null) that = new ShowMap();
        return that;
    }

    @Override
    public void execute() {
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
        icm.addCheckableMenuItem("AlignToCompass", normalMapView.GetAlignToCompass(), () -> normalMapView.setAlignToCompass(!normalMapView.GetAlignToCompass()));
        icm.addMenuItem("CenterWP", null, () -> normalMapView.createWaypointAtCenter());
        icm.addMenuItem("gotoPlace", null, () -> {
            searchCoordinates = new SearchCoordinates() {
                public void callBack(Coordinate coordinate) {
                    if (coordinate != null) {
                        normalMapView.setMapStateFree(); // btn
                        normalMapView.setMapState(MapViewBase.MapState.FREE);
                        normalMapView.setCenter(new CoordinateGPS(coordinate.latitude, coordinate.longitude));
                    }
                    searchCoordinates.doFinish();
                }
            };
            searchCoordinates.doShow();
        });
        icm.addMenuItem("RecTrack", null, this::showMenuTrackRecording);
        return icm;
    }

    private void showMapLayerMenu() {
        Menu icm = new Menu("MapViewLayerMenuTitle");

        String[] curentLayerNames = MapView.mapTileLoader.getCurrentLayer().getAllLayerNames();
        for (Layer layer : LayerManager.getInstance().getLayers()) {
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

            MenuItem mi = icm.addMenuItem("", layer.getName(),
                    (sprite == null) ? null : new SpriteDrawable(sprite),
                    (v, x, y, pointer, button) -> {
                        icm.close();
                        selectLayer(layer);
                        showLanguageSelectionMenu(layer);
                        return true;
                    }); // == friendlyName == FileName !!! ohne Translation
            mi.setData(layer);
            mi.setCheckable(true);
            for (String str : curentLayerNames) {
                if (str.equals(layer.getName())) {
                    mi.setChecked(true);
                    break;
                }
            }
        }

        icm.show();
    }

    private void selectLayer(Layer layer) {
        if (layer.getName().equals(normalMapView.getCurrentLayer().getName())) {
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
                                case MessageBox.BTN_LEFT_POSITIVE:
                                    // add the selected map to the curent layer
                                    normalMapView.addAdditionalLayer(layer1);
                                    break;
                                case MessageBox.BTN_MIDDLE_NEUTRAL:
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

    private void showLanguageSelectionMenu(Layer layer) {
        // boolean hasLanguage = false;
        if (layer.isMapsForge()) {
            if (layer.getLanguages() != null)
                if (layer.getLanguages().length > 1) {
                    final Menu lsm = new Menu("MapViewLayerSelectLanguageTitle");
                    for (String lang : layer.getLanguages()) {
                        lsm.addMenuItem("", lang, null, (v, x, y, pointer, button) -> {
                            lsm.close();
                            String selectedLanguage = ((MenuItem) v).getTitle();
                            Config.PreferredMapLanguage.setValue(selectedLanguage);
                            Config.AcceptChanges();
                            return true;
                        });
                    }
                    lsm.show();
                    // hasLanguage = true;
                }
        }
    }

    private void showMapOverlayMenu() {
        final OptionMenu icm = new OptionMenu("MapViewOverlayMenuTitle");
        icm.setSingleSelection();
        for (Layer layer : LayerManager.getInstance().getOverlayLayers()) {
            MenuItem mi = icm.addMenuItem(layer.getFriendlyName(), "", null,
                    (v, x, y, pointer, button) -> {
                        Layer layer1 = (Layer) v.getData();
                        if (layer1 == MapView.mapTileLoader.getCurrentOverlayLayer()) {
                            // switch off Overlay
                            normalMapView.setCurrentOverlayLayer(null);
                        } else {
                            normalMapView.setCurrentOverlayLayer(layer1);
                        }
                        icm.tickCheckBoxes((MenuItem) v);
                        return true;
                    });
            mi.setCheckable(true);
            mi.setChecked(layer == MapView.mapTileLoader.getCurrentOverlayLayer());
            mi.setData(layer);
        }
        icm.show();
    }

    private void showMapViewLayerMenu() {
        OptionMenu menuMapElements = new OptionMenu("MapViewLayerMenuTitle");
        menuMapElements.addCheckableMenuItem("ShowAtOriginalPosition", Config.showAtOriginalPosition.getValue(), () -> toggleSettingWithReload(Config.showAtOriginalPosition));
        menuMapElements.addCheckableMenuItem("HideFinds", Config.hideMyFinds.getValue(), () -> toggleSettingWithReload(Config.hideMyFinds));
        menuMapElements.addCheckableMenuItem("MapShowInfoBar", Config.showInfo.getValue(), () -> toggleSetting(Config.showInfo));
        menuMapElements.addCheckableMenuItem("ShowAllWaypoints", Config.showAllWaypoints.getValue(), () -> toggleSetting(Config.showAllWaypoints));
        menuMapElements.addCheckableMenuItem("ShowRatings", Config.showRating.getValue(), () -> toggleSetting(Config.showRating));
        menuMapElements.addCheckableMenuItem("ShowDT", Config.showDifficultyTerrain.getValue(), () -> toggleSetting(Config.showDifficultyTerrain));
        menuMapElements.addCheckableMenuItem("ShowTitle", Config.showTitles.getValue(), () -> toggleSetting(Config.showTitles));
        menuMapElements.addCheckableMenuItem("ShowDirectLine", Config.showDirectLine.getValue(), () -> toggleSetting(Config.showDirectLine));
        menuMapElements.addCheckableMenuItem("MenuTextShowAccuracyCircle", Config.showAccuracyCircle.getValue(), () -> toggleSetting(Config.showAccuracyCircle));
        menuMapElements.addCheckableMenuItem("ShowCenterCross", Config.showMapCenterCross.getValue(), () -> toggleSetting(Config.showMapCenterCross));
        menuMapElements.addCheckableMenuItem("ShowDistanceCircle", Config.showDistanceCircle.getValue(), () -> toggleSetting(Config.showDistanceCircle));
        menuMapElements.show();
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
        /*
        cm2.addMenuItem("generateRoute", null, () -> {
            new RouteDialog(new RouteDialog.IReturnListener() {
                @Override
                public void returnFromRoute_Dialog(boolean canceld, boolean Motoway, boolean CycleWay, boolean FootWay, boolean UseTmc) {
                    // OpenRouteService generateOpenRoute
                    // BRouterServiceConnection getTrackFromParams
                }
            }).show();
        });
         */
        cm2.addMenuItem("start", null, TrackRecorder::startRecording).setEnabled(!TrackRecorder.recording);
        if (TrackRecorder.pauseRecording)
            cm2.addMenuItem("continue", null, TrackRecorder::pauseRecording).setEnabled(TrackRecorder.recording);
        else
            cm2.addMenuItem("pause", null, TrackRecorder::pauseRecording).setEnabled(TrackRecorder.recording);
        cm2.addMenuItem("stop", null, TrackRecorder::stopRecording).setEnabled(TrackRecorder.recording | TrackRecorder.pauseRecording);
        cm2.addMenuItem("TrackDistance", null, () -> {
            OptionMenu tdMenu = new OptionMenu("TrackDistance");
            tdMenu.mMsgBoxClickListener = (btnNumber, data) -> {
                int newValue = (Integer) data;
                Config.TrackDistance.setValue(newValue);
                Config.AcceptChanges();
                showMenuTrackRecording();
                return true;
            };
            tdMenu.setSingleSelection();
            for (int i : Config.trackDistanceArray) {
                final int selected = i;
                MenuItem mi = tdMenu.addMenuItem("", "" + i, null, () -> tdMenu.setData(selected));
                mi.setCheckable(true);
                if (i == Config.TrackDistance.getValue())
                    mi.setChecked(true);
                else mi.setChecked(false);
            }
            tdMenu.show();
        });
        cm2.show();
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
                        files.put(FileIO.getFileNameWithoutExtension(tmp), f.getAbsolutePath());
                    }
                }
            }
        }
        return files;
    }

    private void showModusSelectionMenu() {
        OptionMenu mapViewThemeMenu = new OptionMenu("MapViewThemeMenuTitle");
        mapViewThemeMenu.addMenuItem("RenderThemesDay", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.day));
        mapViewThemeMenu.addMenuItem("RenderThemesNight", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.night));
        mapViewThemeMenu.addMenuItem("RenderThemesCarDay", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.carday));
        mapViewThemeMenu.addMenuItem("RenderThemesCarNight", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.carnight));

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
                                Download.download("http://download.openandromaps.org/themes/Elevate4.zip", target);
                                try {
                                    UnZip.extractFolder(target, false);
                                } catch (Exception ex) {
                                    Log.err(log, "Unzip error: " + ex.toString());
                                    MessageBox.show(ex.toString(), "Unzip", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                                }
                                Gdx.files.absolute(target).delete();
                                ((MenuItem) v).setDisabled(true);
                                GL.that.renderOnce();
                            });
                            return true;
                        });
                mapViewThemeMenu.addMenuItem("Download", "\n Freizeitkarte", Sprites.getSprite(IconName.freizeit.name()), this::showFZKThemesDownloadMenu);
            }
        } else {
            if (!Config.RememberAsk_RenderThemePathWritable.getValue()) {
                mapViewThemeMenu.addDivider();
                mapViewThemeMenu.addMenuItem("Download", null, () -> {
                    // MakeRenderThemePathWritable
                    MessageBox.show(Translation.get("MakeRenderThemePathWritable"), Translation.get("Download"), MessageBoxButtons.YesNo, MessageBoxIcon.Hand,
                            (btnNumber, data) -> {
                                if (btnNumber == 1) { // change path
                                    Config.RenderThemesFolder.setValue(Config.RenderThemesFolder.getDefaultValue());
                                    Config.AcceptChanges();
                                }
                                return true;
                            }, Config.RememberAsk_RenderThemePathWritable);
                });
            }
        }

        mapViewThemeMenu.show();
    }

    private void showFZKThemesDownloadMenu() {
        GL.that.postAsync(() -> {
            availableFZKThemesMenu = new Menu("Download");
            downloadAvailableFZKThemesList();
            availableFZKThemesMenu.show();
        });
    }

    private void downloadAvailableFZKThemesList() {
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
            availableFZKThemesMenu.addMenuItem("Download", "\n" + fzkThemesInfo.Description, Sprites.getSprite(Sprites.IconName.freizeit.name()),
                    (v, x, y, pointer, button) -> {
                        GL.that.postAsync(() -> {
                            ((MenuItem) v).setDisabled(false);
                            GL.that.renderOnce();
                            String zipFile = fzkThemesInfo.Url.substring(fzkThemesInfo.Url.lastIndexOf("/") + 1);
                            String target = themesPath + "/" + zipFile;

                            Download.download(fzkThemesInfo.Url, target);
                            try {
                                UnZip.extractFolder(target);
                            } catch (Exception ex) {
                                Log.err(log, "Unzip error: " + ex.toString());
                                MessageBox.show(ex.toString(), "Unzip", MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null);
                            }
                            Gdx.files.absolute(target).delete();
                            ((MenuItem) v).setDisabled(true);
                            GL.that.renderOnce();
                        });
                        return true;
                    });
        }
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

    private void showRenderThemesSelectionMenu(ThemeIsFor whichCase) {
        this.whichCase = whichCase;
        Menu themeMenu = new Menu("MapViewThemeMenuTitle");
        RenderThemes = getRenderThemes();

        addThemeMenuItem(themeMenu, INTERNAL_THEME_DEFAULT, INTERNAL_THEME_DEFAULT);
        ArrayList<String> themes = new ArrayList<>(RenderThemes.keySet());
        Collections.sort(themes, (s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase()));
        for (String theme : themes) {
            addThemeMenuItem(themeMenu, theme, RenderThemes.get(theme));
        }
        addThemeMenuItem(themeMenu, INTERNAL_THEME_CAR, INTERNAL_THEME_CAR);
        addThemeMenuItem(themeMenu, INTERNAL_THEME_OSMARENDER, INTERNAL_THEME_OSMARENDER);

        // for showMapStyleSelection to work
        RenderThemes.put(INTERNAL_THEME_DEFAULT, INTERNAL_THEME_DEFAULT);
        RenderThemes.put(INTERNAL_THEME_CAR, INTERNAL_THEME_CAR);
        RenderThemes.put(INTERNAL_THEME_OSMARENDER, INTERNAL_THEME_OSMARENDER);

        themeMenu.show();
    }

    private void addThemeMenuItem(Menu themeMenu, String theme, String themePaN) {
        MenuItem mi = themeMenu.addMenuItem("", theme, null,
                (v, x, y, pointer, button) -> {
                    themeMenu.close();
                    showMapStyleSelection(RenderThemes.get(((MenuItem) v).getTitle()));
                    return true;
                }); // ohne Translation
        mi.setCheckable(true);
        switch (whichCase) {
            case day:
                mi.setChecked(Config.MapsforgeDayTheme.getValue().equals(themePaN));
                break;
            case night:
                mi.setChecked(Config.MapsforgeNightTheme.getValue().equals(themePaN));
                break;
            case carday:
                mi.setChecked(Config.MapsforgeCarDayTheme.getValue().equals(themePaN));
                break;
            case carnight:
                mi.setChecked(Config.MapsforgeCarNightTheme.getValue().equals(themePaN));
                break;
        }
    }

    private void showMapStyleSelection(String selectedThemePaN) {
        final Menu menuMapStyle = new Menu("Styles");

        // getMapStyles works only for External Themes
        // Internal Themes have no XmlRenderThemeMenuCallback
        HashMap<String, String> mapStyles = getMapStyles(selectedThemePaN);

        if (mapStyles.size() > 1) {
            // ex.: oam
            for (String mapStyle : mapStyles.keySet()) {
                MenuItem mi = menuMapStyle.addMenuItem("", mapStyle, null, (v, x, y, pointer, button) -> {
                    menuMapStyle.close();
                    MenuItem clickedItem = (MenuItem) v;
                    String mapStyleId = (String) clickedItem.getData();
                    HashMap<String, String> StyleOverlays = getStyleOverlays(selectedThemePaN, mapStyleId);
                    String ConfigStyle = getStyleFromConfig(mapStyleId);
                    showOverlaySelection(selectedThemePaN, mapStyleId, StyleOverlays, ConfigStyle);
                    return true;
                }); // ohne Translation
                mi.setData(mapStyles.get(mapStyle));
            }
            menuMapStyle.show();
        } else if (mapStyles.size() == 1) {
            // ex.: fzk --> no need to show the mapstyle selection
            for (String mapStyle : mapStyles.keySet()) {
                String mapStyleValue = mapStyles.get(mapStyle);
                HashMap<String, String> StyleOverlays = getStyleOverlays(selectedThemePaN, mapStyleValue);
                String ConfigStyle = getStyleFromConfig(mapStyleValue);
                showOverlaySelection(selectedThemePaN, mapStyleValue, StyleOverlays, ConfigStyle);
            }
        } else {
            // p.ex.: internal Theme -> there is no style
            // style of Config will be ignored while setting of Theme
            setConfig(selectedThemePaN, "");
            Config.AcceptChanges();
        }
    }

    private void showOverlaySelection(String selectedThemePaN, String mapStyleId, HashMap<String, String> StyleOverlays, String ConfigStyle) {
        final Menu menuMapStyleOverlays = new OptionMenu("MapViewThemeStyleMenuTitle");
        for (String overlay : StyleOverlays.keySet()) {
            MenuItem mi = menuMapStyleOverlays.addMenuItem("", overlay, null, () -> {
            });

            String overlayID = StyleOverlays.get(overlay);
            boolean overlayEnabled = overlayID.startsWith("+");
            if (!(ConfigStyle.contains(overlayID))) {
                if (ConfigStyle.contains(overlayID.substring(1))) {
                    overlayEnabled = !overlayEnabled;
                }
            }
            mi.setData(overlayID.substring(1));
            mi.setCheckable(true);
            mi.setChecked(overlayEnabled);
        }

        menuMapStyleOverlays.mMsgBoxClickListener = (btnNumber, data) -> {
            StringBuilder mapStyleValues = new StringBuilder(mapStyleId);
            for (MenuItem mi : menuMapStyleOverlays.mItems) {
                if (mi.isChecked())
                    mapStyleValues.append("\t").append("+").append(mi.getData());
                else
                    mapStyleValues.append("\t").append("-").append(mi.getData());
            }
            setConfig(selectedThemePaN, mapStyleValues.toString());
            Config.AcceptChanges();
            return true;
        };

        if (StyleOverlays.size() > 0) {
            menuMapStyleOverlays.show();
        } else {
            // save the values, there is perhaps no overlay
            setConfig(selectedThemePaN, mapStyleId);
            Config.AcceptChanges();
        }
    }

    private String getStyleFromConfig(String mapStyleId) {
        String configStyle;
        switch (whichCase) {
            case day:
                configStyle = Config.MapsforgeDayStyle.getValue();
                break;
            case night:
                configStyle = Config.MapsforgeNightStyle.getValue();
                break;
            case carday:
                configStyle = Config.MapsforgeCarDayStyle.getValue();
                break;
            default:
                configStyle = Config.MapsforgeCarNightStyle.getValue();
        }
        if (configStyle.startsWith(mapStyleId)) {
            return configStyle;
        } else {
            // configStyle is not for this layer
            return "";
        }
    }

    private void setConfig(String selectedThemePaN, String mapStyleValue) {
        switch (whichCase) {
            case day:
                Config.MapsforgeDayStyle.setValue(mapStyleValue);
                Config.MapsforgeDayTheme.setValue(selectedThemePaN);
                break;
            case night:
                Config.MapsforgeNightStyle.setValue(mapStyleValue);
                Config.MapsforgeNightTheme.setValue(selectedThemePaN);
                break;
            case carday:
                Config.MapsforgeCarDayStyle.setValue(mapStyleValue);
                Config.MapsforgeCarDayTheme.setValue(selectedThemePaN);
                break;
            case carnight:
                Config.MapsforgeCarNightStyle.setValue(mapStyleValue);
                Config.MapsforgeCarNightTheme.setValue(selectedThemePaN);
                break;
        }
    }

    private HashMap<String, String> getMapStyles(String selectedTheme) {
        try {
            // if the selected theme is an internal theme there will be no style
            CB_InternalRenderTheme.valueOf(selectedTheme.toUpperCase()); // make this check better
        } catch (Exception ex) {
            if (selectedTheme.length() > 0) {
                try {
                    StylesCallback stylesCallBack = new StylesCallback();
                    XmlRenderTheme renderTheme = new ExternalRenderTheme(selectedTheme, stylesCallBack);
                    try {
                        // parse RenderTheme to get XmlRenderThemeMenuCallback getCategories called
                        // CB_RenderThemeHandler.getRenderTheme(PlatformUIBase.getGraphicFactory(MapsForgeLayer.displayModel.getScaleFactor()), MapsForgeLayer.displayModel, renderTheme);
                        RenderThemeHandler.getRenderTheme(getMapsForgeGraphicFactory(), MapsForgeLayer.displayModel, renderTheme);
                    } catch (Exception ex1) {
                        Log.err(log, "getMapStyles for " + selectedTheme, ex1);
                    }
                    return stylesCallBack.getStyles();
                } catch (Exception ignored) {
                }
            }
        }
        return new HashMap<>();
    }

    private HashMap<String, String> getStyleOverlays(String selectedThemePaN, String mapStyleId) {
        if (selectedThemePaN.length() > 0) {
            try {
                OverlaysCallback getOverlaysCallback = new GetOverlaysCallback();
                XmlRenderTheme renderTheme = new ExternalRenderTheme(selectedThemePaN, getOverlaysCallback);
                getOverlaysCallback.setLayer(mapStyleId);
                try {
                    // parse RenderTheme to get XmlRenderThemeMenuCallback getCategories called
                    // CB_RenderThemeHandler.getRenderTheme(PlatformUIBase.getGraphicFactory(MapsForgeLayer.displayModel.getScaleFactor()), MapsForgeLayer.displayModel, renderTheme);
                    RenderThemeHandler.getRenderTheme(getMapsForgeGraphicFactory(), MapsForgeLayer.displayModel, renderTheme);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
                return getOverlaysCallback.getOverlays();
            } catch (Exception ignored) {
            }
        }
        return new HashMap<>();
    }

    private enum ThemeIsFor {
        day, night, carday, carnight
    }

    interface OverlaysCallback extends XmlRenderThemeMenuCallback {
        HashMap<String, String> getOverlays();

        void setLayer(String layer);
    }

    private static class StylesCallback implements XmlRenderThemeMenuCallback {
        private HashMap<String, String> styles;

        @Override
        public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
            styles = new HashMap<>();
            LinkedHashMap<String, XmlRenderThemeStyleLayer> styleLayers = style.getLayers();

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

    private static class GetOverlaysCallback implements OverlaysCallback {
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

    private static class FZKThemesInfo {
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        String MD5;
    }

}
