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

public class EditCoordinate extends Activity {
	private Intent aktIntent;
	
	 public void onCreate(Bundle savedInstanceState) 
	 {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.edit_coordinate);

	        Spinner s1 = (Spinner) findViewById(R.id.edco_typspinner);
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                android.R.layout.simple_spinner_item, mStrings);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        s1.setAdapter(adapter);
	        Bundle bundle = getIntent().getExtras();
	        Coordinate coord = (Coordinate)bundle.getSerializable("Coord");
	        EditText et = (EditText) findViewById(R.id.edco_titleedit);
	        et.setText(coord.Latitude + " - " + coord.Longitude);
	        aktIntent = getIntent();
	        Button but = (Button) findViewById(R.id.edco_ok);
	        but.setOnClickListener(new OnClickListener() {
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
	        Button butc = (Button) findViewById(R.id.edco_cancel);
	        butc.setOnClickListener(new OnClickListener() {
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
