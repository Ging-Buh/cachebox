package CB_Core.GL_UI.Views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GCVote.GCVote;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.EditFieldNotes;
import CB_Core.GL_UI.Activitys.EditFieldNotes.ReturnListner;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.interfaces.RunnableReadyHandler;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;
import CB_Core.Types.Waypoint;

public class FieldNotesView extends V_ListView
{
	public static FieldNotesView that;
	public static FieldNoteEntry aktFieldNote;
	// private static int aktFieldNoteIndex = -1;
	static FieldNoteList lFieldNotes;
	CustomAdapter lvAdapter;
	public static CB_RectF ItemRec;

	public static boolean firstShow = true;

	public FieldNotesView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		ItemRec = new CB_RectF(0, 0, this.width, UiSizes.getButtonHeight() * 1.1f);

		setBackground(SpriteCache.ListBack);

		lFieldNotes = new FieldNoteList();
		lFieldNotes.LoadFieldNotes("");
		this.setHasInvisibleItems(true);
		this.setBaseAdapter(null);
		lvAdapter = new CustomAdapter(lFieldNotes);
		this.setBaseAdapter(lvAdapter);

		this.setEmptyMsg(GlobalCore.Translations.Get("EmptyFieldNotes"));
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
			getContextMenu().show();
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
			float headHeight = (UiSizes.getButtonHeight() / 1.5f) + (Dialog.margin);
			float cacheIfoHeight = (UiSizes.getButtonHeight() / 1.5f) + Dialog.margin + Fonts.Measure("T").height;
			float mesurdWidth = ItemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic()
					- ListViewItemBackground.getRightWidthStatic() - (Dialog.margin * 2);

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

			float commentHeight = (Dialog.margin * 3) + mh;

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
					addNewFieldnote(1);
					return true;
				case MenuID.MI_NOT_FOUND:
					addNewFieldnote(2);
					return true;
				case MenuID.MI_MAINTANCE:
					addNewFieldnote(3);
					return true;
				case MenuID.MI_NOTE:
					addNewFieldnote(4);
					return true;

