package game.boosting.tmp;

import game.GAME;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.INDEXED;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class TmpBoostable<T extends INDEXED> {

	public final int startIndex;
	public final int max;
	private final TmpBoosting daddy;
	
	TmpBoostable(ArrayListGrower<TmpBoostable<?>> all, int max, TmpBoosting daddy){
		int i = 0;
		for (TmpBoostable<?> t : all) {
			i += t.max;
		}
		this.startIndex = i;
		this.max = max;
		this.daddy = daddy;
		all.add(this);
	}
	
	public Data get(T t) {
		return daddy.datas[startIndex + t.index()];
	}
	
	public void set(T t, TmpBoostSpec s, boolean set) {
		get(t).set(s, set);
	}
	
	public void toggle(T t, TmpBoostSpec s) {
		get(t).set(s, !is(t, s));
	}
	
	public boolean is(T t, TmpBoostSpec s) {
		return get(t).is(s);
	}
	
	public double add(T t, Boostable bo) {
		return get(t).add(bo);
	}
	
	public double mul(T t, Boostable bo) {
		return get(t).mul(bo);
	}
	
	public boolean any(T t) {
		return get(t).hasAny();
	}
	
	public void clear(T t) {
		get(t).clear();
	}
	
	public void hover(GBox b, T t) {
		
		for (TmpBoostSpec s : GAME.BOOST().specs()) {
			if (is(t, s)) {
				b.add(s.icon);
				b.textLL(s.name);
				b.NL();
				b.text(s.desc);
				b.NL(8);
				
				for (Boostable bo : BOOSTING.ALL()) {
					
					if (add(t, bo) != 0) {
						b.add(bo.icon);
						b.textL(bo.name);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), add(t, bo)));
						b.NL();
					}
					if (mul(t, bo) != 1) {
						b.add(bo.icon);
						b.textL(bo.name);
						b.tab(6);
						GText tt = b.text();
						tt.add('*');
						b.add(GFORMAT.f1(tt, mul(t, bo)));
						b.NL();
					}
					
				}
				
			}
		}
		
		
	}
	
}
