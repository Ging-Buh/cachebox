package CB_Core.GL_UI;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DrawUtils
{
	public static void drawSpriteLine(SpriteBatch batch, Sprite sprite, float x1, float y1, float x2, float y2)
	{

		if (Float.isNaN(x1) || Float.isNaN(y1) || Float.isNaN(x2) || Float.isNaN(y2)) return;

		float l = (float) Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
		float SpriteHeight = sprite.getHeight();
		int SpriteCount = (int) (l / SpriteHeight);

		float direction = calculateAngle(x1, y1, x2, y2);

		sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
		sprite.setRotation(direction);

		// Sprites auf der Linie Zeichnen
		for (int i = 0; i < SpriteCount; i += SpriteHeight)
		{
			sprite.setPosition(x1, y1);// hier muss ich noch die Formel erstellen lassen!
			sprite.draw(batch);
		}

	}

	/**
	 * Winkelberechnung
	 * 
	 * @param x1
	 *            x-Koordinate des Mittelpunktes
	 * @param y1
	 *            y-Koordinate des Mittelpunktes
	 * @param x2
	 *            x-Koordinate des Zielpunktes
	 * @param y2
	 *            y-Koordinate des Zielpunktes
	 * @return Winkel
	 */
	private static float calculateAngle(float x1, float y1, float x2, float y2)
	{
		if (x1 == x2 && y1 == y2) return 0;

		/* Berechnung der Seitenlängen des Dreiecks: */
		double dx = x2 - x1;
		double dy = y1 - y2;
		double dz = Math.sqrt(dx * dx + dy * dy);

		/*
		 * Berechnung des Winkels nach Pythagoras: sin(gamma) = dy/dz <=> gamma = arcsin(dy/dz)
		 */
		double gamma = Math.asin(dy / dz);

		/* Umrechnung von RAD auf DEG: */
		gamma = 180 * gamma / Math.PI;

		float angle = Math.round(gamma);

		/* erster Quadrant: */
		if (x2 >= x1 && y2 <= y1)
		; /* passt schon so */
		/* zweiter Quadrant: */
		else if (x2 <= x1 && y2 <= y1) angle = 180 - angle;
		/* dritter Quadrant: */
		else if (x2 <= x1 && y2 >= y1) angle = 180 - angle;
		else if (x2 >= x1 && y2 >= y1) angle = 360 + angle;

		return angle;
	}

}
