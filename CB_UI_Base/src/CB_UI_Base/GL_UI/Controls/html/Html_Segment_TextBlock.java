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

import org.slf4j.LoggerFactory;

import CB_UI_Base.Math.Stack;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_Utils.Exceptions.NotImplementedException;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Longri
 */
public class Html_Segment_TextBlock extends Html_Segment {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(Html_Segment_TextBlock.class);
    protected static final float DEFAULT_FONT_SIZE = 11;
    public static final float DEFAULT_FONT_SIZE_FACTOR = 1.2f;

    private Color fontColor = Color.BLACK;
    private float scaledfontSize = 8;
    private GL_FontStyle fontStyle = GL_FontStyle.NORMAL;
    private final GL_FontFamily fontFamily = GL_FontFamily.DEFAULT;
    private H h = H.H0;
    boolean underline = false;
    boolean strikeOut = false;
    CB_List<HyperLinkText> hyperLinkList = new CB_List<HyperLinkText>();

    public Html_Segment_TextBlock(Stack<Tag> atributeStack, String string) {
	super(Html_Segment_Typ.TextBlock, atributeStack, string);
	resolveAtributes();
    }

    @Override
    public void resolveAtributes() {

	resolveHAlignment();

	// resolve Font Color
	String color = null;
	for (Tag tag : tags) {
	    //	    if (!tag.getName().equals("font") && !tag.getName().toLowerCase().equals("style"))
	    //		continue;
	    List<Element> elements = tag.getAllElements();
	    if (elements.isEmpty())
		elements.add(tag.getElement());
	    for (Element ele : elements) {
		Attributes attributes = ele.getAttributes();
		for (Attribute attr : attributes) {
		    if (attr.getKey().equals("color")) {
			color = attr.getValue();
		    }
		    if (attr.getKey().equals("style")) {
			String[] values = attr.getValue().split(";");

			for (String value : values) {
			    String[] paar = value.split(":");
			    if (paar[0].equals("color")) {
				color = paar[1];
			    }
			}
		    }
		}
	    }
	}
	if (color != null) {

	    if (color.startsWith("#")) {
		try {
		    this.fontColor = new HSV_Color(color.replace("#", ""));
		} catch (Exception e) {
		    this.fontColor = Color.BLACK;
		    throw new NotImplementedException("HTML Renderer Color <" + color + "> is not implemented");
		}
	    } else {
		try {
		    this.fontColor = HTMLColors.getColor(color);
		    if (this.fontColor == null) {
			this.fontColor = Color.BLACK;
			throw new NotImplementedException("HTML Renderer Color <" + color + "> is not implemented");
		    }
		} catch (Exception e) {
		    this.fontColor = Color.BLACK;
		    throw new NotImplementedException("HTML Renderer Color <" + color + "> is not implemented");
		}
	    }

	}

	// resolve Font Size

	int size = 3;
	for (Tag tag : tags) {
	    //	    if (!tag.getName().toLowerCase().equals("font") && !tag.getName().toLowerCase().equals("style"))
	    //		continue;
	    List<Element> elements = tag.getAllElements();

	    if (elements.isEmpty())
		elements.add(tag.getElement());

	    for (Element ele : elements) {
		// tag.getElement().
		Attributes attributes = ele.getAttributes();
		for (Attribute attr : attributes) {
		    if (attr.getKey().equals("size")) {

			String value = attr.getValue();

			size = getFontSizeFromString(size, value);
		    }
		    if (attr.getKey().equals("style")) {
			String[] values = attr.getValue().split(";");

			for (String value : values) {
			    String[] paar = value.split(":");
			    if (paar[0].equals("font-size")) {
				size = getFontSizeFromString(size, paar[1]);
			    }
			    if (paar[0].equals("text-decoration")) {
				if (paar[1].equals("underline"))
				    underline = true;
			    }
			}
		    }
		}
	    }
	}
	if (size < 1)
	    size = 1;
	if (size > 7)
	    size = 7;

	this.scaledfontSize = getFontPx(size) * UiSizes.that.getScale() * DEFAULT_FONT_SIZE_FACTOR;

	//resolve underline
	for (Tag tag : tags) {
	    if (tag.getName().toLowerCase().equals("u")) {
		underline = true;
	    }
	    if (tag.getName().toLowerCase().startsWith("h")) {
		String value = tag.getName().substring(1);
		try {
		    int intValue = Integer.parseInt(value);
		    switch (intValue) {
		    case 1:
			h = H.H1;
			break;
		    case 2:
			h = H.H2;
			break;
		    case 3:
			h = H.H3;
			break;
		    case 4:
			h = H.H4;
			break;
		    case 5:
			h = H.H5;
			break;
		    case 6:
			h = H.H6;
			break;
		    default:
			h = H.H0;
			break;
		    }
		} catch (NumberFormatException e) {

		}
	    }

	}

	if (h != H.H0) {

	    /* h1: 2em
	       h2: 1.5em
	       h3: 1.17em
	       h4: 1em
	       h5: 0.83em
	       h6: 0.75em 
	    */

	    switch (h) {

	    case H1:
		this.scaledfontSize *= 2;
		break;
	    case H2:
		this.scaledfontSize *= 1.5f;
		break;
	    case H3:
		this.scaledfontSize *= 1.17f;
		break;
	    case H4:
		break;
	    case H5:
		this.scaledfontSize *= 0.83f;
		break;
	    case H6:
		this.scaledfontSize *= 0.75f;
		break;

	    case H0:
		break;
	    default:
		break;

	    }
	}

	// resolve Font Style

	boolean BOOLD = false;
	boolean ITALIC = false;
	for (Tag tag : tags) {
	    if (tag.getName().toLowerCase().equals("strong") || tag.getName().toLowerCase().equals("b")) {
		BOOLD = true;
	    } else if (tag.getName().toLowerCase().equals("i")) {
		ITALIC = true;
	    } else if (tag.getName().toLowerCase().equals("strike")) {
		strikeOut = true;
	    }

	}

	if (h != H.H0)
	    BOOLD = true;

	if (!BOOLD && !ITALIC)
	    this.fontStyle = GL_FontStyle.NORMAL;
	if (BOOLD && !ITALIC)
	    this.fontStyle = GL_FontStyle.BOLD;
	if (BOOLD && ITALIC)
	    this.fontStyle = GL_FontStyle.BOLD_ITALIC;
	if (!BOOLD && ITALIC)
	    this.fontStyle = GL_FontStyle.ITALIC;

	//	System.out.print(true);
    }

