/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.GL_UI.Controls.Dialogs;

import org.slf4j.LoggerFactory;

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
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Log.Trace;

/**
 * Ein Wait Dialog mit �bergabe eines Runable zur Abarbeitung, welcher abgebrochen werden kann
 * 
 * @author Longri
 */
public class CancelWaitDialog extends WaitDialog {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CancelWaitDialog.class);

    // CancelWaitDialog that;

    public interface IcancelListener {
	public void isCanceld();
    }

    public interface IReadyListener {
	public void isReady();
    }

    protected IcancelListener cancelListener;
    private IReadyListener readyListener;
    private final cancelRunnable runnable;

    public CancelWaitDialog(Size size, String name, IcancelListener listener, cancelRunnable runnable) {
	super(size, name);
	this.cancelListener = listener;
	this.runnable = runnable;
    }

    public static CancelWaitDialog ShowWait(String Msg, IcancelListener listener, cancelRunnable runnable) {
	final CancelWaitDialog wd = ShowWait(Msg, WorkAnimation.GetINSTANCE(), listener, runnable);
	wd.setCallerName(Trace.getCallerName(2));
	return wd;
    }

    public static CancelWaitDialog ShowWait(String Msg, AnimationBase Animation, IcancelListener listener, cancelRunnable runnable) {
	final CancelWaitDialog wd = createDialog(Msg, listener, runnable);
	wd.setCallerName(Trace.getCallerName(1));
	CB_RectF animationRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
	Animation.setRec(animationRec);
	wd.animation = Animation;
	wd.setButtonCaptions(MessageBoxButtons.Cancel);
	wd.mMsgBoxClickListener = new GL_MsgBox.OnMsgBoxClickListener() {

	    @Override
	    public boolean onClick(int which, Object data) {
		if (wd.mRunnThread != null)
		    wd.mRunnThread.Cancel();
		wd.button3.disable();
		wd.button3.setText(Translation.Get("waitForCancel"));
		return false;
	    }
	};

	SizeF contentSize = wd.getContentSize();
	float imageYPos = (contentSize.height < (wd.animation.getHeight() * 1.7)) ? contentSize.halfHeight - wd.animation.getHalfHeight() : contentSize.height - wd.animation.getHeight() - margin;
	wd.animation.setY(imageYPos);
	wd.addChild(wd.animation);
	wd.animation.play();
	wd.Show();
	return wd;
    }

    protected static CancelWaitDialog createDialog(String msg, IcancelListener listener, cancelRunnable runnable) {

	if (msg == null)
	    msg = "";

	Size size = calcMsgBoxSize(msg, false, false, true, false);

	CancelWaitDialog waitDialog = new CancelWaitDialog(size, "WaitDialog", listener, runnable);
	waitDialog.setTitle("");
	waitDialog.setButtonCaptions(MessageBoxButtons.Cancel);

	SizeF contentSize = waitDialog.getContentSize();

	CB_RectF imageRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

	waitDialog.label = new Label("waitDialog" + " label", contentSize.getBounds());
	waitDialog.label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UI_Size_Base.that.getButtonHeight());
	waitDialog.label.setX(imageRec.getMaxX() + margin);
	waitDialog.label.setWrappedText(msg);

	int lineCount = waitDialog.label.getLineCount();
	waitDialog.label.setY(0);

	if (lineCount == 1) {
	    waitDialog.label.setText(msg);
	    waitDialog.label.setVAlignment(VAlignment.CENTER);
	} else {
	    waitDialog.label.setVAlignment(VAlignment.TOP);
	}

	waitDialog.addChild(waitDialog.label);

	return waitDialog;

    }

    RunnableReadyHandler mRunnThread;

    private boolean isRunning = false;

    @Override
    public void onShow() {
	if (!isRunning && this.runnable != null) {

	    isRunning = true;

	    // start runnable on new Thread
	    mRunnThread = new RunnableReadyHandler() {

		@Override
		public void RunnableReady(boolean isCanceld) {
		    // CancelWaitDialog.this.close();
		    if (isCanceld && cancelListener != null) {
			cancelListener.isCanceld();
		    }

		    if (readyListener != null) {
			readyListener.isReady();
		    }

		}

		@Override
		public void run() {
		    runnable.run();
		    if (readyListener != null)
			readyListener.isReady();
		    CancelWaitDialog.this.close();
		}

		@Override
		public boolean cancel() {
		    return runnable.cancel();
		}
	    };
	    mRunnThread.start();
	}
    }

    public void setReadyListener(IReadyListener readyListener) {
	this.readyListener = readyListener;
    }
}
