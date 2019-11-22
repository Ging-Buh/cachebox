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
package de.droidcachebox.gdx.views;

import de.droidcachebox.*;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextFieldBase;
import de.droidcachebox.gdx.controls.dialogs.Toast;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.solver.DataTypes.DataType;
import de.droidcachebox.solver.Solver;
import de.droidcachebox.solver.SolverZeile;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Plattform;

/**
 * @author Longri
 */
public class SolverView extends CB_View_Base implements SelectedCacheChangedEventListener, KeyboardFocusChangedEvent {

    private static SolverView that;
    private WindowState windowState = WindowState.Both;
    private Solver solver = new Solver("", GlobalCore.getInstance());
    private boolean mustLoadSolver;
    private Cache aktCache;
    private CB_Button btnSolve, btnFunct, btnSelect, btnInputWindow, btnBothWindow, btnResultWindow;
    private EditTextField edInput, edResult;

    private SolverView() {
        super(ViewManager.leftTab.getContentRec(), "SolverView");
        addControls();
        layout();
    }

    public static SolverView getInstance() {
        if (that == null) that = new SolverView();
        return that;
    }

    @Override
    public void KeyboardFocusChanged(EditTextField editTextField) {
        layoutEditFields(editTextField);
    }

    @Override
    public void onShow() {
        // view must be refilled with values
        KeyboardFocusChangedEventList.Add(this);
        if (aktCache != GlobalCore.getSelectedCache()) {
            mustLoadSolver = true;
            aktCache = GlobalCore.getSelectedCache();
        }

        if (aktCache == null)
            return;

        SelectedCacheChangedEventListeners.getInstance().add(this);

        if (mustLoadSolver) {
            String sol = Database.GetSolver(aktCache);
            if (sol == null)
                sol = "";
            edInput.setText(sol);
            edInput.showFromLineNo(0);
            mustLoadSolver = false;
            this.edResult.setText("");
        }
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.Remove(this);
        SelectedCacheChangedEventListeners.getInstance().remove(this);
        if (aktCache != null) {
            Database.SetSolver(aktCache, edInput.getText());
            // When Solver 1 changes -> Solver 2 must reload the information from DB to get the changes from Solver 1
            aktCache.setSolver1Changed(true);
        }

        mustLoadSolver = true;
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        layout();
    }

    private void addControls() {
        btnSolve = new CB_Button(Translation.get("Solve"));
        btnSolve.setClickHandler((v, x, y, pointer, button) -> {
            solve();
            return true;
        });
        this.addChild(btnSolve);

        btnFunct = new CB_Button(Translation.get("Funct."));
        btnFunct.setClickHandler((v, x, y, pointer, button) -> {
            SelectSolverFunction ssf = new SelectSolverFunction(solver, DataType.None, function -> {
                // ausgewählte Funktion verarbeiten!
                // wenn funktion==null wurde Cancel gedrückt

                if (function != null) {

                    CharSequence selection = edInput.getSelectedText();

                    String newFunction = function.getLongLocalName();
                    int newFunctionLength = newFunction.length();
                    String zeichen = "";
                    if (function.needsTextArgument()) {
                        zeichen = "\"";
                        if ((selection.length() > 0) && (selection.charAt(0) == '"')) {
                            // Anführungszeichen bereits vorhanden
                            zeichen = "";
                        }
                    }
                    newFunction += "(" + zeichen + selection + zeichen + ")";
                    int newSelectionStart = edInput.getSelectionStart() + newFunctionLength + 1 + zeichen.length() + selection.length();

                    int start = edInput.getSelectionStart();
                    int end = edInput.getSelectionEnd();

                    StringBuilder sb = new StringBuilder();
                    String actText = edInput.getText();

                    sb.append(actText.subSequence(0, start));
                    sb.append(newFunction);
                    sb.append(actText.subSequence(end, actText.length()));
                    edInput.setText(sb.toString());
                    edInput.setCursorPosition(newSelectionStart);
                    edInput.setFocus(true);
                }
            });
            GL.that.showDialog(ssf);
            return true;
        });
        this.addChild(btnFunct);

        btnSelect = new CB_Button(Translation.get("Select."));
        btnSelect.setClickHandler((v, x, y, pointer, button) -> {
            GL.that.setFocusedEditTextField(null);
            return true;
        });
        btnSelect.disable();
        btnSelect.setVisible(false);
        this.addChild(btnSelect);

        btnInputWindow = new CB_Button(Translation.get("LeftWindow"));
        this.addChild(btnInputWindow);
        btnBothWindow = new CB_Button(Translation.get("BothWindow"));
        this.addChild(btnBothWindow);
        btnResultWindow = new CB_Button(Translation.get("RightWindow"));
        this.addChild(btnResultWindow);

        edInput = new EditTextField(this, "edInput");
        edInput.setWrapType(WrapType.MULTILINE);
        this.addChild(edInput);
        edResult = new EditTextField(this, "edResult");
        edResult.setWrapType(WrapType.MULTILINE);
        //edResult.disable();
        edResult.setEditable(false);
        edResult.disableKeyboardPopup();
        this.addChild(edResult);

        btnInputWindow.setClickHandler((v, x, y, pointer, button) -> {
            windowState = WindowState.Left;
            layoutEditFields(GL.that.getFocusedEditTextField());
            return true;
        });

        btnBothWindow.setClickHandler((v, x, y, pointer, button) -> {
            windowState = WindowState.Both;
            layoutEditFields(GL.that.getFocusedEditTextField());
            return true;
        });

        btnResultWindow.setClickHandler((v, x, y, pointer, button) -> {
            windowState = WindowState.Right;
            layoutEditFields(GL.that.getFocusedEditTextField());
            return true;
        });

        edInput.addListPosChangedHandler(() -> GL.that.RunOnGL(() -> {
            int aktInputLine = edInput.getTopLineNo();
            int aktResultLine = edResult.getTopLineNo();
            if (aktInputLine != aktResultLine)
                edResult.showFromLineNo(aktInputLine);
        }));

        edResult.addListPosChangedHandler(() -> GL.that.RunOnGL(() -> {
            int aktInputLine = edInput.getTopLineNo();
            int aktResultLine = edResult.getTopLineNo();
            if (aktInputLine != aktResultLine)
                edInput.showFromLineNo(aktResultLine);
        }));
    }

