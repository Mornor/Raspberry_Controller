package controler; 

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import view.RaspberryControler;

public class Communicator implements SerialPortEventListener
{    
    // Data received from the serial Port (so, from the Joystick)
    String joystickPositionString; 

	//for containing the ports that will be found
    private Enumeration ports = null;
    
    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;

    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";

	CommPortIdentifier curPort; 
    RaspberryControler rspb;
    
    public Communicator(RaspberryControler r){
    	this.rspb = r; 
    }

    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    public CommPortIdentifier searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
            	if(curPort.getName().equals("COM10"))
            		return curPort; 
            }
        } 
        return curPort; 
    }

    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public void connect(CommPortIdentifier selectedPort)
    {
        selectedPortIdentifier = this.curPort; 

        CommPort commPort = null;

        try
        {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("AppName", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;

            setConnected(true);
            
            //logging
            logText = selectedPort.getName() + " opened successfully.";
            System.out.println(logText);
        }
        catch (PortInUseException e)
        {
            logText = selectedPort.getName() + " is in use. (" + e.toString() + ")";
            System.out.println(logText);
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            System.out.println(logText);
        }
    }

    //open the input and output streams
    //pre: an open port
    //post: initialized intput and output streams for use to communicate data
    public boolean initIOStream()
    {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            return successful;
        }
    }

    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
            logText = "Too many listeners. (" + e.toString() + ")";
            System.out.println(logText);
        }
    }

    //disconnect the serial port
    //pre: an open serial port
    //post: clsoed serial port
    public void disconnect()
    {
        //close the serial port
        try
        {
            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);

            logText = "Disconnected.";
            System.out.println(logText);
        }
        catch (Exception e)
        {
            logText = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            System.out.println(logText);
        }
    }

    final public boolean getConnected()
    {
        return bConnected;
    }

    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
    	
    	String msgRcvd = ""; 
    	
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {    
            	boolean continue1 = true; 
            	while(continue1){
            		char test = (char) input.read();
            		if(test != '\n')
            			msgRcvd += test; 
            		else
            			continue1 = false; 
            	}
            	
            	rspb.getControler().sendChoice(msgRcvd);
                System.out.println(msgRcvd);
            }
            catch (Exception e)
            {
                logText = "Failed to read data. (" + e.toString() + ")";
                System.out.println(logText);
            }
        }
    }
    
    public String getLogText() {
		return logText;
	}

	public void setLogText(String logText) {
		this.logText = logText;
	}
	

    public String getJoystickPositionString() {
		return joystickPositionString;
	}

	public void setJoystickPositionString(String joystickPositionString) {
		this.joystickPositionString = joystickPositionString;
	}


}
