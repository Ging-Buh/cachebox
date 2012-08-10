/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_Core.GL_UI;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Enthält die geladenen Sprites und das Handling für Laden und Entladen.
 * 
 * @author Longri
 */
public class SpriteCache
{
	public static class SpriteList extends ArrayList<Sprite>
	{
		private static final long serialVersionUID = 1L;

		public SpriteList()
		{
			new ArrayList<Sprite>();
		}

		public Sprite[] toArray()
		{
			Sprite[] tmp = new Sprite[this.size()];

			int index = 0;
			for (Sprite s : this)
			{
				tmp[index++] = s;
			}
			return tmp;
		}
	}

	public static ArrayList<Sprite> MapIconsSmall = null;
	public static ArrayList<Sprite> MapOverlay = null;
	public static ArrayList<Sprite> MapIcons = null;
	public static ArrayList<Sprite> Arrows = null;
	public static ArrayList<Sprite> MapStars = null;
	public static SpriteList Stars = null;
	public static ArrayList<Sprite> Bubble = null;

	public static ArrayList<Sprite> ToggleBtn = null;
	public static ArrayList<Sprite> ZoomBtn = null;
	public static Sprite ZoomValueBack = null;
	public static ArrayList<Sprite> BigIcons = null;
	public static ArrayList<Sprite> Icons = null;
	public static ArrayList<Sprite> ChkIcons = null;
	public static ArrayList<Sprite> Dialog = null;
	public static SpriteList SizesIcons = null;

	public static Drawable ListBack = null;
	public static Drawable ButtonBack = null;
	public static Drawable AboutBack = null;
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

	private static String PathDefault;
	private static String PathCostum;
	private static String PathDefaultNight;
	private static String PathCostumNight;

	private static TextureAtlas atlasDefault;
	private static TextureAtlas atlasDefaultNight;
	private static TextureAtlas atlasCostum;
	private static TextureAtlas atlasCostumtNight;

	private static Boolean atlasDefaultIsNeverUsed = true;
	private static Boolean atlasDefaultNightIsNeverUsed = true;
	private static Boolean atlasCostumIsNeverUsed = true;
	private static Boolean atlasCostumtNightIsNeverUsed = true;
	public static ArrayList<Sprite> LogIcons;

	public static Drawable activityBackground;
	public static Drawable InfoBack;
	public static Drawable ProgressBack;
	public static Drawable ProgressFill;
	public static Drawable btn;
	public static Drawable btnPressed;
	public static Drawable btnDisabled;
	public static Drawable shaddowRec;

	public static Drawable chkOn;
	public static Drawable chkOff;
	public static Drawable chkOnDisabled;
	public static Drawable chkOffDisabled;

	public static int patch;

	private static void setPath(String path)
	{

		if (Gdx.gl11 != null)
		{
			Gdx.gl11.glFlush();
			Gdx.gl11.glFinish();
		}

		atlasDefaultIsNeverUsed = true;
		atlasDefaultNightIsNeverUsed = true;
		atlasCostumIsNeverUsed = true;
		atlasCostumtNightIsNeverUsed = true;

		PathCostum = path + "/day/UI_IconPack.spp";
		PathCostumNight = path + "/night/UI_IconPack.spp";

		String defaultPath = path;
		int pos = defaultPath.lastIndexOf("/");
		if (GlobalCore.useSmallSkin)
		{
			defaultPath = defaultPath.substring(0, pos) + "/small";
			PathCostum = "";
			PathCostumNight = "";
		}
		else
		{
			defaultPath = defaultPath.substring(0, pos) + "/default";
		}

		PathDefault = defaultPath + "/day/UI_IconPack.spp";
		PathDefaultNight = defaultPath + "/night/UI_IconPack.spp";

		if (atlasDefault != null)
		{
			atlasDefault.dispose();
			atlasDefault = null;
		}

		if (atlasDefaultNight != null)
		{
			atlasDefaultNight.dispose();
			atlasDefaultNight = null;
		}

		if (atlasCostum != null)
		{
			atlasCostum.dispose();
			atlasCostum = null;
		}

		if (atlasCostumtNight != null)
		{
			atlasCostumtNight.dispose();
			atlasCostumtNight = null;
		}

		if (FileIO.FileExists(PathDefault)) atlasDefault = new TextureAtlas(Gdx.files.absolute(PathDefault));
		if (FileIO.FileExists(PathDefaultNight)) atlasDefaultNight = new TextureAtlas(Gdx.files.absolute(PathDefaultNight));

		if (!PathDefault.equals(PathCostum))
		{
			if (FileIO.FileExists(PathCostum)) atlasCostum = new TextureAtlas(Gdx.files.absolute(PathCostum));
			if (FileIO.FileExists(PathCostumNight)) atlasCostumtNight = new TextureAtlas(Gdx.files.absolute(PathCostumNight));
		}
	}

