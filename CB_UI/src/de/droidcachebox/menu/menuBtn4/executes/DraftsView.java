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
package de.droidcachebox.menu.menuBtn4.executes;

import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_LEFT_POSITIVE;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_RIGHT_NEGATIVE;
import static de.droidcachebox.menu.Action.UploadDrafts;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.GCVote;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.CacheList;
import de.droidcachebox.dataclasses.Draft;
import de.droidcachebox.dataclasses.Drafts;
import de.droidcachebox.dataclasses.Drafts.LoadingType;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.InputString;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.popups.PopUpBase;
import de.droidcachebox.gdx.controls.popups.QuickDraftFeedbackPopUp;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.ShowLogs;
import de.droidcachebox.menu.menuBtn4.ShowDrafts;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_FixSizeList;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.converter.Base64;
import de.droidcachebox.utils.http.WebbUtils;
import de.droidcachebox.utils.log.Log;

public class DraftsView extends V_ListView {
    private static final String sClass = "DraftsView";
    private Draft currentDraft;
    private boolean firstShow;
    private Drafts drafts;
    private DraftsViewAdapter draftsViewAdapter;
    private EditDraft editDraft;

    public DraftsView() {
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

    public Draft getCurrentDraft() {
        return currentDraft;
    }

    public void afterEdit(Draft draft, boolean isNewDraft, EditDraft.SaveMode saveMode) {
        firstShow = false;
        if (draft != null) {
            if (isNewDraft) {
                drafts.insert(0, draft);
                // delete a perhaps existing Draft of that type
                if (draft.type == LogType.attended)
                    drafts.deleteDraftByCacheId(draft.CacheId, LogType.attended);
                if (draft.type == LogType.found)
                    drafts.deleteDraftByCacheId(draft.CacheId, LogType.found);
                if (draft.type == LogType.webcam_photo_taken)
                    drafts.deleteDraftByCacheId(draft.CacheId, LogType.webcam_photo_taken);
                if (draft.type == LogType.didnt_find)
                    drafts.deleteDraftByCacheId(draft.CacheId, LogType.didnt_find);
            }
            draft.writeToDatabase();
            currentDraft = draft;

            if (isNewDraft) {
                // if (GlobalCore.getSelectedCache().generatedId == draft.CacheId) would be possible too
                // only a new draft will surely be for the selected cache
                if (draft.type == LogType.found //
                        || draft.type == LogType.attended //
                        || draft.type == LogType.webcam_photo_taken) {
                    // Found it! -> mark Cache as found
                    if (!GlobalCore.getSelectedCache().isFound()) {
                        GlobalCore.getSelectedCache().setFound(true);
                        new CachesDAO().updateFound(GlobalCore.getSelectedCache());
                        Settings.foundOffset.setValue(draft.getFoundNumber());
                        Settings.getInstance().acceptChanges();
                    }

                } else if (draft.type == LogType.didnt_find) {
                    if (GlobalCore.getSelectedCache().isFound()) {
                        GlobalCore.getSelectedCache().setFound(false);
                        new CachesDAO().updateFound(GlobalCore.getSelectedCache());
                        Settings.foundOffset.setValue(Settings.foundOffset.getValue() - 1);
                        Settings.getInstance().acceptChanges();
                    } // and remove a previous found
                    drafts.deleteDraftByCacheId(GlobalCore.getSelectedCache().generatedId, LogType.found);
                }
            }

            if (saveMode == EditDraft.SaveMode.Log)
                draftsViewAdapter.uploadDraftOrLog(currentDraft, true);
            else if (saveMode == EditDraft.SaveMode.Draft)
                draftsViewAdapter.uploadDraftOrLog(currentDraft, false);

            createGeoCacheVisits();

            // Reload List
            // the nonotifyDataSetChanged will do the loadDrafts
            /*
            if (isNewDraft) {
                drafts.loadDrafts(LoadingType.LoadNew);
            } else {
                drafts.loadDrafts(LoadingType.LoadNewLastLength);
            }

             */

            notifyDataSetChanged();

            CacheListChangedListeners.getInstance().fire(sClass + " afterEdit");
            CacheSelectionChangedListeners.getInstance().fire(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWayPoint());
        }
    }

    @Override
    public void onShow() {
        drafts.addSettingsChangedHandler();
        reloadDrafts();
        if (firstShow) {
            firstShow = false;

            GL.that.closeAllDialogs();

            if (Settings.ShowDraftsContextMenuWithFirstShow.getValue())
                ViewManager.that.mToolsButtonOnLeftTabPerformClick();
        }

    }

    @Override
    public void onHide() {
        firstShow = true;
        drafts.removeSettingsChangedHandler();
        ((ShowDrafts) Action.ShowDrafts.action).viewIsHiding();
    }

    private void reloadDrafts() {
        drafts.loadDrafts(LoadingType.LoadNewLastLength);
        setAdapter(null);
        draftsViewAdapter = new DraftsViewAdapter();
        setAdapter(draftsViewAdapter);
    }

    /**
     * @param logType one of the GeoCache LogType possibilities
     * @param andEdit call edit of log (draft) after creation, but not if it is a Quick one
     */
    public void addNewDraft(LogType logType, boolean andEdit) {
        Cache cache = GlobalCore.getSelectedCache();
        if (cache == null) {
            new ButtonDialog(Translation.get("NoCacheSelect"), Translation.get("thisNotWork"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
            return;
        }
        // chk car found?
        if (cache.getGeoCacheCode().equalsIgnoreCase("CBPark")) {
            if (logType == LogType.found) {
                new ButtonDialog(Translation.get("My_Parking_Area_Found"), Translation.get("thisNotWork"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                // perhaps ask for deletion of parking entry
            } else if (logType == LogType.didnt_find) {
                // or simply ignore
                new ButtonDialog(Translation.get("My_Parking_Area_DNF"), Translation.get("thisNotWork"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
            }
            return;
        }
        // not at geocaching.com
        if (!cache.getGeoCacheCode().toLowerCase().startsWith("gc")) {
            // only update cache found column
            // there will be no entry in the local drafts
            // doesn't change find count (is only for geocaching.com)
            if (logType == LogType.found || logType == LogType.attended || logType == LogType.webcam_photo_taken) {
                if (!GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(true);
                    new CachesDAO().updateFound(GlobalCore.getSelectedCache());
                    afterAddDraft(true);
                }
            } else if (logType == LogType.didnt_find) {
                if (GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(false);
                    new CachesDAO().updateFound(GlobalCore.getSelectedCache());
                    afterAddDraft(false);
                }
            }
            return;
        }

        Drafts tmpDrafts = new Drafts();
        tmpDrafts.loadDrafts(Drafts.LoadingType.Loadall);

        Draft newDraft = null;
        if ((logType == LogType.found) //
                || (logType == LogType.attended) //
                || (logType == LogType.webcam_photo_taken) //
                || (logType == LogType.didnt_find)) {
            // Is there already a Draft of this type for this cache
            // then change else new
            for (Draft tmpDraft : tmpDrafts) {
                if ((tmpDraft.CacheId == cache.generatedId) && (tmpDraft.type == logType)) {
                    newDraft = tmpDraft;
                    newDraft.deleteFromDatabase();
                    newDraft.timestamp = new Date();
                    currentDraft = newDraft;
                }
            }
        }

        if (newDraft == null) {
            newDraft = new Draft(logType);
            newDraft.CacheName = cache.getGeoCacheName();
            newDraft.gcCode = cache.getGeoCacheCode();
            newDraft.setFoundNumber(Settings.foundOffset.getValue());
            newDraft.timestamp = new Date();
            newDraft.CacheId = cache.generatedId;
            newDraft.CacheUrl = cache.getUrl();
            newDraft.cacheType = cache.getGeoCacheType().ordinal();
            currentDraft = newDraft;
        } else {
            tmpDrafts.removeValue(newDraft, true);
        }

        switch (logType) {
            case found:
                // if a draft found is to be generated and the cache has not yet been found -> increase foundNumber by 1
                if (!cache.isFound())
                    newDraft.setFoundNumber(newDraft.getFoundNumber() + 1);
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.replaceTemplate(Settings.FoundTemplate.getValue(), newDraft);
                break;
            case attended:
                if (!cache.isFound())
                    newDraft.setFoundNumber(newDraft.getFoundNumber() + 1); //
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.replaceTemplate(Settings.AttendedTemplate.getValue(), newDraft);
                // if a draft found is to be generated and the cache has not yet been found -> increase foundNumber by 1
                break;
            case webcam_photo_taken:
                if (!cache.isFound())
                    newDraft.setFoundNumber(newDraft.getFoundNumber() + 1); //
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.replaceTemplate(Settings.WebcamTemplate.getValue(), newDraft);
                // if a draft found is to be generated and the cache has not yet been found -> increase foundNumber by 1
                break;
            case didnt_find:
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.replaceTemplate(Settings.DNFTemplate.getValue(), newDraft);
                break;
            case needs_maintenance:
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.replaceTemplate(Settings.NeedsMaintenanceTemplate.getValue(), newDraft);
                break;
            case note:
                if (newDraft.comment.length() == 0)
                    newDraft.comment = TemplateFormatter.replaceTemplate(Settings.AddNoteTemplate.getValue(), newDraft);
                break;
            default:
                break;
        }

        if (andEdit) {
            EditDraft editDraft = new EditDraft(newDraft, this::afterEdit, true);
            editDraft.show();
        } else {
            newDraft.writeToDatabase();
            currentDraft = newDraft;
            if (newDraft.type == LogType.found || newDraft.type == LogType.attended || newDraft.type == LogType.webcam_photo_taken) {
                if (!GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(true);
                    new CachesDAO().updateFound(GlobalCore.getSelectedCache());
                    Settings.foundOffset.setValue(getCurrentDraft().getFoundNumber());
                    Settings.getInstance().acceptChanges();
                }
                // and delete any existing Draft DNF
                tmpDrafts.deleteDraftByCacheId(GlobalCore.getSelectedCache().generatedId, LogType.didnt_find);
                afterAddDraft(true);
            } else if (newDraft.type == LogType.didnt_find) {
                if (GlobalCore.getSelectedCache().isFound()) {
                    GlobalCore.getSelectedCache().setFound(false);
                    new CachesDAO().updateFound(GlobalCore.getSelectedCache());
                    Settings.foundOffset.setValue(Settings.foundOffset.getValue() - 1);
                    Settings.getInstance().acceptChanges();
                }
                // and delete any existing Draft FoundIt
                tmpDrafts.deleteDraftByCacheId(GlobalCore.getSelectedCache().generatedId, LogType.found);
                afterAddDraft(false);
            }
        }
    }

    private void afterAddDraft(boolean found) {
        createGeoCacheVisits();
        notifyDataSetChanged(); // perhaps only when visible
        // for status change, for icons in map
        CacheListChangedListeners.getInstance().fire(sClass + " afterAddDraft");
        CacheSelectionChangedListeners.getInstance().fire(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWayPoint());
        new QuickDraftFeedbackPopUp(found).show(PopUpBase.SHOW_TIME_SHORT);
        Platform.vibrate();
    }

    public void deleteAllDrafts() {
        ButtonDialog bd = new ButtonDialog(Translation.get("DelDrafts?"), Translation.get("DeleteAllDrafts"), MsgBoxButton.YesNo, MsgBoxIcon.Warning);
        bd.setButtonClickHandler((which, data) -> {
            if (which == BTN_LEFT_POSITIVE) {
                Drafts drafts = new Drafts();

                // delete all Drafts, reload all Drafts!
                drafts.loadDrafts(Drafts.LoadingType.Loadall);
                for (Draft entry : drafts) {
                    entry.deleteFromDatabase();
                }
                // hint: geocache-visits is not deleted! comment : simply don't upload, if local drafts are deleted
                notifyDataSetChanged();
            }
            return true;
        });
        bd.show();
    }

    public void createGeoCacheVisits() {
        Drafts tmpDrafts = new Drafts();
        tmpDrafts.loadDrafts(Drafts.LoadingType.LoadAscending);

        AbstractFile txtAbstractFile = FileFactory.createFile(Settings.DraftsGarminPath.getValue());
        FileOutputStream writer;
        try {
            writer = txtAbstractFile.getFileOutputStream();

            // write utf8 bom EF BB BF
            byte[] bom = {(byte) 239, (byte) 187, (byte) 191};
            writer.write(bom);

            for (Draft draft : tmpDrafts) {
                if (!draft.isUploaded) {
                    SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    String sDate = datFormat.format(draft.timestamp) + "T";
                    datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    sDate += datFormat.format(draft.timestamp) + "Z";
                    String log = draft.gcCode + "," + sDate + "," + draft.type.toString() + ",\"" + draft.comment + "\"\n";
                    writer.write((log + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.err("createGeoCacheVisits", e + " at\n" + txtAbstractFile.getAbsolutePath());
            new ButtonDialog(e + " at\n" + txtAbstractFile.getAbsolutePath(), Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        reloadDrafts();
        super.notifyDataSetChanged();
    }

    private class DraftsViewAdapter implements Adapter {
        private final CB_FixSizeList<DraftViewItem> fixViewList = new CB_FixSizeList<>(20);
        private final CB_RectF itemRec;

        DraftsViewAdapter() {
            itemRec = new CB_RectF(0, 0, getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f);
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
                    drafts.loadDrafts(LoadingType.LoadMore);
                    notifyDataSetChanged();
                    return true;
                });
            } else {
                v.setClickHandler((v12, x, y, pointer, button) -> onItemClicked((DraftViewItem) v12));
                v.setLongClickHandler((v13, x, y, pointer, button) -> onItemClicked((DraftViewItem) v13));
            }

            fixViewList.addAndGetLastOut(v);

            return v;
        }

        private boolean onItemClicked(DraftViewItem draftViewItem) {
            Menu cm = new Menu("DraftItemMenuTitle");
            currentDraft = drafts.get(draftViewItem.getIndex());
            draftViewItem.headerClicked = false;
            cm.addMenuItem("edit", null, this::editDraft);
            if (currentDraft.gcLogReference != null) {
                if (currentDraft.gcLogReference.startsWith("GL")) {
                    cm.addMenuItem("uploadLogImage", Sprites.getSprite(IconName.imagesIcon.name()), this::uploadLogImage);
                    cm.addMenuItem("BrowseLog", null, () -> Platform.callUrl("https://coord.info/" + currentDraft.gcLogReference));
                }
            }
            if (!isAccessTokenInvalid()) {
                cm.addMenuItem("uploadAsDraft", UploadDrafts.action.getIcon(), () -> uploadDraftOrLog(currentDraft, false));
                cm.addMenuItem("uploadAsLog", UploadDrafts.action.getIcon(), () -> uploadDraftOrLog(currentDraft, true));
            }
            cm.addMenuItem("setUploaded", null, () -> setDraftUploaded(currentDraft));
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
            if (editDraft == null || editDraft.isDisposed()) {
                editDraft = new EditDraft(currentDraft, DraftsView.this::afterEdit, false);
            } else {
                editDraft.setDraft(currentDraft, DraftsView.this::afterEdit, false);
            }
            editDraft.show();
        }
        private void setDraftUploaded(final Draft draft) {
            if (!draft.isUploaded) {
                draft.isUploaded = true;
            } else {
                draft.isUploaded = false;
            }
            draft.updateDatabase();
            createGeoCacheVisits();
        }

        private void uploadLogImage() {
            String mPath = Settings.ImageUploadLastUsedPath.getValue();
            if (mPath.length() == 0) {
                mPath = GlobalCore.workPath + "/User/Media/";
            }
            new FileOrFolderPicker(mPath, "*.jpg", Translation.get("SelectImage"), Translation.get("SelectImageButton"), abstractFile -> new InputString("imageDescription") {
                public void callBack(String description) {
                    GL.that.postAsync(() -> {
                        Settings.ImageUploadLastUsedPath.setValue(abstractFile.getParent());
                        Settings.getInstance().acceptChanges();
                        try {
                            String image = Base64.encodeBytes(WebbUtils.readBytes(abstractFile.getFileInputStream()));
                            GroundspeakAPI.uploadLogImage(currentDraft.gcLogReference, image, description);
                            if (GroundspeakAPI.APIError == OK) {
                                new ButtonDialog(Translation.get("ok") + ":\n", Translation.get("uploadLogImage"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                            } else {
                                new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get("uploadLogImage"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                            }
                        } catch (Exception ignored) {
                        }
                    });
                }
            }.show()).show();
        }

        private void uploadDraftOrLog(final Draft draft, final boolean isLog) {
            final int[] result = {OK};
            AtomicBoolean isCanceled = new AtomicBoolean(false);
            new CancelWaitDialog("Upload Log", new DownloadAnimation(), new RunAndReady() {
                @Override
                public void ready() {

                    if (result[0] == OK) {
                        // after direct Log change state to uploaded
                        draft.isUploaded = true;
                        if (isLog && !draft.isTbDraft) {
                            draft.gcLogReference = GroundspeakAPI.logReferenceCode;
                            ((ShowLogs)Action.ShowLogs.action).resetRenderInitDone();// if own log is written !
                        }
                        afterEdit(draft, false, EditDraft.SaveMode.LocalUpdate);
                    } else {
                        ButtonDialog bd = new ButtonDialog(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MsgBoxButton.YesNoRetry, MsgBoxIcon.Question);
                        bd.setButtonClickHandler((which, data) -> {
                            switch (which) {
                                case BTN_RIGHT_NEGATIVE:
                                    uploadDraftOrLog(draft, true);
                                    // addOrChangeDraft(draft, isNewDraft, true);// try again
                                    break;
                                case ButtonDialog.BTN_MIDDLE_NEUTRAL:
                                    break;
                                case BTN_LEFT_POSITIVE:
                                    // is alread in local database
                                    // addOrChangeDraft(draft, isNewDraft, false);// create Draft
                                    uploadDraftOrLog(draft, false); // or nothing
                            }
                            return true;
                        });
                        bd.show();

                        // todo 2x MsgBox geht nicht ?
                        /*
                        if (GroundspeakAPI.LastAPIError.length() > 0) {
                            new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get("Error"), MsgBoxIcon.Error, (which, data) -> {
                            }).show();
                        }

                         */
                    }
                }

                @Override
                public void run() {
                    if (Settings.GcVotePassword.getEncryptedValue().length() > 0 && !draft.isTbDraft) {
                        if (draft.gc_Vote > 0) {
                            // Stimme abgeben
                            try {
                                if (!GCVote.sendVote(Settings.GcLogin.getValue(), Settings.GcVotePassword.getValue(), draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                                    Log.err(sClass, draft.gcCode + " GC-Vote");
                                }
                            } catch (Exception e) {
                                Log.err(sClass, draft.gcCode + " GC-Vote");
                            }
                        }
                    }
                    result[0] = GroundspeakAPI.uploadDraftOrLog(draft, isLog);
                }

                @Override
                public void setIsCanceled() {
                    isCanceled.set(true);
                }

            }).show();
        }

        private void selectCacheFromDraft() {
            if (currentDraft == null)
                return;

            // suche den Cache aus der DB.
            // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
            CacheList lCaches = new CacheList();
            new CachesDAO().readCacheList(lCaches, "Id = " + currentDraft.CacheId, false, false, false);
            Cache tmpCache = null;
            if (lCaches.size() > 0)
                tmpCache = lCaches.get(0);
            Cache cache = tmpCache;

            if (cache == null) {
                String message = Translation.get("cacheOtherDb", currentDraft.CacheName);
                message += "\n" + Translation.get("DraftNoSelect");
                new ButtonDialog(message, "", MsgBoxButton.OK, null).show();
                return;
            }

            synchronized (CBDB.cacheList) {
                cache = CBDB.cacheList.getCacheByGcCodeFromCacheList(currentDraft.gcCode);
                if (cache == null) {
                    CBDB.cacheList.add(tmpCache);
                    cache = CBDB.cacheList.getCacheByGcCodeFromCacheList(currentDraft.gcCode);
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

            Cache tmpCache = null;
            // suche den Cache aus der DB.
            // Nicht aus der aktuellen cacheList, da dieser herausgefiltert sein könnte
            CacheList lCaches = new CacheList();
            new CachesDAO().readCacheList(lCaches, "Id = " + currentDraft.CacheId, false, false, false);
            if (lCaches.size() > 0)
                tmpCache = lCaches.get(0);
            final Cache cache = tmpCache;

            if (cache == null && !currentDraft.isTbDraft) {
                String message = Translation.get("cacheOtherDb", currentDraft.CacheName);
                message += "\n" + Translation.get("draftNoDelete");
                new ButtonDialog(message, "", MsgBoxButton.OK, null).show();
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

            ButtonDialog bd = new ButtonDialog(message, Translation.get("deleteDraft"), MsgBoxButton.YesNo, MsgBoxIcon.Question);
            bd.setButtonClickHandler((which, data) -> {
                if (which == BTN_LEFT_POSITIVE) {
                    if (cache != null) {
                        if (cache.isFound()) {
                            cache.setFound(false);
                            new CachesDAO().updateFound(cache);
                            Settings.foundOffset.setValue(Settings.foundOffset.getValue() - 1);
                            Settings.getInstance().acceptChanges();
                            // jetzt noch diesen Cache in der aktuellen CacheListe suchen und auch da den Found-Status zurücksetzen
                            // damit das Smiley Symbol aus der Map und der CacheList verschwindet
                            synchronized (CBDB.cacheList) {
                                Cache tc = CBDB.cacheList.getCacheByIdFromCacheList(cache.generatedId);
                                if (tc != null) {
                                    tc.setFound(false);
                                }
                            }
                        }
                    }
                    drafts.deleteDraftById(currentDraft);
                    currentDraft = null;
                    drafts.loadDrafts(LoadingType.LoadNewLastLength);
                    setAdapter(null);
                    draftsViewAdapter = new DraftsViewAdapter();
                    setAdapter(draftsViewAdapter);
                    createGeoCacheVisits();
                }
                return true;
            });
            bd.show();
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
            float cacheIfoHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + UiSizes.getInstance().getMargin() + Fonts.measure("T").height;
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
