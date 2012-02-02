package group2.sdp.pc.commander;

import group2.sdp.pc.vision.SimpleViewer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;

/**
 * The main GUI program to control Alfie during match or testing.
 */
public class CommanderControlStation implements KeyListener {

	// GUI elements
	private JFrame frmAlfieCommandCentre;
	private JTextPane txtLog;
	private JEditorPane Alfie_Speed;
	private JEditorPane Alfie_Angle;

	private JLabel Speed;
	private JLabel Angle;

	private JLabel Info;
	private JLabel Info2;
	
	private JButton startVisionButton;

	/**
	 * The server that sends commands to Alfie.
	 */
	private Server alfieServer;

	/**
	 *  Number of connection attempts before giving up.
	 */
	private static final int CONNECTION_ATTEMPTS = 5;
	/**
	 * Timeout between retries of connecting to Alfie.
	 */
	private static final int RETRY_TIMEOUT = 3000;

	// This is actually used in a child thread, but eclipse is being silly.
	@SuppressWarnings("unused")
	private int key_pressed = 0;
	/**
	 * True if the main window was closed.
	 */
	private boolean exiting = false;
	
	/**
	 * The threads for starting and stopping the communication to Alfie.
	 */
	private Thread init_thread, cleanup_thread;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CommanderControlStation window = new CommanderControlStation();
					window.frmAlfieCommandCentre.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CommanderControlStation() {
		initializeConnectionThreads();
		initializeFrame();
	}

