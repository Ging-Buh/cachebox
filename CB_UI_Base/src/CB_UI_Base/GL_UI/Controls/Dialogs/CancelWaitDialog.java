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

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.CB_Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.Log.Trace;

/**
 * Ein Wait Dialog mit Übergabe eines Runable zur Abarbeitung, welcher abgebrochen werden kann
 *
 * @author Longri
 */
public class CancelWaitDialog extends WaitDialog {

    private static String sKlasse = "CancelWaitDialog";
    // CancelWaitDialog that;
    private final ICancelRunnable cancelRunnable;
    protected IcancelListener cancelListener;
    RunnableReadyHandler mRunnThread;
    private IReadyListener readyListener;
    private boolean isRunning = false;

    public CancelWaitDialog(Size size, String name, IcancelListener cancelListener, ICancelRunnable cancelRunnable) {
        super(size, name);
        this.cancelListener = cancelListener;
        this.cancelRunnable = cancelRunnable;
    }

    public static CancelWaitDialog ShowWait(String Msg, IcancelListener listener, ICancelRunnable cancelRunnable) {
        final CancelWaitDialog wd = ShowWait(Msg, WorkAnimation.GetINSTANCE(), listener, cancelRunnable);
        wd.setCallerName(Trace.getCallerName(2));
        return wd;
    }

    public static CancelWaitDialog ShowWait(String Msg, AnimationBase Animation, IcancelListener cancelListener, ICancelRunnable cancelRunnable) {
        Log.debug(sKlasse, Msg);
        final CancelWaitDialog wd = createDialog(Msg, cancelListener, cancelRunnable);
        wd.setCallerName(Trace.getCallerName(1));
        CB_RectF animationRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
        Animation.setRec(animationRec);
        wd.animation = Animation;
        wd.setButtonCaptions(MessageBoxButtons.Cancel);
        wd.mMsgBoxClickListener = (which, data) -> {
            if (wd.mRunnThread != null)
                wd.mRunnThread.Cancel();
            wd.button3.disable();
            wd.button3.setText(Translation.get("waitForCancel"));
            return false;
        };

        SizeF contentSize = wd.getContentSize();
        float imageYPos = (contentSize.height < (wd.animation.getHeight() * 1.7)) ? contentSize.halfHeight - wd.animation.getHalfHeight() : contentSize.height - wd.animation.getHeight() - margin;
        wd.animation.setY(imageYPos);
        wd.addChild(wd.animation);
        wd.animation.play();
        wd.show();
        return wd;
    }

    private static CancelWaitDialog createDialog(String msg, IcancelListener listener, ICancelRunnable cancelRunnable) {

        if (msg == null)
            msg = "";

        Size size = calcMsgBoxSize(msg, false, false, true, false);

        CancelWaitDialog waitDialog = new CancelWaitDialog(size, "WaitDialog", listener, cancelRunnable);
        waitDialog.setTitle("");
        waitDialog.setButtonCaptions(MessageBoxButtons.Cancel);

        SizeF contentSize = waitDialog.getContentSize();

        CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());

        waitDialog.label = new CB_Label(contentSize.getBounds());
        waitDialog.label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getInstance().getButtonHeight());
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

    @Override
    public void onShow() {
        if (!isRunning && this.cancelRunnable != null) {

            isRunning = true;

            // start cancelRunnable on new Thread
            mRunnThread = new RunnableReadyHandler() {

                @Override
                public void RunnableIsReady(boolean isCanceled) {
                    // CancelWaitDialog.this.close();
                    if (isCanceled && cancelListener != null) {
                        cancelListener.isCanceled();
                    }

                    if (readyListener != null) {
                        readyListener.isReady();
                    }

                    CancelWaitDialog.this.close();

                }

                @Override
                public void run() {
                    cancelRunnable.run();
                    if (readyListener != null)
                        readyListener.isReady();
                    CancelWaitDialog.this.close();
                }

                @Override
                public boolean doCancel() {
                    boolean mCancel = true;
                    if (cancelRunnable != null) {
                        mCancel = cancelRunnable.doCancel();
                    }
                    CancelWaitDialog.this.close();
                    return mCancel;
                }
            };
            mRunnThread.start();
        }
    }

    public void setReadyListener(IReadyListener readyListener) {
        this.readyListener = readyListener;
    }

    public interface IcancelListener {
        void isCanceled();
    }

    public interface IReadyListener {
        void isReady();
    }
}
