/*
 * Copyright (C) 2011-2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_UI_Base.GL_UI;

import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Log;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

/**
 * Enthält die geladenen Sprites und das Handling für Laden und Entladen.
 *
 * @author Longri
 */
public class Sprites {

    private static final String log = "Sprites";
    public static ArrayList<Sprite> Arrows = null;
    public static ArrayList<Sprite> MapStars = null;
    public static SpriteList Stars = null;
    public static ArrayList<Sprite> Bubble = null;
    public static ArrayList<Drawable> Compass = null;
    public static ArrayList<Sprite> ToggleBtn = null;
    public static ArrayList<Sprite> LiveBtn = null;
    public static ArrayList<Sprite> ZoomBtn = null;
    public static Sprite ZoomValueBack = null;
    public static ArrayList<Sprite> ChkIcons = null;
    public static ArrayList<Sprite> Dialog = null;
    public static SpriteList SizesIcons = null;
    public static Drawable ListBack = null;
    public static Drawable ButtonBack = null;
    public static Drawable AboutBack = null;
    public static Drawable Slider = null;
    public static Drawable SliderPushed = null;
    public static Sprite Progress = null;
    public static Sprite ambilwarna_hue = null;
    public static Sprite ambilwarna_cursor = null;
    public static Sprite ambilwarna_target = null;
    public static ButtonSprites CacheList;
    public static ButtonSprites CacheListFilter;
    public static ButtonSprites Cache;
    public static ButtonSprites Nav;
    public static ButtonSprites Tool;
    public static ButtonSprites Misc;
    public static ButtonSprites QuickButton;
    public static ArrayList<Sprite> LogIcons;
    public static Drawable activityBackground;
    public static Drawable activityBorderMask;
    public static Drawable InfoBack;
    public static Drawable ProgressBack;
    public static Drawable ProgressFill;
    public static Drawable ProgressDisabled;
    public static Drawable btn;
    public static Drawable btnPressed;
    public static Drawable btnDisabled;
    public static Drawable shaddowRec;
    public static Drawable chkOn;
    public static Drawable chkOff;
    public static Drawable chkOnDisabled;
    public static Drawable chkOffDisabled;
    public static Drawable radioOn;
    public static Drawable radioBack;
    public static Drawable selection;
    public static Drawable selection_set;
    public static Drawable selection_left;
    public static Drawable selection_right;
    public static Drawable textFieldCursor;
    public static Drawable textFieldBackground;
    public static Drawable textFieldBackgroundFocus;
    public static Drawable logo;
    public static Drawable copy;
    public static Drawable paste;
    public static Drawable cut;
    public static Drawable bar;
    public static Drawable barSmall;
    public static Drawable bar_0;
    public static Drawable barSmall_0;
    public static Drawable[] MapScale;
    public static Sprite[] Accuracy;
    public static int patch;
    public static FileHandle FileHandleDefaultAtlas;
    public static FileHandle FileHandleCustomAtlas;
    public static FileHandle FileHandleDefaultNightAtlas;
    public static FileHandle FileHandleCustomNightAtlas;
    public static boolean loaded = false;
    private static TextureAtlas atlasDefault;
    private static TextureAtlas atlasDefaultNight;
    private static TextureAtlas atlasCustom;
    private static TextureAtlas atlasCustomtNight;

    public static Sprite getMapOverlay(IconName i) {
        switch (i) {
            case shaddowrectselected:
                return getSprite("shaddowrect-selected");
            case live:
                Sprite live = getSprite(IconName.shaddowrect);
                live.setColor(CB_UI_Base_Settings.LiveMapBackgroundColor.getValue());
                return live;
            case liveSelected:
                Sprite liveSelected = getSprite("shaddowrect-selected");
                liveSelected.setColor(CB_UI_Base_Settings.LiveMapBackgroundColor.getValue());
                return liveSelected;
            default:
                return getSprite(i);
        }
    }

