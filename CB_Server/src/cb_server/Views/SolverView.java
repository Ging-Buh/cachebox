package cb_server.Views;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import CB_Core.Database;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import cb_server.Events.SelectedCacheChangedEventList;
import cb_server.Events.SelectedCacheChangedEventListner;

public class SolverView extends CB_ViewBase implements SelectedCacheChangedEventListner {

	private static final long serialVersionUID = -4622500815459784456L;
	private final VerticalLayout vertical;
	private final TextArea formula;
	private final TextArea solution;
	private final HorizontalSplitPanel split;
	private final HorizontalLayout buttons;
	private final Button bSolve;
	private Cache actCache;

	public SolverView() {
		super();
		formula = new TextArea();
		formula.setSizeFull();
		solution = new TextArea();
		solution.setSizeFull();
		split = new HorizontalSplitPanel();
		split.setFirstComponent(formula);
		split.setSecondComponent(solution);
		split.setSizeFull();
		buttons = new HorizontalLayout();
		buttons.setWidth("100%");
		buttons.setHeight("50px");
		vertical = new VerticalLayout();
		vertical.addComponent(split);
		vertical.addComponent(buttons);
		vertical.setSizeFull();

		bSolve = new Button("Solve");
		bSolve.setHeight("100%");
		bSolve.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				Solver solver = new Solver(formula.getValue());
				solver.Solve();
				String result = "";
				for (SolverZeile zeile : solver) {
					result += zeile.Solution + System.getProperty("line.separator");
				}
				solution.setValue(result);
				if (actCache != null) {
					Database.SetSolver(actCache.Id, formula.getValue());
				}
			}
		});
		buttons.addComponent(bSolve);
		this.setCompositionRoot(vertical);
		this.setSizeFull();
		SelectedCacheChangedEventList.Add(this);
	}

	@Override
	public void SelectedCacheChangedEvent(Cache cache2, Waypoint waypoint, boolean cacheChanged, boolean waypointChanged) {
		actCache = cache2;
		formula.setValue(Database.GetSolver(cache2.Id));
		solution.setValue("");
	}
}
