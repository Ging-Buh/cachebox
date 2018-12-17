package cb_server.Import;

import CB_Core.Import.ImporterProgress;
import cb_server.Views.CB_ViewBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerImporterProgress extends ImporterProgress {
	private Logger log = LoggerFactory.getLogger(CB_ViewBase.class);;
	@Override
	public void ProgressInkrement(String Name, String Msg, Boolean Done) {
		super.ProgressInkrement(Name, Msg, Done);
		log.info("ProgressIncrement: " + Name + "(" + getProgress() + "%)" + " - " + Msg + " - Done: " + Done);
	}
	
	@Override
	public void ProgressChangeMsg(String Name, String Msg) {
		super.ProgressChangeMsg(Name, Msg);
		log.info("ProgressChangeMsg: " + Name + "(" + getProgress() + "%)" + "- " + Msg);
	}
}
