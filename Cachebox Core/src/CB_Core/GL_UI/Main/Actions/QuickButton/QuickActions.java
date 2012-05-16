/* 
 * Copyright (C) 2011 team-cachebox.de
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
package CB_Core.GL_UI.Main.Actions.QuickButton;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowActivity;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowHint;
import CB_Core.GL_UI.Main.Actions.CB_Action_switch_Autoresort;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.MoveableList;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 * 
 * @author Longri
 */
public enum QuickActions
{
	DescriptionView, // 0
	WaypointView, // 1
	LogView, // 2
	MapView, // 3
	CompassView, // 4
	CacheListView, // 5
	TrackListView, // 6
	TakePhoto, // 7
	TakeVideo, // 8
	VoiceRecord, // 9
	LiveSearch, // 10
	Filter, // 11
	ScreenLock, // 12
	AutoResort, // 13
	Solver, // 14
	Spoiler, // 15
	Hint, // 16
	Parking, // 17
	Day_Night, // 18

	empty, ;

	/**
	 * Gibt eine ArrayList von Actions zurück aus einem übergebenen String Array
	 * 
	 * @param String
	 *            []
	 * @return ArrayList <Actions>
	 */
	public static MoveableList<QuickButtonItem> getListFromConfig(String[] configList, float height)
	{
		MoveableList<QuickButtonItem> retVel = new MoveableList<QuickButtonItem>();
		if (configList == null || configList.length == 0)
		{
			return retVel;
		}
		try
		{
			int index = 0;
			for (String s : configList)
			{
				s = s.replace(",", "");
				int EnumId = Integer.parseInt(s);
				if (EnumId > -1)
				{
					QuickButtonItem tmp = new QuickButtonItem(new CB_RectF(0, 0, height, height), index,
							QuickActions.getActionEnumById(EnumId), QuickActions.getName(EnumId));

					retVel.add(tmp);
				}
			}
		}
		catch (Exception e)// wenn ein Fehler auftritt, gib die bis dorthin
							// gelesenen Items zurück
		{

		}

		return retVel;
	}

	/**
	 * Gibt die ID des Übergebenen Enums zurück
	 * 
	 * @param attrib
	 * @return long
	 */
	public static int GetIndex(QuickActions attrib)
	{
		return attrib.ordinal();
	}

	public static CB_Action getActionEnumById(int id)
	{
		switch (id)
		{
		case 0:
			return TabMainView.actionShowDescriptionView;
		case 1:
			return TabMainView.actionShowWaypointView;
		case 2:
			return TabMainView.actionShowLogView;
		case 3:
			return TabMainView.actionShowMap;
		case 4:
			return TabMainView.actionShowCompassView;
		case 5:
			return TabMainView.actionShowCacheList;
		case 6:
			return TabMainView.actionShowTrackListView;
		case 7:
			return action_TakePhoto;
		case 8:
			return action_TakeVideo;
		case 9:
			return action_Voicerec;
		case 10:
			return action_Search;
		case 11:
			return action_Filtersettings;
		case 12:
			return action_ScreenLock;
		case 13:
			return action_SwitchAutoResort;
		case 14:
			return TabMainView.actionShowSolverView;
		case 15:
			return TabMainView.actionShowSpoilerView;
		case 16:
			return action_Hint;
		case 17:
			return action_Parking;
		case 18:
			return action_DayNight;
		}
		return null;
	}

	private static String getName(int id)
	{
		switch (id)
		{
		case 0:
			return GlobalCore.Translations.Get("Description");
		case 1:
			return GlobalCore.Translations.Get("Waypoints");
		case 2:
			return GlobalCore.Translations.Get("ShowLogs");
		case 3:
			return GlobalCore.Translations.Get("Map");
		case 4:
			return GlobalCore.Translations.Get("Compass");
		case 5:
			return GlobalCore.Translations.Get("cacheList");
		case 6:
			return GlobalCore.Translations.Get("Tracks");
		case 7:
			return GlobalCore.Translations.Get("TakePhoto");
		case 8:
			return GlobalCore.Translations.Get("RecVideo");
		case 9:
			return GlobalCore.Translations.Get("VoiceRec");
		case 10:
			return GlobalCore.Translations.Get("Search");
		case 11:
			return GlobalCore.Translations.Get("filter");
		case 12:
			return GlobalCore.Translations.Get("screenlock");
		case 13:
			return GlobalCore.Translations.Get("AutoResort");
		case 14:
			return GlobalCore.Translations.Get("Solver");
		case 15:
			return GlobalCore.Translations.Get("spoiler");
		case 16:
			return GlobalCore.Translations.Get("hint");
		case 17:
			return GlobalCore.Translations.Get("MyParking");
		case 18:
			return GlobalCore.Translations.Get("DayNight");
		}
		return "empty";
	}

	private static CB_Action action_TakePhoto = new CB_Action_ShowActivity("TakePhoto", CB_Action.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO,
			SpriteCache.Icons.get(47));

	private static CB_Action action_TakeVideo = new CB_Action_ShowActivity("RecVideo", CB_Action.AID_VIDEO_REC, ViewConst.VIDEO_REC,
			SpriteCache.Icons.get(10));

	private static CB_Action action_Voicerec = new CB_Action_ShowActivity("VoiceRec", CB_Action.AID_VOICE_REC, ViewConst.VOICE_REC,
			SpriteCache.Icons.get(11));

	private static CB_Action action_Search = new CB_Action_ShowActivity("search", CB_Action.AID_SEARCH, ViewConst.SEARCH,
			SpriteCache.Icons.get(27));

	private static CB_Action action_Filtersettings = new CB_Action_ShowActivity("filtersettings", CB_Action.AID_SHOW_FILTER_SETTINGS,
			ViewConst.FILTER_SETTINGS, SpriteCache.Icons.get(13));

	private static CB_Action action_ScreenLock = new CB_Action_ShowActivity("screenlock", CB_Action.AID_LOCK, ViewConst.LOCK,
			SpriteCache.Icons.get(14));

	private static CB_Action action_SwitchAutoResort = new CB_Action_switch_Autoresort();

	private static CB_Action action_Hint = new CB_Action_ShowHint();

	private static CB_Action action_Parking = new CB_Action_ShowActivity("Parking", CB_Action.AID_PARKING, ViewConst.PARKING,
			SpriteCache.BigIcons.get(20));

	private static CB_Action action_DayNight = new CB_Action_ShowActivity("DayNight", CB_Action.AID_DAY_NIGHT, ViewConst.DAY_NIGHT,
			SpriteCache.Icons.get(48));
}
