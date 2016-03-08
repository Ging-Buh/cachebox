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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.Controls.html.elementhandler.H_ElementHandler;
import CB_UI_Base.GL_UI.Controls.html.elementhandler.ImagelementHandler;
import CB_UI_Base.GL_UI.Controls.html.elementhandler.InputElementHandler;
import CB_UI_Base.GL_UI.Controls.html.elementhandler.Not_implemented_ElementHandler;
import CB_UI_Base.GL_UI.Controls.html.elementhandler.SpanElementHandler;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;

/**
 * @author Longri
 */
public class CB_Html_Renderer extends Renderer {
    public final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Html_Renderer.class);

    public static Map<String, ElementHandler> CB_ELEMENT_HANDLERS = new HashMap<String, ElementHandler>();

    static {

        boolean IMPLEMENTED = true;
        boolean NOTIMPLEMENTED = false;
        CB_ELEMENT_HANDLERS.put(HTMLElementName.A, IMPLEMENTED ? A_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.ADDRESS, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.APPLET, NOTIMPLEMENTED ? AlternateTextElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.B, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_B : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.BLOCKQUOTE, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1_INDENT : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.BR, IMPLEMENTED ? BR_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.BUTTON, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.CAPTION, NOTIMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.CENTER, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.CODE, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_CODE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.DD, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0_INDENT : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.DIR, NOTIMPLEMENTED ? ListElementHandler.INSTANCE_UL : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.DIV, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.DT, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.EM, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_I : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.FIELDSET, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.FORM, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.H1, IMPLEMENTED ? H_ElementHandler.INSTANCE_H1 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.H2, IMPLEMENTED ? H_ElementHandler.INSTANCE_H2 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.H3, IMPLEMENTED ? H_ElementHandler.INSTANCE_H3 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.H4, IMPLEMENTED ? H_ElementHandler.INSTANCE_H4 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.H5, IMPLEMENTED ? H_ElementHandler.INSTANCE_H5 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.H6, IMPLEMENTED ? H_ElementHandler.INSTANCE_H6 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.HEAD, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);

        CB_ELEMENT_HANDLERS.put(HTMLElementName.I, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_I : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.IMG, IMPLEMENTED ? ImagelementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.INPUT, IMPLEMENTED ? InputElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.LEGEND, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.MENU, IMPLEMENTED ? ListElementHandler.INSTANCE_UL : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.MAP, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.NOFRAMES, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.NOSCRIPT, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.P, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_1_1 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.PRE, IMPLEMENTED ? PRE_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.SCRIPT, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.SPAN, IMPLEMENTED ? SpanElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.SELECT, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.STRONG, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_B : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.STYLE, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.TEXTAREA, IMPLEMENTED ? RemoveElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.TD, IMPLEMENTED ? TD_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.TH, IMPLEMENTED ? TD_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.TR, IMPLEMENTED ? StandardBlockElementHandler.INSTANCE_0_0 : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.U, IMPLEMENTED ? FontStyleElementHandler.INSTANCE_U : Not_implemented_ElementHandler.INSTANCE);

// CB overridden Handler

        CB_ELEMENT_HANDLERS.put(HTMLElementName.TABLE, IMPLEMENTED ? CB_UI_Base.GL_UI.Controls.html.elementhandler.TableElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.UL, IMPLEMENTED ? CB_UI_Base.GL_UI.Controls.html.elementhandler.ListElementHandler.INSTANCE_UL : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.OL, IMPLEMENTED ? CB_UI_Base.GL_UI.Controls.html.elementhandler.ListElementHandler.INSTANCE_OL : Not_implemented_ElementHandler.INSTANCE);
        CB_ELEMENT_HANDLERS.put(HTMLElementName.LI, IMPLEMENTED ? CB_UI_Base.GL_UI.Controls.html.elementhandler.LI_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
	CB_ELEMENT_HANDLERS.put(HTMLElementName.HR, IMPLEMENTED ? CB_UI_Base.GL_UI.Controls.html.elementhandler.HR_ElementHandler.INSTANCE : Not_implemented_ElementHandler.INSTANCE);
    }

    public CB_Html_Renderer(Segment segment) {
        super(segment);
    }

    public List<Html_Segment> getElementList() {
        List<Html_Segment> segList = new CB_HtmlProcessor(this, rootSegment, getHRLineLength(), getNewLine(), getIncludeHyperlinkURLs(), getIncludeAlternateText(), getDecorateFontStyles(), getConvertNonBreakingSpaces(), getBlockIndentSize(),
                getListIndentSize(), getListBullets(), getTableCellSeparator()).getElementList();

        //remove last line brakes

        for (int i = segList.size() - 1; i > 0; i--) {
            Html_Segment seg = segList.get(i);

            if (!(seg instanceof HTML_Segment_List) && (seg.formatedText.isEmpty() || hasOnlyLineBreakes(seg.formatedText))) {
                segList.remove(i);
            } else {
                break;
            }

        }

        return segList;
    }

    private boolean hasOnlyLineBreakes(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!(c == '\r' || c == '\n' || c == ' '))
                return false;
        }
        return true;
    }

}
