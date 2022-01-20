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
package de.droidcachebox.menu;

import static de.droidcachebox.menu.ViewManager.actionRecordVideo;
import static de.droidcachebox.menu.ViewManager.actionRecordVoice;
import static de.droidcachebox.menu.ViewManager.actionTakePicture;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.menu.menuBtn1.ShowGeoCaches;
import de.droidcachebox.menu.menuBtn1.ShowParkingMenu;
import de.droidcachebox.menu.menuBtn1.ShowTrackables;
import de.droidcachebox.menu.menuBtn2.ShowDescription;
import de.droidcachebox.menu.menuBtn2.ShowHint;
import de.droidcachebox.menu.menuBtn2.ShowLogs;
import de.droidcachebox.menu.menuBtn2.ShowNotes;
import de.droidcachebox.menu.menuBtn2.ShowSpoiler;
import de.droidcachebox.menu.menuBtn2.ShowWaypoints;
import de.droidcachebox.menu.menuBtn3.ShowCompass;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.menu.menuBtn3.ShowTracks;
import de.droidcachebox.menu.menuBtn4.ShowDrafts;
import de.droidcachebox.menu.menuBtn4.ShowSolver1;
import de.droidcachebox.menu.menuBtn4.ShowSolver2;
import de.droidcachebox.menu.menuBtn4.UploadDrafts;
import de.droidcachebox.menu.menuBtn5.SwitchDayNight;
import de.droidcachebox.menu.menuBtn5.SwitchTorch;
import de.droidcachebox.menu.quickBtns.AddWayPoint;
import de.droidcachebox.menu.quickBtns.CreateRoute;
import de.droidcachebox.menu.quickBtns.EditFilterSettings;
import de.droidcachebox.menu.quickBtns.RememberGeoCache;
import de.droidcachebox.menu.quickBtns.ShowSearchDialog;
import de.droidcachebox.menu.quickBtns.SwitchAutoresort;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 *
 * @author Longri
 */
public enum QuickAction {
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
    createRoute,
    rememberGeoCache,

    // ScreenLock, // 21

    empty,
    ;

    public AbstractAction getAction() {
        switch (this) {
            case DescriptionView:
                return ShowDescription.getInstance();
            case WaypointView:
                return ShowWaypoints.getInstance();
            case LogView:
                return ShowLogs.getInstance();
            case MapView:
                return ShowMap.getInstance();
            case CompassView:
                return ShowCompass.getInstance();
            case CacheListView:
                return ShowGeoCaches.getInstance();
            case TrackListView:
                return ShowTracks.getInstance();
            case TakePhoto:
                return actionTakePicture;
            case TakeVideo:
                return actionRecordVideo;
            case VoiceRecord:
                return actionRecordVoice;
            case Search:
                return ShowSearchDialog.getInstance();
            case Filter:
                return EditFilterSettings.getInstance();
            case AutoResort:
                return SwitchAutoresort.getInstance();
            case Solver:
                return ShowSolver1.getInstance();
            case Spoiler:
                return ShowSpoiler.getInstance();
            case Hint:
                return ShowHint.getInstance();
            case Parking:
                return ShowParkingMenu.getInstance();
            case Day_Night:
                return SwitchDayNight.getInstance();
            case Drafts:
                return ShowDrafts.getInstance();
            case QuickDraft:
                return de.droidcachebox.menu.quickBtns.QuickDraft.getInstance();
            case TrackableListView:
                return ShowTrackables.getInstance();
            case addWP:
                return AddWayPoint.getInstance();
            case Solver2:
                return ShowSolver2.getInstance();
            case Notesview:
                return ShowNotes.getInstance();
            case uploadDrafts:
                return UploadDrafts.getInstance();
            case torch:
                return SwitchTorch.getInstance();
            case createRoute:
                return CreateRoute.getInstance();
            case rememberGeoCache:
                return RememberGeoCache.getInstance();
        }
        return null;
    }

    public String getName() {
        switch (this) {
            case DescriptionView:
                return "Description";
            case WaypointView:
                return "Waypoints";
            case LogView:
                return "ShowLogs";
            case MapView:
                return "Map";
            case CompassView:
                return "Compass";
            case CacheListView:
                return "cacheList";
            case TrackListView:
                return "Tracks";
            case TakePhoto:
                return "TakePhoto";
            case TakeVideo:
                return "RecVideo";
            case VoiceRecord:
                return "VoiceRec";
            case Search:
                return "Search";
            case Filter:
                return "Filter";
            case AutoResort:
                return "AutoResort";
            case Solver:
                return "Solver";
            case Spoiler:
                return "spoiler";
            case Hint:
                return "hint";
            case Parking:
                return "MyParking";
            case Day_Night:
                return "DayNight";
            case Drafts:
                return "Drafts";
            case QuickDraft:
                return "QuickDraft";
            case TrackableListView:
                return "TBList";
            case addWP:
                return "AddWaypoint";
            case Solver2:
                return "Solver v2";
            case Notesview:
                return "Notes";
            case uploadDrafts:
                return "uploadDrafts";
            case torch:
                return "torch";
            case createRoute:
                return "generateRoute";
            case rememberGeoCache:
                return "rememberGeoCacheTitle";
        }
        return "empty";
    }
}
