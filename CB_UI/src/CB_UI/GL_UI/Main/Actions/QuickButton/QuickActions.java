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
package CB_UI.GL_UI.Main.Actions.QuickButton;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Main.Actions.CB_Action_UploadFieldNote;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Util.MoveableList;

import static CB_UI.GL_UI.Main.TabMainView.*;

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
    QuickFieldNotes, // 19
    TrackableListView, // 20
    addWP, // 21
    Solver2, // 22
    Notesview, // 23
    uploadFieldNote, // 24
    torch, // 25

    // ScreenLock, // 21

    empty,;

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
    // private static CB_Action action_ScreenLock;

    /**
     * Gibt die ID des übergebenen Enums zurück
     *
     * @param attrib
     * @return long
     */
    public static int GetIndex(QuickActions attrib) {
        return attrib.ordinal();
    }

    public static CB_Action getActionEnumById(int id) {
        switch (id) {
            case 0:
                return actionShowDescriptionView;
            case 1:
                return actionShowWaypointView;
            case 2:
                return actionShowLogView;
            case 3:
                return actionShowMap;
            case 4:
                return actionShowCompassView;
            case 5:
                return actionShowCacheList;
            case 6:
                return actionShowTrackListView;
            case 7:
                return actionRecPicture;
            case 8:
                return actionRecVideo;
            case 9:
                return actionRecVoice;
            case 10:
                return actionShowSearch;
            case 11:
                return actionShowFilter;
            case 12:
                return action_SwitchAutoResort;
            case 13:
                return actionShowSolverView;
            case 14:
                return actionShowSpoilerView;
            case 15:
                return actionShowHint;
            case 16:
                return actionShowParking;
            case 17:
                return action_DayNight;
            case 18:
                return actionShowFieldNotesView;
            case 19:
                return actionQuickFieldNote;
            case 20:
                return actionShowTrackableListView;
            case 21:
                return action_add_WP;
            case 22:
                return actionShowSolverView2;
            case 23:
                return actionShowNotesView;
            case 24:
                return CB_Action_UploadFieldNote.INSTANCE;
            case 25:
                return action_Torch;

        }
        return null;
    }

    public static String getName(int id) {
        switch (id) {
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
                return Translation.Get("Filter");
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
            case 20:
                return Translation.Get("TBList");
            case 21:
                return Translation.Get("AddWaypoint");
            case 22:
                return Translation.Get("Solver") + " 2";
            case 23:
                return Translation.Get("Notes");
            case 24:
                return Translation.Get("uploadFieldNotes");
            case 25:
                return Translation.Get("torch");

        }
        return "empty";
    }
}
