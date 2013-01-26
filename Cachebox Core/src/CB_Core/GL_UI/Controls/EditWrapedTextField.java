package CB_Core.GL_UI.Controls;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Map.Point;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.FloatArray;

public class EditWrapedTextField extends EditTextFieldBase
{
	public enum TextFieldType
	{
		SingleLine, MultiLine, MultiLineWraped
	}

	public static final char BACKSPACE = 8;
	public static final char ENTER_DESKTOP = '\r';
	public static final char ENTER_ANDROID = '\n';
	public static final char TAB = '\t';
	public static final char DELETE = 127;
	public static final char BULLET = 149;

	// factor for the size of the selectinMarkers
	static protected final double markerFactor = 2.0;
	protected final float x = 0;
	protected final float y = 0;

	protected boolean dontShowKeyBoard = false;

	protected TextFieldStyle style;
	protected String text, messageText;
	protected ArrayList<DisplayText> displayText;
	protected Cursor cursor = new Cursor(0, 0);
	// protected int cursor;
	protected float cursorHeight;
	// protected int cursorLine;
	protected float topLine;
	protected float maxLineCount; // Anzahl der darzustellenden Zeilen
	protected float leftPos; // Anzahl der Pixel, um die nach links gescrollt ist
	protected float maxTextWidth; // Anzahl der Pixel des sichtbaren Textes
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

	protected Selection selection = null;
	// protected boolean hasSelection;
	// protected int selectionStart;
	// protected float selectionX, selectionWidth;

	protected char passwordCharacter = BULLET;
	final Lock displayTextLock = new ReentrantLock();

	float bgTopHeight = 0;
	float bgBottomHeight = 0;

	protected TextFieldType type = TextFieldType.SingleLine;

	public EditWrapedTextField(CB_View_Base parent, CB_RectF rec, String Name)
	{
		super(parent, rec, Name);
		this.style = getDefaultStyle();
		displayText = new ArrayList<EditWrapedTextField.DisplayText>();
		setCursorLine(0, true);
		lineHeight = style.font.getLineHeight();
		setText("");
		topLine = 0;
		leftPos = 0;
		this.setClickable(true);
	}

	public EditWrapedTextField(CB_View_Base parent, CB_RectF rec, TextFieldStyle style, String Name)
	{
		super(parent, rec, Name);
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		displayText = new ArrayList<EditWrapedTextField.DisplayText>();
		setCursorLine(0, true);
		lineHeight = style.font.getLineHeight();
		setText("");
		topLine = 0;
		leftPos = 0;
		this.setClickable(true);
	}

	public EditWrapedTextField(CB_View_Base parent, CB_RectF rec, TextFieldStyle style, String Name, TextFieldType type)
	{
		this(parent, rec, style, Name);
		this.type = type;
	}

	public EditWrapedTextField(CB_View_Base parent, CB_RectF rec, TextFieldType type, String Name)
	{
		this(parent, rec, Name);
		this.type = type;
	}

	public EditWrapedTextField()
	{
		super(null, new CB_RectF(), "");
		this.style = getDefaultStyle();
		displayText = new ArrayList<EditWrapedTextField.DisplayText>();
		setCursorLine(0, true);
		lineHeight = style.font.getLineHeight();
		setText("");
		topLine = 0;
		leftPos = 0;
		this.setClickable(true);
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

	}

