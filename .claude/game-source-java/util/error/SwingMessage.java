package util.error;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class SwingMessage {
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			args = new String[] {
				
				"Dear Mac User",
				"You are playing Songs of Syx through Mac, this is good." + System.lineSeparator()
				+ "Unfortionately, the steam overlay breaks the visuals of the game."+ System.lineSeparator()
				+ "The overlay can not be disabled by us developers, it has to be done manually by the user."+ System.lineSeparator()
				+ "Steam > Right click Songs of Syx > Properties > General > Uncheck \"Enable the Steam Overlay while in-game\""+ System.lineSeparator()
				+ "if having trouble: www.reddit.com/r/songsofsyx/comments/umzi1t/deactivate_steam_overlay_to_run_game_on_mac"+ System.lineSeparator()
				+ "Please also report this as a bug so that steam will fix this issue."+ System.lineSeparator()
				+ "https://help.steampowered.com/en/"+ System.lineSeparator()
				+ "The game also works fine to run like a normal app from the installation directory, being completely DRM free."+ System.lineSeparator()
				+ "Appologies for the inconvieniance, the alternative is to delist the game for mac, which would be a travesty, since it should run fine nativly on it. The goal is to apply enough preassure on steam so that they fix it."+ System.lineSeparator()
			};
		}

		
		String title = args[0];
		String mess = args[1];
		
		
		new SwingMessage(title, mess);

		
	}
	
	private SwingMessage(String title, String message){
		
		
		JFrame frame = new JFrame(title);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setMinimumSize(new Dimension(700, 500));
		frame.setMaximumSize(new Dimension(700, 700));

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(new EmptyBorder(5, 5, 5, 5));
		setMessage(container, message);
		
		frame.add(container);

		frame.pack();
		frame.setLocationRelativeTo(null);

		frame.setAutoRequestFocus(true);
		frame.toFront();
		frame.setVisible(true);
	}
	

	
	private void setMessage(JPanel container, String message) {
		JTextArea text = new JTextArea(5, 25);
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		text.setFont(text.getFont().deriveFont(18f));
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		text.setBorder(new EmptyBorder(25, 25, 5, 25));
		text.append(message);

		JScrollPane scroll = new JScrollPane(text);
		container.add(scroll);
	}
	



}