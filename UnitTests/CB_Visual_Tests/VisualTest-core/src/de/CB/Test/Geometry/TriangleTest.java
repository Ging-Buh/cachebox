package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.Triangle;

public class TriangleTest extends GeometryTestCaseBase
{

	public TriangleTest()
	{
		super("Triangle Test");
	}

	@Override
	public void work()
	{
		Triangle geometrie = new Triangle(10, 110, 60, 150, 110, 110);
		vertices = geometrie.getVertices();
		triangles = geometrie.getTriangles();
		super.work();
	}

}