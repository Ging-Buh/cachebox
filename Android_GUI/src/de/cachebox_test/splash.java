package de.cachebox_test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.mapsforge.map.android.graphics.ext_AndroidGraphicFactory;
import org.mapsforge.map.model.DisplayModel;

import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;
import CB_UI_Base.GL_UI.DisplayType;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.Math.devicesSizes;
import CB_Utils.Log.Logger;
import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.PlatformSettings.iPlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingEncryptedString;
import CB_Utils.Settings.SettingEnum;
import CB_Utils.Settings.SettingFile;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingIntArray;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingTime;
import CB_Utils.Util.FileIO;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFiles;

import de.cachebox_test.Components.copyAssetFolder;
import de.cachebox_test.DB.AndroidDB;
import de.cachebox_test.Views.Forms.MessageBox;

public class splash extends Activity
{

	public static Activity splashActivity;
	final Context context = this;
	Handler handler;
	Bitmap bitmap;
	Dialog pWaitD;
	String GcCode = null;
	String guid = null;
	String name = null;
	String GpxPath = null;
	private SharedPreferences androidSetting;
	private SharedPreferences.Editor androidSettingEditor;
	String workPath;
	IgetFolderReturnListner getFolderReturnListner;
	int AdditionalWorkPathCount;
	SharedPreferences AndroidSettings;
	MessageBox msg;

	ArrayList<String> AdditionalWorkPathArray;

