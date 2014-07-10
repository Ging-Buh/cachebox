package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.Ring;

public class RingTest extends GeometryTestCaseBase
{

	public RingTest()
	{
		super("Ring Test");
	}

	@Override
	public void work()
	{
		Ring geometrie = new Ring(70, 70, 40, 60, true);
		vertices = geometrie.getVertices();
		triangles = geometrie.getTriangles();
		super.work();
	}
}
