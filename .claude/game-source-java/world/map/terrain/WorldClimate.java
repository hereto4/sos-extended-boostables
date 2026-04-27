package world.map.terrain;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;

import java.io.IOException;

import init.type.CLIMATE;
import init.type.CLIMATES;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GButt;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.ToolManager;
import world.WORLD;
import world.map.terrain.WorldTerrain.WorldTerrainResource;

public final class WorldClimate extends WorldTerrainResource{

	private final Bitsmap1D map = new Bitsmap1D(0, CLIMATES.ALL().size(), TAREA());
	private final Bitsmap1D offmap = new Bitsmap1D(0, 3, TAREA());
	
	WorldClimate(){
		
		
		
	}
	
	final MAP_OBJECTE<CLIMATE> setter = new MAP_OBJECTE<CLIMATE>() {

		@Override
		public CLIMATE get(int tile) {
			return CLIMATES.ALL().get(map.get(tile));
		}

		@Override
		public CLIMATE get(int tx, int ty) {
			
			return get(tx+ty*TWIDTH());
		}

		@Override
		public void set(int tile, CLIMATE object) {
			map.set(tile, object.index());
			WORLD.changeTile(tile%TWIDTH(), tile/TWIDTH());
		}

		@Override
		public void set(int tx, int ty, CLIMATE object) {
			if (IN_BOUNDS(tx, ty))
				set(tx+ty*TWIDTH(), object);
		}

		
	};
	
	final MAP_DOUBLEE offset = new MAP_DOUBLEE() {
		
		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return offmap.get(tile) -3;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (IN_BOUNDS(tx, ty))
				return set(tx+ty*TWIDTH(), value);
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			int v = (int) (value*4);
			v = CLAMP.i(v, -3, 4);
			v += 3;
			offmap.set(tile, v);
			return this;
		}
	};
	
	
	public final MAP_OBJECT<CLIMATE> getter = setter;

	@Override
	protected void save(FilePutter saveFile) {
		map.save(saveFile);
		offmap.save(saveFile);
	}
	@Override
	protected void load(FileGetter saveFile) throws IOException {
		map.load(saveFile);
		offmap.load(saveFile);
	}
	
	@Override
	public LIST<PLACABLE> placers(ToolManager tm) {
		
		ArrayListGrower<PLACABLE> placers = new ArrayListGrower<>();
		GETTER_IMP<CLIMATE> pg = new GETTER_IMP<CLIMATE>(CLIMATES.COLD());
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		
		for (CLIMATE c : CLIMATES.ALL()) {
			butts.add(new GButt.ButtPanel(c.name) {
				@Override
				protected void clickA() {
					pg.set(c);
				}
				@Override
				protected void renAction() {
					selectedSet(pg.get() == c);
				};
			});
			
			
			
			
		}
		placers.add(new PlacableMulti(CLIMATES.INFO().name, "", CLIMATES.COLD().icon) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				setter.set(tx, ty, pg.get());
				
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public void updateRegardless(GameWindow window, AREA selected) {
				WORLD.OVERLAY().climate.add();
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
		});
		
		return placers;
	}
	
}
