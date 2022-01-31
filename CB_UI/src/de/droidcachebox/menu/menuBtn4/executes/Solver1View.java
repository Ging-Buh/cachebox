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
package de.droidcachebox.menu.menuBtn4.executes;

import static de.droidcachebox.menu.Action.ShowSolver1;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.views.SelectSolverFunction;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.ShowSolver1;
import de.droidcachebox.solver.DataType;
import de.droidcachebox.solver.SolverLine;
import de.droidcachebox.solver.SolverLines;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Plattform;

/**
 * @author Longri
 */
public class Solver1View extends CB_View_Base implements CacheSelectionChangedListeners.CacheSelectionChangedListener, KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {
    private WindowState windowState = WindowState.Both;
    private SolverLines solverLines = new SolverLines("", GlobalCore.getInstance());
    private boolean mustLoadSolver;
    private Cache aktCache;
    private CB_Button btnSolve, btnFunct, btnSelect, btnInputWindow, btnBothWindow, btnResultWindow;
    private EditTextField edInput, edResult;
    private final CachesDAO cachesDAO;

    public Solver1View() {
        super(ViewManager.leftTab.getContentRec(), "SolverView");
        cachesDAO = new CachesDAO();
        addControls();
        layout();
    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        layoutEditFields(editTextField);
    }

    @Override
    public void onShow() {
        // view must be refilled with values
        KeyboardFocusChangedEventList.add(this);
        if (aktCache != GlobalCore.getSelectedCache()) {
            mustLoadSolver = true;
            aktCache = GlobalCore.getSelectedCache();
        }

        if (aktCache == null)
            return;

        CacheSelectionChangedListeners.getInstance().addListener(this);

        if (mustLoadSolver) {
            String sol = cachesDAO.getSolver(aktCache);
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
        KeyboardFocusChangedEventList.remove(this);
        CacheSelectionChangedListeners.getInstance().remove(this);
        if (aktCache != null) {
            cachesDAO.setSolver(aktCache, edInput.getText());
            // When Solver 1 changes -> Solver 2 must reload the information from DB to get the changes from Solver 1
            aktCache.setSolver1Changed(true);
        }

        mustLoadSolver = true;
        ((ShowSolver1) ShowSolver1.action).viewIsHiding();
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
            SelectSolverFunction ssf = new SelectSolverFunction(solverLines, DataType.None, function -> {
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

        edInput.addListPosChangedHandler(() -> GL.that.runOnGL(() -> {
            int aktInputLine = edInput.getTopLineNo();
            int aktResultLine = edResult.getTopLineNo();
            if (aktInputLine != aktResultLine)
                edResult.showFromLineNo(aktInputLine);
        }));

        edResult.addListPosChangedHandler(() -> GL.that.runOnGL(() -> {
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

    private void layoutEditFields(EditTextField focus) {

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

        solverLines = new SolverLines(edInput.getText(), GlobalCore.getInstance());
        if (!solverLines.Solve()) {
            GL.that.toast("Error");
        }
        edResult.setText("");
        StringBuilder result = new StringBuilder();
        for (SolverLine zeile : solverLines) {
            result.append(zeile.Solution).append("\n");
        }

        edResult.setText(result.toString());
        edResult.showFromLineNo(edInput.getTopLineNo());
        edResult.setEditable(false);

        if ((solverLines.MissingVariables != null) && (solverLines.MissingVariables.size() > 0)) {
            // es sind nicht alle Variablen zugewiesen
            // Abfrage, ob die Deklarationen eingefügt werden sollen
            StringBuilder message = new StringBuilder();
            for (String s : solverLines.MissingVariables.keySet()) {
                if (message.length() > 0)
                    message.append(", ");
                message.append(s);
            }

            ButtonDialog bd = new ButtonDialog(Translation.get("insertVars") + "\n" + message, Translation.get("missingVars"), MsgBoxButton.YesNo, MsgBoxIcon.Asterisk);
            bd.setButtonClickHandler((which, data) -> {
                // Behandle das ergebniss
                if (which == 1) {/* User clicked OK so do some stuff */
                    StringBuilder missing = new StringBuilder();
                    for (String s : solverLines.MissingVariables.keySet()) {
                        missing.append(s).append("=\n");
                        edResult.setText("\n" + edInput.getText());
                    }
                    edInput.setText(missing + edInput.getText());
                }
                return true;
            });
            bd.show();
        }
    }

    @Override
    public void handleCacheSelectionChanged(Cache selectedCache, Waypoint waypoint) {
        onShow();
    }

    private enum WindowState {
        Left, Both, Right
    }

}
