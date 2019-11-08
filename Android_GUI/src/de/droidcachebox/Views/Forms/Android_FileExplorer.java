package de.droidcachebox.Views.Forms;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import de.droidcachebox.PlatformUIBase.IgetFileReturnListener;
import de.droidcachebox.PlatformUIBase.IgetFolderReturnListener;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
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
    private final String TitleText;
    private final String ButtonText;
    private final String firstSDCard;
    private final String DIRICON = ((char) new BigInteger("1F4C1", 16).intValue()) + " ";
    private String[] fileList;
    private File currentPath;
    private IgetFileReturnListener fileReturnListener;
    private IgetFolderReturnListener folderReturnListener;
    private boolean selectDirectoryOption;
    private String fileEndsWith;
    private String secondSDCard;

    public Android_FileExplorer(Activity activity, File initialPath, String TitleText, String ButtonText) {
        this(activity, initialPath, TitleText, ButtonText, null);
    }

    private Android_FileExplorer(Activity activity, File initialPath, String TitleText, String ButtonText, String fileEndsWith) {

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

        setFileEndsWith(fileEndsWith);
        try {
            if (!initialPath.exists()) {
                initialPath = FileFactory.createFile(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        } catch (Exception e) {
            Log.err(log, e.getLocalizedMessage());
            initialPath = FileFactory.createFile(firstSDCard);
        }
        currentPath = initialPath;
        this.TitleText = TitleText;
        if (ButtonText == null || ButtonText.length() == 0) {
            this.ButtonText = Translation.get("ok");
        } else {
            this.ButtonText = ButtonText;
        }
    }

    public void setFileReturnListener(IgetFileReturnListener returnListener) {
        fileReturnListener = returnListener;
    }

    public void setFolderReturnListener(IgetFolderReturnListener returnListener) {
        folderReturnListener = returnListener;
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
            if (TitleText == null || TitleText.length() == 0) {
                builder.setTitle(shownPath);
            } else {
                builder.setTitle(TitleText + "\n" + shownPath);
            }

            if (selectDirectoryOption) {
                builder.setPositiveButton(ButtonText, (dialog12, which) -> folderReturnListener.returnFolder(currentPath.getAbsolutePath()));
            }

            builder.setItems(fileList, (dialog1, which) -> {
                File chosenFile;
                try {
                    chosenFile = getChosenFile(fileList[which]);

                    if (chosenFile.isDirectory()) {
                        loadFileList(chosenFile);
                        dialog1.cancel();
                        dialog1.dismiss();
                        showDialog();
                    } else {
                        fileReturnListener.returnFile(chosenFile.getAbsolutePath());
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

    public void setSelectDirectoryOption() {
        this.selectDirectoryOption = true;
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

    private void loadFileList(File path) {
        ArrayList<String> r = new ArrayList<>();
        currentPath = path;

        String absolutePath;
        try {
            absolutePath = currentPath.getAbsolutePath();
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
            File sel = FileFactory.createFile(dir, filename);
            if (!sel.canRead())
                return false;
            if (selectDirectoryOption)
                return sel.isDirectory();
            else {
                boolean endsWith = (fileEndsWith == null) || filename.toLowerCase(Locale.US).endsWith(fileEndsWith);
                return endsWith || sel.isDirectory();
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

    private File getChosenFile(String fileChosen) {
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

    private void setFileEndsWith(String fileEndsWith) {
        if (fileEndsWith == null) {
            this.fileEndsWith = null;
        } else {
            this.fileEndsWith = fileEndsWith.toLowerCase();
        }
    }
}
