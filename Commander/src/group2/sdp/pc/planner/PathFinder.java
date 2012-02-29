
package group2.sdp.pc.planner;

import group2.sdp.pc.breadbin.DynamicPitchInfo;
import group2.sdp.pc.planner.operation.Operation;
import group2.sdp.pc.planner.operation.OperationCharge;
import group2.sdp.pc.planner.operation.OperationOverload;
import group2.sdp.pc.planner.operation.OperationReallocation;
import group2.sdp.pc.planner.operation.OperationStrike;
import group2.sdp.pc.server.skeleton.ServerSkeleton;
import group2.sdp.pc.vision.skeleton.DynamicInfoConsumer;

import java.awt.geom.Point2D;

/**
 * Takes a command from the planner and executes it. This class is responsible for 
 * sending the "physical" instructions to Alfie or the simulator.
 * 
 * Every new command should be added to the switch statement and to the enum in ComplexCommand.
 */
public class PathFinder implements DynamicInfoConsumer {

	/**
	 * Sets verbose mode on or off, for debugging
	 */
	private static final boolean VERBOSE = false;

	/**
	 * TODO THESE THRESHOLDS STILL NEED TO BE TESTED TO FIND IDEAL VALUES 
	 */

	private static final int MAX_SPEED = 54;
	private static final int CRUISING_SPEED = 35;
	private static final int TURNING_SPEED = 54;	

	/**
	 * Alfie needs to be within this angle when he is *FAR AWAY* from the ball (> TARGET_SHORT_THRESHOLD)
	 * before he's satisfied that he's facing in an accurate enough direction
	 */
	private static final int LONG_TURNING_ERROR_THRESHOLD = 25;

	/**
	 * Alfi needs to be within this angle when he is *CLOSE* to the ball (<= TARGET_SHORT_THRESHOLD)
	 * before he's satisfied that he's facing in an accurate enough direction
	 */
	private static final int STOP_TURNING_ERROR_THRESHOLD = 10;

	/**
	 * The SeverSkeleton implementation to use for executing the commands. 
	 * Can be the Alfie bluetooth server or the simulator.
	 */
	private ServerSkeleton alfieServer;

	/**
	 * The command that is currently being executed.
	 */
	private Operation currentOperation; 

	/**
	 * If Alfie is turning right now.
	 */
	private boolean turning;

	/**
	 * Initialise the class and the ServerSkeleton to send commands to Alfie or the simulator, make sure
	 * the ServerSkeleton object passed is already initialised and connected
	 * 
	 * @param alfieServer The initialised bluetooth server or the simulator object
	 */
	public PathFinder(ServerSkeleton alfieServer) {
		this.alfieServer = alfieServer;
	}


	/**
	 * Sets the operation. Called when field marshall replans. consumeInfo handles execution of command
	 *  
	 * @param currentCommand The command to be set.
	 */
	public void setOperation(Operation currentCommand) {
		this.currentOperation = currentCommand;

	}


	/**
	 * This function is the basic movement function. It will take the passed command and use its 
	 * relevant information to work out how to navigate to the target.
	 *
	 * @param currentCommand Contains the state information for Alfie and the target 
	 */
	private void executeOperationReallocation(OperationReallocation currentCommand) {
		Point2D targetPosition = currentCommand.getTarget();
		Point2D alfiePosition = currentCommand.getOrigin();
		double alfieDirection = currentCommand.getFacingDirection();

		int angleToTurn = (int)getAngleToTarget(targetPosition, alfiePosition, alfieDirection);
		int distanceToTarget = (int) alfiePosition.distance(targetPosition);
		int threshold;

		if(distanceToTarget < Overlord.RweClose) {
			threshold = STOP_TURNING_ERROR_THRESHOLD;
		} else {
			threshold = LONG_TURNING_ERROR_THRESHOLD;
		}

		if (VERBOSE) {
			System.out.println("Angle to turn to: " + angleToTurn);
			System.err.println("Distance: " + distanceToTarget);
		}

		// If Alfie is not facing the ball:
		if (Math.abs(angleToTurn) > threshold) {
			turning = true;
			if (angleToTurn < 0) {
				alfieServer.sendSpinRight(TURNING_SPEED, Math.abs(angleToTurn));
				if(VERBOSE) {
					System.out.println("Turning right " + Math.abs(angleToTurn) + " degrees");
				}
			} else {
				alfieServer.sendSpinLeft(TURNING_SPEED, angleToTurn);
				if(VERBOSE) {
					System.out.println("Turning left " + angleToTurn + " degrees");
				}
			}
		} else {
			// Alfie is facing the ball: go forwards
			turning = false;
			//adding in stuff to avoid other player
			alfieServer.sendGoForward(CRUISING_SPEED, 0);
			if(VERBOSE) {
				System.err.println("Going forward at speed: " + CRUISING_SPEED);
			}
		}
	}

