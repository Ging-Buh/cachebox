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

import CB_Core.CacheListChangedEventList;
import CB_Core.LogTypes;
import CB_Core.Types.Cache;
import CB_UI.GL_UI.Controls.PopUps.QuickFieldNoteFeedbackPopUp;
import CB_UI.GL_UI.Views.FieldNotesView;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_QuickFieldNote extends CB_Action {

    private static CB_Action_QuickFieldNote that;

    private CB_Action_QuickFieldNote() {
        super("QuickFieldNote", MenuID.AID_QUICK_FIELDNOTE);
    }

    public static CB_Action_QuickFieldNote getInstance() {
        if (that == null) that = new CB_Action_QuickFieldNote();
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
        Menu cm = new Menu("QuickFieldNote");

        cm.addOnClickListener((v, x, y, pointer, button) -> {
            boolean found = true;
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_WEBCAM_FOTO_TAKEN:
                    FieldNotesView.addNewFieldNote(LogTypes.webcam_photo_taken, "", true);
                    break;
                case MenuID.MI_ATTENDED:
                    FieldNotesView.addNewFieldNote(LogTypes.attended, "", true);
                    break;
                case MenuID.MI_QUICK_FOUND:
                    FieldNotesView.addNewFieldNote(LogTypes.found, "", true);
                    break;
                case MenuID.MI_QUICK_NOT_FOUND:
                    FieldNotesView.addNewFieldNote(LogTypes.didnt_find, "", true);
                    found = false;
                    break;
                default:
                    return false;
            }
            if (FieldNotesView.that != null)
                FieldNotesView.that.notifyDataSetChanged();
            // damit der Status geändert wird
            // damit die Icons in der Map aktualisiert werden
            CacheListChangedEventList.Call();
            SelectedCacheEventList.Call(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
            QuickFieldNoteFeedbackPopUp pop = new QuickFieldNoteFeedbackPopUp(found);
            pop.show(PopUp_Base.SHOW_TIME_SHORT);
            PlatformConnector.vibrate();
            return true;
        });

        Cache cache = GlobalCore.getSelectedCache();
        switch (cache.Type) {
            case Event:
            case MegaEvent:
            case Giga:
            case CITO:
                cm.addItem(MenuID.MI_ATTENDED, "attended", Sprites.getSprite("log9icon"));
                break;
            case Camera:
                cm.addItem(MenuID.MI_WEBCAM_FOTO_TAKEN, "webCamFotoTaken", Sprites.getSprite("log10icon"));
                cm.addItem(MenuID.MI_QUICK_NOT_FOUND, "DNF", Sprites.getSprite("log1icon"));
                break;
            default:
                cm.addItem(MenuID.MI_QUICK_FOUND, "found", Sprites.getSprite("log0icon"));
                cm.addItem(MenuID.MI_QUICK_NOT_FOUND, "DNF", Sprites.getSprite("log1icon"));
                break;
        }

        cm.Show();

    }

}
