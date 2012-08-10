package CB_Core.GL_UI.Activitys;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

public class Import extends ActivityBase
{

	private V_ListView lvPQs;
	private Button bOK, bCancel, refreshPqList;
	private float innerLeft, innerWidth, innerHeight, CollabseBoxHeight, CollabseBoxMaxHeight;
	private Label lblTitle, lblPQ, lblGPX, lblGcVote, lblImage, lblMaps;
	private ProgressBar pgBar;
	private chkBox checkImportPQfromGC, checkBoxImportGPX, checkBoxGcVote, checkBoxPreloadImages, checkBoxImportMaps;
	private Box PQ_ListCollabseBox;

	private ScrollBox scrollBox;

	public Import()
	{
		super(ActivityRec(), "importActivity");
		innerWidth = this.width - Left - Left;
		CollabseBoxMaxHeight = CollabseBoxHeight = UiSizes.getButtonHeight() * 6;
		innerHeight = 1000;
		scrollBox = new ScrollBox(ActivityRec(), innerHeight, "ScrollBox");
		this.addChild(scrollBox);
		createOkCancelBtn();
		createTitleLine();
		scrollBox.setHeight(lblTitle.getY() - bOK.getMaxY() - margin - margin);
		scrollBox.setY(bOK.getMaxY() + margin);
		scrollBox.setBackground(this.getBackground());
		createPQLines();
		createPqCollabseBox();
		createGpxLine();
		createGcVoteLine();
		createImageLine();
		createMapLine();

		initialForm();

		Layout();

		// scrollBox.setBackground(new ColorDrawable(Color.RED));
	}

