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
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.translation.Translation;

/**
 * A WaitDialog with a cancelButton
 * the runnable (runAndReady) starts onShow
 */
public class CancelWaitDialog extends ButtonDialog {

    private static final String sClass = "CancelWaitDialog";
    private final RunAndReady runAndReady;
    AnimationBase animation;

    public CancelWaitDialog(String msg, AnimationBase _animation, RunAndReady runAndReady) {
        super(msg, "", MsgBoxButton.Cancel, MsgBoxIcon.Asterisk);
        // using MsgBoxIcon.Asterisk as placeholder to generate the iconImage
        this.runAndReady = runAndReady;
        buttonClickHandler = (which, data) -> {
            runAndReady.setIsCanceled();
            btnRightNegative.disable();
            btnRightNegative.setText(Translation.get("waitForCancel"));
            return false;
        };
        if (_animation == null)
            animation = new WorkAnimation(iconImage);
        else {
            animation = _animation;
            animation.setRec(iconImage);
        }
        removeChild(iconImage);
        addChild(animation);
    }

    @Override
    public void onShow() {
        if (runAndReady != null) runAndReady.doStart(this);
    }

}
