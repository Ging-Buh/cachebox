package de.droidcachebox.Views.Forms;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.DAO.CacheDAO;
import de.droidcachebox.DAO.LogDAO;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.CacheListChangedEvent;
import CB_Core.Import.Importer;
import CB_Core.Import.Importer.Cache_Log_Return;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * <h1>ProgressDialog</h1>
 * 
 * <img src="doc-files/ImportScreen.png" width=146 height=117>
 * 
 * </br>
 * @author Longri
 * 
 * 
 * </br></br>
  
 */
public class ImportDialog extends Activity implements ViewOptionsMenu {
	private Context context;
	private CheckBox checkBoxImportMaps;
	private CheckBox checkBoxPreloadImages;
	private CheckBox checkBoxImportGPX;
	private CheckBox checkBoxGcVote;
	private CheckBox checkBoxImportGpxFromMail;
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
	private final int MailImport = NOT_IMPLEMENTED;

	public void onCreate(Bundle savedInstanceState) {
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_dialog_layout);
		
			
		context = this.getBaseContext();
				
	
		((TextView) this.findViewById(R.id.title)).setText("Import");
		
		findViewById();
		
		CancelButton.setText(Global.Translations.Get("cancel"));
		CancelButton.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{;
				finish();
			}
		});
		
		ImportButton.setOnClickListener(ImportClick);
		
		
		initialForm();
		
	}
	
	private void findViewById()
	{
		CancelButton =(Button) this.findViewById(R.id.cancelButton);
		ImportButton =(Button) this.findViewById(R.id.importButton);
		
		checkBoxImportMaps = (CheckBox)this.findViewById(R.id.import_Maps);
		checkBoxPreloadImages = (CheckBox)this.findViewById(R.id.import_Image);
		checkBoxImportGPX = (CheckBox)this.findViewById(R.id.import_GPX);
		checkBoxGcVote = (CheckBox)this.findViewById(R.id.import_GcVote);
		checkBoxImportGpxFromMail = (CheckBox)this.findViewById(R.id.import_checkMails);
		checkImportPQfromGC = (CheckBox)this.findViewById(R.id.import_PQ);
	}
	
	
	private void initialForm()
	{
		checkBoxImportMaps.setChecked(Config.GetBool("CacheMapData"));
        checkBoxPreloadImages.setChecked(Config.GetBool("CacheImageData"));
        checkBoxImportGPX.setChecked(Config.GetBool("ImportGpx"));
        checkBoxImportGPX.setOnCheckedChangeListener(checkBoxImportGPX_CheckStateChanged);
        checkImportPQfromGC.setOnCheckedChangeListener(checkImportPQfromGC_CheckStateChanged);
        checkBoxGcVote.setChecked(Config.GetBool("ImportRatings"));
        
        if (Config.GetString("PopHost").length() > 0 && Config.GetStringEncrypted("PopLogin").length() > 0 && Config.GetStringEncrypted("PopPassword").length() > 0)
        {
            checkBoxImportGpxFromMail.setChecked(Config.GetBool("ImportGpxFromMail"));
            checkBoxImportGpxFromMail.setEnabled(true);
        }
        else
        {
            checkBoxImportGpxFromMail.setEnabled(false);
            checkBoxImportGpxFromMail.setChecked(false);
        }

        if (Config.GetStringEncrypted("GcPass").length() > 0)
        {
            checkImportPQfromGC.setChecked(Config.GetBool("ImportPQsFromGeocachingCom"));
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
	}
	
	
	 private OnCheckedChangeListener checkBoxImportGPX_CheckStateChanged = new OnCheckedChangeListener() 
     {	
    	 @Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
    	{ 
    		 checkBoxImportGpxFromMail.setEnabled(checkBoxImportGPX.isChecked());	
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
			 if (!Config.GetBool("CacheImageData") && checkBoxPreloadImages.isChecked())
	            {
	                // only show warn message, if the user changed the state from disable to enable.
	               MessageBox.Show("Download of additional/spoiler images is done on personal responsibility. Read the GEOCACHING.COM SITE TERMS OF USE AGREEMENT (5). Really download?", "Import additional images", MessageBoxButtons.YesNo, MessageBoxIcon.Exclamation,DialogListner); 
	               finish();
	            }
			 else
			 {
				 ImportNow();
			 }
			
		}
	};
	
	
	
 	
	  private final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	   { @Override public void onClick(DialogInterface dialog, int button) 
			{
				// Behandle das ergebniss
				switch (button)
				{
					case -1:
						 Config.Set("GCAdditionalImageDownload", true);
						break;
					case -2:
						Config.Set("GCAdditionalImageDownload", false);
						break;
					
				}
				
				dialog.dismiss();
				ImportNow();
			}
			
	    }
	 ;
	 
	private void ImportNow()
	{
		 Config.Set("CacheMapData", checkBoxImportMaps.isChecked());
         Config.Set("CacheImageData", checkBoxPreloadImages.isChecked());
         Config.Set("ImportGpx", checkBoxImportGPX.isChecked());
         Config.Set("ImportGpxFromMail", checkBoxImportGpxFromMail.isChecked());
         Config.Set("ImportPQsFromGeocachingCom", checkImportPQfromGC.isChecked());
         Config.Set("ImportRatings", checkBoxGcVote.isChecked());
         Config.AcceptChanges();
         
         
         
         Thread ImportThread = new Thread() 
         {
             public void run() 
             {
            	 Importer importer = new Importer();
            	 ImporterProgress ip = new ImporterProgress();
            	 
            	String directoryPath = Config.GetString("PocketQueryFolder");
                         try 
                         {
                         if(checkImportPQfromGC.isChecked())importer.importGC();
                         Thread.sleep(1000);
                         if(checkBoxImportGpxFromMail.isChecked())importer.importMail();
                         Thread.sleep(1000);
                         
                         //Importiere alle GPX Files im Import Folder, auch in ZIP verpackte
                         if(checkBoxImportGPX.isChecked())
                        {
                        	Cache_Log_Return Returns = importer.importGpx(directoryPath,ip);
                        	CacheImports=Returns.CacheCount;
                        	LogImports=Returns.LogCount;
                        	// Schreibe Imports in DB
                        	CacheDAO cacheDAO = new CacheDAO();
                        	cacheDAO.WriteImports(Returns.cacheIterator,Returns.CacheCount,ip);
                        	
                        	LogDAO logDao = new LogDAO();
                        	logDao.WriteImports(Returns.logIterator,Returns.LogCount,ip);
                        	
                        	
                        	// del alten entpackten Ordener wenn vorhanden?
                    		File directory = new File(directoryPath);
                    		File[] filelist = directory.listFiles();
                    		for(File tmp : filelist)
                    		{
                    			if (tmp.isDirectory() )
                    			{
                    				ArrayList<String> ordnerInhalt = Importer.recursiveDirectoryReader(tmp,new ArrayList<String>() );
                    				for (String tmp2 : ordnerInhalt)
                    				{
                    					File forDel = new File(tmp2);
                    					forDel.delete();
                    				}
                    				
                    			}
                    			tmp.delete();
                    		}
                        	
                        	
                        }
                         
                         if(checkBoxGcVote.isChecked())importer.importGcVote();
                         Thread.sleep(1000);
                         if(checkBoxPreloadImages.isChecked())importer.importImages();
                         Thread.sleep(1000);
                         if(checkBoxImportMaps.isChecked())importer.importMaps();
                         
                         
                             if(importCancel) // wenn im ProgressDialog Cancel gedrückt wurde.
                              
                             
                             Thread.sleep(1000);
                         } 
                         catch (InterruptedException e) 
                         {
                             e.printStackTrace();
                         }
                
                 if(!importCancel)
                 {
                     ProgressDialog.Ready();             //Dem Progress Dialog melden, dass der Thread fertig ist!
                     ProgressHandler.post(ProgressReady);    // auf das Ende des Threads reagieren
                 }
             }
         };
             


         
         ImportThread.setPriority(Thread.MAX_PRIORITY);
         ImportStart = new Date();
         ProgressDialog.Show("Import",ImportThread, ProgressCanceld);
         
       finish();
       
	}
	
	
	    

	private Date ImportStart;
	private int LogImports;
	private int CacheImports;
	
	
	
	private Boolean importCancel = false; 
    final Runnable ProgressCanceld = new Runnable() 
    {
        public void run() 
        {
        	importCancel=true;
            
        }
    };

	
    final Handler ProgressHandler = new Handler();
    final Runnable ProgressReady = new Runnable() 
    {
        public void run() 
        {
        	Date Importfin = new Date();
        	long ImportZeit = Importfin.getTime()-ImportStart.getTime();
        	
        	String Msg="Import " + String.valueOf(CacheImports)+"C "+String.valueOf(LogImports)+"L in "+String.valueOf(ImportZeit);
        	
        	Logger.DEBUG(Msg);
            MessageBox.Show("Import fertig! " + Msg);
            CachListChangedEventList.Call();
        }
    };



	
	
	
	
	

	@Override
	public void OnShow() 
	{
		
                
	}

	@Override
	public void OnHide() 
	{
		
	}

	@Override
	public void OnFree() {
		
	}

	@Override
	public int GetMenuId() {
	
		return 0;
	}

	

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		
		
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		
		
	}

	@Override
	public int GetContextMenuId() {
		
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		
		return false;
	}

	
    
 
}