    protected static void setPath() {

        if (Gdx.gl != null) {
            Gdx.gl.glFlush();
            Gdx.gl.glFinish();
        }

        String TexturName = CB_UI_Base_Settings.useMipMap.getValue() ? "UI_IconPack_MipMap.spp.atlas" : "UI_IconPack.spp.atlas";

        FileHandleCustomAtlas = null;
        FileHandleCustomNightAtlas = null;

        if (CB_Skin.getInstance().getSkinFolder().type() == FileType.Absolute) {
            FileHandleCustomAtlas = Gdx.files.absolute(CB_Skin.getInstance().getSkinFolder() + "/day/" + TexturName);
            FileHandleCustomNightAtlas = Gdx.files.absolute(CB_Skin.getInstance().getSkinFolder() + "/night/" + TexturName);
        } else {
            FileHandleCustomAtlas = Gdx.files.internal(CB_Skin.getInstance().getSkinFolder() + "/day/" + TexturName);
            FileHandleCustomNightAtlas = Gdx.files.internal(CB_Skin.getInstance().getSkinFolder() + "/night/" + TexturName);
        }

        FileHandleDefaultAtlas = Gdx.files.internal(CB_Skin.getInstance().getDefaultSkinFolder() + "/day/" + TexturName);
        FileHandleDefaultNightAtlas = Gdx.files.internal(CB_Skin.getInstance().getDefaultSkinFolder() + "/night/" + TexturName);

        if (atlasDefault != null) {
            atlasDefault.dispose();
            atlasDefault = null;
        }

        if (atlasDefaultNight != null) {
            atlasDefaultNight.dispose();
            atlasDefaultNight = null;
        }

        if (atlasCustom != null) {
            atlasCustom.dispose();
            atlasCustom = null;
        }

        if (atlasCustomtNight != null) {
            atlasCustomtNight.dispose();
            atlasCustomtNight = null;
        }

        atlasDefault = new TextureAtlas(FileHandleDefaultAtlas);
        atlasDefaultNight = new TextureAtlas(FileHandleDefaultNightAtlas);

        if (!FileHandleDefaultAtlas.equals(FileHandleCustomAtlas)) {
            try {
                atlasCustom = new TextureAtlas(FileHandleCustomAtlas);
            } catch (Exception e) {
                Log.err(log, "Load Custom Atlas", e);
            }
            try {
                atlasCustomtNight = new TextureAtlas(FileHandleCustomNightAtlas);
            } catch (Exception e) {
                Log.err(log, "Load Custom Night Atlas", e);
            }
        }
    }

    public static Sprite getSprite(IconName iconName) {
        return getSprite(iconName.name(), 1.0f);
    }

    public static Sprite getSprite(String name) {
        return getSprite(name, 1.0f);
    }

    public static Sprite getSprite(String name, float scale) {
        /*
		if (!loaded) {
			loadSprites(false);
		}
		*/
        Sprite tmp = null;
        if (CB_UI_Base_Settings.nightMode.getValue()) {
            tmp = createSprite(atlasCustomtNight, name);
            if (tmp == null) {
                tmp = createSprite(atlasCustom, name);
                if (tmp != null)
                    tmp = setNightColorMatrixSprite(name, atlasCustom);
            }

            if (tmp == null) {
                tmp = createSprite(atlasDefaultNight, name);

                if (tmp == null) {
                    tmp = setNightColorMatrixSprite(name, atlasDefault);
                }

            }

        } else {
            tmp = createSprite(atlasCustom, name);
        }

        if (tmp == null) {
            tmp = createSprite(atlasDefault, name);
        }

        if (tmp != null)
            tmp.setScale(scale);

        if (tmp == null) {
            Log.info(log, "missing icon " + name);
            tmp = createSprite(atlasDefault, "bigUndefined"); // damit kein null Sprite zurückgegeben wird falls ich was übersehen habe
        } else {
            if (name.endsWith("Solved")) {
                tmp.setColor(CB_UI_Base_Settings.SolvedMysteryColor.getValue());
            }
        }

        return tmp;
    }

