package de.droidcachebox.Map;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import android.graphics.Bitmap;

public class Tile implements Destroyable {
    public enum TileState
    {
      Scheduled,
      Present,
      LowResolution,
      Disposed
    };

    public Descriptor Descriptor = null;

    public TileState State;

    /// <summary>
    /// Textur der Kachel
    /// </summary>
    public Bitmap Image = null;

    /// <summary>
    /// Frames seit dem letzten Zugriff auf die Textur
    /// </summary>
    public int Age = 0;

    public Tile(Descriptor desc, Bitmap image, TileState state)
    {
      Descriptor = desc;
      Image = image;
      State = state;
    }

    public String ToString()
    {
      return State.toString() + ", " + Descriptor.ToString();
    }

	@Override
	public void destroy() throws DestroyFailedException {
		if (Image != null)
			Image.recycle();
		
	}

	@Override
	public boolean isDestroyed() {
		return false;
	}

 }
