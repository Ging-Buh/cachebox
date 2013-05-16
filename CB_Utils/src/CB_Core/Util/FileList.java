package CB_Core.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileList extends ArrayList<File> implements Comparator<File>
{
	private static final long serialVersionUID = 2454564654L;

	public FileList(String path, String extension)
	{
		ini(path, extension, false);
	}

	public FileList(String path, String extension, boolean AbsolutePath)
	{
		ini(path, extension, AbsolutePath);
	}

	private void ini(String path, String extension, boolean AbsolutePath)
	{
		File dir = new File(path);
		String[] files = dir.list();
		String absolutePath = AbsolutePath ? path + "/" : "";
		if (!(files == null))
		{
			if (files.length > 0)
			{
				for (String file : files)
				{
					if (FileIO.GetFileExtension(file).equalsIgnoreCase(extension))
					{
						File newfile = new File(absolutePath + file);
						this.add(newfile);
					}
				}
			}
		}
		Resort();
	}

	public void Resort()
	{
		Collections.sort(this, this);
	}

	@Override
	public int compare(File object1, File object2)
	{
		if (object1.lastModified() > object2.lastModified()) return 1;
		else if (object1.lastModified() < object2.lastModified()) return -1;
		else
			return 0;
	}

}
