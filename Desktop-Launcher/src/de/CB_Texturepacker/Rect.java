package de.CB_Texturepacker;

import java.awt.image.BufferedImage;

import CB_UI_Base.CB_Texturepacker.Rect_Base;

/** @author Nathan Sweet */
class Rect extends Rect_Base
{

	Rect(BufferedImage source, int left, int top, int newWidth, int newHeight)
	{
		image = new BufferedImage(source.getColorModel(), source.getRaster()
				.createWritableChild(left, top, newWidth, newHeight, 0, 0, null), source.getColorModel().isAlphaPremultiplied(), null);
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
		return ((BufferedImage) image).getWidth();
	}

	@Override
	public int getHeight()
	{
		return ((BufferedImage) image).getHeight();
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
