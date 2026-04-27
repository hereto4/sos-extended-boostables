package init.paths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

import snake2d.Errors;
import snake2d.util.sets.LinkedList;

class Util {

	private Util() {

	}

	static String getLocal() {
		String OS = (System.getProperty("os.name")).toUpperCase(Locale.ROOT);

		if (OS.contains("MAC") || OS.contains("DARWIN")) {
			String s = System.getProperty("user.home");
			s += File.separator + "Library" + File.separator + "Application Support" + File.separator + "songsofsyx";
			return s;
		} else if (OS.contains("WIN")) {
			return System.getenv("AppData") + File.separator + "songsofsyx";
		} else if (OS.contains("NUX")) {
			String s = System.getProperty("user.home");
			s += File.separator + ".local" + File.separator + "share" + File.separator + "songsofsyx";
			return s;
		} else {
			throw new RuntimeException("could not figure out OS " + (System.getProperty("os.name")).toUpperCase(Locale.ROOT));
		}
		
	}

	static void makeDirs(Path dir) {


		if (!Files.exists(dir)) {
			
			try {
				Files.createDirectories(dir);
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			if (!Files.exists(dir))
				throw new Errors.DataError("The game could not create a directory for game files. Please check permissions",
						dir);
		}

		if (!Files.isReadable(dir)) {
			throw new Errors.DataError("The game could not read from its file directory. Please check permissions",
					dir);
		}

		
	}

	static String abort(String missingfile) throws Errors.DataError {

		String ending = "";

		String root = missingfile;


		throw new Errors.DataError("The file or directory does not exist. Try to reinstall the game.", root + ending);
	}

	static boolean check(Path path) {
		if (path == null)
			return false;
		if (!Files.exists(path))
			return false;
		
		Path pa = path.getParent();

		String cmp = ""+path.getFileName();
		if (cmp.endsWith(File.pathSeparator))
			cmp = cmp.substring(0, cmp.length()-File.pathSeparator.length());
		for (Path p : listFiles(pa)) {

			if ((""+p.getFileName()).startsWith(cmp)) {
				return true;
			}
				
		}
		return false;

		// if (f.isDirectory()) {
		// return f.exists() && path.substring(0, path.length() -
		// 1).equals(f.getAbsolutePath());
		// } else {
		// return f.exists() && path.equals(f.getAbsolutePath());
		// }

	}
	
	static Path checkHard(Path path, String file) {
		path = path.resolve(file);
		if (!Files.exists(path)) {
			throw new Errors.DataError("The file or directory does not exist. Try to reinstall the game.", path);
		}
		return path;

	}
	
	static LinkedList<Path> listFiles(Path path){
		
		LinkedList<Path> res = new LinkedList<>();

		if (!Files.exists(path))
			return res;
		
		try {
			Iterator<Path> it = Files.list(path).iterator();
			while(it.hasNext()) {
				
				Path p = it.next();
				res.add(p);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}

}
