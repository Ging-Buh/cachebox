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

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Controls.PopUps.CopyPastePopUp;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.interfaces.ICopyPaste;
import CB_UI_Base.Global;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Clipboard;

import java.util.Timer;
import java.util.TimerTask;

public abstract class EditTextFieldBase extends CB_View_Base implements ICopyPaste {
    static public final char BACKSPACE = 8;
    static public final char ENTER_DESKTOP = '\r';
    static public final char ENTER_ANDROID = '\n';
    static public final char TAB = '\t';
    static public final char DELETE = 127;
    static public final char BULLET = 149;

    protected boolean KeyboardPopupDisabled = false;
    protected boolean isEditable = true;
    protected TextFieldListener listener;
    protected TextFieldFilter filter;
    protected Clipboard clipboard;
    protected CopyPastePopUp popUp;
    protected boolean cursorOn = true;
    protected long blinkTime = 420;
    protected Timer blinkTimer;
    protected IBecomesFocus becomesFocusListener;

    public EditTextFieldBase(CB_RectF rec, CB_View_Base parent, String Name) {
        super(rec, parent, Name);
        registerPopUpLongClick();
        clipboard = Global.getDefaultClipboard();
        this.setDoubleClickable(true);
    }

    public static TextFieldStyle getDefaultStyle() {
        TextFieldStyle ret = new TextFieldStyle();

        ret.setBackground(Sprites.textFieldBackground, Sprites.textFieldBackgroundFocus);
        ret.font = Fonts.getNormal();
        ret.fontColor = COLOR.getFontColor();

        ret.messageFont = Fonts.getSmall();
        ret.messageFontColor = COLOR.getDisableFontColor();

        ret.cursor = Sprites.textFieldCursor;

        ret.selection = Sprites.selection;

        return ret;
    }

    public abstract boolean keyTyped(char character);

    public abstract boolean keyUp(int KeyCode);

    public abstract boolean keyDown(int KeyCode);

	/*
	public void disable() {
	disabled = true;
	}
	
	public void enable() {
	disabled = false;
	}
	*/

    protected void blinkStart() {
        blinkTimer = new Timer();
        TimerTask blinkTimerTask = new TimerTask() {
            @Override
            public void run() {
                cursorOn = !cursorOn;
                GL.that.renderOnce();
            }
        };
        blinkTimer.scheduleAtFixedRate(blinkTimerTask, 0, blinkTime);
    }

    protected void blinkStop() {
        try {
            blinkTimer.cancel();
            cursorOn = false;
        } catch (Exception ex) {

        }

        blinkTimer = null;
    }

    /**
     * @param newlistener May be null.
     */
    public void setTextFieldListener(TextFieldListener newlistener) {
        this.listener = newlistener;
    }

    public void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    protected abstract void registerPopUpLongClick();

    protected void showPastePopUp(int x, int y) {
        if (popUp != null)
            popUp.close();
        popUp = new CopyPastePopUp(this.name + " popUp", this);
        popUp.setOnlyPaste();
        layoutAndShowPopUp(x, y);
    }

    protected void showCopyPastePopUp(int x, int y) {
        if (popUp != null)
            popUp.close();
        popUp = new CopyPastePopUp(this.name + " popUp", this);
        layoutAndShowPopUp(x, y);
    }

    protected void showCopyPopUp(int x, int y) {
        if (popUp != null)
            popUp.close();
        popUp = new CopyPastePopUp(this.name + " popUp", this);
        popUp.setOnlyCopy();
        layoutAndShowPopUp(x, y);
    }

    private void layoutAndShowPopUp(int x, int y) {

        float noseOffset = popUp.getHalfWidth() / 2;

        CB_RectF world = getWorldRec();

        // not enough place on Top?
        float windowH = UI_Size_Base.that.getWindowHeight();
        float windowW = UI_Size_Base.that.getWindowWidth();
        float worldY = world.getY();

        if (popUp.getHeight() + worldY > windowH * 0.8f) {
            popUp.flipX();
            worldY -= popUp.getHeight() + (popUp.getHeight() * 0.2f);
        }

        x += world.getX() - noseOffset;

        if (x < 0)
            x = 0;
        if (x + popUp.getWidth() > windowW)
            x = (int) (windowW - popUp.getWidth());

        y += worldY + (popUp.getHeight() * 0.2f);
        popUp.showNotCloseAutomaticly(x, y);

    }

    protected void hidePopUp() {
        if (popUp != null)
            popUp.close();
    }

    /**
     * pasteFromClipboard
     */
    @Override
    public abstract String pasteFromClipboard();

    /**
     * copyToClipboard
     */
    @Override
    public abstract String copyToClipboard();

    /**
     * cutToClipboard
     */
    @Override
    public abstract String cutToClipboard();

