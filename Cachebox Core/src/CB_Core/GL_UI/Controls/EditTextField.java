package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.utils.Clipboard;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.TimeUtils;

public class EditTextField extends EditTextFieldBase
{
	static protected final char BACKSPACE = 8;
	static protected final char ENTER_DESKTOP = '\r';
	static protected final char ENTER_ANDROID = '\n';
	static protected final char TAB = '\t';
	static protected final char DELETE = 127;
	static protected final char BULLET = 149;

	protected final float x = 0;
	protected final float y = 0;

	protected TextFieldStyle style;
	protected String text, messageText;
	protected CharSequence displayText;
	protected int cursor;
	protected Clipboard clipboard;

	protected OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

	protected boolean passwordMode;
	protected StringBuilder passwordBuffer;

	protected final Rectangle fieldBounds = new Rectangle();
	protected final TextBounds textBounds = new TextBounds();
	protected final Rectangle scissor = new Rectangle();
	protected float renderOffset, textOffset;
	protected int visibleTextStart, visibleTextEnd;
	protected final FloatArray glyphAdvances = new FloatArray();
	protected final FloatArray glyphPositions = new FloatArray();

	protected boolean cursorOn = true;
	protected float blinkTime = 0.42f;
	protected long lastBlink;

	protected boolean hasSelection;
	protected int selectionStart;
	protected float selectionX, selectionWidth;

	protected char passwordCharacter = BULLET;

	public EditTextField(CB_RectF rec, TextFieldStyle style, String Name)
	{
		super(rec, Name);
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		setText("");
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	public void setStyle(TextFieldStyle style)
	{
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		// invalidateHierarchy();
	}

	public void setPasswordCharacter(char passwordCharacter)
	{
		this.passwordCharacter = passwordCharacter;
	}

	/**
	 * Returns the text field's style. Modifying the returned style may not have an effect until {@link #setStyle(TextFieldStyle)} is
	 * called.
	 */
	public TextFieldStyle getStyle()
	{
		return style;
	}

	private void calculateOffsets()
	{
		float position = glyphPositions.get(cursor);
		float distance = position - Math.abs(renderOffset);
		float visibleWidth = width;
		if (style.background != null) visibleWidth -= style.background.getLeftWidth() + style.background.getRightWidth();

		// check whether the cursor left the left or right side of
		// the visible area and adjust renderoffset.
		if (distance <= 0)
		{
			if (cursor > 0) renderOffset = -glyphPositions.get(cursor - 1);
			else
				renderOffset = 0;
		}
		else
		{
			if (distance > visibleWidth)
			{
				renderOffset -= distance - visibleWidth;
			}
		}

		// calculate first visible char based on render offset
		visibleTextStart = 0;
		textOffset = 0;
		float start = Math.abs(renderOffset);
		int len = glyphPositions.size;
		float startPos = 0;
		for (int i = 0; i < len; i++)
		{
			if (glyphPositions.items[i] >= start)
			{
				visibleTextStart = i;
				startPos = glyphPositions.items[i];
				textOffset = glyphPositions.items[visibleTextStart] - start;
				break;
			}
		}

		// calculate last visible char based on visible width and render offset
		visibleTextEnd = Math.min(displayText.length(), cursor + 1);
		for (; visibleTextEnd <= displayText.length(); visibleTextEnd++)
		{
			if (glyphPositions.items[visibleTextEnd] - startPos > visibleWidth) break;
		}
		visibleTextEnd = Math.max(0, visibleTextEnd - 1);

		// calculate selection x position and width
		if (hasSelection)
		{
			int minIndex = Math.min(cursor, selectionStart);
			int maxIndex = Math.max(cursor, selectionStart);
			float minX = Math.max(glyphPositions.get(minIndex), glyphPositions.get(visibleTextStart));
			float maxX = Math.min(glyphPositions.get(maxIndex), glyphPositions.get(visibleTextEnd));
			selectionX = minX;
			selectionWidth = maxX - minX;
		}
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		final BitmapFont font = style.font;
		final Color fontColor = style.fontColor;
		final TextureRegion selection = style.selection;
		final NinePatch cursorPatch = style.cursor;

		float bgLeftWidth = 0;
		boolean focused = GL_Listener.hasFocus(this);

		if (focused)
		{
			if (style.backgroundFocused != null)
			{
				style.backgroundFocused.draw(batch, x, y, width, height);
				bgLeftWidth = style.backgroundFocused.getLeftWidth();
			}
		}
		else
		{
			if (style.background != null)
			{
				style.background.draw(batch, x, y, width, height);
				bgLeftWidth = style.background.getLeftWidth();
			}
		}

		float textY = (int) (height / 2 + textBounds.height / 2 + font.getDescent());
		calculateOffsets();

		if (focused && hasSelection && selection != null)
		{
			batch.draw(selection, x + selectionX + bgLeftWidth + renderOffset, y + textY - textBounds.height - font.getDescent() / 2,
					selectionWidth, textBounds.height);
		}

		if (displayText.length() == 0)
		{
			if (!focused && messageText != null)
			{
				if (style.messageFontColor != null)
				{
					font.setColor(style.messageFontColor.r, style.messageFontColor.g, style.messageFontColor.b, style.messageFontColor.a);
				}
				else
					font.setColor(0.7f, 0.7f, 0.7f, 1f);
				BitmapFont messageFont = style.messageFont != null ? style.messageFont : font;
				font.draw(batch, messageText, x + bgLeftWidth, y + textY);
			}
		}
		else
		{
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a);
			font.draw(batch, displayText, x + bgLeftWidth + textOffset, y + textY, visibleTextStart, visibleTextEnd);
		}
		if (focused)
		{
			blink();
			if (cursorOn && cursorPatch != null)
			{
				cursorPatch.draw(batch, x + bgLeftWidth + glyphPositions.get(cursor) + renderOffset - 1, y + textY - textBounds.height
						- font.getDescent(), cursorPatch.getTotalWidth(), textBounds.height + font.getDescent() / 2);
			}
		}
	}

