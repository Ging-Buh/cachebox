package de.droidcachebox;

import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

public class Android_FileExplorer_XPlore extends CB_FileExplorer_Base
{
	static Android_FileExplorer_XPlore that;

	public Android_FileExplorer_XPlore()
	{
		that = this;
	}

	@Override
	public boolean getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListner returnListner)
	{

		Intent in = new Intent(Intent.ACTION_VIEW);
		in.setData(Uri.parse("file://" + initialPath));
		in.setClassName("com.lonelycatgames.Xplore", "com.lonelycatgames.Xplore.Browser");

		// // Construct URI from file name.
		// File file = new File(initialPath);
		// intent.setData(Uri.fromFile(file));

		// Set fancy title and button (optional)
		// intent.putExtra("com.estrongs.intent.extra.TITLE", ButtonText);

		try
		{
			CB_Android_FileExplorer.UsedActivity.startActivityForResult(in,
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
		Intent in = new Intent(Intent.ACTION_VIEW);
		in.setData(Uri.parse("file://" + initialPath));
		in.setClassName("com.lonelycatgames.Xplore", "com.lonelycatgames.Xplore.Browser");

		try
		{
			CB_Android_FileExplorer.UsedActivity.startActivityForResult(in,
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
		if (that == null) that = new Android_FileExplorer_XPlore();
		return that;
	}

}
