package de.droidcachebox.core;

import de.droidcachebox.database.Database;
import de.droidcachebox.utils.sqlite.SQLiteClass;

import java.sql.*;

public class DesktopDB extends Database {

    public DesktopDB(DatabaseType databaseType) {
        super(databaseType);
        this.sql = new SQLiteClass();
    }

    @Override
    public int getCacheCountInDB(String filename) {

        int count = 0;
        Connection myDB;
        try {
            myDB = DriverManager.getConnection("jdbc:sqlite:" + filename);
            Statement statement = myDB.createStatement();
            ResultSet result = statement.executeQuery("select count(*) from caches");
            // result.first();
            count = result.getInt(1);
            result.close();
            myDB.close();
        } catch (SQLException e) {
            // String s = e.getMessage();
        }
        return count;
    }

}
