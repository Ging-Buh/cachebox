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
package de.droidcachebox.gdx.views;

import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.solver.SolverZeile;

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
