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
package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.FloatArray;
import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static de.droidcachebox.utils.Config_Core.br;

public class EditTextField extends EditTextFieldBase {
    private static final String log = "EditTextField";
    private final Lock displayTextLock = new ReentrantLock();
    private final CB_List<IListPosChanged> listPosChangedHandlers = new CB_List<>();
    protected TextFieldStyle style;
    protected ArrayList<Line> lines;
    protected Point cursor = new Point(0, 0);
    protected float textHeight;
    private int inputType; // for Android InputType
    private String text;
    private int topLine;
    private int maxLineCount;
    private float leftPos;
    private float textWidth;
    private Selection selectedArea = null;
    private float bgTopHeight = 0;
    private float bgBottomHeight = 0;
    private float bgLeftWidth = 0;
    private boolean selectionIsActive = false;
    private WrapType mWrapType = WrapType.SINGLELINE;
    private Point touchDownPos = null;
    private float topLineAtTouchDown = 0;
    private float leftPosAtTouchDown = 0;

    public EditTextField(CB_View_Base parent, String name) {
        super(UiSizes.getInstance().getButtonRectF(), parent, name);
        initEditTextField();
    }

    public EditTextField(CB_RectF rec, CB_View_Base parent, String name) {
        super(rec, parent, name);
        initEditTextField();
    }

    public EditTextField(CB_RectF rec, CB_View_Base parent, String Name, WrapType WrapType) {
        super(rec, parent, Name);
        if (WrapType != null)
            this.mWrapType = WrapType;
        initEditTextField();
    }

    private void initEditTextField() {
        inputType = 0;
        topLine = 0;
        leftPos = 0;
        this.style = getDefaultStyle();
        lines = new ArrayList<>();
        setText(""); // does all calculations
        setClickable(true);
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int type) {
        inputType = type;
    }

    @Override
    public void onResized(CB_RectF rec) {
        try {
            boolean focused = GL.that.hasFocus(this);
            setTextAtCursorVisible(!focused);
            if (focused) {
                if (selectedArea != null) {
                    showSelectionMarker(SelectionMarker.Type.Left, selectedArea.cursorStart);
                    showSelectionMarker(SelectionMarker.Type.Right, selectedArea.cursorEnd);
                } else {
                    showSelectionMarker(SelectionMarker.Type.Center, cursor);
                    hidePopUp();
                }
            }
            thisInvalidate = true;
        } catch (Exception ex) {
            Log.err(log, "onResized", ex);
        }
    }

    public EditTextField setWrapType(WrapType WrapType) {
        if (WrapType != null) {
            if (WrapType != mWrapType) {
                mWrapType = WrapType;
            }
        }
        return this;
    }

    public TextFieldStyle getStyle() {
        return style;
    }

    public void setStyle(TextFieldStyle style) {
        if (style == null)
            throw new IllegalArgumentException("style cannot be null.");
        this.style = style;
    }

    public void setBackground(Drawable background, Drawable backgroundFocused) {
        style.setBackground(background, backgroundFocused);
    }

    private void calculateSizeDependencies(boolean focused) {
        bgLeftWidth = style.getLeftWidth(focused);
        float bgRightWidth = style.getRightWidth(focused);
        if (mWrapType == WrapType.SINGLELINE) {
            bgTopHeight = bgBottomHeight = Math.max(0, (getHeight() - style.font.getLineHeight()) / 2);
        } else {
            bgTopHeight = style.getTopHeight(focused);
            bgBottomHeight = style.getBottomHeight(focused);
        }
        textWidth = getWidth() - bgLeftWidth - bgRightWidth;
        textHeight = getHeight() - bgTopHeight - bgBottomHeight;
        maxLineCount = (int) (textHeight / style.font.getLineHeight()); // !angebrochene Zeile nicht mitzählen
    }

    public float getTextHeight() {
        // unfocused
        return style.font.getLineHeight() * lines.size() + style.getBottomHeight(false) + style.getTopHeight(false);
    }

