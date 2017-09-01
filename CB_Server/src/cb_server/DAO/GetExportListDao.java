package cb_server.DAO;

import CB_Core.Database;
import CB_RpcCore.Functions.RpcAnswer_GetExportList;
import cb_rpc.Functions.RpcAnswer;
import cb_rpc.Functions.RpcAnswer_Error;
import cb_server.CacheboxServer;
import de.cb.sqlite.CoreCursor;

public class GetExportListDao {

	public RpcAnswer getList() {

		try {
			CoreCursor reader = Database.Data.rawQuery("SELECT gpx.CategoryId, cat.GpxFilename, count(*) from Caches c INNER JOIN GPXFilenames gpx, Category cat on c.GpxFilename_Id=gpx.Id AND gpx.CategoryId=cat.Id GROUP BY gpx.CategoryId", null);

			RpcAnswer_GetExportList result = new RpcAnswer_GetExportList(0);
			if (reader.getCount() > 0) {
				reader.moveToFirst();
				while (reader.isAfterLast() == false) {
					int id = reader.getInt(0);
					String desc = reader.getString(1);
					int count = reader.getInt(2);

					result.addListItem(id, desc, count);
					reader.moveToNext();
				}
			}
			reader.close();
			return result;
		} catch (Exception ex) {
			RpcAnswer result = new RpcAnswer_Error(-1, ex.getMessage());
			CacheboxServer.log.error("GetExportListDao.getList(): " + ex.getMessage());
			return result;
		}

	}

}
