package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedLangChangedEvent;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Geocaching.Cache.CacheTypes;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class EditWaypoint extends Activity {
	private Intent aktIntent;
	private Waypoint waypoint;
	Button bCoord = null;
	Button bOK = null;
	Button bCancel = null;
	TextView tvCacheName = null;
	TextView tvTyp = null;
	TextView tvTitle = null;
	TextView tvDescription = null;
	EditText etDescription = null;
	TextView tvClue = null;
	EditText etClue = null;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_waypoint);

		// Übergebenen Waypoint auslesen
        Bundle bundle = getIntent().getExtras();
        waypoint = (Waypoint)bundle.getSerializable("Waypoint");
        

        bCoord = (Button) findViewById(R.id.edwp_buttoncoord);
        bCoord.setFocusable(true);
        bCoord.setFocusableInTouchMode(true);
		bCoord.requestFocus();
		final EditText et = (EditText) findViewById(R.id.edwp_titleedit);
        tvCacheName = (TextView) findViewById(R.id.edwp_cachename);
        tvTyp = (TextView) findViewById(R.id.edwp_typtext);
        tvTitle = (TextView) findViewById(R.id.edwp_titletext);
        tvDescription = (TextView) findViewById(R.id.edwp_descriptiontext);
        etDescription = (EditText) findViewById(R.id.edwp_descriptionedit);
        tvClue = (TextView) findViewById(R.id.edwp_cluetext);
        etClue = (EditText) findViewById(R.id.edwp_clueedit);
        
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
        bCoord.setText(waypoint.Coordinate.FormatCoordinate());
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
        bOK = (Button) findViewById(R.id.edwp_ok);
        bOK.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		
        		waypoint.Title = et.getText().toString();
        		waypoint.Description = etDescription.getText().toString();
        		waypoint.Clue = etClue.getText().toString();
        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		Bundle extras = new Bundle();
        		extras.putSerializable("WaypointResult", waypoint);
        		aktIntent.putExtras(extras);
        		setResult(RESULT_OK, aktIntent);
        		finish();	            	
        	}
        });
        // Abbrechen Button
        bCancel = (Button) findViewById(R.id.edwp_cancel);
        bCancel.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		setResult(RESULT_CANCELED, aktIntent);
        		finish();	            	
        	}
        });

        // Default values
        etDescription.setText(waypoint.Description);
        etClue.setText(waypoint.Clue);
        // Translations
        bOK.setText(Global.Translations.Get("ok"));
		bCancel.setText(Global.Translations.Get("cancel"));
		tvCacheName.setText(Global.SelectedCache().Name);
		tvTyp.setText(Global.Translations.Get("type"));
		tvTitle.setText(Global.Translations.Get("Title"));
		tvDescription.setText(Global.Translations.Get("Description"));
		tvClue.setText("Clue");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data) {
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Coordinate coord = (Coordinate)bundle.getSerializable("CoordResult");
			if (coord != null)
			{
				waypoint.Coordinate.Latitude = coord.Latitude;
				waypoint.Coordinate.Longitude = coord.Longitude;
		        bCoord.setText(waypoint.Coordinate.FormatCoordinate());
			}
		}
    }

    private static final String[] mStrings = {
    	"Reference", "Stage of a Multicache", "Question to answer", "Trailhead", "Parking Area", "Final"
    };

}
