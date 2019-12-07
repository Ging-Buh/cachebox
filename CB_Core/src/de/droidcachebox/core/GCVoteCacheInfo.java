package de.droidcachebox.core;

public class GCVoteCacheInfo {
    private long id;
    private String gcCode;
    private boolean votePending;
    private String url;
    private int vote;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGcCode() {
        return gcCode;
    }

    public void setGcCode(String gcCode) {
        this.gcCode = gcCode;
    }

    public boolean isVotePending() {
        return votePending;
    }

    public void setVotePending(boolean votePending) {
        this.votePending = votePending;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }
}
