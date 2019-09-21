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
package CB_UI_Base.GL_UI.Activitys;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class ActivityBase extends CB_View_Base {
    protected float MeasuredLabelHeight;
    protected float MeasuredLabelHeightBig;
    protected float ButtonHeight;
    protected float margin = 0;

    public ActivityBase(String Name) {
        this(ActivityBase.ActivityRec(), Name);
    }

    public ActivityBase(CB_RectF rec, String Name) {
        super(rec, Name);
        // dontRenderDialogBackground = true;
        this.setBackground(Sprites.activityBackground);

        MeasuredLabelHeight = Fonts.Measure("T").height * 1.5f;
        MeasuredLabelHeightBig = Fonts.MeasureBig("T").height * 1.5f;
        ButtonHeight = UI_Size_Base.ui_size_base.getButtonHeight();
        this.registerSkinChangedEvent();
    }

    public static CB_RectF ActivityRec() {
        float w = Math.min(UI_Size_Base.ui_size_base.getSmallestWidth(), UI_Size_Base.ui_size_base.getWindowHeight() * 0.66f);

        return new CB_RectF(0, 0, w, UI_Size_Base.ui_size_base.getWindowHeight());
    }

    @Override
    protected void SkinIsChanged() {
        this.setBackground(Sprites.activityBackground);
    }

    @Override
    public GL_View_Base addChild(GL_View_Base view) {
        this.addChildDirekt(view);

        return view;
    }

    public GL_View_Base addChildAtLast(GL_View_Base view) {
        this.addChildDirektLast(view);

        return view;
    }

    @Override
    public void removeChilds() {
        this.removeChildsDirekt();
    }

    @Override
    protected void initialize() {
        // do not call super, it wants clear childs
    }

    protected void finish() {
        GL.that.RunOnGL(() -> GL.that.closeActivity());

    }

    public void show() {
        GL.that.showActivity(this);
    }

    public boolean canCloseWithBackKey() {
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        // that = null;
    }

}
