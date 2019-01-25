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

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CB_Core_Settings;
import CB_Core.GCVote.GCVote;
import CB_Core.LogTypes;
import CB_Core.Types.*;
import CB_Core.Types.FieldNoteList.LoadingType;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.EditFieldNotes;
import CB_UI.GL_UI.Controls.PopUps.QuickFieldNoteFeedbackPopUp;
import CB_UI.GL_UI.Main.Actions.CB_Action_UploadFieldNote;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GlobalCore;
import CB_UI.TemplateFormatter;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.WaitDialog;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Lists.CB_FixSizeList;
import CB_Utils.Log.Log;

import java.util.Date;

import static CB_Core.Database.Data;

public class FieldNotesView extends V_ListView {
    private static final String log = "FieldNotesView";
    public static FieldNotesView that;
    private static FieldNoteEntry aktFieldNote;
    private static boolean firstShow = true;
    private static CB_RectF ItemRec;
    private static FieldNoteList lFieldNotes;
    private static WaitDialog wd;
    private static EditFieldNotes.IReturnListener returnListener = FieldNotesView::addOrChangeFieldNote;
    private static EditFieldNotes efnActivity;
    private CustomAdapter lvAdapter;

    public FieldNotesView(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        this.mCanDispose = false;
        this.setForceHandleTouchEvents(true);
        ItemRec = new CB_RectF(0, 0, this.getWidth(), UI_Size_Base.that.getButtonHeight() * 1.1f);

        setBackground(Sprites.ListBack);

        if (lFieldNotes == null)
            lFieldNotes = new FieldNoteList();
        this.setHasInvisibleItems(true);
        this.setBaseAdapter(null);
        lvAdapter = new CustomAdapter(lFieldNotes);
        this.setBaseAdapter(lvAdapter);

        this.setEmptyMsg(Translation.Get("EmptyFieldNotes"));
        firstShow = true;
    }

    private static void addNewFieldNote(LogTypes type) {
        addNewFieldNote(type, "", false);
    }

