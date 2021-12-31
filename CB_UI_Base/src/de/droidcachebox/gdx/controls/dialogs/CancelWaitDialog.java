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

import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;

/**
 * A WaitDialog with a cancelButton
 * the runnable (runAndReady) starts onShow
 */
public class CancelWaitDialog extends WaitDialog {

    private static final String sClass = "CancelWaitDialog";
    private final RunAndReady runAndReady;

    public CancelWaitDialog(String msg, AnimationBase _animation, RunAndReady runAndReady) {
        super(msg, calcMsgBoxSize(msg, false, false, true, false), sClass);
        this.runAndReady = runAndReady;

        // the cancel button (WaitDialog has none)
        setButtonCaptions(MsgBoxButton.Cancel);
        mMsgBoxClickListener = (which, data) -> {
            runAndReady.setIsCanceled();
            btnRightNegative.disable();
            btnRightNegative.setText(Translation.get("waitForCancel"));
            return false;
        };

        // label with msg-text comes from WaitDialog

        // set animation position and size from WaitDialog
        if (animation != null) {
            _animation.setRec(new CB_RectF(0, 0, animation.getWidth(), animation.getHeight()));
            animation.setY(_animation.getY());
            animation = _animation;
            addChild(animation);
            animation.play();
        }
    }

    @Override
    public void onShow() {
        if (runAndReady != null) runAndReady.doStart(this);
    }

}
