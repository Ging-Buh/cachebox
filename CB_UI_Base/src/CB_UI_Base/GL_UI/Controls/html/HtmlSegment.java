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
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Longri
 */
public class HtmlSegment
{
	public static final String br = System.getProperty("line.separator");
	private static final float DEFAULT_FONT_SIZE = 14;
	private static final float DEFAULT_FONT_SIZE_FACTOR = 1.3f;

	List<StartTag> tags = new ArrayList<StartTag>();
	String formatetText;

	boolean attDirty = false;
	int begin;
	int end;
	boolean isImage = false;

	// Possible attributes
	HAlignment hAlignment = HAlignment.LEFT;
	Color fontColor = Color.BLACK;
	float fontSize = DEFAULT_FONT_SIZE;
	GL_FontStyle fontStyle = GL_FontStyle.NORMAL;
	GL_FontFamily fontFamily = GL_FontFamily.DEFAULT;

	public HtmlSegment()
	{
	}

	public HtmlSegment(Stack<Tag> atributeStack, String string)
	{
		this.formatetText = string;

		for (int i = atributeStack.size() - 1; i >= 0; i--)
		{
			tags.add((StartTag) atributeStack.get(i));
		}
		resolveAtributes();
	}

	public HtmlSegment setIsImage(boolean value)
	{
		isImage = value;
		return this;
	}

	public void addStartTags(List<StartTag> allStartTags)
	{
		attDirty = true;
		int idx = 0;
		for (StartTag tag : allStartTags)
		{
			if (tag.getName().equals("br")) continue;
			tags.add(idx++, tag);
		}
	}

	public void resolveAtributes()
	{
		// resolve HAlignment
		for (Tag tag : tags)
		{
			List<Element> elements = tag.getAllElements();
			if (elements.isEmpty()) elements.add(tag.getElement());
			for (Element ele : elements)
			{
				Attributes attributes = ele.getAttributes();
				for (Attribute attr : attributes)
				{
					if (attr.getKey().equals("align"))
					{
						String val = attr.getValue();
						if (val.contains("center")) hAlignment = HAlignment.CENTER;
						else if (val.contains("left")) hAlignment = HAlignment.LEFT;
						else if (val.contains("right")) hAlignment = HAlignment.RIGHT;
						else
							hAlignment = HAlignment.LEFT;
					}
				}
			}
		}

		// resolve Font Color
		String color = null;
		for (Tag tag : tags)
		{
			if (!tag.getName().equals("font")) continue;
			List<Element> elements = tag.getAllElements();
			if (elements.isEmpty()) elements.add(tag.getElement());
			for (Element ele : elements)
			{
				Attributes attributes = ele.getAttributes();
				for (Attribute attr : attributes)
				{
					if (attr.getKey().equals("color"))
					{
						color = attr.getValue();
					}
				}
			}
		}
		if (color != null)
		{
			String hex = (color.startsWith("#")) ? color.replace("#", "") : color;
			this.fontColor = new HSV_Color(hex);
		}

		// resolve Font Size
		String size = null;
		for (Tag tag : tags)
		{
			if (!tag.getName().equals("font")) continue;
			List<Element> elements = tag.getAllElements();

			if (elements.isEmpty()) elements.add(tag.getElement());

			for (Element ele : elements)
			{
				// tag.getElement().
				Attributes attributes = ele.getAttributes();
				for (Attribute attr : attributes)
				{
					if (attr.getKey().equals("size"))
					{
						size = attr.getValue();
					}
				}
			}
		}
		if (size != null)
		{
			int intSize = Integer.parseInt(size);
			this.fontSize = getFontPx(intSize) * UiSizes.that.getScale() * DEFAULT_FONT_SIZE_FACTOR;
		}
		else
		{
			this.fontSize = DEFAULT_FONT_SIZE * UiSizes.that.getScale() * DEFAULT_FONT_SIZE_FACTOR;
		}

		// resolve Font Style
		for (Tag tag : tags)
		{
			if (!tag.getName().equals("strong")) continue;
			this.fontStyle = GL_FontStyle.BOLD;
		}

	}

	private static float getFontPx(int value)
	{
		switch (value)
		{
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

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		if (tags != null && !tags.isEmpty())
		{
			sb.append("[Attributes: ");
			for (Tag tag : tags)
			{
				sb.append(tag);
			}
			sb.append("]" + br);
		}
		sb.append(isImage ? "IMAGE: " + formatetText : formatetText);
		return sb.toString();
	}
}