	private void createOkCancelBtn()
	{
		bOK = new Button(Left, Left, innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), Left, innerWidth / 2, UiSizes.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(GlobalCore.Translations.Get("import"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				return true;
			}
		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				finish();
				return true;
			}
		});

	}

	private void createTitleLine()
	{
		// Title+Progressbar

		float lineHeight = UiSizes.getButtonHeight() * 0.75f;

		lblTitle = new Label(Left + margin, this.height - Top - lineHeight - margin, width - Left - Right - margin, lineHeight,
				"TitleLabel");
		lblTitle.setFont(Fonts.getBig());
		float lblWidth = (lblTitle.setText(GlobalCore.Translations.Get("import"))).width;
		this.addChild(lblTitle);

		CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), this.width - margin - margin - lblWidth - Left
				- Right, lineHeight);

		pgBar = new ProgressBar(rec, "ProgressBar");

		pgBar.setProgress(50);

		this.addChild(pgBar);

	}

	private void createPQLines()
	{

		innerLeft = Left + margin - this.getLeftWidth();

		checkImportPQfromGC = new chkBox("PQ");
		checkImportPQfromGC.setX(innerLeft);
		checkImportPQfromGC.setY(innerHeight - checkImportPQfromGC.getHeight());

		lblPQ = new Label(checkImportPQfromGC.getMaxX() + margin, checkImportPQfromGC.getY(), this.innerWidth - margin * 3
				- checkImportPQfromGC.getWidth(), checkImportPQfromGC.getHeight(), "");
		lblPQ.setFont(Fonts.getNormal());
		lblPQ.setText(GlobalCore.Translations.Get("PQfromGC"));

		scrollBox.addChild(checkImportPQfromGC);
		scrollBox.addChild(lblPQ);
	}

	private void createPqCollabseBox()
	{
		CB_RectF rec = new CB_RectF(lblPQ.getX(), lblPQ.getY() - CollabseBoxHeight - margin, lblPQ.getWidth(), CollabseBoxHeight);

		PQ_ListCollabseBox = new Box(rec, "PqCollabse");
		PQ_ListCollabseBox.setBackground(this.getBackground());

		refreshPqList = new Button(name);
		refreshPqList.setWidth(PQ_ListCollabseBox.getWidth() - margin - margin);
		refreshPqList.setX(margin);
		refreshPqList.setY(margin);
		refreshPqList.setText(GlobalCore.Translations.Get("refreshPqList"));
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
		lblGPX.setText(GlobalCore.Translations.Get("GPX"));

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
		lblGcVote.setText(GlobalCore.Translations.Get("GCVoteRatings"));

		scrollBox.addChild(checkBoxGcVote);
		scrollBox.addChild(lblGcVote);
	}

	private void createImageLine()
	{
		checkBoxPreloadImages = new chkBox("Image");
		checkBoxPreloadImages.setX(innerLeft);
		checkBoxPreloadImages.setY(checkBoxGcVote.getY() - margin - checkBoxPreloadImages.getHeight());

		lblImage = new Label(checkBoxPreloadImages.getMaxX() + margin, checkBoxPreloadImages.getY(), this.innerWidth - margin * 3
				- checkBoxPreloadImages.getWidth(), checkBoxPreloadImages.getHeight(), "");
		lblImage.setFont(Fonts.getNormal());
		lblImage.setText(GlobalCore.Translations.Get("PreloadImages"));

		scrollBox.addChild(checkBoxPreloadImages);
		scrollBox.addChild(lblImage);
	}

	private void createMapLine()
	{
		checkBoxImportMaps = new chkBox("Image");
		checkBoxImportMaps.setX(innerLeft);
		checkBoxImportMaps.setY(checkBoxPreloadImages.getY() - margin - checkBoxImportMaps.getHeight());

		lblMaps = new Label(checkBoxImportMaps.getMaxX() + margin, checkBoxImportMaps.getY(), this.innerWidth - margin * 3
				- checkBoxImportMaps.getWidth(), checkBoxImportMaps.getHeight(), "");
		lblMaps.setFont(Fonts.getNormal());
		lblMaps.setText(GlobalCore.Translations.Get("Maps"));

		scrollBox.addChild(checkBoxImportMaps);
		scrollBox.addChild(lblMaps);
	}

	private void Layout()
	{
		checkBoxImportMaps.setY(margin);
		lblMaps.setY(margin);

		checkBoxPreloadImages.setY(lblMaps.getMaxY() + margin);
		lblImage.setY(lblMaps.getMaxY() + margin);

		checkBoxGcVote.setY(lblImage.getMaxY() + margin);
		lblGcVote.setY(lblImage.getMaxY() + margin);

		checkBoxImportGPX.setY(lblGcVote.getMaxY() + margin);
		lblGPX.setY(lblGcVote.getMaxY() + margin);

		PQ_ListCollabseBox.setY(checkBoxImportGPX.getMaxY() + margin);
		PQ_ListCollabseBox.setHeight(CollabseBoxHeight);

		checkImportPQfromGC.setY(PQ_ListCollabseBox.getMaxY() + margin);
		lblPQ.setY(PQ_ListCollabseBox.getMaxY() + margin);

		innerHeight = lblPQ.getMaxY() + margin;
		scrollBox.setInerHeight(innerHeight);
	}

	private void initialForm()
	{
		checkBoxImportMaps.setChecked(Config.settings.CacheMapData.getValue());
		checkBoxPreloadImages.setChecked(Config.settings.CacheImageData.getValue());
		checkBoxImportGPX.setChecked(Config.settings.ImportGpx.getValue());
		checkBoxImportGPX.setOnCheckedChangeListener(checkBoxImportGPX_CheckStateChanged);
		checkImportPQfromGC.setOnCheckedChangeListener(checkImportPQfromGC_CheckStateChanged);
		checkBoxGcVote.setChecked(Config.settings.ImportRatings.getValue());

		if (Config.settings.GcAPI.getValue().length() > 0)
		{
			checkImportPQfromGC.setChecked(Config.settings.ImportPQsFromGeocachingCom.getValue());
			checkImportPQfromGC.setEnabled(true);
		}
		else
		{
			checkImportPQfromGC.setChecked(false);
			checkImportPQfromGC.setEnabled(false);

			checkImportPQfromGC.setHeight(0);
			CollabseBoxHeight = 0;
			lblPQ.setHeight(0);

		}

		if (checkImportPQfromGC.isChecked() == true)
		{
			checkBoxImportGPX.setChecked(true);
			checkBoxImportGPX.setEnabled(false);
		}

	}

	private OnCheckedChangeListener checkBoxImportGPX_CheckStateChanged = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(chkBox view, boolean isChecked)
		{

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
			}
			else
			{
				checkBoxImportGPX.setEnabled(true);
			}
		}
	};

}
