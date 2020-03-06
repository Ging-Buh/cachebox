package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.Color;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.CollapseBox.IAnimatedHeightChangedListener;
import de.droidcachebox.gdx.controls.Linearlayout.LayoutChanged;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.solver.DataType;
import de.droidcachebox.solver.Function;
import de.droidcachebox.solver.Functions;
import de.droidcachebox.solver.Solver;
import de.droidcachebox.translation.Translation;

import java.util.ArrayList;
import java.util.Iterator;

public class SelectSolverFunction extends ButtonDialog {
    private final IFunctionResult mResultListener;
    private final CB_RectF categoryBtnRec, itemBtnRec;
    private final DataType dataType;
    private final Solver solver;
    private CB_Label desc;
    private ScrollBox scrollBox;
    private Linearlayout mLinearLayout;
    private Function selectedFunction;

    public SelectSolverFunction(Solver solver, DataType dataType, IFunctionResult resultListener) {
        super(ActivityRec(), "SelectSolverFunctionActivity", "", "", MessageBoxButton.OKCancel, MessageBoxIcon.None, null);
        this.solver = solver;
        mResultListener = resultListener;
        this.dataType = dataType;

        categoryBtnRec = new CB_RectF(leftBorder, 0, innerWidth - leftBorder - rightBorder, UiSizes.getInstance().getButtonHeight());

        itemBtnRec = new CB_RectF(leftBorder, 0, categoryBtnRec.getWidth() - leftBorder - rightBorder, UiSizes.getInstance().getButtonHeight());

        // Initialisiert die unteren Buttons für Ok/Cancel
        iniOkCancel();

        // Über den Buttons liegt ein Wrapped Label, welches die Beschreibeung der Selectierten Function anzeigt
        iniDescLabel();

        // Initialisieren der Controls für die Function List
        iniFunctionList();

        // jetzt sind alle Controls initialisiert und wir können Die Liste mit den Funktionen Füllen
        fillContent();

    }

    public static CB_RectF ActivityRec() {
        float w = Math.min(UiSizes.getInstance().getSmallestWidth(), UiSizes.getInstance().getWindowHeight() * 0.66f);

        return new CB_RectF(0, 0, w, (int) (UiSizes.getInstance().getWindowHeight() * 0.95));
    }

