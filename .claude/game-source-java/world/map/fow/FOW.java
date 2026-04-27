package world.map.fow;

import java.io.IOException;

import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import init.constant.C;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.Bitmap2D;
import util.GUTIL;
import util.data.BOOLEAN.BOOLEANImp;
import util.rendering.RenderData.RenderIterator;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.WRenContext;
import world.map.regions.Region;
import world.region.RD;

public final class FOW extends WorldResource implements MAP_BOOLEAN{

	private final Bitmap2D tmp = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final Bitmap2D visible = new Bitmap2D(WORLD.TBOUNDS(), false);

	public BOOLEANImp toggled = new BOOLEANImp(true) {
		
		@Override
		public BOOLEANImp set(boolean b) {
			dirty = true;
			super.set(b);
			return this;
		};
		
	};
	private boolean dirty = true;
	
	public FOW(){
		super("Fog of War", "FOW");		
		IDebugPanelWorld.add("toggle fow", toggled);
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	void update() {

		visible.clear();
		
		Flooder f = GUTIL.flooder();
		f.init(this);
		
		for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
			Region reg = FACTIONS.player().realm().region(i);
			f.pushSloppy(reg.cx(), reg.cy(), 0);
		}
		
		
		
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			visible.set(t, true);
	
			Region from = WORLD.REGIONS().map.get(t);		
			for (DIR d : DIR.ALL) {
				if (from == null && WORLD.REGIONS().map.get(t, d) == null)
					visible.set(t, d, true);
				else if (from != null && from.is(t, d))
					visible.set(t, d, true);
				if (WORLD.PATH().map.can(t, d)) {
					Region to = WORLD.REGIONS().map.get(t, d);
					if (to == null || from == null || from == to || from.faction() == FACTIONS.player() || (from.faction() != null && DIP.get((FactionNPC) from.faction()).transit))
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
			
		}
		
		f.done();
		
	}
	
	public void render(WRenContext data) {
		
		if (!toggled.b)
			return;
		CORE.renderer().shadowDepthSet((byte)255);
		WORLD.CENTRE().sprite.renderAboveTerrain(data);
		RenderIterator it = data.data.onScreenTiles(0,0,0,0);
		
		while(it.has()) {
			render(data, it);
			it.next();
		}
		
		
	}
	
	public void render(WRenContext con, RenderIterator it) {
		
		if (!is(it.tile()))
			return;
		if (WORLD.REGIONS().centreTile().is(it.tile()) && is(it.tile())) {
			CORE.renderer().shadowDepthSet((byte)127);
		}else {
			CORE.renderer().shadowDepthSet((byte)255);
		}
		CORE.renderer().renderShadow(it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE, COLOR.WHITE100.texture(), (byte)0);
		tmp.set(it.tile(), false);
	}
	
	public void enlighten(int tx, int ty, int radius) {
		if (!toggled.b)
			return;
		for (int i = 0; GUTIL.circle().radius(i) <= radius; i++) {
			tmp.set(tx+GUTIL.circle().get(i).x(), ty+GUTIL.circle().get(i).y(), true);
		}
		
	}

	@Override
	public boolean is(int tile) {
		if (!toggled.b)
			return false;
		if (tmp.is(tile))
			return false;
		if (visible.is(tile))
			return false;
		Region reg = WORLD.REGIONS().map.get(tile);
		if (reg != null && RD.DIST().reachable(reg))
			return false;
		if (reg != null && reg.faction() == FACTIONS.player())
			return false;
		return true;
	}

	@Override
	public boolean is(int tx, int ty) {
		return is(tx+ty*WORLD.TWIDTH());
	}

	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(this);
		if (FACTIONS.player().capitolRegion() == null || !SETT.exists())
			return;
		if (dirty)
			update();
		dirty = false;
		super.update(ds, prof);
		prof.logEnd(this);
	}
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			dirty = true;
		}
		
		@Override
		public void clear() {
			dirty = true;
		}
		
		
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	};
	
	@Override
	protected void initBeforePlay() {
		dirty = true;
	}
	
}
