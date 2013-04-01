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

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowActivity;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowHint;
import CB_Core.GL_UI.Main.Actions.CB_Action_Show_Search;
import CB_Core.GL_UI.Main.Actions.CB_Action_switch_Autoresort;
import CB_Core.GL_UI.Main.Actions.CB_Action_switch_DayNight;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.Math.CB_RectF;
import CB_Core.TranslationEngine.Translation;
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
	AutoResort, // 12
	Solver, // 13
	Spoiler, // 14
	Hint, // 15
	Parking, // 16
	Day_Night, // 17
	FieldNotes, // 18
	QuickFieldNotes, // 19
	// ScreenLock, // 20

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
		InitialActions();
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

					QuickActions type = QuickActions.values()[EnumId];

					QuickButtonItem tmp = new QuickButtonItem(new CB_RectF(0, 0, height, height), index,
							QuickActions.getActionEnumById(EnumId), QuickActions.getName(EnumId), type);

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
			return TabMainView.actionShowFilter;
		case 12:
			return action_SwitchAutoResort;
		case 13:
			return TabMainView.actionShowSolverView;
		case 14:
			return TabMainView.actionShowSpoilerView;
		case 15:
			return action_Hint;
		case 16:
			return TabMainView.actionParking;
		case 17:
			return action_DayNight;
		case 18:
			return TabMainView.actionShowFieldNotesView;
		case 19:
			return TabMainView.actionQuickFieldNote;
			// case 12:
			// return action_ScreenLock;
		}
		return null;
	}

	public static String getName(int id)
	{
		switch (id)
		{
		case 0:
			return Translation.Get("Description");
		case 1:
			return Translation.Get("Waypoints");
		case 2:
			return Translation.Get("ShowLogs");
		case 3:
			return Translation.Get("Map");
		case 4:
			return Translation.Get("Compass");
		case 5:
			return Translation.Get("cacheList");
		case 6:
			return Translation.Get("Tracks");
		case 7:
			return Translation.Get("TakePhoto");
		case 8:
			return Translation.Get("RecVideo");
		case 9:
			return Translation.Get("VoiceRec");
		case 10:
			return Translation.Get("Search");
		case 11:
			return Translation.Get("filter");
		case 12:
			return Translation.Get("AutoResort");
		case 13:
			return Translation.Get("Solver");
		case 14:
			return Translation.Get("spoiler");
		case 15:
			return Translation.Get("hint");
		case 16:
			return Translation.Get("MyParking");
		case 17:
			return Translation.Get("DayNight");
		case 18:
			return Translation.Get("Fieldnotes");
		case 19:
			return Translation.Get("QuickFieldNote");
			// case 12:
			// return Translation.Get("screenlock");
		}
		return "empty";
	}

	public static void InitialActions()
	{
		action_TakePhoto = new CB_Action_ShowActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO,
				SpriteCache.Icons.get(IconName.log10_47.ordinal()));

		action_TakeVideo = new CB_Action_ShowActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC,
				SpriteCache.Icons.get(IconName.video_10.ordinal()));

		action_Voicerec = new CB_Action_ShowActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC,
				SpriteCache.Icons.get(IconName.voiceRec_11.ordinal()));

		action_Search = new CB_Action_Show_Search();

		// action_ScreenLock = new CB_Action_ShowActivity("screenlock", MenuID.AID_LOCK, ViewConst.LOCK, SpriteCache.Icons.get(14));

	}

	private static CB_Action action_TakePhoto;
	private static CB_Action action_TakeVideo;

	private static CB_Action action_Voicerec;
	private static CB_Action action_Search;

	// private static CB_Action action_ScreenLock;

	private static CB_Action action_SwitchAutoResort = new CB_Action_switch_Autoresort();

	private static CB_Action action_Hint = new CB_Action_ShowHint();

	private static CB_Action action_DayNight = new CB_Action_switch_DayNight();
}
