package game.faction.royalty;


import java.io.IOException;

import game.GameDisposable;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.NPCResource;
import game.time.TIME;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.text.D;
import world.WORLD;
import world.region.RD;
import world.region.pop.RDRace;

public final class NPCCourt extends NPCResource{

	public static final int MAX = 4;
	private final King king = new King(this);
	final ArrayList<Royalty> all = new ArrayList<>(MAX);
	public final FactionNPC faction;
	private double addT = 0;
	
	private static CharSequence ¤¤sucession = "{0} ascends the throne of {1}, succeeding the old leader, {2}.";
	
	static {
		D.ts(NPCCourt.class);
	}
	
	public NPCCourt(FactionNPC faction, LISTE<NPCResource> all){
		super(all);
		this.faction = faction;
	}
	
	public LIST<Royalty> all(){
		return all;
	}
	
	public King king() {
		return king;
	}
	

	@Override
	protected SAVABLE saver() {
		return new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				
				
				file.i(all.size());
				for (Royalty r : all)
					r.save(file);
				
				king.save(file);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				
				
				
				int k = file.i();
				all.clear();
				for (int i = 0; i < k; i++) {
					all.add(new Royalty(NPCCourt.this, file));
				}
				king.load(NPCCourt.this, file);
			}
			
			@Override
			public void clear() {
				all.clear();
			}
		};
	}
	
	@Override
	protected void update(FactionNPC faction, double seconds) {
		addT += seconds;
		if (addT > TIME.secondsPerDay()) {
			for (int i = 0; i < all.size(); i++) {
				Royalty r = all.get(i);
				STATS.POP().age.DAYS.inc(r.induvidual, 1);
			}
			
			addT -= TIME.secondsPerDay();
			if (RND.oneIn(16))
				addSuccessor();
		}
		
		
		for (int i = 0; i < all.size(); i++) {
			Royalty r = all.get(i);
			r.update(seconds);
			if (r.successionI() < 0)
				i--;
			else if (TIME.days().bitsSinceStart() > r.deathDay) {
				kill(r);
				i--;
			}
		}
	}

	public void addSuccessor() {
		if (!all.hasRoom())
			return;
		Royalty r = newSuccessor(king.roy().induvidual.race());
		int i = all.size();
		all.add(r);
		for (RoyaltyEventListener l : RoyaltyEventListener.all)
			l.change(i, r, null);
		
	}
	
	private Royalty newSuccessor(Race roy) {
		
		
		double tot = 0;
		for (RDRace r : RD.RACES().all) {
			double d = roy.pref().race(roy);
			if (r.race == roy)
				d += RD.RACES().all.size()*16 - 12*RD.RACES().all.size()*BOOSTABLES.NOBLE().TOLERANCE.get(king.roy().induvidual);
			d*= r.pop.faction().get(faction);
			tot += d;
		}
		tot *= RND.rFloat();
		for (RDRace r : RD.RACES().all) {
			double d = roy.pref().race(roy);
			if (r.race == roy)
				d += RD.RACES().all.size()*16 - 12*RD.RACES().all.size()*BOOSTABLES.NOBLE().TOLERANCE.get(king.roy().induvidual);
			d*= r.pop.faction().get(faction);
			tot -= d;
			if (tot <= 0)
				return new Royalty(this, r.race);
		}
		return new Royalty(this, roy);
	}
	
	void kill(Royalty r) {
		
		STATS.APPEARANCE().dead.indu().set(r.induvidual, 1);
		
		int si = r.successionI();
		
		CharSequence oldKing = ""+king.name;
		

		for (int i = si; i < all.size(); i++) {
			for (RoyaltyEventListener l : RoyaltyEventListener.all)
				l.change(i, all.get(i), i+1 < all.size() ? all.get(i+1) : null);
		}
		all.removeOrdered(si);
		if (si == 0) {
			
			if (all.size() == 0){
				Royalty rn = new Royalty(this, r.induvidual.race());
				all.add(rn);
				for (RoyaltyEventListener l : RoyaltyEventListener.all)
					l.change(1, null, rn);
			}
			
			king.init();
			
			CharSequence newKing = ""+king.name;
			
			Str.TMP.clear().add(¤¤sucession);
			Str.TMP.insert(0, newKing);
			Str.TMP.insert(1, faction.name);
			Str.TMP.insert(2, oldKing);
			WORLD.LOG().log(null, faction, UI.icons().s.crown, Str.TMP, faction.cx(),faction.cy());
			
			
			
		}
		
	}

	@Override
	protected void generate(RDRace race, FactionNPC faction, boolean fromScratch) {
		all.clear();
		all.add(new Royalty(this, race.race));
		while(all.hasRoom())
			addSuccessor();
		king.init();
		
		
		
	}
	
	public void init() {
		if (all.size() == 0)
			all.add(new Royalty(this, RD.RACES().all.get(0).race));
	}
	
	public void promote(Royalty roy, boolean message) {
		if (!all.contains(roy))
			throw new RuntimeException();
		int i = all.indexOf(roy);
		for (RoyaltyEventListener l : RoyaltyEventListener.all)
			l.change(1,all.get(1),roy);
		for (RoyaltyEventListener l : RoyaltyEventListener.all)
			l.change(i,roy,all.get(i));
		all.swap(1, i);
	}

	public Race race() {
		if (king.roy() == null)
			return FACTIONS.player().race();
		return king.roy().induvidual.race();
	}
	
	public static abstract class RoyaltyEventListener {
		
		private static ArrayListGrower<RoyaltyEventListener> all = new ArrayListGrower<>();
		static {
			new GameDisposable() {
				
				@Override
				protected void dispose() {
					all.clear();
				}
			};
		}
		
		protected RoyaltyEventListener() {
			all.add(this);
		}
		
		public abstract void change(int successionI, Royalty old, Royalty nn);
		
	}
	
}
