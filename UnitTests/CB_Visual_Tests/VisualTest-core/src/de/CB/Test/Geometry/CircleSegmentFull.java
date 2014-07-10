package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.CircularSegment;

import com.badlogic.gdx.graphics.g2d.Batch;

public class CircleSegmentFull extends GeometryTestCaseBase
{

	public CircleSegmentFull()
	{
		super("CircleSegment Test");
	}

	@Override
	public void work()
	{
		CircularSegment geometrie = new CircularSegment(10, 10, 5, 90, 0, true);
		vertices = geometrie.getVertices();
		triangles = geometrie.getTriangles();
		super.work();
	}

	@Override
	public void draw(Batch batch)
	{
		if (polygon != null)
		{
			polygon.draw(batch, firstPoint.x, firstPoint.y, 256, 256, 0);
			polygon.draw(batch, secondPoint.x, secondPoint.y, 4096, 4096, 0);
			isReady = true;
		}
	}

}