    protected static NinePatch getThemedPatch(String name) {
        NinePatch tmp = null;
        if (CB_UI_Base_Settings.nightMode.getValue()) {
            tmp = createPatch(atlasCustomtNight, name);
            if (tmp == null) {
                tmp = createPatch(atlasCustom, name);
                if (tmp != null)
                    tmp = setNightColorMatrixPatch(name, atlasCustom);
            }

            if (tmp == null) {
                tmp = createPatch(atlasDefaultNight, name);

                if (tmp == null) {
                    tmp = setNightColorMatrixPatch(name, atlasDefault);
                }

            }

        } else {
            tmp = createPatch(atlasCustom, name);
        }

        if (tmp == null) {
            tmp = createPatch(atlasDefault, name);
        }

        return tmp;
    }

    protected static Sprite setNightColorMatrixSprite(String name, TextureAtlas atlas) {
        Sprite tmp = null;
        tmp = createSprite(atlas, name);
        if (tmp == null)
            return null;

        Color colorOverlay = new Color(0.5f, 0.4f, 0.4f, 1f);
        tmp.setColor(colorOverlay);

        return tmp;
    }

    protected static NinePatch setNightColorMatrixPatch(String name, TextureAtlas atlas) {
        NinePatch tmp = null;
        tmp = createPatch(atlas, name);
        if (tmp == null)
            return null;

        Color colorOverlay = new Color(0.5f, 0.4f, 0.4f, 1f);
        tmp.setColor(colorOverlay);

        return tmp;
    }

    protected static Sprite createSprite(TextureAtlas atlas, String name) {
        Sprite tmp = null;
        if (atlas != null) {
            tmp = atlas.createSprite(name);
        }
        return tmp;
    }

    protected static NinePatch createPatch(TextureAtlas atlas, String name) {
        NinePatch tmp = null;
        if (atlas != null) {
            tmp = atlas.createPatch(name);
        }
        return tmp;
    }

