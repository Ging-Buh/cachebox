/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package de.droidcachebox.Components;

import java.util.Iterator;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.DAO.CacheListDAO;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.Forms.ImportDialog;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;

/**
 * Enthält die Methoden für die Suche in der DB oder über die API
 * @author Longri
 *
 */
public class search {

	/**
	 * True, wenn der Searchdialog sichtbar ist.
	 */
	private boolean mIsVisible=true;
	
	/**
	 * Pointer zu main, wird mit dem Constructor übergeben
	 */
	private main mPtrMain = null;
	
	/**
	 * True, wenn eine Suche läuft und der Iterator mit Next 
	 * weiter durchlaufen werden kann.
	 */
	private boolean mSearchAktive=false;
	
	
	/**
	 * True, wenn die QuickButtonList beim öffnen dieses
	 * Dialogs aufgeklappt war.
	 */
	private boolean mQuickButtonListWasShow;
	
	/*
	 * Buttons
	 */
	
	/**
	 * Option Title, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnTitle;
	
	/**
	 * Option GC-Code, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnGc;
	
	/**
	 * Option Owner, der drei Optionen Title/GC-Code/Owner
	 */
	private MultiToggleButton mTglBtnOwner;
	
	
	/**
	 * Option Online Suche On/Off
	 */
	private MultiToggleButton mTglBtnOnline;
	
	
	/**
	 * Button, welcher eine Suchanfrage als Filter verwendet
	 */
	private Button mBtnFilter;
	
	
	/**
	 * Button, der eine Suche Startet
	 */
	private Button mBtnSearch;
	
	
	/**
	 * Button, der den nächsten Treffer einer gestarteten Suche findet
	 */
	private Button mBtnNext;
	
	
	/**
	 * Button, der den Search Dialog schliesst
	 */
	private Button mBtnCancel;
	
	
	/**
	 * Such Eingabe Feld
	 */
	private EditText mEingabe;
	
	/**
	 * enthält den Index des element der CacheListe,
	 * an der die Suche steht
	 */
	private int mSearchIndex = -1;
	
	/**
	 * Enthält einen Iterator der aktuell durschten
	 * CacheList
	 */
	private Iterator <Cache> CacheListIterator = null;
	
	/**
	 * Enthällt den Aktuellen Such Modus
	 * <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 * 
	 */
	private int mSearchState=0;
	
