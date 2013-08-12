package CB_Core.Settings;

public class Audio
{
	protected Audio()
	{
		// dont Use
	}

	public Audio(String path, boolean absolute, boolean mute, float volume)
	{
		super();
		this.mPath = path;
		this.mClass_Absolute = absolute;
		this.mMute = mute;
		this.mVolume = volume;
	}

	public String mPath;
	public float mVolume;
	public boolean mMute;
	public boolean mClass_Absolute;
}
