package group2.sdp.common.util;

import java.awt.geom.Point2D;

public class Geometry {

	/**
	 * Set to true to output debug data.
	 */
	private static boolean verbose = true;
	
	/**
	 * Gets the number of intersections between a semi-infinite line segment 
	 * (a ray) and a circle.
	 * @param rayStart The start of the ray.
	 * @param rayVector The direction of the ray.
	 * @param circleCentre The centre of the circle.
	 * @param circleRadius The radius of the circle.
	 * @return The number of intersections between the given ray and the given circle.
	 */
	public static int getNumberOfRayCircleIntersections(Point2D rayStart,
			Point2D rayVector, Point2D circleCentre, double circleRadius) {
		double a = circleCentre.getX();
		double b = circleCentre.getY();
		double c = rayStart.getX();
		double d = rayStart.getY(); 
		double e = rayVector.getX();
		double f = rayVector.getY();
		double r = circleRadius;

		double A = e * e + f * f;
		double B = 2 * (e * (c - a) + f * (d - b));
		double C = (c - a) * (c - a) + (d - b) * (d - b) - r * r;
		
		int n = Tools.getNumberOfQuadraticSolutions(A, B, C);
		return n;
	}
	
	/**
	 * Gets the number of intersections between a line segment and a circular 
	 * arc.
	 * @param segmentStart The starting point of the line segment.
	 * @param segmentEnd The ending point of the line segment.
	 * @param circleCentre The centre of the circle containing the arc.
	 * @param circleRadius The radius of the circle containing the arc.
	 * @param arcStartAngle The starting angle of the arc.
	 * @param arcEndAngle The end angle of the arc; when moving on the arc from
	 * the start angle to the end angle, you should be moving anti-clock wise 
	 * (otherwise there is ambiguity of order 2).
	 * @return The number of intersections between a line segment and a 
	 * circular arc.
	 */
	public static int getNumberOfLineSegmentArcIntersections(
			Point2D segmentStart, Point2D segmentEnd, Point2D circleCentre, 
			double circleRadius, double arcStartAngle, double arcEndAngle) {
		
		double a = segmentStart.getX() - circleCentre.getX();
		double b = segmentEnd.getX() - segmentStart.getX();
		double c = segmentStart.getY() - circleCentre.getY();
		double d = segmentEnd.getY() - segmentStart.getY();
		double r = circleRadius;
		
		double A = b * b + d * d;
		double B = 2 * (a * b + c * d);
		double C = a * a + c * c - r * r;
		
		// a point on the given segment is
		// segmentStart + t * (segmentEnd - segmentStart)
		Pair<Double, Double> t = Tools.getQuadraticSolutions(A, B, C);
		if (t == null) {
			return 0;
		}
		
		// theta is the angle on the arc at which the segment crosses it
		Pair<Double, Double> theta = new Pair<Double, Double> (
				Math.toDegrees(Math.atan2(c + t.first * d, a + t.first * b)),
				Math.toDegrees(Math.atan2(c + t.second * d, a + t.second * b))
		);
		
		int n = 0;
		// because of the particular parameterisation of the line segment
		// t should be between 0 and 1; theta should be bounded by the 
		// arc angles
		if (0.0 <= t.first && t.first <= 1.0 && 
				angleWithinBounds(theta.first, arcStartAngle, arcEndAngle)) {
			++n;
		}
		
		// Comparison for doubles is special, so do not use t.first == t.second
		if (t.first.compareTo(t.second) != 0 && 
				0.0 <= t.second && t.second <= 1.0 && 
				angleWithinBounds(theta.second, arcStartAngle, arcEndAngle)) {
			++n;
		}
		return n;
	}

	/**
	 * Checks if an angle is between two given angles. The region should be
	 * anti-clock-wise oriented, that is when you move from arcStartAngle to
	 * arcEndAngle, you should be moving in anti-clock-wise direction. Copes
	 * with 360 - 0 jump.
	 * @param theta The angle to check.
	 * @param arcStartAngle Lower bound with respect to anti-clock-wise 
	 * orientation.
	 * @param arcEndAngle Upper bound with respect to anti-clock-wise 
	 * orientation.
	 * @return True if an angle is between two given angles, false otherwise.
	 */
	public static boolean angleWithinBounds(double theta,
			double arcStartAngle, double arcEndAngle) {
		
		return 
			getAntiClockWiseAngleDistance(arcStartAngle, theta) <=
			getAntiClockWiseAngleDistance(arcStartAngle, arcEndAngle);
	}
	
	/**
	 * Returns the amount you need to turn anti-clock-wise direction in degrees  
	 * to get from angle alpha to angle beta. The result is from
	 * [0 to 360) and the units are degrees.  
	 * @param alpha The starting angle.
	 * @param beta The end angle.
	 * @return The amount you need to turn anti-clock-wise in degrees  
	 * to get from angle alpha to angle beta.
	 */
	public static double getAntiClockWiseAngleDistance(double alpha, 
			double beta) {
		double delta = beta - alpha;
		if (delta < 0) 
			delta += 360;

		return delta;
	}

	/**
	 * Gets the angle of the vector starting from the first point and ending in the
	 * second point.
	 * @param first The start of the vector.
	 * @param second The end of the vector.
	 * @return The angle of the vector.
	 */
	public static double getVectorDirection(Point2D first,
			Point2D second) {
		double dx = second.getX() - first.getX();
		double dy = second.getY() - first.getY();
		return Math.toDegrees(Math.atan2(dy, dx));
	}

	/**
	 * Given the start position, start direction, radius and angle of the
	 * arc, return the end position. The sign of the angle indicates whether
	 * it is a clock-wise
	 * (negative angle) or anti-clock-wise (positive angle) arc.
	 * @param arcStart The starting point of the arc.
	 * @param arcStartDirection The slope of the tangent line to the arc in the 
	 * starting point.
	 * @param radius The radius of the circle containing the arc.
	 * @param angle The central angle of the arc.
	 * @return The end point of the arc.
	 */
	public static Point2D getArcEnd(Point2D arcStart, double arcStartDirection,
			double radius, double angle) {
		// Cosine theorem
		double c = radius * Math.sqrt(2 * (1 - Math.cos(Math.toRadians(angle))));
		
		double beta;
		beta = arcStartDirection + angle / 2;
		
		double x = arcStart.getX() + c * Math.cos(Math.toRadians(beta));
		double y = arcStart.getY() + c * Math.sin(Math.toRadians(beta));
		
		return new Point2D.Double(x, y);
	}
}
