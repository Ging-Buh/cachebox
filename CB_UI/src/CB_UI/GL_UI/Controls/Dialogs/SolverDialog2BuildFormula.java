package CB_UI.GL_UI.Controls.Dialogs;

import java.util.ArrayList;

import CB_Core.Solver.Solver;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;

public class SolverDialog2BuildFormula {
	private String sForm;
	private Solver solver;
	private ArrayList<Label> labels;

	public SolverDialog2BuildFormula(String sForm) {
		this.sForm = sForm;
		solver = new Solver(sForm);
		solver.Solve();
		labels = new ArrayList<Label>();
	}

	public void addControls(ScrollBox scrollBox) {
		if (solver.MissingVariables != null) {
			for (String mv : solver.MissingVariables.keySet()) {
				Label l = new Label(mv);
				scrollBox.addChild(l);
				labels.add(l);
			}
		}

	}

	public float Layout(float y, float innerLeft, float innerWidth, float margin) {
		for (int i = labels.size() - 1; i >= 0; i--) {
			Label label = labels.get(i);
			label.setX(innerLeft);
			label.setWidth(innerWidth);
			label.setY(y);
			y += label.getHeight() + margin;
		}
		return y;
	}

	public void removeChilds(ScrollBox scrollBox) {
		for (Label l : labels) {
			scrollBox.removeChild(l);
		}
	}
}
