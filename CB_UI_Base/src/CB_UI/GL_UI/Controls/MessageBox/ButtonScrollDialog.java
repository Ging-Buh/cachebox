package CB_UI.GL_UI.Controls.MessageBox;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.Fonts;
import CB_UI.GL_UI.GL_View_Base;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Controls.Button;
import CB_UI.GL_UI.Controls.Dialog;
import CB_UI.GL_UI.Controls.Image;
import CB_UI.GL_UI.Controls.Label;
import CB_UI.GL_UI.Controls.ScrollBox;
import CB_UI.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI.GL_UI.GL_Listener.GL;
import CB_UI.GL_UI.SpriteCacheBase.IconName;
import CB_UI.Math.CB_RectF;
import CB_UI.Math.Size;
import CB_UI.Math.SizeF;
import CB_UI.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class ButtonScrollDialog extends Dialog
{

	public final int BUTTON_POSITIVE = 1;
	public final int BUTTON_NEUTRAL = 2;
	public final int BUTTON_NEGATIVE = 3;

	private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

	protected Button button1;
	protected Button button2;
	protected Button button3;
	protected Label label;
	protected ScrollBox scrollBox;

	protected OnMsgBoxClickListener mMsgBoxClickListner;

	protected OnClickListener positiveButtonClickListener;

	protected OnClickListener neutralButtonClickListener;
	protected OnClickListener negativeButtonClickListener;

	public ButtonScrollDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon,
			OnMsgBoxClickListener Listener)
	{
		this(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None)).getBounds().asFloat(), Name,
				msg, title, buttons, icon, Listener);
	}

	public ButtonScrollDialog(CB_RectF rec, String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon,
			OnMsgBoxClickListener Listener)
	{
		super(rec, Name);
		setTitle(title);
		setButtonCaptions(buttons);
		SizeF contentSize = getContentSize();

		rec = new CB_RectF(0, 0, contentSize.width - leftBorder - rightBorder, contentSize.height);
		// initial ScrollBox mit einer Inneren Höhe des halben rec´s.
		// Die Innere Höhe muss angepasst werden, wenn sich die Höhe des LinearLayouts verändert hat.
		// Entweder wenn ein Control hinzugefügt wurde oder wenn eine CollapseBox geöffnrt oder geschlossen wird!
		scrollBox = new ScrollBox(rec);
		// die ScrollBox erhält den Selben Hintergrund wie die Activity und wird damit ein wenig abgegrenzt von den Restlichen Controls
		// scrollBox.setBackground(this.getBackground());
		scrollBox.setMargins(margin, margin);
		scrollBox.initRow();

		// damit die Scrollbox auch Events erhällt
		scrollBox.setClickable(true);

		CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UI_Size_Base.that.getButtonHeight(),
				UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

		if (icon != MessageBoxIcon.None && icon != null)
		{
			Image iconImage = new Image(imageRec, "MsgBoxIcon");
			iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
			scrollBox.addChild(iconImage);
		}

		label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		label.setWidth(contentSize.getBounds().getWidth() - 5 - UI_Size_Base.that.getButtonHeight());
		label.setX(imageRec.getMaxX() + 5);
		label.setY(-margin);
		label.setWrappedText(msg);
		scrollBox.addChild(label);
		mMsgBoxClickListner = Listener;
		this.addChild(scrollBox);
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
		GL.that.closeDialog(this);
	}

	public void setButtonCaptions(MessageBoxButtons buttons)
	{
		if (buttons == null) buttons = MessageBoxButtons.NOTHING;

		if (buttons == MessageBoxButtons.AbortRetryIgnore)
		{
			createButtons(3, Translation.Get("abort"), Translation.Get("retry"), Translation.Get("ignore"));
		}
		else if (buttons == MessageBoxButtons.OK)
		{
			createButtons(1, Translation.Get("ok"), "", "");
		}
		else if (buttons == MessageBoxButtons.OKCancel)
		{
			createButtons(2, Translation.Get("ok"), "", Translation.Get("cancel"));
		}
		else if (buttons == MessageBoxButtons.RetryCancel)
		{
			createButtons(2, Translation.Get("retry"), "", Translation.Get("cancel"));
		}
		else if (buttons == MessageBoxButtons.YesNo)
		{
			createButtons(2, Translation.Get("yes"), "", Translation.Get("no"));
		}
		else if (buttons == MessageBoxButtons.YesNoCancel)
		{
			createButtons(3, Translation.Get("yes"), Translation.Get("no"), Translation.Get("cancel"));
		}
		else if (buttons == MessageBoxButtons.Cancel)
		{
			createButtons(3, "", "", Translation.Get("cancel"));
			button1.setInvisible();
			button2.setInvisible();
		}
		else
		{
			// no Buttons
			setFooterHeight(calcFooterHeight(false));
		}
	}

	protected void createButtons(int anzahl, String t1, String t2, String t3)
	{
		setButtonListner();

		this.setBorders(margin, margin);
		this.setMargins(margin, margin);
		this.initRow(BOTTOMUP, margin);

		switch (anzahl)
		{
		case 1:
			button1 = new Button(t1);
			this.addLast(button1);
			button1.setOnClickListener(positiveButtonClickListener);
			// addFooterChild(button1);
			break;
		case 2:
			button1 = new Button(t1);
			button3 = new Button(t3);
			this.addNext(button1);
			this.addLast(button3);
			button1.setOnClickListener(positiveButtonClickListener);
			button3.setOnClickListener(negativeButtonClickListener);
			// addFooterChild(button1);
			// addFooterChild(button3);
			break;
		case 3:
			button1 = new Button(t1);
			button2 = new Button(t2);
			button3 = new Button(t3);
			this.addNext(button1);
			this.addNext(button2);
			this.addLast(button3);
			button1.setOnClickListener(positiveButtonClickListener);
			button2.setOnClickListener(neutralButtonClickListener);
			button3.setOnClickListener(negativeButtonClickListener);
			// addFooterChild(button1);
			// addFooterChild(button2);
			// addFooterChild(button3);
			break;
		}
		setFooterHeight(this.getHeightFromBottom());
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
		if (mMsgBoxClickListner != null) return mMsgBoxClickListner.onClick(button, data);
		return false;
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	public static ButtonScrollDialog Show(String msg)
	{
		ButtonScrollDialog msgBox = new ButtonScrollDialog("MsgBox", msg, "Title", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);
		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public void Show()
	{
		GL.that.showDialog(this);
	}

	private Sprite getIcon(MessageBoxIcon msgIcon)
	{
		if (msgIcon == null) return null;

		Sprite icon;

		switch (msgIcon.ordinal())
		{
		case 0:
			icon = SpriteCacheBase.Icons.get(IconName.info_32.ordinal());
			break;
		case 1:
			icon = SpriteCacheBase.Icons.get(IconName.close_31.ordinal());
			break;
		case 2:
			icon = SpriteCacheBase.Icons.get(IconName.warning_33.ordinal());
			break;
		case 3:
			icon = SpriteCacheBase.Icons.get(IconName.close_31.ordinal());
			break;
		case 4:
			icon = SpriteCacheBase.Icons.get(IconName.info_32.ordinal());
			break;
		case 5:
			icon = null;
			break;
		case 6:
			icon = SpriteCacheBase.Icons.get(IconName.help_34.ordinal());
			break;
		case 7:
			icon = SpriteCacheBase.Icons.get(IconName.close_31.ordinal());
			break;
		case 8:
			icon = SpriteCacheBase.Icons.get(IconName.warning_33.ordinal());
			break;
		case 9:
			icon = SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal());
			break;
		case 10:
			icon = SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal());
			break;
		default:
			icon = null;

		}

		return icon;
	}

	protected static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon)
	{
		float Width = (((UI_Size_Base.that.getButtonWidthWide() + margin) * 3) + margin);

		if (Width * 1.2 < UI_Size_Base.that.getWindowWidth()) Width *= 1.2f;

		float MsgWidth = (Width * 0.95f) - 5 - UI_Size_Base.that.getButtonHeight();

		TextBounds bounds = Fonts.MeasureWrapped(Text, MsgWidth);
		float MeasuredTextHeight = bounds.height + (margin * 2);

		int Height = (int) (hasIcon ? Math.max(MeasuredTextHeight, (int) UI_Size_Base.that.getButtonHeight()) : (int) MeasuredTextHeight);

		if (hasTitle)
		{
			TextBounds titleBounds = Fonts.Measure("T");
			Height += (titleBounds.height * 3);
			Height += margin * 2;
		}
		Height += calcFooterHeight(hasButtons);
		Height += calcHeaderHeight();

		Height = (int) Math.max(Height, UI_Size_Base.that.getButtonHeight() * 2.5f);

		Size ret = new Size((int) Width, Height);
		return ret;
	}

}
