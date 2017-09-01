package CB_Utils.fileProvider;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import CB_Utils.Util.FileIO;

/**
 * Created by Longri on 17.02.2016.
 */
public class ServerFileFactory extends FileFactory {
	@Override
	protected File createPlatformFile(String path) {
		return new ServerFile(path);
	}

	@Override
	protected File createPlatformFile(File parent) {
		return new ServerFile(parent);
	}

	@Override
	protected File createPlatformFile(File parent, String child) {
		return new ServerFile(parent, child);
	}

	@Override
	protected File createPlatformFile(String parent, String child) {
		return new ServerFile(parent, child);
	}

	@Override
	protected String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix) {
		try {

			String storePath = FileIO.GetDirectoryName(Path) + "/";
			String storeName = FileIO.GetFileNameWithoutExtension(Path);
			String storeExt = FileIO.GetFileExtension(Path).toLowerCase();
			String ThumbPath = storePath + thumbPrefix + THUMB + storeName + "." + storeExt;

			java.io.File ThumbFile = new java.io.File(ThumbPath);

			if (ThumbFile.exists())
				return ThumbPath;

			BufferedImage ori = ImageIO.read(new java.io.File(Path));
			float scalefactor = (float) scaledWidth / (float) ori.getWidth();

			if (scalefactor >= 1)
				return Path; // don't need a thumb, return original path

			int newHeight = (int) (ori.getHeight() * scalefactor);
			int newWidth = (int) (ori.getWidth() * scalefactor);

			Image scaled = ori.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
			BufferedImage img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			img.createGraphics().drawImage(scaled, 0, 0, null);
			ImageIO.write(img, storeExt, ThumbFile);

			img.flush();
			ori.flush();
			scaled.flush();

			img = null;
			ori = null;
			scaled = null;

			return ThumbPath;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
}