	/**
	 * Constructor mit Übergabe der main Class
	 * zur initalisierung des Dialogs mit all seinen Buttons
	 * @param Main
	 */
	public search(main Main)
	{
		mPtrMain=Main;
		
		// initial Buttons
		mTglBtnTitle = (MultiToggleButton) mPtrMain.findViewById(R.id.search_title);
		mTglBtnGc = (MultiToggleButton) mPtrMain.findViewById(R.id.search_gc);
		mTglBtnOwner = (MultiToggleButton) mPtrMain.findViewById(R.id.search_owner);
		mTglBtnOnline = (MultiToggleButton) mPtrMain.findViewById(R.id.search_on);
		
		mBtnFilter = (Button) mPtrMain.findViewById(R.id.search_filter);
		mBtnSearch = (Button) mPtrMain.findViewById(R.id.search_search);
		mBtnNext = (Button) mPtrMain.findViewById(R.id.search_next);
		mBtnCancel = (Button) mPtrMain.findViewById(R.id.search_cancel);
		
		mEingabe = (EditText) mPtrMain.findViewById(R.id.search_edit);
		
		mEingabe.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				textBox_TextChanged();
				
			}
		});
		
		mEingabe.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// wenn das Eingabe Feld den Focus verliert,
				// kann die Tastertur wieder zu!
				
				if(!arg1) // if lost the Focus
				{
					closeSoftKeyPad();
				}
				
			}
		});
		
		mEingabe.setText("");
		
		setLang();
		switchSearcheMode(0);
		
		mBtnCancel.setOnClickListener(cancelButtonOnClick);
		
		mTglBtnTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				switchSearcheMode(0);
				
			}
		});

		mTglBtnGc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				switchSearcheMode(1);
				
			}
		});
	
		mTglBtnOwner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				switchSearcheMode(2);
				
			}
		});
	
		mBtnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				closeSoftKeyPad();
				mSearchAktive=false;
				mSearchIndex=-1;
				searchNow(false);
			}
		});
		
		mBtnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				closeSoftKeyPad();
				searchNow(false);
				
			}
		});
		
		mBtnFilter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				closeSoftKeyPad();
				setFilter();
			}
		});
		
	}
	
	
	/**
	 * Blendet den search Dialog ein
	 */
	public void Show()
	{
		
		setBackGroundDrawable();
		
		// set Visibility to VISIBLE nur in CacheListView und MapView
		mPtrMain.searchLayout.setVisibility(View.VISIBLE);
		mIsVisible=true;
		setLang();
		
		// wenn QuickButtonList ausgeklappt, dann schliesse sie und öffene sie erst wieder mit dem
		// Schliessen dieses Dialogs
		mQuickButtonListWasShow = Config.GetBool("quickButtonLastShow") && Config.GetBool("quickButtonShow") ;
		mPtrMain.setQuickButtonHeight(0);
		Config.Set("quickButtonShow",false);
		Config.AcceptChanges();
		
		//initalisier mit Title Suche 
		switchSearcheMode(0);
		
		mEingabe.setText("");
	}
	
	/**
	 * Blendet den search Dialog aus
	 */
	public void Hide()
	{
		// set Visibility to GONE
		mPtrMain.searchLayout.setVisibility(View.GONE);
		mIsVisible=false;
		mSearchAktive=false;
		
		//QuickButtonList wieder einblenden, wenn sie von diesem
		//Dialog ausgeblendet wurden
		if(mQuickButtonListWasShow)
		{
			Config.Set("quickButtonShow",true);
			Config.AcceptChanges();
			mPtrMain.setQuickButtonHeight(Sizes.getQuickButtonListHeight());
		}
	}
	
	/**
	 * Lifert True, wenn der Search Dialog eingeblendet ist.
	 * Andernfalls False;
	 * @return boolean
	 */
	public boolean isVisible()
	{
		return mIsVisible;
	}
	
	/**
	 * Setzt die Übersetzten Texte auf die Buttons
	 */
	private void setLang()
	{
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, Global.Translations.Get("Title"), Global.Translations.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, Global.Translations.Get("GCCode"), Global.Translations.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, Global.Translations.Get("Owner"), Global.Translations.Get("Owner"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOnline, "Online", "Online");
		
		// der State muss erstmal gesetzt werden, damit die Anzeige Aktuallisiert wird
		mTglBtnOnline.setState(0);
		
		mBtnFilter.setText(Global.Translations.Get("Filter"));
		mBtnSearch.setText(Global.Translations.Get("Search"));
		mBtnNext.setText(Global.Translations.Get("Next"));
		mBtnCancel.setText(Global.Translations.Get("abort"));
		
	}	

	/**
	 * Setzt die Hintergrund Images des Dialogs
	 * in Abhängigkeit vom Theme
	 */
	private void setBackGroundDrawable()
	{
		Resources res = mPtrMain.getResources();
    	
    	
    	Drawable header = res.getDrawable(main.N? R.drawable.night_header : R.drawable.header);
//    	Drawable title = res.getDrawable(main.N? R.drawable.night_title : R.drawable.title);
    	Drawable center = res.getDrawable(main.N? R.drawable.night_center : R.drawable.center);
    	Drawable footer = res.getDrawable(main.N? R.drawable.night_footer : R.drawable.footer);
    	
    	((LinearLayout) mPtrMain.findViewById(R.id.header)).setBackgroundDrawable(header);
//    	((TextView) mPtrMain.findViewById(R.id.title)).setBackgroundDrawable(title);
    	((LinearLayout) mPtrMain.findViewById(R.id.content)).setBackgroundDrawable(center);
    	((RelativeLayout) mPtrMain.findViewById(R.id.footer)).setBackgroundDrawable(footer);
	}
	

	private OnClickListener cancelButtonOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Hide();
			
		}
	};


	/**
	 * Schaltet den Such Modus um.
	 * @param state
	 * <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 */
	private void switchSearcheMode(int state)
	{
		mSearchState = state;
		
		if(state==0)
		{
			mTglBtnTitle.setState(1);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(0);
		}
		if(state==1)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(1);
			mTglBtnOwner.setState(0);
		}
		if(state==2)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(1);
		}
		
	}

	
	private void textBox_TextChanged()
    {
        // reset SearchIndex, because of text changed.
        mSearchIndex = -1;
        
        boolean isText = mEingabe.getEditableText().toString().length() != 0;
        ActivityUtils.setBtnState(mBtnSearch, isText);
        ActivityUtils.setBtnState(mBtnFilter, isText);
        ActivityUtils.setBtnState(mBtnNext, false);
       
    }

	/**
	 * schliesst die virtuelle Tastertur
	 */
	private void closeSoftKeyPad()
	{
		// close the virtual keyboard
		InputMethodManager mgr = (InputMethodManager) mPtrMain.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(mEingabe.getWindowToken(), 0);

	}
	
	/**
	 * Die aktive CahcheList wird durchsucht
	 * gefilterte Caches werden dabei nicht berücksichtigt.
	 *
	 * @param ignoreOnlineSearch (True, wenn Lokal gesucht werden soll, 
	 * obwohl der MultiToggleButton "Online" aktiviert ist.
	 */
	private void searchNow(boolean ignoreOnlineSearch)
	{
		
		
		if(!ignoreOnlineSearch && mTglBtnOnline.getState()==0)
		{

			 mSearchIndex++;

	         String searchPattern = mEingabe.getEditableText().toString().toLowerCase();

	         boolean criterionMatches = false;

	         if(!mSearchAktive)
	         {
	        	 CacheListIterator = Database.Data.Query.iterator();
	        	 mSearchAktive=true;
	         }
	         
	         
	         Cache tmp = null;
	         while (CacheListIterator.hasNext() && !criterionMatches)
	         {
	        	 
	        	 tmp = CacheListIterator.next();
	        	 
	             criterionMatches = (mSearchState == 0 && tmp.Name.toLowerCase().contains(searchPattern))  ||
	                                (mSearchState == 1 && tmp.GcCode.toLowerCase().contains(searchPattern))  ||
	                                (mSearchState == 2 && tmp.Owner.toLowerCase().contains(searchPattern))  ||
	                                (mSearchState == 2 && tmp.PlacedBy.toLowerCase().contains(searchPattern)) ;

	             if (!criterionMatches)
	                 mSearchIndex++;
	         }

	         if (!CacheListIterator.hasNext())
	         {
	        	 ActivityUtils.setBtnState(mBtnNext, false);
	        	 mSearchAktive=false;
	             MessageBox.Show("No cache found", "Search", MessageBoxButtons.OK, MessageBoxIcon.Asterisk,null);
	         }
	         else
	         {
	        	 
	     		Waypoint finalWp = null;
	     		if (tmp.HasFinalWaypoint())
	     			finalWp = tmp.GetFinalWaypoint();
	     		if (tmp != null)
	     			GlobalCore.SelectedWaypoint(tmp, finalWp); 
	             // deactivate autoResort when Cache is selected by hand
	             Global.autoResort = false;
	             
	             ActivityUtils.setBtnState(mBtnNext, true);
	         }
		}
		else
		{
			searchOnlineNow();
		}
		
		
	}
	
	/**
	 * Sucht mit den Vorgaben nach Caches über die API.
	 * Die Gefundenen Caches werden in die DB eingetragen und
	 * im Anschluss wird der lokale Suchvorgang gestartet. 
	 */
	private void searchOnlineNow()
	{
		// TODO Suche Online nach Caches und schreibe sie in die DB.
		
		//Lokale Suche ausführen!
		searchNow(true);
	}
	
	
	/**
	 * setzt den Filter auf die Such Anfrage
	 */
	private void setFilter()
	{
		String searchPattern =  mEingabe.getEditableText().toString().toLowerCase();

		String where = "";
        if (Global.LastFilter.toString().length() > 0)
            where = " AND (";

        Global.LastFilter.filterName = "";
        Global.LastFilter.filterGcCode = "";
        Global.LastFilter.filterOwner = "";

        if (mSearchState == 0)
            Global.LastFilter.filterName = searchPattern;
        else if (mSearchState == 1)
            Global.LastFilter.filterGcCode = searchPattern;
        if (mSearchState == 2)
            Global.LastFilter.filterOwner = searchPattern;

        ApplyFilter();
	}
	
    private android.app.ProgressDialog pd;
	private FilterProperties props;
	public void ApplyFilter()
	    {
		
		
			
			props = Global.LastFilter;
		  pd = android.app.ProgressDialog.show(mPtrMain, "", 
                 Global.Translations.Get("LoadCaches"), true);
		
		  Thread thread = new Thread()
		  {
		      @Override
		      public void run() 
		      {
		    	  String sqlWhere =props.getSqlWhere();
			      Logger.General("Main.ApplyFilter: " + sqlWhere);
			      Database.Data.Query.clear();
			      CacheListDAO cacheListDAO = new CacheListDAO();
			      cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
			      messageHandler.sendMessage(messageHandler.obtainMessage(1));
		      }

		  };

		  thread.start();
	    }
	
	  private Handler messageHandler = new Handler() {

	      @Override
	      public void handleMessage(Message msg) 
	      {
	    	  switch(msg.what) 
	    	  {
	    	  case 1:
	    	  	{
	    	  		CachListChangedEventList.Call();
	    	  		pd.dismiss();
	    	  		Toast.makeText(main.mainActivity, "Applay filter. Found " + String.valueOf(Database.Data.Query.size()) + " Caches!", Toast.LENGTH_LONG).show();
	    	  			
	    	  	}
		 	     
	    	  }
	    	  
	      }

	  };
	
}
