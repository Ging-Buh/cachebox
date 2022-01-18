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
import de.droidcachebox.menu.menuBtn1.executes.Trackables;
import de.droidcachebox.menu.menuBtn4.executes.TemplateFormatter;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

public class TB_Log extends ActivityBase {
    private static TB_Log that;
    private Trackable TB;
    private CB_Button btnClose;
    private ImageButton btnAction;
    private Image icon, CacheIcon;
    private CB_Label lblPlaced;
    private EditTextField lblName;
    private Box contentBox;
    private LogType LT;
    private EditTextField edit;
    private RadioButton rbDirectLog;
    private RadioButton rbOnlyDraft;

    private TB_Log() {
        super("TB_Log_Activity");
        createControls();
        that = this;
    }

    public static TB_Log getInstance() {
        if (that == null) that = new TB_Log();
        return that;
    }

    public void Show(Trackable TB, LogType Type) {
        this.TB = TB;
        this.LT = Type;
        layout();
        GL.that.showActivity(this);
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
        if (LT == LogType.discovered || LT == LogType.visited || LT == LogType.dropped_off || LT == LogType.retrieve) {

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
            if (LT == LogType.discovered) {
                msg = Translation.get("discoveredAt") + ": " + br + c.getGeoCacheName();
            }
            if (LT == LogType.visited) {
                msg = Translation.get("visitedAt") + ": " + br + c.getGeoCacheName();
            }
            if (LT == LogType.dropped_off) {
                msg = Translation.get("dropped_offAt") + ": " + br + c.getGeoCacheName();
            }
            if (LT == LogType.retrieve) {
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
        icon.setImageURL(TB.getIconUrl());
        lblName.setWrapType(WrapType.WRAPPED);
        lblName.setBackground(null, null);
        this.addLast(lblName);
        lblName.setText(TB.getName());
        lblName.setEditable(false);
        lblName.showFromLineNo(0);
        lblName.setCursorPosition(0);

        switch (this.LT) {
            case discovered:
                btnAction.setImage(Sprites.getSprite(IconName.TBDISCOVER.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.DiscoverdTemplate.getValue(), TB));
                break;
            case visited:
                btnAction.setImage(Sprites.getSprite(IconName.TBVISIT.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.VisitedTemplate.getValue(), TB));
                break;
            case dropped_off:
                btnAction.setImage(Sprites.getSprite(IconName.TBDROP.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.DroppedTemplate.getValue(), TB));
                break;
            case grab_it:
                btnAction.setImage(Sprites.getSprite(IconName.TBGRAB.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.GrabbedTemplate.getValue(), TB));
                break;
            case retrieve:
                btnAction.setImage(Sprites.getSprite(IconName.TBPICKED.name()));
                edit.setText(TemplateFormatter.replaceTemplate(Settings.PickedTemplate.getValue(), TB));
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

                // Refresh TB List after Droped Off or Picked or Grabed
                if (LT == LogType.dropped_off || LT == LogType.retrieve || LT == LogType.grab_it) {
                    GL.that.runOnGL(() -> Trackables.trackables.refreshTbList());
                }
            }

            @Override
            public void run() {
                result[0] = uploadTrackableLog(TB, getCache_GcCode(), LogType.CB_LogType2GC(LT), new Date(), edit.getText());
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
        newFieldNote = new Draft(LT);
        newFieldNote.CacheName = getCache_Name();
        newFieldNote.gcCode = getCache_GcCode();
        newFieldNote.foundNumber = Settings.FoundOffset.getValue();
        newFieldNote.timestamp = new Date();
        newFieldNote.CacheId = getCache_ID();
        newFieldNote.comment = edit.getText();
        newFieldNote.CacheUrl = getCache_URL();
        newFieldNote.cacheType = getCache_Type();
        newFieldNote.isTbDraft = true;
        newFieldNote.TbName = TB.getName();
        newFieldNote.TbIconUrl = TB.getIconUrl();
        newFieldNote.TravelBugCode = TB.getTbCode();
        newFieldNote.TrackingNumber = TB.getTrackingCode();
        newFieldNote.writeToDatabase();

        TB_Log.this.finish();
    }

    private String getCache_GcCode() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == LogType.visited || LT == LogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return TB.getCurrentGeoCacheCode();
                }
            }
        }
        return (LT == LogType.dropped_off || LT == LogType.visited || LT == LogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheCode() : "";
    }

    private String getCache_Name() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == LogType.visited || LT == LogType.retrieve) {
                    // TB is perhaps not in the selected cache, but don't want to change selected Cache
                    return TB.getCurrentGeoCacheCode();
                }
            }
        }
        return (LT == LogType.dropped_off || LT == LogType.visited || LT == LogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheName() : "";
    }

    private long getCache_ID() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == LogType.visited || LT == LogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return Cache.generateCacheId(TB.getCurrentGeoCacheCode());
                }
            }
        }
        return (LT == LogType.dropped_off || LT == LogType.visited || LT == LogType.retrieve) ? GlobalCore.getSelectedCache().generatedId : -1;
    }

    private String getCache_URL() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == LogType.visited || LT == LogType.retrieve) {
                    // TB is perhaps not in the selected cache, but don't want to change selected Cache
                    return "https://coord.info/" + TB.getCurrentGeoCacheCode();
                }
            }
        }
        return (LT == LogType.dropped_off || LT == LogType.visited || LT == LogType.retrieve) ? GlobalCore.getSelectedCache().getUrl() : "";
    }

    private int getCache_Type() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == LogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return GeoCacheType.Undefined.ordinal();
                }
            }
        }
        return (LT == LogType.dropped_off || LT == LogType.visited || LT == LogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheType().ordinal() : -1;
    }

    @Override
    public void dispose() {
        that = null;
        TB = null;

        if (btnClose != null)
            btnClose.dispose();
        btnClose = null;

        if (btnAction != null)
            btnAction.dispose();
        btnAction = null;

        if (icon != null)
            icon.dispose();
        icon = null;

        if (lblName != null)
            lblName.dispose();
        btnAction = null;

        if (lblName != null)
            lblName.dispose();
        btnAction = null;

        if (contentBox != null)
            contentBox.dispose();
        contentBox = null;

        if (edit != null)
            edit.dispose();
        edit = null;

        LT = null;

    }
}
