package CB_Core.Import;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.util.ByteArrayBuffer;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
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

			if (!FileIO.DirectoryExists(localDir)) return false;

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

}
