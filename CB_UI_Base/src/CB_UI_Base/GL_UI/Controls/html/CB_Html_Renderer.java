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
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;

import org.slf4j.LoggerFactory;

/**
 * @author Longri
 */
public class CB_Html_Renderer extends Renderer
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Html_Renderer.class);

	public CB_Html_Renderer(Segment segment)
	{
		super(segment);
		// Replace image handler
		ELEMENT_HANDLERS.remove(HTMLElementName.IMG);
		ELEMENT_HANDLERS.put(HTMLElementName.IMG, ImagelementHandler.INSTANCE);

		// // DEBUG
		// ELEMENT_HANDLERS.remove(HTMLElementName.BR);
		// ELEMENT_HANDLERS.put(HTMLElementName.BR, BR_ElementHandler.INSTANCE);

	}

	public List<HtmlSegment> getElementList()
	{
		return new CB_HtmlProcessor(this, rootSegment, getMaxLineLength(), getHRLineLength(), getNewLine(), getIncludeHyperlinkURLs(), getIncludeAlternateText(), getDecorateFontStyles(), getConvertNonBreakingSpaces(), getBlockIndentSize(), getListIndentSize(), getListBullets(), getTableCellSeparator()).getElementList();
	}

	public static final class ImagelementHandler implements ElementHandler
	{
		public static final ElementHandler INSTANCE = new ImagelementHandler();

		@Override
		public void process(Processor x, Element element) throws IOException
		{
			String src = element.getStartTag().getAttributeValue("src");
			if (src == null) return;
			x.appendText(src);
			((CB_HtmlProcessor) x).isImage = true;
		}
	}

	private static final class BR_ElementHandler implements ElementHandler
	{
		public static final ElementHandler INSTANCE = new BR_ElementHandler();

		@Override
		public void process(Processor x, Element element) throws IOException
		{

			if (x.isBlockBoundary() && !x.atStartOfLine && !x.skipInitialNewLines) x.newLine(); // add an extra new line if we're at a block
																								// boundary and aren't already at the start
																								// of the next line and it's not the first
																								// element after <li>
			x.newLine();
			x.appendText("DEBUG <BR/> ID:" + element.getAttributeValue("id"));
			x.blockBoundary(0);
		}
	}

}
