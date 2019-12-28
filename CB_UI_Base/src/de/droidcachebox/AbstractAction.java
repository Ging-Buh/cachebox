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

public abstract class AbstractAction {

    protected String titleTranlationId;
    protected String titleExtension;

    public AbstractAction(String titleTranlationId) {
        this.titleTranlationId = titleTranlationId;
        titleExtension = "";
    }

    public AbstractAction(String titleTranlationId, String titleExtension) {
        this.titleTranlationId = titleTranlationId;
        this.titleExtension = titleExtension;
    }

    public abstract void execute();

    public abstract Sprite getIcon();

    public String getTitleTranlationId() {
        return titleTranlationId;
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
