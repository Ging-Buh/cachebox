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
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Label extends CB_View_Base
{
	public BitmapFontCache cache;
	public VAlignment valignment = VAlignment.CENTER;
	public String text = "";
	public TextBounds bounds;
	private CB_RectF innerRec;

	private float left = 0;
	private float right = 0;
	private float bottom = 0;
	private float top = 0;

	private WrapType wrapType = WrapType.singleLine;
	private int lineCount = 1;
	private HAlignment halignment = HAlignment.LEFT;
	private BitmapFont mBmpFont;
	private int initialedLineCount = 0;

	/**
	 * Dieser Wert entspricht width*0.95. Damit ein TextWrap richtig dargestellt wird.
	 */
	private float innerWidth;

	public final Color color = new Color(1, 1, 1, 1);

	public Label(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		calcInnerRec();
	}

	public Label(CB_RectF rec, String Name)
	{
		super(rec, Name);
		calcInnerRec();
	}

	public Label(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec, Parent, Name);
		calcInnerRec();
	}

	public Label(String name)
	{

		super(new CB_RectF(0, 0, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()), name);
		this.isClickable = true;

	}

	private void calcInnerRec()
	{

		left = drawableBackground != null ? drawableBackground.getLeftWidth() : 0;
		right = drawableBackground != null ? drawableBackground.getRightWidth() : 0;
		top = drawableBackground != null ? drawableBackground.getTopHeight() : 0;
		bottom = drawableBackground != null ? drawableBackground.getBottomHeight() : 0;

		innerRec = new CB_RectF(left, bottom, width - right - left, height - top - bottom);
		innerWidth = innerRec.getWidth();
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		try
		{
			if (cache != null) cache.draw(batch);
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

	// TODO Center bei Wraped und Multi Line passt Hier irgendwie nicht mehr

	private void fontPropertyChanged()
	{

		if (bounds == null) return;

		calcInnerRec();
		float x = innerRec.getX();

		if (this.halignment == HAlignment.CENTER && innerRec.getWidth() > bounds.width)
		{
			if (wrapType == WrapType.singleLine)
			{
				x = (innerRec.getHalfWidth()) - (bounds.width / 2f);
			}
		}
		else if (this.halignment == HAlignment.RIGHT && innerRec.getWidth() > bounds.width)
		{
			x = innerRec.getWidth() - bounds.width;
		}

		VAlignment switchValue = ((valignment != null) ? valignment : VAlignment.CENTER);

		if (wrapType == WrapType.singleLine)
		{

			switch (switchValue)
			{
			case TOP:
				cache.setPosition(x, innerRec.getHeight() - bounds.height);
				break;
			case CENTER:
				cache.setPosition(x, (innerRec.getHeight() - bounds.height) / 2);
				break;
			case BOTTOM:
				cache.setPosition(x, 0);
				break;
			}
		}
		else
		{
			switch (switchValue)
			{
			case TOP:
				cache.setPosition(x, (innerRec.getHeight() - bounds.height) + bottom);
				break;
			case CENTER:
				cache.setPosition(x, ((innerRec.getHeight() - bounds.height) / 2) + bottom);
				break;
			case BOTTOM:
				cache.setPosition(x, 0);
				break;
			}
		}

		lineCount = (int) (bounds.height / cache.getFont().getCapHeight());
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		fontPropertyChanged();
	}

	private void chkCache()
	{
		// Initial mit Arial 18
		if (cache == null) setFont(Fonts.getNormal());
	}

	// code from Libgdx Label

	public void setText(String txt, HAlignment aligment)
	{
		setText(txt);
		setHAlignment(aligment);
	}

	public TextBounds setText(String text)
	{
		return setText(text, null, null);
	}

	public TextBounds setText(String text, BitmapFont font, Color fontColor)
	{
		if (text == null) throw new IllegalArgumentException("text cannot be null.");

		if (font != null) setFont(font);
		if (fontColor != null) setTextColor(fontColor);
		chkCache();
		this.text = text;
		wrapType = WrapType.singleLine;
		lineCount = 1;
		try
		{
			bounds = cache.setText(this.text, 0, cache.getFont().getCapHeight());
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}

		fontPropertyChanged();

		return bounds;
	}

	public void setMultiLineText(String text)
	{
		setMultiLineText(text, HAlignment.LEFT);
	}

	public void setMultiLineText(String text, HAlignment alignment)
	{
		chkCache();
		this.text = text;
		valignment = VAlignment.TOP;
		wrapType = WrapType.multiLine;

		bounds = cache.getFont().getMultiLineBounds(text);
		// cache.setMultiLineText(text, 0, cache.getFont().isFlipped() ? 0 : bounds.height);
		bounds = cache.setMultiLineText(this.text, 0, bounds.height, innerWidth, alignment);
		fontPropertyChanged();
	}

	public TextBounds setWrappedText(String text)
	{
		return setWrappedText(text, HAlignment.LEFT);
	}

	public TextBounds setWrappedText(String text, HAlignment alignment)
	{
		chkCache();
		this.text = text;
		valignment = VAlignment.TOP;
		this.halignment = alignment;
		wrapType = WrapType.wrapped;
		bounds = cache.getFont().getWrappedBounds(text, innerWidth);
		bounds = cache.setWrappedText(this.text, 0, bounds.height, innerWidth, alignment);
		fontPropertyChanged();

		return bounds;
	}

	public void setFont(BitmapFont font)
	{
		mBmpFont = font;

		if (cache == null) initialedLineCount = 0;

		cache = new BitmapFontCache(mBmpFont);
		cache.setColor(Fonts.getFontColor());

		if (lineCount != initialedLineCount) return;
		change();
		fontPropertyChanged();
		initialedLineCount = lineCount;
	}

	private void change()
	{
		calcInnerRec();

		switch (wrapType)
		{
		case singleLine:
			setText(text);
			break;
		case multiLine:
			setMultiLineText(text, halignment);
			break;
		case wrapped:
			setWrappedText(text, halignment);
			break;
		}

	}

	public void setHAlignment(HAlignment aligment)
	{
		this.halignment = aligment;
		change();
	}

	public void setVAlignment(VAlignment aligment)
	{
		if (aligment == null) return;
		this.valignment = aligment;
		change();
	}

	static public enum VAlignment
	{
		TOP, CENTER, BOTTOM
	}

	static private enum WrapType
	{
		singleLine, multiLine, wrapped
	}

	public void setTextMarginLeft(float left)
	{
		this.left = left;
		change();
		fontPropertyChanged();
	}

	public void setTextMarginBottom(float bottom)
	{
		this.bottom = bottom;
		change();
		fontPropertyChanged();
	}

	public void setTextMarginTop(float top)
	{
		this.top = top;
		change();
		fontPropertyChanged();
	}

	public void setTextMarginRight(float right)
	{
		this.right = right;
		change();
		fontPropertyChanged();
	}

	public void setTextMargin(float value)
	{
		right = left = top = bottom = value;
		change();
		fontPropertyChanged();
	}

	public void setScale(float scaleFactor)
	{
		cache.getFont().setScale(scaleFactor);
	}

	@Override
	protected void Initial()
	{

	}

	public void setTextColor(Color color)
	{
		if (cache != null) cache.setColor(color);
	}

	public String getText()
	{
		return this.text;
	}

	@Override
	protected void SkinIsChanged()
	{
		cache = null;
		chkCache();
	}

	@Override
	public void setWidth(float Width)
	{
		super.setWidth(Width);
		change();
		fontPropertyChanged();
	}

	@Override
	public void setHeight(float Height)
	{
		super.setHeight(Height);
		change();
		fontPropertyChanged();
	}

	public int getLineCount()
	{
		if (cache == null) return 0;
		return lineCount;
	}

	public BitmapFont getFont()
	{
		return cache.getFont();
	}

	/**
	 * setzt die Höhe des Labels auf die gemessene Höhe!
	 */
	public void setMeasuredHeight()
	{
		float m = cache.getFont().getLineHeight() + (cache.getFont().getAscent() * 2);

		float h = m * lineCount * 1.1f;

		// h += (cache.getFont().getDescent());

		this.setHeight(h);

	}

	@Override
	public void setBackground(Drawable background)
	{
		super.setBackground(background);
		fontPropertyChanged();
	}

	@Override
	public void dispose()
	{
		cache = null;
		valignment = null;
		text = null;
		bounds = null;
		innerRec = null;
		wrapType = null;
		halignment = null;
		mBmpFont.dispose();
	}

}
