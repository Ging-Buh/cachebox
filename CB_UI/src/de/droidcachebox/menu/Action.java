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

import de.droidcachebox.AbstractAction;
import de.droidcachebox.PlatformAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.menu.menuBtn1.ShowGeoCaches;
import de.droidcachebox.menu.menuBtn1.ShowParkingMenu;
import de.droidcachebox.menu.menuBtn1.ShowTrackableList;
import de.droidcachebox.menu.menuBtn2.ShowDescription;
import de.droidcachebox.menu.menuBtn2.ShowHint;
import de.droidcachebox.menu.menuBtn2.ShowLogs;
import de.droidcachebox.menu.menuBtn2.ShowNotes;
import de.droidcachebox.menu.menuBtn2.ShowSpoiler;
import de.droidcachebox.menu.menuBtn2.ShowWayPoints;
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
import de.droidcachebox.menu.quickBtns.RememberGeoCache;
import de.droidcachebox.menu.quickBtns.ShowEditFilterSettings;
import de.droidcachebox.menu.quickBtns.ShowQuickDraft;
import de.droidcachebox.menu.quickBtns.ShowSearchDialog;
import de.droidcachebox.menu.quickBtns.SwitchAutoResort;

/**
 * contains the possible actions for Quick Buttons
 *
 * @author Longri
 */
public enum Action {
    ShowDescription(new ShowDescription()), // 0
    ShowWayPoints(new ShowWayPoints()), // 1
    ShowLogs(new ShowLogs()), // 2
    ShowMap(new ShowMap()), // 3
    ShowCompass(new ShowCompass()), // 4
    ShowGeoCaches(new ShowGeoCaches()), // 5
    ShowTracks(new ShowTracks()), // 6
    TakePicture(new PlatformAction("TakePhoto", ViewConst.TAKE_PHOTO, Sprites.getSprite(Sprites.IconName.log10icon.name()))), // 7
    RecordVideo(new PlatformAction("RecVideo", ViewConst.VIDEO_REC, Sprites.getSprite(Sprites.IconName.videoIcon.name()))), // 8
    RecordVoice(new PlatformAction("VoiceRec", ViewConst.VOICE_REC, Sprites.getSprite(Sprites.IconName.voiceRecIcon.name()))), // 9
    ShowSearchDialog(new ShowSearchDialog()), // 10
    ShowEditFilterSettings(new ShowEditFilterSettings()), // 11
    SwitchAutoResort(new SwitchAutoResort()), // 12
    ShowSolver1(new ShowSolver1()), // 13
    ShowSpoiler(new ShowSpoiler()), // 14
    ShowHint(new ShowHint()), // 15
    ShowParkingMenu(new ShowParkingMenu()), // 16
    SwitchDayNight(new SwitchDayNight()), // 17
    ShowDrafts(new ShowDrafts()), // 18
    ShowQuickDraft(new ShowQuickDraft()), // 19
    ShowTrackableList(new ShowTrackableList()), // 20
    AddWayPoint(new AddWayPoint()), // 21
    ShowSolver2(new ShowSolver2()), // 22
    ShowNotes(new ShowNotes()), // 23
    UploadDrafts(new UploadDrafts()), // 24
    SwitchTorch(new SwitchTorch()), // 25
    CreateRoute(new CreateRoute()),
    RememberGeoCache(new RememberGeoCache()),
    ;

    public AbstractAction action;

    Action(AbstractAction action) {
        this.action = action;
    }

    public String getName() {
        switch (this) {
            case ShowDescription:
                return "Description";
            case ShowWayPoints:
                return "Waypoints";
            case ShowLogs:
                return "ShowLogs";
            case ShowMap:
                return "Map";
            case ShowCompass:
                return "Compass";
            case ShowGeoCaches:
                return "cacheList";
            case ShowTracks:
                return "Tracks";
            case TakePicture:
                return "TakePhoto";
            case RecordVideo:
                return "RecVideo";
            case RecordVoice:
                return "VoiceRec";
            case ShowSearchDialog:
                return "Search";
            case ShowEditFilterSettings:
                return "Filter";
            case SwitchAutoResort:
                return "AutoResort";
            case ShowSolver1:
                return "Solver";
            case ShowSpoiler:
                return "spoiler";
            case ShowHint:
                return "hint";
            case ShowParkingMenu:
                return "MyParking";
            case SwitchDayNight:
                return "DayNight";
            case ShowDrafts:
                return "Drafts";
            case ShowQuickDraft:
                return "QuickDraft";
            case ShowTrackableList:
                return "TBList";
            case AddWayPoint:
                return "AddWaypoint";
            case ShowSolver2:
                return "Solver v2";
            case ShowNotes:
                return "Notes";
            case UploadDrafts:
                return "uploadDrafts";
            case SwitchTorch:
                return "torch";
            case CreateRoute:
                return "generateRoute";
            case RememberGeoCache:
                return "rememberGeoCacheTitle";
        }
        return "empty";
    }
}
