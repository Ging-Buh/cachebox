package de.CB.TestBase.Actions;

import java.util.ArrayList;


import de.CB.Test.Blog.CircleBlog;
import de.CB.Test.Geometry.CircleSegmentFull;
import de.CB.Test.Geometry.CircleSegmentTest;
import de.CB.Test.Geometry.CircleTest;
import de.CB.Test.Geometry.GeometryListTest;
import de.CB.Test.Geometry.QuadrangleTest;
import de.CB.Test.Geometry.RingSegmentTest;
import de.CB.Test.Geometry.RingTest;
import de.CB.Test.Geometry.TriangleTest;
import de.CB.Test.Map.MapViewTest;
import de.CB.Test.Map.Map_freizeitkarte;
import de.CB.Test.Map.PixmapPackertest;
import de.CB.Test.PolyLine.PolyJoin1;
import de.CB.Test.PolyLine.PolyJoin2;
import de.CB.Test.PolyLine.PolyJoin3;
import de.CB.Test.PolyLine.PolyJoin4;
import de.CB.Test.PolyLine.PolylineOffset;
import de.CB.Test.PolyLine.green20Butt;
import de.CB.Test.PolyLine.Red20Default;
import de.CB.Test.PolyLine.Red20Round;
import de.CB.Test.PolyLine.Red20Square;
import de.CB.Test.PolyLine.Red2Dash1_15;
import de.CB.Test.PolyLine.Red2Dash84;
import de.CB.Test.Polygon.BmpFilled;
import de.CB.Test.Polygon.RedFilled;
import de.CB.Test.SVG.SVG_Postbox;
import de.CB.Test.String.TextOnPathTest;

public class MultiTestList extends ArrayList<TestCaseBase>
{

	private static final long serialVersionUID = 1L;

	private static final int COUNT_PER_TEST = 2;

	public static final MultiTestList INSTANCE = new MultiTestList();

	public static final boolean FastTest = false;

	private MultiTestList()
	{
		Multi_SVG_Test SVGs = new Multi_SVG_Test();

//		addAll(SVGs);
//		
		add(new PixmapPackertest());
//			
//		add(new SVG_Postbox());
//		add(new TextOnPathTest());
		
		// add 100 MapTileTests
//		 for (int i = 0; i < COUNT_PER_TEST; i++)
//		 {
//		 add(new Map_freizeitkarte(false));
//		 }
//		 add(new MapViewTest());
		
		
		//
		add(new Red20Default());
		add(new green20Butt());
		add(new Red20Round());
		add(new Red20Square());
		//
		add(new PolyJoin1());
		add(new PolyJoin2());
		add(new PolyJoin3());
		add(new PolyJoin4());
		//
		add(new Red2Dash84());
		add(new Red2Dash1_15());

		 add(new CircleBlog());
		//
		// // // add 100 MapTileTests
		// // for (int i = 0; i < COUNT_PER_TEST; i++)
		// // {
		// // add(new Map_freizeitkarte(false));
		// // }
		//
		 
		add(new CircleSegmentFull());
		 add(new CircleSegmentTest());
		//
		 add(new RedFilled());
		add(new BmpFilled());
		add(new RingSegmentTest());
		add(new CircleSegmentTest());
		add(new QuadrangleTest());
		add(new TriangleTest());
		add(new CircleTest());
		add(new RingTest());
		add(new GeometryListTest());
		add(new PolylineOffset());
		
		
	}
	
	private static int IndexCount=0;
	
	public boolean add(TestCaseBase test){
		super.add(test);
		test.setTestIndex(IndexCount++);
		return true;
	}
	
	public boolean addAll(ArrayList<TestCaseBase> list)
	{
		super.addAll(list);
		
		for(TestCaseBase t:list)
		{
			t.setTestIndex(IndexCount);
		}
		IndexCount++;
		return true;
	}
}
