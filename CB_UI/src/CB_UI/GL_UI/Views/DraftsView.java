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
import CB_Core.Types.Drafts.LoadingType;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.EditDraft;
import CB_UI.GL_UI.Controls.PopUps.QuickDraftFeedbackPopUp;
import CB_UI.GL_UI.Main.Actions.Action_UploadDrafts;
import CB_UI.GL_UI.Main.ViewManager;
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
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
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

public class DraftsView extends V_ListView {
    private static final String log = "DraftsView";
    private static DraftsView that;
    private static Draft aktDraft;
    private static boolean firstShow = true;
    private static CB_RectF ItemRec;
    private static Drafts lDrafts;
    private static WaitDialog wd;
    private static EditDraft.IReturnListener returnListener = DraftsView::addOrChangeDraft;
    private static EditDraft editDraft;
    private CustomAdapter lvAdapter;

    private DraftsView() {
        super(ViewManager.leftTab.getContentRec(), "DraftsView");
        this.mCanDispose = false;
        this.setForceHandleTouchEvents(true);
        ItemRec = new CB_RectF(0, 0, this.getWidth(), UI_Size_Base.that.getButtonHeight() * 1.1f);

        setBackground(Sprites.ListBack);

        if (lDrafts == null)
            lDrafts = new Drafts();
        this.setHasInvisibleItems(true);
        this.setBaseAdapter(null);
        lvAdapter = new CustomAdapter(lDrafts);
        this.setBaseAdapter(lvAdapter);

        this.setEmptyMsg(Translation.get("EmptyDrafts"));
        firstShow = true;
    }

    public static DraftsView getInstance() {
        if (that == null) that = new DraftsView();
        return that;
    }

    private static void addNewDraft(LogTypes type) {
        addNewDraft(type, "", false);
    }

