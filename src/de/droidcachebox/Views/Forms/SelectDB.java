package de.droidcachebox.Views.Forms;

import java.io.File;
import java.util.ArrayList;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.FileList;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.splash;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.WaypointViewItem;
import de.droidcachebox.Views.WaypointView.CustomAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SelectDB extends Activity {
	private String DBPath;
	private Intent aktIntent;
	private Button bNew;
	private Button bSelect;
	private Button bCancel;
	private Button bAutostart;
	private ListView lvFiles;
	CustomAdapter lvAdapter;
	public static File AktFile = null;
	
	public void onCreate(Bundle savedInstanceState) 
	{
    	ActivityUtils.onActivityCreateSetTheme(this);    	
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.selectdb);

		DBPath = Global.GetDirectoryName(Config.GetString("DatabasePath"));
		String DBFile = Global.GetFileName(Config.GetString("DatabasePath"));
//		Toast.makeText(getApplicationContext(), DBPath, Toast.LENGTH_LONG).show();
		
		aktIntent = getIntent();

		lvFiles = (ListView) findViewById(R.id.sdb_list);
		final FileList files = new FileList(Config.WorkPath, "DB3");
		for (File file : files)
		{
			if (file.getName().equalsIgnoreCase(DBFile))
				AktFile = file;
		}
		
		lvAdapter = new CustomAdapter(getApplicationContext(), files);
		lvFiles.setAdapter(lvAdapter);
		
		bNew = (Button) findViewById(R.id.sdb_new);
		bSelect = (Button) findViewById(R.id.sdb_select);
		bCancel = (Button) findViewById(R.id.sdb_cancel);
		bAutostart = (Button) findViewById(R.id.sdb_autostart);

		// New Button
        bNew.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        	}
        });

        // Select Button
        bSelect.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
                stopTimer();
                if (AktFile == null)
                {
                	Toast.makeText(getApplicationContext(), "Please select Database!", Toast.LENGTH_SHORT).show();
                	return;
                }
/*
                Config.Set("MultiDBAutoStartTime", autoStartTime);
                Config.Set("MultiDBAsk", autoStartTime >= 0);
                Config.AcceptChanges();
*/
                String name = AktFile.getName();
//            	Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();

                String path = DBPath + "/" + name;
//            	Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
                
                Config.Set("DatabasePath", path);
                Config.AcceptChanges();
        		
        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		setResult(RESULT_OK, aktIntent);
        		AktFile = null;
        		finish();
        	}
        });

        // Cancel Button
        bCancel.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		stopTimer();
        		aktIntent.putExtra("SOMETHING", "EXTRAS");
        		setResult(RESULT_CANCELED, aktIntent);
        		finish();
        	}
        });

		// AutoStart Button
        bAutostart.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		stopTimer();
/*
            stopTimer();
            Cachebox.Views.FormNewDB form = new Cachebox.Views.FormNewDB(DBPath);
            DialogResult result = form.ShowDialog();

            if (result == DialogResult.OK)
            {
                dragList1.Items.Clear();

//                string DBPath = System.IO.Path.GetDirectoryName(Config.GetString("DatabasePath"));

                System.IO.DirectoryInfo rootDir = new System.IO.DirectoryInfo(DBPath);
                System.IO.FileInfo[] DBs = rootDir.GetFiles("*.sdf");

                InitList(DBs);
            }

 */
         	}
 
        });

		lvFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				File file = null;
				if (arg2 >= 0)
				{
					file = files.get(arg2);
					SelectDB.AktFile = file;
				}
			}
		});
        // Translations
        bNew.setText(Global.Translations.Get("NewDB"));
        bSelect.setText(Global.Translations.Get("confirm"));
        bCancel.setText(Global.Translations.Get("cancel"));
        bAutostart.setText(Global.Translations.Get("StartWithoutSelection"));

		lvFiles.setBackgroundColor(Config.GetBool("nightMode")? R.color.Night_EmptyBackground : R.color.Day_EmptyBackground);
		lvFiles.setCacheColorHint(R.color.Day_TitleBarColor);
		lvFiles.setDividerHeight(5);
		lvFiles.setDivider(lvFiles.getBackground());
	}

	public class CustomAdapter extends BaseAdapter {
		  
	    private Context context;
	    private FileList files;
	 
	    public CustomAdapter(Context context, FileList files ) {
	        this.context = context;
	        this.files = files;
	    }
	 
	    public void setFiles(FileList files) {
	    	this.files = files;
	    
	    }
	    public int getCount() {
    		return files.size();
	    }
	 
	    public Object getItem(int position) {
	    	return files.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	Boolean BackGroundChanger = ((position % 2) == 1);
	    	SelectDBItem v = new SelectDBItem(context, files.get(position), BackGroundChanger);
	    	return v;
	    }
	 
	    /*public void onClick(View v) {
	            Log.v(LOG_TAG, "Row button clicked");
	    }*/
	 
	}

	
	
	private void stopTimer()
	{
		
	}
	
	
}
