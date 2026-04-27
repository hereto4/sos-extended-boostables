package init.paths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import snake2d.Errors;

public abstract class PATH {

	protected final String filetype;

	PATH(String filetype) {
		this.filetype = filetype;
	}

	public Path get(CharSequence resource) {
		if (filetype.equals(PATHS.s))
			return getRaw(resource);
		if (("" + resource).endsWith(filetype))
			return getRaw(resource);
		return getRaw(resource + filetype);
	}
	
	public Path getLikeHell(CharSequence resource) {
		return getRaw(resource);
	}

	protected abstract Path getRaw(CharSequence resource);

	public final PATH getFolder(CharSequence folder) {
		return getFolder(folder, filetype);
	}
	
	public PATH getFolder(CharSequence folder, String filetype) {
		return getFolder(folder, filetype, false);
	}
	
	protected abstract PATH getFolder(CharSequence folder, String filetype, boolean create);
	
	protected PATH getFolder(CharSequence folder, boolean create) {
		return getFolder(folder, filetype, create);
	}

	public abstract Path get();

	public Path create(CharSequence file) {
		Path p = get().resolve(file + filetype);
		try {
			Files.deleteIfExists(p);
			Files.createFile(p);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Errors.DataError("Unable to process file", ""+p);
		}
		
		return p;
	}

	public void delete(CharSequence file) {
		Path p = get().resolve(file + filetype);
		try {
			Files.deleteIfExists(p);
		} catch (IOException e) {
			e.printStackTrace();
			throw new Errors.DataError("Unable to delete file", ""+p);
		}
	}

	public abstract boolean exists(CharSequence file);
	
	public abstract boolean exists(CharSequence file, CharSequence fileType);
	
	public abstract boolean existsFolder(CharSequence folder);

	public abstract String[] getFiles();
	
	public abstract String[] getFilesOrdered();
	
	public String[] getFiles(int min) {
		String[] ss = getFiles();
		if (ss.length < min)
			throw new Errors.DataError("insufficient files declared. Needs at least " + min, ""+get());
		return ss;
	}
	
	public String[] getFiles(int min, int max) {
		String[] ss = getFiles();
		if (ss.length < min)
			throw new Errors.DataError("insufficient files declared. Needs at least " + min, ""+get());
		else if (ss.length > max) {
			throw new Errors.DataError("too many files declared. Max is: " + max, ""+get());
		}
		return ss;
	}
	
	public abstract String[] folders();


	public String fileEnding() {
		return filetype;
	}

	protected abstract void validate();





	
}