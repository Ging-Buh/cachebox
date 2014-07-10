package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.Quadrangle;

public class QuadrangleTest extends GeometryTestCaseBase
{
	public QuadrangleTest()
	{
		super("Quadrangle Test");
	}

	@Override
	public void work()
	{
		Quadrangle sq = new Quadrangle(10, 10, 10, 100, 30, 100, 30, 10);
		vertices = sq.getVertices();
		triangles = sq.getTriangles();
		super.work();
	}

}
