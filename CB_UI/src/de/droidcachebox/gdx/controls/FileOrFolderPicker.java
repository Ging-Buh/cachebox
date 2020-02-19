package de.droidcachebox.gdx.controls;

import android.os.Environment;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FilenameFilter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static de.droidcachebox.GlobalCore.firstSDCard;
import static de.droidcachebox.GlobalCore.secondSDCard;

public class FileOrFolderPicker extends ActivityBase {
    // private static final String log = "FileOrFolderPicker";
    private static final String PARENT_DIR = "..";
    private final String DIRICON = ((char) new BigInteger("1F4C1", 16).intValue()) + " ";
    private V_ListView filesView;
    private FileAdapter fileAdapter;
    private CB_Label title;
    private String titleText;
    private CB_Button btnSelectFolder, btnCancel, btnRoot, btnSD1, btnSD2, btnParent;
    private String possibleExtensions;
    private PlatformUIBase.IReturnAbstractFile fileReturn;
    private PlatformUIBase.IReturnAbstractFile folderReturn;
    private boolean selectFolder;
    private AbstractFile currentFolder;
    private ArrayList<String> containedFoldersAndFiles;

    public FileOrFolderPicker(AbstractFile initialFolder, String titleText, String selectFolderText) {
        // use this for folder selection (possibleExtensions = null)
        this(initialFolder, titleText, selectFolderText, null);
    }

