package settlement.maintenance;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.path.AvailabilityListener;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.map.AbsGrid;

final class MConsumption{

	
	private final AbsGrid grid = new AbsGrid(SETT.TWIDTH, SETT.THEIGHT, 32);
	private final QData[] datas = new QData[grid.all.size()];
	private final long[] ress = new long[RESOURCES.ALL().size()];
	private final MAINTENANCE m;
	
	private int upI = 0;
	
	private final int DD = 1024*16;
	private final double DDI = 1.0/DD;
	
	MConsumption(MAINTENANCE m) {
		this.m = m;
		for (int i = 0; i < datas.length; i++)
			datas[i] = new QData();
		
		new AvailabilityListener() {
			
			@Override
			protected void changed(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange) {
				if (m.disabled.is(tx, ty))
					return;
				int in = grid.map.get(tx, ty).index;
				datas[in].changed = true;
			}
		};
		
	}
	
	void init(){
		for (int i = 0; i < grid.all.size(); i++) {
			update(i);
		}
	}
	
	void update() {
		
		if (upI >= grid.all.size())
			upI = 0;
		
		if (datas[upI].changed) {
			update(upI);
		}
		upI ++;
		
	}
	
	void change(int tx, int ty) {
		int in = grid.map.get(tx, ty).index;
		datas[in].changed = true;
	}
	
	private void update(int i) {
		QData d = datas[i];
		d.changed = false;
		for (int r = 0; r < RESOURCES.ALL().size(); r++) {
			ress[r] -= Math.ceil(d.amounts[r]*DD);
			d.amounts[r] = 0;
		}
		for (COORDINATE c : grid.get(i)) {
			if (m.disabled.is(c))
				continue;
			for (MType t : m.types) {
				for (int r = 1; r < 5; r++) {
					double am = t.resRate(c.x(), c.y(), r);
					if (am > 0) {
						d.amounts[t.res(c.x(), c.y(), r).index()] += am;
					}
				}
			}
		}
		for (int r = 0; r < RESOURCES.ALL().size(); r++) {
			ress[r] += Math.ceil(d.amounts[r]*DD);
		}
	}
	
	
	private static class QData {
		
		private double[] amounts = new double[RESOURCES.ALL().size()];
		private boolean changed = false;
		
	}


	public double get(RESOURCE res) {
		return ress[res.index()]*DDI;
	}
	
}
