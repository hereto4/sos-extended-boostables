package settlement.path.components;

import static settlement.main.SETT.PATH;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.path.components.finder.SCompFinder.SCompPatherFinder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;

public final class FindableDataRes {

	static final LinkedList<FindableDataRes> all = new LinkedList<>();
	
	private final ArrayList<FindableData> datas;
	private final int index;
	public final CharSequence title;

	FindableDataRes(CharSequence title) {
		this.title = title;
		datas = new ArrayList<>(RESOURCES.ALL().size());
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			datas.add(new Res(RESOURCES.ALL().get(i)));
		}
		index = all.add(this);
	}

	void add(SComponent c, RESOURCE res) {
		datas.get(res.index()).add(c);
	}

	boolean remove(SComponent c, RESOURCE res) {
		return datas.get(res.index()).remove(c);
	}

	public int get(SComponent c, RESOURCE res) {
		return datas.get(res.index()).get(c);
	}
	
	public boolean overflow(SComponent c, RESOURCE res) {
		return datas.get(res.index()).overflow(c);
	}
	
	public int get(SComponent c, int res) {
		return datas.get(res).get(c);
	}

	public RBIT bits(SComponent c) {
		return c.ress[index];
	}
	
	public RBIT bits(int sx, int sy) {
		SComponent s = PATH().comps.zero.get(sx, sy);
		if (s == null)
			return RBIT.NONE;
		while(s.superComp() != null)
			s = s.superComp();
		return s.ress[index];
	}

	public boolean has(SComponent c, RBIT mask) {
		return bits(c).has(mask);
	}
	
	public final void reportPresence(int tx, int ty, RESOURCE res) {
		datas.get(res.index()).reportPresence(tx, ty);
		
	}
	
	public final void reportAbsence(int tx, int ty, RESOURCE res) {
		datas.get(res.index()).reportAbsence(tx, ty);
	}
	
	private final DIR[] dirs = new DIR[] {
		DIR.C,DIR.N,DIR.E,DIR.S,DIR.W
	};
	
	public boolean has(int startX, int startY, RBIT mask) {
		for (DIR d : dirs) {
			SComponent s = PATH().comps.zero.get(startX, startY, d);
			if (s == null)
				continue;
			while(s.superComp() != null)
				s = s.superComp();
			if(has(s, mask))
				return true;
		}
		return false;
	}
	
	public SCompPatherFinder finder(RBITImp mask) {
		fetchmask = mask;
		return finder;
	}
	
	private RBITImp fetchmask;
	private final SCompPatherFinder finder = new SCompPatherFinder() {

		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return c.ress[index].has(fetchmask);
		}
		
	};
	
	private final class Res extends FindableData {
		
		private final RESOURCE res;
		
		Res(RESOURCE res) {
			super(res.name);
			this.res = res;
		}
		
		@Override
		void add(SComponent c) {
			super.add(c);
			c.ress[index].or(res);
		}
		
		@Override
		boolean remove(SComponent c) {
			boolean ret = super.remove(c);
			if (get(c) == 0) {
				c.ress[index].clear(res);
			}
			return ret;
		}
	}

}