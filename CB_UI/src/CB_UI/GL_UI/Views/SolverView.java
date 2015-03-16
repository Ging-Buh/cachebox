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

import org.slf4j.LoggerFactory;

import CB_Core.DB.Database;
import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Solver.Functions.Function;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.SelectSolverFunction;
import CB_UI.GL_UI.Activitys.SelectSolverFunction.IFunctionResult;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextField.IListPosChanged;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.List.IScrollbarParent;
import CB_UI_Base.GL_UI.Controls.List.Scrollbar;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Plattform;

/**
 * 
 * @author Longri
 *
 */
public class SolverView extends CB_View_Base {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(SolverView.class);

    private WindowState windowState = WindowState.Both;
    private Solver solver = new Solver("");
    private boolean mustLoadSolver;
    private Cache aktCache;

    private Button btnSolve, btnFunct, btnSelect, btnInputWindow, btnBothWindow, btnSolveWindow;
    private EditTextField edSolver, edResult;
    private Scrollbar scrollBar;

    private class ScrollBarParent implements IScrollbarParent {

	@Override
	public CB_View_Base getView() {
	    return edSolver;
	}

	@Override
	public boolean isDragable() {
	    return true;
	}

	@Override
	public float getScrollPos() {
	    return -edSolver.getScrollPos();
	}

	@Override
	public float getAllListSize() {
	    return edSolver.getTextHeight();
	}

	@Override
	public void setListPos(float value) {
	    edSolver.setScrollPos(-value);
	    edResult.setScrollPos(-value);
	}

	@Override
	public float getFirstItemSize() {
	    return edSolver.getLineHeight();
	}

	@Override
	public float getLasstItemSize() {
	    return edSolver.getLineHeight();
	}

	@Override
	public void chkSlideBack() {
	    //do nothing 
	}

    }

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

    public void SetSelectedCache(Cache cache, Waypoint waypoint) {
	if (aktCache != cache) {
	    mustLoadSolver = true;
	    aktCache = cache;
	}
    }

    public void onShow() {

	SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

	if (aktCache == null)
	    return;

	if (mustLoadSolver) {

	    String sol = Database.GetSolver(aktCache);
	    if (sol == null)
		sol = "";
	    edSolver.setText(sol);
	    mustLoadSolver = false;
	    edSolver.setScrollPos(0);
	}
    }

    @Override
    public void onHide() {
	// Save changed Solver text
	if (aktCache != null) {
	    Database.SetSolver(aktCache, edSolver.getText().toString());
	    // When Solve 1 changes -> Solver 2 must reload the information from DB to get the changes from Solver 1
	    aktCache.setSolver1Changed(true);
	}
    }

    @Override
    public void onResized(CB_RectF rec) {
	super.onResized(rec);
	layout();
    }

    @Override
    protected void Initial() {

    }

