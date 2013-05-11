package CB_Core.Import;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.http.util.ByteArrayBuffer;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DB.Database;
import CB_Core.DB.Database.Parameters;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;

public class DescriptionImageGrabber
{

	public static class Segment
	{
		public int start;
		public int ende;
		public String text;
	}

	public static ArrayList<Segment> Segmentize(String text, String leftSeperator, String rightSeperator)
	{
		ArrayList<Segment> result = new ArrayList<Segment>();

		if (text == null)
		{
			return result;
		}

		int idx = 0;

		while (true)
		{
			int leftIndex = text.toLowerCase().indexOf(leftSeperator, idx);

			if (leftIndex == -1) break;

			leftIndex += leftSeperator.length();

			int rightIndex = text.toLowerCase().indexOf(rightSeperator, leftIndex);

			if (rightIndex == -1) break;

			// ignoriere URLs, die zu lang sind
			// if (text.length() > 1024)
			if ((rightIndex - leftIndex) > 1024)
			{
				idx = rightIndex;
				continue;
			}

			int forward = leftIndex + 50;

			if (forward > text.length())
			{
				forward = text.length();
			}

			// Test, ob es sich um ein eingebettetes Bild handelt
			if (text.substring(leftIndex, forward).toLowerCase().contains("data:image/"))
			{
				idx = rightIndex;
				continue;
			}

			// Abschnitt gefunden
			Segment curSegment = new Segment();
			curSegment.start = leftIndex;
			curSegment.ende = rightIndex;
			curSegment.text = text.substring(leftIndex, rightIndex/* - leftIndex */);
			result.add(curSegment);

			idx = rightIndex;
		}

		return result;
	}

	public static String RemoveSpaces(String line)
	{
		String dummy = line.replace("\n", "");
		dummy = dummy.replace("\r", "");
		dummy = dummy.replace(" ", "");
		return dummy;
	}

	public static String BuildImageFilename(String GcCode, URI uri)
	{
		String imagePath = Config.settings.DescriptionImageFolder.getValue() + "/" + GcCode.substring(0, 4);
		if (Config.settings.DescriptionImageFolderLocal.getValue().length() > 0) imagePath = Config.settings.DescriptionImageFolderLocal
				.getValue();

		// String uriName = url.Substring(url.LastIndexOf('/') + 1);
		// int idx = uri.AbsolutePath.LastIndexOf('.');
		// //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		int idx = uri.getPath().lastIndexOf('.');
		// String extension = (idx >= 0) ? uri.AbsolutePath.Substring(idx) :
		// ".";!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		String extension = (idx >= 0) ? uri.getPath().substring(idx) : ".";

		// return imagePath + "\\" + GcCode +
		// Global.sdbm(uri.AbsolutePath).ToString() + extension;!!!!!!!!!!!!!
		return imagePath + "/" + GcCode + GlobalCore.sdbm(uri.getPath()) + extension;
	}

