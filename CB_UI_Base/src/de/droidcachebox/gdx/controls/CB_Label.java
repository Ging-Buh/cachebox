/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.graphics.PolygonDrawable;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GeometryList;
import de.droidcachebox.gdx.math.Line;
import de.droidcachebox.gdx.math.Quadrangle;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.LogLevel;

/**
 * @author Longri
 */
public class CB_Label extends CB_View_Base {
    private static final String sClass = "Label";

    private static final float DEFAULTSCROLLSTEP = 0.7f;
    private static final int SCROLL_PAUSE = 60;
    private static float scrollstep = 0;
    private final AtomicBoolean isRenderingOnce = new AtomicBoolean(false);
    protected BitmapFontCache mTextObject;
    protected HAlignment mHAlignment = HAlignment.LEFT;
    protected VAlignment mVAlignment = VAlignment.CENTER;
    private String mText;
    private BitmapFont mFont = Fonts.getNormal();
    private Color mColor = COLOR.getFontColor();
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
     * object for holding Text. default size is ButtonWidthWide x ButtonHeight from UI_Size_Base
     **/
    public CB_Label() {
        super(0, 0, UiSizes.getInstance().getButtonWidthWide(), UiSizes.getInstance().getButtonHeight(), "Label");
        setText();
    }

    /**
     * object for holding Text. default size is ButtonWidthWide x ButtonHeight from UI_Size_Base
     **/
    public CB_Label(String text) {
        super(0, 0, UiSizes.getInstance().getButtonWidthWide(), UiSizes.getInstance().getButtonHeight(), "Label");
        mText = text == null ? "" : text.replace("\r\n", "\n");
        setText();
    }

    /**
     * object for holding Text. default size is ButtonWidthWide x ButtonHeight from UI_Size_Base
     **/
    public CB_Label(String text, BitmapFont font, Color fontColor, WrapType wrapType) {
        super(0, 0, UiSizes.getInstance().getButtonWidthWide(), UiSizes.getInstance().getButtonHeight(), "Label");
        mText = (text == null ? "" : text.replace("\r\n", "\n"));
        if (font != null)
            mFont = font;
        if (fontColor != null)
            mColor = fontColor;
        if (wrapType != null)
            mWrapType = wrapType;
        setText();
    }

    public CB_Label(String name, float x, float y, float width, float height, String text) {
        super(x, y, width, height, name);
        mText = text == null ? "" : text.replace("\r\n", "\n");
        setText();
    }

    public CB_Label(String name, float x, float Y, float width, float height) {
        super(x, Y, width, height, name);
        mText = "";
        setText();
    }

    public CB_Label(String name, CB_RectF rec, String Text) {
        super(rec, name);
        mText = Text == null ? "" : Text.replace("\r\n", "\n");
        setText();
    }

    public CB_Label(CB_RectF rec) {
        super(rec, "");
        mText = "";
        setText();
    }

    private static int GDX_HAlignment(HAlignment alignment) {
        switch (alignment) {
            case CENTER:
            case SCROLL_CENTER:
                return com.badlogic.gdx.utils.Align.center;
            case RIGHT:
            case SCROLL_RIGHT:
                return com.badlogic.gdx.utils.Align.right;
            default:
                return com.badlogic.gdx.utils.Align.left;
        }
    }

