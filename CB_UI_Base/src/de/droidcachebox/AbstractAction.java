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
package de.droidcachebox;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.gdx.main.Menu;

public abstract class AbstractAction {

    protected String titleTranslationId;
    protected String titleExtension;

    public AbstractAction(String titleTranslationId) {
        this.titleTranslationId = titleTranslationId;
        titleExtension = "";
    }

    public abstract void execute();

    public abstract Sprite getIcon();

    /**
     * @return if has
     */
    public boolean hasContextMenu() {
        return false;
    }

    /**
     * returns the ContextMenu of this View
     *
     * @return the Menu
     */
    public Menu getContextMenu() {
        return null;
    }

    public String getTitleTranslationId() {
        return titleTranslationId;
    }

    public String getTitleExtension() {
        return titleExtension;
    }

    public boolean getEnabled() {
        return true;
    }

    public boolean getIsCheckable() {
        return false;
    }

    public boolean getIsChecked() {
        return false;
    }

}
