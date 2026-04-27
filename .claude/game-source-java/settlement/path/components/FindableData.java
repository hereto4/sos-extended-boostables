package settlement.path.components;

import static settlement.main.SETT.PATH;

import game.GAME;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.GUTIL;
import util.data.DataOSimple;
import util.data.INT_O;


class FindableData implements INT_O<SComponent>{
	
	static LinkedList<FindableData> all = new LinkedList<>();
	public final CharSequence name;
	final INT_OE<SComponent> data;
	final INT_OE<SComponent> overflow;
	
	static DataOSimple<SComponent> datao;

	FindableData(CharSequence name){
		this.name = name;
		this.data = datao.new DataByte();
		this.overflow = datao.new DataBit();
		all.add(this);
	}
	
	@Override
	public int min(SComponent c) {
		return 0;
	}
	
	@Override
	public int max(SComponent c) {
		return 15;
	}
	
	@Override
	public int get(SComponent c) {
		return data.get(c);
	}
	
	public boolean overflow(SComponent c) {
		return overflow.isMax(c);
	}
	
	void add(SComponent c) {
		if (data.isMax(c)) {
			overflow.set(c, 1);
		}else {
			data.inc(c, 1);
		}
	}
	
	boolean remove(SComponent c) {
		
		int a = data.get(c);
		
		if (a == 0) {
			if (overflow.get(c) == 0) {
				if (c.level().level() > 0) {
					GUTIL.filler().init(this);
					GUTIL.filler().fill(c.centreX(), c.centreY());
					
					SComponentLevel l = SETT.PATH().comps.all.get(c.level().level()-1);

					while(GUTIL.filler().hasMore()) {
						COORDINATE coo = GUTIL.filler().poll();
						SComponent s = l.get(coo);
						
						SComponentEdge e = s.edgefirst();
						while(e != null) {
							if (e.to().superComp() == c) {
								GUTIL.filler().fill(e.to().centreX(), e.to().centreY());
							}
							e = e.next();
						}
						
					}
					
					GUTIL.filler().done();
				}
				GAME.Notify(name + " " + c.centreX() + " " + c.centreY() + " " + c.level().level());
			}
				
			return true;
		}
		data.inc(c, -1);
		if (a == 0) {
			if (overflow.get(c) == 1)
				return true;
		}
		return false;
		
	}
	
	private static final LIST<DIR> dirs = new ArrayList<>(DIR.ORTHO).join(DIR.C);
	
	private static final void uncheck(int tx, int ty) {
		for (DIR d : dirs) {
			SComponent n = PATH().comps.zero.get(tx, ty, d);
			if (n != null) {
				n.checked = false;
			}
		}
	}
	
	public final void reportPresence(int tx, int ty) {
		uncheck(tx, ty);
		
		for (DIR d : dirs) {
			SComponent n = PATH().comps.zero.get(tx, ty, d);
			if (n != null && !n.checked) {
				n.checked = true;
				add(n);
				
				
				while(n.superComp() != null && get(n) == 1) {
					n = n.superComp();
					add(n);
				}
			}
		}
	
		
	}
	
	public final void reportAbsence(int tx, int ty) {

		uncheck(tx, ty);
		boolean up = false;
		for (DIR d : dirs) {
			SComponent n = PATH().comps.zero.get(tx, ty, d);
			if (n != null && !n.checked) {
				
				n.checked = true;
				int old = get(n);
				up |= remove(n);
				while(n.superComp() != null && old == 1) {
					n = n.superComp();
					old = get(n);
					up |= remove(n);
					
				}
			}
		}
		
		if (up) {
			PATH().comps.updateService(tx, ty);
		}
	}

}