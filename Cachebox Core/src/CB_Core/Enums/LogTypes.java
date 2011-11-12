package CB_Core.Enums;

public enum LogTypes
{
	found, // 0
	didnt_find, // 1
	note, // 2
	published, // 3
	enabled, // 4
	needs_maintenance, // 5
	temporarily_disabled, // 6
	owner_maintenance, // 7
	will_attend, // 8
	attended, // 9
	webcam_photo_taken, // 10
	archived, // 11
	reviewer_note, // 12
	needs_archived // 13
	;

	public static LogTypes parseString(String text)
	{
		if (text.equalsIgnoreCase("found it"))
		{
			return found;
		}
		if (text.equalsIgnoreCase("didn't find it"))
		{
			return didnt_find;
		}
		if (text.equalsIgnoreCase("not found"))
		{
			return didnt_find;
		}
		if (text.equalsIgnoreCase("write note"))
		{
			return note;
		}
		if (text.equalsIgnoreCase("publish listing"))
		{
			return published;
		}
		if (text.equalsIgnoreCase("enable listing"))
		{
			return enabled;
		}
		if (text.equalsIgnoreCase("needs maintenance"))
		{
			return needs_maintenance;
		}
		if (text.equalsIgnoreCase("temporarily disable listing"))
		{
			return temporarily_disabled;
		}
		if (text.equalsIgnoreCase("owner maintenance"))
		{
			return owner_maintenance;
		}
		if (text.equalsIgnoreCase("update coordinates"))
		{
			return owner_maintenance;
		}
		if (text.equalsIgnoreCase("will attend"))
		{
			return will_attend;
		}
		if (text.equalsIgnoreCase("attended"))
		{
			return attended;
		}
		if (text.equalsIgnoreCase("webcam photo taken"))
		{
			return webcam_photo_taken;
		}
		if (text.equalsIgnoreCase("archive"))
		{
			return archived;
		}
		if (text.equalsIgnoreCase("unarchive"))
		{
			return archived;
		}
		if (text.equalsIgnoreCase("post reviewer note"))
		{
			return reviewer_note;
		}
		if (text.equalsIgnoreCase("needs archived"))
		{
			return needs_archived;
		}
		if (text.equalsIgnoreCase("other"))
		{
			return note;
		}
		if (text.equalsIgnoreCase("note"))
		{
			return note;
		}
		if (text.equalsIgnoreCase("geocoins"))
		{
			return note;
		}
		if (text.equalsIgnoreCase("cache disabled!"))
		{
			return temporarily_disabled;
		}
		if (text.equalsIgnoreCase("retract listing"))
		{
			return archived;
		}
		return note;
	}

}
