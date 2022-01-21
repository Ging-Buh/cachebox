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
package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.fetchPocketQuery;
import static de.droidcachebox.core.GroundspeakAPI.fetchPocketQueryList;
import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;
import static de.droidcachebox.core.GroundspeakAPI.isPremiumMember;
import static de.droidcachebox.menu.menuBtn1.contextmenus.ShowImportMenu.MI_IMPORT_CBS;
import static de.droidcachebox.menu.menuBtn1.contextmenus.ShowImportMenu.MI_IMPORT_GCV;

import android.text.InputType;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.GroundspeakAPI.PQ;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.ex_import.ImportProgress;
import de.droidcachebox.ex_import.Importer;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.CollapseBox;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.ImportAnimation;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.Spinner;
import de.droidcachebox.gdx.controls.SpinnerAdapter;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListener;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.Copy;
import de.droidcachebox.utils.CopyJobDefinition;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

public class Import extends ActivityBase {
    private static final String sClass = "Import";
    private static final long ANIMATION_TICK = 450;
    private final float CollapseBoxMaxHeight;
    private final ScrollBox scrollBox;
    private final ArrayList<String> values = new ArrayList<>();
    private final float CollapseBoxHeight;
    private final AtomicBoolean isCanceled;
    private float CollapseBoxLogsMaxHeight;
    private boolean PQ_LINE_ACTIVE = true;
    private boolean CBS_LINE_ACTIVE;
    private boolean GPX_LINE_ACTIVE = true;
    private boolean GCV_LINE_ACTIVE = true;
    private boolean LOG_LINE_ACTIVE = true;
    private boolean DB_LINE_ACTIVE = true;
    private boolean IMAGE_LINE_ACTIVE;
    private V_ListView lvPQs;
    private CB_Button btnOK;
    private CB_Button refreshPqList;
    private CB_Button btnSelectFile;
    private float innerLeft;
    private float innerHeight;
    private ProgressBar progressBar;
    private CB_Label lblPQ;
    private CB_CheckBox checkImportPQFromGC;
    private CB_Label lblCBServer;
    private CB_CheckBox checkImportFromCBServer;
    private CB_Label lblGPX;
    private CB_CheckBox checkBoxImportGPX;
    private CB_Label lblGcVote;
    private CB_CheckBox checkBoxGcVote;
    private CB_Label lblImage;
    private CB_CheckBox checkBoxPreloadImages;
    private CB_Label lblSpoiler;
    private CB_CheckBox checkBoxPreloadSpoiler;
    private CB_Label lblProgressMsg;
    private CB_Label lblLogs;
    private CB_Label lblCompact;
    private CB_CheckBox checkBoxCleanLogs;
    private CB_CheckBox checkBoxCompactDB;
    private CollapseBox pqListCollapseBox;
    private CollapseBox cbServerCollapseBox;
    private CollapseBox logCollapseBox;
    private Spinner spinner;
    private boolean allDownloadsComplete;
    private Timer mAnimationTimer;
    private int animationValue = 0;
    private boolean importStarted = false;
    private ArrayList<PQ> PqList;
    private CB_RectF itemRec;
    private float itemHeight = -1;
    private ImportAnimation importAnimation;

    public Import() {
        this(0);
    }

