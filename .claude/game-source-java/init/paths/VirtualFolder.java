package init.paths;

import static init.paths.Util.check;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

import snake2d.Errors;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

final class VirtualFolder {

	final LIST<Path> bases;
	private final String appendix;
	
	VirtualFolder(LIST<Path> bases, String path) {
		this.bases = bases;
		this.appendix =  path;
		validate();

	}
	
	private Path validate(Path path) {
		if (!check(path)) {
			throw new Errors.DataError("This file/directory does not exist: " + path,
					path);
		}
		
		return path;

	}
	
	private void validate() {
		for (Path p : bases) {
			Path f = resolve(p, null);
			if (Files.exists(f)) {
				if (!Files.isDirectory(f))
					throw new Errors.DataError("This file is not a directory: .",
							f.toAbsolutePath());
				return;
			}
		}
		Path path = resolve(bases.get(bases.size()-1), null);
		throw new Errors.DataError("This file/directory does not exist: " + path,
				path);

	}
	
	VirtualFolder folder(CharSequence next) {
		if (appendix == null || appendix.length() <= 1)
			return new VirtualFolder(bases, ""+next + PATHS.s);
		
		return new VirtualFolder(bases, appendix + PATHS.s + next);
	}
	
	Path getExistingFile(CharSequence name) {
		
		Path p = getPossibleFile(name);
		if (p == null)
			throw new Errors.DataError("This resource could not be found: ", resolve(bases.get(bases.size()-1), ""+name));
		return p;
	}
	
	public boolean exists(CharSequence file, CharSequence filetype) {
		String f = ""+file+filetype;
		for (Path m : bases) {
			if (check(resolve(m, f)))
				return true;
		}
		return false;
	}
	
	Path getPossibleFile(CharSequence name) {
		if (name == null)
			name = "";
		String r = ""+name;
		
		for (Path root : bases) {
			Path p = resolve(root, r);
			if (Files.exists(p)) {
				validate(p);
				return p;
			}
			
		}
		
		return null;
	}
	
	private Path resolve(Path base, String resource) {
		if (appendix.length() > 1)
			base = base.resolve(appendix);
		if (resource == null || resource.length() == 0)
			return base;
		try {
			return base.resolve(resource);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	String[] listFiles(String ending) {
		
		if (ending.length() <= 1)
			throw new RuntimeException();
		
		HashSet<String> map = new HashSet<>();
		boolean ignore = false;
		for (int i = 0; i < bases.size(); i++) {
			Path m = bases.get(i);
			if (i == bases.size()-1) {
				if (ignore)
					continue;
			}else {
				ignore |= checkIgnore(resolve(m, null));
			}
			for (String s : list(resolve(m, null), ending)) {		
				map.add(s);
			}
			
			
		}
		
		String[] all = new String[map.size()];
		map.toArray(all);
		Arrays.sort(all);
		
		return all;
	}
	
	String[] listFilesOrdered(String ending) {
		
		if (ending.length() <= 1)
			throw new RuntimeException();
		
		HashSet<String> map = new HashSet<>();
		for (int i = 0; i < bases.size(); i++) {
			Path m = bases.get(i);
			for (String s : list(resolve(m, null), ending)) {
				map.add(s);
			}
			
			
		}
		
		String[] all = new String[map.size()];
		map.clear();
		int k = 0;
		for (int i = 0; i < bases.size(); i++) {
			Path m = bases.get(i);
			for (String s : list(resolve(m, null), ending)) {
				if (!map.contains(s)) {
					map.add(s);
					all[k++] = s;
				}
			}
			
			
		}
		return all;
	}
	
	String[] listFolders() {
		
		HashSet<String> map = new HashSet<>();
		
		for (int i = 0; i < bases.size(); i++) {
			Path m = resolve(bases.get(i), null);
			final String sep = m.getFileSystem().getSeparator();
			for (Path p : Util.listFiles(m)) {
				if (Files.isDirectory(p)) {
					String s = ""+p.getFileName();
					if (s.startsWith("_"))
						continue;
					if (s.endsWith(sep)) {
						s = s.substring(0, s.length()-sep.length());
					}
					map.add(s);
				}
			}
			if (checkIgnore(resolve(m, null)))
				break;
		}
		
		String[] all = new String[map.size()];
		map.toArray(all);
		Arrays.sort(all);
		
		return all;
	}
	
	private boolean checkIgnore(Path path) {
		path = path.resolve("_IgnoreVanilla.txt");
		return Files.exists(path);
	}
	
	private static String[] list(Path path, String ending) {
		
		if (!Files.exists(path))
			return new String[0];
		if (!Files.isDirectory(path))
			throw new Errors.DataError("This file should be a directory, but is not...",
					""+path);
		
		
		
		LinkedList<String> res = new LinkedList<>();
		
		for (Path p : Util.listFiles(path)) {
			
			String s = "" + p.getFileName();
			res.add(s);
		}
		return clean(0, ending, 0, res);
		
	}
	
	private static String[] clean(int index, String ending, int size, LinkedList<String> origional) {
		
		int am = 0;
		for (String s : origional)
			if (getClean(s, ending) != null)
				am++;
		String[] res = new String[am];
		am = 0;
		for (String s : origional) {
			String c = getClean(s, ending);
			if (c != null)
				res[am++] = c;	
		}
		return res;
	}
	
	private static String getClean(String s, String ending) {
		if (s.charAt(0) == '_')
			return null;
		if (ending != null && !s.endsWith(ending))
			return null;
		return s.substring(0, s.length()-ending.length());

	}
	

	
}
