package CB_Core.GL_UI.Activitys;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.PocketQuery;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.ProgressChangedEvent;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CollabseBox;
import CB_Core.GL_UI.Controls.CollabseBox.animatetHeightChangedListner;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_Core.GL_UI.Controls.SpinnerAdapter;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class Import extends ActivityBase implements ProgressChangedEvent
{

	private V_ListView lvPQs;
	private Button bOK, bCancel, refreshPqList;
	private float innerLeft, innerWidth, innerHeight, CollabseBoxHeight, CollabseBoxMaxHeight, CollabseBoxLogsMaxHeight;
	private Label lblTitle, lblPQ, lblGPX, lblGcVote, lblImage, lblSpoiler, lblMaps, lblProgressMsg, lblLogs, lblCompact;
	private ProgressBar pgBar;
	private chkBox checkImportPQfromGC, checkBoxImportGPX, checkBoxGcVote, checkBoxPreloadImages, checkBoxPreloadSpoiler,
			checkBoxImportMaps, checkBoxCleanLogs, checkBoxCompactDB;
	private CollabseBox PQ_ListCollabseBox, LogCollabseBox;
	private Spinner spinner;

	private Timer mAnimationTimer;
	private long ANIMATION_TICK = 450;
	private int animationValue = 0;

	private Date ImportStart;
	// private int LogImports;
	// private int CacheImports;

	private static Boolean importCancel = false;
	private Boolean importStarted = false;

	private ArrayList<PQ> PqList;

	private CB_RectF itemRec;
	private float itemHeight = -1;

	private ScrollBox scrollBox;
	private disable dis;

	public static boolean isCanceld()
	{
		return importCancel;
	}

	public Import()
	{
		super(ActivityRec(), "importActivity");
		innerWidth = this.width - this.getLeftWidth() - this.getLeftWidth();
		CollabseBoxMaxHeight = CollabseBoxHeight = UI_Size_Base.that.getButtonHeight() * 6;
		innerHeight = 1000;
		scrollBox = new ScrollBox(ActivityRec(), innerHeight, "ScrollBox");
		this.addChild(scrollBox);
		createOkCancelBtn();
		createTitleLine();
		scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
		scrollBox.setY(bOK.getMaxY() + margin);
		scrollBox.setBackground(this.getBackground());
		createPQLines();
		createPqCollabseBox();
		createGpxLine();
		createGcVoteLine();
		createImageLine();
		createMapLine();
		createLogLine();
		createLogCollabseBox();
		createCompactDBLine();

		initialForm();

		Layout();

		// scrollBox.setBackground(new ColorDrawable(Color.RED));
	}

	@Override
	public void onShow()
	{
		ProgresssChangedEventList.Add(this);
	}

	@Override
	public void onHide()
	{
		ProgresssChangedEventList.Remove(this);
	}

	private void createOkCancelBtn()
	{
		bOK = new Button(this.getLeftWidth(), this.getLeftWidth(), innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), this.getLeftWidth(), innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(Translation.Get("import"));
		bCancel.setText(Translation.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				ImportNow();
				return true;
			}
		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (importCancel) return true;

				if (importStarted)
				{
					GL_MsgBox.Show(Translation.Get("WontCancelImport"), Translation.Get("Cancel Import"), MessageBoxButtons.YesNo,
							MessageBoxIcon.Stop, new OnMsgBoxClickListener()
							{

								@Override
								public boolean onClick(int which, Object data)
								{
									if (which == GL_MsgBox.BUTTON_POSITIVE)
									{
										cancelImport();
									}
									return true;
								}
							});
				}
				else
					finish();
				return true;
			}
		});

	}

	private void createTitleLine()
	{
		// Title+Progressbar

		float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

		lblTitle = new Label(this.getLeftWidth() + margin, this.height - this.getTopHeight() - lineHeight - margin, width
				- this.getLeftWidth() - this.getRightWidth() - margin, lineHeight, "TitleLabel");
		lblTitle.setFont(Fonts.getBig());
		lblTitle.setText(Translation.Get("import"));
		float lblWidth = lblTitle.bounds.width;
		this.addChild(lblTitle);

		CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), this.width - margin - margin - lblWidth
				- this.getLeftWidth() - this.getRightWidth(), lineHeight);

		pgBar = new ProgressBar(rec, "ProgressBar");

		pgBar.setProgress(0, "");

		float SmallLineHeight = Fonts.MeasureSmall("Tg").height;

		lblProgressMsg = new Label(this.getLeftWidth() + margin, lblTitle.getY() - margin - SmallLineHeight, this.getWidth()
				- this.getLeftWidth() - this.getRightWidth() - margin - margin, SmallLineHeight, "ProgressMsg");

		lblProgressMsg.setFont(Fonts.getSmall());

		this.addChild(pgBar);
		this.addChild(lblProgressMsg);

	}

	private void createPQLines()
	{

		innerLeft = this.getLeftWidth() + margin - this.getLeftWidth();

		checkImportPQfromGC = new chkBox("PQ");
		checkImportPQfromGC.setX(innerLeft);
		checkImportPQfromGC.setY(innerHeight - checkImportPQfromGC.getHeight());

		lblPQ = new Label(checkImportPQfromGC.getMaxX() + margin, checkImportPQfromGC.getY(), this.innerWidth - margin * 3
				- checkImportPQfromGC.getWidth(), checkImportPQfromGC.getHeight(), "");
		lblPQ.setFont(Fonts.getNormal());
		lblPQ.setText(Translation.Get("PQfromGC"));

		scrollBox.addChild(checkImportPQfromGC);
		scrollBox.addChild(lblPQ);
	}

	private void createPqCollabseBox()
	{
		CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollabseBoxHeight - margin, lblPQ.getWidth(), CollabseBoxHeight);

		PQ_ListCollabseBox = new CollabseBox(rec, "PqCollabse");
		PQ_ListCollabseBox.setBackground(this.getBackground());

		refreshPqList = new Button(name);
		refreshPqList.setWidth(PQ_ListCollabseBox.getWidth() - margin - margin);
		refreshPqList.setX(margin);
		refreshPqList.setY(margin);
		refreshPqList.setText(Translation.Get("refreshPqList"));
		refreshPqList.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				refreshPqList();
				return true;
			}
		});

		lvPQs = new V_ListView(new CB_RectF(this.getLeftWidth(), refreshPqList.getMaxY() + margin, PQ_ListCollabseBox.getWidth(),
				PQ_ListCollabseBox.getHeight() - margin - margin - refreshPqList.getMaxY()), "");

		lvPQs.setEmptyMsg(Translation.Get("EmptyPqList"));

		PQ_ListCollabseBox.addChild(lvPQs);
		PQ_ListCollabseBox.addChild(refreshPqList);

		scrollBox.addChild(PQ_ListCollabseBox);
	}

	private void createGpxLine()
	{
		checkBoxImportGPX = new chkBox("GPX");
		checkBoxImportGPX.setX(innerLeft);
		checkBoxImportGPX.setY(PQ_ListCollabseBox.getY() - margin - checkBoxImportGPX.getHeight());

		lblGPX = new Label(checkBoxImportGPX.getMaxX() + margin, checkBoxImportGPX.getY(), this.innerWidth - margin * 3
				- checkBoxImportGPX.getWidth(), checkBoxImportGPX.getHeight(), "");
		lblGPX.setFont(Fonts.getNormal());
		lblGPX.setText(Translation.Get("GPX"));

		scrollBox.addChild(checkBoxImportGPX);
		scrollBox.addChild(lblGPX);
	}

	private void createGcVoteLine()
	{
		checkBoxGcVote = new chkBox("GcVote");
		checkBoxGcVote.setX(innerLeft);
		checkBoxGcVote.setY(checkBoxImportGPX.getY() - margin - checkBoxImportGPX.getHeight());

		lblGcVote = new Label(checkBoxGcVote.getMaxX() + margin, checkBoxGcVote.getY(), this.innerWidth - margin * 3
				- checkBoxGcVote.getWidth(), checkBoxGcVote.getHeight(), "");
		lblGcVote.setFont(Fonts.getNormal());
		lblGcVote.setText(Translation.Get("GCVoteRatings"));

		scrollBox.addChild(checkBoxGcVote);
		scrollBox.addChild(lblGcVote);
	}

	private void createImageLine()
	{
		// Preload Description Images
		checkBoxPreloadImages = new chkBox("Image");
		checkBoxPreloadImages.setX(innerLeft);
		checkBoxPreloadImages.setY(checkBoxGcVote.getY() - margin - checkBoxPreloadImages.getHeight());

		lblImage = new Label(checkBoxPreloadImages.getMaxX() + margin, checkBoxPreloadImages.getY(), this.innerWidth - margin * 3
				- checkBoxPreloadImages.getWidth(), checkBoxPreloadImages.getHeight(), "");
		lblImage.setFont(Fonts.getNormal());
		lblImage.setText(Translation.Get("PreloadImages"));

		scrollBox.addChild(checkBoxPreloadImages);
		scrollBox.addChild(lblImage);

		// Preload Spoiler Images
		checkBoxPreloadSpoiler = new chkBox("Image");
		checkBoxPreloadSpoiler.setX(innerLeft);
		checkBoxPreloadSpoiler.setY(checkBoxPreloadImages.getY() - margin - checkBoxPreloadSpoiler.getHeight());

		lblSpoiler = new Label(checkBoxPreloadSpoiler.getMaxX() + margin, checkBoxPreloadSpoiler.getY(), this.innerWidth - margin * 3
				- checkBoxPreloadSpoiler.getWidth(), checkBoxPreloadSpoiler.getHeight(), "");
		lblSpoiler.setFont(Fonts.getNormal());
		lblSpoiler.setText(Translation.Get("PreloadSpoiler"));

		scrollBox.addChild(checkBoxPreloadSpoiler);
		scrollBox.addChild(lblSpoiler);
	}

	final boolean MAP_LINE_ACTIVE = false;

	private void createMapLine()
	{
		checkBoxImportMaps = new chkBox("Image");
		checkBoxImportMaps.setX(innerLeft);
		checkBoxImportMaps.setY(checkBoxPreloadSpoiler.getY() - margin - checkBoxImportMaps.getHeight());

		lblMaps = new Label(checkBoxImportMaps.getMaxX() + margin, checkBoxImportMaps.getY(), this.innerWidth - margin * 3
				- checkBoxImportMaps.getWidth(), checkBoxImportMaps.getHeight(), "");
		lblMaps.setFont(Fonts.getNormal());
		lblMaps.setText(Translation.Get("Maps"));

		// TODO wieder einschalten wenn Implementiert
		// scrollBox.addChild(checkBoxImportMaps);
		// scrollBox.addChild(lblMaps);
	}

	private void createLogLine()
	{
		checkBoxCleanLogs = new chkBox("Image");
		checkBoxCleanLogs.setX(innerLeft);

		float yPos = MAP_LINE_ACTIVE ? checkBoxImportMaps.getY() : checkBoxPreloadSpoiler.getY();

		checkBoxCleanLogs.setY(yPos - margin - checkBoxCleanLogs.getHeight());

		lblLogs = new Label(checkBoxCleanLogs.getMaxX() + margin, checkBoxCleanLogs.getY(), this.innerWidth - margin * 3
				- checkBoxCleanLogs.getWidth(), checkBoxCleanLogs.getHeight(), "");
		lblLogs.setFont(Fonts.getNormal());
		lblLogs.setText(Translation.Get("DeleteLogs"));

		scrollBox.addChild(checkBoxCleanLogs);
		scrollBox.addChild(lblLogs);
	}

	ArrayList<String> values = new ArrayList<String>();

	private void createLogCollabseBox()
	{
		float SmallLineHeight = Fonts.MeasureSmall("Tg").height * 1.5f;
		CollabseBoxLogsMaxHeight = checkBoxCleanLogs.getHeight() + (SmallLineHeight * 3.5f) + (margin * 4);

		CB_RectF rec = new CB_RectF(lblLogs.getX(), lblLogs.getY() - CollabseBoxLogsMaxHeight - margin, lblLogs.getWidth(),
				CollabseBoxLogsMaxHeight);
		LogCollabseBox = new CollabseBox(rec, "LogCollabse");
		LogCollabseBox.setBackground(this.getBackground());
		scrollBox.addChild(LogCollabseBox);

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

		final SpinnerAdapter adapter = new SpinnerAdapter()
		{

			@Override
			public String getText(int position)
			{
				return values.get(position);
			}

			@Override
			public Drawable getIcon(int Position)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return values.size();
			}
		};

		spinner = new Spinner(margin, LogCollabseBox.getHeight() - margin - checkBoxCleanLogs.getHeight(), LogCollabseBox.getWidth()
				- margin - margin, checkBoxCleanLogs.getHeight(), "LogLifeSpinner", adapter, new selectionChangedListner()
		{

			@Override
			public void selectionChanged(int index)
			{
				Config.settings.LogMaxMonthAge.setValue(index);
				Config.AcceptChanges();
			}
		});

		LogCollabseBox.addChild(spinner);

		Label lblButKeepLeast = new Label(margin, spinner.getY() - margin - SmallLineHeight, LogCollabseBox.getAvailableWidth(),
				SmallLineHeight, "lblButKeepLeast");
		lblButKeepLeast.setText(Translation.Get("ButKeepLeast"));
		LogCollabseBox.addChild(lblButKeepLeast);

		final EditWrapedTextField input = new EditWrapedTextField(LogCollabseBox, checkBoxCleanLogs.ScaleCenter(2), "InputTextField");
		input.setHeight(SmallLineHeight * 2.5f);
		input.setText(String.valueOf(Config.settings.LogMinCount.getValue()));
		input.setPos(margin, lblButKeepLeast.getY() - margin - input.getHeight());
		LogCollabseBox.addChild(input);

		// prevented Keyboard popup, show NumerikInputBox
		input.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean visible)
			{
				if (visible)
				{
					NumerikInputBox.Show(Translation.Get("ButKeepLeast"), Translation.Get("DeleteLogs"),
							Config.settings.LogMinCount.getValue(), new returnValueListner()
							{

								@Override
								public void returnValue(int value)
								{
									Config.settings.LogMinCount.setValue(value);
									Config.AcceptChanges();
									input.setText(String.valueOf(value));
								}

								@Override
								public void cancelClicked()
								{

								}
							});
				}
			}
		});

	}

	private void createCompactDBLine()
	{
		checkBoxCompactDB = new chkBox("Compact");
		checkBoxCompactDB.setX(innerLeft);
		checkBoxCompactDB.setY(LogCollabseBox.getY() - margin - checkBoxCompactDB.getHeight());

		lblCompact = new Label(checkBoxPreloadSpoiler.getMaxX() + margin, checkBoxCompactDB.getY(), this.innerWidth - margin * 3
				- checkBoxCompactDB.getWidth(), checkBoxCompactDB.getHeight(), "");
		lblCompact.setFont(Fonts.getNormal());
		lblCompact.setText(Translation.Get("CompactDB"));

		scrollBox.addChild(checkBoxCompactDB);
		scrollBox.addChild(lblCompact);
	}

	private void Layout()
	{

		checkBoxCompactDB.setY(margin);
		lblCompact.setY(margin);

		LogCollabseBox.setY(checkBoxCompactDB.getMaxY() + margin);

		checkBoxCleanLogs.setY(LogCollabseBox.getMaxY() + margin);
		lblLogs.setY(LogCollabseBox.getMaxY() + margin);

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

		PQ_ListCollabseBox.setY(checkBoxImportGPX.getMaxY() + margin);
		// PQ_ListCollabseBox.setHeight(CollabseBoxHeight);

		checkImportPQfromGC.setY(PQ_ListCollabseBox.getMaxY() + margin);
		lblPQ.setY(PQ_ListCollabseBox.getMaxY() + margin);

		innerHeight = lblPQ.getMaxY() + margin;
		scrollBox.setInnerHeight(innerHeight);
	}

	private void initialForm()
	{
		checkBoxImportMaps.setChecked(Config.settings.CacheMapData.getValue());
		checkBoxPreloadImages.setChecked(Config.settings.CacheImageData.getValue());
		checkBoxPreloadSpoiler.setChecked(Config.settings.CacheSpoilerData.getValue());
		checkBoxImportGPX.setChecked(Config.settings.ImportGpx.getValue());
		checkImportPQfromGC.setOnCheckedChangeListener(checkImportPQfromGC_CheckStateChanged);
		checkBoxGcVote.setChecked(Config.settings.ImportRatings.getValue());

		if (GroundspeakAPI.isValidAPI_Key(true))
		{
			checkImportPQfromGC.setChecked(Config.settings.ImportPQsFromGeocachingCom.getValue());
			checkImportPQfromGC.setEnabled(true);
			checkBoxPreloadSpoiler.setEnable(true);
			lblSpoiler.setTextColor(Fonts.getFontColor());
			if (checkImportPQfromGC.isChecked())
			{
				PQ_ListCollabseBox.setAnimationHeight(CollabseBoxMaxHeight);
			}
			else
			{
				PQ_ListCollabseBox.setAnimationHeight(0);
			}
		}
		else
		{
			checkImportPQfromGC.setChecked(false);
			checkImportPQfromGC.setEnabled(false);
			checkBoxPreloadSpoiler.setEnable(false);
			lblSpoiler.setTextColor(Fonts.getDisableFontColor());
			checkImportPQfromGC.setHeight(0);
			CollabseBoxHeight = 0;
			lblPQ.setHeight(0);

		}

		if (checkImportPQfromGC.isChecked() == true)
		{
			checkBoxImportGPX.setChecked(true);
			checkBoxImportGPX.setEnabled(false);
		}

		PQ_ListCollabseBox.setAnimationListner(Animationlistner);
		LogCollabseBox.setAnimationListner(Animationlistner);

		checkBoxCleanLogs.setChecked(Config.settings.DeleteLogs.getValue());
		checkBoxCleanLogs.setOnCheckedChangeListener(checkLog_CheckStateChanged);

		if (checkBoxCleanLogs.isChecked())
		{
			LogCollabseBox.setAnimationHeight(CollabseBoxLogsMaxHeight);

			// validate value
			int value = Config.settings.LogMaxMonthAge.getValue();
			if (value > 6)
			{
				Config.settings.LogMaxMonthAge.setValue(6);
				Config.AcceptChanges();
			}

			spinner.setSelection(Config.settings.LogMaxMonthAge.getValue());
		}
		else
		{
			LogCollabseBox.setAnimationHeight(0);
		}

		checkBoxCompactDB.setChecked(Config.settings.CompactDB.getValue());

	}

	animatetHeightChangedListner Animationlistner = new animatetHeightChangedListner()
	{
		@Override
		public void animatetHeightCanged(float Height)
		{
			Layout();
		}
	};

	private OnCheckedChangeListener checkLog_CheckStateChanged = new OnCheckedChangeListener()
	{

		@Override
		public void onCheckedChanged(chkBox view, boolean isChecked)
		{
			if (checkBoxCleanLogs.isChecked())
			{
				LogCollabseBox.expand();
				spinner.setSelection(Config.settings.LogMaxMonthAge.getValue());
			}
			else
			{
				LogCollabseBox.collabse();
			}

			Config.settings.DeleteLogs.setValue(isChecked);
			Config.AcceptChanges();
		}
	};

	private OnCheckedChangeListener checkImportPQfromGC_CheckStateChanged = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(chkBox view, boolean isChecked)
		{
			if (checkImportPQfromGC.isChecked())
			{
				checkBoxImportGPX.setChecked(true);
				checkBoxImportGPX.setEnabled(false);
				PQ_ListCollabseBox.expand();
			}
			else
			{
				checkBoxImportGPX.setEnabled(true);
				PQ_ListCollabseBox.collabse();
			}
		}
	};

	public class CustomAdapter implements Adapter
	{

		public CustomAdapter()
		{
		}

		public int getCount()
		{
			if (PqList != null) return PqList.size();
			else
				return 0;
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			final PQ pq = PqList.get(position);
			if (itemRec == null)
			{
				itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
				float itemWidth = PQ_ListCollabseBox.getWidth() - PQ_ListCollabseBox.getLeftWidth() - PQ_ListCollabseBox.getRightWidth();

				itemRec = new CB_RectF(new SizeF(itemWidth, itemHeight));
			}

			return new Import_PqListItem(itemRec, position, pq);

		}

		@Override
		public float getItemSize(int position)
		{
			if (itemHeight == -1) itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
			return itemHeight;
		}

	}

	private void refreshPqList()
	{

		lvPQs.setBaseAdapter(null);
		lvPQs.notifyDataSetChanged();
		refreshPqList.disable();

		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				PqList = new ArrayList<PQ>();
				PocketQuery.GetPocketQueryList(Config.GetAccessToken(true), PqList);
				lvPQs.setBaseAdapter(new CustomAdapter());
				lvPQs.notifyDataSetChanged();

				stopTimer();
				lvPQs.setEmptyMsg(Translation.Get("EmptyPqList"));

				refreshPqList.enable();
			}

		};

		thread.start();

		mAnimationTimer = new Timer();
		mAnimationTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				TimerMethod();
			}

			private void TimerMethod()
			{
				animationValue++;

				if (animationValue > 5) animationValue = 0;

				String s = "";
				for (int i = 0; i < animationValue; i++)
				{
					s += ".";
				}

				lvPQs.setEmptyMsg(Translation.Get("LoadPqList") + s);

			}

		}, 0, ANIMATION_TICK);

	}

	private void stopTimer()
	{
		if (mAnimationTimer != null)
		{
			mAnimationTimer.cancel();
			mAnimationTimer = null;
		}
	}

	private void ImportNow()
	{
		// disable btn
		bOK.disable();
		importCancel = false;
		// disable UI
		dis = new disable(scrollBox);
		dis.setBackground(getBackground());

		this.addChild(dis, false);

		Config.settings.CacheMapData.setValue(checkBoxImportMaps.isChecked());
		Config.settings.CacheImageData.setValue(checkBoxPreloadImages.isChecked());
		Config.settings.CacheSpoilerData.setValue(checkBoxPreloadSpoiler.isChecked());
		Config.settings.ImportGpx.setValue(checkBoxImportGPX.isChecked());

		Config.settings.ImportPQsFromGeocachingCom.setValue(checkImportPQfromGC.isChecked());
		Config.settings.ImportRatings.setValue(checkBoxGcVote.isChecked());
		Config.settings.CompactDB.setValue(checkBoxCompactDB.isChecked());
		Config.AcceptChanges();
		String directoryPath = Config.settings.PocketQueryFolder.getValue();
		// chk exist import folder
		File directory = new File(directoryPath);

		ImportThread(directoryPath, directory);

	}

	private volatile Thread thread;

	public void ImportThread(final String directoryPath, final File directory)
	{
		thread = new Thread()
		{
			public void run()
			{
				importStarted = true;

				Importer importer = new Importer();
				ImporterProgress ip = new ImporterProgress();

				try
				{
					// Set Progress values
					if (checkImportPQfromGC.isChecked())
					{
						ip.addStep(ip.new Step("importGC", 4));
					}
					if (checkBoxImportGPX.isChecked())
					{
						ip.addStep(ip.new Step("ExtractZip", 1));
						ip.addStep(ip.new Step("AnalyseGPX", 1));
						ip.addStep(ip.new Step("ImportGPX", 4));
					}
					if (checkBoxGcVote.isChecked())
					{
						ip.addStep(ip.new Step("sendGcVote", 1));
						ip.addStep(ip.new Step("importGcVote", 4));
					}

					if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked())
					{
						// ip.addStep(ip.new Step("importImageUrls", 4));
						ip.addStep(ip.new Step("importImages", 4));
					}

					if (checkBoxCleanLogs.isChecked())
					{
						ip.addStep(ip.new Step("DeleteLogs", 1));
					}

					if (checkBoxCompactDB.isChecked())
					{
						ip.addStep(ip.new Step("CompactDB", 1));
					}

					// begin import

					if (checkImportPQfromGC.isChecked())
					{

						if (PqList != null && PqList.size() > 0)
						{

							// PQ-List von nicht Downloadbaren PQs befreien

							ArrayList<PQ> downloadPqList = new ArrayList<PocketQuery.PQ>();

							for (PQ pq : PqList)
							{
								if (pq.downloadAvible) downloadPqList.add(pq);
							}

							Iterator<PQ> iterator = downloadPqList.iterator();

							ip.setJobMax("importGC", downloadPqList.size());

							if (iterator != null && iterator.hasNext())
							{
								do
								{
									if (importCancel)
									{
										importCanceld();
										return;
									}

									PQ pq = iterator.next();

									if (pq.downloadAvible)
									{
										ip.ProgressInkrement("importGC", "Download: " + pq.Name, false);
										try
										{
											PocketQuery.DownloadSinglePocketQuery(pq);
										}
										catch (OutOfMemoryError e)
										{
											Logger.Error("PQ-download", "OutOfMemoryError-" + pq.Name, e);
											e.printStackTrace();
										}
									}

								}
								while (iterator.hasNext());
							}

							if (downloadPqList.size() == 0)
							{
								ip.ProgressInkrement("importGC", "", true);
							}
						}
					}
					// Importiere alle GPX Files im Import Folder, auch in ZIP
					// verpackte
					if (checkBoxImportGPX.isChecked() && directory.exists())
					{

						System.gc();

						long startTime = System.currentTimeMillis();

						Database.Data.beginTransaction();
						try
						{

							importer.importGpx(directoryPath, ip);

							Database.Data.setTransactionSuccessful();
						}
						catch (Exception exc)
						{
							exc.printStackTrace();
						}
						Database.Data.endTransaction();

						if (importCancel)
						{
							importCanceld();
							return;
						}

						Logger.LogCat("Import  GPX Import took " + (System.currentTimeMillis() - startTime) + "ms");

						System.gc();

						// del alten entpackten Ordener wenn vorhanden?
						File[] filelist = directory.listFiles();
						for (File tmp : filelist)
						{
							if (tmp.isDirectory())
							{
								ArrayList<File> ordnerInhalt = FileIO.recursiveDirectoryReader(tmp, new ArrayList<File>());
								for (File tmp2 : ordnerInhalt)
								{
									tmp2.delete();
								}

							}
							tmp.delete();
						}

					}

					if (checkBoxGcVote.isChecked())
					{
						Database.Data.beginTransaction();
						try
						{
							importer.importGcVote(GlobalCore.LastFilter.getSqlWhere(), ip);

							Database.Data.setTransactionSuccessful();
						}
						catch (Exception exc)
						{
							exc.printStackTrace();
						}
						Database.Data.endTransaction();
						if (importCancel)
						{
							importCanceld();
							return;
						}
					}

					if (checkBoxPreloadImages.isChecked() || checkBoxPreloadSpoiler.isChecked())
					{
						importer.importImagesNew(ip, checkBoxPreloadImages.isChecked(), checkBoxPreloadSpoiler.isChecked());
						if (importCancel)
						{
							importCanceld();
							return;
						}
					}

					Thread.sleep(1000);
					if (checkBoxImportMaps.isChecked()) importer.importMaps();

					Thread.sleep(1000);
					if (checkBoxCleanLogs.isChecked())
					{
						ip.setJobMax("DeleteLogs", 1);
						Database.Data.DeleteOldLogs();
						ip.ProgressInkrement("DeleteLogs", "", true);
					}

					if (checkBoxCompactDB.isChecked())
					{
						ip.setJobMax("CompactDB", 1);
						Database.Data.execSQL("vacuum");
						ip.ProgressInkrement("CompactDB", "", true);
					}

				}
				catch (InterruptedException e)
				{
					// import canceld
					importCanceld();
					return;
				}

				if (importCancel)
				{
					importCanceld();
					return;
				}

				finish();

				// finish close activity and notify changes

				CachListChangedEventList.Call();

				Date Importfin = new Date();
				long ImportZeit = Importfin.getTime() - ImportStart.getTime();

				String Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "C " + String.valueOf(GPXFileImporter.LogCount)
						+ "L in " + String.valueOf(ImportZeit);

				Logger.DEBUG(Msg);
				// MessageBox.Show("Import fertig! " + Msg);

				FilterProperties props = GlobalCore.LastFilter;

				EditFilterSettings.ApplyFilter(props);

				GL.that.Toast(Msg, 3000);

			}
		};

		thread.setPriority(Thread.MAX_PRIORITY);
		ImportStart = new Date();
		thread.start();
	}

	private void importCanceld()
	{
		importCancel = false;
		importStarted = false;
		that.removeChild(dis);
		bOK.enable();
	}

	private void cancelImport()
	{
		importCancel = true;
		thread.interrupt();
		thread = null;
		importStarted = false;
		this.finish();
	}

	@Override
	public void ProgressChangedEventCalled(final String Message, final String ProgressMessage, final int Progress)
	{

		this.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				pgBar.setProgress(Progress);
				lblProgressMsg.setText(ProgressMessage);
				if (!Message.equals("")) pgBar.setText(Message);
			}
		});

	}

	private class disable extends Box
	{

		public disable(CB_RectF rec)
		{
			super(rec, "");

			float size = rec.getHalfWidth() / 2;
			float halfSize = rec.getHalfWidth() / 4;

			CB_RectF imageRec = new CB_RectF(this.halfWidth - halfSize, this.halfHeight - halfSize, size, size);

			iconImage = new Image(imageRec, "MsgBoxIcon");
			iconImage.setDrawable(new SpriteDrawable(SpriteCache.Icons.get(IconName.daySpinner_51.ordinal())));
			iconImage.setOrigin(imageRec.getHalfWidth(), imageRec.getHalfHeight());

			this.addChild(iconImage);

			rotateAngle = 0;

			RotateTimer = new Timer();

			RotateTimer.schedule(rotateTimertask, 60, 60);

		}

		private Drawable back;
		private Image iconImage;

		Timer RotateTimer;
		float rotateAngle = 0;
		TimerTask rotateTimertask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (iconImage != null)
				{
					rotateAngle += 5;
					if (rotateAngle > 360) rotateAngle = 0;
					iconImage.setRotate(rotateAngle);
					GL.that.renderOnce("WaitRotateAni");
				}
			}
		};

		public void render(SpriteBatch batch)
		{
			if (drawableBackground != null)
			{
				back = drawableBackground;
				drawableBackground = null;
			}

			if (back != null)
			{
				Color c = batch.getColor();

				float a = c.a;
				float r = c.r;
				float g = c.g;
				float b = c.b;

				Color trans = new Color(0, 0.3f, 0, 0.40f);
				batch.setColor(trans);
				back.draw(batch, 0, 0, this.width, this.height);

				batch.setColor(new Color(r, g, b, a));

			}
		}

		@Override
		public void onHide()
		{
			RotateTimer.cancel();
			iconImage.dispose();
		}

		// alle Touch events abfangen

		@Override
		public boolean onTouchDown(int x, int y, int pointer, int button)
		{
			return true;
		}

		@Override
		public boolean onLongClick(int x, int y, int pointer, int button)
		{
			return true;
		}

		@Override
		public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
		{
			return true;
		}

		@Override
		public boolean onTouchUp(int x, int y, int pointer, int button)
		{
			return true;
		}

		@Override
		public boolean click(int x, int y, int pointer, int button)
		{
			return true;
		}

	}

}
