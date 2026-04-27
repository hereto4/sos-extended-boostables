package snake2d.util.file;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import snake2d.Errors;

public abstract class FileManager {
	
	public static class NAME {
		
		private NAME() {}
		
		public static String timeStampString(String original){
			String postfix = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss-SSS").format(new Date());
			return original + postfix;
		}
		
		public final static String legalChars = "aA - zZ, 0-9, -, _, 'space'";
		
		private final static Pattern okChars = Pattern.compile("[-_ A-Za-z0-9]+");
		
		public static boolean okName(CharSequence filename){
			return okChars.matcher(filename).matches();
		}
		
	}
	
	public static class FILE {
		
		public static boolean exists(String pathname) {
			File f = new File(pathname);
			if (f.exists()) {
				String p2 = f.getAbsolutePath();
				for (int i = 0; i < p2.length(); i++) {
					if (p2.charAt(i) != pathname.charAt(i)) {
						
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		public static String ensureExists(String pathname) {
			if (!exists(pathname))
				throw new Errors.DataError("File missing", pathname);
			return pathname;
		}
		
		public static String toString(String path) {
			byte[] encoded;
			try {
				encoded = Files.readAllBytes(Paths.get(path));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			 return new String(encoded, Charset.defaultCharset());
		}
		
		public static String toStringRelative(Object o, String name) {
			
			String path = new File(o.getClass().getResource(name).getPath()).getAbsolutePath();
			
			byte[] encoded;
			try {
				encoded = Files.readAllBytes(Paths.get(path));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			 return new String(encoded, Charset.defaultCharset());
		}
		
		public static boolean readWriteRights() {
			try {
				Path p = new File("").toPath();
				return Files.isWritable(p);
			}catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}
	
	public static void openDesctop(String path) {
		FileOpener.open(new File(path));
	}
	
	public static boolean sendEmail(String mail, String mess, String title) {
		try {

			String uri = "mailto:" + mail + "?subject=" + title + "&body=";
			uri += URLEncoder.encode(mess, "UTF-8").replaceAll("\\+", "%20");

			Desktop.getDesktop().browse(new URI(uri));
		} catch (IOException | URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	
}


