package de.droidcachebox.gdx.controls;

import static de.droidcachebox.GlobalCore.firstSDCard;
import static de.droidcachebox.GlobalCore.secondSDCard;
import static de.droidcachebox.GlobalCore.workPath;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.InputString;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.FilenameFilter;
import de.droidcachebox.utils.log.Log;

/**
 * picks a file or folder depending on the used constructor
 * there is no longer a platform dependant implementation (for getFile or getFolder),
 * but desktop specifics are not yet implemented (like root selecting, ...), but it is usable
 * usage: create an instance and call show()
 * The GlobalCores firstSDCard and secondSDCard must be set (in global initializing) once before
 */
public class FileOrFolderPicker extends ActivityBase {
    private static final String sClass = "FileOrFolderPicker";
    private static final String PARENT_DIR = "..";
    private final String DIRICON = ((char) new BigInteger("1F4C1", 16).intValue()) + " ";
    private final V_ListView filesView;
    private final FileAdapter fileAdapter;
    private final CB_Label title;
    private final String titleText;
    private final ImageButton btnCancel;
    private final CB_Button btnHome;
    private final CB_Button btnSD1;
    private final CB_Button btnSD2;
    private final CB_Button btnParent;
    private final CB_Button btnPlus;
    private final Spinner btnSort;
    private final String possibleExtensions;
    private final IReturnAbstractFile fileReturn;
    private final boolean selectFolder;
    private CB_Button btnSelectFolder;
    private IReturnAbstractFile folderReturn;
    private AbstractFile currentFolder;
    private ArrayList<String> containedFoldersAndFiles;
    private int sortType;

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
        btnHome = new CB_Button("Home");
        btnHome.setClickHandler((view, x, y, pointer, button) -> {
            String path = workPath;
            currentFolder = FileFactory.createFile(path);
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
        btnSort = new Spinner("ResortList", sortPossibilities(), index -> {
            sortType = index;
            onShow();
        });
        sortType = 0;
        btnSort.setSelection(sortType);
        selectFolder = possibleExtensions == null;
        if (selectFolder)
            this.possibleExtensions = ""; // want to show files on directory selection
        else {
            this.possibleExtensions = possibleExtensions;
        }
        // or selectFolder = fileReturn == null;
        if (selectFolder) {
            btnSelectFolder = new CB_Button(selectFolderText);
            btnSelectFolder.setClickHandler((view, x, y, pointer, button) -> {
                folderReturn.returns(currentFolder);
                finish();
                return true;
            });
        }

        btnPlus = new CB_Button("+");
        btnPlus.setClickHandler((view, x, y, pointer, button) -> {
            Menu btnPlusMenu = new Menu(currentFolder.getName(), "");
            btnPlusMenu.addMenuItem("newDirectory", "", null,
                    (view1, x1, y1, pointer1, button1) -> {
                        btnPlusMenu.close();
                        InputString is = new InputString("newDirectory", true) {
                            public void callBack(String inputString) {
                                AbstractFile res = FileFactory.createFile(currentFolder, "/" + inputString);
                                res.mkdir();
                                onShow();
                            }
                        };
                        is.show();
                        return false;
                    }
            );
            btnPlusMenu.addMenuItem("newFile", "", null,
                    (view1, x1, y1, pointer1, button1) -> {
                        btnPlusMenu.close();
                        InputString is = new InputString("newFile", true) {
                            public void callBack(String inputString) {
                                if (!inputString.contains(".")) {
                                    if (!possibleExtensions.substring(1).contains(".")) {
                                        inputString = inputString + possibleExtensions;
                                    }
                                }
                                FileIO.createFile(currentFolder.getAbsolutePath() + "/" + inputString);
                                onShow();
                            }
                        };
                        is.show();
                        return false;
                    }
            );
            btnPlusMenu.show();
            return true;
        });

        btnCancel = new ImageButton(Sprites.IconName.exit);
        btnCancel.setClickHandler((view, x, y, pointer, button) -> {
            finish();
            return true;
        });
        AbstractFile initialFolder = FileFactory.createFile(initialPath);
        if (!selectFolder) {
            if (!initialFolder.isDirectory())
                initialFolder = initialFolder.getParentFile();
        }
        try {
            if (!initialFolder.exists()) {
                initialFolder = FileFactory.createFile(workPath);
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
        addNext(btnHome);
        finaliseRow();
        initRow(BOTTOMUp);
        if (selectFolder) {
            addNext(btnSelectFolder);
        } else {
            addNext(btnSort);
        }
        addNext(btnPlus);
        addLast(btnCancel);
        btnCancel.setText(Translation.get("cancel"));
        // btnCancel.setText("");
        filesView.setHeight(getAvailableHeight());
        addLast(filesView);
        // maybe setAdapter once after first load of data. then use notifyDataSetChanged
        filesView.setAdapter(fileAdapter);
    }

    private void updateLayout() {

        String currentPath = currentFolder.getAbsolutePath();

        if (currentPath.endsWith(workPath))
            btnHome.disable();
        else
            btnHome.enable();

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

        int l = currentPath.length();
        if (l > 30) {
            currentPath = "..." + currentPath.substring(l - 30);
        }
        if (titleText == null || titleText.length() == 0) {
            title.setText(currentPath);
        } else {
            title.setText(titleText + "\n" + currentPath);
        }

        filesView.notifyDataSetChanged();

    }

    private SpinnerAdapter sortPossibilities() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                if (index == 0) return Translation.get("Name") + " ^";
                else if (index == 1) return Translation.get("Name") + " v";
                else if (index == 2) return Translation.get("date") + " ^";
                return Translation.get("date") + " v";
            }

            @Override
            public Drawable getIcon(int index) {
                return null;
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    private void loadFileList(AbstractFile path) {
        if (path == null) return;
        currentFolder = path;
        ArrayList<String> containedFolders = new ArrayList<>();

        FilenameFilter filter = (dir, filename) -> {
            AbstractFile f = FileFactory.createFile(dir, filename);
            if (!f.canRead())
                return false;
            /*
            if (selectFolder)
                return f.isDirectory();
            else {
            }

             */
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
        };

        if (currentFolder.exists()) {
            AbstractFile[] tmpFileList = currentFolder.listFiles(filter);
            ArrayList<AbstractFile> containedFiles = new ArrayList<>();
            if (tmpFileList != null) {
                for (AbstractFile file : tmpFileList) {
                    if (file.isDirectory()) {
                        containedFolders.add(DIRICON + file.getName());
                    } else {
                        containedFiles.add(file);
                    }
                }
                Collections.sort(containedFolders, String.CASE_INSENSITIVE_ORDER);
                Collections.sort(containedFiles, (o1, o2) -> {
                    if (sortType == 0) {
                        return o1.getName().toLowerCase(Locale.ROOT).compareTo(o2.getName().toLowerCase(Locale.ROOT));
                    } else if (sortType == 1) {
                        return o2.getName().toLowerCase(Locale.ROOT).compareTo(o1.getName().toLowerCase(Locale.ROOT));
                    } else if (sortType == 2) {
                        return Long.compare(o1.lastModified(), o2.lastModified());
                    } else
                        return Long.compare(o2.lastModified(), o1.lastModified());
                });
                containedFoldersAndFiles = new ArrayList<>();
                containedFoldersAndFiles.addAll(containedFolders);
                for (AbstractFile file : containedFiles) {
                    containedFoldersAndFiles.add(file.getName());
                }
            } else {
                containedFoldersAndFiles = new ArrayList<>();
            }
        }
    }

    public interface IReturnAbstractFile {
        void returns(AbstractFile abstractFile);
    }

    private class FileAdapter implements Adapter {
        private final float itemWidth;
        private float itemHeight;

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
                    if (fileReturn != null) {
                        finish();
                        fileReturn.returns(selected);
                    }
                }
                return true;
            });
            v.setLongClickHandler((view, x, y, pointer, button) -> {
                FileItem v1 = (FileItem) view;
                String fn = containedFoldersAndFiles.get(v1.getIndex());
                final String fileName;
                if (fn.startsWith(DIRICON)) {
                    fileName = fn.substring(DIRICON.length());
                    Menu fileModifications = new Menu(fileName, "");
                    fileModifications.addMenuItem("delete", null, () -> {
                        AbstractFile selected = FileFactory.createFile(currentFolder, fileName);
                        FileIO.deleteDirectory(selected);
                        onShow();
                    });
                    fileModifications.addMenuItem("newDirectory", null, () -> (new InputString("newDirectory", true) {
                        public void callBack(String inputString) {
                            AbstractFile selected = FileFactory.createFile(currentFolder, fileName + "/" + inputString);
                            selected.mkdir();
                            currentFolder = selected;
                            onShow();
                        }
                    }).show());
                    fileModifications.addMenuItem("newFile", null, () -> (new InputString("newFile", true) {
                        public void callBack(String inputString) {
                            if (!inputString.contains(".")) {
                                if (!possibleExtensions.substring(1).contains(".")) {
                                    inputString = inputString + possibleExtensions;
                                }
                            }
                            FileIO.createFile(currentFolder.getAbsolutePath() + "/" + fileName + "/" + inputString);
                            currentFolder = FileFactory.createFile(currentFolder.getAbsolutePath() + "/" + fileName);
                            onShow();
                        }
                    }).show());
                    fileModifications.show();
                } else {
                    fileName = fn;
                    Menu fileModifications = new Menu(fileName, "");
                    fileModifications.addMenuItem("delete", null, () -> {
                        AbstractFile selected = FileFactory.createFile(currentFolder, fileName);
                        try {
                            selected.delete();
                            onShow();
                        } catch (IOException ex) {
                            Log.err(sClass, ex.getMessage() + " " + selected);
                        }
                    });
                    fileModifications.show();
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
