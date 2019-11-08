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
package de.droidcachebox.gdx.main;

import de.droidcachebox.Config;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MoveableList;

import static de.droidcachebox.gdx.main.ViewManager.*;

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
    LiveSearch, // 10
    Filter, // 11
    AutoResort, // 12
    Solver, // 13
    Spoiler, // 14
    Hint, // 15
    Parking, // 16
    Day_Night, // 17
    FieldNotes, // 18
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
                    if (QuickActions.getActionEnumById(EnumId) != null) {
                        QuickButtonItem tmp = new QuickButtonItem(new CB_RectF(0, 0, height, height), index++, QuickActions.getActionEnumById(EnumId), QuickActions.getName(EnumId), type);
                        retList.add(tmp);
                    } else
                        invalidEnumId = true;
                }
            }
        } catch (Exception e)// wenn ein Fehler auftritt, gib die bis dorthin
        // gelesenen Items zurück
        {

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

    /**
     * Gibt die ID des übergebenen Enums zurück
     *
     * @param attrib
     * @return long
     */
    public static int GetIndex(QuickActions attrib) {
        return attrib.ordinal();
    }

    public static AbstractAction getActionEnumById(int id) {
        switch (id) {
            case 0:
                return Abstract_ShowDescriptionAction.getInstance();
            case 1:
                return Abstract_ShowWaypointAction.getInstance();
            case 2:
                return Abstract_ShowLogAction.getInstance();
            case 3:
                return Abstract_ShowMap.getInstance();
            case 4:
                return Abstract_ShowCompassAction.getInstance();
            case 5:
                return Abstract_ShowCacheList.getInstance();
            case 6:
                return Abstract_ShowTrackListAction.getInstance();
            case 7:
                return actionTakePicture;
            case 8:
                return actionRecordVideo;
            case 9:
                return actionRecordVoice;
            case 10:
                return de.droidcachebox.gdx.main.Action_SearchDialog.getInstance();
            case 11:
                return de.droidcachebox.gdx.main.Action_EditFilterSettings.getInstance();
            case 12:
                return de.droidcachebox.gdx.main.Action_switch_Autoresort.getInstance();
            case 13:
                return Abstract_ShowSolverAction.getInstance();
            case 14:
                return Abstract_ShowSpoilerAction.getInstance();
            case 15:
                return de.droidcachebox.gdx.main.Action_HintDialog.getInstance();
            case 16:
                return de.droidcachebox.gdx.main.Action_ParkingDialog.getInstance();
            case 17:
                return de.droidcachebox.gdx.main.Action_switch_DayNight.getInstance();
            case 18:
                return Abstract_ShowDraftsAction.getInstance();
            case 19:
                return de.droidcachebox.gdx.main.Action_QuickDraft.getInstance();
            case 20:
                return Abstract_ShowTrackableListAction.getInstance();
            case 21:
                return Action_Add_WP.getInstance();
            case 22:
                return Abstract_ShowSolverAction2.getInstance();
            case 23:
                return Abstract_ShowNotesAction.getInstance();
            case 24:
                return de.droidcachebox.gdx.main.Action_UploadDrafts.getInstance();
            case 25:
                return de.droidcachebox.gdx.main.Action_switch_Torch.getInstance();

        }
        return null;
    }

    public static String getName(int id) {
        switch (id) {
            case 0:
                return Translation.get("Description");
            case 1:
                return Translation.get("Waypoints");
            case 2:
                return Translation.get("ShowLogs");
            case 3:
                return Translation.get("Map");
            case 4:
                return Translation.get("Compass");
            case 5:
                return Translation.get("cacheList");
            case 6:
                return Translation.get("Tracks");
            case 7:
                return Translation.get("TakePhoto");
            case 8:
                return Translation.get("RecVideo");
            case 9:
                return Translation.get("VoiceRec");
            case 10:
                return Translation.get("Search");
            case 11:
                return Translation.get("Filter");
            case 12:
                return Translation.get("AutoResort");
            case 13:
                return Translation.get("Solver");
            case 14:
                return Translation.get("spoiler");
            case 15:
                return Translation.get("hint");
            case 16:
                return Translation.get("MyParking");
            case 17:
                return Translation.get("DayNight");
            case 18:
                return Translation.get("Drafts");
            case 19:
                return Translation.get("QuickDraft");
            case 20:
                return Translation.get("TBList");
            case 21:
                return Translation.get("AddWaypoint");
            case 22:
                return Translation.get("Solver") + " 2";
            case 23:
                return Translation.get("Notes");
            case 24:
                return Translation.get("uploadDrafts");
            case 25:
                return Translation.get("torch");

        }
        return "empty";
    }
}
