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
package CB_UI_Base.GL_UI.Controls;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Math.CB_RectF;

public class HtmlView extends ScrollBox
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(HtmlView.class);

	public HtmlView(CB_RectF rec)
	{
		super(rec);
	}

	public void showHtml(String html)
	{
		Source source = new Source(html);
		Renderer renderer = source.getRenderer();

		renderer.setIncludeFirstElementTopMargin(true);

		// List<Element> elementList = source.getAllElements();
		// for (Element element : elementList)
		// {
		// System.out.println("-------------------------------------------------------------------------------");
		// System.out.println(element.getDebugInfo());
		// if (element.getAttributes() != null) System.out.println("XHTML StartTag:\n" + element.getStartTag().tidy(true));
		// System.out.println("Source text with content:\n" + element);
		// }
		// System.out.println(source.getCacheDebugInfo());

		log.info(renderer.toString());
	}
}
