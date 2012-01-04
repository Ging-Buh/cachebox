package de.cachebox_test.Views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;
import CB_Core.Types.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Ui.ActivityUtils;
import de.cachebox_test.Ui.AllContextMenuCallHandler;
import de.cachebox_test.Ui.Sizes;
import de.cachebox_test.Views.Forms.EditFieldNote;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.MessageBoxButtons;
import de.cachebox_test.Views.Forms.MessageBoxIcon;
import de.cachebox_test.Views.Forms.ProgressDialog;

public class FieldNotesView extends ListView implements ViewOptionsMenu
{
	public static FieldNoteEntry aktFieldNote;
	private int aktFieldNoteIndex = -1;
	Activity parentActivity;
	FieldNoteList lFieldNotes;
	CustomAdapter lvAdapter;

	public FieldNotesView(Context context, final Activity parentActivity)
	{
		super(context);
		this.parentActivity = parentActivity;

		lFieldNotes = new FieldNoteList();

		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lFieldNotes);
		this.setAdapter(lvAdapter);
		// this.setLongClickable(true);
		this.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{

				aktFieldNote = lFieldNotes.get(arg2);
				aktFieldNoteIndex = arg2;
				invalidate();
				return;
			}
		});

		this.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				aktFieldNote = lFieldNotes.get(arg2);
				aktFieldNoteIndex = arg2;
				invalidate();
				AllContextMenuCallHandler.showFiledNotesView_ItemContextMenu();
				return false;
			}
		});

		ActivityUtils.setListViewPropertys(this);

	}

	static public int windowW = 0;
	static public int windowH = 0;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// we overriding onMeasure because this is where the application gets
		// its right size.
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		windowW = getMeasuredWidth();
		windowH = getMeasuredHeight();
	}

	public class CustomAdapter extends BaseAdapter
	{

		private Context context;
		private FieldNoteList fieldNoteList;

		public CustomAdapter(Context context, FieldNoteList fieldNoteList)
		{
			this.context = context;
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

		public View getView(int position, View convertView, ViewGroup parent)
		{
			FieldNoteEntry fne = fieldNoteList.get(position);
			Boolean BackGroundChanger = ((position % 2) == 1);
			FieldNoteViewItem v = new FieldNoteViewItem(context, fne, BackGroundChanger);

			return v;
		}
	}

	private void UploadFieldnotes()
	{

		MessageBox.Show(GlobalCore.Translations.Get("uploadFieldNotes?"), GlobalCore.Translations.Get("uploadFieldNotes"),
				MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, UploadFieldnotesDialogListner);

	}

	private final DialogInterface.OnClickListener UploadFieldnotesDialogListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			switch (button)
			{
			case -1:
				UploadFieldNotes();
				break;
			case -2:

				break;
			}

			dialog.dismiss();
		}

	};

	private void UploadFieldNotes()
	{
		Thread UploadFieldNotesdThread = new Thread()
		{
			public void run()
			{
				ProgresssChangedEventList.Call("Upload", "", 0);

				String accessToken = Config.GetAccessToken();

				int count = 0;
				int anzahl = lFieldNotes.size();
				UploadMeldung = "";
				for (FieldNoteEntry fieldNote : lFieldNotes)
				{
					if (ThreadCancel) // wenn im ProgressDialog Cancel gedrückt
										// wurde.
					break;
					// Progress status Melden
					ProgresssChangedEventList.Call(fieldNote.CacheName, (100 * count) / anzahl);

					if (CB_Core.Api.GroundspeakAPI.CreateFieldNoteAndPublish(accessToken, fieldNote.gcCode, fieldNote.getGcUploadId(),
							fieldNote.timestamp, fieldNote.comment) != 0) UploadMeldung += fieldNote.gcCode + "\n"
							+ CB_Core.Api.GroundspeakAPI.LastAPIError + "\n";
					count++;
				}

				if (!ThreadCancel)
				{
					ProgressDialog.Ready(); // Dem Progress Dialog melden, dass
											// der Thread fertig ist!
					ProgressHandler.post(ProgressReady); // auf das Ende des
															// Threads reagieren
				}
			}
		};

		// ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.
		ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread, ProgressCanceld);

	}

	private Boolean ThreadCancel = false;
	final Runnable ProgressCanceld = new Runnable()
	{
		public void run()
		{
			ThreadCancel = true;
			MessageBox.Show(GlobalCore.Translations.Get("uploadCanceled"));
		}
	};

	private String UploadMeldung = "";

	final Handler ProgressHandler = new Handler();
	final Runnable ProgressReady = new Runnable()
	{
		public void run()
		{
			if (!UploadMeldung.equals(""))
			{
				MessageBox.Show(UploadMeldung, GlobalCore.Translations.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
			}
			else
			{
				MessageBox.Show(GlobalCore.Translations.Get("uploadFinished"), GlobalCore.Translations.Get("uploadFieldNotes"),
						MessageBoxIcon.GC_Live);
			}

		}
	};

	private void addNewFieldnote(int type)
	{
		Cache cache = GlobalCore.SelectedCache();

		if (cache == null)
		{
			MessageBox.Show(GlobalCore.Translations.Get("NoCacheSelect"), GlobalCore.Translations.Get("thisNotWork"), MessageBoxButtons.OK,
					MessageBoxIcon.Error, null);
			return;
		}

		// chk car found?
		if (cache.GcCode.equalsIgnoreCase("CBPark"))
		{

			if (type == 1)
			{
				MessageBox.Show(GlobalCore.Translations.Get("My_Parking_Area_Found"), GlobalCore.Translations.Get("thisNotWork"),
						MessageBoxButtons.OK, MessageBoxIcon.Information, null);
			}
			else if (type == 2)
			{
				MessageBox.Show(GlobalCore.Translations.Get("My_Parking_Area_DNF"), GlobalCore.Translations.Get("thisNotWork"),
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

		Intent mainIntent = new Intent().setClass(getContext(), EditFieldNote.class);
		Bundle b = new Bundle();
		b.putSerializable("FieldNote", newFieldNote);
		mainIntent.putExtras(b);

		main.mainActivity.startActivityForResult(mainIntent, 0);

	}

	@Override
	public void OnShow()
	{
		// reinitial colors
		ActivityUtils.setListViewPropertys(this);
		FieldNoteViewItem.Linepaint = null;
		FieldNoteViewItem.TextPaint = null;
		FieldNoteViewItem.cacheNamePaint = null;

		if (lFieldNotes.size() == 0) lFieldNotes.LoadFieldNotes("");
	}

	@Override
	public void OnHide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void OnFree()
	{

	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null)
		{

			int sollHeight = (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue()) ? Sizes
					.getQuickButtonListHeight() : 0;
			((main) main.mainActivity).setQuickButtonHeight(sollHeight);
			downSlider.isInitial = false;
			((main) main.mainActivity).InfoDownSlider.invalidate();
			return;
		}

		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			FieldNoteEntry fieldNote = (FieldNoteEntry) bundle.getSerializable("FieldNoteResult");
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
				lvAdapter.notifyDataSetChanged();
			}
		}

		int sollHeight = (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue()) ? Sizes
				.getQuickButtonListHeight() : 0;
		((main) main.mainActivity).setQuickButtonHeight(sollHeight);
		downSlider.isInitial = false;
		((main) main.mainActivity).InfoDownSlider.invalidate();
	}

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

	private void editFieldNote()
	{
		Intent mainIntent = new Intent().setClass(getContext(), EditFieldNote.class);
		Bundle b = new Bundle();
		b.putSerializable("FieldNote", aktFieldNote);
		mainIntent.putExtras(b);
		main.mainActivity.startActivityForResult(mainIntent, 0);
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
			MessageBox.Show(message);
			return;
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
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

					lvAdapter.notifyDataSetChanged();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					// do nothing
					break;
				}
				dialog.dismiss();
			}
		};

		String message = GlobalCore.Translations.Get("confirmFieldnoteDeletion", aktFieldNote.typeString, aktFieldNote.CacheName);
		if (aktFieldNote.type == 1) message += GlobalCore.Translations.Get("confirmFieldnoteDeletionRst");

		MessageBox.Show(message, GlobalCore.Translations.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question,
				dialogClickListener);

	}

	private void deleteAllFieldNote()
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					// delete aktFieldNote
					for (FieldNoteEntry entry : lFieldNotes)
					{
						entry.DeleteFromDatabase();

					}

					lFieldNotes.clear();
					aktFieldNote = null;
					aktFieldNoteIndex = -1;
					lvAdapter.notifyDataSetChanged();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					// do nothing
					break;
				}
				dialog.dismiss();

			}
		};
		String message = GlobalCore.Translations.Get("DeleteAllFieldNotesQuestion");
		MessageBox.Show(message, GlobalCore.Translations.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning,
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
			MessageBox.Show(message);
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

	@Override
	public int GetMenuId()
	{
		return 0;
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

	@Override
	public boolean ItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
		case R.id.fieldnotesview_found:
			addNewFieldnote(1);
			return true;
		case R.id.fieldnotesview_notfound:
			addNewFieldnote(2);
			return true;
		case R.id.fieldnotesview_maintenance:
			addNewFieldnote(3);
			return true;
		case R.id.fieldnotesview_addnote:
			addNewFieldnote(4);
			return true;
		case R.id.fieldnotesview_upload:
			UploadFieldnotes();
			return true;
		case R.id.fieldnotesview_deleteall:
			deleteAllFieldNote();
			return true;
		case R.id.c_fnv_edit:
			editFieldNote();
			return true;
		case R.id.c_fnv_delete:
			deleteFieldNote();
			return true;
		case R.id.c_fnv_selectcache:
			selectCacheFromFieldNote();
			return true;
		case R.id.fieldnotesview_manage:
			AllContextMenuCallHandler.showFiledNotesView_manageContextMenu();
			return true;
		}
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
		AllContextMenuCallHandler.showFiledNotesViewContextMenu();
	}

}