	@Override
	protected void SkinIsChanged()
	{

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
		// if (style.background != null) visibleWidth -= style.background.this.LeftWidth + style.background.RightWidth;

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
			final Drawable selectionPatch = style.selection;
			final Drawable cursorPatch = style.cursor;
			lineHeight = style.font.getLineHeight();

			float bgLeftWidth = 0;
			float bgRightWidth = 0;
			bgTopHeight = 0;
			bgBottomHeight = 0;
			boolean focused = GL.that.hasFocus(this);

			if (focused)
			{
				if (style.backgroundFocused != null)
				{
					style.backgroundFocused.draw(batch, x, y, width, height);
					bgLeftWidth = style.backgroundFocused.getLeftWidth();
					bgRightWidth = style.background.getRightWidth();
					bgTopHeight = style.background.getTopHeight();
					bgBottomHeight = style.background.getBottomHeight();
					if (type == TextFieldType.SingleLine)
					{
						bgTopHeight = (height - lineHeight) / 2;
						bgBottomHeight = (height - lineHeight) / 2;
					}
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
					if (type == TextFieldType.SingleLine)
					{
						bgTopHeight = (height - lineHeight) / 2;
						bgBottomHeight = (height - lineHeight) / 2;
					}
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
			textY = (int) height /*- textHeight*/- bgTopHeight + font.getDescent();
			maxLineCount = (height - bgTopHeight - bgBottomHeight - lineHeight / 2) / lineHeight;
			maxTextWidth = width - bgLeftWidth - bgRightWidth;
			calculateOffsets();

			// if (focused && hasSelection && selection != null)
			// {
			// selection.draw(batch, x + selectionX + bgLeftWidth + renderOffset, y + textY - textHeight - font.getDescent() / 2,
			// selectionWidth, textHeight);
			// }

			if (selection != null)
			{
				// Selection zeilenweise durchgehen
				for (int line = selection.cursorStart.line; line <= selection.cursorEnd.line; line++)
				{
					DisplayText dt = getDisplayText(line);
					if (dt == null) continue;
					int start = 0;
					if (line == selection.cursorStart.line) start = selection.cursorStart.pos;
					int end = dt.getDisplayText().length();
					if (line == selection.cursorEnd.line) end = selection.cursorEnd.pos;
					float selectionX = dt.glyphPositions.get(start);
					float selectionY = dt.glyphPositions.get(end);
					float selectionWidth = selectionY - selectionX;
					selectionPatch.draw(batch, x + selectionX + bgLeftWidth - leftPos, y + textY + lineHeight * topLine - lineHeight - line
							* lineHeight - font.getDescent() / 2, selectionWidth, lineHeight);
				}
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
					font.draw(batch, dt.getDisplayText(), x + bgLeftWidth - leftPos, y + textY + mouseTempMove);
					textY -= lineHeight;
				}
			}
			if (focused)
			{

				if (cursorOn && cursorPatch != null)
				{
					// DisplayText dt = displayText.get(cursorLine);
					// float xpos = 0;
					//
					// if (cursor < dt.glyphPositions.size)
					// {
					// xpos = dt.glyphPositions.get(cursor);
					// }
					// else if (dt.glyphPositions.size == 0)
					// {
					// xpos = 0;
					// }
					// else
					// {
					// xpos = dt.glyphPositions.get(dt.glyphPositions.size - 1); // letztes Zeichen
					// }
					float xpos = getCursorX();
					textY = (int) height - bgTopHeight + font.getDescent();

					cursorHeight = font.getLineHeight() + font.getDescent() / 2;

					cursorPatch.draw(batch, getCursorX() - leftPos, getCursorY() + cursorHeight + font.getDescent(),
							cursorPatch.getMinWidth(), cursorHeight);

				}
			}

			if (focused && (selection == null))
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

	private float getCursorX()
	{
		return getCursorX(cursor);
	}

	private float getCursorX(Cursor aCursor)
	{

		float xpos = 0;

		if (displayText.size() > aCursor.line)
		{
			DisplayText dt = displayText.get(aCursor.line);

			if (aCursor.pos < dt.glyphPositions.size)
			{
				xpos = dt.glyphPositions.get(aCursor.pos);
			}
			else if (dt.glyphPositions.size == 0)
			{
				xpos = 0;
			}
			else
			{
				xpos = dt.glyphPositions.get(dt.glyphPositions.size - 1); // letztes Zeichen
			}
		}

		return x + style.background.getLeftWidth() + xpos - 1 - leftPos;
	}

	private float getCursorY()
	{
		return getCursorY(cursor.line);
	}

	private float getCursorY(int aCursorLine)
	{
		float textY = (int) height - bgTopHeight + style.font.getDescent();
		return (int) (y + textY - lineHeight * (aCursorLine - topLine) - lineHeight * 1.5);
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
		sendLineCountChanged(lineCount, lineHeight * lineCount);
	}

	// Wenn calcCursor == true -> Cursorposition wird evtl. angepasst, sonst nicht
	private void updateDisplayText(DisplayText dt, boolean calcCursor)
	{
		float maxWidth = width - 50; // noch falsch!!!!!!!!!!!!!!!!!!!!!
		// wenn dies eine autoWrap Zeile ist muss zuerst die Zeile davor überprüft werden, ob die ersten Zeichen dieser Zeile dahinein
		// kopiert werden können
		if (dt.autoWrap)
		{
			DisplayText prevDt = getDisplayText(cursor.line - 1);
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
						sendLineCountChanged(lineCount, lineHeight * lineCount);
					}
					else
					{
						style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
					}
					if (cursor.pos > idWord)
					{
						cursor.pos -= idWord; // Cursor ist hinter den Zeichen die in die vorherige Zeile verschoben werden -> nach Vorne
												// setzen
					}
					else
					{
						// cursor ist innerhalb der Zeichen, die in die vorherige Zeile verschoben werden -> Cursor in die vorherige Zeile
						// verschieben
						cursor.pos = prevDt.displayText.length() - 1;
						setCursorLine(cursor.line - 1, true);
						// anschließende Zeile noch mal berechnen.
						cursor.line++;
						DisplayText nextDt = getAktDisplayText();
						if (nextDt != null)
						{
							updateDisplayText(nextDt, false);
						}
						cursor.line--;
						return;
					}
				}
			}
		}
		style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
		float len = dt.getWidth();

