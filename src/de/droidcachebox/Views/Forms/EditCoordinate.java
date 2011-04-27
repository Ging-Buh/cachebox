package de.droidcachebox.Views.Forms;

import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Coordinate;
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
	
	 public void onCreate(Bundle savedInstanceState) 
	 {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.edit_coordinate);

	        
	        // übergebene Koordinate auslesen
	        Bundle bundle = getIntent().getExtras();
	        Coordinate coord = (Coordinate)bundle.getSerializable("Coord");
	        
	        aktIntent = getIntent();

			final TableRow trDec =(TableRow)this.findViewById(R.id.edco_table_dec);
			trDec.setVisibility(View.GONE);
			final TableRow trMin =(TableRow)this.findViewById(R.id.edco_table_min);
			trMin.setVisibility(View.VISIBLE);
			
			final ToggleButton bDec = (ToggleButton) findViewById(R.id.edco_dec);
	        final ToggleButton bMin = (ToggleButton) findViewById(R.id.edco_min);
			final ToggleButton bSec = (ToggleButton) findViewById(R.id.edco_sec);
			final ToggleButton bUtm = (ToggleButton) findViewById(R.id.edco_utm);
	        
	        bDec.setText("Dec");
	        bDec.setTextOff("Dec");
	        bDec.setTextOn("Dec");
	        bDec.setOnClickListener(new OnClickListener(){
	        	@Override
	        	public void onClick(View v) {
	            	trMin.setVisibility(View.GONE);	        		
	            	trDec.setVisibility(View.VISIBLE);	        		
	            	bDec.setChecked(true);
	            	bMin.setChecked(false);
	            	bSec.setChecked(false);
	            	bUtm.setChecked(false);
	        	}
	        });
	        bMin.setText("Min");
	        bMin.setTextOff("Min");
	        bMin.setTextOn("Min");
	        bMin.setChecked(true);
	        bMin.setOnClickListener(new OnClickListener(){
	        	@Override
	        	public void onClick(View v) {
	            	trDec.setVisibility(View.GONE);	        		
	            	trMin.setVisibility(View.VISIBLE);	        			        		
	            	bDec.setChecked(false);
	            	bMin.setChecked(true);
	            	bSec.setChecked(false);
	            	bUtm.setChecked(false);
	        	}
	        });
	        bSec.setText("Sec");
	        bSec.setTextOff("Sec");
	        bSec.setTextOn("Sec");
	        bSec.setOnClickListener(new OnClickListener(){
	        	@Override
	        	public void onClick(View v) {
	            	trMin.setVisibility(View.GONE);	        		
	            	trDec.setVisibility(View.GONE);	        		
	            	bDec.setChecked(false);
	            	bMin.setChecked(false);
	            	bSec.setChecked(true);
	            	bUtm.setChecked(false);
	        	}
	        });
	        bUtm.setText("UTM");
	        bUtm.setTextOff("UTM");
	        bUtm.setTextOn("UTM");
	        bUtm.setOnClickListener(new OnClickListener(){
	        	@Override
	        	public void onClick(View v) {
	            	trMin.setVisibility(View.GONE);	        		
	            	trDec.setVisibility(View.GONE);	        		
	            	bDec.setChecked(false);
	            	bMin.setChecked(false);
	            	bSec.setChecked(false);
	            	bUtm.setChecked(true);
	        	}
	        });

	        Button bOK = (Button) findViewById(R.id.edco_ok);
	        bOK.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {

	            	aktIntent.putExtra("SOMETHING", "EXTRAS");
	            	Bundle extras = new Bundle();
	    	        extras.putSerializable("CoordResult", new Coordinate(49, 13));
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
	        
	 }

	 private static final String[] mStrings = {
		 "Reference", "Stage of a Multicache", "Question to answer", "Trailhead", "Parking Area", "Final"
	 };
}
