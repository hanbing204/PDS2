package group2.sdp.pc.globalinfo;

import group2.sdp.common.util.Geometry;
import group2.sdp.pc.breadbin.DynamicBallInfo;
import group2.sdp.pc.breadbin.DynamicInfo;
import group2.sdp.pc.breadbin.DynamicRobotInfo;
import group2.sdp.pc.breadbin.StaticRobotInfo;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Contains various functions all performed on DynamicInfo
 * TODO: move to util, rename to Geometry; move Tests file also
 */
public class DynamicInfoChecker {

	private static final boolean VERBOSE = true;
	
	public DynamicInfoChecker(DynamicInfo dynamicInfo) {
		//		this.dynamicInfo = dynamicInfo;
	}

	/**
	 * This function finds the smallest angle between a robot and his target.
	 * 
	 * @param isAlfie If the robot we are getting the angle for is Alfie or not
	 * @return The angle to turn at.
	 */
	public static int getAngleToBall(Point2D targetPosition, Point2D robotPosition, double facingDirection) {

		double dx = (targetPosition.getX() - robotPosition.getX());
		double dy = (targetPosition.getY() - robotPosition.getY());

		double angle = Math.toDegrees(Math.atan2(dy, dx));

		if (angle < 0) {
			angle = 360 + angle;
		}
		double result = angle - facingDirection;
		// Variables angle and facingDirection are between 0 and 360. Thus result is 
		// between -360 and 360. We need to normalize to -180 and 180. 
		if (result < -180) {
			result += 360;
		} else if (result > 180) {
			result -= 360;
		}
		return (int) result;
	}

	/**
	 * returns the angle from a point to another point with repect the plane of are zero angle. 
	 * @param origin 
	 * @param targetPosition position of the target we are working out the angle to the ball
	 * @return double
	 */
	public static double getAngleFromOrigin(Point2D origin, Point2D targetPosition) {
		double dx = (targetPosition.getX() - origin.getX());
		double dy = (targetPosition.getY() - origin.getY());

		double angle = Math.toDegrees(Math.atan2(dy, dx));
		if(angle<0){
			angle = 360 +angle;
		}
		return angle;
	}

	private static boolean lastHadBall = false;
	
	/**
	 * Projects a point from the centroid of the robot in the facing
	 * direction of the robot. Checks the distance between that point
	 * and the ball. The distance could be more than half the width of
	 * the robot and the centroid is slightly to the back given the 
	 * current construction. Thus, the thresholds.
	 * @param robot is the DynamicRobotInfo of robot we are checking
	 * @param ballPosition position of the ball
	 * @return true if the robot is in possession of the ball
	 */
	public static boolean hasBall(StaticRobotInfo robot, Point2D ballPosition){
		double lengthThreshold = 2.0;
		double halfLength = (StaticRobotInfo.getLength()) / 2 + lengthThreshold;
		double widthThreshold = 0.0;
		double halfWidth = (StaticRobotInfo.getWidth()) / 2 + widthThreshold;
		Point2D frontOfRobot = Geometry.generatePointOnLine(robot.getPosition(),
				robot.getFacingDirection(), halfLength);
		
		boolean result = frontOfRobot.distance(ballPosition) < halfWidth
				? true
				: false;
		if (VERBOSE) {
//			if (!lastHadBall && result) {
//				System.out.println("Acquired ball!");
//			}
//			System.out.println("Front of robot: " + frontOfRobot);
//			System.out.println((result ? "Has " : "Does not have ") + "ball.");
		}
		lastHadBall = result;
		return result;
	}
	
	/**
	 * Looks into the future to check if the given robot would get the given ball
	 * in 0.35 seconds.
	 */
	public static boolean wouldHaveBall(DynamicRobotInfo robot, DynamicBallInfo ball) {
		double lengthThreshold = 2.0;
		double halfLength = (StaticRobotInfo.getLength()) / 2 + lengthThreshold;
		double widthThreshold = 0.0;
		double halfWidth = (StaticRobotInfo.getWidth()) / 2 + widthThreshold;
		
		double FUTURE_PERIOD = 0.35; // seconds
	
		Point2D futureBallPosition = Geometry.generatePointOnLine(
				ball.getPosition(), ball.getRollingDirection(), 
				ball.getRollingSpeed() * FUTURE_PERIOD);
		
		Point2D futurePosition = Geometry.generatePointOnLine(
				robot.getPosition(), robot.getTravelDirection(), 
				robot.getTravelSpeed() * FUTURE_PERIOD);
	
		// TODO future facing direction?
		
		Point2D frontOfRobot = Geometry.generatePointOnLine(futurePosition,
				robot.getFacingDirection(), halfLength);
		
		boolean result = frontOfRobot.distance(futureBallPosition) < halfWidth
				? true
				: false;
		if (VERBOSE) {
			if (!lastHadBall && result) {
				System.out.println("Will acquire ball!");
			}
			System.out.println((result ? "Would have " : "Would not have ") + "ball.");
		}
		return result;
	}