    /**
     * isEditable
     */
    @Override
    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean value) {
        isEditable = value;
        if (!isEditable) {
            KeyboardPopupDisabled = true;
        }
    }

    protected void sendKeyTyped(final char character) {
        if (listener != null) {
            final EditTextFieldBase that = this;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    listener.keyTyped(that, character);
                }
            });
            th.start();
        }
    }

    protected void sendLineCountChanged(final int lineCount, final float textHeight) {
        if (listener != null) {
            final EditTextFieldBase that = this;
            Thread th = new Thread(new Runnable() {
                /**
                 * run
                 */
                @Override
                public void run() {
                    listener.lineCountChanged(that, lineCount, textHeight);
                }
            });
            th.start();
        }

    }

    public void disableKeyboardPopup() {
        KeyboardPopupDisabled = true;
    }

    public boolean isKeyboardPopupDisabled() {
        return KeyboardPopupDisabled;
    }

    public void setBecomesFocusListener(IBecomesFocus becomesFocusListener) {
        this.becomesFocusListener = becomesFocusListener;
    }

    /**
     *
     */
    public void becomesFocus() {
        if (becomesFocusListener != null)
            becomesFocusListener.becomesFocus();
    }

    /**
     * Interface for listening to typed characters.
     *
     * @author mzechner
     */
    public interface TextFieldListener {
        void keyTyped(EditTextFieldBase textField, char key);

        void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight);
    }

    /**
     * Interface for filtering characters entered into the text field.
     *
     * @author mzechner
     */
    public interface TextFieldFilter {
        /**
         * @param textField
         * @param key
         * @return whether to accept the character
         */
        boolean acceptChar(EditTextFieldBase textField, char key);

        class DigitsOnlyFilter implements TextFieldFilter {
            @Override
            public boolean acceptChar(EditTextFieldBase textField, char key) {
                return Character.isDigit(key);
            }

        }
    }

    public interface IBecomesFocus {
        void becomesFocus();
    }

    /**
     * The style for a text field.
     *
     * @author mzechner
     */
    static public class TextFieldStyle {
        /**
         * Optional.
         */
        public Drawable cursor, selection;
        public BitmapFont font;
        public Color fontColor;
        /**
         * Optional.
         */
        public BitmapFont messageFont;
        /**
         * Optional.
         */
        public Color messageFontColor;
        private Drawable background, backgroundFocused;

        public TextFieldStyle() {
        }

        public TextFieldStyle(BitmapFont font, Color fontColor, BitmapFont messageFont, Color messageFontColor, Drawable cursor, Drawable selection, Drawable background, Drawable backgroundFocused) {
            this.messageFont = messageFont;
            this.messageFontColor = messageFontColor;
            this.background = background;
            this.backgroundFocused = backgroundFocused;
            this.cursor = cursor;
            this.font = font;
            this.fontColor = fontColor;
            this.selection = selection;
        }

        public TextFieldStyle(TextFieldStyle style) {
            this.messageFont = style.messageFont;
            if (style.messageFontColor != null)
                this.messageFontColor = new Color(style.messageFontColor);
            this.background = style.background;
            backgroundFocused = style.backgroundFocused;
            this.cursor = style.cursor;
            this.font = style.font;
            if (style.fontColor != null)
                this.fontColor = new Color(style.fontColor);
            this.selection = style.selection;
        }

        public Drawable getBackground(boolean focused) {
            if (focused)
                return this.backgroundFocused;
            else
                return this.background;
        }

        public void setBackground(Drawable background, Drawable backgroundFocused) {
            this.background = background;
            this.backgroundFocused = backgroundFocused;
        }

        public float getLeftWidth(boolean focused) {
            Drawable whichBackground;
            if (focused)
                whichBackground = this.backgroundFocused;
            else
                whichBackground = this.background;
            if (whichBackground == null)
                return 0f;
            else
                return whichBackground.getLeftWidth();
        }

        public float getRightWidth(boolean focused) {
            Drawable whichBackground;
            if (focused)
                whichBackground = this.backgroundFocused;
            else
                whichBackground = this.background;
            if (whichBackground == null)
                return 0f;
            else
                return whichBackground.getRightWidth();
        }

        public float getTopHeight(boolean focused) {
            Drawable whichBackground;
            if (focused)
                whichBackground = this.backgroundFocused;
            else
                whichBackground = this.background;
            if (whichBackground == null)
                return 0f;
            else
                return whichBackground.getTopHeight();
        }

        public float getBottomHeight(boolean focused) {
            Drawable whichBackground;
            if (focused)
                whichBackground = this.backgroundFocused;
            else
                whichBackground = this.background;
            if (whichBackground == null)
                return 0f;
            else
                return whichBackground.getBottomHeight();
        }
    }
}
