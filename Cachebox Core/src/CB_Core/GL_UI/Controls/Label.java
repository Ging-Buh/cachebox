/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Label extends CB_View_Base
{

	static public enum VAlignment
	{
		TOP, CENTER, BOTTOM
	}

	static private enum WrapType
	{
		singleLine, multiLine, wrapped
	}

	private BitmapFontCache TextObject;

	private String text = "";
	private BitmapFont mFont;
	private Color mColor;
	private VAlignment valignment = VAlignment.CENTER;
	private HAlignment halignment = HAlignment.LEFT;

	private TextBounds bounds;

	private float leftBorder;
	private float rightBorder;
	private float topBorder;
	private float bottomBorder;
	private float availableWidth;
	private float availableHeight;

	private WrapType wrapType = WrapType.singleLine;
	private int lineCount = 1;

	public Label(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		initLabel();
	}

	public Label(CB_RectF rec, String Name)
	{
		super(rec, Name);
		initLabel();
	}

	public Label(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec, Parent, Name);
		initLabel();
	}

	public Label(String name)
	{
		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight()), name);
		initLabel();
	}

	private void initLabel()
	{
		mFont = Fonts.getNormal();
		mColor = Fonts.getFontColor();
		TextObject = new BitmapFontCache(mFont, false);
		TextObject.setColor(mColor);
		bounds = TextObject.setText(text, 0, mFont.getCapHeight());
		calcInnerRec();
	}

	private void calcInnerRec()
	{
		leftBorder = drawableBackground != null ? drawableBackground.getLeftWidth() : 0;
		rightBorder = drawableBackground != null ? drawableBackground.getRightWidth() : 0;
		topBorder = drawableBackground != null ? drawableBackground.getTopHeight() : 0;
		bottomBorder = drawableBackground != null ? drawableBackground.getBottomHeight() : 0;
		availableWidth = width - rightBorder - leftBorder;
		availableHeight = height - topBorder - bottomBorder;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		try
		{
			if (TextObject != null) TextObject.draw(batch);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// kommt manchmal wenn der Text geändert wird
		}
		catch (NullPointerException e)
		{
			// kommt manchmal wenn der Text geändert wird
		}
	}

	private void setText()
	{
		wrapType = WrapType.singleLine;
		makeTextObject();
		bounds = mFont.getBounds(text);
		try
		{
			bounds = TextObject.setText(text, 0, bounds.height);
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			Logger.DEBUG(this + " (" + wrapType + "/" + halignment + "/" + valignment + ") " + bounds.width + "," + bounds.height + " \""
					+ text + "\"");
		}
		setTextPosition();
	}

	private void setMultiLineText()
	{
		wrapType = WrapType.multiLine;
		makeTextObject();
		bounds = mFont.getMultiLineBounds(text);
		try
		{
			bounds = TextObject.setMultiLineText(text, 0, bounds.height, bounds.width, halignment);
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			Logger.DEBUG(this + " (" + wrapType + "/" + halignment + "/" + valignment + ") " + bounds.width + "," + bounds.height + " \""
					+ text + "\"");
		}
		setTextPosition();
	}

	private void setWrappedText()
	{
		wrapType = WrapType.wrapped;
		makeTextObject();
		bounds = mFont.getWrappedBounds(text, availableWidth);
		try
		{
			bounds = TextObject.setWrappedText(text, 0, bounds.height, bounds.width, halignment);
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			Logger.DEBUG(this + " (" + wrapType + "/" + halignment + "/" + valignment + ") " + bounds.width + "," + bounds.height + " \""
					+ text + "\"");
		}
		setTextPosition();
	}

	private void makeTextObject()
	{
		if (!TextObject.getFont().equals(mFont))
		{
			TextObject = new BitmapFontCache(mFont, false);
		}
		if (!TextObject.getColor().equals(mColor))
		{
			TextObject.setColor(mColor);
		}
	}

	private void setTextPosition()
	{
		float xPosition = leftBorder + 1; // HAlignment.LEFT !!! Die 1 ist empirisch begründet
		if (availableWidth > bounds.width)
		{
			if (halignment == HAlignment.CENTER)
			{
				xPosition = (availableWidth - bounds.width) / 2f;
			}
			else if (halignment == HAlignment.RIGHT)
			{
				xPosition = availableWidth - bounds.width;
			}
		}
		float yPosition = 0; // VAlignment.BOTTOM
		switch (valignment)
		{
		case TOP:
			yPosition = availableHeight - bounds.height - mFont.getAscent();
			break;
		case CENTER:
			yPosition = (availableHeight - bounds.height - mFont.getAscent()) / 2f;
			break;
		}
		TextObject.setPosition(xPosition, yPosition);
	}

	private void makeText()
	{
		switch (wrapType)
		{
		case singleLine:
			setText();
			break;
		case multiLine:
			setMultiLineText();
			break;
		case wrapped:
			setWrappedText();
			break;
		}
	}

	public TextBounds setText(String text)
	{
		return setText(text, null, null, null);
	}

	public TextBounds setText(String Text, Color color)
	{
		return setText(Text, null, color, null);
	}

	public TextBounds setText(String text, BitmapFont font, Color fontColor)
	{
		return setText(text, font, fontColor, null);
	}

	public TextBounds setText(String _text, BitmapFont font, Color fontColor, HAlignment alignment)
	{
		if (_text == null) _text = "";
		text = _text;
		if (font != null) mFont = font;
		if (fontColor != null) mColor = fontColor;
		if (alignment != null) halignment = alignment;
		setText();
		return bounds;
	}

	public TextBounds setMultiLineText(String _text)
	{
		return setMultiLineText(_text, HAlignment.LEFT);
	}

	public TextBounds setMultiLineText(String _text, HAlignment alignment)
	{
		if (_text == null) _text = "";
		text = _text;
		if (alignment != null) halignment = alignment;
		valignment = VAlignment.TOP;
		setMultiLineText();
		return bounds;
	}

	public TextBounds setWrappedText(String _text)
	{
		return setWrappedText(_text, null, null, HAlignment.LEFT);
	}

	public TextBounds setWrappedText(String _text, HAlignment alignment)
	{
		return setWrappedText(_text, null, null, alignment);
	}

	public TextBounds setWrappedText(String _text, BitmapFont font, Color fontColor, HAlignment alignment)
	{
		if (_text == null) _text = "";
		text = _text;
		if (font != null) mFont = font;
		if (fontColor != null) mColor = fontColor;
		if (alignment != null) halignment = alignment;
		valignment = VAlignment.TOP;
		setWrappedText();
		return bounds;
	}

	public void setFont(BitmapFont font)
	{
		if (font != null)
		{
			if (font != mFont)
			{
				mFont = font;
				makeText();
			}
		}
	}

	public void setHAlignment(HAlignment alignment)
	{
		if (alignment != null)
		{
			if (halignment != alignment)
			{
				halignment = alignment;
				makeText();
			}
		}
	}

	public void setVAlignment(VAlignment alignment)
	{
		if (alignment != null)
		{
			if (valignment != alignment)
			{
				valignment = alignment;
				makeText();
			}
		}
	}

	public void setTextMarginLeft(float _left)
	{
		leftBorder = _left;
		calcInnerRec();
		makeText();
	}

	public void setTextMarginBottom(float _bottom)
	{
		bottomBorder = _bottom;
		calcInnerRec();
		makeText();
	}

	public void setTextMarginTop(float _top)
	{
		topBorder = _top;
		calcInnerRec();
		makeText();
	}

	public void setTextMarginRight(float _right)
	{
		rightBorder = _right;
		calcInnerRec();
		makeText();
	}

	public void setTextMargin(float value)
	{
		rightBorder = leftBorder = topBorder = bottomBorder = value;
		calcInnerRec();
		makeText();
	}

	public void setTextColor(Color color)
	{
		if (color != null)
		{
			if (color != mColor)
			{
				mColor = color;
				makeText();
			}
		}
	}

	public String getText()
	{
		return text;
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{
		initLabel();
		makeText();
	}

	@Override
	public void setWidth(float Width)
	{
		super.setWidth(Width);
		calcInnerRec();
		if (text.length() > 0) makeText();
	}

	@Override
	public void setHeight(float Height)
	{
		super.setHeight(Height);
		calcInnerRec();
		if (text.length() > 0) makeText();
	}

	public int getLineCount()
	{
		if (bounds == null) return 0;
		lineCount = (int) (bounds.height / mFont.getCapHeight());
		return lineCount;
	}

	public BitmapFont getFont()
	{
		return mFont;
	}

	/**
	 * setzt die Höhe des Labels auf die gemessene Höhe!
	 */
	public void setMeasuredHeight()
	{
		// float m = mFont.getCapHeight() + mFont.getAscent() - mFont.getDescent();
		// compare to mFont.getLineHeight()
		// float h = m * (bounds.height / mFont.getCapHeight()) * 1.1f;
		// setHeight(h);
		if (bounds != null)
		{
			setHeight(bounds.height + mFont.getAscent() - mFont.getDescent());
		}
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		calcInnerRec();
		makeText();
	}

	@Override
	public void setBackground(Drawable background)
	{
		super.setBackground(background);
		calcInnerRec();
		makeText();
	}

	@Override
	public void dispose()
	{
		TextObject = null;
		valignment = null;
		halignment = null;
		text = null;
		bounds = null;
	}

}
