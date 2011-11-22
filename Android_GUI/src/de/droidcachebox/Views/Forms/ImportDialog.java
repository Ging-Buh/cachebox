package de.droidcachebox.Views.Forms;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Api.PocketQuery;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Ui.Sizes;

/**
 * <h1>ProgressDialog</h1> <img src="doc-files/ImportScreen.png" width=146
 * height=117> </br>
 * 
 * @author Longri </br></br>
 */
public class ImportDialog extends Activity
{
	public static ImportDialog Me;

	private Context context;
	private CheckBox checkBoxImportMaps;
	private CheckBox checkBoxPreloadImages;
	private CheckBox checkBoxImportGPX;
	private CheckBox checkBoxGcVote;

	private CheckBox checkImportPQfromGC;
	private Button CancelButton;
	private Button ImportButton;

	private final int IMPLEMENTED = 1;
	private final int NOT_IMPLEMENTED = 0;

	private final int MapImport = NOT_IMPLEMENTED;
	private final int GpxImport = NOT_IMPLEMENTED;
	private final int ImageImport = NOT_IMPLEMENTED;
	private final int GcVoteImport = NOT_IMPLEMENTED;
	private final int PQImport = NOT_IMPLEMENTED;

	public void onCreate(Bundle savedInstanceState)
	{
		// ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_dialog_layout);
		Me = this;

		context = this.getBaseContext();

		((TextView) this.findViewById(R.id.title)).setText("Import");

		findViewById();

		checkImportPQfromGC.setText(GlobalCore.Translations.Get("PQfromGC"));
		checkBoxImportGPX.setText(GlobalCore.Translations.Get("GPX"));

		checkBoxGcVote.setText(GlobalCore.Translations.Get("GCVoteRatings"));
		checkBoxPreloadImages.setText(GlobalCore.Translations.Get("PreloadImages"));
		checkBoxImportMaps.setText(GlobalCore.Translations.Get("Maps"));
		ImportButton.setText(GlobalCore.Translations.Get("import"));
		CancelButton.setText(GlobalCore.Translations.Get("cancel"));

		CancelButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				;
				finish();
			}
		});

		ImportButton.setOnClickListener(ImportClick);

		initialForm();

	}

	private void findViewById()
	{
		CancelButton = (Button) this.findViewById(R.id.cancelButton);
		ImportButton = (Button) this.findViewById(R.id.importButton);

		checkBoxImportMaps = (CheckBox) this.findViewById(R.id.import_Maps);
		checkBoxPreloadImages = (CheckBox) this.findViewById(R.id.import_Image);
		checkBoxImportGPX = (CheckBox) this.findViewById(R.id.import_GPX);
		checkBoxGcVote = (CheckBox) this.findViewById(R.id.import_GcVote);
		checkImportPQfromGC = (CheckBox) this.findViewById(R.id.import_PQ);
	}

	private void initialForm()
	{
		checkBoxImportMaps.setChecked(Config.settings.CacheMapData.getValue());
		checkBoxPreloadImages.setChecked(Config.settings.CacheImageData.getValue());
		checkBoxImportGPX.setChecked(Config.settings.ImportGpx.getValue());
		checkBoxImportGPX.setOnCheckedChangeListener(checkBoxImportGPX_CheckStateChanged);
		checkImportPQfromGC.setOnCheckedChangeListener(checkImportPQfromGC_CheckStateChanged);
		checkBoxGcVote.setChecked(Config.settings.ImportRatings.getValue());

		// if (Config.settings.PopHost.getValue().length() > 0 &&
		// Config.GetStringEncrypted("PopLogin").length() > 0
		// && Config.GetStringEncrypted("PopPassword").length() > 0)
		// {
		// checkBoxImportGpxFromMail.setChecked(Config.settings.ImportGpxFromMail.getValue());
		// checkBoxImportGpxFromMail.setEnabled(true);
		// }
		// else
		// {
		// checkBoxImportGpxFromMail.setEnabled(false);
		// checkBoxImportGpxFromMail.setChecked(false);
		// }

		if (Config.settings.GcAPI.getValue().length() > 0)
		{
			checkImportPQfromGC.setChecked(Config.settings.ImportPQsFromGeocachingCom.getValue());
			checkImportPQfromGC.setEnabled(true);
		}
		else
		{
			checkImportPQfromGC.setChecked(false);
			checkImportPQfromGC.setEnabled(false);
		}

		if (checkImportPQfromGC.isChecked() == true)
		{
			checkBoxImportGPX.setChecked(true);
			checkBoxImportGPX.setEnabled(false);
		}

		ImportButton.setWidth(Sizes.getButtonWidthWide());
		CancelButton.setWidth(Sizes.getButtonWidthWide());
		ImportButton.setHeight(Sizes.getButtonHeight());
		CancelButton.setHeight(Sizes.getButtonHeight());
	}

	private OnCheckedChangeListener checkBoxImportGPX_CheckStateChanged = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{

		}
	};

	private OnCheckedChangeListener checkImportPQfromGC_CheckStateChanged = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (checkImportPQfromGC.isChecked())
			{
				checkBoxImportGPX.setChecked(true);
				checkBoxImportGPX.setEnabled(false);
			}
			else
			{
				checkBoxImportGPX.setEnabled(true);
			}
		}
	};

	private OnClickListener ImportClick = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (!Config.settings.CacheImageData.getValue() && checkBoxPreloadImages.isChecked())
			{
				// only show warn message, if the user changed the state from
				// disable to enable.
				MessageBox
						.Show("Download of additional/spoiler images is done on personal responsibility. Read the GEOCACHING.COM SITE TERMS OF USE AGREEMENT (5). Really download?",
								"Import additional images", MessageBoxButtons.YesNo, MessageBoxIcon.Exclamation, DialogListner);
				finish();
			}
			else
			{
				ImportNow();
			}

		}
	};

	private final DialogInterface.OnClickListener DialogListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			// Behandle das ergebniss
			switch (button)
			{
			case -1:
				Config.settings.GCAdditionalImageDownload.setValue(true);
				break;
			case -2:
				Config.settings.GCAdditionalImageDownload.setValue(false);
				break;

			}

			dialog.dismiss();
			ImportNow();
		}

	};

	private DialogInterface WaitDialog;

	private void ImportNow()
	{
		Config.settings.CacheMapData.setValue(checkBoxImportMaps.isChecked());
		Config.settings.CacheImageData.setValue(checkBoxPreloadImages.isChecked());
		Config.settings.ImportGpx.setValue(checkBoxImportGPX.isChecked());

		Config.settings.ImportPQsFromGeocachingCom.setValue(checkImportPQfromGC.isChecked());
		Config.settings.ImportRatings.setValue(checkBoxGcVote.isChecked());
		Config.AcceptChanges();
		directoryPath = Config.settings.PocketQueryFolder.getValue();
		// chk exist import folder
		directory = new File(directoryPath);

		if (checkImportPQfromGC.isChecked())
		{
			// WaitDialog = PleaseWaitMessageBox.Show("Read PQ List",
			// "Groundspeak API", MessageBoxButtons.NOTHING,
			// MessageBoxIcon.Powerd_by_GC_Live, null,this);

			WaitDialog = android.app.ProgressDialog.show(this, "Groundspeak API", "Read PQ List", true, false);

			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					pqList = new ArrayList<PQ>();
					int ret = PocketQuery.GetPocketQueryList(Config.GetAccessToken(), pqList);

					getPqListReadyHandler.sendMessage(getPqListReadyHandler.obtainMessage(ret));
				}

			};

			thread.start();

		}
		else
		{
			ImportThread(directoryPath, directory);
		}

	}

	private ArrayList<PQ> pqList;
	private File directory;
	private String directoryPath;
	private Handler getPqListReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			WaitDialog.dismiss();

			if (msg.what == 0)
			{

				Intent PqListIntent = new Intent().setClass(ImportDialog.Me, ApiPQDialog.class);
				Bundle b = new Bundle();
				b.putSerializable("PqList", pqList);
				PqListIntent.putExtras(b);
				WaitDialog.dismiss();
				ImportDialog.Me.startActivityForResult(PqListIntent, Global.RESULT_SELECT_PQ_LIST);
			}
			else if (msg.what == -1)
			{
				WaitDialog.dismiss();
				MessageBox.Show(GlobalCore.Translations.Get("errorAPI"), GlobalCore.Translations.Get("Error"), MessageBoxIcon.Error);

			}

		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// get result PqList
		if (data == null) return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Iterator<PQ> iterator = ((ArrayList<PQ>) bundle.getSerializable("PqList")).iterator();
			downloadPQList = new ArrayList<PQ>();
			do
			{
				PQ pq = iterator.next();
				if (pq.downloadAvible)
				{
					downloadPQList.add(pq);

				}
			}
			while (iterator.hasNext());

		}

		ImportThread(directoryPath, directory);
	}

	private ArrayList<PQ> downloadPQList = null;

	public void ImportThread(final String directoryPath, final File directory)
	{
		Thread ImportThread = new Thread()
		{
			public void run()
			{
				Importer importer = new Importer();
				ImporterProgress ip = new ImporterProgress();

				try
				{

					if (checkImportPQfromGC.isChecked())
					{
						ip.addStep(ip.new Step("importGC", 4));
					}
					if (checkBoxImportGPX.isChecked())
					{
						ip.addStep(ip.new Step("ExtractZip", 1));
						ip.addStep(ip.new Step("AnalyseGPX", 1));
						ip.addStep(ip.new Step("ImportGPX", 4));
					}
					if (checkBoxGcVote.isChecked())
					{
						ip.addStep(ip.new Step("sendGcVote", 1));
						ip.addStep(ip.new Step("importGcVote", 4));
					}

					if (downloadPQList != null)
					{
						Iterator<PQ> iterator = downloadPQList.iterator();

						ip.setJobMax("importGC", downloadPQList.size());

						do
						{
							PQ pq = iterator.next();

							if (pq.downloadAvible)
							{
								ip.ProgressInkrement("importGC", "Download: " + pq.Name, false);
								PocketQuery.DownloadSinglePocketQuery(pq);
							}

						}
						while (iterator.hasNext());

						if (downloadPQList.size() == 0)
						{
							ip.ProgressInkrement("importGC", "", true);
						}
					}

					// Importiere alle GPX Files im Import Folder, auch in ZIP
					// verpackte
					if (checkBoxImportGPX.isChecked() && directory.exists())
					{

						System.gc();

						long startTime = System.currentTimeMillis();

						Database.Data.beginTransaction();
						try
						{

							importer.importGpx(directoryPath, ip);

							Database.Data.setTransactionSuccessful();
						}
						catch (Exception exc)
						{
							exc.printStackTrace();
						}
						Database.Data.endTransaction();

						Log.i("Import", "GPX Import took " + (System.currentTimeMillis() - startTime) + "ms");

						System.gc();

						// del alten entpackten Ordener wenn vorhanden?
						File[] filelist = directory.listFiles();
						for (File tmp : filelist)
						{
							if (tmp.isDirectory())
							{
								ArrayList<String> ordnerInhalt = Importer.recursiveDirectoryReader(tmp, new ArrayList<String>());
								for (String tmp2 : ordnerInhalt)
								{
									File forDel = new File(tmp2);
									forDel.delete();
								}

							}
							tmp.delete();
						}

					}

					if (checkBoxGcVote.isChecked())
					{
						Database.Data.beginTransaction();
						try
						{
							importer.importGcVote(Global.LastFilter.getSqlWhere(), ip);

							Database.Data.setTransactionSuccessful();
						}
						catch (Exception exc)
						{
							exc.printStackTrace();
						}
						Database.Data.endTransaction();
					}

					if (checkBoxPreloadImages.isChecked()) importer.importImages();
					Thread.sleep(1000);
					if (checkBoxImportMaps.isChecked()) importer.importMaps();

					if (importCancel) // wenn im ProgressDialog Cancel gedrückt
										// wurde.

					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				if (!importCancel)
				{
					ProgressDialog.Ready(); // Dem Progress Dialog melden, dass
											// der Thread fertig ist!
					ProgressHandler.post(ProgressReady); // auf das Ende des
															// Threads reagieren
				}
			}
		};

		ImportThread.setPriority(Thread.MAX_PRIORITY);
		ImportStart = new Date();
		ProgressDialog.Show("Import", ImportThread, ProgressCanceld);
	}

	private Date ImportStart;
	private int LogImports;
	private int CacheImports;

	private Boolean importCancel = false;
	final Runnable ProgressCanceld = new Runnable()
	{
		public void run()
		{
			importCancel = true;

		}
	};

	final Handler ProgressHandler = new Handler();
	final Runnable ProgressReady = new Runnable()
	{
		public void run()
		{
			Date Importfin = new Date();
			long ImportZeit = Importfin.getTime() - ImportStart.getTime();

			String Msg = "Import " + String.valueOf(CacheImports) + "C " + String.valueOf(LogImports) + "L in "
					+ String.valueOf(ImportZeit);

			Logger.DEBUG(Msg);
			// MessageBox.Show("Import fertig! " + Msg);
			ApplyFilter();
		}
	};

	private static android.app.ProgressDialog pd;
	private static FilterProperties props;

	public static void ApplyFilter()
	{

		props = Global.LastFilter;
		pd = android.app.ProgressDialog.show(ImportDialog.Me, "", GlobalCore.Translations.Get("LoadCaches"), true);

		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				String sqlWhere = props.getSqlWhere();
				Logger.General("Main.ApplyFilter: " + sqlWhere);
				Database.Data.Query.clear();
				CacheListDAO cacheListDAO = new CacheListDAO();
				cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
				messageHandler.sendMessage(messageHandler.obtainMessage(1));
			}

		};

		thread.start();

	}

	// Instantiating the Handler associated with the main thread.
	private static Handler messageHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				CachListChangedEventList.Call();
				pd.dismiss();
				Toast.makeText(
						main.mainActivity,
						GlobalCore.Translations.Get("AppliedFilter1") + " " + String.valueOf(Database.Data.Query.size()) + " "
								+ GlobalCore.Translations.Get("AppliedFilter2"), Toast.LENGTH_LONG).show();
				ImportDialog.Me.finish();
			}

			}

		}

	};

}
