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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Enthält die geladenen Sprites und das Handling für Laden und Entladen.
 * 
 * @author Longri
 */
public class SpriteCache
{
	public static ArrayList<Sprite> MapIconsSmall = null;
	public static ArrayList<Sprite> MapOverlay = null;
	public static ArrayList<Sprite> MapIcons = null;
	public static ArrayList<Sprite> Arrows = null;
	public static ArrayList<Sprite> MapStars = null;
	public static ArrayList<Sprite> Stars = null;
	public static ArrayList<Sprite> Bubble = null;
	public static Sprite InfoBack = null;
	public static ArrayList<Sprite> ToggleBtn = null;
	public static ArrayList<Sprite> ZoomBtn = null;
	public static Sprite ZoomValueBack = null;
	public static ArrayList<Sprite> BigIcons = null;
	public static ArrayList<Sprite> Icons = null;
	public static ArrayList<Sprite> ChkIcons = null;
	public static ArrayList<Sprite> Dialog = null;
	public static ArrayList<Sprite> SizesIcons = null;
	public static NinePatch ListBack = null;
	public static Sprite ButtonBack = null;
	public static Sprite AboutBack = null;
	public static Sprite Progress = null;

	public static ButtonSprites CacheList;
	public static ButtonSprites Cache;
	public static ButtonSprites Nav;
	public static ButtonSprites Tool;
	public static ButtonSprites Misc;
	public static ButtonSprites QuickButton;

	private static String PathDefault;
	private static String PathCostum;
	private static String PathDefaultNight;
	private static String PathCostumNight;

	private static void setPath(String path)
	{
		PathCostum = path + "/day/UI_IconPack.spp";
		PathCostumNight = path + "/night/UI_IconPack.spp";

		String defaultPath = path;
		int pos = defaultPath.lastIndexOf("/");
		defaultPath = defaultPath.substring(0, pos) + "/default";

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
		if (FileIO.FileExists(PathCostum)) atlasCostum = new TextureAtlas(Gdx.files.absolute(PathCostum));
		if (FileIO.FileExists(PathCostumNight)) atlasCostumtNight = new TextureAtlas(Gdx.files.absolute(PathCostumNight));

	}

	static TextureAtlas atlasDefault;
	static TextureAtlas atlasDefaultNight;
	static TextureAtlas atlasCostum;
	static TextureAtlas atlasCostumtNight;

