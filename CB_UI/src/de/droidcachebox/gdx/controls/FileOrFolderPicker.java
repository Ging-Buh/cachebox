package de.droidcachebox.gdx.controls;

import static de.droidcachebox.GlobalCore.firstSDCard;
import static de.droidcachebox.GlobalCore.secondSDCard;

import android.os.Environment;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FilenameFilter;

/**
 * picks a file or folder depending on the used constructor
 * there is no longer a platform dependant implementation (for getFile or getFolder),
 * but desktop specifics are not yet implemented (like root selecting, ...), but it is usable
 * usage: create an instance and call show()
 * The GlobalCores firstSDCard and secondSDCard must be set (in global initializing) once before
 */
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
    private IReturnAbstractFile fileReturn;
    private IReturnAbstractFile folderReturn;
    private boolean selectFolder;
    private AbstractFile currentFolder;
    private ArrayList<String> containedFoldersAndFiles;

    public FileOrFolderPicker(String initialPath, String titleText, String selectFolderText, IReturnAbstractFile folderReturn) {
        // use this for folder selection (possibleExtensions = null)
        this(initialPath, null, titleText, selectFolderText, null);
        this.folderReturn = folderReturn;
    }

    public FileOrFolderPicker(String initialPath, String possibleExtensions, String titleText, String selectFolderText, IReturnAbstractFile fileReturn) {
        // for file selection possibleExtensions must not be null (use "" for no restriction, "*" / placeholders are not handled)
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
        // or selectFolder = fileReturn == null;
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
        AbstractFile initialFolder = FileFactory.createFile(initialPath);
        try {
            if (!initialFolder.exists()) {
                initialFolder = FileFactory.createFile(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
        } catch (Exception ex) {
            initialFolder = FileFactory.createFile(firstSDCard);
        }
        currentFolder = initialFolder;
        this.fileReturn = fileReturn;
        // PlatformUIBase.getDirectoryAccess(currentFolder.getAbsolutePath());
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
        String currentPath = currentFolder.getAbsolutePath();
        int l = currentPath.length();
        if (l > 30) {
            currentPath = "..." + currentPath.substring(l - 30);
        }
        if (titleText == null || titleText.length() == 0) {
            title.setText(currentPath);
        } else {
            title.setText(titleText + "\n" + currentPath);
        }
        if (currentPath.equals("/"))
            btnRoot.disable();
        else
            btnRoot.enable();

        if (currentPath.equals(firstSDCard)) {
            btnSD1.disable();
        } else {
            btnSD1.enable();
        }

        if (currentPath.equals(secondSDCard)) {
            btnSD2.disable();
        } else {
            btnSD2.enable();
        }

        if (currentPath.equals("/")) {
            btnParent.disable();
        } else {
            btnParent.enable();
        }

        filesView.notifyDataSetChanged();

    }

    private void loadFileList(AbstractFile path) {
        if (path == null) return;
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

    public interface IReturnAbstractFile {
        void returns(AbstractFile abstractFile);
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
                    finish();
                    fileReturn.returns(selected);
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
                leftBorder = getLeftWidth();
                rightBorder = getRightWidth();
                topBorder = getTopHeight();
                bottomBorder = getBottomHeight();
                String mFolderOrFileName = containedFoldersAndFiles.get(index);
                CB_Label fileOrFolderName;
                if (mFolderOrFileName.startsWith(DIRICON)) {
                    mFolderOrFileName = mFolderOrFileName.substring(DIRICON.length());
                    fileOrFolderName = new CB_Label(mFolderOrFileName);
                    float mIconSize = fileOrFolderName.getHeight();
                    Image icon = new Image(0, 0, mIconSize, mIconSize, "", true);
                    icon.setDrawable(new SpriteDrawable(Sprites.getSprite("file")));
                    addNext(icon, FIXED);
                } else {
                    fileOrFolderName = new CB_Label(mFolderOrFileName);
                }
                addLast(fileOrFolderName);
            }
        }
    }
}
