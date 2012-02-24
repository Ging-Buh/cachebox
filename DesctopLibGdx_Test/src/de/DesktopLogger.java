package de;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import CB_Core.Log.ILog;

public class DesktopLogger implements ILog
{
	private static Logger LOGGER = Logger.getLogger("CACHEBOX");
	
	public DesktopLogger()
	{
		
		BasicConfigurator.configure();		
		CB_Core.Log.Logger.Add(this);
	}
	
	
	@Override
	public void receiveLog(String Msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveShortLog(String Msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveLogCat(String Msg) {
	LOGGER.info(Msg);
	
	}
	
}

