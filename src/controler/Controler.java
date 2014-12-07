package controler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import view.RaspberryControler;

public class Controler {
	
	RaspberryControler rspbCtl;  
	boolean isConnected = false; 
	
	
	public Controler(RaspberryControler ctl){
		this.rspbCtl = ctl;
	}
	
	
	public void connectToServer(int port) throws IOException{
		showMessage("Attempting to connect ...");
		rspbCtl.setConnection(new Socket(InetAddress.getByName(rspbCtl.getHost()), port));
		showMessage("Connected to : "+rspbCtl.getConnection().getInetAddress().getHostName());
		isConnected = true;
	}
	
	public void setStreams() throws IOException{
		rspbCtl.setPrintWriter(new PrintWriter(rspbCtl.getConnection().getOutputStream(), true));
		rspbCtl.setBufferedReader(new BufferedReader(new InputStreamReader(rspbCtl.getConnection().getInputStream())));
		showMessage("Streams are sets");
	}
	
	public void whileCommunicating() throws IOException{
		String msg; 
		do{
			try{
				msg = (String) rspbCtl.getBufferedReader().readLine();
				showMessage(msg);
			}catch(IOException e){
				showMessage("Incorrect data");
			}
		}while(isConnected); 
	}
	
	public void showMessage(String msg){			
		rspbCtl.showMessage(msg);
	}
	
	public void closeConnection(){
		try {
			rspbCtl.getBufferedReader().close();
			rspbCtl.getPrintWriter().close(); 
			rspbCtl.getConnection().close();
			System.out.println("CLOSED CLOSED");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*Send the choice made on the GUI (via RadioButton) to the server*/
	public void sendChoice(String msg){
			rspbCtl.getPrintWriter().println(msg);
			if(!msg.equals("EXIT")){
				isConnected = false; 
			}
	}
	
	public void playVideo(EmbeddedMediaPlayerComponent mp){
		mp.getMediaPlayer().playMedia("http://127.0.0.1:8989/movie"); 
	}

}
