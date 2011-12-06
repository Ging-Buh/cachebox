package de.cachebox_test.Views.AdvancedSettingsForms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Types.MoveableList;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Custom_Controls.QuickButtonList.QuickButtonItem;
import de.cachebox_test.Enums.Actions;
import de.cachebox_test.Ui.Sizes;

public class SettingsListEditQuickButton extends Activity
{
	public static SettingsListEditQuickButton Me;

	private Context context;

	private Button CancelButton;
	private Button OKButton;

	private ListView ActionListView;
	private int ActionListSelectedIndex = -1;
	private Button ActionListUp;
	private Button ActionListDown;
	private Button ActionListDel;
	private Button ActionListAdd;
	private Spinner ActionListAll;
	private boolean ActionListChanged = false;
	private ArrayList<Actions> AllActionList;
	private boolean ActionListButtonAddClicked = false;
	private CheckBox chkQuickButtonShow;
	private static int TextViewWidth = 0;

	public void onCreate(Bundle savedInstanceState)
	{
		// ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_settings_quick_button);

		context = this.getBaseContext();
		Me = this;
		findViewById();

		// loadSettingsFromDB();

		OKButton.setText(GlobalCore.Translations.Get("ok"));
		CancelButton.setText(GlobalCore.Translations.Get("cancel"));

		OKButton.setWidth(Sizes.getButtonWidthWide());
		CancelButton.setWidth(Sizes.getButtonWidthWide());
		OKButton.setHeight(Sizes.getQuickButtonHeight());
		CancelButton.setHeight(Sizes.getQuickButtonHeight());

		Config.settings.SaveToLastValue();

		ActionListUp.setWidth(Sizes.getButtonWidth());
		ActionListDown.setWidth(Sizes.getButtonWidth());
		ActionListDel.setWidth(Sizes.getButtonWidth());
		ActionListAdd.setWidth(Sizes.getButtonWidth());

		CancelButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Config.settings.LoadFromLastValue();
				finish();
			}
		});

		OKButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				boolean QuickButtonShowChanged = (Config.settings.quickButtonShow.getValue() != chkQuickButtonShow.isChecked());

				Config.settings.quickButtonShow.setValue(chkQuickButtonShow.isChecked());
				String ActionsString = "";
				int counter = 0;
				for (QuickButtonItem tmp : Global.QuickButtonList)
				{
					ActionsString += String.valueOf(tmp.getAction().ordinal());
					if (counter < Global.QuickButtonList.size() - 1)
					{
						ActionsString += ",";
					}
					counter++;
				}
				Config.settings.quickButtonList.setValue(ActionsString);

				if (QuickButtonShowChanged)
				{
					downSlider.ButtonShowStateChanged();
				}

				finish();

			}
		});

		ActionListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{

				ActionListSelectedIndex = arg2;
				resortList();
			}
		});
		ActionListUp.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				if (ActionListSelectedIndex < 0 || ActionListSelectedIndex > Global.QuickButtonList.size()) return; // wrong
																													// index
				ActionListSelectedIndex = Global.QuickButtonList.MoveItem(ActionListSelectedIndex, -1);
				resortList();
			}
		});
		ActionListDown.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				if (ActionListSelectedIndex < 0 || ActionListSelectedIndex > Global.QuickButtonList.size()) return; // wrong
																													// index
				ActionListSelectedIndex = Global.QuickButtonList.MoveItem(ActionListSelectedIndex, +1);
				resortList();
			}
		});
		ActionListDel.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				if (ActionListSelectedIndex > -1 && Global.QuickButtonList.size() > ActionListSelectedIndex)
				{
					Global.QuickButtonList.remove(ActionListSelectedIndex);
					resortList();
					ActionListSelectedIndex = -1;
				}

			}
		});
		ActionListAdd.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				ActionListAll.setSelection(AllActionList.size() - 1);
				ActionListAll.setVisibility(View.VISIBLE);
				ActionListButtonAddClicked = false;
				ActionListAll.performClick();
			}
		});
		ActionListAll.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{

				if ((ActionListAll.getVisibility() == View.VISIBLE) && ActionListButtonAddClicked)
				{
					ActionListButtonAddClicked = false;

					// neues Action Item ausgewählt.
					if (Global.QuickButtonList == null)
					{
						Global.QuickButtonList = new MoveableList<QuickButtonItem>();
						ActionListView.setAdapter(QuickListBaseAdapter);
					}
					Global.QuickButtonList.add(new QuickButtonItem(context, AllActionList.get(arg2), Sizes.getQuickButtonHeight()));
					resortList();

					ActionListAll.setVisibility(View.INVISIBLE);
				}
				else
				{
					ActionListButtonAddClicked = true;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				ActionListAll.setVisibility(View.INVISIBLE);
			}
		});

		chkQuickButtonShow.setText(GlobalCore.Translations.Get("ShowQuickButton"));
		ActionListUp.setText(GlobalCore.Translations.Get("up"));
		ActionListDown.setText(GlobalCore.Translations.Get("down"));
		ActionListDel.setText(GlobalCore.Translations.Get("delete"));
		ActionListAdd.setText(GlobalCore.Translations.Get("add"));

		resortList();

	}

	public void resortList()
	{
		ActionListButtonAddClicked = false;
		chkQuickButtonShow.setChecked(Config.settings.quickButtonShow.getValue());
		ActionListView.setAdapter(QuickListBaseAdapter);
		ActionListChanged = false;

		QuickListBaseAdapter.notifyDataSetChanged();
		ActionListView.invalidate();

		((main) main.mainActivity).QuickButtonsAdapter.notifyDataSetChanged();

		AllActionList = new ArrayList<Actions>();
		Actions[] tmp = Actions.values();
		for (Actions item : tmp)
		{
			boolean exist = false;
			for (Iterator<QuickButtonItem> it = Global.QuickButtonList.iterator(); it.hasNext();)
			{
				QuickButtonItem listItem = it.next();
				if (listItem.getAction() == item) exist = true;
			}
			if (!exist) AllActionList.add(item);
		}

		ArrayAdapter<Actions> ActionListAdapter = new ArrayAdapter<Actions>(this, android.R.layout.simple_spinner_item, AllActionList);
		ActionListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ActionListAll.setAdapter(ActionListAdapter);

		if (ActionListSelectedIndex > -1)
		{
			ActionListView.setSelectionFromTop(ActionListSelectedIndex, Sizes.getQuickButtonHeight());
		}

	}

	public void findViewById()
	{
		CancelButton = (Button) this.findViewById(R.id.cancelButton);
		OKButton = (Button) this.findViewById(R.id.OkButton);

		ActionListView = (ListView) findViewById(R.id.settings_quick_list);
		ActionListUp = (Button) findViewById(R.id.settings_quick_up);
		ActionListDown = (Button) findViewById(R.id.settings_quick_down);
		ActionListDel = (Button) findViewById(R.id.settings_quick_del);
		ActionListAdd = (Button) findViewById(R.id.settings_quick_add);
		ActionListAll = (Spinner) this.findViewById(R.id.settings_spinner_Action);
		chkQuickButtonShow = (CheckBox) this.findViewById(R.id.settings_quick_button_show);
	}

	private BaseAdapter QuickListBaseAdapter = new BaseAdapter()
	{

		@Override
		public int getCount()
		{
			if (Global.QuickButtonList == null)
			{
				return 0;
			}
			else
			{
				return Global.QuickButtonList.size();
			}

		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (Global.QuickButtonList == null)
			{
				return null;
			}

			String Name = Global.QuickButtonList.get(position).getDesc();
			View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_list_item, null);
			TextView title = (TextView) retval.findViewById(R.id.title);
			ImageView image = (ImageView) retval.findViewById(R.id.image);
			LinearLayout layout = (LinearLayout) retval.findViewById(R.id.layout);
			title.setText(Name);
			image.setImageDrawable(Global.QuickButtonList.get(position).getIcon());
			int BackGroundColor = (position != ActionListSelectedIndex) ? Global.getColor(R.attr.ListBackground) : Global
					.getColor(R.attr.ListBackground_select);
			layout.setBackgroundColor(BackGroundColor);

			if (TextViewWidth <= 0)
			{
				ImageView iv = (ImageView) retval.findViewById(R.id.image);
				TextViewWidth = (parent.getWidth() - iv.getWidth() - 20);
			}

			TextView tv = (TextView) retval.findViewById(R.id.title);
			tv.setWidth(TextViewWidth);
			return retval;
		}

	};

	@Override
	public void onDestroy()
	{
		Me = null;
		super.onDestroy();
	}

	public void ListInvalidate()
	{
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				Thread t = new Thread()
				{
					public void run()
					{
						SettingsListEditQuickButton.Me.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								SettingsListEditQuickButton.Me.resortList();
							}
						});
					}
				};

				t.start();

			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 500);
	}

}