    /**
     * Load the Sprites from recourse
     */
    public static void loadSprites(boolean reload) {

        if (!reload)
            setPath();

        if (LogIcons == null)
            LogIcons = new ArrayList<Sprite>();
        synchronized (LogIcons) {
            LogIcons.clear();
            LogIcons.add(getSprite("log0icon"));
            LogIcons.add(getSprite("log1icon"));
            LogIcons.add(getSprite("log2icon"));
            LogIcons.add(getSprite("log3icon"));
            LogIcons.add(getSprite("log4icon"));
            LogIcons.add(getSprite("log5icon"));
            LogIcons.add(getSprite("log6icon"));
            LogIcons.add(getSprite("log7icon"));
            LogIcons.add(getSprite("log8icon"));
            LogIcons.add(getSprite("log9icon"));
            LogIcons.add(getSprite("log10icon"));
            LogIcons.add(getSprite("log11icon"));
            LogIcons.add(getSprite("log12icon"));
            LogIcons.add(getSprite("log13icon"));
            LogIcons.add(getSprite("log14icon"));
            LogIcons.add(getSprite("log15icon"));
            LogIcons.add(getSprite("log16icon"));
            LogIcons.add(getSprite(IconName.TBPICKED.name()));
            LogIcons.add(getSprite(IconName.TBDROP.name()));
            LogIcons.add(getSprite(IconName.TBGRAB.name()));
            LogIcons.add(getSprite(IconName.TBDISCOVER.name()));
            LogIcons.add(getSprite(IconName.TBVISIT.name()));
            LogIcons.add(getSprite(IconName.TBNOTE.name()));
        }

        if (Compass == null)
            Compass = new ArrayList<Drawable>();
        synchronized (Compass) {
            Compass.clear();
            Compass.add(new SpriteDrawable(getSprite("compass-frame")));
            Compass.add(new SpriteDrawable(getSprite("compass-scale")));
            Compass.add(new SpriteDrawable(getSprite("compass-frame-small")));
            Compass.add(new SpriteDrawable(getSprite("compass-scale-small")));
            Compass.add(new SpriteDrawable(getSprite("compass_arrow")));
            Compass.add(new SpriteDrawable(getSprite("sonne")));
            Compass.add(new SpriteDrawable(getSprite("mond")));

        }

        if (Arrows == null)
            Arrows = new ArrayList<Sprite>();
        synchronized (Arrows) {

            float scale = UI_Size_Base.that.getScale();

            Arrows.clear();
            Arrows.add(getSprite("arrow-Compass")); // 0
            Arrows.add(getSprite("arrow-Compass-Trans")); // 1
            Arrows.add(getSprite("arrow-GPS")); // 2
            Arrows.add(getSprite("arrow-GPS-Trans")); // 3
            Arrows.add(getSprite("target-arrow")); // 4
            Arrows.add(getSprite("track-line", scale)); // 5
            Arrows.add(getSprite("arrow-down")); // 6
            Arrows.add(getSprite("arrow-up")); // 7
            Arrows.add(getSprite("arrow-left")); // 8
            Arrows.add(getSprite("arrow-right")); // 9
            Arrows.add(getSprite("track-point", scale)); // 10
            Arrows.add(getSprite("ambilwarna-arrow-right")); // 11
            Arrows.add(getSprite("ambilwarna-arrow-down")); // 12
            Arrows.add(getSprite("draw-line", scale)); // 13
            Arrows.add(getSprite("draw-point", scale)); // 14
            Arrows.add(getSprite("arrow-Compass-car")); // 15

        }

        if (MapStars == null)
            MapStars = new ArrayList<Sprite>();
        synchronized (MapStars) {
            MapStars.clear();
            MapStars.add(getSprite("stars0small"));
            MapStars.add(getSprite("stars0-5small"));
            MapStars.add(getSprite("stars1small"));
            MapStars.add(getSprite("stars1-5small"));
            MapStars.add(getSprite("stars2small"));
            MapStars.add(getSprite("stars2-5small"));
            MapStars.add(getSprite("stars3small"));
            MapStars.add(getSprite("stars3-5small"));
            MapStars.add(getSprite("stars4small"));
            MapStars.add(getSprite("stars4-5small"));
            MapStars.add(getSprite("stars5small"));

        }

        if (Stars == null)
            Stars = new SpriteList();
        synchronized (Stars) {
            Stars.clear();
            Stars.add(getSprite("stars0icon"));
            Stars.add(getSprite("stars0-5icon"));
            Stars.add(getSprite("stars1icon"));
            Stars.add(getSprite("stars1-5icon"));
            Stars.add(getSprite("stars2icon"));
            Stars.add(getSprite("stars2-5icon"));
            Stars.add(getSprite("stars3icon"));
            Stars.add(getSprite("stars3-5icon"));
            Stars.add(getSprite("stars4icon"));
            Stars.add(getSprite("stars4-5icon"));
            Stars.add(getSprite("stars5icon"));

        }

        if (Bubble == null)
            Bubble = new ArrayList<Sprite>();
        synchronized (Bubble) {
            Bubble.clear();
            Bubble.add(getSprite("Bubble"));
            Bubble.add(getSprite("Bubble-selected"));
            Bubble.add(getSprite("BubbleOverlay"));
            Bubble.add(getSprite("1to4bubble"));
            Bubble.add(getSprite("5bubble"));
            Bubble.add(getSprite("1to4bubble-flip"));
            Bubble.add(getSprite("5bubble-flip"));

        }

        if (ChkIcons == null)
            ChkIcons = new ArrayList<Sprite>();
        synchronized (ChkIcons) {

            ChkIcons.clear();
            ChkIcons.add(getSprite("check-off"));
            ChkIcons.add(getSprite("check-on"));

        }

        if (Dialog == null)
            Dialog = new ArrayList<Sprite>();
        synchronized (Dialog) {
            Dialog.clear();
            Dialog.add(getSprite("dialog-header"));
            Dialog.add(getSprite("dialog-center"));
            Dialog.add(getSprite("dialog-footer"));
            Dialog.add(getSprite("dialog-title"));
            Dialog.add(getSprite("menu-divider"));

        }

        if (ToggleBtn == null)
            ToggleBtn = new ArrayList<Sprite>();
        synchronized (ToggleBtn) {
            ToggleBtn.clear();
            ToggleBtn.add(getSprite(IconName.btnNormal.name()));
            ToggleBtn.add(getSprite("btn-pressed"));
            ToggleBtn.add(getSprite("toggle-led-gr"));

        }

        if (LiveBtn == null)
            LiveBtn = new ArrayList<Sprite>();
        synchronized (LiveBtn) {
            LiveBtn.clear();
            LiveBtn.add(getSprite("LiveEnabled"));
            LiveBtn.add(getSprite("LiveDisabled"));
            LiveBtn.add(getSprite("Live1"));
            LiveBtn.add(getSprite("Live2"));
            LiveBtn.add(getSprite("Live3"));
            LiveBtn.add(getSprite("Live4"));
            LiveBtn.add(getSprite("Live5"));
            LiveBtn.add(getSprite("Live6"));
            LiveBtn.add(getSprite("Live7"));
            LiveBtn.add(getSprite("Live8"));

        }

        Progress = getSprite("progress");
        ambilwarna_hue = getSprite("ambilwarna-hue");
        ambilwarna_cursor = getSprite("ambilwarna-cursor");
        ambilwarna_target = getSprite("ambilwarna-target");

        if (ZoomBtn == null)
            ZoomBtn = new ArrayList<Sprite>();
        synchronized (ZoomBtn) {
            ZoomBtn.clear();
            ZoomBtn.add(getSprite("day-btn-zoom-down-normal"));
            ZoomBtn.add(getSprite("day-btn-zoom-down-pressed"));
            ZoomBtn.add(getSprite("day-btn-zoom-down-disabled"));
            ZoomBtn.add(getSprite("day-btn-zoom-up-normal"));
            ZoomBtn.add(getSprite("day-btn-zoom-up-pressed"));
            ZoomBtn.add(getSprite("day-btn-zoom-up-disabled"));

        }

        ZoomValueBack = getSprite("zoom-back");

        if (SizesIcons == null)
            SizesIcons = new SpriteList();
        synchronized (SizesIcons) {
            SizesIcons.clear();
            SizesIcons.add(getSprite("other"));
            SizesIcons.add(getSprite("micro"));
            SizesIcons.add(getSprite("small"));
            SizesIcons.add(getSprite("regular"));
            SizesIcons.add(getSprite("large"));

        }

        MapScale = new Drawable[3];

        Sprite MS3 = getSprite("MapScale-3");
        int patchMS3 = (int) Math.max((MS3.getWidth() / 10), 1);
        MapScale[0] = new NinePatchDrawable(new NinePatch(MS3, patchMS3, patchMS3, 0, 0));

        Sprite MS4 = getSprite("MapScale-4");
        int patchMS4 = (int) Math.max((MS4.getWidth() / 10), 1);
        MapScale[1] = new NinePatchDrawable(new NinePatch(MS4, patchMS4, patchMS4, 0, 0));

        Sprite MS5 = getSprite("MapScale-5");
        int patchMS5 = (int) Math.max((MS5.getWidth() / 10), 1);
        MapScale[2] = new NinePatchDrawable(new NinePatch(MS5, patchMS5, patchMS5, 0, 0));

        loadButtonSprites();

        createDrawables();

        loaded = true;

    }