	public static Sprite getThemedSprite(String name)
	{
		Sprite tmp = null;
		if (Config.settings.nightMode.getValue())
		{
			tmp = createSprite(atlasCostumtNight, name);
			if (tmp == null)
			{
				tmp = createSprite(atlasCostum, name);
				if (tmp != null) tmp = setNightColorMatrix(name, atlasCostum);
			}
			else
			{
				atlasCostumtNightIsNeverUsed = false;
			}

			if (tmp == null)
			{
				tmp = createSprite(atlasDefaultNight, name);

				if (tmp == null)
				{
					tmp = setNightColorMatrix(name, atlasDefault);
				}
				else
				{
					atlasDefaultNightIsNeverUsed = false;
				}
			}
			else
			{
				atlasCostumIsNeverUsed = false;
			}

		}
		else
		{
			tmp = createSprite(atlasCostum, name);
		}

		if (tmp == null)
		{
			tmp = createSprite(atlasDefault, name);
		}
		else
		{
			atlasCostumIsNeverUsed = false;
		}

		return tmp;
	}

	private static Sprite setNightColorMatrix(String name, TextureAtlas atlas)
	{
		Sprite tmp = null;
		tmp = createSprite(atlas, name);
		if (tmp == null) return null;

		Color colorOverlay = new Color(0.5f, 0.4f, 0.4f, 1f);
		tmp.setColor(colorOverlay);

		return tmp;
	}

	private static Sprite createSprite(TextureAtlas atlas, String name)
	{
		Sprite tmp = null;
		if (atlas != null)
		{
			tmp = atlas.createSprite(name);
		}
		return tmp;
	}

