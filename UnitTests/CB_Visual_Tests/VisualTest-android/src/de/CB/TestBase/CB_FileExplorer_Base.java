package de.CB.TestBase;

import CB_UI_Base.Events.platformConector.IgetFileReturnListner;
import CB_UI_Base.Events.platformConector.IgetFolderReturnListner;

public abstract class CB_FileExplorer_Base
{

	public abstract boolean getFile(String initialPath, String extension, String TitleText, String ButtonText,
			IgetFileReturnListner returnListner);

	public abstract boolean getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListner returnListner);

}
