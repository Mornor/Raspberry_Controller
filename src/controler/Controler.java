package controler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import view.RaspberryControler;

public class Controler {
	
	RaspberryControler rspbCtl;  
	
	public Controler(RaspberryControler ctl){
		this.rspbCtl = ctl;
	}
	
	public void connectToServer() throws IOException{
		showMessage("Attempting to connect ...");
		rspbCtl.setConnection(new Socket(InetAddress.getByName(rspbCtl.getHost()), 2014));
		showMessage("Connected to : "+rspbCtl.getConnection().getInetAddress().getHostName());
	}
	
	public void setStreams() throws IOException{
		rspbCtl.setOos(new ObjectOutputStream(rspbCtl.getConnection().getOutputStream())); 
		rspbCtl.getOos().flush();
		rspbCtl.setOis(new ObjectInputStream(rspbCtl.getConnection().getInputStream())); 
		showMessage("Streams are sets");
	}
	
	public void whileCommunicating() throws IOException{
		String msg; 
		do{
			try{
				msg = (String) rspbCtl.getOis().readObject(); 
				showMessage(msg);
			}catch(ClassNotFoundException e){
				showMessage("Incorrect data");
			}
		}while(!rspbCtl.isStopConnection()); 
	}
	
	public void showMessage(String msg){			
		rspbCtl.showMessage(msg);
	}
	
	public void closeConnection(){
		try {
			rspbCtl.getOis().close();
			rspbCtl.getOos().close(); 
			rspbCtl.getConnection().close();
			System.out.println("CLOSED CLOSED");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*Send the choice made on the GUI (via RadioButton) to the server*/
	public void sendChoice(String msg){
		try {
			rspbCtl.getOos().writeObject(msg);
			rspbCtl.getOos().flush();
			if(!msg.equals("CLOSE CONNECTION"))
				showMessage("CLIENT - Sent : "+msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
