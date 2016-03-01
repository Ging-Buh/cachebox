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

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Types.Categories;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.Dialogs.NewDB_InputBox;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewBase.IListPosChanged;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.Scrollbar;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Math.Point;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.FileList;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

/**
 * @author ging-buh
 * @author Longri
 */
public class SelectDB extends ActivityBase {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(SelectDB.class);
	private int autoStartTime = 10;
	private int autoStartCounter = 0;
	private String DBPath;
	private Button bNew;
	private Button bSelect;
	private Button bCancel;
	private Button bAutostart;
	private V_ListView lvFiles;
	private Scrollbar scrollbar;
	private CustomAdapter lvAdapter;
	private File AktFile = null;
	private String[] countList;
	private boolean MustSelect = false;
	private IReturnListener returnListener;

	public SelectDB(CB_RectF rec, String Name, boolean mustSelect) {
		super(rec, Name);
		MustSelect = mustSelect;
		DBPath = FileIO.GetDirectoryName(Config.DatabasePath.getValue());

		if (DBPath.endsWith(".db3")) {
			Config.DatabasePath.setValue(DBPath);
			Config.AcceptChanges();
			DBPath = FileIO.GetDirectoryName(DBPath);
		}

		String DBFile = FileIO.GetFileName(Config.DatabasePath.getValue());

		final FileList files = new FileList(Config.mWorkPath, "DB3", true);
		countList = new String[files.size()];

		int index = 0;
		for (File file : files) {
			if (file.getName().equalsIgnoreCase(DBFile))
				AktFile = file;
			countList[index] = "";
			index++;
		}

		lvFiles = new V_ListView(new CB_RectF(leftBorder, this.getBottomHeight() + UI_Size_Base.that.getButtonHeight() * 2, innerWidth, getHeight() - (UI_Size_Base.that.getButtonHeight() * 2) - this.getTopHeight() - this.getBottomHeight()),
				"DB File ListView");

		lvAdapter = new CustomAdapter(files);
		lvFiles.setBaseAdapter(lvAdapter);

		this.addChild(lvFiles);

		this.scrollbar = new Scrollbar(lvFiles);
		this.addChild(this.scrollbar);
		this.lvFiles.addListPosChangedEventHandler(new IListPosChanged() {

			@Override
			public void ListPosChanged() {
				scrollbar.ScrollPositionChanged();
			}
		});

		float btWidth = innerWidth / 3;

		bNew = new Button(new CB_RectF(leftBorder, this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bNew");
		bSelect = new Button(new CB_RectF(bNew.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bSelect");
		bCancel = new Button(new CB_RectF(bSelect.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bCancel");
		bAutostart = new Button(new CB_RectF(leftBorder, bNew.getMaxY(), innerWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bAutostart");

		this.addChild(bSelect);
		this.addChild(bNew);
		this.addChild(bCancel);
		this.addChild(bAutostart);

		// New Button
		bNew.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				stopTimer();
				NewDB_InputBox.Show(WrapType.SINGLELINE, Translation.Get("NewDB"), Translation.Get("InsNewDBName"), "NewDB", mDialogListenerNewDB);
				return true;
			}
		});

		// Select Button
		bSelect.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				stopTimer();
				if (AktFile == null) {
					GL.that.Toast("Please select Database!", Toast.LENGTH_SHORT);
					return false;
				}
				selectDB();
				return true;
			}
		});

		// Cancel Button
		bCancel.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				stopTimer();
				if (MustSelect) {
					TabMainView.actionClose.CallExecute();
				} else {
					finish();
				}

				return true;
			}
		});

