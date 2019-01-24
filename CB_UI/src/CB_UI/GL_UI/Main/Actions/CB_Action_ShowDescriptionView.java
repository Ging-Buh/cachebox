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
package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Menu.CacheContextMenu;
import CB_UI.GL_UI.Views.DescriptionView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowDescriptionView extends CB_Action_ShowView {

    private static final int AID_SHOW_DESCRIPTION = 105;
    private static CB_Action_ShowDescriptionView that;

    private CB_Action_ShowDescriptionView() {
        super("Description", AID_SHOW_DESCRIPTION);
        tabMainView = TabMainView.that;
        tab = TabMainView.leftTab;
    }

    public static CB_Action_ShowDescriptionView getInstance() {
        if (that == null) that = new CB_Action_ShowDescriptionView();
        return that;
    }

    @Override
    public void Execute() {
        if ((TabMainView.descriptionView == null) && (tabMainView != null) && (tab != null))
            TabMainView.descriptionView = new DescriptionView(tab.getContentRec(), "DescriptionView");

        if ((TabMainView.descriptionView != null) && (tab != null))
            tab.ShowView(TabMainView.descriptionView);
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
        return TabMainView.descriptionView;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return CacheContextMenu.getCacheContextMenu(false);
    }
}
