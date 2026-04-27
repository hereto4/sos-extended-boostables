package settlement.tilemap;


import static settlement.main.SETT.TWIDTH;

import init.resources.Minable;
import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.tilemap.terrain.TGrowable;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;

public final class TerrainHotspots {

	private ArrayList<TerrainHotSpot> all = new ArrayList<TerrainHotspots.TerrainHotSpot>(0); 
	
	TerrainHotspots() {
		
	}

	
	public final static class TerrainHotSpot implements BODY_HOLDER {
		
		public final RESOURCE res;
		public final int type;
		private final int subType;
		public final SPRITE icon;
		private final Rec rec;

		private TerrainHotSpot(RESOURCE res, int type, int subType, SPRITE icon, Rec rec){
			this.type = type;
			this.subType = subType;
			this.icon = icon;
			this.rec = rec;
			this.res = res;
			rec.incrW(6);
			rec.incrH(6);
			rec.incrX(-3);
			rec.incrY(-3);
		}
		
		@Override
		public RECTANGLE body() {
			return rec;
		}
		
	}

	public LIST<TerrainHotSpot> all(){
		return all;
	}

	public void init() {
		
		Bitmap1D checked = new Bitmap1D(SETT.TAREA, false);
		
		LinkedList<TerrainHotSpot> all = new LinkedList<>();
		
		for (COORDINATE c : new Rec(SETT.TILE_BOUNDS)) {
			
			TerrainHotSpot sp = make(c.x(), c.y(), checked);
			if (sp != null) {
				all.add(sp);
			}
			
		}
		
		LinkedList<TerrainHotSpot> all2 = new LinkedList<>();
		
		while(!all.isEmpty()) {
			TerrainHotSpot h = all.removeFirst();
			
			boolean fuck = false;
			for (TerrainHotSpot s2 : all2) {
				if (h.type == s2.type && h.subType == s2.subType && h.rec.touches(s2)) {
					s2.rec.unify(h.rec);
					fuck = true;
				}
			}
			if (!fuck)
				all2.add(h);
		}
		
		this.all = new ArrayList<>(all2);
	}
	
	private TerrainHotSpot make(int tx, int ty, Bitmap1D checked) {
		if (checked.get(tx+ty*TWIDTH))
			return null;
		
		int d = 8;
		
		TerrainTile t = SETT.TERRAIN().get(tx, ty);
		if (t != null && t instanceof TGrowable) {
			Rec r = new Rec(1);
			r.moveX1Y1(tx, ty);
			checked.set(tx+ty*TWIDTH, true);
			while(true) {
				if (join(r, t, r.x1()-d, r.x1(), r.y1(), r.y2(), checked))
					continue;
				if (join(r, t, r.x2(), r.x2()+d, r.y1(), r.y2(), checked))
					continue;
				if (join(r, t, r.x1()-d, r.x2()+d, r.y1()-d, r.y1(), checked))
					continue;
				if (join(r, t, r.x1()-d, r.x2()+d, r.y2(), r.y2()+d, checked))
					continue;
				break;
			}
			if (r.width()*r.height() < 9)
				return null;
			return new TerrainHotSpot(((TGrowable)t).growable.resource, 0, ((TGrowable)t).gIndex, t.getIcon(), r);
		}
		else if (SETT.MINERALS().getter.get(tx, ty) != null) {
			Rec r = new Rec();
			r.moveX1Y1(tx, ty);
			checked.set(tx+ty*TWIDTH, true);
			Minable m = SETT.MINERALS().getter.get(tx, ty);
			while(true) {
				if (join(r, m, r.x1()-d, r.x1(), r.y1(), r.y2(), checked))
					continue;
				if (join(r, m, r.x2(), r.x2()+d, r.y1(), r.y2(), checked))
					continue;
				if (join(r, m, r.x1()-d, r.x2()+d, r.y1()-d, r.y1(), checked))
					continue;
				if (join(r, m, r.x1()-d, r.x2()+d, r.y2(), r.y2()+d, checked))
					continue;
				break;
			}
			if (r.width()*r.height() < 9)
				return null;
			return new TerrainHotSpot(m.resource, 1, m.index, m.resource.icon(), r);
		}
		return null;
		
	}
	
	private boolean join(Rec r, TerrainTile t, int x1, int x2, int y1, int y2, Bitmap1D checked) {
		boolean j = false;
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (!SETT.IN_BOUNDS(x, y))
					continue;
				if (t.is(x, y)) {
					checked.set(x+y*TWIDTH, true);
					r.unify(x, y);
					j = true;
				}
			}
		}
		return j;
	}
	
	private boolean join(Rec r, Minable t, int x1, int x2, int y1, int y2, Bitmap1D checked) {
		
		boolean j = false;
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (!SETT.IN_BOUNDS(x, y))
					continue;
				if (SETT.MINERALS().getter.get(x, y) == t) {
					checked.set(x+y*TWIDTH, true);
					r.unify(x, y);
					j = true;
				}
			}
		}
		return j;
	}
	
	public LIST<TerrainHotSpot> ALL(){
		return all;
	}



	
}
