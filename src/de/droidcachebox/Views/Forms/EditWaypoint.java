package de.droidcachebox.Views.Forms;

import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class EditWaypoint extends Activity {
	private Intent aktIntent;
	private Waypoint waypoint;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_waypoint);

		Spinner s1 = (Spinner) findViewById(R.id.edwp_typspinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mStrings);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s1.setAdapter(adapter);
	        
	        Bundle bundle = getIntent().getExtras();
	        waypoint = (Waypoint)bundle.getSerializable("Waypoint");
	        final EditText et = (EditText) findViewById(R.id.edwp_titleedit);
	        et.setText(waypoint.Title);
	        aktIntent = getIntent();
	        
	        Button but = (Button) findViewById(R.id.edwp_ok);
	        but.setText("OK");
	        but.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {

	            	waypoint.Title = et.getText().toString();
	            	aktIntent.putExtra("SOMETHING", "EXTRAS");
	            	Bundle extras = new Bundle();
	    	        extras.putSerializable("WaypointResult", waypoint);
	            	aktIntent.putExtras(extras);
	            	setResult(RESULT_OK, aktIntent);
	            	finish();	            	
	            }
	          });
	        Button butc = (Button) findViewById(R.id.edwp_cancel);
	        butc.setText("Cancel");
	        butc.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {

	            	aktIntent.putExtra("SOMETHING", "EXTRAS");
//	            	Bundle extras = new Bundle();
//	    	        extras.putSerializable("WaypointResult", null);
//	            	aktIntent.putExtras(extras);
	            	setResult(RESULT_CANCELED, aktIntent);
	            	finish();	            	
	            }
	          });
	        
	 }

	 private static final String[] mStrings = {
		 "Reference", "Stage of a Multicache", "Question to answer", "Trailhead", "Parking Area", "Final"
	 };
}
