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
package CB_UI.GL_UI.Views;

import CB_Core.Solver.SolverZeile;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;

public class SolverViewItem extends ListViewItemBackground {
    protected boolean isPressed = false;
    protected SolverZeile solverZeile;
    CB_Label lblSolverZeile;

    public SolverViewItem(CB_RectF rec, int Index, SolverZeile solverZeile) {
        super(rec, Index, "");
        this.solverZeile = solverZeile;
    }

    @Override
    protected void initialize() {
        super.initialize();
        lblSolverZeile = new CB_Label(solverZeile.getOrgText() + "\n" + solverZeile.Solution, Fonts.getNormal(), COLOR.getFontColor(), WrapType.MULTILINE);
        lblSolverZeile.setHeight(this.getHeight()); // todo ob das immer passt?
        this.setBorders(UiSizes.getInstance().getMargin(), UiSizes.getInstance().getMargin());
        this.addLast(lblSolverZeile);
    }

    @Override
    public void dispose() {
        lblSolverZeile = null;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        isPressed = true;

        return false;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        isPressed = false;

        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        isPressed = false;

        return false;
    }

}