	/**
	 * Initialise the threads for connecting to and disconnecting from Alfie.
	 */
	private void initializeConnectionThreads() {
		init_thread = new Thread() {		
			public void run() {
				for(int i = 1; i <= CONNECTION_ATTEMPTS && !exiting; ++i) {	
					log("Connection attempt: " + i);
					
					try {
						alfieServer = new Server();
						log("Connected to Alfie");
						break;
					} catch(Exception e) {
						log("Failed to connect... Retrying in " + (RETRY_TIMEOUT / 1000) + " seconds");
						try {
							Thread.sleep(RETRY_TIMEOUT);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		};
		
		cleanup_thread = new Thread() {		
			public void run() {
				alfieServer.sendReset();
			}
		};
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeFrame() {
		frmAlfieCommandCentre = new JFrame();
		frmAlfieCommandCentre.setTitle("Alfie Command Centre");
		frmAlfieCommandCentre.setBounds(100, 100, 500, 440);
		frmAlfieCommandCentre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAlfieCommandCentre.getContentPane().setLayout(null);

		txtLog = new JTextPane();
		txtLog.setEditable(false);
		txtLog.setBounds(12, 12, 472, 305);
		frmAlfieCommandCentre.getContentPane().add(txtLog);
		txtLog.addKeyListener(this);
		Info = new JLabel();
		Info2 = new JLabel();
		Info.setBounds(12, 329, 500, 25);
		Info2.setBounds(12, 345, 500, 25);
		Info.setText("UP: forward; DOWN: backward; LEFT: trun left; RIGHT: trun right");

		Info2.setText("1: +speed; 2: -speed; 3: +angle; 4: -angle");

		frmAlfieCommandCentre.getContentPane().add(Info);
		frmAlfieCommandCentre.getContentPane().add(Info2);

		Alfie_Speed = new JEditorPane();
		Alfie_Speed.setBounds(180, 373, 40, 25);
		Alfie_Speed.setText("0");

		Alfie_Angle = new JEditorPane();
		Alfie_Angle.setBounds(60, 373, 40, 25);
		Alfie_Angle.setText("0");

		Speed = new JLabel();
		Speed.setText("Speed");
		Speed.setBounds(120, 373, 60, 25);

		Angle = new JLabel();
		Angle.setText("Angle");
		Angle.setBounds(10, 373, 40, 25);

		startVisionButton = new JButton();
		startVisionButton.setText("Start Vision");
		startVisionButton.setBounds(240, 373, 160, 25);
		startVisionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new SimpleViewer();
				startVisionButton.setEnabled(false);
			}
		});
		
		frmAlfieCommandCentre.getContentPane().add(Alfie_Angle);
		frmAlfieCommandCentre.getContentPane().add(Alfie_Speed);
		frmAlfieCommandCentre.getContentPane().add(Angle);
		frmAlfieCommandCentre.getContentPane().add(Speed);
		frmAlfieCommandCentre.getContentPane().add(startVisionButton);
		frmAlfieCommandCentre.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmAlfieCommandCentre.setVisible(true);
		frmAlfieCommandCentre.addWindowListener(new WindowListener() {
			public void windowClosed(WindowEvent arg0) {
			}

			public void windowActivated(WindowEvent arg0) {
				
			}

			public void windowClosing(WindowEvent arg0) {
				exiting = true;
				cleanup_thread.start();
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
				init_thread.start();
			}
		});

	}
	
	/**
	 * Adds a string and a new line to the log text box.
	 * @param logString The string to add.
	 */
	private void log(String logString) {
		txtLog.setText(txtLog.getText() + logString + "\n");
		txtLog.repaint();
	}

	@Override
	public void keyPressed(KeyEvent key) {
		
		switch (key.getKeyCode()) {

		// when pressed "up" key the robot move forward
		case KeyEvent.VK_UP:

			alfieServer.sendGoForward(Integer.parseInt(Alfie_Speed.getText()), Integer.parseInt(Alfie_Angle.getText()));
			key_pressed = KeyEvent.VK_UP;
			break;

		// when pressed "left" key the robot turn left
		case KeyEvent.VK_LEFT:
			
			alfieServer.sendSpinLeft(Integer.parseInt(Alfie_Speed.getText()), Integer.parseInt(Alfie_Angle.getText()));
			key_pressed = KeyEvent.VK_LEFT;
			break;

		// when pressed "right" key the robot turn right
		case KeyEvent.VK_RIGHT:

			alfieServer.sendSpinRight(Integer.parseInt(Alfie_Speed.getText()), Integer.parseInt(Alfie_Angle.getText()));
			key_pressed = KeyEvent.VK_RIGHT;
			break;

		// when pressed "up" key the robot move backward
		case KeyEvent.VK_DOWN:

			alfieServer.sendGoBackwards(Integer.parseInt(Alfie_Speed.getText()), Integer.parseInt(Alfie_Angle.getText()));
			key_pressed = KeyEvent.VK_DOWN;
			break;
		}

	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent key) {
		System.out.println(key.getKeyChar());
		if (key.getKeyChar() == 's') {
			alfieServer.sendKick(1000);

		}
		if (key.getKeyChar() == 'd') {
			alfieServer.sendStop();
			System.out.println("ok");
		}

		if (key.getKeyChar() == '1') {
			Alfie_Speed.setText(Integer.parseInt(Alfie_Speed.getText()) + 1
					+ "");
		}
		if (key.getKeyChar() == '2') {
			Alfie_Speed.setText(Integer.parseInt(Alfie_Speed.getText()) - 1
					+ "");
		}

		if (key.getKeyChar() == '3') {
			Alfie_Angle.setText(Integer.parseInt(Alfie_Angle.getText()) + 1
					+ "");
		}
		if (key.getKeyChar() == '4') {
			Alfie_Angle.setText(Integer.parseInt(Alfie_Angle.getText()) - 1
					+ "");
		}
		if (key.getKeyChar() == ',') {
			alfieServer.sendSpinLeft(Integer.parseInt(Alfie_Speed.getText()), Integer.parseInt(Alfie_Angle.getText()));
		}
		if (key.getKeyChar() == '.') {
			alfieServer.sendSpinRight(Integer.parseInt(Alfie_Speed.getText()), Integer.parseInt(Alfie_Angle.getText()));
			
		}

	}
}
