/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Geometry.GeometryList;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_Utils.Log.Log;
import CB_Utils.Util.HSV_Color;

/**
 * 
 * @author Longri
 *
 */
public class Label extends CB_View_Base {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(Label.class);

    private static final float DEFAULTSCROLLSTEP = 0.7f;
    private static final int SCROLL_PAUSE = 60;

    static public enum VAlignment {
	TOP, CENTER, BOTTOM
    }

    static public enum HAlignment {
	LEFT, CENTER, RIGHT, SCROLL_LEFT, SCROLL_CENTER, SCROLL_RIGHT
    }

    private static float scrollstep = 0;

    public static int GDX_HAlignment(HAlignment ali) {
	switch (ali) {
	case CENTER:
	    return com.badlogic.gdx.utils.Align.center;
	case LEFT:
	    return com.badlogic.gdx.utils.Align.left;
	case RIGHT:
	    return com.badlogic.gdx.utils.Align.right;
	case SCROLL_CENTER:
	    return com.badlogic.gdx.utils.Align.center;
	case SCROLL_LEFT:
	    return com.badlogic.gdx.utils.Align.left;
	case SCROLL_RIGHT:
	    return com.badlogic.gdx.utils.Align.right;
	default:
	    return com.badlogic.gdx.utils.Align.left;

	}
    }

    protected BitmapFontCache mTextObject;
    // FIXME Create BitmapFontCache-Array and reduce PolygonSpriteBatch(10920)
    // constructor for Labels with long Text

    protected String mText = "";
    protected BitmapFont mFont = Fonts.getNormal();
    protected Color mColor = COLOR.getFontColor();
    protected HAlignment mHAlignment = HAlignment.LEFT;

    private VAlignment mVAlignment = VAlignment.CENTER;
    private WrapType mWrapType = WrapType.SINGLELINE;
    private int ErrorCount = 0;

    private int scrollPos = 0;
    private boolean left = true;
    private int scrollPauseCount = 0;
    private boolean underline = false;
    private boolean strikeout = false;
    private GlyphLayout bounds;
    private PolygonDrawable underlineStrikeoutDrawable;

    private float lastRender;

    /**
     * object for holding Text. default size is ButtonWidthWide x ButtonHeight
     * from UI_Size_Base
     **/
    public Label() {
	super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label");
	initLabel();
    }

    /**
     * object for holding Text. default size is ButtonWidthWide x ButtonHeight
     * from UI_Size_Base
     **/
    public Label(String Text) {
	super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label " + Text);
	mText = Text == null ? "" : Text;
	initLabel();
    }

    /**
     * object for holding Text. default size is ButtonWidthWide x ButtonHeight
     * from UI_Size_Base
     **/
    public Label(String Text, BitmapFont Font, Color fontColor, WrapType WrapType) {
	super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label " + Text);
	mText = (Text == null ? "" : Text);
	if (Font != null)
	    mFont = Font;
	if (fontColor != null)
	    mColor = fontColor;
	if (WrapType != null)
	    mWrapType = WrapType;
	initLabel();
    }

    public Label(String Name, float X, float Y, float Width, float Height, String Text) {
	super(X, Y, Width, Height, Name);
	mText = Text == null ? "" : Text;
	initLabel();
    }

    public Label(String Name, float X, float Y, float Width, float Height) {
	super(X, Y, Width, Height, Name);
	mText = "";
	initLabel();
    }

    public Label(String Name, CB_RectF rec, String Text) {
	super(rec, Name);
	mText = Text == null ? "" : Text;
	initLabel();
    }

    public Label(String Name, CB_RectF rec) {
	super(rec, Name);
	mText = "";
	initLabel();
    }

    private void initLabel() {
	setText();
    }

    static int indexOf(CharSequence text, char ch, int start) {
	final int n = text.length();
	for (; start < n; start++)
	    if (text.charAt(start) == ch)
		return start;
	return n;
    }

