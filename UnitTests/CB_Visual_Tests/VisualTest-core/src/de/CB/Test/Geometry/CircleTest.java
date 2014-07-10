package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.Circle;

public class CircleTest extends GeometryTestCaseBase
{

	public CircleTest()
	{
		super("Circle Test");
	}

	@Override
	public void work()
	{
		Circle geometrie = new Circle(70, 70, 60, true);
		vertices = geometrie.getVertices();
		triangles = geometrie.getTriangles();
		super.work();
	}
}
