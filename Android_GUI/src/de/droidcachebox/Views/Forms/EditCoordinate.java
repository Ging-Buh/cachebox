package de.droidcachebox.Views.Forms;

import java.text.NumberFormat;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Custom_Controls.MultiToggleButton;

import de.droidcachebox.UTM.UTMConvert;
import CB_Core.Types.Coordinate;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.ToggleButton;

public class EditCoordinate extends Activity {
	private Intent aktIntent;
	private int aktPage = -1;  // Deg-Min
    UTMConvert convert = new UTMConvert();

	Coordinate coord;
	TableRow trDec;
	TableRow trMin;
	TableRow trSec;
	TableRow trUtm;
// Allgemein
	MultiToggleButton bDec;
	MultiToggleButton bMin;
	MultiToggleButton bSec;
	MultiToggleButton bUtm;
	// Deg
	Button bDLat;
	EditText tbDLat;
	Button bDLon;
	EditText tbDLon;
	// Deg - Min
	Button bMLat;
	EditText tbMLatDeg;
	EditText tbMLatMin;
	Button bMLon;
	EditText tbMLonDeg;
	EditText tbMLonMin;
	// Deg - Min - Sec
	Button bSLat;
	EditText tbSLatDeg;
	EditText tbSLatMin;
	EditText tbSLatSec;
	Button bSLon;
	EditText tbSLonDeg;
	EditText tbSLonMin;
	EditText tbSLonSec;
	// Utm
	EditText tbUX;
	EditText tbUY;
	EditText tbUZone;
	Button bUX;
	Button bUY;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_coordinate);

		// übergebene Koordinate auslesen
		Bundle bundle = getIntent().getExtras();
		coord = (Coordinate)bundle.getSerializable("Coord");
	        
        aktIntent = getIntent();

		trDec =(TableRow)this.findViewById(R.id.edco_table_dec);
		trDec.setVisibility(View.GONE);
		trMin =(TableRow)this.findViewById(R.id.edco_table_min);
		trMin.setVisibility(View.VISIBLE);
		trSec = (TableRow)this.findViewById(R.id.edco_table_sec);
		trSec.setVisibility(View.GONE);
		trUtm = (TableRow)this.findViewById(R.id.edco_table_utm);
		trUtm.setVisibility(View.GONE);
		
		// Allgemein
		bDec = (MultiToggleButton) findViewById(R.id.edco_dec);
        bMin = (MultiToggleButton) findViewById(R.id.edco_min);
		bSec = (MultiToggleButton) findViewById(R.id.edco_sec);
		bUtm = (MultiToggleButton) findViewById(R.id.edco_utm);
		MultiToggleButton.initialOn_Off_ToggleStates(bDec,"Dec","Dec");
		MultiToggleButton.initialOn_Off_ToggleStates(bMin,"Min","Min");
		MultiToggleButton.initialOn_Off_ToggleStates(bSec,"Sec","Sec");
		MultiToggleButton.initialOn_Off_ToggleStates(bUtm,"UTM","UTM");
		
		// Deg
		bDLat = (Button) findViewById(R.id.edco_dec_lat_direction);
		tbDLat = (EditText) findViewById(R.id.edco_dec_lat_value);
		bDLon = (Button) findViewById(R.id.edco_dec_lon_directioin);
		tbDLon = (EditText) findViewById(R.id.edco_dec_lon_value);
		// Deg - Min
		bMLat = (Button) findViewById(R.id.edco_min_lat_direction);
		tbMLatDeg = (EditText) findViewById(R.id.edco_min_lat_value_dec);
		tbMLatMin = (EditText) findViewById(R.id.edco_min_lat_value_min);
		bMLon = (Button) findViewById(R.id.edco_min_lon_direction);
		tbMLonDeg = (EditText) findViewById(R.id.edco_min_lon_value_dec);
		tbMLonMin = (EditText) findViewById(R.id.edco_min_lon_value_min);
		// Deg - Min - Sec
		bSLat = (Button) findViewById(R.id.edco_sec_lat_direction);
		tbSLatDeg = (EditText) findViewById(R.id.edco_sec_lat_value_dec);
		tbSLatMin = (EditText) findViewById(R.id.edco_sec_lat_value_min);
		tbSLatSec = (EditText) findViewById(R.id.edco_sec_lat_value_sec);
		bSLon = (Button) findViewById(R.id.edco_sec_lon_direction);
		tbSLonDeg = (EditText) findViewById(R.id.edco_sec_lon_value_dec);
		tbSLonMin = (EditText) findViewById(R.id.edco_sec_lon_value_min);
		tbSLonSec = (EditText) findViewById(R.id.edco_sec_lon_value_sec);
		// Utm
		tbUX = (EditText) findViewById(R.id.edco_utm_x_value);
		tbUY = (EditText) findViewById(R.id.edco_utm_y_value);
		tbUZone = (EditText) findViewById(R.id.edco_utm_zone_value);
		bUX = (Button) findViewById(R.id.edco_utm_x_direction);
		bUY = (Button) findViewById(R.id.edco_utm_y_directioin);
        
        
        bDec.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(0);
        	}
        });
        
        bMin.setState(1);
        bMin.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(1);
        	}
        });
        
        bSec.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(2);
        	}
        });
        bUtm.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(3);
        	}
        });

        Button bOK = (Button) findViewById(R.id.edco_ok);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (!parseView())
            		return;
            	aktIntent.putExtra("SOMETHING", "EXTRAS");
            	Bundle extras = new Bundle();
    	        extras.putSerializable("CoordResult", coord);
            	aktIntent.putExtras(extras);
            	setResult(RESULT_OK, aktIntent);
            	finish();	            	
            }
          });
        Button bCancel = (Button) findViewById(R.id.edco_cancel);
        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            	aktIntent.putExtra("SOMETHING", "EXTRAS");
            	Bundle extras = new Bundle();
    	        extras.putSerializable("CoordResult", null);
            	aktIntent.putExtras(extras);
            	setResult(RESULT_OK, aktIntent);
            	finish();	            	
            }
          });
        
        bDLat.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (bDLat.getText().equals("N"))
        			bDLat.setText("S");
        		else
        			bDLat.setText("N");
        	}
        });
        bDLon.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (bDLon.getText().equals("E"))
        			bDLon.setText("W");
        		else
        			bDLon.setText("E");
        	}
        });
        bMLat.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (bMLat.getText().equals("N"))
        			bMLat.setText("S");
        		else
        			bMLat.setText("N");
        	}
        });
        bMLon.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if (bMLon.getText().equals("E"))
        			bMLon.setText("W");
        		else
        			bMLon.setText("E");
        	}
        });
        showPage(1);
        
        // Translations
        bOK.setText(Global.Translations.Get("ok"));
        bCancel.setText(Global.Translations.Get("cancel"));
	 }

	 private static final String[] mStrings = {
		 "Reference", "Stage of a Multicache", "Question to answer", "Trailhead", "Parking Area", "Final"
	 };
	 
	 private void showPage(int newPage)
	 {
		 if (aktPage >= 0)
			 parseView();
		 switch (newPage)
		 {
		 case 0:
     		// show Degrees
         	trDec.setVisibility(View.VISIBLE);
         	trMin.setVisibility(View.GONE);	        		
         	trSec.setVisibility(View.GONE);
         	trUtm.setVisibility(View.GONE);
         	bDec.setState(1);
         	bMin.setState(0);
         	bSec.setState(0);
         	bUtm.setState(0);
         	if (coord.Latitude > 0) bDLat.setText("N");	else bDLat.setText("S");
         	if (coord.Longitude > 0) bDLon.setText("E"); else bDLon.setText("W");
         	tbDLat.setText(String.format("%.5f", coord.Latitude));
         	tbDLon.setText(String.format("%.5f", coord.Longitude));
         	break;
		 case 1:
     		// show Degree - Minute
         	trDec.setVisibility(View.GONE);	        		
         	trMin.setVisibility(View.VISIBLE);	        			        		
         	trSec.setVisibility(View.GONE);
         	trUtm.setVisibility(View.GONE);
         	bDec.setState(0);
         	bMin.setState(1);
         	bSec.setState(0);
         	bUtm.setState(0);
         	if (coord.Latitude > 0) bMLat.setText("N");	else bMLat.setText("S");
         	if (coord.Longitude > 0) bMLon.setText("E"); else bMLon.setText("W");

         	double deg = (int)Math.abs(coord.Latitude);
         	double frac = Math.abs(coord.Latitude) - deg;
         	double min = frac * 60;
         	tbMLatDeg.setText(String.format("%.0f", deg));
         	tbMLatMin.setText(String.format("%.3f", min));

         	deg = (int)Math.abs(coord.Longitude);
         	frac = Math.abs(coord.Longitude) - deg;
         	min = frac * 60;
         	tbMLonDeg.setText(String.format("%.0f", deg));
         	tbMLonMin.setText(String.format("%.3f", min));
         	break;
		 case 2:
     		// show Degree - Minute - Second
         	trMin.setVisibility(View.GONE);	        		
         	trDec.setVisibility(View.GONE);	        		
         	trSec.setVisibility(View.VISIBLE);
         	trUtm.setVisibility(View.GONE);
         	bDec.setState(0);
         	bMin.setState(0);
         	bSec.setState(1);
         	bUtm.setState(0);
         	
            deg = Math.abs((int)coord.Latitude);
            frac = Math.abs(coord.Latitude) - deg;
            min = frac * 60;
            int imin = (int)min;
            frac = min - imin;
            double sec = frac * 60;

         	if (coord.Latitude > 0) bSLat.setText("N");	else bSLat.setText("S");
            tbSLatDeg.setText(String.format("%.0f", deg));
            tbSLatMin.setText(String.valueOf(imin));
            tbSLatSec.setText(String.format("%.2f", sec));

            deg = Math.abs((int)coord.Longitude);
            frac = Math.abs(coord.Longitude) - deg;
            min = frac * 60;
            imin = (int)min;
            frac = min - imin;
            sec = frac * 60;

         	if (coord.Longitude > 0) bSLon.setText("E"); else bSLon.setText("W");
            tbSLonDeg.setText(String.format("%.0f", deg));
            tbSLonMin.setText(String.valueOf(imin));
            tbSLonSec.setText(String.format("%.2f", sec));

         	break;
		 case 3:
     		// show UTM
         	trMin.setVisibility(View.GONE);	        		
         	trDec.setVisibility(View.GONE);	        		
         	trSec.setVisibility(View.GONE);
         	trUtm.setVisibility(View.VISIBLE);
         	bDec.setState(0);
         	bMin.setState(0);
         	bSec.setState(0);
         	bUtm.setState(1);

            double nording = 0;
            double easting = 0;
            String zone = "";
            convert.iLatLon2UTM(coord.Latitude, coord.Longitude);
            nording = convert.UTMNorthing;
            easting = convert.UTMEasting;
            zone = convert.sUtmZone;
//            tbUY.setText(String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(nording)));
//            tbUX.setText(String.Format(NumberFormatInfo.InvariantInfo, "{0:0}", Math.Floor(easting)));
            tbUY.setText(String.format("%.1f", nording));
            tbUX.setText(String.format("%.1f", easting));
            tbUZone.setText(zone);
            if (coord.Latitude > 0)
                bUY.setText("N");
            else if (coord.Latitude < 0)
                bUY.setText("S");
            if (coord.Longitude > 0)
                bUX.setText("E");
            else if (coord.Longitude < 0)
                bUX.setText("W");
            break;         	
		 }
		 aktPage = newPage;
	 }
	 
	 private boolean parseView()
	 {
		 String scoord = "";
		 switch (aktPage)
		 {
		 case 0:
     		// show Degrees
             scoord += bDLat.getText() + " " + tbDLat.getText() + "°";
             scoord += " " + bDLon.getText() + " " + tbDLon.getText() + "°";
         	break;
		 case 1:
     		// show Degree - Minute
             scoord += bMLat.getText() + " " + tbMLatDeg.getText() + "° " + tbMLatMin.getText() + "'";
             scoord += " " + bMLon.getText() + " " + tbMLonDeg.getText() + "° " + tbMLonMin.getText() + "'";
         	break;
		 case 2:
     		// show Degree - Minute - Second
             scoord += bSLat.getText() + " " + tbSLatDeg.getText() + "° " + tbSLatMin.getText() + "' " + tbSLatSec.getText() + "''";
             scoord += " " + bSLon.getText() + " " + tbSLonDeg.getText() + "° " + tbSLonMin.getText() + "' " + tbSLonSec.getText() + "''";
         	break;
		 case 3:
     		// show UTM
             scoord += tbUZone.getText() + " " + tbUX.getText() + " " + tbUY.getText();
			 break;		 
		 }
		 Coordinate newCoord = new Coordinate(scoord);
		 if (newCoord.Valid)
		 {
			 coord = newCoord;
			 return true;
		 } else
			 return false;
	 }
}
