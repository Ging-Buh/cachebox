package CB_Core.Math;

import java.util.ArrayList;
import java.util.Collections;

import CB_Core.Map.Descriptor.PointD;

/**
 * Reduktion of Ploylines with Douglas-Peucker-Algorithmus </br> </br> http://de.wikipedia.org/wiki/Douglas-Peucker-Algorithmus </br> </br>
 * code divide from http://www.codeproject.com/Articles/18936/A-C-Implementation-of-Douglas-Peucker-Line-Approxi
 * 
 * @author Longri
 */
public class PolylineReduction
{

	// / <summary>
	// / Uses the Douglas Peucker algorithm to reduce the number of points.
	// / </summary>
	// / <param name="Points">The points.</param>
	// / <param name="Tolerance">The tolerance.</param>
	// / <returns></returns>
	public static ArrayList<PointD> DouglasPeuckerReduction(ArrayList<PointD> Points, Double Tolerance)
	{
		if (Points == null || Points.size() < 3) return Points;

		int firstPoint = 0;
		int lastPoint = Points.size() - 1;
		ArrayList<Integer> pointIndexsToKeep = new ArrayList<Integer>();

		// Add the first and last index to the keepers
		pointIndexsToKeep.add(firstPoint);
		pointIndexsToKeep.add(lastPoint);

		// The first and the last point cannot be the same
		while (Points.get(firstPoint) == Points.get(lastPoint))
		{
			lastPoint--;
		}

		DouglasPeuckerReduction(Points, firstPoint, lastPoint, Tolerance, pointIndexsToKeep);

		ArrayList<PointD> returnPoints = new ArrayList<PointD>();

		Collections.sort(pointIndexsToKeep);

		for (int index : pointIndexsToKeep)
		{
			returnPoints.add(Points.get(index));
		}

		return returnPoints;
	}

	// / <summary>
	// / Douglases the peucker reduction.
	// / </summary>
	// / <param name="points">The points.</param>
	// / <param name="firstPoint">The first point.</param>
	// / <param name="lastPoint">The last point.</param>
	// / <param name="tolerance">The tolerance.</param>
	// / <param name="pointIndexsToKeep">The point index to keep.</param>
	private static void DouglasPeuckerReduction(ArrayList<PointD> points, int firstPoint, int lastPoint, Double tolerance,
			ArrayList<Integer> pointIndexsToKeep)
	{
		Double maxDistance = 0.0;
		int indexFarthest = 0;

		for (int index = firstPoint; index < lastPoint; index++)
		{
			Double distance = PerpendicularDistance(points.get(firstPoint), points.get(lastPoint), points.get(index));
			if (distance > maxDistance)
			{
				maxDistance = distance;
				indexFarthest = index;
			}
		}

		if (maxDistance > tolerance && indexFarthest != 0)
		{
			// Add the largest point that exceeds the tolerance
			pointIndexsToKeep.add(indexFarthest);

			DouglasPeuckerReduction(points, firstPoint, indexFarthest, tolerance, pointIndexsToKeep);
			DouglasPeuckerReduction(points, indexFarthest, lastPoint, tolerance, pointIndexsToKeep);
		}
	}

	// / <summary>
	// / The distance of a point from a line made from point1 and point2.
	// / </summary>
	// / <param name="pt1">The PT1.</param>
	// / <param name="pt2">The PT2.</param>
	// / <param name="p">The p.</param>
	// / <returns></returns>
	public static Double PerpendicularDistance(PointD Point1, PointD Point2, PointD Point)
	{

		try
		{
			Double area = Math.abs(0.5 * (Point1.X * Point2.Y + Point2.X * Point.Y + Point.X * Point1.Y - Point2.X * Point1.Y - Point.X
					* Point2.Y - Point1.X * Point.Y));
			Double bottom = Math.sqrt(Math.pow(Point1.X - Point2.X, 2) + Math.pow(Point1.Y - Point2.Y, 2));

			if (bottom == 0.0) return 0.0;

			Double height = area / bottom * 2;

			return height;
		}
		catch (Exception e)
		{
			return 0.0;
		}

	}

}
