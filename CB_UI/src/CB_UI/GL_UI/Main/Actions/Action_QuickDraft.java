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
package CB_UI.GL_UI.Main.Actions;

import CB_Core.CacheListChangedListeners;
import CB_Core.LogTypes;
import CB_Core.Types.Cache;
import CB_UI.GL_UI.Controls.PopUps.QuickDraftFeedbackPopUp;
import CB_UI.GL_UI.Views.DraftsView;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheChangedEventListeners;
import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_QuickDraft extends AbstractAction {

    private static Action_QuickDraft that;

    private Action_QuickDraft() {
        super("QuickDraft", MenuID.AID_QUICK_DRAFT);
    }

    public static Action_QuickDraft getInstance() {
        if (that == null) that = new Action_QuickDraft();
        return that;
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
    public void Execute() {
        Menu cm = new Menu("QuickDraft");
        Cache cache = GlobalCore.getSelectedCache();
        switch (cache.getType()) {
            case Event:
            case MegaEvent:
            case Giga:
            case CITO:
                cm.addMenuItem("attended", Sprites.getSprite("log9icon"), () -> {
                    DraftsView.addNewDraft(LogTypes.attended, "", true);
                    finalHandling(true);
                });
                break;
            case Camera:
                cm.addMenuItem("webCamFotoTaken", Sprites.getSprite("log10icon"), () -> {
                    DraftsView.addNewDraft(LogTypes.webcam_photo_taken, "", true);
                    finalHandling(true);
                });
                cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> {
                    finalHandling(false);
                });
                break;
            default:
                cm.addMenuItem("found", Sprites.getSprite("log0icon"), () -> {
                    DraftsView.addNewDraft(LogTypes.found, "", true);
                    finalHandling(true);
                });
                cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> {
                    DraftsView.addNewDraft(LogTypes.didnt_find, "", true);
                    finalHandling(false);
                });
                break;
        }
        cm.show();
    }

    private void finalHandling(boolean found) {
        DraftsView.getInstance().notifyDataSetChanged();
        // damit der Status geändert wird
        // damit die Icons in der Map aktualisiert werden
        CacheListChangedListeners.getInstance().cacheListChanged();
        SelectedCacheChangedEventListeners.getInstance().fireEvent(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
        QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(found);
        pop.show(PopUp_Base.SHOW_TIME_SHORT);
        PlatformUIBase.vibrate();
    }

}