	/**
	 * Checks if a robot is between the goal that Alfie needs to guard and the ball or not.
	 */
	public static boolean defensiveSide(DynamicRobotInfo robotInfo, DynamicBallInfo ballInfo){
		double xAlfie = robotInfo.getPosition().getX();
		double xBall = ballInfo.getPosition().getX();
		double xGoal = GlobalInfo.getDefensiveGoalMiddle().getX();
		return Math.abs(xGoal- xBall) > Math.abs(xGoal - xAlfie); 
	}

	/**
	 * Checks if the robot is blocking our path(in the current facing direction). Projects a line from our 
	 * centroid to a box drawn around the opponent and checks for intersection. 
	 * @param alfie
	 * @param opponent
	 * @return is the opponent blocking our path
	 */
	public static boolean opponentBlockingPath(DynamicRobotInfo alfie, Point2D obstaclePosition){
		if(alfie.getFacingDirection()==-1){
			if (VERBOSE)
				System.out.println("angle negative return false");
			return false;
		}
		Point2D alfiePos = alfie.getPosition();
		double x=alfiePos.getX();
		double y=alfiePos.getY();
		double angle=alfie.getFacingDirection();
		double constant;
		double slope;
		//calculating equation of the line from our centroid in facing direction
		if (angle == 90 || angle == 270){
			angle= angle+1;
		}
		slope=Math.tan(Math.toRadians(angle));
		//now work out constant for y=mx+c using c=y-mx
		constant=y-(slope*x);

		//increase x or y by 100 to create arbitrary point for end of line segment(therefore line of minmum length 100. can be tweaked)
		Point2D.Double endP;
		//case increase y
		if (angle>=45 && angle<135){
			double yEnd=alfiePos.getY()+50;
			double xEnd=(yEnd-constant)/slope;
			endP=new Point2D.Double(xEnd, yEnd);
		}
		else if ( (angle >= 0 && angle < 45) || angle >= 315 ){									
			double xEnd = alfiePos.getX() + 50;
			double yEnd = (slope * xEnd) + constant;
			endP=new Point2D.Double(xEnd, yEnd);
		}
		else if (angle >= 135 && angle < 225){
			double xEnd = alfiePos.getX() - 50;
			double yEnd = (slope * xEnd) + constant;
			endP = new Point2D.Double(xEnd, yEnd);
		}else if (angle >= 225 && angle < 315){
			double yEnd = alfiePos.getY() - 50;
			double xEnd = (yEnd-constant) / slope;
			endP = new Point2D.Double(xEnd, yEnd);
		}else{

			System.out.print("angle weird return false");
			return false;
		}
		//line created
		Line2D.Double ourLine = new Line2D.Double(alfiePos, endP);
		//now create box around obstacle 21 by 21 (non rotating)
		double topLeftX=obstaclePosition.getX()-12;
		double topLeftY=obstaclePosition.getY()-12;
		Rectangle2D.Double enemyBox = new Rectangle2D.Double(topLeftX, topLeftY, 24, 24);
		//now check if the line intersects the box 


		if (enemyBox.contains(alfiePos)) {
			return isSimilarAngle(getAngleFromOrigin(alfiePos, obstaclePosition), alfie.getFacingDirection(), 30);
		} else {
			return enemyBox.intersectsLine(ourLine);
		}

	}

