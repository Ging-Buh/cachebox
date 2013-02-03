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
import CB_Core.GL_UI.utils.ColorDrawable;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
	public static ArrayList<Drawable> Compass = null;

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

	private static TextureAtlas atlasDefault;
	private static TextureAtlas atlasDefaultNight;
	private static TextureAtlas atlasCustom;
	private static TextureAtlas atlasCustomtNight;

	public static ArrayList<Sprite> LogIcons;

	public static Drawable activityBackground;
	public static Drawable activityBorderMask;
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

	public static Drawable radioOn;
	public static Drawable radioBack;

	public static Drawable selection;
	public static Drawable selection_set;
	public static Drawable selection_left;
	public static Drawable selection_right;
	public static Drawable textFieldCursor;
	public static Drawable textFiledBackground;
	public static Drawable textFiledBackgroundFocus;
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

	public static String PathDefaultAtlas;
	public static String PathCustomAtlas;
	public static String PathDefaultNightAtlas;
	public static String PathCustomNightAtlas;

	private static void setPath(String path)
	{

		if (Gdx.gl11 != null)
		{
			Gdx.gl11.glFlush();
			Gdx.gl11.glFinish();
		}

		String TexturName = Config.settings.useMipMap.getValue() ? "UI_IconPack_MipMap.spp" : "UI_IconPack.spp";

		GlobalCore.PathCustom = path + "/day/";
		GlobalCore.PathCustomNight = path + "/night/";
		PathCustomAtlas = path + "/day/" + TexturName;
		PathCustomNightAtlas = path + "/night/" + TexturName;

		String defaultPath = path;
		int pos = defaultPath.lastIndexOf("/");
		if (pos == -1) pos = defaultPath.lastIndexOf("\\");
		if (GlobalCore.useSmallSkin)
		{
			defaultPath = defaultPath.substring(0, pos) + "/small";
			GlobalCore.PathCustom = "";
			GlobalCore.PathCustomNight = "";
			PathCustomAtlas = "";
			PathCustomNightAtlas = "";
		}
		else
		{
			defaultPath = defaultPath.substring(0, pos) + "/default";
		}

		GlobalCore.PathDefault = defaultPath + "/day/";
		GlobalCore.PathDefaultNight = defaultPath + "/night/";
		PathDefaultAtlas = defaultPath + "/day/" + TexturName;
		PathDefaultNightAtlas = defaultPath + "/night/" + TexturName;

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

		if (atlasCustom != null)
		{
			atlasCustom.dispose();
			atlasCustom = null;
		}

		if (atlasCustomtNight != null)
		{
			atlasCustomtNight.dispose();
			atlasCustomtNight = null;
		}

		if (FileIO.FileExists(PathDefaultAtlas)) atlasDefault = new TextureAtlas(Gdx.files.absolute(PathDefaultAtlas));
		if (FileIO.FileExists(PathDefaultNightAtlas)) atlasDefaultNight = new TextureAtlas(Gdx.files.absolute(PathDefaultNightAtlas));

		if (!PathDefaultAtlas.equals(PathCustomAtlas))
		{
			if (FileIO.FileExists(PathCustomAtlas)) atlasCustom = new TextureAtlas(Gdx.files.absolute(PathCustomAtlas));
			if (FileIO.FileExists(PathCustomNightAtlas)) atlasCustomtNight = new TextureAtlas(Gdx.files.absolute(PathCustomNightAtlas));
		}
	}

	public static Sprite getThemedSprite(String name)
	{
		return getThemedSprite(name, 1.0f);
	}

	public static Skin night_skin;
	public static Skin day_skin;

	public static Color getThemedColor(String Name)
	{
		String path = Config.settings.SkinFolder.getValue();

		if (day_skin == null)
		{
			String day_skinPath = path + "/day/skin.json";
			day_skin = new Skin(Gdx.files.absolute(day_skinPath));
		}
		if (night_skin == null)
		{
			String night_skinPath = path + "/night/skin.json";
			night_skin = new Skin(Gdx.files.absolute(night_skinPath));
		}
		if (Config.settings.nightMode.getValue())
		{
			return night_skin.getColor(Name);
		}
		else
		{
			return day_skin.getColor(Name);
		}

	}

	public static Sprite getThemedSprite(String name, float scale)
	{
		Sprite tmp = null;
		if (Config.settings.nightMode.getValue())
		{
			tmp = createSprite(atlasCustomtNight, name);
			if (tmp == null)
			{
				tmp = createSprite(atlasCustom, name);
				if (tmp != null) tmp = setNightColorMatrixSprite(name, atlasCustom);
			}

			if (tmp == null)
			{
				tmp = createSprite(atlasDefaultNight, name);

				if (tmp == null)
				{
					tmp = setNightColorMatrixSprite(name, atlasDefault);
				}

			}

		}
		else
		{
			tmp = createSprite(atlasCustom, name);
		}

		if (tmp == null)
		{
			tmp = createSprite(atlasDefault, name);
		}

		if (tmp != null) tmp.setScale(scale);

		return tmp;
	}

	public static NinePatch getThemedPatch(String name)
	{
		NinePatch tmp = null;
		if (Config.settings.nightMode.getValue())
		{
			tmp = createPatch(atlasCustomtNight, name);
			if (tmp == null)
			{
				tmp = createPatch(atlasCustom, name);
				if (tmp != null) tmp = setNightColorMatrixPatch(name, atlasCustom);
			}

			if (tmp == null)
			{
				tmp = createPatch(atlasDefaultNight, name);

				if (tmp == null)
				{
					tmp = setNightColorMatrixPatch(name, atlasDefault);
				}

			}

		}
		else
		{
			tmp = createPatch(atlasCustom, name);
		}

		if (tmp == null)
		{
			tmp = createPatch(atlasDefault, name);
		}

		return tmp;
	}

	private static Sprite setNightColorMatrixSprite(String name, TextureAtlas atlas)
	{
		Sprite tmp = null;
		tmp = createSprite(atlas, name);
		if (tmp == null) return null;

		Color colorOverlay = new Color(0.5f, 0.4f, 0.4f, 1f);
		tmp.setColor(colorOverlay);

		return tmp;
	}

	private static NinePatch setNightColorMatrixPatch(String name, TextureAtlas atlas)
	{
		NinePatch tmp = null;
		tmp = createPatch(atlas, name);
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

	private static NinePatch createPatch(TextureAtlas atlas, String name)
	{
		NinePatch tmp = null;
		if (atlas != null)
		{
			tmp = atlas.createPatch(name);
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

		if (Compass == null) Compass = new ArrayList<Drawable>();
		synchronized (Compass)
		{
			Compass.clear();
			Compass.add(new SpriteDrawable(getThemedSprite("compass-frame")));
			Compass.add(new SpriteDrawable(getThemedSprite("compass-scale")));
			Compass.add(new SpriteDrawable(getThemedSprite("compass-frame-small")));
			Compass.add(new SpriteDrawable(getThemedSprite("compass-scale-small")));
			Compass.add(new SpriteDrawable(getThemedSprite("compass_arrow")));
			Compass.add(new SpriteDrawable(getThemedSprite("sonne")));
			Compass.add(new SpriteDrawable(getThemedSprite("mond")));

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
			MapIcons.add(getThemedSprite("23"));
			MapIcons.add(getThemedSprite("24"));
			MapIcons.add(getThemedSprite("25"));

		}

		if (Arrows == null) Arrows = new ArrayList<Sprite>();
		synchronized (Arrows)
		{

			float scale = UiSizes.getScale();

			Arrows.clear();
			Arrows.add(getThemedSprite("arrow-Compass")); // 0
			Arrows.add(getThemedSprite("arrow-Compass-Trans")); // 1
			Arrows.add(getThemedSprite("arrow-GPS")); // 2
			Arrows.add(getThemedSprite("arrow-GPS-Trans")); // 3
			Arrows.add(getThemedSprite("target-arrow")); // 4
			Arrows.add(getThemedSprite("track-line", scale)); // 5
			Arrows.add(getThemedSprite("arrow-down")); // 6
			Arrows.add(getThemedSprite("arrow-up")); // 7
			Arrows.add(getThemedSprite("arrow-left")); // 8
			Arrows.add(getThemedSprite("arrow-right")); // 9
			Arrows.add(getThemedSprite("track-point", scale)); // 10
			Arrows.add(getThemedSprite("ambilwarna-arrow-right")); // 11
			Arrows.add(getThemedSprite("ambilwarna-arrow-down")); // 12
			Arrows.add(getThemedSprite("draw-line", scale)); // 13
			Arrows.add(getThemedSprite("draw-point", scale)); // 14
			Arrows.add(getThemedSprite("arrow-Compass-car")); // 15

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
			Dialog.add(getThemedSprite("menu-divider"));

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
			BigIcons.add(getThemedSprite("big22icon")); // 22
			BigIcons.add(getThemedSprite("big23icon")); // 23
			BigIcons.add(getThemedSprite("big24icon")); // 24

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
			Icons.add(getThemedSprite("log11icon")); // 45
			Icons.add(getThemedSprite("navigate")); // 46
			Icons.add(getThemedSprite("log10icon")); // 47
			Icons.add(getThemedSprite("d-n")); // 48
			Icons.add(getThemedSprite("cb")); // 49
			Icons.add(getThemedSprite("userdata")); // 50
			Icons.add(getThemedSprite("day-spinner")); // 51
			Icons.add(getThemedSprite("add-icon")); // 52
			Icons.add(getThemedSprite("target-day")); // 53
			Icons.add(getThemedSprite("FieldNote")); // 54
			Icons.add(getThemedSprite("fieldnote-list-icon")); // 55
			Icons.add(getThemedSprite("waypoint-list-icon")); // 56

		}

		MapScale = new Drawable[3];
		MapScale[0] = new SpriteDrawable(getThemedSprite("MapScale-3"));
		MapScale[1] = new SpriteDrawable(getThemedSprite("MapScale-4"));
		MapScale[2] = new SpriteDrawable(getThemedSprite("MapScale-5"));

		Accuracy = new Sprite[3];
		Accuracy[0] = getThemedSprite("Accuracy-0");
		Accuracy[1] = getThemedSprite("Accuracy-1");
		Accuracy[2] = getThemedSprite("Accuracy-2");

		loadButtnSprites();

		createDrawables();

	}

	private static void createDrawables()
	{
		patch = (SpriteCache.getThemedSprite("activity-back").getWidth() > 60) ? 16 : 8;

		activityBackground = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("activity-back"), patch, patch, patch, patch));
		activityBorderMask = new NinePatchDrawable(
				new NinePatch(SpriteCache.getThemedSprite("activity-border"), patch, patch, patch, patch));
		ListBack = new ColorDrawable(getThemedColor("background"));
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

		radioOn = new SpriteDrawable(getThemedSprite("RadioButtonSet"));
		radioBack = new SpriteDrawable(getThemedSprite("RadioButtonBack"));

		textFiledBackground = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("text-field-back"), patch, patch, patch,
				patch));
		textFiledBackgroundFocus = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("text-field-back-focus"), patch, patch,
				patch, patch));

		selection = new SpriteDrawable(getThemedSprite("Selection"));
		selection_set = new SpriteDrawable(getThemedSprite("Selection-set"));
		selection_left = new SpriteDrawable(getThemedSprite("Selection-Left"));
		selection_right = new SpriteDrawable(getThemedSprite("Selection-Right"));
		logo = new SpriteDrawable(getThemedSprite("cachebox-logo"));

		copy = new SpriteDrawable(getThemedSprite("tf-copy"));
		paste = new SpriteDrawable(getThemedSprite("tf-paste"));
		cut = new SpriteDrawable(getThemedSprite("tf-cut"));

		bar = new NinePatchDrawable(getThemedPatch("bar"));
		barSmall = new NinePatchDrawable(getThemedPatch("bar-small"));

		bar_0 = new SpriteDrawable(getThemedSprite("bar-0"));
		barSmall_0 = new SpriteDrawable(getThemedSprite("bar-small-0"));

		textFieldCursor = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2));

		int hp = patch / 2;
		shaddowRec = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("shaddowrect"), hp, hp, hp, hp));

	}

	public static SpriteDrawable getSpriteDrawable(String name)
	{
		return new SpriteDrawable(getThemedSprite(name));
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

		if (atlasCustom != null)
		{
			atlasCustom.dispose();
			atlasCustom = null;
		}

		if (atlasCustomtNight != null)
		{
			atlasCustomtNight.dispose();
			atlasCustomtNight = null;
		}
	}

}
