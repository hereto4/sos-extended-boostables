package settlement.path.finders;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import game.GAME;
import init.type.CAUSE_LEAVES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.components.FindableDataSingle;
import settlement.path.components.SComponent;
import settlement.path.components.finder.SCompFinder.SCompPatherFinder;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimpleTile;

public final class SFinderHumanTarget {

	
	public SFinderHumanTarget() {
		
		IDebugPanelSett.add(new PlacableSimpleTile("kill targets") {
			
			final ArrayList<Humanoid> all = new ArrayList<>(1);
			
			@Override
			public void place(int tx, int ty) {
				all.clear();
				add(all, tx, ty, true, 128, Humanoid.TARGET_MAX);
				
				for (Humanoid h : all) {
					if (!h.isRemoved())
						h.kill(true, CAUSE_LEAVES.MURDER());
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return SETT.PATH().comps.zero.get(tx, ty) != null ? null : E;
			}
		});
		
	}
	
	private FindableDataSingle ff;
	
	private SCompPatherFinder fin = new SCompPatherFinder() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return ff.get(c) > 0;
		}
	};
	
	
	public void add(ArrayList<Humanoid> res, int cx, int cy, boolean player, int distance, int targetLimit) {
		
		ff = SETT.PATH().comps.data.people(player);
		LIST<SComponent> ls = SETT.PATH().comps.pather.fill(cx, cy, fin, distance).path();
		for (SComponent c : ls) {
			
			int x1 = CLAMP.i((c.centreX() & ~(c.level().size()-1)) - 1, 0, TWIDTH);
			int x2 = CLAMP.i(x1+c.level().size()+2, 0, TWIDTH);
			int y1 = CLAMP.i((c.centreY() & ~(c.level().size()-1)) - 1, 0, THEIGHT);
			int y2 = CLAMP.i(y1+c.level().size()+2, 0, THEIGHT);
			boolean found = false;
			for (int y = y1; y < y2; y++) {
				for (int x = x1; x < x2; x++) {
					for (ENTITY e : SETT.ENTITIES().getAtTile(x, y)) {
						if (e instanceof Humanoid) {
							Humanoid h = (Humanoid) e;
							if (h.indu().hostile() != player) {
								found = true;
								if (h.targets() < targetLimit) {
									res.add(h);
									
									targetLimit --;
									if (targetLimit <= 0 || !res.hasRoom())
										return;
								}
								
							}
							
						}
					}
				}
			}
			if (!found)
				GAME.Notify(c.centreX() + " " + c.centreY() + " " + ff.name + " " + x1 + " " + y1);
			
			if (targetLimit <= 0 || !res.hasRoom())
				return;
		}
	}
	
}
