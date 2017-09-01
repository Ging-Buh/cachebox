package cb_server.Views.Dialogs;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import CB_Locator.Coordinate;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class CoordinateDialog extends ButtonDialog implements BlurListener {
	private static final long serialVersionUID = 1834633252228907909L;

	public interface ReturnListner {
		public void returnedCoord(Coordinate coord);
	}

	private ReturnListner returnListner;
	private TabSheet sheets;
	private Component sheetGrad;
	private Component sheetGradMin;
	private Component sheetGradMinSec;
	private Component sheetUTM;
	private Coordinate coord;
	private boolean doNotCheckValid = false;
	private TextField tfCoordinate;
	private ComboBox cbDLat;
	private ComboBox cbDLon;
	private TextField tbDLat;
	private TextField tbDLon;
	private ComboBox cbMLat;
	private ComboBox cbMLon;
	private TextField tbMLatDeg;
	private TextField tbMLatMin;
	private TextField tbMLonDeg;
	private TextField tbMLonMin;
	private ComboBox cbSLat;
	private ComboBox cbSLon;
	private TextField tbSLatDeg;
	private TextField tbSLatMin;
	private TextField tbSLatSec;
	private TextField tbSLonDeg;
	private TextField tbSLonMin;
	private TextField tbSLonSec;

	public CoordinateDialog(Coordinate coord, ReturnListner returnListener) {
		super("Edit Coordinate");
		this.coord = coord;
		this.returnListner = returnListener;
	}
	

	@Override
	protected void cancelClicked() {
	}

	@Override
	protected void okClicked() {
		if (returnListner != null) {
			returnListner.returnedCoord(this.coord);
		}
	}
	
	@Override
	protected void createContent(VerticalLayout content) {
		GridLayout layoutContent = new GridLayout(2, 2);
		content.addComponent(layoutContent);

		sheets = new TabSheet();
		sheets.addTab(sheetGrad = createGradSheet(), "Grad");
		sheets.addTab(sheetGradMin = createGradMinSheet(), "Grd. Min.");
		sheets.addTab(sheetGradMinSec = createGradMinSecSheet(), "Grd. Min. Sec.");
		sheets.addTab(sheetUTM = createUTMSheet(), "UTM");
		sheets.setSizeFull();
		sheets.setWidth("400px");
		sheets.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				updateView();
				parseView();
			}
		});
		layoutContent.addComponent(sheets, 0, 0, 1, 0);

		tfCoordinate = new TextField();
		tfCoordinate.setWidth("100%");
		layoutContent.addComponent(tfCoordinate);

		Button bAnalyze = new Button("Analyze");
		bAnalyze.setWidth("100%");
		bAnalyze.addClickListener(new ClickListener() {
			private static final long serialVersionUID = -4834634673220564100L;
			@Override
			public void buttonClick(ClickEvent event) {
	            String text = tfCoordinate.getValue();
	            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
	            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
	            char sep = symbols.getDecimalSeparator();
	            text = text.replace('.', sep);
	            text = text.replace(',', sep);

	            Coordinate acoord = new Coordinate(text);
	            if (acoord.isValid())
	            {
	            	CoordinateDialog.this.coord = acoord;
	                updateView();
	                return;
	            }

			}
		});
		layoutContent.addComponent(bAnalyze);

		updateView();
		parseView();
	}

	private Component createUTMSheet() {
		GridLayout layout = new GridLayout(5, 2);
		layout.setMargin(true);

		return layout;
	}

	private Component createGradMinSecSheet() {
		GridLayout layout = new GridLayout(7, 2);
		layout.setWidth("100%");
		layout.setMargin(true);

		cbSLat = new ComboBox();
		cbSLat.addItem("N");
		cbSLat.addItem("S");
		cbSLat.setNullSelectionAllowed(false);
		layout.addComponent(cbSLat);

		tbSLatDeg = new TextField();
		layout.addComponent(tbSLatDeg);

		Label lLatGrad = new Label("°");
		layout.addComponent(lLatGrad);

		tbSLatMin = new TextField();
		layout.addComponent(tbSLatMin);

		Label lLatMin = new Label("'");
		layout.addComponent(lLatMin);

		tbSLatSec = new TextField();
		layout.addComponent(tbSLatSec);

		Label lLatSec = new Label("''");
		layout.addComponent(lLatSec);

		cbSLon = new ComboBox();
		cbSLon.setNullSelectionAllowed(false);
		cbSLon.addItem("E");
		cbSLon.addItem("W");
		layout.addComponent(cbSLon);

		tbSLonDeg = new TextField();
		layout.addComponent(tbSLonDeg);

		Label lLonGrad = new Label("°");
		layout.addComponent(lLonGrad);

		tbSLonMin = new TextField();
		layout.addComponent(tbSLonMin);

		Label lLonMin = new Label("''");
		layout.addComponent(lLonMin);

		tbSLonSec = new TextField();
		layout.addComponent(tbSLonSec);

		Label lLonSec = new Label("'");
		layout.addComponent(lLonSec);

		layout.setColumnExpandRatio(0, 0);
		layout.setColumnExpandRatio(1, 3);
		layout.setColumnExpandRatio(2, 0);
		layout.setColumnExpandRatio(3, 3);
		layout.setColumnExpandRatio(4, 0);
		layout.setColumnExpandRatio(5, 3);
		layout.setColumnExpandRatio(6, 0);

		tbSLatDeg.addBlurListener(this);
		tbSLonDeg.addBlurListener(this);
		tbSLatMin.addBlurListener(this);
		tbSLonMin.addBlurListener(this);
		tbSLatSec.addBlurListener(this);
		tbSLonSec.addBlurListener(this);
		cbSLat.addBlurListener(this);
		cbSLon.addBlurListener(this);

		cbSLat.setWidth("40px");
		tbSLatDeg.setWidth("100%");
		lLatGrad.setWidth("20px");
		tbSLatMin.setWidth("100%");
		lLatMin.setWidth("20px");
		tbSLatSec.setWidth("100%");
		lLatSec.setWidth("20px");
		cbSLon.setWidth("40px");
		tbSLonDeg.setWidth("100%");
		lLonGrad.setWidth("20px");
		tbSLonMin.setWidth("100%");
		lLatMin.setWidth("20px");
		tbSLonSec.setWidth("100%");
		lLatSec.setWidth("20px");
		

		return layout;

	}

	private Component createGradMinSheet() {
		GridLayout layout = new GridLayout(5, 2);
		layout.setWidth("100%");
		layout.setMargin(true);

		cbMLat = new ComboBox();
		cbMLat.addItem("N");
		cbMLat.addItem("S");
		cbMLat.setNullSelectionAllowed(false);
		layout.addComponent(cbMLat);

		tbMLatDeg = new TextField();
		layout.addComponent(tbMLatDeg);

		Label lLatGrad = new Label("°");
		layout.addComponent(lLatGrad);

		tbMLatMin = new TextField();
		layout.addComponent(tbMLatMin);

		Label lLatMin = new Label("'");
		layout.addComponent(lLatMin);

		cbMLon = new ComboBox();
		cbMLon.addItem("E");
		cbMLon.addItem("W");
		cbMLon.setNullSelectionAllowed(false);
		layout.addComponent(cbMLon);

		tbMLonDeg = new TextField();
		layout.addComponent(tbMLonDeg);

		Label lLonGrad = new Label("°");
		layout.addComponent(lLonGrad);

		tbMLonMin = new TextField();
		layout.addComponent(tbMLonMin);

		Label lLonMin = new Label("'");
		layout.addComponent(lLonMin);

		tbMLatDeg.addBlurListener(this);
		tbMLonDeg.addBlurListener(this);
		tbMLatMin.addBlurListener(this);
		tbMLonMin.addBlurListener(this);
		cbMLat.addBlurListener(this);
		cbMLon.addBlurListener(this);

		layout.setColumnExpandRatio(0, 0);
		layout.setColumnExpandRatio(1, 3);
		layout.setColumnExpandRatio(2, 0);
		layout.setColumnExpandRatio(3, 3);
		layout.setColumnExpandRatio(4, 0);

		cbMLat.setWidth("40px");
		tbMLatDeg.setWidth("100%");
		lLatGrad.setWidth("20px");
		tbMLatMin.setWidth("100%");
		lLatMin.setWidth("20px");
		
		cbMLon.setWidth("40px");
		tbMLonDeg.setWidth("100%");
		lLonGrad.setWidth("20px");
		tbMLonMin.setWidth("100%");
		lLonMin.setWidth("20px");


		return layout;
	}

	private Component createGradSheet() {
		GridLayout layout = new GridLayout(3, 2);
		layout.setMargin(true);
		layout.setSizeFull();

		cbDLat = new ComboBox();
		cbDLat.addItem("N");
		cbDLat.addItem("S");
		cbDLat.setNullSelectionAllowed(false);
		//		cbNord.setWidth("20%");
		layout.addComponent(cbDLat);

		tbDLat = new TextField();
		//		tfLatitude.setWidth("70%");
		layout.addComponent(tbDLat);

		Label lLatitude = new Label("°");
		//		lLatitude.setWidth("10%");
		layout.addComponent(lLatitude);

		cbDLon = new ComboBox();
		cbDLon.addItem("E");
		cbDLon.addItem("W");
		cbDLon.setNullSelectionAllowed(false);
		//		cbOst.setWidth("20%");
		layout.addComponent(cbDLon);

		tbDLon = new TextField();
		//		tfLongitude.setWidth("70%");
		layout.addComponent(tbDLon);

		Label lLongitude = new Label("°");
		//		lLongitude.setWidth("10%");
		layout.addComponent(lLongitude);

		tbDLat.addBlurListener(this);
		tbDLon.addBlurListener(this);
		cbDLat.addBlurListener(this);
		cbDLon.addBlurListener(this);

		layout.setColumnExpandRatio(0, 0);
		layout.setColumnExpandRatio(1, 3);
		layout.setColumnExpandRatio(2, 0);


		cbDLat.setWidth("40px");
		tbDLat.setWidth("100%");
		lLatitude.setWidth("20px");

		
		cbDLon.setWidth("40px");
		tbDLon.setWidth("100%");
		lLongitude.setWidth("20px");

		return layout;
	}

	private String formatDouble(double nummer, int nachkomma) {
		return String.format("%." + nachkomma + "f", nummer);
	}

	private void updateView() {
		// aktuelle Koordinate ins aktuelle Register eintragen
		doNotCheckValid = true;
		if (sheets.getSelectedTab() == sheetGrad) {
			// Decimalgrad
			double deg = Math.abs(coord.getLatitude());
			if (coord.getLatitude() < 0)
				cbDLat.setValue("S");
			else if (coord.getLatitude() > 0)
				cbDLat.setValue("N");
			tbDLat.setValue(formatDouble(deg, 5));
			deg = Math.abs(coord.getLongitude());
			if (coord.getLongitude() < 0)
				cbDLon.setValue("W");
			else if (coord.getLongitude() > 0)
				cbDLon.setValue("E");
			tbDLon.setValue(formatDouble(deg, 5));
		} else if (sheets.getSelectedTab() == sheetGradMin) {

			// Grad-Minuten
			double deg = Math.abs((int) coord.getLatitude());
			double frac = Math.abs(coord.getLatitude()) - deg;
			double min = frac * 60;

			if (coord.getLatitude() < 0)
				cbMLat.setValue("S");
			else if (coord.getLatitude() > 0)
				cbMLat.setValue("N");
			tbMLatDeg.setValue(formatDouble(deg, 0));
			tbMLatMin.setValue(formatDouble(min, 3));

			deg = Math.abs((int) coord.getLongitude());
			frac = Math.abs(coord.getLongitude()) - deg;
			min = frac * 60;

			if (coord.getLongitude() < 0)
				cbMLon.setValue("W");
			else if (coord.getLongitude() > 0)
				cbMLon.setValue("E");

			tbMLonDeg.setValue(formatDouble(deg, 0));
			tbMLonMin.setValue(formatDouble(min, 3));
		} else if (sheets.getSelectedTab() == sheetGradMinSec) {

			// Grad-Minuten-Sekunden
			double deg = Math.abs((int) coord.getLatitude());
			double frac = Math.abs(coord.getLatitude()) - deg;
			double min = frac * 60;
			int imin = (int) min;
			frac = min - imin;
			double sec = frac * 60;

			if (coord.getLatitude() < 0)
				cbSLat.setValue("S");
			else if (coord.getLatitude() > 0)
				cbSLat.setValue("N");
			tbSLatDeg.setValue(formatDouble(deg, 0));
			tbSLatMin.setValue(formatDouble(imin, 0));
			tbSLatSec.setValue(formatDouble(sec, 2));

			deg = Math.abs((int) coord.getLongitude());
			frac = Math.abs(coord.getLongitude()) - deg;
			min = frac * 60;
			imin = (int) min;
			frac = min - imin;
			sec = frac * 60;

			if (coord.getLongitude() < 0)
				cbSLon.setValue("W");
			else if (coord.getLongitude() > 0)
				cbSLon.setValue("E");
			tbSLonDeg.setValue(formatDouble(deg, 0));
			tbSLonMin.setValue(formatDouble(imin, 0));
			tbSLonSec.setValue(formatDouble(sec, 2));
		} else if (sheets.getSelectedTab() == sheetUTM) {

			/*	TODO			            
						                double nording = 0;
						                double easting = 0;
						                String zone = "";
						                convert.iLatLon2UTM(coord.Latitude, coord.Longitude, ref nording, ref easting, ref zone);
						                tbNording.Text = String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(nording));
						                tbEasting.Text = String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(easting));
						                tbNording.Text = Math.Round(nording, 1).ToString();
						                tbEasting.Text = Math.Round(easting, 1).ToString();
						                tbZone.Text = zone;
						                if (coord.Latitude > 0)
						                    cbULat.Text = "N";
						                else if (coord.Latitude < 0)
						                    cbULat.Text = "S";
						                if (coord.Longitude > 0)
						                    cbULon.Text = "E";
						                else if (coord.Longitude < 0)
						                    cbULon.Text = "W";
						                    */
		}

		doNotCheckValid = false;

	}

	private boolean parseView() {
		String scoord = "";
		if (sheets.getSelectedTab() == sheetGrad) {
			scoord += cbDLat.getValue() + " " + tbDLat.getValue() + "°";
			scoord += " " + cbDLon.getValue() + " " + tbDLon.getValue() + "°";
		} else if (sheets.getSelectedTab() == sheetGradMin) {
			scoord += cbMLat.getValue() + " " + tbMLatDeg.getValue() + "° " + tbMLatMin.getValue() + "'";
			scoord += " " + cbMLon.getValue() + " " + tbMLonDeg.getValue() + "° " + tbMLonMin.getValue() + "'";
		} else if (sheets.getSelectedTab() == sheetGradMinSec) {
			scoord += cbSLat.getValue() + " " + tbSLatDeg.getValue() + "° " + tbSLatMin.getValue() + "' " + tbSLatSec.getValue() + "''";
			scoord += " " + cbSLon.getValue() + " " + tbSLonDeg.getValue() + "° " + tbSLonMin.getValue() + "' " + tbSLonSec.getValue() + "''";
		} else if (sheets.getSelectedTab() == sheetUTM) {
			// TODO                scoord += tbZone.Text + " " + tbEasting.Text + " " + tbNording.Text;

		}
		Coordinate tmpCoord = new Coordinate(scoord);
		if (tmpCoord.isValid()) {
			coord = tmpCoord;
			tfCoordinate.setValue(tmpCoord.FormatCoordinate());
			return true;
		} else
			return false;

	}

	@Override
	public void blur(BlurEvent event) {
		System.out.println(event.getSource());
		if (doNotCheckValid)
			return;
		if (!parseView()) {
			//            if (this.ActiveControl == button2)
			//            {
			//                this.DialogResult = System.Windows.Forms.DialogResult.Cancel;
			//                return;
			//            }
			//            MessageBox.Show("Not a valid Coordinate");
			//            (sender as Control).Focus();
			//            if (sender is TextBox)
			//                (sender as TextBox).SelectAll();
		} else
			updateView();
	}


}
