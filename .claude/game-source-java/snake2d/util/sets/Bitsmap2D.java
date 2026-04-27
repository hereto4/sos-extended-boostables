package snake2d.util.sets;

import java.io.IOException;

import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.map.MAP_INTE;

public class Bitsmap2D implements MAP_INTE, BODY_HOLDER, SAVABLE{
	
	private final Bitsmap1D map;
	private final int width;
	private final Rec body;
	
	public Bitsmap2D(int outof, int bits, int width, int height){
		body = new Rec(width, height);
		map = new Bitsmap1D(outof, bits, width*height);
		this.width = width;
	}
	
	public Bitsmap2D(int outof, int bits, DIMENSION body){
		this.body = new Rec(body.width(), body.height());
		this.width = body.width();
		map = new Bitsmap1D(outof, bits, body.width()*body.height());
		
	}

	@Override
	public int get(int tile) {
		return map.get(tile);
	}

	@Override
	public MAP_INTE set(int tile, int value) {
		map.set(tile, value);
		return this;
	}

	@Override
	public MAP_INTE set(int tx, int ty, int value) {
		if (body.holdsPoint(tx, ty))
			set(tx+ty*width, value);
		return this;
	}

	@Override
	public int get(int tx, int ty) {
		if (!body.holdsPoint(tx, ty))
			return map.outof;
		return get(tx+ty*width);
	}
	
	@Override
	public int max() {
		return map.maxValue();
	}

	@Override
	public void save(FilePutter file) {
		map.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		map.load(file);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public RECTANGLE body() {
		return body;
	}
	
}
