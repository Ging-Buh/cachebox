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

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.TemplateFormatter;
import de.droidcachebox.WrapType;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.TrackableListView;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;

import java.util.Date;

import static de.droidcachebox.core.GroundspeakAPI.*;
import static de.droidcachebox.utils.Config_Core.br;

public class TB_Log extends ActivityBase {
    private static WaitDialog wd;
    private static TB_Log that;
    private Trackable TB;
    private CB_Button btnClose;
    private ImageButton btnAction;
    private Image icon, CacheIcon;
    private CB_Label lblPlaced;
    private EditTextField lblName;
    private Box contentBox;
    private GeoCacheLogType LT;
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

    public void Show(Trackable TB, GeoCacheLogType Type) {
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

        btnAction = new ImageButton("Action");
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

        rbDirectLog = new RadioButton("direct_Log");
        rbOnlyDraft = new RadioButton("onlyDraft");

        rbDirectLog.setText(Translation.get("directLog"));
        rbOnlyDraft.setText(Translation.get("onlyDraft"));

        RadioGroup Group = new RadioGroup();
        Group.add(rbOnlyDraft);
        Group.add(rbDirectLog);
        if (Config.TB_DirectLog.getValue()) {
            rbDirectLog.setChecked(true);
        } else {
            rbOnlyDraft.setChecked(true);
        }
    }

    private void layout() {
        this.removeChilds();
        this.initRow(BOTTOMUP);
        this.addNext(btnAction);
        this.addLast(btnClose);
        this.addLast(contentBox);
        contentBox.initRow(TOPDOWN, contentBox.getHeight());
        contentBox.setNoBorders();
        contentBox.setMargins(0, 0);
        contentBox.addLast(edit);
        contentBox.addLast(rbDirectLog);
        contentBox.addLast(rbOnlyDraft);

        // Show Selected Cache for GeoCacheLogType discovered/visited/dropped_off/retrieve
        if (LT == GeoCacheLogType.discovered || LT == GeoCacheLogType.visited || LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.retrieve) {

            Cache c = GlobalCore.getSelectedCache();
            if (c == null) {
                // Log Inposible, close Activity and give a Message
                final String errorMsg = Translation.get("NoCacheSelect");
                this.finish();

                GL.that.RunOnGL(() -> MessageBox.show(errorMsg, "", MessageBoxIcon.Error));
                return;
            }

            String msg = "";
            if (LT == GeoCacheLogType.discovered) {
                msg = Translation.get("discoveredAt") + ": " + br + c.getGeoCacheName();
            }
            if (LT == GeoCacheLogType.visited) {
                msg = Translation.get("visitedAt") + ": " + br + c.getGeoCacheName();
            }
            if (LT == GeoCacheLogType.dropped_off) {
                msg = Translation.get("dropped_offAt") + ": " + br + c.getGeoCacheName();
            }
            if (LT == GeoCacheLogType.retrieve) {
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
                edit.setText(TemplateFormatter.ReplaceTemplate(Config.DiscoverdTemplate.getValue(), TB));
                break;
            case visited:
                btnAction.setImage(Sprites.getSprite(IconName.TBVISIT.name()));
                edit.setText(TemplateFormatter.ReplaceTemplate(Config.VisitedTemplate.getValue(), TB));
                break;
            case dropped_off:
                btnAction.setImage(Sprites.getSprite(IconName.TBDROP.name()));
                edit.setText(TemplateFormatter.ReplaceTemplate(Config.DroppedTemplate.getValue(), TB));
                break;
            case grab_it:
                btnAction.setImage(Sprites.getSprite(IconName.TBGRAB.name()));
                edit.setText(TemplateFormatter.ReplaceTemplate(Config.GrabbedTemplate.getValue(), TB));
                break;
            case retrieve:
                btnAction.setImage(Sprites.getSprite(IconName.TBPICKED.name()));
                edit.setText(TemplateFormatter.ReplaceTemplate(Config.PickedTemplate.getValue(), TB));
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

        wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), () -> {

        }, new ICancelRunnable() {

            @Override
            public void run() {
                int result = uploadTrackableLog(TB, getCache_GcCode(), GeoCacheLogType.CB_LogType2GC(LT), new Date(), edit.getText());

                if (result == ERROR) {
                    GL.that.Toast(LastAPIError);
                    if (wd != null)
                        wd.close();
                    MessageBox.show(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MessageBoxButton.YesNoRetry, MessageBoxIcon.Question, (which, data) -> {
                        switch (which) {
                            case MessageBox.BTN_RIGHT_NEGATIVE:
                                logOnline();
                                return true;

                            case MessageBox.BTN_MIDDLE_NEUTRAL:
                                return true;

                            case MessageBox.BTN_LEFT_POSITIVE:
                                createTBDraft();
                                return true;
                        }
                        return true;
                    });
                    return;
                }
                if (result != OK) {
                    GL.that.Toast(LastAPIError);
                    if (wd != null)
                        wd.close();
                    MessageBox.show(Translation.get("CreateDraftInstead"), Translation.get("UploadFailed"), MessageBoxButton.YesNoRetry, MessageBoxIcon.Question,
                            (which, data) -> {
                                switch (which) {
                                    case MessageBox.BTN_RIGHT_NEGATIVE:
                                        logOnline();
                                        return true;

                                    case MessageBox.BTN_MIDDLE_NEUTRAL:
                                        return true;

                                    case MessageBox.BTN_LEFT_POSITIVE:
                                        createTBDraft();
                                        return true;
                                }
                                return true;
                            });
                    return;
                }

                if (LastAPIError.length() > 0) {
                    GL.that.RunOnGL(() -> MessageBox.show(LastAPIError, Translation.get("Error"), MessageBoxIcon.Error));
                }

                if (wd != null)
                    wd.close();
                TB_Log.this.finish();

                // Refresh TB List after Droped Off or Picked or Grabed
                if (LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.retrieve || LT == GeoCacheLogType.grab_it) {
                    GL.that.RunOnGL(() -> TrackableListView.that.refreshTbList());
                }

            }

            @Override
            public boolean doCancel() {
                return false;
            }
        });

    }

