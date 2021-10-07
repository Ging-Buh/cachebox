package de.droidcachebox.gdx.controls.dialogs;

import java.util.ArrayList;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.solver.Solver;

public class SolverDialog2BuildFormula {
    private final String sForm;
    private final Solver solver;
    private final ArrayList<CB_Label> labels;

    public SolverDialog2BuildFormula(String sForm) {
        this.sForm = sForm;
        solver = new Solver(sForm, GlobalCore.getInstance());
        solver.Solve();
        labels = new ArrayList<CB_Label>();
    }

    public void addControls(ScrollBox scrollBox) {
        if (solver.MissingVariables != null) {
            for (String mv : solver.MissingVariables.keySet()) {
                CB_Label l = new CB_Label(mv);
                scrollBox.addChild(l);
                labels.add(l);
            }
        }

    }

    public float Layout(float y, float innerLeft, float innerWidth, float margin) {
        for (int i = labels.size() - 1; i >= 0; i--) {
            CB_Label label = labels.get(i);
            label.setX(innerLeft);
            label.setWidth(innerWidth);
            label.setY(y);
            y += label.getHeight() + margin;
        }
        return y;
    }

    public void removeChilds(ScrollBox scrollBox) {
        for (CB_Label l : labels) {
            scrollBox.removeChild(l);
        }
    }
}
