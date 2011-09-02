package de.droidcachebox.Views;


import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Core.Config;
import CB_Core.GlobalCore;
import de.droidcachebox.Global;
import de.droidcachebox.R;

import de.droidcachebox.Components.ListenToPhoneState;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;

import de.droidcachebox.Geocaching.JokerEntry;
import de.droidcachebox.Ui.ActivityUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;




public class JokerView extends ListView implements SelectedCacheEvent, ViewOptionsMenu {
	
	CustomAdapter lvAdapter;
	Activity parentActivity;
	Cache aktCache = null;
	JokerEntry aktJoker = null;
	ListenToPhoneState listener = new ListenToPhoneState();

	/**
	 * Constructor
	 */
	public JokerView(final Context context, final Activity parentActivity) {
		super(context);
		this.parentActivity = parentActivity;
		SelectedCacheEventList.Add(this);
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), GlobalCore.SelectedCache());
		this.setAdapter(lvAdapter);
		this.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String TelephoneNumber;
				
				TelephoneNumber = null;
				if (arg2 >= 0) // Nummer raussuchen und unzulässige Zeichen entfernen
					TelephoneNumber = Global.Jokers.get(arg2).Telefon.replaceAll("[^\\d\\+]", "");
				
				// Telefonnummer wählen
				try {
				 	//TelephoneNumber = "0..."; // Telefonnummer zum testen
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					callIntent.setData(Uri.parse("tel:" + TelephoneNumber));
					parentActivity.startActivity(callIntent);
					TelephonyManager tManager = (TelephonyManager)parentActivity.getSystemService(Context.TELEPHONY_SERVICE);
					listener = new ListenToPhoneState();
					tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
					return true;
					} 
				catch (ActivityNotFoundException e) {
			        Logger.Error("JokerView.onItemLongClick", "Call failed", e);		
					Log.e("DroidCachebox", "Call failed", e);
					return false;
					}
				}
			});
	
		
		ActivityUtils.setListViewPropertys(this);
		
	}
	
	static public int windowW=0;
    static public int windowH=0 ;
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
    // we overriding onMeasure because this is where the application gets its right size.
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    windowW = getMeasuredWidth();
    windowH = getMeasuredHeight();
    }

    @Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (aktCache != cache)
		{
			// Wwenn der aktuelle Cache geändert wurde, Telefonjokerliste löschen
			aktCache = cache;
			Global.Jokers.ClearList();
			this.setAdapter(null);
			lvAdapter = new CustomAdapter(getContext(), cache);
			this.setAdapter(lvAdapter);
			lvAdapter.notifyDataSetChanged();
		} else
			invalidate();
	}

	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
		}
	}

	public class CustomAdapter extends BaseAdapter /*implements OnClickListener*/ {
		 
		/*private class OnItemClickListener implements OnClickListener{
		    private int mPosition;
		    OnItemClickListener(int position){
		            mPosition = position;
		    }
		    public void onClick(View arg0) {
		            Log.v("ddd", "onItemClick at position" + mPosition);
		    }
		}*/
	 
	    private Context context;
	    private Cache cache;
	 
	    public CustomAdapter(Context context, Cache cache ) {
	        this.context = context;
	        this.cache = cache;
	    }
	 
	    public void setCache(Cache cache) {
	    	this.cache = cache;
	    
	    }
	    public int getCount() {
	    	if (cache != null)
	    		return Global.Jokers.size();
	    	else
	    		return 0;
	    }
	 
	    public Object getItem(int position) {
	    	if (cache != null)
	    	{
    			return Global.Jokers.get(position);
	    	} else
	    		return null;
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	if (cache != null)
	    	{
	    		Boolean BackGroundChanger = ((position % 2) == 1);
			    JokerEntry joker = Global.Jokers.get(position);
			    JokerViewItem v = new JokerViewItem(context, cache, joker,BackGroundChanger);
			    return v;
	    	} else
	    		return null;
	    }
	 
	    /*public void onClick(View v) {
	            Log.v(LOG_TAG, "Row button clicked");
	    }*/
	 
	}


	@Override
	public boolean ItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
	}

	@Override
	public void OnShow() {
		ActivityUtils.setListViewPropertys(this);
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnFree() {
		
	}

	@Override
	public int GetMenuId() {
		return 0;
	}

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
}

