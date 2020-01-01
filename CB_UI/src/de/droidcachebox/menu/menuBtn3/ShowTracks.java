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
package de.droidcachebox.menu.menuBtn3;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.TrackCreation;
import de.droidcachebox.gdx.views.TrackListView;
import de.droidcachebox.menu.ViewManager;

public class ShowTracks extends AbstractShowAction {
    private static ShowTracks showTracks;

    private ShowTracks() {
        super("Tracks");
    }

    public static ShowTracks getInstance() {
        if (showTracks == null) showTracks = new ShowTracks();
        return showTracks;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(TrackListView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.trackListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return TrackListView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("TrackListViewContextMenuTitle");
        cm.addMenuItem("load", null, TrackListView.getInstance()::selectTrackFileReadAndAddToTracks);
        cm.addMenuItem("generate", null, () -> TrackCreation.getInstance().execute());
        return cm;
    }
}