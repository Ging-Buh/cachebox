package CB_Core.GL_UI.Controls.MessageBox;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
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
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Settings.SettingBool;

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

	protected SettingBool rememberSeting = null;
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

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, OnMsgBoxClickListener Listener)
	{
		return Show(msg, title, buttons, Listener, null);
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener)
	{
		return Show(msg, title, buttons, icon, Listener, null);
	}

	// ++++++++++++++++++++++

	public static GL_MsgBox Show(String msg, OnMsgBoxClickListener Listener, SettingBool remember)
	{
		return Show(msg, "", Listener, remember);
	}

	public static GL_MsgBox Show(String msg, String title, OnMsgBoxClickListener Listener, SettingBool remember)
	{
		return Show(msg, title, MessageBoxButtons.OK, Listener, remember);
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxIcon icon, SettingBool remember)
	{
		return Show(msg, title, MessageBoxButtons.OK, icon, null, remember);
	}

	public static GL_MsgBox Show(String msg)
	{
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, false, true, false), "MsgBox");
		msgBox.setButtonCaptions(MessageBoxButtons.OK);
		label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(msg);
		msgBox.addChild(label);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, OnMsgBoxClickListener Listener, SettingBool remember)
	{

		if (remember.getValue())
		{
			// wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
			// Wir liefern nur ein On Click auf den OK Button zurück!
			if (Listener != null)
			{
				Listener.onClick(BUTTON_POSITIVE);
			}
			return null;
		}

		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), false, (remember != null)),
				"MsgBox");
		msgBox.rememberSeting = remember;
		msgBox.mMsgBoxClickListner = Listener;
		msgBox.setButtonCaptions(buttons);
		msgBox.setTitle(title);
		label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(msg);
		msgBox.addChild(label);

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
				Listener.onClick(BUTTON_POSITIVE);
			}
			return null;
		}

		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), true, (remember != null)),
				"MsgBox");
		msgBox.rememberSeting = remember;
		msgBox.mMsgBoxClickListner = Listener;
		msgBox.setTitle(title);

		msgBox.setButtonCaptions(buttons);

		SizeF contentSize = msgBox.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight());

		Image iconImage = new Image(imageRec, "MsgBoxIcon");
		if (icon != MessageBoxIcon.None) iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
		msgBox.addChild(iconImage);

		label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		label.setWidth(contentSize.getBounds().getWidth() - 5 - UiSizes.getButtonHeight());
		label.setX(imageRec.getMaxX() + 5);
		label.setY(0);
		label.setWrappedText(msg);
		msgBox.addChild(label);

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
		public boolean onClick(int which);
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
		GL.that.closeDialog(that);

		// wenn Dies eine Remember MsgBox ist, überprüfen wir ob das remember gesetzt ist
		if (rememberSeting != null)
		{
			if (chkRemember.isChecked())
			{
				// User hat Remember aktiviert, was hier abgespeichert wird!
				rememberSeting.setValue(true);
				Config.AcceptChanges();
			}
		}

		if (mMsgBoxClickListner != null) return mMsgBoxClickListner.onClick(button);
		return false;
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

	public void addFooterChild(CB_View_Base view)
	{
		FooterItems.add(view);

		float maxItemY = 0;

		for (CB_View_Base item : FooterItems)
		{
			if (item.getMaxY() > maxItemY) maxItemY = item.getMaxY();
		}
		mFooterHeight = maxItemY + margin;
	}

	public void setButtonCaptions(MessageBoxButtons buttons)
	{
		int button = buttons.ordinal();
		if (button == 0)
		{
			createButtons(this, 3);
			button1.setText(GlobalCore.Translations.Get("abort"));
			button2.setText(GlobalCore.Translations.Get("retry"));
			button3.setText(GlobalCore.Translations.Get("ignore"));
		}
		else if (button == 1)
		{
			createButtons(this, 1);
			button1.setText(GlobalCore.Translations.Get("ok"));
		}
		else if (button == 2)
		{
			createButtons(this, 2);
			button1.setText(GlobalCore.Translations.Get("ok"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 3)
		{
			createButtons(this, 2);
			button1.setText(GlobalCore.Translations.Get("retry"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 4)
		{
			createButtons(this, 2);
			button1.setText(GlobalCore.Translations.Get("yes"));
			button3.setText(GlobalCore.Translations.Get("no"));
		}
		else if (button == 5)
		{
			createButtons(this, 3);
			button1.setText(GlobalCore.Translations.Get("yes"));
			button2.setText(GlobalCore.Translations.Get("no"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 6)
		{
			createButtons(this, 3);
			button1.setInvisible();
			button2.setInvisible();
			button3.setText(GlobalCore.Translations.Get("cancel"));
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
		setButtonListner();

		float buttonY = 7.5f;

		float buttonX_R = msgBox.width - UiSizes.getButtonWidthWide() - margin;
		float buttonX_L = margin;
		float buttonX_C = (msgBox.width - UiSizes.getButtonWidthWide()) / 2;

		switch (anzahl)
		{
		case 1:
			button1 = new Button(new CB_RectF(buttonX_C, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			msgBox.addFooterChild(button1);
			break;
		case 2:
			button1 = new Button(new CB_RectF(buttonX_C, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			msgBox.addFooterChild(button1);
			button3 = new Button(new CB_RectF(buttonX_R, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"negativeButton");
			button3.setOnClickListener(negativeButtonClickListener);
			msgBox.addFooterChild(button3);
			break;
		case 3:
			button1 = new Button(new CB_RectF(buttonX_L, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			msgBox.addFooterChild(button1);
			button2 = new Button(new CB_RectF(buttonX_C, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"negativeButton");
			button2.setOnClickListener(neutralButtonClickListener);
			msgBox.addFooterChild(button2);
			button3 = new Button(new CB_RectF(buttonX_R, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()), "neutralButton");
			button3.setOnClickListener(negativeButtonClickListener);
			msgBox.addFooterChild(button3);
			break;

		}

		float calcedFooterHeight = calcFooterHeight(true);

		// add Remember line?

		if (rememberSeting != null)
		{
			chkRemember = new chkBox("remember");
			chkRemember.setChecked(rememberSeting.getValue());
			chkRemember.setPos(buttonX_L, button1.getMaxY());
			msgBox.addFooterChild(chkRemember);

			Label lbl = new Label(chkRemember.getMaxX() + margin, chkRemember.getY(), this.width - chkRemember.getMaxX(),
					chkRemember.getHeight(), "");

			lbl.setText(GlobalCore.Translations.Get("remember"));

			msgBox.addFooterChild(lbl);

			calcedFooterHeight += chkRemember.getHeight() + margin;
		}

		msgBox.setFooterHeight(calcedFooterHeight);
	}

	private void setButtonListner()
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

	protected static Label label;

	public String getText()
	{
		return label.text;
	}

	public TextBounds setText(String text)
	{
		return label.setWrappedText(text);
	}

	public void close()
	{
		GL.that.closeDialog(that);
	}

	@Override
	protected void SkinIsChanged()
	{
	}
}
