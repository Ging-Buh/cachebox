package de.cachebox_test.Views.Forms;

import CB_Core.GlobalCore;
import CB_Core.Enums.CacheTypes;
import CB_Core.TranslationEngine.LangStrings;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.cachebox_test.R;
import de.cachebox_test.Ui.ActivityUtils;

public class EditWaypoint extends Activity
{
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
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_waypoint);

		// Übergebenen Waypoint auslesen
		Bundle bundle = getIntent().getExtras();
		waypoint = (Waypoint) bundle.getSerializable("Waypoint");

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
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getWaypointTypes());
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
		s1.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
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

			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});

		// Coordinate Button
		bCoord.setText(waypoint.Pos.FormatCoordinate());
		bCoord.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Koordinaten Dialog öffnen
				Intent coordIntent = new Intent().setClass(bCoord.getContext(), EditCoordinate.class);
				Bundle b = new Bundle();
				b.putSerializable("Coord", waypoint.Pos);
				coordIntent.putExtras(b);
				startActivityForResult(coordIntent, 0);
			}
		});

		// OK Button
		bOK = (Button) findViewById(R.id.edwp_ok);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

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
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				aktIntent.putExtra("SOMETHING", "EXTRAS");
				setResult(RESULT_CANCELED, aktIntent);
				finish();
			}
		});

		// Default values
		etDescription.setText(waypoint.Description);
		etClue.setText(waypoint.Clue);
		// Translations
		bOK.setText(GlobalCore.Translations.Get("ok"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));
		tvCacheName.setText(GlobalCore.SelectedCache().Name);
		tvTyp.setText(GlobalCore.Translations.Get("type"));
		tvTitle.setText(GlobalCore.Translations.Get("Title"));
		tvDescription.setText(GlobalCore.Translations.Get("Description"));
		tvClue.setText(GlobalCore.Translations.Get("Clue"));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null) return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Coordinate coord = (Coordinate) bundle.getSerializable("CoordResult");
			if (coord != null)
			{
				waypoint.Pos.Latitude = coord.Latitude;
				waypoint.Pos.Longitude = coord.Longitude;
				bCoord.setText(waypoint.Pos.FormatCoordinate());
			}
		}
	}

	private String[] getWaypointTypes()
	{
		final LangStrings ls = GlobalCore.Translations;
		return new String[]
			{ ls.Get("Reference"), ls.Get("StageofMulti"), ls.Get("Question2Answer"), ls.Get("Trailhead"), ls.Get("Parking"),
					ls.Get("Final") };
	}

	/** hook into menu button for activity */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_editwaypoint, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case R.id.mew_paste:
			ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (clipboardManager.hasText())
			{
				String sCoord = clipboardManager.getText().toString();
				Coordinate coord = new Coordinate(sCoord);
				if (coord.Valid)
				{
					waypoint.Pos.Latitude = coord.Latitude;
					waypoint.Pos.Longitude = coord.Longitude;
					waypoint.Pos.Valid = true;
					bCoord.setText(waypoint.Pos.FormatCoordinate());
				}
				else
				{
					Toast.makeText(getApplicationContext(), "No valid Coordinate in Clipboard!", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
