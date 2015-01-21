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
 * See http://de.selfhtml.org/html/text/trennlinien.htm#gestalten_html
 * 
 * @author Longri
 */
public class Html_Segment_HR extends Html_Segment {

    public Html_Segment_HR(Stack<Tag> atributeStack) {
	super(Html_Segment_Typ.HR, atributeStack, "--Todo--HR----Todo--HR-");
    }

    @Override
    public void resolveAtributes() {

	/*
	 * all priviose Attributes are ignored 
	 * use only Tag attribute values 
	 *   > noshade 
	 *   > width="300" 
	 *   > size="3" 
	 *   > align="left"
	 *   > color="#009900"
	 */
    }

}
