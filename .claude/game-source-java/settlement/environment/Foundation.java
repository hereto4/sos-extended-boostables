package settlement.environment;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import snake2d.util.sets.Bitsmap1D;
import util.text.D;

public class Foundation implements MAP_DOUBLE{

	private final Bitsmap1D data = new Bitsmap1D(0, 2, SETT.TAREA);
	private final double II = 1.0/0b11;
	
	public static CharSequence ¤¤name = "Foundation";
	public static CharSequence ¤¤desc = "How well the ground is suited for supported buildings and rooms. Poor isolation will make constructed rooms require more building materials and maintenance.";
	
	static {
		D.ts(Foundation.class);
	}
	
	Foundation(){
		
	}

	@Override
	public double get(int tile) {
		if (SETT.GROUND().types.ROCK.is(tile))
			return 0.5;
		return II*data.get(tile);
	}

	@Override
	public double get(int tx, int ty) {
		if (SETT.GROUND().types.ROCK.is(tx, ty))
			return 0.5;
		return II*data.get(tx+ty*SETT.TWIDTH);
	}
	
	public void generate() {
		final HeightMap h = new HeightMap(TWIDTH, THEIGHT, 128, 4);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			double d = h.get(c);
			if (d < 0.5)
				d*=d;
			else
				d = Math.sqrt(d);
			data.set(c.x()+c.y()*SETT.TWIDTH, CLAMP.i((int)Math.round(d*0b11), 0, 0b11));
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			data.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			data.load(file);
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
}
