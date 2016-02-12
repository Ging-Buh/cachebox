package de.CB.TestBase;

import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;

public abstract class CB_FileExplorer_Base {

	public abstract boolean getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener);

	public abstract boolean getfolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener);

}
