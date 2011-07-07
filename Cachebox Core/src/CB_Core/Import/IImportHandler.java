package CB_Core.Import;

import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;

public interface IImportHandler {
	
	void handleCache( Cache cache );
	void handleLog( LogEntry log );

}
