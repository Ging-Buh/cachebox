package de.cachebox_test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.fileProvider.FilenameFilter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;

public class Android_FileExplorer {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Android_FileExplorer.class);
	private static final String PARENT_DIR = "..";
	// private final String TAG = getClass().getName();
	private String[] fileList;
	private File currentPath;

	private IgetFileReturnListener CB_FileReturnListener;
	private IgetFolderReturnListener CB_FolderReturnListener;
	private final Activity activity;
	private boolean selectDirectoryOption;
	private String fileEndsWith;
	private final String TitleText;
	private final String ButtonText;

	/**
	 * @param activity 
	 * @param initialPath
	 */
	public Android_FileExplorer(Activity activity, File initialPath, String TitleText, String ButtonText) {
		this(activity, initialPath, TitleText, ButtonText, null);
	}

	public Android_FileExplorer(Activity activity, File initialPath, String TitleText, String ButtonText, String fileEndsWith) {
		this.activity = activity;
		setFileEndsWith(fileEndsWith);
		if (!initialPath.exists())
			initialPath = FileFactory.createFile(Environment.getExternalStorageDirectory().getAbsolutePath());
		currentPath = initialPath;
		this.TitleText = TitleText;
		if (ButtonText == null || ButtonText.length() == 0) {
			this.ButtonText = "Ok";
		} else {
			this.ButtonText = ButtonText;
		}
	}

	public void setFileReturnListener(IgetFileReturnListener returnListener) {
		CB_FileReturnListener = returnListener;
	}

	public void setFolderReturnListener(IgetFolderReturnListener returnListener) {
		CB_FolderReturnListener = returnListener;
	}

	/**
	 * @return file dialog
	 */
	private Dialog createFileDialog() {
		Dialog dialog = null;

		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			loadFileList(currentPath);
			String shownPath = currentPath.getAbsolutePath();
			int l = shownPath.length();
			if (l > 30) {
				shownPath = "..." + shownPath.substring(l - 30);
			}
			if (TitleText == null || TitleText.length() == 0) {
				builder.setTitle(shownPath);
			} else {
				builder.setTitle(TitleText + "\n" + shownPath);
			}

			if (selectDirectoryOption) {
				builder.setPositiveButton(ButtonText, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						CB_FolderReturnListener.getFolderReturn(currentPath.getAbsolutePath());
					}
				});
			}

			builder.setItems(fileList, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String fileChosen = fileList[which];
					File chosenFile = getChosenFile(fileChosen);
					if (chosenFile.isDirectory()) {
						loadFileList(chosenFile);
						dialog.cancel();
						dialog.dismiss();
						showDialog();
					} else {
						CB_FileReturnListener.getFileReturn(chosenFile.getAbsolutePath());
					}
				}
			});

			dialog = builder.show();

		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}

		return dialog;
	}

	public void setSelectDirectoryOption(boolean selectDirectoryOption) {
		this.selectDirectoryOption = selectDirectoryOption;
	}

	/**
	 * Show file dialog
	 */
	public void showDialog() {
		try {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					createFileDialog().show();
				}
			});
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
	}

	private void loadFileList(File path) {
		this.currentPath = path;
		List<String> r = new ArrayList<String>();
		if (path.exists()) {
			if (path.getParentFile() != null)
				r.add(PARENT_DIR);
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = FileFactory.createFile(dir, filename);
					if (!sel.canRead())
						return false;
					if (selectDirectoryOption)
						return sel.isDirectory();
					else {
						boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;
						return endsWith || sel.isDirectory();
					}
				}
			};
			String[] fileList1 = path.list(filter);
			for (String file : fileList1) {
				r.add(file);
			}
		}
		fileList = r.toArray(new String[] {});
	}

	private File getChosenFile(String fileChosen) {
		if (fileChosen.equals(PARENT_DIR))
			return currentPath.getParentFile();
		else
			return FileFactory.createFile(currentPath, fileChosen);
	}

	private void setFileEndsWith(String fileEndsWith) {
		this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
	}

}
