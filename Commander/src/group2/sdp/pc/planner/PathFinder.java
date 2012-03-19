package group2.sdp.pc.planner;

import group2.sdp.pc.breadbin.DynamicInfo;
import group2.sdp.pc.globalinfo.DynamicInfoChecker;
import group2.sdp.pc.globalinfo.GlobalInfo;
import group2.sdp.pc.mouth.MouthInterface;
import group2.sdp.pc.planner.operation.Operation;
import group2.sdp.pc.planner.operation.OperationReallocation;
import group2.sdp.pc.planner.pathstep.PathStep;
import group2.sdp.pc.planner.pathstep.PathStepArcBackwardsLeft;
import group2.sdp.pc.planner.pathstep.PathStepArcBackwardsRight;
import group2.sdp.pc.planner.pathstep.PathStepArcForwardsLeft;
import group2.sdp.pc.planner.pathstep.PathStepArcForwardsRight;
import group2.sdp.pc.planner.pathstep.PathStepGoBackwards;
import group2.sdp.pc.planner.pathstep.PathStepGoForwards;
import group2.sdp.pc.planner.pathstep.PathStepKick;
import group2.sdp.pc.planner.pathstep.PathStepSpinLeft;
import group2.sdp.pc.planner.pathstep.PathStepSpinRight;
import group2.sdp.pc.planner.strategy.Strategy;
import group2.sdp.pc.vision.skeleton.DynamicInfoConsumer;

import java.util.LinkedList;

/**
 * The PathFinder decides how to execute a particular operation.
 * 
 * It does this by checking the operation to be executed and creating a queue of pathStepList. It then
 * proceeds to execute them in order and checking whether they are success or have failed in every
 * consumeInfo() cycle
 */
public class PathFinder implements DynamicInfoConsumer, OperationConsumer{
	
	private static final boolean verbose = true;
	
	/**
	 * This is a queue which stores a list of paths 
	 */
	private LinkedList<PathStep> pathStepList = new LinkedList<PathStep>();
	private PathStep currentStep = null;
	private Operation currentOperation;
	
	@SuppressWarnings("unused")
	private GlobalInfo globalInfo;
	private MouthInterface mouth;
	
	private boolean replan;
	
	/**
	 * Create the path finder object and setup the GlobalInfo and Mouth
	 * 
	 * @param Persistant global pitch info
	 * @param the mouth used for communicating with Alfie or the simulator
	 */
	public PathFinder(GlobalInfo globalInfo, MouthInterface mouth) {
		this.globalInfo = globalInfo;
		this.mouth = mouth;
	}
	
	@Override
	public void consumeInfo(DynamicInfo dpi) {
		if (replan || currentStep == null) {
			plan(dpi);
			executeNextStep(dpi);
			replan = false;
		} else {
			if (currentStep.isSuccessful(dpi)) {
				if (verbose) {
					System.out.println("Successful");
				}
				executeNextStep(dpi);
			} else if (currentStep.problemExists(dpi)) {
				if (verbose) {
					System.out.println("Fail");
				}
				plan(dpi);
			}
		}
	}
	
	public void executeNextStep(DynamicInfo dpi) {	
		// Get the next PathStep from the queue
		currentStep = pathStepList.pollFirst();
		
		// If it is successful, try and execute the next PathStep in the queue
		if(currentStep != null) {
			execute();
		} else {
			// The queue is empty so re-plan
			plan(dpi);
		}		
	}
	
	/**
	 * This method is called by the FieldMarshal every time the operation changes. If the operation
	 * changes something drastic and eventful has happened on the pitch and we should re-plan
	 * @param newOperation The operation we should now execute
	 */
	public void setOperation(Operation newOperation) {
		this.currentOperation = newOperation;
		replan = true;
	}
	
	/**
	 * This method works out all the logic of the pathFinder and adds each step the queue which will
	 * them be executed in turn.
	 * 
	 * Every time this method is called the pathStepList should be cleared and re-planning should take
	 * place from scratch
	 * @param dpi 
	 */

	private void plan(DynamicInfo dpi) {
		
		System.out.println("Looking for a path...");
		
		// Clear the PathStep queue
		pathStepList.clear();
		
		// Plan based on the current operation type
		switch(currentOperation.getType()) {
			
		case REALLOCATION:
			planReallocation(dpi);
			break;
		
		case STRIKE:
			planStrike();
			break;			
			
		case CHARGE:
			planCharge();
			break;	
		
		case OVERLOAD:
			planOverload();
			break;				
		}
	}
	
