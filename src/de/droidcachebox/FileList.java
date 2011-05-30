package de.droidcachebox;

import java.io.File;
import java.util.ArrayList;

public class FileList extends ArrayList<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2454564654L;

	public FileList(String path, String extension)
	{
        File dir = new File(path);
        String[] files = dir.list();
        if (!(files == null))
        {
	        if (files.length>0)
	        {
		        for (String file : files)
			        {
		        		if (Global.GetFileExtension(file).equalsIgnoreCase(extension))
		        		{
		        			File newfile = new File(file);
		        			this.add(newfile);
		        		}
			        }
	        }
        }
	}

}
