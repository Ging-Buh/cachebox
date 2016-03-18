package de.cachebox_test.CB_Texturepacker;

import java.io.IOException;
import java.io.OutputStream;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import CB_UI_Base.CB_Texturepacker.MaxRectsPacker;
import CB_UI_Base.CB_Texturepacker.Page;
import CB_UI_Base.CB_Texturepacker.Rect_Base;
import CB_UI_Base.CB_Texturepacker.Settings;
import CB_UI_Base.CB_Texturepacker.TexturePacker_Base;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * @author Nathan Sweet ; Longri
 */
public class Android_Packer extends TexturePacker_Base {
	@Override
	public TexturePacker_Base getInstanz(File rootDir, Settings settings) {
		return new Android_Packer(rootDir, settings);
	}

	public Android_Packer(File rootDir, Settings settings) {
		this.settings = settings;

		if (settings.pot) {
			if (settings.maxWidth != MathUtils.nextPowerOfTwo(settings.maxWidth))
				throw new RuntimeException("If pot is true, maxWidth must be a power of two: " + settings.maxWidth);
			if (settings.maxHeight != MathUtils.nextPowerOfTwo(settings.maxHeight))
				throw new RuntimeException("If pot is true, maxHeight must be a power of two: " + settings.maxHeight);
		}

		maxRectsPacker = new MaxRectsPacker(settings);
		imageProcessor = new ImageProcessor(rootDir, settings);
	}

	public Android_Packer() {
		super();
		that = this;
		// initial Rect
		new Rect();
	}

	public void writeImages(File outputDir, Array<Page> pages, String packFileName) {
		String imageName = packFileName;
		int dotIndex = imageName.lastIndexOf('.');
		if (dotIndex != -1)
			imageName = imageName.substring(0, dotIndex);

		int fileIndex = 0;
		for (Page page : pages) {
			int width = page.width, height = page.height;
			int paddingX = settings.paddingX;
			int paddingY = settings.paddingY;
			if (settings.duplicatePadding) {
				paddingX /= 2;
				paddingY /= 2;
			}
			width -= settings.paddingX;
			height -= settings.paddingY;
			if (settings.edgePadding) {
				page.x = paddingX;
				page.y = paddingY;
				width += paddingX * 2;
				height += paddingY * 2;
			}
			if (settings.pot) {
				width = MathUtils.nextPowerOfTwo(width);
				height = MathUtils.nextPowerOfTwo(height);
			}
			width = Math.max(settings.minWidth, width);
			height = Math.max(settings.minHeight, height);

			if (settings.forceSquareOutput) {
				if (width > height) {
					height = width;
				} else {
					width = height;
				}
			}

			File outputFile;
			while (true) {
				outputFile = FileFactory.createFile(outputDir, imageName + (fileIndex++ == 0 ? "" : fileIndex) + "." + settings.outputFormat);
				if (!outputFile.exists())
					break;
			}
			page.imageName = outputFile.getName();

			// BufferedImage canvas = new BufferedImage(width, height, getBitmapConfig(settings.format));
			Bitmap canvas = Bitmap.createBitmap(width, height, getBitmapConfig(settings.format));

			// Graphics2D g = (Graphics2D) canvas.getGraphics();
			Canvas g = new Canvas(canvas);

			System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);

			Paint pMag = new Paint();
			pMag.setColor(Color.MAGENTA);
			pMag.setStrokeWidth(0);
			pMag.setStyle(Paint.Style.STROKE);

			for (Rect_Base rect : page.outputRects) {
				int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;
				if (rect.rotated) {
					g.translate(rectX, rectY);
					g.rotate(-90 * MathUtils.degreesToRadians);
					g.translate(-rectX, -rectY);
					g.translate(-(rect.height - settings.paddingY), 0);
				}

				// BufferedImage image = rect.image;
				Bitmap image = (Bitmap) rect.image;

				android.graphics.Rect src = new android.graphics.Rect();
				android.graphics.Rect dst = new android.graphics.Rect();
				Paint p = new Paint();

				if (settings.duplicatePadding) {
					int amountX = settings.paddingX / 2;
					int amountY = settings.paddingY / 2;
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					// Copy corner pixels to fill corners of the padding.

					src.set(0, 0, 1, 1);
					dst.set(rectX - amountX, rectY - amountY, rectX, rectY);
					g.drawBitmap(image, src, dst, p);

					dst.set(rectX + imageWidth, rectY - amountY, rectX + imageWidth + amountX, rectY);
					src.set(imageWidth - 1, 0, imageWidth, 1);
					g.drawBitmap(image, src, dst, p);

					dst.set(rectX - amountX, rectY + imageHeight, rectX, rectY + imageHeight + amountY);
					src.set(0, imageHeight - 1, 1, imageHeight);
					g.drawBitmap(image, src, dst, p);

					dst.set(rectX + imageWidth, rectY + imageHeight, rectX + imageWidth + amountX, rectY + imageHeight + amountY);
					src.set(imageWidth - 1, imageHeight - 1, imageWidth, imageHeight);
					g.drawBitmap(image, src, dst, p);

					// Copy edge pixels into padding.

					dst.set(rectX, rectY - amountY, rectX + imageWidth, rectY);
					src.set(0, 0, imageWidth, 1);
					g.drawBitmap(image, src, dst, p);

					dst.set(rectX, rectY + imageHeight, rectX + imageWidth, rectY + imageHeight + amountY);
					src.set(0, imageHeight - 1, imageWidth, imageHeight);
					g.drawBitmap(image, src, dst, p);

					dst.set(rectX - amountX, rectY, rectX, rectY + imageHeight);
					src.set(0, 0, 1, imageHeight);
					g.drawBitmap(image, src, dst, p);

					dst.set(rectX + imageWidth, rectY, rectX + imageWidth + amountX, rectY + imageHeight);
					src.set(imageWidth - 1, 0, imageWidth, imageHeight);
					g.drawBitmap(image, src, dst, p);

				}

				System.out.println("Writing " + rect.name + "x,y: " + rectX + "," + rectY);
				g.drawBitmap(image, rectX, rectY, null);

				if (rect.rotated) {
					g.translate(rect.height - settings.paddingY, 0);
					g.translate(rectX, rectY);
					g.rotate(90 * MathUtils.degreesToRadians);
					g.translate(-rectX, -rectY);
				}
				if (settings.debug) {
					// p.setColor(Color.MAGENTA);

					g.drawRect(rectX, rectY, rect.width - settings.paddingX - 1 + rectX, rect.height - settings.paddingY - 1 + rectY, pMag);
				}
			}

			if (settings.debug) {
				g.drawRect(0, 0, width - 1, height - 1, pMag);
			}

			try {

				CompressFormat format = CompressFormat.PNG;

				if (settings.outputFormat.equalsIgnoreCase("jpg")) {
					format = CompressFormat.JPEG;
				}

				OutputStream stream = outputFile.getFileOutputStream();
				/* Write bitmap to file using JPEG or PNG and 80% quality hint for JPEG. */
				canvas.compress(format, (int) (settings.jpegQuality * 100), stream);
				stream.close();

			} catch (IOException ex) {
				throw new RuntimeException("Error writing file: " + outputFile, ex);
			}
		}
	}

	private Config getBitmapConfig(Format format) {
		switch (settings.format) {
		case RGBA8888:
		case RGBA4444:
			return Bitmap.Config.ARGB_8888;
		case RGB565:
		case RGB888:
			return Bitmap.Config.RGB_565;
		case Alpha:
			return Bitmap.Config.RGB_565;
		default:
			throw new RuntimeException("Unsupported format: " + settings.format);
		}
	}

}