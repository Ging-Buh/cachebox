package de.droidcachebox.database;

import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.LogLevel;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SQLiteClass implements SQLiteInterface {
    private static final String log = "SQLiteClass";
    private Connection myDB = null;

    public SQLiteClass() {
        try {
            System.setProperty("sqlite.purejava", "true");
            Class.forName("org.sqlite.JDBC");
        } catch (Exception ignore) {
        }
    }

    @Override
    public boolean open(String databasePath) {
        try {
            Log.trace(log, "open data base: " + databasePath);
            myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public boolean openReadOnly(String databasePath) {
        try {
            // todo this is == RW
            Log.trace(log, "open data base: " + databasePath);
            myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public boolean create(String databasePath) {
        // if exists, delete old database file
        File file = FileFactory.createFile(databasePath);
        if (file.exists()) {
            Log.trace(log, "RESET DB, delete file: " + databasePath);
            try {
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Log.trace(log, "create data base: " + databasePath);
            myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            myDB.commit();
            myDB.close();
            return true;
        } catch (Exception exc) {
            Log.err(log, "createDB", exc);
            return false;
        }
    }

    @Override
    public CoreCursor rawQuery(String sql, String[] args) {
        if (myDB == null)
            return null;

        if (LogLevel.shouldWriteLog(LogLevel.TRACE)) {
            StringBuilder sb = new StringBuilder("RAW_QUERY :" + sql + " ARGs= ");
            if (args != null) {
                for (String arg : args)
                    sb.append(arg).append(", ");
            } else
                sb.append("NULL");
            Log.trace(log, sb.toString());
        }

        ResultSet rs = null;
        PreparedStatement statement = null;
        try {

            statement = myDB.prepareStatement(sql);

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement.setString(i + 1, args[i]);
                }
            }
            rs = statement.executeQuery();

        } catch (SQLException ex) {
            Log.trace(log, ex);
        }

        // TODO Hack to get Rowcount
        ResultSet rs2;
        int rowcount = 0;
        PreparedStatement statement2 = null;
        try {

            statement2 = myDB.prepareStatement("select count(*) from (" + sql + ")");

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    statement2.setString(i + 1, args[i]);
                }
            }
            rs2 = statement2.executeQuery();

            rs2.next();

            rowcount = Integer.parseInt(rs2.getString(1));
            statement2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                assert statement2 != null;
                statement2.close();
            } catch (SQLException ignored) {
            }
        }

        return new DesktopCursor(rs, rowcount, statement);
    }

    @Override
    public boolean execSQL(String sql) {
        if (myDB == null)
            return false;

        Log.trace(log, "execSQL : " + sql);

        boolean ret;
        try {
            try (Statement statement = myDB.createStatement()) {
                ret = statement.execute(sql);
            }
        } catch (SQLException e) {
            ret = false;
            // e.printStackTrace();
        }
        return ret;
    }

    @Override
    public long update(String tablename, HashMap<String, Object> val, String whereClause, String[] whereArgs) {

        if (LogLevel.shouldWriteLog(LogLevel.TRACE)) {
            StringBuilder sb = new StringBuilder("Update @ Table:" + tablename);
            sb.append("Parameters:").append(val.toString());
            sb.append("WHERECLAUSE:").append(whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg).append(", ");
                }
            }

            Log.trace(log, sb.toString());
        }

        if (myDB == null)
            return 0;

        StringBuilder sql = new StringBuilder();

        sql.append("update ");
        sql.append(tablename);
        sql.append(" set");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            sql.append("=?");
            if (i != val.size()) {
                sql.append(",");
            }
        }

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }

        PreparedStatement st = null;
        long ret;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            if (whereArgs != null) {
                for (int k = 0; k < whereArgs.length; k++) {
                    st.setString(j + k + 1, whereArgs[k]);
                }
            }

            ret = st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                assert st != null;
                st.close();
            } catch (SQLException ignored) {
            }
        }
        return ret;

    }

    @Override
    public long insert(String tablename, HashMap<String, Object> val) {
        if (myDB == null)
            return 0;
        StringBuilder sql = new StringBuilder();

        sql.append("insert into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            Log.trace(log, "INSERT: " + sql);
            return st.execute() ? 0 : 1;

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                assert st != null;
                st.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public long delete(String tablename, String whereClause, String[] whereArgs) {
        if (LogLevel.shouldWriteLog(LogLevel.TRACE)) {
            StringBuilder sb = new StringBuilder("Delete@ Table:" + tablename);
            sb.append("WHERECLAUSE:").append(whereClause);

            if (whereArgs != null) {
                for (String arg : whereArgs) {
                    sb.append(arg + ", ");
                }
            }

            Log.trace(log, sb.toString());
        }

        if (myDB == null)
            return 0;
        StringBuilder sql = new StringBuilder();

        sql.append("delete from ");
        sql.append(tablename);

        if (!whereClause.isEmpty()) {
            sql.append(" where ");
            sql.append(whereClause);
        }
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            if (whereArgs != null) {
                for (int i = 0; i < whereArgs.length; i++) {
                    st.setString(i + 1, whereArgs[i]);
                }
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }

    @Override
    public void beginTransaction() {
        try {
            Log.trace(log, "begin transaction");
            if (myDB != null)
                myDB.setAutoCommit(false);
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void setTransactionSuccessful() {
        try {
            Log.trace(log, "set Transaction Successful");
            if (myDB != null)
                myDB.commit();
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void endTransaction() {
        try {
            Log.trace(log, "endTransaction");
            if (myDB != null)
                myDB.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public long insertWithConflictReplace(String tablename, HashMap<String, Object> val) {
        if (myDB == null)
            return 0;

        Log.trace(log, "insertWithConflictReplace @Table:" + tablename + "Parameters: " + val.toString());
        StringBuilder sql = new StringBuilder();

        sql.append("insert OR REPLACE into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

    }

    @Override
    public long insertWithConflictIgnore(String tablename, HashMap<String, Object> val) {
        if (myDB == null)
            return 0;

        Log.trace(log, "insertWithConflictIgnore @Table:" + tablename + "Parameters: " + val.toString());

        StringBuilder sql = new StringBuilder();

        sql.append("insert OR IGNORE into ");
        sql.append(tablename);
        sql.append(" (");

        int i = 0;
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            i++;
            sql.append(" ");
            sql.append(entry.getKey());
            if (i != val.size()) {
                sql.append(",");
            }
        }

        sql.append(" ) Values(");

        for (int k = 1; k <= val.size(); k++) {
            sql.append(" ");
            sql.append("?");
            if (k < val.size()) {
                sql.append(",");
            }
        }

        sql.append(" )");
        PreparedStatement st = null;
        try {
            st = myDB.prepareStatement(sql.toString());

            int j = 0;
            for (Map.Entry<String, Object> entry : val.entrySet()) {
                j++;
                st.setObject(j, entry.getValue());
            }

            return st.executeUpdate();

        } catch (SQLException e) {
            return 0;
        } finally {
            try {
                st.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        try {
            Log.trace(log, "close DB:");
            myDB.close();
            myDB = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
