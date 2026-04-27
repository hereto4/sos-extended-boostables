package settlement.environment;

import static settlement.environment.SettEnvMap.RADIUS;

import settlement.environment.SettEnvMap.Updatable;
import settlement.main.SETT;
import settlement.misc.util.TileRayTracer.Ray;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.main.Room;
import settlement.room.main.furnisher.FurnisherItem;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LISTE;

public class SettEnvMonument {
	
	private final All all;
	private final Degrade degrade;
	public MAP_BOOLEAN DEGRADE;
	
	
	SettEnvMonument(LISTE<Updatable> all) {
		this.all = new All(all);
		this.degrade = new Degrade(all);
		DEGRADE = degrade.has;
	}




	
	private static boolean isBlocked(int tx, int ty) {
		if (SETT.ROOMS().map.blueprint.get(tx, ty) instanceof ROOM_MONUMENT)
			return false;
		return SETT.LIGHTS().los().get(tx, ty).blocksEnv(tx, ty);
	}

	public void changeUpgrade(int tx, int ty) {
		SETT.ENV().map.setChanged(tx, ty, all);
	}
	
	public void changeDegrade(int tx, int ty) {
		SETT.ENV().map.setChanged(tx, ty, degrade);
	}

	
	private final Bitmap2D extra = new Bitmap2D(new Rec(SettEnvMap.RADIUS*2+10, SettEnvMap.RADIUS*2+10), false);
	
	public void addExtra(ROOM_MONUMENT m, FurnisherItem it, int x1, int y1) {
		extra.clear();
		
		
		EUpdater.traces.checkInit();
		
		int ssx = x1 + it.width()/2;
		int ssy = y1 + it.height()/2;
		
		for (Ray r : EUpdater.traces.rays()){
			for (int i = 0; i < r.size(); i++) {
				COORDINATE d = r.get(i);
				int sourceX = ssx;
				int sourceY = ssy;
				if ((it.width()&1) == 0 && d.x() > 0)
					sourceX --;
				if ((it.height()&1) == 0 && d.y() > 0)
					sourceY --;
				int dx = d.x()+sourceX;
				int dy = d.y()+sourceY;

				if (!SETT.IN_BOUNDS(dx, dy))
					break;
				
				double rad = r.radius(i);
				
				if (rad >= m.radius(it))
					break;
				
				if (EUpdater.traces.check(d)) {
					
					extra.set(d.x()+sourceX-x1+SettEnvMap.RADIUS, d.y()+sourceY-y1+SettEnvMap.RADIUS, true);
				}
				if (isBlocked(dx, dy))
					break;				
			}
			
		}
		
		
		
		
	}
	
	public int extra(int x1, int y1, int tx, int ty) {
		
		int ex = tx-x1+SettEnvMap.RADIUS;
		int ey = ty-y1+SettEnvMap.RADIUS;
		return extra.is(ex, ey) ? 1 : 0;
	}

	private class All extends Updatable {

		private final Bitmap2D has = new Bitmap2D(SETT.TILE_BOUNDS, false);
		
		All(LISTE<Updatable> all) {
			super(all);
		}