    public Import(int importType) {
        super("importActivity");
        // to direct start of an import
        CBS_LINE_ACTIVE = false;
        IMAGE_LINE_ACTIVE = true;
        switch (importType) {
            case MI_IMPORT_CBS:
                PQ_LINE_ACTIVE = false;
                CBS_LINE_ACTIVE = true;
                GPX_LINE_ACTIVE = false;
                GCV_LINE_ACTIVE = false;
                LOG_LINE_ACTIVE = true;
                DB_LINE_ACTIVE = true;
                break;
            case MI_IMPORT_GCV:
                PQ_LINE_ACTIVE = false;
                CBS_LINE_ACTIVE = false;
                GPX_LINE_ACTIVE = false;
                GCV_LINE_ACTIVE = true;
                LOG_LINE_ACTIVE = false;
                DB_LINE_ACTIVE = false;
                IMAGE_LINE_ACTIVE = false;
        }

        CollapseBoxMaxHeight = CollapseBoxHeight = UiSizes.getInstance().getButtonHeight() * 6;
        innerHeight = 1000;
        scrollBox = new ScrollBox(this);
        this.addChild(scrollBox);
        createOkCancelBtn();
        createTitleLine();
        scrollBox.setHeight(lblProgressMsg.getY() - btnOK.getMaxY() - margin - margin);
        scrollBox.setY(btnOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());
        if (PQ_LINE_ACTIVE) {
            if (!isAccessTokenInvalid()) {
                if (!isPremiumMember()) {
                    PQ_LINE_ACTIVE = false;
                }
            } else {
                new ButtonDialog(Translation.get("Desc_ImportPQsFromGeocachingCom"), Translation.get("apiKeyInvalid"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
            }
        }
        Log.debug(sClass, "is Premium = " + PQ_LINE_ACTIVE);
        createPQLines();
        createCBServerLines();
        createPqCollapseBox();
        createCBServerCollapseBox();
        createGpxLine();
        createGcVoteLine();
        createImageLine();
        createLogLine();
        createLogCollapseBox();
        createCompactDBLine();

        initialForm();

        layout();

        if (importType == MI_IMPORT_CBS) {
            checkImportFromCBServer.setChecked(true);
            checkImportFromCBServer.setVisible(true);
            cbServerCollapseBox.expand();
        } else if (importType == MI_IMPORT_GCV) {
            checkBoxGcVote.setChecked(true);
            checkBoxGcVote.setVisible(true);
        }
        // next 3 lines delete, if CBServer is integrated again
        checkImportFromCBServer.setChecked(false);
        checkImportFromCBServer.setVisible(false);
        lblCBServer.setVisible(false);

        // scrollBox.setBackground(new ColorDrawable(Color.RED));
        isCanceled = new AtomicBoolean();
    }

    private void createOkCancelBtn() {
        btnOK = new CB_Button(leftBorder, leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Import");
        CB_Button btnCancel = new CB_Button(btnOK.getMaxX(), leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Import");

        // Translations
        btnOK.setText(Translation.get("import"));
        btnCancel.setText(Translation.get("cancel"));

        addChild(btnOK);
        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            importNow();
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (importStarted) {
                ButtonDialog bd = new ButtonDialog(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MsgBoxButton.YesNo, MsgBoxIcon.Stop);
                bd.setButtonClickHandler((which, data) -> {
                    if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                        isCanceled.set(true);
                    }
                    return true;
                });
                bd.show();
            } else
                finish();
            return true;
        });

    }

    private void createTitleLine() {
        // Title+Progressbar

        float lineHeight = UiSizes.getInstance().getButtonHeight() * 0.75f;

        CB_Label lblTitle = new CB_Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
        lblTitle.setFont(Fonts.getBig());
        float lblWidth = lblTitle.setText(Translation.get("import")).getTextWidth();
        this.addChild(lblTitle);

        CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth, lineHeight);

        progressBar = new ProgressBar(rec);

        progressBar.setValues(0, "");

        float progressLineHeight = Fonts.measureForSmallFont("Tg").height * 3;

        lblProgressMsg = new CB_Label(this.name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - progressLineHeight, innerWidth - margin - margin, progressLineHeight);
        lblProgressMsg.setFont(Fonts.getBubbleSmall());
        lblProgressMsg.setVAlignment(VAlignment.TOP);

        this.addChild(progressBar);
        this.addChild(lblProgressMsg);

    }

    private void createPQLines() {

        innerLeft = margin;

        checkImportPQFromGC = new CB_CheckBox();
        checkImportPQFromGC.setX(innerLeft);
        checkImportPQFromGC.setY(innerHeight - checkImportPQFromGC.getHeight());
        if (!PQ_LINE_ACTIVE) {
            checkImportPQFromGC.setVisible(false);
            checkImportPQFromGC.setHeight(0);
            checkImportPQFromGC.setChecked(false);
        }
        lblPQ = new CB_Label("lblPQ", checkImportPQFromGC.getMaxX() + margin, checkImportPQFromGC.getY(), innerWidth - margin * 3 - checkImportPQFromGC.getWidth(), checkImportPQFromGC.getHeight());
        lblPQ.setFont(Fonts.getNormal());
        lblPQ.setText(Translation.get("PQfromGC"));
        if (!PQ_LINE_ACTIVE) {
            lblPQ.setVisible(false);
            lblPQ.setHeight(0);
        }

        scrollBox.addChild(checkImportPQFromGC);
        scrollBox.addChild(lblPQ);
    }

    private void createCBServerLines() {

        innerLeft = margin;

        checkImportFromCBServer = new CB_CheckBox();
        checkImportFromCBServer.setX(innerLeft);
        checkImportFromCBServer.setY(innerHeight - checkImportFromCBServer.getHeight());
        if (!CBS_LINE_ACTIVE)
            checkImportFromCBServer.setVisible(false);
        if (!CBS_LINE_ACTIVE)
            checkImportFromCBServer.setHeight(0);
        lblCBServer = new CB_Label(this.name + " lblCBServer", checkImportFromCBServer.getMaxX() + margin, checkImportFromCBServer.getY(), innerWidth - margin * 3 - checkImportFromCBServer.getWidth(), checkImportFromCBServer.getHeight());
        lblCBServer.setFont(Fonts.getNormal());
        lblCBServer.setText(Translation.get("FromCBServer"));
        if (!CBS_LINE_ACTIVE)
            lblCBServer.setVisible(false);
        if (!CBS_LINE_ACTIVE)
            lblCBServer.setHeight(0);
        scrollBox.addChild(checkImportFromCBServer);
        scrollBox.addChild(lblCBServer);
    }

    private void createPqCollapseBox() {
        CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollapseBoxHeight - margin, lblPQ.getWidth(), CollapseBoxHeight);

