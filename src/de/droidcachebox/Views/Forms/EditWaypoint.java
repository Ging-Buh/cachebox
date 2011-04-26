package de.droidcachebox.Views.Forms;

import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Geocaching.Cache.CacheTypes;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class EditWaypoint extends Activity {
	private Intent aktIntent;
	private Waypoint waypoint;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_waypoint);

		// Übergebenen Waypoint auslesen
        Bundle bundle = getIntent().getExtras();
        waypoint = (Waypoint)bundle.getSerializable("Waypoint");
        
        
        final EditText et = (EditText) findViewById(R.id.edwp_titleedit);
        et.setText(waypoint.Title);
        aktIntent = getIntent();

		// Spinner für CacheType initialisieren
		Spinner s1 = (Spinner) findViewById(R.id.edwp_typspinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mStrings);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s1.setAdapter(adapter);
		
		// Spinner initialisieren
		switch (waypoint.Type)
		{
			case ReferencePoint:
				s1.setSelection(0);
				break;
			case MultiStage:
				s1.setSelection(1);
				break;
			case MultiQuestion:
				s1.setSelection(2);
				break;
			case Trailhead:
				s1.setSelection(3);
				break;
			case ParkingArea:
				s1.setSelection(4);
				break;
			case Final:
				s1.setSelection(5);
				break;
		}

		// Rückgabewert von Spinner
        s1.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    	switch (position)
                    	{
                    		case 0:
                    			waypoint.Type = CacheTypes.ReferencePoint;
                    			break;
                    		case 1:
                    			waypoint.Type = CacheTypes.MultiStage;
                    			break;
                    		case 2:
                    			waypoint.Type = CacheTypes.MultiQuestion;
                    			break;
                    		case 3:
                    			waypoint.Type = CacheTypes.Trailhead;
                    			break;
                    		case 4:
                    			waypoint.Type = CacheTypes.ParkingArea;
                    			break;
                    		case 5:
                    			waypoint.Type = CacheTypes.Final;
                    			break;
                    	}
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    	
                    }
                });
	        
        // Coordinate Button
        final Button bCoord = (Button) findViewById(R.id.edwp_buttoncoord);
        bCoord.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// Koordinaten Dialog öffnen
        		Intent coordIntent = new Intent().setClass(bCoord.getContext(), EditCoordinate.class);
    	        Bundle b = new Bundle();
    	        b.putSerializable("Coord", waypoint.Coordinate);
    	        coordIntent.putExtras(b);
        		startActivityForResult(coordIntent, 0);
        	}
        });

        // OK Button
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
        // Abbrechen Button
        Button butc = (Button) findViewById(R.id.edwp_cancel);
        butc.setText("Cancel");
        butc.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {

        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		setResult(RESULT_CANCELED, aktIntent);
        		finish();	            	
        	}
        });
	        
	 }

	 private static final String[] mStrings = {
		 "Reference", "Stage of a Multicache", "Question to answer", "Trailhead", "Parking Area", "Final"
	 };
}
