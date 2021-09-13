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
package de.droidcachebox.gdx.activities;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.ERROR;
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
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.Config;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.core.GroundspeakAPI.PQ;
import de.droidcachebox.database.Database;
import de.droidcachebox.ex_import.BreakawayImportThread;
import de.droidcachebox.ex_import.GPXFileImporter;
import de.droidcachebox.ex_import.Importer;
import de.droidcachebox.ex_import.ImporterProgress;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_CheckBox.OnCheckChangedListener;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.CollapseBox;
import de.droidcachebox.gdx.controls.CollapseBox.IAnimatedHeightChangedListener;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.ImportAnimation;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.Spinner;
import de.droidcachebox.gdx.controls.SpinnerAdapter;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListener;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.Copy;
import de.droidcachebox.utils.CopyRule;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.ProgressChangedEvent;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.log.Log;

public class Import extends ActivityBase implements ProgressChangedEvent {
    private static final String log = "Import";
    private final boolean MAP_LINE_ACTIVE = false;
    private final float CollapseBoxMaxHeight;
    private final long ANIMATION_TICK = 450;
    private final ScrollBox scrollBox;
    private Date ImportStart;
    private ArrayList<String> values = new ArrayList<>();
    private boolean PQ_LINE_ACTIVE = true;
    private boolean CBS_LINE_ACTIVE;
    private boolean GPX_LINE_ACTIVE = true;
    private boolean GCV_LINE_ACTIVE = true;
    private boolean LOG_LINE_ACTIVE = true;
    private boolean DB_LINE_ACTIVE = true;
    private boolean IMAGE_LINE_ACTIVE;
    private V_ListView lvPQs, lvCBServer;
    private CB_Button bOK;
    private CB_Button refreshPqList;
    private CB_Button refreshCBServerList;
    private CB_Button btnSelectFile;
    private float innerLeft, innerHeight, CollapseBoxHeight;
    private float CollapseBoxLogsMaxHeight;
    private CB_Label lblPQ;
    private CB_Label lblCBServer;
    private CB_Label lblGPX;
    private CB_Label lblGcVote;
    private CB_Label lblImage;
    private CB_Label lblSpoiler;
    private CB_Label lblMaps;
    private CB_Label lblProgressMsg;
    private CB_Label lblLogs;
    private CB_Label lblCompact;
    private ProgressBar pgBar;
    private CB_CheckBox checkImportPQfromGC, checkImportFromCBServer, checkBoxImportGPX, checkBoxGcVote, checkBoxPreloadImages, checkBoxPreloadSpoiler, checkBoxImportMaps, checkBoxCleanLogs, checkBoxCompactDB;
    private CollapseBox PQ_ListCollapseBox, CBServerCollapseBox, LogCollapseBox;
    private IAnimatedHeightChangedListener mAnimationListener = height -> animatedHeightChanged();
    private Spinner spinner;

