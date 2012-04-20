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
	public static ArrayList<Sprite> MapArrows = null;
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

	public static ButtonSprites CacheList;
	public static ButtonSprites Cache;
	public static ButtonSprites Nav;
	public static ButtonSprites Tool;
	public static ButtonSprites Misc;

	// UI Atlas
	public static TextureAtlas uiAtlas;
	public static TextureAtlas iconAtlas;

	/**
	 * Load the Sprites from recorce
	 */
	public static void LoadSprites()
	{
		TextureAtlas atlas;
		// atlas = new TextureAtlas(Gdx.files.internal("data/pack"));
		// uiAtlas = new TextureAtlas(Gdx.files.internal("9patch/UI_PackerSheet"));

		String skinPath = Config.settings.SkinFolder.getValue();

		atlas = new TextureAtlas(Gdx.files.absolute(skinPath + "/day/MapSpritePack.spp"));
		uiAtlas = new TextureAtlas(Gdx.files.absolute(skinPath + "/day/UI_SpritePack.spp"));
		iconAtlas = new TextureAtlas(Gdx.files.absolute(skinPath + "/day/UI_IconPack.spp"));

		MapIconsSmall = new ArrayList<Sprite>();
		MapIconsSmall.add(atlas.createSprite("small1yes"));
		MapIconsSmall.add(atlas.createSprite("small2yesyes"));
		MapIconsSmall.add(atlas.createSprite("small3yes"));
		MapIconsSmall.add(atlas.createSprite("small4yes"));
		MapIconsSmall.add(atlas.createSprite("small5yes"));
		MapIconsSmall.add(atlas.createSprite("small5solved"));
		MapIconsSmall.add(atlas.createSprite("small6yes"));
		MapIconsSmall.add(atlas.createSprite("small7yes"));
		MapIconsSmall.add(atlas.createSprite("small1no"));
		MapIconsSmall.add(atlas.createSprite("small2no"));
		MapIconsSmall.add(atlas.createSprite("small3no"));
		MapIconsSmall.add(atlas.createSprite("small4no"));
		MapIconsSmall.add(atlas.createSprite("small5no"));
		MapIconsSmall.add(atlas.createSprite("small5solved_no"));
		MapIconsSmall.add(atlas.createSprite("small6no"));
		MapIconsSmall.add(atlas.createSprite("small7no"));

		MapOverlay = new ArrayList<Sprite>();
		MapOverlay.add(atlas.createSprite("shaddowrect"));
		MapOverlay.add(atlas.createSprite("shaddowrect_selected"));
		MapOverlay.add(atlas.createSprite("deact"));
		MapOverlay.add(atlas.createSprite("cross"));

		MapIcons = new ArrayList<Sprite>();
		MapIcons.add(atlas.createSprite("0"));
		MapIcons.add(atlas.createSprite("1"));
		MapIcons.add(atlas.createSprite("2"));
		MapIcons.add(atlas.createSprite("3"));
		MapIcons.add(atlas.createSprite("4"));
		MapIcons.add(atlas.createSprite("5"));
		MapIcons.add(atlas.createSprite("6"));
		MapIcons.add(atlas.createSprite("7"));
		MapIcons.add(atlas.createSprite("8"));
		MapIcons.add(atlas.createSprite("9"));
		MapIcons.add(atlas.createSprite("10"));
		MapIcons.add(atlas.createSprite("11"));
		MapIcons.add(atlas.createSprite("12"));
		MapIcons.add(atlas.createSprite("13"));
		MapIcons.add(atlas.createSprite("14"));
		MapIcons.add(atlas.createSprite("15"));
		MapIcons.add(atlas.createSprite("16"));
		MapIcons.add(atlas.createSprite("17"));
		MapIcons.add(atlas.createSprite("18"));
		MapIcons.add(atlas.createSprite("19"));
		MapIcons.add(atlas.createSprite("20"));
		MapIcons.add(atlas.createSprite("21"));

		MapArrows = new ArrayList<Sprite>();
		MapArrows.add(uiAtlas.createSprite("arrow_Compass"));
		MapArrows.add(uiAtlas.createSprite("arrow_Compass_Trans"));
		MapArrows.add(uiAtlas.createSprite("arrow_GPS"));
		MapArrows.add(uiAtlas.createSprite("arrow_GPS_Trans"));
		MapArrows.add(atlas.createSprite("target_arrow"));
		MapArrows.add(atlas.createSprite("track_line"));

		MapStars = new ArrayList<Sprite>();
		MapStars.add(atlas.createSprite("stars0small"));
		MapStars.add(atlas.createSprite("stars0_5small"));
		MapStars.add(atlas.createSprite("stars1small"));
		MapStars.add(atlas.createSprite("stars1_5small"));
		MapStars.add(atlas.createSprite("stars2small"));
		MapStars.add(atlas.createSprite("stars2_5small"));
		MapStars.add(atlas.createSprite("stars3small"));
		MapStars.add(atlas.createSprite("stars3_5small"));
		MapStars.add(atlas.createSprite("stars4small"));
		MapStars.add(atlas.createSprite("stars4_5small"));
		MapStars.add(atlas.createSprite("stars5small"));

		Stars = new ArrayList<Sprite>();
		Stars.add(iconAtlas.createSprite("stars0icon"));
		Stars.add(iconAtlas.createSprite("stars0_5icon"));
		Stars.add(iconAtlas.createSprite("stars1icon"));
		Stars.add(iconAtlas.createSprite("stars1_5icon"));
		Stars.add(iconAtlas.createSprite("stars2icon"));
		Stars.add(iconAtlas.createSprite("stars2_5icon"));
		Stars.add(iconAtlas.createSprite("stars3icon"));
		Stars.add(iconAtlas.createSprite("stars3_5icon"));
		Stars.add(iconAtlas.createSprite("stars4icon"));
		Stars.add(iconAtlas.createSprite("stars4_5icon"));
		Stars.add(iconAtlas.createSprite("stars5icon"));

		Bubble = new ArrayList<Sprite>();
		Bubble.add(uiAtlas.createSprite("Bubble"));
		Bubble.add(uiAtlas.createSprite("Bubble_selected"));
		Bubble.add(uiAtlas.createSprite("BubbleOverlay"));

		ChkIcons = new ArrayList<Sprite>();
		ChkIcons.add(uiAtlas.createSprite("check_off"));
		ChkIcons.add(uiAtlas.createSprite("check_on"));

		Dialog = new ArrayList<Sprite>();
		Dialog.add(uiAtlas.createSprite("dialog_header"));
		Dialog.add(uiAtlas.createSprite("dialog_center"));
		Dialog.add(uiAtlas.createSprite("dialog_footer"));
		Dialog.add(uiAtlas.createSprite("dialog_title"));

		InfoBack = uiAtlas.createSprite("InfoPanelBack");

		ToggleBtn = new ArrayList<Sprite>();
		ToggleBtn.add(uiAtlas.createSprite("day_btn_normal"));
		ToggleBtn.add(uiAtlas.createSprite("day_btn_pressed"));
		ToggleBtn.add(uiAtlas.createSprite("toggle_led_gr"));

		ZoomBtn = new ArrayList<Sprite>();
		ZoomBtn.add(uiAtlas.createSprite("day_btn_zoom_down_normal"));
		ZoomBtn.add(uiAtlas.createSprite("day_btn_zoom_down_pressed"));
		ZoomBtn.add(uiAtlas.createSprite("day_btn_zoom_down_disabled"));
		ZoomBtn.add(uiAtlas.createSprite("day_btn_zoom_up_normal"));
		ZoomBtn.add(uiAtlas.createSprite("day_btn_zoom_up_pressed"));
		ZoomBtn.add(uiAtlas.createSprite("day_btn_zoom_up_disabled"));

		ZoomValueBack = uiAtlas.createSprite("zoom_back");

		SizesIcons = new ArrayList<Sprite>();
		SizesIcons.add(atlas.createSprite("other"));
		SizesIcons.add(atlas.createSprite("micro"));
		SizesIcons.add(atlas.createSprite("small"));
		SizesIcons.add(atlas.createSprite("regular"));
		SizesIcons.add(atlas.createSprite("large"));

		BigIcons = new ArrayList<Sprite>();
		BigIcons.add(iconAtlas.createSprite("big0icon")); // 0
		BigIcons.add(iconAtlas.createSprite("big1icon")); // 1
		BigIcons.add(iconAtlas.createSprite("big2icon")); // 2
		BigIcons.add(iconAtlas.createSprite("big3icon")); // 3
		BigIcons.add(iconAtlas.createSprite("big4icon")); // 4
		BigIcons.add(iconAtlas.createSprite("big5icon")); // 5
		BigIcons.add(iconAtlas.createSprite("big6icon")); // 6
		BigIcons.add(iconAtlas.createSprite("big7icon")); // 7
		BigIcons.add(iconAtlas.createSprite("big8icon")); // 8
		BigIcons.add(iconAtlas.createSprite("big9icon")); // 9
		BigIcons.add(iconAtlas.createSprite("big10icon")); // 10
		BigIcons.add(iconAtlas.createSprite("big11icon")); // 11
		BigIcons.add(iconAtlas.createSprite("big12icon")); // 12
		BigIcons.add(iconAtlas.createSprite("big13icon")); // 13
		BigIcons.add(iconAtlas.createSprite("big14icon")); // 14
		BigIcons.add(iconAtlas.createSprite("big15icon")); // 15
		BigIcons.add(iconAtlas.createSprite("big16icon")); // 16
		BigIcons.add(iconAtlas.createSprite("big17icon")); // 17
		BigIcons.add(iconAtlas.createSprite("big18icon")); // 18
		BigIcons.add(iconAtlas.createSprite("log0icon")); // 19
		BigIcons.add(iconAtlas.createSprite("my_parking")); // 20
		BigIcons.add(iconAtlas.createSprite("big19icon")); // 21

		Icons = new ArrayList<Sprite>();
		Icons.add(iconAtlas.createSprite("day_btn_default_normal"));// 0
		Icons.add(iconAtlas.createSprite("button"));// 1
		Icons.add(iconAtlas.createSprite("doc_icon"));// 2
		Icons.add(iconAtlas.createSprite("big_16"));// 3
		Icons.add(iconAtlas.createSprite("list_icon")); // 4 LogView braucht noch ein Icon
		Icons.add(iconAtlas.createSprite("map")); // 5
		Icons.add(iconAtlas.createSprite("compass"));// 6
		Icons.add(iconAtlas.createSprite("cache_list_icon"));// 7
		Icons.add(iconAtlas.createSprite("track_list_icon")); // 8
		Icons.add(iconAtlas.createSprite("log10"));// 9
		Icons.add(iconAtlas.createSprite("video_icon")); // 10
		Icons.add(iconAtlas.createSprite("voice_rec_icon"));// 11
		Icons.add(iconAtlas.createSprite("lupe")); // 12
		Icons.add(iconAtlas.createSprite("filter")); // 13
		Icons.add(iconAtlas.createSprite("lock_icon"));// 14
		Icons.add(iconAtlas.createSprite("auto_sort_on_icon")); // 15
		Icons.add(iconAtlas.createSprite("auto_sort_off_icon")); // 16
		Icons.add(iconAtlas.createSprite("solver_icon")); // 17
		Icons.add(iconAtlas.createSprite("images_icon")); // 18
		Icons.add(iconAtlas.createSprite("hint_icon")); // 19
		Icons.add(iconAtlas.createSprite("doc_icon")); // 20
		Icons.add(iconAtlas.createSprite("list_icon")); // 21
		Icons.add(iconAtlas.createSprite("images_icon")); // 22
		Icons.add(iconAtlas.createSprite("note_icon")); // 23
		Icons.add(iconAtlas.createSprite("solver_icon")); // 24
		Icons.add(iconAtlas.createSprite("joker_phone")); // 25
		Icons.add(iconAtlas.createSprite("settings")); // 26
		Icons.add(iconAtlas.createSprite("lupe")); // 27
		Icons.add(iconAtlas.createSprite("delete_icon")); // 28
		Icons.add(iconAtlas.createSprite("voice_rec_icon")); // 29
		Icons.add(iconAtlas.createSprite("satellite")); // 30
		Icons.add(iconAtlas.createSprite("close_icon")); // 31
		Icons.add(iconAtlas.createSprite("info_icon")); // 32
		Icons.add(iconAtlas.createSprite("warning_icon")); // 33
		Icons.add(iconAtlas.createSprite("help_icon")); // 34
		Icons.add(iconAtlas.createSprite("day_gc_live_icon")); // 35
		Icons.add(iconAtlas.createSprite("tb")); // 36
		Icons.add(iconAtlas.createSprite("cm_icon")); // 37
		Icons.add(iconAtlas.createSprite("tb_list_icon")); // 38
		Icons.add(iconAtlas.createSprite("sort_icon")); // 39
		Icons.add(iconAtlas.createSprite("import")); // 40
		Icons.add(iconAtlas.createSprite("manage_db")); // 41
		Icons.add(iconAtlas.createSprite("favorit")); // 42
		Icons.add(iconAtlas.createSprite("star")); // 43
		Icons.add(iconAtlas.createSprite("disabled")); // 44
		Icons.add(iconAtlas.createSprite("not_available")); // 45
		Icons.add(iconAtlas.createSprite("navigate")); // 46
		loadButtnSprites();

		ListBack = new NinePatch(uiAtlas.createSprite("background"), 1, 1, 1, 1);
		ButtonBack = uiAtlas.createSprite("button_list_back");
		AboutBack = iconAtlas.createSprite("splash_back");
	}

	private static void loadButtnSprites()
	{
		CacheList = new ButtonSprites(uiAtlas, "db", "db_pressed");
		Cache = new ButtonSprites(uiAtlas, "cache", "cache_pressed");
		Nav = new ButtonSprites(uiAtlas, "Nav", "Nav_pressed");
		Tool = new ButtonSprites(uiAtlas, "tool", "tool_pressed");
		Misc = new ButtonSprites(uiAtlas, "misc", "misc_pressed");
	}

	/**
	 * Destroy cached sprites
	 */
	public static void destroyCache()
	{
		MapIconsSmall = null;
		MapOverlay = null;
		MapIcons = null;
		MapArrows = null;
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

	}

}
