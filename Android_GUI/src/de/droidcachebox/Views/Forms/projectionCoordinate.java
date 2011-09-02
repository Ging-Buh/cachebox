package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import CB_Core.Types.Coordinate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class projectionCoordinate extends Activity {
	private Intent aktIntent;
	
	public projectionCoordinate Me;
	
	Coordinate coord;
	String Title;
	boolean radius=false;
	
// Allgemein
	TextView TitleView;
	TextView descDistance;
	TextView descBearing;
	EditText valueDistance;
	EditText valueBearing;
	Button bCoord;
	private LinearLayout NumPadLayout;

	private LinearLayout TitleLayout;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.projection_coordinate);

		Me=this;
		
		// übergebene Koordinate auslesen
		Bundle bundle = getIntent().getExtras();
		coord = (Coordinate)bundle.getSerializable("Coord");
		Title = (String)bundle.getSerializable("Title");
		radius = (Boolean)bundle.getSerializable("Radius");
		
	        
        aktIntent = getIntent();
        
		
		
		// Allgemein
        TitleView=(TextView)this.findViewById(R.id.proco_titleview);
        TitleLayout=(LinearLayout)this.findViewById(R.id.proco_titlelayout);
       	descDistance=(TextView)this.findViewById(R.id.proco_dist_direction);
        descBearing=(TextView)this.findViewById(R.id.proco_bear_directioin);
        valueDistance=(EditText)this.findViewById(R.id.proco_dist_value);
        valueBearing=(EditText)this.findViewById(R.id.proco_bear_value);
        bCoord = (Button)this.findViewById(R.id.proco_buttoncoord);
        NumPadLayout=(LinearLayout)this.findViewById(R.id.NumPadLayout);
        descDistance.setOnFocusChangeListener(onFocusChange);
        descBearing.setOnFocusChangeListener(onFocusChange);
        
        if(radius)
        {
        	descBearing.setVisibility(View.GONE);
        	valueBearing.setVisibility(View.GONE);
        	((TextView)this.findViewById(R.id.proco_bear_einheit)).setVisibility(View.GONE);
        }
        
        valueDistance.setText("0");
        valueBearing.setText("0");
        
        if(Title==null || Title.equals(""))
		{
        	TitleLayout.setVisibility(View.GONE);
		}
        else
		{
        	TitleView.setText(Title);
		}
        
        Button bOK = (Button) findViewById(R.id.proco_ok);
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
        Button bCancel = (Button) findViewById(R.id.proco_cancel);
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
       
        // Coordinate Button
        bCoord.setText(coord.FormatCoordinate());
        bCoord.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// Koordinaten Dialog öffnen
        		Intent coordIntent = new Intent().setClass(bCoord.getContext(), EditCoordinate.class);
    	        Bundle b = new Bundle();
    	        b.putSerializable("Coord", coord);
    	        coordIntent.putExtras(b);
        		startActivityForResult(coordIntent, 0);
        	}
        });

      
        valueDistance.setOnFocusChangeListener(onFocusChange);
        valueBearing.setOnFocusChangeListener(onFocusChange);
        
        // Translations
        bOK.setText(Global.Translations.Get("ok"));
        bCancel.setText(Global.Translations.Get("cancel"));
        descBearing.setText(Global.Translations.Get("Bearing"));
        descDistance.setText(radius? "Radius" : Global.Translations.Get("Distance"));
        
       //Disable OK/CANCEL from NumPadLayout
        ((Button)this.findViewById(R.id.positiveButton)).setVisibility(View.INVISIBLE);
        ((Button)this.findViewById(R.id.negativeButton)).setVisibility(View.INVISIBLE);
	 }

	private boolean editWidthSetted=false;
	private OnFocusChangeListener onFocusChange = new OnFocusChangeListener() 
	{
		@Override
		public void onFocusChange(View arg0, boolean arg1) 
		{
			if(!editWidthSetted)
			{
				 //after the first show set the width to the Bigger value
		        Integer s1= descBearing.getWidth();
		        Integer s2= descDistance.getWidth();
		        int max =Math.max(s1, s2);
		        descBearing.setWidth(max);
		        descDistance.setWidth(max);
		        editWidthSetted=true;
			}
			
			
			//wenn Eine EditBox den Focus bekommt!
			if(EditText.class==(arg0.getClass()) && arg1)
			{// unterscheide ob ein INT oder DBL Eingabe Feld
						
				ActivityUtils.initialNumPadDbl((Activity)Me,NumPadLayout,(EditText)arg0,((EditText) arg0).getText().toString(),null,null);
				
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data) {
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Coordinate returnCoord = (Coordinate)bundle.getSerializable("CoordResult");
			if (returnCoord != null)
			{
				coord=returnCoord;
		        bCoord.setText(coord.FormatCoordinate());
			}
		}
    }

	
	  
	 private boolean parseView()
	 {
		 		
		 double Bearing = Double.parseDouble(valueBearing.getText().toString());
		 double Distance = Double.parseDouble(valueDistance.getText().toString());
		 
		 Coordinate newCoord = Coordinate.Project(coord.Latitude, coord.Longitude, Bearing, Distance);
		 
		 if (newCoord.Valid)
		 {
			 coord = newCoord;
			 return true;
		 } else
			 return false;
	 }
}
