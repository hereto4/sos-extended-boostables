package snake2d;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PreLoaderSwing{

	public static void main(String[] args) {
		if (args.length == 0) {
			args = new String[] {
				"Songs of poop",
				"C:\\Users\\mail__000\\Documents\\syx13\\Syx\\res\\base\\texture\\PreLoader.png"
			};
		}
		new PreLoaderSwing(args[0], args[1], args[2]);
	}
	
	private  JFrame frame;
	
	private PreLoaderSwing(String name, String path, String iconPath){

		try {
			frame = new JFrame(name);
		}catch(Exception e){
			e.printStackTrace(System.out);
		}
		
		
		frame.setLocationRelativeTo(null);
		frame.setUndecorated(true);
		frame.setAlwaysOnTop(true);
		JPanel panel = new JPanel();
	    panel.setBackground(Color.BLACK); 
		
	    String preloader = path;
	    
		if (!new File(preloader).exists())
			throw new RuntimeException("unable to find file: " + preloader);
		ImageIcon icon = new ImageIcon(preloader);
		JLabel label = new JLabel();
	    label.setIcon(icon); 
	    panel.add(label);

//	    JLabel version = new JLabel(name);
//	    version.setSize( label.getPreferredSize() );
//	    version.setBackground(new Color(Color.TRANSLUCENT));
//	    version.setLocation(0, 0);
//	    panel.add(version);
	    
	    frame.getContentPane().add(panel); 
		frame.pack();
		
		frame.setLocation(frame.getLocation().x - frame.getWidth()/2, frame.getLocation().y- frame.getHeight()/2);
		frame.setIconImage(new ImageIcon(iconPath).getImage());
		frame.setFocusable(false);
		frame.setFocusableWindowState(false);
		frame.setVisible(true);
		frame.toFront();

		for (int i = 0; i < 5000; i++) {
			
			
			try {

				//Printer.ln(System.in.available());
				if (System.in.available() > 0 && System.in.read() != -1)
					break;
				Thread.sleep(1);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
		dispose();

	}
	
	private void dispose(){
		frame.setVisible(false);
		
		frame.dispose();
		
	}
	
}