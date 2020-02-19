package de.droidcachebox.views.forms;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import de.droidcachebox.PlatformUIBase.IReturnAbstractFile;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FilenameFilter;
import de.droidcachebox.utils.log.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class Android_FileExplorer {
    private static final String log = "Android_FileExplorer";
    private static final String PARENT_DIR = "..";
    private final Activity activity;
    private final String titleText;
    private final String buttonText;
    private final String firstSDCard;
    private final String DIRICON = ((char) new BigInteger("1F4C1", 16).intValue()) + " ";
    private String[] fileList;
    private AbstractFile currentPath;
    private IReturnAbstractFile fileReturn;
    private IReturnAbstractFile folderReturn;
    private boolean selectDirectoryOption;
    private String possibleExtensions;
    private String secondSDCard;

    public Android_FileExplorer(Activity activity, AbstractFile initialPath, String titleText, String buttonText) {
        this(activity, initialPath, titleText, buttonText, null);
    }

    public Android_FileExplorer(Activity activity, AbstractFile initialPath, String titleText, String buttonText, String possibleExtensions) {

        this.activity = activity;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            java.io.File[] dirs = activity.getExternalFilesDirs(null);

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
                String tmp;
                try {
                    tmp = dirs[1].getAbsolutePath();
                    int pos = tmp.indexOf("Android") - 1;
                    if (pos > 0)
                        secondSDCard = tmp.substring(0, pos);
                    else
                        secondSDCard = "";
                } catch (Exception e) {
                    secondSDCard = "";
                }
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

        setPossibleExtensions(possibleExtensions);
        try {
            if (!initialPath.exists()) {
                initialPath = FileFactory.createFile(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        } catch (Exception ex) {
            Log.err(log, ex);
            initialPath = FileFactory.createFile(firstSDCard);
        }
        currentPath = initialPath;
        this.titleText = titleText;
        if (buttonText == null || buttonText.length() == 0) {
            this.buttonText = Translation.get("ok");
        } else {
            this.buttonText = buttonText;
        }
    }

    public void setFileReturn(IReturnAbstractFile fileReturn) {
        this.fileReturn = fileReturn;
        selectDirectoryOption = false;
    }

    public void setFolderReturn(IReturnAbstractFile folderReturn) {
        this.folderReturn = folderReturn;
        selectDirectoryOption = true;
    }

    /**
     */
    private void createFileDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            loadFileList(currentPath);
            String shownPath = currentPath.getAbsolutePath();
            int l = shownPath.length();
            if (l > 30) {
                shownPath = "..." + shownPath.substring(l - 30);
            }
            if (titleText == null || titleText.length() == 0) {
                builder.setTitle(shownPath);
            } else {
                builder.setTitle(titleText + "\n" + shownPath);
            }

            if (selectDirectoryOption) {
                builder.setPositiveButton(buttonText, (dialog12, which) -> folderReturn.returns(currentPath));
            }

            builder.setItems(fileList, (dialog1, which) -> {
                AbstractFile chosenAbstractFile;
                try {
                    chosenAbstractFile = getChosenFile(fileList[which]);

                    if (chosenAbstractFile.isDirectory()) {
                        loadFileList(chosenAbstractFile);
                        dialog1.cancel();
                        dialog1.dismiss();
                        showDialog();
                    } else {
                        fileReturn.returns(chosenAbstractFile);
                    }

                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
            });

            builder.show();

        } catch (Exception e) {
            Log.err(log, e.getLocalizedMessage());
        }

    }

    /**
     * Show file dialog
     */
    public void showDialog() {
        try {
            activity.runOnUiThread(this::createFileDialog);
        } catch (Exception e) {
            Log.err(log, e.getLocalizedMessage());
        }
    }

    private void loadFileList(AbstractFile path) {
        ArrayList<String> r = new ArrayList<>();
        currentPath = path;

        String absolutePath;
        try {
            absolutePath = currentPath.getAbsolutePath();
            // add the root, if not yet selected
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

        FilenameFilter filter = (dir, filename) -> {
            AbstractFile f = FileFactory.createFile(dir, filename);
            if (!f.canRead())
                return false;
            if (selectDirectoryOption)
                return f.isDirectory();
            else {
                if (f.isDirectory()) return true;
                if (possibleExtensions.length() > 0) {
                    int lastIndex = filename.lastIndexOf('.');
                    // filename.substring(lastIndex) includes the dot
                    if (lastIndex > -1)
                        return possibleExtensions.contains(filename.substring(lastIndex).toLowerCase(Locale.US));
                    // has no extension
                    return false;
                }
                // no restriction by extensions
                return true;
            }
        };

        if (currentPath.exists()) {
            String[] tmpFileList = currentPath.list(filter);
            ArrayList<String> directories = new ArrayList<>();
            ArrayList<String> files = new ArrayList<>();
            if (tmpFileList != null) {
                for (String file : tmpFileList) {
                    if (FileFactory.createFile(currentPath, file).isDirectory()) {
                        directories.add(DIRICON + file);
                    } else {
                        files.add(file);
                    }
                }
                Collections.sort(directories, String.CASE_INSENSITIVE_ORDER);
                r.addAll(directories);
                Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
                r.addAll(files);
            }
        }
        fileList = r.toArray(new String[]{});
    }

    private AbstractFile getChosenFile(String fileChosen) {
        try {
            if (fileChosen.equals(PARENT_DIR))
                return currentPath.getParentFile();
            else if (fileChosen.startsWith("/"))
                return FileFactory.createFile(fileChosen);
            else if (fileChosen.startsWith(DIRICON))
                return FileFactory.createFile(currentPath, fileChosen.substring(DIRICON.length()));
            else
                return FileFactory.createFile(currentPath, fileChosen);
        } catch (Exception e) {
            Log.err(log, e.getLocalizedMessage());
        }
        return currentPath;
    }

    private void setPossibleExtensions(String possibleExtensions) {
        if (possibleExtensions == null) {
            this.possibleExtensions = "";
        } else {
            this.possibleExtensions = possibleExtensions.toLowerCase(Locale.US);
        }
    }
}
