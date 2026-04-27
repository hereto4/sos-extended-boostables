package world.army.ai;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitsmap1D;
import util.updating.IUpdater;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD;

final class War extends IUpdater{

	private final Recruiter recruiter = new Recruiter();
	private final Defender defender = new Defender();
	private final Attacker attacker = new Attacker();
	private final Chiller chiller = new Chiller();
	
	
	private final Bitsmap1D hasSentMessage = new Bitsmap1D(0, 4, FACTIONS.MAX());
	
	private final ArrayList<WArmy> armies = new ArrayList<>(32);
	
	public War() {
		super(FACTIONS.MAX(), TIME.secondsPerDay()/2);
	}

	@Override
	protected void update(int i, double timeSinceLast) {

		Faction f = FACTIONS.getByIndex(i);
		if (!f.isActive())
			return;
		if (f == FACTIONS.player())
			return;
		recruiter.recruit((FactionNPC) f);
		planForWar(f);
		
	}
	
	@Override
	public void save(FilePutter file) {
		hasSentMessage.save(file);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		hasSentMessage.load(file);
		super.load(file);
	}
	
	@Override
	public void clear() {
		hasSentMessage.clear();
		super.clear();
	}
	
	void planForWar(Faction f) {
		
		if (f == null || !f.isActive())
			return;
		if (f == FACTIONS.player())
			return;
		armies.clearSloppy();
		
		for (int ai = 0; ai < f.armies().all().size(); ai++) {
			WArmy a = f.armies().all().get(ai);
			if (f != FACTIONS.player() && a.raiding() && RD.DEVASTATION().current.get(a.region()) > 0.9)
				a.stop();
			if (AD.men(null).get(a) > 0 && armies.hasRoom())
				armies.add(a);
		}
		
		
		for (WArmy a : armies) {
			if (a.intercepting() != null && !DIP.WAR().is(a.intercepting().faction(), f)) {
				a.stop();
			}else if (a.state() == WArmyState.besieging) {
				Region reg = a.besieging();
				
				if (reg == null || !DIP.WAR().is(reg.faction(), f)) {
					//LOG.ln(reg + " " + reg.faction() + " " + FACTIONS.DIP().war.is(reg.faction(), f));
					a.stop();
				}
				armies.remove(a);
			}
			
			if (false) {
				//it can be in enemy of my enemy territory here, and can path back.
			}
			
			if (a.state() == WArmyState.fortified || a.state() == WArmyState.fortifying) {
				if (a.region() == null || (a.region().faction() != f && !DIP.WAR().is(f, a.region().faction()))) {
					allyFaction = f;
					RegDist d = WORLD.PATH().regFinder.single(a.ctx(), a.cty(), Treaty.FACTION, ally);
					armies.remove(a);
					if (d != null) {
						COORDINATE c = WORLD.PATH().rnd(d.reg);
						a.setDestination(c.x(), c.y());
						continue;
					}
					d = WORLD.PATH().regFinder.single(a.ctx(), a.cty(), Treaty.DUMMY, ally);
					
					if (d != null) {
						COORDINATE c = WORLD.PATH().rnd(d.reg);
						a.teleport(c.x(), c.y());
						continue;
					}
					a.disband();
					
				}
			}
			
		}
		
		
		
		if (DIP.WAR().all(f).size() == 0) {
			chiller.chill(f, armies);
			return;
		}

		if (logging)
			log(f, "has " + armies.size() + " armies to use");
		
		defender.defend(f, armies);
		if (logging)
			log(f, "has " + armies.size() + " armies for offence");
		
		attacker.attack(f, armies);
		if (logging)
			log(f, "has " + armies.size() + " armies without orders");
	}
	
	private Faction allyFaction;
	private WRegSel ally = new WRegSel() {

		@Override
		public boolean is(Region t) {
			return t.faction() == allyFaction;
		}
		
	};

	public void init(Faction f) {
		update(f.index(), 0);
		
		
		for (WArmy a : f.armies().all()) {
			for (int i = 0; i < a.divs().size(); i++) {
				a.divs().get(i).menSet(a.divs().get(i).menTarget());
			}
			
			for (ADSupply s : AD.supplies().all) {
				s.current().set(a, s.targetAmount(a));
			}
			
		}
		
	}
	
	static boolean logging = false;
	
	public static void log(WArmy d, String message) {
		String s = d.faction().toString() + " " + d.ctx() + " " + d.cty() + ": " + message;
		LOG.ln(s);
	}
	
	public static void log(Faction f, String message) {
		String s = f.toString() + ": " + message;
		LOG.ln(s);
	}

}
