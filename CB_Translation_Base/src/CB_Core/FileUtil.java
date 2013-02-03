package CB_Core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class FileUtil
{
	/**
	 * Überprüft ob ein File exestiert!
	 * 
	 * @param filename
	 * @return true, wenn das File Exestiert, ansonsten false.
	 */
	public static boolean FileExists(String filename)
	{
		File file = new File(filename);
		return file.exists();
	}

	/**
	 * Überprüft ob ein Ordner exestiert und legt ihn an, wenn er nicht exestiert.
	 * 
	 * @param folder
	 *            Pfad des Ordners
	 * @return true wenn er exestiert oder Angelegt wurde. false wenn das Anlegen nicht Funktioniert hat.
	 */
	public static boolean DirectoryExists(String folder)
	{
		File f = new File(folder);
		if (f.isDirectory()) return true;
		else
		{
			// have the object build the directory structure, if needed.
			return f.mkdirs();
		}
	}

	public static String GetFileExtension(String filename)
	{
		int dotposition = filename.lastIndexOf(".");
		String ext = filename.substring(dotposition + 1, filename.length());
		return ext;
	}

	public static String GetFileNameWithoutExtension(String filename)
	{
		int dotposition = filename.lastIndexOf(".");
		if (dotposition >= 0) filename = filename.substring(0, dotposition);
		int slashposition = filename.lastIndexOf("/");
		if (slashposition >= 0) filename = filename.substring(slashposition + 1, filename.length());
		return filename;

	}

	public static String GetFileName(String filename)
	{
		int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
		if (slashposition >= 0) filename = filename.substring(slashposition + 1, filename.length());
		return filename;

	}

	public static String GetDirectoryName(String filename)
	{
		int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
		if (slashposition >= 0) filename = filename.substring(0, slashposition);
		return filename;
	}

	public static String getRelativePath(String targetPath, String basePath, String pathSeparator)
	{ // We need the -1 argument to split to make sure we get a trailing
		// "" token if the base ends in the path separator and is therefore
		// a directory. We require directory paths to end in the path
		// separator -- otherwise they are indistinguishable from files.
		String[] base = basePath.split(Pattern.quote(pathSeparator), -1);
		String[] target = targetPath.split(Pattern.quote(pathSeparator), 0);
		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		String common = "";
		int commonIndex = 0;
		for (int i = 0; i < target.length && i < base.length; i++)
		{
			if (target[i].equals(base[i]))
			{
				common += target[i] + pathSeparator;
				commonIndex++;
			}
			else
				break;
		}

		if (commonIndex == 0)
		{ // Whoops -- not even a single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized. Return the target path.
			return targetPath;
			// This should never happen when all absolute paths
			// begin with / as in *nix.
		}
		String relative = "";
		if (base.length == commonIndex)
		{
			// Comment this out if you prefer that a relative path not start with ./
			// relative = "." + pathSeparator;
		}
		else
		{
			int numDirsUp = base.length - commonIndex;
			// The number of directories we have to backtrack is the length of
			// the base path MINUS the number of common path elements, minus
			// one because the last element in the path isn't a directory.
			for (int i = 1; i <= (numDirsUp); i++)
			{
				relative += ".." + pathSeparator;
			}
		}
		relative += targetPath.substring(common.length());
		return relative;
	}

	public static String RemoveInvalidFatChars(String str)
	{
		String[] invalidChars = new String[]
			{ ":", "\\", "/", "<", ">", "?", "*", "|", "\"", ";", "#" };

		for (int i = 0; i < invalidChars.length; i++)
			str = str.replace(invalidChars[i], "");

		return str;
	}

	
	/**
	 * Gibt eine ArrayList<File> zurück, die alle Files mit der Endung gpx enthält.
	 * 
	 * @param directory
	 * @param files
	 * @return
	 */
	public static ArrayList<File> recursiveDirectoryReader(File directory, ArrayList<File> files)
	{
		return recursiveDirectoryReader(directory, files, "gpx", false);
	}

	/**
	 * Gibt eine ArrayList<File> zurück, die alle Files mit der angegebenen Endung haben.
	 * 
	 * @param directory
	 * @param files
	 * @return
	 */
	public static ArrayList<File> recursiveDirectoryReader(File directory, ArrayList<File> files, final String Endung, boolean exludeHides)
	{

		File[] filelist = directory.listFiles(new FilenameFilter()
		{

			@Override
			public boolean accept(File dir, String filename)
			{

				return filename.contains("." + Endung);
			}
		});

		for (File localFile : filelist)
			files.add(localFile);

		File[] directories = directory.listFiles();
		for (File recursiveDir : directories)
		{
			if (recursiveDir.isDirectory())
			{
				if (!(exludeHides && recursiveDir.getName().startsWith(".")))
				{
					recursiveDirectoryReader(recursiveDir, files, Endung, exludeHides);
				}

			}

		}
		return files;
	}

}
