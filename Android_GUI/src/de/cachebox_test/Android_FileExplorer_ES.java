package de.cachebox_test;

import CB_Utils.fileProvider.File;

import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_Utils.fileProvider.FileFactory;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

public class Android_FileExplorer_ES extends CB_FileExplorer_Base {
	static Android_FileExplorer_ES that;

	public Android_FileExplorer_ES() {
		that = this;
	}

	@Override
	public boolean getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
		Intent intent = new Intent("com.estrongs.action.PICK_FILE");

		// // Construct URI from file name.
		// File file = new File(initialPath);
		// intent.setData(Uri.fromFile(file));

		// Set fancy title and button (optional)
		intent.putExtra("com.estrongs.intent.extra.TITLE", ButtonText);

		try {
			CB_Android_FileExplorer.UsedActivity.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR);
			return true;
		} catch (ActivityNotFoundException e) {
			return false;
		}

	}

	@Override
	public boolean getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
		Intent intent = new Intent("com.estrongs.action.PICK_DIRECTORY");

		// Construct URI from file name.
		File file = FileFactory.createFile(initialPath);
		intent.setData(Uri.fromFile(new java.io.File(file.getAbsolutePath())));

		// Set fancy title and button (optional)
		intent.putExtra("com.estrongs.intent.extra.TITLE", ButtonText);

		try {
			CB_Android_FileExplorer.UsedActivity.startActivityForResult(intent, Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR);
			return true;
		} catch (ActivityNotFoundException e) {
			return false;
		}
	}

	public static CB_FileExplorer_Base getMyInstanz() {
		if (that == null)
			that = new Android_FileExplorer_ES();
		return that;
	}

}
