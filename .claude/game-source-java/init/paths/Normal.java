package init.paths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import snake2d.util.sets.ArrayList;

final class Normal extends PATH {

	private final VirtualFolder f;
	
	Normal(Path path, String filetype, boolean create) {
		super(filetype);
		if (create)
			Util.makeDirs(path);
		ArrayList<Path> pp = new ArrayList<Path>(path);
		f = new VirtualFolder(pp, "");
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
			Path p = f.getExistingFile(null).resolve(""+folder);
			if (!Files.exists(p)) {
				try {
					Files.createDirectories(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		return new Normal(f.getExistingFile(folder), filetype, create);
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
		return f.exists(file, filetype);
	}

}