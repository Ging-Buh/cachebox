package de.cachebox_test.CB_Texturepacker;

import CB_Core.CB_Texturepacker.Rect_Base;
import android.graphics.Bitmap;

/** @author Nathan Sweet */
class Rect extends Rect_Base
{

	Rect(Bitmap source, int left, int top, int newWidth, int newHeight)
	{
		// image = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());

		image = source.copy(source.getConfig(), true);

		offsetX = left;
		offsetY = top;
		originalWidth = source.getWidth();
		originalHeight = source.getHeight();
		width = newWidth;
		height = newHeight;
	}

	public Rect()
	{
		that = this;
	}

	public Rect(Rect_Base freeNode)
	{
		super(freeNode);
	}

	@Override
	public int getWidth()
	{
		return ((Bitmap) image).getWidth();
	}

	@Override
	public int getHeight()
	{
		return ((Bitmap) image).getHeight();
	}

	@Override
	public Rect_Base getInstanz()
	{
		return new Rect();
	}

	@Override
	public Rect_Base getInstanz(Rect_Base freeNode)
	{
		return new Rect(freeNode);
	}

}
