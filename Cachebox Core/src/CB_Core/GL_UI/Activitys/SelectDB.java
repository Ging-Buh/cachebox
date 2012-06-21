package CB_Core.GL_UI.Activitys;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FileList;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Dialogs.StringInputBox;
import CB_Core.GL_UI.Controls.Dialogs.Toast;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Categories;

public class SelectDB extends ActivityBase
{
	private int autoStartTime = 10;
	private int autoStartCounter = 0;
	private String DBPath;
	private Button bNew;
	private Button bSelect;
	private Button bCancel;
	private Button bAutostart;
	private V_ListView lvFiles;
	CustomAdapter lvAdapter;

	public static File AktFile = null;

	private String[] countList;

	private boolean MusstSelect = false;

	public SelectDB(CB_RectF rec, String Name, boolean mustSelect)
	{
		super(rec, Name);
		MusstSelect = mustSelect;
		DBPath = FileIO.GetDirectoryName(Config.settings.DatabasePath.getValue());
		String DBFile = FileIO.GetFileName(Config.settings.DatabasePath.getValue());

		// lvFiles = (ListView) findViewById(R.id.sdb_list);
		final FileList files = new FileList(Config.WorkPath, "DB3", true);
		countList = new String[files.size()];

		int index = 0;
		for (File file : files)
		{
			if (file.getName().equalsIgnoreCase(DBFile)) AktFile = file;
			countList[index] = "";
			index++;
		}

		lvFiles = new V_ListView(new CB_RectF(Left, Bottom + UiSizes.getButtonHeight() * 2, width - Left - Right, height
				- (UiSizes.getButtonHeight() * 2) - Top - Bottom), "DB File ListView");

		lvAdapter = new CustomAdapter(files);
		lvFiles.setBaseAdapter(lvAdapter);

		this.addChild(lvFiles);

		float btWidth = (width - Left - Right) / 3;

		bNew = new Button(new CB_RectF(Left, Bottom, btWidth, UiSizes.getButtonHeight()), "selectDB.bNew");
		bSelect = new Button(new CB_RectF(bNew.getMaxX(), Bottom, btWidth, UiSizes.getButtonHeight()), "selectDB.bSelect");
		bCancel = new Button(new CB_RectF(bSelect.getMaxX(), Bottom, btWidth, UiSizes.getButtonHeight()), "selectDB.bCancel");
		bAutostart = new Button(new CB_RectF(Left, bNew.getMaxY(), width - Left - Right, UiSizes.getButtonHeight()), "selectDB.bAutostart");

		this.addChild(bSelect);
		this.addChild(bNew);
		this.addChild(bCancel);
		this.addChild(bAutostart);

		// New Button
		bNew.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				stopTimer();
				StringInputBox.Show(GlobalCore.Translations.Get("NewDB"), GlobalCore.Translations.Get("InsNewDBName"), "NewDB",
						DialogListnerNewDB);
				return true;
			}
		});

		// Select Button
		bSelect.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				stopTimer();
				if (AktFile == null)
				{
					GL_Listener.glListener.Toast("Please select Database!", Toast.LENGTH_SHORT);
					return false;
				}
				selectDB();
				return true;
			}
		});

		// Cancel Button
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				stopTimer();
				if (MusstSelect)
				{
					selectDB();
				}
				else
				{
					finish();
				}

				return true;
			}
		});

		// AutoStart Button
		bAutostart.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				stopTimer();
				showSelectionMenu();
				return true;
			}

		});

		// Translations
		bNew.setText(GlobalCore.Translations.Get("NewDB"));
		bSelect.setText(GlobalCore.Translations.Get("confirm"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));
		bAutostart.setText(GlobalCore.Translations.Get("StartWithoutSelection"));

		autoStartTime = Config.settings.MultiDBAutoStartTime.getValue();
		if (autoStartTime > 0)
		{
			autoStartCounter = autoStartTime;
			bAutostart.setText(autoStartCounter + " " + GlobalCore.Translations.Get("confirm"));
			setAutoStartText();
			if ((autoStartTime > 0) && (AktFile != null))
			{
				updateTimer = new Timer();
				updateTimer.scheduleAtFixedRate(timerTask, 1000, 1000);
			}
			else
				stopTimer();
		}

		readCountatThread();
	}

	TimerTask timerTask = new TimerTask()
	{

		@Override
		public void run()
		{
			if (autoStartCounter == 0)
			{
				stopTimer();
				selectDB();
			}
			else
			{
				autoStartCounter--;
				bAutostart.setText(autoStartCounter + "    " + GlobalCore.Translations.Get("confirm"));
			}
		}
	};

	private void readCountatThread()
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				int index = 0;
				for (File file : lvAdapter.files)
				{
					String LastModifit = sdf.format(file.lastModified());
					String FileSize = String.valueOf(file.length() / (1024 * 1024)) + "MB";
					String CacheCount = String.valueOf(Database.Data.getCacheCountInDB(file.getAbsolutePath()));
					countList[index] = CacheCount + " Caches  " + FileSize + "    last use " + LastModifit;
					index++;
				}

				lvFiles.setBaseAdapter(lvAdapter);
			}
		};
		thread.start();
	}

	@Override
	protected void Initial()
	{
		// Set selected item
		for (int i = 0; i < lvAdapter.getCount(); i++)
		{
			File file = lvAdapter.getItem(i);
			if (file.equals(AktFile))
			{
				lvFiles.setSelection(i);
			}
		}

		int itemSpace = lvFiles.getMaxItemCount();

		if (itemSpace >= lvAdapter.getCount())
		{
			lvFiles.setUndragable();
		}
		else
		{
			lvFiles.setDragable();
		}

	}

	OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			stopTimer();
			File file = null;

			for (int i = 0; i < lvAdapter.getCount(); i++)
			{
				if (v.getName().equals(lvAdapter.getItem(i).getName()))
				{
					file = lvAdapter.getItem(i);

					SelectDB.AktFile = file;
					lvFiles.setSelection(i);
					break;
				}
			}
			return true;

		}
	};

	@Override
	protected void SkinIsChanged()
	{
	}

	OnMsgBoxClickListener DialogListnerNewDB = new OnMsgBoxClickListener()
	{

		@Override
		public boolean onClick(int which)
		{
			String text = StringInputBox.editText.getText();
			// Behandle das ergebniss
			switch (which)
			{
			case 1: // ok Clicket

				String FilterString = Config.settings.Filter.getValue();
				GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0])
						: new FilterProperties(FilterString);
				String sqlWhere = GlobalCore.LastFilter.getSqlWhere();

				// initialize Database

				Config.settings.DatabasePath.setValue(Config.WorkPath + "/" + text + ".db3");
				String database = Config.settings.DatabasePath.getValue();
				Database.Data.StartUp(database);
				Config.AcceptChanges();

				GlobalCore.Categories = new Categories();
				Database.Data.GPXFilenameUpdateCacheCount();

				synchronized (Database.Data.Query)
				{
					CacheListDAO cacheListDAO = new CacheListDAO();
					cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
				}

				if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return true;
				Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

				Config.AcceptChanges();
				AktFile = new File(database);
				selectDB();

				break;
			case 2: // cancel clicket
				that.show();
				break;
			case 3:
				that.show();
				break;
			}

			return true;
		}
	};

	protected void selectDB()
	{

		if (AktFile == null)
		{
			GL_Listener.glListener.Toast("no DB selected", 200);
			return;
		}

		Config.settings.MultiDBAutoStartTime.setValue(autoStartTime);
		Config.settings.MultiDBAsk.setValue(autoStartTime >= 0);

		String name = AktFile.getName();
		// Toast.makeText(getApplicationContext(), name,
		// Toast.LENGTH_SHORT).show();

		String path = DBPath + "/" + name;
		// Toast.makeText(getApplicationContext(), path,
		// Toast.LENGTH_SHORT).show();

		Config.settings.DatabasePath.setValue(path);
		Config.AcceptChanges();

		// reload settings for get filter form selected DB
		Config.settings.ReadFromDB();

		if (returnListner != null) returnListner.back();
		finish();
	}

	private void setAutoStartText()
	{
		if (autoStartTime < 0) bAutostart.setText(GlobalCore.Translations.Get("AutoStart") + " "
				+ GlobalCore.Translations.Get("StartWithoutSelection"));
		else if (autoStartTime == 0) bAutostart.setText(GlobalCore.Translations.Get("AutoStart") + " "
				+ GlobalCore.Translations.Get("AutoStartDisabled"));
		else
			bAutostart.setText(GlobalCore.Translations.Get("AutoStart") + " "
					+ GlobalCore.Translations.Get("AutoStartTime", String.valueOf(autoStartTime)));
	}

	public class CustomAdapter implements Adapter
	{

		private FileList files;
		private CB_RectF recItem;

		public CustomAdapter(FileList files)
		{
			this.files = files;
			recItem = UiSizes.getCacheListItemRec().asFloat();
			recItem.setHeight(recItem.getHeight() * 0.8f);
			recItem.setWidth(width - Left - Right);
		}

		public void setFiles(FileList files)
		{
			this.files = files;
		}

		@Override
		public int getCount()
		{
			return files.size();
		}

		public File getItem(int position)
		{
			return files.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			SelectDBItem v = new SelectDBItem(recItem, position, files.get(position), countList[position]);
			v.setOnClickListener(onItemClickListner);
			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			return recItem.getHeight();
		}

	}

	private void stopTimer()
	{
		if (updateTimer != null) updateTimer.cancel();
		bAutostart.setText(GlobalCore.Translations.Get("confirm"));
	}

	private ReturnListner returnListner;

	public void setReturnListner(ReturnListner listner)
	{
		returnListner = listner;
	}

	public interface ReturnListner
	{
		public void back();
	}

	private final int MI_START_WITHOUT_SELECTION = 0;
	private final int MI_AUTO_START_DISABLED = 1;
	private final int MI_5 = 2;
	private final int MI_10 = 3;
	private final int MI_25 = 4;
	private final int MI_60 = 5;

	Timer updateTimer;

	private void showSelectionMenu()
	{
		final String[] cs = new String[6];
		cs[0] = GlobalCore.Translations.Get("StartWithoutSelection");
		cs[1] = GlobalCore.Translations.Get("AutoStartDisabled");
		cs[2] = GlobalCore.Translations.Get("AutoStartTime", "5");
		cs[3] = GlobalCore.Translations.Get("AutoStartTime", "10");
		cs[4] = GlobalCore.Translations.Get("AutoStartTime", "25");
		cs[5] = GlobalCore.Translations.Get("AutoStartTime", "60");

		Menu cm = new Menu("MiscContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_START_WITHOUT_SELECTION:
					autoStartTime = -1;
					setAutoStartText();
					break;
				case MI_AUTO_START_DISABLED:
					autoStartTime = 0;
					setAutoStartText();
					break;
				case MI_5:
					autoStartTime = 5;
					setAutoStartText();
					break;
				case MI_10:
					autoStartTime = 10;
					setAutoStartText();
					break;
				case MI_25:
					autoStartTime = 25;
					setAutoStartText();
					break;
				case MI_60:
					autoStartTime = 60;
					setAutoStartText();
					break;

				}
				that.show();
				return true;
			}
		});

		cm.addItem(MI_START_WITHOUT_SELECTION, cs[0], true);
		cm.addItem(MI_AUTO_START_DISABLED, cs[1], true);
		cm.addItem(MI_5, cs[2], true);
		cm.addItem(MI_10, cs[3], true);
		cm.addItem(MI_25, cs[4], true);
		cm.addItem(MI_60, cs[5], true);

		cm.show();
	}

}
