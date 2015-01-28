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

import java.util.List;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Tag;
import CB_UI_Base.Math.Stack;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Exceptions.NotImplementedException;
import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;

/**
 * See http://de.selfhtml.org/html/text/trennlinien.htm#gestalten_html
 * 
 * @author Longri
 */
public class Html_Segment_HR extends Html_Segment {

    Color color = Color.BLACK;
    float hrsize = 0;

    public Html_Segment_HR(Stack<Tag> atributeStack) {
	super(Html_Segment_Typ.HR, atributeStack, "--Todo--HR----Todo--HR-");
	resolveAtributes();
    }

    @Override
    public void resolveAtributes() {

	/* TODO
	 * all priviose Attributes are ignored 
	 * use only Tag attribute values 
	 *   > noshade 
	 *   > width="300" 
	 *   > size="3" 
	 *   > align="left"
	 *   > color="#009900"
	 */

	String color = null;
	for (Tag tag : tags) {
	    if (!tag.getName().equals("font"))
		continue;
	    List<Element> elements = tag.getAllElements();
	    if (elements.isEmpty())
		elements.add(tag.getElement());
	    for (Element ele : elements) {
		Attributes attributes = ele.getAttributes();
		for (Attribute attr : attributes) {
		    if (attr.getKey().equals("color")) {
			color = attr.getValue();
		    }
		}
	    }
	}
	if (color != null) {

	    if (color.startsWith("#")) {
		try {
		    this.color = new HSV_Color(color.replace("#", ""));
		} catch (Exception e) {
		    this.color = Color.BLACK;
		    throw new NotImplementedException("HTML Renderer Color <" + color + "> is not implemented");
		}
	    } else {
		try {
		    this.color = HTMLColors.getColor(color);
		    if (this.color == null) {
			this.color = Color.BLACK;
			throw new NotImplementedException("HTML Renderer Color <" + color + "> is not implemented");
		    }
		} catch (Exception e) {
		    this.color = Color.BLACK;
		    throw new NotImplementedException("HTML Renderer Color <" + color + "> is not implemented");
		}
	    }

	}

	// resolve Font Size

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

	this.hrsize = (Html_Segment_TextBlock.getFontPx(size) * UiSizes.that.getScale() * Html_Segment_TextBlock.DEFAULT_FONT_SIZE_FACTOR) / 10;
    }

    public Color getColor() {
	return color;
    }
}