		// AutoStart Button
		bAutostart.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				stopTimer();
				showSelectionMenu();
				return true;
			}

		});

		// Translations
		bNew.setText(Translation.Get("NewDB"));
		bSelect.setText(Translation.Get("confirm"));
		bCancel.setText(Translation.Get("cancel"));

		autoStartTime = Config.MultiDBAutoStartTime.getValue();
		if (autoStartTime > 0) {
			autoStartCounter = autoStartTime;
			bAutostart.setText(autoStartCounter + " " + Translation.Get("confirm"));
			if ((autoStartTime > 0) && (AktFile != null)) {
				updateTimer = new Timer();
				updateTimer.scheduleAtFixedRate(timerTask, 1000, 1000);
			} else
				stopTimer();
		}
		setAutoStartText();

		this.isClickable();

		readCountatThread();
	}

	TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if (autoStartCounter == 0) {
				stopTimer();
				selectDB();
			} else {
				autoStartCounter--;
				bAutostart.setText(autoStartCounter + "    " + Translation.Get("confirm"));
			}
		}
	};

	private void readCountatThread() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				int index = 0;
				for (File file : lvAdapter.files) {
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
	protected void Initial() {

	}

	@Override
	public void onShow() {

		int itemSpace = lvFiles.getMaxItemCount();

		if (itemSpace >= lvAdapter.getCount()) {
			lvFiles.setUnDraggable();
		} else {
			lvFiles.setDraggable();
		}

		TimerTask task = new TimerTask() {
			@Override
			public void run() {

				// Set selected item
				for (int i = 0; i < lvAdapter.getCount(); i++) {
					File file = lvAdapter.getItem(i);

					try {
						if (file.getAbsoluteFile().compareTo(AktFile.getAbsoluteFile()) == 0) {
							lvFiles.setSelection(i);
						}

						Point firstAndLast = lvFiles.getFirstAndLastVisibleIndex();

						if (!(firstAndLast.x < i && firstAndLast.y > i))
							lvFiles.scrollToItem(i);
					} catch (Exception e) {
						log.error("select item", e);
					}
				}

				GL.that.RunOnGL(new IRunOnGL() {

					@Override
					public void run() {
						setSelectedItemVisible();
					}
				});

				resetInitial();
				lvFiles.chkSlideBack();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 350);

		GL.that.renderOnce();
	}

	private void setSelectedItemVisible() {
		int id = 0;
		Point firstAndLast = lvFiles.getFirstAndLastVisibleIndex();

		try {
			for (File file : lvAdapter.getFileList()) {
				if (file.getAbsoluteFile().compareTo(AktFile.getAbsoluteFile()) == 0) {
					lvFiles.setSelection(id);
					if (lvFiles.isDragable()) {
						if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
							lvFiles.scrollToItem(id);
							log.debug("Scroll to:" + id);
						}
					}
					break;
				}
				id++;
			}

			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					GL.that.RunOnGL(new IRunOnGL() {

						@Override
						public void run() {
							if (lvFiles != null) {
								lvFiles.chkSlideBack();
								GL.that.renderOnce();
							}
						}
					});
				}
			};

			Timer timer = new Timer();
			timer.schedule(task, 50);

			GL.that.renderOnce();
		} catch (Exception e) {
			log.error("Set selected DB Visible", e);
		}
	}

	OnClickListener onItemClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			stopTimer();
			File file = null;

			for (int i = 0; i < lvAdapter.getCount(); i++) {
				if (v.getName().equals(lvAdapter.getItem(i).getName())) {
					file = lvAdapter.getItem(i);

					AktFile = file;
					lvFiles.setSelection(i);
					break;
				}
			}
			return true;

		}
	};

	@Override
	protected void SkinIsChanged() {
	}

	OnMsgBoxClickListener mDialogListenerNewDB = new OnMsgBoxClickListener() {

		@Override
		public boolean onClick(int which, Object data) {
			String NewDB_Name = NewDB_InputBox.editText.getText();
			// Behandle das Ergebnis
			switch (which) {
			case 1: // ok clicked

				FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));
				String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());

				// initialize Database

				String database = Config.mWorkPath + GlobalCore.fs + NewDB_Name + ".db3";
				Config.DatabasePath.setValue(database);
				Database.Data.Close();
				Database.Data.StartUp(database);

				// OwnRepository?
				if (data != null && !(Boolean) data) {
					String folder = "?/" + NewDB_Name + "/";

					Config.DescriptionImageFolderLocal.setValue(folder + "Images");
					Config.MapPackFolderLocal.setValue(folder + "Maps");
					Config.SpoilerFolderLocal.setValue(folder + "Spoilers");
					Config.TileCacheFolderLocal.setValue(folder + "Cache");
					Config.AcceptChanges();
					log.debug(NewDB_Name + " has own Repository:\n" + //
							Config.DescriptionImageFolderLocal.getValue() + ", \n" + //
							Config.MapPackFolderLocal.getValue() + ", \n" + //
							Config.SpoilerFolderLocal.getValue() + ", \n" + //
							Config.TileCacheFolderLocal.getValue()//
					);

					// Create Folder?
					boolean creationOK = FileIO.createDirectory(Config.DescriptionImageFolderLocal.getValue());
					creationOK = creationOK && FileIO.createDirectory(Config.MapPackFolderLocal.getValue());
					creationOK = creationOK && FileIO.createDirectory(Config.SpoilerFolderLocal.getValue());
					creationOK = creationOK && FileIO.createDirectory(Config.TileCacheFolderLocal.getValue());
					if (!creationOK)
						log.debug("Problem with creation of one of the Directories:" + //
								Config.DescriptionImageFolderLocal.getValue() + ", " + //
								Config.MapPackFolderLocal.getValue() + ", " + //
								Config.SpoilerFolderLocal.getValue() + ", " + //
								Config.TileCacheFolderLocal.getValue()//
						);
				}

				Config.AcceptChanges();

				CoreSettingsForward.Categories = new Categories();
				Database.Data.GPXFilenameUpdateCacheCount();

				synchronized (Database.Data.Query) {
					CacheListDAO cacheListDAO = new CacheListDAO();
					cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
					GlobalCore.checkSelectedCacheValid();
				}

				if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
					return true;
				Database.FieldNotes.StartUp(Config.mWorkPath + "/User/FieldNotes.db3");

				Config.AcceptChanges();
				AktFile = FileFactory.createFile(database);
				selectDB();

				break;
			case 2: // cancel clicked
				SelectDB.this.show();
				break;
			case 3:
				SelectDB.this.show();
				break;
			}

			return true;
		}
	};

	protected void selectDB() {
		if (AktFile == null) {
			GL.that.Toast("no DB selected", 200);
			return;
		}

		Config.MultiDBAutoStartTime.setValue(autoStartTime);
		Config.MultiDBAsk.setValue(autoStartTime >= 0);

		String name = AktFile.getName();
		// Toast.makeText(getApplicationContext(), name,
		// Toast.LENGTH_SHORT).show();

		String path = DBPath + "/" + name;
		// Toast.makeText(getApplicationContext(), path,
		// Toast.LENGTH_SHORT).show();

		Config.DatabasePath.setValue(path);
		Config.AcceptChanges();

		ManagerBase.Manager.initialMapPacks();

		finish();
		if (returnListener != null)
			returnListener.back();

	}

	@Override
	protected void finish() {
		GL.that.closeActivity(!MustSelect);
	}

	private void setAutoStartText() {
		if (autoStartTime < 0)
			bAutostart.setText(Translation.Get("StartWithoutSelection"));
		else if (autoStartTime == 0)
			bAutostart.setText(Translation.Get("AutoStartDisabled"));
		else
			bAutostart.setText(Translation.Get("AutoStartTime", String.valueOf(autoStartTime)));
	}

	public class CustomAdapter implements Adapter {

		private FileList files;
		private final CB_RectF recItem;

		public CustomAdapter(FileList files) {
			this.files = files;
			recItem = UiSizes.that.getCacheListItemRec().asFloat();
			recItem.setHeight(recItem.getHeight() * 0.8f);
			recItem.setWidth(getWidth() - getLeftWidth() - getRightWidth() - (margin * 1.5f));
		}

		public void setFiles(FileList files) {
			this.files = files;
		}

		public FileList getFileList() {
			return files;
		}

		@Override
		public int getCount() {
			return files.size();
		}

		public File getItem(int position) {
			return files.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public ListViewItemBase getView(int position) {
			SelectDBItem v = new SelectDBItem(recItem, position, files.get(position), countList[position]);
			v.setOnClickListener(onItemClickListener);
			return v;
		}

		@Override
		public float getItemSize(int position) {
			return recItem.getHeight();
		}

	}

	private void stopTimer() {
		if (updateTimer != null)
			updateTimer.cancel();
		// bAutostart.setText(Translation.Get("confirm"));
	}

	public void setReturnListener(IReturnListener listener) {
		returnListener = listener;
	}

	public interface IReturnListener {
		public void back();
	}

	Timer updateTimer;

	private void showSelectionMenu() {
		final String[] cs = new String[6];
		cs[0] = Translation.Get("StartWithoutSelection");
		cs[1] = Translation.Get("AutoStartDisabled");
		cs[2] = Translation.Get("AutoStartTime", "5");
		cs[3] = Translation.Get("AutoStartTime", "10");
		cs[4] = Translation.Get("AutoStartTime", "25");
		cs[5] = Translation.Get("AutoStartTime", "60");

		Menu cm = new Menu("MiscContextMenu");

		cm.addOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

				switch (((MenuItem) v).getMenuItemId()) {
				case MenuID.MI_START_WITHOUT_SELECTION:
					autoStartTime = -1;
					setAutoStartText();
					break;
				case MenuID.MI_AUTO_START_DISABLED:
					autoStartTime = 0;
					setAutoStartText();
					break;
				case MenuID.MI_5:
					autoStartTime = 5;
					setAutoStartText();
					break;
				case MenuID.MI_10:
					autoStartTime = 10;
					setAutoStartText();
					break;
				case MenuID.MI_25:
					autoStartTime = 25;
					setAutoStartText();
					break;
				case MenuID.MI_60:
					autoStartTime = 60;
					setAutoStartText();
					break;

				}
				SelectDB.this.show();
				return true;
			}
		});

		cm.addItem(MenuID.MI_START_WITHOUT_SELECTION, cs[0], true);
		cm.addItem(MenuID.MI_AUTO_START_DISABLED, cs[1], true);
		cm.addItem(MenuID.MI_5, cs[2], true);
		cm.addItem(MenuID.MI_10, cs[3], true);
		cm.addItem(MenuID.MI_25, cs[4], true);
		cm.addItem(MenuID.MI_60, cs[5], true);

		cm.Show();
	}

	@Override
	public boolean canCloseWithBackKey() {
		return !MustSelect;
	}

	@Override
	public void dispose() {

		DBPath = null;
		if (bNew != null)
			bNew.dispose();
		bNew = null;
		if (bSelect != null)
			bSelect.dispose();
		bSelect = null;
		if (bCancel != null)
			bCancel.dispose();
		bCancel = null;
		if (bAutostart != null)
			bAutostart.dispose();
		bAutostart = null;
		if (lvFiles != null)
			lvFiles.dispose();
		lvFiles = null;
		if (scrollbar != null)
			scrollbar.dispose();
		scrollbar = null;

		lvAdapter = null;
		AktFile = null;
		countList = null;

		returnListener = null;
		super.dispose();
	}

}
