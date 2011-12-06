package de.cachebox_test.Views.Forms;

import java.io.File;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Types.Categories;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import de.cachebox_test.FileList;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.DB.AndroidDB;
import de.cachebox_test.Ui.ActivityUtils;

public class SelectDB extends Activity
{
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

	public static SelectDB Me;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.selectdb);

		Me = this;

		DBPath = FileIO.GetDirectoryName(Config.settings.DatabasePath.getValue());
		String DBFile = FileIO.GetFileName(Config.settings.DatabasePath.getValue());
		// Toast.makeText(getApplicationContext(), DBPath,
		// Toast.LENGTH_LONG).show();

		aktIntent = getIntent();

		lvFiles = (ListView) findViewById(R.id.sdb_list);
		final FileList files = new FileList(Config.WorkPath, "DB3");
		for (File file : files)
		{
			if (file.getName().equalsIgnoreCase(DBFile)) AktFile = file;
		}

		lvAdapter = new CustomAdapter(getApplicationContext(), files);
		lvFiles.setAdapter(lvAdapter);

		bNew = (Button) findViewById(R.id.sdb_new);
		bSelect = (Button) findViewById(R.id.sdb_select);
		bCancel = (Button) findViewById(R.id.sdb_cancel);
		bAutostart = (Button) findViewById(R.id.sdb_autostart);

		// New Button
		bNew.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				stopTimer();
				StringInputBox.Show(GlobalCore.Translations.Get("NewDB"), GlobalCore.Translations.Get("InsNewDBName"), "NewDB",
						DialogListnerNewDB, Me);
			}
		});

		// Select Button
		bSelect.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
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
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				stopTimer();
				aktIntent.putExtra("SOMETHING", "EXTRAS");
				setResult(RESULT_CANCELED, aktIntent);
				finish();
			}
		});

		// AutoStart Button
		bAutostart.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				stopTimer();

				showDialog(123);
			}

		});

		lvFiles.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{

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
		bNew.setText(GlobalCore.Translations.Get("NewDB"));
		bSelect.setText(GlobalCore.Translations.Get("confirm"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));
		bAutostart.setText(GlobalCore.Translations.Get("StartWithoutSelection"));

		lvFiles.setBackgroundColor(Config.settings.nightMode.getValue() ? R.color.Night_EmptyBackground : R.color.Day_EmptyBackground);
		lvFiles.setCacheColorHint(R.color.Day_TitleBarColor);
		lvFiles.setDividerHeight(5);
		lvFiles.setDivider(lvFiles.getBackground());

		mUpdateUITimerTask = new Runnable()
		{
			public void run()
			{
				if (autoStartCounter == 0)
				{
					stopTimer();
					selectDB();
				}
				else
				{
					autoStartCounter--;
					bAutostart.setText(autoStartCounter + "\n" + GlobalCore.Translations.Get("confirm"));
					mHandler.postDelayed(mUpdateUITimerTask, 1000);
				}
			}
		};
		mHandler = new Handler();

		autoStartTime = Config.settings.MultiDBAutoStartTime.getValue();
		if (autoStartTime > 0)
		{
			autoStartCounter = autoStartTime;
			bAutostart.setText(autoStartCounter + "\n" + GlobalCore.Translations.Get("confirm"));
			setAutoStartText();
			if ((autoStart && autoStartTime > 0) && (AktFile != null))
			{
				mHandler.postDelayed(mUpdateUITimerTask, 1000);
			}
			else
				stopTimer();
		}
	}

	protected final DialogInterface.OnClickListener DialogListnerNewDB = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			String text = StringInputBox.editText.getText().toString();
			// Behandle das ergebniss
			switch (button)
			{
			case -1: // ok Clicket

				dialog.dismiss();

				String FilterString = Config.settings.Filter.getValue();
				Global.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0])
						: new FilterProperties(FilterString);
				String sqlWhere = Global.LastFilter.getSqlWhere();

				// initialize Database
				Database.Data = new AndroidDB(DatabaseType.CacheBox, Me);
				Config.settings.DatabasePath.setValue(Config.WorkPath + "/" + text + ".db3");
				String database = Config.settings.DatabasePath.getValue();
				Database.Data.StartUp(database);
				Config.AcceptChanges();

				GlobalCore.Categories = new Categories();
				Database.Data.GPXFilenameUpdateCacheCount();

				CacheListDAO cacheListDAO = new CacheListDAO();
				cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

				Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, Me);
				if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
				Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

				Config.AcceptChanges();
				AktFile = new File(database);
				selectDB();

				break;
			case -2: // cancel clicket

				break;
			case -3:

				break;
			}

			dialog.dismiss();

		}

	};

	protected void selectDB()
	{
		Config.settings.MultiDBAutoStartTime.setValue(autoStartTime);
		Config.settings.MultiDBAsk.setValue(autoStartTime >= 0);

		String name = AktFile.getName();
		// Toast.makeText(getApplicationContext(), name,
		// Toast.LENGTH_SHORT).show();

		String path = DBPath + "/" + name;
		// Toast.makeText(getApplicationContext(), path,
		// Toast.LENGTH_SHORT).show();

		Config.settings.DatabasePath.setValue(path);
		Config.AcceptChanges();

		// reload settings for get filter form selected DB
		Config.settings.ReadFromDB();

		aktIntent.putExtra("SOMETHING", "EXTRAS");
		setResult(RESULT_OK, aktIntent);
		AktFile = null;
		finish();
	}

	private void setAutoStartText()
	{
		if (autoStartTime < 0) bAutostart.setText(GlobalCore.Translations.Get("AutoStart") + "\n"
				+ GlobalCore.Translations.Get("StartWithoutSelection"));
		else if (autoStartTime == 0) bAutostart.setText(GlobalCore.Translations.Get("AutoStart") + "\n"
				+ GlobalCore.Translations.Get("AutoStartDisabled"));
		else
			bAutostart.setText(GlobalCore.Translations.Get("AutoStart") + "\n"
					+ GlobalCore.Translations.Get("AutoStartTime", String.valueOf(autoStartTime)));
	}

	public class CustomAdapter extends BaseAdapter
	{

		private Context context;
		private FileList files;

		public CustomAdapter(Context context, FileList files)
		{
			this.context = context;
			this.files = files;
		}

		public void setFiles(FileList files)
		{
			this.files = files;

		}

		public int getCount()
		{
			return files.size();
		}

		public Object getItem(int position)
		{
			return files.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			Boolean BackGroundChanger = ((position % 2) == 1);
			SelectDBItem v = new SelectDBItem(context, files.get(position), BackGroundChanger);
			return v;
		}

		/*
		 * public void onClick(View v) { Log.v(LOG_TAG, "Row button clicked"); }
		 */

	}

	private void stopTimer()
	{
		mHandler.removeCallbacks(mUpdateUITimerTask);
		bAutostart.setText(GlobalCore.Translations.Get("confirm"));
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
		case 123:
			final CharSequence[] cs = new String[6];
			cs[0] = GlobalCore.Translations.Get("StartWithoutSelection");
			cs[1] = GlobalCore.Translations.Get("AutoStartDisabled");
			cs[2] = GlobalCore.Translations.Get("AutoStartTime", "5");
			cs[3] = GlobalCore.Translations.Get("AutoStartTime", "10");
			cs[4] = GlobalCore.Translations.Get("AutoStartTime", "25");
			cs[5] = GlobalCore.Translations.Get("AutoStartTime", "60");

			return new AlertDialog.Builder(this).setTitle("Titel").setItems(cs, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{

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
			}).create();
		}
		return null;
	}
}
