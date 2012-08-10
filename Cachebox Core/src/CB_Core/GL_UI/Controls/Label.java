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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Label extends CB_View_Base
{
	public BitmapFontCache cache;
	public VAlignment valignment = VAlignment.CENTER;
	public String text;
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

	private void calcInnerRec()
	{
		innerRec = new CB_RectF(left, bottom, width - right - left, height - top - bottom);
		innerWidth = innerRec.getWidth();
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (cache != null) cache.draw(batch);

	}

	private void fontPropertyChanged()
	{

		if (bounds == null) return;

		calcInnerRec();
		float x = innerRec.getX();

		if (this.halignment == HAlignment.CENTER && innerRec.getWidth() > bounds.width)
		{
			x = (innerRec.getWidth() / 2f) - (bounds.width / 2f);
		}
		else if (this.halignment == HAlignment.RIGHT && innerRec.getWidth() > bounds.width)
		{
			x = innerRec.getWidth() - bounds.width;
		}

		if (wrapType == WrapType.singleLine)
		{
			switch (valignment)
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
			switch (valignment)
			{
			case TOP:
				cache.setPosition(cache.getX(), (bounds.height + cache.getFont().getDescent()) - 1);
				break;
			case CENTER:
				cache.setPosition(cache.getX(), (innerRec.getHeight() - bounds.height) / 2);
				break;
			case BOTTOM:
				cache.setPosition(cache.getX(), 0);
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
		chkCache();
		this.text = text;
		wrapType = WrapType.singleLine;
		lineCount = 1;
		try
		{
			bounds = cache.setText(text, 0, cache.getFont().isFlipped() ? 0 : cache.getFont().getCapHeight());
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
		cache.setMultiLineText(text, 0, cache.getFont().isFlipped() ? 0 : bounds.height, innerWidth, alignment);
		fontPropertyChanged();
	}

	public void setWrappedText(String text)
	{
		setWrappedText(text, HAlignment.LEFT);
	}

	public void setWrappedText(String text, HAlignment alignment)
	{
		chkCache();
		this.text = text;
		valignment = VAlignment.TOP;
		this.halignment = alignment;
		wrapType = WrapType.wrapped;
		bounds = cache.getFont().getWrappedBounds(text, innerWidth);
		cache.setWrappedText(text, 0, cache.getFont().isFlipped() ? bounds.height : cache.getFont().getLineHeight(), innerWidth, alignment);
		fontPropertyChanged();
	}

	public void setFont(BitmapFont font)
	{
		mBmpFont = font;

		if (cache == null) initialedLineCount = 0;

		cache = new BitmapFontCache(mBmpFont);

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
		// TODO Auto-generated method stub

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

}
