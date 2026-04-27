package snake2d.util.misc;

import java.util.Locale;

public enum OS {

	MAC,WINDOWS,LINUX,UNSUPPORTED;
	
	public static OS get() {
		String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		
		if (OS.contains("mac") || OS.contains("darwin")) {
			return MAC;
		}
		if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
			return LINUX;
		}
		if (OS.contains("win"))
			return WINDOWS;
		return UNSUPPORTED;
	}
	
}
