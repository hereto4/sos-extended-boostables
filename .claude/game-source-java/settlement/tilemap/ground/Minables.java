package settlement.tilemap.ground;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TAREA;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;
import java.util.Arrays;

import init.constant.C;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_INTE;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.keymap.MAPSAVE;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.PlacableSimpleTile;

public class Minables {

	private long[] amounts = new long[RESOURCES.minables().all().size()];
	private final Bitsmap1D types = new Bitsmap1D(0, 5, TAREA) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void set(int index, int value) {
			int am = amount.get(index);
			amounts[get(index)] -= am;
			super.set(index, value);
			amounts[get(index)] += am;
			amounts[get(index)] = Math.max(0, amounts[get(index)]);
		};
	};
	private final Bitsmap1D amount = new Bitsmap1D(0, 6, TAREA) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void set(int index, int value) {
			amounts[types.get(index)] -= get(index);
			super.set(index, value);
			amounts[types.get(index)] += get(index);
			amounts[types.get(index)] = Math.max(0, amounts[types.get(index)]);
		};
	};
	


	
	Minables() {
		
		PLACABLE undo = new PlacableMulti("minerals remove") {

		
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				getter.set(tx, ty, null);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return getter.is(tx, ty) ? null : "";
			}
		};
		IDebugPanelSett.add(undo);
		
		for (Minable m : RESOURCES.minables().all()) {
			IDebugPanelSett.add(new PlacableMulti("mineral " + m.resource.name) {
				
				@Override
				public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
					if (getter.is(tx, ty, m))
						amountD.increment(tx, ty, 0.1);
					else
						getter.set(tx, ty,m);
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
					if (!IN_BOUNDS(tx, ty))
						return "";
					if (PATH().solidity.is(tx, ty))
						return "";
					if (ROOMS().map.is(tx, ty))
						return "";
					return null;
				}
				
				
				private final CharSequence name = "mineral " + m.resource.name;
				@Override
				public CharSequence name() {
					return name;
				}
				
				@Override
				public SPRITE getIcon() {
					return m.resource.icon();
				}
				
				
				@Override
				public PLACABLE getUndo() {
					return undo;
				}
			});
			
			
		}
		
	}
	
	public final MAP_OBJECTE<Minable> getter = new MAP_OBJECTE<Minable>() {

		@Override
		public Minable get(int tile) {
			if (amount.get(tile) == 0)
				return null;
			return RESOURCES.minables().getAt(types.get(tile));
		}

		@Override
		public Minable get(int tx, int ty) {
			return get(tx+ty*TWIDTH);
		}

		@Override
		public void set(int tile, Minable object) {
			Minable old = get(tile);
			
			if (object == null) {
				amount.set(tile, 0);
			}else {
				types.set(tile, object.index);
				amount.set(tile, 1);
			}
			
			if (object != old)
				SETT.TILE_MAP().miniCUpdate(tile%TWIDTH, tile/TWIDTH);
		}

		@Override
		public void set(int tx, int ty, Minable object) {
			if (IN_BOUNDS(tx, ty)) {
				set(tx+ty*TWIDTH, object);
			}
		}
	};
	
	public final MAP_INTE amountInt = new MAP_INTE() {
		
		@Override
		public int get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return 0;
		}
		
		@Override
		public int get(int tile) {
			return amount.get(tile);
		}
		
		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			
			
			if (IN_BOUNDS(tx, ty)) {
				set(tx+ty*TWIDTH, value);
			}
			
			
			return this;
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			Minable old = getter.get(tile);
			if (value < 0)
				value = 0;
			if (value > amount.maxValue())
				value = amount.maxValue();
			
			amount.set(tile, value);
			if (getter.get(tile) != old)
				SETT.TILE_MAP().miniCUpdate(tile%TWIDTH, tile/TWIDTH);
			return this;
		}
	};
	
	public final MAP_DOUBLEE amountD = new MAP_DOUBLEE() {
		
		private final double i = 1.0/amount.maxValue();
		
		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return 0;
		}
		
		@Override
		public double get(int tile) {
			
			return amount.get(tile)*i;
		}

		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			
			amountInt.set(tile, (int)(value*amount.maxValue()));
			return this;
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (IN_BOUNDS(tx, ty))
				set(tx+ty*TWIDTH, value);
			return this;
		}
	};
	
	
	final PlacableSimpleTile CLEAR = new PlacableSimpleTile("clear exavatables") {
		
		@Override
		public String name() {
			return "clear exavatables";
		}
		
		@Override
		public SPRITE getIcon() {
			return new SPRITE.Twin(SPRITES.icons().m.pickaxe, SPRITES.icons().m.cancel);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty) {
			return IN_BOUNDS(tx, ty) ? null : E;
		}

		@Override
		public void place(int tx, int ty) {
			amountInt.set(tx,ty, 0);
		}
	};
	final PlacableSimpleTile INCREASE = new PlacableSimpleTile("decrease") {
		
		@Override
		public String name() {
			return "increase";
		}
		
		@Override
		public SPRITE getIcon() {
			return new SPRITE.Twin(SPRITES.icons().m.pickaxe, SPRITES.icons().m.arrow_up);
		}
		
		@Override
		public void place(int tx, int ty) {
			amountInt.increment(tx, ty, 8);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty) {
			return IN_BOUNDS(tx, ty) ? null : E;
		}
	};
	final PlacableSimpleTile DECREASE = new PlacableSimpleTile("decrease") {
		
		@Override
		public String name() {
			return "decrease";
		}
		
		@Override
		public SPRITE getIcon() {
			return new SPRITE.Twin(SPRITES.icons().m.pickaxe, SPRITES.icons().m.arrow_down);
		}
		
		@Override
		public void place(int tx, int ty) {
			amountInt.increment(tx, ty, -8);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty) {
			return IN_BOUNDS(tx, ty) ? null : E;
		}
	};

	
	void save(FilePutter saveFile) {
		MAPSAVE.saveMeta(saveFile, RESOURCES.minables().all());
		amount.save(saveFile);
		types.save(saveFile);
		saveFile.lsE(amounts);
	}

	void load(FileGetter saveFile) throws IOException {
		int[] order = MAPSAVE.saveWash(saveFile, RESOURCES.minables().all(), -1);
		amount.load(saveFile);
		types.load(saveFile);
		saveFile.lsE(amounts);
		if (order != null) {
			
			for (int i = 0; i < SETT.TAREA; i++) {
				int oi = types.get(i);
				int ni = order[oi];
				if (ni == -1) {
					amount.set(i, 0);
					types.set(i, 0);
				}else {
					types.set(i, ni);
				}
			}
			Arrays.fill(amounts, 0);
			for (int i = 0; i < SETT.TAREA; i++) {
				amounts[getter.get(i).resource.index()] += amount.get(i);
			}
		}
	}
	
	
	private final DIR[] dirs = new DIR[] {
		DIR.W,DIR.NW,DIR.N
	};
	
	void render(Renderer r, int tile, int ran, int x, int y) {
		double d = amountD.get(tile);
		
		if (d == 0)
			return;
		
		
		
		int t = (int) (d*8);
		t = CLAMP.i(t, 0, 3);
		t*= 8;
		t += ran&0x07;
		ran = ran >> 3;
//		x = x+(-4 + (ran & 0b0111))*C.SCALE;
//		ran = ran >> 3;
//		y = y+(-4 + (ran & 0b0111))*C.SCALE;
//		ran = ran >> 3;
		
		Minable m = RESOURCES.minables().getAt(types.get(tile));
		m.sheet.render(r, t, x, y);
		
		int a = (int) (d*12);
		
		int iters = (int) CLAMP.i(a/3, 0, 3);
		if (iters > 0) {
			

			int dd = ran%3;
			ran = ran >> 2;
			for (int i = 0; i < iters; i++) {
				int tt = (ran&31);
				ran = ran >> 5;
				DIR dir = dirs[dd];
				dd++;
				dd %= 3;
				m.sheet.render(r, tt, x+dir.x()*C.TILE_SIZEH, y+dir.y()*C.TILE_SIZEH);
			}
			
			
		}
		
	}

	COLOR miniC(ColorImp col, COLOR ground, int tx, int ty) {
		Minable m = getter.get(tx, ty);
		col.interpolate(m.miniColor, ground, 0.5 + amountD.get(tx, ty)*0.5);
		for (DIR d : DIR.ORTHO) {
			if (getter.get(tx, ty, d) != m) {
				return col.shadeSelf(0.75);
			}
		}
		return col;
	}
	
	public final DOUBLE_O<Minable> totals = new DOUBLE_O<Minable>(){

		@Override
		public double getD(Minable t) {
			return (double)amounts[t.index()]/amount.maxValue();
		}

		
	};
	
	
	
	
}
