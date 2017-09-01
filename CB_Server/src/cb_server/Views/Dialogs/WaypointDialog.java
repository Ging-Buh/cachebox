package cb_server.Views.Dialogs;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import CB_Core.CacheTypes;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;

public class WaypointDialog extends ButtonDialog {
	public interface ReturnListner {
		public void returnedWP(Waypoint wp);
	}

	private final ReturnListner returnListner;

	private static final long serialVersionUID = 8222163724284799469L;
	private final Waypoint waypoint;
	private Coordinate coord;
	private Button bCoord;
	private TextField tfName;
	private TextField tfDescription;
	private TextField tfClue;
	private ComboBox cbTyp;

	public WaypointDialog(final Waypoint waypoint, ReturnListner returnListener) {
		super("Edit Waypoint");
		this.waypoint = waypoint;
		this.coord = waypoint.Pos;
		this.returnListner = returnListener;
	}

	@Override
	protected void createContent(VerticalLayout content) {
		GridLayout layoutContent = new GridLayout();
		content.addComponent(layoutContent);

		bCoord = new Button();
		bCoord.setCaption(waypoint.Pos.toString());
		layoutContent.addComponent(bCoord);
		bCoord.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 252870818782021184L;

			@Override
			public void buttonClick(ClickEvent event) {
				CoordinateDialog dial = new CoordinateDialog(waypoint.Pos, new CoordinateDialog.ReturnListner() {
					@Override
					public void returnedCoord(Coordinate result) {
						if (coord != null) {
							coord = result;
							bCoord.setCaption(result.toString());
						}
					}
				});
				WaypointDialog.this.getUI().addWindow(dial);
			}
		});

		tfName = new TextField();
		tfName.setCaption("Titel:");
		tfName.setValue(waypoint.getTitle());
		layoutContent.addComponent(tfName);

		tfDescription = new TextField();
		tfDescription.setCaption("Description:");
		tfDescription.setValue(waypoint.getDescription());
		layoutContent.addComponent(tfDescription);

		tfClue = new TextField();
		tfClue.setCaption("Clue:");
		tfClue.setValue(waypoint.getClue());
		layoutContent.addComponent(tfClue);

		cbTyp = new ComboBox();
		cbTyp.setCaption("Typ");
		cbTyp.addItem(CacheTypes.ReferencePoint);
		cbTyp.addItem(CacheTypes.MultiStage);
		cbTyp.addItem(CacheTypes.MultiQuestion);
		cbTyp.addItem(CacheTypes.Trailhead);
		cbTyp.addItem(CacheTypes.ParkingArea);
		cbTyp.addItem(CacheTypes.Final);
		cbTyp.setItemCaption(CacheTypes.ReferencePoint, "Reference Point");
		cbTyp.setItemCaption(CacheTypes.MultiStage, "State of a Multicache");
		cbTyp.setItemCaption(CacheTypes.MultiQuestion, "Question to answer");
		cbTyp.setItemCaption(CacheTypes.Trailhead, "Trailhead");
		cbTyp.setItemCaption(CacheTypes.ParkingArea, "Parking area");
		cbTyp.setItemCaption(CacheTypes.Final, "Final");
		cbTyp.setValue(waypoint.Type);
		cbTyp.setNullSelectionAllowed(false);
		layoutContent.addComponent(cbTyp);

	}

	@Override
	protected void cancelClicked() {
	}

	@Override
	protected void okClicked() {
		waypoint.setTitle(tfName.getValue());
		waypoint.setDescription(tfDescription.getValue());
		waypoint.setClue(tfClue.getValue());
		waypoint.Type = (CacheTypes) cbTyp.getValue();
		waypoint.Pos = new Coordinate(coord);
		if (returnListner != null) {
			returnListner.returnedWP(waypoint);
		}
	}

}
