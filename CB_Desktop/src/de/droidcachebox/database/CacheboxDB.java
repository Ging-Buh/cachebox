package de.droidcachebox.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheboxDB extends CBDB {

    public CacheboxDB() {
        super();
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
