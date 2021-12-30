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

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CancelListener;
import de.droidcachebox.utils.RunAndReady;

/**
 * A WaitDialog extended by a runnable that can be canceled
 *
 * @author Longri
 */
public class CancelWaitDialog extends WaitDialog {

    private static final String sClass = "CancelWaitDialog";
    private final AtomicBoolean isCanceled;
    private final RunAndReady runAndReady;
    private CancelListener cancelListener;

    public CancelWaitDialog(String msg, AnimationBase _animation, RunAndReady runAndReady) {
        super(calcMsgBoxSize(msg, false, false, true, false), sClass);
        this.runAndReady = runAndReady;
        isCanceled = new AtomicBoolean();

        setTitle("");
        setButtonCaptions(MsgBoxButton.Cancel);
        mMsgBoxClickListener = (which, data) -> {
            isCanceled.set(true);
            if (cancelListener != null)
                cancelListener.setIsCanceled();
            btnRightNegative.disable();
            btnRightNegative.setText(Translation.get("waitForCancel"));
            return false;
        };

        SizeF contentSize = getContentSize();
        label = new CB_Label(contentSize.getBounds());
        label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getInstance().getButtonHeight());
        CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
        label.setX(imageRec.getMaxX() + margin);
        label.setWrappedText(msg);
        label.setY(0);
        int lineCount = label.getLineCount();
        if (lineCount == 1) {
            label.setText(msg);
            label.setVAlignment(VAlignment.CENTER);
        } else {
            label.setVAlignment(VAlignment.TOP);
        }
        addChild(label);

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

    public void setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    @Override
    public void onShow() {
        if (runAndReady != null) runAndReady.doStart();
    }

}
