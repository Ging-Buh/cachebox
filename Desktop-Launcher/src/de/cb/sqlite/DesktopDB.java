package de.cb.sqlite;

import CB_Core.Database;

import java.sql.*;

public class DesktopDB extends Database {

    public DesktopDB(DatabaseType databaseType) {
        super(databaseType);
        this.sql = new SQLiteClass();
    }

    @Override
    public int getCacheCountInDB(String filename) {

        int count = 0;
        Connection myDB = null;
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
