package de.CB.TestBase;

import java.io.File;

import org.openintents.intents.FileManagerIntents;

import de.CB.TestBase.Global;
import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

public class Android_FileExplorer_OI extends CB_FileExplorer_Base
{
	static Android_FileExplorer_OI that;

	public Android_FileExplorer_OI()
	{
		that = this;
	}

	@Override
	public boolean getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListner returnListner)
	{
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);

		// Construct URI from file name.
		File file = new File(initialPath);
		intent.setData(Uri.fromFile(file));

		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, TitleText);
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, ButtonText);

		try
		{
			CB_Android_FileExplorer.UsedActivity.startActivityForResult(intent,
					Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR);
			return true;
		}
		catch (ActivityNotFoundException e)
		{
			return false;
		}

	}

	@Override
	public boolean getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner)
	{
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

		// Construct URI from file name.
		File file = new File(initialPath);
		intent.setData(Uri.fromFile(file));

		// Set fancy title and button (optional)
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, TitleText);
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, ButtonText);

		try
		{
			CB_Android_FileExplorer.UsedActivity.startActivityForResult(intent,
					Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR);
			return true;
		}
		catch (ActivityNotFoundException e)
		{
			return false;
		}
	}

	public static CB_FileExplorer_Base getMyInstanz()
	{
		if (that == null) that = new Android_FileExplorer_OI();
		return that;
	}

}
