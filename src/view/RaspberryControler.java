package view;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import controler.Communicator;
import controler.Controler;

public class RaspberryControler extends JFrame implements SerialPortEventListener
{

	/*Have to declare it in order to use vlcj*/
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
		
	/*Declaring the basics components*/
	private JPanel mainPanel; 
	private JPanel webcamPanel; 
	private JPanel controlPanel;
	private JLabel chooseYourWayofControling; 
	private JLabel byWebcam; 
	private JLabel byJoystick;
	private JLabel choice; 
	private JLabel robotControlledBy; 
	private JLabel liveStream; 
	private JLabel ipAddressLabel; 
	private JScrollPane scrollBar; 
	private JButton startWebcam; 
	private JButton quit;
	private JButton start; 
	private JTextArea stateOfCommunication; 
	private JRadioButton webcamRadioButton; 
	private JRadioButton joystickRadioButton; 
	private JTextField ipAdress; 
	private JCheckBox fineTuning; 
	private boolean isFineTuning; 
	private boolean isSerialConnected; 
	private boolean raisedFlag = false; 
	
	/*Declaring the network elements*/
	private BufferedReader bufferedReader; // in 
	private PrintWriter printWriter; // out
	
	private Socket connection; 
	private String host; 
	private boolean stopConnection; // Usefull to terminate Streams (see whileCommunicating)
	
	/*Declaring the Controler class*/
	Controler controler; 
	Communicator comm; 	
	
	public RaspberryControler(){
		controler = new Controler(this);  
		comm = new Communicator(this); 
		initComponents();
	}
	
	/*Every network actions are done in this thread*/
	Thread startRunning = new Thread(new Runnable() {		
		@Override
		public void run() {
			try {
				controler.connectToServer(45678);
				controler.setStreams();
				controler.whileCommunicating();
			} catch (EOFException e) {
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			} finally{
				controler.closeConnection(); 
			}			
		}
	});
	
