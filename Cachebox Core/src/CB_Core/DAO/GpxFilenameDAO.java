package CB_Core.DAO;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.DB.CoreCursor;
import CB_Core.Types.GpxFilename;

public class GpxFilenameDAO {
    public GpxFilename ReadFromCursor(CoreCursor reader)
    {
        long id;
        String gpxFileName;
        long categoryId = -1;
    	
        id = reader.getLong(0);
        gpxFileName = reader.getString(1);
        
    	GpxFilename result = new GpxFilename(id, gpxFileName, categoryId);

        if (reader.isNull(2))
        	result.Imported = new Date();
        else
        {
            String sDate = reader.getString(2);
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
            	result.Imported = iso8601Format.parse(sDate);
            } catch (ParseException e) 
            {
            	result.Imported = new Date();
    		}
        }
    	
        if (reader.isNull(3))
        	result.CacheCount = 0;
        else
        	result.CacheCount = reader.getInt(3);
        
        return result;
    }

}
