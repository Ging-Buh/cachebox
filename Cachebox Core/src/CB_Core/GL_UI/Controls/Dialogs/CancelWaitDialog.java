package CB_Core.GL_UI.Controls.Dialogs;

import java.util.Timer;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.interfaces.RunnableReadyHandler;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Ein Wait Dialog mit übergabe eines Runable zur Abarbeitung, welcher abgebrochen werden kann
 * 
 * @author Longri
 */
public class CancelWaitDialog extends WaitDialog
{

	CancelWaitDialog that;

	public interface IcancelListner
	{
		public void isCanceld();
	}

	private IcancelListner cancelListner;
	private Runnable runnable;

	public CancelWaitDialog(Size size, String name, IcancelListner listner, Runnable runnable)
	{
		super(size, name);
		this.cancelListner = listner;
		this.runnable = runnable;
		that = this;
	}

	public static CancelWaitDialog ShowWait(String Msg, IcancelListner listner, Runnable runnable)
	{
		final CancelWaitDialog wd = createDialog(Msg, listner, runnable);

		wd.setButtonCaptions(MessageBoxButtons.Cancel);
		wd.mMsgBoxClickListner = new GL_MsgBox.OnMsgBoxClickListener()
		{

			@Override
			public boolean onClick(int which)
			{
				if (wd.mRunnThread != null) wd.mRunnThread.Cancel();
				wd.button3.disable();
				wd.button3.setText(GlobalCore.Translations.Get("waitForCancel"));
				return false;
			}
		};

		wd.Show();
		return wd;
	}

	protected static CancelWaitDialog createDialog(String msg, IcancelListner listner, Runnable runnable)
	{

		if (msg == null) msg = "";

		Size size = calcMsgBoxSize(msg, false, false, true);

		CancelWaitDialog waitDialog = new CancelWaitDialog(size, "WaitDialog", listner, runnable);
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

		waitDialog.rotateAngle = 0;

		waitDialog.RotateTimer = new Timer();

		waitDialog.RotateTimer.schedule(waitDialog.rotateTimertask, 60, 60);

		return (CancelWaitDialog) waitDialog;

	}

	RunnableReadyHandler mRunnThread;

	private boolean isRunning = false;

	@Override
	public void onShow()
	{
		if (!isRunning && this.runnable != null)
		{

			isRunning = true;

			// start runnable on new Thread
			mRunnThread = new RunnableReadyHandler(runnable)
			{

				@Override
				public void RunnableReady(boolean isCanceld)
				{
					that.close();
					if (isCanceld && cancelListner != null)
					{
						cancelListner.isCanceld();
					}

				}
			};
			mRunnThread.run();
		}

	}
}
