package CB_Core.GL_UI.Views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
import CB_Core.GL_UI.Menu.MenuItem;
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
	private int aktFieldNoteIndex = -1;
	FieldNoteList lFieldNotes;
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
		if (lFieldNotes.size() == 0) lFieldNotes.LoadFieldNotes("");
		this.notifyDataSetChanged();
		if (firstShow)
		{
			firstShow = false;
			showContextMenu();
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
			rec.setHeight(mesureItemHeight(fne));
			FieldNoteViewItem v = new FieldNoteViewItem(rec, position, fne);

			v.setOnLongClickListener(itemLogClickListner);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			if (position > fieldNoteList.size() || fieldNoteList.size() == 0) return 0;
			FieldNoteEntry fne = fieldNoteList.get(position);
			return mesureItemHeight(fne);
		}

		private float mesureItemHeight(FieldNoteEntry fne)
		{
			float headHeight = (UiSizes.getButtonHeight() / 1.5f) + (Dialog.margin);
			float cacheIfoHeight = (UiSizes.getButtonHeight() / 1.5f) + Dialog.margin + Fonts.Mesure("T").height;
			float mesurdWidth = ItemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic()
					- ListViewItemBackground.getRightWidthStatic() - (Dialog.margin * 2);

			float mh = 0;
			if (fne.comment != null && !(fne.comment.length() == 0))
			{
				mh = Fonts.MesureWrapped(fne.comment, mesurdWidth).height;
			}

			float commentHeight = (Dialog.margin * 3) + mh;

			return headHeight + cacheIfoHeight + commentHeight;
		}
	}

	private static final int MI_FOUND = 0;
	private static final int MI_NOT_FOUND = 1;
	private static final int MI_MAINTANCE = 2;
	private static final int MI_NOTE = 3;
	private static final int MI_MANAGE = 4;
	private static final int MI_UPLOAD_FIELDNOTE = 5;
	private static final int MI_DELETE_ALL_FIELDNOTES = 6;
	private static final int MI_DELETE_FIELDNOTE = 7;
	private static final int MI_EDIT_FIELDNOTE = 8;
	private static final int MI_SELECT_CACHE = 9;

	public void showContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_FOUND:
					addNewFieldnote(1);
					return true;
				case MI_NOT_FOUND:
					addNewFieldnote(2);
					return true;
				case MI_MAINTANCE:
					addNewFieldnote(3);
					return true;
				case MI_NOTE:
					addNewFieldnote(4);
					return true;

				case MI_MANAGE:
					manageFieldNote();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MI_FOUND, "found", SpriteCache.getThemedSprite("log0icon"));
		cm.addItem(MI_NOT_FOUND, "DNF", SpriteCache.getThemedSprite("log1icon"));
		cm.addItem(MI_MAINTANCE, "maintenance", SpriteCache.getThemedSprite("log5icon"));
		cm.addItem(MI_NOTE, "writenote", SpriteCache.getThemedSprite("log2icon"));
		cm.addItem(MI_MANAGE, "ManageNotes");

		cm.show();

	}

	private void manageFieldNote()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_UPLOAD_FIELDNOTE:
					UploadFieldnotes();
					return true;
				case MI_DELETE_ALL_FIELDNOTES:
					deleteAllFieldNote();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MI_UPLOAD_FIELDNOTE, "uploadFieldNotes", SpriteCache.Icons.get(35));
		cm.addItem(MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", SpriteCache.getThemedSprite("delete"));

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
		final Thread UploadFieldNotesdThread = new Thread()
		{
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

				if (!ThreadCancel)
				{
					ProgressReady.run();
				}
			}
		};

		// ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.
		PD = ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread, ProgressCanceld);

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
	final Runnable ProgressCanceld = new Runnable()
	{
		public void run()
		{
			ThreadCancel = true;
			GL_MsgBox.Show(GlobalCore.Translations.Get("uploadCanceled"));
		}
	};

	private String UploadMeldung = "";

	final Runnable ProgressReady = new Runnable()
	{
		public void run()
		{
			Timer runTimer = new Timer();
			TimerTask task = new TimerTask()
			{

				@Override
				public void run()
				{
					if (!UploadMeldung.equals(""))
					{
						GL_MsgBox.Show(UploadMeldung, GlobalCore.Translations.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error,
								null);
					}
					else
					{
						GL_MsgBox.Show(GlobalCore.Translations.Get("uploadFinished"), GlobalCore.Translations.Get("uploadFieldNotes"),
								MessageBoxIcon.GC_Live);
					}
				}
			};

			runTimer.schedule(task, 200);

		}
	};

	private void addNewFieldnote(int type)
	{
		Cache cache = GlobalCore.SelectedCache();

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
			for (FieldNoteEntry nfne : lFieldNotes)
			{
				if ((nfne.CacheId == cache.Id) && (nfne.type == type))
				{
					newFieldNote = nfne;
					aktFieldNote = newFieldNote;
					aktFieldNoteIndex = index;
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
			aktFieldNoteIndex = -1;
			aktFieldNote = newFieldNote;
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

		efnActivity = new EditFieldNotes(newFieldNote, returnListner);
		efnActivity.show();
	}

	private EditFieldNotes.ReturnListner returnListner = new ReturnListner()
	{

		@Override
		public void returnedFieldNote(FieldNoteEntry fieldNote)
		{

			FieldNotesView.firstShow = false;

			efnActivity.dispose();
			efnActivity = null;

			if (fieldNote != null)
			{
				if ((aktFieldNote != null) && (aktFieldNoteIndex != -1))
				{
					// Änderungen in aktFieldNote übernehmen
					lFieldNotes.remove(aktFieldNoteIndex);
					aktFieldNote = fieldNote;
					lFieldNotes.add(aktFieldNoteIndex, aktFieldNote);
					aktFieldNote.UpdateDatabase();

					FieldNoteList.CreateVisitsTxt();
				}
				else
				{
					// neue FieldNote
					lFieldNotes.add(0, fieldNote);
					fieldNote.WriteToDatabase();
					aktFieldNote = fieldNote;
					if (fieldNote.type == 1)
					{
						// Found it! -> Cache als gefunden markieren
						if (!GlobalCore.SelectedCache().Found)
						{
							GlobalCore.SelectedCache().Found = true;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(GlobalCore.SelectedCache());
							Config.settings.FoundOffset.setValue(aktFieldNote.foundNumber);
							Config.AcceptChanges();
						}
					}
					else if (fieldNote.type == 2)
					{
						// DidNotFound -> Cache als nicht gefunden markieren
						if (GlobalCore.SelectedCache().Found)
						{
							GlobalCore.SelectedCache().Found = false;
							CacheDAO cacheDAO = new CacheDAO();
							cacheDAO.WriteToDatabase_Found(GlobalCore.SelectedCache());
							Config.settings.FoundOffset.setValue(Config.settings.FoundOffset.getValue() - 1);
							Config.AcceptChanges();
						}
						// und eine evtl. vorhandene FieldNote FoundIt löschen
						lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.SelectedCache().Id, 1);
					}

					FieldNoteList.CreateVisitsTxt();
				}
				that.notifyDataSetChanged();
			}
		}
	};

	private String ReplaceTemplate(String template, FieldNoteEntry fieldNote)
	{
		DateFormat iso8601Format = new SimpleDateFormat("HH:mm");
		String stime = iso8601Format.format(fieldNote.timestamp);
		iso8601Format = new SimpleDateFormat("dd-MM-yyyy");
		String sdate = iso8601Format.format(fieldNote.timestamp);

		template = template.replace("<br>", "\n");
		template = template.replace("##finds##", String.valueOf(fieldNote.foundNumber));
		template = template.replace("##date##", sdate);
		template = template.replace("##time##", stime);
		template = template.replace("##owner##", GlobalCore.SelectedCache().Owner);
		template = template.replace("##gcusername##", Config.settings.GcLogin.getValue());
		// template = template.replace("##gcvote##",
		// comboBoxVote.SelectedIndex.ToString());
		return template;
	}

	EditFieldNotes efnActivity;

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
					aktFieldNoteIndex = -1;

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
					aktFieldNoteIndex = -1;
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
		if (cache != null) GlobalCore.SelectedWaypoint(cache, finalWp);
	}

	private OnLongClickListener itemLogClickListner = new OnLongClickListener()
	{

		@Override
		public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			int index = ((ListViewItemBase) v).getIndex();

			aktFieldNote = lFieldNotes.get(index);
			aktFieldNoteIndex = index;

			Menu cm = new Menu("CacheListContextMenu");

			cm.setItemClickListner(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					switch (((MenuItem) v).getMenuItemId())
					{
					case MI_SELECT_CACHE:
						selectCacheFromFieldNote();
						return true;
					case MI_EDIT_FIELDNOTE:
						editFieldNote();
						return true;
					case MI_DELETE_FIELDNOTE:
						deleteFieldNote();
						return true;

					}
					return false;
				}
			});

			cm.addItem(MI_SELECT_CACHE, "SelectCache");
			cm.addItem(MI_EDIT_FIELDNOTE, "edit");
			cm.addItem(MI_DELETE_FIELDNOTE, "delete");

			cm.show();
			return true;
		}
	};

}
