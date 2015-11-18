/* 
 * Copyright (C) 2014-2015 team-cachebox.de
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Global;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Lists.CB_List;
import CB_Utils.Math.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.FloatArray;

public class EditTextField extends EditTextFieldBase {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(EditTextField.class);

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
    protected boolean isEditable = true;

    protected TextFieldStyle style;
    protected String text, messageText; // both in todo state
    protected ArrayList<Line> lines;
    protected Cursor cursor = new Cursor(0, 0);
    protected float cursorHeight;
    /**
     * oberste sichtbare Zeile ist displayText(topline)
     */
    protected int topLine;
    /**
     * Anzahl der sichtbaren Zeilen
     */
    protected int maxLineCount;
    /**
     * Anzahl der Pixel, um die der Text der aktuellen Zeile nach links gescrollt ist
     */
    protected float leftPos;
    /**
     * Anzahl der sichtbaren Pixel
     */
    protected float maxTextWidth;
    protected TextFieldFilter filter;
    protected OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

    protected boolean passwordMode;
    protected StringBuilder passwordBuffer;

    protected final Rectangle fieldBounds = new Rectangle();
    protected final Rectangle scissor = new Rectangle();
    //    protected float renderOffset, textOffset;
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
    float bgLeftWidth = 0;
    float bgRightWidth = 0;

    private WrapType mWrapType = WrapType.SINGLELINE;

    public EditTextField(String name) {
	super(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight(), null, name);
	initEditTextField();
    }

    public EditTextField(CB_View_Base parent, String name) {
	super(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight(), parent, name);
	initEditTextField();
    }

    public EditTextField(CB_RectF rec, CB_View_Base parent, String name) {
	super(rec, parent, name);
	initEditTextField();
    }

    public EditTextField(CB_View_Base parent, CB_RectF rec, TextFieldStyle style, String Name) {
	super(rec, parent, Name);
	if (style != null)
	    this.style = style;
	initEditTextField();
    }

    public EditTextField(CB_View_Base parent, CB_RectF rec, TextFieldStyle style, String Name, WrapType WrapType) {
	this(parent, rec, style, Name);
	if (WrapType != null)
	    this.mWrapType = WrapType;
    }

    public EditTextField(CB_View_Base parent, CB_RectF rec, WrapType WrapType, String Name) {
	super(rec, parent, Name);
	if (WrapType != null)
	    this.mWrapType = WrapType;
	initEditTextField();
    }

    private void initEditTextField() {
	this.style = getDefaultStyle();
	lines = new ArrayList<Line>();
	setCursorLine(0, true);
	setText("");
	topLine = 0;
	leftPos = 0;
	calculateSizeDependences(GL.that.hasFocus(this));
	this.setClickable(true);
    }

    @Override
    public void onResized(CB_RectF rec) {
	thisInvalidate = true;
	topLine = 0;
	leftPos = 0;
	calculateSizeDependences(GL.that.hasFocus(this));
    }

    public EditTextField setWrapType(WrapType WrapType) {
	if (WrapType != null) {
	    if (WrapType != mWrapType) {
		mWrapType = WrapType;
		// todo ? make corresponding changes;
	    }
	}
	return this;
    }

    @Override
    public void onShow() {
	super.onShow();

    }

    @Override
    public void onHide() {
	super.onHide();
    }

    @Override
    protected void Initial() {
    }

    @Override
    protected void SkinIsChanged() {
    }

    public void setStyle(TextFieldStyle style) {
	if (style == null)
	    throw new IllegalArgumentException("style cannot be null.");
	this.style = style;
    }

    public void setPasswordCharacter(char passwordCharacter) {
	this.passwordCharacter = passwordCharacter;
    }

    /**
     * Returns the text field's style. Modifying the returned style may not have an effect until {@link #setStyle(TextFieldStyle)} is
     * called.
     */
    public TextFieldStyle getStyle() {
	return style;
    }

    /**
     * calculates bgLeftWidth, bgRightWidth, bgTopHeight, bgBottomHeight
     * @param focused
     */
    private void calculateSizeDependences(boolean focused) {
	bgLeftWidth = 0;
	bgRightWidth = 0;
	bgTopHeight = 0;
	bgBottomHeight = 0;

	if (focused) {
	    if (style.backgroundFocused != null) {
		bgLeftWidth = style.backgroundFocused.getLeftWidth();
		bgRightWidth = style.background.getRightWidth();
		if (mWrapType == WrapType.SINGLELINE) {
		    bgTopHeight = (getHeight() - this.style.font.getLineHeight()) / 2;
		    bgBottomHeight = (getHeight() - this.style.font.getLineHeight()) / 2;
		} else {
		    bgTopHeight = style.background.getTopHeight();
		    bgBottomHeight = style.background.getBottomHeight();
		}
	    }
	} else {
	    if (style.background != null) {
		bgLeftWidth = style.background.getLeftWidth();
		bgRightWidth = style.background.getRightWidth();
		if (mWrapType == WrapType.SINGLELINE) {
		    bgTopHeight = (getHeight() - this.style.font.getLineHeight()) / 2;
		    bgBottomHeight = (getHeight() - this.style.font.getLineHeight()) / 2;
		} else {
		    bgTopHeight = style.background.getTopHeight();
		    bgBottomHeight = style.background.getBottomHeight();
		}
	    }
	}
	maxLineCount = (int) ((getHeight() - bgTopHeight - bgBottomHeight) / this.style.font.getLineHeight()) - 1;
	maxTextWidth = getWidth() - bgLeftWidth - bgRightWidth;
	this.setTextAtCursorVisible(true); //maxLineCount changed
    }

    @Override
    protected void render(Batch batch) {
	if (this.isDisposed())
	    return;

	displayTextLock.lock();
	// log.debug(this.name + " render");
	try {
	    boolean focused = GL.that.hasFocus(this);
	    if (focused) {
		this.setTextAtCursorVisible(false);
		if (style.backgroundFocused != null) {
		    style.backgroundFocused.draw(batch, x, y, getWidth(), getHeight());
		}
	    } else {
		if (style.background != null) {
		    style.background.draw(batch, x, y, getWidth(), getHeight());
		}
	    }

	    // Background is drawn, now set scissor to inner rec
	    batch.end();

	    CB_RectF innerScissorReg = intersectRec.copy();
	    innerScissorReg.setHeight(intersectRec.getHeight() - bgTopHeight - bgBottomHeight);
	    innerScissorReg.setY(intersectRec.getY() + bgBottomHeight);

	    batch.begin();

	    Gdx.gl.glScissor((int) innerScissorReg.getX(), (int) innerScissorReg.getY(), (int) innerScissorReg.getWidth() + 1, (int) innerScissorReg.getHeight() + 1);

	    float textY = (int) getHeight() - bgTopHeight + style.font.getDescent();

	    if (selection != null) {
		// Selection zeilenweise durchgehen
		for (int lineNo = selection.cursorStart.line; lineNo <= selection.cursorEnd.line; lineNo++) {
		    Line line = getNthLine(lineNo);
		    if (line == null)
			continue;
		    int start = 0;
		    if (lineNo == selection.cursorStart.line)
			start = selection.cursorStart.pos;
		    int end = line.displayText.length();
		    if (lineNo == selection.cursorEnd.line)
			end = selection.cursorEnd.pos;
		    float selectionX = line.glyphPositions.get(start);
		    float selectionY = line.glyphPositions.get(end);
		    float selectionWidth = selectionY - selectionX;
		    style.selection.draw(batch, x + selectionX + bgLeftWidth - leftPos, y + textY + this.style.font.getLineHeight() * (topLine - 1 - lineNo) - style.font.getDescent() / 2, selectionWidth, this.style.font.getLineHeight());
		}
	    }

	    if ((lines.size() == 1) && (lines.get(0).displayText.length() == 0)) {
		if (!focused && messageText != null) {
		    if (style.messageFontColor != null) {
			style.font.setColor(style.messageFontColor.r, style.messageFontColor.g, style.messageFontColor.b, style.messageFontColor.a);
		    } else {
			style.font.setColor(0.7f, 0.7f, 0.7f, 1f);
		    }

		    style.font.draw(batch, messageText, x + bgLeftWidth, y + textY);
		}
	    } else {
		style.font.setColor(style.fontColor.r, style.fontColor.g, style.fontColor.b, style.fontColor.a); //?
		textY += this.style.font.getLineHeight() * topLine;
		for (Line line : lines) {
		    style.font.draw(batch, line.displayText, x + bgLeftWidth - leftPos, y + textY + mouseTempMove);
		    textY -= this.style.font.getLineHeight();
		}
	    }

	    if (focused) {
		if (cursorOn && style.cursor != null) {
		    textY = (int) getHeight() - bgTopHeight + style.font.getDescent();
		    cursorHeight = this.style.font.getLineHeight() + style.font.getDescent() / 2;
		    float xpos = getCursorX();
		    style.cursor.draw(batch, xpos, getCursorY() + cursorHeight + style.font.getDescent(), style.cursor.getMinWidth(), cursorHeight);
		}
	    }

	    if (focused && (selection == null)) {
		if (blinkTimer == null)
		    blinkStart();
	    } else {
		if (blinkTimer != null)
		    blinkStop();
	    }
	} catch (Exception e) {
	} finally {
	    displayTextLock.unlock();
	}
    }

    private float getCursorX() {
	return getCursorX(cursor);
    }

    private float getCursorX(Cursor aCursor) {

	float xpos = 0;

	if (lines.size() > aCursor.line) {
	    Line line = lines.get(aCursor.line);

	    if (aCursor.pos < line.glyphPositions.size) {
		xpos = line.glyphPositions.get(aCursor.pos);
	    } else if (line.glyphPositions.size == 0) {
		xpos = 0;
	    } else {
		xpos = line.glyphPositions.get(line.glyphPositions.size - 1); // letztes Zeichen
	    }
	}

	return x + style.background.getLeftWidth() + xpos - 1 - leftPos;
    }

    private float getCursorY() {
	return getCursorY(cursor.line);
    }

    private float getCursorY(int aCursorLine) {
	float textY = (int) getHeight() - bgTopHeight + style.font.getDescent();
	return (int) (y + textY - this.style.font.getLineHeight() * (aCursorLine - topLine + 1.5));
    }

    private void updateDisplayTextList() {
	lines.clear();
	if (passwordMode && style.font.getData().hasGlyph(passwordCharacter)) {
	    if (passwordBuffer == null)
		passwordBuffer = new StringBuilder(text.length());
	    if (passwordBuffer.length() > text.length()) //
		passwordBuffer.setLength(text.length());
	    else {
		for (int i = passwordBuffer.length(), n = text.length(); i < n; i++)
		    passwordBuffer.append(passwordCharacter);
	    }
	    lines.add(new Line(new String(passwordBuffer), style.font));
	} else {
	    String[] dts = text.split("\n");
	    for (String s : dts)
		lines.add(new Line(s, style.font));
	}

	for (Line line : lines) {
	    computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
	}

	int lineCount = lines.size();
	sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);
    }

    // Wenn calcCursor == true -> Cursorposition wird evtl. angepasst, sonst nicht
    private void updateDisplayText(Line line, boolean calcCursor) {
	float maxWidth = getWidth() - 50; // noch falsch!!!!!!!!!!!!!!!!!!!!!
	// wenn dies eine autoWrap Zeile ist muss zuerst die Zeile davor überprüft werden, 
	// ob die ersten Zeichen dieser Zeile dahinein kopiert werden können
	if (line.autoWrap) {
	    Line previousLine = getNthLine(cursor.line - 1);
	    if (previousLine != null) {
		// Restlichen Platz suchen
		float rest = maxWidth - previousLine.getWidth(); // Restlicher Platz der vorherigen Zeile
		// Breite des ersten Wortes incl. abschließendem Leerzeichen suchen

		int idWord = 0;
		for (int i = 0; i < line.displayText.length(); i++) {
		    char c = line.displayText.charAt(i);
		    float pos = line.glyphPositions.get(i + 1);
		    // Prüfen, ob aktuelles Zeichen ein Leerzeichen ist und ob das Ende nicht weiter als "rest" vom Start der Linie entfernt
		    // ist
		    if ((c == ' ') && (pos <= rest)) {
			idWord = i + 1;
		    } else if (pos > rest) {
			break;
		    }
		}
		if (idWord == 0) {
		    // Prüfen, ob komplette nächste Zeile in diese rein passt
		    if (line.getWidth() <= rest) {
			idWord = line.displayText.length();
		    }
		}
		if (idWord > 0) {
		    // dieses erste Wort passt noch in die letzte Zeile
		    String s1 = line.displayText.toString().substring(0, idWord);
		    String s2 = line.displayText.toString().substring(idWord, line.displayText.length());
		    previousLine.displayText += s1;
		    computeGlyphAdvancesAndPositions(previousLine.displayText, previousLine.glyphAdvances, previousLine.glyphPositions);
		    line.displayText = s2;
		    if (s2.length() == 0) {
			// komplette Zeile löschen
			lines.remove(line);
			int lineCount = lines.size();
			sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);
			// 2014-07-09: cursor.pos und idWord = 0 setzen damit später der Cursor auf das Ende der vorherigen Zeile gesetzt
			// wird
			cursor.pos = 0;
			idWord = 0;
		    } else {
			computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
		    }
		    if (cursor.pos > idWord) {
			cursor.pos -= idWord; // Cursor ist hinter den Zeichen die in die vorherige Zeile verschoben werden -> nach Vorne
					      // setzen
		    } else {
			// cursor ist innerhalb der Zeichen, die in die vorherige Zeile verschoben werden -> Cursor in die vorherige Zeile
			// verschieben
			cursor.pos = previousLine.displayText.length() + 1/* - 1 */;
			setCursorLine(cursor.line - 1, true);
			// anschließende Zeile noch mal berechnen.
			cursor.line++;
			Line nextLine = getCursorsLine();
			if (nextLine != null) {
			    updateDisplayText(nextLine, false);
			}
			cursor.line--;
			return;
		    }
		}
	    }
	}
	computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
	float len = line.getWidth();

	// Prüfen, ob Zeile zu lang geworden ist und ob am Ende Zeichen in die nächste Zeile verschoben werden müssen
	if ((len > maxWidth) && isWraped()) {
	    // automatischen Umbruch einfügen
	    // erstes Zeichen suchen, das außerhalb des max. Bereichs liegt
	    int id = 0;
	    for (int i = 1; i < line.glyphPositions.size; i++) {
		// abschließende Leerzeichen hier nicht berücksichtigen
		if ((line.displayText.length() > i - 1) && (line.displayText.charAt(i - 1) == ' '))
		    continue;
		float pos = line.glyphPositions.get(i);
		if (pos > maxWidth)
		    id = i;
	    }
	    if (id > 0) {
		if (!calcCursor) {
		    calcCursor = false;
		}
		// Zeile Trennen nach dem letzten "Space" vor dem Zeichen id
		for (int j = id - 1; j >= 0; j--) {
		    if (line.displayText.charAt(j) == ' ') {
			id = j + 1;
			break;
		    }
		}
		// Zeilenumbruch an Zeichen id
		// aktuellen String bei id trennen
		String s1 = line.displayText.toString().substring(0, id);
		String s2 = line.displayText.toString().substring(id, line.displayText.length());
		line.displayText = s1;
		computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
		cursor.line++;
		// Text der nächsten Zeile holen und prüfen, ob dies eine durch einen autoWrap eingefügte Zeile ist
		Line nextLine = getCursorsLine();
		cursor.line--;
		if ((nextLine != null) && nextLine.autoWrap) {
		    // Umzubrechnenden Text am Anfang von nextLine anfügen
		    nextLine.displayText = s2 + nextLine.displayText;
		    computeGlyphAdvancesAndPositions(nextLine.displayText, nextLine.glyphAdvances, nextLine.glyphPositions);
		    cursor.line++;
		    updateDisplayText(nextLine, false);
		    cursor.line--;
		} else {
		    // neue Zeile erstellen
		    Line newLine = new Line(s2, true, style.font);
		    lines.add(cursor.line + 1, newLine);
		    computeGlyphAdvancesAndPositions(newLine.displayText, newLine.glyphAdvances, newLine.glyphPositions);
		}
		if (calcCursor && (cursor.pos >= id)) {
		    // Cursor auch in die nächste Zeile verschieben, an die Stelle im Wort an der der Cursor vorher auch war
		    cursor.pos = cursor.pos - id;
		    setCursorLine(cursor.line + 1, true);
		}
	    }
	}
	// Prüfen, ob am Ende der Zeile wieder Platz für Zeichen / Wörter der vorgänger-Zeile ist
	float rest = maxWidth - line.getWidth(); // Restlicher Platz
	// Wenn anschließend eine Wraped-Zeile kommt dann erstes Word dessen suchen und Prüfen, ob dies hier eingefügt werden kann
	Line nextLine = getNthLine(cursor.line + 1);
	if ((nextLine != null) && (nextLine.autoWrap)) {
	    // Breite des ersten Wortes incl. abschließendem Leerzeichen suchen
	    int idWord = 0;
	    for (int i = 0; i < nextLine.displayText.length(); i++) {
		char c = nextLine.displayText.charAt(i);
		float pos = nextLine.glyphPositions.get(i + 1);
		// Prüfen, ob aktuelles Zeichen ein Leerzeichen ist und ob das Ende nicht weiter als "rest" vom Start der Linie entfernt ist
		if ((c == ' ') && (pos <= rest)) {
		    idWord = i + 1;
		} else if (pos > rest) {
		    break;
		}
	    }
	    if (idWord == 0) {
		// Prüfen, ob komplette nächste Zeile in diese rein passt
		if (nextLine.getWidth() <= rest) {
		    idWord = nextLine.displayText.length();
		}
	    }
	    if (idWord > 0) {
		// dieses erste Wort passt noch in die letzte Zeile
		String s1 = nextLine.displayText.toString().substring(0, idWord);
		String s2 = nextLine.displayText.toString().substring(idWord, nextLine.displayText.length());
		line.displayText += s1;
		computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
		updateDisplayText(line, false);
		nextLine.displayText = s2;
		if (s2.length() == 0) {
		    // komplette Zeile löschen
		    lines.remove(nextLine);
		} else {
		    cursor.line++;
		    computeGlyphAdvancesAndPositions(nextLine.displayText, nextLine.glyphAdvances, nextLine.glyphPositions);
		    updateDisplayText(nextLine, false);
		    cursor.line--;
		}
	    }
	} else {
	    // anschließend kommt keine Zeile, die automatisch umgebrochen wurde -> nichts machen
	}
    }

    private Point mouseDown = null;
    private float mouseDownTopLine = 0;
    private int mouseTempMove = 0;
    private float mouseDownLeftPos = 0;

    @Override
    public boolean onTouchDown(int X, int Y, int pointer, int button) {
	mouseDown = new Point(X, Y);
	// topLine merken, zu dem Zeitpunkt als die Maus gedrückt wurde
	mouseDownTopLine = topLine;
	// leftPos merken, zu dem Zeitpunkt als die Maus gedrückt wurde
	mouseDownLeftPos = leftPos;
	return true;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
	boolean bearbeitet = false;
	if (mouseDown != null) {
	    float oldTopLine = topLine;
	    float oldLeftPos = leftPos;
	    if (isMultiLine()) {

		// Scrollen Oben - Unten
		if (lines.size() < maxLineCount) {
		    topLine = 0;
		} else {
		    topLine = (int) (mouseDownTopLine + (y - mouseDown.y) / this.style.font.getLineHeight());
		    if (topLine < 0) {
			topLine = 0;
		    }

		    if (lines.size() - topLine < maxLineCount) {
			topLine = lines.size() - maxLineCount;
		    }
		}
		bearbeitet = true;
	    }
	    if (!isWraped()) {
		// Scrollen Links - Rechts
		float maxWidth = getMaxDisplayTextWidth();
		if (maxWidth < maxTextWidth) {
		    // Text hat auf einmal Platz -> auf Ursprung hin scrollen
		    leftPos = 0;
		} else {
		    // Text hat nicht auf einmal Platz -> Scrollen möglich
		    leftPos = mouseDownLeftPos + (mouseDown.x - x);
		    if (leftPos < 0) {
			leftPos = 0;
		    }
		    if (leftPos > maxWidth - maxTextWidth) {
			leftPos = maxWidth - maxTextWidth;
		    }
		}
	    }
	    moveSelectionMarkers((oldLeftPos - leftPos), (topLine - oldTopLine) * this.style.font.getLineHeight());
	    callListPosChangedEvent();
	}
	GL.that.renderOnce();

	// Scrollen nach oben / unten soll möglich sein trotzdem dass hier evtl. schon links / rechts gescrollt wird ????
	return bearbeitet;
    };

    private float getMaxDisplayTextWidth() {
	float result = 0;
	for (Line line : lines) {
	    float w = line.getWidth();
	    if (w > result)
		result = w;
	}
	return result;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
	if (mouseDown != null) {
	    mouseTempMove = y - mouseDown.y;

	    mouseTempMove = 0;
	}
	mouseDown = null;
	return false;
    }

    private Cursor getClickCursor(int X, int Y) {
	float lx = X;
	float ly = Y;

	lx = lx - style.backgroundFocused.getLeftWidth() + leftPos;

	// Zeile bestimmen, in die geklickt wurde
	float clickPos = ly;
	int clickedCursor = 0;
	int clickedCursorLine = (int) ((this.getHeight() - clickPos) / this.style.font.getLineHeight()) - 1;
	clickedCursorLine += topLine;
	if (clickedCursorLine < 0)
	    clickedCursorLine = 0;
	if (clickedCursorLine >= lines.size())
	    return null;

	Line line = lines.get(clickedCursorLine);

	for (int i = 0; i < line.glyphPositions.size; i++) {
	    float pos = line.glyphPositions.items[i];
	    if (pos > lx) {
		clickedCursor = Math.max(0, i - 1);
		return new Cursor(clickedCursor, clickedCursorLine);
	    }
	}
	clickedCursor = Math.max(0, line.glyphPositions.size - 1);
	return new Cursor(clickedCursor, clickedCursorLine);
    }

    @Override
    public boolean click(int X, int Y, int pointer, int button) {
	if (pointer != 0)
	    return false;
	GL.that.setKeyboardFocus(this);
	setTextAtCursorVisible(false);
	if (!dontShowKeyBoard())
	    keyboard.show(true);
	clearSelection();
	cursorOn = false;

	Cursor newCursor = getClickCursor(X, Y);
	if (newCursor == null)
	    return false;

	cursor.pos = newCursor.pos;
	setCursorLine(newCursor.line, true);
	GL.that.renderOnce();
	showSelectionMarker(SelectionMarker.Type.Center);
	return true;
    }

    @Override
    public boolean doubleClick(int x, int y, int pointer, int button) {
	// Doppelklick markiert Wort unter dem Cursor und setzt 2 Marker
	if (pointer != 0)
	    return false;
	clearSelection();

	Cursor newCursor = getClickCursor(x, y);
	if (newCursor == null)
	    return false;

	Line line = getNthLine(newCursor.line);
	if (line == null)
	    return false;

	if (newCursor.pos < 0)
	    return false;
	if (newCursor.pos >= line.displayText.length())
	    return false;

	Cursor cursorStart = null;
	Cursor cursorEnd = null;
	// Wortanfang und Wortende suchen
	for (int i = newCursor.pos; i >= 0; i--) {
	    if (line.displayText.charAt(i) == ' ') {
		cursorStart = new Cursor(i + 1, newCursor.line);
		break;
	    }
	}
	if (cursorStart == null)
	    cursorStart = new Cursor(0, newCursor.line);
	for (int i = newCursor.pos; i < line.displayText.length(); i++) {
	    if (line.displayText.charAt(i) == ' ') {
		cursorEnd = new Cursor(i, newCursor.line);
		break;
	    }
	}
	if (cursorEnd == null)
	    cursorEnd = new Cursor(line.displayText.length(), newCursor.line);
	if ((cursorStart == null) || (cursorEnd == null))
	    return false;
	if (cursorStart.pos >= cursorEnd.pos)
	    return false;
	hideSelectionMarker();

	selection = new Selection(cursorStart, cursorEnd);
	showSelectionMarker(SelectionMarker.Type.Left, selection.cursorStart);
	showSelectionMarker(SelectionMarker.Type.Right, selection.cursorEnd);
	return true;
    }

    protected void showSelectionMarker(SelectionMarker.Type type) {
	showSelectionMarker(type, cursor);
    }

    protected void showSelectionMarker(final SelectionMarker.Type type, final Cursor tmpCursor) {

	GL.that.showMarker(type);

	Timer v = new Timer();
	TimerTask ta = new TimerTask() {

	    @Override
	    public void run() {
		switch (type) {
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
	};

	v.schedule(ta, 700);
    }

    private void moveSelectionMarkers(float dx, float dy) {
	if (GL.that.selectionMarkerCenterisShown()) {
	    GL.that.selectionMarkerCenterMoveBy(dx, dy);
	}
	if (GL.that.selectionMarkerLeftisShown()) {
	    GL.that.selectionMarkerLeftMoveBy(dx, dy);
	}
	if (GL.that.selectionMarkerRightisShown()) {
	    GL.that.selectionMarkerRightMoveBy(dx, dy);
	}
    }

    protected void hideSelectionMarker() {
	GL.that.hideMarker();
    }

    public void moveSelectionMarker(SelectionMarker.Type type, int newCursor, int newCursorLine) {
	switch (type) {
	case Center:
	    break;
	case Left:
	    break;
	case Right:
	    break;
	}
    }

    public Point GetNextCursorPos(Point touch, SelectionMarker.Type type, boolean setCursor) {
	float lx = touch.x - style.backgroundFocused.getLeftWidth() + leftPos;
	float clickPos = touch.y + cursorHeight / 2;
	int clickedCursorLine = (int) ((this.getHeight() - clickPos) / this.style.font.getLineHeight()) - 1;
	clickedCursorLine += topLine;
	if (clickedCursorLine < 0)
	    return null;
	if (clickedCursorLine >= lines.size())
	    return null;

	Line line = lines.get(clickedCursorLine);

	for (int i = 0; i < line.glyphPositions.size; i++) {
	    float pos = line.glyphPositions.items[i];
	    if (pos > lx) {
		int tmpCursor = Math.max(0, i - 1);
		Point result = new Point((int) (getCursorX(new Cursor(tmpCursor, clickedCursorLine)) + style.cursor.getMinWidth() / 2), (int) (getCursorY(clickedCursorLine)));
		if (setCursor) {
		    switch (type) {
		    case Center:
			cursor.pos = tmpCursor;
			setCursorLine(clickedCursorLine, false);
			break;
		    case Left:
			if (selection != null) {
			    selection.cursorStart = new Cursor(tmpCursor, clickedCursorLine);
			}
			break;
		    case Right:
			if (selection != null) {
			    selection.cursorEnd = new Cursor(tmpCursor, clickedCursorLine);
			}
			break;
		    }
		}
		return result;
	    }
	}
	int tmpCursor = Math.max(0, line.glyphPositions.size - 1);
	Point result = new Point((int) (getCursorX(new Cursor(tmpCursor, clickedCursorLine)) + style.cursor.getMinWidth() / 2), (int) (getCursorY(clickedCursorLine)));
	if (setCursor) {
	    switch (type) {
	    case Center:
		cursor.pos = tmpCursor;
		setCursorLine(clickedCursorLine, false);
		break;
	    case Left:
		if (selection != null) {
		    selection.cursorStart = new Cursor(tmpCursor, clickedCursorLine);
		}
		break;
	    case Right:
		if (selection != null) {
		    selection.cursorEnd = new Cursor(tmpCursor, clickedCursorLine);
		}
		break;
	    }
	}
	return result;
    }

    @Override
    public boolean keyDown(int keycode) {
	displayTextLock.lock();
	try {

	    if (GL.that.hasFocus(this)) {
		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
		    // paste
		    if (keycode == Keys.V)
			pasteFromClipboard();
		    // copy
		    if (keycode == Keys.C || keycode == Keys.INSERT)
			copy();
		} else if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
		    // paste
		    if (keycode == Keys.INSERT)
			pasteFromClipboard();
		    // cut
		    if (keycode == Keys.FORWARD_DEL) {
			// if (hasSelection)
			// {
			// copy();
			// delete();
			// }
		    }
		    // selection
		    if (keycode == Keys.LEFT) {
			// if (!hasSelection)
			// {
			// selectionStart = cursor;
			// hasSelection = true;
			// }
			cursor.pos--;
			setTextAtCursorVisible(true);
		    }
		    if (keycode == Keys.RIGHT) {
			// if (!hasSelection)
			// {
			// selectionStart = cursor;
			// hasSelection = true;
			// }
			cursor.pos++;
			setTextAtCursorVisible(true);
		    }
		    if (keycode == Keys.HOME) {
			// if (!hasSelection)
			// {
			// selectionStart = cursor;
			// hasSelection = true;
			// }
			cursor.pos = 0;
			// überprüfen, ob der Cursor sichtbar ist
			setTextAtCursorVisible(true);
		    }
		    if (keycode == Keys.END) {
			// if (!hasSelection)
			// {
			// selectionStart = cursor;
			// hasSelection = true;
			// }
			cursor.pos = text.length();
			// überprüfen, ob der Cursor sichtbar ist
			setTextAtCursorVisible(true);
		    }

		    /*
		     * cursor.pos = Math.max(0, cursor.pos); cursor.pos = Math.min(text.length(), cursor.pos); // überprüfen, ob der Cursor
		     * sichtbar ist checkCursorVisible(true);
		     */
		} else {
		    // cursor movement or other keys (kill selection)
		    if (keycode == Keys.LEFT) {
			cursorLeftRight(-1);
			clearSelection();
		    } else if (keycode == Keys.RIGHT) {
			cursorLeftRight(1);
			clearSelection();
		    } else if (keycode == Keys.HOME) {
			cursorHomeEnd(-1);
			clearSelection();
		    } else if (keycode == Keys.END) {
			cursorHomeEnd(1);
			clearSelection();
		    } else if (keycode == Keys.UP) {
			cursorUpDown(-1);
			clearSelection();
		    } else if (keycode == Keys.DOWN) {
			cursorUpDown(1);
			clearSelection();
		    } else {
			return false;
		    }
		}
		GL.that.renderOnce();

		return true;
	    }
	} finally {
	    displayTextLock.unlock();
	}
	return false;
    }

    // bewegt den Cursor an den Anfang / Ende der aktuellen Zeile
    private void cursorHomeEnd(int i) {
	Line line = getCursorsLine();
	if (line == null)
	    return;
	if (i < 0) {
	    cursor.pos = 0;
	} else {
	    cursor.pos = line.displayText.length();
	}
	setTextAtCursorVisible(true);
    }

    // bewegt den Cursor nach links - rechts
    public void cursorLeftRight(int i) {
	Line line = getCursorsLine();
	if (line == null)
	    return;
	int newCursor = cursor.pos + i;
	if (newCursor > line.displayText.length()) {
	    if (cursorUpDown(1))
		cursor.pos = 0;
	} else if (newCursor < 0) {
	    if (cursorUpDown(-1)) {
		Line newLine = getCursorsLine();
		cursor.pos = newLine.displayText.length();
	    }
	} else {
	    cursor.pos = newCursor;
	}
	// überprüfen, ob der Cursor sichtbar ist
	// checkCursorVisible(true);
    }

    // fügt eine neue Zeile an der Cursor Position ein
    private void insertNewLine() {
	Line line = getCursorsLine();
	if (line == null)
	    return;
	// aktuellen String bei Cursor-Position trennen
	String s1 = line.displayText.toString().substring(0, cursor.pos);
	String s2 = line.displayText.toString().substring(cursor.pos, line.displayText.length());
	line.displayText = s1;
	Line newLine = new Line(s2, style.font);
	lines.add(cursor.line + 1, newLine);
	computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
	computeGlyphAdvancesAndPositions(newLine.displayText, newLine.glyphAdvances, newLine.glyphPositions);
	setCursorLine(cursor.line + 1, true);
	cursor.pos = 0;

	int lineCount = lines.size();

	sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);

    }

    // bewegt den Cursor nach oben / unten. X-Position des Cursors soll möglichst gleich bleiben
    private boolean cursorUpDown(int i) {
	int newCursorLine = cursor.line + i;
	if (newCursorLine < 0)
	    return false;
	if (newCursorLine >= lines.size())
	    return false;
	Line oldLine = lines.get(cursor.line);
	// X-Koordinate von alter Cursor Position bestimmen
	float x = oldLine.glyphPositions.items[cursor.pos];
	// Cursor in neue Zeile plazieren
	setCursorLine(newCursorLine, true);
	// Cursor möglichst an gleiche x-Position plazieren
	setCursorXPos(x);
	return true;
    }

    /**
     * liefert das DisplayText-Object der aktuellen Zeile
     * @return
     */
    private Line getCursorsLine() {
	if (cursor.line < 0)
	    return null;
	if (cursor.line >= lines.size())
	    return null;
	return lines.get(cursor.line);
    }

    /**
     * liefert das DisplayText-Object der angegebenen Zeile
     * @param nth
     * @return
     */
    private Line getNthLine(int nth) {
	if (nth < 0)
	    return null;
	if (nth >= lines.size())
	    return null;
	return lines.get(nth);
    }

    /**
     * Zeile des Cursors ändern. Sicherstellen, dass der Cursor sichtbar ist
     * @param newCursorLine
     * @param hideSelectionMarker
     */
    private void setCursorLine(int newCursorLine, boolean hideSelectionMarker) {
	cursor.line = newCursorLine;
	setTextAtCursorVisible(hideSelectionMarker);
    }

    /**
     * topLine and leftPos are set for cursor to be in visible part
     * SelectionMarker will be hidden, if desired 
     * @param hideSelectionMarker
     */
    private void setTextAtCursorVisible(boolean hideSelectionMarker) {
	if (GL.that.hasFocus(this)) {
	    try {
		if (hideSelectionMarker)
		    hideSelectionMarker();
		// Cursorpos prüfen, ob ausserhalb sichtbaren Bereich (Oben-Unten)
		if (cursor.line - topLine >= maxLineCount) {
		    topLine = cursor.line - maxLineCount + 1;
		}
		if (cursor.line < topLine) {
		    topLine = cursor.line;
		}
		// links-Rechts
		// Cursor Pos in Pixeln vom Textanfang an
		Line line = getCursorsLine();
		if (line != null) {
		    float xCursor = 0;
		    if (cursor.pos < line.glyphPositions.size) {
			xCursor = line.glyphPositions.get(cursor.pos);
		    } else {
			xCursor = line.glyphPositions.get(line.glyphPositions.size - 1);
		    }
		    // Prüfen, ob der Cursor links außen ist
		    if (xCursor < 0) {
			leftPos = xCursor;
		    } else {
			// Prüfen, ob der Cursr rechts außen ist
			if ((xCursor > maxTextWidth) && (maxTextWidth > 0)) {
			    leftPos = xCursor - maxTextWidth;
			}
		    }
		}
	    } catch (Exception e) {
		log.error(this.name + " setTextAtCursorVisible", e);
		if (cursor.pos == -1)
		    setCursorPosition(0);
	    }
	}
    }

    // Cursor in aktueller Zeile an die gegebene X-Position (in Pixel) senden
    private void setCursorXPos(float xPos) {
	Line line = getCursorsLine();
	if (line == null)
	    return;
	if (xPos <= 0) {
	    cursor.pos = 0;
	    setTextAtCursorVisible(true);
	    return;
	}
	for (int i = 0; i < line.glyphPositions.size; i++) {
	    float pos = line.glyphPositions.items[i];
	    if (pos > xPos) {
		cursor.pos = Math.max(0, i - 1);
		setTextAtCursorVisible(true);
		return;
	    }
	}
	// kein Passendes Zeichen gefunden an gegebener Position -> Cursor ans Ende setzen
	cursor.pos = Math.max(0, line.glyphPositions.size - 1);
	setTextAtCursorVisible(true);
    }

    /**
     * Copies the contents of this TextField to the {@link Clipboard} implementation set on this TextField.
     */
    @Override
    public String copyToClipboard() {
	if (clipboard == null)
	    return null;
	if (selection != null) {
	    String content = "";
	    content = this.getSelectedText();
	    clipboard.setContents(content);
	    return ""; // only copy Msg
	}
	return null;
    }

    @Override
    public String cutToClipboard() {
	String ret = copyToClipboard();
	delete();
	return ret;
    }

    /**
     * Pastes the content of the {@link Clipboard} implementation set on this Textfield to this TextField.
     */
    @Override
    public String pasteFromClipboard() {
	if (clipboard == null)
	    return null;
	if (selection != null) {
	    // zuerst evtl. markierten Text löschen
	    delete();
	}

	String[] contents = clipboard.getContents().split("\n");

	if ((contents != null) && (contents.length > 0)) {
	    boolean firstLine = true;
	    // nach Zeilenvorschüben trennen
	    for (String content : contents) {
		StringBuilder builder = new StringBuilder();
		content = content.replace("\b", "");
		content = content.replace("\u2010", "-");
		content = content.replace("\u2011", "-");
		content = content.replace("\u2012", "-");
		content = content.replace("\u2013", "-");
		content = content.replace("\u2014", "-");
		content = content.replace("\u2015", "-");
		content = content.replace("\u23AF", "-");
		content = content.replace("\u23E4", "-");
		content = content.replace("\u2E3A", "-");
		content = content.replace("\u2E3B", "-");
		content = content.replace("\u00B7", "*");
		content = content.replace("\u2715", "*");
		content = content.replace("\u2716", "*");
		content = content.replace("\u22C5", "*");
		content = content.replace("\u2219", "*");
		content = content.replace("\u2217", "*");
		content = content.replace("\u2062", "*");
		content = content.replace("\u2010", "+");
		content = content.replace("\u02D6", "+");
		content = content.replace("\u2064", "+");
		content = content.replace("\u2795", "+");
		content = content.replace("\uFF0B", "+");
		content = content.replace("\u00F7", "/");
		content = content.replace("\u2215", "/");
		content = content.replace("\u2044", "/");
		content = content.replace("\u2236", "/");
		content = content.replace("\u00A0", " ");
		content = content.replace("\u202F", " ");
		content = content.replace("\uFEFF", " ");
		content = content.replace("\u2007", " ");
		content = content.replace("\u180E", " ");
		if (!firstLine) {
		    // bei jeder weiteren Zeile vor dem Einfügen einen Zeilenvorschub machen
		    insertNewLine();
		}
		firstLine = false;
		// Zeile für Zeile
		for (int i = 0; i < content.length(); i++) {
		    char c = content.charAt(i);

		    if (style.font.getData().hasGlyph(c)) {
			builder.append(c);
			keyTyped(c);
		    }
		}
		setTextAtCursorVisible(true);
	    }
	    return ""; // only paste Msg
	}
	return null;
    }

    private void delete() {
	if (selection == null)
	    return;
	// markierte Zeichen löschen
	String zeilenText = "";
	// Zeilenanfang bis Markierung Start
	Line line = getNthLine(selection.cursorStart.line);
	if (line == null)
	    return;
	if (selection.cursorStart.pos > 0) {
	    zeilenText = line.displayText.substring(0, selection.cursorStart.pos);
	}

	// Markierung Ende bis Zeilenende
	Line lineEnd = getNthLine(selection.cursorEnd.line);
	if (lineEnd == null)
	    return;
	if (selection.cursorEnd.pos < lineEnd.displayText.length()) {
	    zeilenText = zeilenText + lineEnd.displayText.substring(selection.cursorEnd.pos, lineEnd.displayText.length());
	}
	line.displayText = zeilenText;
	updateDisplayText(line, false);
	try {
	    for (int i = selection.cursorEnd.line; i > selection.cursorStart.line; i--) {
		lines.remove(i);
	    }
	} catch (Exception e) {
	    selection = null;
	    return;
	}

	cursor.pos = selection.cursorStart.pos;
	cursor.line = selection.cursorStart.line;
	setTextAtCursorVisible(true);
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

    @Override
    public boolean keyTyped(char character) {
	return keyTyped(character, false);
    }

    public boolean keyTyped(char character, boolean ignoreFocus) {

	if (disabled || !isEditable)
	    return false;

	final BitmapFont font = style.font;
	Line line = getCursorsLine();
	if (line == null || disabled)
	    return false;

	if (GL.that.hasFocus(this) || ignoreFocus) {
	    if (character == BACKSPACE) {
		if (selection != null) {
		    delete();
		    sendKeyTyped(character);
		    return true;
		} else {
		    try {
			if (cursor.pos > 0) {
			    line.displayText = line.displayText.substring(0, cursor.pos - 1) + line.displayText.substring(cursor.pos, line.displayText.length());
			    updateDisplayText(line, true);
			    cursor.pos--;
			    setTextAtCursorVisible(true);
			    GL.that.renderOnce();
			    sendKeyTyped(character);
			    return true;
			} else {
			    if (cursor.line > 0) {
				setCursorLine(cursor.line - 1, true);
				Line line2 = getCursorsLine();
				cursor.pos = line2.displayText.length();
				setTextAtCursorVisible(true);
				line2.displayText = line2.displayText + line.displayText;
				lines.remove(cursor.line + 1);
				updateDisplayText(line2, true);

				int lineCount = lines.size();
				sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);
			    }
			    GL.that.renderOnce();
			    sendKeyTyped(character);
			    return true;
			}
		    } catch (Exception e) {
			return true;
		    }
		}
	    }
	    if (character == DELETE) {
		if (selection != null) {
		    delete();
		    return true;
		} else {
		    if (cursor.pos < line.displayText.length()) {
			line.displayText = line.displayText.substring(0, cursor.pos) + line.displayText.substring(cursor.pos + 1, line.displayText.length());
			updateDisplayText(line, true);
			GL.that.renderOnce();
			sendKeyTyped(character);
			return true;
		    } else {
			if (cursor.line + 1 < lines.size()) {
			    cursor.line++;
			    Line line2 = getCursorsLine();
			    cursor.line--;
			    lines.remove(line2);
			    line.displayText += line2.displayText;
			    updateDisplayText(line, true);
			    GL.that.renderOnce();
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

	    if (character == ENTER_DESKTOP || character == ENTER_ANDROID) {
		if (isMultiLine()) {
		    if (selection != null)
			delete();
		    insertNewLine();
		    clearSelection();
		    sendKeyTyped(character);
		    return true;
		}
	    }
	    if (character != ENTER_DESKTOP && character != ENTER_ANDROID) {
		if (filter != null && !filter.acceptChar(this, character))
		    return true;
	    }

	    if (font.getData().getGlyph(character) != null) {
		if (selection != null)
		    delete();
		{
		    try {
			line.displayText = line.displayText.substring(0, cursor.pos) + character + line.displayText.substring(cursor.pos, line.displayText.length());
			updateDisplayText(line, true);
			cursor.pos++;
			setTextAtCursorVisible(true);
		    } catch (Exception e) {
			return false;
		    }
		}

		GL.that.renderOnce();
	    }
	    sendKeyTyped(character);
	    if (passwordMode)
		updateDisplayTextList();
	    return true;
	} else
	    return false;
    }

    private boolean isMultiLine() {
	if ((mWrapType == WrapType.MULTILINE) || (mWrapType == WrapType.WRAPPED))
	    return true;
	else
	    return false;
    }

    private boolean isWraped() {
	return mWrapType == WrapType.WRAPPED;
    }

    /**
     * @param filter
     *            May be null.
     */
    public void setTextFieldFilter(TextFieldFilter filter) {
	this.filter = filter;
    }

    /** @return May be null. */
    public String getMessageText() {
	return messageText;
    }

    /**
     * Sets the text that will be drawn in the text field if no text has been entered.
     * 
     * @parma messageText May be null.
     */
    public void setMessageText(String messageText) {
	this.messageText = messageText;
    }

    public void setText(String inputText) {
	if (inputText == null)
	    throw new IllegalArgumentException("text cannot be null.");

	BitmapFont font = style.font;

	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < inputText.length(); i++) {
	    char c = inputText.charAt(i);
	    if (font.getData().hasGlyph(c) || (c == '\n'))
		buffer.append(c);
	}

	// replace lineBreaks
	String bText = buffer.toString().replace("\r\n", "\r");

	lines.clear();
	Line newLine = new Line("", true, style.font);
	lines.add(newLine);

	cursor.pos = 0;
	cursor.line = 0;

	this.text = "";

	for (int i = 0; i < bText.length(); i++) {
	    char c = bText.charAt(i);
	    keyTyped(c, true);
	}

	setCursorPosition(inputText.length());

    }

    /** @return Never null, might be an empty string. */
    public String getText() {

	StringBuilder sb = new StringBuilder();

	lines.size();
	int index = 0;
	for (Line line : lines) {
	    if ((index > 0) && (!line.autoWrap)) {
		sb.append(Global.br);
	    }
	    sb.append(line.displayText);
	    index++;
	}

	String ret = sb.toString();

	if (this.mWrapType == WrapType.SINGLELINE) {
	    ret = ret.replace("\n", "");
	    ret = ret.replace("\r", "");
	}

	return ret;
    }

    public String getSelectedText() {
	if (selection == null)
	    return "";

	StringBuilder sb = new StringBuilder();

	// Alle Zeilen durchgehen, in denen selectierter Text liegt
	for (int n = selection.cursorStart.line; n <= selection.cursorEnd.line; n++) {
	    Line line = getNthLine(n);

	    int startPos = 0;
	    int endPos = line.displayText.length();

	    if (n == selection.cursorStart.line) {
		startPos = selection.cursorStart.pos;
	    }
	    if (n == selection.cursorEnd.line) {
		endPos = selection.cursorEnd.pos;
	    }

	    sb.append(line.displayText.substring(startPos, endPos));
	    if (n < selection.cursorEnd.line) {
		sb.append(Global.br);
	    }

	}
	String ret = sb.toString();

	if (this.mWrapType == WrapType.SINGLELINE) {
	    ret = ret.replace("\n", "");
	    ret = ret.replace("\r", "");
	}

	return ret;
    }

    /** Sets the selected text. */
    public void setSelection(int selectionStart, int selectionEnd) {

	String aktText = getText();

	if (selectionStart < 0)
	    throw new IllegalArgumentException("selectionStart must be >= 0");
	if (selectionEnd < 0)
	    throw new IllegalArgumentException("selectionEnd must be >= 0");
	selectionStart = Math.min(aktText.length(), selectionStart);
	selectionEnd = Math.min(aktText.length(), selectionEnd);
	if (selectionEnd == selectionStart) {
	    clearSelection();
	    return;
	}
	if (selectionEnd < selectionStart) {
	    int temp = selectionEnd;
	    selectionEnd = selectionStart;
	    selectionStart = temp;
	}

	Cursor cursorStart = new Cursor(0, 0);
	cursorStart.pos = selectionStart;
	Cursor cursorEnd = new Cursor(0, 0);
	cursorEnd.pos = selectionEnd;
	selection = new Selection(cursorStart, cursorEnd);
	// showSelectionMarker(SelectionMarker.Type.Left, selection.cursorStart);
	// showSelectionMarker(SelectionMarker.Type.Right, selection.cursorEnd);

    }

    public void clearSelection() {
	selection = null;
	// hasSelection = false;
    }

    /** Sets the cursor position and clears any selection. */
    public void setCursorPosition(int cursorPosition) {
	if (cursorPosition < 0)
	    throw new IllegalArgumentException("cursorPosition must be >= 0");

	clearSelection();

	for (int n = 0; n < lines.size(); n++) {
	    Line line = getNthLine(n);
	    if (cursorPosition < line.displayText.length()) {
		cursor.line = n;
		cursor.pos = cursorPosition;
		break;
	    }
	    cursorPosition = cursorPosition - line.displayText.length() + 1;
	}

	setTextAtCursorVisible(true);
    }

    public int getCursorPosition() {
	return cursor.pos;
    }

    /** Default is an instance of {@link DefaultOnscreenKeyboard}. */
    @Override
    public OnscreenKeyboard getOnscreenKeyboard() {
	return keyboard;
    }

    @Override
    public void setOnscreenKeyboard(OnscreenKeyboard keyboard) {
	this.keyboard = keyboard;
    }

    @Override
    public boolean keyUp(int KeyCode) {
	return true;
    }

    private class Line {
	private String displayText;
	private final GlyphLayout textBounds = new GlyphLayout();
	private final FloatArray glyphAdvances = new FloatArray();
	private final FloatArray glyphPositions = new FloatArray();
	private boolean autoWrap;

	public Line(String displayText, BitmapFont font) {
	    this.displayText = displayText;

	    calcTextBounds(font);
	}

	public Line(String displayText, boolean autoWrap, BitmapFont font) {
	    this.displayText = displayText;
	    this.autoWrap = autoWrap;

	    calcTextBounds(font);
	}

	private void calcTextBounds(BitmapFont font) {
	    textBounds.setText(font, displayText);
	    computeGlyphAdvancesAndPositions(displayText, glyphAdvances, glyphPositions);
	}

	public GlyphLayout getTextBounds() {
	    return textBounds;
	}

	public float getWidth() {
	    return glyphPositions.get(glyphPositions.size - 1) + glyphAdvances.get(glyphAdvances.size - 1);
	}

	@Override
	public String toString() {
	    return this.displayText;
	}
    }

    public float getMeasuredHeight() {

	float h = 0;

	for (Line line : lines) {
	    h = h + line.getTextBounds().height;
	}

	return h;
    }

    private class Cursor {
	public int pos;
	public int line;

	/**
	 * 
	 * @param pos
	 * @param line
	 */
	public Cursor(int pos, int line) {
	    this.pos = pos;
	    this.line = line;
	}
    }

    private class Selection {
	public Cursor cursorStart;
	public Cursor cursorEnd;

	public Selection(Cursor cursorStart, Cursor cursorEnd) {
	    this.cursorStart = cursorStart;
	    this.cursorEnd = cursorEnd;
	}
    }

    public BitmapFont getFont() {
	return style.font;
    }

    public void setFocus() {
	setFocus(true);
    }

    public void setFocus(boolean value) {
	hasFocus = value;
	if (value == true) {
	    GL.that.setKeyboardFocus(this);
	} else {
	    if (GL.that.getKeyboardFocus() == this)
		GL.that.setKeyboardFocus(null);
	}
    }

    public void resetFocus() {
	hasFocus = false;
	GL.that.setKeyboardFocus(null);

    }

    public void setPasswordMode() {
	passwordMode = true;
    }

    public int getSelectionStart() {
	if (selection == null) {
	    int pos = 0;
	    for (int n = 0; n <= cursor.line; n++) {
		Line line = getNthLine(n);
		if (n == cursor.line) {
		    pos += cursor.pos;
		    break;
		}
		pos = pos + line.displayText.length() + 1;
	    }
	    return pos;
	}

	int pos = 0;
	for (int n = 0; n <= selection.cursorStart.line; n++) {
	    Line line = getNthLine(n);
	    if (n == selection.cursorStart.line) {
		pos = pos + selection.cursorStart.pos;
		break;
	    }
	    pos = pos + line.displayText.length() + 1;
	}
	return pos;

    }

    public int getSelectionEnd() {
	if (selection == null) {
	    int pos = 0;
	    for (int n = 0; n <= cursor.line; n++) {
		Line line = getNthLine(n);
		if (n == cursor.line) {
		    pos = pos + cursor.pos;
		    break;
		}
		pos = pos + line.displayText.length() + 1;
	    }
	    return pos;
	}

	int pos = 0;
	for (int n = 0; n <= selection.cursorEnd.line; n++) {
	    Line line = getNthLine(n);
	    if (n == selection.cursorEnd.line) {
		pos += selection.cursorEnd.pos;
		break;
	    }
	    pos = pos + line.displayText.length() + 1;
	}

	return pos;
    }

    public float getTextHeight() {
	return getMeasuredHeight() - bgTopHeight - style.font.getDescent();// displayText.size() * lineHeight;
    }

    public int getTopLineNo() {
	return topLine + 1; // count No from 1
    }

    public void setScrollPos(float value) {
	topLine = (int) (value / this.style.font.getLineHeight());

	if (topLine < 0) {
	    topLine = 0;
	}

	if (lines.size() - topLine < maxLineCount) {
	    topLine = lines.size() - maxLineCount;
	}
    }

    public void showFromLineNo(int lineNo) {
	if (lineNo < 1) {
	    topLine = 0;
	} else {
	    topLine = lineNo - 1;
	}
    }

    public void showLastLines() {
	if (lines.size() < this.maxLineCount) {
	    topLine = 0;
	} else {
	    topLine = lines.size() - this.maxLineCount;
	}
    }

    public interface IListPosChanged {
	public void ListPosChanged();
    }

    private final CB_List<IListPosChanged> EventHandlerList = new CB_List<IListPosChanged>();

    public void addListPosChangedEventHandler(IListPosChanged handler) {
	if (!EventHandlerList.contains(handler))
	    EventHandlerList.add(handler);
    }

    protected void callListPosChangedEvent() {
	for (int i = 0, n = EventHandlerList.size(); i < n; i++) {
	    IListPosChanged handler = EventHandlerList.get(i);
	    if (handler != null)
		handler.ListPosChanged();
	}
    }

    public float getLineHeight() {
	return this.style.font.getLineHeight();
    }

    public void setEditable(boolean value) {
	isEditable = value;
    }

    @Override
    public boolean isEditable() {
	return isEditable;
    }

    //#########################################################################################################

    /** Computes the glyph advances for the given character sequence and stores them in the provided {@link FloatArray}. The float
     * arrays are cleared. An additional element is added at the end.
     * @param glyphAdvances the glyph advances output array.
     * @param glyphPositions the glyph positions output array. */
    public void computeGlyphAdvancesAndPositions(CharSequence str, FloatArray glyphAdvances, FloatArray glyphPositions) {
	glyphAdvances.clear();
	glyphPositions.clear();
	int index = 0;
	int end = str.length();
	float width = 0;
	Glyph lastGlyph = null;
	BitmapFontData data = style.font.getData();
	if (data.scaleX == 1) {
	    for (; index < end; index++) {
		char ch = str.charAt(index);
		Glyph g = data.getGlyph(ch);
		if (g != null) {
		    if (lastGlyph != null)
			width += lastGlyph.getKerning(ch);
		    lastGlyph = g;
		    glyphAdvances.add(g.xadvance);
		    glyphPositions.add(width);
		    width += g.xadvance;
		}
	    }
	    glyphAdvances.add(0);
	    glyphPositions.add(width);
	} else {
	    float scaleX = style.font.getData().scaleX;
	    for (; index < end; index++) {
		char ch = str.charAt(index);
		Glyph g = data.getGlyph(ch);
		if (g != null) {
		    if (lastGlyph != null)
			width += lastGlyph.getKerning(ch) * scaleX;
		    lastGlyph = g;
		    float xadvance = g.xadvance * scaleX;
		    glyphAdvances.add(xadvance);
		    glyphPositions.add(width);
		    width += xadvance;
		}
	    }
	    glyphAdvances.add(0);
	    glyphPositions.add(width);
	}
    }

    @Override
    protected void registerPopUpLongClick() {
	this.setOnLongClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

		//if no selection, select the word under long click
		if (selection == null) {
		    EditTextField.this.doubleClick(x, y, pointer, button);
		}
		showPopUp(x, y);
		return true;
	    }

	});
    }
}
