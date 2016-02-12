package org.mapsforge.map.layer.renderer;

import java.io.ByteArrayOutputStream;

public class UnsaveByteArrayOutputStream extends ByteArrayOutputStream {
	private byte[] EMPTY;

	public UnsaveByteArrayOutputStream() {
		this(32);
	}

	public UnsaveByteArrayOutputStream(int size) {
		super(size);
		EMPTY = new byte[size];
	}

	// @Override
	// public synchronized byte[] toByteArray()
	// {
	// return buf;
	// }

	public void dispose() {
		EMPTY = null;
		buf = null;
	}

	public void clear() {
		System.arraycopy(EMPTY, 0, buf, 0, EMPTY.length);
		count = 0;
	}

}
