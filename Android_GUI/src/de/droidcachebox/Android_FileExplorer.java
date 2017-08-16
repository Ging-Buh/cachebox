package de.droidcachebox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;

import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_Utils.Log.Log;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.fileProvider.FilenameFilter;
import android.annotation.SuppressLint;
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
	private final String firstSDCard;
	private final String secondSDCard;

	/**
	 * @param activity 
	 * @param initialPath
	 */
	public Android_FileExplorer(Activity activity, File initialPath, String TitleText, String ButtonText) {
		this(activity, initialPath, TitleText, ButtonText, null);
	}

	@SuppressLint("NewApi")
	private Android_FileExplorer(Activity activity, File initialPath, String TitleText, String ButtonText, String fileEndsWith) {

		this.activity = activity;

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			java.io.File dirs[] = activity.getExternalFilesDirs(null);

			if (dirs.length > 0) {
				String tmp = dirs[0].getAbsolutePath();
				int pos = tmp.indexOf("Android") - 1;
				if (pos > 0)
					firstSDCard = tmp.substring(0, pos);
				else
					firstSDCard = "";
			} else {
				firstSDCard = "";
			}

			if (dirs.length > 1) {
				String tmp = dirs[1].getAbsolutePath();
				int pos = tmp.indexOf("Android") - 1;
				if (pos > 0)
					secondSDCard = tmp.substring(0, pos);
				else
					secondSDCard = "";
			} else {
				secondSDCard = "";
			}
		} else {
			String tmp = activity.getExternalFilesDir(null).getAbsolutePath();
			int pos = tmp.indexOf("Android") - 1;
			if (pos > 0)
				firstSDCard = tmp.substring(0, pos);
			else
				firstSDCard = "";
			// or get from firstSDCard = Environment.getExternalStorageDirectory().getAbsolutePath();
			secondSDCard = "";
		}
		// Log.info(log, "firstSDCard '" + firstSDCard + "'");
		// Log.info(log, "secondSDCard '" + secondSDCard + "'");

		setFileEndsWith(fileEndsWith);
		try {
			if (!initialPath.exists()) {
				initialPath = FileFactory.createFile(Environment.getExternalStorageDirectory().getAbsolutePath());
			}
		} catch (Exception e) {
			Log.err(log, e.getLocalizedMessage());
			initialPath = FileFactory.createFile(firstSDCard);
		}
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
						CB_FolderReturnListener.returnFolder(currentPath.getAbsolutePath());
					}
				});
			}

			builder.setItems(fileList, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					File chosenFile;
					try {
						chosenFile = getChosenFile(fileList[which]);

						if (chosenFile.isDirectory()) {
							loadFileList(chosenFile);
							dialog.cancel();
							dialog.dismiss();
							showDialog();
						} else {
							CB_FileReturnListener.returnFile(chosenFile.getAbsolutePath());
						}

					} catch (Exception e) {
						Log.err(log, e.getLocalizedMessage());
					}
				}
			});

			dialog = builder.show();

		} catch (Exception e) {
			Log.err(log, e.getLocalizedMessage());
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
			Log.err(log, e.getLocalizedMessage());
		}
	}

	private void loadFileList(File path) {
		List<String> r = new ArrayList<String>();
		currentPath = path;

		String absolutePath;
		try {
			absolutePath = currentPath.getAbsolutePath();
			if (!absolutePath.equals("/")) {
				r.add("/");
			}
			if (firstSDCard.length() > 0)
				if (!absolutePath.equals(firstSDCard))
					r.add(firstSDCard);
			if (secondSDCard.length() > 0)
				if (!absolutePath.equals(secondSDCard))
					r.add(secondSDCard);
			if (!absolutePath.equals("/")) {
				r.add(PARENT_DIR);
			}
		} catch (Exception e) {
			if (firstSDCard.length() > 0)
				r.add(firstSDCard);
			if (secondSDCard.length() > 0)
				r.add(secondSDCard);
		}

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				File sel = FileFactory.createFile(dir, filename);
				if (!sel.canRead())
					return false;
				if (selectDirectoryOption)
					return sel.isDirectory();
				else {
					boolean endsWith = fileEndsWith != null ? filename.toLowerCase(Locale.US).endsWith(fileEndsWith) : true;
					return endsWith || sel.isDirectory();
				}
			}
		};

		if (currentPath.exists()) {
			String[] tmpFileList = currentPath.list(filter);
			if (tmpFileList != null) {
				for (String file : tmpFileList) {
					r.add(file);
				}
			}
		}
		fileList = r.toArray(new String[] {});
	}

	private File getChosenFile(String fileChosen) {
		try {
			if (fileChosen.equals(PARENT_DIR))
				return currentPath.getParentFile();
			else if (fileChosen.startsWith("/"))
				return FileFactory.createFile(fileChosen);
			else
				return FileFactory.createFile(currentPath, fileChosen);
		} catch (Exception e) {
			Log.err(log, e.getLocalizedMessage());
		}
		return currentPath;
	}

	private void setFileEndsWith(String fileEndsWith) {
		this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
	}

}
