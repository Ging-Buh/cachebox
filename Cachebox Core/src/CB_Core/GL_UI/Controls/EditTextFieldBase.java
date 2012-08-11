package CB_Core.GL_UI.Controls;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Clipboard;

public abstract class EditTextFieldBase extends CB_View_Base
{

	public EditTextFieldBase(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	public abstract boolean keyTyped(char character);

	public abstract boolean keyUp(int KeyCode);

	public abstract boolean keyDown(int KeyCode);

	protected TextFieldListener listener;
	protected TextFieldFilter filter;
	protected OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();
	protected Clipboard clipboard;
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
		public NinePatch background, cursor, backgroundFocused;
		public BitmapFont font;
		public Color fontColor;
		/** Optional. */
		public TextureRegion selection;
		/** Optional. */
		public BitmapFont messageFont;
		/** Optional. */
		public Color messageFontColor;

		public TextFieldStyle()
		{
		}

		public TextFieldStyle(BitmapFont font, Color fontColor, BitmapFont messageFont, Color messageFontColor, NinePatch cursor,
				TextureRegion selection, NinePatch background, NinePatch backgroundFocused)
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

		ret.background = new NinePatch(SpriteCache.getThemedSprite("text-field-back"), 16, 16, 16, 16);
		ret.backgroundFocused = new NinePatch(SpriteCache.getThemedSprite("text-field-back-focus"), 16, 16, 16, 16);
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2);

		ret.selection = SpriteCache.getThemedSprite("InfoPanelBack");

		return ret;
	}

	boolean hasFocus = false;

	public void setFocus()
	{
		hasFocus = true;
		this.onTouchDown(0, 0, 0, 0);

		GL_Listener.glListener.renderForTextField(this);
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

}
