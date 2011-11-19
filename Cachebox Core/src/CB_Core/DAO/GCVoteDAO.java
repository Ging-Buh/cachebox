package CB_Core.DAO;

import java.util.ArrayList;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.GCVote.GCVoteCacheInfo;

public class GCVoteDAO
{

	public int getCacheCountToGetVotesFor(String whereClause)
	{
		int count = 0;

		CoreCursor reader = Database.Data.rawQuery("select count(GcCode) from Caches "
				+ ((whereClause.length() > 0) ? "where " + whereClause : whereClause), null);

		reader.moveToFirst();

		if (!reader.isAfterLast())
		{
			count = reader.getInt(0);
		}
		reader.close();

		return count;
	}

	public ArrayList<GCVoteCacheInfo> getGCVotePackage(String whereClause, int packagesize, int offset)
	{
		ArrayList<GCVoteCacheInfo> caches = new ArrayList<GCVoteCacheInfo>();

		CoreCursor reader = Database.Data.rawQuery(
				"select Id, GcCode, VotePending from Caches " + ((whereClause.length() > 0) ? "where " + whereClause : whereClause)
						+ " LIMIT " + String.valueOf(offset) + "," + String.valueOf(packagesize), null);

		reader.moveToFirst();

		while (!reader.isAfterLast())
		{
			GCVoteCacheInfo info = new GCVoteCacheInfo();
			info.Id = reader.getLong(0);
			info.GcCode = reader.getString(1);
			info.VotePending = (reader.getInt(2) == 1 ? true : false);
			caches.add(info);

			reader.moveToNext();
		}
		reader.close();

		return caches;
	}
}
