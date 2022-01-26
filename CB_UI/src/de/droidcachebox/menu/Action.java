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
    ShowDescription(new ShowDescription(),"Description"), // 0
    ShowWayPoints(new ShowWayPoints(), "Waypoints"), // 1
    ShowLogs(new ShowLogs(),"ShowLogs"), // 2
    ShowMap(new ShowMap(),"Map"), // 3
    ShowCompass(new ShowCompass(), "Compass"), // 4
    ShowGeoCaches(new ShowGeoCaches(), "cacheList"), // 5
    ShowTracks(new ShowTracks(), "Tracks"), // 6
    TakePicture(new PlatformAction("TakePhoto", ViewConst.TAKE_PHOTO, Sprites.getSprite(Sprites.IconName.log10icon.name())), "TakePhoto"), // 7
    RecordVideo(new PlatformAction("RecVideo", ViewConst.VIDEO_REC, Sprites.getSprite(Sprites.IconName.videoIcon.name())), "RecVideo"), // 8
    RecordVoice(new PlatformAction("VoiceRec", ViewConst.VOICE_REC, Sprites.getSprite(Sprites.IconName.voiceRecIcon.name())), "VoiceRec"), // 9
    ShowSearchDialog(new ShowSearchDialog(), "Search"), // 10
    ShowEditFilterSettings(new ShowEditFilterSettings(), "Filter"), // 11
    SwitchAutoResort(new SwitchAutoResort(), "AutoResort"), // 12
    ShowSolver1(new ShowSolver1(), "Solver"), // 13
    ShowSpoiler(new ShowSpoiler(), "spoiler"), // 14
    ShowHint(new ShowHint(), "hint"), // 15
    ShowParkingMenu(new ShowParkingMenu(), "MyParking"), // 16
    SwitchDayNight(new SwitchDayNight(), "DayNight"), // 17
    ShowDrafts(new ShowDrafts(), "Drafts"), // 18
    ShowQuickDraft(new ShowQuickDraft(), "QuickDraft"), // 19
    ShowTrackableList(new ShowTrackableList(), "TBList"), // 20
    AddWayPoint(new AddWayPoint(), "AddWaypoint"), // 21
    ShowSolver2(new ShowSolver2(), "Solver v2"), // 22
    ShowNotes(new ShowNotes(), "Notes"), // 23
    UploadDrafts(new UploadDrafts(), "uploadDrafts"), // 24
    SwitchTorch(new SwitchTorch(), "torch"), // 25
    CreateRoute(new CreateRoute(), "generateRoute"),
    RememberGeoCache(new RememberGeoCache(), "rememberGeoCacheTitle"),
    ;

    public AbstractAction action;
    public String translationId;

    Action(AbstractAction action, String translationId) {
        this.action = action;
        this.translationId = translationId;
    }
}
