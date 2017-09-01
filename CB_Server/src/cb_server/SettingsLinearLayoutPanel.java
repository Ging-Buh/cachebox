/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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

package cb_server;

import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Linear Layout Panel for Category's at Settings Window
 * @author Longri
 *
 */
public class SettingsLinearLayoutPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public SettingsLinearLayoutPanel() {

	}

	public void setContent(VerticalLayout layout, float height) {
		this.setHeight(height, Unit.PIXELS);
		this.setContent(layout);
	}

}
