package CB_Core.GL_UI.Controls;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Map.Point;
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
import com.badlogic.gdx.scenes.scene2d.utils.Clipboard;
import com.badlogic.gdx.utils.FloatArray;

public class EditWrapedTextField extends EditTextFieldBase
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
	protected ArrayList<DisplayText> displayText;
	protected int cursor;
	protected int cursorLine;
	protected float topLine;
	protected float maxLineCount; // Anzahl der darzustellenden Zeilen
	protected Clipboard clipboard;
	protected TextFieldListener listener;
	protected TextFieldFilter filter;
	protected OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

	protected boolean passwordMode;
	protected StringBuilder passwordBuffer;

	protected final Rectangle fieldBounds = new Rectangle();
	protected float textHeight;
	protected float lineHeight;
	protected final Rectangle scissor = new Rectangle();
	protected float renderOffset, textOffset;
	protected int visibleTextStart, visibleTextEnd;
	// protected final FloatArray glyphAdvances = new FloatArray();
	// protected final FloatArray glyphPositions = new FloatArray();

	protected boolean hasSelection;
	protected int selectionStart;
	protected float selectionX, selectionWidth;

	protected char passwordCharacter = BULLET;
	final Lock displayTextLock = new ReentrantLock();

	public EditWrapedTextField(CB_RectF rec, String Name)
	{
		super(rec, Name);
		this.style = getDefaultStyle();
		displayText = new ArrayList<EditWrapedTextField.DisplayText>();
		setCursorLine(0);
		lineHeight = style.font.getLineHeight();
		setText("");
		topLine = 0;
		this.isClickable = true;
	}

	public EditWrapedTextField(CB_RectF rec, TextFieldStyle style, String Name)
	{
		super(rec, Name);
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		displayText = new ArrayList<EditWrapedTextField.DisplayText>();
		setCursorLine(0);
		lineHeight = style.font.getLineHeight();
		setText("");
		topLine = 0;
		this.isClickable = true;
	}

	@Override
	public void onShow()
	{
		super.onShow();

	}

	@Override
	public void onHide()
	{
		super.onHide();
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
		if (true) return;
		// float position = glyphPositions.get(cursor);
		// float distance = position - Math.abs(renderOffset);
		// float visibleWidth = width;
		// if (style.background != null) visibleWidth -= style.background.getLeftWidth() + style.background.getRightWidth();

		// check whether the cursor left the left or right side of
		// the visible area and adjust renderoffset.
		// if (distance <= 0)
		// {
		// if (cursor > 0) renderOffset = -glyphPositions.get(cursor - 1);
		// else
		// renderOffset = 0;
		// }
		// else
		// {
		// if (distance > visibleWidth)
		// {
		// renderOffset -= distance - visibleWidth;
		// }
		// }
		//
		// // calculate first visible char based on render offset
		// visibleTextStart = 0;
		// textOffset = 0;
		// float start = Math.abs(renderOffset);
		// int len = glyphPositions.size;
		// float startPos = 0;
		// for (int i = 0; i < len; i++)
		// {
		// if (glyphPositions.items[i] >= start)
		// {
		// visibleTextStart = i;
		// startPos = glyphPositions.items[i];
		// textOffset = glyphPositions.items[visibleTextStart] - start;
		// break;
		// }
		// }
		//
		// // calculate last visible char based on visible width and render offset
		// visibleTextEnd = Math.min(displayText.get(cursorLine).getDisplayText().length(), cursor + 1);
		// for (; visibleTextEnd <= displayText.get(cursorLine).getDisplayText().length(); visibleTextEnd++)
		// {
		// if (glyphPositions.items[visibleTextEnd] - startPos > visibleWidth) break;
		// }
		// visibleTextEnd = Math.max(0, visibleTextEnd - 1);
		//
		// // calculate selection x position and width
		// if (hasSelection)
		// {
		// int minIndex = Math.min(cursor, selectionStart);
		// int maxIndex = Math.max(cursor, selectionStart);
		// float minX = Math.max(glyphPositions.get(minIndex), glyphPositions.get(visibleTextStart));
		// float maxX = Math.min(glyphPositions.get(maxIndex), glyphPositions.get(visibleTextEnd));
		// selectionX = minX;
		// selectionWidth = maxX - minX;
		// }
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		displayTextLock.lock();
		try
		{
			final BitmapFont font = style.font;
			final Color fontColor = style.fontColor;
			final TextureRegion selection = style.selection;
			final NinePatch cursorPatch = style.cursor;
			lineHeight = style.font.getLineHeight();

			float bgLeftWidth = 0;
			float bgRightWidth = 0;
			float bgTopHeight = 0;
			float bgBottomHeight = 0;
			boolean focused = GL_Listener.hasFocus(this);

			if (focused)
			{
				if (style.backgroundFocused != null)
				{
					style.backgroundFocused.draw(batch, x, y, width, height);
					bgLeftWidth = style.backgroundFocused.getLeftWidth();
					bgRightWidth = style.background.getRightWidth();
					bgTopHeight = style.background.getTopHeight();
					bgBottomHeight = style.background.getBottomHeight();
				}
			}
			else
			{
				if (style.background != null)
				{
					style.background.draw(batch, x, y, width, height);
					bgLeftWidth = style.background.getLeftWidth();
					bgRightWidth = style.background.getRightWidth();
					bgTopHeight = style.background.getTopHeight();
					bgBottomHeight = style.background.getBottomHeight();
				}
			}

			{// Background is drawed, now set scissor to inner rec
				batch.end();

				CB_RectF innerScissorReg = intersectRec.copy();
				innerScissorReg.setHeight(intersectRec.getHeight() - bgTopHeight - bgBottomHeight);
				innerScissorReg.setY(intersectRec.getY() + bgBottomHeight);

				batch.begin();

				Gdx.gl.glScissor((int) innerScissorReg.getX(), (int) innerScissorReg.getY(), (int) innerScissorReg.getWidth() + 1,
						(int) innerScissorReg.getHeight() + 1);

			}

			float textY = (int) (height / 2 + textHeight / 2 + font.getDescent());
			textY = (int) height - textHeight - bgTopHeight;
			maxLineCount = (height - bgTopHeight - bgBottomHeight - lineHeight / 2) / lineHeight;
			calculateOffsets();

			if (focused && hasSelection && selection != null)
			{
				batch.draw(selection, x + selectionX + bgLeftWidth + renderOffset, y + textY - textHeight - font.getDescent() / 2,
						selectionWidth, textHeight);
			}

			if ((displayText.size() == 1) && (displayText.get(0).getDisplayText().length() == 0))
			{
				if (!focused && messageText != null)
				{
					if (style.messageFontColor != null)
					{
						font.setColor(style.messageFontColor.r, style.messageFontColor.g, style.messageFontColor.b,
								style.messageFontColor.a);
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
				textY += lineHeight * topLine;
				for (DisplayText dt : displayText)
				{
					// font.draw(batch, dt.getDisplayText(), x + bgLeftWidth + textOffset, y + textY, visibleTextStart, visibleTextEnd);
					font.draw(batch, dt.getDisplayText(), x + bgLeftWidth, y + textY + mouseTempMove);
					textY -= lineHeight;
				}
			}
			if (focused)
			{

				if (cursorOn && cursorPatch != null)
				{
					DisplayText dt = displayText.get(cursorLine);
					float xpos = 0;

					if (cursor < dt.glyphPositions.size)
					{
						xpos = dt.glyphPositions.get(cursor);
					}
					else if (dt.glyphPositions.size == 0)
					{
						xpos = 0;
					}
					else
					{
						xpos = dt.glyphPositions.get(dt.glyphPositions.size - 1); // letztes Zeichen
					}
					textY = (int) height - textHeight - bgTopHeight;

					float cursorHeight = displayText.get(0).textBounds.height + font.getDescent() / 2;

					cursorPatch.draw(batch, x + bgLeftWidth + xpos - 1, (y + textY - bgTopHeight - lineHeight * (cursorLine - topLine))
							+ font.getDescent(), cursorPatch.getTotalWidth(), cursorHeight);

				}
			}

			if (focused)
			{
				if (blinkTimer == null) blinkStart();
			}
			else
			{
				if (blinkTimer != null) blinkStop();
			}
		}
		finally
		{
			displayTextLock.unlock();
		}
	}

	private void updateDisplayTextList()
	{
		displayText.clear();
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
			displayText.add(new DisplayText(new String(passwordBuffer), style.font));
		}
		else
		{
			String[] dts = text.split("\n");
			for (String s : dts)
				displayText.add(new DisplayText(s, style.font));
		}

		for (DisplayText dt : displayText)
		{
			style.font.computeGlyphAdvancesAndPositions(dt.getDisplayText(), dt.glyphAdvances, dt.glyphPositions);
		}

		int lineCount = displayText.size();
		if (listener != null) listener.lineCountChanged(this, lineCount, lineHeight * lineCount);
	}

	// Wenn calcCursor == true -> Cursorposition wird evtl. angepasst, sonst nicht
	private void updateDisplayText(DisplayText dt, boolean calcCursor)
	{
		float maxWidth = width - 50; // noch falsch!!!!!!!!!!!!!!!!!!!!!
		// wenn dies eine autoWrap Zeile ist muss zuerst die Zeile davor überprüft werden, ob die ersten Zeichen dieser Zeile dahinein
		// kopiert werden können
		if (dt.autoWrap)
		{
			DisplayText prevDt = getDisplayText(cursorLine - 1);
			if (prevDt != null)
			{
				// Restlichen Platz suchen
				float len = prevDt.getWidth();
				float rest = maxWidth - prevDt.getWidth(); // Restlicher Platz der vorherigen Zeile
				// Breite des ersten Wortes incl. abschließendem Leerzeichen suchen
				float posWord = 0;
				int idWord = 0;
				for (int i = 0; i < dt.displayText.length(); i++)
				{
					char c = dt.displayText.charAt(i);
					float pos = dt.glyphPositions.get(i + 1);
					// Prüfen, ob aktuelles Zeichen ein Leerzeichen ist und ob das Ende nicht weiter als "rest" vom Start der Linie entfernt
					// ist
					if ((c == ' ') && (pos <= rest))
					{
						posWord = pos;
						idWord = i + 1;
					}
					else if (pos > rest)
					{
						break;
					}
				}
				if (idWord == 0)
				{
					// Prüfen, ob komplette nächste Zeile in diese rein passt
					if (dt.getWidth() <= rest)
					{
						idWord = dt.displayText.length();
					}
				}
				if (idWord > 0)
				{
					// dieses erste Wort passt noch in die letzte Zeile
					String s1 = dt.displayText.toString().substring(0, idWord);
					String s2 = dt.displayText.toString().substring(idWord, dt.displayText.length());
					prevDt.displayText += s1;
					style.font.computeGlyphAdvancesAndPositions(prevDt.displayText, prevDt.glyphAdvances, prevDt.glyphPositions);
					dt.displayText = s2;
					if (s2.length() == 0)
					{
						// komplette Zeile löschen
						displayText.remove(dt);
						int lineCount = displayText.size();
						if (listener != null) listener.lineCountChanged(this, lineCount, lineHeight * lineCount);
					}
					else
					{
						style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
					}
					if (cursor > idWord)
					{
						cursor -= idWord; // Cursor ist hinter den Zeichen die in die vorherige Zeile verschoben werden -> nach Vorne setzen
					}
					else
					{
						// cursor ist innerhalb der Zeichen, die in die vorherige Zeile verschoben werden -> Cursor in die vorherige Zeile
						// verschieben
						cursor = prevDt.displayText.length() - 1;
						setCursorLine(cursorLine - 1);
						// anschließende Zeile noch mal berechnen.
						cursorLine++;
						DisplayText nextDt = getAktDisplayText();
						if (nextDt != null)
						{
							updateDisplayText(nextDt, false);
						}
						cursorLine--;
						return;
					}
				}
			}
		}
		style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
		float len = dt.getWidth();

		// Prüfen, ob Zeile zu lang geworden ist und ob am Ende Zeichen in die nächste Zeile verschoben werden müssen
		if (len > maxWidth)
		{
			// automatischen Umbruch einfügen
			// erstes Zeichen suchen, das außerhalb des max. Bereichs liegt
			int id = 0;
			for (int i = 1; i < dt.glyphPositions.size; i++)
			{
				// abschließende Leerzeichen hier nicht berücksichtigen
				if ((dt.displayText.length() > i - 1) && (dt.displayText.charAt(i - 1) == ' ')) continue;
				float pos = dt.glyphPositions.get(i);
				if (pos > maxWidth) id = i;
			}
			if (id > 0)
			{
				if (!calcCursor)
				{
					calcCursor = false;
				}
				// Zeile Trennen nach dem letzten "Space" vor dem Zeichen id
				for (int j = id - 1; j >= 0; j--)
				{
					if (dt.displayText.charAt(j) == ' ')
					{
						id = j + 1;
						break;
					}
				}
				// Zeilenumbruch an Zeichen id
				// aktuellen String bei id trennen
				String s1 = dt.displayText.toString().substring(0, id);
				String s2 = dt.displayText.toString().substring(id, dt.displayText.length());
				dt.displayText = s1;
				style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
				cursorLine++;
				// Text der nächsten Zeile holen und prüfen, ob dies eine durch einen autoWrap eingefügte Zeile ist
				DisplayText nextDt = getAktDisplayText();
				cursorLine--;
				if ((nextDt != null) && nextDt.autoWrap)
				{
					// Umzubrechnenden Text am Anfang von nextDT anfügen
					nextDt.displayText = s2 + nextDt.displayText;
					style.font.computeGlyphAdvancesAndPositions(nextDt.displayText, nextDt.glyphAdvances, nextDt.glyphPositions);
					cursorLine++;
					updateDisplayText(nextDt, false);
					cursorLine--;
				}
				else
				{
					// neue Zeile erstellen
					DisplayText newDt = new DisplayText(s2, true, style.font);
					displayText.add(cursorLine + 1, newDt);
					style.font.computeGlyphAdvancesAndPositions(newDt.displayText, newDt.glyphAdvances, newDt.glyphPositions);
				}
				if (calcCursor && (cursor >= id))
				{
					// Cursor auch in die nächste Zeile verschieben, an die Stelle im Wort an der der Cursor vorher auch war
					cursor = cursor - id;
					setCursorLine(cursorLine + 1);
				}
			}
		}
		// Prüfen, ob am Ende der Zeile wieder Platz für Zeichen / Wörter der vorgänger-Zeile ist
		float rest = maxWidth - dt.getWidth(); // Restlicher Platz
		// Wenn anschließend eine Wraped-Zeile kommt dann erstes Word dessen suchen und Prüfen, ob dies hier eingefügt werden kann
		DisplayText nextDt = getDisplayText(cursorLine + 1);
		if ((nextDt != null) && (nextDt.autoWrap))
		{
			// Breite des ersten Wortes incl. abschließendem Leerzeichen suchen
			float posWord = 0;
			int idWord = 0;
			for (int i = 0; i < nextDt.displayText.length(); i++)
			{
				char c = nextDt.displayText.charAt(i);
				float pos = nextDt.glyphPositions.get(i + 1);
				// Prüfen, ob aktuelles Zeichen ein Leerzeichen ist und ob das Ende nicht weiter als "rest" vom Start der Linie entfernt ist
				if ((c == ' ') && (pos <= rest))
				{
					posWord = pos;
					idWord = i + 1;
				}
				else if (pos > rest)
				{
					break;
				}
			}
			if (idWord == 0)
			{
				// Prüfen, ob komplette nächste Zeile in diese rein passt
				if (nextDt.getWidth() <= rest)
				{
					idWord = nextDt.displayText.length();
				}
			}
			if (idWord > 0)
			{
				// dieses erste Wort passt noch in die letzte Zeile
				String s1 = nextDt.displayText.toString().substring(0, idWord);
				String s2 = nextDt.displayText.toString().substring(idWord, nextDt.displayText.length());
				dt.displayText += s1;
				style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
				updateDisplayText(dt, false);
				nextDt.displayText = s2;
				if (s2.length() == 0)
				{
					// komplette Zeile löschen
					displayText.remove(nextDt);
				}
				else
				{
					cursorLine++;
					style.font.computeGlyphAdvancesAndPositions(nextDt.displayText, nextDt.glyphAdvances, nextDt.glyphPositions);
					updateDisplayText(nextDt, false);
					cursorLine--;
				}
			}
		}
		else
		{
			// anschließend kommt keine Zeile, die automatisch umgebrochen wurde -> nichts machen
		}
	}

	private Point mouseDown = null;
	private float mouseDownTopLine = 0;
	private int mouseTempMove = 0;

	@Override
	public boolean onTouchDown(int X, int Y, int pointer, int button)
	{
		mouseDown = new Point(X, Y);
		// topLine merken, zu dem Zeitpunkt als die Maus gedrückt wurde
		mouseDownTopLine = topLine;
		return true;
	}

	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (mouseDown != null)
		{
			if (displayText.size() < maxLineCount)
			{
				topLine = 0;
			}
			else
			{
				topLine = mouseDownTopLine + (float) (y - mouseDown.y) / lineHeight;
				if (topLine < 0)
				{
					topLine = 0;
				}

				if (displayText.size() - topLine < maxLineCount)
				{
					topLine = displayText.size() - maxLineCount;
				}
			}
		}
		GL_Listener.glListener.renderOnce("EditWrapedTextField");
		return true;
	};

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (mouseDown != null)
		{
			mouseTempMove = y - mouseDown.y;

			mouseTempMove = 0;
		}
		mouseDown = null;
		return false;
	}

	@Override
	public boolean click(int X, int Y, int pointer, int button)
	{
		float x = (float) X;
		float y = (float) Y;

		if (pointer != 0) return false;
		GL_Listener.setKeyboardFocus(this);
		keyboard.show(true);
		clearSelection();

		cursorOn = false;
		x = x - style.backgroundFocused.getLeftWidth();

		// Zeile bestimmen, in die geklickt wurde

		float clickPos = y;
		int clickedCursorLine = (int) ((this.height - style.font.getLineHeight() - clickPos + (lineHeight)) / lineHeight) - 1;
		clickedCursorLine += topLine;
		if (clickedCursorLine < 0) return false;
		if (clickedCursorLine >= displayText.size()) return false;

		DisplayText dt = displayText.get(clickedCursorLine);

		for (int i = 0; i < dt.glyphPositions.size; i++)
		{
			float pos = dt.glyphPositions.items[i];
			if (pos > x)
			{
				cursor = Math.max(0, i - 1);
				setCursorLine(clickedCursorLine);
				return true;
			}
		}
		cursor = Math.max(0, dt.glyphPositions.size - 1);
		setCursorLine(clickedCursorLine);
		GL_Listener.glListener.renderOnce("EditWrapedTextField");
		return true;
	}

	public boolean keyDown(int keycode)
	{
		final BitmapFont font = style.font;

		displayTextLock.lock();
		try
		{

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
						checkCursorVisible();
					}
					if (keycode == Keys.RIGHT)
					{
						if (!hasSelection)
						{
							selectionStart = cursor;
							hasSelection = true;
						}
						cursor++;
						checkCursorVisible();
					}
					if (keycode == Keys.HOME)
					{
						if (!hasSelection)
						{
							selectionStart = cursor;
							hasSelection = true;
						}
						cursor = 0;
						// überprüfen, ob der Cursor sichtbar ist
						checkCursorVisible();
					}
					if (keycode == Keys.END)
					{
						if (!hasSelection)
						{
							selectionStart = cursor;
							hasSelection = true;
						}
						cursor = text.length();
						// überprüfen, ob der Cursor sichtbar ist
						checkCursorVisible();
					}

					cursor = Math.max(0, cursor);
					cursor = Math.min(text.length(), cursor);
					// überprüfen, ob der Cursor sichtbar ist
					checkCursorVisible();
				}
				else
				{
					// cursor movement or other keys (kill selection)
					if (keycode == Keys.LEFT)
					{
						cursorLeftRight(-1);
						clearSelection();
					}
					if (keycode == Keys.RIGHT)
					{
						cursorLeftRight(1);
						clearSelection();
					}
					if (keycode == Keys.HOME)
					{
						cursorHomeEnd(-1);
						clearSelection();
					}
					if (keycode == Keys.END)
					{
						cursorHomeEnd(1);
						clearSelection();
					}
					if (keycode == Keys.UP)
					{
						cursorUpDown(-1);
						clearSelection();
					}
					if (keycode == Keys.DOWN)
					{
						cursorUpDown(1);
						clearSelection();
					}
				}
				GL_Listener.glListener.renderOnce("EditWrapedTextField");

				return true;
			}
		}
		finally
		{
			displayTextLock.unlock();
		}
		return false;
	}

	// bewegt den Cursor an den Anfang / Ende der aktuellen Zeile
	private void cursorHomeEnd(int i)
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		if (i < 0)
		{
			cursor = 0;
			// überprüfen, ob der Cursor sichtbar ist
			checkCursorVisible();
		}
		else
		{
			cursor = dt.displayText.length();
			// überprüfen, ob der Cursor sichtbar ist
			checkCursorVisible();
		}
	}

	// bewegt den Cursor nach links - rechts
	private void cursorLeftRight(int i)
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		int newCursor = cursor + i;
		if (newCursor > dt.displayText.length())
		{
			if (cursorUpDown(1)) cursor = 0;
		}
		else if (newCursor < 0)
		{
			if (cursorUpDown(-1))
			{
				DisplayText newDt = getAktDisplayText();
				cursor = newDt.displayText.length();
			}
		}
		else
		{
			cursor = newCursor;
		}
		// überprüfen, ob der Cursor sichtbar ist
		checkCursorVisible();
	}

	// fügt eine neue Zeile an der Cursor Position ein
	private void insertNewLine()
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		// aktuellen String bei Cursor-Position trennen
		String s1 = dt.displayText.toString().substring(0, cursor);
		String s2 = dt.displayText.toString().substring(cursor, dt.displayText.length());
		dt.displayText = s1;
		DisplayText newDt = new DisplayText(s2, style.font);
		displayText.add(cursorLine + 1, newDt);
		style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
		style.font.computeGlyphAdvancesAndPositions(newDt.displayText, newDt.glyphAdvances, newDt.glyphPositions);
		setCursorLine(cursorLine + 1);
		cursor = 0;

		int lineCount = displayText.size();

		if (listener != null) listener.lineCountChanged(this, lineCount, lineHeight * lineCount);

	}

	// bewegt den Cursor nach oben / unten. X-Position des Cursors soll möglichst gleich bleiben
	private boolean cursorUpDown(int i)
	{
		int newCursorLine = cursorLine + i;
		if (newCursorLine < 0) return false;
		if (newCursorLine >= displayText.size()) return false;
		DisplayText oldDt = displayText.get(cursorLine);
		// X-Koordinate von alter Cursor Position bestimmen
		float x = oldDt.glyphPositions.items[cursor];
		// Cursor in neue Zeile plazieren
		setCursorLine(newCursorLine);
		// Cursor möglichst an gleiche x-Position plazieren
		setCursorXPos(x);
		return true;
	}

	// liefert das DisplayText-Object der aktuellen Zeile
	private DisplayText getAktDisplayText()
	{
		if (cursorLine < 0) return null;
		if (cursorLine >= displayText.size()) return null;
		DisplayText newDt = displayText.get(cursorLine);
		return newDt;
	}

	// liefert das DisplayText-Object der aktuellen Zeile
	private DisplayText getDisplayText(int line)
	{
		if (line < 0) return null;
		if (line >= displayText.size()) return null;
		DisplayText newDt = displayText.get(line);
		return newDt;
	}

	// Zeile des Cursors ändern. Sicherstellen, dass der Cursor sichtbar ist
	private void setCursorLine(int newCursorLine)
	{
		cursorLine = newCursorLine;
		checkCursorVisible();
	}

	private void checkCursorVisible()
	{
		// Cursorpos prüfen, ob ausserhalb sichtbaren Bereich
		if (cursorLine - topLine >= maxLineCount)
		{
			topLine = cursorLine - maxLineCount + 1;
		}
		if (cursorLine < topLine)
		{
			topLine = cursorLine;
		}
	}

	// Cursor in aktueller Zeile an die gegebene X-Position (in Pixel) senden
	private void setCursorXPos(float xPos)
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		if (xPos <= 0)
		{
			cursor = 0;
			checkCursorVisible();
			return;
		}
		for (int i = 0; i < dt.glyphPositions.size; i++)
		{
			float pos = dt.glyphPositions.items[i];
			if (pos > xPos)
			{
				cursor = Math.max(0, i - 1);
				checkCursorVisible();
				return;
			}
		}
		// kein Passendes Zeichen gefunden an gegebener Position -> Cursor ans Ende setzen
		cursor = Math.max(0, dt.glyphPositions.size - 1);
		checkCursorVisible();
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
				// updateDisplayText();
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
				// updateDisplayText();
				cursor = minIndex + content.length();
				checkCursorVisible();
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
		// updateDisplayText();
		cursor = minIndex;
		checkCursorVisible();
		clearSelection();
	}

	public boolean keyTyped(char character)
	{
		final BitmapFont font = style.font;
		DisplayText dt = getAktDisplayText();
		if (dt == null || disabled) return false;

		if (GL_Listener.hasFocus(this))
		{
			if (character == BACKSPACE)
			{
				if (cursor > 0)
				{
					dt.displayText = dt.displayText.substring(0, cursor - 1) + dt.displayText.substring(cursor, dt.displayText.length());
					updateDisplayText(dt, true);
					cursor--;
					checkCursorVisible();
					GL_Listener.glListener.renderOnce("EditWrapedTextField");
					return true;
				}
				else
				{
					if (cursorLine > 0)
					{
						setCursorLine(cursorLine - 1);
						DisplayText dt2 = getAktDisplayText();
						cursor = dt2.displayText.length();
						checkCursorVisible();
						dt2.displayText += dt.displayText;
						displayText.remove(cursorLine + 1);
						updateDisplayText(dt2, true);

						int lineCount = displayText.size();
						if (listener != null) listener.lineCountChanged(this, lineCount, lineHeight * lineCount);
					}
					GL_Listener.glListener.renderOnce("EditWrapedTextField");
					return true;
				}
			}
			if (character == DELETE)
			{
				if (cursor < dt.displayText.length())
				{
					dt.displayText = dt.displayText.substring(0, cursor) + dt.displayText.substring(cursor + 1, dt.displayText.length());
					updateDisplayText(dt, true);
					GL_Listener.glListener.renderOnce("EditWrapedTextField");
					return true;
				}
				else
				{
					if (cursorLine + 1 < displayText.size())
					{
						cursorLine++;
						DisplayText dt2 = getAktDisplayText();
						cursorLine--;
						displayText.remove(dt2);
						dt.displayText += dt2.displayText;
						updateDisplayText(dt, true);
						GL_Listener.glListener.renderOnce("EditWrapedTextField");
					}
					return true;
				}
			}
			// if (character == BACKSPACE && (cursor > 0 || hasSelection))
			// {
			// if (!hasSelection)
			// {
			// text = text.substring(0, cursor - 1) + text.substring(cursor);
			// updateDisplayText();
			// cursor--;
			// }
			// else
			// {
			// delete();
			// }
			// }
			// if (character == DELETE)
			// {
			// if (cursor < text.length() || hasSelection)
			// {
			// if (!hasSelection)
			// {
			// text = text.substring(0, cursor) + text.substring(cursor + 1);
			// updateDisplayText();
			// }
			// else
			// {
			// delete();
			// }
			// }
			// return true;
			// }

			if (character == ENTER_DESKTOP || character == ENTER_ANDROID)
			{
				insertNewLine();
				clearSelection();
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
					dt.displayText = dt.displayText.substring(0, cursor) + character
							+ dt.displayText.substring(cursor, dt.displayText.length());
					updateDisplayText(dt, true);
					cursor++;
					checkCursorVisible();
				}
				else
				{
					int minIndex = Math.min(cursor, selectionStart);
					int maxIndex = Math.max(cursor, selectionStart);

					text = (minIndex > 0 ? text.substring(0, minIndex) : "")
							+ (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
					cursor = minIndex;
					text = text.substring(0, cursor) + character + text.substring(cursor, text.length());
					// updateDisplayText();
					cursor++;
					checkCursorVisible();
					clearSelection();
				}
				GL_Listener.glListener.renderOnce("EditWrapedTextField");
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
			if (font.containsCharacter(c) || (c == '\n')) buffer.append(c);
		}

		String bText = buffer.toString();

		// replace lineBreaks
		this.text = bText.replace("\r\n", "\r");

		updateDisplayTextList();
		cursor = 0;
		checkCursorVisible();
		clearSelection();

		textHeight = -font.getDescent() * 2;
		for (DisplayText dt : displayText)
		{
			dt.calcTextBounds(font);
		}
	}

	/** @return Never null, might be an empty string. */
	public String getText()
	{

		StringBuilder sb = new StringBuilder();

		int lastLine = displayText.size();
		int index = 0;
		for (DisplayText dt : displayText)
		{
			sb.append(dt.displayText);
			if (!dt.autoWrap && index != lastLine) sb.append(GlobalCore.br);
			index++;
		}

		return sb.toString();
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
		checkCursorVisible();
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
		checkCursorVisible();
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

	private class DisplayText
	{
		private String displayText;
		private TextBounds textBounds = new TextBounds();
		private final FloatArray glyphAdvances = new FloatArray();
		private final FloatArray glyphPositions = new FloatArray();
		private boolean autoWrap;

		public DisplayText(String displayText, BitmapFont font)
		{
			this.displayText = displayText;

			calcTextBounds(font);
		}

		public DisplayText(String displayText, boolean autoWrap, BitmapFont font)
		{
			this.displayText = displayText;
			this.autoWrap = autoWrap;

			calcTextBounds(font);
		}

		public void calcTextBounds(BitmapFont font)
		{
			textBounds.set(font.getBounds(displayText));
			textBounds.height -= font.getDescent() * 2;
			font.computeGlyphAdvancesAndPositions(displayText, glyphAdvances, glyphPositions);
		}

		public String getDisplayText()
		{
			return displayText;
		}

		public TextBounds getTextBounds()
		{
			return textBounds;
		}

		public float getWidth()
		{
			return glyphPositions.get(glyphPositions.size - 1) + glyphAdvances.get(glyphAdvances.size - 1);
		}
	}

	public float getMesuredHeight()
	{

		float h = 0;

		for (DisplayText text : displayText)
		{
			h += text.getTextBounds().height;
		}

		return h;
	}

}