	/**
	 * Create the PathSteps for an OperationReallocation
	 * 
	 * This method returns void but it should populate the pathStepList with pathSteps
	 */
	private void planReallocation(DynamicInfo dpi) {
		OperationReallocation op = (OperationReallocation) currentOperation;
		
//		PathStepArcForwardsRight firstArc = 
//			new PathStepArcForwardsRight(
//					dpi.getAlfieInfo().getPosition(), 
//					dpi.getAlfieInfo().getFacingDirection(), 
//					30, 
//					180, 
//					10
//			);
		PathStepKick firstArc = new PathStepKick(1000);
//		PathStepArcForwardsLeft secondArc = 
//			new PathStepArcForwardsLeft(
//					firstArc.getTargetDestination(), 
//					firstArc.getTargetOrientation(),
//					30, 
//					180, 
//					10
//			);
		pathStepList.add(firstArc);
//		pathStepList.add(secondArc);
	}	
	
	/**
	 * Create the PathSteps for an OperationStrike
	 * 
	 * This method returns void but it should populate the pathStepList with pathSteps
	 */
	private void planStrike() {
		/**
		 * TODO: Make magic happen
		 */	
	}

	/**
	 * Create the PathSteps for an OperationCharge
	 * 
	 * This method returns void but it should populate the pathStepList with pathSteps
	 */
	private void planCharge() {
		/**
		 * TODO: Make magic happen
		 */	
	}
	
	/**
	 * Create the PathSteps for an OperationOverload
	 * 
	 * This method returns void but it should populate the pathStepList with pathSteps
	 */	
	private void planOverload() {
		/**
		 * TODO: Make magic happen
		 */
	}

	/**
	 * Creates a candy packet based on the current operation and send it to MouthInstance thats been
	 * passed in
	 */
	private void execute() {
		
		switch(currentStep.getType()) {
	
		case GO_FORWARDS:
			PathStepGoForwards goForwards = (PathStepGoForwards) currentStep;
			mouth.sendGoForward(goForwards.getSpeed(), goForwards.getDistance());
			break;
			
		case GO_BACKWARDS:
			PathStepGoBackwards goBackwards = (PathStepGoBackwards) currentStep;
			mouth.sendGoBackwards(goBackwards.getSpeed(), goBackwards.getDistance());
			break;
			
		case SPIN_LEFT:
			PathStepSpinLeft spinLeft = (PathStepSpinLeft) currentStep;
			mouth.sendSpinLeft(spinLeft.getSpeed(), spinLeft.getAngle());
			break;
			
		case SPIN_RIGHT:
			PathStepSpinRight spinRight = (PathStepSpinRight) currentStep;
			mouth.sendSpinRight(spinRight.getSpeed(), spinRight.getAngle());
			break;
			
		case ARC_FORWARDS_LEFT:
			PathStepArcForwardsLeft arcForwardsLeft = (PathStepArcForwardsLeft) currentStep;
			mouth.sendForwardArcLeft(arcForwardsLeft.getRadius(), arcForwardsLeft.getAngle());
			break;
			
		case ARC_FORWARDS_RIGHT:
			PathStepArcForwardsRight arcForwardsRight = (PathStepArcForwardsRight) currentStep;
			mouth.sendForwardArcRight(arcForwardsRight.getRadius(), arcForwardsRight.getAngle());
			break;
			
		case ARC_BACKWARDS_LEFT:
			PathStepArcBackwardsLeft arcBackwardsLeft = (PathStepArcBackwardsLeft) currentStep;
			mouth.sendBackwardsArcLeft(arcBackwardsLeft.getRadius(), arcBackwardsLeft.getAngle());
			break;
			
		case ARC_BACKWARDS_RIGHT:
			PathStepArcBackwardsRight arcBackwardsRight = (PathStepArcBackwardsRight) currentStep;
			mouth.sendBackwardsArcRight(arcBackwardsRight.getRadius(), arcBackwardsRight.getAngle());
			break;
			
		case KICK:
			PathStepKick kick = (PathStepKick) currentStep;
			mouth.sendKick(kick.getPower());
			break;
			
		case STOP:
			mouth.sendStop();
			break;
		}
	}

	
	@Override
	public void consumeOperation(Operation operation) {
		setOperation(operation);
	}
}
