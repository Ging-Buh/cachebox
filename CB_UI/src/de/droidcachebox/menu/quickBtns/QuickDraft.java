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
package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.popups.PopUp_Base;
import de.droidcachebox.gdx.controls.popups.QuickDraftFeedbackPopUp;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.menuBtn4.ShowDrafts;
import de.droidcachebox.menu.menuBtn4.executes.DraftsView;

public class QuickDraft extends AbstractAction {

    private static QuickDraft quickDraft;
    private DraftsView draftsView;

    private QuickDraft() {
        super("QuickDraft");
        draftsView = new DraftsView();
    }

    public static QuickDraft getInstance() {
        if (quickDraft == null) quickDraft = new QuickDraft();
        return quickDraft;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.FieldNote.name());
    }

    @Override
    public void execute() {
        Menu cm = new Menu("QuickDraft");
        Cache cache = GlobalCore.getSelectedCache();
        switch (cache.getGeoCacheType()) {
            case Event:
            case MegaEvent:
            case Giga:
            case CITO:
                cm.addMenuItem("attended", Sprites.getSprite("log9icon"), () -> {
                    ShowDrafts.getInstance().addNewDraft(LogType.attended, false);
                    finalHandling(true);
                });
                break;
            case Camera:
                cm.addMenuItem("webCamFotoTaken", Sprites.getSprite("log10icon"), () -> {
                    ShowDrafts.getInstance().addNewDraft(LogType.webcam_photo_taken, false);
                    finalHandling(true);
                });
                cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> finalHandling(false));
                break;
            default:
                cm.addMenuItem("found", Sprites.getSprite("log0icon"), () -> {
                    ShowDrafts.getInstance().addNewDraft(LogType.found, false);
                    finalHandling(true);
                });
                cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> {
                    ShowDrafts.getInstance().addNewDraft(LogType.didnt_find, false);
                    finalHandling(false);
                });
                break;
        }
        cm.show();
    }

    private void finalHandling(boolean found) {
        draftsView.notifyDataSetChanged();
        // for status change, for icons in map
        CacheListChangedListeners.getInstance().cacheListChanged();
        CacheSelectionChangedListeners.getInstance().fire(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWayPoint());
        new QuickDraftFeedbackPopUp(found).show(PopUp_Base.SHOW_TIME_SHORT);
        Platform.vibrate();
    }

}
