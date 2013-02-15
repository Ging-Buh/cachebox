package CB_Core.GL_UI.Activitys.FilterSettings;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Controls.Box;
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
import CB_Core.TranslationEngine.Translation;

public class EditFilterSettings extends ActivityBase
{
	public static EditFilterSettings that;
	public static CB_RectF ItemRec;

	private MultiToggleButton btPre;
	private MultiToggleButton btSet;
	private MultiToggleButton btCat;
	private MultiToggleButton btTxt;

	private Box contentBox;

	public PresetListView lvPre;
	private FilterSetListView lvSet;
	private CategorieListView lvCat;
	private TextFilterView vTxt;
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

		float innerWidth = this.width - this.getLeftWidth();

		Button bOK = new Button(this.getLeftWidth() / 2, this.getLeftWidth(), innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");

		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				lvCat.SetCategory();
				GlobalCore.LastFilter = tmpFilterProps;

				// Text Filter ?
				String txtFilter = vTxt.getFilterString();
				if (txtFilter.length() > 0)
				{
					int FilterMode = vTxt.getFilterState();
					if (FilterMode == 0) GlobalCore.LastFilter.filterName = txtFilter;
					else if (FilterMode == 1) GlobalCore.LastFilter.filterGcCode = txtFilter;
					else if (FilterMode == 2) GlobalCore.LastFilter.filterOwner = txtFilter;
				}
				else
				{
					GlobalCore.LastFilter.filterName = "";
					GlobalCore.LastFilter.filterGcCode = "";
					GlobalCore.LastFilter.filterOwner = "";
				}

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

		float topButtonY = this.height - this.getLeftWidth() - UiSizes.getButtonHeight();

		contentBox = new Box(new CB_RectF(0, bOK.getMaxY(), this.width, topButtonY - bOK.getMaxY()), "contentBox");
		contentBox.setBackground(SpriteCache.activityBackground);
		this.addChild(contentBox);

		CB_RectF MTBRec = new CB_RectF(this.getLeftWidth() / 2, topButtonY, innerWidth / 4, UiSizes.getButtonHeight());

		btPre = new MultiToggleButton(MTBRec, "btPre");
		btSet = new MultiToggleButton(MTBRec, "btSet");
		btCat = new MultiToggleButton(MTBRec, "btCat");
		btTxt = new MultiToggleButton(MTBRec, "btTxt");

		// btPre.setX(this.getLeftWidth());
		btSet.setX(btPre.getMaxX());
		btCat.setX(btSet.getMaxX());
		btTxt.setX(btCat.getMaxX());

		this.addChild(btPre);
		this.addChild(btSet);
		this.addChild(btCat);
		this.addChild(btTxt);

		String sPre = Translation.Get("preset");
		String sSet = Translation.Get("setting");
		String sCat = Translation.Get("category");
		String sTxt = Translation.Get("text");

		MultiToggleButton.initialOn_Off_ToggleStates(btPre, sPre, sPre);
		MultiToggleButton.initialOn_Off_ToggleStates(btSet, sSet, sSet);
		MultiToggleButton.initialOn_Off_ToggleStates(btCat, sCat, sCat);
		MultiToggleButton.initialOn_Off_ToggleStates(btTxt, sTxt, sTxt);

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
		btTxt.setOnStateChangedListner(new OnStateChangeListener()
		{
			@Override
			public void onStateChange(GL_View_Base v, int State)
			{
				if (State == 1) switchVisibility(3);
			}
		});

		// Translations
		bOK.setText(Translation.Get("ok"));
		bCancel.setText(Translation.Get("cancel"));

		ListViewRec = new CB_RectF(0, margin, this.width, btPre.getY() - bOK.getMaxY() - margin - margin);

		initialPresets();
		initialSettings();
		initialCategorieView();
		fillListViews();
		initialTextView();

		switchVisibility(0);

	}