        pqListCollapseBox = new CollapseBox(rec, "PqCollapse");
        pqListCollapseBox.setBackground(this.getBackground());

        refreshPqList = new CB_Button(name);
        refreshPqList.setWidth(pqListCollapseBox.getWidth() - margin - margin);
        refreshPqList.setX(margin);
        refreshPqList.setY(margin);
        refreshPqList.setText(Translation.get("refreshPqList"));
        refreshPqList.setClickHandler((v, x, y, pointer, button) -> {
            refreshPqList();
            return true;
        });

        lvPQs = new V_ListView(new CB_RectF(leftBorder, refreshPqList.getMaxY() + margin, pqListCollapseBox.getWidth(), pqListCollapseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

        lvPQs.setEmptyMsgItem(Translation.get("EmptyPqList"));

        pqListCollapseBox.addChild(lvPQs);
        pqListCollapseBox.addChild(refreshPqList);

        scrollBox.addChild(pqListCollapseBox);
    }

    private void createCBServerCollapseBox() {
        CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollapseBoxHeight - margin, lblPQ.getWidth(), CollapseBoxHeight);

        cbServerCollapseBox = new CollapseBox(rec, "CBServerCollapse");
        cbServerCollapseBox.setBackground(this.getBackground());

        CB_Button refreshCBServerList = new CB_Button(name);
        refreshCBServerList.setWidth(pqListCollapseBox.getWidth() - margin - margin);
        refreshCBServerList.setX(margin);
        refreshCBServerList.setY(margin);
        refreshCBServerList.setText(Translation.get("refreshCBServerList"));
        refreshCBServerList.setClickHandler((v, x, y, pointer, button) -> true);

        V_ListView lvCBServer = new V_ListView(new CB_RectF(leftBorder, refreshPqList.getMaxY() + margin, cbServerCollapseBox.getWidth(), cbServerCollapseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

        lvCBServer.setEmptyMsgItem(Translation.get("EmptyCBServerList"));

        cbServerCollapseBox.addChild(lvCBServer);
        cbServerCollapseBox.addChild(refreshCBServerList);

        scrollBox.addChild(cbServerCollapseBox);
    }

    private void createGpxLine() {
        checkBoxImportGPX = new CB_CheckBox();
        checkBoxImportGPX.setX(innerLeft);
        checkBoxImportGPX.setY(pqListCollapseBox.getY() - margin - checkBoxImportGPX.getHeight());

        btnSelectFile = new CB_Button(Translation.get("selectFile"));

        if (!GPX_LINE_ACTIVE) {
            checkBoxImportGPX.setVisible(false);
            checkBoxImportGPX.setHeight(0);
            btnSelectFile.setVisible(false);
        }
        lblGPX = new CB_Label(this.name + " lblGPX", checkBoxImportGPX.getMaxX() + margin, checkBoxImportGPX.getY(), innerWidth - margin * 3 - checkBoxImportGPX.getWidth(), checkBoxImportGPX.getHeight());
        lblGPX.setFont(Fonts.getNormal());
        lblGPX.setText(Translation.get("GPX"));

        btnSelectFile.setPos(checkBoxImportGPX.getMaxX() + (checkBoxImportGPX.getWidth() * 2.2f), checkBoxImportGPX.getY());
        btnSelectFile.setWidth(scrollBox.getInnerWidth() - (btnSelectFile.getX() + margin));

        btnSelectFile.setClickHandler((v, x, y, pointer, button) -> {
            new FileOrFolderPicker(GlobalCore.workPath + "/User", "*.gpx|*.zip", "", "", this::copyGPX2PQFolder).show();
            return true;
        });

        scrollBox.addChild(checkBoxImportGPX);
        scrollBox.addChild(lblGPX);
        scrollBox.addChild(btnSelectFile);
    }

    private void createGcVoteLine() {
        if (Settings.GcVotePassword.getValue().length() == 0)
            GCV_LINE_ACTIVE = false;
        checkBoxGcVote = new CB_CheckBox();
        checkBoxGcVote.setX(innerLeft);
        checkBoxGcVote.setY(checkBoxImportGPX.getY() - margin - checkBoxImportGPX.getHeight());
        if (!GCV_LINE_ACTIVE) {
            checkBoxGcVote.setVisible(false);
            checkBoxGcVote.setHeight(0);
        }
        lblGcVote = new CB_Label(this.name + " lblGcVote", checkBoxGcVote.getMaxX() + margin, checkBoxGcVote.getY(), innerWidth - margin * 3 - checkBoxGcVote.getWidth(), checkBoxGcVote.getHeight());
        lblGcVote.setFont(Fonts.getNormal());
        lblGcVote.setText(Translation.get("GCVoteRatings"));
        if (!GCV_LINE_ACTIVE) {
            lblGcVote.setVisible(false);
            lblGcVote.setHeight(0);
        }

        scrollBox.addChild(checkBoxGcVote);
        scrollBox.addChild(lblGcVote);
    }

    private void createImageLine() {
        // Preload Description Images
        checkBoxPreloadImages = new CB_CheckBox();
        checkBoxPreloadImages.setX(innerLeft);
        checkBoxPreloadImages.setY(checkBoxGcVote.getY() - margin - checkBoxPreloadImages.getHeight());

        lblImage = new CB_Label(this.name + " lblImage", checkBoxPreloadImages.getMaxX() + margin, checkBoxPreloadImages.getY(), innerWidth - margin * 3 - checkBoxPreloadImages.getWidth(), checkBoxPreloadImages.getHeight());
        lblImage.setFont(Fonts.getNormal());
        lblImage.setText(Translation.get("PreloadImages"));

        if (IMAGE_LINE_ACTIVE) {
            scrollBox.addChild(checkBoxPreloadImages);
            scrollBox.addChild(lblImage);
        }
        // Preload Spoiler Images
        checkBoxPreloadSpoiler = new CB_CheckBox();
        checkBoxPreloadSpoiler.setX(innerLeft);
        checkBoxPreloadSpoiler.setY(checkBoxPreloadImages.getY() - margin - checkBoxPreloadSpoiler.getHeight());

        lblSpoiler = new CB_Label(this.name + " lblSpoiler", checkBoxPreloadSpoiler.getMaxX() + margin, checkBoxPreloadSpoiler.getY(), innerWidth - margin * 3 - checkBoxPreloadSpoiler.getWidth(), checkBoxPreloadSpoiler.getHeight());
        lblSpoiler.setFont(Fonts.getNormal());
        lblSpoiler.setText(Translation.get("PreloadSpoiler"));

        if (IMAGE_LINE_ACTIVE) {
            scrollBox.addChild(checkBoxPreloadSpoiler);
            scrollBox.addChild(lblSpoiler);
        }
        if (!IMAGE_LINE_ACTIVE) {
            checkBoxPreloadImages.setChecked(false);
            checkBoxPreloadSpoiler.setChecked(false);
        }
    }

    private void createLogLine() {
        checkBoxCleanLogs = new CB_CheckBox();
        checkBoxCleanLogs.setX(innerLeft);

        float yPos = checkBoxPreloadSpoiler.getY();

        checkBoxCleanLogs.setY(yPos - margin - checkBoxCleanLogs.getHeight());
        if (!LOG_LINE_ACTIVE) {
            checkBoxCleanLogs.setVisible(false);
            checkBoxCleanLogs.setHeight(0);
        }
        lblLogs = new CB_Label(this.name + " lblLogs", checkBoxCleanLogs.getMaxX() + margin, checkBoxCleanLogs.getY(), innerWidth - margin * 3 - checkBoxCleanLogs.getWidth(), checkBoxCleanLogs.getHeight());
        lblLogs.setFont(Fonts.getNormal());
        lblLogs.setText(Translation.get("DeleteLogs"));

        if (!LOG_LINE_ACTIVE) {
            lblLogs.setVisible(false);
            lblLogs.setHeight(0);
        }
        scrollBox.addChild(checkBoxCleanLogs);
        scrollBox.addChild(lblLogs);
    }

    private void createLogCollapseBox() {
        float SmallLineHeight = Fonts.measureForSmallFont("Tg").height * 1.5f;
        CollapseBoxLogsMaxHeight = checkBoxCleanLogs.getHeight() + (SmallLineHeight * 3.5f) + (margin * 4);

        CB_RectF rec = new CB_RectF(lblLogs.getX(), lblLogs.getY() - CollapseBoxLogsMaxHeight - margin, lblLogs.getWidth(), CollapseBoxLogsMaxHeight);
        logCollapseBox = new CollapseBox(rec, "LogCollapse");
        logCollapseBox.setBackground(this.getBackground());
        scrollBox.addChild(logCollapseBox);

        // ################################
        // create and fill LogLife spinner
        // ################################

        values.clear();
        values.add(Translation.get("ImportLogAgeIndex_0"));
        values.add(Translation.get("ImportLogAgeIndex_1"));
        values.add(Translation.get("ImportLogAgeIndex_2"));
        values.add(Translation.get("ImportLogAgeIndex_3"));
        values.add(Translation.get("ImportLogAgeIndex_4"));
        values.add(Translation.get("ImportLogAgeIndex_5"));
        values.add(Translation.get("ImportLogAgeIndex_6"));

        final SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return values.get(position);
            }

            @Override
            public Drawable getIcon(int Position) {
                return null;
            }

            @Override
            public int getCount() {
                return values.size();
            }
        };

        spinner = new Spinner(margin, logCollapseBox.getHeight() - margin - checkBoxCleanLogs.getHeight(), logCollapseBox.getWidth() - margin - margin, checkBoxCleanLogs.getHeight(), "ImportDeleteLogsTitle", adapter, index -> {
            Settings.LogMaxMonthAge.setValue(index);
            Settings.getInstance().acceptChanges();
        });

        logCollapseBox.addChild(spinner);

        CB_Label lblButKeepLeast = new CB_Label(this.name + " lblButKeepLeast", margin, spinner.getY() - margin - SmallLineHeight, logCollapseBox.getInnerWidth(), SmallLineHeight);
        lblButKeepLeast.setText(Translation.get("ButKeepLeast"));
        logCollapseBox.addChild(lblButKeepLeast);

        final EditTextField input = new EditTextField(checkBoxCleanLogs.scaleCenter(2), logCollapseBox, "*" + Translation.get("ButKeepLeast"));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        input.setHeight(SmallLineHeight * 2.5f);
        input.setText(String.valueOf(Settings.LogMinCount.getValue()));
        input.setPos(margin, lblButKeepLeast.getY() - margin - input.getHeight());
        logCollapseBox.addChild(input);
        input.setClickHandler((v, x, y, pointer, button) -> {
            NumericInputBox numericInputBox = new NumericInputBox(Translation.get("ButKeepLeast"), Translation.get("DeleteLogs"));
            numericInputBox.initIntInput(Settings.LogMinCount.getValue(), new IReturnValueListener() {
                @Override
                public void returnValue(int value) {
                    Settings.LogMinCount.setValue(value);
                    Settings.getInstance().acceptChanges();
                    input.setText(String.valueOf(value));
                }

                @Override
                public void cancelClicked() {
                }

            });
            numericInputBox.show();
            return true;
        });
    }

