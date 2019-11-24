/*
 * Copyright (C) 2015 team-cachebox.de
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

import de.droidcachebox.AbstractGlobal;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Dialog;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

public class ConnectionError extends Toast {
    public static ConnectionError INSTANCE = new ConnectionError();

    private Image mIcon;

    private ConnectionError() {
        super(new CB_RectF(0, 0, 10, 10), "ConectionError");
        createContent("");
    }

    public ConnectionError(String msg) {
        super(new CB_RectF(0, 0, 10, 10), "ConectionError");
        createContent(msg);
    }

    private void createContent(String msg) {
        float wh = UiSizes.getInstance().getButtonHeight() * 4f;

        CB_RectF posRec = new CB_RectF((UiSizes.getInstance().getButtonHeight() / 2) - (wh / 2), UiSizes.getInstance().getWindowHeight() - wh - Dialog.margin, wh, wh / 2);

        this.setRec(posRec);

        this.mTextField.setHeight(this.getHalfHeight());
        this.mTextField.setWidth(this.getWidth() * 0.8f);
        this.mTextField.setX(this.getHalfWidth() - mTextField.getHalfWidth());
        this.mTextField.setY(this.getHeight() - (mTextField.getHeight() + Dialog.margin));

        this.setWrappedText(Translation.get("ConnectionError") + AbstractGlobal.br + msg);

        this.mIcon = new Image(0, 0, getWidth(), getHalfHeight(), "ImageIcon", false);
        this.mIcon.setSprite(Sprites.getSprite("connection-error"));
        float top = 300; // TODO set on the Top of Screen
        this.setY(top - this.getHeight());
        super.addChildToOverlay(mIcon);
    }

}
