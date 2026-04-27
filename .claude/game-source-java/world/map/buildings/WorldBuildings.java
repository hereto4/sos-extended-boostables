package world.map.buildings;

import java.io.IOException;

import game.GAME;
import game.debug.Profiler;
import game.time.TIME;
import init.constant.C;
import snake2d.CORE;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;
import util.rendering.RenderData.RenderIterator;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.WRenContext;
import world.map.regions.Region;
import world.map.regions.centre.WorldRaceSheet;
import world.region.RD;

public class WorldBuildings extends WorldResource{

	final Bitmap2D village = new Bitmap2D(WORLD.TBOUNDS(), false);
	public final WorldBuildingSprites sprites = new WorldBuildingSprites();
	private final OPACITY[] ops = new OPACITY[16];
	boolean debugVisible = false;
	
	public WorldBuildings() throws IOException {
		super("buildings", "BUILDINGS");
		for (int i = 0; i < ops.length; i++) {
			ops[i] = new OpacityImp((int) (255*(0.4+0.4*RND.rFloat())));
		}
	}
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			village.save(file);	
		}

		@Override
		public void load(FileGetter file) throws IOException {
			village.load(file);
		}
		
		@Override
		public void clear() {
			village.clear();
		}
		
		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			ArrayListGrower<PLACABLE> placers = new ArrayListGrower<>();
			Placer p = new Placer();
			placers.add(p);
			placers.add(p.getUndo());
			return placers;
		}
		
		
		@Override
		public void generate(ACTION loadPrint) {
			loadPrint.exe();
			new WorldGeneratorBuildings();
		}
		
		@Override
		public void addDebugView() {
			debugVisible = true;
		};
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}

	

	public void renderAboveGround(WRenContext con, RenderIterator it){

		if (village.is(it.tile())){
			WorldRaceSheet.Village sh = isVisible(it.ran(), it.tile());
			
			if (sh != null) {
				int ran = it.ran()>>4;
				ops[(ran>>5)&15].bind();
				WorldRaceSheet.Farm farm = RD.RACES().all.getC(it.ran()).race.appearance().world.farm;
				farm.render(con, ran, it.x(), it.y());
				OPACITY.unbind();
				COLOR.unbind();
			}
		}
		
	}
	
	

	public void renderAbove(WRenContext con, RenderIterator it){
		if (village.is(it.tile())){
			if ((it.ran() & 2) == 0) {
				WorldRaceSheet.Village sh = isVisible(it.ran(), it.tile());
				if(sh != null) {
					int ran = ((it.ran()>>16) & 31);
					int x = it.x()+((it.ran()>>20)&7)*C.SCALE;
					int y = it.y()+((it.ran()>>24)&7)*C.SCALE;
					int li = (it.ran()>>28)&0x07;
					sh.render(con.r, con.s, ran, x, y);
					
					if (TIME.light().nightIs() && (TIME.light().partOfCircular()*16 > li)) {
						x += C.TILE_SIZEH/2+(GAME.intervals().get05()+it.ran() & 0b11);
						y += C.TILE_SIZEH/2+(GAME.intervals().get05()+(it.ran()>>4) & 0b11);
						CORE.renderer().renderUniLight(x, y, 2, 128);
					}
				}
				
			}
		}
	}
	
	public void renderAboveTerrain(WRenContext con, RenderIterator it){
		Region reg = WORLD.REGIONS().map.get(it.tile());
		if (reg != null && RD.BUILDINGS().levelMine.get(reg)*0x07 > (it.ran()&0x0FF) && !WORLD.WATER().is(it.tile())) {
			int i = it.ran()&0b1;
			i*= 4;
			
			int m = (GAME.intervals().get02() + (it.ran()>>1))&0b0111;
			if (m >= 4) {
				m -= 4;
				m = 3-m;
			}
			i += m;
			sprites.mines.render(con.r, i, it.x(), it.y());
		}
	}
	
	private WorldRaceSheet.Village isVisible(int ran, int tile) {
		if (WORLD.FOREST().amount.get(tile) == 1)
			return null;
		if (WORLD.WATER().isBig.is(tile))
			return null;
		Region r = WORLD.REGIONS().map.get(tile);
		if (debugVisible)
			return RD.RACES().all.getC(ran).race.appearance().world.village;
		if (r != null) {
			double v = RD.RACES().popSizeD(r)*(1.0-RD.DEVASTATION().current.getD(r));
			int k = (int) (0x0FFFF*v);
			if ((ran & 0x0FFFF) <= k) {
				return RD.RACES().visuals.vRace(r, ran).appearance().world.village;
			}
		}
		
		return null;
	}
	
	@Override
	protected void afterRender() {
		debugVisible = false;
	}
	
	@Override
	protected void update(double ds, Profiler prof) {
//		prof.logStart(this);
//		camp.update(ds);
//		prof.logEnd(this);
		
	}

	

	

}
