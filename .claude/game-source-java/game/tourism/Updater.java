package game.tourism;

import java.io.IOException;

import game.time.TIME;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsNeeds.StatNeedNormal;
import settlement.stats.service.StatService;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.updating.IUpdater;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

final class Updater extends IUpdater{

	
	private final LIST<TRace> races;
	
	private int mI = 0;
	private static CharSequence ¤¤MessageFirst = "¤Our city is now impressive enough to attract our first tourist. A {RACE} pilgrim by the name of {NAME} has arrived. This particular tourist is looking for inn to stay, and wants to visit: {ATTRACTION} and {SERVICE}.";
	private final Coo entry = new Coo(-1, -1);
	
	static {
		D.ts(Updater.class);
	}
	
	Updater(){
		super(TOURISM.AMOUNT, TIME.secondsPerDay()*16);
		double tot = 0;
		
		for (Race r : RACES.all()) {
			tot += r.tourism().occurence;
		}
		
		LinkedList<TRace> li = new LinkedList<>();
		for (Race r : RACES.all()) {
			if (r.tourism().occurence > 0)
				li.add(new TRace(r, r.tourism().occurence/tot));
		}
		races = new ArrayList<>(li);
		
		IDebugPanelSett.add("TOURIST_SPAWN", new ACTION() {
			
			@Override
			public void exe() {
				Race r = getRace();
				Humanoid h = pspawn(r, r.tourism().attractions.rnd());
				if (h == null)
					LOG.ln("nope");
				else
					mess(h);
					
			}
		});
		
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(mI);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		mI = file.i();
		super.load(file);
	}
	
	
	private static class TRace {
		
		public final Race race;
		public final double occ;
		
		private TRace(Race race, double occ) {
			this.race = race;
			this.occ = occ;
		}
		
	}

	@Override
	protected void update(int index, double timeSinceLast) {
		
		double c = (STATS.POP().POP.data().get(null)-500)/500.0;
		
		if (c < RND.rFloat())
			return;
		
		if (0.25 + TOURISM.score() < RND.rFloat()*1.25)
			return;
		
		if (!canAttract())
			return;
		
		spawn();
		
			
		
	}
	
	public double chance() {
		
		double chance = 0;
		for (TRace r : races) {
			if (!TOURISM.permit(r.race))
				continue;
			double att = 0;
			for (RoomBlueprintIns<?> p : r.race.tourism().attractions) {
				att += p.employment().employed();
			}
			chance += r.occ*(CLAMP.d(att/TOURISM.MAX_EMPLOYEES, 0, 1));
		}
		double c = (STATS.POP().POP.data().get(null)-500)/500.0;
		
		return chance*CLAMP.d(c, 0, 1);
	}
	
	private Race getRace() {
		double d = RND.rFloat()-0.05;
		for (TRace r : races) {
			d -= r.occ;
			if (d <= 0) {
				return r.race;
			}
		}
		return null;
	}
	
	private RoomBlueprintIns<?> getAttraction(Race race) {
		int em = 0;
		int most = 0;
		RoomBlueprintIns<?> best = null;
		int tot = 0;
		LIST<RoomBlueprintIns<?>> li = race.tourism().attractions;
		
		for (int bi = 0; bi < li.size(); bi++) {
			int e = li.get(bi).employment().employed();
			tot += e;
			if (e > 0) {
				em += e;
				if (e > most) {
					most = e;
					best = li.get(bi);
				}
			}
		}
		
		if (most <= 0)
			return null;
		
		if (tot < TOURISM.MAX_EMPLOYEES*RND.rFloat())
			return null;
		
		if((em-most)/race.tourism().attractions.size() > RND.rInt(most)) {
			int other = (int) (RND.rFloat()*(em-most));
			for (int bi = 0; bi < li.size(); bi++) {
				int e = li.get(bi).employment().employed();
				if (e > 0) {
					other -= e;
					if (other <= 0)
						return li.get(bi);
				}
			}
		}
		return best;
	}
	
	private boolean canAttract() {
		if (SETT.ENTRY().isClosed())
			return false;
		COORDINATE c = SETT.ENTRY().points.randomReachable();
		if (c == null)
			return false;
		entry.set(c);
		return true;
	}
	
	private Humanoid spawn() {
		
		
		Race r = getRace();
		if (!TOURISM.permit(r))
			return null;
			
		RoomBlueprintIns<?> a = getAttraction(r);
		

		if (a != null) {
			Humanoid h = pspawn(r, a);
			if (h != null && mI < 2) {
				mI = 2;
				
				if (h != null) {
					mess(h);
				}
			}
			return h;
			
			
		}
		return null;
		
	}
	
	private void mess(Humanoid h) {
		Str t = Str.TMP.clear().add(¤¤MessageFirst);
		t.insert("RACE", h.race().info.namePosessive);
		t.insert("NAME", STATS.APPEARANCE().name(h.indu()));
		t.insert("ATTRACTION", attraction(h.indu()).info.name);
		t.insert("SERVICE", TOURISM.service(h.indu()).name);
		
		if (new MessageText(HTYPES.TOURIST().names, t).send()) {
			
			
		}
	}
	
	
	private Humanoid pspawn(Race race, RoomBlueprintIns<?> blue) {
		
		if (!canAttract())
			return null;
		
		
		
		Humanoid h = new Humanoid(entry.x()*C.TILE_SIZE+C.TILE_SIZEH, entry.y()*C.TILE_SIZE+C.TILE_SIZEH, race, HTYPES.TOURIST(), null);
		
		if (h.isRemoved())
			return null;
		
		STATS.WORK().profession.set(h.indu(), blue);
		StatService s = TOURISM.service(h.indu());
		if (s != null) {
			s.clearAccess(h.indu());
		}
		
		for (StatNeedNormal n : STATS.NEEDS().SNEEDS) {
			n.stat().indu().setD(h.indu(), 0.5 + RND.rFloat()*0.5);
		}
		
		TOURISM.self.history.inc(1);

		return h;
		
		
	}
	
	public static RoomBlueprintIns<?> attraction(Induvidual indu) {
		RoomBlueprintImp r = STATS.WORK().profession.get(indu);
		if (r == null || !(r instanceof RoomBlueprintIns<?>))
			return indu.race().tourism().getAttraction(0);
		return (RoomBlueprintIns<?>) r;
	}
	

	
}