    @Override
    protected void SkinIsChanged() {

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
	btnFunct.setOnClickListener(functClicked);
	this.addChild(btnFunct);
	btnSelect = new Button(Translation.Get("Select."));
	btnSelect.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		GL.that.setKeyboardFocus(null);
		return true;
	    }
	});
	this.addChild(btnSelect);

	btnInputWindow = new Button(Translation.Get("LeftWindow"));
	this.addChild(btnInputWindow);
	btnBothWindow = new Button(Translation.Get("BothWindow"));
	this.addChild(btnBothWindow);
	btnSolveWindow = new Button(Translation.Get("RightWindow"));
	this.addChild(btnSolveWindow);

	edSolver = new EditTextField();
	edSolver.setWrapType(WrapType.MULTILINE);
	this.addChild(edSolver);
	edResult = new EditTextField();
	edResult.setWrapType(WrapType.MULTILINE);
	edResult.disable();
	edResult.dontShowSoftKeyBoardOnFocus(true);
	this.addChild(edResult);

	btnInputWindow.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		windowState = WindowState.Left;
		layoutEditFields(GL.that.getKeyboardFocus());
		return true;
	    }
	});

	btnBothWindow.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		windowState = WindowState.Both;
		layoutEditFields(GL.that.getKeyboardFocus());
		return true;
	    }
	});

	btnSolveWindow.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		windowState = WindowState.Right;
		layoutEditFields(GL.that.getKeyboardFocus());
		return true;
	    }
	});

	ScrollBarParent scrollBarControl = new ScrollBarParent();
	scrollBar = new Scrollbar(scrollBarControl);
	scrollBar.setSliderAlwaysVisible(true);

	edSolver.addListPosChangedEventHandler(new IListPosChanged() {
	    @Override
	    public void ListPosChanged() {
		scrollBar.ScrollPositionChanged();
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			if (edSolver.getMeasuredHeight() > edSolver.getHeight())
			    edResult.setScrollPos(edSolver.getScrollPos());
		    }
		});
	    }
	});
	edResult.addListPosChangedEventHandler(new IListPosChanged() {
	    @Override
	    public void ListPosChanged() {
		scrollBar.ScrollPositionChanged();
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			if (edSolver.getMeasuredHeight() > edSolver.getHeight())
			    edSolver.setScrollPos(edResult.getScrollPos());
		    }
		});
	    }
	});

	this.addChild(scrollBar);
    }

    private OnClickListener functClicked = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
	    SelectSolverFunction ssf = new SelectSolverFunction(solver, DataType.None, new IFunctionResult() {

		@Override
		public void selectedFunction(final Function function) {
		    // ausgewählte Funktion verarbeiten!
		    // wenn funktion==null wurde Cancel gedrückt

		    if (function != null) {

			CharSequence selection = edSolver.getSelectedText();

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
			int newSelectionStart = edSolver.getSelectionStart() + newFunctionLength + 1 + zeichen.length() + selection.length();

			int start = edSolver.getSelectionStart();
			int end = edSolver.getSelectionEnd();

			StringBuilder sb = new StringBuilder();
			String actText = edSolver.getText();

			sb.append(actText.subSequence(0, start));
			sb.append(newFunction);
			sb.append(actText.subSequence(end, actText.length()));
			edSolver.setText(sb.toString());
			edSolver.setCursorPosition(newSelectionStart);
			edSolver.setFocus();
		    }
		}
	    });
	    GL.that.showDialog(ssf);
	    return true;
	}
    };

    private void layout() {

	float btnWidth = this.getWidth() / 3;
	float yPos = this.getHeight() - btnSolve.getHeight();

	btnSolve.setWidth(btnWidth);
	btnFunct.setWidth(btnWidth);
	btnSelect.setWidth(btnWidth);
	btnInputWindow.setWidth(btnWidth);
	btnBothWindow.setWidth(btnWidth);
	btnSolveWindow.setWidth(btnWidth);

	//first Line
	btnSolve.setPos(0, yPos);
	btnFunct.setPos(btnWidth, yPos);
	btnSelect.setPos(btnWidth * 2, yPos);

	//bottom Line
	yPos = 0;
	btnInputWindow.setPos(0, yPos);
	btnBothWindow.setPos(btnWidth, yPos);
	btnSolveWindow.setPos(btnWidth * 2, yPos);

	layoutEditFields(GL.that.getKeyboardFocus());

    }

    private enum WindowState {
	Left, Both, Right
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

	if (Plattform.used == Plattform.Android && focus == edSolver) {
	    y = this.getHalfHeight();
	    editHeight = btnSolve.getY() - y;
	}

	edSolver.set(0, y, widthLeft, editHeight);
	edResult.set(widthLeft, y, this.getWidth() - widthLeft, editHeight);

	float slW = scrollBar.getSliderWidth();
	float psW = scrollBar.getPushSliderWidth();
	scrollBar.set(widthLeft, y, widthLeft, editHeight);
	scrollBar.set(widthLeft - ((slW / 2) + psW), y, widthLeft, editHeight);

    }

    protected void solve() {
	// Hide Keyboard when Calculating
	// showVirturalKeyboard(false);

	edResult.enable();

	solver = new Solver(edSolver.getText().toString());
	if (!solver.Solve()) {
	    GL.that.Toast("Error", Toast.LENGTH_SHORT);
	}
	edResult.setText("");
	String result = "";
	for (SolverZeile zeile : solver) {
	    result += zeile.Solution + "\n";
	}

	edResult.setText(result);
	edResult.disable();
	scrollBar.ScrollPositionChanged();
	GL.that.RunOnGL(new IRunOnGL() {

	    @Override
	    public void run() {
		edResult.setScrollPos(edSolver.getScrollPos());
	    }
	});

	if ((solver.MissingVariables != null) && (solver.MissingVariables.size() > 0)) {
	    // es sind nicht alle Variablen zugewiesen
	    // Abfrage, ob die Deklarationen eingefügt werden sollen
	    String message = "";
	    for (String s : solver.MissingVariables.keySet()) {
		if (message != "")
		    message += ", ";
		message += s;
	    }

	    GL_MsgBox.Show(Translation.Get("insertVars") + "\n" + message, Translation.Get("missingVars"), MessageBoxButtons.YesNo, MessageBoxIcon.Asterisk, DialogListner);
	}
    }

    private final OnMsgBoxClickListener DialogListner = new OnMsgBoxClickListener() {
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
			edResult.setText("\n" + edSolver.getText().toString());
		    }
		    edSolver.setText(missing + edSolver.getText().toString());
		}
		return true;
	    }
	}

    };

}
