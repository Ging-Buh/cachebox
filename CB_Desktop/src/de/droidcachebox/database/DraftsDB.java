package de.droidcachebox.database;

public class DraftsDB extends DraftsDatabase{
    public DraftsDB() {
        super();
        this.sql = new SQLiteClass();
    }
}
