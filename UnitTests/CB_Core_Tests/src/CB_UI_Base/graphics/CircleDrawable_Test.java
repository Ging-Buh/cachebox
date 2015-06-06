package CB_UI_Base.graphics;

import junit.framework.TestCase;

import org.junit.Test;

public class CircleDrawable_Test extends TestCase {

    @Test
    public void test_CircleInside() {
	CircleDrawable cd = new CircleDrawable(100, 100, 50, new GL_Paint(), 100, 100);

	assertTrue("Point is inside the circle, function must return TRUE", cd.contains(100, 100));
	assertTrue("Point is inside the circle, function must return TRUE", cd.contains(50, 100));
	assertTrue("Point is inside the circle, function must return TRUE", cd.contains(100, 50));
	assertTrue("Point is inside the circle, function must return TRUE", cd.contains(130, 70));

	assertFalse("Point is outside the circle, function must return FALSE", cd.contains(151, 70));
	assertFalse("Point is outside the circle, function must return FALSE", cd.contains(50, 49));

    }

}
