package CB_UI.GL_UI.Controls;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipException;

import CB_Core.Import.UnZip;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.MapDownload.MapRepositoryInfo;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.Downloader;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.CopyHelper.Copy;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class MapDownloadItem extends CB_View_Base
{
	private final MapRepositoryInfo mapInfo;
	private int lastProgress = 0;
	private Downloader dl;
	private chkBox checkBoxMap;
	private ProgressBar pgBar;
	private float margin;
	private Label lblName, lblSize;
	private boolean canceld = false;

	public MapDownloadItem(MapRepositoryInfo mapInfo, float ItemWidth)
	{
		super(mapInfo.Name);
		this.mapInfo = mapInfo;
		margin = UI_Size_Base.that.getMargin();

		checkBoxMap = new chkBox("Image");
		this.setHeight(checkBoxMap.getHeight() + (margin * 2));
		this.setWidth(ItemWidth);
		checkBoxMap.setX(margin);

		checkBoxMap.setY(margin);

		lblName = new Label(checkBoxMap.getMaxX() + margin, checkBoxMap.getY(), innerWidth - margin * 3 - checkBoxMap.getWidth(),
				checkBoxMap.getHeight(), "");
		lblName.setFont(Fonts.getNormal());
		lblName.setText(mapInfo.Name);

		lblSize = new Label(checkBoxMap.getMaxX() + margin, checkBoxMap.getY(), innerWidth - margin * 3 - checkBoxMap.getWidth(),
				checkBoxMap.getHeight(), "");
		lblSize.setFont(Fonts.getNormal());

		// Format Size
		int s = mapInfo.Size / 1024 / 1024;
		lblSize.setHAlignment(HAlignment.RIGHT);
		lblSize.setText(s + " MB");

		this.addChild(checkBoxMap);
		this.addChild(lblName);
		this.addChild(lblSize);

		chkExists();
	}

	private void chkExists()
	{
		int slashPos = mapInfo.Url.lastIndexOf("/");
		String zipFile = mapInfo.Url.substring(slashPos, mapInfo.Url.length());

		String FileString = FileIO.GetFileNameWithoutExtension(zipFile);

		String workPath = Config.MapPackFolder.getValue();

		File file = new File(workPath + "/" + FileString);

		if (file.exists())
		{
			checkBoxMap.setChecked(true);
			checkBoxMap.disable();
			checkBoxMap.setOnClickListener(new OnClickListener()
			{

				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					if (checkBoxMap.isDisabled())
					{
						checkBoxMap.enable();
					}
					else
					{
						checkBoxMap.setChecked(true);
						checkBoxMap.disable();
					}

					return true;
				}
			});
		}
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	private AtomicBoolean DownloadRuns = new AtomicBoolean(false);

	public void beginDownload()
	{
		canceld = false;

		if (!checkBoxMap.isChecked() || checkBoxMap.isDisabled())
		{
			lastProgress = -1;
			return;
		}

		DownloadRuns.set(true);
		float ProgressHeight = (SpriteCacheBase.ProgressBack.getBottomHeight() + SpriteCacheBase.ProgressBack.getTopHeight());
		CB_RectF rec = new CB_RectF(checkBoxMap.getMaxX() + margin, 0, innerWidth - margin * 3 - checkBoxMap.getWidth(), ProgressHeight);

		if (pgBar == null)
		{
			pgBar = new ProgressBar(rec, "");
			this.addChild(pgBar);
			lblName.setY(pgBar.getHalfHeight() - margin);
			lblSize.setY(pgBar.getHalfHeight() - margin);
		}

		lastProgress = 0;

		Thread runThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				int slashPos = mapInfo.Url.lastIndexOf("/");
				String zipFile = mapInfo.Url.substring(slashPos + 1, mapInfo.Url.length());

				String workPath = Config.MapPackFolder.getValue();

				if (workPath.length() == 0 || !(new File(workPath).isDirectory()))
				{
					workPath = Config.MapPackFolder.getDefaultValue();
				}

				File target = new File(workPath + "/" + zipFile);

				try
				{
					dl = new Downloader(new URL(mapInfo.Url), target);
				}
				catch (MalformedURLException e)
				{
					return;
				}

				pgBar.setProgress(lastProgress, lastProgress + " %");

				Thread dlProgressChecker = new Thread(new Runnable()
				{

					@Override
					public void run()
					{

						while (!dl.isCompleted())
						{
							// chk if canceld
							if (canceld)
							{
								dl.cancel();
								break;
							}

							int progress = dl.getProgressPercent();
							if (lastProgress != progress)
							{
								lastProgress = progress;
								pgBar.setProgress(lastProgress, lastProgress + " %");
							}
							try
							{
								Thread.sleep(100);
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

					}
				});

				dlProgressChecker.start();

				dl.run();

				try
				{
					UnZip.extractFolder(target.getAbsolutePath());
				}
				catch (ZipException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Copy and Clear
				File folder = new File(workPath + "/" + FileIO.GetFileNameWithoutExtension(zipFile));
				File newfolder = new File(workPath + "/" + FileIO.GetFileNameWithoutExtension(folder.getName()));

				if (folder.isDirectory())
				{
					folder.renameTo(newfolder);

					try
					{
						Copy.copyFolder(newfolder, new File(workPath));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}

					deleteDirectory(newfolder);
				}
				target.delete();

				lastProgress = canceld ? 0 : 100;
				pgBar.setProgress(lastProgress, lastProgress + " %");
				DownloadRuns.set(false);
			}
		});

		runThread.start();

	}

	public static boolean deleteDirectory(File directory)
	{
		if (directory.exists())
		{
			File[] files = directory.listFiles();
			if (null != files)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						deleteDirectory(files[i]);
					}
					else
					{
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

	public void cancelDownload()
	{
		canceld = true;
	}

	public int getDownloadProgress()
	{
		return lastProgress;
	}

	public boolean isFinish()
	{
		if (dl == null)
		{
			if (DownloadRuns.get()) return false;
			else
				return true;
		}
		return dl.isCompleted();
	}
}
