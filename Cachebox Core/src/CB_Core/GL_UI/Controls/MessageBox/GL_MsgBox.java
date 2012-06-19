package CB_Core.GL_UI.Controls.MessageBox;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class GL_MsgBox extends Dialog
{

	private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

	protected static Button button1;
	protected static Button button2;
	protected static Button button3;

	protected static OnMsgBoxClickListener mMsgBoxClickListner;

	protected static OnClickListener positiveButtonClickListener;

	protected static OnClickListener neutralButtonClickListener;
	protected static OnClickListener negativeButtonClickListener;

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

	private static boolean ButtonClick(int button)
	{
		GL_Listener.glListener.closeDialog();
		if (mMsgBoxClickListner != null) return mMsgBoxClickListner.onClick(button);
		return false;
	}

	public GL_MsgBox(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mFooterHeight = 80;
	}

	protected static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon)
	{
		float Width = (((UiSizes.getButtonWidthWide() + margin) * 3) + margin);

		if (Width * 1.2 < UiSizes.getWindowWidth()) Width *= 1.2f;

		float iconWidth = 0;

		if (hasIcon) iconWidth += UiSizes.getButtonHeight() + margin * 4.5;

		float MsgWidth = Width - iconWidth;

		TextBounds bounds = Fonts.MesureWrapped(Text, MsgWidth);
		int Height = (int) (hasIcon ? Math.max(bounds.height, (int) UiSizes.getButtonHeight()) : (int) bounds.height);

		if (hasTitle) Height += 40;
		Height += calcFooterHeight(hasButtons);
		Height += calcHeaderHeight();

		Height += margin * 2;

		Size ret = new Size((int) Width, Height);
		return ret;
	}

	public GL_MsgBox(Size size, String name)
	{
		super(size.getBounds().asFloat(), name);

	}

	public void addFooterChild(CB_View_Base view)
	{
		FooterItems.add(view);
	}

	public static void Show(String msg)
	{
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, false, true, false), "MsgBox");
		Label label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(msg);
		msgBox.addChild(label);
		setButtonCaptions(msgBox, MessageBoxButtons.OK);
		GL_Listener.glListener.showDialog(msgBox);

	}

	public static void Show(String msg, OnMsgBoxClickListener Listener)
	{
		mMsgBoxClickListner = Listener;
		Show(msg);
	}

	public static void Show(String msg, String title, OnMsgBoxClickListener Listener)
	{
		Show(msg, title, MessageBoxButtons.OK, Listener);
	}

	public static void Show(String msg, String title, MessageBoxButtons buttons, OnMsgBoxClickListener Listener)
	{
		mMsgBoxClickListner = Listener;
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), false), "MsgBox");
		msgBox.setTitle(title);
		label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(msg);
		msgBox.addChild(label);
		setButtonCaptions(msgBox, buttons);
		GL_Listener.glListener.showDialog(msgBox);
	}

	public static void Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener)
	{
		mMsgBoxClickListner = Listener;
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), true), "MsgBox");
		msgBox.setTitle(title);

		SizeF contentSize = msgBox.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, contentSize.halfHeight, UiSizes.getButtonHeight(), UiSizes.getButtonHeight());

		Image iconImage = new Image(imageRec, "MsgBoxIcon");
		iconImage.setSprite(getIcon(icon));
		msgBox.addChild(iconImage);

		label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		label.setWidth(contentSize.getBounds().getWidth() - 5 - UiSizes.getButtonHeight());
		label.setX(imageRec.getMaxX() + 5);
		label.setY(0);
		label.setWrappedText(msg);
		msgBox.addChild(label);
		setButtonCaptions(msgBox, buttons);
		GL_Listener.glListener.showDialog(msgBox);
	}

	public static void Show(String msg, String title, MessageBoxIcon icon)
	{
		Show(msg, title, MessageBoxButtons.OK, icon, null);

	}

	protected static void setButtonCaptions(GL_MsgBox msgBox, MessageBoxButtons buttons)
	{
		int button = buttons.ordinal();
		if (button == 0)
		{
			createButtons(msgBox, 3);
			button1.setText(GlobalCore.Translations.Get("abort"));
			button2.setText(GlobalCore.Translations.Get("retry"));
			button3.setText(GlobalCore.Translations.Get("ignore"));
		}
		else if (button == 1)
		{
			createButtons(msgBox, 1);
			button1.setText(GlobalCore.Translations.Get("ok"));
		}
		else if (button == 2)
		{
			createButtons(msgBox, 2);
			button1.setText(GlobalCore.Translations.Get("ok"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 3)
		{
			createButtons(msgBox, 2);
			button1.setText(GlobalCore.Translations.Get("retry"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 4)
		{
			createButtons(msgBox, 2);
			button1.setText(GlobalCore.Translations.Get("yes"));
			button3.setText(GlobalCore.Translations.Get("no"));
		}
		else if (button == 5)
		{
			createButtons(msgBox, 3);
			button1.setText(GlobalCore.Translations.Get("yes"));
			button2.setText(GlobalCore.Translations.Get("no"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else
		{
			// no Buttons
			msgBox.mFooterHeight = calcFooterHeight(false);
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

	private static void createButtons(GL_MsgBox msgBox, int anzahl)
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

		msgBox.mFooterHeight = calcFooterHeight(true);
	}

	private static void setButtonListner()
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
		for (Iterator<CB_View_Base> iterator = FooterItems.iterator(); iterator.hasNext();)
		{
			childs.add(iterator.next());
		}
	}

	protected static Label label;

	public String getText()
	{
		return label.text;
	}

	public void setText(String text)
	{
		label.setWrappedText(text);

	}

	public void close()
	{
		GL_Listener.glListener.closeDialog();
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}
}
