package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import CB_Core.Api.PocketQuery.PQ;
import CB_Core.Events.ProgressChangedEvent;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CollapseBox.animatetHeightChangedListner;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

public class SyncActivity extends ActivityBase implements ProgressChangedEvent
{
	public static final boolean RELEASED = false;

	private V_ListView lvPQs;
	private Button bOK, bCancel;
	private float innerLeft, innerHeight, CollapseBoxHeight, CollapseBoxMaxHeight, CollapseBoxLogsMaxHeight;
	private Label lblTitle, lblProgressMsg;
	private ProgressBar pgBar;
	private chkBox checkImportPQfromGC, checkBoxImportGPX, checkBoxGcVote, checkBoxPreloadImages, checkBoxPreloadSpoiler,
			checkBoxImportMaps, checkBoxCleanLogs, checkBoxCompactDB;
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
	private ImportAnimation dis;

	public static boolean isCanceld()
	{
		return importCancel;
	}

	public SyncActivity()
	{
		super(ActivityRec(), "importActivity");
		CollapseBoxMaxHeight = CollapseBoxHeight = UI_Size_Base.that.getButtonHeight() * 6;
		innerHeight = 1000;
		scrollBox = new ScrollBox(ActivityRec());
		this.addChild(scrollBox);
		createOkCancelBtn();
		createTitleLine();
		scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
		scrollBox.setY(bOK.getMaxY() + margin);
		scrollBox.setBackground(this.getBackground());

		initialForm();

		Layout();
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
		bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(Translation.Get("import"));
		bCancel.setText(Translation.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// ImportNow();
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
										// cancelImport();
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

		lblTitle = new Label(leftBorder + margin, this.height - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight,
				"TitleLabel");
		lblTitle.setFont(Fonts.getBig());
		float lblWidth = lblTitle.setText(Translation.Get("import")).getTextWidth();
		this.addChild(lblTitle);

		CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth,
				lineHeight);

		pgBar = new ProgressBar(rec, "ProgressBar");

		pgBar.setProgress(0, "");

		float SmallLineHeight = Fonts.MeasureSmall("Tg").height;

		lblProgressMsg = new Label(leftBorder + margin, lblTitle.getY() - margin - SmallLineHeight, innerWidth - margin - margin,
				SmallLineHeight, "ProgressMsg");

		lblProgressMsg.setFont(Fonts.getSmall());

		this.addChild(pgBar);
		this.addChild(lblProgressMsg);

	}

	private void Layout()
	{

		innerHeight = this.height;
		scrollBox.setVirtualHeight(innerHeight);
	}

	private void initialForm()
	{

	}

	animatetHeightChangedListner Animationlistner = new animatetHeightChangedListner()
	{
		@Override
		public void animatedHeightChanged(float Height)
		{
			Layout();
		}
	};

	private void stopTimer()
	{
		if (mAnimationTimer != null)
		{
			mAnimationTimer.cancel();
			mAnimationTimer = null;
		}
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

}