	/**
	 * Finds the position "behind" the ball. Checks which part of the pitch 
	 * the ball is in an then gets to a position based on that. If the ball 
	 * is near one of the top or bottom walls then it gets to a position so that 
	 * when it turns to face the ball it will be at a 45 degree angle and therefore 
	 * more likely to score if we kick. If we are within the y coordinates of the goal 
	 * then we get behind the ball so that when we face the ball we will face the goal.
	 * @param ballPosition
	 * @return
	 */
	public static Point2D getKickingPosition(Point2D ballPosition) {
		float kickingPositionX,kickingPositionY;

		// distance to be away from the ball
		int distance = 25;
		// distance to be away from the ball by x and y coordinates.
		double sideDistance = Math.sqrt(2*distance*distance);
		// if the ball is within the y coordinates of the goal then get immediately behind it
		if (GlobalInfo.getPitch().getTopGoalPostYCoordinate() - 5 > ballPosition.getY() &&
				GlobalInfo.getPitch().getBottomGoalPostYCoordinate() + 5 < ballPosition.getY()) {

			kickingPositionX = (float) (GlobalInfo.isAttackingRight()
					?  ballPosition.getX() - distance
							: ballPosition.getX() + distance);
			kickingPositionY = (float) (ballPosition.getY());
			// have already checked if ball is near middle of pitch so this check is sufficient
		} else if (ballPosition.getY() > 0) {
			kickingPositionX = (float) (GlobalInfo.isAttackingRight()
					? ballPosition.getX() - sideDistance
							: ballPosition.getX() + sideDistance);
			kickingPositionY = (float) (ballPosition.getY() + sideDistance);
		} else {
			kickingPositionX = (float) (GlobalInfo.isAttackingRight()
					? ballPosition.getX() - sideDistance
							: ballPosition.getX() + sideDistance);
			kickingPositionY = (float) (ballPosition.getY() - sideDistance);
		}

		// check if position is within bounds
		if ((kickingPositionY > GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMaxY() - 13) || 
				(kickingPositionY < GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMinY() + 13)){
			//out of bounds :(
			kickingPositionX = (float) (GlobalInfo.isAttackingRight()
					?  ballPosition.getX() - distance
							: ballPosition.getX() + distance);
			kickingPositionY = (float) (ballPosition.getY());
		}
		if ((kickingPositionY > GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMaxY() - 13) || 
				(kickingPositionY < GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMinY() + 13)){
			kickingPositionX = (float) ballPosition.getX();
			kickingPositionY = (float) ballPosition.getY();
		}
		if (VERBOSE)
			System.out.println("DEFENSIVE");

		Point2D kickingPosition = new Point.Float(kickingPositionX,kickingPositionY);


		return kickingPosition;

	}

	/**
	 * See http://en.wikipedia.org/wiki/Line-line_intersection for calculation
	 * @param robotPosition
	 * @param ballPosition
	 * @param opponentPosition
	 * @param radius
	 * @return
	 */
	public static Point2D.Double findTangentIntersect(Point2D robotPosition, Point2D ballPosition, Point2D opponentPosition, double radius) {

		Point2D.Double alfieDangerZoneIntersection = findCircleTangentIntersect(robotPosition, opponentPosition, radius);
		Point2D.Double ballDangerZoneIntersection = findCircleTangentIntersect(ballPosition, opponentPosition, radius);

		// purely for shortness and to fit with wikipedia maths
		double x1 = alfieDangerZoneIntersection.getX();
		double y1 = alfieDangerZoneIntersection.getY();
		double x2 = robotPosition.getX();
		double y2 = robotPosition.getY();
		double x3 = ballPosition.getX();
		double y3 = ballPosition.getY();
		double x4 = ballDangerZoneIntersection.getX();
		double y4 = ballDangerZoneIntersection.getY();

		double intersectionX = ((x1*y2 - y1*x2)*(x3 - x4) - (x1 - x2)*(x3*y4 - y3*x4)) / 
		(((x1 - x2)*(y3 - y4)) - ((y1 - y2)*(x3 - x4)));

		double intersectionY = ((x1*y2 - y1*x2)*(y3 - y4) - (y1 - y2)*(x3*y4 - y3*x4)) / 
		(((x1 - x2)*(y3 - y4)) - ((y1 - y2)*(x3 - x4)));

		return new Point2D.Double(intersectionX, intersectionY);
	}
	/**
	 * See http://paulbourke.net/geometry/2circle/
	 * @param P0 Alfie or ball (outside circle)
	 * @param P1 Opponent (circle center)
	 * @param r1 radius of danger zone
	 * @return
	 */
	public static Point2D.Double findCircleTangentIntersect(Point2D p0, Point2D p1, double r1) {

		double d = p1.distance(p0);
		double r0 = Math.sqrt(r1*r1 + d*d);
		double a = ((r0*r0 - r1*r1) + d*d)/(2*d);
		double h = Math.sqrt(r0*r0 - a*a);

		//TODO check signs
		double halfWayPointX = p0.getX() + (a*(p1.getX() - p0.getX())/d);
		double halfWayPointY = p0.getY() + (a*(p1.getY() - p0.getY())/d);

		double x1 = halfWayPointX + h*(p1.getY() - p0.getY())/d;
		double y1 = halfWayPointY - h*(p1.getX() - p0.getX())/d;

		double x2 = halfWayPointX - h*(p1.getY() - p0.getY())/d;
		double y2 = halfWayPointY + h*(p1.getX() - p0.getX())/d;

		Point2D.Double point1 = new Point.Double(x1,y1);
		Point2D.Double point2 = new Point.Double(x2,y2);

		if (p1.getY() > 0) {
			if (y1 > y2) {
				return point2;
			} else {
				return point1;
			}
		} else {
			if (y1 > y2) {
				return point1;
			} else {
				return point2;
			}
		}

	}