	public static Sprite getThemedSprite(String name)
	{
		Sprite tmp = null;
		if (Config.settings.nightMode.getValue())
		{
			tmp = createSprite(atlasCostumtNight, name);
			if (tmp == null)
			{
				tmp = createSprite(atlasCostum, name);
			}
			if (tmp == null)
			{
				tmp = createSprite(atlasDefaultNight, name);
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

		if (MapIconsSmall == null) MapIconsSmall = new ArrayList<Sprite>();
		else
			MapIconsSmall.clear();
		MapIconsSmall.add(getThemedSprite("small1yes"));
		MapIconsSmall.add(getThemedSprite("small2yesyes"));
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
		MapIconsSmall.add(getThemedSprite("small5solved_no"));
		MapIconsSmall.add(getThemedSprite("small6no"));
		MapIconsSmall.add(getThemedSprite("small7no"));

		if (MapOverlay == null) MapOverlay = new ArrayList<Sprite>();
		else
			MapOverlay.clear();
		MapOverlay.add(getThemedSprite("shaddowrect"));
		MapOverlay.add(getThemedSprite("shaddowrect_selected"));
		MapOverlay.add(getThemedSprite("deact"));
		MapOverlay.add(getThemedSprite("cross"));

		if (MapIcons == null) MapIcons = new ArrayList<Sprite>();
		else
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

		if (Arrows == null) Arrows = new ArrayList<Sprite>();
		else
			Arrows.clear();
		Arrows.add(getThemedSprite("arrow_Compass"));
		Arrows.add(getThemedSprite("arrow_Compass_Trans"));
		Arrows.add(getThemedSprite("arrow_GPS"));
		Arrows.add(getThemedSprite("arrow_GPS_Trans"));
		Arrows.add(getThemedSprite("target_arrow"));
		Arrows.add(getThemedSprite("track_line"));
		Arrows.add(getThemedSprite("arrow_down"));
		Arrows.add(getThemedSprite("arrow_up"));
		Arrows.add(getThemedSprite("arrow_left"));
		Arrows.add(getThemedSprite("arrow_right"));
		Arrows.add(getThemedSprite("track_point"));

		if (MapStars == null) MapStars = new ArrayList<Sprite>();
		else
			MapStars.clear();
		MapStars.add(getThemedSprite("stars0small"));
		MapStars.add(getThemedSprite("stars0_5small"));
		MapStars.add(getThemedSprite("stars1small"));
		MapStars.add(getThemedSprite("stars1_5small"));
		MapStars.add(getThemedSprite("stars2small"));
		MapStars.add(getThemedSprite("stars2_5small"));
		MapStars.add(getThemedSprite("stars3small"));
		MapStars.add(getThemedSprite("stars3_5small"));
		MapStars.add(getThemedSprite("stars4small"));
		MapStars.add(getThemedSprite("stars4_5small"));
		MapStars.add(getThemedSprite("stars5small"));

		if (Stars == null) Stars = new ArrayList<Sprite>();
		else
			Stars.clear();
		Stars.add(getThemedSprite("stars0icon"));
		Stars.add(getThemedSprite("stars0_5icon"));
		Stars.add(getThemedSprite("stars1icon"));
		Stars.add(getThemedSprite("stars1_5icon"));
		Stars.add(getThemedSprite("stars2icon"));
		Stars.add(getThemedSprite("stars2_5icon"));
		Stars.add(getThemedSprite("stars3icon"));
		Stars.add(getThemedSprite("stars3_5icon"));
		Stars.add(getThemedSprite("stars4icon"));
		Stars.add(getThemedSprite("stars4_5icon"));
		Stars.add(getThemedSprite("stars5icon"));

		if (Bubble == null) Bubble = new ArrayList<Sprite>();
		else
			Bubble.clear();
		Bubble.add(getThemedSprite("Bubble"));
		Bubble.add(getThemedSprite("Bubble_selected"));
		Bubble.add(getThemedSprite("BubbleOverlay"));
		Bubble.add(getThemedSprite("1to4bubble"));
		Bubble.add(getThemedSprite("5bubble"));
		Bubble.add(getThemedSprite("1to4bubble_flip"));
		Bubble.add(getThemedSprite("5bubble_flip"));

		if (ChkIcons == null) ChkIcons = new ArrayList<Sprite>();
		else
			ChkIcons.clear();
		ChkIcons.add(getThemedSprite("check_off"));
		ChkIcons.add(getThemedSprite("check_on"));

		if (Dialog == null) Dialog = new ArrayList<Sprite>();
		else
			Dialog.clear();
		Dialog.add(getThemedSprite("dialog_header"));
		Dialog.add(getThemedSprite("dialog_center"));
		Dialog.add(getThemedSprite("dialog_footer"));
		Dialog.add(getThemedSprite("dialog_title"));

		InfoBack = getThemedSprite("InfoPanelBack");

		if (ToggleBtn == null) ToggleBtn = new ArrayList<Sprite>();
		else
			ToggleBtn.clear();
		ToggleBtn.add(getThemedSprite("btn_normal"));
		ToggleBtn.add(getThemedSprite("btn_pressed"));
		ToggleBtn.add(getThemedSprite("toggle_led_gr"));

		Progress = getThemedSprite("progress");

		if (ZoomBtn == null) ZoomBtn = new ArrayList<Sprite>();
		else
			ZoomBtn.clear();
		ZoomBtn.add(getThemedSprite("day_btn_zoom_down_normal"));
		ZoomBtn.add(getThemedSprite("day_btn_zoom_down_pressed"));
		ZoomBtn.add(getThemedSprite("day_btn_zoom_down_disabled"));
		ZoomBtn.add(getThemedSprite("day_btn_zoom_up_normal"));
		ZoomBtn.add(getThemedSprite("day_btn_zoom_up_pressed"));
		ZoomBtn.add(getThemedSprite("day_btn_zoom_up_disabled"));

		ZoomValueBack = getThemedSprite("zoom_back");

		if (SizesIcons == null) SizesIcons = new ArrayList<Sprite>();
		else
			SizesIcons.clear();
		SizesIcons.add(getThemedSprite("other"));
		SizesIcons.add(getThemedSprite("micro"));
		SizesIcons.add(getThemedSprite("small"));
		SizesIcons.add(getThemedSprite("regular"));
		SizesIcons.add(getThemedSprite("large"));

		if (BigIcons == null) BigIcons = new ArrayList<Sprite>();
		else
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
		BigIcons.add(getThemedSprite("my_parking")); // 20
		BigIcons.add(getThemedSprite("big19icon")); // 21

		if (Icons == null) Icons = new ArrayList<Sprite>();
		else
			Icons.clear();
		Icons.add(getThemedSprite("btn_default_normal"));// 0
		Icons.add(getThemedSprite("button"));// 1
		Icons.add(getThemedSprite("doc_icon"));// 2
		Icons.add(getThemedSprite("big_16"));// 3
		Icons.add(getThemedSprite("list_icon")); // 4 LogView braucht noch ein Icon
		Icons.add(getThemedSprite("map")); // 5
		Icons.add(getThemedSprite("compass"));// 6
		Icons.add(getThemedSprite("cache_list_icon"));// 7
		Icons.add(getThemedSprite("track_list_icon")); // 8
		Icons.add(getThemedSprite("log10"));// 9
		Icons.add(getThemedSprite("video_icon")); // 10
		Icons.add(getThemedSprite("voice_rec_icon"));// 11
		Icons.add(getThemedSprite("lupe")); // 12
		Icons.add(getThemedSprite("filter")); // 13
		Icons.add(getThemedSprite("lock_icon"));// 14
		Icons.add(getThemedSprite("auto_sort_on_icon")); // 15
		Icons.add(getThemedSprite("auto_sort_off_icon")); // 16
		Icons.add(getThemedSprite("solver_icon")); // 17
		Icons.add(getThemedSprite("images_icon")); // 18
		Icons.add(getThemedSprite("hint_icon")); // 19
		Icons.add(getThemedSprite("doc_icon")); // 20
		Icons.add(getThemedSprite("list_icon")); // 21
		Icons.add(getThemedSprite("images_icon")); // 22
		Icons.add(getThemedSprite("note_icon")); // 23
		Icons.add(getThemedSprite("solver_icon")); // 24
		Icons.add(getThemedSprite("joker_phone")); // 25
		Icons.add(getThemedSprite("settings")); // 26
		Icons.add(getThemedSprite("lupe")); // 27
		Icons.add(getThemedSprite("delete_icon")); // 28
		Icons.add(getThemedSprite("voice_rec_icon")); // 29
		Icons.add(getThemedSprite("satellite")); // 30
		Icons.add(getThemedSprite("close_icon")); // 31
		Icons.add(getThemedSprite("info_icon")); // 32
		Icons.add(getThemedSprite("warning_icon")); // 33
		Icons.add(getThemedSprite("help_icon")); // 34
		Icons.add(getThemedSprite("day_gc_live_icon")); // 35
		Icons.add(getThemedSprite("tb")); // 36
		Icons.add(getThemedSprite("cm_icon")); // 37
		Icons.add(getThemedSprite("tb_list_icon")); // 38
		Icons.add(getThemedSprite("sort_icon")); // 39
		Icons.add(getThemedSprite("import")); // 40
		Icons.add(getThemedSprite("manage_db")); // 41
		Icons.add(getThemedSprite("favorit")); // 42
		Icons.add(getThemedSprite("star")); // 43
		Icons.add(getThemedSprite("disabled")); // 44
		Icons.add(getThemedSprite("not_available")); // 45
		Icons.add(getThemedSprite("navigate")); // 46
		Icons.add(getThemedSprite("log10icon")); // 47
		Icons.add(getThemedSprite("d_n")); // 48
		Icons.add(getThemedSprite("cb")); // 49
		Icons.add(getThemedSprite("userdata")); // 50
		loadButtnSprites();

		ListBack = new NinePatch(getThemedSprite("background"), 1, 1, 1, 1);
		ButtonBack = getThemedSprite("button_list_back");
		AboutBack = getThemedSprite("splash_back");
	}

	private static void loadButtnSprites()
	{
		CacheList = new ButtonSprites(getThemedSprite("db"), getThemedSprite("db_pressed"));
		Cache = new ButtonSprites(getThemedSprite("cache"), getThemedSprite("cache_pressed"));
		Nav = new ButtonSprites(getThemedSprite("Nav"), getThemedSprite("Nav_pressed"));
		Tool = new ButtonSprites(getThemedSprite("tool"), getThemedSprite("tool_pressed"));
		Misc = new ButtonSprites(getThemedSprite("misc"), getThemedSprite("misc_pressed"));
		QuickButton = new ButtonSprites(getThemedSprite("button"), getThemedSprite("btn_pressed"));
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

		CacheList.dispose();
		Cache.dispose();
		Nav.dispose();
		Tool.dispose();
		Misc.dispose();

		CacheList = null;
		Cache = null;
		Nav = null;
		Tool = null;
		Misc = null;
		QuickButton = null;
	}

}
