package test;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Main {
	
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	public static void main(final String[] args) {
		//RaspberryControler raspberryController = new RaspberryControler("127.0.0.1");		
		//NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/Program Files (x86)/VideoLAN/VLC");
		
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/VLC/VideoLAN/VLC");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main(args);
            }
        });
		
	}
	
	private Main(String[] args) {
        JFrame frame = new JFrame("vlcj Tutorial");

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        frame.setContentPane(mediaPlayerComponent);

        frame.setLocation(100, 100);
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        mediaPlayerComponent.getMediaPlayer().playMedia("http://127.0.0.1:8989/movie");
    }
}