	/**
	 * Method for comparison of angles. If angle within certain threshold of each other then
	 * return true else false
	 * @param angle1 first angle for comparison
	 * @param angle2 second angle for comparison
	 * @param threshold max difference for angles to be similar
	 * @return are angles within threshold of each other
	 */
	public static boolean isSimilarAngle(double angle1, double angle2, double threshold){
		double bigAngle;
		double smallAngle;
		if (angle1==angle2){
			return true;
		}
		if (angle1>=angle2){
			bigAngle=angle1;
			smallAngle=angle2;
		}else{
			bigAngle=angle2;
			smallAngle=angle1;
		}
		if(bigAngle-smallAngle<=threshold){
			return true;
		}
		//check to solve 360-0 problem
		if(bigAngle>=(360-threshold) && smallAngle<=0+(threshold-(360-bigAngle))){
			return true;
		}
		return false;
	}

	/**
	 * this function will tell us if we are facing the oppositions goal
	 * it compares the angle we are facing with the angle to the extremes
	 * of the goal, it must act differently for each goal as one of the goals has the zero angle in the middle 
	 * @param robotInfo robot's info
	 * @param opponentInfo opponent info used to get the points of the goal where shooting for
	 * @param ball nuff said
	 * @return boolean
	 */
	public static boolean shotOnGoal(DynamicRobotInfo robotInfo, DynamicRobotInfo opponentInfo, Point2D ball){
		float x = (float) (
				!GlobalInfo.isAttackingRight() 
				? GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMinX()
						: GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMaxX()
		);
		float y1 = GlobalInfo.getPitch().getTopGoalPostYCoordinate();
		float y2 = GlobalInfo.getPitch().getBottomGoalPostYCoordinate();


		Point2D topGoal = new Point2D.Float(x, y1);
		Point2D bottomGoal = new Point2D.Float(x, y2);
		Point2D alfiePos = robotInfo.getPosition();
		Point2D enemyPos = opponentInfo.getPosition();
		double facing = robotInfo.getFacingDirection();

		x = (float) (
				GlobalInfo.isAttackingRight() 
				? GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMinX()
						: GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMaxX()
		);
		float y = GlobalInfo.getPitch().getTopGoalPostYCoordinate();

		Point2D ourGoal = new Point2D.Float(x, y);
		double ourGoalLine = ourGoal.getX();
		double theirGoalLine = topGoal.getX();

		double topAngle = getAngleFromOrigin(alfiePos,topGoal);
		double bottomAngle = getAngleFromOrigin(alfiePos, bottomGoal);
		//if other robot is in the way threshold can be changed, current uses 30 degree angle and 30cm distance
		if((alfiePos.distance(enemyPos)<30)&&(isSimilarAngle(getAngleFromOrigin(alfiePos,enemyPos),robotInfo.getFacingDirection(),30))){
			if (VERBOSE)
				System.out.println("ENEMY CLOSE NO SHOT");
			return false;
		}

		if(theirGoalLine > ourGoalLine) {
			if(facing>bottomAngle || facing<topAngle) {
				return true;
			}else{
				return false;
			}
		} else {
			if (facing<bottomAngle && facing>topAngle) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Get the x coordinate of the defensive goal of the robot
	 * @param isAlfie whether or not we want to get the defensive 
	 * 			goal of Alfie
	 * @return x coordinate of the goal we are defending
	 */
	public static int getDefensiveGoalOfRobot(boolean isAlfie){

		if (isAlfie){
			return GlobalInfo.isAttackingRight() 
			? (int)GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMinX()
					: (int)GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMaxX();
		}
		else{
			return !GlobalInfo.isAttackingRight() 
			? (int)GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMinX()
					: (int)GlobalInfo.getPitch().getMinimumEnclosingRectangle().getMaxX();
		}
	}
}
