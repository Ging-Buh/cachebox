package de.droidcachebox.database;

import de.droidcachebox.core.CB_Core_Settings;

public enum LogTypes {
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
    Submit_for_Review, //
    ;

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
    public static LogTypes parseString(String text) {

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

    public static LogTypes GC2CB_LogType(int value) {
        switch (value) {
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

    /**
     * GS LogTypeId's:</br>4 - Post Note </br>13 - Retrieve It from a Cache </br>14 - Place in a cache </br>16 - Mark as missing </br>19 -
     * Grab </br>48 - Discover </br>69 - Move to collection </br>70 - Move to inventory </br>75 - Visit
     *
     * @param value
     * @return
     */
    public static int CB_LogType2GC(LogTypes value) {
        switch (value) {
            case unarchive:
                return 1;
            case found:
                return 2;
            case didnt_find:
                return 3;
            case note:
                return 4;
            case archived:
                return 5;
            case needs_archived:
                return 7;
            case will_attend:
                return 9;
            case attended:
                return 10;
            case webcam_photo_taken:
                return 11;
            // GC hat unarchive doppelt, wir nutzen nur [1]
            // case unarchive:
            // return 12;
            case retrieve:
                return 13;
            case dropped_off:
                return 14;
            case mark_missing:
                return 16;
            case reviewer_note:
                return 18;
            case grab_it:
                return 19;
            case temporarily_disabled:
                return 22;
            case enabled:
                return 23;
            case published:
                return 24;
            case retract:
                return 25;
            case needs_maintenance:
                return 45;
            case owner_maintenance:
                return 46;
            case update_coord:
                return 47;
            case discovered:
                return 48;
            // GC hat reviewer_note doppelt, wir nutzen nur [18]
            // case reviewer_note:
            // return 68;
            case move_to_collection:
                return 69;
            case move_to_inventory:
                return 70;
            case visited:
                return 75;

            default:
                break;

        }

        return 4;
    }

    /**
     * Returns True if the log type a TB Log
     *
     * @return
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
        if (t == 25)
            return true; // visited

        return false;
    }

    /**
     * Returns True if the log type possible to direct online Log
     * or made possible by Setting
     *
     * @return
     */
    public boolean isDirectLogType() {
        if (CB_Core_Settings.DirectOnlineLog.getValue())
            return true;
        int t = this.ordinal();
        if (t == 4)
            return true; // enabled
        if (t == 5)
            return true; // needs_maintenance
        if (t == 6)
            return true; // temporarily_disabled
        if (t == 7)
            return true; // owner_maintenance
        if (t == 8)
            return true; // will_attend

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
        if (t == 25)
            return true; // visited

        return false;
    }

    public int getIconID() {
        switch (this.ordinal()) {
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

    public int getGcLogTypeId() {
        return CB_LogType2GC(this);
    }

    @Override
    public String toString() {

        switch (this) {
            case unarchive:
                return "unarchive";
            case found:
                return "Found it";
            case didnt_find:
                return "Didn't find it";
            case note:
                return "note";
            case archived:
                return "archived";
            case needs_archived:
                return "needs_archived";
            case will_attend:
                return "will_attend";
            case attended:
                return "attended";
            case webcam_photo_taken:
                return "webcam_photo_taken";
            // GC hat unarchive doppelt, wir nutzen nur [1]
            // case unarchive:
            // return 12;
            case retrieve:
                return "retrieve";
            case dropped_off:
                return "dropped_off";
            case mark_missing:
                return "mark_missing";
            case reviewer_note:
                return "reviewer_note";
            case grab_it:
                return "grab_it";
            case temporarily_disabled:
                return "temporarily_disabled";
            case enabled:
                return "enabled";
            case published:
                return "published";
            case retract:
                return "retract";
            case needs_maintenance:
                return "needs_maintenance";
            case owner_maintenance:
                return "owner_maintenance";
            case update_coord:
                return "update_coord";
            case discovered:
                return "discovered";
            // GC hat reviewer_note doppelt, wir nutzen nur [18]
            // case reviewer_note:
            // return 68;
            case move_to_collection:
                return "move_to_collection";
            case move_to_inventory:
                return "move_to_inventory";
            case visited:
                return "visited";

            default:
                return "";

        }
    }
}