	private void startLiveFeed(){
		try {
			Socket connection = new Socket(InetAddress.getByName("192.168.1.91"), 45679);
			PrintWriter p = new PrintWriter(connection.getOutputStream(), true); 
			p.println("START");
			System.out.println("After");
			p.close();
			connection.close();
			System.out.println("After close connection");
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
	}
	
	private void initComponents(){
		
		setBasicParameters();
		createControlPanel(); 
		//createWebcamPanel(); 
		createMainPanel();
		setListeners();		
		
		/*Set the last parameters of the frame*/
		this.setVisible(true);
		this.revalidate();
		this.repaint();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		//controler.playVideo(mediaPlayerComponent);
	}
	
	
	private void setListeners(){
		
		/*Add the listeners on the Buttons*/
		webcamRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(isSerialConnected)
					comm.disconnect();
				
				controler.sendChoice("ms:t");
				joystickRadioButton.setSelected(false);
				fineTuning.setEnabled(false);
				robotControlledBy.setText("Webcam");
			}
		});
		
		joystickRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			
				controler.sendChoice("ms:j");
				
				comm.connect(comm.searchForPorts());
				if (comm.getConnected() == true) {
					if (comm.initIOStream() == true) {
						comm.initListener();
					}
				}
				
				comm.writeData("ms:j\n");
				webcamRadioButton.setSelected(false);
				fineTuning.setEnabled(true);
				isSerialConnected = true; 
				robotControlledBy.setText("The Joystick");				
			}
		});
		
		fineTuning.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fineTuning.isSelected())
					comm.writeData("ms:j\n");
				else 
					comm.writeData("ms:ja\n");
			}
		});
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(checkIfIp()){
					setHost(); 
					startRunning.start();
					start.setVisible(false);
					quit.setVisible(true);
					webcamRadioButton.setEnabled(true);
					joystickRadioButton.setEnabled(true);
				}				
			}
		});
		
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startRunning.interrupt();
				stopConnection = false; 
				quit.setEnabled(false);
				controler.sendChoice("EXIT");
				webcamRadioButton.setEnabled(false);
				joystickRadioButton.setEnabled(false);
				showMessage("\nCLIENT - Communication is aborted");				
			}
		});
		
		startWebcam.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					startLiveFeed();
			}
		});
		
	}
	
	private void setHost(){
		this.host = ipAdress.getText(); 
	}
	
	/*Return true if there is something in the IP field*/
	// TODO !
	private boolean checkIfIp(){
		return true; 
	}
	
	private void createMainPanel(){

		mainPanel = new JPanel();
		
		/*Set the Layout*/
		mainPanel.setLayout(new FlowLayout());
		//mainPanel.add(webcamPanel); 
		mainPanel.add(controlPanel); 
		
		/*Set the parameters*/ 
		this.getContentPane().add(mainPanel);
	}
	
	private void setBasicParameters(){
		
		/*Set the Parameters of the JFrame*/
		this.setTitle("Raspberry Controller");
		this.setSize(new Dimension(470, 540));
		this.setResizable(false);
	}
	
	private void createControlPanel(){
		
		/*Create the components*/
		controlPanel = new JPanel(); 
		chooseYourWayofControling = new JLabel("How would you like to control the robot ?"); 
		byWebcam = new JLabel("Using the webcam"); 
		byJoystick = new JLabel("Using the Joystick"); 
		choice = new JLabel("Robot is controlled by : "); 
		robotControlledBy = new JLabel("Nothing at the moment");
		ipAddressLabel = new JLabel("IP Adress of the server: "); 
		ipAdress = new JTextField(); 
		stateOfCommunication = new JTextArea();
		stateOfCommunication.setEditable(false);
		quit = new JButton("Close connection"); 
		start = new JButton("Start connection");
		startWebcam = new JButton("Start webcam"); 
		webcamRadioButton = new JRadioButton(); 
		joystickRadioButton = new JRadioButton();
		scrollBar = new JScrollPane(stateOfCommunication);
		fineTuning = new JCheckBox("Fine tuning"); 
		isSerialConnected = false; 
		
		/*Set parameters of the components*/
		chooseYourWayofControling.setPreferredSize(new Dimension(430, 30));
		byWebcam.setPreferredSize(new Dimension(180, 30));
		byJoystick.setPreferredSize(new Dimension(180, 30));
		choice.setPreferredSize(new Dimension(200, 50));
		scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 	
		scrollBar.setPreferredSize(new Dimension(240, 250));
		ipAdress.setText("192.168.1.91");
		isFineTuning = false; 
		fineTuning.setSelected(true);
		
		ipAdress.setPreferredSize(new Dimension(140, 25));
		ipAddressLabel.setPreferredSize(new Dimension(150, 20)); 
		start.setPreferredSize(new Dimension(200, 25));
		quit.setPreferredSize(new Dimension(200, 25));
		robotControlledBy.setPreferredSize(new Dimension(200, 20));
		robotControlledBy.setForeground(Color.RED);
		quit.setVisible(false);
		fineTuning.setPreferredSize(new Dimension(350, 20));
		fineTuning.setEnabled(false);
		startWebcam.setPreferredSize(new Dimension(240, 25));
		webcamRadioButton.setEnabled(false);
		joystickRadioButton.setEnabled(false); 
		
		/*Set the layout*/
		controlPanel.setLayout(new FlowLayout()); 
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		controlPanel.setPreferredSize(new Dimension(450, 503));
		
		/*Place components inside controlPanel*/
		controlPanel.add(chooseYourWayofControling); 
		controlPanel.add(joystickRadioButton); 
		controlPanel.add(byJoystick);
		controlPanel.add(webcamRadioButton);
		controlPanel.add(byWebcam); 
		controlPanel.add(fineTuning);
		controlPanel.add(startWebcam); 
		controlPanel.add(scrollBar);  
		controlPanel.add(choice); 
		controlPanel.add(robotControlledBy); 
		controlPanel.add(ipAddressLabel); 
		controlPanel.add(ipAdress); 
		controlPanel.add(start); 
		controlPanel.add(quit); 
		controlPanel.add(startWebcam);
		
	}

	private void createWebcamPanel(){  
		
		/*Get the VLC Libraries*/
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/VLC/VideoLAN/VLC");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		/*Create components*/
		liveStream = new JLabel("Live Stream"); 
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent(); 
		
		/*Set parameters of the components*/
		liveStream.setPreferredSize(new Dimension(200, 30));
		liveStream.setHorizontalAlignment(SwingConstants.CENTER);
		
		/*Set the layout*/
		webcamPanel = new JPanel();
		webcamPanel.setLayout(new BorderLayout()); 
		webcamPanel.setPreferredSize(new Dimension(550, 480));
		webcamPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		/*Place the components*/
		webcamPanel.add(liveStream, BorderLayout.NORTH);	    
		webcamPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
	}	
	
	public void showMessage(String msg){
		stateOfCommunication.append(msg+"\n");
	}
	
	/*Getters and Setters*/
	public Socket getConnection() {
		return connection;
	}

	public void setConnection(Socket connection) {
		this.connection = connection;
	}

	public String getHost() {
		return host;
	}

	public BufferedReader getBufferedReader() {
		return bufferedReader;
	}


	public void setBufferedReader(BufferedReader br) {
		this.bufferedReader = br;
	}
	

	public PrintWriter getPrintWriter() {
		return printWriter;
	}


	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}


	/**
	 * @return False is the button to stop the connection has been clicked. 
	 * */
	public boolean isStopConnection() {
		return stopConnection;
	}
	
	public Controler getControler() {
		return controler;
	}
	
	public boolean isFineTuning() {
		return isFineTuning;
	}


	@Override
	public void serialEvent(SerialPortEvent evt) {
		comm.serialEvent(evt);
	}


	
	
	
}
