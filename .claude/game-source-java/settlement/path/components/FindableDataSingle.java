package settlement.path.components;

import static settlement.main.SETT.PATH;

import settlement.path.components.finder.SCompFinder.SCompPatherFinder;
import snake2d.util.sets.LinkedList;


public final class FindableDataSingle extends FindableData implements SCompPatherFinder{
	
	static final LinkedList<FindableDataSingle> all = new LinkedList<>();
	
	FindableDataSingle(CharSequence name){
		super(name);
		all.add(this);
	}

	@Override
	public boolean isInComponent(SComponent c, double distance) {
		return get(c) > 0;
	}
	
	public boolean has(int startX, int startY) {
		SComponent s = PATH().comps.zero.get(startX, startY);
		if (s == null)
			return false;
		while(s.superComp() != null)
			s = s.superComp();
		return get(s) > 0;
	}
	
	public boolean has(SComponent s) {
		if (s == null)
			return false;
		while(s.superComp() != null)
			s = s.superComp();
		return get(s) > 0;
	}

}