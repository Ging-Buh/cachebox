package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.Animation.RotateAnimation;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WaitDialog extends ButtonDialog
{
	public static final int WAIT_DURATION = 2000;
	RotateAnimation iconImage;
	WaitDialog that;

	public WaitDialog(Size size, String name)
	{
		super(size.getBounds().asFloat(), name, "", "", null, null, null);
		that = this;
	}

	public static WaitDialog ShowWait()
	{
		WaitDialog wd = createDialog("");
		wd.Show();
		return wd;
	}

	public static WaitDialog ShowWait(String Msg)
	{
		WaitDialog wd = createDialog(Msg);
		wd.Show();
		return wd;
	}

	protected static WaitDialog createDialog(String msg)
	{

		Size size = calcMsgBoxSize(msg, false, false, true);

		WaitDialog waitDialog = new WaitDialog(size, "WaitDialog");
		waitDialog.setTitle("");

		SizeF contentSize = waitDialog.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

		waitDialog.iconImage = new RotateAnimation(imageRec, "MsgBoxIcon");
		waitDialog.iconImage.setSprite(SpriteCache.Icons.get(26));
		waitDialog.iconImage.setOrigin(waitDialog.halfWidth, waitDialog.halfHeight);
		waitDialog.iconImage.play(WAIT_DURATION);
		waitDialog.iconImage.setOrigin(imageRec.getHalfWidth(), imageRec.getHalfHeight());
		waitDialog.addChild(waitDialog.iconImage);

		waitDialog.label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		waitDialog.label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UI_Size_Base.that.getButtonHeight());
		waitDialog.label.setX(imageRec.getMaxX() + margin);
		waitDialog.label.setWrappedText(msg);

		int lineCount = waitDialog.label.getLineCount();
		waitDialog.label.setY(0);

		if (lineCount == 1)
		{
			waitDialog.label.setText(msg);
			waitDialog.label.setVAlignment(VAlignment.CENTER);
		}
		else
		{
			waitDialog.label.setVAlignment(VAlignment.TOP);
		}

		float imageYPos = (contentSize.height < (waitDialog.iconImage.getHeight() * 1.7)) ? contentSize.halfHeight
				- waitDialog.iconImage.getHalfHeight() : contentSize.height - waitDialog.iconImage.getHeight() - margin;
		waitDialog.iconImage.setY(imageYPos);

		waitDialog.addChild(waitDialog.label);
		waitDialog.setButtonCaptions(MessageBoxButtons.NOTHING);

		return (WaitDialog) waitDialog;

	}

	boolean canceld = false;

	public void dismis()
	{
		Logger.LogCat("WaitDialog.Dismis");
		GL.that.RunOnGL(new runOnGL()
		{
			@Override
			public void run()
			{
				GL.that.closeDialog(that);
				GL.that.renderOnce("dismis WaitDialog");
			}
		});
	}

	@Override
	public void dispose()
	{
		this.removeChild(iconImage);

		super.dispose();
		Logger.LogCat("WaitDialog.disposed");
	}

	@Override
	public void render(SpriteBatch batch)
	{
		super.render(batch);
	}

}
