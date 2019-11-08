package de.droidcachebox.gdx;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.droidcachebox.AbstractGlobal;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.utils.log.Log;

public class CB_Skin {
    private static final String sKlasse = "CB_Skin";

    private static CB_Skin mINSTANCE;

    private Skin night_skin;
    private Skin day_skin;
    private Skin default_night_skin;
    private Skin default_day_skin;

    private FileHandle SkinFolder;
    private FileHandle DefaultSkinFolder;

    private boolean NightMode = false;
    private int SizeBiggest = 27;
    private int SizeBig = 18;
    private int SizeNormal = 15;
    private int SizeNormalbubble = 14;
    private int SizeSmall = 12;
    private int SizeSmallBubble = 10;

    private CB_Skin() {
        super();
        init();
    }

    public static CB_Skin getInstance() {
        if (mINSTANCE == null) {
            mINSTANCE = new CB_Skin();
        }
        return mINSTANCE;
    }

    private void init() {

        if (CB_UI_Base_Settings.SkinFolder.getValue().equals("default")) {
            SkinFolder = AbstractGlobal.getInternalFileHandle("skins/default");
        } else if (CB_UI_Base_Settings.SkinFolder.getValue().equals("small")) {
            SkinFolder = AbstractGlobal.getInternalFileHandle("skins/small");
        } else {
            SkinFolder = Gdx.files.absolute(CB_UI_Base_Settings.SkinFolder.getValue());
            if (!SkinFolder.isDirectory()) {
                SkinFolder = AbstractGlobal.getInternalFileHandle("skins/default");
            }
        }

        DefaultSkinFolder = AbstractGlobal.getInternalFileHandle("skins/default");

        SizeBiggest = CB_UI_Base_Settings.FONT_SIZE_COMPASS_DISTANCE.getValue();
        SizeBig = CB_UI_Base_Settings.FONT_SIZE_BIG.getValue();
        SizeNormal = CB_UI_Base_Settings.FONT_SIZE_NORMAL.getValue();
        SizeNormalbubble = CB_UI_Base_Settings.FONT_SIZE_NORMAL_BUBBLE.getValue();
        SizeSmall = CB_UI_Base_Settings.FONT_SIZE_SMALL.getValue();
        SizeSmallBubble = CB_UI_Base_Settings.FONT_SIZE_SMALL_BUBBLE.getValue();

        NightMode = CB_UI_Base_Settings.nightMode.getValue();

        FileHandle default_day_skinPath = AbstractGlobal.getInternalFileHandle("skins/default/day/skin.json");
        default_day_skin = new Skin(default_day_skinPath);
        FileHandle default_night_skinPath = AbstractGlobal.getInternalFileHandle("skins/default/night/skin.json");
        default_night_skin = new Skin(default_night_skinPath);
        try {
            String day_skinPath = SkinFolder + "/day/skin.json";
            if (SkinFolder.type() == Files.FileType.Absolute) {
                day_skin = new Skin(Gdx.files.absolute(day_skinPath));
            } else {
                day_skin = new Skin(Gdx.files.internal(day_skinPath));
            }
        } catch (Exception ex) {
            Log.err(sKlasse, "Load Custom Skin", ex);
        }
        try {
            String night_skinPath = SkinFolder + "/night/skin.json";
            if (SkinFolder.type() == Files.FileType.Absolute) {
                night_skin = new Skin(Gdx.files.absolute(night_skinPath));
            } else {
                night_skin = new Skin(Gdx.files.internal(night_skinPath));
            }
        } catch (Exception ex) {
            Log.err(sKlasse, "Load Custom Night Skin", ex);
        }

    }

    public Skin getDefaultDaySkin() {
        return default_day_skin;
    }

    public Skin getDefaultNightSkin() {
        return default_night_skin;
    }

    public Skin getDaySkin() {
        return day_skin;
    }

    public Skin getNightSkin() {
        return night_skin;
    }


    public HSV_Color getThemedColor(String Name) {
        if (CB_UI_Base_Settings.nightMode.getValue()) {
            return new HSV_Color(night_skin.getColor(Name));
        } else {
            return new HSV_Color(day_skin.getColor(Name));
        }
    }

    public boolean getNightMode() {
        return NightMode;
    }

    public FileHandle getSkinFolder() {
        return SkinFolder;
    }

    public FileHandle getDefaultSkinFolder() {
        return DefaultSkinFolder;
    }

    public int getSizeBiggest() {
        return  SizeBiggest;
    }

    public int getSizeBig() {
        return  SizeBig;
    }

    public int getSizeNormal() {
        return  SizeNormal;
    }

    public int getSizeNormalBubble() {
        return  SizeNormalbubble;
    }

    public int getSizeSmall() {
        return  SizeSmall;
    }

    public int getSizeSmallBubble() {
        return  SizeSmallBubble;
    }

    public void setNightMode(boolean value) {
        NightMode = value;
    }
}
