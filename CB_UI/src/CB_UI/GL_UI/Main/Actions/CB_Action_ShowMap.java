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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Locator.LocatorSettings;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_UI.Config;
import CB_UI.TrackRecorder;
import CB_UI.GL_UI.Activitys.MapDownload;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Menu.OptionMenu;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

/**
 * @author Longri
 */
public class CB_Action_ShowMap extends CB_Action_ShowView {

	public CB_Action_ShowMap() {
		super("Map", MenuID.AID_SHOW_MAP);
	}

	@Override
	public void Execute() {
		if ((TabMainView.mapView == null) && (tabMainView != null) && (tab != null))
			TabMainView.mapView = new MapView(tab.getContentRec(), MapMode.Normal, "MapView");

		if ((TabMainView.mapView != null) && (tab != null))
			tab.ShowView(TabMainView.mapView);
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.mapView;
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
		icm.addCheckableItem(MenuID.MI_ALIGN_TO_COMPSS, "AlignToCompass", MapView.that.GetAlignToCompass());
		icm.addItem(MenuID.MI_CENTER_WP, "CenterWP");
		// icm.addItem(MenuID.MI_SETTINGS, "settings", Sprites.getSprite(IconName.settings.name()));
		// icm.addItem(MenuID.MI_SEARCH, "search", SpriteCache.Icons.get(27));
		icm.addItem(MenuID.MI_MAPVIEW_VIEW, "view");
		//icm.addItem(MenuID.MI_TREC_REC, "RecTrack");
		icm.addItem(MenuID.MI_MAP_DOWNOAD, "MapDownload");

		icm.addOnClickListener(onItemClickListener);
		return icm;
	}