    private void createTBDraft() {
        Draft newFieldNote;
        newFieldNote = new Draft(LT);
        newFieldNote.CacheName = getCache_Name();
        newFieldNote.gcCode = getCache_GcCode();
        newFieldNote.foundNumber = Config.FoundOffset.getValue();
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
        newFieldNote.fillType();
        newFieldNote.WriteToDatabase();

        TB_Log.this.finish();
    }

    private String getCache_GcCode() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return TB.getCurrentGeoCacheCode();
                }
            }
        }
        return (LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheCode() : "";
    }

    private String getCache_Name() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) {
                    // TB is perhaps not in the selected cache, but don't want to change selected Cache
                    return TB.getCurrentGeoCacheCode();
                }
            }
        }
        return (LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheName() : "";
    }

    private long getCache_ID() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return Cache.generateCacheId(TB.getCurrentGeoCacheCode());
                }
            }
        }
        return (LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) ? GlobalCore.getSelectedCache().generatedId : -1;
    }

    private String getCache_URL() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) {
                    // TB is perhaps not in the selected cache, but don't want to change selected Cache
                    return "https://coord.info/" + TB.getCurrentGeoCacheCode();
                }
            }
        }
        return (LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) ? GlobalCore.getSelectedCache().getUrl() : "";
    }

    private int getCache_Type() {
        /*
         * Muss je nach LogType leer oder gefüllt sein
         */
        if (TB.getCurrentGeoCacheCode() != null) {
            if (!GlobalCore.getSelectedCache().getGeoCacheCode().equals(TB.getCurrentGeoCacheCode()) && TB.getCurrentGeoCacheCode().length() > 0) {
                if (LT == GeoCacheLogType.retrieve) {
                    // TB is perhaps not in the selected cache
                    return GeoCacheType.Undefined.ordinal();
                }
            }
        }
        return (LT == GeoCacheLogType.dropped_off || LT == GeoCacheLogType.visited || LT == GeoCacheLogType.retrieve) ? GlobalCore.getSelectedCache().getGeoCacheType().ordinal() : -1;
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