    private void layout() {

        float btnWidth = this.getWidth() / 3;
        float yPos = this.getHeight() - btnSolve.getHeight();

        btnSolve.setWidth(btnWidth);
        btnFunct.setWidth(btnWidth);
        btnSelect.setWidth(btnWidth);
        btnInputWindow.setWidth(btnWidth);
        btnBothWindow.setWidth(btnWidth);
        btnResultWindow.setWidth(btnWidth);

        //first Line
        btnSolve.setPos(0, yPos);
        btnFunct.setPos(btnWidth, yPos);
        btnSelect.setPos(btnWidth * 2, yPos);

        //bottom Line
        yPos = 0;
        btnInputWindow.setPos(0, yPos);
        btnBothWindow.setPos(btnWidth, yPos);
        btnResultWindow.setPos(btnWidth * 2, yPos);

        layoutEditFields(GL.that.getFocusedEditTextField());

    }

    private void layoutEditFields(EditTextFieldBase focus) {

        float le = this.getHalfWidth();
        float le2 = this.getWidth() * 0.2f;
        switch (windowState) {
            case Left:
                le = le2;
                break;
            case Right:
                le = this.getWidth() - le2;
                break;
            default:
                break;

        }

        float y = btnInputWindow.getMaxY();
        float editHeight = this.getHeight() - (btnSolve.getHeight() * 2);
        float widthLeft = this.getWidth() - le;

        if (Plattform.used == Plattform.Android && focus == edInput) {
            y = this.getHalfHeight();
            editHeight = btnSolve.getY() - y;
        }

        edInput.set(0, y, widthLeft, editHeight);
        edResult.set(widthLeft, y, this.getWidth() - widthLeft, editHeight);

    }

    protected void solve() {

        edResult.setEditable(true);

        solver = new Solver(edInput.getText(), GlobalCore.getInstance());
        if (!solver.Solve()) {
            GL.that.Toast("Error", Toast.LENGTH_SHORT);
        }
        edResult.setText("");
        StringBuilder result = new StringBuilder();
        for (SolverZeile zeile : solver) {
            result.append(zeile.Solution).append("\n");
        }

        edResult.setText(result.toString());
        edResult.showFromLineNo(edInput.getTopLineNo());
        edResult.setEditable(false);

        if ((solver.MissingVariables != null) && (solver.MissingVariables.size() > 0)) {
            // es sind nicht alle Variablen zugewiesen
            // Abfrage, ob die Deklarationen eingefügt werden sollen
            StringBuilder message = new StringBuilder();
            for (String s : solver.MissingVariables.keySet()) {
                if (message.length() > 0)
                    message.append(", ");
                message.append(s);
            }

            MessageBox.show(Translation.get("insertVars") + "\n" + message, Translation.get("missingVars"), MessageBoxButtons.YesNo, MessageBoxIcon.Asterisk,
                    (which, data) -> {
                        // Behandle das ergebniss
                        if (which == 1) {/* User clicked OK so do some stuff */
                            StringBuilder missing = new StringBuilder();
                            for (String s : solver.MissingVariables.keySet()) {
                                missing.append(s).append("=\n");
                                edResult.setText("\n" + edInput.getText());
                            }
                            edInput.setText(missing + edInput.getText());
                        }
                        return true;
                    });
        }
    }

    @Override
    public void selectedCacheChanged(Cache selectedCache, Waypoint waypoint) {
        onShow();
    }

    private enum WindowState {
        Left, Both, Right
    }

}
