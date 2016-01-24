/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI.GL_UI.Views;

import java.util.Date;

import org.slf4j.LoggerFactory;

import CB_Core.Database;
import CB_Core.LogTypes;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;
import CB_Core.Types.FieldNoteList.LoadingType;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.TemplateFormatter;
import CB_UI.GL_UI.Activitys.EditFieldNotes;
import CB_UI.GL_UI.Activitys.EditFieldNotes.IReturnListener;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Controls.PopUps.QuickFieldNoteFeedbackPopUp;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Main.Actions.CB_Action_UploadFieldNote;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.WaitDialog;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Interfaces.cancelRunnable;

public class FieldNotesView extends V_ListView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(FieldNotesView.class);

    private static FieldNoteList lFieldNotes;
    public static FieldNotesView that;
    public static FieldNoteEntry aktFieldNote;
    public static CB_RectF ItemRec;
    public static boolean firstShow = true;

    private static WaitDialog wd;
    private CustomAdapter lvAdapter;

    public FieldNotesView(CB_RectF rec, String Name) {
	super(rec, Name);
	that = this;
	ItemRec = new CB_RectF(0, 0, this.getWidth(), UI_Size_Base.that.getButtonHeight() * 1.1f);

	setBackground(SpriteCacheBase.ListBack);

	if (lFieldNotes == null)
	    lFieldNotes = new FieldNoteList();
	this.setHasInvisibleItems(true);
	this.setBaseAdapter(null);
	lvAdapter = new CustomAdapter(lFieldNotes);
	this.setBaseAdapter(lvAdapter);

	this.setEmptyMsg(Translation.Get("EmptyFieldNotes"));
	firstShow = true;
    }

    @Override
    public void onShow() {
	reloadFieldNotes();
	if (firstShow) {
	    firstShow = false;

	    GL.that.closeAllDialogs();

	    if (Config.ShowFieldnotesCMwithFirstShow.getValue())
		TabMainView.that.mToolsButtonOnLeftTabPerformClick();
	}

    }

    @Override
    public void onHide() {
	firstShow = true;
    }

    @Override
    public void Initial() {

    }

    private void reloadFieldNotes() {
	if (lFieldNotes == null)
	    lFieldNotes = new FieldNoteList();
	lFieldNotes.LoadFieldNotes("", LoadingType.loadNewLastLength);

	that.setBaseAdapter(null);
	lvAdapter = new CustomAdapter(lFieldNotes);
	that.setBaseAdapter(lvAdapter);
    }

    @Override
    protected void SkinIsChanged() {

    }

    public class CustomAdapter implements Adapter {

	private FieldNoteList fieldNoteList;

	public CustomAdapter(FieldNoteList fieldNoteList) {
	    this.fieldNoteList = fieldNoteList;
	}

	@Override
	public int getCount() {
	    int count = fieldNoteList.size();
	    if (fieldNoteList.isCropped())
		count++;
	    return count;
	}

	public Object getItem(int position) {
	    if (position >= fieldNoteList.size())
		return null;
	    return fieldNoteList.get(position);
	}

	public long getItemId(int position) {
	    return position;
	}

	@Override
	public ListViewItemBase getView(int position) {
	    FieldNoteEntry fne = null;

	    if (position < fieldNoteList.size()) {
		fne = fieldNoteList.get(position);
	    }

	    CB_RectF rec = ItemRec.copy().ScaleCenter(0.97f);
	    rec.setHeight(MeasureItemHeight(fne));
	    FieldNoteViewItem v = new FieldNoteViewItem(rec, position, fne);

	    if (fne == null) {
		v.setOnClickListener(new OnClickListener() {

		    @Override
		    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			// Load More
			lFieldNotes.LoadFieldNotes("", LoadingType.loadMore);
			FieldNotesView.this.notifyDataSetChanged();
			return true;
		    }
		});
	    } else {
		v.setOnLongClickListener(itemLogClickListener);
	    }

	    return v;
	}

	@Override
	public float getItemSize(int position) {
	    if (position > fieldNoteList.size() || fieldNoteList.size() == 0)
		return 0;

	    FieldNoteEntry fne = null;

	    if (position < fieldNoteList.size()) {
		fne = fieldNoteList.get(position);
	    }

	    return MeasureItemHeight(fne);
	}

	private float MeasureItemHeight(FieldNoteEntry fne) {
	    float headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + (UI_Size_Base.that.getMargin());
	    float cacheIfoHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + UI_Size_Base.that.getMargin() + Fonts.Measure("T").height;
	    float mesurdWidth = ItemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic() - (UI_Size_Base.that.getMargin() * 2);

	    float mh = 0;
	    if (fne != null) {
		try {
		    if (fne.comment != null && !(fne.comment.length() == 0)) {
			mh = Fonts.MeasureWrapped(fne.comment, mesurdWidth).height;
		    }
		} catch (Exception e) {

		    e.printStackTrace();
		}
	    }
	    float commentHeight = (UI_Size_Base.that.getMargin() * 3) + mh;

	    return headHeight + cacheIfoHeight + commentHeight;
	}

	private void dispose() {
	    fieldNoteList = null;
	}
    }

    public Menu getContextMenu() {

	Cache cache = GlobalCore.getSelectedCache();

	// if (cache == null) return null;

	final Menu cm = new Menu("FieldNoteContextMenu");

	cm.addOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		cm.close();

		switch (((MenuItem) v).getMenuItemId()) {
		case MenuID.MI_FOUND:
		    addNewFieldnote(LogTypes.found);
		    return true;
		case MenuID.MI_ATTENDED:
		    addNewFieldnote(LogTypes.attended);
		    return true;
		case MenuID.MI_WILL_ATTENDED:
		    addNewFieldnote(LogTypes.will_attend);
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

	if (cache != null) {

	    // Found je nach CacheType
	    if (cache.Type == null)
		return null;
	    switch (cache.Type) {
	    case Giga:
		cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", SpriteCacheBase.getThemedSprite("log8icon"));
		cm.addItem(MenuID.MI_ATTENDED, "attended", SpriteCacheBase.getThemedSprite("log9icon"));
		break;
	    case MegaEvent:
		cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", SpriteCacheBase.getThemedSprite("log8icon"));
		cm.addItem(MenuID.MI_ATTENDED, "attended", SpriteCacheBase.getThemedSprite("log9icon"));
		break;
	    case Event:
		cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", SpriteCacheBase.getThemedSprite("log8icon"));
		cm.addItem(MenuID.MI_ATTENDED, "attended", SpriteCacheBase.getThemedSprite("log9icon"));
		break;
	    case Camera:
		cm.addItem(MenuID.MI_WEBCAM_FOTO_TAKEN, "webCamFotoTaken", SpriteCacheBase.getThemedSprite("log10icon"));
		break;
	    default:
		cm.addItem(MenuID.MI_FOUND, "found", SpriteCacheBase.getThemedSprite("log0icon"));
		break;
	    }

	    cm.addItem(MenuID.MI_NOT_FOUND, "DNF", SpriteCacheBase.getThemedSprite("log1icon"));
	}

	// Aktueller Cache ist von geocaching.com dann weitere Menüeinträge freigeben
	if (cache != null && cache.getGcCode().toLowerCase().startsWith("gc")) {
	    cm.addItem(MenuID.MI_MAINTANCE, "maintenance", SpriteCacheBase.getThemedSprite("log5icon"));
	    cm.addItem(MenuID.MI_NOTE, "writenote", SpriteCacheBase.getThemedSprite("log2icon"));
	}

	cm.addItem(MenuID.MI_UPLOAD_FIELDNOTE, "uploadFieldNotes", SpriteCacheBase.Icons.get(IconName.uploadFieldNote_64.ordinal()));
	cm.addItem(MenuID.MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", SpriteCacheBase.getThemedSprite("delete"));

	if (cache != null) {
	    cm.addMoreMenu(getSecondMenu(), Translation.Get("defaultLogTypes"), Translation.Get("ownerLogTypes"));
	}

	return cm;

    }

    private Menu getSecondMenu() {
	Menu sm = new Menu("FieldNoteContextMenu/2");
	MenuItem mi;
	boolean IM_owner = GlobalCore.getSelectedCache().ImTheOwner();
	sm.addOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		switch (((MenuItem) v).getMenuItemId()) {
		case MenuID.MI_ENABLED:
		    addNewFieldnote(LogTypes.enabled);
		    return true;
		case MenuID.MI_TEMPORARILY_DISABLED:
		    addNewFieldnote(LogTypes.temporarily_disabled);
		    return true;
		case MenuID.MI_OWNER_MAINTENANCE:
		    addNewFieldnote(LogTypes.owner_maintenance);
		    return true;
		case MenuID.MI_ATTENDED:
		    addNewFieldnote(LogTypes.attended);
		    return true;
		case MenuID.MI_WEBCAM_FOTO_TAKEN:
		    addNewFieldnote(LogTypes.webcam_photo_taken);
		    return true;
		case MenuID.MI_REVIEWER_NOTE:
		    addNewFieldnote(LogTypes.reviewer_note);
		    return true;
		}
		return false;
	    }
	});

	mi = sm.addItem(MenuID.MI_ENABLED, "enabled", SpriteCacheBase.getThemedSprite("log4icon"));
	mi.setEnabled(IM_owner);
	mi = sm.addItem(MenuID.MI_TEMPORARILY_DISABLED, "temporarilyDisabled", SpriteCacheBase.getThemedSprite("log6icon"));
	mi.setEnabled(IM_owner);
	mi = sm.addItem(MenuID.MI_OWNER_MAINTENANCE, "ownerMaintenance", SpriteCacheBase.getThemedSprite("log7icon"));
	mi.setEnabled(IM_owner);

	return sm;
    }

    public static void addNewFieldnote(LogTypes type) {
	addNewFieldnote(type, false);
    }

    public static void addNewFieldnote(LogTypes type, boolean witoutShowEdit) {
	Cache cache = GlobalCore.getSelectedCache();

	if (cache == null) {
	    GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
	    return;
	}

	// chk car found?
	if (cache.getGcCode().equalsIgnoreCase("CBPark")) {

	    if (type == LogTypes.found) {
		GL_MsgBox.Show(Translation.Get("My_Parking_Area_Found"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
	    } else if (type == LogTypes.didnt_find) {
		GL_MsgBox.Show(Translation.Get("My_Parking_Area_DNF"), Translation.Get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
	    }

	    return;
	}

	// GC fremder Cache gefunden?
	if (!cache.getGcCode().toLowerCase().startsWith("gc")) {

	    if (type == LogTypes.found) {
		// Found it! -> fremden Cache als gefunden markieren
		if (!GlobalCore.getSelectedCache().isFound()) {
		    GlobalCore.getSelectedCache().setFound(true);
		    CacheDAO cacheDAO = new CacheDAO();
		    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
		    QuickFieldNoteFeedbackPopUp pop = new QuickFieldNoteFeedbackPopUp(true);
		    pop.show(PopUp_Base.SHOW_TIME_SHORT);
		    PlatformConnector.vibrate();
		}
	    } else if (type == LogTypes.didnt_find) {
		// DidNotFound -> fremden Cache als nicht gefunden markieren
		if (GlobalCore.getSelectedCache().isFound()) {
		    GlobalCore.getSelectedCache().setFound(false);
		    CacheDAO cacheDAO = new CacheDAO();
		    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
		    QuickFieldNoteFeedbackPopUp pop2 = new QuickFieldNoteFeedbackPopUp(false);
		    pop2.show(PopUp_Base.SHOW_TIME_SHORT);
		    PlatformConnector.vibrate();
		}
	    }

	    if (that != null)
		that.notifyDataSetChanged();
	    return;
	}

	FieldNoteList tmpFieldNotes = new FieldNoteList();
	tmpFieldNotes.LoadFieldNotes("", LoadingType.Loadall);

	FieldNoteEntry newFieldNote = null;
	if ((type == LogTypes.found) || (type == LogTypes.didnt_find)) {
	    // nachsehen, ob für diesen Cache bereits eine FieldNote des Types
	    // angelegt wurde
	    // und gegebenenfalls diese ändern und keine neue anlegen
	    // gilt nur für Found It! und DNF.
	    // needMaintance oder Note können zusätzlich angelegt werden

	    // .LoadFieldNotes("", false);

	    for (FieldNoteEntry nfne : tmpFieldNotes) {
		if ((nfne.CacheId == cache.Id) && (nfne.type == type)) {
		    newFieldNote = nfne;
		    newFieldNote.DeleteFromDatabase();
		    newFieldNote.timestamp = new Date();
		    aktFieldNote = newFieldNote;
		}
	    }
	}

	if (newFieldNote == null) {
	    newFieldNote = new FieldNoteEntry(type);
	    newFieldNote.CacheName = cache.getName();
	    newFieldNote.gcCode = cache.getGcCode();
	    newFieldNote.foundNumber = Config.FoundOffset.getValue();
	    newFieldNote.timestamp = new Date();
	    newFieldNote.CacheId = cache.Id;
	    newFieldNote.comment = "";
	    newFieldNote.CacheUrl = cache.getUrl();
	    newFieldNote.cacheType = cache.Type.ordinal();
	    newFieldNote.fillType();
	    // aktFieldNoteIndex = -1;
	    aktFieldNote = newFieldNote;
	} else {
	    tmpFieldNotes.remove(newFieldNote);

	}

	switch (type) {
	case found:
	    if (!cache.isFound())
		newFieldNote.foundNumber++; //
	    newFieldNote.fillType();
	    if (newFieldNote.comment.equals(""))
		newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newFieldNote);
	    // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
	    // nicht gefunden war -> foundNumber um 1 erhöhen
	    break;
	case attended:
	    if (!cache.isFound())
		newFieldNote.foundNumber++; //
	    newFieldNote.fillType();
	    if (newFieldNote.comment.equals(""))
		newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newFieldNote);
	    // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
	    // nicht gefunden war -> foundNumber um 1 erhöhen
	    break;
	case didnt_find:
	    if (newFieldNote.comment.equals(""))
		newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.DNFTemplate.getValue(), newFieldNote);
	    break;
	case needs_maintenance:
	    if (newFieldNote.comment.equals(""))
		newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.NeedsMaintenanceTemplate.getValue(), newFieldNote);
	    break;
	case note:
	    if (newFieldNote.comment.equals(""))
		newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), newFieldNote);
	    break;
	default:
	    break;
	}

	if (!witoutShowEdit) {
	    efnActivity = new EditFieldNotes(newFieldNote, returnListener, true);
	    efnActivity.show();
	} else {

	    // neue FieldNote
	    tmpFieldNotes.add(0, newFieldNote);
	    newFieldNote.WriteToDatabase();
	    aktFieldNote = newFieldNote;
	    if (newFieldNote.type == LogTypes.found) {
		// Found it! -> Cache als gefunden markieren
		if (!GlobalCore.getSelectedCache().isFound()) {
		    GlobalCore.getSelectedCache().setFound(true);
		    CacheDAO cacheDAO = new CacheDAO();
		    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
		    Config.FoundOffset.setValue(aktFieldNote.foundNumber);
		    Config.AcceptChanges();
		}
		// und eine evtl. vorhandene FieldNote DNF löschen
		tmpFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.didnt_find);
	    } else if (newFieldNote.type == LogTypes.didnt_find) {
		// DidNotFound -> Cache als nicht gefunden markieren
		if (GlobalCore.getSelectedCache().isFound()) {
		    GlobalCore.getSelectedCache().setFound(false);
		    CacheDAO cacheDAO = new CacheDAO();
		    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
		    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
		    Config.AcceptChanges();
		}
		// und eine evtl. vorhandene FieldNote FoundIt löschen
		tmpFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.found);
	    }

	    FieldNoteList.CreateVisitsTxt(Config.FieldNotesGarminPath.getValue());

	    if (that != null)
		that.notifyDataSetChanged();

	}
    }

    private static EditFieldNotes.IReturnListener returnListener = new IReturnListener() {

	@Override
	public void returnedFieldNote(FieldNoteEntry fieldNote, boolean isNewFieldNote, boolean directlog) {
	    addOrChangeFieldNote(fieldNote, isNewFieldNote, directlog);
	}

    };

    private static void addOrChangeFieldNote(FieldNoteEntry fieldNote, boolean isNewFieldNote, boolean directLog) {

	if (directLog) {
	    // try to direct upload
	    logOnline(fieldNote, isNewFieldNote);
	    return;
	}

	FieldNotesView.firstShow = false;

	if (fieldNote != null) {

	    if (isNewFieldNote) {
		// nur, wenn eine FieldNote neu angelegt wurde
		// neue FieldNote
		lFieldNotes.add(0, fieldNote);

		// eine evtl. vorhandene FieldNote /DNF löschen
		if (fieldNote.type == LogTypes.attended || fieldNote.type == LogTypes.found || fieldNote.type == LogTypes.didnt_find) {
		    lFieldNotes.DeleteFieldNoteByCacheId(fieldNote.CacheId, LogTypes.found);
		    lFieldNotes.DeleteFieldNoteByCacheId(fieldNote.CacheId, LogTypes.didnt_find);
		}
	    }

	    fieldNote.WriteToDatabase();
	    aktFieldNote = fieldNote;

	    if (isNewFieldNote) {
		// nur, wenn eine FieldNote neu angelegt wurde
		// wenn eine FieldNote neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
		// SelectedCache eine fieldNote angelegt wird
		if (fieldNote.type == LogTypes.found || fieldNote.type == LogTypes.attended) { // Found it! -> Cache als gefunden markieren
		    if (!GlobalCore.getSelectedCache().isFound()) {
			GlobalCore.getSelectedCache().setFound(true);
			CacheDAO cacheDAO = new CacheDAO();
			cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
			Config.FoundOffset.setValue(aktFieldNote.foundNumber);
			Config.AcceptChanges();
		    }

		} else if (fieldNote.type == LogTypes.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
		    if (GlobalCore.getSelectedCache().isFound()) {
			GlobalCore.getSelectedCache().setFound(false);
			CacheDAO cacheDAO = new CacheDAO();
			cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
			Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
			Config.AcceptChanges();
		    } // und eine evtl. vorhandene FieldNote FoundIt löschen
		    lFieldNotes.DeleteFieldNoteByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.found);
		}
	    }
	    FieldNoteList.CreateVisitsTxt(Config.FieldNotesGarminPath.getValue());

	    // Reload List
	    if (isNewFieldNote) {
		lFieldNotes.LoadFieldNotes("", LoadingType.LoadNew);
	    } else {
		lFieldNotes.LoadFieldNotes("", LoadingType.loadNewLastLength);
	    }
	}
	that.notifyDataSetChanged();
    }

    private static void logOnline(final FieldNoteEntry fieldNote, final boolean isNewFieldNote) {

	wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), new IcancelListener() {

	    @Override
	    public void isCanceld() {

	    }
	}, new cancelRunnable() {

	    @Override
	    public void run() {
		GroundspeakAPI.LastAPIError = "";

		boolean dl = fieldNote.isDirectLog;
		int result = CB_Core.Api.GroundspeakAPI.CreateFieldNoteAndPublish(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, dl, this);

		if (result == GroundspeakAPI.IO) {
		    fieldNote.uploaded = true;

		    // after direct Log create a fieldNote with uploded state
		    addOrChangeFieldNote(fieldNote, isNewFieldNote, false);
		}

		if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
		    GL.that.Toast(ConnectionError.INSTANCE);
		    if (wd != null)
			wd.close();
		    GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

			@Override
			public boolean onClick(int which, Object data) {
			    switch (which) {
			    case GL_MsgBox.BUTTON_NEGATIVE:
				addOrChangeFieldNote(fieldNote, isNewFieldNote, true);// try again
				return true;

			    case GL_MsgBox.BUTTON_NEUTRAL:
				return true;

			    case GL_MsgBox.BUTTON_POSITIVE:
				addOrChangeFieldNote(fieldNote, isNewFieldNote, false);// create Fieldnote
				return true;
			    }
			    return true;
			}
		    });
		    return;
		}
		if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
		    GL.that.Toast(ApiUnavailable.INSTANCE);
		    if (wd != null)
			wd.close();
		    GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

			@Override
			public boolean onClick(int which, Object data) {
			    switch (which) {
			    case GL_MsgBox.BUTTON_NEGATIVE:
				addOrChangeFieldNote(fieldNote, isNewFieldNote, true);// try again
				return true;

			    case GL_MsgBox.BUTTON_NEUTRAL:
				return true;

			    case GL_MsgBox.BUTTON_POSITIVE:
				addOrChangeFieldNote(fieldNote, isNewFieldNote, false);// create Fieldnote
				return true;
			    }
			    return true;
			}
		    });
		    return;
		}

		if (GroundspeakAPI.LastAPIError.length() > 0) {
		    GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
			    GL_MsgBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("Error"), MessageBoxIcon.Error);
			}
		    });
		}

		if (wd != null)
		    wd.close();
	    }

	    @Override
	    public boolean cancel() {
		// TODO handle cancel
		return false;
	    }
	});

    }

    private static EditFieldNotes efnActivity;

    private void editFieldNote() {
	if (efnActivity != null && !efnActivity.isDisposed()) {
	    efnActivity.setFieldNote(aktFieldNote, returnListener, false);
	} else {
	    efnActivity = new EditFieldNotes(aktFieldNote, returnListener, false);
	}

	efnActivity.show();
    }

    private void deleteFieldNote() {
	// aktuell selectierte FieldNote löschen
	if (aktFieldNote == null)
	    return;
	// final Cache cache =
	// Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);

	Cache tmpCache = null;
	// suche den Cache aus der DB.
	// Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
	CacheList lCaches = new CacheList();
	CacheListDAO cacheListDAO = new CacheListDAO();
	cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId, false, false);
	if (lCaches.size() > 0)
	    tmpCache = lCaches.get(0);
	final Cache cache = tmpCache;

	if (cache == null && !aktFieldNote.isTbFieldNote) {
	    String message = Translation.Get("cacheOtherDb", aktFieldNote.CacheName);
	    message += "\n" + Translation.Get("fieldNoteNoDelete");
	    GL_MsgBox.Show(message);
	    return;
	}

	GL_MsgBox.OnMsgBoxClickListener dialogClickListener = new GL_MsgBox.OnMsgBoxClickListener() {
	    @Override
	    public boolean onClick(int which, Object data) {
		switch (which) {
		case GL_MsgBox.BUTTON_POSITIVE:
		    // Yes button clicked
		    // delete aktFieldNote
		    if (cache != null) {
			if (cache.isFound()) {
			    cache.setFound(false);
			    CacheDAO cacheDAO = new CacheDAO();
			    cacheDAO.WriteToDatabase_Found(cache);
			    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
			    Config.AcceptChanges();
			    // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
			    // damit das Smiley Symbol aus der Map und der CacheList verschwindet
			    synchronized (Database.Data.Query) {
				Cache tc = Database.Data.Query.GetCacheById(cache.Id);
				if (tc != null) {
				    tc.setFound(false);
				}
			    }
			}
		    }
		    lFieldNotes.DeleteFieldNote(aktFieldNote.Id, aktFieldNote.type);

		    aktFieldNote = null;

		    lFieldNotes.LoadFieldNotes("", LoadingType.loadNewLastLength);

		    that.setBaseAdapter(null);
		    lvAdapter = new CustomAdapter(lFieldNotes);
		    that.setBaseAdapter(lvAdapter);

		    FieldNoteList.CreateVisitsTxt(Config.FieldNotesGarminPath.getValue());

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
	if (aktFieldNote.isTbFieldNote) {
	    message = Translation.Get("confirmFieldnoteDeletionTB", aktFieldNote.typeString, aktFieldNote.TbName);
	} else {
	    message = Translation.Get("confirmFieldnoteDeletion", aktFieldNote.typeString, aktFieldNote.CacheName);
	    if (aktFieldNote.type == LogTypes.found)
		message += Translation.Get("confirmFieldnoteDeletionRst");
	}

	GL_MsgBox.Show(message, Translation.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);

    }

    private void deleteAllFieldNote() {
	final GL_MsgBox.OnMsgBoxClickListener dialogClickListener = new GL_MsgBox.OnMsgBoxClickListener() {
	    @Override
	    public boolean onClick(int which, Object data) {
		switch (which) {
		case GL_MsgBox.BUTTON_POSITIVE:
		    // Yes button clicked
		    // delete all FieldNote

		    // reload all Fieladnotes!
		    lFieldNotes.LoadFieldNotes("", LoadingType.Loadall);

		    for (FieldNoteEntry entry : lFieldNotes) {
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
	final String message = Translation.Get("DeleteAllFieldNotesQuestion");
	GL.that.RunOnGL(new IRunOnGL() {

	    @Override
	    public void run() {
		GL_MsgBox.Show(message, Translation.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener);

	    }
	});

    }

    private void selectCacheFromFieldNote() {
	if (aktFieldNote == null)
	    return;

	// suche den Cache aus der DB.
	// Nicht aus der aktuellen Query, da dieser herausgefiltert sein könnte
	CacheList lCaches = new CacheList();
	CacheListDAO cacheListDAO = new CacheListDAO();
	cacheListDAO.ReadCacheList(lCaches, "Id = " + aktFieldNote.CacheId, false, false);
	Cache tmpCache = null;
	if (lCaches.size() > 0)
	    tmpCache = lCaches.get(0);
	Cache cache = tmpCache;

	if (cache == null) {
	    String message = Translation.Get("cacheOtherDb", aktFieldNote.CacheName);
	    message += "\n" + Translation.Get("fieldNoteNoSelect");
	    GL_MsgBox.Show(message);
	    return;
	}

	synchronized (Database.Data.Query) {
	    cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
	}

	if (cache == null) {
	    Database.Data.Query.add(tmpCache);
	    cache = Database.Data.Query.GetCacheByGcCode(aktFieldNote.gcCode);
	}

	Waypoint finalWp = null;
	if (cache != null) {
	    if (cache.HasFinalWaypoint())
		finalWp = cache.GetFinalWaypoint();
	    else if (cache.HasStartWaypoint())
		finalWp = cache.GetStartWaypoint();
	    GlobalCore.setSelectedWaypoint(cache, finalWp);
	}
    }

    private final OnClickListener itemLogClickListener = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

	    int index = ((ListViewItemBase) v).getIndex();

	    aktFieldNote = lFieldNotes.get(index);

	    Menu cm = new Menu("CacheListContextMenu");

	    cm.addOnClickListener(new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		    switch (((MenuItem) v).getMenuItemId()) {
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
    public void notifyDataSetChanged() {
	reloadFieldNotes();
	super.notifyDataSetChanged();
    }

    @Override
    public void dispose() {
	this.setBaseAdapter(null);
	if (lvAdapter != null)
	    lvAdapter.dispose();
	lvAdapter = null;
	that = null;
	super.dispose();
	log.debug("FieldNotesView disposed");
    }
}
