package de.cachebox_test.CB_Texturepacker;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CB_UI.CB_Texturepacker.IImageprozessor;
import CB_UI.CB_Texturepacker.Rect_Base;
import CB_UI.CB_Texturepacker.Settings;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.badlogic.gdx.utils.Array;

public class ImageProcessor implements IImageprozessor
{
	static private final Bitmap emptyImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
	static private Pattern indexPattern = Pattern.compile("(.+)_(\\d+)$");

	private String rootPath;
	private final Settings settings;
	private final HashMap<String, Rect> crcs = new HashMap<String, Rect>();
	private final Array<Rect_Base> rects = new Array<Rect_Base>();

	public ImageProcessor(File rootDir, Settings settings)
	{
		this.settings = settings;

		rootPath = rootDir.getAbsolutePath().replace('\\', '/');
		if (!rootPath.endsWith("/")) rootPath += "/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Texturepacker.IImageprozessor#addImage(java.io.File)
	 */
	@Override
	public void addImage(File file)
	{
		Bitmap image;
		try
		{
			// image = ImageIO.read(file);
			image = BitmapFactory.decodeFile(file.getAbsolutePath());
			image.prepareToDraw();
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Error reading image: " + file, ex);
		}

		// Strip root dir off front of image path.
		String name = file.getAbsolutePath().replace('\\', '/');
		if (!name.startsWith(rootPath)) throw new RuntimeException("Path '" + name + "' does not start with root: " + rootPath);
		name = name.substring(rootPath.length());

		// Strip extension.
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex != -1) name = name.substring(0, dotIndex);

		Rect rect = null;

		// Strip ".9" from file name, read ninepatch split pixels, and strip ninepatch split pixels.
		int[] splits = null;
		int[] pads = null;
		if (name.endsWith(".9"))
		{
			name = name.substring(0, name.length() - 2);
			splits = getSplits(image, name);
			pads = getPads(image, name, splits);
			// Strip split pixels.
			// BufferedImage newImage = new BufferedImage(image.getWidth() - 2, image.getHeight() - 2, BufferedImage.TYPE_4BYTE_ABGR);
			Bitmap newImage = Bitmap.createBitmap(image.getWidth() - 2, image.getHeight() - 2, Config.ARGB_4444);

			Canvas g = new Canvas(newImage);

			// g.drawImage(image, 0, 0, newImage.getWidth(), newImage.getHeight(), 1, 1, image.getWidth() - 1, image.getHeight() - 1, null);
			g.drawBitmap(image, new android.graphics.Rect(1, 1, image.getWidth() - 1, image.getHeight() - 1), new android.graphics.Rect(0,
					0, newImage.getWidth(), newImage.getHeight()), new Paint());

			image = newImage;
			// Ninepatches won't be rotated or whitespace stripped.
			rect = new Rect(image, 0, 0, image.getWidth(), image.getHeight());
			rect.splits = splits;
			rect.pads = pads;
			rect.canRotate = false;
		}

		// Strip digits off end of name and use as index.
		Matcher matcher = indexPattern.matcher(name);
		int index = -1;
		if (matcher.matches())
		{
			name = matcher.group(1);
			index = Integer.parseInt(matcher.group(2));
		}

		if (rect == null)
		{
			rect = createRect(image);
			if (rect == null)
			{
				System.out.println("Ignoring blank input image: " + name);
				return;
			}
		}

		rect.name = name;
		rect.index = index;

		if (settings.alias)
		{
			String crc = hash((Bitmap) rect.image);
			Rect existing = crcs.get(crc);
			if (existing != null)
			{
				System.out.println(rect.name + " (alias of " + existing.name + ")");
				existing.aliases.add(rect);
				return;
			}
			crcs.put(crc, rect);
		}

		rects.add(rect);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Texturepacker.IImageprozessor#getImages()
	 */
	@Override
	public Array<Rect_Base> getImages()
	{
		return rects;
	}

	/** Strips whitespace and returns the rect, or null if the image should be ignored. */
	private Rect createRect(Bitmap source)
	{
		// WritableRaster alphaRaster = source.getAlphaRaster();

		if (!source.hasAlpha() || (!settings.stripWhitespaceX && !settings.stripWhitespaceY))
		{
			System.out.println("Return new Rect" + source.getWidth() + ":" + source.getHeight() + " === " + 0 + "," + 0 + "/"
					+ source.getWidth() + "," + source.getHeight());
			return new Rect(source, 0, 0, source.getWidth(), source.getHeight());
		}

		final int[] a = new int[4];
		int top = 0;
		int bottom = source.getHeight();
		if (settings.stripWhitespaceX)
		{
			outer: for (int y = 0; y < source.getHeight(); y++)
			{
				for (int x = 0; x < source.getWidth(); x++)
				{
					getDataElements(source, x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				top++;
			}
			outer: for (int y = source.getHeight(); --y >= top;)
			{
				for (int x = 0; x < source.getWidth(); x++)
				{
					getDataElements(source, x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				bottom--;
			}
		}
		int left = 0;
		int right = source.getWidth();
		if (settings.stripWhitespaceY)
		{
			outer: for (int x = 0; x < source.getWidth(); x++)
			{
				for (int y = top; y < bottom; y++)
				{
					getDataElements(source, x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				left++;
			}
			outer: for (int x = source.getWidth(); --x >= left;)
			{
				for (int y = top; y < bottom; y++)
				{
					getDataElements(source, x, y, a);
					int alpha = a[0];
					if (alpha < 0) alpha += 256;
					if (alpha > settings.alphaThreshold) break outer;
				}
				right--;
			}
		}
		int newWidth = right - left;
		int newHeight = bottom - top;
		if (newWidth <= 0 || newHeight <= 0)
		{
			if (settings.ignoreBlankImages)
			{
				System.out.println("Return Rect = NULL (ignoreBlankImages)");
				return null;
			}
			else
			{
				System.out.println("Return Rect = emptyImage (!ignoreBlankImages)");
				return new Rect(emptyImage, 0, 0, 1, 1);
			}

		}

		System.out.println("Return new Rect" + source.getWidth() + ":" + source.getHeight() + " === " + left + "," + top + "/" + newWidth
				+ "," + newHeight);
		source.prepareToDraw();
		return new Rect(source, left, top, newWidth, newHeight);
	}

	private String splitError(int x, int y, int[] rgba, String name)
	{
		throw new RuntimeException("Invalid " + name + " ninepatch split pixel at " + x + ", " + y + ", rgba: " + rgba[0] + ", " + rgba[1]
				+ ", " + rgba[2] + ", " + rgba[3]);
	}

	/**
	 * Returns the splits, or null if the image had no splits or the splits were only a single region. Splits are an int[4] that has left,
	 * right, top, bottom.
	 */
	private int[] getSplits(Bitmap image, String name)
	{
		// WritableRaster raster = image.getRaster();

		int startX = getSplitPoint(image, name, 1, 0, true, true);
		int endX = getSplitPoint(image, name, startX, 0, false, true);
		int startY = getSplitPoint(image, name, 0, 1, true, false);
		int endY = getSplitPoint(image, name, 0, startY, false, false);

		// Ensure pixels after the end are not invalid.
		getSplitPoint(image, name, endX + 1, 0, true, true);
		getSplitPoint(image, name, 0, endY + 1, true, false);

		// No splits, or all splits.
		if (startX == 0 && endX == 0 && startY == 0 && endY == 0) return null;

		// Subtraction here is because the coordinates were computed before the 1px border was stripped.
		if (startX != 0)
		{
			startX--;
			endX = image.getWidth() - 2 - (endX - 1);
		}
		else
		{
			// If no start point was ever found, we assume full stretch.
			endX = image.getWidth() - 2;
		}
		if (startY != 0)
		{
			startY--;
			endY = image.getHeight() - 2 - (endY - 1);
		}
		else
		{
			// If no start point was ever found, we assume full stretch.
			endY = image.getHeight() - 2;
		}

		return new int[]
			{ startX, endX, startY, endY };
	}

	/**
	 * Returns the pads, or null if the image had no pads or the pads match the splits. Pads are an int[4] that has left, right, top,
	 * bottom.
	 */
	private int[] getPads(Bitmap image, String name, int[] splits)
	{
		// WritableRaster raster = image.getRaster();

		int bottom = image.getHeight() - 1;
		int right = image.getWidth() - 1;

		int startX = getSplitPoint(image, name, 1, bottom, true, true);
		int startY = getSplitPoint(image, name, right, 1, true, false);

		// No need to hunt for the end if a start was never found.
		int endX = 0;
		int endY = 0;
		if (startX != 0) endX = getSplitPoint(image, name, startX + 1, bottom, false, true);
		if (startY != 0) endY = getSplitPoint(image, name, right, startY + 1, false, false);

		// Ensure pixels after the end are not invalid.
		getSplitPoint(image, name, endX + 1, bottom, true, true);
		getSplitPoint(image, name, right, endY + 1, true, false);

		// No pads.
		if (startX == 0 && endX == 0 && startY == 0 && endY == 0)
		{
			return null;
		}

		// -2 here is because the coordinates were computed before the 1px border was stripped.
		if (startX == 0 && endX == 0)
		{
			startX = -1;
			endX = -1;
		}
		else
		{
			if (startX > 0)
			{
				startX--;
				endX = image.getWidth() - 2 - (endX - 1);
			}
			else
			{
				// If no start point was ever found, we assume full stretch.
				endX = image.getWidth() - 2;
			}
		}
		if (startY == 0 && endY == 0)
		{
			startY = -1;
			endY = -1;
		}
		else
		{
			if (startY > 0)
			{
				startY--;
				endY = image.getHeight() - 2 - (endY - 1);
			}
			else
			{
				// If no start point was ever found, we assume full stretch.
				endY = image.getHeight() - 2;
			}
		}

		int[] pads = new int[]
			{ startX, endX, startY, endY };

		if (splits != null && Arrays.equals(pads, splits))
		{
			return null;
		}

		return pads;
	}

	/**
	 * Hunts for the start or end of a sequence of split pixels. Begins searching at (startX, startY) then follows along the x or y axis
	 * (depending on value of xAxis) for the first non-transparent pixel if startPoint is true, or the first transparent pixel if startPoint
	 * is false. Returns 0 if none found, as 0 is considered an invalid split point being in the outer border which will be stripped.
	 */
	private int getSplitPoint(Bitmap raster, String name, int startX, int startY, boolean startPoint, boolean xAxis)
	{
		int[] rgba = new int[4];

		int next = xAxis ? startX : startY;
		int end = xAxis ? raster.getWidth() : raster.getHeight();
		int breakA = startPoint ? 255 : 0;

		int x = startX;
		int y = startY;
		while (next != end)
		{
			if (xAxis) x = next;
			else
				y = next;

			getDataElements(raster, x, y, rgba);

			if (rgba[3] == breakA) return next;

			if (!startPoint && (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 255)) splitError(x, y, rgba, name);

			next++;
		}

		return 0;
	}

	private void getDataElements(Bitmap raster, int x, int y, int[] rgba)
	{
		int mColor = raster.getPixel(x, y);

		rgba[0] = Color.red(mColor);
		rgba[1] = Color.green(mColor);
		rgba[2] = Color.blue(mColor);
		rgba[3] = Color.alpha(mColor);
	}

	static private String hash(Bitmap image)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			int width = image.getWidth();
			int height = image.getHeight();
			if (image.getConfig() != Bitmap.Config.ARGB_8888)
			{
				Bitmap newImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);// new BufferedImage(width, height,
																						// BufferedImage.TYPE_INT_ARGB);

				Canvas g = new Canvas(newImage);
				g.drawBitmap(image, 0, 0, new Paint());
				// newImage.getGraphics().drawImage(image, 0, 0, null);
				image = newImage;

				System.out.println("ImageProzessor Redraw Image ARGB_8888");
			}
			// WritableRaster raster = image.getRaster();
			int[] pixels = new int[width * height];
			image.getPixels(pixels, 0, width, 0, 0, width, height);

			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int rgba = pixels[x];
					digest.update((byte) (rgba >> 24));
					digest.update((byte) (rgba >> 16));
					digest.update((byte) (rgba >> 8));
					digest.update((byte) rgba);
				}
			}
			return new BigInteger(1, digest.digest()).toString(16);
		}
		catch (NoSuchAlgorithmException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}