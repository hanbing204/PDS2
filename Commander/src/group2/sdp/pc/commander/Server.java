package group2.sdp.pc.commander;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTConnector;

/**
 * Connects to the robot and can send commands to it.
 * Op codes:
 * 0 - stop,
 * 1 - go forward,
 * 2 - go backwards,
 * 3 - kick,
 * 4 - spin,
 * 126 - reset,
 * 127 - terminate.
 */
public class Server {
	
	private String nxtAddress = "btspp://group2";
	
	private NXTConnector conn;
	private DataOutputStream dos;
	private DataInputStream dis;
	
	/**
	 * Default constructor. Initialises the blue-tooth connection and adds a 
	 * log listener.
	 * @throws Exception 
	 */
	public Server () throws Exception {
		conn = new NXTConnector();
		
		conn.addLogListener(new NXTCommLogListener() {
			public void logEvent(String message) {
				System.out.println("BTSend Log.listener: " + message);				
			}
			public void logEvent(Throwable throwable) {
				System.out.println("BTSend Log.listener - stack trace: ");
				throwable.printStackTrace();
			}
		} 
		);

		// Connect to Alfie
		boolean connected = conn.connectTo(nxtAddress);
	
		if (!connected) {
			System.err.println("Failed to connect to Alfie");
			throw new Exception();
		}
		
		dos = conn.getDataOut();
		dis = conn.getDataIn();
	}
	
	/**
	 * Called when the object is garbage-collected. Closes the connections.
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			dis.close();
			dos.close();
			conn.close();
		} catch (IOException ioe) {
			System.out.println("IOException closing connection:");
			System.out.println(ioe.getMessage());
		}
		super.finalize();
	}

	/**
	 * Converts an integer to four bytes.
	 * @param arg The integer to convert.
	 * @return A byte array consisting of four bytes.
	 */
	public byte[] intToByte4(int arg) {
		byte [] result = new byte [4];
		result[0] = (byte)(arg >> 24);
		result[1] = (byte)((arg >> 16) & 255);
		result[2] = (byte)((arg >> 8) & 255);
		result[3] = (byte)(arg & 255);
		return result;
	}
	
	/**
	 * Tells Alfie to stop moving.
	 */
	public void sendStop() {
		byte op = 0;
		byte [] b = {op, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Tells Alfie to start moving forward. 
	 * @param speed The speed for the command.
	 */
	public void sendGoForward(int speed) {
		byte op = 1;
		byte [] speed_b = intToByte4(speed);
		byte [] b = {op, 0, 0, 0, speed_b[0], speed_b[1], speed_b[2], speed_b[3], 0, 0, 0, 0, 0, 0, 0, 0,
					 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Tells Alfie to start moving backwards. 
	 * @param speed The speed for the command.
	 */
	public void sendGoBackwards(int speed) {
		byte op = 2;
		byte [] speed_b = intToByte4(speed);
		byte [] b = {op, 0, 0, 0, speed_b[0], speed_b[1], speed_b[2], speed_b[3], 0, 0, 0, 0, 0, 0, 0, 0,
					 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Tells Alfie to become aggressive.
	 * @param power The power for the kick.
	 */
	public void sendKick(int power) {
		byte op = 3;
		byte [] power_b = intToByte4(power);
		byte [] b = {op, 0, 0, 0, power_b[0], power_b[1], power_b[2], power_b[3], 0, 0, 0, 0, 0, 0, 0, 0,
					 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Tells Alfie to spin on the spot.
	 * @param speed The speed for the spin.
	 * @param angle The angle for the spin.
	 */
	public void sendSpin(int speed, int angle) {
		byte op = 4;
		byte [] speed_b = intToByte4(speed);
		byte [] angle_b = intToByte4(angle);
		byte [] b = {op, 0, 0, 0, speed_b[0], speed_b[1], speed_b[2], speed_b[3], angle_b[0], angle_b[1], angle_b[2], angle_b[3], 0, 0, 0, 0,
					 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Tells Alfie to reset communication.
	 */
	public void sendReset() {
		byte op = 126;
		byte [] b = {op, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Tells the Alfie to go to sleep.
	 */
	public void sendExit() {
		byte op = 127;
		byte [] b = {op, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		sendBytes(b, true);
	}
	
	/**
	 * Sends the given bytes across the opened connection and checks the 
	 * response. 
	 * @param b The bytes to send.
	 * @param verbose If true, the bytes are printed before being sent.
	 */
	private void sendBytes(byte [] b, boolean verbose) {
		//long start = System.currentTimeMillis();
		
		boolean success = false;
		do {
			try {
				// Print output if requested
				if (verbose) {
					System.out.print("Sending bytes:");
					for (int i = 0; i < b.length; ++i) {
						System.out.print(" " + b[i]);
					}
					System.out.println();
				}
	
				// Send bytes
				dos.write(b, 0, b.length);
				dos.flush();
			} catch (IOException ioe) {
				System.out.println("IO Exception writing bytes:");
				System.out.println(ioe.getMessage());
				break;
			}
			
			try {
				// On success Alfie should repeat the command back.
				byte [] b2 = new byte [b.length];
				dis.read(b2, 0, b.length);
				success = true;
				for (int i = 0; i < b.length; ++i) {
					if (b[i] != b2[i]) {
						System.out.println("WARNING: command is not the same; RESENDING...");
						success = false;
						break;
					}
				}
			} catch (IOException ioe) {
				System.out.println("IO Exception reading bytes:");
				System.out.println(ioe.getMessage());
				break;
			}
		} while (!success) ;
		
		//System.out.println(System.currentTimeMillis() - start);
	}
}
