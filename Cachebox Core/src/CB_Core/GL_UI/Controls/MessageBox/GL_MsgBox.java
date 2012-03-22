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
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class GL_MsgBox extends Dialog
{

	private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

	protected static Button button1;
	protected static Button button2;
	protected static Button button3;

	static float margin = 5f;

	protected static MsgBox.OnClickListener mMsgBoxClickListner;

	protected static OnClickListener positiveButtonClickListener;

	protected static OnClickListener neutralButtonClickListener;
	protected static OnClickListener negativeButtonClickListener;

	private static boolean ButtonClick(int button)
	{
		GL_Listener.glListener.closeDialog();
		if (mMsgBoxClickListner != null) return mMsgBoxClickListner.onClick(button);
		return false;
	}

	public GL_MsgBox(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		mFooterHeight = 80;
	}

	protected static Size calcMsgBoxSize(String Text, boolean hasTitle)
	{
		float Width = (((UiSizes.getButtonWidthWide() + margin) * 3) + margin);

		BitmapFontCache mesure = new BitmapFontCache(Fonts.get18());
		TextBounds bounds = mesure.setWrappedText(Text, 0, 0, Width - 20);
		int Height = (int) bounds.height + 150;// + footer and header height + x(=100)

		if (hasTitle) Height += 40;

		Size ret = new Size((int) Width, Height);
		return ret;
	}

	public GL_MsgBox(Size size, String name)
	{
		super(size.getBounds().asFloat(), name);
		mFooterHeight = 80;
	}

	public void addFooterChild(CB_View_Base view)
	{
		FooterItems.add(view);
	}

	public static void Show(String msg)
	{
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, false), "MsgBox");
		Label label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(msg);
		msgBox.addChild(label);
		setButtonCaptions(msgBox, MessageBoxButtons.OK);
		GL_Listener.glListener.showDialog(msgBox);

	}

	public static void Show(String msg, MsgBox.OnClickListener Listener)
	{
		mMsgBoxClickListner = Listener;
		Show(msg);
	}

	public static void Show(String msg, String title, MsgBox.OnClickListener Listener)
	{
		Show(msg, title, MessageBoxButtons.OK, Listener);
	}

	public static void Show(String msg, String title, MessageBoxButtons buttons, MsgBox.OnClickListener Listener)
	{
		mMsgBoxClickListner = Listener;
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true), "MsgBox");
		msgBox.setTitle(title);
		label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(msg);
		msgBox.addChild(label);
		setButtonCaptions(msgBox, buttons);
		GL_Listener.glListener.showDialog(msgBox);
	}

	public static void Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, MsgBox.OnClickListener Listener)
	{
		mMsgBoxClickListner = Listener;
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true), "MsgBox");
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
	}

	private static Sprite getIcon(MessageBoxIcon msgIcon)
	{

		Sprite icon;

		switch (msgIcon.ordinal())
		{
		case 0:
			icon = SpriteCache.BtnIcons.get(32);
			break;
		case 1:
			icon = SpriteCache.BtnIcons.get(31);
			break;
		case 2:
			icon = SpriteCache.BtnIcons.get(33);
			break;
		case 3:
			icon = SpriteCache.BtnIcons.get(31);
			break;
		case 4:
			icon = SpriteCache.BtnIcons.get(32);
			break;
		case 5:
			icon = null;
			break;
		case 6:
			icon = SpriteCache.BtnIcons.get(34);
			break;
		case 7:
			icon = SpriteCache.BtnIcons.get(31);
			break;
		case 8:
			icon = SpriteCache.BtnIcons.get(33);
			break;
		case 9:
			icon = SpriteCache.BtnIcons.get(35);
			break;
		case 10:
			icon = SpriteCache.BtnIcons.get(35);
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
}