	private boolean mOriantationRestart = false;
	private static devicesSizes ui;
	private boolean isLandscape = false;
	private boolean ToastEx = false;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.splash);

		DisplayMetrics displaymetrics = this.getResources().getDisplayMetrics();

		GlobalCore.displayDensity = displaymetrics.density;
		int h = displaymetrics.heightPixels;
		int w = displaymetrics.widthPixels;

		int sw = h > w ? w : h;
		sw /= GlobalCore.displayDensity;

		// check if tablet
		GlobalCore.isTab = sw > 400 ? true : false;

		int dpH = (int) (h / GlobalCore.displayDensity + 0.5);
		int dpW = (int) (w / GlobalCore.displayDensity + 0.5);

		if (dpH * dpW >= 960 * 720) GlobalCore.displayType = DisplayType.xLarge;
		else if (dpH * dpW >= 640 * 480) GlobalCore.displayType = DisplayType.Large;
		else if (dpH * dpW >= 470 * 320) GlobalCore.displayType = DisplayType.Normal;
		else
			GlobalCore.displayType = DisplayType.Small;

		// überprüfen, ob ACB im Hochformat oder Querformat gestartet wurde.
		// Hochformat -> Handymodus
		// Querformat -> Tablet-Modus
		if (w > h) isLandscape = true;

		// Porträt erzwingen wenn Normal oder Small display
		if (isLandscape && (GlobalCore.displayType == DisplayType.Normal || GlobalCore.displayType == DisplayType.Small))
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// chek if use small skin
		GlobalCore.useSmallSkin = GlobalCore.displayType == DisplayType.Small ? true : false;

		// chk if tabletLayout posible
		GlobalCore.posibleTabletLayout = (GlobalCore.displayType == DisplayType.xLarge || GlobalCore.displayType == DisplayType.Large);

		// get parameters
		final Bundle extras = getIntent().getExtras();
		final Uri uri = getIntent().getData();

		// try to get data from extras
		if (extras != null)
		{
			GcCode = extras.getString("geocode");
			name = extras.getString("name");
			guid = extras.getString("guid");
		}

		// try to get data from URI
		if (GcCode == null && guid == null && uri != null)
		{
			String uriHost = uri.getHost().toLowerCase();
			String uriPath = uri.getPath().toLowerCase();
			// String uriQuery = uri.getQuery();

			if (uriHost.contains("geocaching.com") == true)
			{
				GcCode = uri.getQueryParameter("wp");
				guid = uri.getQueryParameter("guid");

				if (GcCode != null && GcCode.length() > 0)
				{
					GcCode = GcCode.toUpperCase();
					guid = null;
				}
				else if (guid != null && guid.length() > 0)
				{
					GcCode = null;
					guid = guid.toLowerCase();
				}
				else
				{
					// warning.showToast(res.getString(R.string.err_detail_open));
					finish();
					return;
				}
			}
			else if (uriHost.contains("coord.info") == true)
			{
				if (uriPath != null && uriPath.startsWith("/gc") == true)
				{
					GcCode = uriPath.substring(1).toUpperCase();
				}
				else
				{
					// warning.showToast(res.getString(R.string.err_detail_open));
					finish();
					return;
				}
			}
		}

		if (uri != null)
		{
			if (uri.getEncodedPath().endsWith(".gpx"))
			{
				GpxPath = uri.getEncodedPath();
			}
		}

		// if ACB running, call this instance
		if (main.mainActivity != null)
		{

			Bundle b = new Bundle();
			if (GcCode != null)
			{

				b.putSerializable("GcCode", GcCode);
				b.putSerializable("name", name);
				b.putSerializable("guid", guid);

			}

			if (GpxPath != null)
			{
				b.putSerializable("GpxPath", GpxPath);
			}

			Intent mainIntent = main.mainActivity.getIntent();

			mainIntent.putExtras(b);

			startActivity(mainIntent);
			finish();
		}

		splashActivity = this;

		LoadImages();

		if (savedInstanceState != null)
		{
			mSelectDbIsStartet = savedInstanceState.getBoolean("SelectDbIsStartet");
			mOriantationRestart = savedInstanceState.getBoolean("OriantationRestart");
		}

		if (mOriantationRestart) return; // wait for result

	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// initial GDX
		Gdx.files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGL20 = true;

		// first, try to find stored preferences of workPath
		AndroidSettings = this.getSharedPreferences(Global.PREFS_NAME, 0);

		workPath = AndroidSettings.getString("WorkPath", "");
		boolean askAgain = AndroidSettings.getBoolean("AskAgain", true);

		Global.initTheme(this);
		Global.InitIcons(this);

		CB_Android_FileExplorer fileExplorer = new CB_Android_FileExplorer(this);
		platformConector.setGetFileListner(fileExplorer);
		platformConector.setGetFolderListner(fileExplorer);

		String LangPath = AndroidSettings.getString("Sel_LanguagePath", "");
		if (LangPath.length() == 0)
		{
			// set default lang

			String locale = Locale.getDefault().getLanguage();
			if (locale.contains("de"))
			{
				LangPath = "data/lang/de/strings.ini";
			}
			else if (locale.contains("cs"))
			{
				LangPath = "data/lang/cs/strings.ini";
			}
			else if (locale.contains("cs"))
			{
				LangPath = "data/lang/cs/strings.ini";
			}
			else if (locale.contains("fr"))
			{
				LangPath = "data/lang/fr/strings.ini";
			}
			else if (locale.contains("nl"))
			{
				LangPath = "data/lang/nl/strings.ini";
			}
			else if (locale.contains("pl"))
			{
				LangPath = "data/lang/pl/strings.ini";
			}
			else if (locale.contains("pt"))
			{
				LangPath = "data/lang/pt/strings.ini";
			}
			else
			{
				LangPath = "data/lang/en-GB/strings.ini";
			}
		}

		new Translation(workPath, FileType.Internal);
		try
		{
			Translation.LoadTranslation(LangPath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// check Write permission
		if (!askAgain)
		{
			if (!checkWritePermission(workPath))
			{
				askAgain = true;
				if (!ToastEx)
				{
					ToastEx = true;
					String WriteProtectionMsg = Translation.Get("NoWriteAcces");
					Toast.makeText(splash.this, WriteProtectionMsg, Toast.LENGTH_LONG).show();
				}
			}
		}

		if ((askAgain))
		{
			// no saved workPath found -> search sd-cards and if more than 1 is found give the user the possibility to select one

			String externalSd = getExternalSdPath("/CacheBox");

			boolean hasExtSd;
			final String externalSd2 = externalSd;

			if (externalSd != null)
			{
				hasExtSd = (externalSd.length() > 0) && (!externalSd.equalsIgnoreCase(workPath));
			}
			else
			{
				hasExtSd = false;
			}

			// externe SD wurde gefunden != internal
			// oder Tablet Layout möglich
			// -> Auswahldialog anzeigen
			try
			{
				final Dialog dialog = new Dialog(context)
				{
					@Override
					public boolean onKeyDown(int keyCode, KeyEvent event)
					{
						if (keyCode == KeyEvent.KEYCODE_BACK)
						{
							splash.this.finish();
						}

						return super.onKeyDown(keyCode, event);
					}
				};

				dialog.setContentView(R.layout.sdselectdialog);
				TextView title = (TextView) dialog.findViewById(R.id.select_sd_title);
				title.setText(Translation.Get("selectWorkSpace") + "\n\n");
				/*
				 * TextView tbLayout = (TextView) dialog.findViewById(R.id.select_sd_layout); tbLayout.setText("\nLayout"); final RadioGroup
				 * rgLayout = (RadioGroup) dialog.findViewById(R.id.select_sd_radiogroup); final RadioButton rbHandyLayout = (RadioButton)
				 * dialog.findViewById(R.id.select_sd_handylayout); final RadioButton rbTabletLayout = (RadioButton)
				 * dialog.findViewById(R.id.select_sd_tabletlayout); rbHandyLayout.setText("Handy-Layout");
				 * rbTabletLayout.setText("Tablet-Layout"); if (!GlobalCore.posibleTabletLayout) {
				 * rgLayout.setVisibility(RadioGroup.INVISIBLE); rbHandyLayout.setChecked(true); } else { if (GlobalCore.isTab) {
				 * rbTabletLayout.setChecked(true); } else { rbHandyLayout.setChecked(true); } }
				 */
				final CheckBox cbAskAgain = (CheckBox) dialog.findViewById(R.id.select_sd_askagain);
				cbAskAgain.setText(Translation.Get("AskAgain"));
				cbAskAgain.setChecked(askAgain);
				Button buttonI = (Button) dialog.findViewById(R.id.button1);
				buttonI.setText("Internal SD\n\n" + workPath);
				buttonI.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// close select dialog
						dialog.dismiss();

						// show please wait dialog
						showPleaseWaitDialog();

						// use internal SD -> nothing to change
						Thread thread = new Thread()
						{
							@Override
							public void run()
							{
								boolean askAgain = cbAskAgain.isChecked();
								// boolean useTabletLayout = rbTabletLayout.isChecked();
								saveWorkPath(askAgain/* , useTabletLayout */);
								dialog.dismiss();
								startInitial();
							}
						};
						thread.run();
					}
				});
				Button buttonE = (Button) dialog.findViewById(R.id.button2);
				buttonE.setText("External SD\n\n" + externalSd);
				if (!hasExtSd)
				{
					buttonE.setVisibility(Button.INVISIBLE);
				}
				buttonE.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// close select dialog
						dialog.dismiss();

						// show please wait dialog
						showPleaseWaitDialog();

						// use external SD -> change workPath
						Thread thread = new Thread()
						{
							@Override
							public void run()
							{
								workPath = externalSd2;
								boolean askAgain = cbAskAgain.isChecked();
								// boolean useTabletLayout = rbTabletLayout.isChecked();
								saveWorkPath(askAgain/* , useTabletLayout */);
								startInitial();
							}
						};
						thread.run();

					}
				});

				// Set max height of ScrollView
				ScrollView sv = (ScrollView) dialog.findViewById(R.id.scrollView);
				// TODO set max
				LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.scrollViewLinearLayout);

				// add all Buttons for created Workspaces

				AdditionalWorkPathArray = getAdditionalWorkPathArray();

				for (final String AddWorkPath : AdditionalWorkPathArray)
				{

					final String Name = FileIO.GetFileNameWithoutExtension(AddWorkPath);

					if (!checkWritePermission(AddWorkPath))
					{
						// delete this Work Path
						deleteWorkPath(AddWorkPath);
						continue;
					}

					Button buttonW = new Button(context);
					buttonW.setText(Name + "\n\n" + AddWorkPath);

					buttonW.setOnLongClickListener(new OnLongClickListener()
					{

						@Override
						public boolean onLongClick(View v)
						{

							// setting the MassageBox then the UI_sizes are not initial in this moment
							Resources res = splash.this.getResources();
							float scale = res.getDisplayMetrics().density;
							float calcBase = 533.333f * scale;

							FrameLayout frame = (FrameLayout) findViewById(R.id.frameLayout1);
							int width = frame.getMeasuredWidth();
							int height = frame.getMeasuredHeight();

							MessageBox.Builder.WindowWidth = width;
							MessageBox.Builder.WindowHeight = height;
							MessageBox.Builder.textSize = (calcBase / res.getDimensionPixelSize(R.dimen.BtnTextSize)) * scale;
							MessageBox.Builder.ButtonHeight = (int) (50 * scale);

							// Ask before delete
							msg = (MessageBox) MessageBox.Show(Translation.Get("shuredeleteWorkspace", Name),
									Translation.Get("deleteWorkspace"), MessageBoxButtons.YesNo, MessageBoxIcon.Question,
									new DialogInterface.OnClickListener()
									{

										@Override
										public void onClick(DialogInterface dialog, int which)
										{
											if (which == MessageBox.BUTTON_POSITIVE)
											{
												// Delete this Workpath only from Settings don't delete any File
												deleteWorkPath(AddWorkPath);
											}
											// Start again to exclude the old Folder
											msg.dismiss();
											onStart();
										}

									});

							dialog.dismiss();
							return true;
						}
					});

					buttonW.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							// close select dialog
							dialog.dismiss();

							// show please wait dialog
							showPleaseWaitDialog();

							// use external SD -> change workPath
							Thread thread = new Thread()
							{
								@Override
								public void run()
								{
									workPath = AddWorkPath;
									boolean askAgain = cbAskAgain.isChecked();
									// boolean useTabletLayout = rbTabletLayout.isChecked();
									saveWorkPath(askAgain/* , useTabletLayout */);
									startInitial();
								}
							};
							thread.run();

						}
					});

					ll.addView(buttonW);
				}

				Button buttonC = (Button) dialog.findViewById(R.id.buttonCreateWorkspace);
				buttonC.setText(Translation.Get("createWorkSpace"));
				buttonC.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						// close select dialog
						dialog.dismiss();
						getFolderReturnListner = new IgetFolderReturnListner()
						{

							@Override
							public void getFolderReturn(String Path)
							{
								if (checkWritePermission(Path))
								{

									AdditionalWorkPathArray.add(Path);
									writeAdditionalWorkPathArray(AdditionalWorkPathArray);
									// Start again to include the new Folder
									onStart();
								}
								else
								{
									String WriteProtectionMsg = Translation.Get("NoWriteAcces");
									Toast.makeText(splash.this, WriteProtectionMsg, Toast.LENGTH_LONG).show();
								}
							}
						};

						platformConector.getFolder("", Translation.Get("select_folder"), Translation.Get("select"), getFolderReturnListner);

					}
				});

				dialog.show();

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			if (GlobalCore.displayType == DisplayType.Large || GlobalCore.displayType == DisplayType.xLarge) GlobalCore.isTab = isLandscape;

			// restore the saved workPath
			// test whether workPath is available by checking the free size on the SD
			String workPathToTest = workPath.substring(0, workPath.lastIndexOf("/"));
			long bytesAvailable = 0;
			try
			{
				StatFs stat = new StatFs(workPathToTest);
				bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
			}
			catch (Exception ex)
			{
				bytesAvailable = 0;
			}
			if (bytesAvailable == 0)
			{
				// there is a workPath stored but this workPath is not available at the moment (maybe SD is removed)
				Toast.makeText(splashActivity, "WorkPath " + workPath + " is not available!\nMaybe SD-Card is removed?", Toast.LENGTH_LONG)
						.show();
				finish();
				return;
			}

			startInitial();
		}

	}

	private boolean checkWritePermission(String addWorkPath)
	{
		try
		{
			String testFolderName = addWorkPath + "/Test";

			File testFolder = new File(testFolderName);
			File test = new File(testFolderName + "/Test.txt");
			testFolder.mkdirs();
			test.createNewFile();
			if (!test.exists())
			{
				return false;
			}
			test.delete();
			testFolder.delete();
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	private String getExternalSdPath(String Folder)
	{
		// check if Layout forced from User
		workPath = Environment.getExternalStorageDirectory() + Folder;

		// extract first part of path ("/mnt/" or "/storage/" ...)
		int pos = workPath.indexOf("/", 2); // search for the second /
		String prev = "/mnt";
		if (pos > 0)
		{
			prev = workPath.substring(0, pos);
		}
		// search for an external SD-Card
		String externalSd = "";

		// search for an external sd card on different devices

		if ((externalSd = testExtSdPath(prev + "/extSdCard")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/MicroSD")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard/ext_sd")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/ext_card")) != null)
		{
			// Sony Xperia sola
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/external")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard2")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard1")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard/_ExternalSD")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard-ext")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/external1")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard/external_sd")) != null)
		{
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/emmc")) != null)
		{
			// for CM9
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath("/Removable/MicroSD")) != null)
		{
			// Asus Transformer
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath("/mnt/ext_sd")) != null)
		{
			// ODYS Motion
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath("/sdcard/tflash")) != null)
		{
			// Car Radio
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath(prev + "/sdcard")) != null)
		{
			// on some devices it is possible that the SD-Card reported by getExternalStorageDirectory() is the extSd and the real
			// external SD is /mnt/sdcard (Faktor2 Tablet!!!)
			externalSd += Folder;
		}
		else if ((externalSd = testExtSdPath("/mnt/shared/ExtSD")) != null)
		{
			// GinyMotion Emulator
			externalSd += Folder;
		}

		if (android.os.Build.VERSION.SDK_INT == 19)
		{
			// check for Root permission
			try
			{
				String testFolderName = externalSd + "/Test";

				File testFolder = new File(testFolderName);
				File test = new File(testFolderName + "/Test.txt");
				testFolder.mkdirs();
				test.createNewFile();
				if (!test.exists())
				{
					return null;
				}
				test.delete();
				testFolder.delete();
			}
			catch (IOException e)
			{
				return null;
			}
		}

		return externalSd;
	}

	private ArrayList<String> getAdditionalWorkPathArray()
	{
		ArrayList<String> retList = new ArrayList<String>();
		AdditionalWorkPathCount = AndroidSettings.getInt("AdditionalWorkPathCount", 0);
		for (int i = 0; i < AdditionalWorkPathCount; i++)
		{
			retList.add(AndroidSettings.getString("AdditionalWorkPath" + String.valueOf(i), ""));
		}
		return retList;
	}

	private void writeAdditionalWorkPathArray(ArrayList<String> list)
	{
		Editor editor = AndroidSettings.edit();

		// first remove all
		for (int i = 0; i < AdditionalWorkPathCount; i++)
		{
			String delWorkPath = "AdditionalWorkPath" + String.valueOf(i);
			editor.remove(delWorkPath);
		}
		editor.commit();

		int index = 0;
		for (String workpath : list)
		{
			String addWorkPath = "AdditionalWorkPath" + String.valueOf(index);
			editor.putString(addWorkPath, workpath);
			index++;
		}
		AdditionalWorkPathCount = index;
		editor.putInt("AdditionalWorkPathCount", AdditionalWorkPathCount);
		editor.commit();
	}

	private void deleteWorkPath(String addWorkPath)
	{
		int index = AdditionalWorkPathArray.indexOf(addWorkPath);
		if (index >= 0) AdditionalWorkPathArray.remove(index);
		writeAdditionalWorkPathArray(AdditionalWorkPathArray);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR)
		{
			if (resultCode == android.app.Activity.RESULT_OK && data != null)
			{
				// obtain the filename
				Uri fileUri = data.getData();
				if (fileUri != null)
				{
					String filePath = fileUri.getPath();
					if (filePath != null)
					{
						if (getFolderReturnListner != null) getFolderReturnListner.getFolderReturn(filePath);
					}
				}
			}
			return;

		}

	}

	private void showPleaseWaitDialog()
	{
		pWaitD = ProgressDialog.show(splash.this, "In progress", "Copy resources");
		pWaitD.show();
		TextView tv1 = (TextView) pWaitD.findViewById(android.R.id.message);
		tv1.setTextColor(Color.WHITE);
	}

	// this will test whether the extPath is an existing path to an external sd card
	private String testExtSdPath(String extPath)
	{
		if (extPath.equalsIgnoreCase(workPath)) return null; // if this extPath is the same than the actual workPath -> this is the
																// internal SD, not
		// the external!!!
		if (FileIO.FileExists(extPath))
		{
			StatFs stat = new StatFs(extPath);
			long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
			if (bytesAvailable == 0)
			{
				return null; // ext SD-Card is not plugged in -> do not use it
			}
			else
			{
				// Check can Read/Write

				File f = new File(extPath);
				if (f.canWrite())
				{
					if (f.canRead())
					{
						return f.getAbsolutePath(); // ext SD-Card is plugged in
					}
				}

				// Check can Read/Write on Application Storage
				String appPath = this.getApplication().getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
				int Pos = appPath.indexOf("/Android/data/");
				String p = appPath.substring(Pos);
				File fi = new File(extPath + p);// "/Android/data/de.cachebox_test/files");
				fi.mkdirs();
				if (fi.canWrite())
				{
					if (fi.canRead())
					{
						return fi.getAbsolutePath();
					}
				}
				return null;
			}
		}
		return null;
	}

	private void saveWorkPath(boolean askAgain/* , boolean useTabletLayout */)
	{

		if (GlobalCore.displayType == DisplayType.Large || GlobalCore.displayType == DisplayType.xLarge) GlobalCore.isTab = isLandscape;

		SharedPreferences settings = this.getSharedPreferences(Global.PREFS_NAME, 0);
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("WorkPath", workPath);
		editor.putBoolean("AskAgain", askAgain);
		// editor.putBoolean("UseTabletLayout", isLandscape);
		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onDestroy()
	{
		if (isFinishing())
		{
			ReleaseImages();
			// versionTextView = null;
			// myTextView = null;
			// descTextView = null;
			splashActivity = null;

		}
		super.onDestroy();
		if (pWaitD != null && pWaitD.isShowing())
		{
			pWaitD.dismiss();
			pWaitD = null;
		}

		ui = null;

	}

	private void startInitial()
	{

		// show wait dialog if not running
		if (pWaitD == null) showPleaseWaitDialog();

		// saved workPath found -> use this
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				Initial();
			}
		};

		thread.start();

	}

	private void Initial()
	{
		// Jetzt ist der workPath erstmal festgelegt.

		Logger.setDebugFilePath(workPath + "/debug.txt");

		// Zur Kompatibilität mit älteren Installationen wird hier noch die redirection.txt abgefragt
		if (FileIO.FileExists(workPath + "/redirection.txt"))
		{
			BufferedReader Filereader;

			try
			{
				Filereader = new BufferedReader(new FileReader(workPath + "/redirection.txt"));
				String line;

				while ((line = Filereader.readLine()) != null)
				{
					// chk ob der umleitungs Ordner exestiert
					if (FileIO.FileExists(line))
					{
						workPath = line;
					}
				}

				Filereader.close();
			}
			catch (IOException e)
			{
				Logger.Error("read redirection", "", e);
				e.printStackTrace();
			}

		}

		if (GlobalCore.isTab)
		{
			// Tab Modus only Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		}
		else
		{
			// Phone Modus only Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		}

		new Config(workPath);

		// Read Config
		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		Database.Settings = new AndroidDB(DatabaseType.Settings, this);
		if (!FileIO.createDirectory(Config.WorkPath + "/User")) return;
		Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");

		// initialisieren der PlattformSettings
		PlatformSettings.setPlatformSettings(new iPlatformSettings()
		{

			@Override
			public void Write(SettingBase<?> setting)
			{
				if (androidSetting == null) androidSetting = splash.this.getSharedPreferences(Global.PREFS_NAME, 0);
				if (androidSettingEditor == null) androidSettingEditor = androidSetting.edit();

				if (setting instanceof SettingBool)
				{
					androidSettingEditor.putBoolean(setting.getName(), ((SettingBool) setting).getValue());
				}

				else if (setting instanceof SettingString)
				{
					androidSettingEditor.putString(setting.getName(), ((SettingString) setting).getValue());
				}
				else if (setting instanceof SettingInt)
				{
					androidSettingEditor.putInt(setting.getName(), ((SettingInt) setting).getValue());
				}

				// Commit the edits!
				androidSettingEditor.commit();
			}

			@Override
			public SettingBase<?> Read(SettingBase<?> setting)
			{
				if (androidSetting == null) androidSetting = splash.this.getSharedPreferences(Global.PREFS_NAME, 0);

				if (setting instanceof SettingString)
				{
					String value = androidSetting.getString(setting.getName(), "");
					((SettingString) setting).setValue(value);
				}
				else if (setting instanceof SettingBool)
				{
					boolean value = androidSetting.getBoolean(setting.getName(), ((SettingBool) setting).getDefaultValue());
					((SettingBool) setting).setValue(value);
				}
				else if (setting instanceof SettingInt)
				{
					int value = androidSetting.getInt(setting.getName(), ((SettingInt) setting).getDefaultValue());
					((SettingInt) setting).setValue(value);
				}
				setting.clearDirty();
				return setting;
			}
		});

		// wenn die Settings DB neu Erstellt wurde, müssen die Default werte
		// geschrieben werden.
		if (Database.Settings.isDbNew())
		{
			Config.settings.LoadAllDefaultValues();
			Config.settings.WriteToDB();
		}
		else
		{
			Config.settings.ReadFromDB();
		}

		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);

		// wenn eine cachebox.config existiert, werden die Werte in die DB
		// übertragen
		if (FileIO.FileExists(workPath + "/cachebox.config"))
		{
			Config.readConfigFile(/* getAssets() */);

			for (Iterator<SettingBase<?>> it = Config.settings.iterator(); it.hasNext();)
			{
				SettingBase<?> setting = it.next();

				if (setting instanceof SettingBool)
				{
					((SettingBool) setting).setValue(Config.GetBool(setting.getName()));
				}
				else if (setting instanceof SettingIntArray)
				{
					((SettingIntArray) setting).setValue(Config.GetInt(setting.getName()));
				}
				else if (setting instanceof SettingTime)
				{
					((SettingTime) setting).setValue(((Config.GetInt("LockM") * 60) + Config.GetInt("LockSec")) * 1000);
				}
				else if (setting instanceof SettingInt)
				{
					((SettingInt) setting).setValue(Config.GetInt(setting.getName()));
				}
				else if (setting instanceof SettingDouble)
				{
					((SettingDouble) setting).setValue(Config.GetDouble(setting.getName()));
				}
				else if (setting instanceof SettingFolder)
				{
					((SettingFolder) setting).setValue(Config.GetString(setting.getName()));
				}
				else if (setting instanceof SettingFile)
				{
					((SettingFile) setting).setValue(Config.GetString(setting.getName()));
				}
				else if (setting instanceof SettingEnum)
				{
					((SettingEnum<?>) setting).setValue(Config.GetString(setting.getName()));
				}
				else if (setting instanceof SettingEncryptedString)
				{
					((SettingEncryptedString) setting).setEncryptedValue(Config.GetString(setting.getName() + "Enc"));
				}
				else if (setting instanceof SettingString)
				{
					((SettingString) setting).setValue(Config.GetString(setting.getName()));
				}

			}
			// Schreibe settings in die DB
			Config.AcceptChanges();

			// cachebox.config umbenennen.
			File f = new File(workPath + "/cachebox.config");
			f.renameTo(new File(workPath + "/ALT_cachebox.config"));

		}

		// copy AssetFolder only if Rev-Number changed, like at new installation
		if (Config.installRev.getValue() < GlobalCore.CurrentRevision)
		{
			String[] exclude = new String[]
				{ "webkit", "sound", "sounds", "images", "skins", "lang", "kioskmode", "string-files", "" };
			copyAssetFolder myCopie = new copyAssetFolder();

			myCopie.copyAll(getAssets(), Config.WorkPath, exclude);
			Config.installRev.setValue(GlobalCore.CurrentRevision);
			Config.newInstall.setValue(true);
			Config.AcceptChanges();

			File CreateFile;

			// create .nomedia Files
			try
			{
				CreateFile = new File(workPath + "/data/.nomedia");
				CreateFile.getParentFile().mkdirs();
				CreateFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				CreateFile = new File(workPath + "/skins/.nomedia");
				CreateFile.getParentFile().mkdirs();
				CreateFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				CreateFile = new File(workPath + "/repository/.nomedia");
				CreateFile.getParentFile().mkdirs();
				CreateFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				CreateFile = new File(workPath + "/Repositories/.nomedia");
				CreateFile.getParentFile().mkdirs();
				CreateFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				CreateFile = new File(workPath + "/cache/.nomedia");
				CreateFile.getParentFile().mkdirs();
				CreateFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			Config.newInstall.setValue(false);
			Config.AcceptChanges();
		}

		// UiSize Structur für die Berechnung der Größen zusammen stellen!
		Resources res = this.getResources();

		// WindowManager w = this.getWindowManager();
		// Display d = w.getDefaultDisplay();

		FrameLayout frame = (FrameLayout) findViewById(R.id.frameLayout1);
		int width = frame.getMeasuredWidth();
		int height = frame.getMeasuredHeight();

		if (ui == null)
		{
			ui = new devicesSizes();

			ui.Window = new Size(width, height);
			ui.Density = res.getDisplayMetrics().density;
			ui.RefSize = res.getDimensionPixelSize(R.dimen.RefSize);
			ui.TextSize_Normal = res.getDimensionPixelSize(R.dimen.TextSize_normal);
			ui.ButtonTextSize = res.getDimensionPixelSize(R.dimen.BtnTextSize);
			ui.IconSize = res.getDimensionPixelSize(R.dimen.IconSize);
			ui.Margin = res.getDimensionPixelSize(R.dimen.Margin);
			ui.ArrowSizeList = res.getDimensionPixelSize(R.dimen.ArrowSize_List);
			ui.ArrowSizeMap = res.getDimensionPixelSize(R.dimen.ArrowSize_Map);
			ui.TB_IconSize = res.getDimensionPixelSize(R.dimen.TB_icon_Size);
			ui.isLandscape = false;
		}

		new UiSizes();
		UI_Size_Base.that.initial(ui);

		Global.Paints.init(this);

		{// restrict MapsforgeScaleFactor to max 1.0f (TileSize 256x256)
			ext_AndroidGraphicFactory.createInstance(this.getApplication());

			float restrictedScaleFactor = 1f;
			DisplayModel.setDeviceScaleFactor(restrictedScaleFactor);
			new de.cachebox_test.Map.AndroidManager(new DisplayModel());
		}

		Initial2();
	}

	private boolean mSelectDbIsStartet = false;

	private void Initial2()
	{

		// initialize Database
		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
		Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);

		Config.AcceptChanges();

		// Initial Ready Show main
		finish();
		Intent mainIntent = new Intent().setClass(splash.this, main.class);
		Bundle b = new Bundle();
		if (GcCode != null)
		{

			b.putSerializable("GcCode", GcCode);
			b.putSerializable("name", name);
			b.putSerializable("guid", guid);

		}

		if (GpxPath != null)
		{
			b.putSerializable("GpxPath", GpxPath);
		}

		if (pWaitD != null)
		{
			pWaitD.dismiss();
			pWaitD = null;
		}

		b.putSerializable("UI", ui);

		GlobalCore.RunFromSplash = true;

		mainIntent.putExtras(b);
		Log.d("CACHEBOX", "Splash start Main Intent");
		startActivity(mainIntent);
		finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean("SelectDbIsStartet", mSelectDbIsStartet);
		outState.putBoolean("OriantationRestart", true);
	}

	private void LoadImages()
	{

		((TextView) findViewById(R.id.splash_textViewDesc)).setVisibility(View.INVISIBLE);
		((TextView) findViewById(R.id.splash_textViewVersion)).setVisibility(View.INVISIBLE);
		((TextView) findViewById(R.id.splash_TextView)).setVisibility(View.INVISIBLE);

	}

	private void ReleaseImages()
	{
		((ImageView) findViewById(R.id.splash_BackImage)).setImageResource(0);

		if (bitmap != null)
		{
			bitmap.recycle();
			bitmap = null;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			this.finish();
		}

		return super.onKeyDown(keyCode, event);
	}

}
