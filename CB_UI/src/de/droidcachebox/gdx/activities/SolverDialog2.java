package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.graphics.Color;

import java.util.TreeMap;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_CheckBox.OnCheckChangedListener;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.CoordinateButton;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldListener;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.gdx.controls.MultiToggleButton.OnStateChangeListener;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.SelectSolverFunction;
import de.droidcachebox.gdx.views.SelectSolverFunction.IFunctionResult;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.solver.DataType;
import de.droidcachebox.solver.Function;
import de.droidcachebox.solver.SolverLines;
import de.droidcachebox.translation.Translation;

public class SolverDialog2 extends ActivityBase implements OnStateChangeListener {
    //private final SolverDialog2BuildFormula buildFormula;
    private final TreeMap<buttons, MultiToggleButton> visibleButtons = new TreeMap<buttons, MultiToggleButton>();
    private final Cache aktCache;
    private final String solverString;
    private final DataType dataType; // DataType of Parameter which must be returned
    private final ScrollBox scrollBox;
    // Page Zahl
    private final String[] lZahl = new String[]{"0", ",", "<-", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final SolverLines solverLines; // Solver Object dieses Caches für die Functions, Variablen...
    boolean doNotChangeCBVariable = false;
    private float visibleButtonsHeight = 0;
    private ISolverBackStringListener mBackStringListener;
    OnClickListener oclSolverString = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
            String param = "";
            final EditTextField tb = (EditTextField) view.getData();
            if (tb != null) {
                param = tb.getText();
            }
            SolverDialog2 sd2 = new SolverDialog2(aktCache, solverLines, param, false, DataType.Float);
            sd2.show(new ISolverBackStringListener() {
                @Override
                public void BackString(String backString) {
                    if (tb != null) {
                        tb.setText(backString);
                    }
                }
            });
            return true;
        }
    };
    private String sVar;
    private String sForm;
    private pages page;
    private CB_Button bOK, bCancel;
    private CB_Label lblTitle;
    private float innerLeft;
    private EditTextField mVariableField;
    private CB_Button bVariableWaypoint;
    private CB_Label lblGleich;
    // Page Text
    private EditTextField mFormulaField;
    private CB_CheckBox cbFormulaAsText;
    private CB_Label lFormulaAsText;
    private EditTextField tbZahl;
    private CB_Button[] bZahl;
    // Page Function
    private EditTextField tbFunction;
    private CB_Button bFunction;
    private EditTextField[] tbFunctionParam = null;
    private CB_Label[] lFunctionParam = null;
    private CB_Button[] bFunctionParam = null;
    // Page Variables
    private CB_CheckBox[] cbVariables = null;
    private CB_Label[] lVariables = null;
    // Page Operator
    private EditTextField[] tbOperator = null;
    private CB_Button[] bOperator = null;
    // Page Waypoint
    private CB_CheckBox[] cbWaypoints = null;
    private CB_Label[] lWaypoints = null;
    // Page Coordinate
    private CoordinateButton bCoord = null;

    public SolverDialog2(Cache aktCache, SolverLines solverLines, String solverString, boolean showVariableField, DataType dataType) {
        super("SolverDialog2");
        this.solverLines = solverLines;
        //this.buildFormula = null;
        this.solverString = solverString;
        this.aktCache = aktCache;
        this.dataType = dataType;
        // Split Solver String by =
        String[] solverStrings = solverString.split("=", 2);
        sVar = "";
        sForm = "";
        if (solverStrings.length == 1) {
            sForm = solverStrings[0];
        } else if (solverStrings.length > 1) {
            sVar = solverStrings[0];
            sForm = solverStrings[1];
        }

        page = pages.Nothing;
        scrollBox = new ScrollBox(this);
        this.addChild(scrollBox);
        createOkCancelBtn();
        createTitleLine();
        scrollBox.setHeight(lblTitle.getY() - bOK.getMaxY() - margin - margin);
        scrollBox.setY(bOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());

        if (showVariableField) {
            createVariableLines();
        }
        createButtonsLine();

        Layout();

        // removed: now the page Number should be shown when the right side of a formula is empty but only if a left side is available
        // if (sForm.length() > 0)
        // {
        // only show special page if sForm is not empty

        if (isFunction(sForm)) {
            showPage(pages.Function);
        } else if (isCoordinate(sForm)) {
            showPage(pages.Coordinate);
        } else if (isNumber(sForm)) {
            showPage(pages.Zahl);
        } else if (isWaypoint(sForm)) {
            showPage(pages.Waypoint);
        } else if (isVariable(sForm)) {
            showPage(pages.Variable);
        } else if (dataType == DataType.Waypoint) {
            showPage(pages.Waypoint);
        } else {
            showPage(pages.Text);
        }
        // }
        // else
        // {
        // showPage(pages.Text);
        // }
    }

    private boolean isVariable(String solverString2) {
        for (String var : solverLines.Variablen.keySet()) {
            if (solverString2.equalsIgnoreCase(var)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWaypoint(String string) {
        if (string.equals("&" + aktCache.getGeoCacheCode()))
            return true;
        for (int i = 0; i < aktCache.getWayPoints().size(); i++) {
            Waypoint waypoint = aktCache.getWayPoints().get(i);
            if (this.solverString.equals("$" + waypoint.getWaypointCode()))
                return true;
        }
        return false;
    }

    private boolean isNumber(String string) {
        if (this.solverString.length() == 0)
            return false;
        return isZahl(string);
    }

    private boolean isCoordinate(String string) {
        Coordinate coord = new Coordinate(string);
        return coord.isValid();
    }

    private boolean isFunction(String string) {
        // Funktion aufsplitten nach Funktionsname und Parameter (falls möglich!)
        String formula = string;
        formula.trim();
        int posKlammerAuf = formula.indexOf("(");
        int posKlammerZu = formula.lastIndexOf(")");
        if (posKlammerAuf <= 0)
            return false;
        if (posKlammerZu < posKlammerAuf)
            return false;
        if (posKlammerZu != formula.length() - 1)
            return false;
        // in eine gültigen Formel dürfen nur normale Buchstaben oder Zahlen stehen
        for (int i = 0; i < posKlammerAuf; i++) {
            char c = formula.charAt(i);
            if (!Character.isLetter(c) && !Character.isDigit(c))
                return false;
        }
        // gültige Formel erkannt anhand dem Format.
        return true;
    }

    public void show(ISolverBackStringListener listener) {
        mBackStringListener = listener;

        show();
    }

    private void createVariableLines() {
        innerLeft = margin;

        mVariableField = new EditTextField(this, "mVariableField");
        mVariableField.setX(innerLeft);
        mVariableField.setY(innerHeight - mVariableField.getHeight());
        mVariableField.setWidth(innerWidth - mVariableField.getHeight());
        scrollBox.addChild(mVariableField);
        mVariableField.setText(sVar);
        bVariableWaypoint = new CB_Button("$GC");
        bVariableWaypoint.setX(innerLeft + innerWidth - mVariableField.getHeight());
        bVariableWaypoint.setY(innerHeight - mVariableField.getHeight());
        bVariableWaypoint.setWidth(mVariableField.getHeight());
        scrollBox.addChild(bVariableWaypoint);
        bVariableWaypoint.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(final GL_View_Base view, int x, int y, int pointer, int button) {
                //String param = "";
                DataType type = DataType.Waypoint;
                SolverDialog2 sd2 = new SolverDialog2(aktCache, solverLines, mVariableField.getText(), false, type);
                sd2.show(new ISolverBackStringListener() {
                    @Override
                    public void BackString(String backString) {
                        mVariableField.setText(backString);
                    }
                });
                return true;
            }
        });

        lblGleich = new CB_Label("=");
        lblGleich.setWidth(innerWidth);
        lblGleich.setX(innerLeft);
        lblGleich.setHAlignment(HAlignment.CENTER);
        scrollBox.addChild(lblGleich);
    }

    private void createButtonsLine() {
        innerLeft = margin;

        visibleButtons.clear();
        // Buttons zur Auswahl des Dialog-Typs
        // nur die Buttons werden angezeigt, die auch den gewünschten DataType liefern können
        for (buttons btn : buttons.values()) {
            if (btn == buttons.Operator)
                continue; // Operator erstmal noch nicht anzeigen
            if ((dataType == null) || btn.hasDataType(dataType)) {
                MultiToggleButton button = new MultiToggleButton(0, 0, 0, UiSizes.getInstance().getButtonHeight(), btn.description);
                button.addState(btn.description, new HSV_Color(Color.GRAY));
                button.addState(btn.description, new HSV_Color(Color.GREEN));
                button.setOnStateChangedListener(this);
                visibleButtons.put(btn, button);
                scrollBox.addChild(button);
                visibleButtonsHeight = button.getHeight();
            }
        }
        float w = innerWidth / visibleButtons.size();
        float x = 0;
        for (MultiToggleButton btn : visibleButtons.values()) {
            btn.setX(x);
            btn.setWidth(w);
            x += w;
        }
    }

    private void Layout() {
        float y = margin + innerHeight - visibleButtonsHeight * 4;

        switch (page) {
            case Function:
                if ((lFunctionParam != null) && (tbFunctionParam != null) && (bFunctionParam != null)) {
                    for (int i = lFunctionParam.length - 1; i >= 0; i--) {
                        CB_Label l = lFunctionParam[i];
                        EditTextField tb = tbFunctionParam[i];
                        CB_Button b = bFunctionParam[i];
                        l.setY(y);
                        l.setWidth((float) (innerWidth * 0.3));
                        tb.setY(y);
                        tb.setX(l.getX() + l.getWidth());
                        tb.setWidth((float) (innerWidth * 0.6));
                        b.setY(y);
                        b.setX(tb.getX() + tb.getWidth());
                        b.setWidth((float) (innerWidth * 0.1));
                        y += tb.getHeight() + margin;
                    }
                }
                tbFunction.setY(y);
                bFunction.setY(y);
                tbFunction.setWidth((float) (innerWidth * 0.8));
                bFunction.setWidth((float) (innerWidth * 0.2));
                bFunction.setX(tbFunction.getX() + tbFunction.getWidth());
                y += bFunction.getHeight() + margin;
                break;
            case Nothing:
                break;
            case Operator:
                tbOperator[0].setY(y);
                tbOperator[0].setWidth((float) (innerWidth * 0.9));
                bOperator[0].setY(y);
                bOperator[0].setX(tbOperator[0].getX() + tbOperator[0].getWidth());
                bOperator[0].setWidth((float) (innerWidth * 0.1));
                y += tbOperator[0].getHeight();
                tbOperator[1].setY(y);
                tbOperator[1].setWidth((float) (innerWidth * 0.9));
                bOperator[1].setY(y);
                bOperator[1].setX(tbOperator[1].getX() + tbOperator[1].getWidth());
                bOperator[1].setWidth((float) (innerWidth * 0.1));
                y += tbOperator[1].getHeight();
                break;
            case Text:
                // layout missing Variables
                // if (buildFormula != null)
                // {
                // y = buildFormula.layout(y, innerLeft, innerWidth, margin);
                // }
                cbFormulaAsText.setY(y);
                lFormulaAsText.setY(y);
                lFormulaAsText.setX(cbFormulaAsText.getX() + cbFormulaAsText.getWidth());
                lFormulaAsText.setWidth(innerWidth - cbFormulaAsText.getWidth());
                y += cbFormulaAsText.getHeight() + margin;
                mFormulaField.setY(y);
                y += mFormulaField.getHeight() + margin;
                break;
            case Zahl:
                // 10er Tastatur
                float width = innerWidth / 3;
                for (int row = 0; row < 4; row++) {
                    for (int col = 0; col < 3; col++) {
                        int i = row * 3 + col;
                        bZahl[i].setY(y);
                        bZahl[i].setWidth(width);
                        bZahl[i].setX(width * col);
                    }
                    y += bZahl[0].getHeight() + margin;
                }
                tbZahl.setY(y);
                tbZahl.setWidth(innerWidth);
                y += tbZahl.getHeight() + margin;
                break;
            case Variable:
                for (int i = cbVariables.length - 1; i >= 0; i--) {
                    CB_CheckBox cb = cbVariables[i];
                    CB_Label l = lVariables[i];
                    cb.setY(y);
                    cb.setWidth(cb.getHeight());
                    l.setY(y);
                    l.setX(cb.getX() + cb.getWidth());
                    l.setWidth(innerHeight - cb.getWidth());
                    y += l.getHeight();
                }
                break;
            case Waypoint:
                for (int i = cbWaypoints.length - 1; i >= 0; i--) {
                    if ((dataType == DataType.Waypoint) && (i == 0)) {
                        continue;
                    }
                    CB_CheckBox cb = cbWaypoints[i];
                    CB_Label l = lWaypoints[i];
                    cb.setY(y);
                    cb.setWidth(cb.getHeight());
                    l.setY(y);
                    l.setX(cb.getX() + cb.getWidth());
                    l.setWidth(innerHeight - cb.getWidth());
                    y += l.getHeight();
                }
                break;
            case Coordinate:
                bCoord.setY(y);
                bCoord.setWidth(innerWidth);
                y += bCoord.getHeight() + margin;
                break;
            default:
                break;
        }

        for (MultiToggleButton mtb : visibleButtons.values()) {
            mtb.setY(y);
        }
        y += visibleButtonsHeight + margin;

        // tbGesamt.setY(y);
        // y += tbGesamt.getHeight();

        if (lblGleich != null) {
            lblGleich.setY(y - lblGleich.getHalfHeight() / 2);
            y += lblGleich.getHeight() - lblGleich.getHalfHeight();
        }
        if (bVariableWaypoint != null) {
            bVariableWaypoint.setY(y);
        }
        if (mVariableField != null) {
            mVariableField.setY(y);
            y += mVariableField.getHeight() + margin;
        }

        scrollBox.setVirtualHeight(y);

    }

    private void createOkCancelBtn() {
        bOK = new CB_Button(leftBorder, leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        bCancel = new CB_Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        // Translations
        bOK.setText(Translation.get("ok"));
        bCancel.setText(Translation.get("cancel"));

        this.addChild(bOK);
        bOK.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                // damit die Änderungen in sForm gespeichert werden
                saveAktPage();
                String result = "";
                if (mVariableField != null) {
                    result = mVariableField.getText();
                    if (result.length() > 0)
                        result += "=";
                }
                result += sForm;
                finish();
                if (mBackStringListener != null) {
                    final String finalResult = result;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBackStringListener.BackString(finalResult);
                        }
                    });
                    thread.start();
                }
                return true;
            }
        });

        this.addChild(bCancel);
        bCancel.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                finish();
                return true;
            }
        });

    }

    private void createTitleLine() {
        // Title+Progressbar

        float lineHeight = UiSizes.getInstance().getButtonHeight() * 0.75f;

        lblTitle = new CB_Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
        lblTitle.setFont(Fonts.getBig());
        lblTitle.setText(Translation.get("solver_formula")).getTextWidth();
        this.addChild(lblTitle);

    }

    @Override
    public void onStateChange(GL_View_Base v, int state) {

        pages newPage = null;
        // Statusänderung eines MultiToggleButtons
        if (state == 1) {
            // Werte der aktuellen Seite in den String sForm speichern
            saveAktPage();

            if (v == visibleButtons.get(buttons.Text)) {
                newPage = pages.Text;
            }
            if (v == visibleButtons.get(buttons.Zahl)) {
                if (isZahl(sForm)) {
                    newPage = pages.Zahl;
                }
            }
            if (v == visibleButtons.get(buttons.Function)) {
                if ((sForm.length() == 0) || (isFunction(sForm))) {
                    newPage = pages.Function;
                }
            }
            if (v == visibleButtons.get(buttons.Variable)) {
                if ((sForm.length() == 0) || isVariable(sForm)) {
                    newPage = pages.Variable;
                }
            }
            if (v == visibleButtons.get(buttons.Operator)) {
                newPage = pages.Operator;
            }
            if (v == visibleButtons.get(buttons.Waypoint)) {
                if ((sForm.length() == 0) || (isWaypoint(sForm))) {
                    newPage = pages.Waypoint;
                }
            }
            if (v == visibleButtons.get(buttons.Coordinate)) {
                if ((sForm.length() == 0) || (isCoordinate(sForm))) {
                    newPage = pages.Coordinate;
                }
            }

        }
        if (newPage != null) {
            if (checkDataType(newPage)) {
                showPage(newPage);
            } else {
                // Button State zurück setzen
                setButtonStates();
            }
        }
        // check states of visibleButtons
        setButtonStates();
    }

    // Werte der aktuellen Seite in den String sForm speichern
    private void saveAktPage() {
        switch (this.page) {
            case Coordinate:
                savePageCoordinate();
                break;
            case Function:
                savePageFunction();
                break;
            case Nothing:
                break;
            case Operator:
                // savePageOperator();
                break;
            case Text:
                savePageText();
                break;
            case Variable:
                savePageVariable();
                break;
            case Waypoint:
                savePageWaypoint();
                break;
            case Zahl:
                savePageZahl();
                break;
            default:
                break;

        }
    }

    private boolean checkDataType(pages p) {
        boolean valid = true;
        if (sForm.length() == 0)
            return true;
        switch (p) {
            case Coordinate:
                Coordinate c = new CoordinateGPS(sForm);
                valid = (c != null) && (c.isValid());
                break;
            case Function:
                break;
            case Nothing:
                break;
            case Operator:
                break;
            case Text:
                break;
            case Variable:
                break;
            case Waypoint:
                if (sForm.charAt(0) != '$')
                    valid = false;
                break;
            case Zahl:
                if (!isZahl(sForm))
                    valid = false;
                break;
            default:
                break;

        }
        return valid;

    }

    // überprüft für alle pages, ob der aktuell eingegebene String einen gültigen Wert für diese Page darstellt
	/*
	private void checkDataTypes() {
	for (pages p : pages.values()) {
	    checkDataType(p);
	}
	}
	*/

    private void showPage(pages page) {
        if (page == this.page)
            return;

        // remove old controls
        switch (this.page) {
            case Text:
                hidePageText();
                break;
            case Zahl:
                hidePageZahl();
                break;
            case Function:
                hidePageFunction();
                break;
            case Variable:
                hidePageVariable();
                break;
            case Operator:
                hidePageOperator();
                break;
            case Waypoint:
                hidePageWaypoint();
                break;
            case Coordinate:
                hidePageCoordinate();
                break;
            default:
                break;
        }

        this.page = page;
        // set State of buttons
        setButtonStates();

        switch (page) {
            case Text:
                showPageText();
                break;
            case Zahl:
                showPageZahl();
                break;
            case Function:
                showPageFunction();
                break;
            case Variable:
                showPageVariable();
                break;
            case Operator:
                showPageOperator();
                break;
            case Waypoint:
                showPageWaypoint();
                break;
            case Coordinate:
                showPageCoordinate();
                break;
            default:
                break;
        }
        Layout();
    }

    private void setButtonStates() {
        if (visibleButtons.get(buttons.Text) != null)
            visibleButtons.get(buttons.Text).setState(page == pages.Text ? 1 : 0);
        if (visibleButtons.get(buttons.Zahl) != null)
            visibleButtons.get(buttons.Zahl).setState(page == pages.Zahl ? 1 : 0);
        if (visibleButtons.get(buttons.Function) != null)
            visibleButtons.get(buttons.Function).setState(page == pages.Function ? 1 : 0);
        if (visibleButtons.get(buttons.Variable) != null)
            visibleButtons.get(buttons.Variable).setState(page == pages.Variable ? 1 : 0);
        if (visibleButtons.get(buttons.Operator) != null)
            visibleButtons.get(buttons.Operator).setState(page == pages.Operator ? 1 : 0);
        if (visibleButtons.get(buttons.Waypoint) != null)
            visibleButtons.get(buttons.Waypoint).setState(page == pages.Waypoint ? 1 : 0);
        if (visibleButtons.get(buttons.Coordinate) != null)
            visibleButtons.get(buttons.Coordinate).setState(page == pages.Coordinate ? 1 : 0);
    }

    private void hidePageText() {
        // if (buildFormula != null)
        // {
        // buildFormula.removeChilds(scrollBox);
        // }
        scrollBox.removeChild(mFormulaField);
        scrollBox.removeChild(cbFormulaAsText);
        scrollBox.removeChild(lFormulaAsText);
        mFormulaField = null;
        cbFormulaAsText = null;
        lFormulaAsText = null;
    }

    private void showPageText() {
        // buildFormula = new SolverDialog2BuildFormula(sForm);

        String text = sForm;
        boolean asText = false;
        if (text.length() >= 2) {
            if ((text.charAt(0) == '"') && (text.charAt(text.length() - 1) == '"')) {
                if (text.indexOf("\"", 1) == text.length() - 1) {
                    text = text.substring(1, text.length() - 1);
                    asText = true;
                }
            }
        }

        mFormulaField = new EditTextField(this, "mFormulaField");
        mFormulaField.setWrapType(WrapType.SINGLELINE);
        mFormulaField.setX(innerLeft);
        mFormulaField.setWidth(innerWidth);
        mFormulaField.setText(text);
        mFormulaField.setZeroPos();
        mFormulaField.setTextFieldListener(new TextFieldListener() {
            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {
            }

            @Override
            public void keyTyped(EditTextField textField, char key) {
            }
        });
        cbFormulaAsText = new CB_CheckBox();
        // cbFormulaAsText.setText("Als Text in \"\" eintragen");
        cbFormulaAsText.setChecked(asText);
        lFormulaAsText = new CB_Label("Als Text in \"\" eintragen");
        SolverLines solv = new SolverLines(sForm, GlobalCore.getInstance());
        if (solv.Solve()) {
            if (solv.MissingVariables != null) {
                for (String mv : solv.MissingVariables.keySet()) {
                    System.out.println(mv);
                }
            }
        }

        scrollBox.addChild(mFormulaField);
        scrollBox.addChild(cbFormulaAsText);
        scrollBox.addChild(lFormulaAsText);
        // buildFormula.addControls(scrollBox);
    }

    private void savePageText() {
        if (mFormulaField != null) {
            sForm = mFormulaField.getText();
        }
        if (cbFormulaAsText != null) {
            if (cbFormulaAsText.isChecked()) {
                sForm = "\"" + sForm + "\"";
            }
        }
    }

    private void showPageZahl() {
        tbZahl = new EditTextField(this, "tbZahl");
        tbZahl.disableKeyboardPopup();
        tbZahl.setText(sForm);
        scrollBox.addChild(tbZahl);
        bZahl = new CB_Button[12];
        for (int i = 0; i < 12; i++) {
            bZahl[i] = new CB_Button(lZahl[i]);
            bZahl[i].setData(i);
            scrollBox.addChild(bZahl[i]);
            if ((i == 1) && (dataType == DataType.Integer)) {
                // Integer erwartet -> kein Komma anzeigen
                bZahl[i].setVisible(false);
            }
            bZahl[i].setClickHandler(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                    Integer i = (Integer) view.getData();
                    if (i != null) {
                        if (i == 2) {
                            // Delete
                            String text = tbZahl.getText();
                            if (text.length() > 1) {
                                text = text.substring(0, text.length() - 1);
                            } else if (text.length() == 1) {
                                text = "";
                            }
                            // Resultierende Zahl überprüfen
                            if (isZahl(text)) {
                                tbZahl.setText(text);
                            }
                        } else {
                            String text = tbZahl.getText() + lZahl[i];
                            if (isZahl(text)) {
                                tbZahl.setText(text);
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    private boolean isZahl(String text) {
        if (text.length() == 0)
            return true;
        char sep = '.';
        text = text.replace('.', sep);
        text = text.replace(',', sep);
        try {
            @SuppressWarnings("unused")
            double zahl = Double.valueOf(text);
        } catch (Exception ex) {
            // Fehler, Zahl konnte nicht interpretiert werden -> keine Änderung!!!
            return false;
        }
        return true;
    }

    private void hidePageZahl() {
        scrollBox.removeChild(tbZahl);
        tbZahl = null;
        for (int i = 0; i < 12; i++) {
            scrollBox.removeChild(bZahl[i]);
        }
        bZahl = null;
    }

    private void savePageZahl() {
        if (tbZahl != null) {
            sForm = tbZahl.getText();
        }
    }

    private void showPageFunction() {

        tbFunction = new EditTextField(this, "tbFunction");
        tbFunction.setText(sForm);
        scrollBox.addChild(tbFunction);

        bFunction = new CB_Button("");
        bFunction.setText("F(x)");
        scrollBox.addChild(bFunction);

        // Funktion aufsplitten nach Funktionsname und Parameter (falls möglich!)
        String formula = sForm.trim();
        int posKlammerAuf = formula.indexOf("(");
        int posKlammerZu = formula.lastIndexOf(")");
        if ((posKlammerAuf >= 0) && (posKlammerZu > posKlammerAuf)) {
            // gültige Formel erkannt
            String function = formula.substring(0, posKlammerAuf);
            tbFunction.setText(function);
            String parameter = formula.substring(posKlammerAuf + 1, posKlammerZu);
            // Parameter nach ";" trennen
            String[] parameters = parameter.split(";");

            tbFunctionParam = new EditTextField[parameters.length];
            lFunctionParam = new CB_Label[parameters.length];
            bFunctionParam = new CB_Button[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                addFunctionParamLine(function, i, parameters[i].trim());
            }
        }
        bFunction.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                // Funktionsauswahl zeigen
                SelectSolverFunction ssf = new SelectSolverFunction(solverLines, dataType, new IFunctionResult() {
                    @Override
                    public void selectedFunction(Function function) {
                        if (function == null)
                            return;
                        tbFunction.setText(function.getLongLocalName());
                        // evtl. vorhandene Parameter-Eingaben entfernen
                        removeFunctionParam();
                        tbFunctionParam = new EditTextField[function.getAnzParam()];
                        lFunctionParam = new CB_Label[function.getAnzParam()];
                        bFunctionParam = new CB_Button[function.getAnzParam()];
                        for (int i = 0; i < function.getAnzParam(); i++) {
                            addFunctionParamLine(function, i, "");
                        }
                        Layout();
                    }
                });
                GL.that.showDialog(ssf);
                return true;
            }
        });
    }

    private void addFunctionParamLine(final Function function, int i, String string) {
        String paramName = "Parameter " + i;
        if (function != null) {
            paramName = Translation.get(function.getParamName(i));
        }
        // Eingabefelder für die Parameter einfügen
        lFunctionParam[i] = new CB_Label();
        lFunctionParam[i].setText(paramName);
        scrollBox.addChild(lFunctionParam[i]);

        tbFunctionParam[i] = new EditTextField(SolverDialog2.this, "tbFunctionParam[" + i + "]");
        tbFunctionParam[i].setText(string);
        scrollBox.addChild(tbFunctionParam[i]);

        bFunctionParam[i] = new CB_Button("..");
        scrollBox.addChild(bFunctionParam[i]);
        bFunctionParam[i].setData(i);
        bFunctionParam[i].setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(final GL_View_Base view, int x, int y, int pointer, int button) {
                String param = "";
                DataType type = DataType.None;
                Integer i = (Integer) view.getData();
                if (i != null) {
                    param = tbFunctionParam[i].getText();
                    if (function != null) {
                        type = function.getParamType(i);
                    }
                }
                SolverDialog2 sd2 = new SolverDialog2(aktCache, solverLines, param, false, type);
                sd2.show(new ISolverBackStringListener() {
                    @Override
                    public void BackString(String backString) {
                        Integer i = (Integer) view.getData();
                        if (i != null) {
                            tbFunctionParam[i].setText(backString);
                        }
                    }
                });
                return true;
            }
        });
    }

    private void addFunctionParamLine(String functionString, int i, String string) {
        Function function = solverLines.functions.getFunction(functionString);
        addFunctionParamLine(function, i, string);
    }

	/*
	private DataType getFunctionDataType(String functionName) {
	Function function = solver.functions.getFunction(functionName);
	if (function == null) {
	    return DataType.None;
	} else {
	    return function.getReturnType();
	}
	}
	*/

    private void hidePageFunction() {
        // Parameter entfernen
        removeFunctionParam();
        scrollBox.removeChild(tbFunction);
        scrollBox.removeChild(bFunction);
        tbFunction = null;
        bFunction = null;
    }

    private void savePageFunction() {
        if (tbFunction == null) {
            return;
        }
        // geänderte Formel merken
        sForm = tbFunction.getText();
        if (sForm.length() > 0) {
            sForm += "(";
            if (tbFunctionParam != null) {
                for (int i = 0; i < tbFunctionParam.length; i++) {
                    if (i > 0)
                        sForm += "; ";
                    sForm += tbFunctionParam[i].getText();
                }
            }
            sForm += ")";
        }
    }

    private void removeFunctionParam() {
        if (tbFunctionParam != null) {
            for (int i = 0; i < tbFunctionParam.length; i++) {
                scrollBox.removeChild(tbFunctionParam[i]);
            }
            tbFunctionParam = null;
        }
        if (lFunctionParam != null) {
            for (int i = 0; i < lFunctionParam.length; i++) {
                scrollBox.removeChild(lFunctionParam[i]);
            }
            lFunctionParam = null;
        }
        if (bFunctionParam != null) {
            for (int i = 0; i < bFunctionParam.length; i++) {
                scrollBox.removeChild(bFunctionParam[i]);
            }
            bFunctionParam = null;
        }
    }

    private void showPageVariable() {
        cbVariables = new CB_CheckBox[solverLines.Variablen.size()];
        lVariables = new CB_Label[solverLines.Variablen.size()];
        int i = 0;
        for (String variable : solverLines.Variablen.keySet()) {
            String value = solverLines.Variablen.get(variable);
            cbVariables[i] = new CB_CheckBox();
            cbVariables[i].setData(variable);
            scrollBox.addChild(cbVariables[i]);
            lVariables[i] = new CB_Label(variable + " (" + value + ")");
            scrollBox.addChild(lVariables[i]);
            cbVariables[i].setChecked(sForm.equalsIgnoreCase(variable));
            cbVariables[i].setOnCheckChangedListener(new OnCheckChangedListener() {
                @Override
                public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
                    if (doNotChangeCBVariable)
                        return;
                    doNotChangeCBVariable = true;
                    for (CB_CheckBox cb : cbVariables) {
                        cb.setChecked(cb == view);
                    }
                    doNotChangeCBVariable = false;
                }
            });
            i++;
        }
    }

    private void hidePageVariable() {
        for (CB_Label l : lVariables) {
            scrollBox.removeChild(l);
        }
        for (CB_CheckBox cb : cbVariables) {
            scrollBox.removeChild(cb);
        }
        cbVariables = null;
        lVariables = null;
    }

    private void savePageVariable() {
        if (cbVariables == null)
            return;
        for (CB_CheckBox cb : cbVariables) {
            if (cb.isChecked()) {
                String variable = (String) cb.getData();
                if (variable != null) {
                    sForm = variable;
                }
            }
        }

    }

    private void showPageOperator() {
        tbOperator = new EditTextField[2];
        bOperator = new CB_Button[2];
        for (int i = 0; i < 2; i++) {
            tbOperator[i] = new EditTextField(this, "tbOperator[" + i + "]");
            scrollBox.addChild(tbOperator[i]);
            bOperator[i] = new CB_Button("..");
            scrollBox.addChild(bOperator[i]);
            bOperator[i].setData(tbOperator[i]);
            bOperator[i].setClickHandler(oclSolverString);
        }
    }

    private void hidePageOperator() {
        sForm = tbOperator[0].getText() + "+" + tbOperator[1].getText();
        for (int i = 0; i < 2; i++) {
            scrollBox.removeChild(tbOperator[i]);
            scrollBox.removeChild(bOperator[i]);
        }
        tbOperator = null;
        bOperator = null;
    }

    private void showPageWaypoint() {
        cbWaypoints = new CB_CheckBox[aktCache.getWayPoints().size() + 1];
        lWaypoints = new CB_Label[aktCache.getWayPoints().size() + 1];

        for (int i = 0; i <= aktCache.getWayPoints().size(); i++) {
            if ((dataType == DataType.Waypoint) && (i == 0)) {
                continue;
            }
            Waypoint waypoint = null;
            String data = "";
            String description = "";
            if (i > 0) {
                waypoint = aktCache.getWayPoints().get(i - 1);
                data = "$" + waypoint.getWaypointCode();
                description = "$" + waypoint.getWaypointCode() + " - " + waypoint.getTitle();
            } else {
                data = "$" + aktCache.getGeoCacheCode();
                description = "$" + aktCache.getGeoCacheCode() + " - " + aktCache.getGeoCacheName();
            }

            cbWaypoints[i] = new CB_CheckBox();
            cbWaypoints[i].setData(data);
            scrollBox.addChild(cbWaypoints[i]);
            lWaypoints[i] = new CB_Label(description);
            scrollBox.addChild(lWaypoints[i]);
            cbWaypoints[i].setChecked(sForm.equalsIgnoreCase(data));

            cbWaypoints[i].setOnCheckChangedListener(new OnCheckChangedListener() {
                @Override
                public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
                    if (doNotChangeCBVariable)
                        return;
                    doNotChangeCBVariable = true;
                    for (CB_CheckBox cb : cbWaypoints) {
                        cb.setChecked(cb == view);
                    }
                    doNotChangeCBVariable = false;
                }
            });
        }
    }

    private void hidePageWaypoint() {
        if (cbWaypoints == null)
            return;
        for (CB_CheckBox cb : cbWaypoints) {
            scrollBox.removeChild(cb);
        }
        for (CB_Label l : lWaypoints) {
            scrollBox.removeChild(l);
        }
        cbWaypoints = null;
        lWaypoints = null;
    }

    private void savePageWaypoint() {
        if (cbWaypoints == null)
            return;
        for (CB_CheckBox cb : cbWaypoints) {
            if (cb == null)
                continue;
            // scrollBox.removeChild(cb);
            if (cb.isChecked()) {
                String waypoint = (String) cb.getData();
                if (waypoint != null) {
                    sForm = waypoint;
                }
            }
        }
    }

    private void showPageCoordinate() {
        bCoord = new CoordinateButton("Coordinate");
        bCoord.setCoordinate(new CoordinateGPS(sForm));
        scrollBox.addChild(bCoord);
    }

    private void hidePageCoordinate() {
        scrollBox.removeChild(bCoord);
        bCoord = null;
    }

    private void savePageCoordinate() {
        if (bCoord == null)
            return;
        if (bCoord.getCoordinate().isValid()) {
            sForm = "\"" + bCoord.getCoordinate().formatCoordinate() + "\"";
        }
    }

    private enum pages {
        Nothing, Text, Zahl, Function, Variable, Operator, Waypoint, Coordinate
    }

    private enum buttons {
        Text(pages.Text, "TXT"), Zahl(pages.Zahl, "123"), Function(pages.Function, "f(x)"), Variable(pages.Variable, "@"), Operator(pages.Operator, "+-*/"), Waypoint(pages.Waypoint, "$GC"), Coordinate(pages.Coordinate, "°");
        public String description;
        private pages page;

        private buttons(pages page, String description) {
            this.page = page;
            this.description = description;
        }

        public boolean hasDataType(DataType dataType) {
            if (dataType == DataType.None)
                return true; // wenn kein spezieller DataType angegeben ist -> alle Pages anzeigen
            if (dataType == DataType.String)
                return true; // alles kann als String zurück gegeben werden
            switch (page) {
                case Coordinate:
                    return dataType == DataType.Coordinate;
                case Function:
                    if (dataType != DataType.Waypoint) {
                        return true;
                    }
                    break;
                case Nothing:
                    if (dataType != DataType.Waypoint) {
                        return true;
                    }
                    break;
                case Operator:
                    return (dataType == DataType.Integer) || (dataType == DataType.Float);
                case Text:
                    if (dataType != DataType.Waypoint) {
                        return true;
                    }
                    break;
                case Variable:
                    if (dataType != DataType.Waypoint) {
                        return true;
                    }
                    break;
                case Waypoint:
                    return (dataType == DataType.Coordinate) || (dataType == DataType.Waypoint);
                case Zahl:
                    return (dataType == DataType.Integer) || (dataType == DataType.Float);
                default:
                    break;

            }
            return false;
        }
    }

    public interface ISolverBackStringListener {
        void BackString(String backString);
    }
}