    private void createCompactDBLine() {
        checkBoxCompactDB = new CB_CheckBox();
        checkBoxCompactDB.setX(innerLeft);
        checkBoxCompactDB.setY(logCollapseBox.getY() - margin - checkBoxCompactDB.getHeight());

        if (!DB_LINE_ACTIVE) {
            checkBoxCompactDB.setVisible(false);
            checkBoxCompactDB.setHeight(0);
        }
        lblCompact = new CB_Label(this.name + " lblCompact", checkBoxPreloadSpoiler.getMaxX() + margin, checkBoxCompactDB.getY(), innerWidth - margin * 3 - checkBoxCompactDB.getWidth(), checkBoxCompactDB.getHeight());
        lblCompact.setFont(Fonts.getNormal());
        lblCompact.setText(Translation.get("CompactDB"));

        if (!DB_LINE_ACTIVE) {
            lblCompact.setVisible(false);
            lblCompact.setHeight(0);
        }
        scrollBox.addChild(checkBoxCompactDB);
        scrollBox.addChild(lblCompact);
    }

    private void layout() {

        checkBoxCompactDB.setY(margin);
        lblCompact.setY(margin);

        logCollapseBox.setY(checkBoxCompactDB.getMaxY() + margin);

        checkBoxCleanLogs.setY(logCollapseBox.getMaxY() + margin);
        lblLogs.setY(logCollapseBox.getMaxY() + margin);

        float yPos = lblLogs.getMaxY();

        checkBoxPreloadSpoiler.setY(yPos + margin);
        lblSpoiler.setY(yPos + margin);

        yPos = checkBoxPreloadSpoiler.getMaxY();

        checkBoxPreloadImages.setY(yPos + margin);
        lblImage.setY(yPos + margin);

        checkBoxGcVote.setY(lblImage.getMaxY() + margin);
        lblGcVote.setY(lblImage.getMaxY() + margin);

        checkBoxImportGPX.setY(lblGcVote.getMaxY() + margin);
        lblGPX.setY(lblGcVote.getMaxY() + margin);
        btnSelectFile.setY(lblGcVote.getMaxY() + margin);

        cbServerCollapseBox.setY(checkBoxImportGPX.getMaxY() + margin);

        checkImportFromCBServer.setY(cbServerCollapseBox.getMaxY() + margin);
        lblCBServer.setY(cbServerCollapseBox.getMaxY() + margin);

        pqListCollapseBox.setY(lblCBServer.getMaxY() + margin);
        // PQ_ListCollapseBox.setHeight(CollapseBoxHeight);

        checkImportPQFromGC.setY(pqListCollapseBox.getMaxY() + margin);
        lblPQ.setY(pqListCollapseBox.getMaxY() + margin);

        innerHeight = lblPQ.getMaxY() + margin;
        scrollBox.setVirtualHeight(innerHeight);
    }

