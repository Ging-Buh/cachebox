/*
 * Copyright (C) 2015 team-cachebox.de
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
package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.contextmenus.CacheContextMenu;
import de.droidcachebox.menu.menuBtn2.executes.DescriptionView;

public class ShowDescription extends AbstractShowAction {

    private DescriptionView descriptionView;

    public ShowDescription() {
        super("Description");
    }

    @Override
    public void execute() {
        if (descriptionView == null) descriptionView = new DescriptionView();
        ViewManager.leftTab.showView(descriptionView);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.docIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return descriptionView;
    }

    @Override
    public void viewIsHiding() {
        descriptionView = null;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return CacheContextMenu.getInstance().getCacheContextMenu(false);
    }

    public void updateDescriptionView(boolean forceReload) {
        if (descriptionView != null) {
            if (forceReload) descriptionView.forceReload();
            if (descriptionView.isVisible()) {
                // so this will never be called, i think
                descriptionView.onShow();
            }
        }
    }
}