    protected static void createDrawables() {
        patch = (Sprites.getSprite("activity-back").getWidth() > 60) ? 16 : 8;

        activityBackground = new NinePatchDrawable(new NinePatch(Sprites.getSprite("activity-back"), patch, patch, patch, patch));
        activityBorderMask = new NinePatchDrawable(new NinePatch(Sprites.getSprite("activity-border"), patch, patch, patch, patch));
        ListBack = new ColorDrawable(CB_Skin.getInstance().getThemedColor("background"));
        ButtonBack = new SpriteDrawable(getSprite("button-list-back"));
        AboutBack = new SpriteDrawable(getSprite("splash-back"));
        InfoBack = new NinePatchDrawable(new NinePatch(getSprite("InfoPanelBack"), patch, patch, patch, patch));
        ProgressBack = new NinePatchDrawable(new NinePatch(ToggleBtn.get(0), patch, patch, patch, patch));
        ProgressFill = new NinePatchDrawable(new NinePatch(Sprites.Progress, patch - 1, patch - 1, patch - 1, patch - 1));
        ProgressDisabled = new NinePatchDrawable(new NinePatch(getSprite("progress-disabled"), patch - 1, patch - 1, patch - 1, patch - 1));
        btn = new NinePatchDrawable(new NinePatch(Sprites.getSprite(IconName.btnNormal.name()), patch, patch, patch, patch));
        btnPressed = new NinePatchDrawable(new NinePatch(Sprites.getSprite("btn-pressed"), patch, patch, patch, patch));
        btnDisabled = new NinePatchDrawable(new NinePatch(Sprites.getSprite("btn-disabled"), patch, patch, patch, patch));

        SliderPushed = new SpriteDrawable(getSprite("scrollbarPushedSlider"));
        // SliderBack = new SpriteDrawable(getThemedSprite(????));

        Sprite tmpSlider = getSprite("scrollbarSlider");

        int sliderPatch = (int) (tmpSlider.getWidth() / 4);

        Slider = new NinePatchDrawable(new NinePatch(tmpSlider, sliderPatch, sliderPatch, sliderPatch, sliderPatch));

        chkOn = new SpriteDrawable(getSprite("check-on"));
        chkOff = new SpriteDrawable(getSprite("check-off"));
        chkOnDisabled = new SpriteDrawable(getSprite("check-disable"));
        chkOffDisabled = new SpriteDrawable(getSprite("check-off"));

        radioOn = new SpriteDrawable(getSprite("RadioButtonSet"));
        radioBack = new SpriteDrawable(getSprite("RadioButtonBack"));

        textFieldBackground = new NinePatchDrawable(new NinePatch(Sprites.getSprite("text-field-back"), patch, patch, patch, patch));
        textFieldBackgroundFocus = new NinePatchDrawable(new NinePatch(Sprites.getSprite("text-field-back-focus"), patch, patch, patch, patch));

        selection = new SpriteDrawable(getSprite("Selection"));
        selection_set = new SpriteDrawable(getSprite("Selection-set"));
        selection_left = new SpriteDrawable(getSprite("Selection-Left"));
        selection_right = new SpriteDrawable(getSprite("Selection-Right"));
        logo = new SpriteDrawable(getSprite("cachebox-logo"));

        copy = new SpriteDrawable(getSprite("tf-copy"));
        paste = new SpriteDrawable(getSprite("tf-paste"));
        cut = new SpriteDrawable(getSprite("tf-cut"));

        bar = new NinePatchDrawable(getThemedPatch("bar"));
        barSmall = new NinePatchDrawable(getThemedPatch("bar-small"));

        bar_0 = new SpriteDrawable(getSprite("bar-0"));
        barSmall_0 = new SpriteDrawable(getSprite("bar-small-0"));

        textFieldCursor = new NinePatchDrawable(new NinePatch(Sprites.getSprite("selection-input-icon"), 1, 1, 2, 2));

        int hp = patch / 2;
        shaddowRec = new NinePatchDrawable(new NinePatch(Sprites.getSprite("shaddowrect"), hp, hp, hp, hp));

    }

