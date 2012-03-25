package group2.sdp.pc.planner;

import group2.sdp.pc.breadbin.DynamicInfo;
import group2.sdp.pc.controlstation.ControlStation;
import group2.sdp.pc.globalinfo.DynamicInfoChecker;
import group2.sdp.pc.globalinfo.GlobalInfo;
import group2.sdp.pc.planner.strategy.Strategy;
import group2.sdp.pc.vision.Bakery;
import group2.sdp.pc.vision.skeleton.DynamicInfoConsumer;

/**
 *<p><b>Description</b>: "If they have no bread, let them eat cake!" The popularity of 
*               this misquotation and the subjects of its metaphor suggest that
*              the products of a bakery would be of interest to a majesty. Thus
*               the name of the class that consumes the products of the {@link Bakery} 
*               and produces decisions on what {@link Strategy} to use is Overlord. It 
*               passes the Strategy to a Strategy Consumer, supplied on 
*               construction of the Overlord. Also passes the {@link DynamicInfo} that
*               was received down to another {@link DynamicInfoConsumer}, supplied on
*               construction of the Overlord. Note that the two Consumers can 
*               be the same object, but the Overlord does not need to know.</p>
* <p><b>Main client</b>:	{@link FieldMarshal}</p>
* <p><b>Produces</b>:	{@link Strategy}</p>
* <p><b>Responsibilities</b>:</br>
*              Producing a Strategy and monitoring if it is successful or if a 
*               problem occurs.</p>
* <p><b>Policy</b>:</br>      
*  Planning:</br>   Analysing the DynamicInfo (*how*), the Overlord comes up with a Strategy.
*               After that, it checks the success of the strategy or if a 
*               problem occurred on each DynamicInfo it receives. If there is either,
*               the Overlord comes up with a new Strategy. Otherwise, just 
*               passes the DynamicInfo to its DynamicInfoConsumer.</p>
 */
public class Overlord implements DynamicInfoConsumer {
	 
	/**
	 * Indicates if the overlord is running or not.
	 */
	protected boolean running = false;
	
	
	protected DynamicInfoChecker dynamicInfoChecker;

	/**
	 * The object to which we pass the Strategy
	 */
	protected StrategyConsumer strategyConsumer;
	
	/**
	 * The object to which we pass on the DynamicInfo
	 */
	protected DynamicInfoConsumer dynamicInfoConsumer;
	
	/**
	 * The current strategy that is being executed.
	 */
	protected Strategy currentStrategy;

	/**
	 * Stopping the Overlord. Sending a STOP strategy.
	 */
	private boolean stopping;
	
	private GlobalInfo globalInfo;
	
	public Overlord(GlobalInfo globalInfo, StrategyConsumer strategyConsumer, DynamicInfoConsumer dynamicInfoConsumer) {
		this.globalInfo = globalInfo;
		this.strategyConsumer = strategyConsumer;
		this.dynamicInfoConsumer = dynamicInfoConsumer;
	}
	
	/**
	 * When this method is invoked the Overlord starts computing the strategy
	 * and poking the FieldMarshal with new DynamicPitchInfos.
	 */
	public void start() {
		running = true;
		strategyConsumer.start();
	}
	
	/**
	 * When this method is invoked the Overlord stops computing the strategy
	 * and poking the FieldMarshal with new DynamicPitchInfos.
	 */
	public void stop() {
		if (running) {
			stopping = true;
			strategyConsumer.stop();
		} else {
			ControlStation.log("Overlord is busy conquering elsewhere.");
		}
		// Running is set to false once a stop command is sent to Alfie
	}

	/**
	 * When running, computes the strategy that should be employed depending 
	 * on the current pitch status and passes the information to the 
	 * FieldMarshal.
	 */
	@Override
	public void consumeInfo(DynamicInfo dpi) {
		dynamicInfoChecker = new DynamicInfoChecker(globalInfo,dpi);
		if (running) {
			Strategy strategy = computeStrategy(dpi);
			if (strategy != currentStrategy) {
				strategyConsumer.setStrategy(strategy);
				currentStrategy = strategy;
			}
			dynamicInfoConsumer.consumeInfo(dpi);
		}
	}

	/**
	 * Most important method of the class. Computes the high-level strategy 
	 * that should be employed, depending on the current dynamic pitch 
	 * information.
	 * @param dpi The DynamicPitchInfo to use when deciding what strategy 
	 * should be employed.
	 * @return The strategy that should be currently employed.
	 */
	protected Strategy computeStrategy(DynamicInfo dpi) {
		if (stopping) {
			stopping = false;
			running = false;
			return Strategy.STOP;
		}
		
		// TODO: remove after testing
		return Strategy.TEST_PATH_FINDER;
		
//		DynamicRobotInfo alfieInfo = dpi.getAlfieInfo();
//		DynamicRobotInfo opponentInfo = dpi.getOpponentInfo();
//		DynamicBallInfo ballInfo = dpi.getBallInfo();
//		
//		Point2D ballPosition = ballInfo.getPosition();  
//		
//		if(dynamicInfoChecker.isInAttackingPosition(opponentInfo, ballPosition)
//				|| !dynamicInfoChecker.correctSide(alfieInfo,ballPosition)){
//			return Strategy.DEFENSIVE;
//		} else {
//			return Strategy.OFFENSIVE;
//		}
	}
	
	/**
	 * 1. Strategy is Offensive - successful if we score a goal;
       2. Strategy is Defensive - successful if there is no threat of the 
           other team scoring a goal; 
       3. Strategy is Take a Penalty - successful if we score a goal;
       4. Strategy is Defend a Penalty - successful if Alfie prevents the 
           opponent from scoring a penalty; and 
       5. Strategy is Stealth - successful if Alfie stops.
       TODO: implement the above
	 * @param di
	 * @return
	 */
	protected boolean strategySuccessful(DynamicInfo di) {
		return true;
	}
	
	/**
	 * 1. Strategy is Offensive - problem exists if the other robot gets the
           ball;
       2. Strategy is Defensive - problem exists if the other team scores 
           a goal; 
       3. Strategy is Take a Penalty - problem exists if Alfie misses after 
           the shot;
       4. Strategy is Defend a Penalty - problem exists if the opponent 
           scores a goal; and 
       5. Strategy is Stealth - problem exists if Alfie is being moved by 
          the other robot.
          TODO: implement the above
	 * @param di
	 * @return
	 */
	protected boolean problemExists(DynamicInfo di) {
		return true;
	}
}
