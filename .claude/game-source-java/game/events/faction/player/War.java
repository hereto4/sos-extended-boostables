package game.events.faction.player;

import game.events.faction.player.EventDiplomacy.EData;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.deal.Deal;
import game.faction.diplomacy.deal.DealDrawfter;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.diplomacy.UIDipMess;
import view.ui.diplomacy.UIDipMessDeal;
import world.WORLD;
import world.army.AD;
import world.army.ADDiv;
import world.entity.army.WArmy;
import world.region.RD;

class War {

	private static CharSequence ¤¤WarByProxy = "Proxy war!";
	private static CharSequence ¤¤WarByProxyD = "My lord, even in distant places they manage to hate our freedom and way of life. They can not reach us directly, but they have offered their aid to our enemies, bolstering their ranks!.";
	private static CharSequence ¤¤WarByProxyD2 = "Our agents reveal that our enemies have managed to increase their armies by {0} soldiers, and were sponsored by the following factions: ";
	
	private static CharSequence ¤¤War = "War!";
	private static CharSequence ¤¤Crusade = "World War!";
	private static CharSequence ¤¤WarD = "The enemy has shown itself. Let us muster and fight!";
	private static CharSequence ¤¤CrusadeD = "This faction has in secret conspired against you and have convinced other factions to join in on war against you. Now we must defend our freedom and way of life. To arms!";
	private static CharSequence ¤¤CrusadeD2 = "The following Factions have joined their cause:";
	private static CharSequence ¤¤Demand = "Demand";
	
	static {
		D.ts(War.class);
	}
	
	private ArrayList<FactionNPC> team = new ArrayList<FactionNPC>(FACTIONS.MAX());
	private ArrayList<FactionNPC> sponsors = new ArrayList<FactionNPC>(FACTIONS.MAX());
	private ArrayList<FactionNPC> teamNew = new ArrayList<FactionNPC>(FACTIONS.MAX());
	private ArrayList<Faction> player = new ArrayList<Faction>(FACTIONS.MAX());
	
	
	
	void update(EData[] data) {
		if (SETT.INVADOR().invading())
			return;
		if (DIP.overlord(FACTIONS.player()) != null)
			return;
		
		if (false) {
			//vassals here are strange. Should they have their own clause?
			//yes, they should. Vassals should not threaten you though, they should rise up. You can remove this thing in Vassal.java
			//threatening the player without attacking is pretty nonsensical
			//allies can attack the player here?
		}
		
		double hv = 0;
		team.clearSloppy();
		teamNew.clearSloppy();
		player.clearSloppy();
		sponsors.clearSloppy();
		player.add(FACTIONS.player());
		
		for (FactionNPC f : FACTIONS.NPCs()) {
			
			if (ROPINIONS.peaceValue(f) > 0.5)
				data[f.index()].warMess = false;
			
			if (validEnemy(f)) {
				team.add(f);
				if (!DIP.WAR().is(f))
					teamNew.add(f);
				continue;
			}
			
			if (DIP.get(f).ally) {
				player.add(f);
			}else if (proxy(f)) {
				sponsors.add(f);
			}
		}
		
		double off = DIP.WAR().offenseValue(team, player);
		
		if (teamNew.size() == 0) {
			if (sponsors.size() > 0 && off > 0) {
				proxy();
			}
			return;
		}
		
		FactionNPC hatefullest = null;
		for (FactionNPC f : teamNew) {
			double h = -ROPINIONS.peaceValue(f);
			if (h > hv) {
				hv = h;
				hatefullest = f;
			}
		}
		
		if (!data[hatefullest.index()].warMess) {
			data[hatefullest.index()].warMess = true;
			warn(hatefullest);
			return;
		}
		
		if (off <= 0) {
			return;
		}
		
		for (Faction f : teamNew)
			DIP.WAR().set(f, FACTIONS.player());
		if (teamNew.size() > 1)
			new Mess2(teamNew).send();
		else if (teamNew.size() == 1)
			new Mess1(teamNew.get(0)).send();
		
		
		
		//proxy();
		
		
		return;
	}
	
	
	private boolean proxy(FactionNPC f) {
		if (DIP.NEUTRAL().is(f) && ROPINIONS.current(f) < 0 && AD.power().get(f) > 500 && !RD.DIST().factionHasRegionBorderingPlayer(f)) {
			if (RND.rFloat() * -ROPINIONS.current(f)*0.1 > 1.0)
				return true;
		}
		return false;
	}
	
	private void proxy() {
		
		if (sponsors.size() == 0)
			return;
		
		teamNew.clearSloppy();
		
		for (FactionNPC f : FACTIONS.NPCs()) {
			if (RD.DIST().factionHasRegionBorderingPlayer(f) && DIP.WAR().is(f) && f.armies().all().size() > 0) {
				teamNew.add(f);
			}
			
		}
		if (teamNew.size() == 0)
			return;
		
		team.clearSloppy();
		
		int am = 0;
		
		FactionNPC f = sponsors.rnd();
		
		int a = proxyMove(f, teamNew);
		if (a > 0) {
			am += a;
			team.add(f);
		}
		
		if (am > 0) {
			
			new MessProxy(team, am).send();
		}
		
	}
	