	private void updateDisplayText()
	{
		if (passwordMode && style.font.containsCharacter(passwordCharacter))
		{
			if (passwordBuffer == null) passwordBuffer = new StringBuilder(text.length());
			if (passwordBuffer.length() > text.length()) //
			passwordBuffer.setLength(text.length());
			else
			{
				for (int i = passwordBuffer.length(), n = text.length(); i < n; i++)
					passwordBuffer.append(passwordCharacter);
			}
			displayText = passwordBuffer;
		}
		else
			displayText = text;
		style.font.computeGlyphAdvancesAndPositions(displayText, glyphAdvances, glyphPositions);
	}

	private void blink()
	{
		long time = TimeUtils.nanoTime();
		if ((time - lastBlink) / 1000000000.0f > blinkTime)
		{
			cursorOn = !cursorOn;
			lastBlink = time;
		}
	}

	@Override
	public boolean onTouchDown(int X, int Y, int pointer, int button)
	{
		float x = (float) X;
		float y = (float) Y;

		if (pointer != 0) return false;
		GL_Listener.setKeyboardFocus(this);
		keyboard.show(true);
		clearSelection();
		lastBlink = 0;
		cursorOn = false;
		x = x - renderOffset;
		for (int i = 0; i < glyphPositions.size; i++)
		{
			float pos = glyphPositions.items[i];
			if (pos > x)
			{
				cursor = Math.max(0, i - 1);
				return true;
			}
		}
		cursor = Math.max(0, glyphPositions.size - 1);
		return true;
	}