    public FileOrFolderPicker(AbstractFile initialFolder, String titleText, String selectFolderText, String possibleExtensions) {
        // for file selection possibleExtensions must not be null (use "" for no restriction)
        super("FileOrFolderPicker");
        this.titleText = titleText;
        filesView = new V_ListView(this, "files");
        fileAdapter = new FileAdapter(innerWidth);
        title = new CB_Label(titleText);
        btnRoot = new CB_Button("/");
        btnRoot.setClickHandler((view, x, y, pointer, button) -> {
            currentFolder = FileFactory.createFile("/");
            onShow();
            return true;
        });
        btnSD1 = new CB_Button("sd1");
        btnSD1.setClickHandler((view, x, y, pointer, button) -> {
            currentFolder = FileFactory.createFile(firstSDCard);
            onShow();
            return true;
        });
        btnSD2 = new CB_Button("sd2");
        btnSD2.setClickHandler((view, x, y, pointer, button) -> {
            currentFolder = FileFactory.createFile(secondSDCard);
            onShow();
            return true;
        });
        btnParent = new CB_Button(PARENT_DIR);
        btnParent.setClickHandler((view, x, y, pointer, button) -> {
            currentFolder = currentFolder.getParentFile();
            onShow();
            return true;
        });
        this.possibleExtensions = possibleExtensions;
        selectFolder = possibleExtensions == null;
        if (selectFolder) {
            btnSelectFolder = new CB_Button(selectFolderText);
            btnSelectFolder.setClickHandler((view, x, y, pointer, button) -> {
                folderReturn.returns(currentFolder);
                finish();
                return true;
            });
        }
        btnCancel = new CB_Button(Translation.get("cancel"));
        btnCancel.setClickHandler((view, x, y, pointer, button) -> {
            finish();
            return true;
        });
        try {
            if (!initialFolder.exists()) {
                initialFolder = FileFactory.createFile(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        } catch (Exception ex) {
            initialFolder = FileFactory.createFile(firstSDCard);
        }
        currentFolder = initialFolder;
        layout();
    }

    @Override
    public void onShow() {
        loadFileList(currentFolder);
        updateLayout();
    }

    private void layout() {
        addLast(title);
        addNext(btnParent);
        if (firstSDCard.length() > 0) addNext(btnSD1);
        if (secondSDCard.length() > 0) addNext(btnSD2);
        addNext(btnRoot);
        finaliseRow();
        initRow(BOTTOMUP);
        if (selectFolder) addNext(btnSelectFolder);
        addLast(btnCancel);
        filesView.setHeight(getAvailableHeight());
        addLast(filesView);
        // maybe setAdapter once after first load of data. then use notifyDataSetChanged
        filesView.setAdapter(fileAdapter);
    }

    private void updateLayout() {
        String shownPath = currentFolder.getAbsolutePath();
        int l = shownPath.length();
        if (l > 30) {
            shownPath = "..." + shownPath.substring(l - 30);
        }
        if (titleText == null || titleText.length() == 0) {
            title.setText(shownPath);
        } else {
            title.setText(titleText + "\n" + shownPath);
        }


        String absolutePath;
        try {
            absolutePath = currentFolder.getAbsolutePath();
            if (absolutePath.equals("/"))
                btnRoot.disable();
            else
                btnRoot.enable();

            if (absolutePath.equals(firstSDCard)) {
                btnSD1.disable();
            } else {
                btnSD1.enable();
            }

            if (absolutePath.equals(secondSDCard)) {
                btnSD2.disable();
            } else {
                btnSD2.enable();
            }

            if (absolutePath.equals("/")) {
                btnParent.disable();
            } else {
                btnParent.enable();
            }
        } catch (Exception e) {
            if (firstSDCard.length() > 0)
                btnSD1.enable();
            if (secondSDCard.length() > 0)
                btnSD2.enable();
        }

        filesView.notifyDataSetChanged();

    }

    private void loadFileList(AbstractFile path) {
        currentFolder = path;
        containedFoldersAndFiles = new ArrayList<>();

        FilenameFilter filter = (dir, filename) -> {
            AbstractFile f = FileFactory.createFile(dir, filename);
            if (!f.canRead())
                return false;
            if (selectFolder)
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

        if (currentFolder.exists()) {
            String[] tmpFileList = currentFolder.list(filter);
            ArrayList<String> containedFiles = new ArrayList<>();
            if (tmpFileList != null) {
                for (String fileName : tmpFileList) {
                    if (FileFactory.createFile(currentFolder, fileName).isDirectory()) {
                        containedFoldersAndFiles.add(DIRICON + fileName);
                    } else {
                        containedFiles.add(fileName);
                    }
                }
                Collections.sort(containedFoldersAndFiles, String.CASE_INSENSITIVE_ORDER);
                Collections.sort(containedFiles, String.CASE_INSENSITIVE_ORDER);
                containedFoldersAndFiles.addAll(containedFiles);
            }
        }
    }

    public FileOrFolderPicker setFileReturn(PlatformUIBase.IReturnAbstractFile fileReturn) {
        this.fileReturn = fileReturn;
        selectFolder = false;
        return this;
    }

    public FileOrFolderPicker setFolderReturn(PlatformUIBase.IReturnAbstractFile folderReturn) {
        this.folderReturn = folderReturn;
        selectFolder = true;
        return this;
    }

    private class FileAdapter implements Adapter {
        private float itemWidth, itemHeight;

        public FileAdapter(float itemWidth) {
            this.itemWidth = itemWidth;
            containedFoldersAndFiles = new ArrayList<>();
            containedFoldersAndFiles.add("dummy");
            FileItem v = new FileItem(new CB_RectF(0, 0, itemWidth, itemHeight), 0, "dummy");
            itemHeight = -1 * v.getAvailableHeight();
        }

        @Override
        public int getCount() {
            return containedFoldersAndFiles.size();
        }

        @Override
        public ListViewItemBase getView(int position) {
            // cause a filelist can be very long, the items view is not created in advance (we don't/won't have different item heights)
            FileItem v = new FileItem(new CB_RectF(0, 0, itemWidth, itemHeight), position, "FileItem" + position);
            v.setClickHandler((view, x, y, pointer, button) -> {
                FileItem v1 = (FileItem) view;
                String fileName = containedFoldersAndFiles.get(v1.getIndex());
                if (fileName.startsWith(DIRICON)) fileName = fileName.substring(DIRICON.length());
                AbstractFile selected = FileFactory.createFile(currentFolder, fileName);
                if (selected.isDirectory()) {
                    currentFolder = selected;
                    onShow();
                } else {
                    fileReturn.returns(selected);
                    finish();
                }
                return true;
            });
            return v;
        }

        @Override
        public float getItemSize(int position) {
            // return the height of the item;
            return itemHeight;
        }

        private class FileItem extends ListViewItemBackground {
            /**
             * Constructor
             *
             * @param rec   size
             * @param index Index in der List
             * @param name  name
             */
            public FileItem(CB_RectF rec, int index, String name) {
                super(rec, index, name);
                CB_Label fileOrFolderName = new CB_Label(containedFoldersAndFiles.get(index));
                addLast(fileOrFolderName);
            }
        }
    }
}
