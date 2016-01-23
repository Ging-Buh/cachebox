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
package CB_UI_Base.GL_UI.Main.Actions;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Action.class);

    protected String name;
    protected int id;
    protected String nameExtention = "";

    /**
     * Constructor
     * 
     * @param name
     *            = Translation ID
     * @param id
     *            = Action ID ( AID_xxxx )
     */
    public CB_Action(String name, int id) {
	//super();
	this.name = name;
	this.id = id;
    }

    public CB_Action(String name, String nameExtention, int id) {
	//super();
	this.name = name;
	this.id = id;
	this.nameExtention = nameExtention;
    }

    public void CallExecute() {
	log.debug("ACTION => " + name + " execute");
	Execute();
    }

    protected void Execute() {
	return;
    }

    public String getName() {
	return name;
    }

    public String getNameExtention() {
	return nameExtention;
    }

    public int getId() {
	return id;
    }

    /**
     * hiermit kann der Menüpunkt enabled oder disabled werden
     * 
     * @return
     */
    public boolean getEnabled() {
	return true;
    }

    public Sprite getIcon() {
	return null;
    }

    public boolean getIsCheckable() {
	return false;
    }

    public boolean getIsChecked() {
	return false;
    }

}
