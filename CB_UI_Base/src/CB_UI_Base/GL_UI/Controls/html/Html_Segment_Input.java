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

import net.htmlparser.jericho.Tag;
import CB_UI_Base.Math.Stack;

/**
 * 
 * @author Longri
 *
 */
public class Html_Segment_Input extends Html_Segment {

	final String imagePath;
	final String value;

	public Html_Segment_Input(Html_Segment_Typ segmentTyp, Stack<Tag> atributeStack, String ImagePath, String Value) {
		super(segmentTyp, atributeStack, "");
		this.imagePath = ImagePath;
		this.value = Value;
	}

	@Override
	public void resolveAtributes() {
		// nothing to do
	}

	public String getValue() {
		return value;
	}

	@Override
	public void dispose() {
		super.dispose();
		// nothing to do	
	}

}
