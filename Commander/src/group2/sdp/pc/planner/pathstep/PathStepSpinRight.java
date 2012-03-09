package group2.sdp.pc.planner.pathstep;


/**
 * 
 * 
 * @author Shaun A.K.A the bringer of bad code!!! beware but chris was here so its kay :)
 *
 * Act: Tell Alfie to start spinning clock-wise, possibly specifying the angle to be
 * covered.
 * 
 * Parameters:
 * An angle for the turn, threshold delta angle for success.
 * 
 */
public class PathStepSpinRight implements PathStep {

	@Override
	public Type getType() {
		return Type.SPIN_RIGHT;
	}

	
	/**
	 * Succeed:
	 * If Alfie is within the specified threshold delta from the specified angle.
	 */
	@Override
	public boolean isSuccessful() {
		// TODO Auto-generated method stub 
		// logic yet to be added
		return false;
	}

	/**
	 *
	 * Fail:
	 * If Alfie is turning away from the destination angle.
	 */
	@Override
	public boolean problemExists() {
		// TODO Auto-generated method stub
		//Coming soon Logic!
		return false;
	}

	
}

