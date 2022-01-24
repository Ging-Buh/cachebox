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
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.executes.TrackCreation;
import de.droidcachebox.menu.menuBtn3.executes.TrackList;
import de.droidcachebox.menu.menuBtn3.executes.TrackListView;

public class ShowTracks extends AbstractShowAction {
    private TrackListView trackListView;
    boolean isExecuting;

    public ShowTracks() {
        super("Tracks");
        isExecuting = false;
    }

    @Override
    public void execute() {
        isExecuting = true;
        trackListView = new TrackListView();
        ViewManager.leftTab.showView(trackListView);
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
        return trackListView;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("TrackListViewContextMenuTitle");
        cm.addMenuItem("load", null, TrackList.getInstance()::selectTrackFileReadAndAddToTracks);
        cm.addMenuItem("generate", null, TrackCreation.getInstance()::execute);
        return cm;
    }

    public void onHide() {
        isExecuting = false;
        trackListView = null;
    }

    public void notifyDataSetChanged() {
        if (isExecuting) {
            trackListView.notifyDataSetChanged();
        }
    }

    public void notifyCurrentRouteChanged() {
        if (isExecuting) {
            trackListView.notifyCurrentRouteChanged();
        }
    }
}