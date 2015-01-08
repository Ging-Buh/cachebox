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

import org.slf4j.LoggerFactory;

import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Label extends CB_View_Base
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Label.class);

	static public enum VAlignment
	{
		TOP, CENTER, BOTTOM
	}

	static public enum HAlignment
	{
		LEFT, CENTER, RIGHT, SCROLL_LEFT, SCROLL_CENTER, SCROLL_RIGHT
	}

	public static com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment GDX_HAlignment(HAlignment ali)
	{
		switch (ali)
		{
		case CENTER:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.CENTER;
		case LEFT:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.LEFT;
		case RIGHT:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.RIGHT;
		case SCROLL_CENTER:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.CENTER;
		case SCROLL_LEFT:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.LEFT;
		case SCROLL_RIGHT:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.RIGHT;
		default:
			return com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment.LEFT;

		}
	}

	BitmapFontCache TextObject; // FIXME Create BitmapFontCache-Array and reduce PolygonSpriteBatch(10920) constructor for Labels with long
								// Text

	private String mText = "";
	private BitmapFont mFont = Fonts.getNormal();
	private Color mColor = COLOR.getFontColor();
	private VAlignment mVAlignment = VAlignment.CENTER;
	private HAlignment mHAlignment = HAlignment.LEFT;
	private WrapType mWrapType = WrapType.SINGLELINE;
	private int ErrorCount = 0;

	TextBounds bounds;

	/**
	 * object for holding Text. default size is ButtonWidthWide x ButtonHeight from UI_Size_Base
	 **/
	public Label()
	{
		super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label");
		initLabel();
	}

	/**
	 * object for holding Text. default size is ButtonWidthWide x ButtonHeight from UI_Size_Base
	 **/
	public Label(String Text)
	{
		super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label " + Text);
		mText = Text == null ? "" : Text;
		initLabel();
	}

	/**
	 * object for holding Text. default size is ButtonWidthWide x ButtonHeight from UI_Size_Base
	 **/
	public Label(String Text, BitmapFont Font, Color fontColor, WrapType WrapType)
	{
		super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Label " + Text);
		mText = (Text == null ? "" : Text);
		if (Font != null) mFont = Font;
		if (fontColor != null) mColor = fontColor;
		if (WrapType != null) mWrapType = WrapType;
		initLabel();
	}

	public Label(float X, float Y, float Width, float Height, String Text)
	{
		super(X, Y, Width, Height, "Label " + Text);
		mText = Text == null ? "" : Text;
		initLabel();
	}

	public Label(CB_RectF rec, String Text)
	{
		super(rec, "Label " + Text);
		mText = Text == null ? "" : Text;
		initLabel();
	}

	private void initLabel()
	{
		TextObject = new BitmapFontCache(mFont, false);
		TextObject.setColor(mColor);
		makeText();
	}

	final int ScrollPause = 120;
	int test = 0;
	boolean left = true;
	int scrollPauseCount = 0;

	@Override
	protected void render(Batch batch)
	{

		try
		{
			if (TextObject != null) TextObject.draw(batch);
			if (mHAlignment == HAlignment.SCROLL_CENTER || mHAlignment == HAlignment.SCROLL_LEFT || mHAlignment == HAlignment.SCROLL_RIGHT)
			{
				int max = (int) (bounds.width - innerWidth) + 20;

				if (left)
				{
					scrollPauseCount++;
					if (scrollPauseCount > ScrollPause)
					{
						test--;
						if (test < -max)
						{
							left = false;
							scrollPauseCount = 0;
						}
					}

				}
				else
				{

					scrollPauseCount++;
					if (scrollPauseCount > ScrollPause)
					{

						scrollPauseCount = test = 0;
						left = true;
					}
				}

				setTextPosition();
				GL.that.renderOnce();
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// kommt manchmal wenn der Text ge�ndert wird
			makeText();
		}
		catch (NullPointerException e)
		{
			// kommt manchmal wenn der Text ge�ndert wird
			makeText();
		}
	}

	private void setText()
	{
		mWrapType = WrapType.SINGLELINE;
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
			log.debug(this + " (" + mWrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + bounds.width + "," + bounds.height + " \"" + mText + "\"");
		}
		setTextPosition();
	}

	private void setMultiLineText()
	{
		mWrapType = WrapType.MULTILINE;
		makeTextObject();
		bounds = mFont.getMultiLineBounds(mText);
		try
		{
			bounds = TextObject.setMultiLineText(mText, 0, bounds.height, bounds.width, GDX_HAlignment(mHAlignment));
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			log.debug(this + " (" + mWrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + bounds.width + "," + bounds.height + " \"" + mText + "\"");
		}
		setTextPosition();
	}

	private void setWrappedText()
	{
		mWrapType = WrapType.WRAPPED;
		makeTextObject();
		bounds = mFont.getWrappedBounds(mText, innerWidth);
		try
		{
			bounds = TextObject.setWrappedText(mText, 0, bounds.height, bounds.width, GDX_HAlignment(mHAlignment));
		}
		catch (Exception e)
		{
			// java.lang.ArrayIndexOutOfBoundsException kommt mal vor
			e.printStackTrace();
			log.debug(this + " (" + mWrapType + "/" + mHAlignment + "/" + mVAlignment + ") " + bounds.width + "," + bounds.height + " \"" + mText + "\"");
		}
		setTextPosition();
	}

	private void makeTextObject()
	{
		if (TextObject == null)
		{
			TextObject = new BitmapFontCache(mFont, false);
		}
		else if (!TextObject.getFont().equals(mFont))
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
		float xPosition = leftBorder + 1; // HAlignment.LEFT !!! Die 1 ist empirisch begr�ndet
		if (innerWidth > bounds.width)
		{
			if (mHAlignment == HAlignment.CENTER || mHAlignment == HAlignment.SCROLL_CENTER)
			{
				xPosition = (innerWidth - bounds.width) / 2f;
			}
			else if (mHAlignment == HAlignment.RIGHT || mHAlignment == HAlignment.SCROLL_RIGHT)
			{
				xPosition = innerWidth - bounds.width;
			}
		}
		else if (mHAlignment == HAlignment.SCROLL_CENTER || mHAlignment == HAlignment.SCROLL_LEFT || mHAlignment == HAlignment.SCROLL_RIGHT)
		{
			xPosition += test;
		}

		float yPosition = 0; // VAlignment.BOTTOM

		if (mVAlignment == null) mVAlignment = VAlignment.CENTER;
		switch (mVAlignment)
		{
		case TOP:
			yPosition = innerHeight - bounds.height - mFont.getAscent();
			break;
		case CENTER:
			yPosition = (innerHeight - bounds.height - mFont.getAscent()) / 2f;
			break;
		case BOTTOM:
			// TODO implement
			break;

		}

		try
		{
			TextObject.setPosition(xPosition, yPosition);
			ErrorCount = 0;
		}
		catch (Exception e)
		{
			// Try again
			ErrorCount++;
			if (ErrorCount < 5) GL.that.RunOnGL(new IRunOnGL()
			{
				@Override
				public void run()
				{
					setTextPosition();
				}
			});
		}
	}

	private void makeText()
	{
		switch (mWrapType)
		{
		case SINGLELINE:
			setText();
			break;
		case MULTILINE:
			setMultiLineText();
			break;
		case WRAPPED:
			setWrappedText();
			break;
		}
	}

	/**
	 * setting the Text, depending on WrapType, ...
	 **/
	public Label setText(String text)
	{
		if (text == null) text = "";
		mText = text;
		makeText();
		return this;
	}

	/**
	 * setting the Text. new line is GlobalCore.br
	 **/
	public Label setMultiLineText(String text)
	{
		if (text == null) text = "";
		mText = text;
		mVAlignment = VAlignment.TOP;
		setMultiLineText();
		return this;
	}

	/**
	 * setting the Text. new Line wrap determined by width
	 **/
	public Label setWrappedText(String text)
	{
		if (text == null) text = "";
		mText = text;
		mVAlignment = VAlignment.TOP;
		setWrappedText();
		return this;
	}

	public Label setWrapType(WrapType WrapType)
	{
		if (WrapType != null)
		{
			if (WrapType != mWrapType)
			{
				mWrapType = WrapType;
				makeText();
			}
		}
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
		// todo den korrekten Font (original Fontgr�sse nicht bekannt) setzen
		mFont = Fonts.getNormal();
		mColor = COLOR.getFontColor();
		initLabel();
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
