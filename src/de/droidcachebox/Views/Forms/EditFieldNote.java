package de.droidcachebox.Views.Forms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditFieldNote extends Activity {
	private Intent aktIntent;
	private FieldNoteEntry fieldNote;
	Button bOK = null;
	Button bCancel = null;
	TextView tvCacheName = null;
	EditText etComment = null;
	ImageView ivTyp = null;
	TextView tvFounds = null;
	TextView tvDate = null;
	TextView tvTime = null;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_fieldnote);
        aktIntent = getIntent();

		// Übergebene FieldNote auslesen
        Bundle bundle = getIntent().getExtras();
        fieldNote = (FieldNoteEntry)bundle.getSerializable("FieldNote");
	
	
        tvCacheName = (TextView) findViewById(R.id.edfn_cachename);
        etComment = (EditText)findViewById(R.id.edfn_comment);
        ivTyp = (ImageView) findViewById(R.id.edfn_image);
        tvFounds = (TextView) findViewById(R.id.edfn_founds);
        tvDate = (TextView) findViewById(R.id.edfn_date);
        tvTime = (TextView) findViewById(R.id.edfn_time);
        
        // OK Button
        bOK = (Button) findViewById(R.id.edfn_ok);
        bOK.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        
        		fieldNote.comment = etComment.getText().toString();
        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		Bundle extras = new Bundle();
        		extras.putSerializable("FieldNoteResult", fieldNote);
        		aktIntent.putExtras(extras);
        		setResult(RESULT_OK, aktIntent);
        		finish();	            	
        	}
        });
        // Abbrechen Button
        bCancel = (Button) findViewById(R.id.edfn_cancel);
        bCancel.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		setResult(RESULT_CANCELED, aktIntent);
        		finish();	            	
        	}
        });

        
        // Default values
        etComment.setText(fieldNote.comment);
        // Translations
        bOK.setText(Global.Translations.Get("ok"));
		bCancel.setText(Global.Translations.Get("cancel"));
		tvCacheName.setText(fieldNote.CacheName);
		tvFounds.setText("Founds: #" + fieldNote.foundNumber);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = iso8601Format.format(fieldNote.timestamp);
		tvDate.setText("Date: " + sDate);
        iso8601Format = new SimpleDateFormat("HH:mm");
        String sTime = iso8601Format.format(fieldNote.timestamp);
		tvTime.setText("Time: " + sTime);
		ivTyp.setImageResource(fieldNote.typeIconId);
	}
}