	/**
	 * This function is the basic dribbling function. Currently it just dribbles forward
	 * Later, logic should be added to steer Alfie towards goal and away from the opponent
	 *
	 * @param currentCommand Contains the state information for Alfie, the ball and the opponent robot
	 */	
	private void executeOperationCharge(OperationCharge currentCommand) {
		// moves in an arc to face the goal. The arc diameter is half the distance 
		// between the ball and the goal, so that it has time to shoot before it is facing 
		// the goal
		double distance = (currentCommand.getAlfie().distance(currentCommand.getMiddle()));
		alfieServer.sendMoveArc((int)(0.5*distance), 90);
	}

	/**
	 * This function is called directly before we kick ass and explode into rampant celebration 
	 * 
	 * @param currentCommand Contains absolutely no useful information at all
	 */	
	private void executeOperationStrike(OperationStrike currentCommand) {
		alfieServer.sendKick(MAX_SPEED);
	}

	/**
	 * This function stops Alfie and makes him wait for the next instruction
	 */
	private void executeOperationOverload(OperationOverload currentCommand) {
		alfieServer.sendStop();
	}

	/**
	 * This function finds the smallest angle between Alfie and his target.
	 * 
	 * @param targetPosition Position of the target.
	 * @param alfiePosition Position of Alfie.
	 * @param facingDirection The angle Alfie is facing.
	 * 
	 * @return The angle to turn at.
	 */
	protected static double getAngleToTarget(Point2D targetPosition, Point2D alfiePosition, double facingDirection) {
		double dx = (targetPosition.getX() - alfiePosition.getX());
		double dy = (targetPosition.getY() - alfiePosition.getY());

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
		return result;
	}

	@Override
	public void consumeInfo(DynamicPitchInfo dpi) {
		//TODO act upon all commands correctly

		/*
		 * OperationReallocation should either make Alfi spin until he is facing his target or move forward
		 * until he is within STOP_TURNING_ERROR_THRESHOLD 
		 */
		if (currentOperation instanceof OperationReallocation) {

			if (VERBOSE) { 
				System.out.println("OperationReallocation"); 
			}

			OperationReallocation cmd = (OperationReallocation) currentOperation;

			// Get the position of the target from OperationReallocation
			Point2D targetPosition = cmd.getTarget();

			// Calculate the angle between the ball and the target (usually the ball)
			int angleToTurn = (int) getAngleToTarget(targetPosition, 
					dpi.getAlfieInfo().getPosition(), 
					dpi.getAlfieInfo().getFacingDirection());

			if (VERBOSE) {
				System.out.println("Target at " + angleToTurn + " degrees");
			}

			/*
			 * If Alfi is turning and the angle is within STOP_TURNING_ERROR_THRESHOLD then we need to stop
			 * and have the FieldMarshall decide what he should do next
			 */
			if (turning && Math.abs(angleToTurn) <= STOP_TURNING_ERROR_THRESHOLD) {

				// Makes Alfi stop turning
				if(VERBOSE) {
					System.out.println("Alfie is facing the correct direction!");
				}

				// Create new OperationReallocation command which should make Alfi move forward
				cmd = new OperationReallocation(cmd.getTarget(), 
						dpi.getAlfieInfo().getPosition(), 
						dpi.getAlfieInfo().getFacingDirection());

				// Perform the Alfi magic!
				executeOperationReallocation(cmd);

			} else {

				// Alfie isn't facing the ball so we need to reposition him
				if (Math.abs(angleToTurn) > STOP_TURNING_ERROR_THRESHOLD) {

					if(VERBOSE) {
						System.out.println("in moving forward the robot is no longer facing the robot new operation");
					}

					/*
					 * Create a new OperationReallocation which should tell Alfi to turn until he is facing
					 * the ball					
					 */
					cmd = new OperationReallocation(cmd.getTarget(), 
							dpi.getAlfieInfo().getPosition(), 
							dpi.getAlfieInfo().getFacingDirection());

					// Do the magic!
					executeOperationReallocation(cmd);

				} else {
					cmd = new OperationReallocation(cmd.getTarget(), 
							dpi.getAlfieInfo().getPosition(), 
							dpi.getAlfieInfo().getFacingDirection());

					// Do the magic!
					executeOperationReallocation(cmd);
					/* 
					 * Alfi should automatically stop when he reaches the ball and score a goal if he has faith
					 * in his ability to score (i.e. is facing the goal), otherwise he'll probably do something
					 * stupid (like a real football player)
					 */	
					System.err.println("Field Marshall should give a new command");
				}

			}

		} else if (currentOperation instanceof OperationOverload) {
			System.out.println("OperationOverload");
			executeOperationOverload((OperationOverload)currentOperation);	
		} else if (currentOperation instanceof OperationCharge) {
			System.out.println("OperationCharge");
			executeOperationCharge((OperationCharge)currentOperation);	
		} else if (currentOperation instanceof OperationStrike) {
			System.out.println("OperationStrike");
			executeOperationStrike((OperationStrike)currentOperation);	
		}
	}
}
