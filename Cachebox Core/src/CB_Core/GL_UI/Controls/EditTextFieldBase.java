package CB_Core.GL_UI.Controls;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.PopUps.CopiePastePopUp;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Clipboard;

public abstract class EditTextFieldBase extends CB_View_Base
{
	static public final char BACKSPACE = 8;
	static public final char ENTER_DESKTOP = '\r';
	static public final char ENTER_ANDROID = '\n';
	static public final char TAB = '\t';
	static public final char DELETE = 127;
	static public final char BULLET = 149;

	public EditTextFieldBase that;

	public EditTextFieldBase(CB_View_Base parent, CB_RectF rec, String Name)
	{
		super(rec, Name);
		this.parent = parent;
		that = this;
		registerPopUpLongClick();

		clipboard = GlobalCore.getDefaultClipboard();
	}

	public abstract boolean keyTyped(char character);

	public abstract boolean keyUp(int KeyCode);

	public abstract boolean keyDown(int KeyCode);

	protected TextFieldListener listener;
	protected TextFieldFilter filter;
	protected OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();
	protected Clipboard clipboard;
	protected CopiePastePopUp popUp;
	protected boolean disabled = false;

	protected boolean cursorOn = true;
	protected long blinkTime = 420;
	protected Timer blinkTimer;

	protected void blinkStart()
	{
		blinkTimer = new Timer();
		TimerTask blinkTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				cursorOn = !cursorOn;
				GL_Listener.glListener.renderOnce("TextFieldBase: CursorBlink");
			}
		};
		blinkTimer.scheduleAtFixedRate(blinkTimerTask, 0, blinkTime);
	}

	protected void blinkStop()
	{
		try
		{
			blinkTimer.cancel();
			cursorOn = false;
		}
		catch (Exception ex)
		{

		}

		blinkTimer = null;
	}

	public void disable()
	{
		disabled = true;
	}

	public void enable()
	{
		disabled = false;
	}

	/**
	 * Interface for listening to typed characters.
	 * 
	 * @author mzechner
	 */
	static public interface TextFieldListener
	{
		public void keyTyped(EditTextFieldBase textField, char key);

		public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight);
	}

	/**
	 * @param listener
	 *            May be null.
	 */
	public void setTextFieldListener(TextFieldListener newlistener)
	{
		this.listener = newlistener;
	}

	/**
	 * Interface for filtering characters entered into the text field.
	 * 
	 * @author mzechner
	 */
	static public interface TextFieldFilter
	{
		/**
		 * @param textField
		 * @param key
		 * @return whether to accept the character
		 */
		public boolean acceptChar(EditTextFieldBase textField, char key);

		static public class DigitsOnlyFilter implements TextFieldFilter
		{
			@Override
			public boolean acceptChar(EditTextFieldBase textField, char key)
			{
				return Character.isDigit(key);
			}

		}
	}

	/**
	 * An interface for onscreen keyboards. Can invoke the default keyboard or render your own keyboard!
	 * 
	 * @author mzechner
	 */
	static public interface OnscreenKeyboard
	{
		public void show(boolean visible);
	}

	/**
	 * The default {@link OnscreenKeyboard} used by all {@link TextField} instances. Just uses
	 * {@link Input#setOnscreenKeyboardVisible(boolean)} as appropriate. Might overlap your actual rendering, so use with care!
	 * 
	 * @author mzechner
	 */
	static public class DefaultOnscreenKeyboard implements OnscreenKeyboard
	{
		@Override
		public void show(boolean visible)
		{
			Gdx.input.setOnscreenKeyboardVisible(visible);
		}
	}

	/**
	 * The style for a text field, see {@link TextField}.
	 * 
	 * @author mzechner
	 */
	static public class TextFieldStyle
	{
		/** Optional. */
		public Drawable background, cursor, backgroundFocused, selection;
		public BitmapFont font;
		public Color fontColor;

		/** Optional. */
		public BitmapFont messageFont;
		/** Optional. */
		public Color messageFontColor;

		public TextFieldStyle()
		{
		}

		public TextFieldStyle(BitmapFont font, Color fontColor, BitmapFont messageFont, Color messageFontColor, Drawable cursor,
				Drawable selection, Drawable background, Drawable backgroundFocused)
		{
			this.messageFont = messageFont;
			this.messageFontColor = messageFontColor;
			this.background = background;
			this.backgroundFocused = backgroundFocused;
			this.cursor = cursor;
			this.font = font;
			this.fontColor = fontColor;
			this.selection = selection;
		}

		public TextFieldStyle(TextFieldStyle style)
		{
			this.messageFont = style.messageFont;
			if (style.messageFontColor != null) this.messageFontColor = new Color(style.messageFontColor);
			this.background = style.background;
			this.cursor = style.cursor;
			this.font = style.font;
			if (style.fontColor != null) this.fontColor = new Color(style.fontColor);
			this.selection = style.selection;
		}
	}

	public static TextFieldStyle getDefaultStyle()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = SpriteCache.textFiledBackground;
		ret.backgroundFocused = SpriteCache.textFiledBackgroundFocus;
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = SpriteCache.textFieldCursor;

		ret.selection = SpriteCache.selection;

		return ret;
	}

	boolean hasFocus = false;

	public void setFocus()
	{
		setFocus(true);
	}

	public void setFocus(boolean value)
	{
		hasFocus = value;
		if (value == true) GL_Listener.setKeyboardFocus(this);
		else
		{
			if (GL_Listener.getKeyboardFocus() == this) GL_Listener.setKeyboardFocus(null);
		}
		GL_Listener.glListener.renderForTextField(this);

	}

	public void resetFocus()
	{
		hasFocus = false;
		GL_Listener.setKeyboardFocus(null);

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

	protected void registerPopUpLongClick()
	{
		this.setOnLongClickListener(new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showPopUp(x, y);
				return true;
			}

		});
	}

	protected void showPopUp(int x, int y)
	{
		if (popUp == null)
		{
			popUp = new CopiePastePopUp("CopiePastePopUp=>" + getName(), that);
		}

		float noseOffset = popUp.getHalfWidth() / 2;

		// Logger.LogCat("Show CopyPaste PopUp");

		CB_RectF world = getWorldRec();

		// not enough place on Top?
		float windowH = UiSizes.getWindowHeight();
		float windowW = UiSizes.getWindowWidth();
		float worldY = world.getY();

		if (popUp.getHeight() + worldY > windowH * 0.8f)
		{
			popUp.flipX();
			worldY -= popUp.getHeight() + (popUp.getHeight() * 0.2f);
		}

		x += world.getX() - noseOffset;

		if (x < 0) x = 0;
		if (x + popUp.getWidth() > windowW) x = (int) (windowW - popUp.getWidth());

		y += worldY + (popUp.getHeight() * 0.2f);
		popUp.show(x, y);
	}

	public abstract void pasteFromClipboard();

	public abstract void copyToClipboard();

	public abstract void cutToClipboard();

}
