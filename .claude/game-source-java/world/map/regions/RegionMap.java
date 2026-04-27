package world.map.regions;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.sets.Bitsmap1D;
import world.WORLD;

public final class RegionMap implements MAP_OBJECTE<Region>, SAVABLE {


	private final Bitsmap1D mapID = new Bitsmap1D(0, Integer.numberOfTrailingZeros(WREGIONS.MAX+1), TAREA());
	
	RegionMap() {
		
	}


	@Override
	public Region get(int tile) {
		if (mapID.get(tile) == 0)
			return null;
		return WORLD.REGIONS().getByIndex(mapID.get(tile)-1);
	}
	
	@Override
	public Region get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return get(tx+ty*TWIDTH());
		return null;
	}
	
	@Override
	public void set(int tile, Region object) {
		if (object == null)
			mapID.set(tile, 0);
		else
			mapID.set(tile, object.index()+1);
		WORLD.REGIONS().dirty = true;
	}

	@Override
	public void set(int tx, int ty, Region object) {
		if (IN_BOUNDS(tx, ty)) {
			set(tx+ty*TWIDTH(), object);
		}
	}
	
	@Override
	public void save(FilePutter file) {
		mapID.save(file);
	}


	@Override
	public void load(FileGetter file) throws IOException {
		mapID.load(file);
	}
	
	@Override
	public void clear() {
		
		mapID.clear();
		WORLD.REGIONS().dirty = true;
	}
	
}