    @Override
    protected void render(Batch batch) {
        if (this.isDisposed())
            return;

        displayTextLock.lock();
        try {
            boolean focused = GL.that.hasFocus(this);

            if (style.getBackground(focused) != null) {
                style.getBackground(focused).draw(batch, 0f, 0f, getWidth(), getHeight());
            }
            batch.end();

            batch.begin();

            // Background is drawn, now set scissor to inner rect
            Gdx.gl.glScissor((int) (intersectRec.getX() + bgLeftWidth), (int) (intersectRec.getY() + bgBottomHeight), (int) textWidth + 1, (int) textHeight + 1);

            float textY = (int) getHeight() - bgTopHeight + style.font.getDescent();

            if (selectedArea != null) {
                // Selection zeilenweise durchgehen
                for (int lineNo = selectedArea.cursorStart.y; lineNo <= selectedArea.cursorEnd.y; lineNo++) {
                    Line line = getNthLine(lineNo);
                    if (line == null)
                        continue;
                    int startColumn = 0;
                    if (lineNo == selectedArea.cursorStart.y)
                        startColumn = selectedArea.cursorStart.x;
                    int endColumn = line.displayText.length();
                    if (lineNo == selectedArea.cursorEnd.y)
                        endColumn = selectedArea.cursorEnd.x;
                    float selectionX = line.glyphPositions.get(Math.max(0, startColumn));
                    float selectionY = line.glyphPositions.get(Math.min(endColumn, line.glyphPositions.size - 1));
                    float selectionWidth = selectionY - selectionX;
                    style.selection.draw(batch, selectionX + bgLeftWidth - leftPos, textY + this.style.font.getLineHeight() * (topLine - 1 - lineNo) - style.font.getDescent() / 2, selectionWidth, this.style.font.getLineHeight());
                }
            }

            style.font.setColor(style.fontColor.r, style.fontColor.g, style.fontColor.b, style.fontColor.a);
            textY += this.style.font.getLineHeight() * topLine;
            for (Line line : lines) {
                // try hack : if (textY < this.getHeight() && textY > 0) {                    }
                if (line != null) {
                    if (line.displayText != null) {
                        try {
                            style.font.draw(batch, line.displayText, bgLeftWidth - leftPos, textY);
                        } catch (Exception ex) {
                            Log.err(log, "not displayed: " + line.displayText + " length: " + line.displayText.length(), ex);
                        }
                        textY -= style.font.getLineHeight();
                    }
                } else {
                    Log.err(log, "a line of lines is null");
                }
            }

            if (focused) {
                if (cursorOn && style.cursor != null) {
                    float cursorHeight = style.font.getLineHeight();
                    float cursorWidth = style.cursor.getMinWidth();
                    style.cursor.draw(batch, getCursorX() - cursorWidth, getCursorY() + cursorHeight - style.font.getAscent(), cursorWidth * 2, cursorHeight);
                }
            } else {
                // hack to show text only inside box
                style.cursor.draw(batch, 0, 0, style.cursor.getMinWidth(), 0);
            }

            // input cursor
            if (focused && (selectedArea == null)) {
                if (blinkTimer == null)
                    blinkStart();
            } else {
                if (blinkTimer != null)
                    blinkStop();
            }

        } catch (Exception ex) {
            Log.err(log, "render ", ex);
        } finally {
            displayTextLock.unlock();
        }
    }

    private float getCursorX() {
        return getCursorX(cursor);
    }

    private float getCursorX(Point aCursor) {
        float xpos = 0;
        if (lines.size() > aCursor.y) {
            Line line = getNthLine(aCursor.y);
            if (line != null) {
                // no text
                // after the last character
                if (aCursor.x < line.glyphPositions.size) {
                    // in front of the char
                    xpos = line.glyphPositions.get(aCursor.x);
                } else {
                    xpos = line.glyphPositions.size == 0 ? 0 : line.glyphPositions.get(line.glyphPositions.size - 1);
                }
            }
        }
        return this.bgLeftWidth + xpos - leftPos - 3;// empirische Korrektur
    }

    private float getCursorY() {
        return getCursorY(cursor.y);
    }

    private float getCursorY(int aCursorLine) {
        float textY = (int) getHeight() - bgTopHeight + style.font.getDescent();
        return (int) (textY - this.style.font.getLineHeight() * (aCursorLine - topLine + 1.5));
    }

