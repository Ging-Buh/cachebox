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
	needs_archived, // 13
	unarchive, // 14
	retract, // 15
	update_coord, // 16
	retrieve, // 17
	dropped_off, // 18
	mark_missing, // 19
	grab_it, // 20
	discovered, // 21
	move_to_collection, // 22
	move_to_inventory, // 23
	announcement, // 24
	visited, // 25
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

	public int getIconID()
	{
		switch (this.ordinal())
		{
		case 0:
			return 0; // Found
		case 1:
			return 1; // DNF
		case 2:
			return 2; // Note
		case 3:
			return 3; // Publish
		case 4:
			return 4; // Enable
		case 5:
			return 5; // needs maintains
		case 6:
			return 6; // Disable
		case 7:
			return 7; // Owner Maintains
		case 8:
			return 8; // Will attend
		case 9:
			return 9; // Attended
		case 10:
			return 10; // Photo
		case 11:
			return 11; // Archive
		case 12:
			return 12; // Reviewer Note
		case 13:
			return 13; // needs maintains
		case 14:
			return 11; // Unarchive
		case 15:
			return 14; // Retract
		case 16:
			return 16; // Update Coords
		case 17:
			return 17; // Retrive
		case 18:
			return 18; // Dropped
		case 19:
			return 2; // Mark missing
		case 20:
			return 19; // Grab It
		case 21:
			return 20; // Discover
		case 22:
			return 2; // Move to Collection
		case 23:
			return 2; // Move to Inventory
		case 24:
			return 15; // Announcement
		case 25:
			return 21; // Visited

		}

		return -1; // Note
	}

	public static LogTypes GC2CB_LogType(int value)
	{
		switch (value)
		{

		case 1:
			return unarchive;
		case 2:
			return found;
		case 3:
			return didnt_find;
		case 4:
			return note;
		case 5:
			return archived;

		case 7:
			return needs_archived;

		case 9:
			return will_attend;
		case 10:
			return attended;
		case 11:
			return webcam_photo_taken;
		case 12:
			return unarchive;
		case 13:
			return retrieve;
		case 14:
			return dropped_off;

		case 16:
			return mark_missing;

		case 18:
			return reviewer_note;
		case 19:
			return grab_it;

		case 22:
			return temporarily_disabled;
		case 23:
			return enabled;
		case 24:
			return published;
		case 25:
			return retract;

		case 45:
			return needs_maintenance;
		case 46:
			return owner_maintenance;
		case 47:
			return update_coord;
		case 48:
			return discovered;
		case 68:
			return reviewer_note;
		case 69:
			return move_to_collection;
		case 70:
			return move_to_inventory;
		case 75:
			return visited;
		}

		return note;
	}

	public int getGcLogTypeId()
	{
		return CB_LogType2GC(this);
	}

	/**
	 * GS LogTypeId's:</br>4 - Post Note </br>13 - Retrieve It from a Cache </br>14 - Place in a cache </br>16 - Mark as missing </br>19 -
	 * Grab </br>48 - Discover </br>69 - Move to collection </br>70 - Move to inventory </br>75 - Visit
	 * 
	 * @param value
	 * @return
	 */
	public static int CB_LogType2GC(LogTypes value)
	{
		switch (value)
		{

		case note:
			return 4;

		case retrieve:
			return 13;

		case dropped_off:
			return 14;

		case mark_missing:
			return 16;

		case grab_it:
			return 19;

		case discovered:
			return 48;

		case move_to_collection:
			return 69;

		case move_to_inventory:
			return 70;

		case visited:
			return 75;

		}

		return 4;
	}
}
