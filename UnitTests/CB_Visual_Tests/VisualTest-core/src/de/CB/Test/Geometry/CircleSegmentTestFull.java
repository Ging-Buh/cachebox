package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.CircularSegment;

public class CircleSegmentTestFull extends GeometryTestCaseBase
{

	public CircleSegmentTestFull()
	{
		super("CircleSegment Test");
	}

	@Override
	public void work()
	{
		CircularSegment geometrie = new CircularSegment(70, 70, 60, 0, 360, true);
		vertices = geometrie.getVertices();
		triangles = geometrie.getTriangles();
		super.work();
	}

}
