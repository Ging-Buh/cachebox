package CB_Core.GL_UI.Views;

import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.TemplateFormatter;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.LogTypes;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Activitys.EditFieldNotes;
import CB_Core.GL_UI.Activitys.EditFieldNotes.ReturnListner;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.PopUp_Base;
import CB_Core.GL_UI.Controls.PopUps.QuickFieldNoteFeedbackPopUp;
import CB_Core.GL_UI.Main.Actions.CB_Action_UploadFieldNote;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;
import CB_Core.Types.Waypoint;

public class FieldNotesView extends V_ListView
{
	private static FieldNoteList lFieldNotes;

	public static FieldNotesView that;
	public static FieldNoteEntry aktFieldNote;
	public static CB_RectF ItemRec;
	public static boolean firstShow = true;

	private CustomAdapter lvAdapter;

	public FieldNotesView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		ItemRec = new CB_RectF(0, 0, this.width, UI_Size_Base.that.getButtonHeight() * 1.1f);

		setBackground(SpriteCache.ListBack);

		lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("");
		this.setHasInvisibleItems(true);
		this.setBaseAdapter(null);
		lvAdapter = new CustomAdapter(lFieldNotes);
		this.setBaseAdapter(lvAdapter);

		this.setEmptyMsg(Translation.Get("EmptyFieldNotes"));
		firstShow = true;
	}

	@Override
	public void onShow()
	{
		lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("");
		this.notifyDataSetChanged();
		if (firstShow)
		{
			firstShow = false;
			getContextMenu().Show();
		}

	}

	@Override
	public void onHide()
	{
		firstShow = true;
	}

	@Override
	public void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public class CustomAdapter implements Adapter
	{

		private FieldNoteList fieldNoteList;

		public CustomAdapter(FieldNoteList fieldNoteList)
		{
			this.fieldNoteList = fieldNoteList;
		}

		public int getCount()
		{
			return fieldNoteList.size();
		}

		public Object getItem(int position)
		{
			return fieldNoteList.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			FieldNoteEntry fne = fieldNoteList.get(position);

			CB_RectF rec = ItemRec.copy().ScaleCenter(0.97f);
			rec.setHeight(MeasureItemHeight(fne));
			FieldNoteViewItem v = new FieldNoteViewItem(rec, position, fne);

			v.setOnLongClickListener(itemLogClickListner);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			if (position > fieldNoteList.size() || fieldNoteList.size() == 0) return 0;
			FieldNoteEntry fne = fieldNoteList.get(position);
			return MeasureItemHeight(fne);
		}

		private float MeasureItemHeight(FieldNoteEntry fne)
		{
			float headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + (Dialog.getMargin());
			float cacheIfoHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + Dialog.getMargin() + Fonts.Measure("T").height;
			float mesurdWidth = ItemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic()
					- ListViewItemBackground.getRightWidthStatic() - (Dialog.getMargin() * 2);

			float mh = 0;
			try
			{
				if (fne.comment != null && !(fne.comment.length() == 0))
				{
					mh = Fonts.MeasureWrapped(fne.comment, mesurdWidth).height;
				}
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			float commentHeight = (Dialog.getMargin() * 3) + mh;

			return headHeight + cacheIfoHeight + commentHeight;
		}
	}

	public Menu getContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_FOUND:
					addNewFieldnote(LogTypes.found);
					return true;
				case MenuID.MI_NOT_FOUND:
					addNewFieldnote(LogTypes.didnt_find);
					return true;
				case MenuID.MI_MAINTANCE:
					addNewFieldnote(LogTypes.needs_maintenance);
					return true;
				case MenuID.MI_NOTE:
					addNewFieldnote(LogTypes.note);
					return true;

				case MenuID.MI_UPLOAD_FIELDNOTE:
					CB_Action_UploadFieldNote.INSTANCE.Execute();
					return true;
				case MenuID.MI_DELETE_ALL_FIELDNOTES:
					deleteAllFieldNote();
					return true;
				}
				return false;
			}
		});

		Cache cache = GlobalCore.getSelectedCache();

		cm.addItem(MenuID.MI_FOUND, "found", SpriteCache.getThemedSprite("log0icon"));
		cm.addItem(MenuID.MI_NOT_FOUND, "DNF", SpriteCache.getThemedSprite("log1icon"));

		// Aktueller Cache ist ist von geocaching.com dann weitere Menüeinträge freigeben
		if (cache != null && cache.GcCode.toLowerCase().startsWith("gc"))
		{
			cm.addItem(MenuID.MI_MAINTANCE, "maintenance", SpriteCache.getThemedSprite("log5icon"));
			cm.addItem(MenuID.MI_NOTE, "writenote", SpriteCache.getThemedSprite("log2icon"));
		}

		cm.addItem(MenuID.MI_UPLOAD_FIELDNOTE, "uploadFieldNotes", SpriteCache.Icons.get(IconName.uploadFieldNote_64.ordinal()));
		cm.addItem(MenuID.MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", SpriteCache.getThemedSprite("delete"));

		return cm;

	}

	public static void addNewFieldnote(LogTypes type)
	{
		addNewFieldnote(type, false);
	}

	public static void addNewFieldnote(LogTypes type, boolean witoutShowEdit)
	{
		Cache cache = GlobalCore.getSelectedCache();

		if (cache == null)
		{
			GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error,
					null);
			return;
		}

		// chk car found?
		if (cache.GcCode.equalsIgnoreCase("CBPark"))
		{

			if (type == LogTypes.found)
			{
				GL_MsgBox.Show(Translation.Get("My_Parking_Area_Found"), Translation.Get("thisNotWork"), MessageBoxButtons.OK,
						MessageBoxIcon.Information, null);
			}
			else if (type == LogTypes.didnt_find)
			{
				GL_MsgBox.Show(Translation.Get("My_Parking_Area_DNF"), Translation.Get("thisNotWork"), MessageBoxButtons.OK,
						MessageBoxIcon.Error, null);
			}

			return;
		}

		// GC fremder Cache gefunden?
		if (!cache.GcCode.toLowerCase().startsWith("gc"))
		{

			if (type == LogTypes.found)
			{
				// Found it! -> fremden Cache als gefunden markieren
				if (!GlobalCore.getSelectedCache().Found)
				{
					GlobalCore.getSelectedCache().Found = true;
					CacheDAO cacheDAO = new CacheDAO();
					cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
					QuickFieldNoteFeedbackPopUp pop = new QuickFieldNoteFeedbackPopUp(true);
					pop.show(PopUp_Base.SHOW_TIME_SHORT);
					platformConector.vibrate();
				}
			}
			else if (type == LogTypes.didnt_find)
			{
				// DidNotFound -> fremden Cache als nicht gefunden markieren
				if (GlobalCore.getSelectedCache().Found)
				{
					GlobalCore.getSelectedCache().Found = false;
					CacheDAO cacheDAO = new CacheDAO();
					cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
					QuickFieldNoteFeedbackPopUp pop2 = new QuickFieldNoteFeedbackPopUp(false);
					pop2.show(PopUp_Base.SHOW_TIME_SHORT);
					platformConector.vibrate();
				}
			}

			if (that != null) that.notifyDataSetChanged();
			return;
		}

		FieldNoteEntry newFieldNote = null;
		if ((type == LogTypes.found) || (type == LogTypes.didnt_find))
		{
			// nachsehen, ob für diesen Cache bereits eine FieldNote des Types
			// angelegt wurde
			// und gegebenenfalls diese ändern und keine neue anlegen
			// gilt nur für Found It! und DNF.
			// needMaintance oder Note können zusätzlich angelegt werden

			if (lFieldNotes == null)
			{
				lFieldNotes = new FieldNoteList();

			}

			lFieldNotes.LoadFieldNotes("");

			for (FieldNoteEntry nfne : lFieldNotes)
			{
				if ((nfne.CacheId == cache.Id) && (nfne.type == type))
				{
					newFieldNote = nfne;
					newFieldNote.DeleteFromDatabase();
					newFieldNote.timestamp = new Date();
					aktFieldNote = newFieldNote;
				}
			}
		}

		if (newFieldNote == null)
		{
			newFieldNote = new FieldNoteEntry(type);
			newFieldNote.CacheName = cache.Name;
			newFieldNote.gcCode = cache.GcCode;
			newFieldNote.foundNumber = Config.settings.FoundOffset.getValue();
			newFieldNote.timestamp = new Date();
			newFieldNote.CacheId = cache.Id;
			newFieldNote.comment = "";
			newFieldNote.CacheUrl = cache.Url;
			newFieldNote.cacheType = cache.Type.ordinal();
			newFieldNote.fillType();
			// aktFieldNoteIndex = -1;
			aktFieldNote = newFieldNote;
		}
		else
		{
			lFieldNotes.remove(newFieldNote);

		}

		switch (type)
		{
		case found:
			if (!cache.Found) newFieldNote.foundNumber++; //
			newFieldNote.fillType();
			if (newFieldNote.comment.equals("")) newFieldNote.comment = TemplateFormatter.ReplaceTemplate(
					Config.settings.FoundTemplate.getValue(), newFieldNote);
			// wenn eine FieldNote Found erzeugt werden soll und der Cache noch
			// nicht gefunden war -> foundNumber um 1 erhöhen
			break;
		case didnt_find:
			if (newFieldNote.comment.equals("")) newFieldNote.comment = TemplateFormatter.ReplaceTemplate(
					Config.settings.DNFTemplate.getValue(), newFieldNote);
			break;
		case needs_maintenance:
			if (newFieldNote.comment.equals("")) newFieldNote.comment = TemplateFormatter.ReplaceTemplate(
					Config.settings.NeedsMaintenanceTemplate.getValue(), newFieldNote);
			break;
		case note:
			if (newFieldNote.comment.equals("")) newFieldNote.comment = TemplateFormatter.ReplaceTemplate(
					Config.settings.AddNoteTemplate.getValue(), newFieldNote);
			break;
		default:
			break;
		}

		if (!witoutShowEdit)
		{
			efnActivity = new EditFieldNotes(newFieldNote, returnListner, true);
			efnActivity.show();
		}
		else
		{

			// neue FieldNote
			lFieldNotes.add(0, newFieldNote);
			newFieldNote.WriteToDatabase();
			aktFieldNote = newFieldNote;
			if (newFieldNote.type == LogTypes.found)
			{
				// Found it! -> Cache als gefunden markieren
				if (!GlobalCore.getSelectedCache().Found)
				{
					GlobalCore.getSelectedCache().Found = true;
					CacheDAO cacheDAO = new CacheDAO();
					cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
					Config.settings.FoundOffset.setValue(aktFieldNote.foundNumber);
					Config.AcceptChanges();
				}
				// und eine evtl. vorhandene FieldNote DNF löschen
				lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.didnt_find);
			}
			else if (newFieldNote.type == LogTypes.didnt_find)
			{
				// DidNotFound -> Cache als nicht gefunden markieren
				if (GlobalCore.getSelectedCache().Found)
				{
					GlobalCore.getSelectedCache().Found = false;
					CacheDAO cacheDAO = new CacheDAO();
					cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
					Config.settings.FoundOffset.setValue(Config.settings.FoundOffset.getValue() - 1);
					Config.AcceptChanges();
				}
				// und eine evtl. vorhandene FieldNote FoundIt löschen
				lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.found);
			}

			FieldNoteList.CreateVisitsTxt();

			if (that != null) that.notifyDataSetChanged();

		}
	}

	private static EditFieldNotes.ReturnListner returnListner = new ReturnListner()
	{

		@Override
		public void returnedFieldNote(FieldNoteEntry fieldNote, boolean isNewFieldNote)
		{

			FieldNotesView.firstShow = false;

			efnActivity.dispose();
			efnActivity = null;

			if (fieldNote != null)
			{

				if (isNewFieldNote)
				{
					// nur, wenn eine FieldNote neu angelegt wurde
					// neue FieldNote
					lFieldNotes.add(0, fieldNote);

					// eine evtl. vorhandene FieldNote /DNF löschen
					if (fieldNote.type == LogTypes.found || fieldNote.type == LogTypes.didnt_find)
					{
						lFieldNotes.DeleteFieldNoteByCacheId(fieldNote.CacheId, LogTypes.found);
						lFieldNotes.DeleteFieldNoteByCacheId(fieldNote.CacheId, LogTypes.didnt_find);
					}
				}

				fieldNote.WriteToDatabase();
				aktFieldNote = fieldNote;

				if (isNewFieldNote)
				{
					// nur, wenn eine FieldNote neu angelegt wurde
					// wenn eine FieldNote neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
					// SelectedCache eine fieldNote angelegt wird
					if (fieldNote.type == LogTypes.found)
					{ // Found it! -> Cache als gefunden markieren
						if (!GlobalCore.getSelectedCache().Found)
						{
							GlobalCore.getSelectedCache().Found = true;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
							Config.settings.FoundOffset.setValue(aktFieldNote.foundNumber);
							Config.AcceptChanges();
						}

					}
					else if (fieldNote.type == LogTypes.didnt_find)
					{ // DidNotFound -> Cache als nicht gefunden markieren
						if (GlobalCore.getSelectedCache().Found)
						{
							GlobalCore.getSelectedCache().Found = false;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
							Config.settings.FoundOffset.setValue(Config.settings.FoundOffset.getValue() - 1);
							Config.AcceptChanges();
						} // und eine evtl. vorhandene FieldNote FoundIt löschen
						lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.found);
					}
				}
				FieldNoteList.CreateVisitsTxt();

			}
			that.notifyDataSetChanged();
		}
	};

	static EditFieldNotes efnActivity;

	private void editFieldNote()
	{
		efnActivity = new EditFieldNotes(aktFieldNote, returnListner, false);
		efnActivity.show();
	}

	private void deleteFieldNote()
	{
		// aktuell selectierte FieldNote löschen
		if (aktFieldNote == null) return;
		// final Cache cache =
		// Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);

		Cache tmpCache = null;
		// suche den Cache aus der DB.
		// Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
		CacheList lCaches = new CacheList();
		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId);
		if (lCaches.size() > 0) tmpCache = lCaches.get(0);
		final Cache cache = tmpCache;

		if (cache == null && !aktFieldNote.isTbFieldNote)
		{
			String message = Translation.Get("cacheOtherDb", aktFieldNote.CacheName);
			message += "\n" + Translation.Get("fieldNoteNoDelete");
			GL_MsgBox.Show(message);
			return;
		}

		GL_MsgBox.OnMsgBoxClickListener dialogClickListener = new GL_MsgBox.OnMsgBoxClickListener()
		{
			@Override
			public boolean onClick(int which, Object data)
			{
				switch (which)
				{
				case GL_MsgBox.BUTTON_POSITIVE:
					// Yes button clicked
					// delete aktFieldNote
					if (cache != null)
					{
						if (cache.Found)
						{
							cache.Found = false;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(cache);
							Config.settings.FoundOffset.setValue(Config.settings.FoundOffset.getValue() - 1);
							Config.AcceptChanges();
							// jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
							// damit das Smiley Symbol aus der Map und der CacheList verschwindet
							synchronized (Database.Data.Query)
							{
								Cache tc = Database.Data.Query.GetCacheById(cache.Id);
								if (tc != null)
								{
									tc.Found = false;
								}
							}
						}
					}
					lFieldNotes.DeleteFieldNote(aktFieldNote.Id, aktFieldNote.type);

					aktFieldNote = null;

					lFieldNotes = new FieldNoteList();
					lFieldNotes.LoadFieldNotes("");

					that.setBaseAdapter(null);
					lvAdapter = new CustomAdapter(lFieldNotes);
					that.setBaseAdapter(lvAdapter);

					break;
				case GL_MsgBox.BUTTON_NEGATIVE:
					// No button clicked
					// do nothing
					break;
				}
				return true;
			}

		};

		String message = "";
		if (aktFieldNote.isTbFieldNote)
		{
			message = Translation.Get("confirmFieldnoteDeletionTB", aktFieldNote.typeString, aktFieldNote.TbName);
		}
		else
		{
			message = Translation.Get("confirmFieldnoteDeletion", aktFieldNote.typeString, aktFieldNote.CacheName);
			if (aktFieldNote.type == LogTypes.found) message += Translation.Get("confirmFieldnoteDeletionRst");
		}

		GL_MsgBox.Show(message, Translation.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);

	}

	private void deleteAllFieldNote()
	{
		GL_MsgBox.OnMsgBoxClickListener dialogClickListener = new GL_MsgBox.OnMsgBoxClickListener()
		{
			@Override
			public boolean onClick(int which, Object data)
			{
				switch (which)
				{
				case GL_MsgBox.BUTTON_POSITIVE:
					// Yes button clicked
					// delete aktFieldNote
					for (FieldNoteEntry entry : lFieldNotes)
					{
						entry.DeleteFromDatabase();

					}

					lFieldNotes.clear();
					aktFieldNote = null;

					that.setBaseAdapter(null);
					lvAdapter = new CustomAdapter(lFieldNotes);
					that.setBaseAdapter(lvAdapter);
					break;

				case GL_MsgBox.BUTTON_NEGATIVE:
					// No button clicked
					// do nothing
					break;
				}
				return true;

			}
		};
		String message = Translation.Get("DeleteAllFieldNotesQuestion");
		GL_MsgBox.Show(message, Translation.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener);

	}

	private void selectCacheFromFieldNote()
	{
		if (aktFieldNote == null) return;

		// suche den Cache aus der DB.
		// Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
		CacheList lCaches = new CacheList();
		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId);
		Cache tmpCache = null;
		if (lCaches.size() > 0) tmpCache = lCaches.get(0);
		Cache cache = tmpCache;

		if (cache == null)
		{
			String message = Translation.Get("cacheOtherDb", aktFieldNote.CacheName);
			message += "\n" + Translation.Get("fieldNoteNoSelect");
			GL_MsgBox.Show(message);
			return;
		}

		synchronized (Database.Data.Query)
		{
			cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
		}

		if (cache == null)
		{
			Database.Data.Query.add(tmpCache);
			cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
		}

		Waypoint finalWp = null;
		if (cache != null)
		{
			if (cache.HasFinalWaypoint()) finalWp = cache.GetFinalWaypoint();
			else if (cache.HasStartWaypoint()) finalWp = cache.GetStartWaypoint();
			GlobalCore.setSelectedWaypoint(cache, finalWp);
		}
	}

	private OnClickListener itemLogClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			int index = ((ListViewItemBase) v).getIndex();

			aktFieldNote = lFieldNotes.get(index);

			Menu cm = new Menu("CacheListContextMenu");

			cm.addItemClickListner(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					switch (((MenuItem) v).getMenuItemId())
					{
					case MenuID.MI_SELECT_CACHE:
						selectCacheFromFieldNote();
						return true;
					case MenuID.MI_EDIT_FIELDNOTE:
						editFieldNote();
						return true;
					case MenuID.MI_DELETE_FIELDNOTE:
						deleteFieldNote();
						return true;

					}
					return false;
				}
			});

			cm.addItem(MenuID.MI_SELECT_CACHE, "SelectCache");
			cm.addItem(MenuID.MI_EDIT_FIELDNOTE, "edit");
			cm.addItem(MenuID.MI_DELETE_FIELDNOTE, "delete");

			cm.Show();
			return true;
		}
	};

	@Override
	public void notifyDataSetChanged()
	{
		lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("");

		that.setBaseAdapter(null);
		lvAdapter = new CustomAdapter(lFieldNotes);
		that.setBaseAdapter(lvAdapter);

		super.notifyDataSetChanged();
	}

}
