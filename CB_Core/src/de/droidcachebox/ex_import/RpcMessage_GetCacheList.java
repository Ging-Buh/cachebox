package de.droidcachebox.ex_import;

import de.droidcachebox.rpc.RpcMessage;

public class RpcMessage_GetCacheList extends RpcMessage {
    private static final long serialVersionUID = -8640103804448375026L;

    private long categoryId;
    private int startIndex;
    private int count;

    public RpcMessage_GetCacheList(long categoryId, int startIndex, int count) {
        this.categoryId = categoryId;
        this.setStartIndex(startIndex);
        this.setCount(count);
    }

    public long getCategoryId() {
        return categoryId;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
