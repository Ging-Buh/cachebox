package de.droidcachebox.Views.Forms;

import java.util.ArrayList;
import java.util.zip.Inflater;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Custom_Controls.MultiToggleButton;

import de.droidcachebox.UTM.UTMConvert;
import de.droidcachebox.Ui.ActivityUtils;
import CB_Core.Types.Coordinate;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class EditCoordinate extends Activity {
	
	public EditCoordinate Me;
	
	private Intent aktIntent;
	private String Title ="";
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
	private TextView TitleView;
	private LinearLayout TitleLayout;
	private LinearLayout NumPadLayout;
	
	private ArrayList<Integer> dblEditTextList = new ArrayList<Integer>();
	
	public void onCreate(Bundle savedInstanceState) 
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_coordinate);

		Me=this;
		
		// übergebene Koordinate auslesen
		Bundle bundle = getIntent().getExtras();
		coord = (Coordinate)bundle.getSerializable("Coord");
		Title = (String)bundle.getSerializable("Title");
		
        aktIntent = getIntent();

        TitleView=(TextView)this.findViewById(R.id.edco_titleview);
        TitleLayout=(LinearLayout)this.findViewById(R.id.edco_titlelayout);
        NumPadLayout=(LinearLayout)this.findViewById(R.id.NumPadLayout);
        
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
		if(Title==null || Title.equals(""))
		{
			TitleLayout.setVisibility(View.GONE);
		}
        else
		{
        	TitleView.setText(Title);
		}
		
		// Deg
		bDLat = (Button) findViewById(R.id.edco_dec_lat_direction);
		tbDLat = (EditText) findViewById(R.id.edco_dec_lat_value);
		tbDLat.setOnFocusChangeListener(onFocusChange);
				
		bDLon = (Button) findViewById(R.id.edco_dec_lon_directioin);
		tbDLon = (EditText) findViewById(R.id.edco_dec_lon_value);
		tbDLon.setOnFocusChangeListener(onFocusChange);
		// Deg - Min
		bMLat = (Button) findViewById(R.id.edco_min_lat_direction);
		tbMLatDeg = (EditText) findViewById(R.id.edco_min_lat_value_dec);
		tbMLatDeg.setOnFocusChangeListener(onFocusChange);
		tbMLatMin = (EditText) findViewById(R.id.edco_min_lat_value_min);
		tbMLatMin.setOnFocusChangeListener(onFocusChange);
		bMLon = (Button) findViewById(R.id.edco_min_lon_direction);
		tbMLonDeg = (EditText) findViewById(R.id.edco_min_lon_value_dec);
		tbMLonDeg.setOnFocusChangeListener(onFocusChange);
		tbMLonMin = (EditText) findViewById(R.id.edco_min_lon_value_min);
		tbMLonMin.setOnFocusChangeListener(onFocusChange);
		// Deg - Min - Sec
		bSLat = (Button) findViewById(R.id.edco_sec_lat_direction);
		tbSLatDeg = (EditText) findViewById(R.id.edco_sec_lat_value_dec);
		tbSLatDeg.setOnFocusChangeListener(onFocusChange);
		tbSLatMin = (EditText) findViewById(R.id.edco_sec_lat_value_min);
		tbSLatMin.setOnFocusChangeListener(onFocusChange);
		tbSLatSec = (EditText) findViewById(R.id.edco_sec_lat_value_sec);
		tbSLatSec.setOnFocusChangeListener(onFocusChange);
		bSLon = (Button) findViewById(R.id.edco_sec_lon_direction);
		tbSLonDeg = (EditText) findViewById(R.id.edco_sec_lon_value_dec);
		tbSLonDeg.setOnFocusChangeListener(onFocusChange);
		tbSLonMin = (EditText) findViewById(R.id.edco_sec_lon_value_min);
		tbSLonMin.setOnFocusChangeListener(onFocusChange);
		tbSLonSec = (EditText) findViewById(R.id.edco_sec_lon_value_sec);
		tbSLonSec.setOnFocusChangeListener(onFocusChange);
		// Utm
		tbUX = (EditText) findViewById(R.id.edco_utm_x_value);
		tbUX.setOnFocusChangeListener(onFocusChange);
		tbUY = (EditText) findViewById(R.id.edco_utm_y_value);
		tbUY.setOnFocusChangeListener(onFocusChange);
		tbUZone = (EditText) findViewById(R.id.edco_utm_zone_value);
		tbUZone.setOnFocusChangeListener(new OnFocusChangeListener() 
		{
			@Override
			public void onFocusChange(View arg0, boolean arg1) 
			{
				if(!arg1) // if lost the Focus
				{
					// close the virtual keyboard
					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.hideSoftInputFromWindow(tbUZone.getWindowToken(), 0);

				}
				
			}
		});
		
		bUX = (Button) findViewById(R.id.edco_utm_x_direction);
		bUY = (Button) findViewById(R.id.edco_utm_y_directioin);
        
		
		//fill dbl list of EditText Id´s
		dblEditTextList.add(tbDLat.getId());
		dblEditTextList.add(tbDLon.getId());
		dblEditTextList.add(tbMLatMin.getId());
		dblEditTextList.add(tbMLonMin.getId());
		dblEditTextList.add(tbSLatSec.getId());
		dblEditTextList.add(tbSLonSec.getId());
		dblEditTextList.add(tbUX.getId());
		dblEditTextList.add(tbUY.getId());
		
		((Button) NumPadLayout.findViewById(R.id.negativeButton)).setVisibility(View.INVISIBLE);
		((Button) NumPadLayout.findViewById(R.id.positiveButton)).setVisibility(View.INVISIBLE);
		
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

	
    /** hook into menu button for activity */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editcoordinate, menu);
        return true;    
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.mec_paste:
        	ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        	if (clipboardManager.hasText())
        	{
        		String sCoord = clipboardManager.getText().toString();
        		Coordinate coord = new Coordinate(sCoord);
        		if (coord.Valid)
        		{
        			this.coord = coord;
        			int oldPage = aktPage;
        			aktPage = -1;
        			showPage(oldPage);
        		} else
        		{
        			Toast.makeText(getApplicationContext(), "No valid Coordinate in Clipboard!", Toast.LENGTH_SHORT).show();        		
        		}
        	}
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }	

    private OnFocusChangeListener onFocusChange = new OnFocusChangeListener() 
	{
		@Override
		public void onFocusChange(View arg0, boolean arg1) 
		{
			//wenn Eine EditBox den Focus bekommt!
			if(EditText.class==(arg0.getClass()) && arg1)
			{// unterscheide ob ein INT oder DBL Eingabe Feld
						
				if(dblEditTextList.contains(arg0.getId()))
				{
					ActivityUtils.initialNumPadDbl((Activity)Me,NumPadLayout,(EditText)arg0,((EditText) arg0).getText().toString(),null,null);
				}
				else
				{
					ActivityUtils.initialNumPadInt((Activity)Me,NumPadLayout,(EditText)arg0,((EditText) arg0).getText().toString(),null,null);
				}
				
				
			}
		}
	};
	
		
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
         	tbDLat.setText(String.format("%.5f", coord.Latitude).replace(",","."));
         	tbDLon.setText(String.format("%.5f", coord.Longitude).replace(",","."));
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
         	tbMLatDeg.setText(String.format("%.0f", deg).replace(",","."));
         	tbMLatMin.setText(String.format("%.3f", min).replace(",","."));

         	deg = (int)Math.abs(coord.Longitude);
         	frac = Math.abs(coord.Longitude) - deg;
         	min = frac * 60;
         	tbMLonDeg.setText(String.format("%.0f", deg).replace(",","."));
         	tbMLonMin.setText(String.format("%.3f", min).replace(",","."));
         	
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
            tbSLatDeg.setText(String.format("%.0f", deg).replace(",","."));
            tbSLatMin.setText(String.valueOf(imin).replace(",","."));
            tbSLatSec.setText(String.format("%.2f", sec).replace(",","."));

            deg = Math.abs((int)coord.Longitude);
            frac = Math.abs(coord.Longitude) - deg;
            min = frac * 60;
            imin = (int)min;
            frac = min - imin;
            sec = frac * 60;

         	if (coord.Longitude > 0) bSLon.setText("E"); else bSLon.setText("W");
            tbSLonDeg.setText(String.format("%.0f", deg).replace(",","."));
            tbSLonMin.setText(String.valueOf(imin).replace(",","."));
            tbSLonSec.setText(String.format("%.2f", sec).replace(",","."));

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
            tbUY.setText(String.format("%.1f", nording).replace(",","."));
            tbUX.setText(String.format("%.1f", easting).replace(",","."));
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
		 
		 
		 //replace , with .
		 scoord=scoord.replace(",", ".");
		 
		 Coordinate newCoord = new Coordinate(scoord);
		 if (newCoord.Valid)
		 {
			 coord = newCoord;
			 return true;
		 } else
			 return false;
	 }
}
