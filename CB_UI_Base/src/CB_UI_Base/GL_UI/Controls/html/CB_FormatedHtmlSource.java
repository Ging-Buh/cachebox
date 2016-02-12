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

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;

/**
 * CB need formated html source for parsing Attributes! This Class extends the net.htmlparser.jericho.Source.Class to format the html source
 * before giving to constructor!
 * 
 * @author Longri
 */
public class CB_FormatedHtmlSource extends net.htmlparser.jericho.Source {

	CB_FormatedHtmlSource(String sourceText) {
		super(getFormattedString(sourceText));
	}

	private static String getFormattedString(String sourceText) {
		Source source = new Source(sourceText);
		return new SourceFormatter(source).setTidyTags(true).setCollapseWhiteSpace(true).setIndentAllElements(true).toString();
	}

}
