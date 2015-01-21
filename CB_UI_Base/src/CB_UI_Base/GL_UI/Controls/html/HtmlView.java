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

import net.htmlparser.jericho.Source;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_Fonts;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

/**
 * @author Longri
 */
public class HtmlView extends ScrollBox {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(HtmlView.class);
    int margin;
    private Box contentBox;
    private List<Html_Segment> segmentList;
    private CB_List<CB_View_Base> segmentViewList;

    public HtmlView(CB_RectF rec) {
	super(rec);
	margin = UI_Size_Base.that.getMargin();
    }

    public void showHtml(final String HTMLSOURCE) throws Exception {

	if (segmentList != null) {
	    segmentList.clear();
	} else {
	    segmentList = new ArrayList<Html_Segment>();
	}
	segmentViewList = new CB_List<CB_View_Base>();
	Exception any = null;

	try {
	    Source source = new CB_FormatedHtmlSource(HTMLSOURCE);
	    segmentList = new CB_Html_Renderer(source).getElementList();

	    for (int i = 0, n = segmentList.size(); i < n; i++) {
		Html_Segment seg = segmentList.get(i);

		switch (seg.segmentTyp) {
		case HR:
		    addHR(segmentViewList, (Html_Segment_HR) seg);
		    break;
		case Image:
		    addImage(segmentViewList, seg);
		    break;
		case TextBlock:
		    addTextBlog(segmentViewList, (Html_Segment_TextBlock) seg);
		    break;
		default:
		    break;

		}
	    }
	} catch (Exception e) {
	    any = e;
	}
	layout(segmentViewList);
	if (any != null)
	    throw any;
    }

    private void layout(CB_List<CB_View_Base> segmentViewList) {

	this.removeChilds();

	float innerWidth = this.getInnerWidth() - (margin * 2);

	float contentHeight = 0;
	for (int i = 0, n = segmentViewList.size(); i < n; i++) {
	    contentHeight += segmentViewList.get(i).getHeight();
	}

	contentBox = new Box(this, "topContent");
	contentBox.setWidth(innerWidth);

	contentBox.setHeight(contentHeight);
	contentBox.setZeroPos();
	contentBox.setX(margin);

	contentBox.setMargins(margin, margin);
	contentBox.initRow();

	for (int i = 0, n = segmentViewList.size(); i < n; i++) {
	    contentBox.addLast(segmentViewList.get(i));
	}

	this.addChild(contentBox);
	this.setVirtualHeight(contentHeight);
	this.scrollTo(0);
    }

    private float addHR(CB_List<CB_View_Base> segmentViewList, Html_Segment_HR seg) {
	// TODO Auto-generated method stub
	return 0;
    }

    private float addImage(final CB_List<CB_View_Base> segmentViewList, Html_Segment seg) {
	Image img = new Image(0, 0, 50, 50 * UiSizes.that.getScale(), "Html-Image", true) {

	    @Override
	    public void onResized(CB_RectF rec) {
		super.onResized(rec);
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			layout(segmentViewList);
		    }
		});
	    }
	};
	img.setImageURL(seg.formatetText);

	segmentViewList.add(img);
	return img.getHeight();
    }

    private float addTextBlog(CB_List<CB_View_Base> segmentViewList, Html_Segment_TextBlock seg) {
	BitmapFont font = GL_Fonts.get(seg.getFontFamily(), seg.getFontStyle(), seg.getFontSize());
	TextBounds bounds = font.getWrappedBounds(seg.formatetText, innerWidth);
	float segHeight = bounds.height + margin;
	Label lbl = new Label(0, 0, this.getWidth(), segHeight, "DescLabel");
	lbl.setTextColor(seg.getFontColor());
	lbl.setFont(font).setHAlignment(seg.hAlignment);
	lbl.setWrappedText(seg.formatetText);
	segmentViewList.add(lbl);
	return segHeight;
    }
}
