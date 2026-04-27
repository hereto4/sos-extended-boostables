package game.raiding;

import java.io.Serializable;

import game.GAME;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.Dic;

public class Raider implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final Induvidual indu;
	public final String name;
	public RaiderArmy army;
	public int raids;
	public int bounty;
	public boolean defeated = false;
	public double worth; 
	boolean hasAttacked = false;
	public final RaiderText text;
	public double secondDefeated;
	
	Raider(double wealth, double power, double quality){
		
		double ri = 0;
		for (Race r : RACES.all()) {
			ri += r.physics.raiding;
		}
		ri *= RND.rFloat();
		Race rr = RACES.playable().rnd();
		for (Race r : RACES.all()) {
			ri -= r.physics.raiding;
			if (ri <= 0) {
				rr = r;
				break;
			}
		}
		
		indu = new Induvidual(HTYPES.SOLDIER(), rr);
		Str.TMP.clear().add(indu.race().info.raiderNames[RND.rInt(indu.race().info.raiderNames.length)]);  
		Str.TMP.insert(0, STATS.APPEARANCE().nameFirst(indu));
		name = "" + Str.TMP;		
		adjust(wealth, power, quality);
		text = new RaiderText();
	}
	
	public Raider(Race race, double power) {
		indu = new Induvidual(HTYPES.SOLDIER(), race);
		Str.TMP.clear().add(indu.race().info.raiderNames[RND.rInt(indu.race().info.raiderNames.length)]);  
		Str.TMP.insert(0, STATS.APPEARANCE().nameFirst(indu));
		name = "" + Str.TMP;		
		adjust(0, power, RND.rFloat());
		text = new RaiderText();
	}
	
	public void adjust(double wealth, double power, double quality) {
		worth = wealth;
		army = new RaiderArmy(indu.race(), power, quality);
		bounty = (int) (wealth);
	}

	public void hover(GUI_BOX text) {
		GBox b = (GBox) text;
		b.title(name);
		
		b.text(Dic.¤¤Soldiers);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), army.men));
		b.NL();
		
		b.text(Dic.¤¤Power);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), army.power));
		b.NL();
	
		b.text(Dic.¤¤Currs);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), (int)worth));
		b.NL();
		
	}

	public boolean hasInterrest() {
		if (!SETT.ROOMS().BARRACKS.get(0).reqs.passes(FACTIONS.player()))
			return false;
		return GAME.raiders().util.ransomCurrent() > worth;
	}
	
	public boolean isScared() {
		return army.power < GAME.raiders().util.weakestRegionPow() || army.power < GAME.raiders().util.playerPow();
	}
	



}
