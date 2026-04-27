package util.error;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import snake2d.util.file.FileManager;

public class ErrorMessage {
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			args = new String[] {
				"test thing",
				"bugs@gugs.com",
				"2",
				"oh no!",
				"C:\\Users\\Jake\\AppData\\Roaming\\songsofsyx\\logs\\error03-22-2021-15-15-02-157.txt",
				"C:",
				"Runtime...",
			};
		}

		
		String pgmname = args[0];
		String bugmail = args[1];
		int type = Integer.parseInt(args[2]);
		String message = args[3];
		String dump = args[4];
		String path = args.length > 5 ? args[5] : null;
		String key = args[6];
		
		new ErrorMessage(pgmname, bugmail, type, message, dump, path, key);
		
		//new ErrorMessage("hello", "bugmail", 2, "oh no", "oh,nonnono", ".");
		
	}
	
	private ErrorMessage(String pgmname, String bugMail, int type, String message, String dump, String path, String key){
		
		
		JFrame frame = new JFrame(pgmname + " Error Message");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setMinimumSize(new Dimension(700, 100));
		frame.setMaximumSize(new Dimension(700, 700));

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(new EmptyBorder(5, 5, 5, 5));
		if (type == 0) { //dataError
			setHeader(container, new Color(230, 200, 128), "Failed loading assets!");
			setFile(container, path);
			setMessage(container, message);
		}else if(type == 1 ){ //gameerror
			setHeader(container, new Color(230, 200, 128), "Game Notification");
			setMessage(container, message);
			//setDump(container, dump, bugMail, key);
		}else if(type == 2 ){ //gameerror
			setHeader(container, new Color(230, 200, 128), "Output");
			if (new File(path).exists())
				setFile(container, path);
			setMessage(container, message);
			
			//setDump(container, dump, bugMail, key);
		}
		else {
			setHeader(container, new Color(240, 20, 20), "Unexpected Problems!");
			setMessage(container, message);
			setDump(container, dump, bugMail, key);
		}
		
		frame.add(container);

		frame.pack();
		frame.setLocationRelativeTo(null);

		frame.setAutoRequestFocus(true);
		frame.toFront();
		frame.setVisible(true);
	}
	
	private final void setHeader(JPanel container, Color col, String m) {
		JLabel header = new JLabel(m);
		header.setFont(header.getFont().deriveFont(24f));
		JPanel c = new JPanel();
		c.setBackground(col);
		c.setAlignmentX(0.5f);
		c.setAlignmentY(0);
		c.add(header);
		container.add(c);
	}

	private final void setFile(JPanel container, String filepath) {
		JPanel file = new JPanel();
		
		final String p;
		if (filepath == null)
			filepath = "null";
		JLabel label;
		if (filepath != null && new File(filepath).exists()) {
			label = new JLabel("file corrupt:");
			p = filepath;
		} else {
			if (filepath.endsWith(File.separator)) {
				String pa = filepath.substring(0, filepath.length()-1);
				int last = pa.lastIndexOf(File.separator);
				String fol = pa.substring(last, pa.length());
				label = new JLabel("directory missing: " + fol);
				p = pa.substring(0, last);
			}else {
				String pa = filepath;
				int last = pa.lastIndexOf(File.separator)+1;
				String fol = pa.substring(last, pa.length());
				label = new JLabel("file missing: " + fol);
				p = pa.substring(0, last);
			}
		}
		
		label.setFont(label.getFont().deriveFont(16f));
		file.add(label);

		JLabel path = new JLabel(p);
		path.setFont(label.getFont().deriveFont(16f));
		path.setForeground(Color.blue);
		
		path.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		path.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				try{
					FileManager.openDesctop(p);
				}catch(Exception ex) {
					
				}
				

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		file.add(path);

		// label.setLayout(new BoxLayout(label, BoxLayout.Y_AXIS));

		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		container.add(file);
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
	
	private void setDump(JPanel container, String dumpfile, String mail, String key) {
		if (dumpfile == null || dumpfile.equals("none"))
			return;
		
		JPanel panel;
		JLabel label;
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		label = new JLabel("Please report this terrible incident!");
		label.setFont(label.getFont().deriveFont(20f));
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);
		
		label = new JLabel("(By doing so you will share your computers specs with the developer, used for debugging)");
		label.setFont(label.getFont().deriveFont(12f));
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);
		
		JTextField f= new JTextField("please send file to: " + mail);
		f.setEditable(false);
		f.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(f);
		
		
		container.add(panel);
		
		
		
		panel = new JPanel();
		//panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		label = new JLabel("Dump file: ");
		label.setFont(label.getFont().deriveFont(16f));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(label);
		
		label = new JLabel(dumpfile);
		label.setFont(label.getFont().deriveFont(12f));
		label.setForeground(Color.blue);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				FileManager.openDesctop(dumpfile);
				System.exit(0);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		panel.add(label);
		container.add(panel);
		
		panel = new JPanel();
		//panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		label = new JLabel("Message: ");
		label.setFont(label.getFont().deriveFont(16f));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(label);
		container.add(panel);
		
		
		JTextArea text = new JTextArea(5, 25);
		text.setWrapStyleWord(true);
		text.setLineWrap(false);
		text.setFont(text.getFont().deriveFont(18f));
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		text.setBorder(new EmptyBorder(5,5, 5, 5));
		text.append("It just happened, man!");
		final JTextArea mess = text;
		JScrollPane scroll = new JScrollPane(text);
		container.add(scroll);
		
		JPanel c = new JPanel();
		
		JButton ok = new JButton("REPORT");
		ok.addActionListener(new ActionListener() {
			boolean sent = false;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sent)
					return;
				sent = true;
				try {
					if (new ErrorSender().send(key, text.getText(), new String(Files.readAllBytes(Paths.get(dumpfile))))) {
						System.exit(0);
					}else {
						String m = "";
						try {
							m = mess.getText() + "\n" + new String(Files.readAllBytes(Paths.get(dumpfile)));
							
						} catch (Exception e1) {
							e1.printStackTrace();
							if (!FileManager.sendEmail(mail, m, "Bug")) {
								ok.setText("ERROR..");
							}else {
								System.exit(0);
							}
							
							
						}
					}
				} catch (Exception e2) {
					e2.printStackTrace();
					String m = "";
					try {
						m = mess.getText() + "\n" + new String(Files.readAllBytes(Paths.get(dumpfile)));
						
					} catch (Exception e1) {
						e1.printStackTrace();
						if (!FileManager.sendEmail(mail, m, "Bug")) {
							ok.setText("ERROR..");
						}else {
							System.exit(0);
						}
						
						
					}
					
				}
			
				
				
				//System.exit(0);
			}
		});
		ok.setFont(ok.getFont().deriveFont(24f));
		ok.setAlignmentX(Component.CENTER_ALIGNMENT);
		ok.setBackground(new Color(200, 200, 200));
		c.add(ok);
		
		c.setBorder(new EmptyBorder(15, 0, 5, 0));
		container.add(c);
		
	}


}