package settlement.entity.animal.spawning;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import java.io.IOException;

import init.constant.C;
import settlement.entity.animal.Animal;
import settlement.entity.animal.AnimalSpecies;
import settlement.main.SETT;
import settlement.tilemap.terrain.TBuilding.BuildingComponent;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import util.GUTIL;

public final class AnimalSpawnSpot implements COORDINATE, INDEXED{

	public static final int MAX = 64;
	private final int index;
	private int cx,cy,am,max;
	private int si;
	boolean blocked;
	private double timer;
	
	AnimalSpawnSpot(int index){
		this.index = index;
		clear();
	}
	
	@Override
	public int x() {
		return cx;
	}

	@Override
	public int y() {
		return cy;
	}
	
	public int max() {
		return max;
	}
	
	public int current() {
		return am;
	}

	public AnimalSpecies species() {
		return SETT.ANIMALS().species.get(si);
	}
	
	public boolean active() {
		return cx > 0 && cy > 0 && !blocked;
	}
	
	public boolean blocked() {
		return blocked;
	}

	@Override
	public int index() {
		return index;
	}
	
	public void deregisterAnimal() {
		am --;
		am = CLAMP.i(am, 0, max);
	}
	
	void init(int cx, int cy, int max, AnimalSpecies s) {
		this.cy = cy;
		this.cx = cx;
		this.max = max;
		this.am = max;
		this.si = s.index();
	}

	void save(FilePutter f) {
		f.i(cx);
		f.i(cy);
		f.i(am);
		f.i(max);
		SETT.ANIMALS().map.saver().save(species(), f);
		f.d(timer);
		f.bool(blocked);
	}
	
	void load(FileGetter f) throws IOException {
		this.cx = f.i();
		this.cy = f.i();
		this.am = f.i();
		this.max = f.i();
		si = SETT.ANIMALS().map.loader().loadB(f, null).index();
		this.timer = f.d();
		blocked = f.bool();
	}

	void clear() {
		cx = -1;
		cy = -1;
		am = 0;
		max = 0;
		si = 0;
		timer = 0;
	}
	
	@Override
	public String toString() {
		return species().name + " " + max + " " + active() + " " + blocked();
	}
	
	void update(double rate) {
		
		blocked = false;
		if (!active())
			return;
		blocked = checkBlocked();
		
		if (blocked) {
			return;
		}
		
		int m = GUTIL.coos().getI();
		GUTIL.coos().shuffle(m);
		
		timer += rate*max;
		for (int i = 0; i < m && timer >= 1 && am < max; i++) {
			GUTIL.coos().set(i);
			int ax = GUTIL.coos().get().x()*C.TILE_SIZE+C.TILE_SIZEH;
			int ay = GUTIL.coos().get().y()*C.TILE_SIZE+C.TILE_SIZEH;
			if (SETT.ANIMALS().isPlacable(species(),ax, ay)) {
				Animal a = new Animal(ax, ay, species(), this);
				if (!a.isRemoved()) {
					am ++;
					timer -= 1;
				}
			}
		}
		
		timer -= (int) timer;
		
	}
	
	private boolean checkBlocked() {
		
		blocked = true;
		int x1 = x()-4;
		int y1 = y()-4;
		int x2 = x1+8;
		int y2 = y1+8;
		
		GUTIL.coos().set(0);
		
		boolean any = false;
		
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (!IN_BOUNDS(x, y))
					continue;
				
				if (SETT.ROOMS().map.is(x, y))
					return true;
				if (SETT.FLOOR().getter.is(x, y))
					return true;
				if (SETT.TERRAIN().get(x, y) instanceof BuildingComponent)
					return true;
				any |= !PATH().solidity.is(x, y);
				GUTIL.coos().get().set(x, y);
				GUTIL.coos().inc();
			}
		}
		return !any;
		
		
	}
	
}
