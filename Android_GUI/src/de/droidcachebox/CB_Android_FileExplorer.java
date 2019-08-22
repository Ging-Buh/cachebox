package de.droidcachebox;

import CB_UI_Base.Events.PlatformConnector.IgetFileListener;
import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderListener;
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.app.Activity;

public final class CB_Android_FileExplorer implements IgetFileListener, IgetFolderListener {
    private Activity usedActivity;

    CB_Android_FileExplorer(Activity activity) {
        usedActivity = activity;
    }

    @Override
    public void getFile(String initialPath, String extension, String TitleText, String ButtonText, IgetFileReturnListener returnListener) {
        File mPath = FileFactory.createFile(initialPath);
        Android_FileExplorer fileDialog = new Android_FileExplorer(usedActivity, mPath, TitleText, ButtonText);
        fileDialog.setFileReturnListener(returnListener);
        fileDialog.showDialog();
    }

    @Override
    public void getFolder(String initialPath, String TitleText, String ButtonText, IgetFolderReturnListener returnListener) {
        File mPath = FileFactory.createFile(initialPath);
        Android_FileExplorer folderDialog = new Android_FileExplorer(usedActivity, mPath, TitleText, ButtonText);
        folderDialog.setSelectDirectoryOption();
        folderDialog.setFolderReturnListener(returnListener);
        folderDialog.showDialog();
    }

}
