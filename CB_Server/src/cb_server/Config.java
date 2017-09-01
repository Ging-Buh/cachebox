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

import CB_Utils.Config_Core;

public class Config extends Config_Core {

	public Config(String workPath) {
		super(workPath);
	}

	public static SettingsClass settings;

	public static void Initialize(String workPath) {
		mWorkPath = workPath;
		settings = new SettingsClass();

	}

	@Override
	protected void acceptChanges() {
		// TODO Auto-generated method stub

	}

}
