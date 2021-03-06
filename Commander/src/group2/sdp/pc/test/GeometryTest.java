package group2.sdp.pc.test;

import group2.sdp.common.util.Geometry;
import group2.sdp.common.util.Pair;

import java.awt.Point;
import java.awt.geom.Point2D;

import junit.framework.Assert;

import org.junit.Test;

public class GeometryTest {
	
	public void testSegmentArcIntersectionCase (
			Point2D segmentStart, Point2D segmentEnd, Point2D circleCentre, 
			double circleRadius, double arcStartAngle, double arcEndAngle,
			int expected) {
		int n = Geometry.getNumberOfLineSegmentArcIntersections(
				segmentStart, 
				segmentEnd, 
				circleCentre, 
				circleRadius, 
				arcStartAngle, 
				arcEndAngle
		);
		
		Assert.assertEquals(expected, n);
	}
	
	@Test
	public void testSegmentArcIntersection() {
		// docs/diagrams/ @ diagram 1
		testSegmentArcIntersectionCase(
				new Point2D.Double(1.0, 1.0), 
				new Point2D.Double(2.0, 2.0), 
				new Point2D.Double(1.0, 1.0), 
				1.0, 
				0.0,
				90.0, 
				1
		);
		
		// docs/diagrams/ @ diagram 2
		testSegmentArcIntersectionCase(
				new Point2D.Double(3.0, 0.0), 
				new Point2D.Double(0.0, 3.0), 
				new Point2D.Double(1.0, 1.0), 
				1.0, 
				0.0,
				90.0, 
				2
		);
		
		testSegmentArcIntersectionCase(
				new Point2D.Double(3.0, 0.0), 
				new Point2D.Double(-1.0, 3.0), 
				new Point2D.Double(1.0, 1.0), 
				1.0, 
				0.0,
				90.0, 
				0
		);
		
		// docs/diagrams/ @ diagram 3
		testSegmentArcIntersectionCase(
				new Point2D.Double(-1.0, 1.0), 
				new Point2D.Double(1.0, -1.0), 
				new Point2D.Double(0.0, 0.0), 
				1.0,
				180.0,
				270.0, 
				0
		);
		
		testSegmentArcIntersectionCase(
				new Point2D.Double(-1.0, 1.0), 
				new Point2D.Double(1.0, -1.0), 
				new Point2D.Double(0.0, 0.0), 
				1.0,
				180.0, 
				270.0, 
				0
		);
		
		// docs/diagrams/ @ diagram 4
		testSegmentArcIntersectionCase(
				new Point2D.Double(1.0, 0.0), 
				new Point2D.Double(1.0, 2.0), 
				new Point2D.Double(0.0, 1.0), 
				1.0,
				270.0,
				90.0, 
				1
		);
		
		// docs/diagrams/ @ diagram 5
		testSegmentArcIntersectionCase(
				new Point2D.Double(2.5, 0.0), 
				new Point2D.Double(0.0, 2.5), 
				new Point2D.Double(0.0, 0.0), 
				2.0,
				0.0,
				90.0, 
				2
		);
		
		// docs/diagrams/ @ diagram 6
		testSegmentArcIntersectionCase(
				new Point2D.Double(0.5, 0.0), 
				new Point2D.Double(-0.5, 2.0), 
				new Point2D.Double(-1.0, 1.0), 
				1.0,
				0.0,
				90.0, 
				2
		);
		
		// docs/diagrams/ @ diagram 7
		testSegmentArcIntersectionCase(
				new Point2D.Double(1.0, 0.0), 
				new Point2D.Double(3.0, 0.0), 
				new Point2D.Double(0.0, 0.0), 
				2.0,
				0.0,
				90.0, 
				1
		);
	}
	
	public void testArcEndCase(Point2D arcStart, double arcStartDirection,
			double radius, double angle, Point2D expected) {
		Point2D p = Geometry.getArcEnd(arcStart, arcStartDirection, radius, angle);
		System.out.println("expected = " + expected);
		System.out.println("actual = " + p);
		double goodEnough = 1e-6; // Used 
		
		Assert.assertTrue(expected.distance(p) < goodEnough);
	}
	
