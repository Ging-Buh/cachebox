package CB_UI.GL_UI.Controls.Dialogs;

import CB_Core.Solver.Solver;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;

import java.util.ArrayList;

public class SolverDialog2BuildFormula {
    private String sForm;
    private Solver solver;
    private ArrayList<CB_Label> labels;

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
