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
package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.DescriptionView;

public class Abstract_ShowDescriptionAction extends AbstractShowAction {

    private static final int AID_SHOW_DESCRIPTION = 105;
    private static Abstract_ShowDescriptionAction that;

    private Abstract_ShowDescriptionAction() {
        super("Description", AID_SHOW_DESCRIPTION);
    }

    public static Abstract_ShowDescriptionAction getInstance() {
        if (that == null) that = new Abstract_ShowDescriptionAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(DescriptionView.getInstance());
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
        return DescriptionView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return CacheContextMenu.getCacheContextMenu(false);
    }

    public void updateDescriptionView(boolean forceReload) {
        if (forceReload) DescriptionView.getInstance().forceReload();
        if (DescriptionView.getInstance().isVisible()) {
            // so this will never be called, i think
            DescriptionView.getInstance().onShow();
        }
    }
}