	@Test
	public void testArcEnd() {
		// docs/diagrams/ @ diagram 8
		testArcEndCase(
				new Point2D.Double(1.0, 2.0),
				0.0,
				1.0,
				90,
				new Point2D.Double(2.0, 3.0)
		);
		
		// docs/diagrams/ @ diagram 9
		testArcEndCase(
				new Point2D.Double(0.0, 0.0),
				0.0,
				1.0,
				-90,
				new Point2D.Double(1.0, -1.0)
		);
		
		// docs/diagrams/ @ diagram 10
		testArcEndCase(
				new Point2D.Double(0.0, 0.0),
				0.0,
				-1.0,
				-90,
				new Point2D.Double(-1.0, 1.0)
		);
		
		// docs/diagrams/ @ diagram 11
		testArcEndCase(
				new Point2D.Double(0.0, 0.0),
				0.0,
				-1.0,
				90,
				new Point2D.Double(-1.0, -1.0)
		);
		
		// docs/diagrams/ @ diagram 12
		testArcEndCase(
				new Point2D.Double(0.0, 0.0),
				90.0,
				-1.0,
				90,
				new Point2D.Double(1.0, -1.0)
		);
		
		testArcEndCase(
				new Point2D.Double(50.0, 0.0), 
				90.0, 
				50.0, 
				90.0,
				new Point2D.Double(0.0, 50.0)
		);
	}
	
	public void testDirectionCase(Point2D start, Point2D end, double expected) {
		double p = Geometry.getVectorDirection(start, end);

		double goodEnough = 1e-2;
		Assert.assertTrue(Math.abs(expected - p) < goodEnough);
	}
	
	@Test
	public void testDirection() {
		testDirectionCase(new Point2D.Double(1,1), new Point2D.Double(2,2), 45);
		testDirectionCase(new Point2D.Double(0,0), new Point2D.Double(-3,-3), -135);
		testDirectionCase(new Point2D.Double(0,0), new Point2D.Double(3,-3), -45);
		testDirectionCase(new Point2D.Double(0,0), new Point2D.Double(-3,3), 135);
		testDirectionCase(new Point2D.Double(2,2), new Point2D.Double(3,-1.5), -74.05);
		testDirectionCase(new Point2D.Double(0,0), new Point2D.Double(0,3), 90.0);
		testDirectionCase(new Point2D.Double(2,2), new Point2D.Double(-3,-1.5), -145);
	}
	
	public void testAntiClockwiseAngleCase(double start, double end, double expected){
		double p = Geometry.getAntiClockWiseAngleDistance(start, end);
		double goodEnough = 1e-2;
		Assert.assertTrue(Math.abs(expected - p) < goodEnough);
	}
	
	@Test
	public void testAntiClockwiseAngle(){
		testAntiClockwiseAngleCase(0, 90, 90);
		testAntiClockwiseAngleCase(90, 0, 270);
		testAntiClockwiseAngleCase(90, 90, 0);
		testAntiClockwiseAngleCase(90, 89, 359);
		testAntiClockwiseAngleCase(44, 184, 140);
		testAntiClockwiseAngleCase(45, -45, 270);
		testAntiClockwiseAngleCase(-45, 45, 90);
		testAntiClockwiseAngleCase(-45, -45, 0);
	}
	
	public void testAngleWithinBoundsCase(double theta,
			double first, double second, boolean expected){
		boolean check = Geometry.angleWithinBounds(theta, first, second);
		Assert.assertEquals(expected, check);
	}
	
	@Test
	public void testAngleWithinBounds(){
		testAngleWithinBoundsCase(90, 0, 180, true);
		testAngleWithinBoundsCase(0, 90, 180, false);
		testAngleWithinBoundsCase(-15, 340, 0, true);
		testAngleWithinBoundsCase(-15, -90, -5, true);
		testAngleWithinBoundsCase(-15, -90, 355, true);
		testAngleWithinBoundsCase(-15, -90, -25, false);
	}
	
