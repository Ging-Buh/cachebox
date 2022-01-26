package de.droidcachebox.dataclasses;

public enum LogType {
    found(2, 0, "Found it"),
    didnt_find(3, 1, "Didn't find it"),
    note(4, 2, "note"),
    published(24, 3, "published"),
    enabled(23, 4, "enabled"),
    needs_maintenance(45, 5, "needs_maintenance"),
    temporarily_disabled(22, 6, "temporarily_disabled"),
    owner_maintenance(46, 7, "owner_maintenance"),
    will_attend(9, 8, "will_attend"),
    attended(10, 9, "attended"),
    webcam_photo_taken(11, 10, "webcam_photo_taken"),
    archived(5, 11, "archived"),
    reviewer_note(18, 12, "reviewer_note"),
    needs_archived(7, 13, "needs_archived"),
    unarchive(1, 11, "unarchive"), // == archived
    retract(25, 14,"retract"), // 15
    update_coord(47, 16, "update_coord"),
    retrieve(13, 17, "retrieve"),
    dropped_off(14, 18, "dropped_off"),
    mark_missing(16, 2, "mark_missing"), // == dnf
    grab_it(19, 19, "grab_it"),
    discovered(48, 20, "discovered"),
    move_to_collection(69, 2, "move_to_collection"),
    move_to_inventory(70, 2, "move_to_inventory"),
    announcement(74, 15, "Event Announcement"),
    visited(75, 21, "visited"),
    Submit_for_Review(4, 2, "Submit for Review"), // 4=note
    ;
    /**
     * GS LogTypeId's:</br>4 - Post Note </br>13 - Retrieve It from a Cache </br>14 - Place in a cache </br>16 - Mark as missing </br>19 -
     * Grab </br>48 - Discover </br>69 - Move to collection </br>70 - Move to inventory </br>75 - Visit
     * GC has "unarchive" twice, we use  [1] not [12]
     * GC has "reviewer_note" twice, we use [18] not [68]
     */
    public int gsLogTypeId;
    public int iconId;
    private final String string;

    LogType(int gsLogTypeId, int iconId, String string) {
        this.gsLogTypeId = gsLogTypeId;
        this.iconId = iconId;
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }


    /*
    GEOCACHE LOG TYPES
        Id	Name	Description
        2	Found It	found the geocache
        3	DNF it	Did Not Find (DNF) the geocache
        4	Write note	Adding a comment to the geocache
        5	Archive	changing the status of the geocache to archived
        7	Needs archiving	flagging the geocache as needing to be archived
        9	Will attend	RSVPing for an event
        10	Attended	Attended an event (counts as a find)
        11	Webcam photo taken	Successfully captured a webcam geocache (counts as a find)
        12	Unarchive	changing the status of the geocache from archived to active
        22	Temporarily Disable Listing	changing the status of the geocache to disabled
        23	Enable Listing	changing the status of the geocache from disabled to active
        24	Publish Listing	changing the status of the geocache from unpublished to active
        45	Needs Maintenance	flagging a geocache owner that the geocache needs some attention
        46	Owner Maintenance	announcing that owner maintenance was done
        47	Update Coordinates	updating the coordinates of the geocache
        68	Post Reviewer Note	a note left by the reviewer
        74	Event Announcement	event host announcement to attendees
    */
    /*
        TRACKABLE LOG TYPES
        Id	Name
        4	Write Note
        13	Retrieve It from a Cache
        14	Dropped Off
        15	Transfer
        16	Mark Missing
        19	Grab It (Not from a Cache)
        48	Discovered It
        69	Move to Collection
        70	Move to Inventory
        75	Visited
     */
    /* Api 1.0
    ['Found It',
    'DNF it',
    'Write note',
    'Archive',
    'Needs archiving',
    'Will attend',
    'Attended',
    'Webcam photo taken',
    'Unarchive',
    'Post Reviewer Note',
    'Temporarily Disable Listing',
    'Enable Listing',
    'Publish Listing',
    'Needs Maintenance',
    'Owner Maintenance',
    'Update Coordinates',
    'Post Reviewer Note - Post Publish',
    'Event Announcement',
    'Submit for Review']
     */
    public static LogType parseString(String text) {

        if (text.equalsIgnoreCase("Found It")) {
            return found;
        }
        if (text.equalsIgnoreCase("didn't find it")) {
            return didnt_find;
        }
        if (text.equalsIgnoreCase("DNF it")) {
            return didnt_find;
        }
        if (text.equalsIgnoreCase("not found")) {
            return didnt_find;
        }
        if (text.equalsIgnoreCase("write note")) {
            return note;
        }
        if (text.equalsIgnoreCase("Publish Listing")) {
            return published;
        }
        if (text.equalsIgnoreCase("Enable Listing")) {
            return enabled;
        }
        if (text.equalsIgnoreCase("Needs Maintenance")) {
            return needs_maintenance;
        }
        if (text.equalsIgnoreCase("Temporarily Disable Listing")) {
            return temporarily_disabled;
        }
        if (text.equalsIgnoreCase("Owner Maintenance")) {
            return owner_maintenance;
        }
        if (text.equalsIgnoreCase("Update Coordinates")) {
            return owner_maintenance;
        }
        if (text.equalsIgnoreCase("Will attend")) {
            return will_attend;
        }
        if (text.equalsIgnoreCase("Attended")) {
            return attended;
        }
        if (text.equalsIgnoreCase("Webcam photo taken")) {
            return webcam_photo_taken;
        }
        if (text.equalsIgnoreCase("Archive")) {
            return archived;
        }
        if (text.equalsIgnoreCase("Unarchive")) {
            return unarchive;
        }
        if (text.equalsIgnoreCase("Post Reviewer Note")) {
            return reviewer_note;
        }
        if (text.equalsIgnoreCase("Post Reviewer Note - Post Publish")) {
            return reviewer_note; // ? own enum
        }
        if (text.equalsIgnoreCase("needs archived")) {
            return needs_archived;
        }
        if (text.equalsIgnoreCase("Needs archiving")) {
            return needs_archived;
        }
        if (text.equalsIgnoreCase("other")) {
            return note;
        }
        if (text.equalsIgnoreCase("note")) {
            return note;
        }
        if (text.equalsIgnoreCase("geocoins")) {
            return note;
        }
        if (text.equalsIgnoreCase("cache disabled!")) {
            return temporarily_disabled;
        }
        if (text.equalsIgnoreCase("retract listing")) {
            return archived;
        }
        if (text.equalsIgnoreCase("Event Announcement")) {
            return announcement;
        }
        if (text.equalsIgnoreCase("Submit for Review")) {
            return Submit_for_Review;
        }
        return note;
    }

    public static LogType GC2CB_LogType(int value) {
        switch (value) {
            case 1:
            case 12:
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
            case 13:
                return retrieve;
            case 14:
                return dropped_off;
            case 16:
                return mark_missing;
            case 18:
            case 68:
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
            case 69:
                return move_to_collection;
            case 70:
                return move_to_inventory;
            case 75:
                return visited;
        }

        return note;
    }

    /**
     * Returns True if the log type is a TB Log
     *
     * @return boolean true or false
     */
    public boolean isTbLog() {
        int t = this.ordinal();
        if (t == 17)
            return true; // retrieve
        if (t == 18)
            return true; // dropped_off
        if (t == 19)
            return true; // mark_missing
        if (t == 20)
            return true; // grab_it
        if (t == 21)
            return true; // discovered
        if (t == 22)
            return true; // move_to_collection
        if (t == 23)
            return true; // move_to_inventory
        return t == 25; // visited
    }
}
