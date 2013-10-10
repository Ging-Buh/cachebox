package CB_UI_Base;

import CB_UI_Base.GL_UI.DisplayType;
import CB_Utils.Config_Core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Clipboard;

public abstract class Global
{
	protected static Global Instance;

	public static Plattform platform = Plattform.undef;

	/**
	 * Wird im Splash gesetzt und ist True, wenn es sich um ein Tablet handelt!
	 */
	public static boolean isTab = false;
	public static boolean forceTab = false;
	public static boolean forcePhone = false;
	public static boolean useSmallSkin = false;
	public static DisplayType displayType = DisplayType.Normal;
	public static boolean posibleTabletLayout;
	public static final String br = System.getProperty("line.separator");
	public static final String fs = System.getProperty("file.separator");
	public static double displayDensity = 1;
	private static Clipboard defaultClipBoard;

	private static boolean isTestVersionCheked = false;
	private static boolean isTestVersion = false;

	protected abstract String getVersionPrefix();

	protected Global()
	{
		Instance = this;
	}

	public static FileHandle getInternalFileHandle(String path)
	{
		if (Global.platform == Plattform.undef) throw new IllegalArgumentException("Platform not def");

		if (Global.platform == Plattform.Android)
		{
			return Gdx.files.internal(path);
		}
		else
		{
			return Gdx.files.classpath(path);
		}
	}

	public static Clipboard getDefaultClipboard()
	{
		if (defaultClipBoard == null)
		{
			return null;
		}
		else
		{
			return defaultClipBoard;
		}
	}

	public static void setDefaultClipboard(Clipboard clipBoard)
	{
		defaultClipBoard = clipBoard;
	}

	public static boolean isTestVersion()
	{
		if (isTestVersionCheked) return isTestVersion;
		if (Instance == null) return false;

		isTestVersion = Instance.getVersionPrefix().contains("Test");
		isTestVersionCheked = true;
		return isTestVersion;
	}

	private static boolean mIsDevelop = false;
	private static boolean mIsDevelopChecked = false;

	public static boolean isDevelop()
	{
		if (mIsDevelopChecked) return mIsDevelop;

		// Chk is Develop
		mIsDevelop = Gdx.files.absolute(Config_Core.WorkPath + "/deve.lop").exists();
		mIsDevelopChecked = true;
		return mIsDevelop;
	}
}