	/**
	 * Load the Sprites from recorce
	 */
	public static void LoadSprites(boolean reload)
	{

		if (!reload) setPath(Config.settings.SkinFolder.getValue());

		if (LogIcons == null) LogIcons = new ArrayList<Sprite>();
		synchronized (LogIcons)
		{
			LogIcons.clear();
			LogIcons.add(getThemedSprite("log0icon"));
			LogIcons.add(getThemedSprite("log1icon"));
			LogIcons.add(getThemedSprite("log2icon"));
			LogIcons.add(getThemedSprite("log3icon"));
			LogIcons.add(getThemedSprite("log4icon"));
			LogIcons.add(getThemedSprite("log5icon"));
			LogIcons.add(getThemedSprite("log6icon"));
			LogIcons.add(getThemedSprite("log7icon"));
			LogIcons.add(getThemedSprite("log8icon"));
			LogIcons.add(getThemedSprite("log9icon"));
			LogIcons.add(getThemedSprite("log10icon"));
			LogIcons.add(getThemedSprite("log11icon"));
			LogIcons.add(getThemedSprite("log12icon"));
			LogIcons.add(getThemedSprite("log13icon"));
			LogIcons.add(getThemedSprite("log14icon"));
			LogIcons.add(getThemedSprite("log15icon"));
			LogIcons.add(getThemedSprite("log16icon"));
			LogIcons.add(getThemedSprite("log17icon"));
			LogIcons.add(getThemedSprite("log18icon"));
			LogIcons.add(getThemedSprite("log19icon"));
			LogIcons.add(getThemedSprite("log20icon"));
			LogIcons.add(getThemedSprite("log21icon"));
		}

		if (MapIconsSmall == null) MapIconsSmall = new ArrayList<Sprite>();
		synchronized (MapIconsSmall)
		{
			MapIconsSmall.clear();
			MapIconsSmall.add(getThemedSprite("small1yes"));
			MapIconsSmall.add(getThemedSprite("small2yes"));
			MapIconsSmall.add(getThemedSprite("small3yes"));
			MapIconsSmall.add(getThemedSprite("small4yes"));
			MapIconsSmall.add(getThemedSprite("small5yes"));
			MapIconsSmall.add(getThemedSprite("small5solved"));
			MapIconsSmall.add(getThemedSprite("small6yes"));
			MapIconsSmall.add(getThemedSprite("small7yes"));
			MapIconsSmall.add(getThemedSprite("small1no"));
			MapIconsSmall.add(getThemedSprite("small2no"));
			MapIconsSmall.add(getThemedSprite("small3no"));
			MapIconsSmall.add(getThemedSprite("small4no"));
			MapIconsSmall.add(getThemedSprite("small5no"));
			MapIconsSmall.add(getThemedSprite("small5solved-no"));
			MapIconsSmall.add(getThemedSprite("small6no"));
			MapIconsSmall.add(getThemedSprite("small7no"));
			MapIconsSmall.add(getThemedSprite("20"));

		}

		if (MapOverlay == null) MapOverlay = new ArrayList<Sprite>();
		synchronized (MapOverlay)
		{
			MapOverlay.clear();
			MapOverlay.add(getThemedSprite("shaddowrect"));
			MapOverlay.add(getThemedSprite("shaddowrect-selected"));
			MapOverlay.add(getThemedSprite("deact"));
			MapOverlay.add(getThemedSprite("cross"));

		}

		if (MapIcons == null) MapIcons = new ArrayList<Sprite>();
		synchronized (MapIcons)
		{
			MapIcons.clear();
			MapIcons.add(getThemedSprite("0"));
			MapIcons.add(getThemedSprite("1"));
			MapIcons.add(getThemedSprite("2"));
			MapIcons.add(getThemedSprite("3"));
			MapIcons.add(getThemedSprite("4"));
			MapIcons.add(getThemedSprite("5"));
			MapIcons.add(getThemedSprite("6"));
			MapIcons.add(getThemedSprite("7"));
			MapIcons.add(getThemedSprite("8"));
			MapIcons.add(getThemedSprite("9"));
			MapIcons.add(getThemedSprite("10"));
			MapIcons.add(getThemedSprite("11"));
			MapIcons.add(getThemedSprite("12"));
			MapIcons.add(getThemedSprite("13"));
			MapIcons.add(getThemedSprite("14"));
			MapIcons.add(getThemedSprite("15"));
			MapIcons.add(getThemedSprite("16"));
			MapIcons.add(getThemedSprite("17"));
			MapIcons.add(getThemedSprite("18"));
			MapIcons.add(getThemedSprite("19"));
			MapIcons.add(getThemedSprite("20"));
			MapIcons.add(getThemedSprite("21"));
			MapIcons.add(getThemedSprite("star"));

		}

		if (Arrows == null) Arrows = new ArrayList<Sprite>();
		synchronized (Arrows)
		{
			Arrows.clear();
			Arrows.add(getThemedSprite("arrow-Compass"));
			Arrows.add(getThemedSprite("arrow-Compass-Trans"));
			Arrows.add(getThemedSprite("arrow-GPS"));
			Arrows.add(getThemedSprite("arrow-GPS-Trans"));
			Arrows.add(getThemedSprite("target-arrow"));
			Arrows.add(getThemedSprite("track-line"));
			Arrows.add(getThemedSprite("arrow-down"));
			Arrows.add(getThemedSprite("arrow-up"));
			Arrows.add(getThemedSprite("arrow-left"));
			Arrows.add(getThemedSprite("arrow-right"));
			Arrows.add(getThemedSprite("track-point"));
			Arrows.add(getThemedSprite("ambilwarna-arrow-right"));
			Arrows.add(getThemedSprite("ambilwarna-arrow-down"));

		}

		if (MapStars == null) MapStars = new ArrayList<Sprite>();
		synchronized (MapStars)
		{
			MapStars.clear();
			MapStars.add(getThemedSprite("stars0small"));
			MapStars.add(getThemedSprite("stars0-5small"));
			MapStars.add(getThemedSprite("stars1small"));
			MapStars.add(getThemedSprite("stars1-5small"));
			MapStars.add(getThemedSprite("stars2small"));
			MapStars.add(getThemedSprite("stars2-5small"));
			MapStars.add(getThemedSprite("stars3small"));
			MapStars.add(getThemedSprite("stars3-5small"));
			MapStars.add(getThemedSprite("stars4small"));
			MapStars.add(getThemedSprite("stars4-5small"));
			MapStars.add(getThemedSprite("stars5small"));

		}

		if (Stars == null) Stars = new SpriteList();
		synchronized (Stars)
		{
			Stars.clear();
			Stars.add(getThemedSprite("stars0icon"));
			Stars.add(getThemedSprite("stars0-5icon"));
			Stars.add(getThemedSprite("stars1icon"));
			Stars.add(getThemedSprite("stars1-5icon"));
			Stars.add(getThemedSprite("stars2icon"));
			Stars.add(getThemedSprite("stars2-5icon"));
			Stars.add(getThemedSprite("stars3icon"));
			Stars.add(getThemedSprite("stars3-5icon"));
			Stars.add(getThemedSprite("stars4icon"));
			Stars.add(getThemedSprite("stars4-5icon"));
			Stars.add(getThemedSprite("stars5icon"));

		}

		if (Bubble == null) Bubble = new ArrayList<Sprite>();
		synchronized (Bubble)
		{
			Bubble.clear();
			Bubble.add(getThemedSprite("Bubble"));
			Bubble.add(getThemedSprite("Bubble-selected"));
			Bubble.add(getThemedSprite("BubbleOverlay"));
			Bubble.add(getThemedSprite("1to4bubble"));
			Bubble.add(getThemedSprite("5bubble"));
			Bubble.add(getThemedSprite("1to4bubble-flip"));
			Bubble.add(getThemedSprite("5bubble-flip"));

		}

		if (ChkIcons == null) ChkIcons = new ArrayList<Sprite>();
		synchronized (ChkIcons)
		{

			ChkIcons.clear();
			ChkIcons.add(getThemedSprite("check-off"));
			ChkIcons.add(getThemedSprite("check-on"));

		}

		if (Dialog == null) Dialog = new ArrayList<Sprite>();
		synchronized (Dialog)
		{
			Dialog.clear();
			Dialog.add(getThemedSprite("dialog-header"));
			Dialog.add(getThemedSprite("dialog-center"));
			Dialog.add(getThemedSprite("dialog-footer"));
			Dialog.add(getThemedSprite("dialog-title"));

		}

		if (ToggleBtn == null) ToggleBtn = new ArrayList<Sprite>();
		synchronized (ToggleBtn)
		{
			ToggleBtn.clear();
			ToggleBtn.add(getThemedSprite("btn-normal"));
			ToggleBtn.add(getThemedSprite("btn-pressed"));
			ToggleBtn.add(getThemedSprite("toggle-led-gr"));

		}

		Progress = getThemedSprite("progress");
		ambilwarna_hue = getThemedSprite("ambilwarna-hue");
		ambilwarna_cursor = getThemedSprite("ambilwarna-cursor");
		ambilwarna_target = getThemedSprite("ambilwarna-target");

		if (ZoomBtn == null) ZoomBtn = new ArrayList<Sprite>();
		synchronized (ZoomBtn)
		{
			ZoomBtn.clear();
			ZoomBtn.add(getThemedSprite("day-btn-zoom-down-normal"));
			ZoomBtn.add(getThemedSprite("day-btn-zoom-down-pressed"));
			ZoomBtn.add(getThemedSprite("day-btn-zoom-down-disabled"));
			ZoomBtn.add(getThemedSprite("day-btn-zoom-up-normal"));
			ZoomBtn.add(getThemedSprite("day-btn-zoom-up-pressed"));
			ZoomBtn.add(getThemedSprite("day-btn-zoom-up-disabled"));

		}

		ZoomValueBack = getThemedSprite("zoom-back");

		if (SizesIcons == null) SizesIcons = new SpriteList();
		synchronized (SizesIcons)
		{
			SizesIcons.clear();
			SizesIcons.add(getThemedSprite("other"));
			SizesIcons.add(getThemedSprite("micro"));
			SizesIcons.add(getThemedSprite("small"));
			SizesIcons.add(getThemedSprite("regular"));
			SizesIcons.add(getThemedSprite("large"));

		}

		if (BigIcons == null) BigIcons = new ArrayList<Sprite>();
		synchronized (BigIcons)
		{
			BigIcons.clear();
			BigIcons.add(getThemedSprite("big0icon")); // 0
			BigIcons.add(getThemedSprite("big1icon")); // 1
			BigIcons.add(getThemedSprite("big2icon")); // 2
			BigIcons.add(getThemedSprite("big3icon")); // 3
			BigIcons.add(getThemedSprite("big4icon")); // 4
			BigIcons.add(getThemedSprite("big5icon")); // 5
			BigIcons.add(getThemedSprite("big6icon")); // 6
			BigIcons.add(getThemedSprite("big7icon")); // 7
			BigIcons.add(getThemedSprite("big8icon")); // 8
			BigIcons.add(getThemedSprite("big9icon")); // 9
			BigIcons.add(getThemedSprite("big10icon")); // 10
			BigIcons.add(getThemedSprite("big11icon")); // 11
			BigIcons.add(getThemedSprite("big12icon")); // 12
			BigIcons.add(getThemedSprite("big13icon")); // 13
			BigIcons.add(getThemedSprite("big14icon")); // 14
			BigIcons.add(getThemedSprite("big15icon")); // 15
			BigIcons.add(getThemedSprite("big16icon")); // 16
			BigIcons.add(getThemedSprite("big17icon")); // 17
			BigIcons.add(getThemedSprite("big18icon")); // 18
			BigIcons.add(getThemedSprite("log0icon")); // 19
			BigIcons.add(getThemedSprite("my-parking")); // 20
			BigIcons.add(getThemedSprite("big19icon")); // 21

		}

		if (Icons == null) Icons = new ArrayList<Sprite>();
		synchronized (Icons)
		{
			Icons.clear();
			Icons.add(getThemedSprite("btn-normal"));// 0
			Icons.add(getThemedSprite("button"));// 1
			Icons.add(getThemedSprite("doc-icon"));// 2
			Icons.add(getThemedSprite("big16icon"));// 3
			Icons.add(getThemedSprite("list-icon")); // 4 LogView braucht noch ein Icon
			Icons.add(getThemedSprite("map")); // 5
			Icons.add(getThemedSprite("compass"));// 6
			Icons.add(getThemedSprite("cache-list-icon"));// 7
			Icons.add(getThemedSprite("track-list-icon")); // 8
			Icons.add(getThemedSprite("log10icon"));// 9
			Icons.add(getThemedSprite("video-icon")); // 10
			Icons.add(getThemedSprite("voice-rec-icon"));// 11
			Icons.add(getThemedSprite("lupe")); // 12
			Icons.add(getThemedSprite("filter")); // 13
			Icons.add(getThemedSprite("lock-icon"));// 14
			Icons.add(getThemedSprite("auto-sort-on-icon")); // 15
			Icons.add(getThemedSprite("auto-sort-off-icon")); // 16
			Icons.add(getThemedSprite("solver-icon")); // 17
			Icons.add(getThemedSprite("images-icon")); // 18
			Icons.add(getThemedSprite("hint-icon")); // 19
			Icons.add(getThemedSprite("doc-icon")); // 20
			Icons.add(getThemedSprite("list-icon")); // 21
			Icons.add(getThemedSprite("images-icon")); // 22
			Icons.add(getThemedSprite("note-icon")); // 23
			Icons.add(getThemedSprite("solver-icon")); // 24
			Icons.add(getThemedSprite("joker-phone")); // 25
			Icons.add(getThemedSprite("settings")); // 26
			Icons.add(getThemedSprite("lupe")); // 27
			Icons.add(getThemedSprite("delete-icon")); // 28
			Icons.add(getThemedSprite("voice-rec-icon")); // 29
			Icons.add(getThemedSprite("satellite")); // 30
			Icons.add(getThemedSprite("close-icon")); // 31
			Icons.add(getThemedSprite("info-icon")); // 32
			Icons.add(getThemedSprite("warning-icon")); // 33
			Icons.add(getThemedSprite("help-icon")); // 34
			Icons.add(getThemedSprite("day-gc-live-icon")); // 35
			Icons.add(getThemedSprite("tb")); // 36
			Icons.add(getThemedSprite("cm-icon")); // 37
			Icons.add(getThemedSprite("tb-list-icon")); // 38
			Icons.add(getThemedSprite("sort-icon")); // 39
			Icons.add(getThemedSprite("import")); // 40
			Icons.add(getThemedSprite("manage-db")); // 41
			Icons.add(getThemedSprite("favorit")); // 42
			Icons.add(getThemedSprite("star")); // 43
			Icons.add(getThemedSprite("disabled")); // 44
			Icons.add(getThemedSprite("not-available")); // 45
			Icons.add(getThemedSprite("navigate")); // 46
			Icons.add(getThemedSprite("log10icon")); // 47
			Icons.add(getThemedSprite("d-n")); // 48
			Icons.add(getThemedSprite("cb")); // 49
			Icons.add(getThemedSprite("userdata")); // 50
			Icons.add(getThemedSprite("day-spinner")); // 51

		}

		loadButtnSprites();

		createDrawables();

		// cleanUp();
	}

