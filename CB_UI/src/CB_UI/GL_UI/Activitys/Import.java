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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.PocketQuery;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.Import.BreakawayImportThread;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportCBServer;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_RpcCore.ClientCB.RpcClientCB;
import CB_RpcCore.Functions.RpcAnswer_GetExportList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Activitys.APIs.ImportAPIListItem;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.CollapseBox;
import CB_UI_Base.GL_UI.Controls.CollapseBox.IAnimatedHeightChangedListener;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.Spinner;
import CB_UI_Base.GL_UI.Controls.Spinner.ISelectionChangedListener;
import CB_UI_Base.GL_UI.Controls.SpinnerAdapter;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.GL_UI.Controls.chkBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox.IReturnValueListener;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.StringH;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.CopyHelper.Copy;
import CB_Utils.Util.CopyHelper.CopyRule;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import cb_rpc.Functions.RpcAnswer;

public class Import extends ActivityBase implements ProgressChangedEvent {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Import.class);
	final boolean MAP_LINE_ACTIVE = false;
	boolean PQ_LINE_ACTIVE = true;
	boolean CBS_LINE_ACTIVE = false;
	boolean GPX_LINE_ACTIVE = true;
	boolean GCV_LINE_ACTIVE = true;
	boolean LOG_LINE_ACTIVE = true;
	boolean DB_LINE_ACTIVE = true;
	private int importType = 0; // um direkt gleich den Import für eine bestimmte API starten zu können

	private V_ListView lvPQs, lvCBServer;
	private Button bOK, bCancel, refreshPqList, refreshCBServerList, btnSelectFile;
	private float innerLeft, innerHeight, CollapseBoxHeight;
	private final float CollapseBoxMaxHeight;
	private float CollapseBoxLogsMaxHeight;
	private Label lblTitle, lblPQ, lblCBServer, lblGPX, lblGcVote, lblImage, lblSpoiler, lblMaps, lblProgressMsg, lblLogs, lblCompact;
	private ProgressBar pgBar;
	private chkBox checkImportPQfromGC, checkImportFromCBServer, checkBoxImportGPX, checkBoxGcVote, checkBoxPreloadImages, checkBoxPreloadSpoiler, checkBoxImportMaps, checkBoxCleanLogs, checkBoxCompactDB;
	private CollapseBox PQ_ListCollapseBox, CBServerCollapseBox, LogCollapseBox;
	private Spinner spinner;

	private Timer mAnimationTimer;
	private final long ANIMATION_TICK = 450;
	private int animationValue = 0;

	protected Date ImportStart;

	private Boolean importStarted = false;

	private ArrayList<PQ> PqList;
	private ArrayList<RpcAnswer_GetExportList.ListItem> cbServerExportList;

	private CB_RectF itemRec;
	private CB_RectF itemRecCBServer;
	private float itemHeight = -1;

	private final ScrollBox scrollBox;
	private ImportAnimation dis;

	public Import() {
		this(0);
	}

	public Import(int importType) {
		super(ActivityRec(), "importActivity");
		this.importType = importType;
		CBS_LINE_ACTIVE = !StringH.isEmpty(Config.CBS_IP.getValue());
		switch (importType) {
		case MenuID.MI_IMPORT_GS_PQ:
			PQ_LINE_ACTIVE = true;
			CBS_LINE_ACTIVE = false;
			GPX_LINE_ACTIVE = false;
			GCV_LINE_ACTIVE = false;
			LOG_LINE_ACTIVE = true;
			DB_LINE_ACTIVE = true;
			break;
		case MenuID.MI_IMPORT_CBS:
			PQ_LINE_ACTIVE = false;
			CBS_LINE_ACTIVE = true;
			GPX_LINE_ACTIVE = false;
			GCV_LINE_ACTIVE = false;
			LOG_LINE_ACTIVE = true;
			DB_LINE_ACTIVE = true;
			break;
		case MenuID.MI_IMPORT_GPX:
			PQ_LINE_ACTIVE = false;
			CBS_LINE_ACTIVE = false;
			GPX_LINE_ACTIVE = true;
			GCV_LINE_ACTIVE = false;
			LOG_LINE_ACTIVE = true;
			DB_LINE_ACTIVE = true;
			break;
		case MenuID.MI_IMPORT_GCV:
			PQ_LINE_ACTIVE = false;
			CBS_LINE_ACTIVE = false;
			GPX_LINE_ACTIVE = false;
			GCV_LINE_ACTIVE = true;
			LOG_LINE_ACTIVE = true;
			DB_LINE_ACTIVE = true;
		}

		CollapseBoxMaxHeight = CollapseBoxHeight = UI_Size_Base.that.getButtonHeight() * 6;
		innerHeight = 1000;
		scrollBox = new ScrollBox(ActivityRec());
		this.addChild(scrollBox);
		createOkCancelBtn();
		createTitleLine();
		scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
		scrollBox.setY(bOK.getMaxY() + margin);
		scrollBox.setBackground(this.getBackground());
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

		if (importType == MenuID.MI_IMPORT_GS_PQ) {
			checkImportPQfromGC.setChecked(true);
			checkImportPQfromGC.setVisible(true);
			refreshPqList();
			PQ_ListCollapseBox.expand();
		} else if (importType == MenuID.MI_IMPORT_CBS) {
			checkImportFromCBServer.setChecked(true);
			checkImportFromCBServer.setVisible(true);
			refreshCBServerList();
			CBServerCollapseBox.expand();
		} else if (importType == MenuID.MI_IMPORT_GPX) {
			checkBoxImportGPX.setChecked(true);
			checkBoxImportGPX.setVisible(true);
		} else if (importType == MenuID.MI_IMPORT_GCV) {
			checkBoxGcVote.setChecked(true);
			checkBoxGcVote.setVisible(true);
		}

		// scrollBox.setBackground(new ColorDrawable(Color.RED));
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
				if (BreakawayImportThread.isCanceled()) {
					BreakawayImportThread.reset();
					finish();
					return true;
				}

				if (importStarted) {
					GL_MsgBox.Show(Translation.Get("WantCancelImport"), Translation.Get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop, new OnMsgBoxClickListener() {

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

		float progressLineHeight = Fonts.MeasureSmall("Tg").height * 3;

		lblProgressMsg = new Label(this.name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - progressLineHeight, innerWidth - margin - margin, progressLineHeight);
		lblProgressMsg.setFont(Fonts.getBubbleSmall());
		lblProgressMsg.setVAlignment(VAlignment.TOP);

		this.addChild(pgBar);
		this.addChild(lblProgressMsg);

	}

	private void createPQLines() {

		innerLeft = margin;

		checkImportPQfromGC = new chkBox("PQ");
		checkImportPQfromGC.setX(innerLeft);
		checkImportPQfromGC.setY(innerHeight - checkImportPQfromGC.getHeight());
		if (!PQ_LINE_ACTIVE) {
			checkImportPQfromGC.setVisible(false);
			checkImportPQfromGC.setHeight(0);
		}
		lblPQ = new Label(this.name + " lblPQ", checkImportPQfromGC.getMaxX() + margin, checkImportPQfromGC.getY(), innerWidth - margin * 3 - checkImportPQfromGC.getWidth(), checkImportPQfromGC.getHeight());
		lblPQ.setFont(Fonts.getNormal());
		lblPQ.setText(Translation.Get("PQfromGC"));
		if (!PQ_LINE_ACTIVE) {
			lblPQ.setVisible(false);
			lblPQ.setHeight(0);
		}

		scrollBox.addChild(checkImportPQfromGC);
		scrollBox.addChild(lblPQ);
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

	private void createPqCollapseBox() {
		CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollapseBoxHeight - margin, lblPQ.getWidth(), CollapseBoxHeight);

		PQ_ListCollapseBox = new CollapseBox(rec, "PqCollapse");
		PQ_ListCollapseBox.setBackground(this.getBackground());

		refreshPqList = new Button(name);
		refreshPqList.setWidth(PQ_ListCollapseBox.getWidth() - margin - margin);
		refreshPqList.setX(margin);
		refreshPqList.setY(margin);
		refreshPqList.setText(Translation.Get("refreshPqList"));
		refreshPqList.setOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				refreshPqList();
				return true;
			}
		});

		lvPQs = new V_ListView(new CB_RectF(leftBorder, refreshPqList.getMaxY() + margin, PQ_ListCollapseBox.getWidth(), PQ_ListCollapseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

		lvPQs.setEmptyMsg(Translation.Get("EmptyPqList"));

		PQ_ListCollapseBox.addChild(lvPQs);
		PQ_ListCollapseBox.addChild(refreshPqList);

		scrollBox.addChild(PQ_ListCollapseBox);
	}

	private void createCBServerCollapseBox() {
		CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollapseBoxHeight - margin, lblPQ.getWidth(), CollapseBoxHeight);

		CBServerCollapseBox = new CollapseBox(rec, "CBServerCollapse");
		CBServerCollapseBox.setBackground(this.getBackground());

		refreshCBServerList = new Button(name);
		refreshCBServerList.setWidth(PQ_ListCollapseBox.getWidth() - margin - margin);
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

		lvCBServer = new V_ListView(new CB_RectF(leftBorder, refreshPqList.getMaxY() + margin, CBServerCollapseBox.getWidth(), CBServerCollapseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

		lvCBServer.setEmptyMsg(Translation.Get("EmptyCBServerList"));

		CBServerCollapseBox.addChild(lvCBServer);
		CBServerCollapseBox.addChild(refreshCBServerList);

		scrollBox.addChild(CBServerCollapseBox);
	}

	private void createGpxLine() {
		checkBoxImportGPX = new chkBox("GPX");
		checkBoxImportGPX.setX(innerLeft);
		checkBoxImportGPX.setY(PQ_ListCollapseBox.getY() - margin - checkBoxImportGPX.getHeight());

		btnSelectFile = new Button(Translation.Get("selectFile"));

		if (!GPX_LINE_ACTIVE) {
			checkBoxImportGPX.setVisible(false);
			checkBoxImportGPX.setHeight(0);
			btnSelectFile.setVisible(false);
		}
		lblGPX = new Label(this.name + " lblGPX", checkBoxImportGPX.getMaxX() + margin, checkBoxImportGPX.getY(), innerWidth - margin * 3 - checkBoxImportGPX.getWidth(), checkBoxImportGPX.getHeight());
		lblGPX.setFont(Fonts.getNormal());
		lblGPX.setText(Translation.Get("GPX"));

		btnSelectFile.setPos(checkBoxImportGPX.getMaxX() + (checkBoxImportGPX.getWidth() * 2.2f), checkBoxImportGPX.getY());
		btnSelectFile.setWidth(scrollBox.getInnerWidth() - (btnSelectFile.getX() + margin));

		btnSelectFile.setOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				PlatformConnector.getFile("", "", "", "", new IgetFileReturnListener() {

					@Override
					public void returnFile(String Path) {
						copyGPX2PQ_Folder(Path);
					}
				});
				return true;
			}
		});

		scrollBox.addChild(checkBoxImportGPX);
		scrollBox.addChild(lblGPX);
		scrollBox.addChild(btnSelectFile);
	}

	private void createGcVoteLine() {
		checkBoxGcVote = new chkBox("GcVote");
		checkBoxGcVote.setX(innerLeft);
		checkBoxGcVote.setY(checkBoxImportGPX.getY() - margin - checkBoxImportGPX.getHeight());
		if (!GCV_LINE_ACTIVE) {
			checkBoxGcVote.setVisible(false);
			checkBoxGcVote.setHeight(0);
		}
		lblGcVote = new Label(this.name + " lblGcVote", checkBoxGcVote.getMaxX() + margin, checkBoxGcVote.getY(), innerWidth - margin * 3 - checkBoxGcVote.getWidth(), checkBoxGcVote.getHeight());
		lblGcVote.setFont(Fonts.getNormal());
		lblGcVote.setText(Translation.Get("GCVoteRatings"));
		if (!GCV_LINE_ACTIVE) {
			lblGcVote.setVisible(false);
			lblGcVote.setHeight(0);
		}

		scrollBox.addChild(checkBoxGcVote);
		scrollBox.addChild(lblGcVote);
	}

	private void createImageLine() {
		// Preload Description Images
		checkBoxPreloadImages = new chkBox("Image");
		checkBoxPreloadImages.setX(innerLeft);
		checkBoxPreloadImages.setY(checkBoxGcVote.getY() - margin - checkBoxPreloadImages.getHeight());

		lblImage = new Label(this.name + " lblImage", checkBoxPreloadImages.getMaxX() + margin, checkBoxPreloadImages.getY(), innerWidth - margin * 3 - checkBoxPreloadImages.getWidth(), checkBoxPreloadImages.getHeight());
		lblImage.setFont(Fonts.getNormal());
		lblImage.setText(Translation.Get("PreloadImages"));

		scrollBox.addChild(checkBoxPreloadImages);
		scrollBox.addChild(lblImage);

		// Preload Spoiler Images
		checkBoxPreloadSpoiler = new chkBox("Image");
		checkBoxPreloadSpoiler.setX(innerLeft);
		checkBoxPreloadSpoiler.setY(checkBoxPreloadImages.getY() - margin - checkBoxPreloadSpoiler.getHeight());

		lblSpoiler = new Label(this.name + " lblSpoiler", checkBoxPreloadSpoiler.getMaxX() + margin, checkBoxPreloadSpoiler.getY(), innerWidth - margin * 3 - checkBoxPreloadSpoiler.getWidth(), checkBoxPreloadSpoiler.getHeight());
		lblSpoiler.setFont(Fonts.getNormal());
		lblSpoiler.setText(Translation.Get("PreloadSpoiler"));

		scrollBox.addChild(checkBoxPreloadSpoiler);
		scrollBox.addChild(lblSpoiler);
	}

	private void createMapLine() {
		checkBoxImportMaps = new chkBox("Image");
		checkBoxImportMaps.setX(innerLeft);
		checkBoxImportMaps.setY(checkBoxPreloadSpoiler.getY() - margin - checkBoxImportMaps.getHeight());

		lblMaps = new Label(this.name + " lblMaps", checkBoxImportMaps.getMaxX() + margin, checkBoxImportMaps.getY(), innerWidth - margin * 3 - checkBoxImportMaps.getWidth(), checkBoxImportMaps.getHeight());
		lblMaps.setFont(Fonts.getNormal());
		lblMaps.setText(Translation.Get("Maps"));

		// TODO wieder einschalten wenn Implementiert
		// scrollBox.addChild(checkBoxImportMaps);
		// scrollBox.addChild(lblMaps);
	}

	private void createLogLine() {
		checkBoxCleanLogs = new chkBox("Image");
		checkBoxCleanLogs.setX(innerLeft);

		float yPos = MAP_LINE_ACTIVE ? checkBoxImportMaps.getY() : checkBoxPreloadSpoiler.getY();

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

		spinner = new Spinner(margin, LogCollapseBox.getHeight() - margin - checkBoxCleanLogs.getHeight(), LogCollapseBox.getWidth() - margin - margin, checkBoxCleanLogs.getHeight(), "LogLifeSpinner", adapter, new ISelectionChangedListener() {

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
					NumericInputBox.Show(Translation.Get("ButKeepLeast"), Translation.Get("DeleteLogs"), Config.LogMinCount.getValue(), new IReturnValueListener() {

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
		lblCompact = new Label(this.name + " lblCompact", checkBoxPreloadSpoiler.getMaxX() + margin, checkBoxCompactDB.getY(), innerWidth - margin * 3 - checkBoxCompactDB.getWidth(), checkBoxCompactDB.getHeight());
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
		checkImportFromCBServer.setOnCheckChangedListener(checkImportFromCBServer_CheckStateChanged);
		checkBoxGcVote.setChecked(GCV_LINE_ACTIVE ? Config.ImportRatings.getValue() : false);

		checkImportPQfromGC.setChecked(PQ_LINE_ACTIVE ? Config.ImportPQsFromGeocachingCom.getValue() : false);
		checkImportPQfromGC.setEnabled(true);

		if (checkImportPQfromGC.isChecked()) {
			checkBoxImportGPX.setChecked(GPX_LINE_ACTIVE ? true : false);
			checkBoxImportGPX.setEnabled(false);
		}
		checkBoxCompactDB.setChecked(DB_LINE_ACTIVE ? Config.CompactDB.getValue() : false);

		/*
		 * if (importType == MenuID.MI_IMPORT_GS_PQ) { // alles andere als den PQ Import deaktivieren checkImportPQfromGC.setChecked(true);
		 * checkImportFromCBServer.setChecked(false); checkBoxGcVote.setChecked(false); checkBoxCleanLogs.setChecked(false);
		 * checkBoxCompactDB.setChecked(false); }
		 */
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

	IAnimatedHeightChangedListener mAnimationListener = new IAnimatedHeightChangedListener() {
		@Override
		public void animatedHeightChanged(float Height) {
			Layout();
		}
	};

	private final OnCheckChangedListener checkLog_CheckStateChanged = new OnCheckChangedListener() {

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

	private final OnCheckChangedListener checkImportPQfromGC_CheckStateChanged = new OnCheckChangedListener() {
		@Override
		public void onCheckedChanged(chkBox view, boolean isChecked) {
			if ((importType == MenuID.MI_IMPORT_GS_PQ) || (checkImportPQfromGC.isChecked())) {
				checkBoxImportGPX.setChecked(true);
				checkBoxImportGPX.setEnabled(false);
				PQ_ListCollapseBox.expand();
			} else {
				checkBoxImportGPX.setEnabled(true);
				PQ_ListCollapseBox.collapse();
			}
		}
	};

	private final OnCheckChangedListener checkImportFromCBServer_CheckStateChanged = new OnCheckChangedListener() {
		@Override
		public void onCheckedChanged(chkBox view, boolean isChecked) {
			if (checkImportFromCBServer.isChecked()) {
				CBServerCollapseBox.expand();
			} else {
				CBServerCollapseBox.collapse();
			}
		}
	};

	public class CustomAdapter implements Adapter {

		public CustomAdapter() {
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
				itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
				float itemWidth = PQ_ListCollapseBox.getInnerWidth();

				itemRec = new CB_RectF(new SizeF(itemWidth, itemHeight));
			}

			return new Import_PqListItem(itemRec, position, pq);

		}

		@Override
		public float getItemSize(int position) {
			if (itemHeight == -1)
				itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
			return itemHeight;
		}

	}

	public class CustomAdapterCBServer implements Adapter {

		public CustomAdapterCBServer() {
		}

		@Override
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

	private void refreshPqList() {

		lvPQs.setBaseAdapter(null);
		lvPQs.notifyDataSetChanged();
		refreshPqList.disable();

		Thread thread = new Thread() {
			@Override
			public void run() {
				PqList = new ArrayList<PQ>();
				PocketQuery.GetPocketQueryList(PqList);
				lvPQs.setBaseAdapter(new CustomAdapter());
				lvPQs.notifyDataSetChanged();

				stopTimer();
				lvPQs.setEmptyMsg(Translation.Get("EmptyPqList"));

				refreshPqList.enable();
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

				lvPQs.setEmptyMsg(Translation.Get("LoadPqList") + s);

			}

		}, 0, ANIMATION_TICK);

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
		File directory = FileFactory.createFile(directoryPath);

		ImportThread(directoryPath, directory);

	}

	private volatile BreakawayImportThread importThread;

	public void ImportThread(final String directoryPath, final File directory) {
		importThread = new BreakawayImportThread() {
			@Override
			public void run() {
				importStarted = true;

				Importer importer = new Importer();
				ImporterProgress ip = new ImporterProgress();

				try {
					// Set Progress values
					if (checkImportPQfromGC.isChecked()) {
						ip.addStep(ip.new Step("importGC", 4));
					}
					if (checkImportFromCBServer.isChecked()) {
						ip.addStep(ip.new Step("importCBServer", 4));
					}
					if (checkBoxImportGPX.isChecked()) {
						ip.addStep(ip.new Step("ExtractZip", 1));
						ip.addStep(ip.new Step("AnalyseGPX", 1));
						ip.addStep(ip.new Step("ImportGPX", 4));
					}
					if (checkBoxGcVote.isChecked()) {
						ip.addStep(ip.new Step("sendGcVote", 1));
						ip.addStep(ip.new Step("importGcVote", 4));
					}

					if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked()) {
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

					if (checkImportPQfromGC.isChecked()) {

						if (PqList != null && PqList.size() > 0) {

							// PQ-List von nicht Downloadbaren PQs befreien

							ArrayList<PQ> downloadPqList = new ArrayList<PocketQuery.PQ>();

							for (PQ pq : PqList) {
								if (pq.downloadAvailable)
									downloadPqList.add(pq);
							}

							Iterator<PQ> iterator = downloadPqList.iterator();

							ip.setJobMax("importGC", downloadPqList.size());

							dis.setAnimationType(AnimationType.Download);

							if (iterator != null && iterator.hasNext()) {
								do {
									if (BreakawayImportThread.isCanceled()) {
										cancelImport();
										ip.ProgressChangeMsg("", "");
										return;
									}

									PQ pq = iterator.next();

									if (pq.downloadAvailable) {
										ip.ProgressInkrement("importGC", "Download: " + pq.Name, false);
										try {
											PocketQuery.DownloadSinglePocketQuery(pq, Config.PocketQueryFolder.getValue());
										} catch (OutOfMemoryError e) {
											Log.err(log, "PQ-download", "OutOfMemoryError-" + pq.Name, e);
											e.printStackTrace();
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
					} catch (Exception e) {

					}

					// Importiere alle GPX Files im Import Folder, auch in ZIP
					// verpackte
					if (checkBoxImportGPX.isChecked() && directory.exists()) {

						System.gc();

						long startTime = System.currentTimeMillis();

						Database.Data.beginTransaction();
						Database.Data.Query.clear();
						try {

							importer.importGpx(directoryPath, ip);

							Database.Data.setTransactionSuccessful();
						} catch (Exception exc) {
							exc.printStackTrace();
							Database.Data.endTransaction();
							cancelImport();
							ip.ProgressChangeMsg("", "");
							return;
						}
						Database.Data.endTransaction();

						if (BreakawayImportThread.isCanceled()) {
							cancelImport();
							ip.ProgressChangeMsg("", "");
							return;
						}

						Log.debug(log, "Import  GPX Import took " + (System.currentTimeMillis() - startTime) + "ms");

						System.gc();

						// del alten entpackten Ordener wenn vorhanden?
						File[] filelist = directory.listFiles();
						for (File tmp : filelist) {
							if (tmp.isDirectory()) {
								ArrayList<File> ordnerInhalt = FileIO.recursiveDirectoryReader(tmp, new ArrayList<File>());
								for (File tmp2 : ordnerInhalt) {
									try {
										tmp2.delete();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							}
							try {
								tmp.delete();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}

					if (checkImportFromCBServer.isChecked()) {
						// Import from CBServer
						System.gc();
						ImportCBServer importCBServer = new ImportCBServer();

						long startTime = System.currentTimeMillis();

						Database.Data.beginTransaction();
						try {

							importCBServer.importCBServer(cbServerExportList, ip, true);

							Database.Data.setTransactionSuccessful();
						} catch (Exception exc) {
							exc.printStackTrace();
						}
						Database.Data.endTransaction();

						if (BreakawayImportThread.isCanceled()) {
							cancelImport();
							ip.ProgressChangeMsg("", "");
							return;
						}

						Log.debug(log, "Import CBServer took " + (System.currentTimeMillis() - startTime) + "ms");

						System.gc();
					}

					if (checkBoxGcVote.isChecked()) {
						dis.setAnimationType(AnimationType.Download);
						Database.Data.beginTransaction();
						try {
							importer.importGcVote(FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue()), ip);

							Database.Data.setTransactionSuccessful();
						} catch (Exception exc) {
							exc.printStackTrace();
						}
						dis.setAnimationType(AnimationType.Work);
						Database.Data.endTransaction();
						if (BreakawayImportThread.isCanceled()) {
							cancelImport();
							ip.ProgressChangeMsg("", "");
							return;
						}
					}

					if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked()) {
						dis.setAnimationType(AnimationType.Download);
						int result = importer.importImagesNew(ip, checkBoxPreloadImages.isChecked(), checkBoxPreloadSpoiler.isChecked(), FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue()));

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
					FilterProperties props = FilterInstances.getLastFilter();
					EditFilterSettings.ApplyFilter(props);
					ip.ProgressChangeMsg("", "");
					return;
				}

				if (BreakawayImportThread.isCanceled()) {
					FilterProperties props = FilterInstances.getLastFilter();
					EditFilterSettings.ApplyFilter(props);
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
	protected void finish() {
		super.finish();

		// finish close activity and notify changes

		CacheListChangedEventList.Call();
		String Msg;

		if (ImportStart != null) {
			Date Importfin = new Date();
			long ImportZeit = Importfin.getTime() - ImportStart.getTime();

			Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "C " + String.valueOf(GPXFileImporter.LogCount) + "L in " + String.valueOf(ImportZeit);
		} else {
			Msg = "Import canceld";
		}

		Log.debug(log, Msg);

		FilterProperties props = FilterInstances.getLastFilter();
		EditFilterSettings.ApplyFilter(props);

		GL.that.Toast(Msg, 3000);

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

	private volatile Thread CopyThread;

	private void copyGPX2PQ_Folder(final String file) {
		// disable UI
		dis = new ImportAnimation(scrollBox);
		dis.setBackground(getBackground());

		this.addChild(dis, false);

		dis.setAnimationType(AnimationType.Work);

		CopyThread = new Thread(new Runnable() {

			@Override
			public void run() {
				CopyRule rule = new CopyRule(file, Config.PocketQueryFolder.getValue());
				Copy copyHelper = new Copy(rule);
				try {
					copyHelper.Run();
				} catch (IOException e) {
					e.printStackTrace();
				}
				cancelImport();
			}
		});
		CopyThread.start();
	}

}
