package settlement.entry;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.PRISONER_TYPE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import util.data.INT_O.INT_OE;
import view.sett.IDebugPanelSett;

final class PeopleSpawner implements SAVABLE{

//	private static ArrayListShortResize queue = new ArrayListShortResize(128, ENTETIES.MAX);
//	private int currentAmount;
//	private int currentType;
//	private int currentRace;
	
	private int rspot = RND.rInt();

	private double time;
	private int ri;
	private int ti;
//	private final int[][] onTheirWay = new int[RACES.all().size()+1][HTYPES.ALL().size()+1];

	private final IM[] onTheirWay = new IM[RACES.all().size()];
	private final IM onTotal = new IM();
	
	public PeopleSpawner() {
		IDebugPanelSett.add("Spawn immigrants", new ACTION() {
			
			@Override
			public void exe() {
				add(FACTIONS.player().race(), HTYPES.SUBJECT(), 100);
			}
		});
		for (int i = 0; i < onTheirWay.length; i++) {
			onTheirWay[i] = new IM();
		}
	}
	
	@Override
	public void save(FilePutter file) {
		RACES.map().saver().save(onTheirWay, file);
		onTotal.save(file);
		file.i(rspot);
		file.i(ri);
		file.i(ti);
		file.d(time);
		
			
	}

	@Override
	public void load(FileGetter file) throws IOException {
		RACES.map().loader().load(onTheirWay, file);
		onTotal.load(file);
		rspot = file.i();
		ri = file.i();
		ti = file.i();
		time = file.d();
	}

	@Override
	public void clear() {
		for (IM is: onTheirWay)
			is.clear();
		onTotal.clear();
	}
	
	public int onTheirWay(Race race, HTYPE type) {
		IM ii = race == null ? onTotal : onTheirWay[race.index()];
		return ii.get(type);
	}
	
	public void add(Race race, HTYPE type, int amount) {
		if (amount > Short.MAX_VALUE)
			throw new RuntimeException();
		if (amount < 0)
			return;
		onTotal.inc(type, amount);
		onTheirWay[race.index()].inc(type, amount);
	}
	
	void update(double ds) {

		if (onTotal.tot() == 0)
			return;
		
		if (SETT.ENTRY().points.reachable().size() == 0)
			return;
		
		time += ds;
		int rr = RACES.all().size();
		while(time > 0 && rr-- > 0) {
			ri %= RACES.all().size();
			IM aa = onTheirWay[ri];
			if (aa.tot() > 0) {
				for (int i = 0; i < HTYPES.ALL().size(); i++) {
					ti %= HTYPES.ALL().size();
					HTYPE tt = HTYPES.ALL().get(ti);
					if (aa.get(tt) > 0) {
						COORDINATE c = SETT.ENTRY().points.randomReachable(rspot);
						if (c == null) {
							return;
						}
						spawn(c, RACES.all().get(ri), HTYPES.ALL().get(ti));
						time --;
						if (time < 0)
							return;
					}else {
						ti++;
					}	
				}
				
			}
			rspot = RND.rInt();
			ti = 0;
			ri++;
		}
		
	}
	
	private boolean spawn(COORDINATE spot, Race r, HTYPE t) {
		
		
		DIR d = DIR.get(SETT.TWIDTH/2, SETT.THEIGHT/2, spot.x(), spot.y()).next(2);
		if (!d.isOrtho())
			d = d.next((int) (1*RND.rSign()));
		
		int tx = spot.x() + d.x()*RND.rInt(6);
		int ty = spot.y() + d.y()*RND.rInt(6);
		for (int dd = 0; dd <= 6; dd++) {
			if (SETT.PATH().connectivity.is(tx, ty)) {
				
				
				Humanoid h = SETT.HUMANOIDS().create(r, tx, ty, t, CAUSE_ARRIVES.IMMIGRATED());
				init(h);;
				
				if (t == HTYPES.PRISONER()) {
					STATS.LAW().prisonerType.set(h.indu(), PRISONER_TYPE.WAR);
				}
				
				onTheirWay[r.index].inc(t, -1);
				onTotal.inc(t, -1);
				
				return true;
			}
			tx -= d.x();
			ty -= d.y();
		}
		return false;
	}
	
	private void init(Humanoid h) {
		if (h == null || !h.indu().hType().player) {
			return;
		}
		STATS.Arrive(h);

	}
	
	private static class IM implements SAVABLE, INT_OE<HTYPE>{
		
		private int tot;
		private final int [] pam = new int[HTYPES.ALL().size()];

		@Override
		public void save(FilePutter file) {
			HTYPES.MAP().saver().save(pam, file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			HTYPES.MAP().loader().load(pam, file, 0);
			tot = 0;
			for (int i : pam)
				tot += i;
		}

		@Override
		public void clear() {
			tot = 0;
			Arrays.fill(pam, 0);
		}

		@Override
		public int get(HTYPE t) {
			return pam[t.index()];
		}

		@Override
		public int min(HTYPE t) {
			return 0;
		}

		@Override
		public int max(HTYPE t) {
			return Integer.MAX_VALUE;
		}

		@Override
		public void set(HTYPE t, int i) {
			tot -= pam[t.index()];
			pam[t.index()] = i;
			tot += pam[t.index()];
		}
		
		public int tot() {
			return tot;
		}
		
	}
	
}
