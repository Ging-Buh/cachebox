package de.droidcachebox.Views.Forms;

import java.text.NumberFormat;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Coordinate;
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

	Coordinate coord;
	TableRow trDec;
	TableRow trMin;
// Allgemein
	ToggleButton bDec;
    ToggleButton bMin;
	ToggleButton bSec;
	ToggleButton bUtm;
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
	// Utm
	
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
		
		// Allgemein
		bDec = (ToggleButton) findViewById(R.id.edco_dec);
        bMin = (ToggleButton) findViewById(R.id.edco_min);
		bSec = (ToggleButton) findViewById(R.id.edco_sec);
		bUtm = (ToggleButton) findViewById(R.id.edco_utm);
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
		// Utm
        
        bDec.setText("Dec");
        bDec.setTextOff("Dec");
        bDec.setTextOn("Dec");
        bDec.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(0);
        	}
        });
        bMin.setText("Min");
        bMin.setTextOff("Min");
        bMin.setTextOn("Min");
        bMin.setChecked(true);
        bMin.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(1);
        	}
        });
        bSec.setText("Sec");
        bSec.setTextOff("Sec");
        bSec.setTextOn("Sec");
        bSec.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		showPage(2);
        	}
        });
        bUtm.setText("UTM");
        bUtm.setTextOff("UTM");
        bUtm.setTextOn("UTM");
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
         	trMin.setVisibility(View.GONE);	        		
         	trDec.setVisibility(View.VISIBLE);	        		
         	bDec.setChecked(true);
         	bMin.setChecked(false);
         	bSec.setChecked(false);
         	bUtm.setChecked(false);
         	if (coord.Latitude > 0) bDLat.setText("N");	else bDLat.setText("S");
         	if (coord.Longitude > 0) bDLon.setText("E"); else bDLon.setText("W");
         	tbDLat.setText(String.format("%.5f", coord.Latitude));
         	tbDLon.setText(String.format("%.5f", coord.Longitude));
         	break;
		 case 1:
     		// show Degree - Minute
         	trDec.setVisibility(View.GONE);	        		
         	trMin.setVisibility(View.VISIBLE);	        			        		
         	bDec.setChecked(false);
         	bMin.setChecked(true);
         	bSec.setChecked(false);
         	bUtm.setChecked(false);
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
         	bDec.setChecked(false);
         	bMin.setChecked(false);
         	bSec.setChecked(true);
         	bUtm.setChecked(false);
         	break;
		 case 3:
     		// show UTM
         	trMin.setVisibility(View.GONE);	        		
         	trDec.setVisibility(View.GONE);	        		
         	bDec.setChecked(false);
         	bMin.setChecked(false);
         	bSec.setChecked(false);
         	bUtm.setChecked(true);
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
//             scoord += cbSLat.Text + " " + tbSLatDeg.Text + "° " + tbSLatMin.Text + "' " + tbSLatSec.Text + "''";
//             scoord += " " + cbSLon.Text + " " + tbSLonDeg.Text + "° " + tbSLonMin.Text + "' " + tbSLonSec.Text + "''";
         	break;
		 case 3:
     		// show UTM
//             scoord += tbZone.Text + " " + tbEasting.Text + " " + tbNording.Text;
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
