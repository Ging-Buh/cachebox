package de.cachebox;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main
{
	public static void main(String[] args)
	{
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "refresh_libraries";
		cfg.useGL20 = false;
		cfg.width = 950;
		cfg.height = 600;

		new LwjglApplication(new Core(), cfg);
	}
}