    private void initialForm() {
        checkBoxPreloadImages.setChecked(Settings.CacheImageData.getValue());
        checkBoxPreloadSpoiler.setChecked(Settings.CacheSpoilerData.getValue());
        checkBoxImportGPX.setChecked(GPX_LINE_ACTIVE ? Settings.ImportGpx.getValue() : false);
        checkImportPQFromGC.setOnCheckChangedListener((view, isChecked) -> {
            if (checkImportPQFromGC.isChecked()) {
                checkBoxImportGPX.setChecked(true);
                checkBoxImportGPX.setEnabled(false);
                pqListCollapseBox.expand();
                if (lvPQs.getAllListSize() == 0)
                    refreshPqList();
            } else {
                checkBoxImportGPX.setEnabled(true);
                pqListCollapseBox.collapse();
            }
        });
        checkBoxGcVote.setChecked(GCV_LINE_ACTIVE ? Settings.ImportRatings.getValue() : false);

        checkImportPQFromGC.setChecked(PQ_LINE_ACTIVE ? Settings.ImportPQsFromGeocachingCom.getValue() : false);
        checkImportPQFromGC.setEnabled(true);

        if (checkImportPQFromGC.isChecked()) {
            checkBoxImportGPX.setChecked(GPX_LINE_ACTIVE);
            checkBoxImportGPX.setEnabled(false);
        }
        checkBoxCompactDB.setChecked(DB_LINE_ACTIVE ? Settings.CompactDB.getValue() : false);

        checkBoxPreloadSpoiler.setEnable(true);
        lblSpoiler.setTextColor(COLOR.getFontColor());
        if (checkImportPQFromGC.isChecked()) {
            pqListCollapseBox.setAnimationHeight(CollapseBoxMaxHeight);
        } else {
            pqListCollapseBox.setAnimationHeight(0);
        }

        if (checkImportFromCBServer.isChecked()) {
            cbServerCollapseBox.setAnimationHeight(CollapseBoxHeight);
        } else {
            cbServerCollapseBox.setAnimationHeight(0);
        }

        pqListCollapseBox.setAnimationListener(height -> animatedHeightChanged());
        cbServerCollapseBox.setAnimationListener(height -> animatedHeightChanged());
        logCollapseBox.setAnimationListener(height -> animatedHeightChanged());

        checkBoxCleanLogs.setChecked(LOG_LINE_ACTIVE ? Settings.DeleteLogs.getValue() : false);

        checkBoxCleanLogs.setOnCheckChangedListener((view, isChecked) -> {
            if (checkBoxCleanLogs.isChecked()) {
                logCollapseBox.expand();
                spinner.setSelection(Settings.LogMaxMonthAge.getValue());
            } else {
                logCollapseBox.collapse();
            }

            Settings.DeleteLogs.setValue(isChecked);
            Settings.getInstance().acceptChanges();
        });

        if (checkBoxCleanLogs.isChecked()) {
            logCollapseBox.setAnimationHeight(CollapseBoxLogsMaxHeight);

            // validate value
            int value = Settings.LogMaxMonthAge.getValue();
            if (value > 6) {
                Settings.LogMaxMonthAge.setValue(6);
                Settings.getInstance().acceptChanges();
            }

            spinner.setSelection(Settings.LogMaxMonthAge.getValue());
        } else {
            logCollapseBox.setAnimationHeight(0);
        }

    }