	public void testGetNumberOfRayCircleIntersectionsCase
				(Point2D vectorStart,
						Point2D directionVector, Point2D circleCentre,
						double circleRadius, int expected){
		int p = Geometry.getNumberOfRayCircleIntersections(vectorStart, directionVector, circleCentre, circleRadius);
		Assert.assertEquals(expected, p);
	}
	
	@Test
	public void testGetNumberOfRayCircleIntersections(){
		testGetNumberOfRayCircleIntersectionsCase(new Point2D.Double(0,0),
				new Point2D.Double(0,1),
				new Point2D.Double(0,0),
				1,
				2);
		testGetNumberOfRayCircleIntersectionsCase(new Point2D.Double(0,1),
				new Point2D.Double(1,0),
				new Point2D.Double(0,0),
				1,
				1);
		testGetNumberOfRayCircleIntersectionsCase(new Point2D.Double(2,2),
				new Point2D.Double(1,0),
				new Point2D.Double(0,0),
				1,
				0);
		testGetNumberOfRayCircleIntersectionsCase(new Point2D.Double(-1,-1),
				new Point2D.Double(1,1),
				new Point2D.Double(0,0),
				1,
				2);
		testGetNumberOfRayCircleIntersectionsCase(new Point2D.Double(-1,1),
				new Point2D.Double(1,1),
				new Point2D.Double(0,0),
				1,
				0);
	}
	
	
	@Test
	public void testIsArcLeft() {
		Assert.assertTrue(
				Geometry.isArcLeft(
						new Point2D.Double(0.0, 0.0),
						0.0,
						new Point2D.Double(1.0, 1.0)
				)
		);
	}
	
	@Test
	public void testIsPointBehind() {
		Assert.assertFalse(
				Geometry.isPointBehind(
						new Point2D.Double(0.0, 0.0), 
						90.0, 
						new Point2D.Double(1.0, 1.0)
				)
		);
		
		Assert.assertTrue(
				Geometry.isPointBehind(
						new Point2D.Double(0.0, 0.0), 
						90.0, 
						new Point2D.Double(1.0, -1.0)
				)
		);
		
		Assert.assertTrue(
				Geometry.isPointBehind(
						new Point2D.Double(0.0, 0.0), 
						90.0, 
						new Point2D.Double(-1.0, -1.0)
				)
		);
		
		Assert.assertFalse(
				Geometry.isPointBehind(
						new Point2D.Double(0.0, 0.0), 
						90.0, 
						new Point2D.Double(-1.0, 1.0)
				)
		);
	}
	
	public void testGetArcAngleCase(Point2D arcStart, double direction, Point2D arcEnd, Point2D centre, double expected) {
		double threshold = 0.0001;
		double actual = Geometry.getArcOrientedAngle(arcStart, direction, arcEnd, centre);
		System.out.println("expected: " + expected);
		System.out.println("actual: " + actual);
		Assert.assertTrue(Math.abs(expected - actual) < threshold);
	}
	
	@Test
	public void testGetArcAngle() {
		testGetArcAngleCase(
				new Point(0,0), 
				90, 
				new Point(1,1), 
				new Point(1,0), 
				90
				);
		
		testGetArcAngleCase(
				new Point(0,0), 
				90, 
				new Point(2,2), 
				new Point(2,0),
				90
				);
		
		testGetArcAngleCase(
				new Point(0,0), 
				90, 
				new Point(-1,1), 
				new Point(-1,0), 
				90
				);
		
		testGetArcAngleCase(
				new Point(0,0), 
				-90, 
				new Point(-1,-1), 
				new Point(-1, 0), 
				90
				);
		
		testGetArcAngleCase(
				new Point(0,0), 
				0, 
				new Point(1,-1), 
				new Point(0, -1), 
				90
				);
		
		testGetArcAngleCase(
				new Point(0,0), 
				0, 
				new Point(0,-60), 
				new Point(0,-30), 
				180
				);
		
		testGetArcAngleCase(
				new Point(0,0), 
				90, 
				new Point(-60, 0), 
				new Point(-30, 0),
				180
				);
		
		testGetArcAngleCase(
			new Point(0, 0), 
			0, 
			new Point(50, 50), 
			new Point(50, 0), 
			90
		);
	}

