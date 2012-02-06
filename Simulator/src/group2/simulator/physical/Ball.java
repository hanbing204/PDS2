package group2.simulator.physical;

import java.awt.Color;

import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.shapes.Circle;

/**
 * @author s0923534
 *
 */
/**
 * @author s0923534
 *
 */
public class Ball extends BoardObject {

	
	private static int scoreTimeCounter;
	
	private static int leftGateCordX = 100;
	private static int rightGateCordX = 700;
	private static int pitchTopCordY = 112;
	private static int pitchDownCordY = 420;
	
	private static int gateTopCordY = 193;
	private static int gateDownCordY = 338;

	
	
	float radius;
	Body leftGoalLine;
	Body rightGoalLine;
	

	/**
	 * Constructor that fully initialises the ball object
	 * @param x
	 * @param y
	 * @param radius
	 * @param color
	 * @param angle
	 */
	public Ball(float x, float y, float radius, Color color, int angle) {
		super(x, y, "Ball", new Circle(radius), 5, color, angle);
		this.body.setUserData(this);
		this.body.setDamping(0.005f);
		this.body.setRestitution(0.8f);
		this.body.setCanRest(true);
		this.radius = radius;
		scoreTimeCounter = 0;
		
	}
	
	
	/**
	 * Function to kick the ball and change its position 
	 * @param angle is angle at which the ball is kicked
	 */
	public void kick(double angle) {
		int force = 1000;
		float x = (force * (float) Math.cos(Math.toRadians(angle)));
		float y = (force * (float) Math.sin(Math.toRadians(angle)));
		this.body.addForce(new Vector2f(x, y));
		this.move(angle);

	}
		
	/**
	 * Move the ball forward by distance dist
	 * @param angle
	 */
	public void move(double angle) {
		int dist = 5;
		float x = (this.getX() + (dist * (float) Math
				.cos(Math.toRadians(angle))));
		float y = (this.getY() + (dist * (float) Math
				.sin(Math.toRadians(angle))));
		this.body.setPosition(x, y);
		
	}

	/**
	 * Function that stops the ball
	 */
	public void stop() {
		this.body.setForce(0, 0);
		this.body.adjustVelocity(((Vector2f) this.body.getVelocity()).negate());
	}
	
	public void setGoalLines(Body leftGL, Body rightGL) {
		this.leftGoalLine = leftGL;
		this.rightGoalLine = rightGL;
	}

	public void ignoreGoalLines() {
		this.body.addExcludedBody(leftGoalLine);
		this.body.addExcludedBody(rightGoalLine);
	}

	public void stayInGoal() {
		this.body.removeExcludedBody(leftGoalLine);
		this.body.removeExcludedBody(rightGoalLine);
	}
	
	
	/**
	 *  Checks if the ball has hit the wall
	 *  If it does - increments ScoreTimeCounter.
	 *  First 'if' statement checks if the the ball is in left gate
	 *  First 'if' statement checks if the the ball is in right gate
	 *  Third 'if' statement checks if the the ball is in pitch
	 * @return true if it hits the wall 
	 */
	public boolean doesItHitWall() {
		float x = this.getX();
		float y = this.getY();
		
		if ( x < leftGateCordX && y > gateTopCordY+1 && y < gateDownCordY+1)
		{
			
			incrScoreTime();
			return false;
		}
		else
			if( x > rightGateCordX+1 && y > gateTopCordY && y < gateDownCordY)
			{
				incrScoreTime();
				return false;	
			}
			else
			
			if (x < 101 || x > 720 || y < pitchTopCordY || y > pitchDownCordY ){
				System.out.println(x +" "+ y);
				return true;
			}
			else
			
			return false;
			
		
	}

	public float checkXPosition(float x, float y) {
		if ((x < 105 && y < 192) || ( x < 105 && y > 338)){
			
			return 105;	
		}
		else if (x > rightGateCordX &&  y < pitchTopCordY & y > gateDownCordY)
		{	
			
			return rightGateCordX;	
		}
		return x;
	}

	public float checkYPosition(float x, float y) {
		if (y < pitchTopCordY-2 )
			return pitchTopCordY-2;
		else if (y > pitchDownCordY+2)
			return pitchDownCordY+2;
		return y;
	}
	
	
	public static void incrScoreTime(){
		scoreTimeCounter++;
	}
	

	/**
	 * Check is the ball has scored
	 * @param scoreTimeCounter hold the number of frames of ball being in gates
	 * @return true if the robot scored
	 */
	public boolean didItScore(){
		if (scoreTimeCounter == 10){
			scoreTimeCounter = 0;
			System.out.println("Score!!!!");
			return true;
		}
		return false;				
		
	}
	
	
	

}