    private void updateDisplayText(Line line, boolean calcCursor) {
        float maxWidth = getWidth() - 50; // noch falsch!!!!!!!!!!!!!!!!!!!!!
        // wenn dies eine autoWrap Zeile ist muss zuerst die Zeile davor überprüft werden,
        // ob die ersten Zeichen dieser Zeile dahinein kopiert werden können
        if (line.autoWrap) {
            Line previousLine = getNthLine(cursor.y - 1);
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
                    String s1 = line.displayText.substring(0, idWord);
                    String s2 = line.displayText.substring(idWord);
                    previousLine.displayText += s1;
                    computeGlyphAdvancesAndPositions(previousLine.displayText, previousLine.glyphAdvances, previousLine.glyphPositions);
                    line.displayText = s2;
                    if (s2.length() == 0) {
                        // komplette Zeile löschen
                        lines.remove(line);
                        int lineCount = lines.size();
                        sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);
                        // 2014-07-09: cursor.x und idWord = 0 setzen damit später der Cursor auf das Ende der vorherigen Zeile gesetzt wird
                        cursor.x = 0;
                        idWord = 0;
                    } else {
                        computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
                    }
                    if (cursor.x > idWord) {
                        cursor.x -= idWord; // Cursor ist hinter den Zeichen die in die vorherige Zeile verschoben werden -> nach Vorne setzen
                    } else {
                        // cursor ist innerhalb der Zeichen, die in die vorherige Zeile verschoben werden -> Cursor in die vorherige Zeile verschieben
                        cursor.x = previousLine.displayText.length() + 1/* - 1 */;
                        setCursorLine(cursor.y - 1, true);
                        // anschließende Zeile noch mal berechnen.
                        cursor.y++;
                        Line nextLine = getNthLine(cursor.y);
                        if (nextLine != null) {
                            updateDisplayText(nextLine, false);
                        }
                        cursor.y--;
                        return;
                    }
                }
            }
        }
        computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
        float len = line.getWidth();

        // Prüfen, ob Zeile zu lang geworden ist und ob am Ende Zeichen in die nächste Zeile verschoben werden müssen
        if ((len > maxWidth) && mWrapType == WrapType.WRAPPED) {
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
                // Zeile Trennen nach dem letzten "Space" vor dem Zeichen id
                for (int j = id - 1; j >= 0; j--) {
                    if (isaPossibleLineBeak(line.displayText.charAt(j))) {
                        id = j + 1;
                        break;
                    }
                }
                // Zeilenumbruch an Zeichen id
                // aktuellen String bei id trennen
                String s1 = line.displayText.substring(0, id);
                String s2 = line.displayText.substring(id);
                line.displayText = s1;
                computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
                cursor.y++;
                // Text der nächsten Zeile holen und prüfen, ob dies eine durch einen autoWrap eingefügte Zeile ist
                Line nextLine = getNthLine(cursor.y);
                cursor.y--;
                if ((nextLine != null) && nextLine.autoWrap) {
                    // Umzubrechnenden Text am Anfang von nextLine anfügen
                    nextLine.displayText = s2 + nextLine.displayText;
                    computeGlyphAdvancesAndPositions(nextLine.displayText, nextLine.glyphAdvances, nextLine.glyphPositions);
                    cursor.y++;
                    updateDisplayText(nextLine, false);
                    cursor.y--;
                } else {
                    // neue Zeile erstellen
                    Line newLine = new Line(s2, true, style.font);
                    lines.add(cursor.y + 1, newLine);
                    computeGlyphAdvancesAndPositions(newLine.displayText, newLine.glyphAdvances, newLine.glyphPositions);
                }
                if (calcCursor && (cursor.x >= id)) {
                    // Cursor auch in die nächste Zeile verschieben, an die Stelle im Wort an der der Cursor vorher auch war
                    cursor.x = cursor.x - id;
                    setCursorLine(cursor.y + 1, true);
                }
            }
        }
        // Prüfen, ob am Ende der Zeile wieder Platz für Zeichen / Wörter der vorgänger-Zeile ist
        float rest = maxWidth - line.getWidth(); // Restlicher Platz
        // Wenn anschließend eine Wrapped-Zeile kommt dann erstes Word dessen suchen und Prüfen, ob dies hier eingefügt werden kann
        Line nextLine = getNthLine(cursor.y + 1);
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
                String s1 = nextLine.displayText.substring(0, idWord);
                String s2 = nextLine.displayText.substring(idWord);
                line.displayText += s1;
                computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
                updateDisplayText(line, false);
                nextLine.displayText = s2;
                if (s2.length() == 0) {
                    // komplette Zeile löschen
                    lines.remove(nextLine);
                } else {
                    cursor.y++;
                    computeGlyphAdvancesAndPositions(nextLine.displayText, nextLine.glyphAdvances, nextLine.glyphPositions);
                    updateDisplayText(nextLine, false);
                    cursor.y--;
                }
            }
        }
        //else {
        // anschließend kommt keine Zeile, die automatisch umgebrochen wurde -> nichts machen
        //}
    }

    private boolean isaPossibleLineBeak(char c) {
        return c == ' ' || c == '(' || c == ')' || c == '[' || c == ']' | c == '{' || c == '}' || c == '.' || c == ',' || c == ';' || c == ':';
    }

    @Override
    public boolean onTouchDown(int X, int Y, int pointer, int button) {
        touchDownPos = new Point(X, Y);
        // topLine merken, zu dem Zeitpunkt als die Maus gedrückt wurde
        topLineAtTouchDown = topLine;
        // leftPos merken, zu dem Zeitpunkt als die Maus gedrückt wurde
        leftPosAtTouchDown = leftPos;
        return true;
    }

    @Override
    public boolean onTouchDragged(int dx, int dy, int pointer, boolean KineticPan) {
        boolean bearbeitet = false;
        try {
            if (touchDownPos != null) {
                float oldTopLine = topLine;
                float oldLeftPos = leftPos;
                if (isMultiLine()) {
                    // Scrollen Oben - Unten
                    if (lines.size() < maxLineCount) {
                        topLine = 0;
                    } else {
                        topLine = (int) (topLineAtTouchDown + (dy - touchDownPos.y) / this.style.font.getLineHeight());
                        if (topLine < 0) {
                            topLine = 0;
                        }
                        if (lines.size() - topLine < maxLineCount) {
                            topLine = lines.size() - maxLineCount;
                        }
                    }
                    bearbeitet = true;
                }

                // Scrollen Links - Rechts
                float maxWidth = maxLineWidth();
                if (maxWidth < textWidth) {
                    // Text hat auf einmal Platz -> auf Ursprung hin scrollen
                    leftPos = 0;
                } else {
                    // Text hat nicht auf einmal Platz -> Scrollen möglich
                    leftPos = leftPosAtTouchDown + (touchDownPos.x - dx);
                    if (leftPos < 0) {
                        leftPos = 0;
                    }
                    if (leftPos > maxWidth - textWidth) {
                        leftPos = maxWidth - textWidth;
                    }
                }

                moveSelectionMarkers((oldLeftPos - leftPos), (topLine - oldTopLine) * this.style.font.getLineHeight());
                callListPosChangedEvent();
            }
            GL.that.renderOnce();
        } catch (Exception ex) {
            Log.err(log, "onTouchDragged", ex);
        }

        // Scrollen nach oben / unten soll möglich sein trotzdem dass hier evtl. schon links / rechts gescrollt wird ????
        return bearbeitet;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        touchDownPos = null;
        return false;
    }

    private float maxLineWidth() {
        float result = 0;
        for (Line line : lines) {
            float w = line.getWidth();
            if (w > result)
                result = w;
        }
        return result;
    }

    private Point getClickedCursor(int X, int Y) {
        // Zeile bestimmen, in die geklickt wurde
        int clickedLine = topLine + (int) ((this.getHeight() - bgTopHeight - Y) / this.style.font.getLineHeight());
        if (clickedLine < 0)
            clickedLine = 0;
        if (clickedLine >= lines.size())
            return new Point(0, lines.size() - 1);
        // Spalte bestimmen, in die geklickt wurde.
        Line line = getNthLine(clickedLine);
        if (line != null) {
            float lx = X - style.getLeftWidth(true) + leftPos; // isClicked=isFocused
            for (int i = 0; i < line.glyphPositions.size; i++) {
                if (line.glyphPositions.items[i] > lx) {
                    return new Point(Math.max(0, i - 1), clickedLine);
                }
            }
            return new Point(Math.max(0, line.glyphPositions.size - 1), clickedLine);
        }
        return new Point(0, 0);
    }

    @Override
    public boolean click(int X, int Y, int pointer, int button) {
        if (pointer != 0)
            return false;
        Point newCursor = getClickedCursor(X, Y);
        cursor.x = newCursor.x;
        setCursorLine(newCursor.y, true);

        GL.that.setFocusedEditTextField(this);

        setTextAtCursorVisible(false);

        clearSelection();
        cursorOn = false; // startvalue for blinking (off)
        showSelectionMarker(SelectionMarker.Type.Center, cursor);
        hidePopUp();
        GL.that.renderOnce();

        return true;
    }

    @Override
    public boolean doubleClick(int x, int y, int pointer, int button) {
        // Doppelklick markiert Wort unter dem Cursor und setzt 2 Marker
        if (pointer != 0)
            return false;
        if (selectionIsActive) {
            // ein Wort ist schon markiert, dann markiere ganze Zeile
            selectionIsActive = false;
            hideSelectionMarker();
            int clickedLine = topLine + (int) ((this.getHeight() - bgTopHeight - y) / (this.getStyle().font.getLineHeight()));
            if (clickedLine < 0)
                clickedLine = 0;
            if (clickedLine >= lines.size())
                clickedLine = lines.size() - 1;
            Line nthLine = getNthLine(clickedLine);
            if (nthLine != null) {
                int endOfClickedLine = nthLine.displayText.length();
                selectedArea = new Selection(new Point(0, clickedLine), new Point(endOfClickedLine, clickedLine));
            } else return false;
        } else {
            selectionIsActive = true;
            Point newCursor = getClickedCursor(x, y);

            Line line = getNthLine(newCursor.y);
            if (line == null)
                return false;

            if (newCursor.x >= line.displayText.length())
                newCursor.x = line.displayText.length() - 1;
            if (newCursor.x < 0)
                newCursor.x = 0;

            Point cursorStart = null;
            Point cursorEnd = null;
            // Wortanfang und Wortende suchen
            if (line.displayText.length() > 0) {
                for (int i = newCursor.x; i >= 0; i--) {
                    if (line.displayText.charAt(i) == ' ') {
                        cursorStart = new Point(i + 1, newCursor.y);
                        break;
                    }
                }
            }
            if (cursorStart == null)
                cursorStart = new Point(0, newCursor.y);
            if (line.displayText.length() > 0) {
                for (int i = newCursor.x; i < line.displayText.length(); i++) {
                    if (line.displayText.charAt(i) == ' ') {
                        cursorEnd = new Point(i, newCursor.y);
                        break;
                    }
                }
            }
            if (cursorEnd == null)
                cursorEnd = new Point(line.displayText.length(), newCursor.y);
            hideSelectionMarker();
            selectedArea = new Selection(cursorStart, cursorEnd);
        }
        showSelectionMarker(SelectionMarker.Type.Left, selectedArea.cursorStart);
        showSelectionMarker(SelectionMarker.Type.Right, selectedArea.cursorEnd);
        if (isEditable)
            showCopyPastePopUp(x, y);
        else {
            showCopyPopUp(x, y);
        }
        return true;
    }

    private void showSelectionMarker(final SelectionMarker.Type type, final Point tmpCursor) {
        GL.that.showMarker(type);
        Timer v = new Timer();
        TimerTask ta;
        ta = new TimerTask() {
            @Override
            public void run() {
                switch (type) {
                    case Center:
                        GL.that.selectionMarkerCenterMoveTo(getCursorX(tmpCursor) + style.cursor.getMinWidth() / 2, getCursorY(tmpCursor.y));
                        break;
                    case Left:
                        GL.that.selectionMarkerLeftMoveTo(getCursorX(tmpCursor) + style.cursor.getMinWidth() / 2, getCursorY(tmpCursor.y));
                        break;
                    case Right:
                        GL.that.selectionMarkerRightMoveTo(getCursorX(tmpCursor) + style.cursor.getMinWidth() / 2, getCursorY(tmpCursor.y));
                        break;
                }
            }
        };
        try {
            v.schedule(ta, 700);
        } catch (Exception ignored) {
        }
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

    private void hideSelectionMarker() {
        selectedArea = null;
        GL.that.hideMarker();
    }

    Point aSelectionMarkerIsDragged(int dx, int dy, SelectionMarker.Type selectionMarkerType) {
        Point useCursor = cursor;
        switch (selectionMarkerType) {
            case Center:
                useCursor = cursor;
                break;
            case Left:
                if (selectedArea != null) {
                    useCursor = selectedArea.cursorStart;
                }
                break;
            case Right:
                if (selectedArea != null) {
                    useCursor = selectedArea.cursorEnd;
                }
                break;
        }

        int clickedLine = useCursor.y - (int) (dy / this.style.font.getLineHeight());
        if (clickedLine == useCursor.y && dx == 0)
            return null;
        if (clickedLine < 0) {
            clickedLine = 0;
        }
        if (clickedLine >= lines.size()) {
            clickedLine = lines.size() - 1;
        }

        int lx = 0;
        Line line = getNthLine(clickedLine);
        if (line != null) {
            if (useCursor.y == clickedLine) {
                lx = (int) line.glyphPositions.items[useCursor.x] + dx;
            } else {
                Line nthLine = getNthLine(useCursor.y);
                if (nthLine != null) {
                    lx = (int) nthLine.glyphPositions.items[useCursor.x] + dx;
                }
            }

            // in front of the char
            for (int i = 0; i < line.glyphPositions.size; i++) {
                float pos = line.glyphPositions.items[i];
                if (pos > lx) {
                    int clickedColumn = Math.max(0, i - 1);
                    Point result = new Point((int) (getCursorX(new Point(clickedColumn, clickedLine)) + style.cursor.getMinWidth() / 2), (int) (getCursorY(clickedLine)));
                    useCursor.x = clickedColumn;
                    useCursor.y = clickedLine;
                    if (selectionMarkerType == SelectionMarker.Type.Center) {
                        setCursorLine(clickedLine, false);
                    }
                    // Log.info(log, "drag " + dy + "/" + dx + " to char" + clickedColumn + " line " + clickedLine);
                    return result;
                }
            }

            // after the last character
            int clickedColumn = Math.max(0, line.glyphPositions.size - 1);
            Point result = new Point((int) (getCursorX(new Point(clickedColumn, clickedLine)) + style.cursor.getMinWidth() / 2), (int) (getCursorY(clickedLine)));
            useCursor.x = clickedColumn;
            useCursor.y = clickedLine;
            if (selectionMarkerType == SelectionMarker.Type.Center) {
                setCursorLine(clickedLine, false);
            }
            //Log.info(log, "drag " + dy + "/" + dx + " to char" + clickedColumn + " line " + clickedLine);
            return result;

        }

        return new Point(0, 0);

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
                        copyToClipboard();
                } else if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
                    // paste
                    if (keycode == Keys.INSERT)
                        pasteFromClipboard();
                    // cut
                    /*
                    if (keycode == Keys.FORWARD_DEL) {
                        // if (hasSelection)
                        // {
                        // copy();
                        // delete();
                        // }
                    }
                     */
                    // selection
                    if (keycode == Keys.LEFT) {
                        // if (!hasSelection)
                        // {
                        // selectionStart = cursor;
                        // hasSelection = true;
                        // }
                        cursor.x--;
                        setTextAtCursorVisible(true);
                    }
                    if (keycode == Keys.RIGHT) {
                        // if (!hasSelection)
                        // {
                        // selectionStart = cursor;
                        // hasSelection = true;
                        // }
                        cursor.x++;
                        setTextAtCursorVisible(true);
                    }
                    if (keycode == Keys.HOME) {
                        // if (!hasSelection)
                        // {
                        // selectionStart = cursor;
                        // hasSelection = true;
                        // }
                        cursor.x = 0;
                        // überprüfen, ob der Cursor sichtbar ist
                        setTextAtCursorVisible(true);
                    }
                    if (keycode == Keys.END) {
                        // if (!hasSelection)
                        // {
                        // selectionStart = cursor;
                        // hasSelection = true;
                        // }
                        cursor.x = text.length();
                        // überprüfen, ob der Cursor sichtbar ist
                        setTextAtCursorVisible(true);
                    }

                    /*
                     * cursor.x = Math.max(0, cursor.x); cursor.x = Math.min(text.length(), cursor.x); // überprüfen, ob der Cursor
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
                    // überprüfen, ob der Cursor sichtbar ist
                    setTextAtCursorVisible(true);
                }
                GL.that.renderOnce();

                return true;
            }
        } finally {
            displayTextLock.unlock();
        }
        return false;
    }

    @Override
    public boolean keyUp(int KeyCode) {
        return true;
    }

    // bewegt den Cursor an den Anfang / Ende der aktuellen Zeile
    private void cursorHomeEnd(int i) {
        Line line = getNthLine(cursor.y);
        if (line == null)
            return;
        if (i < 0) {
            cursor.x = 0;
        } else {
            cursor.x = line.displayText.length();
        }
        setTextAtCursorVisible(true);
    }

    // bewegt den Cursor nach links - rechts
    void cursorLeftRight(int i) {
        Line line = getNthLine(cursor.y);
        if (line == null)
            return;
        int newPosition = cursor.x + i;
        if (newPosition > line.displayText.length()) {
            if (cursorUpDown(1))
                cursor.x = 0;
        } else if (newPosition < 0) {
            if (cursorUpDown(-1)) {
                Line newLine = getNthLine(cursor.y);
                if (newLine != null) {
                    cursor.x = newLine.displayText.length();
                } else
                    cursor.x = 0;
            }
        } else {
            cursor.x = newPosition;
        }
    }

    // fügt eine neue Zeile an der Cursor Position ein
    private void insertNewLine() {
        Line line = getNthLine(cursor.y);
        if (line == null)
            return;
        // aktuellen String bei Cursor-Position trennen
        String s1 = "";
        String s2 = "";
        if (cursor.x >= 0 && cursor.x <= line.displayText.length()) {
            s1 = line.displayText.substring(0, cursor.x);
            s2 = line.displayText.substring(cursor.x);
        }
        line.displayText = s1;
        Line newLine = new Line(s2, style.font);
        lines.add(cursor.y + 1, newLine);
        computeGlyphAdvancesAndPositions(line.displayText, line.glyphAdvances, line.glyphPositions);
        computeGlyphAdvancesAndPositions(newLine.displayText, newLine.glyphAdvances, newLine.glyphPositions);
        setCursorLine(cursor.y + 1, true);
        cursor.x = 0;

        int lineCount = lines.size();

        sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);
    }

    // bewegt den Cursor nach oben / unten. X-Position des Cursors soll möglichst gleich bleiben
    private boolean cursorUpDown(int i) {
        int newCursorLine = cursor.y + i;
        if (newCursorLine < 0)
            return false;
        if (newCursorLine >= lines.size())
            return false;
        Line oldLine = getNthLine(cursor.y);
        if (oldLine != null) {
            // X-Koordinate von alter Cursor Position bestimmen
            float x = oldLine.glyphPositions.items[cursor.x];
            // Cursor in neue Zeile plazieren
            setCursorLine(newCursorLine, true);
            // Cursor möglichst an gleiche x-Position plazieren
            setCursorXPos(x);
        }
        return true;
    }

    private Line getNthLine(int nth) {
        if (nth < 0)
            return null;
        if (nth >= lines.size())
            return null;
        return lines.get(nth);
    }

    private void setCursorLine(int newCursorLine, boolean hideSelectionMarker) {
        cursor.y = newCursorLine;
        setTextAtCursorVisible(hideSelectionMarker);
    }

    private void setTextAtCursorVisible(boolean hideSelectionMarker) {
        try {
            if (hideSelectionMarker)
                hideSelectionMarker();
            calculateSizeDependencies(GL.that.hasFocus(this));
            // Oben-Unten => topLine anpassen
            if (cursor.y - topLine >= maxLineCount) {
                topLine = cursor.y - maxLineCount + 1;
            }
            if (cursor.y < topLine) {
                topLine = cursor.y;
            }
            // links-rechts => leftPos anpassen
            Line line = getNthLine(cursor.y);
            if (line != null && textWidth > 0) {
                float xCursor;
                if (cursor.x < line.glyphPositions.size) {
                    xCursor = line.glyphPositions.get(cursor.x);
                } else {
                    xCursor = line.glyphPositions.get(line.glyphPositions.size - 1);
                }
                if (xCursor > textWidth) {
                    leftPos = xCursor - textWidth;
                } else {
                    leftPos = 0;
                }
            }
        } catch (Exception ex) {
            Log.err(log, this.name + " setTextAtCursorVisible", ex);
            if (cursor.x == -1)
                setCursorPosition(0);
        }
    }

    // Cursor in aktueller Zeile an die gegebene X-Position (in Pixel) senden
    private void setCursorXPos(float xPos) {
        Line line = getNthLine(cursor.y);
        if (line == null)
            return;
        if (xPos <= 0) {
            cursor.x = 0;
            setTextAtCursorVisible(true);
            return;
        }
        for (int i = 0; i < line.glyphPositions.size; i++) {
            float pos = line.glyphPositions.items[i];
            if (pos > xPos) {
                cursor.x = Math.max(0, i - 1);
                setTextAtCursorVisible(true);
                return;
            }
        }
        // kein Passendes Zeichen gefunden an gegebener Position -> Cursor ans Ende setzen
        cursor.x = Math.max(0, line.glyphPositions.size - 1);
        setTextAtCursorVisible(true);
    }

    @Override
    public String copyToClipboard() {
        if (clipboard == null)
            return null;
        if (selectedArea != null) {
            String content;
            content = this.getSelectedText();
            clipboard.setContents(content);
            return ""; // only copy Msg
        }
        return null;
    }

    @Override
    public String cutToClipboard() {
        String ret = copyToClipboard();
        deleteSelection();
        return ret;
    }

    @Override
    public String pasteFromClipboard() {
        if (clipboard == null)
            return null;
        if (selectedArea != null) {
            // zuerst evtl. markierten Text löschen
            deleteSelection();
        }

        String[] contents = clipboard.getContents().split("\n");

        if (contents.length > 0) {
            boolean firstLine = true;
            // nach Zeilenvorschüben trennen
            for (String content : contents) {
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
                        keyTyped(c, true);
                    }
                }
                setTextAtCursorVisible(true);
            }
            return ""; // only paste Msg
        }
        return null;
    }

    private void deleteSelection() {
        if (selectedArea == null)
            return;
        String zeilenText = "";
        // Zeilenanfang bis Markierung Start
        Line line = getNthLine(selectedArea.cursorStart.y);
        if (line == null)
            return;
        if (selectedArea.cursorStart.x > 0) {
            zeilenText = line.displayText.substring(0, selectedArea.cursorStart.x);
        }
        // Markierung Ende bis Zeilenende
        Line lineEnd = getNthLine(selectedArea.cursorEnd.y);
        if (lineEnd == null)
            return;
        if (selectedArea.cursorEnd.x < lineEnd.displayText.length()) {
            zeilenText = zeilenText + lineEnd.displayText.substring(selectedArea.cursorEnd.x);
        }
        line.displayText = zeilenText;
        updateDisplayText(line, false);

        try {
            if (selectedArea.cursorEnd.y >= selectedArea.cursorStart.y + 1) {
                lines.subList(selectedArea.cursorStart.y + 1, selectedArea.cursorEnd.y + 1).clear();
            }
        } catch (Exception e) {
            selectedArea = null;
            return;
        }

        cursor.x = selectedArea.cursorStart.x;
        cursor.y = selectedArea.cursorStart.y;
        setTextAtCursorVisible(true);
        clearSelection();
    }

    @Override
    public boolean keyTyped(char character) {
        return keyTyped(character, false);
    }

    public boolean keyTyped(char character, boolean ignoreFocus) {

        if (!isEditable)
            return false;

        final BitmapFont font = style.font;
        Line line = getNthLine(cursor.y);
        if (line == null)
            return false;

        if (GL.that.hasFocus(this) || ignoreFocus) {
            if (character == BACKSPACE) {
                if (selectedArea != null) {
                    deleteSelection();
                    sendKeyTyped(character);
                    return true;
                } else {
                    try {
                        if (cursor.x > 0) {
                            line.displayText = line.displayText.substring(0, cursor.x - 1) + line.displayText.substring(cursor.x);
                            updateDisplayText(line, true);
                            cursor.x--;
                            setTextAtCursorVisible(true);
                            GL.that.renderOnce();
                            sendKeyTyped(character);
                            return true;
                        } else {
                            if (cursor.y > 0) {
                                setCursorLine(cursor.y - 1, true);
                                Line line2 = getNthLine(cursor.y);
                                if (line2 != null) {
                                    cursor.x = line2.displayText.length();
                                    setTextAtCursorVisible(true);
                                    line2.displayText = line2.displayText + line.displayText;
                                    lines.remove(cursor.y + 1);
                                    updateDisplayText(line2, true);

                                    int lineCount = lines.size();
                                    sendLineCountChanged(lineCount, this.style.font.getLineHeight() * lineCount);
                                }
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
                if (selectedArea != null) {
                    deleteSelection();
                    return true;
                } else {
                    if (cursor.x < line.displayText.length()) {
                        line.displayText = line.displayText.substring(0, cursor.x) + line.displayText.substring(cursor.x + 1);
                        updateDisplayText(line, true);
                        GL.that.renderOnce();
                        sendKeyTyped(character);
                        return true;
                    } else {
                        if (cursor.y + 1 < lines.size()) {
                            cursor.y++;
                            Line line2 = getNthLine(cursor.y);
                            if (line2 != null) {
                                cursor.y--;
                                lines.remove(line2);
                                line.displayText += line2.displayText;
                                updateDisplayText(line, true);
                                GL.that.renderOnce();
                            }
                        }
                        sendKeyTyped(character);
                        return true;
                    }
                }
            }

            if (character == ENTER_DESKTOP || character == ENTER_ANDROID) {
                if (isMultiLine()) {
                    if (selectedArea != null)
                        deleteSelection();
                    insertNewLine();
                    clearSelection();
                    sendKeyTyped(character);
                    return true;
                }
            }

            if (font.getData().getGlyph(character) != null) {
                if (selectedArea != null)
                    deleteSelection();
                {
                    try {
                        line.displayText = line.displayText.substring(0, cursor.x) + character + line.displayText.substring(cursor.x);
                        updateDisplayText(line, true);
                        cursor.x++;
                        setTextAtCursorVisible(true);
                    } catch (Exception e) {
                        return false;
                    }
                }

                GL.that.renderOnce();
            }
            sendKeyTyped(character);
            return true;
        } else
            return false;
    }

    private boolean isMultiLine() {
        return (mWrapType == WrapType.MULTILINE) || (mWrapType == WrapType.WRAPPED);
    }

    public String getText() {

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Line line : lines) {
            if ((index > 0) && (!line.autoWrap)) {
                sb.append(br);
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

    public void setText(String inputText) {
        if (inputText == null)
            throw new IllegalArgumentException("text cannot be null.");

        BitmapFont font = style.font;

        StringBuilder buffer = new StringBuilder();
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

        cursor.x = 0;
        cursor.y = 0;

        this.text = "";

        boolean stateIsEditable = isEditable;
        isEditable = true;
        for (int i = 0; i < bText.length(); i++) {
            char c = bText.charAt(i);
            keyTyped(c, true);
        }
        isEditable = stateIsEditable;

        setCursorPosition(inputText.length());

    }

    public String getSelectedText() {
        if (selectedArea == null)
            return "";

        StringBuilder sb = new StringBuilder();
        // Alle Zeilen durchgehen, in denen selektierter Text liegt
        for (int n = selectedArea.cursorStart.y; n <= selectedArea.cursorEnd.y; n++) {
            Line line = getNthLine(n);
            if (line != null) {
                int startPos = 0;
                int endPos = line.displayText.length();

                if (n == selectedArea.cursorStart.y) {
                    startPos = selectedArea.cursorStart.x;
                }
                if (n == selectedArea.cursorEnd.y) {
                    endPos = selectedArea.cursorEnd.x;
                }

                sb.append(line.displayText, startPos, endPos);
                if (n < selectedArea.cursorEnd.y) {
                    sb.append(br);
                }
            }
        }
        String ret = sb.toString();

        if (this.mWrapType == WrapType.SINGLELINE) {
            ret = ret.replace("\n", "");
            ret = ret.replace("\r", "");
        }

        return ret;
    }

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

        Point cursorStart = new Point(0, 0);
        cursorStart.x = selectionStart;
        Point cursorEnd = new Point(0, 0);
        cursorEnd.x = selectionEnd;
        selectedArea = new Selection(cursorStart, cursorEnd);
        // showSelectionMarker(SelectionMarker.Type.Left, selection.cursorStart);
        // showSelectionMarker(SelectionMarker.Type.Right, selection.cursorEnd);

    }

    private void clearSelection() {
        selectedArea = null;
    }

    public void setCursorPosition(int cursorPosition) {
        if (cursorPosition < 0)
            throw new IllegalArgumentException("cursorPosition must be >= 0");

        clearSelection();

        for (int n = 0; n < lines.size(); n++) {
            Line line = getNthLine(n);
            if (line != null) {
                if (cursorPosition < line.displayText.length()) {
                    cursor.y = n;
                    cursor.x = cursorPosition;
                    break;
                }
                cursorPosition = cursorPosition - line.displayText.length() + 1;
            }
        }

        setTextAtCursorVisible(true);
    }

    public BitmapFont getFont() {
        return style.font;
    }

    public void setFocus(boolean value) {
        if (value != GL.that.hasFocus(this)) {
            if (value) {
                GL.that.setFocusedEditTextField(this);
            } else {
                if (GL.that.hasFocus(this))
                    GL.that.setFocusedEditTextField(null);
            }
        }
    }

    @Override
    public void becomesFocus() {
        if (becomesFocusListener != null)
            becomesFocusListener.becomesFocus();
        this.calculateSizeDependencies(true);
    }

    public int getSelectionStart() {
        if (selectedArea == null) {
            int pos = 0;
            for (int n = 0; n <= cursor.y; n++) {
                Line line = getNthLine(n);
                if (line != null) {
                    if (n == cursor.y) {
                        pos += cursor.x;
                        break;
                    }
                    pos = pos + line.displayText.length() + 1;
                }
            }
            return pos;
        }

        int pos = 0;
        for (int n = 0; n <= selectedArea.cursorStart.y; n++) {
            Line line = getNthLine(n);
            if (line != null) {
                if (n == selectedArea.cursorStart.y) {
                    pos = pos + selectedArea.cursorStart.x;
                    break;
                }
                pos = pos + line.displayText.length() + 1;
            }
        }
        return pos;

    }

    public int getSelectionEnd() {
        if (selectedArea == null) {
            int pos = 0;
            for (int n = 0; n <= cursor.y; n++) {
                Line line = getNthLine(n);
                if (line != null) {
                    if (n == cursor.y) {
                        pos = pos + cursor.x;
                        break;
                    }
                    pos = pos + line.displayText.length() + 1;
                }
            }
            return pos;
        }

        int pos = 0;
        for (int n = 0; n <= selectedArea.cursorEnd.y; n++) {
            Line line = getNthLine(n);
            if (line != null) {
                if (n == selectedArea.cursorEnd.y) {
                    pos += selectedArea.cursorEnd.x;
                    break;
                }
                pos = pos + line.displayText.length() + 1;
            }
        }
        return pos;
    }

    public float getMeasuredHeight() {

        float h = 0;

        for (Line line : lines) {
            h = h + line.getTextBounds().height;
        }

        return h;
    }

    public int getTopLineNo() {
        return topLine;
    }

    public void showFromLineNo(int lineNo) {
        topLine = Math.max(lineNo, 0);
    }

    public int getLineCount() {
        return lines.size();
    }

    public void showLastLines() {
        if (lines.size() < this.maxLineCount) {
            topLine = 0;
        } else {
            topLine = lines.size() - this.maxLineCount;
        }
    }

    public void addListPosChangedHandler(IListPosChanged handler) {
        if (!listPosChangedHandlers.contains(handler))
            listPosChangedHandlers.add(handler);
    }

    private void callListPosChangedEvent() {
        for (int i = 0, n = listPosChangedHandlers.size(); i < n; i++) {
            IListPosChanged handler = listPosChangedHandlers.get(i);
            if (handler != null)
                handler.listPosChanged();
        }
    }

    private void computeGlyphAdvancesAndPositions(CharSequence str, FloatArray glyphAdvances, FloatArray glyphPositions) {
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
            /**
             * shows PastePopUp<br>
             */
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                hideSelectionMarker();
                selectedArea = new Selection(getClickedCursor(x, y), getClickedCursor(x, y));
                showSelectionMarker(SelectionMarker.Type.Left, selectedArea.cursorStart);
                showSelectionMarker(SelectionMarker.Type.Right, selectedArea.cursorEnd);
                if (isEditable)
                    showCopyPastePopUp(x, y);
                else {
                    showCopyPopUp(x, y);
                }
                return true;
            }
        });
    }

    public interface IListPosChanged {
        void listPosChanged();
    }

    //#########################################################################################################

    private static class Selection {
        private final Point cursorStart;
        private final Point cursorEnd;

        private Selection(Point cursorStart, Point cursorEnd) {
            this.cursorStart = cursorStart;
            this.cursorEnd = cursorEnd;
        }
    }

    private class Line {
        private final GlyphLayout textBounds = new GlyphLayout();
        private final FloatArray glyphAdvances = new FloatArray();
        private final FloatArray glyphPositions = new FloatArray();
        private String displayText;
        private boolean autoWrap;

        Line(String displayText, BitmapFont font) {
            this.displayText = displayText;

            calcTextBounds(font);
        }

        Line(String displayText, boolean autoWrap, BitmapFont font) {
            this.displayText = displayText;
            this.autoWrap = autoWrap;

            calcTextBounds(font);
        }

        private void calcTextBounds(BitmapFont font) {
            textBounds.setText(font, displayText);
            computeGlyphAdvancesAndPositions(displayText, glyphAdvances, glyphPositions);
        }

        GlyphLayout getTextBounds() {
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
}
