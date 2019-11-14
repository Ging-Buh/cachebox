package de.droidcachebox.rpc;

import de.droidcachebox.database.CacheList;
import de.droidcachebox.database.LogEntry;

import java.util.ArrayList;

public class RpcAnswer_GetCacheList extends RpcAnswer {

    private static final long serialVersionUID = -1277842023438172166L;
    private CacheList cacheList;
    private ArrayList<LogEntry> logList = new ArrayList<>();
    private boolean dataAvailable; // wird auf true gesetzt falls noch weitere Daten vorhanden sind, die noch nicht abgeholt wurden

    public RpcAnswer_GetCacheList(int result) {
        super(result);
        this.cacheList = null;
        setDataAvailable(false);
    }

    public void addLog(LogEntry log) {
        logList.add(log);
    }

    public ArrayList<LogEntry> getLogs() {
        return logList;
    }

    public CacheList getCacheList() {
        return cacheList;
    }

    public void setCacheList(CacheList cacheList) {
        this.cacheList = cacheList;
    }

    public boolean isDataAvailable() {
        return dataAvailable;
    }

    public void setDataAvailable(boolean dataAvailable) {
        this.dataAvailable = dataAvailable;
    }
}