	public void testGetLinesIntersectionCase(Point2D a, Point2D b, Point2D c, Point2D d, Point2D expected) {

		Point2D actual = Geometry.getLinesIntersection(a, b, c, d);
		Assert.assertEquals(actual, expected);
	}
	
	@Test
	public void testGetLinesIntersection() {
		testGetLinesIntersectionCase(
				new Point(0,0), 
				new Point(1,1),  
				new Point(1,1), 
				new Point(1,2),
				new Point(1,1)
				);
		
		testGetLinesIntersectionCase(
				new Point(0,0), 
				new Point(2,2), 
				new Point(0,1), 
				new Point(1,2),
				null
				);
		
		testGetLinesIntersectionCase(
				new Point(0,0), 
				new Point(2,4), 
				new Point(2,2), 
				new Point(1,2),
				new Point(1,2)
				);
		
		testGetLinesIntersectionCase(
				new Point(1,0), 
				new Point(-2,-6), 
				new Point(2,2), 
				new Point(3,2),
				new Point(2,2)
				);
		
		testGetLinesIntersectionCase(
				new Point(1,0), 
				new Point(-3,0), 
				new Point(2,6), 
				new Point(-5,6),
				null
				);
		
		testGetLinesIntersectionCase(
				new Point(-2,0), 
				new Point(-2,-6), 
				new Point(-2,-2), 
				new Point(3,-4),
				new Point(-2,-2)
				);
		
		testGetLinesIntersectionCase(
				new Point(-2,0), 
				new Point(-2,-6), 
				new Point(-2,-2), 
				new Point(3,-4),
				new Point(-2,-2)
				);
		
		testGetLinesIntersectionCase(
				new Point(4,0), 
				new Point(-2,-6),
				new Point(2,-2), 
				new Point(4,-4),
				new Point(2,-2)
				);
	}

	public void testGetLineCircleIntersectionsParametersCase(Point2D segmentStart, Point2D segmentEnd, Point2D circleCentre, 
			double circleRadius, Pair<Double, Double> expected) {
		Assert.assertEquals(
				expected, 
				Geometry.getLineCircleIntersectionsParameters(
						segmentStart, 
						segmentEnd, 
						circleCentre, 
						circleRadius
				)
		);
	}
	
	@Test
	public void testGetLineCircleIntersectionsParameters() {
		testGetLineCircleIntersectionsParametersCase(
				new Point2D.Double(0.0, 0.0),
				new Point2D.Double(40.0, 40.0),
				new Point2D.Double(30.0, 40.0),
				10.0,
				new Pair <Double, Double> (
						0.75,
						1.0
				)
		);
	}
	
	public void testGetLineCircleIntersectionsCase(
			Point2D segmentStart, Point2D segmentEnd, Point2D circleCentre, 
			double circleRadius, Pair<Point2D, Point2D> expected
			) {
		Pair <Point2D, Point2D> n = Geometry.getLineCircleIntersections(
				segmentStart, 
				segmentEnd, 
				circleCentre, 
				circleRadius
		);
		
		Assert.assertEquals(expected, n);		
	}
	
