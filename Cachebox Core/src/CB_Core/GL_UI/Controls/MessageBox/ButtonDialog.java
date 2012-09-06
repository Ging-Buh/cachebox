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
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class ButtonDialog extends Dialog
{

	public final int BUTTON_POSITIVE = 1;
	public final int BUTTON_NEUTRAL = 2;
	public final int BUTTON_NEGATIVE = 3;

	private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

	protected Button button1;
	protected Button button2;
	protected Button button3;
	protected Label label;

	protected OnMsgBoxClickListener mMsgBoxClickListner;

	protected OnClickListener positiveButtonClickListener;

	protected OnClickListener neutralButtonClickListener;
	protected OnClickListener negativeButtonClickListener;

	public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon,
			OnMsgBoxClickListener Listener)
	{
		this(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None)).getBounds().asFloat(), Name,
				msg, title, buttons, icon, Listener);
	}

	public ButtonDialog(CB_RectF rec, String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon,
			OnMsgBoxClickListener Listener)
	{
		super(rec, Name);
		setTitle(title);
		setButtonCaptions(buttons);
		SizeF contentSize = getContentSize();

		CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UiSizes.getButtonHeight(), UiSizes.getButtonHeight(),
				UiSizes.getButtonHeight());

		if (icon != MessageBoxIcon.None)
		{
			Image iconImage = new Image(imageRec, "MsgBoxIcon");
			iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
			addChild(iconImage);
		}

		label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		label.setWidth(contentSize.getBounds().getWidth() - 5 - UiSizes.getButtonHeight());
		label.setX(imageRec.getMaxX() + 5);
		label.setY(-margin);
		label.setWrappedText(msg);
		addChild(label);
		mMsgBoxClickListner = Listener;

		mFooterHeight = 80;
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
		GL.that.closeDialog(this);
	}

	protected void setButtonCaptions(MessageBoxButtons buttons)
	{
		int button = buttons.ordinal();
		if (button == 0)
		{
			createButtons(3);
			button1.setText(GlobalCore.Translations.Get("abort"));
			button2.setText(GlobalCore.Translations.Get("retry"));
			button3.setText(GlobalCore.Translations.Get("ignore"));
		}
		else if (button == 1)
		{
			createButtons(1);
			button1.setText(GlobalCore.Translations.Get("ok"));
		}
		else if (button == 2)
		{
			createButtons(2);
			button1.setText(GlobalCore.Translations.Get("ok"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 3)
		{
			createButtons(2);
			button1.setText(GlobalCore.Translations.Get("retry"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 4)
		{
			createButtons(2);
			button1.setText(GlobalCore.Translations.Get("yes"));
			button3.setText(GlobalCore.Translations.Get("no"));
		}
		else if (button == 5)
		{
			createButtons(3);
			button1.setText(GlobalCore.Translations.Get("yes"));
			button2.setText(GlobalCore.Translations.Get("no"));
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else if (button == 6)
		{
			createButtons(3);
			button1.setVisibility(CB_View_Base.INVISIBLE);
			button2.setVisibility(CB_View_Base.INVISIBLE);
			button3.setText(GlobalCore.Translations.Get("cancel"));
		}
		else
		{
			// no Buttons
			mFooterHeight = calcFooterHeight(false);
		}
	}

	protected void createButtons(int anzahl)
	{
		setButtonListner();

		float buttonY = 7.5f;

		float buttonX_R = width - UiSizes.getButtonWidthWide() - margin;
		float buttonX_L = margin;
		float buttonX_C = (width - UiSizes.getButtonWidthWide()) / 2;

		switch (anzahl)
		{
		case 1:
			button1 = new Button(new CB_RectF(buttonX_C, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			addFooterChild(button1);
			break;
		case 2:
			button1 = new Button(new CB_RectF(buttonX_C, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			addFooterChild(button1);
			button3 = new Button(new CB_RectF(buttonX_R, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"negativeButton");
			button3.setOnClickListener(negativeButtonClickListener);
			addFooterChild(button3);
			break;
		case 3:
			button1 = new Button(new CB_RectF(buttonX_L, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			addFooterChild(button1);
			button2 = new Button(new CB_RectF(buttonX_C, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()),
					"negativeButton");
			button2.setOnClickListener(neutralButtonClickListener);
			addFooterChild(button2);
			button3 = new Button(new CB_RectF(buttonX_R, buttonY, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight()), "neutralButton");
			button3.setOnClickListener(negativeButtonClickListener);
			addFooterChild(button3);
			break;

		}

		mFooterHeight = calcFooterHeight(true);
	}

	public void addFooterChild(CB_View_Base view)
	{
		FooterItems.add(view);
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

	private boolean ButtonClick(int button)
	{
		GL.that.closeDialog(this);
		if (mMsgBoxClickListner != null) return mMsgBoxClickListner.onClick(button);
		return false;
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	public static ButtonDialog Show(String msg)
	{
		ButtonDialog msgBox = new ButtonDialog("MsgBox", msg, "Title", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);
		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public void Show()
	{
		GL.that.showDialog(this);
	}

	private Sprite getIcon(MessageBoxIcon msgIcon)
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

	protected static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon)
	{
		float Width = (((UiSizes.getButtonWidthWide() + margin) * 3) + margin);

		if (Width * 1.2 < UiSizes.getWindowWidth()) Width *= 1.2f;

		float MsgWidth = (Width * 0.95f) - 5 - UiSizes.getButtonHeight();

		TextBounds bounds = Fonts.MesureWrapped(Text, MsgWidth);
		float mesuredTextHeight = bounds.height + (margin * 2);

		int Height = (int) (hasIcon ? Math.max(mesuredTextHeight, (int) UiSizes.getButtonHeight()) : (int) mesuredTextHeight);

		if (hasTitle)
		{
			TextBounds titleBounds = Fonts.Mesure("T");
			Height += (titleBounds.height * 3);
			Height += margin * 2;
		}
		Height += calcFooterHeight(hasButtons);
		Height += calcHeaderHeight();

		Height = (int) Math.max(Height, UiSizes.getButtonHeight() * 2.5f);

		Size ret = new Size((int) Width, Height);
		return ret;
	}

}
