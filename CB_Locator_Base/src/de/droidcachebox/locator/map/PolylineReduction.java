package de.droidcachebox.locator.map;

import de.droidcachebox.utils.PointD;

import java.util.ArrayList;
import java.util.Collections;

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
    public static ArrayList<TrackPoint> polylineReduction(ArrayList<TrackPoint> points, double tolerance) {
        if (points == null || points.size() < 50)
            return points;

        int lastPoint = points.size() - 1;
        if (points.get(0).equals(points.get(lastPoint))) {
            // avoid closed polygon: The first and the last point cannot be the same
            // avoid closed polygon: simplified by splitting into 2 ArrayLists of same size.
            // (normally you should take the two points with the maximal distance)
            ArrayList<TrackPoint> r1 = reduce(new ArrayList<>(points.subList(0, lastPoint / 2)), tolerance);
            ArrayList<TrackPoint> r2 = reduce(new ArrayList<>(points.subList(lastPoint / 2, lastPoint)), tolerance);
            r1.addAll(r2);
            return r1;
        } else {
            return reduce(points, tolerance);
        }
    }

    private static ArrayList<TrackPoint> reduce(ArrayList<TrackPoint> trackPoints, double tolerance) {
        int lastPoint = trackPoints.size() - 1;
        if (lastPoint > 0) {
            while (trackPoints.get(0).equals(trackPoints.get(lastPoint))) {
                lastPoint--;
                if (lastPoint == 0) return trackPoints; // simplified use the original, all points are equal
            }
        }
        ArrayList<Integer> reducedIndexes = new ArrayList<>();
        reducedIndexes.add(0);
        reducedIndexes.add(trackPoints.size() - 1);
        reduceWithDouglasPeuckerAlgorithm(trackPoints, 0, trackPoints.size() - 1, tolerance, reducedIndexes);
        Collections.sort(reducedIndexes);
        ArrayList<TrackPoint> result = new ArrayList<>();
        for (int index : reducedIndexes) {
            result.add(trackPoints.get(index));
        }
        return result;
    }

    // / <summary>
    // / Douglases the peucker reduction.
    // / </summary>
    // / <param name="points">The points.</param>
    // / <param name="firstPoint">The first point.</param>
    // / <param name="lastPoint">The last point.</param>
    // / <param name="tolerance">The tolerance.</param>
    // / <param name="pointIndexsToKeep">The point index to keep.</param>
    private static void reduceWithDouglasPeuckerAlgorithm(ArrayList<TrackPoint> points, int firstPoint, int lastPoint, double tolerance, ArrayList<Integer> pointIndexsToKeep) {
        double maxDistance = 0.0;
        int indexFarthest = 0;

        for (int index = firstPoint; index < lastPoint; index++) {
            double distance = calculatePerpendicularDistance(points.get(firstPoint), points.get(lastPoint), points.get(index));
            if (distance > maxDistance) {
                maxDistance = distance;
                indexFarthest = index;
            }
        }

        if (maxDistance > tolerance && indexFarthest != 0) {
            // Add the largest point that exceeds the tolerance
            pointIndexsToKeep.add(indexFarthest);

            reduceWithDouglasPeuckerAlgorithm(points, firstPoint, indexFarthest, tolerance, pointIndexsToKeep);
            reduceWithDouglasPeuckerAlgorithm(points, indexFarthest, lastPoint, tolerance, pointIndexsToKeep);
        }
    }

    // / <summary>
    // / The distance of a point from a line made from point1 and point2.
    // / </summary>
    // / <param name="pt1">The PT1.</param>
    // / <param name="pt2">The PT2.</param>
    // / <param name="p">The p.</param>
    // / <returns></returns>
    private static double calculatePerpendicularDistance(PointD Point1, PointD Point2, PointD Point) {

        try {
            // double area = Math.abs(0.5 * (Point1.X * Point2.Y + Point2.X * Point.Y + Point.X * Point1.Y - Point2.X * Point1.Y - Point.X
            // * Point2.Y - Point1.X * Point.Y));

            double pr1 = Point1.x * Point2.y;
            double pr2 = Point2.x * Point.y;
            double pr3 = Point.x * Point1.y;
            double pr4 = Point2.x * Point1.y;
            double pr5 = Point.x * Point2.y;
            double pr6 = Point1.x * Point.y;

            double area = Math.abs((pr1 + pr2 + pr3 - pr4 - pr5 - pr6) / 2);

            double bottom = Math.sqrt(Math.pow(Point1.x - Point2.x, 2) + Math.pow(Point1.y - Point2.y, 2));

            if (bottom == 0.0)
                return 0.0;

            return area / bottom * 2;
        } catch (Exception e) {
            return 0.0;
        }

    }

    private void getReduced() {

    }

}
