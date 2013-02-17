package CB_Core.GL_UI.Controls.MessageBox;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Settings.SettingBool;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class GL_MsgBox extends Dialog
{
	static GL_MsgBox that;
	public static final int BUTTON_POSITIVE = 1;
	public static final int BUTTON_NEUTRAL = 2;
	public static final int BUTTON_NEGATIVE = 3;

	private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

	// TODO make private with getter and setter *********
	public Button button1;
	public Button button2;
	public Button button3;
	public OnMsgBoxClickListener mMsgBoxClickListner;
	public OnClickListener positiveButtonClickListener;
	public OnClickListener neutralButtonClickListener;
	public OnClickListener negativeButtonClickListener;

	public Label label;

	protected SettingBool rememberSetting = null;
	protected chkBox chkRemember;

	// **************************************************

	public static GL_MsgBox Show(String msg, OnMsgBoxClickListener Listener)
	{
		return Show(msg, "", Listener);
	}

	public static GL_MsgBox Show(String msg, String title, OnMsgBoxClickListener Listener)
	{
		return Show(msg, title, MessageBoxButtons.OK, Listener, null);
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxIcon icon)
	{
		return Show(msg, title, MessageBoxButtons.OK, icon, null, null);
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener)
	{
		return Show(msg, title, buttons, icon, Listener, null);
	}

	public static GL_MsgBox Show(String msg)
	{
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, false, true, false), "MsgBox" + msg.substring(0, Math.max(10, msg.length())));
		msgBox.setButtonCaptions(MessageBoxButtons.OK);
		msgBox.label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		msgBox.label.setZeroPos();
		msgBox.label.setWrappedText(msg);
		msgBox.addChild(msgBox.label);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, OnMsgBoxClickListener Listener, SettingBool remember)
	{

		if (remember != null && remember.getValue())
		{
			// wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
			// Wir liefern nur ein On Click auf den OK Button zurück!
			if (Listener != null)
			{
				Listener.onClick(BUTTON_POSITIVE, null);
			}
			return null;
		}

		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), false, (remember != null)),
				"MsgBox" + title);
		msgBox.rememberSetting = remember;
		msgBox.mMsgBoxClickListner = Listener;
		msgBox.setButtonCaptions(buttons);
		msgBox.setTitle(title);
		msgBox.label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		msgBox.label.setZeroPos();
		msgBox.label.setWrappedText(msg);
		msgBox.addChild(msgBox.label);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener,
			SettingBool remember)
	{

		if (remember != null && remember.getValue())
		{
			// wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
			// Wir liefern nur ein On Click auf den OK Button zurück!
			if (Listener != null)
			{
				Listener.onClick(BUTTON_POSITIVE, null);
			}
			return null;
		}

		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), true, (remember != null)),
				"MsgBox" + title);
		msgBox.rememberSetting = remember;
		msgBox.mMsgBoxClickListner = Listener;
		msgBox.setTitle(title);

		msgBox.setButtonCaptions(buttons);

		SizeF contentSize = msgBox.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight());

		Image iconImage = new Image(imageRec, "MsgBoxIcon");
		if (icon != MessageBoxIcon.None) iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
		msgBox.addChild(iconImage);

		msgBox.label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		msgBox.label.setWidth(contentSize.getBounds().getWidth() - 5 - UiSizes.getButtonHeight());
		msgBox.label.setX(imageRec.getMaxX() + 5);
		msgBox.label.setY(0);
		msgBox.label.setWrappedText(msg);
		msgBox.addChild(msgBox.label);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	/**
	 * Interface used to allow the creator of a dialog to run some code when an item on the dialog is clicked..
	 */
	public interface OnMsgBoxClickListener
	{
		/**
		 * This method will be invoked when a button in the dialog is clicked.
		 * 
		 * @param which
		 *            The button that was clicked ( the position of the item clicked.
		 * @return
		 */
		public boolean onClick(int which, Object data);
	}

	public GL_MsgBox(CB_RectF rec, String Name)
	{
		super(rec, Name);
		setFooterHeight(80);
		that = this;
	}

	public GL_MsgBox(Size size, String name)
	{
		super(size.getBounds().asFloat(), name);
		that = this;
	}

	private boolean ButtonClick(int button)
	{

		// wenn Dies eine Remember MsgBox ist, überprüfen wir ob das remember gesetzt ist
		if (rememberSetting != null)
		{
			if (chkRemember.isChecked())
			{
				// User hat Remember aktiviert, was hier abgespeichert wird!
				rememberSetting.setValue(true);
				Config.AcceptChanges();
			}
		}
		GL.that.closeDialog(that);

		boolean retValue = false;
		if (mMsgBoxClickListner != null)
		{
			retValue = mMsgBoxClickListner.onClick(button, that.data);
		}

		return retValue;
	}

	public static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon)
	{
		return calcMsgBoxSize(Text, hasTitle, hasButtons, hasIcon, false);
	}

	public static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon, boolean hasRemember)
	{
		float Width = (((UiSizes.getButtonWidthWide() + margin) * 3) + margin);

		if (Width * 1.2 < UiSizes.getWindowWidth()) Width *= 1.2f;

		float MsgWidth = (Width * 0.95f) - 5 - UiSizes.getButtonHeight();

		TextBounds bounds = Fonts.MeasureWrapped(Text, MsgWidth);
		float MeasuredTextHeight = bounds.height + (margin * 2);

		int Height = (int) (hasIcon ? Math.max(MeasuredTextHeight, (int) UiSizes.getButtonHeight()) : (int) MeasuredTextHeight);

		if (hasTitle)
		{
			TextBounds titleBounds = Fonts.Measure("T");
			Height += (titleBounds.height * 3);
			Height += margin * 2;
		}
		Height += calcFooterHeight(hasButtons);
		if (hasRemember) Height += UiSizes.getChkBoxSize().height;
		Height += calcHeaderHeight();

		// min Height festlegen
		Height = (int) Math.max(Height, UiSizes.getButtonHeight() * 2.5f);

		// max Height festlegen
		Height = (int) Math.min(Height, UiSizes.getWindowHeight() * 0.95f);

		Size ret = new Size((int) Width, Height);
		return ret;
	}

	public void setButtonCaptions(MessageBoxButtons buttons)
	{
		BitmapFont font = Fonts.getNormal();
		Color color = Fonts.getFontColor();
		int button = buttons.ordinal();
		if (button == MessageBoxButtons.AbortRetryIgnore.ordinal())
		{
			createButtons(this, 3);
			button1.setText(Translation.Get("abort"), font, color);
			button2.setText(Translation.Get("retry"), font, color);
			button3.setText(Translation.Get("ignore"), font, color);
		}
		else if (button == MessageBoxButtons.OK.ordinal())
		{
			createButtons(this, 1);
			button1.setText(Translation.Get("ok"), font, color);
		}
		else if (button == MessageBoxButtons.OKCancel.ordinal())
		{
			createButtons(this, 2);
			button1.setText(Translation.Get("ok"), font, color);
			button3.setText(Translation.Get("cancel"), font, color);
		}
		else if (button == MessageBoxButtons.RetryCancel.ordinal())
		{
			createButtons(this, 2);
			button1.setText(Translation.Get("retry"), font, color);
			button3.setText(Translation.Get("cancel"), font, color);
		}
		else if (button == MessageBoxButtons.YesNo.ordinal())
		{
			createButtons(this, 2);
			button1.setText(Translation.Get("yes"), font, color);
			button3.setText(Translation.Get("no"), font, color);
		}
		else if (button == MessageBoxButtons.YesNoCancel.ordinal())
		{
			createButtons(this, 3);
			button1.setText(Translation.Get("yes"), font, color);
			button2.setText(Translation.Get("no"), font, color);
			button3.setText(Translation.Get("cancel"), font, color);
		}
		else if (button == MessageBoxButtons.Cancel.ordinal())
		{
			createButtons(this, 3);
			button1.setInvisible();
			button2.setInvisible();
			button3.setText(Translation.Get("cancel"), font, color);
		}
		else
		{
			// no Buttons
			this.setFooterHeight(calcFooterHeight(false));
		}
	}

	private static Sprite getIcon(MessageBoxIcon msgIcon)
	{

		Sprite icon;

		switch (msgIcon.ordinal())
		{
		case 0:
			icon = SpriteCache.Icons.get(32);
			break;
		case 1:
			icon = SpriteCache.Icons.get(31);
			break;
		case 2:
			icon = SpriteCache.Icons.get(33);
			break;
		case 3:
			icon = SpriteCache.Icons.get(31);
			break;
		case 4:
			icon = SpriteCache.Icons.get(32);
			break;
		case 5:
			icon = null;
			break;
		case 6:
			icon = SpriteCache.Icons.get(34);
			break;
		case 7:
			icon = SpriteCache.Icons.get(31);
			break;
		case 8:
			icon = SpriteCache.Icons.get(33);
			break;
		case 9:
			icon = SpriteCache.Icons.get(35);
			break;
		case 10:
			icon = SpriteCache.Icons.get(35);
			break;

		default:
			icon = null;

		}

		return icon;
	}

	protected void createButtons(GL_MsgBox msgBox, int anzahl)
	{
		setButtonListener();

		switch (anzahl)
		{
		case 1:
			button1 = new Button("positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			msgBox.initRow(false, margin);
			msgBox.addLast(button1);
			break;
		case 2:
			button1 = new Button("positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			button3 = new Button("negativeButton");
			button3.setOnClickListener(negativeButtonClickListener);
			msgBox.initRow(false, margin);
			msgBox.addNext(button1);
			msgBox.addLast(button3);
			break;
		case 3:
			button1 = new Button("positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			button2 = new Button("negativeButton");
			button2.setOnClickListener(neutralButtonClickListener);
			button3 = new Button("neutralButton");
			button3.setOnClickListener(negativeButtonClickListener);
			msgBox.initRow(false, margin);
			msgBox.addNext(button1);
			msgBox.addNext(button2);
			msgBox.addLast(button3);
			break;
		}

		if (rememberSetting != null)
		{
			chkRemember = new chkBox("remember");
			msgBox.setBorders(chkRemember.getHeight() / 2f, 0);
			msgBox.setMargins(chkRemember.getHeight() / 2f, 0);
			msgBox.addNext(chkRemember, chkRemember.getHeight() * 2f / msgBox.getWidth());
			Label lbl = new Label("lbl");
			msgBox.addLast(lbl);

			chkRemember.setChecked(rememberSetting.getValue());
			lbl.setText(Translation.Get("remember"));
		}

		msgBox.setFooterHeight(msgBox.getYPos() + margin);
	}

	private void setButtonListener()
	{
		positiveButtonClickListener = new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				return ButtonClick(1);
			}
		};

		neutralButtonClickListener = new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				return ButtonClick(2);
			}
		};

		negativeButtonClickListener = new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				return ButtonClick(3);
			}
		};
	}

	@Override
	protected void Initial()
	{
		super.Initial();
		synchronized (childs)
		{
			for (Iterator<CB_View_Base> iterator = FooterItems.iterator(); iterator.hasNext();)
			{
				childs.add(iterator.next());
			}
		}
	}

	public void setText(String text)
	{
		label.setWrappedText(text);
	}

	public void close()
	{
		GL.that.closeDialog(that);
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public void dispose()
	{
		Logger.LogCat("Dispose GL_MsgBox=> " + name);

		if (FooterItems != null)
		{
			for (CB_View_Base t : FooterItems)
			{
				t.dispose();
				t = null;
			}
			FooterItems = null;
		}

		button1 = null;
		button2 = null;
		button3 = null;
		mMsgBoxClickListner = null;
		positiveButtonClickListener = null;
		neutralButtonClickListener = null;
		negativeButtonClickListener = null;

		label = null;

		rememberSetting = null;
		chkRemember = null;

		super.dispose();
	}
}
