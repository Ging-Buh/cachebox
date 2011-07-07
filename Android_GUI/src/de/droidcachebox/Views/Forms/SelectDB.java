package de.droidcachebox.Views.Forms;

import java.io.File;
import CB_Core.Config;
import de.droidcachebox.FileList;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;

import CB_Core.FileIO;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
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
    public static boolean autoStart;
    private int autoStartTime = 10;
    private int autoStartCounter = 0;
	private String DBPath;
	private Intent aktIntent;
	private Button bNew;
	private Button bSelect;
	private Button bCancel;
	private Button bAutostart;
	private ListView lvFiles;
	CustomAdapter lvAdapter;
	public static File AktFile = null;
	private Handler mHandler;
	private Runnable mUpdateUITimerTask;
	
	public void onCreate(Bundle savedInstanceState) 
	{
    	ActivityUtils.onActivityCreateSetTheme(this);    	
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.selectdb);

		DBPath = FileIO.GetDirectoryName(Config.GetString("DatabasePath"));
		String DBFile = FileIO.GetFileName(Config.GetString("DatabasePath"));
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
                selectDB();
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

        		showDialog(123);
        	}
 
        });

		lvFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				stopTimer();
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
		

    	mUpdateUITimerTask = new Runnable() {
    	    public void run() {
    	        if (autoStartCounter == 0)
    	        {
    	    		stopTimer();
    	    		selectDB();
    	        }
    	        else
    	        {
    	            autoStartCounter--;
    	            bAutostart.setText(autoStartCounter + "\n" + Global.Translations.Get("confirm"));
    	        	mHandler.postDelayed(mUpdateUITimerTask, 1000);    	            
    	        }
    	    }
    	};
    	mHandler = new Handler();        
		
		
        autoStartTime = Config.GetInt("MultiDBAutoStartTime");
        autoStartCounter = autoStartTime;
        bAutostart.setText(autoStartCounter + "\n" + Global.Translations.Get("confirm"));
        setAutoStartText();
        if ((autoStart && autoStartTime > 0) && (AktFile != null))
        {
        	mHandler.postDelayed(mUpdateUITimerTask, 1000);
        }
        else
            stopTimer();

	}

	
	protected void selectDB() {
        Config.Set("MultiDBAutoStartTime", autoStartTime);
        Config.Set("MultiDBAsk", autoStartTime >= 0);
		
		String name = AktFile.getName();
//    	Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();

        String path = DBPath + "/" + name;
//    	Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
        
        Config.Set("DatabasePath", path);
        Config.AcceptChanges();
		
		aktIntent.putExtra("SOMETHING", "EXTRAS");
		setResult(RESULT_OK, aktIntent);
		AktFile = null;
		finish();
	}


	private void setAutoStartText() {
	    if (autoStartTime < 0)
            bAutostart.setText(Global.Translations.Get("AutoStart") + "\n" + Global.Translations.Get("StartWithoutSelection"));
        else if (autoStartTime == 0)
            bAutostart.setText(Global.Translations.Get("AutoStart") + "\n" + Global.Translations.Get("AutoStartDisabled"));
        else
            bAutostart.setText(Global.Translations.Get("AutoStart") + "\n" + Global.Translations.Get("AutoStartTime").replace("%s", String.valueOf(autoStartTime)));		
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
		mHandler.removeCallbacks(mUpdateUITimerTask);
		bAutostart.setText(Global.Translations.Get("confirm"));
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) 
        {
        case 123:
        	final CharSequence[] cs = new String[6];
            cs[0] = Global.Translations.Get("StartWithoutSelection");
            cs[1] = Global.Translations.Get("AutoStartDisabled");
            cs[2] = Global.Translations.Get("AutoStartTime").replace("%s", "5");
            cs[3] = Global.Translations.Get("AutoStartTime").replace("%s", "10");
            cs[4] = Global.Translations.Get("AutoStartTime").replace("%s", "25");
            cs[5] = Global.Translations.Get("AutoStartTime").replace("%s", "60");
        	
            return new AlertDialog.Builder(this)
            .setTitle("Titel")
            .setItems(cs, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    switch (which)
                    {
		            case 0:
			            autoStartTime = -1;
			            setAutoStartText();
		               	break;
                    case 1:
			            autoStartTime = 0;
			            setAutoStartText();
                    	break;
                    case 2:
                    	autoStartTime = 5;
            			setAutoStartText();
                    	break;
                    case 3:
			            autoStartTime = 10;
			            setAutoStartText();
                    	break;
                    case 4:
			            autoStartTime = 25;
			            setAutoStartText();
                    	break;
                    case 5:
			            autoStartTime = 60;
			            setAutoStartText();
                    	break;
               
                    }
                	
                }
            })
            .create();
        }
        return null;
    }
}
