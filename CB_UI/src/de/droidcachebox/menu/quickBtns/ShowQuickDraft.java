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

import static de.droidcachebox.menu.Action.ShowDrafts;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.menuBtn4.ShowDrafts;

public class ShowQuickDraft extends AbstractAction {

    public ShowQuickDraft() {
        super("QuickDraft");
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
                    ((ShowDrafts) ShowDrafts.action).addNewDraft(LogType.attended, false);
                });
                break;
            case Camera:
                cm.addMenuItem("webCamFotoTaken", Sprites.getSprite("log10icon"), () -> {
                    ((ShowDrafts) ShowDrafts.action).addNewDraft(LogType.webcam_photo_taken, false);
                });
                break;
            default:
                cm.addMenuItem("found", Sprites.getSprite("log0icon"), () -> {
                    ((ShowDrafts) ShowDrafts.action).addNewDraft(LogType.found, false);
                });
                cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> {
                    ((ShowDrafts) ShowDrafts.action).addNewDraft(LogType.didnt_find, false);
                });
                break;
        }
        cm.show();
    }

}
