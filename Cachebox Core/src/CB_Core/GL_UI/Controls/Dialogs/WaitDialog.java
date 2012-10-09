package CB_Core.GL_UI.Controls.Dialogs;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class WaitDialog extends ButtonDialog
{

	static Image iconImage;
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

		CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getButtonHeight(), UiSizes.getButtonHeight());

		iconImage = new Image(imageRec, "MsgBoxIcon");
		iconImage.setDrawable(new SpriteDrawable(SpriteCache.Icons.get(51)));
		iconImage.setOrigin(imageRec.getHalfWidth(), imageRec.getHalfHeight());
		waitDialog.addChild(iconImage);

		waitDialog.label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		waitDialog.label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getButtonHeight());
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

		float imageYPos = (contentSize.height < (iconImage.getHeight() * 1.7)) ? contentSize.halfHeight - iconImage.getHalfHeight()
				: contentSize.height - iconImage.getHeight() - margin;
		iconImage.setY(imageYPos);

		waitDialog.addChild(waitDialog.label);
		waitDialog.setButtonCaptions(MessageBoxButtons.NOTHING);
		// Dialog will be shown later automatically...
		// GL.that.showDialog(waitDialog);

		waitDialog.rotateAngle = 0;

		waitDialog.RotateTimer = new Timer();

		waitDialog.RotateTimer.schedule(waitDialog.rotateTimertask, 60, 60);

		return (WaitDialog) waitDialog;

	}

	boolean canceld = false;

	Timer RotateTimer;
	float rotateAngle = 0;
	TimerTask rotateTimertask = new TimerTask()
	{
		@Override
		public void run()
		{
			if (iconImage != null)
			{
				rotateAngle += 5;
				if (rotateAngle > 360) rotateAngle = 0;
				iconImage.setRotate(rotateAngle);
				GL.that.renderOnce("WaitRotateAni");
			}
		}
	};

	public void dismis()
	{
		RotateTimer.cancel();
		iconImage.dispose();

		GL.that.closeDialog(that);
	}

}
