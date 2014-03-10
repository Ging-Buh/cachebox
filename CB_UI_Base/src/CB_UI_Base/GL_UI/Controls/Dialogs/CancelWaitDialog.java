package CB_UI_Base.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;

/**
 * Ein Wait Dialog mit übergabe eines Runable zur Abarbeitung, welcher abgebrochen werden kann
 * 
 * @author Longri
 */
public class CancelWaitDialog extends WaitDialog
{

	// CancelWaitDialog that;

	public interface IcancelListner
	{
		public void isCanceld();
	}

	public interface IReadyListner
	{
		public void isReady();
	}

	protected IcancelListner cancelListner;
	private IReadyListner readyListner;
	private final Runnable runnable;

	public CancelWaitDialog(Size size, String name, IcancelListner listner, Runnable runnable)
	{
		super(size, name);
		this.cancelListner = listner;
		this.runnable = runnable;
	}

	public static CancelWaitDialog ShowWait(String Msg, IcancelListner listner, Runnable runnable)
	{
		return ShowWait(Msg, WorkAnimation.GetINSTANCE(), listner, runnable);
	}

	public static CancelWaitDialog ShowWait(String Msg, AnimationBase Animation, IcancelListner listner, Runnable runnable)
	{
		final CancelWaitDialog wd = createDialog(Msg, listner, runnable);

		CB_RectF animationRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
		Animation.setRec(animationRec);
		wd.animation = Animation;
		wd.setButtonCaptions(MessageBoxButtons.Cancel);
		wd.mMsgBoxClickListner = new GL_MsgBox.OnMsgBoxClickListener()
		{

			@Override
			public boolean onClick(int which, Object data)
			{
				if (wd.mRunnThread != null) wd.mRunnThread.Cancel();
				wd.button3.disable();
				wd.button3.setText(Translation.Get("waitForCancel"));
				return false;
			}
		};

		SizeF contentSize = wd.getContentSize();
		float imageYPos = (contentSize.height < (wd.animation.getHeight() * 1.7)) ? contentSize.halfHeight - wd.animation.getHalfHeight()
				: contentSize.height - wd.animation.getHeight() - margin;
		wd.animation.setY(imageYPos);
		wd.addChild(wd.animation);
		wd.animation.play();
		wd.Show();
		return wd;
	}

	protected static CancelWaitDialog createDialog(String msg, IcancelListner listner, Runnable runnable)
	{

		if (msg == null) msg = "";

		Size size = calcMsgBoxSize(msg, false, false, true, false);

		CancelWaitDialog waitDialog = new CancelWaitDialog(size, "WaitDialog", listner, runnable);
		waitDialog.setTitle("");
		waitDialog.setButtonCaptions(MessageBoxButtons.Cancel);

		SizeF contentSize = waitDialog.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

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

		waitDialog.addChild(waitDialog.label);

		return waitDialog;

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
					// CancelWaitDialog.this.close();
					if (isCanceld && cancelListner != null)
					{
						cancelListner.isCanceld();
					}

					if (readyListner != null)
					{
						readyListner.isReady();
					}

				}
			};
			mRunnThread.run();
		}
	}

	public void setReadyListner(IReadyListner ReadyListner)
	{
		readyListner = ReadyListner;
	}
}