	private void initialPresets()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), margin, width - this.getRightWidth() - this.getLeftWidth(),
				UiSizes.getButtonHeight());
		btnAddPreset = new Button(rec, "AddPresetButon");
		btnAddPreset.setText(Translation.Get("AddOwnFilterPreset"));
		btnAddPreset.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				addUserPreset();
				return true;
			}
		});
		contentBox.addChild(btnAddPreset);

		CB_RectF preRec = new CB_RectF(ListViewRec);
		preRec.setHeight(ListViewRec.getHeight() - UiSizes.getButtonHeight() - margin);
		preRec.setY(btnAddPreset.getMaxY() + margin);

		lvPre = new PresetListView(preRec);
		contentBox.addChild(lvPre);
	}

	private void initialSettings()
	{
		lvSet = new FilterSetListView(ListViewRec);
		contentBox.addChild(lvSet);

	}

	private void initialCategorieView()
	{
		lvCat = new CategorieListView(ListViewRec);
		contentBox.addChild(lvCat);
	}

	private void initialTextView()
	{
		vTxt = new TextFilterView(ListViewRec, "TextFilterView");
		contentBox.addChild(vTxt);
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
			vTxt.setInvisible();
			btnAddPreset.setVisible();
			if (lvCat != null) lvCat.SetCategory();
			lvPre.onShow();
		}

		if (btSet.getState() == 1)
		{
			lvPre.setInvisible();
			lvSet.setVisible();
			lvCat.setInvisible();
			vTxt.setInvisible();
			btnAddPreset.setInvisible();
			if (lvCat != null) lvCat.SetCategory();
			lvSet.onShow();
		}
		if (btCat.getState() == 1)
		{
			lvPre.setInvisible();
			lvSet.setInvisible();
			lvCat.setVisible();
			vTxt.setInvisible();
			btnAddPreset.setInvisible();
			lvCat.onShow();
		}
		if (btTxt.getState() == 1)
		{
			lvPre.setInvisible();
			lvSet.setInvisible();
			lvCat.setInvisible();
			vTxt.setVisible();
			btnAddPreset.setInvisible();
			vTxt.onShow();
		}

	}

	private void switchVisibility(int state)
	{
		if (state == 0)
		{
			btPre.setState(1);
			btSet.setState(0);
			btCat.setState(0);
			btTxt.setState(0);
		}
		if (state == 1)
		{
			btPre.setState(0);
			btSet.setState(1);
			btCat.setState(0);
			btTxt.setState(0);
		}
		if (state == 2)
		{
			btPre.setState(0);
			btSet.setState(0);
			btCat.setState(1);
			btTxt.setState(0);
		}
		if (state == 3)
		{
			btPre.setState(0);
			btSet.setState(0);
			btCat.setState(0);
			btTxt.setState(1);
		}

		switchVisibility();
	}

	private static FilterProperties props;

	static WaitDialog pd;

	public static void ApplyFilter(final FilterProperties Props)
	{

		props = Props;
		pd = WaitDialog.ShowWait(Translation.Get("FilterCaches"));

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
						Database.Data.Query.checkSelectedCacheValid();
					}
					CachListChangedEventList.Call();
					pd.dismis();
					TabMainView.that.filterSetChanged();

					// Notify Map
					if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);

					// save Filtersettings
					Config.settings.Filter.setValue(Props.ToString());
					Config.AcceptChanges();
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
			GL_MsgBox.Show(Translation.Get("PresetExist") + GlobalCore.br + GlobalCore.br + "\"" + existName + "\"", null,
					MessageBoxButtons.OK, MessageBoxIcon.Warning, new OnMsgBoxClickListener()
					{

						@Override
						public boolean onClick(int which, Object data)
						{
							that.show();
							return true;
						}
					});
			return;
		}

		StringInputBox.Show(TextFieldType.SingleLine, Translation.Get("NewUserPreset"), Translation.Get("InsNewUserPreset"), "UserPreset",
				new OnMsgBoxClickListener()
				{

					@Override
					public boolean onClick(int which, Object data)
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

	@Override
	public void onShow()
	{
		// Load and set TxtFilter
		if (vTxt != null)
		{
			if (GlobalCore.LastFilter.filterName.length() > 0) vTxt.setFilterString(GlobalCore.LastFilter.filterName, 0);
			else if (GlobalCore.LastFilter.filterGcCode.length() > 0) vTxt.setFilterString(GlobalCore.LastFilter.filterGcCode, 1);
			else if (GlobalCore.LastFilter.filterOwner.length() > 0) vTxt.setFilterString(GlobalCore.LastFilter.filterOwner, 2);
		}
	}
}
