package de.droidcachebox.Views.Forms;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.JokerViewItem;
import de.droidcachebox.Views.CacheListView.CustomAdapter;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import CB_Core.GlobalCore;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.JokerEntry;

public class ApiPQDialog extends Activity implements ViewOptionsMenu
{
	public static ApiPQDialog Me;

	private Context context;
	private ListView listView;
	private Button CancelButton;
	private Button OKButton;
	private ArrayList<PQ> PqList;
	private CustomAdapter lvAdapter;

	public ApiPQDialog(ArrayList<PQ> pqList)
	{
		PqList = pqList;
	}

	public ApiPQDialog()
	{

	}

	public void onCreate(Bundle savedInstanceState)
	{
//		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pq_list_view_layout);
		Me = this;

		context = this.getBaseContext();
		findViewById();
		
		
		// übergebene PQ List auslesen und an ListView übergeben
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) PqList = (ArrayList<PQ>) bundle.getSerializable("PqList");

		if (PqList == null) PqList = new ArrayList<PQ>();

		lvAdapter = new CustomAdapter(main.mainActivity, PqList);
		listView.setAdapter(lvAdapter);

		((TextView) this.findViewById(R.id.title)).setText("Import");

		

		OKButton.setText(GlobalCore.Translations.Get("ok"));
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

		initialForm();

	}

	private void findViewById()
	{
		CancelButton = (Button) this.findViewById(R.id.cancelButton);
		OKButton = (Button) this.findViewById(R.id.OkButton);
		listView = (ListView) this.findViewById(R.id.pq_ListView);
	}

	private void initialForm()
	{
		OKButton.setWidth(Sizes.getButtonWidthWide());
		CancelButton.setWidth(Sizes.getButtonWidthWide());
		OKButton.setHeight(Sizes.getButtonHeight());
		CancelButton.setHeight(Sizes.getButtonHeight());
	}

	@Override
	public void OnShow()
	{

	}

	@Override
	public void OnHide()
	{

	}

	@Override
	public void OnFree()
	{

	}

	@Override
	public int GetMenuId()
	{

		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{

	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{

		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{

	}

	@Override
	public int GetContextMenuId()
	{

		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{

	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{

		return false;
	}

	public class CustomAdapter extends BaseAdapter
	{

		private Context context;
		private ArrayList<PQ> list;

		public CustomAdapter(Context context, ArrayList<PQ> pqList)
		{
			this.context = context;
			this.list = pqList;
		}

		public int getCount()
		{
			if (list != null) return list.size();
			else
				return 0;
		}

		public Object getItem(int position)
		{
			if (list != null)
			{
				return list.get(position);
			}
			else
				return null;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (list != null)
			{
				View row = convertView;
				PQ pq = list.get(position);
				if (row == null)
				{
					LayoutInflater inflater = getLayoutInflater();
					row = inflater.inflate(R.layout.pq_list_view_item, parent, false);
				}

				TextView label = (TextView) row.findViewById(R.id.textView1);
				label.setText(pq.Name);
				label.setTextSize(Sizes.getScaledFontSize_normal());
				label.setTextColor(Global.getColor(R.attr.TextColor));
				
				TextView label2 = (TextView) row.findViewById(R.id.textView2);
				SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy"); 
			    String dateString = postFormater.format(pq.DateLastGenerated); 
			    DecimalFormat df = new DecimalFormat("###.##");
			    String FileSize = df.format(pq.SizeMB)+" MB";
			    String Count = "   Count="+String.valueOf(pq.PQCount);
				label2.setText(dateString+"  "+FileSize+Count);
				label2.setTextSize((float) (Sizes.getScaledFontSize_small()*0.8));
				label2.setTextColor(Global.getColor(R.attr.TextColor));
				return row;
			}
			else
				return null;
		}

	}

}
