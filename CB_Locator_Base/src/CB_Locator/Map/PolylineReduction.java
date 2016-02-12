package CB_Locator.Map;

import java.util.ArrayList;
import java.util.Collections;

import CB_Utils.Math.PointD;

/**
 * Reduktion of Ploylines with Douglas-Peucker-Algorithmus </br> </br> http://de.wikipedia.org/wiki/Douglas-Peucker-Algorithmus </br> </br>
 * code divide from http://www.codeproject.com/Articles/18936/A-C-Implementation-of-Douglas-Peucker-Line-Approxi
 * 
 * @author Longri
 */
public class PolylineReduction {

	// / <summary>
	// / Uses the Douglas Peucker algorithm to reduce the number of points.
	// / </summary>
	// / <param name="Points">The points.</param>
	// / <param name="Tolerance">The tolerance.</param>
	// / <returns></returns>
	public static ArrayList<TrackPoint> DouglasPeuckerReduction(ArrayList<TrackPoint> points, double Tolerance) {
		if (points == null || points.size() < 50)
			return points; // created Circle have 36 points and don`t need reduction

		int firstPoint = 0;
		int lastPoint = points.size() - 1;
		ArrayList<Integer> pointIndexsToKeep = new ArrayList<Integer>();

		// Add the first and last index to the keepers
		pointIndexsToKeep.add(firstPoint);
		pointIndexsToKeep.add(lastPoint);

		// The first and the last point cannot be the same
		while (points.get(firstPoint) == points.get(lastPoint)) {
			lastPoint--;
		}

		DouglasPeuckerReduction(points, firstPoint, lastPoint, Tolerance, pointIndexsToKeep);

		ArrayList<TrackPoint> returnPoints = new ArrayList<TrackPoint>();

		Collections.sort(pointIndexsToKeep);

		for (int index : pointIndexsToKeep) {
			returnPoints.add(points.get(index));
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
	private static void DouglasPeuckerReduction(ArrayList<TrackPoint> points, int firstPoint, int lastPoint, double tolerance, ArrayList<Integer> pointIndexsToKeep) {
		double maxDistance = 0.0;
		int indexFarthest = 0;

		for (int index = firstPoint; index < lastPoint; index++) {
			double distance = PerpendicularDistance(points.get(firstPoint), points.get(lastPoint), points.get(index));
			if (distance > maxDistance) {
				maxDistance = distance;
				indexFarthest = index;
			}
		}

		if (maxDistance > tolerance && indexFarthest != 0) {
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
	public static double PerpendicularDistance(PointD Point1, PointD Point2, PointD Point) {

		try {
			// double area = Math.abs(0.5 * (Point1.X * Point2.Y + Point2.X * Point.Y + Point.X * Point1.Y - Point2.X * Point1.Y - Point.X
			// * Point2.Y - Point1.X * Point.Y));

			double pr1 = Point1.X * Point2.Y;
			double pr2 = Point2.X * Point.Y;
			double pr3 = Point.X * Point1.Y;
			double pr4 = Point2.X * Point1.Y;
			double pr5 = Point.X * Point2.Y;
			double pr6 = Point1.X * Point.Y;

			double area = Math.abs((pr1 + pr2 + pr3 - pr4 - pr5 - pr6) / 2);

			double bottom = Math.sqrt(Math.pow(Point1.X - Point2.X, 2) + Math.pow(Point1.Y - Point2.Y, 2));

			if (bottom == 0.0)
				return 0.0;

			double height = area / bottom * 2;

			return height;
		} catch (Exception e) {
			return 0.0;
		}

	}

}
