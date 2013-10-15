package CB_UI_Base.settings;

import CB_UI_Base.Global;
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

public interface CB_UI_Base_Settings
{
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingBool nightMode = new SettingBool("nightMode", SettingCategory.Internal, NEVER, false,
			SettingStoreType.Global);

	public static final SettingFolder SkinFolder = new SettingFolder("SkinFolder", SettingCategory.Folder, INVISIBLE, Config_Core.WorkPath
			+ "/skins/default", SettingStoreType.Global);

	public static final SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", SettingCategory.Skin, EXPERT,
			27, SettingStoreType.Global);
	public static final SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", SettingCategory.Skin, EXPERT, 18,
			SettingStoreType.Global);
	public static final SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", SettingCategory.Skin, EXPERT, 15,
			SettingStoreType.Global);
	public static final SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", SettingCategory.Skin, EXPERT, 14,
			SettingStoreType.Global);
	public static final SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", SettingCategory.Skin, EXPERT, 13,
			SettingStoreType.Global);
	public static final SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", SettingCategory.Skin, EXPERT, 11,
			SettingStoreType.Global);

	public static final SettingBool useMipMap = new SettingBool("useMipMap", SettingCategory.Skin, NORMAL, false, SettingStoreType.Global);

	public static final SettingDouble MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", SettingCategory.Map, NEVER, 1.0,
			SettingStoreType.Global);

	public static final SettingInt LongClicktime = new SettingInt("LongClicktime", SettingCategory.Misc, NORMAL, 600,
			SettingStoreType.Global);

	public static final SettingBool DebugSpriteBatchCountBuffer = new SettingBool("DebugSpriteBatchCountBuffer", SettingCategory.Debug,
			EXPERT, false, SettingStoreType.Global);

	public static final SettingBool DebugMode = new SettingBool("DebugMode", SettingCategory.Debug, EXPERT, false, SettingStoreType.Global);

	public static final SettingBool WriteLoggerDebugMode = new SettingBool("WriteLoggerDebugMode", SettingCategory.Debug, EXPERT, false,
			SettingStoreType.Global);

	public static final SettingsAudio GlobalVolume = new SettingsAudio("GlobalVolume", SettingCategory.Sounds, NORMAL, new Audio(
			"data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global);

	public static final SettingFloat MapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", SettingCategory.Map, EXPERT,
			(float) Global.displayDensity, SettingStoreType.Global);

	public static final SettingFolder ImageCacheFolderLocal = new SettingFolder("ImageCacheFolderLocal", SettingCategory.Folder, NEVER,
			Config_Core.WorkPath + "/repository/cache", SettingStoreType.Local);

	public static final SettingBool GestureOn = new SettingBool("GestureOn", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global);

}
