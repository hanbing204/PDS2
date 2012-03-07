
package group2.sdp.pc.planner;

import group2.sdp.pc.breadbin.DynamicInfo;
import group2.sdp.pc.globalinfo.DynamicInfoChecker;
import group2.sdp.pc.globalinfo.GlobalInfo;
import group2.sdp.pc.planner.operation.*;
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
	private static final int CRUISING_SPEED = 10;
	private static final int TURNING_SPEED = 10;	
	private static final int MOVING_KICK_SPEED = 25;
	
	int movetype =0;
	
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

	double oldAngle = 0;
	double newAngle =0;
	
	/**
	 * The SeverSkeleton implementation to use for executing the commands. 
	 * Can be the Alfie bluetooth server or the simulator.
	 */
	private ServerSkeleton alfieServer;

	/**
	 * The command that is currently being executed.
	 */
	private Operation currentOperation; 

	private GlobalInfo globalInfo;

	/**
	 * Used to perform functions on the DynamicInfo
	 */
	private DynamicInfoChecker dynamicInfoChecker;
	
	/**
	 * Initialise the class and the ServerSkeleton to send commands to Alfie or the simulator, make sure
	 * the ServerSkeleton object passed is already initialised and connected
	 * 
	 * @param alfieServer The initialised bluetooth server or the simulator object
	 */
	public PathFinder(GlobalInfo globalInfo,ServerSkeleton alfieServer) {
		this.globalInfo = globalInfo;
		this.alfieServer = alfieServer;
	}


	/**
	 * Sets the operation. Called when field marshal re-plans. consumeInfo handles execution of command
	 *  
	 * @param currentOperation The command to be set.
	 */
	public void setOperation(Operation currentOperation) {
		this.currentOperation = currentOperation;
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
		Point2D enemyPosition = currentCommand.getOpponent();
		double alfieDirection = currentCommand.getFacingDirection();

		int angleToTurn = dynamicInfoChecker.getAngleToBall(targetPosition, alfiePosition, alfieDirection);
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
			movetype = 1;
			if (angleToTurn < 0) {
				alfieServer.sendSpinRight(TURNING_SPEED, Math.abs(angleToTurn)-10);
				if(VERBOSE) {
					System.out.println("Turning right " + Math.abs(angleToTurn) + " degrees");
				}
			} else {
				alfieServer.sendSpinLeft(TURNING_SPEED, Math.abs(angleToTurn)-10);
				if(VERBOSE) {
					System.out.println("Turning right " + Math.abs(angleToTurn) + " degrees");
				}
				if(VERBOSE) {
					System.out.println("Turning left " + angleToTurn + " degrees");
				}
			}
		} else {
			// Alfie is facing the ball: go forwards
			
			//adding in stuff to avoid other player
//			if((alfiePosition.distance(enemyPosition)<35)&&(dynamicInfoChecker.isSimilarAngle(dynamicInfoChecker.getAngleFromOrigin(alfiePosition,enemyPosition),alfieDirection,30))){
//				System.out.println("ENEMY CLOSE SIT STILL");
//				alfieServer.sendStop();
//				return;
//			}
			movetype = 2;
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
		//double distance = (currentCommand.getAlfie().distance(currentCommand.getMiddle()));
		//alfieServer.sendForwardArcLeft((int)(5), 45);
		System.err.println("HOW DID WE GET HERE.");
	}

	/**
	 * This function is called directly before we kick ass and explode into rampant celebration 
	 * 
	 * @param currentCommand Contains absolutely no useful information at all
	 */	
	private void executeOperationStrike(OperationStrike currentCommand) {
		alfieServer.sendGoForward(MOVING_KICK_SPEED, 22);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		alfieServer.sendKick(MAX_SPEED);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This function stops Alfie and makes him wait for the next instruction
	 */
	private void executeOperationOverload(OperationOverload currentCommand) {
		alfieServer.sendStop();
	}


	@Override
	public void consumeInfo(DynamicInfo dpi) {
		//TODO act upon all commands correctly
		dynamicInfoChecker = new DynamicInfoChecker(globalInfo,dpi);
		if (currentOperation instanceof OperationReallocation) {

			if (VERBOSE) { 
				System.out.println("OperationReallocation"); 
			}

			OperationReallocation cmd = (OperationReallocation) currentOperation;

			// Get the position of the target from OperationReallocation
			Point2D targetPosition = cmd.getTarget();

			// Calculate the angle between the ball and the target (usually the ball)
			int angleToTurn = dynamicInfoChecker.getAngleToBall(targetPosition, 
					dpi.getAlfieInfo().getPosition(), 
					dpi.getAlfieInfo().getFacingDirection());
			
            oldAngle = newAngle;
			newAngle = angleToTurn;
			
			if (VERBOSE) {
				//System.out.println("Target at " + angleToTurn + " degrees");
			}

			/*
			 * If Alfi is turning and the angle is within STOP_TURNING_ERROR_THRESHOLD then we need to stop
			 * and have the FieldMarshall decide what he should do next
			 */
			switch(movetype){
			
			case(0):
				//System.out.println("why are you here!!!!");
				cmd = new OperationReallocation(cmd.getTarget(), 
						dpi.getAlfieInfo().getPosition(), 
						dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

				// Perform the Alfi magic!
				executeOperationReallocation(cmd);
			
			//turning case
			case(1):
				//System.out.println("we are turning");
				
			
				if(Math.abs(angleToTurn) <= STOP_TURNING_ERROR_THRESHOLD){
					if(VERBOSE) {
						//System.out.println("Alfie is facing the correct direction!");
					}
					//System.out.println("we are facing the right way");
					// Create new OperationReallocation command which should make Alfi move forward
					cmd = new OperationReallocation(cmd.getTarget(), 
							dpi.getAlfieInfo().getPosition(), 
							dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

					// Perform the Alfi magic!
					executeOperationReallocation(cmd);
				} else {
					if(oldAngle<newAngle){
						//System.out.println("the old angle was smaller than the new angle");
						cmd = new OperationReallocation(cmd.getTarget(), 
								dpi.getAlfieInfo().getPosition(), 
								dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

						// Perform the Alfi magic!
						executeOperationReallocation(cmd);
					}
					
				}
				
				break;
			
			// moving case
			case(2):
				//System.out.println("we are currently moving forward");
				if(Math.abs(angleToTurn) > STOP_TURNING_ERROR_THRESHOLD ){
					//System.out.println("we are currently moving but angle is out");
					cmd = new OperationReallocation(cmd.getTarget(), 
							dpi.getAlfieInfo().getPosition(), 
							dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

					// Perform the Alfie magic!
					executeOperationReallocation(cmd);
				}
				//System.out.println("Previously we would sit still here");
				executeOperationReallocation(cmd);
//				Point2D alfiePosition = dpi.getAlfieInfo().getPosition();
//				Point2D enemyPosition = dpi.getOpponentInfo().getPosition();
//				double alfieDirection = dpi.getAlfieInfo().getFacingDirection();
//				
//				if((alfiePosition.distance(enemyPosition)<50)&&(dynamicInfoChecker.isSimilarAngle(dynamicInfoChecker.getAngleFromOrigin(alfiePosition,enemyPosition),alfieDirection,30))){
//					System.out.println("ENEMY CLOSE SIT STILL");
//					alfieServer.sendStop();
//					return;
//				}
				break;
			}
				
			
			
			
			
			
			
			
			
			/*
			if (turning && Math.abs(angleToTurn) <= STOP_TURNING_ERROR_THRESHOLD) {

				// Makes Alfi stop turning
				if(VERBOSE) {
					System.out.println("Alfie is facing the correct direction!");
				}

				// Create new OperationReallocation command which should make Alfi move forward
				cmd = new OperationReallocation(cmd.getTarget(), 
						dpi.getAlfieInfo().getPosition(), 
						dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

				// Perform the Alfi magic!
				executeOperationReallocation(cmd);

			} else {

				// Alfie isn't facing the ball so we need to reposition him
				if (Math.abs(angleToTurn) > STOP_TURNING_ERROR_THRESHOLD) {

					if(VERBOSE) {
						System.out.println("in moving forward the robot is no longer facing the robot new operation");
					}

				
					cmd = new OperationReallocation(cmd.getTarget(), 
							dpi.getAlfieInfo().getPosition(), 
							dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

					// Do the magic!
					executeOperationReallocation(cmd);

				} else {
					cmd = new OperationReallocation(cmd.getTarget(), 
							dpi.getAlfieInfo().getPosition(), 
							dpi.getAlfieInfo().getFacingDirection(),dpi.getOpponentInfo().getPosition());

					
					executeOperationReallocation(cmd);
					
					System.err.println("Field Marshall should give a new command");
				}

			}
	*/

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