	@Test
	public void testGetLineCircleIntersections() {
		
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(0.0, 0.0), 
				new Point2D.Double(40.0, 40.0), 
				new Point2D.Double(30.0, 40.0), 
				10.0,
				new Pair<Point2D, Point2D> (
						new Point2D.Double(30.0, 30.0),
						new Point2D.Double(40.0, 40.0)
				)
		);
		
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(1.0, 3.0), 
				new Point2D.Double(1.0, -5.0), 
				new Point2D.Double(1.0, 0.0), 
				1.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(1.0, 1.0),new Point2D.Double(1.0, -1.0))
				);
		
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(0.0, 2.0), 
				new Point2D.Double(0.0, -8.0), 
				new Point2D.Double(1.0, 0.0), 
				1.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(0.0, 0.0),new Point2D.Double(0.0, 0.0))
				);
		
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(0.0, 2.0), 
				new Point2D.Double(8.0, 2.0), 
				new Point2D.Double(1.0, 1.0), 
				1.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(1.0, 2.0),new Point2D.Double(1.0, 2.0))
				);
		
		try{
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(0.0, 2.0), 
				new Point2D.Double(12.0, 14.0), 
				new Point2D.Double(1.0, 1.0), 
				1.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(1.0, 2.0),new Point2D.Double(1.0, 2.0))
				);
				Assert.fail("There are no solutions to quadratic equation, so null is returned.");
		}catch(NullPointerException ex){
			
		}
		
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(1.0, 2.0), 
				new Point2D.Double(5.0, 2.0), 
				new Point2D.Double(2.0, 2.0), 
				1.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(1.0, 2.0),new Point2D.Double(3.0, 2.0))
				);
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(-1.0, 7.0), 
				new Point2D.Double(-1.0, -10.0), 
				new Point2D.Double(-1.0, -1.0), 
				2.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(-1.0, 1.0),new Point2D.Double(-1.0, -3.0))
				);
		
		try {
		testGetLineCircleIntersectionsCase(
				new Point2D.Double(2.0, 3.0), 
				new Point2D.Double(-4.0, 6.0), 
				new Point2D.Double(1.0, 1.0), 
				1.0,
				new Pair<Point2D, Point2D>(new Point2D.Double(1.0, 2.0),new Point2D.Double(1.0, 2.0))
				);
				Assert.fail("There are no solutions to quadratic equation, so null is returned.");
		} catch (NullPointerException ex) {
			
		}
	}
	
	public void testCrossProductCase (Point2D p1, Point2D p2, Point2D p3, double expected) {
		double crossProduct = Geometry.crossProduct(
				Geometry.getVectorDifference(p1, p2),
				Geometry.getVectorDifference(p3, p2)
		);
		
		Assert.assertEquals(expected, crossProduct);
	}
	
	@Test
	public void testCrossProduct() {
		testCrossProductCase(new Point(1,1), new Point(0,0), new Point(-1,1), 2.0);
	}
	
	@Test
	public void testGetVectorDifference() {
		Point p1 = new Point(0,0);
		Point p2 = new Point(-1,1);
		Point p3 = new Point(0,2);
		
		Point exp = new Point(1, -1);
		testGetVectorDifferenceCase(p1, p2, exp);

		exp = new Point(1, 1);
		testGetVectorDifferenceCase(p3, p2, exp);
	}

	public void testGetVectorDifferenceCase(Point p1, Point p2, Point exp) {
		Assert.assertEquals(exp, Geometry.getVectorDifference(p1, p2));
	}
	
	public void testgetNumberOfLineSegmentArcIntersectionsCase(Point2D segmentStart,
			Point2D segmentEnd, 
			Point2D circleCentre,
			double circleRadius,
			double arcStartAngle,
			double arcEndAngle,
			int expected){
		int actual = Geometry.getNumberOfLineSegmentArcIntersections(
				segmentStart, segmentEnd, circleCentre, circleRadius, 
				arcStartAngle, arcEndAngle);
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testIsPointOnArc(){
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(1,0),
				new Point2D.Double(-1,0),
				new Point2D.Double(0,0),
				1,
				0,
				180,
				2);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(-1,-1),
				new Point2D.Double(1,1),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				0);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(-2,-2),
				new Point2D.Double(1,1),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				1);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(-2,-2),
				new Point2D.Double(2,2),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				2);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(-2,-2),
				new Point2D.Double(-2,2),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				1);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(2,-2),
				new Point2D.Double(-1,1),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				0);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(-1,-3),
				new Point2D.Double(1,-1),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				1);
		testgetNumberOfLineSegmentArcIntersectionsCase(
				new Point2D.Double(-1,-3),
				new Point2D.Double(3,1),
				new Point2D.Double(0,0),
				2,
				0,
				270,
				2);
	}
}
