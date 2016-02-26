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

import CB_UI_Base.Math.Stack;
import CB_UI_Base.Math.UiSizes;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Tag;

/**
 * 
 * @author Longri
 *
 */
public class HTML_Segment_Table extends Html_Segment {

    private final List<Html_Segment> listEntrys = new ArrayList<Html_Segment>();
    private float scaledfontSize;
    final ArrayList<ArrayList<ArrayList<Html_Segment>>> tableSegments;
    private final float borderSize;
    private final float cellpaddingSize;
    private final float cellspacingSize;

    public HTML_Segment_Table(Stack<Tag> atributeStack, ArrayList<ArrayList<ArrayList<Html_Segment>>> tableSegments,
	    float borderSize, float cellpaddingSize, float cellspacingSize) {
	super(Html_Segment_Typ.Table, atributeStack, "");
	this.tableSegments = tableSegments;
	resolveAtributes();

	this.borderSize = borderSize;
	this.cellpaddingSize = cellpaddingSize;
	this.cellspacingSize = cellspacingSize;
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
			 * 1, 2, 3, 4, 5, 6, 7 +1, +2, +3, +4, +5, +6 -1, -2,
			 * -3, -4, -5, -6
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

	this.scaledfontSize = Html_Segment_TextBlock.getFontPx(size) * UiSizes.that.getScale()
		* Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR;

    }

    public float getFondSize() {
	return this.scaledfontSize;
    }

    @Override
    public void dispose() {
	super.dispose();
	if (listEntrys != null) {
	    for (Html_Segment entry : listEntrys) {
		if (entry != null) {
		    entry.dispose();
		    entry = null;
		}
	    }
	    listEntrys.clear();
	}
    }

    public float getBorderSize() {
	return borderSize;
    }

    public float getCellspacing() {
	return cellspacingSize;
    }

}
