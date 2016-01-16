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

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Clipboard;

import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.PopUps.CopyPastePopUp;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.interfaces.ICopyPaste;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public abstract class EditTextFieldBase extends CB_View_Base implements ICopyPaste {
    static public final char BACKSPACE = 8;
    static public final char ENTER_DESKTOP = '\r';
    static public final char ENTER_ANDROID = '\n';
    static public final char TAB = '\t';
    static public final char DELETE = 127;
    static public final char BULLET = 149;

    protected boolean dontShowKeyBoard = false;
    protected boolean isEditable = true;

    public EditTextFieldBase(CB_RectF rec, CB_View_Base parent, String Name) {
	super(rec, Name);
	this.parent = parent;
	registerPopUpLongClick();

	clipboard = Global.getDefaultClipboard();
	this.setDoubleClickable(true);
    }

    public EditTextFieldBase(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name) {
	super(X, Y, Width, Height, Parent, Name);
	registerPopUpLongClick();

	clipboard = Global.getDefaultClipboard();
	this.setDoubleClickable(true);
    }

    public abstract boolean keyTyped(char character);

    public abstract boolean keyUp(int KeyCode);

    public abstract boolean keyDown(int KeyCode);

    protected TextFieldListener listener;
    protected TextFieldFilter filter;
    protected OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();
    protected Clipboard clipboard;
    protected CopyPastePopUp popUp;
    protected boolean disabled = false;

    protected boolean cursorOn = true;
    protected long blinkTime = 420;
    protected Timer blinkTimer;

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

    public void disable() {
	disabled = true;
    }

    public void enable() {
	disabled = false;
    }

    /**
     * Interface for listening to typed characters.
     * 
     * @author mzechner
     */
    static public interface TextFieldListener {
	public void keyTyped(EditTextFieldBase textField, char key);

	public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight);
    }

    /**
     * @param listener
     *            May be null.
     */
    public void setTextFieldListener(TextFieldListener newlistener) {
	this.listener = newlistener;
    }

    /**
     * Interface for filtering characters entered into the text field.
     * 
     * @author mzechner
     */
    static public interface TextFieldFilter {
	/**
	 * @param textField
	 * @param key
	 * @return whether to accept the character
	 */
	public boolean acceptChar(EditTextFieldBase textField, char key);

	static public class DigitsOnlyFilter implements TextFieldFilter {
	    @Override
	    public boolean acceptChar(EditTextFieldBase textField, char key) {
		return Character.isDigit(key);
	    }

	}
    }

    /**
     * An interface for onscreen keyboards. Can invoke the default keyboard or render your own keyboard!
     * 
     * @author mzechner
     */
    static public interface OnscreenKeyboard {
	public void show(boolean visible);
    }

    /**
     * The default {@link OnscreenKeyboard} used by all {@link TextField} instances. Just uses
     * {@link Input#setOnscreenKeyboardVisible(boolean)} as appropriate. Might overlap your actual rendering, so use with care!
     * 
     * @author mzechner
     */
    static public class DefaultOnscreenKeyboard implements OnscreenKeyboard {
	@Override
	public void show(boolean visible) {
	    Gdx.input.setOnscreenKeyboardVisible(visible);
	}
    }

    /**
     * The style for a text field, see {@link TextField}.
     * 
     * @author mzechner
     */
    static public class TextFieldStyle {
	/** Optional. */
	public Drawable background, cursor, backgroundFocused, selection;
	public BitmapFont font;
	public Color fontColor;

	/** Optional. */
	public BitmapFont messageFont;
	/** Optional. */
	public Color messageFontColor;

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
	    this.cursor = style.cursor;
	    this.font = style.font;
	    if (style.fontColor != null)
		this.fontColor = new Color(style.fontColor);
	    this.selection = style.selection;
	}
    }

    public static TextFieldStyle getDefaultStyle() {
	TextFieldStyle ret = new TextFieldStyle();

	ret.background = SpriteCacheBase.textFiledBackground;
	ret.backgroundFocused = SpriteCacheBase.textFiledBackgroundFocus;
	ret.font = Fonts.getNormal();
	ret.fontColor = COLOR.getFontColor();

	ret.messageFont = Fonts.getSmall();
	ret.messageFontColor = COLOR.getDisableFontColor();

	ret.cursor = SpriteCacheBase.textFieldCursor;

	ret.selection = SpriteCacheBase.selection;

	return ret;
    }

    /** Default is an instance of {@link DefaultOnscreenKeyboard}. */
    public OnscreenKeyboard getOnscreenKeyboard() {
	return keyboard;
    }

    public void setOnscreenKeyboard(OnscreenKeyboard keyboard) {
	this.keyboard = keyboard;
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

    public void dontShowSoftKeyBoardOnFocus() {
	dontShowSoftKeyBoardOnFocus(true);
    }

    public void dontShowSoftKeyBoardOnFocus(boolean value) {
	dontShowKeyBoard = value;
    }

    public boolean dontShowKeyBoard() {
	return dontShowKeyBoard;
    }

    public interface IBecomesFocus {
	public void becomesFocus();
    }

    protected IBecomesFocus becomesFocusListener;

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
}
