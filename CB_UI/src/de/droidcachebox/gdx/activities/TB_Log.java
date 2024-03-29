/*
 * Copyright (C) 2014-2015 team-cachebox.de
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
package de.droidcachebox.gdx.activities;

import static de.droidcachebox.core.GroundspeakAPI.ERROR;
import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.uploadTrackableLog;
import static de.droidcachebox.settings.Config_Core.br;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Draft;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.dataclasses.Trackable;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.RadioButton;
import de.droidcachebox.gdx.controls.RadioGroup;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.menuBtn4.executes.TemplateFormatter;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

public class TB_Log extends ActivityBase {
    private Trackable trackable;
    private CB_Button btnClose;
    private ImageButton btnAction;
    private Image icon, CacheIcon;
    private CB_Label lblPlaced;
    private EditTextField lblName;
    private Box contentBox;
    private LogType logType;
    private EditTextField edit;
    private RadioButton rbDirectLog;
    private RadioButton rbOnlyDraft;

    TB_Log() {
        super("TB_Log_Activity");
        createControls();
    }

    public void show(Trackable trackable, LogType logType) {
        this.trackable = trackable;
        this.logType = logType;
        layout();
        show();
    }

    private void createControls() {

        btnClose = new CB_Button("Close");
        btnClose.setText(Translation.get("close"));
        btnClose.setClickHandler((v, x, y, pointer, button) -> {
            TB_Log.this.finish();
            return true;
        });

        btnAction = new ImageButton();
        btnAction.setClickHandler((v, x, y, pointer, button) -> {
            LogNow();
            return true;
        });

        contentBox = new Box(this, "ContentBox");
        contentBox.setHeight(this.getHeight() - (btnClose.getHeight() - margin) * 2.5f);
        contentBox.setBackground(Sprites.activityBackground);

        CB_RectF iconRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight());
        iconRec = iconRec.scaleCenter(0.8f);

        icon = new Image(iconRec, "Icon", false);
        lblName = new EditTextField(iconRec, this, "lblName");

        CacheIcon = new Image(iconRec, "CacheIcon", false);
        lblPlaced = new CB_Label(iconRec);

        edit = new EditTextField(this, "edit").setWrapType(WrapType.WRAPPED);
        edit.setWidth(contentBox.getInnerWidth());
        edit.setHeight(contentBox.getHalfHeight());

        rbDirectLog = new RadioButton();
        rbOnlyDraft = new RadioButton();

        rbDirectLog.setText(Translation.get("directLog"));
        rbOnlyDraft.setText(Translation.get("onlyDraft"));

        RadioGroup Group = new RadioGroup();
        Group.add(rbOnlyDraft);
        Group.add(rbDirectLog);
        if (Settings.TB_DirectLog.getValue()) {
            rbDirectLog.setChecked(true);
        } else {
            rbOnlyDraft.setChecked(true);
        }
    }

    private void layout() {
        this.removeChildren();
        this.initRow(BOTTOMUp);
        this.addNext(btnAction);
        this.addLast(btnClose);
        this.addLast(contentBox);
        contentBox.initRow(TOPDown, contentBox.getHeight());
        contentBox.setNoBorders();
        contentBox.setMargins(0, 0);
        contentBox.addLast(edit);
        contentBox.addLast(rbDirectLog);
        contentBox.addLast(rbOnlyDraft);

        // Show Selected Cache for LogType discovered/visited/dropped_off/retrieve
        if (logType == LogType.discovered || logType == LogType.visited || logType == LogType.dropped_off || logType == LogType.retrieve) {

            Cache c = GlobalCore.getSelectedCache();
            if (c == null) {
                // no log without geocache possible
                ButtonDialog bd = new ButtonDialog(Translation.get("NoCacheSelect"), Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error);
                bd.setButtonClickHandler((btnNumber, data) -> {
                    TB_Log.this.finish();
                    return true;
                });
                bd.show();
                return;
            }

            String msg = "";
            if (logType == LogType.discovered) {
                msg = Translation.get("discoveredAt") + ": " + br + c.getGeoCacheName();
            }
            if (logType == LogType.visited) {
                msg = Translation.get("visitedAt") + ": " + br + c.getGeoCacheName();
            }
            if (logType == LogType.dropped_off) {
                msg = Translation.get("dropped_offAt") + ": " + br + c.getGeoCacheName();
            }
            if (logType == LogType.retrieve) {
                msg = Translation.get("retrieveAt") + ": " + br + c.getGeoCacheName();
            }

            CacheIcon.setSprite(Sprites.getSprite("big" + c.getGeoCacheType().name()));

            lblPlaced.setWidth(contentBox.getInnerWidth() - CacheIcon.getWidth() - (margin * 3));
            lblPlaced.setWrappedText(msg);
            lblPlaced.setHeight(lblPlaced.getTextHeight() + lblPlaced.getTopHeight() + lblPlaced.getBottomHeight());

            contentBox.setMargins(margin, margin * 3);
            contentBox.setRowYPos(contentBox.getRowYPos() - (margin * 3));

            contentBox.addNext(CacheIcon, FIXED);
            contentBox.addLast(lblPlaced);

        }

        this.setMargins(margin * 2, 0);
        this.addNext(icon, FIXED);
        icon.setImageURL(trackable.getIconUrl());
        lblName.setWrapType(WrapType.WRAPPED);
        lblName.setBackground(null, null);
        this.addLast(lblName);
        lblName.setText(trackable.getName());
        lblName.setEditable(false);
        lblName.showFromLineNo(0);
        lblName.setCursorPosition(0);

        switch (this.logType) {
            case discovered:
                btnAction.setImage(Sprites.getSprite(IconName.TBDISCOVER.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.DiscoverdTemplate.getValue(), trackable));
                break;
            case visited:
                btnAction.setImage(Sprites.getSprite(IconName.TBVISIT.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.VisitedTemplate.getValue(), trackable));
                break;
            case dropped_off:
                btnAction.setImage(Sprites.getSprite(IconName.TBDROP.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.DroppedTemplate.getValue(), trackable));
                break;
            case grab_it:
                btnAction.setImage(Sprites.getSprite(IconName.TBGRAB.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.GrabbedTemplate.getValue(), trackable));
                break;
            case retrieve:
                btnAction.setImage(Sprites.getSprite(IconName.TBPICKED.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.PickedTemplate.getValue(), trackable));
                break;
            case note:
                btnAction.setImage(Sprites.getSprite(IconName.TBNOTE.name()));
                edit.setText("");
                break;
            default:
                break;
        }
    }

    private void LogNow() {
        if (rbDirectLog.isChecked())
            logOnline();
        else
            createTBDraft();

    }

    private void logOnline() {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        final int[] result = {OK};
        CancelWaitDialog wd = new CancelWaitDialog("Upload Log", new DownloadAnimation(), new RunAndReady() {
            @Override
            public void ready() {

                if (result[0] == ERROR) {
                    GL.that.toast(LastAPIError);
                    ButtonDialog bd = new ButtonDialog(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MsgBoxButton.YesNoRetry, MsgBoxIcon.Question);
                    bd.setButtonClickHandler((which, data) -> {
                        switch (which) {
                            case ButtonDialog.BTN_RIGHT_NEGATIVE:
                                logOnline();
                                return true;

                            case ButtonDialog.BTN_MIDDLE_NEUTRAL:
                                return true;

                            case ButtonDialog.BTN_LEFT_POSITIVE:
                                createTBDraft();
                                return true;
                        }
                        return true;
                    });
                    bd.show();
                    return;
                }

                if (result[0] != OK) {
                    GL.that.toast(LastAPIError);
                    ButtonDialog bd = new ButtonDialog(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MsgBoxButton.YesNoRetry, MsgBoxIcon.Question);
                    bd.setButtonClickHandler((which, data) -> {
                        switch (which) {
                            case ButtonDialog.BTN_RIGHT_NEGATIVE:
                                logOnline();
                                return true;

                            case ButtonDialog.BTN_MIDDLE_NEUTRAL:
                                return true;

                            case ButtonDialog.BTN_LEFT_POSITIVE:
                                createTBDraft();
                                return true;
                        }
                        return true;
                    });
                    bd.show();
                    return;
                }

                if (LastAPIError.length() > 0) {
                    new ButtonDialog(LastAPIError, Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
                }

                TB_Log.this.finish();

                // Refresh TB List after Dropped Off or Picked or Grabed
                if (logType == LogType.dropped_off || logType == LogType.retrieve || logType == LogType.grab_it) {
                    // GL.that.runOnGL(() -> Trackables.trackables.refreshTbList());
                }
            }

            @Override
            public void run() {
                result[0] = uploadTrackableLog(trackable, getCache_GcCode(), logType.gsLogTypeId, new Date(), edit.getText());
            }

            @Override
            public void setIsCanceled() {
                isCanceled.set(true);
            }

        });
        wd.show();
    }

    private void createTBDraft() {
        Draft newFieldNote;
        newFieldNote = new Draft(logType);
        newFieldNote.CacheName = getCache_Name();
        newFieldNote.gcCode = getCache_GcCode();
        newFieldNote.setFoundNumber(Settings.foundOffset.getValue());
        newFieldNote.timestamp = new Date();
        newFieldNote.CacheId = getCache_ID();
        newFieldNote.comment = edit.getText();
        newFieldNote.CacheUrl = getCache_URL();
        newFieldNote.cacheType = getCache_Type();
        newFieldNote.isTbDraft = true;
        newFieldNote.TbName = trackable.getName();
        newFieldNote.TbIconUrl = trackable.getIconUrl();
        newFieldNote.TravelBugCode = trackable.getTbCode();
        newFieldNote.TrackingNumber = trackable.getTrackingCode();
        newFieldNote.writeToDatabase();

        TB_Log.this.finish();
    }

    private String getCache_GcCode() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (trackable.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(trackable.getCurrentGeoCacheCode()) && trackable.getCurrentGeoCacheCode().length() > 0) {
                if (logType == LogType.visited || logType == LogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return trackable.getCurrentGeoCacheCode();
                }
            }
        }
        return (logType == LogType.dropped_off || logType == LogType.visited || logType == LogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheCode() : "";
    }

    private String getCache_Name() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (trackable.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(trackable.getCurrentGeoCacheCode()) && trackable.getCurrentGeoCacheCode().length() > 0) {
                if (logType == LogType.visited || logType == LogType.retrieve) {
                    // TB is perhaps not in the selected cache, but don't want to change selected Cache
                    return trackable.getCurrentGeoCacheCode();
                }
            }
        }
        return (logType == LogType.dropped_off || logType == LogType.visited || logType == LogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheName() : "";
    }

    private long getCache_ID() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (trackable.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(trackable.getCurrentGeoCacheCode()) && trackable.getCurrentGeoCacheCode().length() > 0) {
                if (logType == LogType.visited || logType == LogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return Cache.generateCacheId(trackable.getCurrentGeoCacheCode());
                }
            }
        }
        return (logType == LogType.dropped_off || logType == LogType.visited || logType == LogType.retrieve) ? GlobalCore.getSelectedCache().generatedId : -1;
    }

    private String getCache_URL() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (trackable.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(trackable.getCurrentGeoCacheCode()) && trackable.getCurrentGeoCacheCode().length() > 0) {
                if (logType == LogType.visited || logType == LogType.retrieve) {
                    // TB is perhaps not in the selected cache, but don't want to change selected Cache
                    return "https://coord.info/" + trackable.getCurrentGeoCacheCode();
                }
            }
        }
        return (logType == LogType.dropped_off || logType == LogType.visited || logType == LogType.retrieve) ? GlobalCore.getSelectedCache().getUrl() : "";
    }

    private int getCache_Type() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (trackable.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(trackable.getCurrentGeoCacheCode()) && trackable.getCurrentGeoCacheCode().length() > 0) {
                if (logType == LogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return GeoCacheType.Undefined.ordinal();
                }
            }
        }
        return (logType == LogType.dropped_off || logType == LogType.visited || logType == LogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheType().ordinal() : -1;
    }

}
