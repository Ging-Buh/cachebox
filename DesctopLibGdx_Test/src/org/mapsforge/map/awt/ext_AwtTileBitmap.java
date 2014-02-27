package org.mapsforge.map.awt;

import java.awt.image.BufferedImage;

import CB_Utils.Lists.CB_List;

public class ext_AwtTileBitmap extends ext_AwtBitmap
{

	static CB_List<BufferedImage> ReusebleList = new CB_List<BufferedImage>();

	private static BufferedImage getReusable(int tileSize)
	{
		synchronized (ReusebleList)
		{
			for (BufferedImage reuse : ReusebleList)
			{
				if (reuse != null && reuse.getWidth() == tileSize && reuse.getHeight() == tileSize)
				{
					ReusebleList.remove(reuse);
					return reuse;
				}
			}
		}
		return null;
	}

	public ext_AwtTileBitmap(int tileSize)
	{
		BufferedImage reuse = getReusable(tileSize);
		if (reuse != null)
		{
			this.bufferedImage = reuse;
		}
		else
		{
			this.bufferedImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
		}
	}

	@Override
	public void recycle()
	{

		// chk if reuseList full or cann put BufferdImage to ReuseList?
		synchronized (ReusebleList)
		{
			if (ReusebleList.size() < 15)
			{
				ReusebleList.add(bufferedImage);
			}
		}
		instCount++;
		this.bufferedImage = null;
	}

}