	private int proxyMove(FactionNPC fromFaction, LIST<FactionNPC> toFactions) {

		for (int ai = 0; ai < fromFaction.armies().all().size(); ai++) {
			WArmy a = fromFaction.armies().all().get(ai);
			if (AD.men(null).get(a) > 50) {
				return proxyMove(a, toFactions);
				
			}
		}
		return 0;
	}
	
	private int proxyMove(WArmy fromArmy, LIST<FactionNPC> toAllies) {

		FactionNPC toFaction = toAllies.rnd();
		
		int ii = RND.rInt(toFaction.armies().all().size());
		int am = 0;
		for (int ai = 0; ai < toFaction.armies().all().size(); ai++) {
			
			
			WArmy toArmy = toFaction.armies().all().getC(ii+ai);
			am += proxyMove(fromArmy, toArmy);
			
		}
		
		
		
		if (fromArmy.divs().size() > 0) {
			COORDINATE c = WORLD.PATH().rnd(toFaction.capitolRegion());
			WArmy to = WORLD.ENTITIES().armies.create(c.x(), c.y(), toFaction);
			if (to != null)
				am += proxyMove(fromArmy, to);
		}
		return am;
	}
	
	private int proxyMove(WArmy fromArmy, WArmy toArmy) {
		int am = 0;
		while (fromArmy.divs().size() > 0 && toArmy.divs().canAdd()) {
			
			ADDiv toMove = fromArmy.divs().get(fromArmy.divs().size()-1);
			
			am += toMove.men();
			toMove.reassign(toArmy);
		}
		return am;
	}
	
	private boolean validEnemy(FactionNPC f) {
		
		if (ROPINIONS.peaceValue(f) > 0)
			return false;
		
		if (DIP.secondSinceStance(f) < TIME.secondsPerDay()/2)
			return false;
		
		if (RD.DIST().factionCanAttackPlayerAllies(f))
			return true;
		for (int ai = 0; ai < FACTIONS.player().armies().all().size(); ai++) {
			WArmy a = FACTIONS.player().armies().all().get(ai);
			if (a.region() != null && a.region().faction() == f)
				return true;
		}
		return false;
	}
	
	private void warn(FactionNPC f) {
		Deal d = DIP.TMP();
		d.setFactionAndClear(f);
		DealDrawfter.draft(d, -d.player.selfWorth()*(0.2 + RND.rFloat()*0.2), true, true);
		
		if (d.hasDeal()) {
			double v = ROPINIONS.GIFTS().getGenerosityNeededForPeace(f);
			new UIDipMessDeal(¤¤Demand, f.king().induvidual.race().kingMessage().DEMAND.get(f), d, v, -0.5).send();
		}
	}
	
	private static class Mess1 extends UIDipMess {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Mess1(FactionNPC f) {
			super(¤¤War, ¤¤WarD, "", f);
			
		}
		
	}
	
	private static class Mess2 extends UIDipMess {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int[] lls;
		
		public Mess2(LIST<FactionNPC> ffs) {
			super(¤¤Crusade, ¤¤CrusadeD, ¤¤CrusadeD2, ffs.get(0));
			lls = new int[ffs.size()];
			int i = 0;
			for (Faction fa : ffs) {
				lls[i++] = fa.index();
			}
		}
		
		
		@Override
		protected void make(GuiSection section) {
			super.make(section);
			GuiSection ss = new GuiSection();
			
			for (int fi : lls) {
				Faction f = FACTIONS.getByIndex(fi);
				
				ss.addRightC(8, new HOVERABLE.HoverableAbs(Icon.HUGE) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						f.banner().HUGE.render(r, body);
						
					}
				}.hoverInfoSet(f.name));
				
			}
			
			section.addRelBody(8, DIR.S, ss);
		}
		
	}
	
	private static class MessProxy extends UIDipMess {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int[] lls;
		
		public MessProxy(LIST<FactionNPC> ffs, int am) {
			super(¤¤WarByProxy, ¤¤WarByProxyD, Str.TMP.clear().add(¤¤WarByProxyD2).insert(0, am), ffs.get(0));
			lls = new int[ffs.size()];
			int i = 0;
			for (Faction fa : ffs) {
				lls[i++] = fa.index();
			}
		}
		
		
		@Override
		protected void make(GuiSection section) {
			super.make(section);
			GuiSection ss = new GuiSection();
			
			int max = 0;
			
			for (int fi : lls) {
				Faction f = FACTIONS.getByIndex(fi);
				
				ss.addRightC(8, new HOVERABLE.HoverableAbs(Icon.HUGE) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						f.banner().HUGE.render(r, body);
						
					}
				}.hoverInfoSet(f.name));
				if (max -- < 0)
					break;
			}
			
			section.addRelBody(8, DIR.S, ss);
		}
		
	}
	
}
