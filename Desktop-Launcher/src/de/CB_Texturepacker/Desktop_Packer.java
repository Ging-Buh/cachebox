package de.CB_Texturepacker;

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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import CB_UI_Base.CB_Texturepacker.MaxRectsPacker;
import CB_UI_Base.CB_Texturepacker.Page;
import CB_UI_Base.CB_Texturepacker.Rect_Base;
import CB_UI_Base.CB_Texturepacker.Settings;
import CB_UI_Base.CB_Texturepacker.TexturePacker_Base;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class Desktop_Packer extends TexturePacker_Base {

	@Override
	public TexturePacker_Base getInstanz(File rootDir, Settings settings) {
		return new Desktop_Packer(rootDir, settings);
	}

	public Desktop_Packer(File rootDir, Settings settings) {
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

	public Desktop_Packer() {
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
				outputFile = new File(outputDir, imageName + (fileIndex++ == 0 ? "" : fileIndex) + "." + settings.outputFormat);
				if (!outputFile.exists())
					break;
			}
			page.imageName = outputFile.getName();

			BufferedImage canvas = new BufferedImage(width, height, getBufferedImageType(settings.format));
			Graphics2D g = (Graphics2D) canvas.getGraphics();

			System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);

			for (Rect_Base rect : page.outputRects) {
				int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;
				if (rect.rotated) {
					g.translate(rectX, rectY);
					g.rotate(-90 * MathUtils.degreesToRadians);
					g.translate(-rectX, -rectY);
					g.translate(-(rect.height - settings.paddingY), 0);
				}
				BufferedImage image = (BufferedImage) rect.image;
				if (settings.duplicatePadding) {
					int amountX = settings.paddingX / 2;
					int amountY = settings.paddingY / 2;
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();
					// Copy corner pixels to fill corners of the padding.
					g.drawImage(image, rectX - amountX, rectY - amountY, rectX, rectY, 0, 0, 1, 1, null);
					g.drawImage(image, rectX + imageWidth, rectY - amountY, rectX + imageWidth + amountX, rectY, imageWidth - 1, 0, imageWidth, 1, null);
					g.drawImage(image, rectX - amountX, rectY + imageHeight, rectX, rectY + imageHeight + amountY, 0, imageHeight - 1, 1, imageHeight, null);
					g.drawImage(image, rectX + imageWidth, rectY + imageHeight, rectX + imageWidth + amountX, rectY + imageHeight + amountY, imageWidth - 1, imageHeight - 1, imageWidth, imageHeight, null);
					// Copy edge pixels into padding.
					g.drawImage(image, rectX, rectY - amountY, rectX + imageWidth, rectY, 0, 0, imageWidth, 1, null);
					g.drawImage(image, rectX, rectY + imageHeight, rectX + imageWidth, rectY + imageHeight + amountY, 0, imageHeight - 1, imageWidth, imageHeight, null);
					g.drawImage(image, rectX - amountX, rectY, rectX, rectY + imageHeight, 0, 0, 1, imageHeight, null);
					g.drawImage(image, rectX + imageWidth, rectY, rectX + imageWidth + amountX, rectY + imageHeight, imageWidth - 1, 0, imageWidth, imageHeight, null);
				}
				g.drawImage(image, rectX, rectY, null);
				if (rect.rotated) {
					g.translate(rect.height - settings.paddingY, 0);
					g.translate(rectX, rectY);
					g.rotate(90 * MathUtils.degreesToRadians);
					g.translate(-rectX, -rectY);
				}
				if (settings.debug) {
					g.setColor(Color.magenta);
					g.drawRect(rectX, rectY, rect.width - settings.paddingX - 1, rect.height - settings.paddingY - 1);
				}
			}

			if (settings.debug) {
				g.setColor(Color.magenta);
				g.drawRect(0, 0, width - 1, height - 1);
			}

			try {
				if (settings.outputFormat.equalsIgnoreCase("jpg")) {
					Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
					ImageWriter writer = writers.next();
					ImageWriteParam param = writer.getDefaultWriteParam();
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(settings.jpegQuality);
					ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
					writer.setOutput(ios);
					writer.write(null, new IIOImage(canvas, null, null), param);
				} else
					ImageIO.write(canvas, "png", outputFile);
			} catch (IOException ex) {
				throw new RuntimeException("Error writing file: " + outputFile, ex);
			}
		}
	}

	private int getBufferedImageType(Format format) {
		switch (settings.format) {
		case RGBA8888:
		case RGBA4444:
			return BufferedImage.TYPE_INT_ARGB;
		case RGB565:
		case RGB888:
			return BufferedImage.TYPE_INT_RGB;
		case Alpha:
			return BufferedImage.TYPE_BYTE_GRAY;
		default:
			throw new RuntimeException("Unsupported format: " + settings.format);
		}
	}

}
