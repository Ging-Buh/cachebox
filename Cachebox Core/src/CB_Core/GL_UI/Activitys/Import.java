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
import CB_Core.Api.PocketQuery;
import CB_Core.Api.PocketQuery.PQ;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.ProgressChangedEvent;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CollabseBox;
import CB_Core.GL_UI.Controls.CollabseBox.animatetHeightChangedListner;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Import extends ActivityBase implements ProgressChangedEvent
{

	private V_ListView lvPQs;
	private Button bOK, bCancel, refreshPqList;
	private float innerLeft, innerWidth, innerHeight, CollabseBoxHeight, CollabseBoxMaxHeight;
	private Label lblTitle, lblPQ, lblGPX, lblGcVote, lblImage, lblMaps, lblProgressMsg;
	private ProgressBar pgBar;
	private chkBox checkImportPQfromGC, checkBoxImportGPX, checkBoxGcVote, checkBoxPreloadImages, checkBoxImportMaps;
	private CollabseBox PQ_ListCollabseBox;
	private Timer mAnimationTimer;
	private long ANIMATION_TICK = 450;
	private int animationValue = 0;

	private Date ImportStart;
	// private int LogImports;
	// private int CacheImports;

	private Boolean importCancel = false;
	private Boolean importStarted = false;

	private ArrayList<PQ> PqList;
	private CustomAdapter lvAdapter;

	private CB_RectF itemRec;
	private float itemHeight = -1;

	private ScrollBox scrollBox;
	private disable dis;

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
		scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
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

				if (importStarted) cancelImport();
				else
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

		pgBar.setProgress(0, "");

		float SmallLineHeight = Fonts.MesureSmall("T").height;

		lblProgressMsg = new Label(Left + margin, lblTitle.getY() - margin - SmallLineHeight, this.getWidth() - Left - Right - margin
				- margin, SmallLineHeight, "ProgressMsg");

		lblProgressMsg.setFont(Fonts.getSmall());

		this.addChild(pgBar);
		this.addChild(lblProgressMsg);

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

		PQ_ListCollabseBox = new CollabseBox(rec, "PqCollabse");
		PQ_ListCollabseBox.setBackground(this.getBackground());

		refreshPqList = new Button(name);
		refreshPqList.setWidth(PQ_ListCollabseBox.getWidth() - margin - margin);
		refreshPqList.setX(margin);
		refreshPqList.setY(margin);
		refreshPqList.setText(GlobalCore.Translations.Get("refreshPqList"));
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

		lvPQs.setEmptyMsg(GlobalCore.Translations.Get("EmptyPqList"));

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
		// PQ_ListCollabseBox.setHeight(CollabseBoxHeight);

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
		checkImportPQfromGC.setOnCheckedChangeListener(checkImportPQfromGC_CheckStateChanged);
		checkBoxGcVote.setChecked(Config.settings.ImportRatings.getValue());

		if (Config.settings.GcAPI.getValue().length() > 0)
		{
			checkImportPQfromGC.setChecked(Config.settings.ImportPQsFromGeocachingCom.getValue());
			checkImportPQfromGC.setEnabled(true);
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

			checkImportPQfromGC.setHeight(0);
			CollabseBoxHeight = 0;
			lblPQ.setHeight(0);

		}

		if (checkImportPQfromGC.isChecked() == true)
		{
			checkBoxImportGPX.setChecked(true);
			checkBoxImportGPX.setEnabled(false);
		}

		PQ_ListCollabseBox.setAnimationListner(new animatetHeightChangedListner()
		{

			@Override
			public void animatetHeightCanged(float Height)
			{
				Layout();
			}
		});

	}

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
				itemHeight = UiSizes.getChkBoxSize().height + UiSizes.getChkBoxSize().halfHeight;
				float itemWidth = PQ_ListCollabseBox.getWidth() - PQ_ListCollabseBox.getLeftWidth() - PQ_ListCollabseBox.getRightWidth();

				itemRec = new CB_RectF(new SizeF(itemWidth, itemHeight));
			}

			return new Import_PqListItem(itemRec, position, pq);

		}

		@Override
		public float getItemSize(int position)
		{
			if (itemHeight == -1) itemHeight = UiSizes.getChkBoxSize().height + UiSizes.getChkBoxSize().halfHeight;
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
				PocketQuery.GetPocketQueryList(Config.GetAccessToken(), PqList);
				lvPQs.setBaseAdapter(new CustomAdapter());
				lvPQs.notifyDataSetChanged();

				stopTimer();
				lvPQs.setEmptyMsg(GlobalCore.Translations.Get("EmptyPqList"));

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

				lvPQs.setEmptyMsg(GlobalCore.Translations.Get("LoadPqList") + s);

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

		// disable UI
		dis = new disable(scrollBox);
		dis.setBackground(getBackground());

		this.addChild(dis, false);

		Config.settings.CacheMapData.setValue(checkBoxImportMaps.isChecked());
		Config.settings.CacheImageData.setValue(checkBoxPreloadImages.isChecked());
		Config.settings.ImportGpx.setValue(checkBoxImportGPX.isChecked());

		Config.settings.ImportPQsFromGeocachingCom.setValue(checkImportPQfromGC.isChecked());
		Config.settings.ImportRatings.setValue(checkBoxGcVote.isChecked());
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

					if (checkBoxPreloadImages.isChecked())
					{
						ip.addStep(ip.new Step("importImages", 4));
					}

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

					if (checkBoxPreloadImages.isChecked())
					{
						importer.importImages(GlobalCore.LastFilter.getSqlWhere(), ip);
						if (importCancel)
						{
							importCanceld();
							return;
						}
					}

					Thread.sleep(1000);
					if (checkBoxImportMaps.isChecked()) importer.importMaps();

					Thread.sleep(1000);
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

				// finish close activity and notifay changes

				CachListChangedEventList.Call();

				Date Importfin = new Date();
				long ImportZeit = Importfin.getTime() - ImportStart.getTime();

				String Msg = "Import " + String.valueOf(GPXFileImporter.CacheCount) + "C " + String.valueOf(GPXFileImporter.LogCount)
						+ "L in " + String.valueOf(ImportZeit);

				Logger.DEBUG(Msg);
				// MessageBox.Show("Import fertig! " + Msg);

				FilterProperties props = GlobalCore.LastFilter;

				EditFilterSettings.ApplyFilter(props);

				GL_Listener.glListener.Toast(Msg, 3000);

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
			iconImage.setSprite(SpriteCache.Icons.get(51));
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
					GL_Listener.glListener.renderOnce("WaitRotateAni");
				}
			}
		};

		public void renderWithoutScissor(SpriteBatch batch)
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

				Color trans = new Color(0, 0.3f, 0, 0.25f);
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
