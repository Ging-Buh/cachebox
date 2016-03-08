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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;

import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.html.HyperLinkText;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.HSV_Color;

/**
 * 
 * @author Longri
 *
 */
public class LinkLabel extends MultiColorLabel {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LinkLabel.class);
    private final AtomicBoolean dirty = new AtomicBoolean(true);;
    private final AtomicBoolean inParse = new AtomicBoolean(false);
    CB_List<HyperLinkText> hyperLinkList = new CB_List<HyperLinkText>();
    private boolean isMarkup = false;

    public LinkLabel(String Name, float X, float Y, float Width, float Height) {
	super(Name, X, Y, Width, Height);

	this.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

		String link = isLinkClicked(x, y);

		if (link != null && !link.isEmpty()) {
		    boolean error = true;
		    log.debug("LinkText: " + link);

		    for (int i = 0; i < hyperLinkList.size(); i++) {
			HyperLinkText hl = hyperLinkList.get(i);

			if (hl.content.equals(link)) {
			    log.debug("Link found call: " + hl.url + " (" + hl.content + ")");

			    String url = hl.url.replaceAll("<", "").replace(">", "");
			    PlatformConnector.callUrl(url);

			    error = false;
			    break;
			}
		    }
		    if (error)
			log.error("Link not found: " + link);
		}

		return true;
	    }
	});

    }

    private static final float floatBits = new HSV_Color("0000fffe").toFloatBits();

    private String isLinkClicked(int x, int y) {

	// check if clicked Glyph color equals #0000fffe

	float lineHeight = mTextObject.getFont().getLineHeight();
	float halfGlyphWidth = lineHeight / 3;
	float[] vertices = mTextObject.getVertices();

	for (int i = 0, n = vertices.length - 21; i < n; i += 20) {
	    float lx1 = vertices[i + 0] - halfGlyphWidth;
	    float ly1 = vertices[i + 1] - halfGlyphWidth;
	    float lx2 = vertices[i + 10] + halfGlyphWidth;
	    float ly2 = ly1 + lineHeight;

	    if (lx1 < x && ly1 < y && lx2 > x && ly2 > y) {
		if (floatBits == vertices[i + 2])
		    return Link(i);
	    }
	}
	return null;
    }

    private String Link(int verticeStart) {
	StringBuilder sb = new StringBuilder();
	float[] vertices = mTextObject.getVertices();

	// search begin of Link
	int Start = 0;
	for (int i = verticeStart; i >= 0; i -= 20) {
	    if (floatBits != vertices[i + 2]) {
		Start = i + 20;
		break;
	    }
	}

	// search end of Link
	int end = vertices.length - 20;
	for (int i = verticeStart, n = vertices.length - 21; i < n; i += 20) {
	    if (floatBits != vertices[i + 2]) {
		end = i - 20;
		break;
	    }
	}

	if (glyphList == null)
	    fillGlyphList();

	for (int i = Start; i <= end; i += 20) {
	    GlyphUV glyphUV = new GlyphUV(vertices[i + 3], vertices[i + 4]);
	    if (floatBits == vertices[i + 2]) {

		String s = glyphList.get(glyphUV);
		if (s != null) {
		    sb.append(s);
		}
	    }
	}

	return sb.toString();
    }

    final static String ALL_LINK_CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890/_. :-[]()!";
    HashMap<GlyphUV, String> glyphList;

    private static class GlyphUV {
	final float U;
	final float V;

	private GlyphUV(float u, float v) {
	    U = u;
	    V = v;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    } else if (!(obj instanceof GlyphUV)) {
		return false;
	    }
	    GlyphUV other = (GlyphUV) obj;
	    if (other.U != U || other.V != V)
		return false;
	    return true;
	}

	@Override
	public int hashCode() {
	    final float prime = 31;
	    float result = 1;
	    result = prime * result + U;
	    result = prime * result + V;
	    return (int) result;
	}
    }

    private void fillGlyphList() {
	BitmapFontData data = mFont.getData();
	glyphList = new HashMap<GlyphUV, String>();
	for (int i = 0, n = ALL_LINK_CHAR.length(); i < n; i++) {
	    char ch = ALL_LINK_CHAR.charAt(i);
	    Glyph g = data.getGlyph(ch);
	    GlyphUV glyphUV = new GlyphUV(g.u, g.v);
	    glyphList.put(glyphUV, String.valueOf(ch));
	}

	Glyph g = data.getGlyph('w');
	GlyphUV glyphUV = new GlyphUV(g.u, g.v);

	String w = glyphList.get(glyphUV);

	System.out.println(w);

    }

    @Override
    public void render(Batch batch) {
	if (dirty.get()) {
	    if (inParse.get())
		return;
	    parse();

	}

	if (isMarkup)
	    this.mFont.getData().markupEnabled = true;
	else
	    this.mFont.getData().markupEnabled = false;
	super.render(batch);
    }

    private void parse() {
	inParse.set(true);

	for (int i = 0, n = this.hyperLinkList.size(); i < n; i++) {

	    HyperLinkText hyper = this.hyperLinkList.get(i);
	    isMarkup = true;
	    this.setText(this.mText.replace(hyper.content, "[#0000fffe]" + hyper.content + "[]"));

	}

	dirty.set(false);
	inParse.set(false);
    }

    public void addHyperlink(HyperLinkText hyperLink) {
	this.hyperLinkList.add(hyperLink);
    }

    public void addHyperlinks(CB_List<HyperLinkText> HyperLinkList) {
	this.hyperLinkList.addAll(HyperLinkList);
    }

    public void setMarkupEnabled(boolean isMarkUp) {
	this.isMarkup = isMarkUp;
    }

}
