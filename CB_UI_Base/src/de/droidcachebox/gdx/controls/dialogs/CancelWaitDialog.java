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
package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.RunnableReadyHandler;
import de.droidcachebox.utils.TestCancelRunnable;

/**
 * Ein Wait Dialog mit Übergabe eines Runable zur Abarbeitung, welcher abgebrochen werden kann
 *
 * @author Longri
 */
public class CancelWaitDialog extends WaitDialog {

    private static String sClass = "CancelWaitDialog";
    private final TestCancelRunnable cancelRunnable;
    protected IcancelListener cancelListener;
    RunnableReadyHandler runnableReadyHandler;
    private IReadyListener readyListener;
    private boolean isRunning = false;

    public CancelWaitDialog(String msg, AnimationBase _animation, IcancelListener cancelListener, TestCancelRunnable cancelRunnable) {
        super(calcMsgBoxSize(msg, false, false, true, false), sClass);
        this.cancelListener = cancelListener;
        this.cancelRunnable = cancelRunnable;

        setTitle("");
        setButtonCaptions(MsgBoxButton.Cancel);

        SizeF contentSize = getContentSize();

        CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());

        label = new CB_Label(contentSize.getBounds());
        label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getInstance().getButtonHeight());
        label.setX(imageRec.getMaxX() + margin);
        label.setWrappedText(msg);

        int lineCount = label.getLineCount();
        label.setY(0);

        if (lineCount == 1) {
            label.setText(msg);
            label.setVAlignment(VAlignment.CENTER);
        } else {
            label.setVAlignment(VAlignment.TOP);
        }

        addChild(label);

        setButtonCaptions(MsgBoxButton.Cancel);

        mMsgBoxClickListener = (which, data) -> {
            if (runnableReadyHandler != null)
                runnableReadyHandler.doInterrupt();
            btnRightNegative.disable();
            btnRightNegative.setText(Translation.get("waitForCancel"));
            return false;
        };

        animation = _animation;

        if (animation != null) {
            CB_RectF animationRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
            animation.setRec(animationRec);
            // SizeF contentSize = getContentSize();
            float imageYPos = (contentSize.getHeight() < (animation.getHeight() * 1.7)) ? contentSize.getHalfHeight() - animation.getHalfHeight() : contentSize.getHeight() - animation.getHeight() - margin;
            animation.setY(imageYPos);
            addChild(animation);
            animation.play();
        }

    }

    @Override
    public void onShow() {
        if (!isRunning && this.cancelRunnable != null) {

            isRunning = true;

            // start cancelRunnable on new Thread
            runnableReadyHandler = new RunnableReadyHandler() {

                @Override
                public void ready(boolean isCanceled) {
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
                public boolean checkCanceled() {
                    boolean mCancel = true;
                    if (cancelRunnable != null) {
                        mCancel = cancelRunnable.checkCanceled();
                    }
                    CancelWaitDialog.this.close();
                    return mCancel;
                }
            };
            runnableReadyHandler.doStart();
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
