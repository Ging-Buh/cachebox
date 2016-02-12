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
package CB_UI_Base.GL_UI.Controls.html;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_Utils.Lists.CB_List;

/**
 * 
 * @author Longri
 *
 */
public interface ListLayout {

	public abstract void layout(CB_List<CB_View_Base> segmentViewList);

}