    public static void addNewFieldNote(LogTypes type, String templateText, boolean withoutShowEdit) {
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

        // kein GC Cache
        if (!cache.getGcCode().toLowerCase().startsWith("gc")) {

            if (type == LogTypes.found || type == LogTypes.attended || type == LogTypes.webcam_photo_taken) {
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
        if ((type == LogTypes.found) //
                || (type == LogTypes.attended) //
                || (type == LogTypes.webcam_photo_taken) //
                || (type == LogTypes.didnt_find)) {
            // Is there already a FieldNote of this type for this cache
            // then change else new
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
            newFieldNote.comment = templateText;
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
                // wenn eine FieldNote Found erzeugt werden soll und der Cache noch nicht gefunden war -> foundNumber um 1 erhöhen
                if (!cache.isFound())
                    newFieldNote.foundNumber++;
                newFieldNote.fillType();
                if (newFieldNote.comment.length() == 0)
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newFieldNote);
                break;
            case attended:
                if (!cache.isFound())
                    newFieldNote.foundNumber++; //
                newFieldNote.fillType();
                if (newFieldNote.comment.length() == 0)
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.AttendedTemplate.getValue(), newFieldNote);
                // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case webcam_photo_taken:
                if (!cache.isFound())
                    newFieldNote.foundNumber++; //
                newFieldNote.fillType();
                if (newFieldNote.comment.length() == 0)
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.WebcamTemplate.getValue(), newFieldNote);
                // wenn eine FieldNote Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case didnt_find:
                if (newFieldNote.comment.length() == 0)
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.DNFTemplate.getValue(), newFieldNote);
                break;
            case needs_maintenance:
                if (newFieldNote.comment.length() == 0)
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.NeedsMaintenanceTemplate.getValue(), newFieldNote);
                break;
            case note:
                if (newFieldNote.comment.length() == 0)
                    newFieldNote.comment = TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), newFieldNote);
                break;
            default:
                break;
        }

        if (withoutShowEdit) {
            // neue FieldNote
            tmpFieldNotes.add(0, newFieldNote);
            newFieldNote.WriteToDatabase();
            aktFieldNote = newFieldNote;
            if (newFieldNote.type == LogTypes.found || newFieldNote.type == LogTypes.attended || newFieldNote.type == LogTypes.webcam_photo_taken) {
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

        } else {
            efnActivity = new EditFieldNotes(newFieldNote, returnListener, true);
            efnActivity.show();
        }
    }

    private static void addOrChangeFieldNote(FieldNoteEntry fieldNote, boolean isNewFieldNote, boolean directLog) {

        if (directLog) {
            // try to direct upload
            logOnline(fieldNote, isNewFieldNote);
            return;
        }

        FieldNotesView.firstShow = false;

        if (fieldNote != null) {

            if (isNewFieldNote) {

                lFieldNotes.add(0, fieldNote);

                // eine evtl. vorhandene FieldNote /DNF löschen
                if (fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.webcam_photo_taken //
                        || fieldNote.type == LogTypes.didnt_find) {
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
                if (fieldNote.type == LogTypes.found //
                        || fieldNote.type == LogTypes.attended //
                        || fieldNote.type == LogTypes.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
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

        wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), () -> {

        }, new ICancelRunnable() {

            @Override
            public void run() {

                if (Config.GcVotePassword.getEncryptedValue().length() > 0 && !fieldNote.isTbFieldNote) {
                    if (fieldNote.gc_Vote > 0) {
                        // Stimme abgeben
                        try {
                            if (!GCVote.SendVotes(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), fieldNote.gc_Vote, fieldNote.CacheUrl, fieldNote.gcCode)) {
                                Log.err(log, fieldNote.gcCode + " GC-Vote");
                            }
                        } catch (Exception e) {
                            Log.err(log, fieldNote.gcCode + " GC-Vote");
                        }
                    }
                }

                if (GroundspeakAPI.OK == GroundspeakAPI.UploadDraftOrLog(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, fieldNote.isDirectLog)) {
                    // after direct Log change state to uploaded
                    fieldNote.uploaded = true;
                    addOrChangeFieldNote(fieldNote, isNewFieldNote, false);
                } else {
                    // Error handling
                    GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, (which, data) -> {
                        switch (which) {
                            case GL_MsgBox.BUTTON_NEGATIVE:
                                addOrChangeFieldNote(fieldNote, isNewFieldNote, true);// try again
                                break;
                            case GL_MsgBox.BUTTON_NEUTRAL:
                                break;
                            case GL_MsgBox.BUTTON_POSITIVE:
                                addOrChangeFieldNote(fieldNote, isNewFieldNote, false);// create Fieldnote
                        }
                        return true;
                    });
                }
                if (GroundspeakAPI.LastAPIError.length() > 0) {
                    GL.that.RunOnGL(() -> GL_MsgBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("Error"), MessageBoxIcon.Error));
                }
                if (wd != null)
                    wd.close();

            }

            @Override
            public boolean doCancel() {
                return false;
            }
        });

    }

    @Override
    public void onShow() {
        reloadFieldNotes();
        if (firstShow) {
            firstShow = false;

            GL.that.closeAllDialogs();

            if (Config.ShowFieldnotesContextMenuWithFirstShow.getValue())
                TabMainView.that.mToolsButtonOnLeftTabPerformClick();
        }

    }

    @Override
    public void onHide() {
        firstShow = true;
    }

    private void reloadFieldNotes() {
        if (lFieldNotes == null)
            lFieldNotes = new FieldNoteList();
        lFieldNotes.LoadFieldNotes("", LoadingType.loadNewLastLength);

        that.setBaseAdapter(null);
        lvAdapter = new CustomAdapter(lFieldNotes);
        that.setBaseAdapter(lvAdapter);
    }

    public Menu getContextMenu() {

        Cache cache = GlobalCore.getSelectedCache();

        final Menu cm = new Menu("FieldNoteContextMenu");

        cm.addOnClickListener((v, x, y, pointer, button) -> {
            cm.close();

            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_FOUND:
                    addNewFieldNote(LogTypes.found);
                    return true;
                case MenuID.MI_ATTENDED:
                    addNewFieldNote(LogTypes.attended);
                    return true;
                case MenuID.MI_WEBCAM_FOTO_TAKEN:
                    addNewFieldNote(LogTypes.webcam_photo_taken);
                    return true;
                case MenuID.MI_WILL_ATTENDED:
                    addNewFieldNote(LogTypes.will_attend);
                    return true;
                case MenuID.MI_NOT_FOUND:
                    addNewFieldNote(LogTypes.didnt_find);
                    return true;
                case MenuID.MI_MAINTANCE:
                    addNewFieldNote(LogTypes.needs_maintenance);
                    return true;
                case MenuID.MI_NOTE:
                    addNewFieldNote(LogTypes.note);
                    return true;
                case MenuID.MI_UPLOAD_FIELDNOTE:
                    CB_Action_UploadFieldNote.getInstance().Execute();
                    return true;
                case MenuID.MI_DELETE_ALL_FIELDNOTES:
                    deleteAllFieldNotes();
                    return true;
            }
            return false;
        });

        if (cache != null) {

            // Found je nach CacheType
            if (cache.Type == null)
                return null;
            switch (cache.Type) {
                case Giga:
                    cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", Sprites.getSprite("log8icon"));
                    cm.addItem(MenuID.MI_ATTENDED, "attended", Sprites.getSprite("log9icon"));
                    break;
                case MegaEvent:
                    cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", Sprites.getSprite("log8icon"));
                    cm.addItem(MenuID.MI_ATTENDED, "attended", Sprites.getSprite("log9icon"));
                    break;
                case Event:
                    cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", Sprites.getSprite("log8icon"));
                    cm.addItem(MenuID.MI_ATTENDED, "attended", Sprites.getSprite("log9icon"));
                    break;
                case CITO:
                    cm.addItem(MenuID.MI_WILL_ATTENDED, "will-attended", Sprites.getSprite("log8icon"));
                    cm.addItem(MenuID.MI_ATTENDED, "attended", Sprites.getSprite("log9icon"));
                    break;
                case Camera:
                    cm.addItem(MenuID.MI_WEBCAM_FOTO_TAKEN, "webCamFotoTaken", Sprites.getSprite("log10icon"));
                    break;
                default:
                    cm.addItem(MenuID.MI_FOUND, "found", Sprites.getSprite("log0icon"));
                    break;
            }

            cm.addItem(MenuID.MI_NOT_FOUND, "DNF", Sprites.getSprite("log1icon"));
        }

        // Aktueller Cache ist von geocaching.com dann weitere Menüeinträge freigeben
        if (cache != null && cache.getGcCode().toLowerCase().startsWith("gc")) {
            cm.addItem(MenuID.MI_MAINTANCE, "maintenance", Sprites.getSprite("log5icon"));
            cm.addItem(MenuID.MI_NOTE, "writenote", Sprites.getSprite("log2icon"));
        }

        cm.addItem(MenuID.MI_UPLOAD_FIELDNOTE, "uploadFieldNotes", Sprites.getSprite(IconName.UPLOADFIELDNOTE.name()));
        cm.addItem(MenuID.MI_DELETE_ALL_FIELDNOTES, "DeleteAllNotes", Sprites.getSprite(IconName.DELETE.name()));

        if (cache != null) {
            cm.addMoreMenu(getSecondMenu(), Translation.Get("defaultLogTypes"), Translation.Get("ownerLogTypes"));
        }

        return cm;

    }

    private Menu getSecondMenu() {
        Menu sm = new Menu("FieldNoteContextMenu/2");
        MenuItem mi;
        boolean IM_owner = GlobalCore.getSelectedCache().ImTheOwner();
        sm.addOnClickListener((v, x, y, pointer, button) -> {
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_ENABLED:
                    addNewFieldNote(LogTypes.enabled);
                    return true;
                case MenuID.MI_TEMPORARILY_DISABLED:
                    addNewFieldNote(LogTypes.temporarily_disabled);
                    return true;
                case MenuID.MI_OWNER_MAINTENANCE:
                    addNewFieldNote(LogTypes.owner_maintenance);
                    return true;
                case MenuID.MI_ATTENDED:
                    addNewFieldNote(LogTypes.attended);
                    return true;
                case MenuID.MI_WEBCAM_FOTO_TAKEN:
                    addNewFieldNote(LogTypes.webcam_photo_taken);
                    return true;
                case MenuID.MI_REVIEWER_NOTE:
                    addNewFieldNote(LogTypes.reviewer_note);
                    return true;
            }
            return false;
        });

        mi = sm.addItem(MenuID.MI_ENABLED, "enabled", Sprites.getSprite("log4icon"));
        mi.setEnabled(IM_owner);
        mi = sm.addItem(MenuID.MI_TEMPORARILY_DISABLED, "temporarilyDisabled", Sprites.getSprite("log6icon"));
        mi.setEnabled(IM_owner);
        mi = sm.addItem(MenuID.MI_OWNER_MAINTENANCE, "ownerMaintenance", Sprites.getSprite("log7icon"));
        mi.setEnabled(IM_owner);

        return sm;
    }

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
        // Database.Data.cacheList.GetCacheByGcCode(aktFieldNote.gcCode);

        Cache tmpCache = null;
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
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

        String message;
        if (aktFieldNote.isTbFieldNote) {
            message = Translation.Get("confirmFieldnoteDeletionTB", aktFieldNote.typeString, aktFieldNote.TbName);
        } else {
            message = Translation.Get("confirmFieldnoteDeletion", aktFieldNote.typeString, aktFieldNote.CacheName);
            if (aktFieldNote.type == LogTypes.found || aktFieldNote.type == LogTypes.attended || aktFieldNote.type == LogTypes.webcam_photo_taken)
                message += Translation.Get("confirmFieldnoteDeletionRst");
        }

        GL_MsgBox.Show(message, Translation.Get("deleteFieldnote"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, (which, data) -> {
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
                            synchronized (Data.cacheList) {
                                Cache tc = Data.cacheList.GetCacheById(cache.Id);
                                if (tc != null) {
                                    tc.setFound(false);
                                }
                            }
                        }
                    }
                    lFieldNotes.DeleteFieldNote(aktFieldNote);
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
        });

    }

    private void deleteAllFieldNotes() {
        final GL_MsgBox.OnMsgBoxClickListener dialogClickListener = (which, data) -> {
            switch (which) {
                case GL_MsgBox.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete all FieldNotes
                    // reload all Fieldnotes!
                    lFieldNotes.LoadFieldNotes("", LoadingType.Loadall);

                    for (FieldNoteEntry entry : lFieldNotes) {
                        entry.DeleteFromDatabase();

                    }

                    lFieldNotes.clear();
                    aktFieldNote = null;

                    that.setBaseAdapter(null);
                    lvAdapter = new CustomAdapter(lFieldNotes);
                    that.setBaseAdapter(lvAdapter);

                    // hint: geocache-visits is not deleted! comment : simply don't upload, if local drafts are deleted
                    break;

                case GL_MsgBox.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;

        };

        final String message = Translation.Get("DeleteAllFieldNotesQuestion");
        GL.that.RunOnGL(() -> GL_MsgBox.Show(message, Translation.Get("DeleteAllNotes"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener));

    }

    private void selectCacheFromFieldNote() {
        if (aktFieldNote == null)
            return;

        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
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

        synchronized (Data.cacheList) {
            cache = Data.cacheList.GetCacheByGcCode(aktFieldNote.gcCode);
        }

        if (cache == null) {
            Data.cacheList.add(tmpCache);
            cache = Data.cacheList.GetCacheByGcCode(aktFieldNote.gcCode);
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
        Log.debug(log, "FieldNotesView disposed");
    }

    public class CustomAdapter implements Adapter {

        private final CB_FixSizeList<FieldNoteViewItem> fixViewList = new CB_FixSizeList<>(20);
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

        @Override
        public ListViewItemBase getView(int position) {

            // check if the FieldNoteViewItem in the buffer list
            for (FieldNoteViewItem item : fixViewList) {
                if (item.getIndex() == position)
                    return item;
            }

            FieldNoteEntry fne = null;

            if (position < fieldNoteList.size()) {
                fne = fieldNoteList.get(position);
            }

            CB_RectF rec = ItemRec.copy().ScaleCenter(0.97f);
            rec.setHeight(MeasureItemHeight(fne));
            FieldNoteViewItem v = new FieldNoteViewItem(rec, position, fne);

            if (fne == null) {
                v.setOnClickListener((v14, x, y, pointer, button) -> {
                    // Load More
                    lFieldNotes.LoadFieldNotes("", LoadingType.loadMore);
                    FieldNotesView.this.notifyDataSetChanged();
                    return true;
                });
            } else {
                v.setOnClickListener((v12, x, y, pointer, button) -> {
                    int index = ((ListViewItemBase) v12).getIndex();
                    aktFieldNote = lFieldNotes.get(index);
                    editFieldNote();
                    return true;
                });
                v.setOnLongClickListener((v13, x, y, pointer, button) -> {
                    int index = ((ListViewItemBase) v13).getIndex();
                    aktFieldNote = lFieldNotes.get(index);
                    Menu cm = new Menu("FieldNotesContextMenu");
                    cm.addOnClickListener((v1, x1, y1, pointer1, button1) -> {
                        switch (((MenuItem) v1).getMenuItemId()) {
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
                    });
                    cm.addItem(MenuID.MI_SELECT_CACHE, "SelectCache");
                    cm.addItem(MenuID.MI_EDIT_FIELDNOTE, "edit");
                    cm.addItem(MenuID.MI_DELETE_FIELDNOTE, "delete");
                    cm.Show();
                    return true;
                });
            }

            // put item to buffer
            fixViewList.addAndGetLastOut(v);

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
}
