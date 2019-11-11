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
package de.droidcachebox.main;

import de.droidcachebox.Config;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings;
import de.droidcachebox.main.menuBtn4.UploadDrafts;
import de.droidcachebox.main.quickBtns.*;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.main.menuBtn1.ParkingDialog;
import de.droidcachebox.main.menuBtn1.ShowCacheList;
import de.droidcachebox.main.menuBtn1.ShowTrackableList;
import de.droidcachebox.main.menuBtn2.*;
import de.droidcachebox.main.menuBtn3.ShowCompass;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.main.menuBtn3.ShowTrackList;
import de.droidcachebox.main.menuBtn4.ShowDrafts;
import de.droidcachebox.main.menuBtn4.ShowSolver1;
import de.droidcachebox.main.menuBtn4.ShowSolver2;
import de.droidcachebox.main.menuBtn5.SwitchDayNight;
import de.droidcachebox.main.menuBtn5.SwitchTorch;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MoveableList;

import static de.droidcachebox.main.ViewManager.*;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 *
 * @author Longri
 */
public enum QuickActions {
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
    Search, // 10
    Filter, // 11
    AutoResort, // 12
    Solver, // 13
    Spoiler, // 14
    Hint, // 15
    Parking, // 16
    Day_Night, // 17
    Drafts, // 18
    QuickDraft, // 19
    TrackableListView, // 20
    addWP, // 21
    Solver2, // 22
    Notesview, // 23
    uploadDrafts, // 24
    torch, // 25

    // ScreenLock, // 21

    empty,
    ;

    public static MoveableList<QuickButtonItem> getListFromConfig(String[] configList, float height) {
        MoveableList<QuickButtonItem> retList = new MoveableList<QuickButtonItem>();
        if (configList == null || configList.length == 0) {
            return retList;
        }

        boolean invalidEnumId = false;
        try {
            int index = 0;

            for (String s : configList) {
                s = s.replace(",", "");
                int EnumId = Integer.parseInt(s);
                if (EnumId > -1) {

                    QuickActions type = QuickActions.values()[EnumId];
                    if (QuickActions.getAction(type) != null) {
                        QuickButtonItem tmp = new QuickButtonItem(new CB_RectF(0, 0, height, height), index++, QuickActions.getAction(type), QuickActions.getName(type), type);
                        retList.add(tmp);
                    } else
                        invalidEnumId = true;
                }
            }
        } catch (Exception ignored)
        {
            // wenn ein Fehler auftritt, gib die bis dorthin gelesenen Items zurück
        }
        if (invalidEnumId) {
            //	    write valid id's back

            String ActionsString = "";
            int counter = 0;
            for (int i = 0, n = retList.size(); i < n; i++) {
                QuickButtonItem tmp = retList.get(i);
                ActionsString += String.valueOf(tmp.getAction().ordinal());
                if (counter < retList.size() - 1) {
                    ActionsString += ",";
                }
                counter++;
            }
            Config.quickButtonList.setValue(ActionsString);
            Config.AcceptChanges();
        }
        return retList;
    }
    // private static AbstractAction action_ScreenLock;

    public static AbstractAction getAction(QuickActions id) {
        switch (id) {
            case DescriptionView:
                return ShowDescription.getInstance();
            case WaypointView:
                return ShowWaypoint.getInstance();
            case LogView:
                return ShowLogs.getInstance();
            case MapView:
                return ShowMap.getInstance();
            case CompassView:
                return ShowCompass.getInstance();
            case CacheListView:
                return ShowCacheList.getInstance();
            case TrackListView:
                return ShowTrackList.getInstance();
            case TakePhoto:
                return actionTakePicture;
            case TakeVideo:
                return actionRecordVideo;
            case VoiceRecord:
                return actionRecordVoice;
            case Search:
                return SearchDialog.getInstance();
            case Filter:
                return EditFilterSettings.getInstance();
            case AutoResort:
                return SwitchAutoresort.getInstance();
            case Solver:
                return ShowSolver1.getInstance();
            case Spoiler:
                return ShowSpoiler.getInstance();
            case Hint:
                return HintDialog.getInstance();
            case Parking:
                return ParkingDialog.getInstance();
            case Day_Night:
                return SwitchDayNight.getInstance();
            case Drafts:
                return ShowDrafts.getInstance();
            case QuickDraft:
                return de.droidcachebox.main.quickBtns.QuickDraft.getInstance();
            case TrackableListView:
                return ShowTrackableList.getInstance();
            case addWP:
                return Add_WP.getInstance();
            case Solver2:
                return ShowSolver2.getInstance();
            case Notesview:
                return ShowNotes.getInstance();
            case uploadDrafts:
                return UploadDrafts.getInstance();
            case torch:
                return SwitchTorch.getInstance();

        }
        return null;
    }

    public static String getName(QuickActions id) {
        switch (id) {
            case DescriptionView:
                return Translation.get("Description");
            case WaypointView:
                return Translation.get("Waypoints");
            case LogView:
                return Translation.get("ShowLogs");
            case MapView:
                return Translation.get("Map");
            case CompassView:
                return Translation.get("Compass");
            case CacheListView:
                return Translation.get("cacheList");
            case TrackListView:
                return Translation.get("Tracks");
            case TakePhoto:
                return Translation.get("TakePhoto");
            case TakeVideo:
                return Translation.get("RecVideo");
            case VoiceRecord:
                return Translation.get("VoiceRec");
            case Search:
                return Translation.get("Search");
            case Filter:
                return Translation.get("Filter");
            case AutoResort:
                return Translation.get("AutoResort");
            case Solver:
                return Translation.get("Solver");
            case Spoiler:
                return Translation.get("spoiler");
            case Hint:
                return Translation.get("hint");
            case Parking:
                return Translation.get("MyParking");
            case Day_Night:
                return Translation.get("DayNight");
            case Drafts:
                return Translation.get("Drafts");
            case QuickDraft:
                return Translation.get("QuickDraft");
            case TrackableListView:
                return Translation.get("TBList");
            case addWP:
                return Translation.get("AddWaypoint");
            case Solver2:
                return Translation.get("Solver") + " 2";
            case Notesview:
                return Translation.get("Notes");
            case uploadDrafts:
                return Translation.get("uploadDrafts");
            case torch:
                return Translation.get("torch");
        }
        return "empty";
    }
}