    @Override
    protected void render(Batch batch) {

        if (lastRender <= 0)
            lastRender = GL.that.getStateTime();

        try {
            if (mTextObject != null) {
                if (mTextObject.usesIntegerPositions())
                    try {
                        mTextObject.draw(batch);
                    } catch (Exception ex) {
                        Log.err(sClass, "Rendering " + mText + "\r\n" + mTextObject.toString() + "\r\n" + ex.toString() + "\r\n" + ex.getLocalizedMessage());
                    }
            }

            // Draw Underline
            if (underline || strikeout) {
                if (underlineStrikeoutDrawable == null) {
                    GeometryList lineList = new GeometryList();
                    if (underline)
                        addLine(lineList, 0);
                    if (strikeout)
                        if (mTextObject != null)
                            addLine(lineList, mTextObject.getFont().getDescent());
                    GL_Paint PAINT = new GL_Paint();
                    PAINT.setColor(mColor);
                    underlineStrikeoutDrawable = new PolygonDrawable(lineList.getVertices(), lineList.getTriangles(), PAINT, this.getWidth(), this.getHeight());
                }
                underlineStrikeoutDrawable.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
            }

            // calculate scroll animation
            if (mHAlignment == HAlignment.SCROLL_CENTER || mHAlignment == HAlignment.SCROLL_LEFT || mHAlignment == HAlignment.SCROLL_RIGHT) {

                if (scrollstep <= 0) {
                    scrollstep = DEFAULTSCROLLSTEP * UiSizes.getInstance().getScale();
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
        } catch (Exception e) {
            setText();
        }
    }

    private void checkRenderMustStart() {
        if (isRenderingOnce.get())
            return;
        new Thread(() -> {
            isRenderingOnce.set(true);
            while (lastRender + 0.01 < GL.that.getStateTime()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                GL.that.renderOnce();
                isRenderingOnce.set(false);
            }
        }).start();
    }

    private void addLine(GeometryList lineList, float yOffset) {
        float ascent = mTextObject.getFont().getAscent();
        float underlineHight = ascent * 0.333f;
        float[] vertices = mTextObject.getVertices();

        int start2 = 0;
        float lxStart = 0;
        float lxEnd;
        float ly = 0;

        int n = vertices.length - 21;
        for (int i = 0; i < n; i += 20) {

            if (start2 == i) {
                lxStart = vertices[i];
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
    }

    private void setText() {

        if (mText == null) return;

        final int n = mText.length();

        if (LogLevel.shouldWriteLog(LogLevel.TRACE)) {
            // show chars, that don't exist in the mFont
            for (int start = 0; start < n; start++) {
                if (mFont.getData().getGlyph(mText.charAt(start)) == null) {
                    char c = mText.charAt(start);
                    if (c != '\r' && c != '\n')
                        Log.err(sClass, "Unknown Char {" + c + "} @:" + mText + "[" + start + "]");
                }
            }
        }

        /*
        if (mTextObject == null) {
            mTextObject = new BitmapFontCache(mFont, true);
        } else if (!mTextObject.getFont().equals(mFont)) {
            mTextObject = new BitmapFontCache(mFont, true);
        }
        if (!mColor.equals(mTextObject.getColor())) {
            mTextObject.setColor(mColor);
        }

         */
        mTextObject = new BitmapFontCache(mFont, true);
        if (mColor != null) mTextObject.setColor(mColor);

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
            Log.err(sClass, "Für " + mText + "\n" + e);
        }
        if (underlineStrikeoutDrawable != null) {
            underlineStrikeoutDrawable.dispose();
            underlineStrikeoutDrawable = null;
        }
        setTextPosition();
    }

    private void setTextPosition() {

        if (bounds == null) {
            //try to reset text
            TimerTask later = new TimerTask() {
                @Override
                public void run() {
                    GL.that.runOnGL(() -> setText());
                }
            };
            new Timer().schedule(later, 100);
            Log.debug(sClass, "bounds are NULL on setTextPosition! Try setText()");
            return;
        }

        // left : text starts at xPosition
        float xPosition = leftBorder + 1; // !!! Die 1 ist empirisch begründet
        // default HAlignment.LEFT
        if (innerWidth > bounds.width) {
            if (mHAlignment == HAlignment.CENTER || mHAlignment == HAlignment.SCROLL_CENTER) {
                if (mWrapType == WrapType.SINGLELINE) {
                    xPosition = (innerWidth - bounds.width) / 2f;
                }
            } else if (mHAlignment == HAlignment.RIGHT || mHAlignment == HAlignment.SCROLL_RIGHT) {
                if (mWrapType == WrapType.SINGLELINE) {
                    xPosition = innerWidth - bounds.width;
                }
            }
        } else {
            if (mHAlignment == HAlignment.SCROLL_CENTER || mHAlignment == HAlignment.SCROLL_LEFT || mHAlignment == HAlignment.SCROLL_RIGHT) {
                xPosition += scrollPos;
            }
            /*
            else {
                // no horizontal scrolling and Text out of limits
                // Log.debug(log, "Label Text is too long: " + mText);
            }
             */
        }
        // bottom : text starts at yPosition, Text wird von hier aus unterhalb geschrieben (Descent ist negativ, daher -)
        float yPosition = 0;
        if (mVAlignment == null)
            mVAlignment = VAlignment.CENTER;
        switch (mVAlignment) {
            case TOP:
                yPosition = innerHeight - topBorder - mFont.getAscent();
                break;
            case CENTER:
                // bounds.height == mFont.getCapHeight() only if one line text
                yPosition = (innerHeight + bounds.height - mFont.getDescent()) / 2f; //  - mFont.getDescent()
                break;
            case BOTTOM:
                yPosition = bottomBorder + mFont.getCapHeight() - mFont.getDescent();
        }

        try {
            mTextObject.setPosition(xPosition, yPosition);
            ErrorCount = 0;
        } catch (Exception e) {
            // Try again
            ErrorCount++;
            if (ErrorCount < 5)
                GL.that.runOnGL(this::setTextPosition);
        }
        GL.that.renderOnce();
    }

    /**
     * setting the Text. new line is GlobalCore.br
     **/
    public void setMultiLineText(String text) {
        if (text == null)
            text = "";
        mText = text.replace("\r\n", "\n");
        mVAlignment = VAlignment.TOP;
        this.mWrapType = WrapType.MULTILINE;
        setText();
    }

    /**
     * setting the Text. new Line wrap determined by width
     **/
    public CB_Label setWrappedText(String text) {
        if (text == null)
            text = "";
        mText = text.replace("\r\n", "\n");
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

    public CB_Label setWrapType(WrapType WrapType) {
        // layout depends on the width and text
        if (WrapType != mWrapType) {
            mWrapType = WrapType;
            setText();
        }
        return this;
    }

    public void updateHeight(CB_View_Base parentControl, boolean onlyExpand) {
        // intended for text not fitting into the predefined height
        // the Y position of other controls must be corrected
        float h = getTextHeight();
        if (!(onlyExpand && h <= getHeight())) {
            if (parentControl != null) {
                parentControl.updateRowY(h, this);
            }
            setHeight(h);
        }
    }

    public CB_Label setHAlignment(HAlignment HAlignment) {
        if (HAlignment != null) {
            if (mHAlignment != HAlignment) {
                mHAlignment = HAlignment;
                setText();
            }
        }
        return this;
    }

    public CB_Label setVAlignment(VAlignment VAlignment) {
        if (VAlignment != null) {
            if (mVAlignment != VAlignment) {
                mVAlignment = VAlignment;
                setText();
            }
        }
        return this;
    }

    public void setTextColor(Color color) {
        if (color != null) {
            if (!mColor.equals(color)) {
                mColor = color;
                setText();
            }
        }
    }

    public String getText() {
        return mText;
    }

    /**
     * setting the Text
     **/
    public CB_Label setText(String text) {
        if (text == null)
            text = "";
        mText = text.replace("\r\n", "\n");
        this.mWrapType = WrapType.SINGLELINE;
        setText();
        return this;
    }

    public int getLineCount() {
        if (bounds == null)
            return 0;
        // int lc = 1 + (int) ((bounds.height - mFont.getCapHeight()) / mFont.getLineHeight());
        return bounds.runs.size;
    }

    /*
    public float getLineHeight() {
        return mFont.getLineHeight();
    }
     */

    public BitmapFont getFont() {
        return mFont;
    }

    public CB_Label setFont(BitmapFont Font) {
        if (Font != null) {
            if (Font != mFont) {
                mFont = Font;
                setText();
            }
        }
        return this;
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
    protected void skinIsChanged() {
        // todo set the correct Font (original FontSize not known)
        mFont = Fonts.getNormal();
        Color c = COLOR.getFontColor();
        if (c != null) mColor = c;
        setText();
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

    public void setText(String text, HSV_Color color) {
        if (color != null) {
            if (!color.equals(mColor)) {
                mColor = color;
            }
        }

        if (text == null)
            text = "";
        mText = text.replace("\r\n", "\n");
        setText();
    }

    public Color getColor() {
        return mColor;
    }

    public enum VAlignment {
        TOP, CENTER, BOTTOM
    }

    public enum HAlignment {
        LEFT, CENTER, RIGHT, SCROLL_LEFT, SCROLL_CENTER, SCROLL_RIGHT
    }

}