    public static SpriteDrawable getSpriteDrawable(String name) {
        return new SpriteDrawable(getSprite(name));
    }

    protected static void loadButtonSprites() {
        CacheList = new ButtonSprites(getSprite("db"), getSprite("db-pressed"), null, getSprite("db-pressed"));
        CacheListFilter = new ButtonSprites(getSprite("db-filter-active"), getSprite("db-pressed-filter-active"), null, getSprite("db-pressed-filter-active"));
        Cache = new ButtonSprites(getSprite("cache"), getSprite("cache-pressed"), null, getSprite("cache-pressed"));
        Nav = new ButtonSprites(getSprite("Nav"), getSprite("Nav-pressed"), null, getSprite("Nav-pressed"));
        Tool = new ButtonSprites(getSprite("tool"), getSprite("tool-pressed"), null, getSprite("tool-pressed"));
        Misc = new ButtonSprites(getSprite("misc"), getSprite("misc-pressed"), null, getSprite("misc-pressed"));
        QuickButton = new ButtonSprites(getSprite("button"), getSprite("btn-pressed"), null, getSprite("btn-pressed"));
    }

    /**
     * Destroy cached sprites
     */
    public static void destroyCache() {
        Arrows = null;
        MapStars = null;
        Bubble = null;
        ToggleBtn = null;
        LiveBtn = null;
        ZoomBtn = null;
        ZoomValueBack = null;

        if (CacheList != null)
            CacheList.dispose();
        if (Cache != null)
            Cache.dispose();
        if (Nav != null)
            Nav.dispose();
        if (Tool != null)
            Tool.dispose();
        if (Misc != null)
            Misc.dispose();

        CacheList = null;
        Cache = null;
        Nav = null;
        Tool = null;
        Misc = null;
        QuickButton = null;

        if (atlasDefault != null) {
            atlasDefault.dispose();
            atlasDefault = null;
        }

        if (atlasDefaultNight != null) {
            atlasDefaultNight.dispose();
            atlasDefaultNight = null;
        }

        if (atlasCustom != null) {
            atlasCustom.dispose();
            atlasCustom = null;
        }

        if (atlasCustomtNight != null) {
            atlasCustomtNight.dispose();
            atlasCustomtNight = null;
        }
    }

