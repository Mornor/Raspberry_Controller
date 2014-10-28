package view;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import controler.Controler;

public class RaspberryControler extends JFrame 
{

	/*Have to declare it in order to use vlcj*/
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private EmbeddedMediaPlayer mediaPlayer;
	
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
	private JScrollPane scrollBar; 
	private JButton quit;
	private JButton start; 
	private JTextArea stateOfCommunication; 
	private JRadioButton webcamRadioButton; 
	private JRadioButton joystickRadioButton; 
	
	/*Declaring the network elements*/
	private ObjectOutputStream oos; 
	private ObjectInputStream ois; 
	private Socket connection; 
	private String host; 
	private boolean stopConnection; // Usefull to terminate Streams (see whileCommunicating)
	
	/*Declaring the Controler class*/
	Controler controler; 

	
	public RaspberryControler(String host){
		this.host = host;
		controler = new Controler(this);  
		initComponents();
	}
	
	/*Every network actions are done in this thread*/
	Thread startRunning = new Thread(new Runnable() {		
		@Override
		public void run() {
			try {
				controler.connectToServer();
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
	
	private void initComponents(){
		
		setBasicParameters();
		createControlPanel();
		createWebcamPanel(); 
		createMainPanel();
		setListeners();		
		
		/*Set the last parameters of the frame*/
		this.revalidate();
		this.repaint();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
	}
	
	private void setListeners(){
		
		/*Add the listeners on the Buttons*/
		webcamRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controler.sendChoice("Webcam");
				joystickRadioButton.setSelected(false);
				robotControlledBy.setText("The Webcam");
			}
		});
		
		joystickRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controler.sendChoice("Joystick");
				webcamRadioButton.setSelected(false);
				robotControlledBy.setText("The Joystick");				
			}
		});
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startRunning.start();
				start.setVisible(false);
				quit.setVisible(true);
				webcamRadioButton.setEnabled(true);
				joystickRadioButton.setEnabled(true);
			}
		});
		
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startRunning.interrupt();
				stopConnection = false; 
				quit.setEnabled(false);
				controler.sendChoice("CLOSE CONNECTION");
				webcamRadioButton.setEnabled(false);
				joystickRadioButton.setEnabled(false);
				showMessage("\nCLIENT - Communication is aborted");				
			}
		});
		
	}
	
	private void createMainPanel(){

		mainPanel = new JPanel();
		
		/*Set the Layout*/
		mainPanel.setLayout(new FlowLayout());
		
		/*Set the parameters*/
		mainPanel.add(webcamPanel); 
		mainPanel.add(controlPanel);  
		this.getContentPane().add(mainPanel);
	}
	
	private void setBasicParameters(){
		
		/*Set the Parameters of the JFrame*/
		this.setVisible(true);
		this.setTitle("Raspberry Controller");
		this.setSize(new Dimension(1016, 520));
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
		stateOfCommunication = new JTextArea(); 
		quit = new JButton("Close connection"); 
		start = new JButton("Start connection"); 
		webcamRadioButton = new JRadioButton(); 
		joystickRadioButton = new JRadioButton();
		scrollBar = new JScrollPane(stateOfCommunication);
		
		/*Set parameters of the components*/
		chooseYourWayofControling.setPreferredSize(new Dimension(430, 30));
		byWebcam.setPreferredSize(new Dimension(180, 50));
		byJoystick.setPreferredSize(new Dimension(180, 50));
		choice.setPreferredSize(new Dimension(200, 50));
		scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); 	
		scrollBar.setPreferredSize(new Dimension(240, 250));
		robotControlledBy.setPreferredSize(new Dimension(200, 20));
		robotControlledBy.setForeground(Color.RED);
		quit.setVisible(false);
		webcamRadioButton.setEnabled(false);
		joystickRadioButton.setEnabled(false);
		
		
		/*Set the layout*/
		controlPanel.setLayout(new FlowLayout()); 
		controlPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		controlPanel.setPreferredSize(new Dimension(450, 480));
		
		/*Place components inside controlPanel*/
		controlPanel.add(chooseYourWayofControling); 
		controlPanel.add(webcamRadioButton);
		controlPanel.add(byWebcam); 
		controlPanel.add(joystickRadioButton); 
		controlPanel.add(byJoystick); 
		controlPanel.add(scrollBar);  
		controlPanel.add(choice); 
		controlPanel.add(robotControlledBy); 
		controlPanel.add(start); 
		controlPanel.add(quit); 
		
	}

	private void createWebcamPanel(){  
		
		/*Get the VLC Libraries*/
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/VLC/VideoLAN/VLC");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		/*Create the canvas*/
		Canvas canvas = new Canvas(); 
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(); 
		canvas.setVisible(true);
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas); 
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer(); 
		mediaPlayer.setVideoSurface(videoSurface);
		
		/*Create components*/
		liveStream = new JLabel("Live Stream"); 
		
		/*Set parameters of the components*/
		liveStream.setPreferredSize(new Dimension(200, 30));
		liveStream.setHorizontalAlignment(SwingConstants.CENTER);
		
		/*Set the layout*/
		webcamPanel = new JPanel();
		webcamPanel.setLayout(new BorderLayout()); 
		webcamPanel.setPreferredSize(new Dimension(550, 480));
		webcamPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		/*Place the components*/
		webcamPanel.setVisible(true);
		webcamPanel.add(canvas);
		webcamPanel.add(liveStream, BorderLayout.NORTH);  
		try{
		mediaPlayer.playMedia("http://127.0.0.1:8989/movie");
		}catch(IllegalStateException e){
			System.out.println(e.toString());
		}
	}
	
	
	public void showMessage(String msg){
		stateOfCommunication.append(msg+"\n");
	}
	
	/*Getters and Setters*/
	public ObjectOutputStream getOos() {
		return oos;
	}

	public void setOos(ObjectOutputStream oos) {
		this.oos = oos;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public void setOis(ObjectInputStream ois) {
		this.ois = ois;
	}

	public Socket getConnection() {
		return connection;
	}

	public void setConnection(Socket connection) {
		this.connection = connection;
	}

	public String getHost() {
		return host;
	}

	/**
	 * @return False is th button to stop the connection has been clicked. 
	 * */
	public boolean isStopConnection() {
		return stopConnection;
	}
	
	
	
}
