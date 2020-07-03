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
package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.*;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.GCVote;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.*;
import de.droidcachebox.database.Drafts.LoadingType;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditDraft;
import de.droidcachebox.gdx.activities.InputString;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.controls.popups.PopUp_Base;
import de.droidcachebox.gdx.controls.popups.QuickDraftFeedbackPopUp;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.UploadDrafts;
import de.droidcachebox.menu.menuBtn4.UploadLogs;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_FixSizeList;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.converter.Base64;
import de.droidcachebox.utils.http.WebbUtils;
import de.droidcachebox.utils.log.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.database.Database.Data;

public class DraftsView extends V_ListView {
    private static final String log = "DraftsView";
    private static DraftsView draftsView;
    private Draft currentDraft;
    private boolean firstShow;
    private Drafts drafts;
    private DraftsViewAdapter draftsViewAdapter;
    private WaitDialog wd;
    private EditDraft editDraft;

    private DraftsView() {
        super(ViewManager.leftTab.getContentRec(), "DraftsView");
        mCanDispose = false;
        setForceHandleTouchEvents();
        setBackground(Sprites.ListBack);
        drafts = new Drafts();
        setHasInvisibleItems();
        setAdapter(null);
        setEmptyMsgItem(Translation.get("EmptyDrafts"));
        firstShow = true;
    }

    public static DraftsView getInstance() {
        if (draftsView == null) draftsView = new DraftsView();
        return draftsView;
    }

