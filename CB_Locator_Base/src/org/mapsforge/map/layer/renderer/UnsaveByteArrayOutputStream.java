package org.mapsforge.map.layer.renderer;

import java.io.ByteArrayOutputStream;

public class UnsaveByteArrayOutputStream extends ByteArrayOutputStream
{
	public UnsaveByteArrayOutputStream(int size)
	{
		super(size);
	}

	@Override
	public synchronized byte[] toByteArray()
	{
		return buf;
	}
}
