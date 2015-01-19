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

import net.htmlparser.jericho.Source;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.graphics.GL_Fonts;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

/**
 * @author Longri
 */
public class HtmlView extends ScrollBox
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(HtmlView.class);

	private Box contentBox;

	public HtmlView(CB_RectF rec)
	{
		super(rec);
	}

	public void showHtml(final String HTMLSOURCE)
	{
		this.removeChilds();
		int margin = UI_Size_Base.that.getMargin();
		Source source = new CB_FormatedHtmlSource(HTMLSOURCE);

		CB_Html_Renderer renderer = new CB_Html_Renderer(source);
		List<HtmlSegment> segmentList = renderer.getElementList();

		float innerWidth = this.getInnerWidth() - (margin * 2);

		CB_List<CB_View_Base> segmentViewList = new CB_List<CB_View_Base>();
		float contentHeight = 0;
		for (int i = 0, n = segmentList.size(); i < n; i++)
		{
			float segHeight = 0;
			HtmlSegment seg = segmentList.get(i);
			if (seg.isImage)
			{

			}
			else
			{
				BitmapFont font = GL_Fonts.get(seg.fontFamily, seg.fontStyle, seg.fontSize);

				TextBounds bounds = font.getWrappedBounds(seg.formatetText, innerWidth);
				segHeight = bounds.height + margin;
				Label lbl = new Label(0, 0, this.getWidth(), segHeight, "DescLabel");
				lbl.setTextColor(seg.fontColor);
				lbl.setFont(font).setHAlignment(seg.hAlignment);
				lbl.setWrappedText(seg.formatetText);
				segmentViewList.add(lbl);
				contentHeight += segHeight;

			}

		}

		contentBox = new Box(this, "topContent");
		contentBox.setWidth(innerWidth);

		contentBox.setHeight(contentHeight);
		contentBox.setZeroPos();
		contentBox.setX(margin);

		contentBox.setMargins(margin, margin);
		contentBox.initRow();

		for (int i = 0, n = segmentViewList.size(); i < n; i++)
		{
			contentBox.addLast(segmentViewList.get(i));
		}

		this.addChild(contentBox);
		this.setVirtualHeight(contentHeight);
		this.scrollTo(0);

	}
}