	public boolean keyDown(int keycode)
	{
		final BitmapFont font = style.font;

		if (GL_Listener.hasFocus(this))
		{
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
			{
				// paste
				if (keycode == Keys.V) paste();
				// copy
				if (keycode == Keys.C || keycode == Keys.INSERT) copy();
			}
			else if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
			{
				// paste
				if (keycode == Keys.INSERT) paste();
				// cut
				if (keycode == Keys.FORWARD_DEL)
				{
					if (hasSelection)
					{
						copy();
						delete();
					}
				}
				// selection
				if (keycode == Keys.LEFT)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor--;
				}
				if (keycode == Keys.RIGHT)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor++;
				}
				if (keycode == Keys.HOME)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor = 0;
				}
				if (keycode == Keys.END)
				{
					if (!hasSelection)
					{
						selectionStart = cursor;
						hasSelection = true;
					}
					cursor = text.length();
				}

				cursor = Math.max(0, cursor);
				cursor = Math.min(text.length(), cursor);
			}
			else
			{
				// cursor movement or other keys (kill selection)
				if (keycode == Keys.LEFT)
				{
					cursor--;
					clearSelection();
				}
				if (keycode == Keys.RIGHT)
				{
					cursor++;
					clearSelection();
				}
				if (keycode == Keys.HOME)
				{
					cursor = 0;
					clearSelection();
				}
				if (keycode == Keys.END)
				{
					cursor = text.length();
					clearSelection();
				}

				cursor = Math.max(0, cursor);
				cursor = Math.min(text.length(), cursor);
			}

			return true;
		}
		return false;
	}

	/**
	 * Copies the contents of this TextField to the {@link Clipboard} implementation set on this TextField.
	 */
	public void copyToClipboard()
	{
		if (hasSelection && clipboard != null)
		{
			int minIndex = Math.min(cursor, selectionStart);
			int maxIndex = Math.max(cursor, selectionStart);
			clipboard.setContents(text.substring(minIndex, maxIndex));
		}
	}

	/**
	 * Pastes the content of the {@link Clipboard} implementation set on this Textfield to this TextField.
	 */
	public void paste()
	{
		if (clipboard == null) return;

		String content = clipboard.getContents();
		if (content != null)
		{
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < content.length(); i++)
			{
				char c = content.charAt(i);
				if (style.font.containsCharacter(c)) builder.append(c);
			}
			content = builder.toString();

			if (!hasSelection)
			{
				text = text.substring(0, cursor) + content + text.substring(cursor, text.length());
				updateDisplayText();
				cursor += content.length();
			}
			else
			{
				int minIndex = Math.min(cursor, selectionStart);
				int maxIndex = Math.max(cursor, selectionStart);

				text = (minIndex > 0 ? text.substring(0, minIndex) : "")
						+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
				cursor = minIndex;
				text = text.substring(0, cursor) + content + text.substring(cursor, text.length());
				updateDisplayText();
				cursor = minIndex + content.length();
				clearSelection();
			}

		}
	}

	private void delete()
	{
		int minIndex = Math.min(cursor, selectionStart);
		int maxIndex = Math.max(cursor, selectionStart);
		text = (minIndex > 0 ? text.substring(0, minIndex) : "")
				+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
		updateDisplayText();
		cursor = minIndex;
		clearSelection();
	}

	public boolean keyTyped(char character)
	{
		final BitmapFont font = style.font;

		if (GL_Listener.hasFocus(this))
		{
			if (character == BACKSPACE && (cursor > 0 || hasSelection))
			{
				if (!hasSelection)
				{
					text = text.substring(0, cursor - 1) + text.substring(cursor);
					updateDisplayText();
					cursor--;
				}
				else
				{
					delete();
				}
			}
			if (character == DELETE)
			{
				if (cursor < text.length() || hasSelection)
				{
					if (!hasSelection)
					{
						text = text.substring(0, cursor) + text.substring(cursor + 1);
						updateDisplayText();
					}
					else
					{
						delete();
					}
				}
				return true;
			}
			if (character != ENTER_DESKTOP && character != ENTER_ANDROID)
			{
				if (filter != null && !filter.acceptChar(this, character)) return true;
			}

			{
				// Auskommentiert, weil hier das nächste LibGdx-TextField den Focus erhalten soll.

				// if (character == TAB || character == ENTER_ANDROID) next(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
				// || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT));

			}

			if (font.containsCharacter(character))
			{
				if (!hasSelection)
				{
					text = text.substring(0, cursor) + character + text.substring(cursor, text.length());
					updateDisplayText();
					cursor++;
				}
				else
				{
					int minIndex = Math.min(cursor, selectionStart);
					int maxIndex = Math.max(cursor, selectionStart);

					text = (minIndex > 0 ? text.substring(0, minIndex) : "")
							+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
					cursor = minIndex;
					text = text.substring(0, cursor) + character + text.substring(cursor, text.length());
					updateDisplayText();
					cursor++;
					clearSelection();
				}
			}
			if (listener != null) listener.keyTyped(this, character);
			return true;
		}
		else
			return false;
	}

	/**
	 * @param listener
	 *            May be null.
	 */
	public void setTextFieldListener(TextFieldListener listener)
	{
		this.listener = listener;
	}

	/**
	 * @param filter
	 *            May be null.
	 */
	public void setTextFieldFilter(TextFieldFilter filter)
	{
		this.filter = filter;
	}

	/** @return May be null. */
	public String getMessageText()
	{
		return messageText;
	}

	/**
	 * Sets the text that will be drawn in the text field if no text has been entered.
	 * 
	 * @parma messageText May be null.
	 */
	public void setMessageText(String messageText)
	{
		this.messageText = messageText;
	}

	public void setText(String text)
	{
		if (text == null) throw new IllegalArgumentException("text cannot be null.");

		BitmapFont font = style.font;

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (font.containsCharacter(c)) buffer.append(c);
		}

		String bText = buffer.toString();

		// replace lineBreaks
		this.text = bText.replace("\r\n", "\r");

		updateDisplayText();
		cursor = 0;
		clearSelection();

		textBounds.set(font.getBounds(displayText));
		textBounds.height -= font.getDescent() * 2;
		font.computeGlyphAdvancesAndPositions(displayText, glyphAdvances, glyphPositions);
	}

	/** @return Never null, might be an empty string. */
	public String getText()
	{
		return text;
	}

	/** Sets the selected text. */
	public void setSelection(int selectionStart, int selectionEnd)
	{
		if (selectionStart < 0) throw new IllegalArgumentException("selectionStart must be >= 0");
		if (selectionEnd < 0) throw new IllegalArgumentException("selectionEnd must be >= 0");
		selectionStart = Math.min(text.length(), selectionStart);
		selectionEnd = Math.min(text.length(), selectionEnd);
		if (selectionEnd == selectionStart)
		{
			clearSelection();
			return;
		}
		if (selectionEnd < selectionStart)
		{
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}

		hasSelection = true;
		this.selectionStart = selectionStart;
		cursor = selectionEnd;
	}

	public void clearSelection()
	{
		hasSelection = false;
	}

	/** Sets the cursor position and clears any selection. */
	public void setCursorPosition(int cursorPosition)
	{
		if (cursorPosition < 0) throw new IllegalArgumentException("cursorPosition must be >= 0");
		clearSelection();
		cursor = Math.min(cursorPosition, text.length());
	}

	public int getCursorPosition()
	{
		return cursor;
	}

	/** Default is an instance of {@link DefaultOnscreenKeyboard}. */
	public OnscreenKeyboard getOnscreenKeyboard()
	{
		return keyboard;
	}

	public void setOnscreenKeyboard(OnscreenKeyboard keyboard)
	{
		this.keyboard = keyboard;
	}

	public void setClipboard(Clipboard clipboard)
	{
		this.clipboard = clipboard;
	}

	@Override
	public boolean keyUp(int KeyCode)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
