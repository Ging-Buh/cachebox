package CB_Core.GL_UI.Activitys.FilterSettings;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.Dialogs.StringInputBox;
import CB_Core.GL_UI.Controls.Dialogs.WaitDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

public class EditFilterSettings extends ActivityBase
{
	public static EditFilterSettings that;
	public static CB_RectF ItemRec;

	private MultiToggleButton btPre;
	private MultiToggleButton btSet;
	private MultiToggleButton btCat;

	public PresetListView lvPre;
	private FilterSetListView lvSet;
	private CategorieListView lvCat;
	private Button btnAddPreset;
	public static FilterProperties tmpFilterProps;
	private CB_RectF ListViewRec;

	public EditFilterSettings(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		ItemRec = new CB_RectF(this.getLeftWidth(), 0, this.width - this.getLeftWidth() - this.getRightWidth(),
				UiSizes.getButtonHeight() * 1.1f);

		tmpFilterProps = GlobalCore.LastFilter;

		float innerWidth = this.width - this.getLeftWidth() - this.getLeftWidth();
		CB_RectF MTBRec = new CB_RectF(this.getLeftWidth(), this.height - this.getLeftWidth() - UiSizes.getButtonHeight(), innerWidth / 3,
				UiSizes.getButtonHeight());

		btPre = new MultiToggleButton(MTBRec, "btPre");
		btSet = new MultiToggleButton(MTBRec, "btSet");
		btCat = new MultiToggleButton(MTBRec, "btCat");

		btPre.setX(this.getLeftWidth());
		btSet.setX(btPre.getMaxX());
		btCat.setX(btSet.getMaxX());

		this.addChild(btPre);
		this.addChild(btSet);
		this.addChild(btCat);

		String sPre = GlobalCore.Translations.Get("preset");
		String sSet = GlobalCore.Translations.Get("setting");
		String sCat = GlobalCore.Translations.Get("category");

		MultiToggleButton.initialOn_Off_ToggleStates(btPre, sPre, sPre);
		MultiToggleButton.initialOn_Off_ToggleStates(btSet, sSet, sSet);
		MultiToggleButton.initialOn_Off_ToggleStates(btCat, sCat, sCat);

		btPre.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchVisibility(0);
				return true;
			}
		});

		btSet.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchVisibility(1);
				return true;
			}
		});

		btCat.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchVisibility(2);
				return true;
			}
		});

		btPre.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) switchVisibility(0);
			}
		});

		btSet.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) switchVisibility(1);
			}
		});
		btCat.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) switchVisibility(2);
			}
		});

		Button bOK = new Button(this.getLeftWidth(), this.getLeftWidth(), innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");

		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				lvCat.SetCategory();
				GlobalCore.LastFilter = tmpFilterProps;
				ApplyFilter(GlobalCore.LastFilter);

				// Save selected filter
				Config.settings.Filter.setValue(GlobalCore.LastFilter.ToString());
				Config.AcceptChanges();
				finish();
				return true;
			}
		});

		this.addChild(bOK);

		Button bCancel = new Button(bOK.getMaxX(), this.getLeftWidth(), innerWidth / 2, UiSizes.getButtonHeight(), "Cancel Button");

		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				finish();
				return true;
			}
		});

		this.addChild(bCancel);

		// Translations
		bOK.setText(GlobalCore.Translations.Get("ok"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		ListViewRec = new CB_RectF(0, bOK.getMaxY(), this.width, btPre.getY() - bOK.getMaxY());

		initialPresets();
		initialSettings();
		initialCategorieView();
		fillListViews();

		switchVisibility(0);

	}

	private void initialPresets()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), ListViewRec.getY(), width - this.getRightWidth() - this.getLeftWidth(),
				UiSizes.getButtonHeight());
		btnAddPreset = new Button(rec, "AddPresetButon");
		btnAddPreset.setText(GlobalCore.Translations.Get("AddOwnFilterPreset"));
		btnAddPreset.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				addUserPreset();
				return true;
			}
		});
		this.addChild(btnAddPreset);

		CB_RectF preRec = new CB_RectF(ListViewRec);
		preRec.setHeight(ListViewRec.getHeight() - UiSizes.getButtonHeight() - margin);
		preRec.setY(btnAddPreset.getMaxY() + margin);

		lvPre = new PresetListView(preRec);
		this.addChild(lvPre);
	}

	private void initialSettings()
	{
		lvSet = new FilterSetListView(ListViewRec);
		this.addChild(lvSet);

	}

	private void initialCategorieView()
	{
		lvCat = new CategorieListView(ListViewRec);
		this.addChild(lvCat);
	}

	private void fillListViews()
	{
	}

	private void switchVisibility()
	{
		if (btPre.getState() == 1)
		{
			lvSet.setInvisible();
			lvPre.setVisible();
			lvCat.setInvisible();
			btnAddPreset.setVisible();
			if (lvCat != null) lvCat.SetCategory();
			lvPre.onShow();
		}

		if (btSet.getState() == 1)
		{
			lvPre.setInvisible();
			lvSet.setVisible();
			lvCat.setInvisible();
			btnAddPreset.setInvisible();
			if (lvCat != null) lvCat.SetCategory();
			lvSet.onShow();
		}
		if (btCat.getState() == 1)
		{
			lvPre.setInvisible();
			lvSet.setInvisible();
			lvCat.setVisible();
			btnAddPreset.setInvisible();
			lvCat.onShow();
		}
	}

	private void switchVisibility(int state)
	{
		if (state == 0)
		{
			btPre.setState(1);
			btSet.setState(0);
			btCat.setState(0);
		}
		if (state == 1)
		{
			btPre.setState(0);
			btSet.setState(1);
			btCat.setState(0);
		}
		if (state == 2)
		{
			btPre.setState(0);
			btSet.setState(0);
			btCat.setState(1);
		}

		switchVisibility();
	}

	private static FilterProperties props;

	static WaitDialog pd;

	public static void ApplyFilter(FilterProperties Props)
	{

		props = Props;
		pd = WaitDialog.ShowWait(GlobalCore.Translations.Get("FilterCaches"));

		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					synchronized (Database.Data.Query)
					{
						String sqlWhere = props.getSqlWhere();
						Logger.General("Main.ApplyFilter: " + sqlWhere);
						Database.Data.Query.clear();
						CacheListDAO cacheListDAO = new CacheListDAO();
						cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
					}
					CachListChangedEventList.Call();
					pd.dismis();
					TabMainView.that.filterSetChanged();

					// Notify Map
					if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);
				}
				catch (Exception e)
				{
					pd.dismis();
				}
			}

		};

		thread.start();

	}

	private void addUserPreset()
	{
		GL.that.closeActivity();

		// Check if Preset exist
		boolean exist = false;
		String existName = "";
		for (PresetListViewItem v : lvPre.lItem)
		{
			if (PresetListViewItem.chkPresetFilter(v.getEntry().getPresetString(), tmpFilterProps.ToString()))
			{
				exist = true;
				existName = v.getEntry().getName();
			}
		}

		if (exist)
		{
			GL_MsgBox.Show(GlobalCore.Translations.Get("PresetExist") + GlobalCore.br + GlobalCore.br + "\"" + existName + "\"", null,
					MessageBoxButtons.OK, MessageBoxIcon.Warning, new OnMsgBoxClickListener()
					{

						@Override
						public boolean onClick(int which)
						{
							that.show();
							return true;
						}
					});
			return;
		}

		StringInputBox.Show(TextFieldType.SingleLine, GlobalCore.Translations.Get("NewUserPreset"),
				GlobalCore.Translations.Get("InsNewUserPreset"), "UserPreset", new OnMsgBoxClickListener()
				{

					@Override
					public boolean onClick(int which)
					{
						String text = StringInputBox.editText.getText();
						// Behandle das ergebniss
						switch (which)
						{
						case 1: // ok Clicket
							String uF = Config.settings.UserFilter.getValue();
							String aktFilter = tmpFilterProps.ToString();

							// Category Filterungen aus Filter entfernen
							int pos = aktFilter.indexOf("^");
							aktFilter = aktFilter.substring(0, pos);

							uF += text + ";" + aktFilter + "#";
							Config.settings.UserFilter.setValue(uF);
							Config.AcceptChanges();
							lvPre.fillPresetList();
							lvPre.notifyDataSetChanged();
							that.show();
							break;
						case 2: // cancel clicket
							that.show();
							break;
						case 3:
							that.show();
							break;
						}

						return true;
					}
				});
	}

}
