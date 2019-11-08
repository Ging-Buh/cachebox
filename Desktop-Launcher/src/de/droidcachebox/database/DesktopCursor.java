package de.droidcachebox.database;

import de.droidcachebox.database.CoreCursor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DesktopCursor extends CoreCursor {
    private ResultSet rs;
    private PreparedStatement ps;
    private int rowcount;

    public DesktopCursor(ResultSet rs, PreparedStatement ps) {
        this.rs = rs;
        this.ps = ps;
    }

    public DesktopCursor(ResultSet rs, int rowcount, PreparedStatement ps) {
        this.rs = rs;
        this.rowcount = rowcount;
        this.ps = ps;
    }

    @Override
    public boolean moveToFirst() {
        try {
            if (rs.isBeforeFirst()) {
                rs.next();
            }
            if (rs.isFirst())
                return true;
            return rs.first();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isAfterLast() {

        try {
            return rs.isAfterLast();
        } catch (Exception e) {
            return true;
        }

    }

    @Override
    public boolean moveToNext() {
        try {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (rs != null)
                rs.close();
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rs = null;
        ps = null;
    }

    @Override
    public String getString(int columnIndex) {

        try {
            return rs.getString(columnIndex + 1);
        } catch (SQLException e) {
            return null;
        }

    }

    @Override
    public String getString(String column) {

        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }

    }

    @Override
    public long getLong(int columnIndex) {

        try {
            return rs.getLong(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }

    }

    @Override
    public long getLong(String column) {

        try {
            return rs.getLong(column);
        } catch (SQLException e) {
            return 0;
        }

    }

    @Override
    public int getInt(int columnIndex) {
        try {
            return rs.getInt(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public int getInt(String column) {
        try {
            return rs.getInt(column);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public boolean isNull(int columnIndex) {

        try {
            if (rs.getObject(columnIndex + 1) == null || rs.getObject(columnIndex + 1).toString().length() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

    }

    @Override
    public boolean isNull(String column) {

        try {
            if (rs.getObject(column) == null || rs.getObject(column).toString().length() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

    }

    @Override
    public double getDouble(int columnIndex) {
        try {
            return rs.getDouble(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public double getDouble(String column) {
        try {
            return rs.getDouble(column);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public short getShort(int columnIndex) {
        try {
            return rs.getShort(columnIndex + 1);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public short getShort(String column) {
        try {
            return rs.getShort(column);
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public int getCount() {
        return rowcount;
    }

}