    public static enum IconName {
        btnNormal, //
        button, //
        docIcon, //
        manualWayPoint, //
        listIcon, // CB_Action_ShowLogView
        map, // CB_Action_ShowMap
        compass, // CB_Action_ShowCompassView
        cacheListIcon, // CB_Action_ShowCacheList
        trackListIcon, // CB_Action_GenerateRoute, CB_Action_RecTrack, CB_Action_ShowTrackListView
        log10icon, //
        videoIcon, // QuickActions/CB_Action_ShowActivity/RecVideo, TabMainView/CB_Action_ShowActivity/RecVideo
        voiceRecIcon, // QuickActions/CB_Action_ShowActivity/VoiceRec, TabMainView/CB_Action_ShowActivity/VoiceRec
        lupe, // CB_Action_ShowCacheList/getContextMenu/MenuID.MI_SEARCH_LIST
        filter, // CB_Action_ShowCacheList/getContextMenu/MenuID.MI_FilterSet MI_RESET_FILTER, CB_Action_ShowFilterSettings
        lockIcon, // not used
        autoSortOnIcon, // QuickButtonItem
        autoSortOffIcon, // QuickButtonItem, CB_Action_switch_Autoresort
        solverIcon, // CB_Action_ShowSolverView, CB_Action_ShowSolverView2
        imagesIcon, // CB_Action_ShowSpoilerView, QuickButtonItem
        hintIcon, // CB_Action_ShowHint, QuickButtonItem, showBtnCacheContextMenu
        noteIcon, // 23 not used
        settings, // 26 Image
        DELETE, //
        satellite, //
        closeIcon, // 31
        infoIcon, // 32
        warningIcon, // 33
        helpIcon, // 34
        dayGcLiveIcon, // 35
        tb, // 36
        cmIcon, // 37
        tbListIcon, // 38
        sortIcon, // 39
        importIcon, // 40
        manageDb, // 41
        favorit, // 42
        star, // 43
        disabled, // 44
        log11icon, // 45
        navigate, // 46
        DayNight, // 48
        cb, // 49
        ADD, // 52
        targetDay, // 53
        FieldNote, // 54
        fieldnoteListIcon, // 55
        waypointListIcon, // 56
        addCacheIcon, // 57
        TBDISCOVER, // 58
        TBDROP, // 59
        TBGRAB, // 60
        TBPICKED, // 61
        TBVISIT, // 62
        TBNOTE, // 63
        UPLOADFIELDNOTE, // 64
        MENUFILTERED, // 65
        save, // 66
        TORCHON, // 67
        TORCHOFF, // 68
        userdata, //
        mapsforge_logo, //
        download, //
        freizeit,//
        shaddowrect, // 0
        shaddowrectselected, // 1
        deact, // 2
        cross, // 3
        live, // 4
        liveSelected, // 5
        FavPoi,
        LoadLogImages,
    }

    public static enum DialogElement {
        header, // 0
        center, // 1
        footer, // 2
        title, // 3
        divider // 4
    }

    public static class SpriteList extends ArrayList<Sprite> {
        private static final long serialVersionUID = 1L;

        public SpriteList() {
            new ArrayList<Sprite>();
        }

        @Override
        public Sprite[] toArray() {
            Sprite[] tmp = new Sprite[this.size()];

            int index = 0;
            for (Sprite s : this) {
                tmp[index++] = s;
            }
            return tmp;
        }
    }

}
