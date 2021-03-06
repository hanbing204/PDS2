package group2.simulator.core;

import group2.sdp.pc.breadbin.StaticInfo;
import group2.sdp.pc.vision.skeleton.StaticInfoConsumer;

public class SimulatorDoughProvider {

	private StaticInfoConsumer consumer;
	private Thread workerThread;
	
	public SimulatorDoughProvider(StaticInfoConsumer consumer) {
		this.consumer = consumer;
		initWorkerThread();
	}
	
	private void initWorkerThread() {
		workerThread = new Thread() {
			public void run() {
				while (true) {
					// wait for clock-tick
					process();
				}
			}
		};
	}

	private void process() {
		StaticInfo spi = generatePitchInfo();
		consumer.consumeInfo(spi);
	}

	private StaticInfo generatePitchInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
