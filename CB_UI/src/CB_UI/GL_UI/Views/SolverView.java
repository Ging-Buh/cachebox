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
import CB_Core.Solver.Function;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.SelectSolverFunction;
import CB_UI.GL_UI.Activitys.SelectSolverFunction.IFunctionResult;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextField.IListPosChanged;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Plattform;
import org.slf4j.LoggerFactory;

/**
 * @author Longri
 */
public class SolverView extends CB_View_Base implements SelectedCacheEvent {

    private WindowState windowState = WindowState.Both;
    private Solver solver = new Solver("");
    private boolean mustLoadSolver;
    private Cache aktCache;

    private Button btnSolve, btnFunct, btnSelect, btnInputWindow, btnBothWindow, btnResultWindow;
    private EditTextField edInput, edResult;
    private final OnClickListener btnFunctClicked = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            SelectSolverFunction ssf = new SelectSolverFunction(solver, DataType.None, new IFunctionResult() {

                @Override
                public void selectedFunction(final Function function) {
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
                }
            });
            GL.that.showDialog(ssf);
            return true;
        }
    };
    private final OnMsgBoxClickListener mDialogListener = new OnMsgBoxClickListener() {
        @Override
        public boolean onClick(int which, Object data) {
            {
                // Behandle das ergebniss
                switch (which) {
                    case 1:
                        /* User clicked OK so do some stuff */
                        String missing = "";
                        for (String s : solver.MissingVariables.keySet()) {
                            missing += s + "=\n";
                            edResult.setText("\n" + edInput.getText().toString());
                        }
                        edInput.setText(missing + edInput.getText().toString());
                }
                return true;
            }
        }

    };

    public SolverView(CB_RectF rec, String Name) {
        super(rec, Name);
        addControls();
        layout();
        KeyboardFocusChangedEventList.Add(new KeyboardFocusChangedEvent() {
            @Override
            public void KeyboardFocusChanged(EditTextFieldBase focus) {
                layoutEditFields(focus);
            }
        });
    }

    @Override
    public void onShow() {
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
        SelectedCacheEventList.Remove(this);
        if (aktCache != null) {
            Database.SetSolver(aktCache, edInput.getText().toString());
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
        btnSolve = new Button(Translation.Get("Solve"));
        btnSolve.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                solve();
                return true;
            }
        });
        this.addChild(btnSolve);

        btnFunct = new Button(Translation.Get("Funct."));
        btnFunct.setOnClickListener(btnFunctClicked);
        this.addChild(btnFunct);

        btnSelect = new Button(Translation.Get("Select."));
        btnSelect.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                GL.that.setFocusedEditTextField(null);
                return true;
            }
        });
        btnSelect.disable();
        btnSelect.setVisible(false);
        this.addChild(btnSelect);

        btnInputWindow = new Button(Translation.Get("LeftWindow"));
        this.addChild(btnInputWindow);
        btnBothWindow = new Button(Translation.Get("BothWindow"));
        this.addChild(btnBothWindow);
        btnResultWindow = new Button(Translation.Get("RightWindow"));
        this.addChild(btnResultWindow);

        edInput = new EditTextField(this.name + " edInput");
        edInput.setWrapType(WrapType.MULTILINE);
        this.addChild(edInput);
        edResult = new EditTextField(this.name + " edResult");
        edResult.setWrapType(WrapType.MULTILINE);
        //edResult.disable();
        edResult.setEditable(false);
        edResult.dontShowSoftKeyBoardOnFocus(true);
        this.addChild(edResult);

        btnInputWindow.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                windowState = WindowState.Left;
                layoutEditFields(GL.that.getFocusedEditTextField());
                return true;
            }
        });

        btnBothWindow.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                windowState = WindowState.Both;
                layoutEditFields(GL.that.getFocusedEditTextField());
                return true;
            }
        });

        btnResultWindow.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                windowState = WindowState.Right;
                layoutEditFields(GL.that.getFocusedEditTextField());
                return true;
            }
        });

        edInput.addListPosChangedHandler(new IListPosChanged() {
            @Override
            public void listPosChanged() {
                GL.that.RunOnGL(new IRunOnGL() {
                    @Override
                    public void run() {
                        int aktInputLine = edInput.getTopLineNo();
                        int aktResultLine = edResult.getTopLineNo();
                        if (aktInputLine != aktResultLine)
                            edResult.showFromLineNo(aktInputLine);
                    }
                });
            }
        });

        edResult.addListPosChangedHandler(new IListPosChanged() {
            @Override
            public void listPosChanged() {
                GL.that.RunOnGL(new IRunOnGL() {
                    @Override
                    public void run() {
                        int aktInputLine = edInput.getTopLineNo();
                        int aktResultLine = edResult.getTopLineNo();
                        if (aktInputLine != aktResultLine)
                            edInput.showFromLineNo(aktResultLine);
                    }
                });
            }
        });
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

        solver = new Solver(edInput.getText());
        if (!solver.Solve()) {
            GL.that.Toast("Error", Toast.LENGTH_SHORT);
        }
        edResult.setText("");
        String result = "";
        for (SolverZeile zeile : solver) {
            result += zeile.Solution + "\n";
        }

        edResult.setText(result);
        edResult.showFromLineNo(edInput.getTopLineNo());
        edResult.setEditable(false);

        if ((solver.MissingVariables != null) && (solver.MissingVariables.size() > 0)) {
            // es sind nicht alle Variablen zugewiesen
            // Abfrage, ob die Deklarationen eingefügt werden sollen
            String message = "";
            for (String s : solver.MissingVariables.keySet()) {
                if (message != "")
                    message += ", ";
                message += s;
            }

            GL_MsgBox.Show(Translation.Get("insertVars") + "\n" + message, Translation.Get("missingVars"), MessageBoxButtons.YesNo, MessageBoxIcon.Asterisk, mDialogListener);
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