    private void refreshPqList() {

        lvPQs.setAdapter(null);
        lvPQs.notifyDataSetChanged();
        refreshPqList.disable();

        new Thread(() -> {
            PqList = fetchPocketQueryList();
            if (APIError != OK) {
                new ButtonDialog(LastAPIError, Translation.get("PQfromGC"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
            }
            // even if error: you can use PqList, may be empty
            Collections.sort(PqList, (p1, p2) -> p1.name.compareTo(p2.name));
            lvPQs.setAdapter(new PqListAdapter());
            lvPQs.notifyDataSetChanged();

            stopAnimationTimer();
            lvPQs.setEmptyMsgItem(Translation.get("EmptyPqList"));

            refreshPqList.enable();
        }).start();

        mAnimationTimer = new Timer();
        mAnimationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

            private void TimerMethod() {
                animationValue++;

                if (animationValue > 5)
                    animationValue = 0;

                StringBuilder s = new StringBuilder();
                for (int i = 0; i < animationValue; i++) {
                    s.append(".");
                }

                lvPQs.setEmptyMsgItem(Translation.get("LoadPqList") + s);

            }

        }, 0, ANIMATION_TICK);

    }

    private void stopAnimationTimer() {
        if (mAnimationTimer != null) {
            mAnimationTimer.cancel();
            mAnimationTimer = null;
        }
    }

