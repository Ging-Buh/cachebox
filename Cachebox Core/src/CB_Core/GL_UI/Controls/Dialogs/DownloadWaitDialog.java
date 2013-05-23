package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Animation.FrameAnimation;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

public class DownloadWaitDialog extends CancelWaitDialog
{
	public static final int ANIMATION_DURATION = 1000;

	public DownloadWaitDialog(Size size, String name, IcancelListner listner, Runnable runnable)
	{
		super(size, name, listner, runnable);
		that = this;
	}

	protected static CancelWaitDialog createDialog(String msg, IcancelListner listner, Runnable runnable)
	{
		CancelWaitDialog waitDialog = CancelWaitDialog.createDialog(msg, listner, runnable);

		waitDialog.removeChild(waitDialog.iconImage);

		CB_RectF imageRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

		waitDialog.iconImage = new FrameAnimation(imageRec, "download animation");

		((FrameAnimation) waitDialog.iconImage).addFrame(SpriteCache.getThemedSprite("download-1"));
		((FrameAnimation) waitDialog.iconImage).addFrame(SpriteCache.getThemedSprite("download-2"));
		((FrameAnimation) waitDialog.iconImage).addFrame(SpriteCache.getThemedSprite("download-3"));
		((FrameAnimation) waitDialog.iconImage).addFrame(SpriteCache.getThemedSprite("download-4"));
		((FrameAnimation) waitDialog.iconImage).addFrame(SpriteCache.getThemedSprite("download-5"));
		waitDialog.iconImage.play(ANIMATION_DURATION);
		waitDialog.addChild(waitDialog.iconImage);

		return waitDialog;
	}

	public static CancelWaitDialog ShowWait(String Msg, IcancelListner listner, Runnable runnable)
	{
		final CancelWaitDialog wd = createDialog(Msg, listner, runnable);

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

		wd.Show();
		return wd;
	}

}