				case MenuID.MI_MANAGE:
					manageFieldNote();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_FOUND, "found", SpriteCache.getThemedSprite("log0icon"));
		cm.addItem(MenuID.MI_NOT_FOUND, "DNF", SpriteCache.getThemedSprite("log1icon"));
		cm.addItem(MenuID.MI_MAINTANCE, "maintenance", SpriteCache.getThemedSprite("log5icon"));
		cm.addItem(MenuID.MI_NOTE, "writenote", SpriteCache.getThemedSprite("log2icon"));
		cm.addItem(MenuID.MI_MANAGE, "ManageNotes");

		return cm;

	}

	private void manageFieldNote()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_UPLOAD_FIELDNOTE:
					UploadFieldnotes();
					return true;
				case MenuID.MI_DELETE_ALL_FIELDNOTES:
					deleteAllFieldNote();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_UPLOAD_FIELDNOTE, "uploadFieldNotes", SpriteCache.Icons.get(35));
		cm.addItem(MenuID.MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", SpriteCache.getThemedSprite("delete"));

		cm.show();

	}

	private void UploadFieldnotes()
	{

		GL_MsgBox.Show(GlobalCore.Translations.Get("uploadFieldNotes?"), GlobalCore.Translations.Get("uploadFieldNotes"),
				MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, UploadFieldnotesDialogListner);

	}

	private final GL_MsgBox.OnMsgBoxClickListener UploadFieldnotesDialogListner = new GL_MsgBox.OnMsgBoxClickListener()
	{
		@Override
		public boolean onClick(int which)
		{
			switch (which)
			{
			case GL_MsgBox.BUTTON_POSITIVE:
				UploadFieldNotes();

				return true;
			}
			return false;
		}

	};

	private ProgressDialog PD;

	private void UploadFieldNotes()
	{
		ThreadCancel = false;
		final RunnableReadyHandler UploadFieldNotesdThread = new RunnableReadyHandler(new Runnable()
		{

			@Override
			public void run()
			{
				ProgresssChangedEventList.Call("Upload", "", 0);

				String accessToken = Config.GetAccessToken();

				int count = 0;
				int anzahl = 0;
				for (FieldNoteEntry fieldNote : lFieldNotes)
				{
					if (!fieldNote.uploaded) anzahl++;
				}

				boolean sendGCVote = !Config.settings.GcVotePassword.getEncryptedValue().equalsIgnoreCase("");

				if (anzahl > 0)
				{
					UploadMeldung = "";
					for (FieldNoteEntry fieldNote : lFieldNotes)
					{
						if (fieldNote.uploaded) continue;
						if (ThreadCancel) // wenn im ProgressDialog Cancel gedrückt
											// wurde.
						break;
						// Progress status Melden
						ProgresssChangedEventList.Call(fieldNote.CacheName, (100 * count) / anzahl);

						if (sendGCVote)
						{
							sendCacheVote(fieldNote);
						}

						if (CB_Core.Api.GroundspeakAPI.CreateFieldNoteAndPublish(accessToken, fieldNote.gcCode, fieldNote.getGcUploadId(),
								fieldNote.timestamp, fieldNote.comment) != 0)
						{
							UploadMeldung += fieldNote.gcCode + "\n" + CB_Core.Api.GroundspeakAPI.LastAPIError + "\n";
						}
						else
						{
							fieldNote.uploaded = true;
							fieldNote.UpdateDatabase();
						}
						count++;
					}
				}

				PD.close();

			}
		})
		{

			@Override
			public void RunnableReady(boolean canceld)
			{
				if (!UploadMeldung.equals(""))
				{
					GL_MsgBox.Show(UploadMeldung, GlobalCore.Translations.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
				}
				else
				{
					GL_MsgBox.Show(GlobalCore.Translations.Get("uploadFinished"), GlobalCore.Translations.Get("uploadFieldNotes"),
							MessageBoxIcon.GC_Live);
				}
			}
		};

		// ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.
		PD = ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread);

	}

	void sendCacheVote(FieldNoteEntry fieldNote)
	{

		// Stimme abgeben
		try
		{
			if (!GCVote.SendVotes(Config.settings.GcLogin.getValue(), Config.settings.GcVotePassword.getValue(), fieldNote.gc_Vote,
					fieldNote.CacheUrl, fieldNote.gcCode))
			{
				UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
			}
		}
		catch (Exception e)
		{
			UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
		}
	}

	private Boolean ThreadCancel = false;

	private String UploadMeldung = "";

	public static void addNewFieldnote(int type)
	{
		addNewFieldnote(type, false);
	}

	public static void addNewFieldnote(int type, boolean witoutShowEdit)
	{
		Cache cache = GlobalCore.getSelectedCache();

		if (cache == null)
		{
			GL_MsgBox.Show(GlobalCore.Translations.Get("NoCacheSelect"), GlobalCore.Translations.Get("thisNotWork"), MessageBoxButtons.OK,
					MessageBoxIcon.Error, null);
			return;
		}

		// chk car found?
		if (cache.GcCode.equalsIgnoreCase("CBPark"))
		{

			if (type == 1)
			{
				GL_MsgBox.Show(GlobalCore.Translations.Get("My_Parking_Area_Found"), GlobalCore.Translations.Get("thisNotWork"),
						MessageBoxButtons.OK, MessageBoxIcon.Information, null);
			}
			else if (type == 2)
			{
				GL_MsgBox.Show(GlobalCore.Translations.Get("My_Parking_Area_DNF"), GlobalCore.Translations.Get("thisNotWork"),
						MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			}

			return;
		}

		FieldNoteEntry newFieldNote = null;
		if ((type == 1) || (type == 2))
		{
			// nachsehen, ob für diesen Cache bereits eine FieldNote des Types
			// angelegt wurde
			// und gegebenenfalls diese ändern und keine neue anlegen
			// gilt nur für Found It! und DNF.
			// needMaintance oder Note können zusätzlich angelegt werden
			int index = 0;

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
					// aktFieldNoteIndex = index;
				}
				index++;
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
		case 1:
			if (!cache.Found) newFieldNote.foundNumber++; //
			newFieldNote.fillType();
			if (newFieldNote.comment.equals("")) newFieldNote.comment = ReplaceTemplate(Config.settings.FoundTemplate.getValue(),
					newFieldNote);
			// wenn eine FieldNote Found erzeugt werden soll und der Cache noch
			// nicht gefunden war -> foundNumber um 1 erhöhen
			break;
		case 2:
			if (newFieldNote.comment.equals("")) newFieldNote.comment = ReplaceTemplate(Config.settings.DNFTemplate.getValue(),
					newFieldNote);
			break;
		case 3:
			if (newFieldNote.comment.equals("")) newFieldNote.comment = ReplaceTemplate(
					Config.settings.NeedsMaintenanceTemplate.getValue(), newFieldNote);
			break;
		case 4:
			if (newFieldNote.comment.equals("")) newFieldNote.comment = ReplaceTemplate(Config.settings.AddNoteTemplate.getValue(),
					newFieldNote);
			break;
		}

		if (!witoutShowEdit)
		{
			efnActivity = new EditFieldNotes(newFieldNote, returnListner);
			efnActivity.show();
		}
		else
		{
			// newFieldNote.WriteToDatabase();
			//
			// if (newFieldNote.type == 1)
			// {
			// // Found it! -> Cache als gefunden markieren
			// if (!GlobalCore.getSelectedCache().Found)
			// {
			// GlobalCore.getSelectedCache().Found = true;
			// CacheDAO cacheDAO = new CacheDAO();
			// cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
			// Config.settings.FoundOffset.setValue(aktFieldNote.foundNumber);
			// Config.AcceptChanges();
			// }
			// }
			// else if (newFieldNote.type == 2)
			// {
			// // DidNotFound -> Cache als nicht gefunden markieren
			// if (GlobalCore.getSelectedCache().Found)
			// {
			// GlobalCore.getSelectedCache().Found = false;
			// CacheDAO cacheDAO = new CacheDAO();
			// cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
			// Config.settings.FoundOffset.setValue(Config.settings.FoundOffset.getValue() - 1);
			// Config.AcceptChanges();
			// }
			// // und eine evtl. vorhandene FieldNote FoundIt löschen
			// lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, 1);
			// }
			//
			// FieldNoteList.CreateVisitsTxt();

			// neue FieldNote
			lFieldNotes.add(0, newFieldNote);
			newFieldNote.WriteToDatabase();
			aktFieldNote = newFieldNote;
			if (newFieldNote.type == 1)
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
				lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, 2);
			}
			else if (newFieldNote.type == 2)
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
				lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, 1);
			}

			FieldNoteList.CreateVisitsTxt();

			if (that != null) that.notifyDataSetChanged();

		}
	}

	private static EditFieldNotes.ReturnListner returnListner = new ReturnListner()
	{

		@Override
		public void returnedFieldNote(FieldNoteEntry fieldNote)
		{

			FieldNotesView.firstShow = false;

			efnActivity.dispose();
			efnActivity = null;

			if (fieldNote != null)
			{

				// neue FieldNote
				lFieldNotes.add(0, fieldNote);
				fieldNote.WriteToDatabase();
				aktFieldNote = fieldNote;
				if (fieldNote.type == 1)
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
					// und eine evtl. vorhandene FieldNote FoundIt löschen
					lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, 2);
				}
				else if (fieldNote.type == 2)
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
					lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, 1);
				}

				FieldNoteList.CreateVisitsTxt();

			}
			that.notifyDataSetChanged();
		}
	};

	private static String ReplaceTemplate(String template, FieldNoteEntry fieldNote)
	{
		DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
		String stime = iso8601Format.format(fieldNote.timestamp);
		iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
		String sdate = iso8601Format.format(fieldNote.timestamp);

		template = template.replace("<br>", "\n");
		template = template.replace("##finds##", String.valueOf(fieldNote.foundNumber));
		template = template.replace("##date##", sdate);
		template = template.replace("##time##", stime);
		template = template.replace("##owner##", GlobalCore.getSelectedCache().Owner);
		template = template.replace("##gcusername##", Config.settings.GcLogin.getValue());
		// template = template.replace("##gcvote##",
		// comboBoxVote.SelectedIndex.ToString());
		return template;
	}

	static EditFieldNotes efnActivity;

	private void editFieldNote()
	{
		efnActivity = new EditFieldNotes(aktFieldNote, returnListner);
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

		if (cache == null)
		{
			String message = GlobalCore.Translations.Get("cacheOtherDb", aktFieldNote.CacheName);
			message += "\n" + GlobalCore.Translations.Get("fieldNoteNoDelete");
			GL_MsgBox.Show(message);
			return;
		}

		GL_MsgBox.OnMsgBoxClickListener dialogClickListener = new GL_MsgBox.OnMsgBoxClickListener()
		{
			@Override
			public boolean onClick(int which)
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
							Cache tc = Database.Data.Query.GetCacheById(cache.Id);
							if (tc != null)
							{
								tc.Found = false;
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

		String message = GlobalCore.Translations.Get("confirmFieldnoteDeletion", aktFieldNote.typeString, aktFieldNote.CacheName);
		if (aktFieldNote.type == 1) message += GlobalCore.Translations.Get("confirmFieldnoteDeletionRst");

		GL_MsgBox.Show(message, GlobalCore.Translations.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question,
				dialogClickListener);

	}

	private void deleteAllFieldNote()
	{
		GL_MsgBox.OnMsgBoxClickListener dialogClickListener = new GL_MsgBox.OnMsgBoxClickListener()
		{
			@Override
			public boolean onClick(int which)
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
		String message = GlobalCore.Translations.Get("DeleteAllFieldNotesQuestion");
		GL_MsgBox.Show(message, GlobalCore.Translations.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning,
				dialogClickListener);

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
			String message = GlobalCore.Translations.Get("cacheOtherDb", aktFieldNote.CacheName);
			message += "\n" + GlobalCore.Translations.Get("fieldNoteNoSelect");
			GL_MsgBox.Show(message);
			return;
		}

		cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);

		if (cache == null)
		{
			Database.Data.Query.add(tmpCache);
			cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
		}

		Waypoint finalWp = null;
		if (cache.HasFinalWaypoint()) finalWp = cache.GetFinalWaypoint();
		if (cache != null) GlobalCore.setSelectedWaypoint(cache, finalWp);
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

			cm.show();
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
