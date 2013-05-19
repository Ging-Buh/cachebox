package CB_Core.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.http.util.ByteArrayBuffer;

public class FileIO
{
	/**
	 * �berpr�ft ob ein File exestiert!
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
	 * �berpr�ft ob ein Ordner exestiert und legt ihn an, wenn er nicht exestiert.
	 * 
	 * @param folder
	 *            Pfad des Ordners
	 * @return true, wenn er existiert oder angelegt wurde. false, wenn das Anlegen nicht funktioniert hat.
	 */
	public static boolean createDirectory(String folder)
	{
		File f = new File(folder);
		if (f.isDirectory()) return true;
		else
		{
			// have the object build the directory structure, if needed.
			return f.mkdirs();
		}
	}

	public static boolean DirectoryExists(String folder)
	{
		File f = new File(folder);
		if (f.isDirectory()) return true;
		return false;
	}

	public static String GetFileExtension(String filename)
	{
		int dotposition = filename.lastIndexOf(".");
		String ext = "";
		if (dotposition > -1)
		{
			ext = filename.substring(dotposition + 1, filename.length());
		}

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

	public static Boolean Download(String uri, String local)
	{
		try
		{
			String localDir = local.substring(0, local.lastIndexOf("/"));

			if (!FileIO.createDirectory(localDir)) return false;

			URL aURL = new URL(uri.replace("&amp;", "&"));

			File file = new File(local);

			URLConnection con = aURL.openConnection();

			InputStream is = con.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1)
			{
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Gibt eine ArrayList<File> zur�ck, die alle Files mit der Endung gpx enth�lt.
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
	 * Gibt eine ArrayList<File> zur�ck, die alle Files mit der angegebenen Endung haben.
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

	public static void deleteDir(File file)
	{
		if (file.isDirectory())
		{

			// directory is empty, then delete it
			if (file.list().length == 0)
			{

				file.delete();
				System.out.println("Directory is deleted : " + file.getAbsolutePath());

			}
			else
			{

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files)
				{
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					deleteDir(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0)
				{
					file.delete();
					System.out.println("Directory is deleted : " + file.getAbsolutePath());
				}
			}

		}
		else
		{
			// if file, then delete it
			file.delete();
			System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

}