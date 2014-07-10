package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.RingSegment;

public class RingSegmentTest extends GeometryTestCaseBase
{

	public RingSegmentTest()
	{
		super("RingSegment Test");
	}

	@Override
	public void work()
	{
		RingSegment geometrie = new RingSegment(70, 70, 50, 70, 200, 0, true);
		vertices = geometrie.getVertices();
		triangles = geometrie.getTriangles();
		super.work();
	}

}