    private void iniOkCancel() {

        btnLeftPositive.setText(Translation.get("ok"));
        btnLeftPositive.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                if (mResultListener != null) {
                    try {
                        mResultListener.selectedFunction(selectedFunction);
                    } catch (NullPointerException e) {
                        throw new IllegalArgumentException("Der Returnlistener kann hier die Rückgabe von NULL nicht verarbeiten!");
                    }
                }
                GL.that.closeDialog(SelectSolverFunction.this);
                return true;
            }
        });
        btnRightNegative.setText(Translation.get("cancel"));
        btnRightNegative.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                if (mResultListener != null)
                    try {
                        mResultListener.selectedFunction(null);
                    } catch (NullPointerException e) {
                        throw new IllegalArgumentException("Der Returnlistener kann hier die Rückgabe von NULL nicht verarbeiten!");
                    }
                GL.that.closeDialog(SelectSolverFunction.this);
                return true;
            }
        });
    }

    private void iniDescLabel() {
        // rechteck für Label erstellen
        CB_RectF rec = new CB_RectF(0, this.getBottomHeight(), this.getWidth(), UiSizes.getInstance().getButtonHeight() * 1.5f);

        desc = new CB_Label(rec);

        // das Beschreibungs Label erhällt auch den BackGround der Activity.
        // Damit haben alle Bereiche der Activity den Selben Rahmen, dies Wirkt aufgeräumter
        desc.setBackground(this.getBackground());

        this.addChild(desc);
    }

    private void iniFunctionList() {
        // rechteck für die List erstellen.
        // diese ergibt sich aus dem Platzangebot oberhalb des desc Labels
        CB_RectF rec = new CB_RectF(0, desc.getMaxY(), desc.getWidth(), this.getHeight() - desc.getMaxY() - mFooterHeight);

        // Die Einträge der Function List werden aber nicht in einer ListView dargestellt, sondern werden in ein LinearLayout von oben nach
        // unten geschrieben.
        //
        // Dieses LinearLayout wird dann in eine ScrollBox verpackt, damit dies Scrollbar ist, wenn die Länge den Anzeige Bereich
        // überschreitet!
        scrollBox = new ScrollBox(rec);

        // damit die Scrollbox auch Events erhällt
        scrollBox.setClickable(true);

        // die ScrollBox erhält den Selben Hintergrund wie die Activity und wird damit ein wenig abgegrenzt von den Restlichen Controls
        scrollBox.setBackground(this.getBackground());

        // Initial LinearLayout
        // Dieses wird nur mit der Breite Initialisiert, die Höhe ergibt sich aus dem Inhalt
        mLinearLayout = new Linearlayout(categoryBtnRec.getWidth(), "SelectSolverFunction-LinearLayout");

        // damit das LinearLayout auch Events erhällt
        mLinearLayout.setClickable(true);

        mLinearLayout.setZeroPos();

        // hier setzen wir ein LayoutChanged Listener, um die innere Höhe der ScrollBox bei einer Veränderung der Höhe zu setzen!
        mLinearLayout.setLayoutChangedListener(new LayoutChanged() {
            @Override
            public void LayoutIsChanged(Linearlayout linearLayout, float newHeight) {
                mLinearLayout.setZeroPos();
                scrollBox.setVirtualHeight(newHeight);
            }
        });

        // add LinearLayout zu ScrollBox und diese zu der Activity
        scrollBox.addChild(mLinearLayout);
        this.addChild(scrollBox);

    }

    private void fillContent() {

        /**
         * in dieser liste sind alle Function Buttons enthalten! diese wird benötigt, um hier den Zustand der Buttons ändern zu können. wenn
         * ein Button selectiert wurde müssen alle anderen deselectiert werden.
         */
        final ArrayList<CB_Button> functBtnList = new ArrayList<CB_Button>();

        Iterator<Functions> iteratorCat = solver.functions.values().iterator();

        if (iteratorCat != null && iteratorCat.hasNext()) {
            do {
                Functions cat = iteratorCat.next();

                // erstelle Category Button
                final CB_Button categoryButton = new CB_Button(categoryBtnRec, "Btn-" + cat.getName());
                categoryButton.setText(Translation.get(cat.getName()));

                // alle Buttons müssen das Atribut Dragable habe, da sie sich in einer Dragable View befinden.
                categoryButton.setDraggable();

                // Category Button Gelb einfärben, damit sie sich von den Function Buttons unterscheiden
                categoryButton.setColorFilter(new Color(1f, 0.8f, 0.0f, 1));

                // erstelle Category Box
                final LinearCollapseBox lay = new LinearCollapseBox(categoryBtnRec, "CollabsBox-" + cat.getName());

                // die CollapseBox mit einem Rahmen versehen
                lay.setBackground(this.getBackground());

                lay.setClickable(true);

                // Zähler für die Anzahl der Funktionen, die zu dieser CollabsBox hinzugefügt wurden.
                // Dies wird dazu benutzt, um zu entscheiden, ob die Category vielleicht keine Einträge hat und garnicht in der Liste
                // erscheinen soll.
                int EntryCount = 0;

                Iterator<Function> iteratorFunctions = cat.iterator();
                if (iteratorFunctions != null && iteratorFunctions.hasNext()) {
                    do {
                        // erstelle einzelnen Funktions Button

                        final Function fct = iteratorFunctions.next();
                        if (!fct.returnsDataType(dataType)) {
                            continue;
                        }
                        final CB_Button btnFct = new CB_Button(itemBtnRec, "FunctionBtn-" + fct.getName());

                        // den Function Button der algemeinen Liste hinzufügen
                        functBtnList.add(btnFct);

                        // setze Button Text
                        btnFct.setText(fct.getName());

                        // alle Buttons müssen das Atribut Dragable habe, da sie sich in einer Dragable View befinden.
                        btnFct.setDraggable();

                        // Wenn Der Button geclickt wurd, wird dieser als Selecktiert Markiert
                        btnFct.setClickHandler(new OnClickListener() {

                            @Override
                            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                                // ColorFilter aller Buttons zurück setzen
                                Iterator<CB_Button> btnIterator = functBtnList.iterator();
                                do {
                                    btnIterator.next().clearColorFilter();
                                } while (btnIterator.hasNext());

                                // setze für diesen Button den ColorFilter als selected Markierung
                                btnFct.setColorFilter(new Color(1f, 0.5f, 0.5f, 1));

                                // Schreibe die Funktions Beschreibung in das Desc Label
                                desc.setWrappedText(fct.getDescription());

                                selectedFunction = fct;

                                // hier muss einmal gerendert werden, damit die Änderungen übernommen werden
                                GL.that.renderOnce();

                                return false;
                            }
                        });

                        // den Function Button der Collapse Box hinzufügen
                        lay.addChild(btnFct);
                        EntryCount++; // Den Function Zähler erhöhen;

                    } while (iteratorFunctions.hasNext());
                }

                // Nur wenn die Anzahl der Einträge größer 0 ist, erscheinen Category Buttn und CollbaseBox in dem LinearLayout
                if (EntryCount > 0) {

                    // CategoryButton und CollabsBox werden beide auf Position 0,0 gesetzt, da,it sie richtig angeordnet werden können
                    categoryButton.setZeroPos();
                    lay.setZeroPos();

                    // Den Category Button zum LinearLayout hinzufügen mit einem normalen Abstand zum darüberliegendem Control
                    mLinearLayout.addChild(categoryButton, margin);

                    // die Collapse noch Schliessen, bevor sie zum LinearLayout hinzugefügt wird.
                    // Da wir hier aber keine Animation haben wollen, setzen wir die AnimationsHöhe auf null und rufen nicht die Methode
                    // Collapse() auf, da diese eine Animation starten würde.
                    lay.setAnimationHeight(0f);

                    // Die mit den Functions Buttons gefüllte CollapseBox zum LinearLayout hinzufügen mit keinem Abstand zum
                    // darüberliegendem
                    // Category Button!
                    mLinearLayout.addChild(lay, margin);

                    // Wenn die CollapseBox ihre größe verändert, muss dies noch dem LinearLayout mitgeteilt werden und auch der ScrollBox,
                    // dass sich die innere Höhe geändert hat!
                    lay.setAnimationListener(new IAnimatedHeightChangedListener() {
                        @Override
                        public void animatedHeightChanged(float Height) {
                            mLinearLayout.layout();

                            // mLinearLayout.setZeroPos();
                            // scrollBox.setVirtualHeight(mLinearLayout.getHeight());
                        }
                    });

                    // Bei einem Click auf dem Category Button wird die darunterliegende CollabsBox geöfnet oder geschlossen
                    categoryButton.setClickHandler(new OnClickListener() {
                        @Override
                        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                            lay.Toggle();
                            return false;
                        }
                    });
                }

            } while (iteratorCat.hasNext());

        }
    }

    public interface IFunctionResult {
        public void selectedFunction(Function function);
    }

}
