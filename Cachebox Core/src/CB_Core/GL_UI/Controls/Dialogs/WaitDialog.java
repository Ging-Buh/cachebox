package CB_Core.GL_UI.Controls.Dialogs;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

public class WaitDialog extends GL_MsgBox
{

	static Image iconImage;

	public WaitDialog(Size size, String name)
	{
		super(size, name);
	}

	public static WaitDialog ShowWait()
	{
		return onShow("");
	}

	public static WaitDialog ShowWait(String Msg)
	{
		return onShow(Msg);
	}

	private static WaitDialog onShow(String msg)
	{
		mMsgBoxClickListner = null;

		Size size = calcMsgBoxSize(msg, false, false, true);

		WaitDialog waitDialog = new WaitDialog(size, "WaitDialog");
		waitDialog.setTitle("");

		SizeF contentSize = waitDialog.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getButtonHeight(), UiSizes.getButtonHeight());

		iconImage = new Image(imageRec, "MsgBoxIcon");
		iconImage.setSprite(SpriteCache.Icons.get(51));
		iconImage.setOrigin(imageRec.getHalfWidth(), imageRec.getHalfHeight());
		waitDialog.addChild(iconImage);

		label = new Label(contentSize.getBounds(), "MsgBoxLabel");
		label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getButtonHeight());
		label.setX(imageRec.getMaxX() + margin);
		label.setWrappedText(msg);

		int lineCount = label.getLineCount();
		label.setY(0);

		if (lineCount == 1)
		{
			label.setText(msg);
			label.setVAlignment(VAlignment.CENTER);
		}
		else
		{
			label.setVAlignment(VAlignment.TOP);
		}

		float imageYPos = (contentSize.height < (iconImage.getHeight() * 1.7)) ? contentSize.halfHeight - iconImage.getHalfHeight()
				: contentSize.height - iconImage.getHeight() - margin;
		iconImage.setY(imageYPos);

		waitDialog.addChild(label);
		setButtonCaptions(waitDialog, MessageBoxButtons.NOTHING);
		GL_Listener.glListener.showDialog(waitDialog);

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
				GL_Listener.glListener.renderOnce("WaitRotateAni");
			}
		}
	};

	public void dismis()
	{
		RotateTimer.cancel();
		iconImage.dispose();

		GL_Listener.glListener.closeDialog();
	}

}