	private void showMapLayerMenu() {
		Menu icm = new Menu("MapViewShowLayerContextMenu");

		// Sorting (perhaps use an arraylist of layers without the overlay layers) 
		Collections.sort(ManagerBase.Manager.getLayers(), new Comparator<Layer>() {
			@Override
			public int compare(Layer layer1, Layer layer2) {
				return layer1.Name.toLowerCase().compareTo(layer2.Name.toLowerCase());
			}
		});

		int menuID = 0;
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
				if (layer == MapView.mapTileLoader.getCurrentLayer()) {
					mi.setChecked(true);
				}
			}
		}

		icm.addOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				final Layer layer = (Layer) ((MenuItem) v).getData();

				// if curent layer a Mapsforge map, it is posible to add the selected Mapsforge map
				// to the current layer. We ask the User!
				if (MapView.mapTileLoader.getCurrentLayer().isMapsForge() && layer.isMapsForge()) {
					GL_MsgBox msgBox = GL_MsgBox.Show("add or change", "Map selection", MessageBoxButtons.YesNoCancel, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

						@Override
						public boolean onClick(int which, Object data) {

							switch (which) {
							case GL_MsgBox.BUTTON_POSITIVE:
								// add the selected map to the curent layer
								TabMainView.mapView.addToCurrentLayer(layer);
								break;
							case GL_MsgBox.BUTTON_NEUTRAL:
								// switch curent layer to selected
								TabMainView.mapView.setCurrentLayer(layer);
								break;
							default:
								// do nothing
							}

							return true;
						}
					});
					msgBox.button1.setText("add");
					msgBox.button2.setText("select");
					return true;
				}

				TabMainView.mapView.setCurrentLayer(layer);
				return true;
			}
		});

		icm.Show();
	}

	private void showMapOverlayMenu() {
		final OptionMenu icm = new OptionMenu("MapViewShowMapOverlayMenu");

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
					TabMainView.mapView.SetCurrentOverlayLayer(null);
				} else {
					TabMainView.mapView.SetCurrentOverlayLayer(layer);
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

		icm.addCheckableItem(MenuID.MI_HIDE_FINDS, "HideFinds", Config.MapHideMyFinds.getValue());
		icm.addCheckableItem(MenuID.MI_MAP_SHOW_COMPASS, "MapShowCompass", Config.MapShowCompass.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_ALL_WAYPOINTS, "ShowAllWaypoints", Config.ShowAllWaypoints.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_RATINGS, "ShowRatings", Config.MapShowRating.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_DT, "ShowDT", Config.MapShowDT.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_TITLE, "ShowTitle", Config.MapShowTitles.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_DIRECT_LINE, "ShowDirectLine", Config.ShowDirektLine.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_ACCURACY_CIRCLE, "MenuTextShowAccuracyCircle", Config.ShowAccuracyCircle.getValue());
		icm.addCheckableItem(MenuID.MI_SHOW_CENTERCROSS, "ShowCenterCross", Config.ShowMapCenterCross.getValue());

		icm.addOnClickListener(onItemClickListener);
		icm.Show();
	}

	private final OnClickListener onItemClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			switch (((MenuItem) v).getMenuItemId()) {
			case MenuID.MI_LAYER:
				showMapLayerMenu();
				return true;
			case MenuID.MI_RENDERTHEMES:
				MenuItem mi = (MenuItem) v;
				if (mi.isEnabled()) {
					return showRenderThemesMenu();
				} else
					return false;
			case MenuID.MI_MAPVIEW_OVERLAY_VIEW:
				showMapOverlayMenu();
				return true;
			case MenuID.MI_MAPVIEW_VIEW:
				showMapViewLayerMenu();
				return true;
			case MenuID.MI_ALIGN_TO_COMPSS:
				MapView.that.SetAlignToCompass(!MapView.that.GetAlignToCompass());
				return true;
			case MenuID.MI_SHOW_ALL_WAYPOINTS:
				toggleSetting(Config.ShowAllWaypoints);
				return true;
			case MenuID.MI_HIDE_FINDS:
				toggleSettingWithReload(Config.MapHideMyFinds);
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
			case MenuID.MI_MAP_SHOW_COMPASS:
				toggleSetting(Config.MapShowCompass);
				return true;
			case MenuID.MI_CENTER_WP:
				if (MapView.that != null) {
					MapView.that.createWaypointAtCenter();
				}
				return true;
			/*
			case MenuID.MI_SETTINGS:
				TabMainView.actionShowSettings.Execute();
				return true;
			*/
			/*
			case MenuID.MI_SEARCH:
				if (SearchDialog.that == null) {
					new SearchDialog();
				}
				SearchDialog.that.showNotCloseAutomaticly();
				return true;
			*/
			case MenuID.MI_TREC_REC:
				showMenuTrackRecording();
				return true;
			case MenuID.MI_MAP_DOWNOAD:
				MapDownload.INSTANCE.show();
				return true;
			default:
				return false;
			}
		}
	};

	private static final int START = 1;
	private static final int PAUSE = 2;
	private static final int STOP = 3;

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
		if (MapView.that != null)
			MapView.that.setNewSettings(MapView.INITIAL_SETTINGS_WITH_OUT_ZOOM);
	}

	private void toggleSettingWithReload(SettingBool setting) {
		setting.setValue(!setting.getValue());
		Config.AcceptChanges();
		if (MapView.that != null)
			MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);
	}

	private ArrayList<String> getRenderThemes() {
		ArrayList<String> files = new ArrayList<String>();
		String directory = LocatorSettings.RenderThemesFolder.getValue();
		if (directory.length() > 0) {
			File dir = FileFactory.createFile(directory);
			String[] dirFiles = dir.list();
			if (dirFiles != null && dirFiles.length > 0) {
				for (String tmp : dirFiles) {
					String ttt = tmp.toLowerCase();
					if (ttt.endsWith("xml")) {
						files.add(FileIO.GetFileNameWithoutExtension(tmp));
					}
				}
			}
		}
		return files;
	}

	private boolean showRenderThemesMenu() {
		final Menu lRenderThemesMenu = new OptionMenu("RenderThemesMenu");
		lRenderThemesMenu.addItem(0, "RenderThemesDay");
		lRenderThemesMenu.addItem(1, "RenderThemesNight");
		lRenderThemesMenu.addItem(2, "RenderThemesCarDay");
		lRenderThemesMenu.addItem(3, "RenderThemesCarNight");

		lRenderThemesMenu.addOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				return showRenderThemesSubMenu(((MenuItem) v).getMenuItemId());
			}
		});

		lRenderThemesMenu.Show();
		return true;
	}

	private boolean showRenderThemesSubMenu(int which) {
		final Menu lRenderThemesSubMenu = new Menu("RenderThemesSubMenu");

		int menuID = 0;
		for (String theme : getRenderThemes()) {
			MenuItem mi = lRenderThemesSubMenu.addItem(menuID++, "", theme); // ohne Translation
			mi.setData(which);
			mi.setCheckable(true);
			switch (which) {
			case 0:
				if (LocatorSettings.MapsforgeDayTheme.getValue().contains(theme)) {
					mi.setChecked(true);
				}
				break;
			case 1:
				if (LocatorSettings.MapsforgeNightTheme.getValue().contains(theme)) {
					mi.setChecked(true);
				}
				break;
			case 2:
				if (LocatorSettings.MapsforgeCarDayTheme.getValue().contains(theme)) {
					mi.setChecked(true);
				}
				break;
			case 3:
				if (LocatorSettings.MapsforgeCarNightTheme.getValue().contains(theme)) {
					mi.setChecked(true);
				}
				break;
			}
		}

		lRenderThemesSubMenu.addOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				MenuItem mi = (MenuItem) v;
				int which = (int) mi.getData();
				String selectedValue;
				if (mi.isChecked()) {
					selectedValue = "";
				} else {
					selectedValue = LocatorSettings.RenderThemesFolder.getValue() + "/" + mi.getTitle() + ".xml";
				}
				switch (which) {
				case 0:
					LocatorSettings.MapsforgeDayTheme.setValue(selectedValue);
					break;
				case 1:
					LocatorSettings.MapsforgeNightTheme.setValue(selectedValue);
					break;
				case 2:
					//LocatorSettings.MapsforgeDayCarTheme.setValue(selectedValue);
					break;
				case 3:
					//LocatorSettings.MapsforgeDayCarTheme.setValue(selectedValue);
					break;
				}
				Config.AcceptChanges();
				return true;
			}
		});

		lRenderThemesSubMenu.Show();
		return true;
	}
}
