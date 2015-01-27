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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.Controls.html.elementhandler.H_ElementHandler;
import CB_Utils.Exceptions.NotImplementedException;

/**
 * @author Longri
 */
public class CB_Html_Renderer extends Renderer {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Html_Renderer.class);

    public static Map<String, ElementHandler> ELEMENT_HANDLERS = new HashMap<String, ElementHandler>();
    static {

	boolean IMPLEMENTED = true;
	boolean NOTIMPLEMENTED = false;
	ELEMENT_HANDLERS.put(HTMLElementName.A, IMPLEMENTED ? A_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.ADDRESS, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.APPLET, NOTIMPLEMENTED ? AlternateTextElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.B, NOTIMPLEMENTED ? FontStyleElementHandler.INSTANCE_B : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.BLOCKQUOTE, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1_INDENT : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.BR, IMPLEMENTED ? BR_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.BUTTON, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.CAPTION, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.CENTER, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.CODE, NOTIMPLEMENTED ? FontStyleElementHandler.INSTANCE_CODE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.DD, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0_INDENT : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.DIR, NOTIMPLEMENTED ? ListElementHandler.INSTANCE_UL : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.DIV, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.DT, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.EM, NOTIMPLEMENTED ? FontStyleElementHandler.INSTANCE_I : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.FIELDSET, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.FORM, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.H1, IMPLEMENTED ? H_ElementHandler.INSTANCE_H1 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.H2, IMPLEMENTED ? H_ElementHandler.INSTANCE_H2 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.H3, IMPLEMENTED ? H_ElementHandler.INSTANCE_H3 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.H4, IMPLEMENTED ? H_ElementHandler.INSTANCE_H4 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.H5, IMPLEMENTED ? H_ElementHandler.INSTANCE_H5 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.H6, IMPLEMENTED ? H_ElementHandler.INSTANCE_H6 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.HEAD, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.HR, IMPLEMENTED ? HR_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.I, NOTIMPLEMENTED ? FontStyleElementHandler.INSTANCE_I : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.IMG, IMPLEMENTED ? ImagelementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.INPUT, NOTIMPLEMENTED ? AlternateTextElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.LEGEND, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.LI, NOTIMPLEMENTED ? LI_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.MENU, NOTIMPLEMENTED ? ListElementHandler.INSTANCE_UL : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.MAP, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.NOFRAMES, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.NOSCRIPT, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.OL, NOTIMPLEMENTED ? ListElementHandler.INSTANCE_OL : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.P, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.PRE, NOTIMPLEMENTED ? PRE_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.SCRIPT, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.SELECT, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.STRONG, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_B : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.STYLE, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.TEXTAREA, NOTIMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.TD, NOTIMPLEMENTED ? TD_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.TH, NOTIMPLEMENTED ? TD_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.TR, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.U, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_U : Not_implemented_ElementHandler.INSTANCE);
	ELEMENT_HANDLERS.put(HTMLElementName.UL, NOTIMPLEMENTED ? ListElementHandler.INSTANCE_UL : Not_implemented_ElementHandler.INSTANCE);
    }

    public CB_Html_Renderer(Segment segment) {
	super(segment);
    }

    public List<Html_Segment> getElementList() {
	return new CB_HtmlProcessor(this, rootSegment, getMaxLineLength(), getHRLineLength(), getNewLine(), getIncludeHyperlinkURLs(), getIncludeAlternateText(), getDecorateFontStyles(), getConvertNonBreakingSpaces(), getBlockIndentSize(), getListIndentSize(), getListBullets(), getTableCellSeparator()).getElementList();
    }

    private static final class Not_implemented_ElementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new Not_implemented_ElementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {
	    throw new NotImplementedException("HTML Renderer element <" + element.getName() + "> is not implemented");
	}
    }

    private static final class HR_ElementHandler extends AbstractBlockElementHandler {
	public static final ElementHandler INSTANCE = new HR_ElementHandler();

	private HR_ElementHandler() {
	    this(0, 0, false);
	}

	private HR_ElementHandler(int topMargin, int bottomMargin, boolean indent) {
	    super(topMargin, bottomMargin, indent);
	}

	@Override
	protected void processBlockContent(Processor x, Element element) throws IOException {
	    CB_HtmlProcessor cb_processor = (CB_HtmlProcessor) x;

	    cb_processor.createNewSegment();
	    cb_processor.createNewHrSegment();
	}

	@Override
	protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	    return new HR_ElementHandler(topMargin, bottomMargin, indent);
	}
    }

    public static final class ImagelementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new ImagelementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {
	    ((CB_HtmlProcessor) x).createNewSegment();
	    String src = element.getStartTag().getAttributeValue("src");
	    if (src == null)
		return;
	    x.appendText(src);
	    ((CB_HtmlProcessor) x).isImage = true;
	    ((CB_HtmlProcessor) x).createNewSegment();
	}
    }

    public static final class A_ElementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new A_ElementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {
	    CB_HtmlProcessor cb_processor = (CB_HtmlProcessor) x;

	    String lastText = x.appendable.toString();

	    x.appendElementContent(element);

	    String text = x.appendable.toString().replace(lastText, "");
	    String renderedHyperlinkURL = x.renderer.renderHyperlinkURL(element.getStartTag());

	    cb_processor.add(new HyperLinkText(text, renderedHyperlinkURL));

	    //	    if (renderedHyperlinkURL == null) {
	    //		x.appendElementContent(element);
	    //		return;
	    //	    }
	    //	    String href = element.getAttributeValue("href");
	    //	    final boolean displayContent = href == null || !getInformalURL(href).equals(getInformalURL(element.getContent().toString()));
	    //	    int linkLength = renderedHyperlinkURL.length();
	    //	    if (displayContent) {
	    //		x.appendElementContent(element);
	    //		linkLength++; // allow for space after content
	    //	    }
	    //	    if (x.maxLineLength > 0 && x.col + linkLength >= x.maxLineLength) {
	    //		x.startNewLine(0);
	    //	    } else if (displayContent) {
	    //		x.append(' ');
	    //	    }
	    //	    x.append(renderedHyperlinkURL);
	    //	    x.lastCharWhiteSpace = true;
	}
    }

}
