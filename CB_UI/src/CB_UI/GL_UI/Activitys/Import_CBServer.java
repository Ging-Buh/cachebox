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
package CB_UI.GL_UI.Activitys;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CacheListChangedEventList;
import CB_Core.Import.BreakawayImportThread;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportCBServer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.ExportEntry;
import CB_Core.Types.ExportList;
import CB_Core.Types.Waypoint;
import CB_RpcCore.ClientCB.RpcClientCB;
import CB_RpcCore.Functions.RpcAnswer_ExportChangesToServer;
import CB_RpcCore.Functions.RpcAnswer_GetExportList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Activitys.APIs.ExportCBServerListItem;
import CB_UI.GL_UI.Activitys.APIs.ImportAPIListItem;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.CollapseBox;
import CB_UI_Base.GL_UI.Controls.CollapseBox.animatetHeightChangedListner;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.Spinner;
import CB_UI_Base.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_UI_Base.GL_UI.Controls.SpinnerAdapter;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.StringH;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import cb_rpc.Functions.RpcAnswer;
import de.cb.sqlite.CoreCursor;

public class Import_CBServer extends ActivityBase implements ProgressChangedEvent {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Import_CBServer.class);
    final boolean MAP_LINE_ACTIVE = false;
    boolean CBS_LINE_ACTIVE = false;
    boolean EXPORT_LINE_ACTIVE = false;
    boolean LOG_LINE_ACTIVE = true;
    boolean DB_LINE_ACTIVE = true;
    private V_ListView lvCBServer, lvExport;
    private Button bOK, bCancel, refreshCBServerList, refreshExportList;
    private float innerLeft, innerHeight, CollapseBoxHeight, CollapseBoxLogsMaxHeight;
    private Label lblTitle, lblCBServer, lblExportCBServer, lblImage, lblProgressMsg, lblLogs, lblCompact;
    private ProgressBar pgBar;
    private chkBox checkImportFromCBServer, checkBoxExportToCBServer, checkBoxPreloadImages, checkBoxCleanLogs, checkBoxCompactDB;
    private CollapseBox CBServerCollapseBox, ExportCollapseBox, LogCollapseBox;
    private Spinner spinner;

    private Timer mAnimationTimer;
    private long ANIMATION_TICK = 450;
    private int animationValue = 0;

    private Date ImportStart;

    private Boolean importStarted = false;

    private ArrayList<RpcAnswer_GetExportList.ListItem> cbServerExportList;
    private ExportList exportList;

    private CB_RectF itemRecCBServer;
    private float itemHeight = -1;

    private ScrollBox scrollBox;
    private ImportAnimation dis;

    public Import_CBServer() {
	this(0);
    }

    public Import_CBServer(int importType) {
	super(ActivityRec(), "importActivity");
	CBS_LINE_ACTIVE = !StringH.isEmpty(Config.CBS_IP.getValue());
	EXPORT_LINE_ACTIVE = true;
	CBS_LINE_ACTIVE = true;
	LOG_LINE_ACTIVE = true;
	DB_LINE_ACTIVE = true;
	CollapseBoxHeight = UI_Size_Base.that.getButtonHeight() * 6;
	scrollBox = new ScrollBox(ActivityRec());
	this.addChild(scrollBox);
	createOkCancelBtn();
	createTitleLine();
	scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
	scrollBox.setY(bOK.getMaxY() + margin);
	scrollBox.setBackground(this.getBackground());
	createCBServerLines();
	createCBServerCollapseBox();
	createExportLines();
	createExportCollapseBox();
	createImageLine();
	createLogLine();
	createLogCollapseBox();
	createCompactDBLine();

	initialForm();

	Layout();

	checkImportFromCBServer.setChecked(true);
	checkImportFromCBServer.setVisible(true);

	checkBoxExportToCBServer.setChecked(false);
	checkBoxExportToCBServer.setVisible(true);

	refreshCBServerList();
	refreshExportList();
	CBServerCollapseBox.expand();
	ExportCollapseBox.collapse();
    }

    @Override
    public void onShow() {
	ProgresssChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
	ProgresssChangedEventList.Remove(this);
    }

    private void createOkCancelBtn() {
	bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
	bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

	// Translations
	bOK.setText(Translation.Get("import"));
	bCancel.setText(Translation.Get("cancel"));

	this.addChild(bOK);
	bOK.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		ImportNow();
		return true;
	    }
	});

	this.addChild(bCancel);
	bCancel.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		if (BreakawayImportThread.isCanceld()) {
		    BreakawayImportThread.reset();
		    finish();
		    return true;
		}

		if (importStarted) {
		    GL_MsgBox.Show(Translation.Get("WontCancelImport"), Translation.Get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop, new OnMsgBoxClickListener() {

			@Override
			public boolean onClick(int which, Object data) {
			    if (which == GL_MsgBox.BUTTON_POSITIVE) {
				cancelImport();
			    }
			    return true;
			}
		    });
		} else
		    finish();
		return true;
	    }
	});

    }

    private void createTitleLine() {
	// Title+Progressbar

	float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

	lblTitle = new Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
	lblTitle.setFont(Fonts.getBig());
	float lblWidth = lblTitle.setText(Translation.Get("import")).getTextWidth();
	this.addChild(lblTitle);

	CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth, lineHeight);

	pgBar = new ProgressBar(rec, "ProgressBar");

	pgBar.setProgress(0, "");

	float SmallLineHeight = Fonts.MeasureSmall("Tg").height;

	lblProgressMsg = new Label(this.name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - SmallLineHeight, innerWidth - margin - margin, SmallLineHeight);

	lblProgressMsg.setFont(Fonts.getSmall());

	this.addChild(pgBar);
	this.addChild(lblProgressMsg);

    }

    private void createCBServerLines() {
	innerLeft = margin;

	checkImportFromCBServer = new chkBox("CBServer");
	checkImportFromCBServer.setX(innerLeft);
	checkImportFromCBServer.setY(innerHeight - checkImportFromCBServer.getHeight());
	if (!CBS_LINE_ACTIVE)
	    checkImportFromCBServer.setVisible(false);
	if (!CBS_LINE_ACTIVE)
	    checkImportFromCBServer.setHeight(0);
	lblCBServer = new Label(this.name + " lblCBServer", checkImportFromCBServer.getMaxX() + margin, checkImportFromCBServer.getY(), innerWidth - margin * 3 - checkImportFromCBServer.getWidth(), checkImportFromCBServer.getHeight());
	lblCBServer.setFont(Fonts.getNormal());
	lblCBServer.setText(Translation.Get("FromCBServer"));
	if (!CBS_LINE_ACTIVE)
	    lblCBServer.setVisible(false);
	if (!CBS_LINE_ACTIVE)
	    lblCBServer.setHeight(0);
	scrollBox.addChild(checkImportFromCBServer);
	scrollBox.addChild(lblCBServer);
    }

    private void createExportLines() {
	innerLeft = margin;

	checkBoxExportToCBServer = new chkBox("CBServer");
	checkBoxExportToCBServer.setX(innerLeft);
	checkBoxExportToCBServer.setY(innerHeight - checkBoxExportToCBServer.getHeight());
	if (!EXPORT_LINE_ACTIVE)
	    checkBoxExportToCBServer.setVisible(false);
	if (!EXPORT_LINE_ACTIVE)
	    checkBoxExportToCBServer.setHeight(0);
	lblExportCBServer = new Label(this.name + " lblExportCBServer", checkBoxExportToCBServer.getMaxX() + margin, checkBoxExportToCBServer.getY(), innerWidth - margin * 3 - checkBoxExportToCBServer.getWidth(), checkBoxExportToCBServer.getHeight());
	lblExportCBServer.setFont(Fonts.getNormal());
	lblExportCBServer.setText(Translation.Get("ToCBServer"));
	if (!EXPORT_LINE_ACTIVE)
	    lblExportCBServer.setVisible(false);
	if (!EXPORT_LINE_ACTIVE)
	    lblExportCBServer.setHeight(0);
	scrollBox.addChild(checkBoxExportToCBServer);
	scrollBox.addChild(lblExportCBServer);
    }

    private void createCBServerCollapseBox() {
	CB_RectF rec = new CB_RectF(lblCBServer.getX(), lblCBServer.getY() - CollapseBoxHeight - margin, lblCBServer.getWidth(), CollapseBoxHeight);

	CBServerCollapseBox = new CollapseBox(rec, "CBServerCollapse");
	CBServerCollapseBox.setBackground(this.getBackground());

	refreshCBServerList = new Button(name);
	refreshCBServerList.setWidth(CBServerCollapseBox.getWidth() - margin - margin);
	refreshCBServerList.setX(margin);
	refreshCBServerList.setY(margin);
	refreshCBServerList.setText(Translation.Get("refreshCBServerList"));
	refreshCBServerList.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		refreshCBServerList();
		return true;
	    }
	});

	lvCBServer = new V_ListView(new CB_RectF(leftBorder, refreshCBServerList.getMaxY() + margin, CBServerCollapseBox.getWidth(), CBServerCollapseBox.getHeight() - margin - margin - refreshCBServerList.getMaxY()), "");

	lvCBServer.setEmptyMsg(Translation.Get("EmptyCBServerList"));

	CBServerCollapseBox.addChild(lvCBServer);
	CBServerCollapseBox.addChild(refreshCBServerList);

	scrollBox.addChild(CBServerCollapseBox);
    }

    private void createExportCollapseBox() {
	CB_RectF rec = new CB_RectF(lblExportCBServer.getX(), lblExportCBServer.getY() - CollapseBoxHeight - margin, lblExportCBServer.getWidth(), CollapseBoxHeight);

	ExportCollapseBox = new CollapseBox(rec, "ExportCollapse");
	ExportCollapseBox.setBackground(this.getBackground());

	refreshExportList = new Button(name);
	refreshExportList.setWidth(ExportCollapseBox.getWidth() - margin - margin);
	refreshExportList.setX(margin);
	refreshExportList.setY(margin);
	refreshExportList.setText(Translation.Get("refreshExportList"));
	refreshExportList.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		refreshExportList();
		return true;
	    }
	});

	lvExport = new V_ListView(new CB_RectF(leftBorder, refreshExportList.getMaxY() + margin, ExportCollapseBox.getWidth(), ExportCollapseBox.getHeight() - margin - margin - refreshExportList.getMaxY()), "");

	lvExport.setEmptyMsg(Translation.Get("EmptyExportList"));

	ExportCollapseBox.addChild(lvExport);
	ExportCollapseBox.addChild(refreshExportList);

	scrollBox.addChild(ExportCollapseBox);
    }

    private void createImageLine() {
	// Preload Description Images
	checkBoxPreloadImages = new chkBox("Image");
	checkBoxPreloadImages.setX(innerLeft);
	checkBoxPreloadImages.setY(ExportCollapseBox.getY() - margin - checkBoxPreloadImages.getHeight());

	lblImage = new Label(this.name + " lblImage", checkBoxPreloadImages.getMaxX() + margin, checkBoxPreloadImages.getY(), innerWidth - margin * 3 - checkBoxPreloadImages.getWidth(), checkBoxPreloadImages.getHeight());
	lblImage.setFont(Fonts.getNormal());
	lblImage.setText(Translation.Get("PreloadImages"));

	scrollBox.addChild(checkBoxPreloadImages);
	scrollBox.addChild(lblImage);

    }

    private void createLogLine() {
	checkBoxCleanLogs = new chkBox("Image");
	checkBoxCleanLogs.setX(innerLeft);

	float yPos = lblImage.getY();

	checkBoxCleanLogs.setY(yPos - margin - checkBoxCleanLogs.getHeight());
	if (!LOG_LINE_ACTIVE) {
	    checkBoxCleanLogs.setVisible(false);
	    checkBoxCleanLogs.setHeight(0);
	}
	lblLogs = new Label(this.name + " lblLogs", checkBoxCleanLogs.getMaxX() + margin, checkBoxCleanLogs.getY(), innerWidth - margin * 3 - checkBoxCleanLogs.getWidth(), checkBoxCleanLogs.getHeight());
	lblLogs.setFont(Fonts.getNormal());
	lblLogs.setText(Translation.Get("DeleteLogs"));

	if (!LOG_LINE_ACTIVE) {
	    lblLogs.setVisible(false);
	    lblLogs.setHeight(0);
	}
	scrollBox.addChild(checkBoxCleanLogs);
	scrollBox.addChild(lblLogs);
    }

    ArrayList<String> values = new ArrayList<String>();

    private void createLogCollapseBox() {
	float SmallLineHeight = Fonts.MeasureSmall("Tg").height * 1.5f;
	CollapseBoxLogsMaxHeight = checkBoxCleanLogs.getHeight() + (SmallLineHeight * 3.5f) + (margin * 4);

	CB_RectF rec = new CB_RectF(lblLogs.getX(), lblLogs.getY() - CollapseBoxLogsMaxHeight - margin, lblLogs.getWidth(), CollapseBoxLogsMaxHeight);
	LogCollapseBox = new CollapseBox(rec, "LogCollapse");
	LogCollapseBox.setBackground(this.getBackground());
	scrollBox.addChild(LogCollapseBox);

	// ################################
	// create and fill LogLife spinner
	// ################################

	values.clear();
	values.add(Translation.Get("comboBoxLogLifeItem_0"));
	values.add(Translation.Get("comboBoxLogLifeItem_1"));
	values.add(Translation.Get("comboBoxLogLifeItem_2"));
	values.add(Translation.Get("comboBoxLogLifeItem_3"));
	values.add(Translation.Get("comboBoxLogLifeItem_4"));
	values.add(Translation.Get("comboBoxLogLifeItem_5"));
	values.add(Translation.Get("comboBoxLogLifeItem_6"));

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

	spinner = new Spinner(margin, LogCollapseBox.getHeight() - margin - checkBoxCleanLogs.getHeight(), LogCollapseBox.getWidth() - margin - margin, checkBoxCleanLogs.getHeight(), "LogLifeSpinner", adapter, new selectionChangedListner() {

	    @Override
	    public void selectionChanged(int index) {
		Config.LogMaxMonthAge.setValue(index);
		Config.AcceptChanges();
	    }
	});

	LogCollapseBox.addChild(spinner);

	Label lblButKeepLeast = new Label(this.name + " lblButKeepLeast", margin, spinner.getY() - margin - SmallLineHeight, LogCollapseBox.getInnerWidth(), SmallLineHeight);
	lblButKeepLeast.setText(Translation.Get("ButKeepLeast"));
	LogCollapseBox.addChild(lblButKeepLeast);

	final EditTextField input = new EditTextField(checkBoxCleanLogs.ScaleCenter(2), LogCollapseBox, this.name + " input");
	input.setHeight(SmallLineHeight * 2.5f);
	input.setText(String.valueOf(Config.LogMinCount.getValue()));
	input.setPos(margin, lblButKeepLeast.getY() - margin - input.getHeight());
	LogCollapseBox.addChild(input);

	// prevented Keyboard popup, show NumerikInputBox
	input.setOnscreenKeyboard(new OnscreenKeyboard() {
	    @Override
	    public void show(boolean visible) {
		if (visible) {
		    NumerikInputBox.Show(Translation.Get("ButKeepLeast"), Translation.Get("DeleteLogs"), Config.LogMinCount.getValue(), new returnValueListner() {

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
		}
	    }
	});

    }

    private void createCompactDBLine() {
	checkBoxCompactDB = new chkBox("Compact");
	checkBoxCompactDB.setX(innerLeft);
	checkBoxCompactDB.setY(LogCollapseBox.getY() - margin - checkBoxCompactDB.getHeight());

	if (!DB_LINE_ACTIVE) {
	    checkBoxCompactDB.setVisible(false);
	    checkBoxCompactDB.setHeight(0);
	}
	lblCompact = new Label(this.name + " lblCompact", lblImage.getMaxX() + margin, checkBoxCompactDB.getY(), innerWidth - margin * 3 - checkBoxCompactDB.getWidth(), checkBoxCompactDB.getHeight());
	lblCompact.setFont(Fonts.getNormal());
	lblCompact.setText(Translation.Get("CompactDB"));

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

	float yPos = lblLogs.getMaxY();

	checkBoxPreloadImages.setY(yPos + margin);
	lblImage.setY(yPos + margin);

	CBServerCollapseBox.setY(lblImage.getMaxY() + margin);
	checkImportFromCBServer.setY(CBServerCollapseBox.getMaxY() + margin);
	lblCBServer.setY(CBServerCollapseBox.getMaxY() + margin);

	ExportCollapseBox.setY(lblCBServer.getMaxY() + margin);
	lblExportCBServer.setY(ExportCollapseBox.getMaxY() + margin);
	checkBoxExportToCBServer.setY(ExportCollapseBox.getMaxY() + margin);

	innerHeight = lblExportCBServer.getMaxY() + margin;
	scrollBox.setVirtualHeight(innerHeight);
    }

    private void initialForm() {
	checkBoxPreloadImages.setChecked(Config.CacheImageData.getValue());
	checkImportFromCBServer.setOnCheckedChangeListener(checkImportFromCBServer_CheckStateChanged);
	checkBoxExportToCBServer.setOnCheckedChangeListener(checkBoxExportToCBServer_CheckStateChanged);

	checkBoxCompactDB.setChecked(DB_LINE_ACTIVE ? Config.CompactDB.getValue() : false);

	if (checkImportFromCBServer.isChecked()) {
	    CBServerCollapseBox.setAnimationHeight(CollapseBoxHeight);
	} else {
	    CBServerCollapseBox.setAnimationHeight(0);
	}
	if (checkBoxExportToCBServer.isChecked()) {
	    ExportCollapseBox.setAnimationHeight(CollapseBoxHeight);
	} else {
	    ExportCollapseBox.setAnimationHeight(0);
	    ExportCollapseBox.collapse();
	}

	CBServerCollapseBox.setAnimationListner(Animationlistner);
	ExportCollapseBox.setAnimationListner(Animationlistner);
	LogCollapseBox.setAnimationListner(Animationlistner);

	checkBoxCleanLogs.setChecked(LOG_LINE_ACTIVE ? Config.DeleteLogs.getValue() : false);

	checkBoxCleanLogs.setOnCheckedChangeListener(checkLog_CheckStateChanged);

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

    animatetHeightChangedListner Animationlistner = new animatetHeightChangedListner() {
	@Override
	public void animatedHeightChanged(float Height) {
	    Layout();
	}
    };

    private OnCheckedChangeListener checkLog_CheckStateChanged = new OnCheckedChangeListener() {

	@Override
	public void onCheckedChanged(chkBox view, boolean isChecked) {
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

    private OnCheckedChangeListener checkImportFromCBServer_CheckStateChanged = new OnCheckedChangeListener() {
	@Override
	public void onCheckedChanged(chkBox view, boolean isChecked) {
	    if (checkImportFromCBServer.isChecked()) {
		CBServerCollapseBox.expand();
	    } else {
		CBServerCollapseBox.collapse();
	    }
	}
    };

    private OnCheckedChangeListener checkBoxExportToCBServer_CheckStateChanged = new OnCheckedChangeListener() {
	@Override
	public void onCheckedChanged(chkBox view, boolean isChecked) {
	    if (checkBoxExportToCBServer.isChecked()) {
		ExportCollapseBox.expand();
	    } else {
		ExportCollapseBox.collapse();
	    }
	}
    };

    public class CustomAdapterCBServer implements Adapter {

	public CustomAdapterCBServer() {
	}

	public int getCount() {
	    if (cbServerExportList != null)
		return cbServerExportList.size();
	    else
		return 0;
	}

	@Override
	public ListViewItemBase getView(int position) {
	    final RpcAnswer_GetExportList.ListItem it = cbServerExportList.get(position);
	    if (itemRecCBServer == null) {
		itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
		float itemWidth = CBServerCollapseBox.getInnerWidth();

		itemRecCBServer = new CB_RectF(new SizeF(itemWidth, itemHeight));
	    }

	    return new ImportAPIListItem(itemRecCBServer, position, it);

	}

	@Override
	public float getItemSize(int position) {
	    if (itemHeight == -1)
		itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
	    return itemHeight;
	}

    }

    public class CustomAdapterExportCBServer implements Adapter {

	public CustomAdapterExportCBServer() {
	}

	public int getCount() {
	    if (exportList != null) {
		return exportList.size();
	    } else {
		return 0;
	    }
	}

	@Override
	public ListViewItemBase getView(int position) {
	    final ExportEntry it = exportList.get(position);
	    if (itemRecCBServer == null) {
		itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
		float itemWidth = ExportCollapseBox.getInnerWidth();

		itemRecCBServer = new CB_RectF(new SizeF(itemWidth, itemHeight));
	    }

	    return new ExportCBServerListItem(itemRecCBServer, position, it);
	}

	@Override
	public float getItemSize(int position) {
	    if (itemHeight == -1)
		itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
	    return itemHeight;
	}

    }

    private void refreshCBServerList() {

	lvCBServer.setBaseAdapter(null);
	lvCBServer.notifyDataSetChanged();
	refreshCBServerList.disable();

	Thread thread = new Thread() {
	    @Override
	    public void run() {
		// PqList = new ArrayList<PQ>();
		// PocketQuery.GetPocketQueryList(PqList);
		RpcClientCB rpc = new RpcClientCB();
		RpcAnswer answer = rpc.getExportList();

		if (answer != null) {
		    if (answer instanceof RpcAnswer_GetExportList) {
			cbServerExportList = ((RpcAnswer_GetExportList) answer).getList();
			GL_MsgBox.Show("RpcAntwort: " + answer.toString());
		    } else {
			cbServerExportList = null;
		    }
		} else {
		    cbServerExportList = null;
		}

		lvCBServer.setBaseAdapter(new CustomAdapterCBServer());
		lvCBServer.notifyDataSetChanged();

		stopTimer();
		lvCBServer.setEmptyMsg(Translation.Get("EmptyCBServerList"));

		refreshCBServerList.enable();
	    }

	};

	thread.start();

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

		String s = "";
		for (int i = 0; i < animationValue; i++) {
		    s += ".";
		}

		lvCBServer.setEmptyMsg(Translation.Get("LoadCBServerList") + s);

	    }

	}, 0, ANIMATION_TICK);

    }

    private void refreshExportList() {

	lvExport.setBaseAdapter(null);
	lvExport.notifyDataSetChanged();
	refreshExportList.disable();

	Thread thread = new Thread() {
	    @Override
	    public void run() {
		exportList = new ExportList();
		exportList.loadExportList();

		lvExport.setBaseAdapter(new CustomAdapterExportCBServer());
		lvExport.notifyDataSetChanged();

		stopTimer();
		lvExport.setEmptyMsg(Translation.Get("EmptyExportCBServerList"));

		refreshExportList.enable();
		if (exportList.size() > 0) {
		    checkBoxExportToCBServer.setChecked(true);
		}
	    }

	};

	thread.start();

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

		String s = "";
		for (int i = 0; i < animationValue; i++) {
		    s += ".";
		}

		lvExport.setEmptyMsg(Translation.Get("LoadExport CBServerList") + s);

	    }

	}, 0, ANIMATION_TICK);

    }

    private void stopTimer() {
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

	Config.CacheImageData.setValue(checkBoxPreloadImages.isChecked());

	Config.CompactDB.setValue(checkBoxCompactDB.isChecked());
	Config.AcceptChanges();
	String directoryPath = Config.PocketQueryFolder.getValue();
	// chk exist import folder
	File directory = new File(directoryPath);

	ImportThread(directoryPath, directory);

    }

    private volatile BreakawayImportThread importThread;

    public void ImportThread(final String directoryPath, final File directory) {
	importThread = new BreakawayImportThread() {
	    public void run() {
		importStarted = true;

		ImporterProgress ip = new ImporterProgress();

		try {
		    // Set Progress values
		    if (checkBoxExportToCBServer.isChecked()) {
			ip.addStep(ip.new Step("exportCBServer", 4));
		    }

		    if (checkImportFromCBServer.isChecked()) {
			ip.addStep(ip.new Step("importCBServer", 4));
		    }

		    if (checkBoxPreloadImages.isChecked()) {
			// ip.addStep(ip.new Step("importImageUrls", 4));
			ip.addStep(ip.new Step("importImages", 4));
		    }

		    if (checkBoxCleanLogs.isChecked()) {
			ip.addStep(ip.new Step("DeleteLogs", 1));
		    }

		    if (checkBoxCompactDB.isChecked()) {
			ip.addStep(ip.new Step("CompactDB", 1));
		    }

		    // begin import

		    dis.setAnimationType(AnimationType.Work);

		    if (checkBoxPreloadImages.isChecked()) {
			dis.setAnimationType(AnimationType.Download);
			int result = 0;

			if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
			    GL.that.Toast(ConnectionError.INSTANCE);
			    ip.ProgressChangeMsg("", "");
			    return;
			}

			if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
			    GL.that.Toast(ApiUnavailable.INSTANCE);
			    ip.ProgressChangeMsg("", "");
			    return;
			}

			if (BreakawayImportThread.isCanceld()) {
			    cancelImport();
			    ip.ProgressChangeMsg("", "");
			    return;
			}
			dis.setAnimationType(AnimationType.Work);
		    }

		    if (checkBoxExportToCBServer.isChecked()) {
			ip.setJobMax("exportCBServer", 1);
			ip.ProgressChangeMsg("Export CBServer", "");
			runExport();
			ip.ProgressInkrement("exportCBServer", "", true);
		    }

		    Thread.sleep(1000);

		    if (checkImportFromCBServer.isChecked()) {
			// Import from CBServer
			System.gc();
			ImportCBServer importCBServer = new ImportCBServer();

			long startTime = System.currentTimeMillis();

			try {

			    importCBServer.importCBServer(cbServerExportList, ip, checkBoxPreloadImages.isChecked());

			} catch (Exception exc) {
			    exc.printStackTrace();
			}

			if (BreakawayImportThread.isCanceld()) {
			    cancelImport();
			    ip.ProgressChangeMsg("", "");
			    return;
			}

			log.debug("Import CBServer took " + (System.currentTimeMillis() - startTime) + "ms");

			System.gc();
		    }

		    Thread.sleep(1000);
		    if (checkBoxCleanLogs.isChecked()) {
			ip.setJobMax("DeleteLogs", 1);
			ip.ProgressChangeMsg("DeleteLogs", "");
			Database.Data.DeleteOldLogs(Config.LogMinCount.getValue(), Config.LogMaxMonthAge.getValue());
			ip.ProgressInkrement("DeleteLogs", "", true);
		    }

		    if (checkBoxCompactDB.isChecked()) {
			ip.setJobMax("CompactDB", 1);
			ip.ProgressChangeMsg("CompactDB", "");
			Database.Data.execSQL("vacuum");
			ip.ProgressInkrement("CompactDB", "", true);
		    }

		} catch (InterruptedException e) {
		    // import canceld
		    cancelImport();
		    FilterProperties props = FilterProperties.LastFilter;
		    EditFilterSettings.ApplyFilter(props);
		    ip.ProgressChangeMsg("", "");
		    return;
		}

		if (BreakawayImportThread.isCanceld()) {
		    FilterProperties props = FilterProperties.LastFilter;
		    EditFilterSettings.ApplyFilter(props);
		    ip.ProgressChangeMsg("", "");
		    return;
		}

		finish();

		// finish close activity and notify changes

		CacheListChangedEventList.Call();

		Date Importfin = new Date();
		long ImportZeit = Importfin.getTime() - ImportStart.getTime();

		String Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "C " + String.valueOf(GPXFileImporter.LogCount) + "L in " + String.valueOf(ImportZeit);

		log.debug(Msg);

		FilterProperties props = FilterProperties.LastFilter;
		EditFilterSettings.ApplyFilter(props);

		GL.that.Toast(Msg, 3000);

	    }
	};

	importThread.setPriority(Thread.MAX_PRIORITY);
	ImportStart = new Date();
	importThread.start();
    }

    private void cancelImport() {
	if (importThread != null) {
	    importThread.cancel();
	    importThread = null;
	}

	importStarted = false;

	if (dis != null) {
	    this.removeChildsDirekt(dis);
	    dis.dispose();
	    dis = null;
	}
	bOK.enable();

	if (importThread != null && !importThread.isAlive()) {
	    this.finish();
	}
    }

    @Override
    public void ProgressChangedEventCalled(final String Message, final String ProgressMessage, final int Progress) {

	GL.that.RunOnGL(new IRunOnGL() {

	    @Override
	    public void run() {
		pgBar.setProgress(Progress);
		lblProgressMsg.setText(ProgressMessage);
		if (!Message.equals(""))
		    pgBar.setText(Message);
	    }
	});

    }

    protected void runExport() {
	ExportList toExport = new ExportList();
	// notwendige Informationen sammeln
	for (ExportEntry entry : exportList) {
	    switch (entry.changeType) {
	    case WaypointChanged:
	    case NewWaypoint:
		// Waypoint Informationen laden
		entry.waypoint = readWaypoint(entry.wpGcCode);
		break;
	    case NotesText:
		// Note Text laden
		entry.note = Database.GetNote(entry.cacheId);
		break;
	    case SolverText:
		// Solver Text laden
		entry.solver = Database.GetSolver(entry.cacheId);
		break;
	    default:
		break;
	    }
	    if (entry.toExport) {
		// nur die Einträge exportieren die markiert wurden
		toExport.add(entry);
	    }
	}
	// Export zm CB_Server auführen
	RpcClientCB client = new RpcClientCB();
	RpcAnswer answer = client.ExportChangesToServer(toExport);
	if ((answer != null) && (answer instanceof RpcAnswer_ExportChangesToServer)) {
	    // Export ohne Fehler -> Replicationseinträge entfernen
	    String sql = "delete from Replication";
	    Database.Data.execSQL(sql);
	    // Liste neu laden
	    exportList.loadExportList();
	    lvExport.setBaseAdapter(new CustomAdapterExportCBServer());
	    lvExport.notifyDataSetChanged();
	} else {
	    // Fehler beim Export
	    // TODO
	}
    }

    private Waypoint readWaypoint(String wpGcCode) {
	Waypoint result = null;
	CoreCursor reader = Database.Data.rawQuery(WaypointDAO.SQL_WP_FULL + " where GcCode = ?", new String[] { wpGcCode });
	reader.moveToFirst();
	while (!reader.isAfterLast()) {
	    WaypointDAO waypointDAO = new WaypointDAO();
	    Waypoint wp = waypointDAO.getWaypoint(reader, true);
	    result = wp;
	    reader.moveToNext();

	}
	reader.close();
	return result;
    }

}