    @Override
    protected void render(Batch batch) {

	if (lastRender <= 0)
	    lastRender = GL.that.getStateTime();

		
	
	try {
	    if (mTextObject != null) {
		if (mTextObject.usesIntegerPositions())
		    mTextObject.draw(batch);
	    }

	    // Draw Underline
	    if (underline | strikeout) {
		if (underlineStrikeoutDrawable == null) {
		    GeometryList lineList = new GeometryList();
		    if (underline)
			addLine(lineList, 0);
		    if (strikeout)
			addLine(lineList, mTextObject.getFont().getDescent());
		    GL_Paint PAINT = new GL_Paint();
		    PAINT.setColor(mColor);
		    underlineStrikeoutDrawable = new PolygonDrawable(lineList.getVertices(), lineList.getTriangles(),
			    PAINT, this.getWidth(), this.getHeight());
		}
		underlineStrikeoutDrawable.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
	    }

	    // calculate scroll animation
	    if (mHAlignment == HAlignment.SCROLL_CENTER || mHAlignment == HAlignment.SCROLL_LEFT
		    || mHAlignment == HAlignment.SCROLL_RIGHT) {

		if (scrollstep <= 0) {
		    scrollstep = DEFAULTSCROLLSTEP * UI_Size_Base.that.getScale();
		}

		int max = (int) (bounds.width - innerWidth) + 20;
		if (max < 0)
		    return;

		if (lastRender + 0.01 < GL.that.getStateTime()) {
		    lastRender = GL.that.getStateTime();
		    if (left) {
			scrollPauseCount++;
			if (scrollPauseCount > SCROLL_PAUSE) {
			    scrollPos -= scrollstep;
			    if (scrollPos < -max) {
				left = false;
				scrollPauseCount = 0;
			    }
			}

		    } else {

			scrollPauseCount++;
			if (scrollPauseCount > SCROLL_PAUSE) {

			    scrollPauseCount = scrollPos = 0;
			    left = true;
			}
		    }

		    setTextPosition();
		    GL.that.renderOnce();
		} else {
		    checkRenderMustStart();
		}

	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    // kommt manchmal wenn der Text geändert wird
	    setText();
	} catch (NullPointerException e) {
	    // kommt manchmal wenn der Text geändert wird
	    setText();
	}
    }

    private final AtomicBoolean checkRuns = new AtomicBoolean(false);

    private void checkRenderMustStart() {
	if (checkRuns.get())
	    return;
	Thread thread = new Thread(new Runnable() {
	    @Override
	    public void run() {
		checkRuns.set(true);
		while (lastRender + 0.01 < GL.that.getStateTime()) {
		    try {
			Thread.sleep(10);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    GL.that.renderOnce();
		    checkRuns.set(false);
		}
	    }
	});
	thread.start();
    }

    private GeometryList addLine(GeometryList lineList, float yOffset) {
	float ascent = mTextObject.getFont().getAscent();
	float underlineHight = ascent * 0.333f;
	float[] vertices = mTextObject.getVertices();

	int start2 = 0;
	float lxStart = 0;
	float lxEnd = 0;
	float ly = 0;

	for (int i = 0, n = vertices.length - 21; i < n; i += 20) {

	    if (start2 == i) {
		lxStart = vertices[i + 0];
		ly = vertices[i + 1] - underlineHight;
	    }

	    float ly2 = vertices[i + 21];

	    if ((ly2 + ascent < ly) || (i == n - 19)) {

		// search last visible
		boolean mustBreak = false;
		if ((i == n - 19)) {
		    while (true) {
			if (vertices[i] == 0 && vertices[i + 1] == 0 && vertices[i + 2] == 0) {
			    i -= 20;
			} else {
			    mustBreak = true;
			    break;
			}
		    }

		}

		// Line breake
		lxEnd = vertices[i + 15];
		start2 = i + 20;

		if (lxStart != lxEnd) {
		    Line line = new Line(lxStart, ly - yOffset, lxEnd, ly - yOffset);
		    Quadrangle qr = new Quadrangle(line, underlineHight);
		    lineList.add(qr);
		}
		if (mustBreak)
		    break;
	    }

	}
	return lineList;
    }

    private void setText() {
    	
    	final int n = mText.length();
    	
    	for (int start=0; start < n; start++){
    	    if (mFont.getData().getGlyph(mText.charAt(start)) == null){
    	    	char c=mText.charAt(start);
    	    	if(c!='\r'&& c!='\n')
    	    	log.error("Unknown Char {"+c+"} IntValue:");
    	    }
    	}
    	
    	
	if (mTextObject == null) {
	    mTextObject = new BitmapFontCache(mFont, true);
	} else if (!mTextObject.getFont().equals(mFont)) {
	    mTextObject = new BitmapFontCache(mFont, true);
	}
	if (!mColor.equals(mTextObject.getColor())) {
	    mTextObject.setColor(mColor);
	}
	try {
	    switch (mWrapType) {
	    case SINGLELINE:
		bounds = mTextObject.setText(mText, 0f, 0f);
		break;
	    case MULTILINE:
		bounds = mTextObject.setText(mText, 0f, 0f, this.getWidth(), GDX_HAlignment(mHAlignment), false);
		break;
	    case WRAPPED:
		bounds = mTextObject.setText(mText, 0f, 0f, this.getWidth(), GDX_HAlignment(mHAlignment), true);
		break;
	    }
	} catch (Exception e) {
	    // java.lang.ArrayIndexOutOfBoundsException kommt mal vor
	    e.printStackTrace();
	    Log.err(log, this + " (" + mWrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + " \"" + mText + "\"", e);
	}
	if (underlineStrikeoutDrawable != null) {
	    underlineStrikeoutDrawable.dispose();
	    underlineStrikeoutDrawable = null;
	}
	setTextPosition();
    }

    private void setTextPosition() {
	// left : text starts at xPosition
	float xPosition = leftBorder + 1; // !!! Die 1 ist empirisch begründet
	// default HAlignment.LEFT
	if (innerWidth > bounds.width) {
	    if (mHAlignment == HAlignment.CENTER || mHAlignment == HAlignment.SCROLL_CENTER) {
		if (mWrapType == WrapType.SINGLELINE) {
		    xPosition = (innerWidth - bounds.width) / 2f;
		} else {
		}
	    } else if (mHAlignment == HAlignment.RIGHT || mHAlignment == HAlignment.SCROLL_RIGHT) {
		if (mWrapType == WrapType.SINGLELINE) {
		    xPosition = innerWidth - bounds.width;
		} else {
		}
	    }
	} else {
	    if (mHAlignment == HAlignment.SCROLL_CENTER || mHAlignment == HAlignment.SCROLL_LEFT
		    || mHAlignment == HAlignment.SCROLL_RIGHT) {
		xPosition += scrollPos;
	    } else {
		// no horizontal scrolling and Text out of limits
		// Log.debug(log, "Label Text is too long: " + mText);
	    }
	}
	// bottom : text starts at yPosition, Text wird von hier aus unterhalb
	// geschrieben (Descent ist negativ, daher -)
	float yPosition = bottomBorder + mFont.getCapHeight() - mFont.getDescent(); // VAlignment.BOTTOM
	if (mVAlignment == null)
	    mVAlignment = VAlignment.CENTER;
	switch (mVAlignment) {
	case TOP:
	    yPosition = innerHeight - topBorder - mFont.getAscent();
	    break;
	case CENTER:
	    yPosition = (innerHeight + bounds.height) / 2f;
	    break;
	default:
	    break;
	}

	try {
	    mTextObject.setPosition(xPosition, yPosition);
	    ErrorCount = 0;
	} catch (Exception e) {
	    // Try again
	    ErrorCount++;
	    if (ErrorCount < 5)
		GL.that.RunOnGL(new IRunOnGL() {
		    @Override
		    public void run() {
			setTextPosition();
		    }
		});
	}
    }

    /**
     * setting the Text
     **/
    public Label setText(String text) {
	if (text == null)
	    text = "";
	mText = text;
	this.mWrapType = WrapType.SINGLELINE;
	setText();
	return this;
    }

    /**
     * setting the Text. new line is GlobalCore.br
     **/
    public Label setMultiLineText(String text) {
	if (text == null)
	    text = "";
	mText = text;
	mVAlignment = VAlignment.TOP;
	this.mWrapType = WrapType.MULTILINE;
	setText();
	return this;
    }

    /**
     * setting the Text. new Line wrap determined by width
     **/
    public Label setWrappedText(String text) {
	if (text == null)
	    text = "";
	mText = text;
	mVAlignment = VAlignment.TOP;
	this.mWrapType = WrapType.WRAPPED;
	setText();
	return this;
    }

    public void setUnderline(boolean value) {
	underline = value;
    }

    public void setStrikeout(boolean value) {
	strikeout = value;
    }

    public Label setWrapType(WrapType WrapType) {
	if (WrapType != null) {
	    if (WrapType != mWrapType) {
		mWrapType = WrapType;
		setText();
	    }
	}
	return this;
    }

    public Label setFont(BitmapFont Font) {
	if (Font != null) {
	    if (Font != mFont) {
		mFont = Font;
		setText();
	    }
	}
	return this;
    }

    public Label setHAlignment(HAlignment HAlignment) {
	if (HAlignment != null) {
	    if (mHAlignment != HAlignment) {
		mHAlignment = HAlignment;
		setText();
	    }
	}
	return this;
    }

    public Label setVAlignment(VAlignment VAlignment) {
	if (VAlignment != null) {
	    if (mVAlignment != VAlignment) {
		mVAlignment = VAlignment;
		setText();
	    }
	}
	return this;
    }

    public Label setTextColor(Color color) {
	if (color != null) {
	    if (!mColor.equals(color)) {
		mColor = color;
		setText();
	    }
	}
	return this;
    }

    public String getText() {
	return mText;
    }

    public int getLineCount() {
	if (bounds == null)
	    return 0;
	int lc = 1 + (int) ((bounds.height - mFont.getCapHeight()) / mFont.getLineHeight());
	return lc;
    }

    public BitmapFont getFont() {
	return mFont;
    }

    public float getTextHeight() {
	if (bounds != null)
	    return bounds.height + mFont.getAscent() - mFont.getDescent();
	return 0f;
    }

    public float getTextWidth() {
	if (bounds != null) {
	    return bounds.width;
	}
	return 0f;
    }

    @Override
    protected void Initial() {
	// must implement Initial
    }

    @Override
    protected void SkinIsChanged() {
	// todo den korrekten Font (original Fontgrösse nicht bekannt) setzen
	mFont = Fonts.getNormal();
	mColor = COLOR.getFontColor();
	initLabel();
    }

    @Override
    public void onResized(CB_RectF rec) {
	// wird automatisch aufgerufen,
	// wenn setWidth oder setHeight, ...
	setText();
    }

    @Override
    public void setBackground(Drawable background) {
	super.setBackground(background);
	setText();
    }

    @Override
    public void dispose() {
	mTextObject = null;
	mVAlignment = null;
	mHAlignment = null;
	mText = null;
	bounds = null;
    }

    public void setText(String text, HSV_Color color) {
	if (color != null) {
	    if (!color.equals(mColor)) {
		mColor = color;
	    }
	}

	if (text == null)
	    text = "";
	mText = text;
	setText();
    }

    @Override
    public void measureRec() {
	// Some Controls can change there size

	if (bounds != null) {
	    this.setSize(bounds.width + leftBorder + rightBorder + (mFont.getCapHeight() - mFont.getDescent()),
		    bounds.height + bottomBorder + topBorder + (mFont.getCapHeight() - mFont.getDescent()));
	}

    }

}
