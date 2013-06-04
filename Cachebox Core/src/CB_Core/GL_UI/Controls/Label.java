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

	private String mText = "";
	private BitmapFont mFont;
	private Color mColor;
	private VAlignment mVAlignment = VAlignment.CENTER;
	private HAlignment mHAlignment = HAlignment.LEFT;

	private TextBounds bounds;

	private WrapType wrapType = WrapType.singleLine;

	public Label()
	{
		super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label");
		initLabel();
	}

	public Label(String Text)
	{
		super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label " + Text);
		mText = Text;
		initLabel();
	}

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

	private void initLabel()
	{
		mFont = Fonts.getNormal();
		mColor = Fonts.getFontColor();
		TextObject = new BitmapFontCache(mFont, false);
		TextObject.setColor(mColor);
		bounds = TextObject.setText(mText, 0, mFont.getCapHeight());
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
		bounds = mFont.getBounds(mText);
		try
		{
			bounds = TextObject.setText(mText, 0, bounds.height);
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			Logger.DEBUG(this + " (" + wrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + bounds.width + "," + bounds.height + " \""
					+ mText + "\"");
		}
		setTextPosition();
	}

	private void setMultiLineText()
	{
		wrapType = WrapType.multiLine;
		makeTextObject();
		bounds = mFont.getMultiLineBounds(mText);
		try
		{
			bounds = TextObject.setMultiLineText(mText, 0, bounds.height, bounds.width, mHAlignment);
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			Logger.DEBUG(this + " (" + wrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + bounds.width + "," + bounds.height + " \""
					+ mText + "\"");
		}
		setTextPosition();
	}

	private void setWrappedText()
	{
		wrapType = WrapType.wrapped;
		makeTextObject();
		bounds = mFont.getWrappedBounds(mText, innerWidth);
		try
		{
			bounds = TextObject.setWrappedText(mText, 0, bounds.height, bounds.width, mHAlignment);
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			Logger.DEBUG(this + " (" + wrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + bounds.width + "," + bounds.height + " \""
					+ mText + "\"");
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
		if (innerWidth > bounds.width)
		{
			if (mHAlignment == HAlignment.CENTER)
			{
				xPosition = (innerWidth - bounds.width) / 2f;
			}
			else if (mHAlignment == HAlignment.RIGHT)
			{
				xPosition = innerWidth - bounds.width;
			}
		}
		float yPosition = 0; // VAlignment.BOTTOM
		switch (mVAlignment)
		{
		case TOP:
			yPosition = innerHeight - bounds.height - mFont.getAscent();
			break;
		case CENTER:
			yPosition = (innerHeight - bounds.height - mFont.getAscent()) / 2f;
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

	public Label setText(String text)
	{
		return setText(text, null, null, null);
	}

	public Label setText(String Text, Color color)
	{
		return setText(Text, null, color, null);
	}

	public Label setText(String text, BitmapFont Font, Color fontColor)
	{
		return setText(text, Font, fontColor, null);
	}

	public Label setText(String text, BitmapFont Font, Color fontColor, HAlignment HAlignment)
	{
		if (text == null) text = "";
		mText = text;
		if (Font != null) mFont = Font;
		if (fontColor != null) mColor = fontColor;
		if (HAlignment != null) mHAlignment = HAlignment;
		setText();
		return this;
	}

	public Label setMultiLineText(String text)
	{
		return setMultiLineText(text, null, null, HAlignment.LEFT, VAlignment.TOP);
	}

	public Label setMultiLineText(String text, HAlignment HAlignment)
	{
		return setMultiLineText(text, null, null, HAlignment, VAlignment.TOP);
	}

	public Label setMultiLineText(String text, BitmapFont Font, Color fontColor, HAlignment HAlignment, VAlignment VAlignment)
	{
		if (text == null) text = "";
		mText = text;
		if (Font != null) mFont = Font;
		if (fontColor != null) mColor = fontColor;
		if (HAlignment != null) mHAlignment = HAlignment;
		if (VAlignment != null) mVAlignment = VAlignment;
		setMultiLineText();
		return this;
	}

	public Label setWrappedText(String text)
	{
		return setWrappedText(text, null, null, HAlignment.LEFT);
	}

	public Label setWrappedText(String text, BitmapFont Font)
	{
		return setWrappedText(text, Font, null, null);
	}

	public Label setWrappedText(String text, HAlignment HAlignment)
	{
		return setWrappedText(text, null, null, HAlignment);
	}

	public Label setWrappedText(String text, BitmapFont Font, Color fontColor, HAlignment HAlignment)
	{
		if (text == null) text = "";
		mText = text;
		if (Font != null) mFont = Font;
		if (fontColor != null) mColor = fontColor;
		if (HAlignment != null) mHAlignment = HAlignment;
		mVAlignment = VAlignment.TOP;
		setWrappedText();
		return this;
	}

	public Label setFont(BitmapFont Font)
	{
		if (Font != null)
		{
			if (Font != mFont)
			{
				mFont = Font;
				makeText();
			}
		}
		return this;
	}

	public Label setHAlignment(HAlignment HAlignment)
	{
		if (HAlignment != null)
		{
			if (mHAlignment != HAlignment)
			{
				mHAlignment = HAlignment;
				makeText();
			}
		}
		return this;
	}

	public Label setVAlignment(VAlignment VAlignment)
	{
		if (VAlignment != null)
		{
			if (mVAlignment != VAlignment)
			{
				mVAlignment = VAlignment;
				makeText();
			}
		}
		return this;
	}

	public Label setTextColor(Color color)
	{
		if (color != null)
		{
			if (color != mColor)
			{
				mColor = color;
				makeText();
			}
		}
		return this;
	}

	public String getText()
	{
		return mText;
	}

	public int getLineCount()
	{
		if (bounds == null) return 0;
		return (int) (bounds.height / mFont.getCapHeight());
	}

	public BitmapFont getFont()
	{
		return mFont;
	}

	public float getTextHeight()
	{
		if (bounds != null)
		{
			return bounds.height + mFont.getAscent() - mFont.getDescent();
		}
		return 0f;
	}

	public float getTextWidth()
	{
		if (bounds != null)
		{
			return bounds.width;
		}
		return 0f;
	}

	@Override
	protected void Initial()
	{
		// must implement Initial
	}

	@Override
	protected void SkinIsChanged()
	{
		initLabel();
		makeText();
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		// wird automatisch aufgerufen,
		// wenn setWidth oder setHeight, ...
		makeText();
	}

	@Override
	public void setBackground(Drawable background)
	{
		super.setBackground(background);
		makeText();
	}

	@Override
	public void dispose()
	{
		TextObject = null;
		mVAlignment = null;
		mHAlignment = null;
		mText = null;
		bounds = null;
	}

}
