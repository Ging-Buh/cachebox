package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.ImageMultiToggleButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class RouteDialog extends ButtonDialog
{

	private Linearlayout layout;

	private float TextFieldHeight;
	private SizeF msgBoxContentSize;
	private ImageMultiToggleButton btMotoWay, btCycleWay, btFootWay;
	private chkBox chkTmc;

	public interface returnListner
	{
		public void returnFromRoute_Dialog(boolean canceld, boolean Motoway, boolean CycleWay, boolean FootWay, boolean UseTmc);
	}

	private returnListner mReturnListner;

	public RouteDialog(returnListner listner)
	{
		super(Menu.getMenuRec(), "PW-Dialog", "", Translation.Get("RouteToWaypoit"), MessageBoxButtons.OKCancel, null, null);
		mReturnListner = listner;

		msgBoxContentSize = getContentSize();
		// initial VariableField
		TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

		float innerWidth = msgBoxContentSize.width + this.getLeftWidth() + this.getRightWidth();

		layout = new Linearlayout(innerWidth, "Layout");
		layout.setX(0);
		// layout.setBackground(new ColorDrawable(Color.GREEN));

		CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UiSizes.getButtonHeight() * 2);

		btMotoWay = new ImageMultiToggleButton(MTBRec, "btMotoWay");
		btCycleWay = new ImageMultiToggleButton(MTBRec, "btCycleWay");
		btFootWay = new ImageMultiToggleButton(MTBRec, "btFootWay");

		btMotoWay.setImage(new SpriteDrawable(SpriteCache.getThemedSprite("pictureBox2")));
		btCycleWay.setImage(new SpriteDrawable(SpriteCache.getThemedSprite("pictureBox1")));
		btFootWay.setImage(new SpriteDrawable(SpriteCache.getThemedSprite("pictureBox3")));

		btMotoWay.setX(0);
		btCycleWay.setX(btMotoWay.getMaxX());
		btFootWay.setX(btCycleWay.getMaxX());

		Box box = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getButtonHeight() * 2), "");

		box.addChild(btMotoWay);
		box.addChild(btCycleWay);
		box.addChild(btFootWay);

		layout.addChild(box);

		MultiToggleButton.initialOn_Off_ToggleStates(btMotoWay, "", "");
		MultiToggleButton.initialOn_Off_ToggleStates(btCycleWay, "", "");
		MultiToggleButton.initialOn_Off_ToggleStates(btFootWay, "", "");

		Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getButtonHeight()), "");
		chkTmc = new chkBox("TMC");
		box2.addChild(chkTmc);

		Label lblPW = new Label(chkTmc.getMaxX() + margin, 0, innerWidth - chkTmc.getWidth() - margin, chkTmc.getHeight(), "");
		lblPW.setVAlignment(VAlignment.CENTER);
		lblPW.setText(Translation.Get("UseTmc"));
		box2.addChild(lblPW);

		layout.addChild(box2);

		this.addChild(layout);

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize("teste", true, true, false);
		msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (TextFieldHeight / 2));
		this.setSize(msgBoxSize.asFloat());

		mMsgBoxClickListner = new OnMsgBoxClickListener()
		{

			@Override
			public boolean onClick(int which, Object data)
			{
				if (which == BUTTON_POSITIVE)
				{

					if (mReturnListner != null) mReturnListner.returnFromRoute_Dialog(false, state == 0, state == 1, state == 2,
							chkTmc.isChecked());
				}
				else
				{
					if (mReturnListner != null) mReturnListner.returnFromRoute_Dialog(true, false, false, false, false);
				}

				return true;
			}
		};

		btMotoWay.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchVisibility(0);
				return true;
			}
		});

		btCycleWay.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchVisibility(1);
				return true;
			}
		});

		btFootWay.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchVisibility(2);
				return true;
			}
		});

		switchVisibility(0);

	}

	int state = -1;

	private void switchVisibility(int state)
	{
		this.state = state;

		if (state == 0)
		{
			btMotoWay.setState(1);
			btCycleWay.setState(0);
			btFootWay.setState(0);
		}
		if (state == 1)
		{
			btMotoWay.setState(0);
			btCycleWay.setState(1);
			btFootWay.setState(0);
		}
		if (state == 2)
		{
			btMotoWay.setState(0);
			btCycleWay.setState(0);
			btFootWay.setState(1);
		}

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}
