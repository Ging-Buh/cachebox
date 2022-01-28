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

package de.droidcachebox.menu.menuBtn3;

import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_LEFT_POSITIVE;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_MIDDLE_NEUTRAL;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_RIGHT_NEGATIVE;
import static de.droidcachebox.locator.LocatorMethods.getMapsForgeGraphicFactory;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;
import static de.droidcachebox.locator.map.MapsForgeLayer.INTERNAL_THEME_CAR;
import static de.droidcachebox.locator.map.MapsForgeLayer.INTERNAL_THEME_DEFAULT;
import static de.droidcachebox.locator.map.MapsForgeLayer.INTERNAL_THEME_OSMARENDER;
import static de.droidcachebox.menu.Action.ShowMap;
import static de.droidcachebox.menu.Action.ShowTracks;
import static de.droidcachebox.settings.AllSettings.RenderThemesFolder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;

import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.rendertheme.rule.RenderThemeHandler;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.ex_import.UnZip;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.SearchCoordinates;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.main.OptionMenu;
import de.droidcachebox.locator.CBLocation;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.CB_InternalRenderTheme;
import de.droidcachebox.locator.map.Layer;
import de.droidcachebox.locator.map.LayerManager;
import de.droidcachebox.locator.map.MapTileLoader;
import de.droidcachebox.locator.map.MapsForgeLayer;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.maps.Router;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.executes.MapView;
import de.droidcachebox.menu.menuBtn3.executes.MapView.MapMode;
import de.droidcachebox.menu.menuBtn3.executes.TrackCreation;
import de.droidcachebox.menu.menuBtn3.executes.TrackList;
import de.droidcachebox.menu.menuBtn3.executes.TrackRecorder;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class ShowMap extends AbstractShowAction {
    private static final String sClass = "ShowMap";
    private static Router router;
    private final Array<FZKThemesInfo> fzkThemesInfoList = new Array<>();
    public MapView normalMapView;
    private HashMap<String, String> RenderThemes;
    private String themesPath;
    private ThemeIsFor whichCase;
    private Menu availableFZKThemesMenu;
    private FZKThemesInfo fzkThemesInfo;
    private SearchCoordinates searchCoordinates;
    private SpriteDrawable[] routeProfileIcons;

    public ShowMap() {
        super("Map");
        normalMapView = new MapView(ViewManager.leftTab.getContentRec(), MapMode.Normal);
        normalMapView.setZoom(Settings.lastZoomLevel.getValue());
    }

    public static void setRouter(Router _router) {
        router = _router;
    }

    @Override
    public void execute() {
        if (Platform.isGPSon()) {
            Platform.request_getLocationIfInBackground();
        }
        ViewManager.leftTab.showView(normalMapView);
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
    public void viewIsHiding() {

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
        MenuItem mi = icm.addMenuItem("Renderthemes", null, this::showModeSelectionMenu);
        if (RenderThemesFolder.getValue().length() == 0) {
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
                        normalMapView.setBtnMapStateToFree(); // btn
                        // normalMapView.setMapState(MapViewBase.MapState.FREE);
                        normalMapView.setCenter(new CoordinateGPS(coordinate.latitude, coordinate.longitude));
                    }
                    searchCoordinates.doFinish();
                }
            };
            searchCoordinates.doShow();
        });
        icm.addMenuItem("TrackRecordMenuTitle", null, this::showMenuTrackFunctions);
        return icm;
    }

    private void showMapLayerMenu() {
        Menu icm = new Menu("MapViewLayerMenuTitle");
        Layer currentLayer = MapTileLoader.getInstance().getCurrentLayer();
        Layer[] currentLayers = currentLayer.getAllLayers();

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
                        LayerManager.getInstance().activateNewList();
                        selectLayer(layer);
                        showLanguageSelectionMenu(layer);
                        return true;
                    }); // == friendlyName == FileName !!! without translation
            mi.setData(layer);
            mi.setCheckable(true);
            for (Layer l : currentLayers) {
                if (l.getName().equals(layer.getName())) {
                    mi.setChecked(true);
                    break;
                }
            }
        }
        icm.addMenuItem("Other", "", null, (view, x, y, pointer, button) -> {
            icm.close();
            Platform.getDocumentAccess(Settings.MapPackFolder.getValue(), returnUri -> {
                MapsForgeLayer l = new MapsForgeLayer(returnUri);
                if (l.getName().length() > 0) {
                    LayerManager.getInstance().activateNewList();
                    selectLayer(l);
                }
            });
            return true;
        });

        icm.show();
    }

    private void selectLayer(Layer layer) {
        if (layer.getName().equals(normalMapView.getCurrentLayer().getName())) {
            normalMapView.clearAdditionalLayers();
        } else {
            // if current layer is a Mapsforge map, it is possible to add the selected Mapsforge map to the current layer. We ask the User!
            if (MapTileLoader.getInstance().getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
                ButtonDialog msgBox = new ButtonDialog(
                        Translation.get("AddOrChangeMap"),
                        Translation.get("Layer"),
                        MsgBoxButton.YesNoCancel,
                        MsgBoxIcon.Question
                );
                msgBox.setButtonClickHandler((which, data) -> {
                    Layer layer1 = (Layer) data;
                    switch (which) {
                        case BTN_LEFT_POSITIVE:
                            // add the selected map to the current layer
                            normalMapView.addAdditionalLayer(layer1);
                            break;
                        case BTN_MIDDLE_NEUTRAL:
                            // switch current layer to selected
                            normalMapView.setCurrentLayer(layer1);
                            break;
                        default:
                            normalMapView.removeAdditionalLayer();
                    }
                    return true;
                });
                msgBox.setButtonText(BTN_LEFT_POSITIVE, "+");
                msgBox.setButtonText(BTN_MIDDLE_NEUTRAL, "=");
                msgBox.setButtonText(BTN_RIGHT_NEGATIVE, "-");
                msgBox.setData(layer);
                msgBox.show();
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
                            Settings.preferredMapLanguage.setValue(selectedLanguage);
                            Settings.getInstance().acceptChanges();
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
                        if (layer1 == MapTileLoader.getInstance().getCurrentOverlayLayer()) {
                            // switch off Overlay
                            normalMapView.setCurrentOverlayLayer(null);
                        } else {
                            normalMapView.setCurrentOverlayLayer(layer1);
                        }
                        icm.tickCheckBoxes((MenuItem) v);
                        return true;
                    });
            mi.setCheckable(true);
            mi.setChecked(layer == MapTileLoader.getInstance().getCurrentOverlayLayer());
            mi.setData(layer);
        }
        icm.show();
    }

    private void showMapViewLayerMenu() {
        OptionMenu menuMapElements = new OptionMenu("MapViewLayerMenuTitle");
        menuMapElements.addCheckableMenuItem("ShowLiveMap", !Settings.disableLiveMap.getValue(), () -> toggleSetting(Settings.disableLiveMap));
        menuMapElements.addCheckableMenuItem("ShowAtOriginalPosition", Settings.showAtOriginalPosition.getValue(), () -> toggleSettingWithReload(Settings.showAtOriginalPosition));
        menuMapElements.addCheckableMenuItem("HideFinds", Settings.hideMyFinds.getValue(), () -> toggleSettingWithReload(Settings.hideMyFinds));
        menuMapElements.addCheckableMenuItem("MapShowInfoBar", Settings.showInfo.getValue(), () -> toggleSetting(Settings.showInfo));
        menuMapElements.addCheckableMenuItem("ShowAllWaypoints", Settings.showAllWaypoints.getValue(), () -> toggleSetting(Settings.showAllWaypoints));
        menuMapElements.addCheckableMenuItem("ShowRatings", Settings.showRating.getValue(), () -> toggleSetting(Settings.showRating));
        menuMapElements.addCheckableMenuItem("ShowDT", Settings.showDifficultyTerrain.getValue(), () -> toggleSetting(Settings.showDifficultyTerrain));
        menuMapElements.addCheckableMenuItem("ShowTitle", Settings.showTitles.getValue(), () -> toggleSetting(Settings.showTitles));
        menuMapElements.addCheckableMenuItem("ShowDirectLine", Settings.showDirectLine.getValue(), () -> toggleSetting(Settings.showDirectLine));
        menuMapElements.addCheckableMenuItem("MenuTextShowAccuracyCircle", Settings.showAccuracyCircle.getValue(), () -> toggleSetting(Settings.showAccuracyCircle));
        menuMapElements.addCheckableMenuItem("ShowCenterCross", Settings.showMapCenterCross.getValue(), () -> toggleSetting(Settings.showMapCenterCross));
        menuMapElements.addCheckableMenuItem("ShowDistanceToCenter", Settings.showDistanceToCenter.getValue(), () -> toggleSetting(Settings.showDistanceToCenter));
        menuMapElements.addCheckableMenuItem("ShowDistanceCircle", Settings.showDistanceCircle.getValue(), () -> toggleSetting(Settings.showDistanceCircle));
        menuMapElements.show();
    }

    private void toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Settings.getInstance().acceptChanges();
        normalMapView.setNewSettings(MapView.INITIAL_SETTINGS_WITH_OUT_ZOOM);
    }

    private void toggleSettingWithReload(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Settings.getInstance().acceptChanges();
        normalMapView.setNewSettings(INITIAL_WP_LIST);
    }

    private void showMenuTrackFunctions() {
        Menu cm2 = new Menu("TrackRecordMenuTitle");
        if (router != null) {
            if (router.open()) {
                if (routeProfileIcons == null) {
                    routeProfileIcons = new SpriteDrawable[3];
                    routeProfileIcons[0] = new SpriteDrawable(Sprites.getSprite("pedestrian"));
                    routeProfileIcons[1] = new SpriteDrawable(Sprites.getSprite("bicycle"));
                    routeProfileIcons[2] = new SpriteDrawable(Sprites.getSprite("car"));
                }
                MenuItem mi = cm2.addMenuItem("generateRoute", "", routeProfileIcons[Settings.routeProfile.getValue()], (v, x, y, pointer, button) -> {
                    if (((MenuItem) v).isIconClicked(x)) {
                        Settings.routeProfile.setValue(((Settings.routeProfile.getValue() + 1) % 3));
                        ((MenuItem) v).setIcon(routeProfileIcons[Settings.routeProfile.getValue()]);
                    } else {
                        cm2.close();
                        boolean checked = ((MenuItem) v).isChecked();
                        if (((MenuItem) v).isCheckboxClicked(x))
                            checked = !checked;
                        if (checked) {
                            setRoutingTrack();
                        } else {
                            TrackList.getInstance().removeRoutingTrack();
                        }
                        ((ShowTracks) ShowTracks.action).notifyDataSetChanged();
                    }
                    return true;
                });
                mi.setCheckable(TrackList.getInstance().existsRoutingTrack());
                mi.setChecked(true);
            } else {
                cm2.addMenuItem("InstallRoutingApp", Sprites.getSprite("openrouteservice_logo"),
                        () -> Platform.callUrl("https://play.google.com/store/apps/details?id=btools.routingapp&hl=de"));
            }
        }

        cm2.addDivider();
        cm2.addMenuItem("",Translation.get("TrackDistance","" + Settings.trackDistance.getValue()), null, () -> {
            OptionMenu tdMenu = new OptionMenu("TrackDistance");
            tdMenu.buttonClickHandler = (btnNumber, data) -> {
                int newValue = (Integer) data;
                Settings.trackDistance.setValue(newValue);
                Settings.getInstance().acceptChanges();
                showMenuTrackFunctions();
                return true;
            };
            tdMenu.setSingleSelection();
            for (int i : Settings.trackDistanceArray) {
                final int selected = i;
                MenuItem mi = tdMenu.addMenuItem("", "" + i, null, () -> tdMenu.setData(selected));
                mi.setCheckable(true);
                mi.setChecked(i == Settings.trackDistance.getValue());
            }
            tdMenu.show();
        });
        cm2.addMenuItem("start", null, TrackRecorder.getInstance()::startRecording).setEnabled(!TrackRecorder.getInstance().recording);
        if (TrackRecorder.getInstance().pauseRecording)
            cm2.addMenuItem("continue", null, TrackRecorder.getInstance()::pauseRecording).setEnabled(TrackRecorder.getInstance().recording);
        else
            cm2.addMenuItem("pause", null, TrackRecorder.getInstance()::pauseRecording).setEnabled(TrackRecorder.getInstance().recording);
        cm2.addMenuItem("stop", null, TrackRecorder.getInstance()::stopRecording).setEnabled(TrackRecorder.getInstance().recording || TrackRecorder.getInstance().pauseRecording);
        cm2.addDivider();
        cm2.addMenuItem("load", null, TrackList.getInstance()::selectTrackFileReadAndAddToTracks);
        cm2.addMenuItem("generate", null, () -> new TrackCreation().execute());
        cm2.addDivider();
        cm2.addMenuItem("Tracks", Sprites.getSprite(IconName.trackListIcon.name()), () -> ShowTracks.action.execute());
        cm2.show();
    }

    public boolean openRouter() {
        if (router != null)
            return router.open();
        return false;
    }

    public void setRoutingTrack() {
        Coordinate start = Locator.getInstance().getMyPosition(CBLocation.ProviderType.GPS);
        if (start == null || !start.isValid()) {
            // from center map
            Coordinate mapCenter = ((ShowMap) ShowMap.action).normalMapView.center;
            if (mapCenter != null) start = mapCenter;
        }
        Coordinate destination = GlobalCore.getSelectedCoordinate();
        if (destination != null) {
            if (start != null) {
                if (start.isValid()) {
                    Track track = router.getTrack(start, destination);
                    if (track != null && track.getTrackPoints().size() > 0) {
                        track.setVisible(true);
                        track.setName("Route");
                        TrackList.getInstance().setRoutingTrack(track);
                    } else {
                        Log.err(sClass, "no route generated");
                    }
                }
            }
        }
    }

    private HashMap<String, String> getRenderThemes() {
        HashMap<String, String> files = new HashMap<>();
        String directory = RenderThemesFolder.getValue();
        if (directory.length() > 0) {
            files.putAll(getDirsRenderThemes(directory));
        }
        return files;
    }

    private HashMap<String, String> getDirsRenderThemes(String directory) {
        HashMap<String, String> files = new HashMap<>();
        AbstractFile dir = FileFactory.createFile(directory);
        String[] dirFiles = dir.list();
        if (dirFiles != null && dirFiles.length > 0) {
            for (String tmp : dirFiles) {
                AbstractFile f = FileFactory.createFile(directory + "/" + tmp);
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

    private void showModeSelectionMenu() {
        OptionMenu mapViewThemeMenu = new OptionMenu("MapViewThemeMenuTitle");
        mapViewThemeMenu.addMenuItem("RenderThemesDay", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.day));
        mapViewThemeMenu.addMenuItem("RenderThemesNight", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.night));
        mapViewThemeMenu.addMenuItem("RenderThemesCarDay", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.carday));
        mapViewThemeMenu.addMenuItem("RenderThemesCarNight", null, () -> showRenderThemesSelectionMenu(ThemeIsFor.carnight));

        themesPath = RenderThemesFolder.getValue();
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
                                // Download.download("http://download.openandromaps.org/themes/Elevate4.zip", target);
                                Download download = new Download(null, null);
                                download.download("https://www.openandromaps.org/wp-content/users/tobias/Elevate.zip", target);
                                try {
                                    UnZip.extract(target, false);
                                } catch (Exception ex) {
                                    Log.err(sClass, target + ": Unzip error: " + ex.toString());
                                    new ButtonDialog(target + ": " + ex.toString(), "Unzip", MsgBoxButton.OK, MsgBoxIcon.Exclamation).show();
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
            if (!Settings.RememberAsk_RenderThemePathWritable.getValue()) {
                mapViewThemeMenu.addDivider();
                mapViewThemeMenu.addMenuItem("Download", null, () -> {
                    // MakeRenderThemePathWritable
                    new ButtonDialog(Translation.get("MakeRenderThemePathWritable"), Translation.get("Download"), MsgBoxButton.YesNo, MsgBoxIcon.Hand,
                            (btnNumber, data) -> {
                                if (btnNumber == BTN_LEFT_POSITIVE) { // change path
                                    RenderThemesFolder.setValue(RenderThemesFolder.getDefaultValue());
                                    Settings.getInstance().acceptChanges();
                                }
                                return true;
                            }, Settings.RememberAsk_RenderThemePathWritable).show();
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
                    .readTimeout(Settings.socket_timeout.getValue())
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
                            Download download = new Download(null, null);
                            download.download(fzkThemesInfo.Url, target);
                            try {
                                UnZip.extractHere(target);
                            } catch (Exception ex) {
                                Log.err(sClass, "Unzip error: " + ex.toString());
                                new ButtonDialog(ex.toString(), "Unzip", MsgBoxButton.OK, MsgBoxIcon.Exclamation).show();
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

        if (Settings.Sel_LanguagePath.getValue().contains("/de/")) {
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
                }); // without translation
        mi.setCheckable(true);
        switch (whichCase) {
            case day:
                mi.setChecked(Settings.mapsForgeDayTheme.getValue().equals(themePaN));
                break;
            case night:
                mi.setChecked(Settings.mapsForgeNightTheme.getValue().equals(themePaN));
                break;
            case carday:
                mi.setChecked(Settings.mapsForgeCarDayTheme.getValue().equals(themePaN));
                break;
            case carnight:
                mi.setChecked(Settings.mapsForgeCarNightTheme.getValue().equals(themePaN));
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
                }); // without translation
                mi.setData(mapStyles.get(mapStyle));
            }
            menuMapStyle.show();
        } else if (mapStyles.size() == 1) {
            // ex.: fzk --> no need to show the map_style selection
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
            Settings.getInstance().acceptChanges();
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

        menuMapStyleOverlays.buttonClickHandler = (btnNumber, data) -> {
            StringBuilder mapStyleValues = new StringBuilder(mapStyleId);
            for (MenuItem mi : menuMapStyleOverlays.getItems()) {
                if (mi.isChecked())
                    mapStyleValues.append("\t").append("+").append(mi.getData());
                else
                    mapStyleValues.append("\t").append("-").append(mi.getData());
            }
            setConfig(selectedThemePaN, mapStyleValues.toString());
            Settings.getInstance().acceptChanges();
            return true;
        };

        if (StyleOverlays.size() > 0) {
            menuMapStyleOverlays.show();
        } else {
            // save the values, there is perhaps no overlay
            setConfig(selectedThemePaN, mapStyleId);
            Settings.getInstance().acceptChanges();
        }
    }

    private String getStyleFromConfig(String mapStyleId) {
        String configStyle;
        switch (whichCase) {
            case day:
                configStyle = Settings.mapsForgeDayStyle.getValue();
                break;
            case night:
                configStyle = Settings.mapsForgeNightStyle.getValue();
                break;
            case carday:
                configStyle = Settings.mapsForgeCarDayStyle.getValue();
                break;
            default:
                configStyle = Settings.mapsForgeCarNightStyle.getValue();
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
                Settings.mapsForgeDayStyle.setValue(mapStyleValue);
                Settings.mapsForgeDayTheme.setValue(selectedThemePaN);
                break;
            case night:
                Settings.mapsForgeNightStyle.setValue(mapStyleValue);
                Settings.mapsForgeNightTheme.setValue(selectedThemePaN);
                break;
            case carday:
                Settings.mapsForgeCarDayStyle.setValue(mapStyleValue);
                Settings.mapsForgeCarDayTheme.setValue(selectedThemePaN);
                break;
            case carnight:
                Settings.mapsForgeCarNightStyle.setValue(mapStyleValue);
                Settings.mapsForgeCarNightTheme.setValue(selectedThemePaN);
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
                        Log.err(sClass, "getMapStyles for " + selectedTheme, ex1);
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
                    Log.err(sClass, e.toString());
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
            LinkedHashMap<String, XmlRenderThemeStyleLayer> styleLayers = (LinkedHashMap<String, XmlRenderThemeStyleLayer>) style.getLayers();

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
