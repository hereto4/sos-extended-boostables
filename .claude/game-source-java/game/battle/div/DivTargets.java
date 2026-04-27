package game.battle.div;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;

public final class DivTargets {

	private static final ArrayList<Humanoid> list = new ArrayList<>(64);
	private float now = -10;
	private int[] targets = new int[64];
	private byte ti;
	private byte tl;
	private final short di;
	
	DivTargets(Div div) {
		this.di = div.index();
		saver().clear();
	}
	
	public Humanoid getNextTarget() {
		double t = TIME.currentSecond();
		
		
		if (TIME.currentSecond() < now)
			return null;
		
		while(ti < tl) {
			Humanoid h = validateTarget(targets[ti]);
			ti++;
			if (h != null)
				return h;
		}
		
		ti = 0;
		tl = 0;
		
		if (div().deployed() == 0) {
			now = (float) (t+10);
			return null;
		}
		
		COORDINATE c = div().position().centreTile();
		
		if (c == null)
			return null;
		
		int x = c.x();
		int y = c.y();
		if (!SETT.IN_BOUNDS(x, y)) {
			now = (float) (t+10);
			return null;
		}
		
		list.clearSloppy();
		SETT.PATH().finders().target.add(list, x, y, !player(), 128, 64);
	
		
		
		for (Humanoid a : list) {
			targets[tl++] = a.id();
		}
		
		if (tl == 0) {
			now = (float) (t+10);
			return null;
		}
		ti++;
		return validateTarget(targets[ti-1]);
		
	}
	
	private boolean player() {
		return GAME.ARMIES().division(di).army() == GAME.ARMIES().player();
	}
	
	private Div div() {
		return GAME.ARMIES().division(di);
	}
	
	public Humanoid validateTarget(int pointer) {
		ENTITY e = SETT.ENTITIES().getByID(pointer);
		if (e == null || !(e instanceof Humanoid))
			return null;
		Humanoid a = (Humanoid) e;
		if (player() == a.indu().hostile()) {
			return a;
		}
		
		return null;
	}
	
	
	
	
	static DivTargets s;
	static final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.f(s.now);
			file.is(s.targets);
			file.b(s.ti);
			file.b(s.tl);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			s.now = file.f();
			file.is(s.targets);
			s.ti = file.b();
			s.tl = file.b();
		}
		
		@Override
		public void clear() {
			s.ti = 0;
			s.tl = 0;
			s.now = 0;
		}
	};
	
	SAVABLE saver() {
		s = this;
		return saver;
	}
	
}
