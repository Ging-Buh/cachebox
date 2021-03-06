package de.droidcachebox.database;

import de.droidcachebox.core.GCVoteCacheInfo;
import de.droidcachebox.database.Database_Core.Parameters;

import java.util.ArrayList;

public class GCVoteDAO {

    public int getCacheCountToGetVotesFor(String whereClause) {
        int count = 0;

        CoreCursor reader = Database.Data.sql.rawQuery("select count(GcCode) from Caches " + ((whereClause.length() > 0) ? "where " + whereClause : whereClause), null);

        reader.moveToFirst();

        if (!reader.isAfterLast()) {
            count = reader.getInt(0);
        }
        reader.close();

        return count;
    }

    public ArrayList<GCVoteCacheInfo> getGCVotePackage(String whereClause, int packagesize, int offset) {
        ArrayList<GCVoteCacheInfo> caches = new ArrayList<>();

        CoreCursor reader = Database.Data.sql.rawQuery("select Id, GcCode, VotePending from Caches " + ((whereClause.length() > 0) ? "where " + whereClause : whereClause) + " LIMIT " + offset + "," + packagesize, null);

        reader.moveToFirst();

        while (!reader.isAfterLast()) {
            GCVoteCacheInfo gcVoteCacheInfo = new GCVoteCacheInfo();
            gcVoteCacheInfo.setId(reader.getLong(0));
            gcVoteCacheInfo.setGcCode(reader.getString(1));
            gcVoteCacheInfo.setVotePending(reader.getInt(2) == 1);
            caches.add(gcVoteCacheInfo);

            reader.moveToNext();
        }
        reader.close();

        return caches;
    }

    public void updateRatingAndVote(Long Id, Float Rating, Float Vote) {
        Parameters parm = new Parameters();
        parm.put("Rating", Math.round(Rating * 100));
        parm.put("Vote", Math.round(Vote * 100));
        parm.put("VotePending", false);

        Database.Data.sql.update("Caches", parm, "Id=?", new String[]{String.valueOf(Id)});
    }

    public void updateRating(Long Id, Float Rating) {
        Parameters parm = new Parameters();
        parm.put("Rating", Math.round(Rating * 100));

        Database.Data.sql.update("Caches", parm, "Id=?", new String[]{String.valueOf(Id)});
    }

    public void updatePendingVote(Long Id) {
        Parameters parm = new Parameters();
        parm.put("VotePending", false);

        Database.Data.sql.update("Caches", parm, "Id=?", new String[]{String.valueOf(Id)});
    }

    /**
     * get users votes from db
     *
     * @return GCVoteCacheInfo the ArrayList with the changed votes to upload to gcvote
     */
    public ArrayList<GCVoteCacheInfo> getPendingGCVotes() {
        ArrayList<GCVoteCacheInfo> caches = new ArrayList<>();

        CoreCursor reader = Database.Data.sql.rawQuery("select Id, GcCode, Url, Vote from Caches where VotePending=1", null);

        reader.moveToFirst();

        while (!reader.isAfterLast()) {
            GCVoteCacheInfo info = new GCVoteCacheInfo();
            info.setId(reader.getLong(0));
            info.setGcCode(reader.getString(1));
            info.setUrl(reader.getString(2));
            info.setVote(reader.getInt(3));
            caches.add(info);

            reader.moveToNext();
        }
        reader.close();

        return caches;
    }
}