    public static void addNewDraft(LogTypes type, String templateText, boolean withoutShowEdit) {
        Cache cache = GlobalCore.getSelectedCache();

        if (cache == null) {
            MessageBox.show(Translation.get("NoCacheSelect"), Translation.get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
            return;
        }

        // chk car found?
        if (cache.getGcCode().equalsIgnoreCase("CBPark")) {

            if (type == LogTypes.found) {
                MessageBox.show(Translation.get("My_Parking_Area_Found"), Translation.get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else if (type == LogTypes.didnt_find) {
                MessageBox.show(Translation.get("My_Parking_Area_DNF"), Translation.get("thisNotWork"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
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
                    QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(true);
                    pop.show(PopUp_Base.SHOW_TIME_SHORT);
                    PlatformConnector.vibrate();
                }
            } else if (type == LogTypes.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    QuickDraftFeedbackPopUp pop2 = new QuickDraftFeedbackPopUp(false);
                    pop2.show(PopUp_Base.SHOW_TIME_SHORT);
                    PlatformConnector.vibrate();
                }
            }

            if (that != null)
                that.notifyDataSetChanged();
            return;
        }

        Drafts tmpDrafts = new Drafts();
        tmpDrafts.loadDrafts("", LoadingType.Loadall);

        Draft newDraft = null;
        if ((type == LogTypes.found) //
                || (type == LogTypes.attended) //
                || (type == LogTypes.webcam_photo_taken) //
                || (type == LogTypes.didnt_find)) {
            // Is there already a Draft of this type for this cache
            // then change else new
            for (Draft tmpDraft : tmpDrafts) {
                if ((tmpDraft.CacheId == cache.Id) && (tmpDraft.type == type)) {
                    newDraft = tmpDraft;
                    newDraft.DeleteFromDatabase();
                    newDraft.timestamp = new Date();
                    aktDraft = newDraft;
                }
            }
        }

        if (newDraft == null) {
            newDraft = new Draft(type);
            newDraft.CacheName = cache.getName();
            newDraft.gcCode = cache.getGcCode();
            newDraft.foundNumber = Config.FoundOffset.getValue();
            newDraft.timestamp = new Date();
            newDraft.CacheId = cache.Id;
            newDraft.comment = templateText;
            newDraft.CacheUrl = cache.getUrl();
            newDraft.cacheType = cache.Type.ordinal();
            newDraft.fillType();
            aktDraft = newDraft;
        } else {
            tmpDrafts.remove(newDraft);

        }

        switch (type) {
            case found:
                // wenn eine Draft Found erzeugt werden soll und der Cache noch nicht gefunden war -> foundNumber um 1 erhöhen
                if (!cache.isFound())
                    newDraft.foundNumber++;
                newDraft.fillType();
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newDraft);
                break;
            case attended:
                if (!cache.isFound())
                    newDraft.foundNumber++; //
                newDraft.fillType();
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.AttendedTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case webcam_photo_taken:
                if (!cache.isFound())
                    newDraft.foundNumber++; //
                newDraft.fillType();
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.WebcamTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case didnt_find:
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.DNFTemplate.getValue(), newDraft);
                break;
            case needs_maintenance:
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.NeedsMaintenanceTemplate.getValue(), newDraft);
                break;
            case note:
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), newDraft);
                break;
            default:
                break;
        }

        if (withoutShowEdit) {
            // neue Draft
            tmpDrafts.add(0, newDraft);
            newDraft.WriteToDatabase();
            aktDraft = newDraft;
            if (newDraft.type == LogTypes.found || newDraft.type == LogTypes.attended || newDraft.type == LogTypes.webcam_photo_taken) {
                // Found it! -> Cache als gefunden markieren
                if (!GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(true);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    Config.FoundOffset.setValue(aktDraft.foundNumber);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft DNF löschen
                tmpDrafts.DeleteDraftByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.didnt_find);
            } else if (newDraft.type == LogTypes.didnt_find) {
                // DidNotFound -> Cache als nicht gefunden markieren
                if (GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft FoundIt löschen
                tmpDrafts.DeleteDraftByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.found);
            }

            Drafts.CreateGeoCacheVisits(Config.DraftsGarminPath.getValue());

            if (that != null)
                that.notifyDataSetChanged();

        } else {
            editDraft = new EditDraft(newDraft, returnListener, true);
            editDraft.show();
        }
    }

    private static void addOrChangeDraft(Draft draft, boolean isNewDraft, boolean directLog) {

        if (directLog) {
            // try to direct upload
            logOnline(draft, isNewDraft);
            return;
        }

        DraftsView.firstShow = false;

        if (draft != null) {

            if (isNewDraft) {

                lDrafts.add(0, draft);

                // eine evtl. vorhandene Draft /DNF löschen
                if (draft.type == LogTypes.attended //
                        || draft.type == LogTypes.found //
                        || draft.type == LogTypes.webcam_photo_taken //
                        || draft.type == LogTypes.didnt_find) {
                    lDrafts.DeleteDraftByCacheId(draft.CacheId, LogTypes.found);
                    lDrafts.DeleteDraftByCacheId(draft.CacheId, LogTypes.didnt_find);
                }
            }

            draft.WriteToDatabase();
            aktDraft = draft;

            if (isNewDraft) {
                // nur, wenn eine Draft neu angelegt wurde
                // wenn eine Draft neu angelegt werden soll dann kann hier auf SelectedCache zugegriffen werden, da nur für den
                // SelectedCache eine Draft angelegt wird
                if (draft.type == LogTypes.found //
                        || draft.type == LogTypes.attended //
                        || draft.type == LogTypes.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
                    if (!GlobalCore.getSelectedCache().isFound()) {
                        GlobalCore.getSelectedCache().setFound(true);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                        Config.FoundOffset.setValue(aktDraft.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (draft.type == LogTypes.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (GlobalCore.getSelectedCache().isFound()) {
                        GlobalCore.getSelectedCache().setFound(false);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                        Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                        Config.AcceptChanges();
                    } // und eine evtl. vorhandene Draft FoundIt löschen
                    lDrafts.DeleteDraftByCacheId(GlobalCore.getSelectedCache().Id, LogTypes.found);
                }
            }
            Drafts.CreateGeoCacheVisits(Config.DraftsGarminPath.getValue());

            // Reload List
            if (isNewDraft) {
                lDrafts.loadDrafts("", LoadingType.LoadNew);
            } else {
                lDrafts.loadDrafts("", LoadingType.loadNewLastLength);
            }
        }
        that.notifyDataSetChanged();
    }

    private static void logOnline(final Draft draft, final boolean isNewDraft) {

        wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), () -> {

        }, new ICancelRunnable() {

            @Override
            public void run() {

                if (Config.GcVotePassword.getEncryptedValue().length() > 0 && !draft.isTbDraft) {
                    if (draft.gc_Vote > 0) {
                        // Stimme abgeben
                        try {
                            if (!GCVote.sendVote(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                                Log.err(log, draft.gcCode + " GC-Vote");
                            }
                        } catch (Exception e) {
                            Log.err(log, draft.gcCode + " GC-Vote");
                        }
                    }
                }

                if (GroundspeakAPI.OK == GroundspeakAPI.UploadDraftOrLog(draft.gcCode, draft.type.getGcLogTypeId(), draft.timestamp, draft.comment, draft.isDirectLog)) {
                    // after direct Log change state to uploaded
                    draft.uploaded = true;
                    addOrChangeDraft(draft, isNewDraft, false);
                } else {
                    // Error handling
                    MessageBox.show(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, (which, data) -> {
                        switch (which) {
                            case MessageBox.BUTTON_NEGATIVE:
                                addOrChangeDraft(draft, isNewDraft, true);// try again
                                break;
                            case MessageBox.BUTTON_NEUTRAL:
                                break;
                            case MessageBox.BUTTON_POSITIVE:
                                addOrChangeDraft(draft, isNewDraft, false);// create Draft
                        }
                        return true;
                    });
                }
                if (GroundspeakAPI.LastAPIError.length() > 0) {
                    GL.that.RunOnGL(() -> MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("Error"), MessageBoxIcon.Error));
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
        reloadDrafts();
        if (firstShow) {
            firstShow = false;

            GL.that.closeAllDialogs();

            if (Config.ShowDraftsContextMenuWithFirstShow.getValue())
                ViewManager.that.mToolsButtonOnLeftTabPerformClick();
        }

    }

    @Override
    public void onHide() {
        firstShow = true;
    }

    private void reloadDrafts() {
        if (lDrafts == null)
            lDrafts = new Drafts();
        lDrafts.loadDrafts("", LoadingType.loadNewLastLength);

        that.setBaseAdapter(null);
        lvAdapter = new CustomAdapter(lDrafts);
        that.setBaseAdapter(lvAdapter);
    }

    public Menu getContextMenu() {

        Cache cache = GlobalCore.getSelectedCache();

        final Menu cm = new Menu("DraftsContextMenuTitle");

        if (cache != null) {

            // Found je nach CacheType
            if (cache.Type == null)
                return null;
            switch (cache.Type) {
                case Event:
                case CITO:
                case MegaEvent:
                case Giga:
                    cm.addMenuItem("will-attended", Sprites.getSprite("log8icon"),()-> addNewDraft(LogTypes.will_attend));
                    cm.addMenuItem("attended", Sprites.getSprite("log9icon"),()->addNewDraft(LogTypes.attended));
                    break;
                case Camera:
                    cm.addMenuItem("webCamFotoTaken", Sprites.getSprite("log10icon"),()->addNewDraft(LogTypes.webcam_photo_taken));
                    break;
                default:
                    cm.addMenuItem("found", Sprites.getSprite("log0icon"),()->addNewDraft(LogTypes.found));
                    break;
            }

            cm.addMenuItem("DNF", Sprites.getSprite("log1icon"),()->addNewDraft(LogTypes.didnt_find));
        }

        // Aktueller Cache ist von geocaching.com dann weitere Menüeinträge freigeben
        if (cache != null && cache.getGcCode().toLowerCase().startsWith("gc")) {
            cm.addMenuItem("maintenance", Sprites.getSprite("log5icon"),()->addNewDraft(LogTypes.needs_maintenance));
            cm.addMenuItem("writenote", Sprites.getSprite("log2icon"),()->addNewDraft(LogTypes.note));
        }

        cm.addDivider();

        cm.addMenuItem("uploadDrafts", Sprites.getSprite(IconName.UPLOADFIELDNOTE.name()),()->Action_UploadDrafts.getInstance().Execute());
        cm.addMenuItem("DeleteAllDrafts", Sprites.getSprite(IconName.DELETE.name()), this::deleteAllDrafts);

        if (cache != null) {
            cm.addMoreMenu(getSecondMenu(), Translation.get("defaultLogTypes"), Translation.get("ownerLogTypes"));
        }

        return cm;

    }

    private Menu getSecondMenu() {
        Menu sm = new Menu("OwnerLogTypesTitle");
        MenuItem mi;
        boolean IM_owner = GlobalCore.getSelectedCache().ImTheOwner();
        mi = sm.addMenuItem( "enabled", Sprites.getSprite("log4icon"),()->addNewDraft(LogTypes.enabled));
        mi.setEnabled(IM_owner);
        mi = sm.addMenuItem("temporarilyDisabled", Sprites.getSprite("log6icon"),()->addNewDraft(LogTypes.temporarily_disabled));
        mi.setEnabled(IM_owner);
        mi = sm.addMenuItem("ownerMaintenance", Sprites.getSprite("log7icon"),()->addNewDraft(LogTypes.owner_maintenance));
        mi.setEnabled(IM_owner);
        // addNewDraft(LogTypes.attended);
        // addNewDraft(LogTypes.webcam_photo_taken);
        // addNewDraft(LogTypes.reviewer_note);
        return sm;
    }

    private void editDraft() {
        if (editDraft != null && !editDraft.isDisposed()) {
            editDraft.setDraft(aktDraft, returnListener, false);
        } else {
            editDraft = new EditDraft(aktDraft, returnListener, false);
        }

        editDraft.show();
    }

    private void deleteDraft() {
        // aktuell selectierte draft löschen
        if (aktDraft == null)
            return;
        // final Cache cache =
        // Database.Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);

        Cache tmpCache = null;
        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(lCaches, "Id = " + aktDraft.CacheId, false, false);
        if (lCaches.size() > 0)
            tmpCache = lCaches.get(0);
        final Cache cache = tmpCache;

        if (cache == null && !aktDraft.isTbDraft) {
            String message = Translation.get("cacheOtherDb", aktDraft.CacheName);
            message += "\n" + Translation.get("draftNoDelete");
            MessageBox.show(message);
            return;
        }

        String message;
        if (aktDraft.isTbDraft) {
            message = Translation.get("confirmDraftDeletionTB", aktDraft.typeString, aktDraft.TbName);
        } else {
            message = Translation.get("confirmDraftDeletion", aktDraft.typeString, aktDraft.CacheName);
            if (aktDraft.type == LogTypes.found || aktDraft.type == LogTypes.attended || aktDraft.type == LogTypes.webcam_photo_taken)
                message += Translation.get("confirmDraftDeletionRst");
        }

        MessageBox.show(message, Translation.get("deleteDraft"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, (which, data) -> {
            switch (which) {
                case MessageBox.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete aktDraft
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
                    lDrafts.deleteDraft(aktDraft);
                    aktDraft = null;

                    lDrafts.loadDrafts("", LoadingType.loadNewLastLength);

                    that.setBaseAdapter(null);
                    lvAdapter = new CustomAdapter(lDrafts);
                    that.setBaseAdapter(lvAdapter);

                    Drafts.CreateGeoCacheVisits(Config.DraftsGarminPath.getValue());

                    break;
                case MessageBox.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;
        });

    }

    private void deleteAllDrafts() {
        final MessageBox.OnMsgBoxClickListener dialogClickListener = (which, data) -> {
            switch (which) {
                case MessageBox.BUTTON_POSITIVE:
                    // Yes button clicked
                    // delete all Drafts
                    // reload all Drafts!
                    lDrafts.loadDrafts("", LoadingType.Loadall);

                    for (Draft entry : lDrafts) {
                        entry.DeleteFromDatabase();

                    }

                    lDrafts.clear();
                    aktDraft = null;

                    that.setBaseAdapter(null);
                    lvAdapter = new CustomAdapter(lDrafts);
                    that.setBaseAdapter(lvAdapter);

                    // hint: geocache-visits is not deleted! comment : simply don't upload, if local drafts are deleted
                    break;

                case MessageBox.BUTTON_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;

        };

        final String message = Translation.get("DelDrafts?");
        GL.that.RunOnGL(() -> MessageBox.show(message, Translation.get("DeleteAllDrafts"), MessageBoxButtons.YesNo, MessageBoxIcon.Warning, dialogClickListener));

    }

    private void selectCacheFromDraft() {
        if (aktDraft == null)
            return;

        // suche den Cache aus der DB.
        // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
        CacheList lCaches = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(lCaches, "Id = " + aktDraft.CacheId, false, false);
        Cache tmpCache = null;
        if (lCaches.size() > 0)
            tmpCache = lCaches.get(0);
        Cache cache = tmpCache;

        if (cache == null) {
            String message = Translation.get("cacheOtherDb", aktDraft.CacheName);
            message += "\n" + Translation.get("DraftNoSelect");
            MessageBox.show(message);
            return;
        }

        synchronized (Data.cacheList) {
            cache = Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);
        }

        if (cache == null) {
            Data.cacheList.add(tmpCache);
            cache = Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);
        }

        Waypoint finalWp = null;
        if (cache != null) {
            finalWp = cache.getCorrectedFinal();
            if (finalWp == null)
                finalWp = cache.getStartWaypoint();
            GlobalCore.setSelectedWaypoint(cache, finalWp);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        reloadDrafts();
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
        Log.debug(log, "DraftsView disposed");
    }

    public class CustomAdapter implements Adapter {

        private final CB_FixSizeList<DraftViewItem> fixViewList = new CB_FixSizeList<>(20);
        private Drafts drafts;

        public CustomAdapter(Drafts drafts) {
            this.drafts = drafts;
        }

        @Override
        public int getCount() {
            int count = drafts.size();
            if (drafts.isCropped())
                count++;
            return count;
        }

        @Override
        public ListViewItemBase getView(int position) {

            // check if the DraftViewItem in the buffer list
            for (DraftViewItem item : fixViewList) {
                if (item.getIndex() == position)
                    return item;
            }

            Draft fne = null;

            if (position < drafts.size()) {
                fne = drafts.get(position);
            }

            CB_RectF rec = ItemRec.copy().ScaleCenter(0.97f);
            rec.setHeight(MeasureItemHeight(fne));
            DraftViewItem v = new DraftViewItem(rec, position, fne);

            if (fne == null) {
                v.addClickHandler((v14, x, y, pointer, button) -> {
                    // Load More
                    lDrafts.loadDrafts("", LoadingType.loadMore);
                    DraftsView.this.notifyDataSetChanged();
                    return true;
                });
            } else {
                v.addClickHandler((v12, x, y, pointer, button) -> {
                    int index = ((ListViewItemBase) v12).getIndex();
                    aktDraft = lDrafts.get(index);
                    editDraft();
                    return true;
                });
                v.setOnLongClickListener((v13, x, y, pointer, button) -> {
                    int index = ((ListViewItemBase) v13).getIndex();
                    aktDraft = lDrafts.get(index);
                    Menu cm = new Menu("DraftItemMenuTitle");
                    cm.addMenuItem("SelectCache", null, () -> selectCacheFromDraft());
                    cm.addMenuItem( "edit",null,()-> editDraft());
                    cm.addMenuItem( "delete",null,()-> deleteDraft());
                    cm.show();
                    return true;
                });
            }

            // put item to buffer
            fixViewList.addAndGetLastOut(v);

            return v;
        }

        @Override
        public float getItemSize(int position) {
            if (position > drafts.size() || drafts.size() == 0)
                return 0;

            Draft fne = null;

            if (position < drafts.size()) {
                fne = drafts.get(position);
            }

            return MeasureItemHeight(fne);
        }

        private float MeasureItemHeight(Draft fne) {
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
            drafts = null;
        }
    }
}
