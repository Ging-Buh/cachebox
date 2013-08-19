package de.cachebox_test;

import CB_UI.Events.platformConector.IgetFileReturnListner;
import CB_UI.Events.platformConector.IgetFolderReturnListner;

public abstract class CB_FileExplorer_Base
{

	public abstract boolean getFile(String initialPath, String extension, String TitleText, String ButtonText,
			IgetFileReturnListner returnListner);

	public abstract boolean getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner);

}
