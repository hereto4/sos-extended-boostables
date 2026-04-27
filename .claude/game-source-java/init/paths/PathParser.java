package init.paths;

import java.nio.file.Path;

import game.GAME;
import snake2d.Errors;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public final class PathParser {

	public static final String split = "->";
	
	public static Path get(PATH base, String relPath, Json json, String jsonKey, int off) {
		
		String[] ss = relPath.split(split);
		if (ss.length < 1-off || ss.length-1-off < 0) {
			String e = relPath + " " + "does not specify a path with the root of: " + base.get().toAbsolutePath() + " paths are specifided by folder->folder->file, where the folder part is optional. Both the folders and the file must exist.";
			error(e, json, jsonKey);
			return null;
		}

		PATH p = base;
		for (int i = 0; i < ss.length-1-off; i++) {
			if (!p.existsFolder(ss[i])) {
				String e = "The folder specified: " + ss[i] + ", does not exist in: " + p.get().toAbsolutePath();
				error(e, json, jsonKey);
				return null;
			}
			p = p.getFolder(ss[i]);
		}
		String file = ss[ss.length-1-off];
		
		if (!p.exists(file)) {
			
			String e = "The file: " + file + p.fileEnding() + ", does not exist in: " + p.get().toAbsolutePath();
			error(e, json, jsonKey);
			return null;
		}
		
		return p.get(file);
		
	}
	
	public static LIST<Path> getMany(PATH base, String relPath, Json json, String jsonKey) {
		
		LinkedList<Path> res = new LinkedList<>();
		
		String[] ss = relPath.split(split);
		if (ss.length < 1) {
			String e = relPath + " " + "does not specify a path with the root of: " + base.get().toAbsolutePath() + " paths are specifided by folder->folder->file, where the folder part is optional. Both the folders and the file must exist.";
			error(e, json, jsonKey);
			return null;
		}

		PATH p = base;
		for (int i = 0; i < ss.length-1; i++) {
			if (!p.existsFolder(ss[i])) {
				String e = "The folder specified: " + ss[i] + ", does not exist in: " + p.get().toAbsolutePath();
				error(e, json, jsonKey);
				return null;
			}
			p = p.getFolder(ss[i]);
		}
		String file = ss[ss.length-1];
		
		if (file.charAt(file.length()-1) == '*') {
			String begin = file.substring(0, file.length()-1);
			for (String f : p.getFiles()) {
				if (f.startsWith(begin)) {
					res.add(p.get(f));
				}
			}
			if (res.size() == 0) {
				String e = "There are no files: " + relPath + ", that match this pattern: " + p.get().toAbsolutePath();
				error(e, json, jsonKey);
			}
			
			return res;
		}
		
		if (!p.exists(file)) {
			String e = "The file: " + file + p.fileEnding() + ", does not exist in: " + p.get().toAbsolutePath();
			error(e, json, jsonKey);
			return null;
		}
		
		res.add(p.get(file));
		
		return res;
		
	}
	
	public static void error(String error, Json json, String jsonKey) {
		
		if (json != null) {
			GAME.Warn(json.errorGet(error, jsonKey));
		}else {
			throw new Errors.DataError(error);
		}
		
	}
	
	
}
