package CB_UI.settings;

import CB_UI.Global;
import CB_Utils.Config_Core;
import CB_Utils.Settings.Audio;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingFloat;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingsAudio;
import CB_Utils.Settings.SettingsList;

public interface CB_UI_Base_Settings
{
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingBool nightMode = (SettingBool) SettingsList.addSetting(new SettingBool("nightMode",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));

	public static final SettingFolder SkinFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("SkinFolder",
			SettingCategory.Folder, INVISIBLE, Config_Core.WorkPath + "/skins/default", SettingStoreType.Global));

	public static final SettingInt FONT_SIZE_COMPASS_DISTANCE = (SettingInt) SettingsList.addSetting(new SettingInt(
			"FONT_SIZE_COMPASS_DISTANCE", SettingCategory.Skin, EXPERT, 27, SettingStoreType.Global));
	public static final SettingInt FONT_SIZE_BIG = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_BIG",
			SettingCategory.Skin, EXPERT, 18, SettingStoreType.Global));
	public static final SettingInt FONT_SIZE_NORMAL = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_NORMAL",
			SettingCategory.Skin, EXPERT, 15, SettingStoreType.Global));
	public static final SettingInt FONT_SIZE_NORMAL_BUBBLE = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_NORMAL_BUBBLE",
			SettingCategory.Skin, EXPERT, 14, SettingStoreType.Global));
	public static final SettingInt FONT_SIZE_SMALL = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_SMALL",
			SettingCategory.Skin, EXPERT, 13, SettingStoreType.Global));
	public static final SettingInt FONT_SIZE_SMALL_BUBBLE = (SettingInt) SettingsList.addSetting(new SettingInt("FONT_SIZE_SMALL_BUBBLE",
			SettingCategory.Skin, EXPERT, 11, SettingStoreType.Global));

	public static final SettingBool useMipMap = (SettingBool) SettingsList.addSetting(new SettingBool("useMipMap", SettingCategory.Skin,
			NORMAL, false, SettingStoreType.Global));

	public static final SettingFloat MapViewDPIFaktor = (SettingFloat) SettingsList.addSetting(new SettingFloat("MapViewDPIFaktor",
			SettingCategory.Map, EXPERT, (float) Global.displayDensity, SettingStoreType.Global));

	public static final SettingDouble MapViewFontFaktor = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapViewFontFaktor",
			SettingCategory.Map, NEVER, 1.0, SettingStoreType.Global));

	public static final SettingFolder TileCacheFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("TileCacheFolder",
			SettingCategory.Folder, NORMAL, Config_Core.WorkPath + "/repository/cache", SettingStoreType.Global));

	public static final SettingInt LongClicktime = (SettingInt) SettingsList.addSetting(new SettingInt("LongClicktime",
			SettingCategory.Misc, NORMAL, 600, SettingStoreType.Global));

	public static final SettingBool DebugSpriteBatchCountBuffer = (SettingBool) SettingsList.addSetting(new SettingBool(
			"DebugSpriteBatchCountBuffer", SettingCategory.Debug, EXPERT, false, SettingStoreType.Global));

	public static final SettingBool DebugMode = (SettingBool) SettingsList.addSetting(new SettingBool("DebugMode", SettingCategory.Debug,
			EXPERT, false, SettingStoreType.Global));

	public static final SettingsAudio GlobalVolume = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("GlobalVolume",
			SettingCategory.Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global));

}
