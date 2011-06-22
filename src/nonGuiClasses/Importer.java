package nonGuiClasses;

import de.droidcachebox.Events.ProgresssChangedEventList;

public class Importer 
{
	public void importGC()
	{
		ProgresssChangedEventList.Call("import Gc.com", "", 0);
		
		
	}
	
	public void importGpx()
	{
		ProgresssChangedEventList.Call("import GPX", "", 10);
		
	}
	
	public void importGcVote()
	{
		ProgresssChangedEventList.Call("import GcVote", "", 0);
				
	}
	
	public void importImages()
	{
		ProgresssChangedEventList.Call("import Images", "", 0);
	
	}
	
	public void importMaps()
	{
		ProgresssChangedEventList.Call("import Map", "", 0);
		
	}
	
	public void importMail()
	{
		ProgresssChangedEventList.Call("import from Mail", "", 0);
		
	}
	
}
