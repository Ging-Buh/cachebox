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
	public void receiveLog(String Msg)
	{
		Msg = Msg.replace("\r", "");
		LOGGER.debug(Msg);

	}

	@Override
	public void receiveShortLog(String Msg)
	{

	}

	@Override
	public void receiveLogCat(String Msg)
	{
		Msg = Msg.replace("\r", "");
		LOGGER.info(Msg);

	}

}
