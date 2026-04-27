package init.paths;

import java.nio.file.Path;

import game.VERSION;
import snake2d.LOG;
import snake2d.util.file.Json;

public final class ModInfo {
	
	public final String absolutePath;
	public final String path;
	public final String name;
	public final String desc;
	public final String version;
	public final int majorVersion;
	public final String author;
	public final String info;
	int TEXTURE_CACHE_SIZE = 4096;
	
	public ModInfo(String dir) throws ModInfoException{
		path = dir;
		
		Json j;
		try {
			PATH g = PATHS.local().MODS.getFolder(dir, ".txt");
			absolutePath = ""+g.get().toAbsolutePath();
			j = new Json(g.get("_Info"));
			name = j.text("NAME", "???");
			desc = j.text("DESC", "???");
			version = j.text("VERSION", "???");
			majorVersion = bestVersion(dir);
			author = j.text("AUTHOR", "???");
			info = j.text("INFO", "???");
			TEXTURE_CACHE_SIZE = j.i("TEXTURE_CACHE_SIZE", 0, 16384, 4096);
			
		}catch(Exception e){
			LOG.ln("unable to load mod: " + dir + " reason: " + e.getMessage());
			//e.printStackTrace();
			throw new ModInfoException(e);
		}

		if (TEXTURE_CACHE_SIZE < 4096 || TEXTURE_CACHE_SIZE > 4096*4 || (TEXTURE_CACHE_SIZE & (TEXTURE_CACHE_SIZE - 1)) != 0) {
			throw new ModInfoException("TEXTURE_CACHE_SIZE - Invalid value: " + TEXTURE_CACHE_SIZE + ".  Accepted are 4096, 8192, 16384" + " " + PATHS.local().MODS.get(dir));
		}
	}
	

	public static int bestVersion(String mod) throws ModInfoException {
		int best = -1;
		
		for (String ss : PATHS.local().MODS.getFolder(mod).folders()) {
			
			if (ss.length() >= 2 && ss.charAt(0) == 'V') {
				String nr = ss.substring(1);
				try {
					int i = Integer.parseInt(nr);
					if (i == VERSION.VERSION_MAJOR)
						return i;
					if (Math.ceil(VERSION.VERSION_MAJOR-i) > best)
						best = i;
				}catch(Exception e) {
					
				}
				
			}
		}
		if (best != -1)
			return best;
		throw new ModInfoException("There are no version folders in mod. " + PATHS.local().MODS.getFolder(mod).get());
	}
	
	Path getModFolder() {
		return PATHS.local().MODS.getFolder(path).getFolder("V"+majorVersion).get();
	}
	
	public static class ModInfoException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ModInfoException(Exception e) {
			super("unable to load mod", e);
		}
		
		public ModInfoException(String m) {
			super(m);
		}
		
	}
	
	
}