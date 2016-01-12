package de.CB.TestBase;

import CB_UI_Base.Events.PlatformConnector.IgetFileListener;
import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public final class CB_Android_FileExplorer implements IgetFileListener, IgetFolderListener {
    static IgetFileReturnListener getFileReturnListener = null;
    static IgetFolderReturnListener getFolderReturnListener = null;
    public static Activity UsedActivity;

    public CB_Android_FileExplorer(Activity activity) {
	UsedActivity = activity;
	new Android_FileExplorer_OI();
	new Android_FileExplorer_ES();
    }

    @Override
    public void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
	getFileReturnListener = returnListener;
	getFolderReturnListener = null;

	// Try OI-Datei-Explorer
	if (!Android_FileExplorer_OI.getMyInstanz().getFile(initialPath, extension, TitleText, ButtonText, returnListener)) {
	    // Try ES-Datei-Explorer
	    if (!Android_FileExplorer_ES.getMyInstanz().getFile(initialPath, extension, TitleText, ButtonText, returnListener)) {
		// No compatible file manager was found.
		Toast.makeText(UsedActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
	    }
	}

    }

    @Override
    public void getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
	getFileReturnListener = null;
	getFolderReturnListener = returnListener;
	// Try OI-Datei-Explorer
	if (!Android_FileExplorer_OI.getMyInstanz().getfolder(initialPath, TitleText, ButtonText, returnListener)) {
	    // Try ES-Datei-Explorer
	    if (!Android_FileExplorer_ES.getMyInstanz().getfolder(initialPath, TitleText, ButtonText, returnListener)) {
		// No compatible file manager was found.
		Toast.makeText(UsedActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
	    }
	}
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (resultCode == android.app.Activity.RESULT_OK && data != null) {
	    // obtain the filename
	    Uri fileUri = data.getData();
	    if (fileUri != null) {
		String filePath = fileUri.getPath();
		if (filePath != null) {
		    if (getFileReturnListener != null)
			getFileReturnListener.getFileReturn(filePath);
		    if (getFolderReturnListener != null)
			getFolderReturnListener.getFolderReturn(filePath);
		}
	    }
	}
    }

}