    private void importNow() {
        if (importStarted)
            return;

        allDownloadsComplete = false;

        btnOK.disable();

        // disable UI
        importAnimation = new ImportAnimation(scrollBox);
        importAnimation.setBackground(getBackground());
        addChild(importAnimation, false);

        Settings.CacheImageData.setValue(checkBoxPreloadImages.isChecked());
        Settings.CacheSpoilerData.setValue(checkBoxPreloadSpoiler.isChecked());
        Settings.ImportGpx.setValue(checkBoxImportGPX.isChecked());

        Settings.ImportPQsFromGeocachingCom.setValue(checkImportPQFromGC.isChecked());
        Settings.ImportRatings.setValue(checkBoxGcVote.isChecked());
        Settings.CompactDB.setValue(checkBoxCompactDB.isChecked());
        Settings.getInstance().acceptChanges();

        isCanceled.set(false);
        importStarted = true;

        ImportProgress importProgress = new ImportProgress(
                (textWithinProgressBar, textInExtraLabel, percent) -> GL.that.runOnGL(
                        () -> {
                            progressBar.fillBarAt(percent);
                            lblProgressMsg.setText(textInExtraLabel);
                            if (!textWithinProgressBar.equals(""))
                                progressBar.setText(textWithinProgressBar);
                        })
        );

        if (checkImportPQFromGC.isChecked()) {
            importProgress.addStep("importGC", 4);
        }
        if (checkImportFromCBServer.isChecked()) {
            importProgress.addStep("importCBServer", 4);
        }
        if (checkBoxImportGPX.isChecked()) {
            importProgress.addStep("ExtractZip", 1);
            importProgress.addStep("AnalyseGPX", 1);
            importProgress.addStep("ImportGPX", 4);
        }
        if (checkBoxGcVote.isChecked()) {
            importProgress.addStep("sendGcVote", 1);
            importProgress.addStep("importGcVote", 4);
        }
        if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked()) {
            importProgress.addStep("importImages", 4);
        }
        if (checkBoxCleanLogs.isChecked()) {
            importProgress.addStep("DeleteLogs", 1);
        }
        if (checkBoxCompactDB.isChecked()) {
            importProgress.addStep("CompactDB", 1);
        }

