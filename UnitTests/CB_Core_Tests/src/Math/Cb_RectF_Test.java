package Math;

import junit.framework.TestCase;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.math.Vector2;

public class Cb_RectF_Test extends TestCase
{
	public void test_Intersection()
	{

		// zwei Rechtecke erstellen und das zureschtgeschnittene Rechteck überprüfen
		CB_RectF rec1 = new CB_RectF(50, 50, 200, 200);
		CB_RectF rec2 = new CB_RectF(10, 10, 100, 100);

		CB_RectF result = rec1.createIntersection(rec2);

		assertEquals(50f, result.getX());
		assertEquals(50f, result.getY());
		assertEquals(60f, result.getWidth());
		assertEquals(60f, result.getHeight());
	}

	public void test_Intersection2()
	{

		// zwei Rechtecke erstellen und das zureschtgeschnittene Rechteck überprüfen
		CB_RectF rec1 = new CB_RectF(50, 50, 200, 200);
		CB_RectF rec2 = new CB_RectF(100, 10, 150, 100);

		CB_RectF result = rec1.createIntersection(rec2);

		assertEquals(100f, result.getX());
		assertEquals(50f, result.getY());
		assertEquals(150f, result.getWidth());
		assertEquals(60f, result.getHeight());
	}

	public void test_Translation()
	{
		// testet die verschiebung von Positionen
		CB_RectF rec1 = new CB_RectF(50, 100, 400, 200);

		assertEquals(50f, rec1.getX());
		assertEquals(100f, rec1.getY());
		assertEquals(400f, rec1.getWidth());
		assertEquals(200f, rec1.getHeight());

		rec1.setPos(new Vector2(10, 30));

		assertEquals(10f, rec1.getX());
		assertEquals(30f, rec1.getY());
		assertEquals(400f, rec1.getWidth());
		assertEquals(200f, rec1.getHeight());

	}
}
