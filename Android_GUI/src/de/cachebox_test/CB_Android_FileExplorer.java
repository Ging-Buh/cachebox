package de.cachebox_test;

import CB_UI.Events.platformConector.IgetFileListner;
import CB_UI.Events.platformConector.IgetFileReturnListner;
import CB_UI.Events.platformConector.IgetFolderListner;
import CB_UI.Events.platformConector.IgetFolderReturnListner;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public final class CB_Android_FileExplorer implements IgetFileListner, IgetFolderListner
{
	static IgetFileReturnListner getFileReturnListner = null;
	static IgetFolderReturnListner getFolderReturnListner = null;
	public static Activity UsedActivity;

	public CB_Android_FileExplorer(Activity activity)
	{
		UsedActivity = activity;
		new Android_FileExplorer_OI();
		new Android_FileExplorer_ES();
	}

	@Override
	public void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListner returnListner)
	{
		getFileReturnListner = returnListner;
		getFolderReturnListner = null;

		// Try OI-Datei-Explorer
		if (!Android_FileExplorer_OI.getMyInstanz().getFile(initialPath, extension, TitleText, ButtonText, returnListner))
		{
			// Try ES-Datei-Explorer
			if (!Android_FileExplorer_ES.getMyInstanz().getFile(initialPath, extension, TitleText, ButtonText, returnListner))
			{
				// No compatible file manager was found.
				Toast.makeText(UsedActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public void getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner)
	{
		getFileReturnListner = null;
		getFolderReturnListner = returnListner;
		// Try OI-Datei-Explorer
		if (!Android_FileExplorer_OI.getMyInstanz().getfolder(initialPath, TitleText, ButtonText, returnListner))
		{
			// Try ES-Datei-Explorer
			if (!Android_FileExplorer_ES.getMyInstanz().getfolder(initialPath, TitleText, ButtonText, returnListner))
			{
				// No compatible file manager was found.
				Toast.makeText(UsedActivity, "No compatible file manager found", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == android.app.Activity.RESULT_OK && data != null)
		{
			// obtain the filename
			Uri fileUri = data.getData();
			if (fileUri != null)
			{
				String filePath = fileUri.getPath();
				if (filePath != null)
				{
					if (getFileReturnListner != null) getFileReturnListner.getFieleReturn(filePath);
					if (getFolderReturnListner != null) getFolderReturnListner.getFolderReturn(filePath);
				}
			}
		}
	}

}