    private int getFontSizeFromString(int size, String value) {
	/*
	    * The following values are acceptable:
	   *
	   * 1, 2, 3, 4, 5, 6, 7
	   * +1, +2, +3, +4, +5, +6
	   * -1, -2, -3, -4, -5, -6
	    */

	/* Or
	  	xx-small = 1.
		x-small = 2.
		small = 3.
		medium = 4.
		large = 5.
		x-large = 6.
		xx-large = 7.
	 */

	if (value.equals("xx-small"))
	    return 1;
	if (value.equals("x-small"))
	    return 2;
	if (value.equals("small"))
	    return 3;
	if (value.equals("medium"))
	    return 4;
	if (value.equals("large"))
	    return 5;
	if (value.equals("x-large"))
	    return 6;
	if (value.equals("xx-large"))
	    return 7;

	if (value.startsWith("+")) {
	    int intSize = Integer.parseInt(value.replace("+", ""));
	    size += intSize;
	} else if (value.startsWith("-")) {
	    int intSize = Integer.parseInt(value.replace("-", ""));
	    size -= intSize;
	} else {
	    try {
		size = Integer.parseInt(value);
	    } catch (NumberFormatException e) {
		log.error("wrong size value =>" + value);
	    }
	}
	return size;
    }

    static float getFontPx(int value) {
	switch (value) {
	case 1:
	    return 7;
	case 2:
	    return 9;
	case 3:
	    return 12;
	case 4:
	    return 14;
	case 5:
	    return 18;
	case 6:
	    return 24;
	case 7:
	    return 34;
	default:
	    return 14;
	}
    }

    public float getFontSize() {
	if (this.attDirty)
	    resolveAtributes();
	return this.scaledfontSize;
    }

    public Color getFontColor() {
	if (this.attDirty)
	    resolveAtributes();
	return fontColor;
    }

    public GL_FontFamily getFontFamily() {
	if (this.attDirty)
	    resolveAtributes();
	return fontFamily;
    }

    public GL_FontStyle getFontStyle() {
	if (this.attDirty)
	    resolveAtributes();
	return fontStyle;
    }

    public void add(CB_List<HyperLinkText> hyperLinkList) {
	this.hyperLinkList.addAll(hyperLinkList);
    }

    @Override
    public void dispose() {
	super.dispose();
	if (hyperLinkList != null) {
	    for (HyperLinkText hyperLink : hyperLinkList) {
		if (hyperLink != null) {
		    hyperLink.dispose();
		    hyperLink = null;
		}
	    }
	    hyperLinkList.clear();
	    hyperLinkList = null;
	}
    }

}