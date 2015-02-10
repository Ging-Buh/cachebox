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

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.ImageLoader;
import CB_UI_Base.GL_UI.Controls.LinkLabel;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.graphics.GL_Fonts;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Trace;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

/**
 * @author Longri
 */
public class HtmlView extends ScrollBox implements ListLayout {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(HtmlView.class);
    static int margin;
    private Box contentBox;
    private List<Html_Segment> segmentList;
    private CB_List<CB_View_Base> segmentViewList;

    public HtmlView(CB_RectF rec) {
	super(rec);
	margin = UI_Size_Base.that.getMargin() / 2;
	this.setClickable(true);
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

	    addViewsToBox(segmentList, segmentViewList, this.getWidth(), this);
	} catch (Exception e) {
	    any = e;
	}
	layout(segmentViewList);
	if (any != null)
	    throw any;
    }

    public static void addViewsToBox(List<Html_Segment> segmentList, CB_List<CB_View_Base> segmentViewList, float innerWidth, final ListLayout relayout) {
	for (int i = 0, n = segmentList.size(); i < n; i++) {
	    Html_Segment seg = segmentList.get(i);

	    switch (seg.segmentTyp) {
	    case HR:
		addHR(segmentViewList, (Html_Segment_HR) seg, innerWidth);
		break;
	    case Image:
		addImage(segmentViewList, seg, relayout, innerWidth);
		break;
	    case TextBlock:
		addTextBlog(segmentViewList, (Html_Segment_TextBlock) seg, innerWidth);
		break;
	    case List:
		addListBlog(segmentViewList, (HTML_Segment_List) seg, innerWidth);
		break;
	    case Input:
		addInput(segmentViewList, (Html_Segment_Input) seg, relayout, innerWidth);
		break;
	    default:
		break;

	    }
	}
    }

    private static float addInput(final CB_List<CB_View_Base> segmentViewList, final Html_Segment_Input seg, final ListLayout relayout, float innerWidth) {

	if (!seg.value.startsWith("att_"))
	    return 0;

	ImageLoader img = new ImageLoader();
	img.setImage(seg.imagePath);

	ImageButton imgBtn = new ImageButton(img) {

	    @Override
	    public void onResized(CB_RectF rec) {
		super.onResized(rec);
		this.setWidth(this.getHeight());
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			relayout.layout(segmentViewList);
		    }
		});
	    }
	};
	imgBtn.setWidth(imgBtn.getHeight());

	imgBtn.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		GL_MsgBox.Show(Translation.Get(seg.value));
		return true;
	    }
	});

	segmentViewList.add(imgBtn);
	return imgBtn.getHeight();
    }

    private static float addListBlog(CB_List<CB_View_Base> segmentViewList, HTML_Segment_List seg, float innerWidth) {
	Html_ListView ListcontentBox = new Html_ListView(innerWidth, seg);
	segmentViewList.add(ListcontentBox);
	return ListcontentBox.getHeight();
    }

    private int testcount = 0;

    @Override
    public void layout(CB_List<CB_View_Base> segmentViewList) {
	testcount = 0;

	this.removeChilds();

	log.debug("HTML View Layout");
	log.debug("   " + Trace.getCallerName(0));
	log.debug("   " + Trace.getCallerName(1));
	log.debug("   " + Trace.getCallerName(2));

	float innerWidth = this.getInnerWidth() - (margin * 2);
	int maxAttributeButtonsPerLine = (int) (innerWidth / (UI_Size_Base.that.getButtonHeight()));
	float contentHeight = 0;
	int attLines = 1;
	for (int i = 0, n = segmentViewList.size(); i < n; i++) {

	    CB_View_Base view = segmentViewList.get(i);

	    if (view instanceof ImageButton) {
		if (testcount++ > maxAttributeButtonsPerLine) {
		    attLines++;
		    testcount = 0;
		}
	    } else {
		contentHeight += view.getHeight();
	    }
	}

	contentHeight += (attLines * UI_Size_Base.that.getButtonHeight());

	contentBox = new Box(this, "topContent");
	contentBox.setWidth(innerWidth);
	contentBox.setClickable(true);
	contentBox.setHeight(contentHeight);
	contentBox.setZeroPos();
	contentBox.setX(margin);

	contentBox.setMargins(0, 0);
	contentBox.initRow();
	testcount = 0;
	for (int i = 0, n = segmentViewList.size(); i < n; i++) {

	    CB_View_Base view = segmentViewList.get(i);

	    if (view instanceof Image) {
		contentBox.addLast(segmentViewList.get(i));
	    } else if (view instanceof ImageButton) {
		if (testcount++ > maxAttributeButtonsPerLine) {
		    contentBox.addLast(segmentViewList.get(i), FIXED);
		    testcount = 0;
		} else {
		    contentBox.addNext(segmentViewList.get(i), FIXED);
		}

	    } else {
		contentBox.addLast(segmentViewList.get(i), FIXED);
	    }

	}

	for (int i = 0, n = contentBox.getchilds().size(); i < n; i++) {

	    GL_View_Base child = contentBox.getChild(i);
	    if (child instanceof Html_ListView) {
		// move tab on x
		float tabX = contentBox.getWidth() - ((Html_ListView) child).getContentWidth();
		child.setX(tabX);
		//		child.setX(0);
	    }

	}

	this.addChild(contentBox);
	this.setVirtualHeight(contentHeight);
	this.scrollTo(0);
    }

    private static float addHR(CB_List<CB_View_Base> segmentViewList, Html_Segment_HR seg, float innerWidth) {

	HrView hrView = new HrView(0, 0, innerWidth, seg.hrsize, "hr");

	hrView.setBackground(new ColorDrawable(seg.getColor()));

	segmentViewList.add(hrView);
	return hrView.getHeight();
    }

    private static float lastRelayoutStateTime = 0;

    private static float addImage(final CB_List<CB_View_Base> segmentViewList, Html_Segment seg, final ListLayout relayout, float innerWidth) {
	Image img = new Image(0, 0, innerWidth, 50, "Html-Image", true) {

	    @Override
	    public void onResized(CB_RectF rec) {
		super.onResized(rec);
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			if (lastRelayoutStateTime == GL.that.getStateTime())
			    return;
			lastRelayoutStateTime = GL.that.getStateTime();
			relayout.layout(segmentViewList);
		    }
		});
	    }
	};
	img.setHAlignment(seg.hAlignment);
	img.setImageURL(seg.formatetText);

	img.forceImageLoad();

	segmentViewList.add(img);
	return img.getHeight();
    }

    private static float addTextBlog(CB_List<CB_View_Base> segmentViewList, Html_Segment_TextBlock seg, float innerWidth) {
	BitmapFont font = GL_Fonts.get(seg.getFontFamily(), seg.getFontStyle(), seg.getFontSize());
	TextBounds bounds = font.getWrappedBounds(seg.formatetText, innerWidth - (margin * 2));
	float segHeight = bounds.height + (margin * 2);

	parseHyperLinks(seg, "http://");
	parseHyperLinks(seg, "www.");

	LinkLabel lbl = new LinkLabel(0, 0, innerWidth - (margin * 2), segHeight, "DescLabel");

	if (!seg.hyperLinkList.isEmpty()) {
	    lbl.setMarkupEnabled(true);
	}

	lbl.setTextColor(seg.getFontColor());
	lbl.setFont(font).setHAlignment(seg.hAlignment);

	if (!seg.hyperLinkList.isEmpty()) {
	    lbl.addHyperlinks(seg.hyperLinkList);
	}

	lbl.setWrappedText(seg.formatetText);
	lbl.setUnderline(seg.underline);
	lbl.setStrikeout(seg.strikeOut);
	segmentViewList.add(lbl);
	return segHeight;
    }

    private static void parseHyperLinks(Html_Segment_TextBlock seg, String hyperLinkTag) {
	try {
	    if (seg.formatetText.contains(hyperLinkTag)) {
		// add to hyperLings

		int start = seg.formatetText.indexOf(hyperLinkTag);

		int end1 = seg.formatetText.indexOf(" ", start);
		int end2 = seg.formatetText.indexOf("\r", start);
		int end3 = seg.formatetText.indexOf("\n", start);

		if (end1 < 0)
		    end1 = Integer.MAX_VALUE;
		if (end2 < 0)
		    end2 = Integer.MAX_VALUE;
		if (end3 < 0)
		    end3 = Integer.MAX_VALUE;

		int end = Math.min(Math.min(end1, end2), end3);

		if (end == Integer.MAX_VALUE) {
		    end = seg.formatetText.length();
		}

		String link = seg.formatetText.substring(start, end);

		if (link.endsWith(")") || link.endsWith("]") || link.endsWith("}")) {
		    link = seg.formatetText.substring(start, end - 1);
		}

		HyperLinkText hyper = new HyperLinkText(link.trim(), link.trim());
		seg.hyperLinkList.add(hyper);
	    }
	} catch (Exception e) {
	    log.error("parseHyperLinks", e);
	}
    }

    @Override
    public void dispose() {
	super.dispose();
    }

}
