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

	public Audio(Audio value)
	{
		mPath = value.mPath;
		mVolume = value.mVolume;
		mMute = value.mMute;
		mClass_Absolute = value.mClass_Absolute;
	}

	public String mPath;
	public float mVolume;
	public boolean mMute;
	public boolean mClass_Absolute;

	@Override
	public boolean equals(Object obj)
	{
		boolean ret = false;
		if (obj instanceof Audio)
		{
			ret = true;
			Audio aud = (Audio) obj;
			if (!mPath.equalsIgnoreCase(aud.mPath)) ret = false;
			if (mClass_Absolute != aud.mClass_Absolute) ret = false;
			if (mMute != aud.mMute) ret = false;
			if (mVolume != aud.mVolume) ret = false;
		}

		return ret;
	}
}
