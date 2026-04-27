package snake2d.util.file;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * 
 * @author jjYBdx4IL
 */
final class FileOpener {

	public static void open(File file) {

		if (!openSystem(file.getPath()) && !openDESKTOP(file))
			System.err.println("unable to open file " + System.getProperty("os.name"));
	}

	private static boolean openSystem(String what) {

		String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

		if (os.contains("win")) {
			return (run("explorer", "%s", what));
		}

		if (os.contains("mac")) {
			return (run("open", "%s", what));
		}

		return run("kde-open", "%s", what) || run("gnome-open", "%s", what) || run("xdg-open", "%s", what);

	}

	private static boolean openDESKTOP(File file) {

		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
			try {
				Desktop.getDesktop().open(file);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;

	}

	private static boolean run(String command, String arg, String file) {

		String[] args = arg.split(" ");
		String[] parts = new String[args.length + 1];
		parts[0] = command;
		for (int i = 0; i < args.length; i++) {
			parts[i + 1] = String.format(args[0], file).trim();
		}

		try {
			Process p = Runtime.getRuntime().exec(parts);
			if (p == null)
				return false;

			try {
				if (p.exitValue() == 0)
					return true;
				return false;
			} catch (IllegalThreadStateException itse) {
				return true;
			}
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
	}

}
