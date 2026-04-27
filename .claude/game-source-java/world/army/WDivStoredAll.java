package world.army;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import world.entity.army.WArmy;

public final class WDivStoredAll {

	
	private final WDivStored[] divs = new WDivStored[Config.battle().DIVISIONS_PER_ARMY];
	int amount;
	final int[] ramounts = new int [RACES.all().size()];
	private double upD;
	private int upDI;
	
	WDivStoredAll(){
		for (int i = 0; i < divs.length; i++)
			divs[i] = new WDivStored(i);
	}
	
	void save(FilePutter file) {
		for (WDivStored d : divs) {
			d.save(file);
		}
	}

	void load(FileGetter file) throws IOException {
		amount = 0;
		Arrays.fill(ramounts, 0);
		for (WDivStored d : divs) {
			d.load(file);
		}
	}
	
	WDivStored get(long data) {
		return divs[(int) (data& 0x0FFFF)];
	}
	
	public WArmy attachedArmy(Div div) {
		if (div.army() == GAME.ARMIES().enemy())
			return null;
		return divs[div.index()].army();
	}
	
	public double daysToReturn(Div div) {
		if (divs[div.index()].men() > 0)
			return Math.max(0, divs[div.index()].returnSecond()-TIME.currentSecond())*TIME.secondsPerDayI();
		return -1;
	}
	
	public WDIV get(Div div) {
		return divs[div.index()];
	}
	
	public void attach(WArmy a, Div div) {
		if (a != null) {
			for (ResSupply s : RESOURCES.SUP().ALL) {
				int am = s.amount(div.info.race(), div.menNrOf());
				if (am <= 0)
					continue;
				am = CLAMP.i(am, 0, SETT.ROOMS().STOCKPILE.tally().amountReservable.get(s.resource));
				if (am > 0) {
					s.resource.remove(am, RTYPE.ARMY_SUPPLY);
					AD.supplies().get(s).current().inc(a, am);
				}
			}
		}
		divs[div.index()].reassign(a);
		
	}
	
//	public static int needed(ResSupply s, Div div) {
//		
//		if (s.health == 0)
//			return 0;
//		return (int) s.maximum(div.info.race(), div.menNrOf());
//	}
	
	public void add(Humanoid i, Div div) {
		divs[div.index()].add(i);
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			AD.supplies().get(e).current().inc(attachedArmy(div), e.stat().indu().get(i.indu()));
		}
	}
	
	public int total() {
		return amount;
	}
	
	
	public int total(Race race) {
		return ramounts[race.index];
	}
	
	void update(double ds) {
		upD += ds*32;
		
		while(upD > 1) {
			upD -= 1;

			if (!SETT.ENTRY().points.hasAny() || SETT.ENTRY().isClosed())
				return;

			WDivStored d = divs[upDI];
			upDI++;
			upDI %= divs.length;
			
			if (!shouldReturn(d))
				continue;

			COORDINATE cret = SETT.ENTRY().points.randomReachable(upDI);

			if (cret == null) {
				upD -= (int) upD;
				continue;
			}
			

			
			Humanoid h = d.popSoldier(cret.x(), cret.y(), HTYPES.SUBJECT());

			if (h != null) {
				
				Div dd = GAME.ARMIES().player().divisions().get(d.index());
				
				
					
				
				if (dd.menNrOf() < Config.battle().MEN_PER_DIVISION)
					h.setDivision(dd);
			}
			
				
			
		}		
	}

	private boolean shouldReturn(WDivStored d) {
		
		if (d.men() == 0)
			return false;
		
		if (d.army() != null) {
			if (d.men() > d.menTarget())
				return d.returnSecond() < TIME.currentSecond();
			return false;
		}
		
		return d.returnSecond() < TIME.currentSecond();
		
	}
}
