package world.battle;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.trade.ITYPE;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.HTYPES;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import util.GUTIL;
import util.text.D;
import view.ui.message.MessageText;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.battle.Side.SideUnit;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.map.regions.centre.WCentre;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

class Util {



	Util(){
		
	}
	
	public static boolean allies(Faction a, Faction b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		return DIP.get(a, b).ally;
	}
	
	public static boolean enemies(Faction a, Faction b) {
		return DIP.WAR().is(a, b);
	}
	
	private final Pair res = new Pair();
	
	public Pair fill(Faction a, Faction b, int cx, int cy) {
		
		res.a.clearSloppy();
		res.b.clearSloppy();

		GUTIL.flooder().init(Util.class);
		GUTIL.flooder().pushSloppy(cx, cy, 0);
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			
			if (t.getValue() > WArmy.reinforceTiles)
				break;
			
			for (WArmy ar : WORLD.ENTITIES().armies.fillTile(t.x(), t.y())) {
				if (valid(ar) == null)
					continue;
				if (ar.ctx() != t.x() || ar.cty() != t.y())
					continue;
				
				if (allies(a, ar.faction()) && enemies(b, ar.faction())) {
					res.a.add(ar);
				}else if (allies(b, ar.faction()) && enemies(a, ar.faction()))
					res.b.add(ar);
			}

			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.PATH().map.can(t, d))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		GUTIL.flooder().done();
		return res;
	}
	
	private Rec fillBounds = new Rec(WCentre.TILE_DIM*2);
	
	public WArmy getBesieger(Region reg) {
		if (!reg.active())
			return null;
		if (!reg.besieged())
			return null;
		fillBounds.moveC(reg.cx(), reg.cy());
		for (WArmy a : WORLD.ENTITIES().armies.fillTiles(fillBounds)) {
			if (valid(a) != null && a.faction() == FACTIONS.player() && a.besieging(reg)) {
				return a;
			}
		}
		for (WArmy a : WORLD.ENTITIES().armies.fillTiles(fillBounds)) {
			if (valid(a) != null && a.besieging(reg)) {
				return a;
			}
		}
		return null;
	}

	public static WArmy valid(WArmy a) {
		if (a == null)
			return null;
		if (AD.men(null).get(a) <= 0)
			return null;
		return a;
	}

	public static COORDINATE retTile(WArmy a) {
		
		if (a.faction() == null)
			return null;
		
		GUTIL.flooder().init(Util.class);
		GUTIL.flooder().pushSloppy(a.ctx(), a.cty(), 0);
		
		double ap = AD.power().get(a);
		double pow = enemyPower(a.faction(), a.ctx(), a.cty());
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (t.getValue() > 8)
				break;
			
			double p = enemyPower(a.faction(), t.x(), t.y());
			
			if (p > pow)
				continue;
			
			if (p < ap && (a.ctx() != t.x() || a.cty() != t.y())) {
				GUTIL.flooder().done();
				return t;
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.PATH().map.can(t, d))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		GUTIL.flooder().done();
		return null;
	}
	
	public static double enemyPower(Faction f, int tx, int ty) {
		
		double pow = 0;
		Region reg = WORLD.REGIONS().map.get(tx, ty);
		if (reg != null && Util.enemies(f, reg.faction()))
			pow += RD.MILITARY().power.getD(reg);
		
		
		if (WORLD.PATH().map.is.is(tx, ty)) {
			
			for (int di = 0; di < DIR.ALLC.size(); di++) {
				DIR d = DIR.ALLC.get(di);
				if (d == DIR.C || WORLD.PATH().map.can(tx, ty, d)) {
					
					int dx = tx + d.x();
					int dy = ty + d.y();
					
					for (WArmy a : WORLD.ENTITIES().armies.fillTile(dx, dy)) {
						if (a.ctx() == dx && a.cty() == dy) {
							if (Util.enemies(f, a.faction())) {
								pow += AD.power().get(a);
							}
						}
					}
				}
			}
		}
		
		return pow;
		
	}
	
	
	static class Pair {
		
		public final ArrayList<WArmy> a = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
		public final ArrayList<WArmy> b = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	}

	private static CharSequence ¤¤lost = "Settlement lost!";
	private static CharSequence ¤¤lostC = "Region of {0} has fallen to our enemies.";

	private static CharSequence ¤¤factionMove = "Capital Relocated";
	private static CharSequence ¤¤factionMoveD = "The faction of {0} has moved its capital. Its people still resist.";

	static {
		D.ts(Util.class);
	}
	
	public static void conquer(Side side, double devistation, double death, Region reg, Faction newFaction) {
		

		Faction fAttacker = side.us.get(0).faction();
		Faction fDefender = reg.faction();
		
		if (fDefender == FACTIONS.player()) {
			Str.TMP.clear().add(¤¤lostC).insert(0, reg.info.name());
			new MessageText(¤¤lost).paragraph(Str.TMP).send();
		}else if (fDefender != null && fDefender.capitolRegion() == reg) {
			Region newCapitol = reg;
			int ri = Rnd.i(reg.faction().realm().all().size());
			for (int i = 0; i < reg.faction().realm().all().size(); i++) {
				Region r = reg.faction().realm().all().get((i+ri)%reg.faction().realm().all().size());
				if (r != newCapitol) {
					newCapitol = r;
					break;
				}
			}
			
			double pow = side.us.get(0).power();
			if (fAttacker != null)
				pow = AD.power().get(fAttacker)/2;
			
			
			if (newCapitol == reg || AD.power().get(fDefender) <  pow && Rnd.oneIn(reg.faction().realm().all().size())){
				FACTIONS.remove((FactionNPC) fDefender, true);
			}else {
				
				Str.TMP.clear().add(¤¤factionMoveD).insert(0, Faction.name(fDefender));
				WORLD.LOG().log(null, fDefender, UI.icons().s.arrow_right, Str.TMP, newCapitol.cx(), newCapitol.cy());
				new MessageText(¤¤factionMove).paragraph(Str.TMP).send();
				
				newCapitol.setCapitol();
			}
			
			
		}
		
		double dev = RD.DEVASTATION().current.getD(reg);
		dev += devistation;
		dev = CLAMP.d(dev, 0, 1);
		RD.DEVASTATION().current.setD(reg, dev);
		
		
		if (newFaction == FACTIONS.player() && RD.OWNER().prevOwner(reg) != FACTIONS.player()) {
			for (RDBuilding bu : RD.BUILDINGS().all) {
				
				
				if (bu.level.get(reg) > 0) {
					bu.level.set(reg, 0);
					
				}
			}
		}
		
		for (RDBuilding bu : RD.BUILDINGS().all) {
			
			
			if (bu.level.get(reg) > 0) {
				double ll = bu.level.get(reg)*devistation;
				int l = (int) ll;
				ll-= l;
				if (ll > Rnd.f())
					l++;
				bu.level.set(reg, l);

			}
		}
		
		for (RDRace r : RD.RACES().all) {
			int tot = r.pop.get(reg);
			int nn = (int) (1 + tot*(1.0-death));
			nn = CLAMP.i(nn, 1, tot);
			r.pop.set(reg, nn);
		}
		
		reg.fationSet(newFaction, true);
		BattleListener.notify(side, reg);
	}
	
	private static double[] needs = null;
	
	public static void ship(Side toSide, Side fromSide, int[] slaves, int[] loot) {
		
		if (needs == null) {
			needs = new double[AD.supplies().all.size()];
		}
		
		Shipment s = null;
		
		Faction to = toSide.us.get(0).faction();
		
		if (to != null && to.capitolRegion() != null) {
			for (Race r : RACES.all()) {
				if (slaves[r.index] > 0) {
					if (s == null) {
						s = WORLD.ENTITIES().caravans.create(fromSide.us.get(0).x(), fromSide.us.get(0).y(), to.capitolRegion(), ITYPE.spoils);
					}
					if (s != null) {
						s.load(r, slaves[r.index], HTYPES.PRISONER());
					}
				}
			}
		}
		
		Arrays.fill(needs, 0);
		
		for (SideUnit u : toSide.us) {
			if (u.a() != null && AD.men(null).get(u.a()) > 0) {
				WArmy a = u.a();
				for (ADSupply su : AD.supplies().all) {
					needs[su.index()] += su.needed(a);
				}
				
			}
		}

		for (SideUnit u : toSide.us) {
			if (u.a() != null && AD.men(null).get(u.a()) > 0) {
				WArmy a = u.a();
				for (ADSupply su : AD.supplies().all) {
					double n = su.needed(a);
					if (n == 0)
						continue;
					int am = (int) Math.ceil((n*loot[su.res.index()]/needs[su.index()]));
					am = CLAMP.i(am, 0, loot[su.res.index()]);
					am = CLAMP.i(am, 0, (int)n);
					su.current().inc(a, am);
					loot[su.res.index()] -= am;
				}
				
			}
		}

		if (to == null || to.capitolRegion() == null)
			return;
		
		
		for (RESOURCE res : RESOURCES.ALL()) {

			if (loot[res.index()] > 0) {
				if (s == null) {
					s = WORLD.ENTITIES().caravans.create(fromSide.us.get(0).x(), fromSide.us.get(0).y(), to.capitolRegion(), ITYPE.spoils);
				}
				if (s != null) {
					s.loadAndReserve(res, loot[res.index()]);
				}
				
			}
		}
		
		
		
	}
	
	
}