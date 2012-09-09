package CB_Core.GL_UI.Controls.PopUps;

import java.util.Iterator;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DB.Database;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.SearchOverPosition;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.Slider;
import CB_Core.GL_UI.Controls.Slider.YPositionChanged;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Views.CacheListView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SearchDialog extends PopUp_Base
{
	public static SearchDialog that;

	/**
	 * True, wenn der Searchdialog sichtbar ist.
	 */
	private boolean mIsVisible = true;

	/**
	 * True, wenn eine Suche läuft und der Iterator mit Next weiter durchlaufen werden kann.
	 */
	private boolean mSearchAktive = false;

	/**
	 * True, wenn die QuickButtonList beim öffnen dieses Dialogs aufgeklappt war.
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
	private ImageButton mBtnFilter;

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
	private EditWrapedTextField mEingabe;

	/**
	 * enthält den Index des element der CacheListe, an der die Suche steht
	 */
	private int mSearchIndex = -1;

	/**
	 * Enthält einen Iterator der aktuell durschten CacheList
	 */
	private Iterator<Cache> CacheListIterator = null;

	/**
	 * Enthällt den Aktuellen Such Modus <br/>
	 * 0 = Title <br/>
	 * 1 = Gc-Code <br/>
	 * 2 = Owner <br/>
	 */
	private int mSearchState = 0;

	public SearchDialog()
	{
		super(new CB_RectF(), "SearchDialog");

		that = this;

		this.setSize(UiSizes.getCacheListItemSize().asFloat());

		if (GlobalCore.isTab)
		{
			// TODO Set Background mit Rahmen
		}
		else
		{
			this.setBackground(SpriteCache.ListBack);
		}
		// initial Buttons

		float margin = UiSizes.getMargin();
		float btnWidth = (this.width - (margin * 7)) / 4;

		CB_RectF rec = new CB_RectF(0, 0, btnWidth, UiSizes.getButtonHeight());

		mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
		mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
		mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");
		mTglBtnOnline = new MultiToggleButton(rec, "mTglBtnOnline");

		rec.setWidth(btnWidth = (this.width - (margin * 5)) / 4);

		mBtnFilter = new ImageButton(rec, "mBtnFilter");
		mBtnSearch = new Button(rec, "mBtnSearch");
		mBtnNext = new Button(rec, "mBtnNext");
		mBtnCancel = new Button(rec, "mBtnCancel");

		rec.setWidth(this.width - (margin * 2));

		mEingabe = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "");

		mEingabe.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{

			}

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{
				textBox_TextChanged();
			}
		});

		mEingabe.setText("");

		// Layout! da es sich nicht ändert, brauchen wir es nicht in eine Methode packen
		float y = margin;

		mBtnFilter.setPos(margin, y);
		mBtnSearch.setPos(mBtnFilter.getMaxX() + margin, y);
		mBtnNext.setPos(mBtnSearch.getMaxX() + margin, y);
		mBtnCancel.setPos(mBtnNext.getMaxX() + margin, y);

		mEingabe.setPos(margin, mBtnCancel.getMaxY() + margin);

		y = mEingabe.getMaxY() + margin;

		mTglBtnOnline.setPos(margin, y);
		mTglBtnTitle.setPos(mTglBtnOnline.getMaxX() + margin + margin, y);
		mTglBtnGc.setPos(mTglBtnTitle.getMaxX() + margin, y);
		mTglBtnOwner.setPos(mTglBtnGc.getMaxX() + margin, y);

		// die Höhe nach dem Verbrauchten Platz einstellen

		this.setHeight(mTglBtnOwner.getMaxY() + margin);

		// Controls zum Dialog hinzufügen
		this.addChild(mTglBtnTitle);
		this.addChild(mTglBtnGc);
		this.addChild(mTglBtnOwner);
		this.addChild(mTglBtnOnline);
		this.addChild(mBtnFilter);
		this.addChild(mBtnSearch);
		this.addChild(mBtnNext);
		this.addChild(mBtnCancel);
		this.addChild(mEingabe);

		setLang();
		switchSearcheMode(0);

		mBtnCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// eventuell eingesetzten Search Filter zurück setzen
				clearSearchFilter();

				close();
				return true;
			}

		});

		mTglBtnTitle.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(0);
				return true;
			}
		});

		mTglBtnGc.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(1);
				return true;
			}
		});

		mTglBtnOwner.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switchSearcheMode(2);
				return true;
			}
		});

		mBtnSearch.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				mSearchAktive = false;
				mSearchIndex = -1;
				searchNow(false);
				return true;
			}
		});

		mBtnNext.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				searchNow(true);
				return true;

			}
		});

		mBtnFilter.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				closeSoftKeyPad();
				if (mTglBtnOnline.getState() == 1)
				{
					close();
					askPremium();
				}
				else
				{
					setFilter();
				}
				return true;
			}
		});

		mTglBtnOnline.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				setFilterBtnState();
				textBox_TextChanged();
				return true;
			}
		});

	}

	private void setFilterBtnState()
	{
		if (mTglBtnOnline.getState() == 0)
		{
			mBtnFilter.setImage(null);
			mBtnFilter.setText(GlobalCore.Translations.Get("Filter"));
		}
		else
		{

			mBtnFilter.setImage(new SpriteDrawable(SpriteCache.Icons.get(53)));
			mBtnFilter.setText("");
		}
	}

	/**
	 * Setzt die Übersetzten Texte auf die Buttons
	 */
	private void setLang()
	{
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnTitle, GlobalCore.Translations.Get("Title"),
				GlobalCore.Translations.Get("Title"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnGc, GlobalCore.Translations.Get("GCCode"),
				GlobalCore.Translations.Get("GCCode"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOwner, GlobalCore.Translations.Get("Owner"),
				GlobalCore.Translations.Get("Owner"));
		MultiToggleButton.initialOn_Off_ToggleStates(mTglBtnOnline, "Online", "Online");

		// der State muss erstmal gesetzt werden, damit die Anzeige
		// Aktuallisiert wird
		mTglBtnOnline.setState(0);

		mBtnFilter.setText(GlobalCore.Translations.Get("Filter"));
		mBtnSearch.setText(GlobalCore.Translations.Get("Search"));
		mBtnNext.setText(GlobalCore.Translations.Get("Next"));
		mBtnCancel.setText(GlobalCore.Translations.Get("abort"));

	}

	/**
	 * Schaltet den Such Modus um.
	 * 
	 * @param state
	 * <br/>
	 *            0 = Title <br/>
	 *            1 = Gc-Code <br/>
	 *            2 = Owner <br/>
	 */
	private void switchSearcheMode(int state)
	{
		mSearchState = state;

		if (state == 0)
		{
			mTglBtnTitle.setState(1);
			mTglBtnGc.setState(0);
			mTglBtnOwner.setState(0);
		}
		if (state == 1)
		{
			mTglBtnTitle.setState(0);
			mTglBtnGc.setState(1);
			mTglBtnOwner.setState(0);
		}
		if (state == 2)
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

		boolean isText = mEingabe.getText().length() != 0;
		mBtnSearch.setEnable(isText);
		mBtnNext.disable();

		if (mTglBtnOnline.getState() == 0)
		{
			mBtnFilter.setEnable(isText);
		}
		else
		{
			mBtnFilter.enable();
		}
		filterSearchByTextChnge();
	}

	/**
	 * schliesst die virtuelle Tastertur
	 */
	private void closeSoftKeyPad()
	{
		// close the virtual keyboard
		// InputMethodManager mgr = (InputMethodManager) mPtrMain.getSystemService(Context.INPUT_METHOD_SERVICE);
		// mgr.hideSoftInputFromWindow(mEingabe.getWindowToken(), 0);
	}

	/**
	 * Die aktive CahcheList wird durchsucht gefilterte Caches werden dabei nicht berücksichtigt.
	 * 
	 * @param ignoreOnlineSearch
	 *            (True, wenn Lokal gesucht werden soll, obwohl der MultiToggleButton "Online" aktiviert ist.
	 */
	private void searchNow(boolean ignoreOnlineSearch)
	{

		if (ignoreOnlineSearch || mTglBtnOnline.getState() == 0)
		{

			mSearchIndex++;

			// Replase LineBreaks

			String searchPattern = mEingabe.getText().toLowerCase();

			// Replase LineBreaks
			searchPattern = searchPattern.replace("\n", "");
			searchPattern = searchPattern.replace("\r", "");

			boolean criterionMatches = false;

			if (!mSearchAktive)
			{
				CacheListIterator = Database.Data.Query.iterator();
				mSearchAktive = true;
			}

			Cache tmp = null;
			while (CacheListIterator.hasNext() && !criterionMatches)
			{

				tmp = CacheListIterator.next();

				// criterionMatches = (mSearchState == 0 &&
				// tmp.Name.toLowerCase().contains(searchPattern))
				// || (mSearchState == 1 &&
				// tmp.GcCode.toLowerCase().contains(searchPattern))
				// || (mSearchState == 2 &&
				// tmp.Owner.toLowerCase().contains(searchPattern))
				// || (mSearchState == 2 &&
				// tmp.PlacedBy.toLowerCase().contains(searchPattern));

				switch (mSearchState)
				{
				case 0:
					criterionMatches = tmp.Name.toLowerCase().contains(searchPattern);
					break;
				case 1:
					criterionMatches = tmp.GcCode.toLowerCase().contains(searchPattern);
					break;
				case 2:
					criterionMatches = tmp.Owner.toLowerCase().contains(searchPattern)
							|| tmp.PlacedBy.toLowerCase().contains(searchPattern);
					break;
				}

				if (!criterionMatches) mSearchIndex++;
			}

			if (!criterionMatches)
			{
				mBtnNext.disable();
				mSearchAktive = false;
				GL_MsgBox.Show(GlobalCore.Translations.Get("NoCacheFound"), GlobalCore.Translations.Get("search"), MessageBoxButtons.OK,
						MessageBoxIcon.Asterisk, null);
			}
			else
			{

				Waypoint finalWp = null;
				if (tmp.HasFinalWaypoint()) finalWp = tmp.GetFinalWaypoint();
				if (tmp != null) GlobalCore.SelectedWaypoint(tmp, finalWp);
				// deactivate autoResort when Cache is selected by hand
				GlobalCore.autoResort = false;

				mBtnNext.enable();

			}
		}
		else
		{
			searchAPI();
		}

	}

	/**
	 * setzt bei Eingabe eines Zeichens die CacheListItems auf Sichtbar oder unsichtbar
	 */
	private void filterSearchByTextChnge()
	{
		if (!Config.settings.dynamicFilterAtSearch.getValue()) return;
		if (CacheListView.that == null) return;
		if (mTglBtnOnline.getState() == 1)
		{
			// nicht bei Online Suche
			clearSearchFilter();
			return;
		}

		String searchPattern = mEingabe.getText().toLowerCase();

		// Replase LineBreaks
		searchPattern = searchPattern.replace("\n", "");
		searchPattern = searchPattern.replace("\r", "");

		for (Cache cache : Database.Data.Query)
		{
			boolean set = true;
			switch (mSearchState)
			{
			case 0:
				set = cache.Name.toLowerCase().contains(searchPattern);
				break;
			case 1:
				set = cache.GcCode.toLowerCase().contains(searchPattern);
				break;
			case 2:
				set = cache.Owner.toLowerCase().contains(searchPattern) || cache.PlacedBy.toLowerCase().contains(searchPattern);
				break;
			}

			cache.setSearchVisible(set);
		}

		CacheListView.that.getListView().setHasInvisibleItems(true);
		CacheListView.that.CacheListChangedEvent();
	}

	private void clearSearchFilter()
	{
		if (!Config.settings.dynamicFilterAtSearch.getValue()) return;
		for (Cache cache : Database.Data.Query)
		{

			cache.setSearchVisible(true);

		}

		if (CacheListView.that != null)
		{
			CacheListView.that.getListView().setHasInvisibleItems(false);
			CacheListView.that.CacheListChangedEvent();
		}
	}

	/**
	 * Sucht mit den Vorgaben nach Caches über die API. Die Gefundenen Caches werden in die DB eingetragen und im Anschluss wird der lokale
	 * Suchvorgang gestartet.
	 */
	private void searchAPI()
	{
		// TODO searchOnline();
	}

	/**
	 * setzt den Filter auf die Such Anfrage
	 */
	private void setFilter()
	{
		String searchPattern = mEingabe.getText().toLowerCase();

		String where = "";
		if (GlobalCore.LastFilter.toString().length() > 0) where = " AND (";

		GlobalCore.LastFilter.filterName = "";
		GlobalCore.LastFilter.filterGcCode = "";
		GlobalCore.LastFilter.filterOwner = "";

		if (mSearchState == 0) GlobalCore.LastFilter.filterName = searchPattern;
		else if (mSearchState == 1) GlobalCore.LastFilter.filterGcCode = searchPattern;
		if (mSearchState == 2) GlobalCore.LastFilter.filterOwner = searchPattern;

		ApplyFilter();
	}

	private FilterProperties props;

	public void ApplyFilter()
	{
		ApplyFilter(GlobalCore.LastFilter);
	}

	public void ApplyFilter(FilterProperties filter)
	{
		EditFilterSettings.ApplyFilter(filter);
	}

	public enum searchMode
	{
		Titel, GcCode, Owner
	}

	public void addSearch(String searchPattern, searchMode Mode)
	{

		Logger.DEBUG("addSearch " + searchPattern);

		// TODO weiß nicht mehr was ich damit bezwecke=> show(searchPattern, Mode.ordinal());

		// auf Online schalten
		mTglBtnOnline.setState(1);
		setFilterBtnState();

		// Suche auslösen
		mBtnSearch.performClick();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void onShow()
	{
		if (GlobalCore.isTab)
		{
			// TODO searchDialog plaziere rechts neben der Cache List
		}
		else
		{
			if (CacheListView.that != null)
			{
				setY(CacheListView.that.getMaxY() - this.height);
				CacheListView.that.setTopPlaceHolder(this.height);
			}
		}
		if (!GL.that.PopUpIsShown()) that.showNotCloseAutomaticly();

		Slider.that.registerPosChangedEvent(listner);
	}

	@Override
	public void onHide()
	{
		Slider.that.removePosChangedEvent(listner);

		if (!GlobalCore.isTab)
		{
			if (CacheListView.that != null)
			{
				CacheListView.that.resetPlaceHolder();
			}
		}
	}

	private YPositionChanged listner = new YPositionChanged()
	{

		@Override
		public void Position(float SliderTop, float SliderBottom)
		{
			if (GlobalCore.isTab)
			{
				// TODO plaziere rechts neben der Cache List
			}
			else
			{
				setY(CacheListView.that.getMaxY() - that.height);
			}

		}
	};

	private void askPremium()
	{

		if ("".equals(Config.GetAccessToken()))
		{
			GL_MsgBox.Show(GlobalCore.Translations.Get("apiKeyNeeded"), GlobalCore.Translations.Get("Clue"), MessageBoxButtons.OK,
					MessageBoxIcon.Exclamation, null);
		}
		else
		{

			CancelWaitDialog.ShowWait(GlobalCore.Translations.Get("chkApiState"), new IcancelListner()
			{

				@Override
				public void isCanceld()
				{
					// TODO Auto-generated method stub

				}
			}, new Runnable()
			{

				@Override
				public void run()
				{
					int ret = GroundspeakAPI.GetMembershipType(Config.GetAccessToken());
					if (ret == 3)
					{
						// searchOnlineNow();
						showTargetApiDialog();
					}
					else
					{
						GL_MsgBox.Show(GlobalCore.Translations.Get("GC_basic"), GlobalCore.Translations.Get("GC_title"),
								MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, new OnMsgBoxClickListener()
								{

									@Override
									public boolean onClick(int which)
									{
										if (which == GL_MsgBox.BUTTON_POSITIVE) showTargetApiDialog();
										return true;
									}
								});
					}
				}
			});

		}

	}

	private void showTargetApiDialog()
	{
		new SearchOverPosition().show();
	}

}
