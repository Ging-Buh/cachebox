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
package CB_UI_Base.GL_UI.Controls.PopUps;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Global;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

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
        float wh = UI_Size_Base.ui_size_base.getButtonWidth() * 4f;

        CB_RectF posRec = new CB_RectF((UI_Size_Base.ui_size_base.getWindowWidth() / 2) - (wh / 2), UI_Size_Base.ui_size_base.getWindowHeight() - wh - Dialog.margin, wh, wh / 2);

        this.setRec(posRec);

        this.mTextField.setHeight(this.getHalfHeight());
        this.mTextField.setWidth(this.getWidth() * 0.8f);
        this.mTextField.setX(this.getHalfWidth() - mTextField.getHalfWidth());
        this.mTextField.setY(this.getHeight() - (mTextField.getHeight() + Dialog.margin));

        this.setWrappedText(Translation.get("ConnectionError") + Global.br + msg);

        this.mIcon = new Image(0, 0, getWidth(), getHalfHeight(), "ImageIcon", false);
        this.mIcon.setSprite(Sprites.getSprite("connection-error"), false);
        float top = 300; // TODO set on the Top of Screen
        this.setY(top - this.getHeight());
        super.addChildToOverlay(mIcon);
    }

}