		// Prüfen, ob Zeile zu lang geworden ist und ob am Ende Zeichen in die nächste Zeile verschoben werden müssen
		if ((len > maxWidth) && isWraped())
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
				cursor.line++;
				// Text der nächsten Zeile holen und prüfen, ob dies eine durch einen autoWrap eingefügte Zeile ist
				DisplayText nextDt = getAktDisplayText();
				cursor.line--;
				if ((nextDt != null) && nextDt.autoWrap)
				{
					// Umzubrechnenden Text am Anfang von nextDT anfügen
					nextDt.displayText = s2 + nextDt.displayText;
					style.font.computeGlyphAdvancesAndPositions(nextDt.displayText, nextDt.glyphAdvances, nextDt.glyphPositions);
					cursor.line++;
					updateDisplayText(nextDt, false);
					cursor.line--;
				}
				else
				{
					// neue Zeile erstellen
					DisplayText newDt = new DisplayText(s2, true, style.font);
					displayText.add(cursor.line + 1, newDt);
					style.font.computeGlyphAdvancesAndPositions(newDt.displayText, newDt.glyphAdvances, newDt.glyphPositions);
				}
				if (calcCursor && (cursor.pos >= id))
				{
					// Cursor auch in die nächste Zeile verschieben, an die Stelle im Wort an der der Cursor vorher auch war
					cursor.pos = cursor.pos - id;
					setCursorLine(cursor.line + 1, true);
				}
			}
		}
		// Prüfen, ob am Ende der Zeile wieder Platz für Zeichen / Wörter der vorgänger-Zeile ist
		float rest = maxWidth - dt.getWidth(); // Restlicher Platz
		// Wenn anschließend eine Wraped-Zeile kommt dann erstes Word dessen suchen und Prüfen, ob dies hier eingefügt werden kann
		DisplayText nextDt = getDisplayText(cursor.line + 1);
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
					cursor.line++;
					style.font.computeGlyphAdvancesAndPositions(nextDt.displayText, nextDt.glyphAdvances, nextDt.glyphPositions);
					updateDisplayText(nextDt, false);
					cursor.line--;
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
	private float mouseDownLeftPos = 0;

	@Override
	public boolean onTouchDown(int X, int Y, int pointer, int button)
	{
		mouseDown = new Point(X, Y);
		// topLine merken, zu dem Zeitpunkt als die Maus gedrückt wurde
		mouseDownTopLine = topLine;
		// leftPos merken, zu dem Zeitpunkt als die Maus gedrückt wurde
		mouseDownLeftPos = leftPos;
		return true;
	}

	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		boolean bearbeitet = false;
		if (mouseDown != null)
		{
			float oldTopLine = topLine;
			float oldLeftPos = leftPos;
			if (isMultiLine())
			{

				// Scrollen Oben - Unten
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
				bearbeitet = true;
			}
			if (!isWraped())
			{
				// Scrollen Links - Rechts
				float maxWidth = getMaxDisplayTextWidth();
				if (maxWidth < maxTextWidth)
				{
					// Text hat auf einmal Platz -> auf Ursprung hin scrollen
					leftPos = 0;
				}
				else
				{
					// Text hat nicht auf einmal Platz -> Scrollen möglich
					leftPos = mouseDownLeftPos + (float) (mouseDown.x - x);
					if (leftPos < 0)
					{
						leftPos = 0;
					}
					if (leftPos > maxWidth - maxTextWidth)
					{
						leftPos = maxWidth - maxTextWidth;
					}
				}
			}
			moveSelectionMarkers((oldLeftPos - leftPos), (topLine - oldTopLine) * lineHeight);
		}
		GL.that.renderOnce("EditWrapedTextField");

		// Scrollen nach oben / unten soll möglich sein trotzdem dass hier evtl. schon links / rechts gescrollt wird ????
		return bearbeitet;
	};

	private float getMaxDisplayTextWidth()
	{
		float result = 0;
		for (DisplayText dt : displayText)
		{
			float w = dt.getWidth();
			if (w > result) result = w;
		}
		return result;
	}

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

	private Cursor getClickCursor(int X, int Y)
	{
		float x = (float) X;
		float y = (float) Y;

		x = x - style.backgroundFocused.getLeftWidth() + leftPos;

		// Zeile bestimmen, in die geklickt wurde
		float clickPos = y;
		int clickedCursor = 0;
		int clickedCursorLine = (int) ((this.height - style.font.getLineHeight() - clickPos + (lineHeight)) / lineHeight) - 1;
		clickedCursorLine += topLine;
		if (clickedCursorLine < 0) return null;
		if (clickedCursorLine >= displayText.size()) return null;

		DisplayText dt = displayText.get(clickedCursorLine);

		for (int i = 0; i < dt.glyphPositions.size; i++)
		{
			float pos = dt.glyphPositions.items[i];
			if (pos > x)
			{
				clickedCursor = Math.max(0, i - 1);
				return new Cursor(clickedCursor, clickedCursorLine);
			}
		}
		clickedCursor = Math.max(0, dt.glyphPositions.size - 1);
		return new Cursor(clickedCursor, clickedCursorLine);
	}

	@Override
	public boolean click(int X, int Y, int pointer, int button)
	{
		if (pointer != 0) return false;
		GL.that.setKeyboardFocus(this);
		keyboard.show(true);
		clearSelection();
		cursorOn = false;

		Cursor newCursor = getClickCursor(X, Y);
		if (newCursor == null) return false;

		cursor.pos = newCursor.pos;
		setCursorLine(newCursor.line, true);
		GL.that.renderOnce("EditWrapedTextField");
		showSelectionMarker(SelectionMarker.Type.Center);
		return true;
	}

	@Override
	public boolean doubleClick(int x, int y, int pointer, int button)
	{
		// Doppelklick markiert Wort unter dem Cursor und setzt 2 Marker
		if (pointer != 0) return false;
		clearSelection();

		Cursor newCursor = getClickCursor(x, y);
		if (newCursor == null) return false;

		DisplayText dt = getDisplayText(newCursor.line);
		if (dt == null) return false;

		if (newCursor.pos < 0) return false;
		if (newCursor.pos >= dt.getDisplayText().length()) return false;

		Cursor cursorStart = null;
		Cursor cursorEnd = null;
		// Wortanfang und Wortende suchen
		for (int i = newCursor.pos; i >= 0; i--)
		{
			if (dt.getDisplayText().charAt(i) == ' ')
			{
				cursorStart = new Cursor(i + 1, newCursor.line);
				break;
			}
		}
		if (cursorStart == null) cursorStart = new Cursor(0, newCursor.line);
		for (int i = newCursor.pos; i < dt.getDisplayText().length(); i++)
		{
			if (dt.getDisplayText().charAt(i) == ' ')
			{
				cursorEnd = new Cursor(i, newCursor.line);
				break;
			}
		}
		if (cursorEnd == null) cursorEnd = new Cursor(dt.getDisplayText().length(), newCursor.line);
		if ((cursorStart == null) || (cursorEnd == null)) return false;
		if (cursorStart.pos >= cursorEnd.pos) return false;
		hideSelectionMarker();

		selection = new Selection(cursorStart, cursorEnd);
		showSelectionMarker(SelectionMarker.Type.Left, selection.cursorStart);
		showSelectionMarker(SelectionMarker.Type.Right, selection.cursorEnd);
		return true;
	}

	protected void showSelectionMarker(SelectionMarker.Type type)
	{
		showSelectionMarker(type, cursor);
	}

	protected void showSelectionMarker(SelectionMarker.Type type, Cursor tmpCursor)
	{

		GL.that.showMarker(type);

		switch (type)
		{
		case Center:

			GL.that.selectionMarkerCenterMoveTo(getCursorX(tmpCursor) + style.cursor.getMinWidth() / 2, getCursorY(tmpCursor.line));
			break;
		case Left:

			GL.that.selectionMarkerLeftMoveTo(getCursorX(tmpCursor) + style.cursor.getMinWidth() / 2, getCursorY(tmpCursor.line));
			break;
		case Right:

			GL.that.selectionMarkerRightMoveTo(getCursorX(tmpCursor) + style.cursor.getMinWidth() / 2, getCursorY(tmpCursor.line));
			break;
		}

	}

	private void moveSelectionMarkers(float dx, float dy)
	{
		if (GL.that.selectionMarkerCenterisShown())
		{
			GL.that.selectionMarkerCenterMoveBy(dx, dy);
		}
		if (GL.that.selectionMarkerLeftisShown())
		{
			GL.that.selectionMarkerLeftMoveBy(dx, dy);
		}
		if (GL.that.selectionMarkerRightisShown())
		{
			GL.that.selectionMarkerRightMoveBy(dx, dy);
		}
	}

	protected void hideSelectionMarker()
	{
		GL.that.hideMarker();
	}

	public void moveSelectionMarker(SelectionMarker.Type type, int newCursor, int newCursorLine)
	{
		switch (type)
		{
		case Center:
			break;
		case Left:
			break;
		case Right:
			break;
		}
	}

	public Point GetNextCursorPos(Point touch, SelectionMarker.Type type, boolean setCursor)
	{
		float x = touch.x - style.backgroundFocused.getLeftWidth() + leftPos;
		float clickPos = touch.y + cursorHeight / 2;
		int clickedCursorLine = (int) ((this.height - style.font.getLineHeight() - clickPos + (lineHeight)) / lineHeight) - 1;
		clickedCursorLine += topLine;
		if (clickedCursorLine < 0) return null;
		if (clickedCursorLine >= displayText.size()) return null;

		DisplayText dt = displayText.get(clickedCursorLine);

		for (int i = 0; i < dt.glyphPositions.size; i++)
		{
			float pos = dt.glyphPositions.items[i];
			if (pos > x)
			{
				int tmpCursor = Math.max(0, i - 1);
				Point result = new Point((int) (getCursorX(new Cursor(tmpCursor, clickedCursorLine)) + style.cursor.getMinWidth() / 2),
						(int) (getCursorY(clickedCursorLine)));
				if (setCursor)
				{
					switch (type)
					{
					case Center:
						cursor.pos = tmpCursor;
						setCursorLine(clickedCursorLine, false);
						break;
					case Left:
						if (selection != null)
						{
							selection.cursorStart = new Cursor(tmpCursor, clickedCursorLine);
						}
						break;
					case Right:
						if (selection != null)
						{
							selection.cursorEnd = new Cursor(tmpCursor, clickedCursorLine);
						}
						break;
					}
				}
				return result;
			}
		}
		int tmpCursor = Math.max(0, dt.glyphPositions.size - 1);
		Point result = new Point((int) (getCursorX(new Cursor(tmpCursor, clickedCursorLine)) + style.cursor.getMinWidth() / 2),
				(int) (getCursorY(clickedCursorLine)));
		if (setCursor)
		{
			switch (type)
			{
			case Center:
				cursor.pos = tmpCursor;
				setCursorLine(clickedCursorLine, false);
				break;
			case Left:
				if (selection != null)
				{
					selection.cursorStart = new Cursor(tmpCursor, clickedCursorLine);
				}
				break;
			case Right:
				if (selection != null)
				{
					selection.cursorEnd = new Cursor(tmpCursor, clickedCursorLine);
				}
				break;
			}
		}
		return result;
	}

	public boolean keyDown(int keycode)
	{
		final BitmapFont font = style.font;

		displayTextLock.lock();
		try
		{

			if (GL.that.hasFocus(this))
			{
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
				{
					// paste
					if (keycode == Keys.V) pasteFromClipboard();
					// copy
					if (keycode == Keys.C || keycode == Keys.INSERT) copy();
				}
				else if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))
				{
					// paste
					if (keycode == Keys.INSERT) pasteFromClipboard();
					// cut
					if (keycode == Keys.FORWARD_DEL)
					{
						// if (hasSelection)
						// {
						// copy();
						// delete();
						// }
					}
					// selection
					if (keycode == Keys.LEFT)
					{
						// if (!hasSelection)
						// {
						// selectionStart = cursor;
						// hasSelection = true;
						// }
						cursor.pos--;
						checkCursorVisible(true);
					}
					if (keycode == Keys.RIGHT)
					{
						// if (!hasSelection)
						// {
						// selectionStart = cursor;
						// hasSelection = true;
						// }
						cursor.pos++;
						checkCursorVisible(true);
					}
					if (keycode == Keys.HOME)
					{
						// if (!hasSelection)
						// {
						// selectionStart = cursor;
						// hasSelection = true;
						// }
						cursor.pos = 0;
						// überprüfen, ob der Cursor sichtbar ist
						checkCursorVisible(true);
					}
					if (keycode == Keys.END)
					{
						// if (!hasSelection)
						// {
						// selectionStart = cursor;
						// hasSelection = true;
						// }
						cursor.pos = text.length();
						// überprüfen, ob der Cursor sichtbar ist
						checkCursorVisible(true);
					}

					/*
					 * cursor.pos = Math.max(0, cursor.pos); cursor.pos = Math.min(text.length(), cursor.pos); // überprüfen, ob der Cursor
					 * sichtbar ist checkCursorVisible(true);
					 */
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
				GL.that.renderOnce("EditWrapedTextField");

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
			cursor.pos = 0;
			// überprüfen, ob der Cursor sichtbar ist
			checkCursorVisible(true);
		}
		else
		{
			cursor.pos = dt.displayText.length();
			// überprüfen, ob der Cursor sichtbar ist
			checkCursorVisible(true);
		}
	}

	// bewegt den Cursor nach links - rechts
	public void cursorLeftRight(int i)
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		int newCursor = cursor.pos + i;
		if (newCursor > dt.displayText.length())
		{
			if (cursorUpDown(1)) cursor.pos = 0;
		}
		else if (newCursor < 0)
		{
			if (cursorUpDown(-1))
			{
				DisplayText newDt = getAktDisplayText();
				cursor.pos = newDt.displayText.length();
			}
		}
		else
		{
			cursor.pos = newCursor;
		}
		// überprüfen, ob der Cursor sichtbar ist
		// checkCursorVisible(true);
	}

	// fügt eine neue Zeile an der Cursor Position ein
	private void insertNewLine()
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		// aktuellen String bei Cursor-Position trennen
		String s1 = dt.displayText.toString().substring(0, cursor.pos);
		String s2 = dt.displayText.toString().substring(cursor.pos, dt.displayText.length());
		dt.displayText = s1;
		DisplayText newDt = new DisplayText(s2, style.font);
		displayText.add(cursor.line + 1, newDt);
		style.font.computeGlyphAdvancesAndPositions(dt.displayText, dt.glyphAdvances, dt.glyphPositions);
		style.font.computeGlyphAdvancesAndPositions(newDt.displayText, newDt.glyphAdvances, newDt.glyphPositions);
		setCursorLine(cursor.line + 1, true);
		cursor.pos = 0;

		int lineCount = displayText.size();

		sendLineCountChanged(lineCount, lineHeight * lineCount);

	}

	// bewegt den Cursor nach oben / unten. X-Position des Cursors soll möglichst gleich bleiben
	private boolean cursorUpDown(int i)
	{
		int newCursorLine = cursor.line + i;
		if (newCursorLine < 0) return false;
		if (newCursorLine >= displayText.size()) return false;
		DisplayText oldDt = displayText.get(cursor.line);
		// X-Koordinate von alter Cursor Position bestimmen
		float x = oldDt.glyphPositions.items[cursor.pos];
		// Cursor in neue Zeile plazieren
		setCursorLine(newCursorLine, true);
		// Cursor möglichst an gleiche x-Position plazieren
		setCursorXPos(x);
		return true;
	}

	// liefert das DisplayText-Object der aktuellen Zeile
	private DisplayText getAktDisplayText()
	{
		if (cursor.line < 0) return null;
		if (cursor.line >= displayText.size()) return null;
		DisplayText newDt = displayText.get(cursor.line);
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
	private void setCursorLine(int newCursorLine, boolean hideCursor)
	{
		cursor.line = newCursorLine;
		checkCursorVisible(hideCursor);
	}

	private void checkCursorVisible(boolean hideCursor)
	{
		if (hideCursor) hideSelectionMarker();
		// Cursorpos prüfen, ob ausserhalb sichtbaren Bereich (Oben-Unten)
		if (cursor.line - topLine >= maxLineCount)
		{
			topLine = cursor.line - maxLineCount + 1;
		}
		if (cursor.line < topLine)
		{
			topLine = cursor.line;
		}
		// links-Rechts
		// Cursor Pos in Pixeln vom Textanfang an
		DisplayText dt = getAktDisplayText();
		if (dt != null)
		{
			float xCursor = 0;
			if (cursor.pos < dt.glyphPositions.size)
			{
				xCursor = dt.glyphPositions.get(cursor.pos);
			}
			else
			{
				xCursor = dt.glyphPositions.get(dt.glyphPositions.size - 1);
			}
			// Prüfen, ob der Cursor links außen ist
			if (xCursor < leftPos)
			{
				leftPos = xCursor;
			}
			// Prüfen, ob der Cursr rechts außen ist
			if ((xCursor > leftPos + maxTextWidth) && (maxTextWidth > 0))
			{
				leftPos = xCursor - maxTextWidth;
			}
		}
	}

	// Cursor in aktueller Zeile an die gegebene X-Position (in Pixel) senden
	private void setCursorXPos(float xPos)
	{
		DisplayText dt = getAktDisplayText();
		if (dt == null) return;
		if (xPos <= 0)
		{
			cursor.pos = 0;
			checkCursorVisible(true);
			return;
		}
		for (int i = 0; i < dt.glyphPositions.size; i++)
		{
			float pos = dt.glyphPositions.items[i];
			if (pos > xPos)
			{
				cursor.pos = Math.max(0, i - 1);
				checkCursorVisible(true);
				return;
			}
		}
		// kein Passendes Zeichen gefunden an gegebener Position -> Cursor ans Ende setzen
		cursor.pos = Math.max(0, dt.glyphPositions.size - 1);
		checkCursorVisible(true);
	}

	/**
	 * Copies the contents of this TextField to the {@link Clipboard} implementation set on this TextField.
	 */
	@Override
	public void copyToClipboard()
	{
		if (selection != null)
		{
			String content = "";
			content = this.getSelectedText();
			clipboard.setContents(content);
		}
		// if (hasSelection && clipboard != null)
		// {
		// int minIndex = Math.min(cursor, selectionStart);
		// int maxIndex = Math.max(cursor, selectionStart);
		// clipboard.setContents(text.substring(minIndex, maxIndex));
		// }
	}

	@Override
	public void cutToClipboard()
	{
		copyToClipboard();
		delete();
	}

	/**
	 * Pastes the content of the {@link Clipboard} implementation set on this Textfield to this TextField.
	 */
	@Override
	public void pasteFromClipboard()
	{
		if (clipboard == null) return;
		if (selection != null)
		{
			// zuerst evtl. markierten Text löschen
			delete();
		}

		String[] contents = clipboard.getContents().split("\n");

		if ((contents != null) && (contents.length > 0))
		{
			boolean firstLine = true;
			// nach Zeilenvorschüben trennen
			for (String content : contents)
			{
				StringBuilder builder = new StringBuilder();
				content = content.replace("\b", "");
				if (!firstLine)
				{
					// bei jeder weiteren Zeile vor dem Einfügen einen Zeilenvorschub machen
					insertNewLine();
				}
				firstLine = false;
				// Zeile für Zeile
				for (int i = 0; i < content.length(); i++)
				{
					char c = content.charAt(i);
					if (style.font.containsCharacter(c)) builder.append(c);
				}
				content = builder.toString();

				DisplayText dt = getDisplayText(cursor.line);
				dt.displayText = dt.displayText.substring(0, cursor.pos) + content
						+ dt.displayText.substring(cursor.pos, dt.displayText.length());
				updateDisplayText(dt, true);
				cursor.pos += content.length();
				checkCursorVisible(true);
			}
			// else
			// {
			// int minIndex = Math.min(cursor, selectionStart);
			// int maxIndex = Math.max(cursor, selectionStart);
			//
			// text = (minIndex > 0 ? text.substring(0, minIndex) : "")
			// + (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
			// cursor = minIndex;
			// text = text.substring(0, cursor) + content + text.substring(cursor, text.length());
			// // updateDisplayText();
			// cursor = minIndex + content.length();
			// checkCursorVisible(true);
			// clearSelection();
			// }

		}
	}

	private void delete()
	{
		if (selection == null) return;
		// markierte Zeichen löschen
		String text = "";
		// Zeilenanfang bis Markierung Start
		DisplayText dt = getDisplayText(selection.cursorStart.line);
		if (dt == null) return;
		if (selection.cursorStart.pos > 0)
		{
			text = dt.getDisplayText().substring(0, selection.cursorStart.pos);
		}

		// Markierung Ende bis Zeilenende
		DisplayText dt2 = getDisplayText(selection.cursorEnd.line);
		if (dt2 == null) return;
		if (selection.cursorEnd.pos < dt2.getDisplayText().length())
		{
			text += dt2.getDisplayText().substring(selection.cursorEnd.pos, dt2.getDisplayText().length());
		}
		dt.displayText = text;
		updateDisplayText(dt, false);
		for (int i = selection.cursorEnd.line; i > selection.cursorStart.line; i--)
		{
			displayText.remove(i);
		}

		cursor.pos = selection.cursorStart.pos;
		cursor.line = selection.cursorStart.line;
		checkCursorVisible(true);
		clearSelection();

		// int minIndex = Math.min(cursor, selectionStart);
		// int maxIndex = Math.max(cursor, selectionStart);
		// text = (minIndex > 0 ? text.substring(0, minIndex) : "")
		// + (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
		// // updateDisplayText();
		// cursor = minIndex;
		// checkCursorVisible(true);
		// clearSelection();
	}

	public boolean keyTyped(char character)
	{
		final BitmapFont font = style.font;
		DisplayText dt = getAktDisplayText();
		if (dt == null || disabled) return false;

		if (GL.that.hasFocus(this))
		{
			if (character == BACKSPACE)
			{
				if (selection != null)
				{
					delete();
					sendKeyTyped(character);
					return true;
				}
				else
				{
					try
					{
						if (cursor.pos > 0)
						{
							dt.displayText = dt.displayText.substring(0, cursor.pos - 1)
									+ dt.displayText.substring(cursor.pos, dt.displayText.length());
							updateDisplayText(dt, true);
							cursor.pos--;
							checkCursorVisible(true);
							GL.that.renderOnce("EditWrapedTextField");
							sendKeyTyped(character);
							return true;
						}
						else
						{
							if (cursor.line > 0)
							{
								setCursorLine(cursor.line - 1, true);
								DisplayText dt2 = getAktDisplayText();
								cursor.pos = dt2.displayText.length();
								checkCursorVisible(true);
								dt2.displayText += dt.displayText;
								displayText.remove(cursor.line + 1);
								updateDisplayText(dt2, true);

								int lineCount = displayText.size();
								sendLineCountChanged(lineCount, lineHeight * lineCount);
							}
							GL.that.renderOnce("EditWrapedTextField");
							sendKeyTyped(character);
							return true;
						}
					}
					catch (Exception e)
					{
						return true;
					}
				}
			}
			if (character == DELETE)
			{
				if (selection != null)
				{
					delete();
					return true;
				}
				else
				{
					if (cursor.pos < dt.displayText.length())
					{
						dt.displayText = dt.displayText.substring(0, cursor.pos)
								+ dt.displayText.substring(cursor.pos + 1, dt.displayText.length());
						updateDisplayText(dt, true);
						GL.that.renderOnce("EditWrapedTextField");
						sendKeyTyped(character);
						return true;
					}
					else
					{
						if (cursor.line + 1 < displayText.size())
						{
							cursor.line++;
							DisplayText dt2 = getAktDisplayText();
							cursor.line--;
							displayText.remove(dt2);
							dt.displayText += dt2.displayText;
							updateDisplayText(dt, true);
							GL.that.renderOnce("EditWrapedTextField");
						}
						sendKeyTyped(character);
						return true;
					}
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
				if (isMultiLine())
				{
					if (selection != null) delete();
					insertNewLine();
					clearSelection();
					sendKeyTyped(character);
					return true;
				}
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
				if (selection != null) delete();
				// if (!hasSelection)
				{
					dt.displayText = dt.displayText.substring(0, cursor.pos) + character
							+ dt.displayText.substring(cursor.pos, dt.displayText.length());
					updateDisplayText(dt, true);
					cursor.pos++;
					checkCursorVisible(true);
				}
				// else
				// {
				// int minIndex = Math.min(cursor, selectionStart);
				// int maxIndex = Math.max(cursor, selectionStart);
				//
				// text = (minIndex > 0 ? text.substring(0, minIndex) : "")
				// + (maxIndex < text.length() ? text.substring(maxIndex, text.length()) : "");
				// cursor = minIndex;
				// text = text.substring(0, cursor) + character + text.substring(cursor, text.length());
				// // updateDisplayText();
				// cursor++;
				// checkCursorVisible(true);
				// clearSelection();
				// }
				GL.that.renderOnce("EditWrapedTextField");
			}
			sendKeyTyped(character);
			if (passwordMode) updateDisplayTextList();
			return true;
		}
		else
			return false;
	}

	private boolean isMultiLine()
	{
		if ((type == TextFieldType.MultiLine) || (type == TextFieldType.MultiLineWraped)) return true;
		else
			return false;
	}

	private boolean isWraped()
	{
		return type == TextFieldType.MultiLineWraped;
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
		cursor.pos = 0;
		checkCursorVisible(true);
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

		String ret = sb.toString();

		if (this.type == TextFieldType.SingleLine)
		{
			ret = ret.replace("\n", "");
			ret = ret.replace("\r", "");
		}

		return ret;
	}

	public String getSelectedText()
	{
		if (selection == null) return "";

		StringBuilder sb = new StringBuilder();

		// Alle Zeilen durchgehen, in denen selectierter Text liegt
		for (int line = selection.cursorStart.line; line <= selection.cursorEnd.line; line++)
		{
			DisplayText dt = getDisplayText(line);

			int startPos = 0;
			int endPos = dt.displayText.length();

			if (line == selection.cursorStart.line)
			{
				startPos = selection.cursorStart.pos;
			}
			if (line == selection.cursorEnd.line)
			{
				endPos = selection.cursorEnd.pos;
			}

			sb.append(dt.displayText.substring(startPos, endPos));
			if (line < selection.cursorEnd.line)
			{
				sb.append(GlobalCore.br);
			}

		}
		String ret = sb.toString();

		if (this.type == TextFieldType.SingleLine)
		{
			ret = ret.replace("\n", "");
			ret = ret.replace("\r", "");
		}

		return ret;
	}

	/** Sets the selected text. */
	public void setSelection(int selectionStart, int selectionEnd)
	{
		// if (selectionStart < 0) throw new IllegalArgumentException("selectionStart must be >= 0");
		// if (selectionEnd < 0) throw new IllegalArgumentException("selectionEnd must be >= 0");
		// selectionStart = Math.min(text.length(), selectionStart);
		// selectionEnd = Math.min(text.length(), selectionEnd);
		// if (selectionEnd == selectionStart)
		// {
		// clearSelection();
		// return;
		// }
		// if (selectionEnd < selectionStart)
		// {
		// int temp = selectionEnd;
		// selectionEnd = selectionStart;
		// selectionStart = temp;
		// }
		//
		// hasSelection = true;
		// this.selectionStart = selectionStart;
		// cursor = selectionEnd;
		// checkCursorVisible(true);
	}

	public void clearSelection()
	{
		selection = null;
		// hasSelection = false;
	}

	/** Sets the cursor position and clears any selection. */
	public void setCursorPosition(int cursorPosition)
	{
		if (cursorPosition < 0) throw new IllegalArgumentException("cursorPosition must be >= 0");
		clearSelection();
		cursor.pos = Math.min(cursorPosition, text.length());
		checkCursorVisible(true);
	}

	public int getCursorPosition()
	{
		return cursor.pos;
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

	@Override
	public boolean keyUp(int KeyCode)
	{
		return true;
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

	public float getMeasuredHeight()
	{

		float h = 0;

		for (DisplayText text : displayText)
		{
			h += text.getTextBounds().height;
		}

		return h;
	}

	private class Cursor
	{
		public int pos;
		public int line;

		public Cursor(int pos, int line)
		{
			this.pos = pos;
			this.line = line;
		}
	}

	private class Selection
	{
		public Cursor cursorStart;
		public Cursor cursorEnd;

		public Selection(Cursor cursorStart, Cursor cursorEnd)
		{
			this.cursorStart = cursorStart;
			this.cursorEnd = cursorEnd;
		}
	}

	public BitmapFont getFont()
	{
		return style.font;
	}

	public void setFocus()
	{
		setFocus(true);
	}

	public void setFocus(boolean value)
	{
		hasFocus = value;
		if (value == true) GL.that.setKeyboardFocus(this);
		else
		{
			if (GL.that.getKeyboardFocus() == this) GL.that.setKeyboardFocus(null);
		}
	}

	public void resetFocus()
	{
		hasFocus = false;
		GL.that.setKeyboardFocus(null);

	}

	public void setPasswordMode()
	{
		passwordMode = true;
	}

	public void dontShowSoftKeyBoardOnFocus()
	{
		dontShowSoftKeyBoardOnFocus(true);
	}

	public void dontShowSoftKeyBoardOnFocus(boolean value)
	{
		dontShowKeyBoard = value;
	}

	public boolean dontShowKeyBoard()
	{
		return dontShowKeyBoard;
	}
}
