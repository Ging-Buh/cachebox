package de.droidcachebox.database;

import de.droidcachebox.database.Database_Core.Parameters;

public class Replication {

    public static void updateFound(long CacheId, boolean found) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            ChangeType changeType;
            if (found)
                changeType = ChangeType.Found;
            else
                changeType = ChangeType.NotFound;

            CBDB.getInstance().delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=6 or CacheId=" + String.valueOf(CacheId) + " and ChangeType=7", null);

            Parameters val = new Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            CBDB.getInstance().insert("Replication", val);
        }

    }

    public static void SolverChanged(long CacheId, int oldSolverCheckSum, int newSolverCheckSum) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            if (oldSolverCheckSum != newSolverCheckSum) {
                if (oldSolverCheckSum != newSolverCheckSum) {
                    // When the Solver text is changed then this must be added
                    // into the Replication table
                    // When for this cache already a SolverChanged entry exists
                    // -> read the SolverCheckSum from this entry -> if the
                    // stored CheckSum == newSolverCheckSum
                    // -> the new Solver Text is the same than the original ->
                    // remove this entry
                    // When no SolverText entry for this Cache exists -> create
                    // a new entry and store the original CheckSum of the Solver
                    // Text
                    int dbCheckSum = -1;
                    CoreCursor c = CBDB.getInstance().rawQuery("select SolverCheckSum from Replication where CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(ChangeType.SolverText.ordinal())});
                    if (c != null) {
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            dbCheckSum = c.getInt(0);
                        }
                        c.close();
                    }
                    if (dbCheckSum < 0) {
                        // a Change for the Solvertext for this Cache must be added to the Replication Table
                        Parameters val = new Parameters();
                        val.put("CacheId", CacheId);
                        val.put("ChangeType", ChangeType.SolverText.ordinal());
                        val.put("SolverCheckSum", oldSolverCheckSum);
                        CBDB.getInstance().insert("Replication", val);
                    } else {
                        // compare the stored CheckSum with the new
                        if (dbCheckSum == newSolverCheckSum) {
                            // the dbCheckSum is the same than the newCheckSum
                            // -> Solver text is the same than the original
                            // -> delete this entry!
                            CBDB.getInstance().delete("Replication", "CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(ChangeType.SolverText.ordinal())});
                        }
                    }
                }
            }
        }
    }

    public static void NoteChanged(long CacheId, int oldNoteCheckSum, int newNoteCheckSum) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            if (oldNoteCheckSum != newNoteCheckSum) {
                if (oldNoteCheckSum != newNoteCheckSum) {
                    // When the Note text is changed then this must be added
                    // into the Replication table
                    // When for this cache already a NoteChanged entry exists
                    // -> read the NoteCheckSum from this entry -> if the stored
                    // CheckSum == newNoteCheckSum
                    // -> the new Note Text is the same than the original ->
                    // remove this entry
                    // When no Note Text entry for this Cache exists -> create a
                    // new entry and store the original CheckSum of the Note
                    // Text
                    int dbCheckSum = -1;
                    CoreCursor c = CBDB.getInstance().rawQuery("select NotesCheckSum from Replication where CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(ChangeType.NotesText.ordinal())});
                    if (c != null) {
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            dbCheckSum = c.getInt(0);
                        }
                        c.close();
                    }
                    if (dbCheckSum < 0) {
                        // a Change for the Notestext for this Cache must be
                        // added to the Replication Table
                        Parameters val = new Parameters();
                        val.put("CacheId", CacheId);
                        val.put("ChangeType", ChangeType.NotesText.ordinal());
                        val.put("SolverCheckSum", oldNoteCheckSum);
                        CBDB.getInstance().insert("Replication", val);
                    } else {
                        // compare the stored CheckSum with the new
                        if (dbCheckSum == newNoteCheckSum) {
                            // the dbCheckSum is the same than the newCheckSum
                            // -> Notes text is the same than the original
                            // -> delete this entry!
                            CBDB.getInstance().delete("Replication", "CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(ChangeType.NotesText.ordinal())});
                        }
                    }
                }
            }
        }
    }

    public static void waypointChanged(long CacheId, int oldCheckSum, int newCheckSum, String WpGcCode) {
        Changed(CacheId, oldCheckSum, newCheckSum, "WpCoordCheckSum", ChangeType.WaypointChanged, WpGcCode);
    }

    public static void WaypointNew(long CacheId, int oldCheckSum, int newCheckSum, String WpGcCode) {
        Changed(CacheId, oldCheckSum, newCheckSum, "WpCoordCheckSum", ChangeType.NewWaypoint, WpGcCode);
    }

    public static void WaypointDelete(long CacheId, int oldCheckSum, int newCheckSum, String WpGcCode) {
        Changed(CacheId, oldCheckSum, newCheckSum, "WpCoordCheckSum", ChangeType.DeleteWaypoint, WpGcCode);
    }

    private static void Changed(long CacheId, int oldCheckSum, int newCheckSum, String checkSumType, ChangeType changeType, String WpGcCode) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            if (oldCheckSum != newCheckSum) {
                // When the item is changed then this must be added into the
                // Replication table
                // When for this cache already a Changed entry exists
                // -> read the CheckSum from this entry -> if the stored
                // CheckSum == newCheckSum
                // -> the new item is the same than the original -> remove this
                // entry
                // When no entry for this Cache exists -> create a new entry and
                // store the original CheckSum of the item
                int dbCheckSum = -1;
                CoreCursor c = CBDB.getInstance().rawQuery("select " + checkSumType + " from Replication where CacheId=? and ChangeType=? and WpGcCode=?", new String[]{String.valueOf(CacheId), String.valueOf(changeType.ordinal()), WpGcCode});
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        dbCheckSum = c.getInt(0);
                    }
                    c.close();
                }
                if (dbCheckSum < 0) {
                    // a Change for the WP for this Cache must be added to the
                    // Replication Table
                    Parameters val = new Parameters();
                    val.put("CacheId", CacheId);
                    val.put("ChangeType", changeType.ordinal());
                    val.put("WpCoordCheckSum", oldCheckSum);
                    val.put("WpGcCode", WpGcCode);
                    CBDB.getInstance().insert("Replication", val);
                } else {
                    // compare the stored CheckSum with the new
                    if (dbCheckSum == newCheckSum) {
                        // the dbCheckSum is the same than the newCheckSum ->
                        // value is the same than the original
                        // -> delete this entry!
                        CBDB.getInstance().delete("Replication", "CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(changeType)});
                    }
                }
            }
        }
    }

    public static void AvailableChanged(long CacheId, boolean available) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            ChangeType changeType;
            if (available)
                changeType = ChangeType.Available;
            else
                changeType = ChangeType.NotAvailable;

            CBDB.getInstance().delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=10 or CacheId=" + String.valueOf(CacheId) + " and ChangeType=11", null);

            Parameters val = new Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            CBDB.getInstance().insert("Replication", val);
        }

    }

    public static void ArchivedChanged(long CacheId, boolean archived) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            ChangeType changeType;
            if (archived)
                changeType = ChangeType.Archived;
            else
                changeType = ChangeType.NotArchived;

            CBDB.getInstance().delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=8 or CacheId=" + String.valueOf(CacheId) + " and ChangeType=9", null);

            Parameters val = new Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            CBDB.getInstance().insert("Replication", val);
        }

    }

    public static void NumTravelbugsChanged(long CacheId, int numTravelbugs) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            ChangeType changeType;

            CBDB.getInstance().delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=12", null);

            changeType = ChangeType.NumTravelbugs;
            Parameters val = new Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            val.put("SolverCheckSum", numTravelbugs);
            CBDB.getInstance().insert("Replication", val);
        }

    }

    public static void NumFavPointsChanged(long CacheId, int numFavPoints) {
        if (CBDB.getInstance().MasterDatabaseId > 0) {
            ChangeType changeType = ChangeType.FavPoints;

            CBDB.getInstance().delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=" + changeType.ordinal(), null);

            Parameters val = new Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            val.put("SolverCheckSum", numFavPoints); // todo ok? handle
            CBDB.getInstance().insert("Replication", val);
        }

    }

}
