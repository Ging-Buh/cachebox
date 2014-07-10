package de.CB.Test.Geometry;

import CB_UI_Base.graphics.Geometry.Circle;
import CB_UI_Base.graphics.Geometry.GeometryList;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.Geometry.Ring;
import CB_UI_Base.graphics.Geometry.Triangle;

public class GeometryListTest extends GeometryTestCaseBase
{
	public GeometryListTest()
	{
		super("Multi Geometry Test");
	}

	@Override
	public void work()
	{
		GeometryList list = new GeometryList();

		list.add(new Circle(20, 20, 60, true));
		list.add(new Ring(70, 70, 40, 60, true));
		list.add(new Triangle(10, 110, 60, 150, 110, 110));
		list.add(new Quadrangle(10, 200, 10, 300, 30, 300, 30, 200));
		list.add(new Triangle(10, 110, 60, 150, 110, 110));
		list.add(new Triangle(110, 110, 160, 150, 210, 110));

		vertices = list.getVertices();
		triangles = list.getTriangles();
		super.work();
	}
}
