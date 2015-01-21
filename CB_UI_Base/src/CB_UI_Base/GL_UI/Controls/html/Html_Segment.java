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
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.Math.Stack;

/**
 * @author Longri
 */
public abstract class Html_Segment {

    public static final String br = System.getProperty("line.separator");

    protected List<StartTag> tags = new ArrayList<StartTag>();
    protected String formatetText;
    protected boolean attDirty = false;
    public final Html_Segment_Typ segmentTyp;
    protected HAlignment hAlignment = HAlignment.LEFT;

    public Html_Segment(Html_Segment_Typ segmentTyp, Stack<Tag> atributeStack, String string) {
	super();
	this.segmentTyp = segmentTyp;
	this.formatetText = string;

	for (int i = atributeStack.size() - 1; i >= 0; i--) {
	    this.tags.add((StartTag) atributeStack.get(i));
	}
	resolveAtributes();
    }

    public abstract void resolveAtributes();

    protected void addStartTags(List<StartTag> allStartTags) {
	attDirty = true;
	int idx = 0;
	for (StartTag tag : allStartTags) {
	    if (tag.getName().equals("br"))
		continue;
	    this.tags.add(idx++, tag);
	}
    }

    protected void resolveHAligment() {
	// resolve HAlignment
	for (Tag tag : tags) {
	    List<Element> elements = tag.getAllElements();
	    if (elements.isEmpty())
		elements.add(tag.getElement());
	    for (Element ele : elements) {
		Attributes attributes = ele.getAttributes();
		for (Attribute attr : attributes) {
		    if (attr.getKey().equals("align")) {
			String val = attr.getValue();
			if (val.contains("center"))
			    hAlignment = HAlignment.CENTER;
			else if (val.contains("left"))
			    hAlignment = HAlignment.LEFT;
			else if (val.contains("right"))
			    hAlignment = HAlignment.RIGHT;
			else
			    hAlignment = HAlignment.LEFT;
		    }
		}
	    }
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	if (tags != null && !tags.isEmpty()) {
	    sb.append("[Attributes: ");
	    for (Tag tag : tags) {
		sb.append(tag);
	    }
	    sb.append("]" + br);
	}
	sb.append(segmentTyp.toString() + ": " + formatetText);
	return sb.toString();
    }

}