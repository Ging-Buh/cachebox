package de.droidcachebox.database;

import java.util.ArrayList;

import de.droidcachebox.core.GCVoteCacheInfo;
import de.droidcachebox.database.Database_Core.Parameters;

public class GCVoteDAO {

    public int getCacheCountToGetVotesFor(String whereClause) {
        int count = 0;

        CoreCursor c = CBDB.getInstance().rawQuery("select count(GcCode) from Caches " + ((whereClause.length() > 0) ? "where " + whereClause : whereClause), null);
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                count = c.getInt(0);
            }
            c.close();
        }
        return count;
    }

    public ArrayList<GCVoteCacheInfo> getGCVotePackage(String whereClause, int packagesize, int offset) {
        ArrayList<GCVoteCacheInfo> caches = new ArrayList<>();
        CoreCursor c = CBDB.getInstance().rawQuery("select Id, GcCode, VotePending from Caches " + ((whereClause.length() > 0) ? "where " + whereClause : whereClause) + " LIMIT " + offset + "," + packagesize, null);
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                GCVoteCacheInfo gcVoteCacheInfo = new GCVoteCacheInfo();
                gcVoteCacheInfo.setId(c.getLong(0));
                gcVoteCacheInfo.setGcCode(c.getString(1));
                gcVoteCacheInfo.setVotePending(c.getInt(2) == 1);
                caches.add(gcVoteCacheInfo);
                c.moveToNext();
            }
            c.close();
        }
        return caches;
    }

    public void updateRatingAndVote(Long Id, Float Rating, Float Vote) {
        Parameters parm = new Parameters();
        parm.put("Rating", Math.round(Rating * 100));
        parm.put("Vote", Math.round(Vote * 100));
        parm.put("VotePending", false);

        CBDB.getInstance().update("Caches", parm, "Id=?", new String[]{String.valueOf(Id)});
    }

    public void updateRating(Long Id, Float Rating) {
        Parameters parm = new Parameters();
        parm.put("Rating", Math.round(Rating * 100));

        CBDB.getInstance().update("Caches", parm, "Id=?", new String[]{String.valueOf(Id)});
    }

    public void updatePendingVote(Long Id) {
        Parameters parm = new Parameters();
        parm.put("VotePending", false);

        CBDB.getInstance().update("Caches", parm, "Id=?", new String[]{String.valueOf(Id)});
    }

    /**
     * get users votes from db
     *
     * @return GCVoteCacheInfo the ArrayList with the changed votes to upload to gcvote
     */
    public ArrayList<GCVoteCacheInfo> getPendingGCVotes() {
        ArrayList<GCVoteCacheInfo> caches = new ArrayList<>();
        CoreCursor c = CBDB.getInstance().rawQuery("select Id, GcCode, Url, Vote from Caches where VotePending=1", null);
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                GCVoteCacheInfo info = new GCVoteCacheInfo();
                info.setId(c.getLong(0));
                info.setGcCode(c.getString(1));
                info.setUrl(c.getString(2));
                info.setVote(c.getInt(3));
                caches.add(info);
                c.moveToNext();
            }
            c.close();
        }
        return caches;
    }
}
