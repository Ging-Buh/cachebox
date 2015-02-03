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

import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Tag;
import CB_UI_Base.Math.Stack;
import CB_UI_Base.Math.UiSizes;

/**
 * 
 * @author Longri
 *
 */
public class HTML_Segment_List extends Html_Segment {

    private final List<Html_Segment> listEntrys = new ArrayList<Html_Segment>();
    private final int listBulletNumber;
    private final int tabLeveL;
    private float scaledfontSize;

    public HTML_Segment_List(Stack<Tag> atributeStack, int bulletNumber, int tabLevel, boolean ordert) {
	super(Html_Segment_Typ.List, atributeStack, "");

	this.tabLeveL = tabLevel;
	if (ordert) {
	    this.listBulletNumber = -1;
	} else {
	    this.listBulletNumber = bulletNumber;
	}
	resolveAtributes();
    }

    @Override
    public void resolveAtributes() {
	resolveHAlignment();

	int size = 3;
	for (Tag tag : tags) {
	    if (!tag.getName().toLowerCase().equals("font"))
		continue;
	    List<Element> elements = tag.getAllElements();

	    if (elements.isEmpty())
		elements.add(tag.getElement());

	    for (Element ele : elements) {
		// tag.getElement().
		Attributes attributes = ele.getAttributes();
		for (Attribute attr : attributes) {
		    if (attr.getKey().equals("size")) {

			/*
			    * The following values are acceptable:
			   *
			   * 1, 2, 3, 4, 5, 6, 7
			   * +1, +2, +3, +4, +5, +6
			   * -1, -2, -3, -4, -5, -6
			    */

			String value = attr.getValue();
			if (value.startsWith("+")) {
			    int intSize = Integer.parseInt(value.replace("+", ""));
			    size += intSize;
			} else if (value.startsWith("-")) {
			    int intSize = Integer.parseInt(value.replace("-", ""));
			    size -= intSize;
			} else {
			    size = Integer.parseInt(value);
			}

		    }
		}
	    }
	}
	if (size < 1)
	    size = 1;
	if (size > 7)
	    size = 7;

	this.scaledfontSize = Html_Segment_TextBlock.getFontPx(size) * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR;

    }

    public void addListEntry(Html_Segment entry) {
	listEntrys.add(entry);
    }

    public List<Html_Segment> getSegmentList() {
	return listEntrys;
    }

    public int getTabLevel() {
	return tabLeveL;
    }

    public int getBulletNumber() {
	return listBulletNumber;
    }

    public float getFondSize() {
	return this.scaledfontSize;
    }

}
