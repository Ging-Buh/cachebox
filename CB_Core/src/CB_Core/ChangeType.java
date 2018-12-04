package CB_Core;

import java.io.Serializable;

public enum ChangeType implements Serializable {
    Undefined, // 0
    SolverText, // 1
    NotesText, // 2
    WaypointChanged, // 3
    NewWaypoint, // 4
    DeleteWaypoint, // 5
    Found, // 6
    NotFound, // 7
    Archived, // 8
    NotArchived, // 9
    Available, // 10
    NotAvailable, // 11
    NumTravelbugs, // 12
    FavPoints //

}