		@Override
		protected void update(RECTANGLE bounds, RECTANGLE area) {

			for (COORDINATE c : area) {
				if (SETT.IN_BOUNDS(c)) {
					has.set(c, false);
					for (ROOM_MONUMENT m : SETT.ROOMS().MONUMENTS.all) {
						m.mapData.set(c, 0);
						m.mapUpgrade.set(c, m.mapUpgrade.max());
					}
				}
				
			}
			
			for (COORDINATE c : bounds) {
				trace2(c, area);
				
				
			}
		}
		

		

		
		private void trace2(COORDINATE source, RECTANGLE area) {
			
			Room ro = SETT.ROOMS().map.get(source);
			
			if (ro == null)
				return;
			
			if (!(ro.blueprint() instanceof ROOM_MONUMENT))
				return;
			
			if (!isCentre(ro, source.x(), source.y(), source.x(), source.y())) {
				return;
			}
			
			ROOM_MONUMENT m = (ROOM_MONUMENT) ro.blueprint();
			
			int ra = (int) (m.radius(SETT.ROOMS().fData.item.get(source.x(), source.y())));
			if (ra == 0)
				return;
			if (!area.holdsPoint(source)) {
				if (Math.abs(area.cX()-source.x())-ra > RADIUS/2)
					return;
				if (Math.abs(area.cY()-source.y())-ra > RADIUS/2)
					return;
			}
			
			int up = ro.upgrade(source.x(), source.y());
			
			EUpdater.traces.checkInit();
			
			for (Ray r : EUpdater.rays(source.x(), source.y(), area)){
				for (int i = 0; i < r.size(); i++) {
					if (r.radius(i) >= ra)
						break;
					COORDINATE d = r.get(i);
					
					int dx = d.x()+source.x();
					int dy = d.y()+source.y();
					if (area.holdsPoint(dx, dy) && EUpdater.traces.check(d)) {
						
						if (m.mapUpgrade.get(dx, dy) > up) {
							m.mapUpgrade.set(dx, dy, up);
						}
						if (m.mapData.get(dx, dy) < m.maxEnv())
							m.mapData.increment(dx, dy, 1);
						has.set(dx, dy, true);
						
						
					}
					
					
					
					if (isBlocked(dx, dy))
						break;				
				}
				
			}

		}

		private boolean isCentre(Room ro, int dx, int dy, int sourceX, int sourceY) {
			int w = ro.width(dx, dy);
			int h = ro.height(dx, dy);
			int x1 = ro.x1(dx, dy);
			int y1 = ro.y1(dx, dy);
			int cx = x1+w/2;
			int cy = y1+h/2;
			
			if ((w&1) == 0 && sourceX > cx)
				cx--;
			if ((h&1) == 0 && sourceY > cy)
				cy--;
			return dx == cx && dy == cy;
		}
		
		@Override
		public double getBaseValue(int tx, int ty) {
			return SETT.ROOMS().map.blueprintImp.get(tx, ty) instanceof ROOM_MONUMENT ? 1 : 0;
		}
		
		@Override
		protected boolean has(int tx, int ty) {
			return has.is(tx, ty);
		}

		@Override
		protected void clear() {
			for (ROOM_MONUMENT m : SETT.ROOMS().MONUMENTS.all) {
				m.mapData.clear();
				m.mapUpgrade.clear();
			}
			has.clear();
		}
		
	}
	
	private class Degrade extends Updatable {

		private final Bitmap2D has = new Bitmap2D(SETT.TILE_BOUNDS, false);

		Degrade(LISTE<Updatable> all) {
			super(all);
		}

		@Override
		public double getBaseValue(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null && r.blueprint() instanceof ROOM_MONUMENT) {
				return r.degrader(tx, ty).isRealDegraded() ? 1 : 0; 
			}
			return 0;
		}

		@Override
		protected void update(RECTANGLE bounds, RECTANGLE area) {
			for (COORDINATE c : area) {
				if (SETT.IN_BOUNDS(c)) {
					has.set(c, false);
				}
				
			}
			
			for (COORDINATE c : bounds) {
				int tx = c.x();
				int ty = c.y();
				Room r = SETT.ROOMS().map.get(tx, ty);
				if (r != null && r.blueprint() instanceof ROOM_MONUMENT) {
					if (r.degrader(tx, ty).isRealDegraded()) {
						trace(tx, ty, area);
					}
				}
				
			}
			
		}

		private void trace(int sourceX, int sourceY, RECTANGLE area) {
			
			
			EUpdater.traces.checkInit();
			
			for (Ray r : EUpdater.rays(sourceX, sourceY, area)){
				for (int i = 0; i < r.size(); i++) {
					COORDINATE d = r.get(i);
					int dx = d.x()+sourceX;
					int dy = d.y()+sourceY;

					if (isBlocked(dx, dy))
						break;
					
					if (area.holdsPoint(dx, dy) && !isBlocked(dx, dy)) {
						has.set(dx, dy, true);

					}
				}
				
			}

		}
		
		@Override
		protected boolean has(int tx, int ty) {
			return has.is(tx, ty);
		}

		@Override
		protected void clear() {
			has.clear();
		}
		
		
	}
	

}