    private final OnCheckChangedListener checkLog_CheckStateChanged = new OnCheckChangedListener() {

        @Override
        public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
            if (checkBoxCleanLogs.isChecked()) {
                LogCollapseBox.expand();
                spinner.setSelection(Config.LogMaxMonthAge.getValue());
            } else {
                LogCollapseBox.collapse();
            }

            Config.DeleteLogs.setValue(isChecked);
            Config.AcceptChanges();
        }
    };
    private Timer mAnimationTimer;
    private int animationValue = 0;
    private Boolean importStarted = false;
    private ArrayList<PQ> PqList;
    private final OnCheckChangedListener checkImportPQfromGC_CheckStateChanged = new OnCheckChangedListener() {
        @Override
        public void onCheckedChanged(CB_CheckBox view, boolean isChecked) {
            if (checkImportPQfromGC.isChecked()) {
                checkBoxImportGPX.setChecked(true);
                checkBoxImportGPX.setEnabled(false);
                PQ_ListCollapseBox.expand();
                if (lvPQs.getAllListSize() == 0)
                    refreshPqList();
            } else {
                checkBoxImportGPX.setEnabled(true);
                PQ_ListCollapseBox.collapse();
            }
        }
    };

    private CB_RectF itemRec;
    private CB_RectF itemRecCBServer;
    private float itemHeight = -1;
    private ImportAnimation dis;
    private volatile BreakawayImportThread importThread;

    public Import() {
        this(0);
    }

    public Import(int importType) {
        super("importActivity");
        // um direkt gleich den Import für eine bestimmte API starten zu können
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
        scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
        scrollBox.setY(bOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());
        if (PQ_LINE_ACTIVE) {
            if (!isAccessTokenInvalid()) {
                if (!isPremiumMember()) {
                    PQ_LINE_ACTIVE = false;
                }
            } else {
                MessageBox.show(Translation.get("Desc_ImportPQsFromGeocachingCom"), Translation.get("apiKeyInvalid"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
            }
        }
        Log.debug(log, "is Premium = " + PQ_LINE_ACTIVE);
        createPQLines();
        createCBServerLines();
        createPqCollapseBox();
        createCBServerCollapseBox();
        createGpxLine();
        createGcVoteLine();
        createImageLine();
        createMapLine();
        createLogLine();
        createLogCollapseBox();
        createCompactDBLine();

        initialForm();

        Layout();

        if (importType == MI_IMPORT_CBS) {
            checkImportFromCBServer.setChecked(true);
            checkImportFromCBServer.setVisible(true);
            CBServerCollapseBox.expand();
        } else if (importType == MI_IMPORT_GCV) {
            checkBoxGcVote.setChecked(true);
            checkBoxGcVote.setVisible(true);
        }
        // next 3 lines delete, if CBServer is integrated again
        checkImportFromCBServer.setChecked(false);
        checkImportFromCBServer.setVisible(false);
        lblCBServer.setVisible(false);

        // scrollBox.setBackground(new ColorDrawable(Color.RED));
    }

    @Override
    public void onShow() {
        ProgresssChangedEventList.add(this);
    }

    @Override
    public void onHide() {
        ProgresssChangedEventList.remove(this);
    }

    private void createOkCancelBtn() {
        bOK = new CB_Button(leftBorder, leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Import");
        CB_Button bCancel = new CB_Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Import");

        // Translations
        bOK.setText(Translation.get("import"));
        bCancel.setText(Translation.get("cancel"));

        this.addChild(bOK);
        bOK.setClickHandler((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        this.addChild(bCancel);
        bCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (BreakawayImportThread.isCanceled()) {
                BreakawayImportThread.reset();
                finish();
                return true;
            }

            if (importStarted) {
                MessageBox.show(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MessageBoxButton.YesNo, MessageBoxIcon.Stop, (which, data) -> {
                    if (which == MessageBox.BTN_LEFT_POSITIVE) {
                        cancelImport();
                    }
                    return true;
                });
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

        pgBar = new ProgressBar(rec, "ProgressBar");

        pgBar.setProgress(0, "");

        float progressLineHeight = Fonts.measureForSmallFont("Tg").height * 3;

        lblProgressMsg = new CB_Label(this.name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - progressLineHeight, innerWidth - margin - margin, progressLineHeight);
        lblProgressMsg.setFont(Fonts.getBubbleSmall());
        lblProgressMsg.setVAlignment(VAlignment.TOP);

        this.addChild(pgBar);
        this.addChild(lblProgressMsg);

    }

    private void createPQLines() {

        innerLeft = margin;

        checkImportPQfromGC = new CB_CheckBox();
        checkImportPQfromGC.setX(innerLeft);
        checkImportPQfromGC.setY(innerHeight - checkImportPQfromGC.getHeight());
        if (!PQ_LINE_ACTIVE) {
            checkImportPQfromGC.setVisible(false);
            checkImportPQfromGC.setHeight(0);
            checkImportPQfromGC.setChecked(false);
        }
        lblPQ = new CB_Label("lblPQ", checkImportPQfromGC.getMaxX() + margin, checkImportPQfromGC.getY(), innerWidth - margin * 3 - checkImportPQfromGC.getWidth(), checkImportPQfromGC.getHeight());
        lblPQ.setFont(Fonts.getNormal());
        lblPQ.setText(Translation.get("PQfromGC"));
        if (!PQ_LINE_ACTIVE) {
            lblPQ.setVisible(false);
            lblPQ.setHeight(0);
        }

        scrollBox.addChild(checkImportPQfromGC);
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

        PQ_ListCollapseBox = new CollapseBox(rec, "PqCollapse");
        PQ_ListCollapseBox.setBackground(this.getBackground());

        refreshPqList = new CB_Button(name);
        refreshPqList.setWidth(PQ_ListCollapseBox.getWidth() - margin - margin);
        refreshPqList.setX(margin);
        refreshPqList.setY(margin);
        refreshPqList.setText(Translation.get("refreshPqList"));
        refreshPqList.setClickHandler((v, x, y, pointer, button) -> {
            refreshPqList();
            return true;
        });

        lvPQs = new V_ListView(new CB_RectF(leftBorder, refreshPqList.getMaxY() + margin, PQ_ListCollapseBox.getWidth(), PQ_ListCollapseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

        lvPQs.setEmptyMsgItem(Translation.get("EmptyPqList"));

        PQ_ListCollapseBox.addChild(lvPQs);
        PQ_ListCollapseBox.addChild(refreshPqList);

        scrollBox.addChild(PQ_ListCollapseBox);
    }

    private void createCBServerCollapseBox() {
        CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollapseBoxHeight - margin, lblPQ.getWidth(), CollapseBoxHeight);

        CBServerCollapseBox = new CollapseBox(rec, "CBServerCollapse");
        CBServerCollapseBox.setBackground(this.getBackground());

        refreshCBServerList = new CB_Button(name);
        refreshCBServerList.setWidth(PQ_ListCollapseBox.getWidth() - margin - margin);
        refreshCBServerList.setX(margin);
        refreshCBServerList.setY(margin);
        refreshCBServerList.setText(Translation.get("refreshCBServerList"));
        refreshCBServerList.setClickHandler((v, x, y, pointer, button) -> {
            return true;
        });

        lvCBServer = new V_ListView(new CB_RectF(leftBorder, refreshPqList.getMaxY() + margin, CBServerCollapseBox.getWidth(), CBServerCollapseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

        lvCBServer.setEmptyMsgItem(Translation.get("EmptyCBServerList"));

        CBServerCollapseBox.addChild(lvCBServer);
        CBServerCollapseBox.addChild(refreshCBServerList);

        scrollBox.addChild(CBServerCollapseBox);
    }

    private void createGpxLine() {
        checkBoxImportGPX = new CB_CheckBox();
        checkBoxImportGPX.setX(innerLeft);
        checkBoxImportGPX.setY(PQ_ListCollapseBox.getY() - margin - checkBoxImportGPX.getHeight());

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
            new FileOrFolderPicker(Config.workPath + "/User", "*.gpx|*.zip", "", "", this::copyGPX2PQ_Folder).show();
            return true;
        });

        scrollBox.addChild(checkBoxImportGPX);
        scrollBox.addChild(lblGPX);
        scrollBox.addChild(btnSelectFile);
    }

    private void createGcVoteLine() {
        if (Config.GcVotePassword.getValue().length() == 0)
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

    private void createMapLine() {
        checkBoxImportMaps = new CB_CheckBox();
        checkBoxImportMaps.setX(innerLeft);
        checkBoxImportMaps.setY(checkBoxPreloadSpoiler.getY() - margin - checkBoxImportMaps.getHeight());

        lblMaps = new CB_Label(this.name + " lblMaps", checkBoxImportMaps.getMaxX() + margin, checkBoxImportMaps.getY(), innerWidth - margin * 3 - checkBoxImportMaps.getWidth(), checkBoxImportMaps.getHeight());
        lblMaps.setFont(Fonts.getNormal());
        lblMaps.setText(Translation.get("Maps"));

        // wieder einschalten wenn Implementiert
        // scrollBox.addChild(checkBoxImportMaps);
        // scrollBox.addChild(lblMaps);
    }

    private void createLogLine() {
        checkBoxCleanLogs = new CB_CheckBox();
        checkBoxCleanLogs.setX(innerLeft);

        float yPos = MAP_LINE_ACTIVE ? checkBoxImportMaps.getY() : checkBoxPreloadSpoiler.getY();

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
        LogCollapseBox = new CollapseBox(rec, "LogCollapse");
        LogCollapseBox.setBackground(this.getBackground());
        scrollBox.addChild(LogCollapseBox);

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

        spinner = new Spinner(margin, LogCollapseBox.getHeight() - margin - checkBoxCleanLogs.getHeight(), LogCollapseBox.getWidth() - margin - margin, checkBoxCleanLogs.getHeight(), "ImportDeleteLogsTitle", adapter, index -> {
            Config.LogMaxMonthAge.setValue(index);
            Config.AcceptChanges();
        });

        LogCollapseBox.addChild(spinner);

        CB_Label lblButKeepLeast = new CB_Label(this.name + " lblButKeepLeast", margin, spinner.getY() - margin - SmallLineHeight, LogCollapseBox.getInnerWidth(), SmallLineHeight);
        lblButKeepLeast.setText(Translation.get("ButKeepLeast"));
        LogCollapseBox.addChild(lblButKeepLeast);

        final EditTextField input = new EditTextField(checkBoxCleanLogs.scaleCenter(2), LogCollapseBox, "*" + Translation.get("ButKeepLeast"));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        input.setHeight(SmallLineHeight * 2.5f);
        input.setText(String.valueOf(Config.LogMinCount.getValue()));
        input.setPos(margin, lblButKeepLeast.getY() - margin - input.getHeight());
        LogCollapseBox.addChild(input);
        input.setClickHandler((v, x, y, pointer, button) -> {
            NumericInputBox.Show(Translation.get("ButKeepLeast"), Translation.get("DeleteLogs"), Config.LogMinCount.getValue(), new IReturnValueListener() {
                @Override
                public void returnValue(int value) {
                    Config.LogMinCount.setValue(value);
                    Config.AcceptChanges();
                    input.setText(String.valueOf(value));
                }

                @Override
                public void cancelClicked() {
                }

            });
            return true;
        });
    }

    private void createCompactDBLine() {
        checkBoxCompactDB = new CB_CheckBox();
        checkBoxCompactDB.setX(innerLeft);
        checkBoxCompactDB.setY(LogCollapseBox.getY() - margin - checkBoxCompactDB.getHeight());

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

    private void Layout() {

        checkBoxCompactDB.setY(margin);
        lblCompact.setY(margin);

        LogCollapseBox.setY(checkBoxCompactDB.getMaxY() + margin);

        checkBoxCleanLogs.setY(LogCollapseBox.getMaxY() + margin);
        lblLogs.setY(LogCollapseBox.getMaxY() + margin);

        checkBoxImportMaps.setY(lblLogs.getMaxY() + margin);
        lblMaps.setY(lblLogs.getMaxY() + margin);

        float yPos = MAP_LINE_ACTIVE ? lblMaps.getMaxY() : lblLogs.getMaxY();

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

        CBServerCollapseBox.setY(checkBoxImportGPX.getMaxY() + margin);

        checkImportFromCBServer.setY(CBServerCollapseBox.getMaxY() + margin);
        lblCBServer.setY(CBServerCollapseBox.getMaxY() + margin);

        PQ_ListCollapseBox.setY(lblCBServer.getMaxY() + margin);
        // PQ_ListCollapseBox.setHeight(CollapseBoxHeight);

        checkImportPQfromGC.setY(PQ_ListCollapseBox.getMaxY() + margin);
        lblPQ.setY(PQ_ListCollapseBox.getMaxY() + margin);

        innerHeight = lblPQ.getMaxY() + margin;
        scrollBox.setVirtualHeight(innerHeight);
    }

    private void initialForm() {
        checkBoxImportMaps.setChecked(MAP_LINE_ACTIVE ? Config.CacheMapData.getValue() : false);
        checkBoxPreloadImages.setChecked(Config.CacheImageData.getValue());
        checkBoxPreloadSpoiler.setChecked(Config.CacheSpoilerData.getValue());
        checkBoxImportGPX.setChecked(GPX_LINE_ACTIVE ? Config.ImportGpx.getValue() : false);
        checkImportPQfromGC.setOnCheckChangedListener(checkImportPQfromGC_CheckStateChanged);
        checkBoxGcVote.setChecked(GCV_LINE_ACTIVE ? Config.ImportRatings.getValue() : false);

        checkImportPQfromGC.setChecked(PQ_LINE_ACTIVE ? Config.ImportPQsFromGeocachingCom.getValue() : false);
        checkImportPQfromGC.setEnabled(true);

        if (checkImportPQfromGC.isChecked()) {
            checkBoxImportGPX.setChecked(GPX_LINE_ACTIVE);
            checkBoxImportGPX.setEnabled(false);
        }
        checkBoxCompactDB.setChecked(DB_LINE_ACTIVE ? Config.CompactDB.getValue() : false);

        checkBoxPreloadSpoiler.setEnable(true);
        lblSpoiler.setTextColor(COLOR.getFontColor());
        if (checkImportPQfromGC.isChecked()) {
            PQ_ListCollapseBox.setAnimationHeight(CollapseBoxMaxHeight);
        } else {
            PQ_ListCollapseBox.setAnimationHeight(0);
        }

        if (checkImportFromCBServer.isChecked()) {
            CBServerCollapseBox.setAnimationHeight(CollapseBoxHeight);
        } else {
            CBServerCollapseBox.setAnimationHeight(0);
        }

        PQ_ListCollapseBox.setAnimationListener(mAnimationListener);
        CBServerCollapseBox.setAnimationListener(mAnimationListener);
        LogCollapseBox.setAnimationListener(mAnimationListener);

        checkBoxCleanLogs.setChecked(LOG_LINE_ACTIVE ? Config.DeleteLogs.getValue() : false);

        checkBoxCleanLogs.setOnCheckChangedListener(checkLog_CheckStateChanged);

        if (checkBoxCleanLogs.isChecked()) {
            LogCollapseBox.setAnimationHeight(CollapseBoxLogsMaxHeight);

            // validate value
            int value = Config.LogMaxMonthAge.getValue();
            if (value > 6) {
                Config.LogMaxMonthAge.setValue(6);
                Config.AcceptChanges();
            }

            spinner.setSelection(Config.LogMaxMonthAge.getValue());
        } else {
            LogCollapseBox.setAnimationHeight(0);
        }

    }

    private void refreshPqList() {

        lvPQs.setAdapter(null);
        lvPQs.notifyDataSetChanged();
        refreshPqList.disable();

        new Thread(() -> {
            PqList = fetchPocketQueryList();
            if (APIError != OK) {
                MessageBox.show(LastAPIError, Translation.get("PQfromGC"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
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

    private void ImportNow() {
        // disable btn
        bOK.disable();

        // disable UI
        dis = new ImportAnimation(scrollBox);
        dis.setBackground(getBackground());

        this.addChild(dis, false);

        Config.CacheMapData.setValue(checkBoxImportMaps.isChecked());
        Config.CacheImageData.setValue(checkBoxPreloadImages.isChecked());
        Config.CacheSpoilerData.setValue(checkBoxPreloadSpoiler.isChecked());
        Config.ImportGpx.setValue(checkBoxImportGPX.isChecked());

        Config.ImportPQsFromGeocachingCom.setValue(checkImportPQfromGC.isChecked());
        Config.ImportRatings.setValue(checkBoxGcVote.isChecked());
        Config.CompactDB.setValue(checkBoxCompactDB.isChecked());
        Config.AcceptChanges();
        String directoryPath = Config.PocketQueryFolder.getValue();
        // chk exist import folder
        AbstractFile directory = FileFactory.createFile(directoryPath);

        ImportThread(directoryPath, directory);

    }

    private void ImportThread(final String directoryPath, final AbstractFile directory) {
        importThread = new BreakawayImportThread() {
            @Override
            public void run() {
                importStarted = true;

                Importer importer = new Importer();
                ImporterProgress ip = new ImporterProgress();

                try {
                    // Set Progress values
                    if (checkImportPQfromGC.isChecked()) {
                        ip.addStep(new ImporterProgress.Step("importGC", 4));
                    }
                    if (checkImportFromCBServer.isChecked()) {
                        ip.addStep(new ImporterProgress.Step("importCBServer", 4));
                    }
                    if (checkBoxImportGPX.isChecked()) {
                        ip.addStep(new ImporterProgress.Step("ExtractZip", 1));
                        ip.addStep(new ImporterProgress.Step("AnalyseGPX", 1));
                        ip.addStep(new ImporterProgress.Step("ImportGPX", 4));
                    }
                    if (checkBoxGcVote.isChecked()) {
                        ip.addStep(new ImporterProgress.Step("sendGcVote", 1));
                        ip.addStep(new ImporterProgress.Step("importGcVote", 4));
                    }

                    if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked()) {
                        // ip.addStep(ip.new Step("importImageUrls", 4));
                        ip.addStep(new ImporterProgress.Step("importImages", 4));
                    }

                    if (checkBoxCleanLogs.isChecked()) {
                        ip.addStep(new ImporterProgress.Step("DeleteLogs", 1));
                    }

                    if (checkBoxCompactDB.isChecked()) {
                        ip.addStep(new ImporterProgress.Step("CompactDB", 1));
                    }

                    // begin import

                    if (checkImportPQfromGC.isChecked()) {

                        if (PqList != null && PqList.size() > 0) {

                            // PQ-List von nicht Downloadbaren PQs befreien

                            ArrayList<PQ> downloadPqList = new ArrayList<>();

                            for (PQ pq : PqList) {
                                if (pq.doDownload)
                                    downloadPqList.add(pq);
                            }


                            ip.setJobMax("importGC", downloadPqList.size());

                            dis.setAnimationType(AnimationType.Download);

                            Iterator<PQ> iterator = downloadPqList.iterator();
                            if (iterator.hasNext()) {
                                do {
                                    if (BreakawayImportThread.isCanceled()) {
                                        cancelImport();
                                        ip.ProgressChangeMsg("", "");
                                        return;
                                    }

                                    PQ pq = iterator.next();

                                    if (pq.doDownload) {
                                        ip.ProgressInkrement("importGC", "Download: " + pq.name, false);
                                        fetchPocketQuery(pq, Config.PocketQueryFolder.getValue());
                                        if (APIError != OK) {
                                            MessageBox.show(LastAPIError, Translation.get("PQfromGC"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                                        }
                                    }

                                } while (iterator.hasNext());
                            }

                            if (downloadPqList.size() == 0) {
                                ip.ProgressInkrement("importGC", "", true);
                            }
                        }
                    }

                    try {
                        dis.setAnimationType(AnimationType.Work);
                    } catch (Exception ignored) {
                    }

                    // Importiere alle GPX Files im Import Folder, auch in ZIP verpackte
                    if (checkBoxImportGPX.isChecked() && directory.exists()) {

                        System.gc();

                        long startTime = System.currentTimeMillis();

                        Database.Data.sql.beginTransaction();
                        Database.Data.cacheList.clear();
                        try {
                            importer.importGpx(directoryPath, ip);
                            Database.Data.sql.setTransactionSuccessful();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                            Database.Data.sql.endTransaction();
                            cancelImport();
                            ip.ProgressChangeMsg("", "");
                            return;
                        }
                        Database.Data.sql.endTransaction();

                        if (BreakawayImportThread.isCanceled()) {
                            cancelImport();
                            ip.ProgressChangeMsg("", "");
                            return;
                        }

                        Log.debug(log, "Import  GPX Import took " + (System.currentTimeMillis() - startTime) + "ms");

                        System.gc();

                        // delete all files and directories from import folder, normally the PocketQuery subfolder
                        AbstractFile[] filelist = directory.listFiles();
                        for (AbstractFile tmp : filelist) {
                            if (tmp.isDirectory()) {
                                ArrayList<AbstractFile> ordnerInhalt = FileIO.recursiveDirectoryReader(tmp, new ArrayList<>());
                                for (AbstractFile tmp2 : ordnerInhalt) {
                                    try {
                                        tmp2.delete();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                            try {
                                tmp.delete();
                                if (tmp.exists()) {
                                    Log.err(log, "Delete " + tmp.getAbsolutePath());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    if (checkBoxGcVote.isChecked()) {
                        dis.setAnimationType(AnimationType.Download);
                        Database.Data.sql.beginTransaction();
                        try {
                            importer.importGcVote(FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue()), ip);

                            Database.Data.sql.setTransactionSuccessful();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                        dis.setAnimationType(AnimationType.Work);
                        Database.Data.sql.endTransaction();
                        if (BreakawayImportThread.isCanceled()) {
                            cancelImport();
                            ip.ProgressChangeMsg("", "");
                            return;
                        }
                    }

                    if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked()) {
                        dis.setAnimationType(AnimationType.Download);
                        int result = importer.importImages(ip, checkBoxPreloadImages.isChecked(), checkBoxPreloadSpoiler.isChecked(), FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue()));

                        if (result == ERROR) {
                            GL.that.toast(LastAPIError);
                            ip.ProgressChangeMsg("", "");
                            return;
                        }

                        if (BreakawayImportThread.isCanceled()) {
                            cancelImport();
                            ip.ProgressChangeMsg("", "");
                            return;
                        }
                        if (dis != null)
                            dis.setAnimationType(AnimationType.Work);
                    }

                    Thread.sleep(1000);
                    if (checkBoxImportMaps.isChecked())
                        importer.importMaps();

                    Thread.sleep(1000);
                    if (checkBoxCleanLogs.isChecked()) {
                        ip.setJobMax("DeleteLogs", 1);
                        ip.ProgressChangeMsg("DeleteLogs", "");
                        Database.Data.deleteOldLogs(Config.LogMinCount.getValue(), Config.LogMaxMonthAge.getValue());
                        ip.ProgressInkrement("DeleteLogs", "", true);
                    }

                    if (checkBoxCompactDB.isChecked()) {
                        ip.setJobMax("CompactDB", 1);
                        ip.ProgressChangeMsg("CompactDB", "");
                        Database.Data.sql.execSQL("vacuum");
                        ip.ProgressInkrement("CompactDB", "", true);
                    }

                } catch (InterruptedException e) {
                    // import canceld
                    cancelImport();
                    FilterProperties props = FilterInstances.getLastFilter();
                    EditFilterSettings.applyFilter(props);
                    ip.ProgressChangeMsg("", "");
                    return;
                }

                if (BreakawayImportThread.isCanceled()) {
                    FilterProperties props = FilterInstances.getLastFilter();
                    EditFilterSettings.applyFilter(props);
                    ip.ProgressChangeMsg("", "");
                    return;
                }

                finish();

            }
        };

        importThread.setPriority(Thread.MAX_PRIORITY);
        ImportStart = new Date();
        importThread.start();
    }

    @Override
    public void finish() {
        super.finish();

        // finish close activity and notify changes

        CacheListChangedListeners.getInstance().cacheListChanged();
        String Msg;

        if (ImportStart != null) {
            Date Importfin = new Date();
            long ImportZeit = Importfin.getTime() - ImportStart.getTime();
            Msg = "Import " + GPXFileImporter.CacheCount + "Cache " + GPXFileImporter.LogCount + "Logs in " + ImportZeit;
        } else {
            Msg = "Import canceld";
        }

        Log.debug(log, Msg);

        FilterProperties props = FilterInstances.getLastFilter();
        EditFilterSettings.applyFilter(props);

        // an der richtigen Stelle ausgeben, kommt auch bei CBServer import: GL.that.toast(Msg, 3000);

    }

    private void cancelImport() {
        if (importThread != null) {
            importThread.cancel();
            importThread = null;
        }

        importStarted = false;

        if (dis != null) {
            this.removeChildDirect(dis);
            dis.dispose();
            dis = null;
        }
        bOK.enable();

        if (importThread != null && !importThread.isAlive()) {
            this.finish();
        }
    }

    @Override
    public void progressChanged(final String Message, final String ProgressMessage, final int Progress) {

        GL.that.RunOnGL(() -> {
            pgBar.setPogress(Progress);
            lblProgressMsg.setText(ProgressMessage);
            if (!Message.equals(""))
                pgBar.setText(Message);
        });

    }

    private void copyGPX2PQ_Folder(final AbstractFile abstractFile) {
        // disable UI
        dis = new ImportAnimation(scrollBox);
        dis.setBackground(getBackground());

        this.addChild(dis, false);

        dis.setAnimationType(AnimationType.Work);

        Thread copyThread = new Thread(() -> {
            CopyRule rule = new CopyRule(abstractFile, Config.PocketQueryFolder.getValue());
            Copy copyHelper = new Copy(rule);
            try {
                copyHelper.Run();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cancelImport();
        });
        copyThread.start();
    }

    private void animatedHeightChanged() {
        Layout();
    }

    public static class Import_PqListItem extends ListViewItemBackground {

        Import_PqListItem(CB_RectF rec, int Index, final PQ pq) {
            super(rec, Index, "");

            CB_Label lblName = new CB_Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
            CB_Label lblInfo = new CB_Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

            lblName.setFont(Fonts.getSmall());
            lblInfo.setFont(Fonts.getBubbleSmall());

            lblName.setText(pq.name);

            SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
            String dateString = Translation.get("PQcreationDate") + ": " + postFormater.format(pq.lastGenerated);
            //DecimalFormat df = new DecimalFormat("###.##");
            //String FileSize = df.format(pq.sizeMB) + " MB";
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
                float itemWidth = PQ_ListCollapseBox.getInnerWidth();

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