	private static void createDrawables()
	{
		patch = (SpriteCache.getThemedSprite("activity-back").getWidth() > 60) ? 16 : 8;

		activityBackground = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("activity-back"), patch, patch, patch, patch));
		ListBack = new NinePatchDrawable(new NinePatch(getThemedSprite("background"), 1, 1, 1, 1));
		ButtonBack = new SpriteDrawable(getThemedSprite("button-list-back"));
		AboutBack = new SpriteDrawable(getThemedSprite("splash-back"));
		InfoBack = new NinePatchDrawable(new NinePatch(getThemedSprite("InfoPanelBack"), patch, patch, patch, patch));
		ProgressBack = new NinePatchDrawable(new NinePatch(ToggleBtn.get(0), patch, patch, patch, patch));
		ProgressFill = new NinePatchDrawable(new NinePatch(SpriteCache.Progress, patch - 1, patch - 1, patch - 1, patch - 1));
		btn = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("btn-normal"), patch, patch, patch, patch));
		btnPressed = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("btn-pressed"), patch, patch, patch, patch));
		btnDisabled = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("btn-disabled"), patch, patch, patch, patch));

		chkOn = new SpriteDrawable(getThemedSprite("check-on"));
		chkOff = new SpriteDrawable(getThemedSprite("check-off"));
		chkOnDisabled = new SpriteDrawable(getThemedSprite("check-disable"));
		chkOffDisabled = new SpriteDrawable(getThemedSprite("check-off"));

		int hp = patch / 2;
		shaddowRec = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("shaddowrect"), hp, hp, hp, hp));

	}

	private static void loadButtnSprites()
	{
		CacheList = new ButtonSprites(getThemedSprite("db"), getThemedSprite("db-pressed"));
		CacheListFilter = new ButtonSprites(getThemedSprite("db-filter-active"), getThemedSprite("db-pressed-filter-active"));
		Cache = new ButtonSprites(getThemedSprite("cache"), getThemedSprite("cache-pressed"));
		Nav = new ButtonSprites(getThemedSprite("Nav"), getThemedSprite("Nav-pressed"));
		Tool = new ButtonSprites(getThemedSprite("tool"), getThemedSprite("tool-pressed"));
		Misc = new ButtonSprites(getThemedSprite("misc"), getThemedSprite("misc-pressed"));
		QuickButton = new ButtonSprites(getThemedSprite("button"), getThemedSprite("btn-pressed"));

	}

	/**
	 * Destroy cached sprites
	 */
	public static void destroyCache()
	{
		MapIconsSmall = null;
		MapOverlay = null;
		MapIcons = null;
		Arrows = null;
		MapStars = null;
		Bubble = null;
		ToggleBtn = null;
		ZoomBtn = null;
		ZoomValueBack = null;

		if (CacheList != null) CacheList.dispose();
		if (Cache != null) Cache.dispose();
		if (Nav != null) Nav.dispose();
		if (Tool != null) Tool.dispose();
		if (Misc != null) Misc.dispose();

		CacheList = null;
		Cache = null;
		Nav = null;
		Tool = null;
		Misc = null;
		QuickButton = null;

		if (atlasDefault != null)
		{
			atlasDefault.dispose();
			atlasDefault = null;
		}

		if (atlasDefaultNight != null)
		{
			atlasDefaultNight.dispose();
			atlasDefaultNight = null;
		}

		if (atlasCostum != null)
		{
			atlasCostum.dispose();
			atlasCostum = null;
		}

		if (atlasCostumtNight != null)
		{
			atlasCostumtNight.dispose();
			atlasCostumtNight = null;
		}
	}

	private static void cleanUp()
	{

		// die TextureAtlanten, welche nicht benutzt werden, werden hier Disposed

		if (atlasDefaultIsNeverUsed && atlasDefault != null)
		{
			atlasDefault.dispose();
			atlasDefault = null;
		}

		if (atlasDefaultNightIsNeverUsed && atlasDefaultNight != null)
		{
			atlasDefaultNight.dispose();
			atlasDefaultNight = null;
		}

		if (atlasCostumIsNeverUsed && atlasCostum != null)
		{
			atlasCostum.dispose();
			atlasCostum = null;
		}

		if (atlasCostumtNightIsNeverUsed && atlasCostumtNight != null)
		{
			atlasCostumtNight.dispose();
			atlasCostumtNight = null;
		}

	}

}
