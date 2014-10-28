package test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import view.RaspberryControler;


public class Main {
	
	
	public static void main(final String[] args) {
		RaspberryControler raspberryController = new RaspberryControler("127.0.0.1");		
		
//		SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                new Main(args);
//            }
//        });
		
	}
	
//	private Main(String[] args) {
//        JFrame frame = new JFrame("vlcj Tutorial");
//
//        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
//
//        frame.setContentPane(mediaPlayerComponent);
//
//        frame.setLocation(100, 100);
//        frame.setSize(1050, 600);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//
//        mediaPlayerComponent.getMediaPlayer().playMedia("http://127.0.0.1:8989/movie");
//    }
}