	public static String ResolveImages(Cache Cache, String html, boolean suppressNonLocalMedia, LinkedList<String> NonLocalImages,
			LinkedList<String> NonLocalImagesUrl)
	{
		/*
		 * NonLocalImages = new List<string>(); NonLocalImagesUrl = new List<string>();
		 */

		URI baseUri;
		try
		{
			baseUri = URI.create(Cache.Url);
		}
		catch (Exception exc)
		{
			/*
			 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " + exc.ToString());
			 * #endif
			 */
			baseUri = null;
		}

		if (baseUri == null)
		{
			Cache.Url = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + Cache.GcCode;
			try
			{
				baseUri = URI.create(Cache.Url);
			}
			catch (Exception exc)
			{
				/*
				 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve '" + Cache.Url + "': " +
				 * exc.ToString()); #endif
				 */
				return html;
			}
		}

		// String htmlNoSpaces = RemoveSpaces(html);

		ArrayList<Segment> imgTags = Segmentize(html, "<img", ">");

		int delta = 0;

		for (Segment img : imgTags)
		{
			int srcIdx = img.text.toLowerCase().indexOf("src=");
			int srcStart = img.text.indexOf('"', srcIdx + 4);
			int srcEnd = img.text.indexOf('"', srcStart + 1);

			if (srcIdx != -1 && srcStart != -1 && srcEnd != -1)
			{
				String src = img.text.substring(srcStart + 1, srcEnd/*
																	 * - srcStart - 1
																	 */);
				try
				{
					URI imgUri = URI.create(/* baseUri, */src); // NICHT
																// ORGINAL!!!!!!!!!
					String localFile = BuildImageFilename(Cache.GcCode, imgUri);

					if (FileIO.FileExists(localFile))
					{
						int idx = 0;

						while ((idx = html.indexOf(src, idx)) >= 0)
						{
							if (idx >= (img.start + delta) && (idx <= img.ende + delta))
							{
								String head = html.substring(0, img.start + delta);
								String tail = html.substring(img.ende + delta);
								String uri = "file://" + localFile;
								String body = img.text.replace(src, uri);

								delta += (uri.length() - src.length());
								html = head + body + tail;
							}
							idx++;
						}
					}
					else
					{
						NonLocalImages.add(localFile);
						NonLocalImagesUrl.add(imgUri.toString());

						if (suppressNonLocalMedia)
						{
							// Wenn nicht-lokale Inhalte unterdrückt werden
							// sollen,
							// wird das <img>-Tag vollständig entfernt
							html = html.substring(0, img.start - 4 + delta) + html.substring(img.ende + 1 + delta);
							delta -= 5 + img.ende - img.start;
						}

					}
				}
				catch (Exception exc)
				{
					/*
					 * #if DEBUG Global.AddLog( "DescriptionImageGrabber.ResolveImages: failed to resolve relative uri. Base '" + baseUri +
					 * "', relative '" + src + "': " + exc.ToString()); #endif
					 */
				}
			}
		}

		return html;
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

	public static LinkedList<String> GetAllImages(Cache Cache)
	{

		LinkedList<String> images = new LinkedList<String>();

		URI baseUri;
		try
		{
			baseUri = URI.create(Cache.Url);
		}
		catch (Exception exc)
		{
			baseUri = null;
		}

		if (baseUri == null)
		{
			Cache.Url = "http://www.geocaching.com/seek/cache_details.aspx?wp=" + Cache.GcCode;
			try
			{
				baseUri = URI.create(Cache.Url);
			}
			catch (Exception exc)
			{
				return images;
			}
		}

		ArrayList<Segment> imgTags = Segmentize(Cache.shortDescription, "<img", ">");

		imgTags.addAll(Segmentize(Cache.longDescription, "<img", ">"));

		for (Segment img : imgTags)
		{
			int srcStart = -1;
			int srcEnd = -1;
			int srcIdx = img.text.toLowerCase().indexOf("src=");
			if (srcIdx != -1) srcStart = img.text.indexOf('"', srcIdx + 4);
			if (srcStart != -1) srcEnd = img.text.indexOf('"', srcStart + 1);

			if (srcIdx != -1 && srcStart != -1 && srcEnd != -1)
			{
				String src = img.text.substring(srcStart + 1, srcEnd);
				try
				{
					URI imgUri = URI.create(src);

					images.add(imgUri.toString());

				}
				catch (Exception exc)
				{
				}
			}
		}

		return images;
	}

	public static LinkedList<URI> GetImageUris(String html, String baseUrl)
	{

		LinkedList<URI> images = new LinkedList<URI>();

		// chk baseUrl
		try
		{
			URI.create(baseUrl);
		}
		catch (Exception exc)
		{
			return images;
		}

		ArrayList<Segment> imgTags = Segmentize(html, "<img", ">");

		for (Segment img : imgTags)
		{
			int srcStart = -1;
			int srcEnd = -1;
			int srcIdx = img.text.toLowerCase().indexOf("src=");
			if (srcIdx != -1) srcStart = img.text.indexOf('"', srcIdx + 4);
			if (srcStart != -1) srcEnd = img.text.indexOf('"', srcStart + 1);

			if (srcIdx != -1 && srcStart != -1 && srcEnd != -1)
			{
				String src = img.text.substring(srcStart + 1, srcEnd);
				try
				{
					URI imgUri = URI.create(src);

					images.add(imgUri);

				}
				catch (Exception exc)
				{
				}
			}
		}

		return images;
	}

	public static void GrabImagesSelectedByCache(ImporterProgress ip, boolean descriptionImagesUpdated, boolean additionalImagesUpdated,
			long id, String gcCode, String name, String description, String url)
	{
		boolean imageLoadError = false;

		if (!descriptionImagesUpdated)
		{
			ip.ProgressChangeMsg("importImages", "Importing Description Images for " + gcCode);

			LinkedList<URI> imgUris = GetImageUris(description, url);

			for (URI uri : imgUris)
			{
				String local = BuildImageFilename(gcCode, uri);

				ip.ProgressChangeMsg("importImages", "Importing Description Images for " + gcCode + " - Download: " + uri);

				// build URL
				for (int j = 0; j < 1 /* && !parent.Cancel */; j++)
				{
					if (Download(uri.toString(), local))
					{
						// Next image
						DeleteMissingImageInformation(local);
						break;
					}
					else
					{
						imageLoadError = HandleMissingImages(imageLoadError, uri, local);
					}
				}
			}

			descriptionImagesUpdated = true;

			if (imageLoadError == false)
			{
				Parameters args = new Parameters();
				args.put("DescriptionImagesUpdated", descriptionImagesUpdated);
				Database.Data.update("Caches", args, "Id = ?", new String[]
					{ String.valueOf(id) });
			}
		}

		if (!additionalImagesUpdated)
		{
			// Get additional images (Spoiler)

			// Liste aller Spoiler Images für diesen Cache erstellen
			// anhand dieser Liste kann überprüft werden, ob ein Spoiler schon geladen ist und muss nicht ein 2. mal geladen werden.
			// Außerdem können anhand dieser Liste veraltete Spoiler identifiziert werden, die gelöscht werden können / müssen
			String[] files = getFilesInDirectory(Config.settings.SpoilerFolder.getValue(), gcCode);
			String[] filesLocal = getFilesInDirectory(Config.settings.SpoilerFolderLocal.getValue(), gcCode);
			ArrayList<String> afiles = new ArrayList<String>();
			for (String file : files)
				afiles.add(file);
			for (String file : filesLocal)
				afiles.add(file);

			{
				ip.ProgressChangeMsg("importImages", "Importing Spoiler Images for " + gcCode);
				HashMap<String, URI> allimgDict = GroundspeakAPI.GetAllImageLinks(Config.GetAccessToken(true), gcCode);

				if (allimgDict == null) return;

				for (String key : allimgDict.keySet())
				{
					URI uri = allimgDict.get(key);
					if (uri.toString().startsWith("http://img.geocaching.com/cache/log")) continue; // LOG-Image

					ip.ProgressChangeMsg("importImages", "Importing Spoiler Images for " + gcCode + " - Download: " + uri);

					String decodedImageName = key;

					String local = BuildAdditionalImageFilename(gcCode, decodedImageName, uri);
					String filename = local.substring(local.lastIndexOf('/') + 1);
					// überprüfen, ob dieser Spoiler bereits geladen wurde
					if (afiles.contains(filename))
					{
						// wenn ja, dann aus der Liste der aktuell vorhandenen Spoiler entfernen und mit dem nächsten Spoiler weiter machen
						// dieser Spoiler muss jetzt nicht mehr geladen werden da er schon vorhanden ist.
						afiles.remove(filename);
						continue;
					}

					// build URL
					for (int j = 0; j < 1 /* && !parent.Cancel */; j++)
					{
						if (Download(uri.toString(), local))
						{
							// Next image
							DeleteMissingImageInformation(local);
							break;
						}
						else
						{
							imageLoadError = HandleMissingImages(imageLoadError, uri, local);
						}
						System.gc();
					}
				}

				additionalImagesUpdated = true;

				if (imageLoadError == false)
				{
					Parameters args = new Parameters();
					args.put("ImagesUpdated", additionalImagesUpdated);
					Database.Data.update("Caches", args, "Id = ?", new String[]
						{ String.valueOf(id) });
					// jetzt können noch alle "alten" Spoiler gelöscht werden. "alte" Spoiler sind die, die auf der SD vorhanden sind, aber
					// nicht als Link über die API gemeldet wurden
					// Alle Spoiler in der Liste afiles sind "alte"
					for (String file : afiles)
					{
						File f = new File(Config.settings.SpoilerFolder.getValue() + "/" + gcCode.substring(0, 4) + '/' + file);
						try
						{
							f.delete();
						}
						catch (Exception ex)
						{
							Logger.Error("DescriptionImageGrabber - GrabImagesSelectedByCache - DeleteSpoiler", ex.getMessage());
						}
					}
				}

			}
		}
	}

	private static String[] getFilesInDirectory(String path, final String GcCode)
	{
		String imagePath = path + "/" + GcCode.substring(0, 4);
		boolean imagePathDirExists = FileIO.DirectoryExists(imagePath);

		if (imagePathDirExists)
		{
			File dir = new File(imagePath);
			FilenameFilter filter = new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String filename)
				{

					filename = filename.toLowerCase();
					if (filename.indexOf(GcCode.toLowerCase()) == 0)
					{
						return true;
					}
					return false;
				}
			};
			String[] files = dir.list(filter);
			return files;
		}
		return new String[0];
	}

	public static String BuildAdditionalImageFilename(String GcCode, String ImageName, URI uri)
	{
		String imagePath = Config.settings.SpoilerFolder.getValue() + "/" + GcCode.substring(0, 4);

		if (Config.settings.SpoilerFolderLocal.getValue().length() > 0) imagePath = Config.settings.SpoilerFolderLocal.getValue() + "/"
				+ GcCode.substring(0, 4);

		ImageName = ImageName.replace("[/:*?\"<>|]", "");
		ImageName = ImageName.replace("\\", "");
		ImageName = ImageName.replace("\n", "");
		ImageName = ImageName.trim();

		int idx = uri.toString().lastIndexOf('.');
		String extension = (idx >= 0) ? uri.toString().substring(idx) : ".";

		return imagePath + "/" + GcCode + " - " + ImageName + extension;
	}

	private static boolean HandleMissingImages(boolean imageLoadError, URI uri, String local)
	{
		try
		{
			File file = new File(local + "_broken_link.txt");
			if (!file.exists())
			{
				if (file.exists())
				{
					// After first try, we can be sure that the image cannot be loaded.
					// At this point mark the image as loaded and go ahead.
					file.renameTo(file);
				}
				else
				{
					// Crate a local file for marking it that it could not load one time.
					// Maybe the link is broken temporarely. So try it next time once again.
					try
					{
						String text = "Could not load image from:" + uri;
						BufferedWriter out = new BufferedWriter(new FileWriter(local + ".1st"));
						out.write(text);
						out.close();
						imageLoadError = true;
					}
					catch (IOException e)
					{
						System.out.println("Exception ");
					}
				}
			}
		}
		catch (Exception ex)
		{
			// Global.AddLog("HandleMissingImages (uri=" + uri + ") (local=" + local + ") - " + ex.ToString());
		}
		return imageLoadError;
	}

	private static void DeleteMissingImageInformation(String local)
	{
		File file = new File(local + "_broken_link.txt");
		if (file.exists())
		{
			file.delete();
		}

		file = new File(local + ".1st");
		if (file.exists())
		{
			file.delete();
		}
	}

}
