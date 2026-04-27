package world.map.landmark;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;

public class WorldLandmark {
	
	public final short index;
	public final Str name = new Str(32);
	public final Str description = new Str(256);
	private int size = 0;
	public short cx,cy;
	public byte textSize;
	
	WorldLandmark(int index) {
		this.index = (short) index;
		clear();
	}
	
	public int index() {
		return index;
	}
	
	void save (FilePutter f) {
		
		name.save(f);
		
		description.save(f);
		f.i(cx).i(cy);
		f.i(textSize);
		f.i(size);
	}
	
	void load (FileGetter f) throws IOException {

		name.load(f);
		description.load(f);
		cx = (short) f.i();
		cy = (short) f.i();
		textSize = (byte) f.i();
		size = f.i();
	}
	
	void clear(){
		name.clear().add(index);
		description.clear();
		cx = -1;
		cy = -1;
		textSize = -1;
		size = 0;
	}
	
	void init(int cx, int cy, int area, int textDir) {
		this.cx = (short) cx;
		this.cy = (short) cy;
		this.textSize = (byte) textDir;
	}
	
}
