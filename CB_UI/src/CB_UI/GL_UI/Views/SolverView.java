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
package CB_UI.GL_UI.Views;

import CB_Core.Database;
import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.SelectSolverFunction;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Plattform;

/**
 * @author Longri
 */
public class SolverView extends CB_View_Base implements SelectedCacheEvent, KeyboardFocusChangedEvent {

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
        // todo you are possibly comparing the same objects, but the values could have changed
        if (aktCache != GlobalCore.getSelectedCache()) {
            mustLoadSolver = true;
            aktCache = GlobalCore.getSelectedCache();
        }

        if (aktCache == null)
            return;

        SelectedCacheEventList.Add(this);

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
        SelectedCacheEventList.Remove(this);
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
        btnSolve.setOnClickListener((v, x, y, pointer, button) -> {
            solve();
            return true;
        });
        this.addChild(btnSolve);

        btnFunct = new CB_Button(Translation.get("Funct."));
        btnFunct.setOnClickListener((v, x, y, pointer, button) -> {
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
        btnSelect.setOnClickListener((v, x, y, pointer, button) -> {
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

        btnInputWindow.setOnClickListener((v, x, y, pointer, button) -> {
            windowState = WindowState.Left;
            layoutEditFields(GL.that.getFocusedEditTextField());
            return true;
        });

        btnBothWindow.setOnClickListener((v, x, y, pointer, button) -> {
            windowState = WindowState.Both;
            layoutEditFields(GL.that.getFocusedEditTextField());
            return true;
        });

        btnResultWindow.setOnClickListener((v, x, y, pointer, button) -> {
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
            case Both:
                break;
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
            StringBuilder message =  new StringBuilder();
            for (String s : solver.MissingVariables.keySet()) {
                if (message.length() > 0)
                    message.append(", ");
                message.append(s);
            }

            MessageBox.show(Translation.get("insertVars") + "\n" + message, Translation.get("missingVars"), MessageBoxButtons.YesNo, MessageBoxIcon.Asterisk, (which, data) -> {
                {
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
                }
            });
        }
    }

    @Override
    public void SelectedCacheChanged(Cache selectedCache, Waypoint waypoint) {
        onShow();
    }

    private enum WindowState {
        Left, Both, Right
    }

}