    public static void createGeoCacheVisits() {
        Drafts drafts = new Drafts();
        drafts.loadDrafts("", "Timestamp ASC", LoadingType.Loadall);

        AbstractFile txtAbstractFile = FileFactory.createFile(Config.DraftsGarminPath.getValue());
        FileOutputStream writer;
        try {
            writer = txtAbstractFile.getFileOutputStream();

            // write utf8 bom EF BB BF
            byte[] bom = {(byte) 239, (byte) 187, (byte) 191};
            writer.write(bom);

            for (Draft draft : drafts) {
                SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                String sDate = datFormat.format(draft.timestamp) + "T";
                datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                sDate += datFormat.format(draft.timestamp) + "Z";
                String log = draft.gcCode + "," + sDate + "," + draft.type.toString() + ",\"" + draft.comment + "\"\n";
                writer.write((log + "\n").getBytes(StandardCharsets.UTF_8));
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.err(log, e.toString() + " at\n" + txtAbstractFile.getAbsolutePath());
            MessageBox.show(e.toString() + " at\n" + txtAbstractFile.getAbsolutePath(), Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
        }
    }

    private void addOrChangeDraft(Draft draft, boolean isNewDraft, EditDraft.SaveMode saveMode) {
        firstShow = false;
        if (draft != null) {
            if (isNewDraft) {
                drafts.insert(0, draft);

                // eine evtl. vorhandene Draft /DNF löschen
                if (draft.type == LogType.attended //
                        || draft.type == LogType.found //
                        || draft.type == LogType.webcam_photo_taken //
                        || draft.type == LogType.didnt_find) {
                    drafts.deleteDraftByCacheId(draft.CacheId, LogType.found);
                    drafts.deleteDraftByCacheId(draft.CacheId, LogType.didnt_find);
                }
            }

            draft.writeToDatabase();
            currentDraft = draft;

            if (isNewDraft) {
                // if (GlobalCore.getSelectedCache().generatedId == draft.CacheId) would be possible too
                // nur, wenn eine Draft neu angelegt wurde, ist sie bestimmt für den SelectedCache
                if (draft.type == LogType.found //
                        || draft.type == LogType.attended //
                        || draft.type == LogType.webcam_photo_taken) {
                    // Found it! -> Cache als gefunden markieren
                    if (!GlobalCore.getSelectedCache().isFound()) {
                        GlobalCore.getSelectedCache().setFound(true);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                        Config.FoundOffset.setValue(draft.foundNumber);
                        Config.AcceptChanges();
                    }

                } else if (draft.type == LogType.didnt_find) { // DidNotFound -> Cache als nicht gefunden markieren
                    if (GlobalCore.getSelectedCache().isFound()) {
                        GlobalCore.getSelectedCache().setFound(false);
                        CacheDAO cacheDAO = new CacheDAO();
                        cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                        Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                        Config.AcceptChanges();
                    } // und eine evtl. vorhandene Draft FoundIt löschen
                    drafts.deleteDraftByCacheId(GlobalCore.getSelectedCache().generatedId, LogType.found);
                }
            }

            createGeoCacheVisits();

            if (saveMode == EditDraft.SaveMode.Log)
                draftsViewAdapter.logOnline(currentDraft, true);
            else if (saveMode == EditDraft.SaveMode.Draft)
                draftsViewAdapter.logOnline(currentDraft, false);

            // Reload List
            if (isNewDraft) {
                drafts.loadDrafts("", LoadingType.LoadNew);
            } else {
                drafts.loadDrafts("", LoadingType.loadNewLastLength);
            }
        }

        draftsView.notifyDataSetChanged();
    }

    private void addNewDraft(LogType type) {
        addNewDraft(type, "", false);
    }

    public void addNewDraft(LogType type, String templateText, boolean withoutShowEdit) {
        Cache cache = GlobalCore.getSelectedCache();

        if (cache == null) {
            MessageBox.show(Translation.get("NoCacheSelect"), Translation.get("thisNotWork"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            return;
        }

        // chk car found?
        if (cache.getGeoCacheCode().equalsIgnoreCase("CBPark")) {

            if (type == LogType.found) {
                MessageBox.show(Translation.get("My_Parking_Area_Found"), Translation.get("thisNotWork"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            } else if (type == LogType.didnt_find) {
                MessageBox.show(Translation.get("My_Parking_Area_DNF"), Translation.get("thisNotWork"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            }

            return;
        }

        // kein GC Cache
        if (!cache.getGeoCacheCode().toLowerCase().startsWith("gc")) {

            if (type == LogType.found || type == LogType.attended || type == LogType.webcam_photo_taken) {
                // Found it! -> fremden Cache als gefunden markieren
                if (!GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(true);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    QuickDraftFeedbackPopUp pop = new QuickDraftFeedbackPopUp(true);
                    pop.show(PopUp_Base.SHOW_TIME_SHORT);
                    PlatformUIBase.vibrate();
                }
            } else if (type == LogType.didnt_find) {
                // DidNotFound -> fremden Cache als nicht gefunden markieren
                if (GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    QuickDraftFeedbackPopUp pop2 = new QuickDraftFeedbackPopUp(false);
                    pop2.show(PopUp_Base.SHOW_TIME_SHORT);
                    PlatformUIBase.vibrate();
                }
            }

            notifyDataSetChanged();

            return;
        }

        Drafts tmpDrafts = new Drafts();
        tmpDrafts.loadDrafts("", LoadingType.Loadall);

        Draft newDraft = null;
        if ((type == LogType.found) //
                || (type == LogType.attended) //
                || (type == LogType.webcam_photo_taken) //
                || (type == LogType.didnt_find)) {
            // Is there already a Draft of this type for this cache
            // then change else new
            for (Draft tmpDraft : tmpDrafts) {
                if ((tmpDraft.CacheId == cache.generatedId) && (tmpDraft.type == type)) {
                    newDraft = tmpDraft;
                    newDraft.deleteFromDatabase();
                    newDraft.timestamp = new Date();
                    currentDraft = newDraft;
                }
            }
        }

        if (newDraft == null) {
            newDraft = new Draft(type);
            newDraft.CacheName = cache.getGeoCacheName();
            newDraft.gcCode = cache.getGeoCacheCode();
            newDraft.foundNumber = Config.FoundOffset.getValue();
            newDraft.timestamp = new Date();
            newDraft.CacheId = cache.generatedId;
            newDraft.comment = templateText;
            newDraft.CacheUrl = cache.getUrl();
            newDraft.cacheType = cache.getGeoCacheType().ordinal();
            currentDraft = newDraft;
        } else {
            tmpDrafts.removeValue(newDraft, true);
        }

        switch (type) {
            case found:
                // wenn eine Draft Found erzeugt werden soll und der Cache noch nicht gefunden war -> foundNumber um 1 erhöhen
                if (!cache.isFound())
                    newDraft.foundNumber++;
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.FoundTemplate.getValue(), newDraft);
                break;
            case attended:
                if (!cache.isFound())
                    newDraft.foundNumber++; //
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.ReplaceTemplate(Config.AttendedTemplate.getValue(), newDraft);
                // wenn eine Draft Found erzeugt werden soll und der Cache noch
                // nicht gefunden war -> foundNumber um 1 erhöhen
                break;
            case webcam_photo_taken:
                if (!cache.isFound())
                    newDraft.foundNumber++; //
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
            tmpDrafts.insert(0, newDraft);
            newDraft.writeToDatabase();
            currentDraft = newDraft;
            if (newDraft.type == LogType.found || newDraft.type == LogType.attended || newDraft.type == LogType.webcam_photo_taken) {
                // Found it! -> Cache als gefunden markieren
                if (!GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(true);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    Config.FoundOffset.setValue(currentDraft.foundNumber);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft DNF löschen
                tmpDrafts.deleteDraftByCacheId(GlobalCore.getSelectedCache().generatedId, LogType.didnt_find);
            } else if (newDraft.type == LogType.didnt_find) {
                // DidNotFound -> Cache als nicht gefunden markieren
                if (GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(false);
                    CacheDAO cacheDAO = new CacheDAO();
                    cacheDAO.WriteToDatabase_Found(GlobalCore.getSelectedCache());
                    Config.FoundOffset.setValue(Config.FoundOffset.getValue() - 1);
                    Config.AcceptChanges();
                }
                // und eine evtl. vorhandene Draft FoundIt löschen
                tmpDrafts.deleteDraftByCacheId(GlobalCore.getSelectedCache().generatedId, LogType.found);
            }

            createGeoCacheVisits();

            if (draftsView != null)
                draftsView.notifyDataSetChanged();

        } else {
            editDraft = new EditDraft(newDraft, this::addOrChangeDraft, true);
            editDraft.show();

            CacheListChangedListeners.getInstance().cacheListChanged();
            CacheSelectionChangedListeners.getInstance().fireEvent(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWayPoint());

        }
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
        drafts.loadDrafts("", LoadingType.loadNewLastLength);
        setAdapter(null);
        draftsViewAdapter = new DraftsViewAdapter();
        setAdapter(draftsViewAdapter);
    }

    public Menu getContextMenu() {

        Cache cache = GlobalCore.getSelectedCache();

        final Menu cm = new Menu("DraftsContextMenuTitle");

        if (cache != null) {

            // Found je nach CacheType
            if (cache.getGeoCacheType() == null)
                return null;
            switch (cache.getGeoCacheType()) {
                case Event:
                case CITO:
                case MegaEvent:
                case Giga:
                    cm.addMenuItem("will-attended", Sprites.getSprite("log8icon"), () -> addNewDraft(LogType.will_attend));
                    cm.addMenuItem("attended", Sprites.getSprite("log9icon"), () -> addNewDraft(LogType.attended));
                    break;
                case Camera:
                    cm.addMenuItem("webCamFotoTaken", Sprites.getSprite("log10icon"), () -> addNewDraft(LogType.webcam_photo_taken));
                    break;
                default:
                    cm.addMenuItem("found", Sprites.getSprite("log0icon"), () -> addNewDraft(LogType.found));
                    break;
            }

            cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> addNewDraft(LogType.didnt_find));
        }

        // Aktueller Cache ist von geocaching.com dann weitere Menüeinträge freigeben
        if (cache != null && cache.getGeoCacheCode().toLowerCase().startsWith("gc")) {
            cm.addMenuItem("maintenance", Sprites.getSprite("log5icon"), () -> addNewDraft(LogType.needs_maintenance));
            cm.addMenuItem("writenote", Sprites.getSprite("log2icon"), () -> addNewDraft(LogType.note));
        }

        cm.addDivider();

        cm.addMenuItem("uploadDrafts", UploadDrafts.getInstance().getIcon(), () -> UploadDrafts.getInstance().execute());
        cm.addMenuItem("directLog", UploadLogs.getInstance().getIcon(), () -> UploadLogs.getInstance().execute());
        //
        cm.addMenuItem("DeleteAllDrafts", Sprites.getSprite(IconName.DELETE.name()), this::deleteAllDrafts);

        if (cache != null)
            if (cache.iAmTheOwner()) {
                Menu ownerLogTypesTitleMenu = new Menu("OwnerLogTypesTitle");
                ownerLogTypesTitleMenu.addMenuItem("enabled", Sprites.getSprite("log4icon"), () -> addNewDraft(LogType.enabled));
                ownerLogTypesTitleMenu.addMenuItem("temporarilyDisabled", Sprites.getSprite("log6icon"), () -> addNewDraft(LogType.temporarily_disabled));
                ownerLogTypesTitleMenu.addMenuItem("ownerMaintenance", Sprites.getSprite("log7icon"), () -> addNewDraft(LogType.owner_maintenance));
                cm.addMoreMenu(ownerLogTypesTitleMenu, Translation.get("defaultLogTypes"), Translation.get("ownerLogTypes"));
            }

        return cm;

    }

    private void deleteAllDrafts() {
        final MessageBox.OnMsgBoxClickListener dialogClickListener = (which, data) -> {
            switch (which) {
                case MessageBox.BTN_LEFT_POSITIVE:
                    // Yes button clicked
                    // delete all Drafts
                    // reload all Drafts!
                    drafts.loadDrafts("", LoadingType.Loadall);

                    for (Draft entry : drafts) {
                        entry.deleteFromDatabase();

                    }

                    drafts.clear();
                    currentDraft = null;

                    draftsView.setAdapter(null);
                    draftsViewAdapter = new DraftsViewAdapter();
                    draftsView.setAdapter(draftsViewAdapter);

                    // hint: geocache-visits is not deleted! comment : simply don't upload, if local drafts are deleted
                    break;

                case MessageBox.BTN_RIGHT_NEGATIVE:
                    // No button clicked
                    // do nothing
                    break;
            }
            return true;

        };

        final String message = Translation.get("DelDrafts?");
        MessageBox.show(message, Translation.get("DeleteAllDrafts"), MessageBoxButton.YesNo, MessageBoxIcon.Warning, dialogClickListener);

    }

    @Override
    public void notifyDataSetChanged() {
        reloadDrafts();
        super.notifyDataSetChanged();
    }

    @Override
    public void dispose() {
        setAdapter(null);
        if (draftsViewAdapter != null)
            draftsViewAdapter.dispose();
        draftsViewAdapter = null;
        draftsView = null;
        super.dispose();
        Log.debug(log, "DraftsView disposed");
    }

    private class DraftsViewAdapter implements Adapter {
        private final CB_FixSizeList<DraftViewItem> fixViewList = new CB_FixSizeList<>(20);
        private final CB_RectF itemRec;

        DraftsViewAdapter() {
            itemRec = new CB_RectF(0, 0, draftsView.getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f);
        }

        @Override
        public int getCount() {
            int count = drafts.size;
            if (drafts.isCropped())
                count++;
            return count;
        }

        @Override
        public ListViewItemBase getView(int position) {

            // check if the DraftViewItem is in the buffer list
            for (DraftViewItem item : fixViewList) {
                if (item.getIndex() == position)
                    return item;
            }

            Draft fne = null;
            if (position < drafts.size) {
                fne = drafts.get(position);
            }

            CB_RectF rec = new CB_RectF(itemRec);
            rec.scaleCenter(0.97f);
            rec.setHeight(MeasureItemHeight(fne));
            DraftViewItem v = new DraftViewItem(rec, position, fne);

            if (fne == null) {
                v.setClickHandler((v14, x, y, pointer, button) -> {
                    // Load More
                    drafts.loadDrafts("", LoadingType.loadMore);
                    notifyDataSetChanged();
                    return true;
                });
            } else {
                v.setClickHandler((v12, x, y, pointer, button) -> onItemClicked((DraftViewItem) v12));
                v.setOnLongClickListener((v13, x, y, pointer, button) -> onItemClicked((DraftViewItem) v13));
            }

            fixViewList.addAndGetLastOut(v);

            return v;
        }

        private boolean onItemClicked(DraftViewItem draftViewItem) {
            int index = draftViewItem.getIndex();
            currentDraft = drafts.get(index);
            draftViewItem.headerClicked = false;
            Menu cm = new Menu("DraftItemMenuTitle");
            cm.addMenuItem("edit", null, this::editDraft);
            if (currentDraft.GcId != null) {
                if (currentDraft.GcId.startsWith("GL")) {
                    cm.addMenuItem("uploadLogImage", Sprites.getSprite(IconName.imagesIcon.name()), this::uploadLogImage);
                    cm.addMenuItem("BrowseLog", null, () -> PlatformUIBase.callUrl("https://coord.info/" + currentDraft.GcId));
                }
            }
            cm.addMenuItem("uploadAsDraft", UploadDrafts.getInstance().getIcon(), () -> logOnline(currentDraft, false));
            cm.addMenuItem("uploadAsLog", UploadDrafts.getInstance().getIcon(), () -> logOnline(currentDraft, true));
            Sprite icon;
            if (currentDraft.isTbDraft) {
                // Sprite from url ?  draft.TbIconUrl
                icon = null;
            } else {
                icon = Sprites.getSprite("big" + GeoCacheType.values()[currentDraft.cacheType].name());
            }
            cm.addMenuItem("SelectCache", icon, this::selectCacheFromDraft);
            cm.addMenuItem("delete", Sprites.getSprite(IconName.DELETE.name()), this::deleteDraft);
            cm.show();
            return true;
        }

        private void editDraft() {
            if (editDraft != null && !editDraft.isDisposed()) {
                editDraft.setDraft(currentDraft, DraftsView.this::addOrChangeDraft, false);
            } else {
                editDraft = new EditDraft(currentDraft, DraftsView.this::addOrChangeDraft, false);
            }
            editDraft.show();
        }

        private void uploadLogImage() {
            String mPath = Config.ImageUploadLastUsedPath.getValue();
            if (mPath.length() == 0) {
                mPath = Config.workPath + "/User/Media/";
            }
            new FileOrFolderPicker(mPath, "*.jpg", Translation.get("SelectImage"), Translation.get("SelectImageButton"), abstractFile -> new InputString("imageDescription") {
                public void callBack(String description) {
                    GL.that.postAsync(() -> {
                        Config.ImageUploadLastUsedPath.setValue(abstractFile.getParent());
                        Config.AcceptChanges();
                        try {
                            String image = Base64.encodeBytes(WebbUtils.readBytes(abstractFile.getFileInputStream()));
                            GroundspeakAPI.uploadLogImage(currentDraft.GcId, image, description);
                            if (GroundspeakAPI.APIError == OK) {
                                MessageBox.show(Translation.get("ok") + ":\n", Translation.get("uploadLogImage"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                            } else {
                                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("uploadLogImage"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                            }
                        } catch (Exception ignored) {
                        }
                    });
                }
            }.show()).show();
        }

        private void logOnline(final Draft draft, final boolean directLog) {
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

                    if (OK == GroundspeakAPI.UploadDraftOrLog(draft.gcCode, draft.type.getGcLogTypeId(), draft.timestamp, draft.comment, directLog)) {
                        // after direct Log change state to uploaded
                        draft.uploaded = true;
                        if (directLog && !draft.isTbDraft) {
                            draft.GcId = GroundspeakAPI.logReferenceCode;
                            LogListView.getInstance().resetIsInitialized(); // if own log is written !
                        }
                        addOrChangeDraft(draft, false, EditDraft.SaveMode.LocalUpdate);
                    } else {
                        // Error handling
                        MessageBox.show(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MessageBoxButton.YesNoRetry, MessageBoxIcon.Question, (which, data) -> {
                            switch (which) {
                                case MessageBox.BTN_RIGHT_NEGATIVE:
                                    logOnline(draft, true);
                                    // addOrChangeDraft(draft, isNewDraft, true);// try again
                                    break;
                                case MessageBox.BTN_MIDDLE_NEUTRAL:
                                    break;
                                case MessageBox.BTN_LEFT_POSITIVE:
                                    // is alread in local database
                                    // addOrChangeDraft(draft, isNewDraft, false);// create Draft
                                    logOnline(draft, false); // or nothing
                            }
                            return true;
                        });
                    }
                    if (GroundspeakAPI.LastAPIError.length() > 0) {
                        MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("Error"), MessageBoxIcon.Error);
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

        private void selectCacheFromDraft() {
            if (currentDraft == null)
                return;

            // suche den Cache aus der DB.
            // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
            CacheList lCaches = CacheListDAO.getInstance().readCacheList("Id = " + currentDraft.CacheId, false, false, false);
            Cache tmpCache = null;
            if (lCaches.size() > 0)
                tmpCache = lCaches.get(0);
            Cache cache = tmpCache;

            if (cache == null) {
                String message = Translation.get("cacheOtherDb", currentDraft.CacheName);
                message += "\n" + Translation.get("DraftNoSelect");
                MessageBox.show(message);
                return;
            }

            synchronized (Data.cacheList) {
                cache = Data.cacheList.getCacheByGcCodeFromCacheList(currentDraft.gcCode);
                if (cache == null) {
                    Data.cacheList.add(tmpCache);
                    cache = Data.cacheList.getCacheByGcCodeFromCacheList(currentDraft.gcCode);
                }
            }

            Waypoint finalWp;
            if (cache != null) {
                finalWp = cache.getCorrectedFinal();
                if (finalWp == null)
                    finalWp = cache.getStartWaypoint();
                GlobalCore.setSelectedWaypoint(cache, finalWp);
            }
        }

        private void deleteDraft() {
            //  perhaps delete the uploaded draft or log
            // aktuell selectierte draft löschen
            if (currentDraft == null)
                return;
            // final Cache cache =
            // Database.Data.cacheList.GetCacheByGcCode(aktDraft.gcCode);

            Cache tmpCache = null;
            // suche den Cache aus der DB.
            // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
            CacheList lCaches = CacheListDAO.getInstance().readCacheList("Id = " + currentDraft.CacheId, false, false, false);
            if (lCaches.size() > 0)
                tmpCache = lCaches.get(0);
            final Cache cache = tmpCache;

            if (cache == null && !currentDraft.isTbDraft) {
                String message = Translation.get("cacheOtherDb", currentDraft.CacheName);
                message += "\n" + Translation.get("draftNoDelete");
                MessageBox.show(message);
                return;
            }

            String message;
            if (currentDraft.isTbDraft) {
                message = Translation.get("confirmDraftDeletionTB", currentDraft.getTypeString(), currentDraft.TbName);
            } else {
                message = Translation.get("confirmDraftDeletion", currentDraft.getTypeString(), currentDraft.CacheName);
                if (currentDraft.type == LogType.found || currentDraft.type == LogType.attended || currentDraft.type == LogType.webcam_photo_taken)
                    message += Translation.get("confirmDraftDeletionRst");
            }

            MessageBox.show(message, Translation.get("deleteDraft"), MessageBoxButton.YesNo, MessageBoxIcon.Question, (which, data) -> {
                if (which == MessageBox.BTN_LEFT_POSITIVE) {
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
                                Cache tc = Data.cacheList.getCacheByIdFromCacheList(cache.generatedId);
                                if (tc != null) {
                                    tc.setFound(false);
                                }
                            }
                        }
                    }
                    drafts.deleteDraftById(currentDraft);
                    currentDraft = null;
                    drafts.loadDrafts("", LoadingType.loadNewLastLength);
                    draftsView.setAdapter(null);
                    draftsViewAdapter = new DraftsViewAdapter();
                    draftsView.setAdapter(draftsViewAdapter);
                    createGeoCacheVisits();
                }
                return true;
            });

        }

        @Override
        public float getItemSize(int position) {
            if (position > drafts.size || drafts.size == 0)
                return 0;

            Draft fne = null;

            if (position < drafts.size) {
                fne = drafts.get(position);
            }

            return MeasureItemHeight(fne);
        }

        private float MeasureItemHeight(Draft fne) {
            float headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + (UiSizes.getInstance().getMargin());
            float cacheIfoHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + UiSizes.getInstance().getMargin() + Fonts.Measure("T").height;
            float mesurdWidth = itemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic() - (UiSizes.getInstance().getMargin() * 2);

            float mh = 0;
            if (fne != null) {
                try {
                    if (fne.comment != null && !(fne.comment.length() == 0)) {
                        mh = Fonts.measureWrapped(fne.comment, mesurdWidth).height;
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
            float commentHeight = (UiSizes.getInstance().getMargin() * 3) + mh;

            return headHeight + cacheIfoHeight + commentHeight;
        }

        private void dispose() {
            drafts = null;
        }
    }
}