        new Thread(() -> {
            downloadAll(importProgress);
            if (allDownloadsComplete) {
                // without error
                finishImport();
            } else {
                if (isCanceled.get()) {
                    // canceled by user
                    finishImport();
                } else {
                    // with error
                    finishImport();
                }
            }
        }).start();

    }

    private void downloadAll(ImportProgress importProgress) {
        Importer importer = null;
        if (checkImportPQFromGC.isChecked()) {
            if (PqList != null && PqList.size() > 0) {
                // first step "importGC" : import pocket-queries from GroundSpeak
                ArrayList<PQ> downloadPqList = new ArrayList<>();
                for (PQ pq : PqList) {
                    if (pq.doDownload)
                        downloadPqList.add(pq);
                }
                importProgress.setStepFinalValue("importGC", downloadPqList.size());
                importAnimation.setAnimationType(AnimationType.Download);
                for (PQ pq : downloadPqList) {
                    importProgress.incrementStep("importGC", "Download: " + pq.name);
                    fetchPocketQuery(pq, Settings.PocketQueryFolder.getValue());
                    if (isCanceled.get()) {
                        break;
                    }
                    if (APIError != OK) {
                        isCanceled.set(true);
                        new ButtonDialog(LastAPIError, Translation.get("PQfromGC"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                    }
                }
                if (isCanceled.get()) {
                    // delete downloaded files
                    for (PQ pq : downloadPqList) {
                        AbstractFile pqFile = FileFactory.createFile(Settings.PocketQueryFolder.getValue() + "/" + pq.GUID + ".zip");
                        try {
                            pqFile.delete();
                        } catch (IOException ignored) {
                        }
                    }
                    importProgress.finishStep("importGC", "");
                    return;
                }
                importProgress.finishStep("importGC", "");
            }
        }
        if (isCanceled.get()) return;

        importAnimation.setAnimationType(AnimationType.Work);
        if (checkBoxImportGPX.isChecked()) {
            String pqFolderName = Settings.PocketQueryFolder.getValue();
            AbstractFile pqFolder = FileFactory.createFile(pqFolderName);
            if (pqFolder.exists()) {
                System.gc();
                long startTime = System.currentTimeMillis();
                CBDB.getInstance().beginTransaction();
                CBDB.getInstance().cacheList.clear();
                try {
                    importer = new Importer();
                    importer.importGpx(pqFolderName, importProgress, isCanceled::get);
                    CBDB.getInstance().setTransactionSuccessful();
                } catch (Exception exc) {
                    if (!isCanceled.get()) Log.err(sClass, "importGpx", exc);
                    importProgress.changeMsg("", "");
                }
                CBDB.getInstance().endTransaction();
                Log.debug(sClass, "Import  GPX Import took " + (System.currentTimeMillis() - startTime) + "ms");
                System.gc();

                // delete all files and directories from import folder, normally the PocketQuery subfolder
                AbstractFile[] fileList = pqFolder.listFiles();
                for (AbstractFile tmp : fileList) {
                    if (tmp.isDirectory()) {
                        ArrayList<AbstractFile> folderContent = FileIO.recursiveDirectoryReader(tmp, new ArrayList<>());
                        for (AbstractFile tmp2 : folderContent) {
                            try {
                                tmp2.delete();
                            } catch (IOException e) {
                                Log.err(sClass, "Delete " + tmp2.getAbsolutePath(), e);
                            }
                        }

                    }
                    try {
                        tmp.delete();
                        if (tmp.exists()) {
                            Log.err(sClass, "No deletion " + tmp.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        Log.err(sClass, "Delete " + tmp.getAbsolutePath(), e);
                    }
                }

            }
        }
        if (isCanceled.get()) return;

        if (checkBoxGcVote.isChecked()) {
            importAnimation.setAnimationType(AnimationType.Download);
            CBDB.getInstance().beginTransaction();
            try {
                if (importer == null) importer = new Importer();
                importer.importGcVote(FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue()), importProgress, isCanceled::get);
                CBDB.getInstance().setTransactionSuccessful();
            } catch (Exception exc) {
                if (!isCanceled.get()) {
                    Log.err(sClass, "importGcVote", exc);
                }
            }
            CBDB.getInstance().endTransaction();
            importAnimation.setAnimationType(AnimationType.Work);
        }
        if (isCanceled.get()) return;

        if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked()) {
            importAnimation.setAnimationType(AnimationType.Download);
            if (importer == null) importer = new Importer();
            importer.importImages(importProgress, isCanceled::get, checkBoxPreloadImages.isChecked(), checkBoxPreloadSpoiler.isChecked(), FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue()));
            importAnimation.setAnimationType(AnimationType.Work);
        }
        if (isCanceled.get()) return;

        if (checkBoxCleanLogs.isChecked()) {
            importProgress.setStepFinalValue("DeleteLogs", 1);
            importProgress.changeMsg("DeleteLogs", "");
            LogsTableDAO.getInstance().deleteOldLogs(Settings.LogMinCount.getValue(), Settings.LogMaxMonthAge.getValue());
            importProgress.finishStep("DeleteLogs", "");
        }
        if (isCanceled.get()) return;

        if (checkBoxCompactDB.isChecked()) {
            importProgress.setStepFinalValue("CompactDB", 1);
            importProgress.changeMsg("CompactDB", "");
            CBDB.getInstance().execSQL("vacuum");
            importProgress.finishStep("CompactDB", "");
        }
        if (isCanceled.get()) return;

        allDownloadsComplete = true;

    }

    private void finishImport() {
        importStarted = false;

        btnOK.enable();

        if (importAnimation != null) {
            removeChildDirect(importAnimation);
            importAnimation.dispose();
            importAnimation = null;
        }

        CacheListChangedListeners.getInstance().cacheListChanged();
        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());
    }

    private void copyGPX2PQFolder(final AbstractFile abstractFile) {
        if (abstractFile.getAbsolutePath().startsWith(Settings.PocketQueryFolder.getValue()))
            return;

        new WaitDialog(Translation.get("copyingFile"), new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void setIsCanceled() {

            }

            @Override
            public void run() {
                Copy copyHelper = new Copy(new CopyJobDefinition(abstractFile, Settings.PocketQueryFolder.getValue()));
                try {
                    copyHelper.run();
                } catch (IOException ex) {
                    Log.err(sClass, "copyGPX2PQ_Folder", ex);
                }
            }
        }).show();

    }

    private void animatedHeightChanged() {
        layout();
    }

    public static class Import_PqListItem extends ListViewItemBackground {

        Import_PqListItem(CB_RectF rec, int Index, final PQ pq) {
            super(rec, Index, "");

            CB_Label lblName = new CB_Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
            CB_Label lblInfo = new CB_Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

            lblName.setFont(Fonts.getSmall());
            lblInfo.setFont(Fonts.getBubbleSmall());

            lblName.setText(pq.name);

            String dateString = Translation.get("PQcreationDate") + ": " + (new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)).format(pq.lastGenerated);
            String Count = "\n" + Translation.get("Count") + ": " + pq.cacheCount;
            lblInfo.setText(dateString + Count); // + "  " + FileSize

            CB_CheckBox chk = new CB_CheckBox();
            chk.setRec(chk.scaleCenter(0.6f));
            chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UiSizes.getInstance().getMargin());
            chk.setY((this.getHalfHeight() - chk.getHalfHeight()) + chk.getHalfHeight());
            chk.setChecked(pq.doDownload);
            chk.setOnCheckChangedListener((view, isChecked) -> pq.doDownload = isChecked);
            this.addChild(lblName);
            this.addChild(lblInfo);
            this.addChild(chk);
        }
    }

    public class PqListAdapter implements Adapter {

        PqListAdapter() {
        }

        @Override
        public int getCount() {
            if (PqList != null)
                return PqList.size();
            else
                return 0;
        }

        @Override
        public ListViewItemBase getView(int position) {
            final PQ pq = PqList.get(position);
            if (itemRec == null) {
                itemHeight = UiSizes.getInstance().getChkBoxSize().getHeight() + UiSizes.getInstance().getChkBoxSize().getHalfHeight();
                float itemWidth = pqListCollapseBox.getInnerWidth();

                itemRec = new CB_RectF(new SizeF(itemWidth, itemHeight));
            }

            return new Import_PqListItem(itemRec, position, pq);

        }

        @Override
        public float getItemSize(int position) {
            if (itemHeight == -1)
                itemHeight = UiSizes.getInstance().getChkBoxSize().getHeight() + UiSizes.getInstance().getChkBoxSize().getHalfHeight();
            return itemHeight;
        }

    }
}
