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
package de.cachebox_test.Enums;

import CB_Core.GlobalCore;
import CB_Core.Types.MoveableList;
import android.graphics.drawable.Drawable;
import de.cachebox_test.Global;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.QuickButtonList.QuickButtonItem;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 * 
 * @author Longri
 */
public enum Actions
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

	empty, ;

	/**
	 * Gibt eine ArrayList von Actions zurück aus einem übergebenen String Array
	 * 
	 * @param String
	 *            []
	 * @return ArrayList <Actions>
	 */
	public static MoveableList<QuickButtonItem> getListFromConfig(String[] configList)
	{
		MoveableList<QuickButtonItem> retVel = new MoveableList<QuickButtonItem>();
		if (configList == null || configList.length == 0)
		{
			return retVel;
		}
		try
		{
			for (String s : configList)
			{
				s = s.replace(",", "");
				int EnumId = Integer.parseInt(s);
				if (EnumId > -1)
				{
					QuickButtonItem tmp = new QuickButtonItem(main.mainActivity, Actions.getActionEnumById(EnumId),
							Actions.getDrawable(EnumId), Actions.getName(EnumId));

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
	public static int GetIndex(Actions attrib)
	{
		return attrib.ordinal();
	}

	public static Actions getActionEnumById(int id)
	{
		switch (id)
		{
		case 0:
			return Actions.DescriptionView;
		case 1:
			return Actions.WaypointView;
		case 2:
			return Actions.LogView;
		case 3:
			return Actions.MapView;
		case 4:
			return Actions.CompassView;
		case 5:
			return Actions.CacheListView;
		case 6:
			return Actions.TrackListView;
		case 7:
			return Actions.TakePhoto;
		case 8:
			return Actions.TakeVideo;
		case 9:
			return Actions.VoiceRecord;
		case 10:
			return Actions.LiveSearch;
		case 11:
			return Actions.Filter;
		case 12:
			return Actions.ScreenLock;
		case 13:
			return Actions.AutoResort;
		case 14:
			return Actions.Solver;
		case 15:
			return Actions.Spoiler;
		case 16:
			return Actions.Hint;
		}
		return Actions.empty;
	}

	public static Drawable getDrawable(int id)
	{
		switch (id)
		{
		case 0:
			return Global.BtnIcons[2]; // Description
		case 1:
			return Global.BtnIcons[3]; // Waypoints
		case 2:
			return Global.BtnIcons[4]; // LogView
		case 3:
			return Global.BtnIcons[5]; // MapView
		case 4:
			return Global.BtnIcons[6]; // CompassView
		case 5:
			return Global.BtnIcons[7]; // CacheListView
		case 6:
			return Global.BtnIcons[8]; // TrackListView
		case 7:
			return Global.BtnIcons[9]; // TakePhoto
		case 8:
			return Global.BtnIcons[10]; // TakeVideo
		case 9:
			return Global.BtnIcons[11]; // VoiceRec
		case 10:
			return Global.BtnIcons[12]; // Live Search
		case 11:
			return Global.BtnIcons[13]; // Filter
		case 12:
			return Global.BtnIcons[14]; // ScreenLock
		case 13:
			return Global.autoResort ? Global.BtnIcons[16] : Global.BtnIcons[15]; // AutoResort
		case 14:
			return Global.BtnIcons[17];
		case 15:
			return Global.BtnIcons[18];
		case 16:
			return Global.BtnIcons[19]; // Hint
		}
		return null;
	}

	public static String getName(int id)
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
		}
		return "empty";
	}

	public static Drawable getDrawable(Actions action)
	{
		return getDrawable(Actions.GetIndex(action));
	}

	public static String getName(Actions action)
	{
		return getName(Actions.GetIndex(action));
	}

	@Override
	public String toString()
	{
		return getName(this);
	}
}
