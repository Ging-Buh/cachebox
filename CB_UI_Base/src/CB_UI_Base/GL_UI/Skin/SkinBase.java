package CB_UI_Base.GL_UI.Skin;

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
import CB_Utils.Log.Log; import org.slf4j.LoggerFactory;

import CB_UI_Base.Global;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Implements all infos for a Skin like Font-Path Ui_iconPack Path ....
 * 
 * @author Longri
 */
public abstract class SkinBase {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(SkinBase.class);

	public static SkinBase that;
	private static Skin night_skin;
	private static Skin day_skin;
	private static Skin default_night_skin;
	private static Skin default_day_skin;

	protected static SkinSettings settings;

	public static Skin getDefaultDaySkin() {
		if (default_day_skin == null)
			initialSkin();
		return default_day_skin;
	}

	public static Skin getDefaultNightSkin() {
		if (default_night_skin == null)
			initialSkin();
		return default_night_skin;
	}

	public static Skin getDaySkin() {
		if (day_skin == null)
			initialSkin();
		return day_skin;
	}

	public static Skin getNightSkin() {
		if (night_skin == null)
			initialSkin();
		return night_skin;
	}

	public SkinSettings getSettings() {
		return settings;
	}

	protected SkinBase() {
	}

	public static HSV_Color getThemedColor(String Name) {
		if (night_skin == null || day_skin == null)
			initialSkin();
		if (CB_UI_Base_Settings.nightMode.getValue()) {
			return new HSV_Color(night_skin.getColor(Name));
		} else {
			return new HSV_Color(day_skin.getColor(Name));
		}

	}

	private static void initialSkin() {
		if (default_day_skin == null) {
			FileHandle default_day_skinPath = Global.getInternalFileHandle("skins/default/day/skin.json");
			default_day_skin = new Skin(default_day_skinPath);
		}

		if (default_night_skin == null) {
			FileHandle default_night_skinPath = Global.getInternalFileHandle("skins/default/night/skin.json");
			default_night_skin = new Skin(default_night_skinPath);
		}

		if (day_skin == null) {
			try {
				String day_skinPath = settings.SkinFolder + "/day/skin.json";
				if (settings.SkinFolder.type() == FileType.Absolute) {
					day_skin = new Skin(Gdx.files.absolute(day_skinPath));
				} else {
					day_skin = new Skin(Gdx.files.internal(day_skinPath));
				}
			} catch (Exception e) {
				Log.err(log, "Load Custum Skin", e);
			}
		}

		if (night_skin == null) {
			try {
				String night_skinPath = settings.SkinFolder + "/night/skin.json";
				if (settings.SkinFolder.type() == FileType.Absolute) {
					night_skin = new Skin(Gdx.files.absolute(night_skinPath));
				} else {
					night_skin = new Skin(Gdx.files.internal(night_skinPath));
				}
			} catch (Exception e) {
				Log.err(log, "Load Custum Night Skin", e);
			}
		}

		if (day_skin == null)
			day_skin = default_day_skin;
		if (night_skin == null)
			night_skin = default_night_skin;
	}
}
