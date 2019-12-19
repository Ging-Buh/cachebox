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
package de.droidcachebox.main;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class AbstractAction {

    protected String titleTranlationId;
    protected int id;
    protected String titleExtension;

    public AbstractAction(String titleTranlationId, int id) {
        this.titleTranlationId = titleTranlationId;
        this.id = id;
        titleExtension = "";
    }

    public AbstractAction(String titleTranlationId, String titleExtension, int id) {
        this.titleTranlationId = titleTranlationId;
        this.id = id;
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

    public int getId() {
        return id;
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
