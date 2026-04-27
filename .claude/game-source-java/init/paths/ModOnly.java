package init.paths;

import java.nio.file.Path;

import snake2d.util.sets.ArrayList;

final class ModOnly extends PATH {
	
	private final VirtualFolder f;
	
	ModOnly(String path, String filetype, boolean create) {
		super(filetype);
		
		Path p = PATHS.i.paths.get(0);
		if (PATHS.currentMods().size() == 0)
			p = PATHS.i.paths.get(PATHS.i.paths.size()-1);
		if (create)
			Util.makeDirs(p.resolve(path));
		f = new VirtualFolder(new ArrayList<Path>(p), path);
	}
	
	ModOnly(String patha, String path, String filetype, boolean create) {
		super(filetype);
		
		Path p = PATHS.i.paths.get(0);
		if (PATHS.currentMods().size() == 0)
			p = PATHS.i.paths.get(PATHS.i.paths.size()-1);
		if (create)
			Util.makeDirs(p.resolve(path));
		f = new VirtualFolder(new ArrayList<Path>(p), path);
	}
	
	private ModOnly(VirtualFolder f, String filetype) {
		super(filetype);
		this.f = f;
	}

	@Override
	public String[] getFiles() {
		return f.listFiles(filetype);
	}
	
	@Override
	public String[] getFilesOrdered() {
		return f.listFilesOrdered(filetype);
	}
	
	@Override
	public String[] folders() {
		return f.listFolders();
	}

	@Override
	protected Path getRaw(CharSequence resource) {
		
		return f.getExistingFile(resource);
	}

	@Override
	protected void validate() {
		
	}
	
	@Override
	protected PATH getFolder(CharSequence folder, String filetype, boolean create) {
		
		if (create) {
			Path p = get();
			Util.makeDirs(p.resolve(""+folder));
		}
		return new ModOnly(f.folder(folder), filetype);
	}

	@Override
	public boolean exists(CharSequence file) {
		return f.exists(file, filetype);
	}

	@Override
	public Path get() {
		return f.getExistingFile(null);
	}
	
	@Override
	public boolean existsFolder(CharSequence folder) {
		return f.exists(folder, "");
	}

	@Override
	public boolean exists(CharSequence file, CharSequence fileType) {
		return f.exists(file, fileType);
	}

}