package game.battle.state;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.ArmyFormations.ArmyFormation;
import game.battle.util.ArmyFormations.ArmyFormationDiv;
import game.battle.util.DivGeneration;
import init.constant.C;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class BattleStateGenArmy {

//	private final static Tree<WDivId> sort = new Tree<WDivId>(Config.battle().DIVISIONS_PER_ARMY) {
//
//		@Override
//		protected boolean isGreaterThan(WDivId current, WDivId cmp) {
//			return !current.gen.isRange;
//		}
//		
//		
//	};
	
	public static void genArmy(ArrayList<DivGeneration> ss, boolean player, DIR d, LIST<ROOM_ARTILLERY> art, Race artrace, double moraleBase) {
		
		GAME.ARMIES().factors.init(player ? GAME.ARMIES().player() : GAME.ARMIES().enemy(), moraleBase);
		
		ArmyFormation ff = player ? GAME.battle().formations.player : GAME.battle().formations.all.rnd();
		
		ArrayList<ArmyFormationDiv> divs = ff.getFirstRow(ss); 
		
		
		int w = 12;
		int cx = SETT.TILE_BOUNDS.cX()+d.x()*120 + d.next(2).x()*w/2;
		int cy = SETT.TILE_BOUNDS.cY()+d.y()*120 + d.next(2).y()*w/2;
		
		int depth = 0;
		for (; depth < 256 && !divs.isEmpty(); depth += w+1) {
			for (int width = 0; width < 220; width++) {
				if (divs.isEmpty())
					break;
				int x1 = cx + depth*d.x() + d.next(2).x()*width;
				int y1 = cy + depth*d.y() + d.next(2).y()*width;
				ArmyFormationDiv div = ff.get(divs, 1.0-width/220.0, width/220.0, 1.0-depth/256.0, depth/256.0);
				if (divPlace(div, player, w, d, x1, y1)) {
					width -= w;
					if (width < 0)
						width = 0;
					divs.remove(div);
				}
					
				
				if (divs.isEmpty())
					break;
				
				
				x1 = cx + depth*d.x() - d.next(2).x()*width;
				y1 = cy + depth*d.y() - d.next(2).y()*width;
				div = ff.get(divs, 1.0-width/220.0, width/220.0, 1.0-depth/256.0, depth/256.0);
				if (divPlace(div, player, w, d, x1, y1)) {
					width -= w;
					if (width < 0)
						width = 0;
					
					divs.remove(div);
				}
			}
		}
		
		depth += BattleStateArt.placeArt(cx, cy, depth, d, art, artrace, player);
		
		
		
		if (player) {
			placeThrone(cx, cy, depth, d);
		}
		
		
	}
	
	private static void placeThrone(int cx, int cy, int depth, DIR d) {
		depth += 15;
		int x1 = cx + depth*d.x();
		int y1 = cy + depth*d.y();
		int dd = 0;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			if (DIR.ORTHO.get(di) == d.perpendicular())
				break;
			dd++;
		}
		
		SETT.ROOMS().THRONE.init.place(x1, y1, dd);
	}
	
	static int men;
	private static final Rec tmp = new Rec();
	private static final ArrayList<Div> tmpDivs = new ArrayList<>(1);
	
	private static boolean divPlace(ArmyFormationDiv wdiv, boolean player, int widthMax, DIR d, int tx1, int ty1) {
		
		
		
		DivGeneration div = wdiv.g;
		
		if (div.indus.length == 0)
			return true;
		
		
		int depthMax = (int) Math.ceil((double)div.indus.length/widthMax);
		if (d.x() != 0) {
			int bi = depthMax;
			depthMax = widthMax;
			widthMax = bi;
		}
		
		d = d.next(-1);
		
		
		
		
		
		tmp.set(tx1, tx1+d.x()*depthMax, ty1,  ty1+d.y()*widthMax);
		tmp.makePositive();
		
		int men = 0;
		
		for (COORDINATE c : tmp) {
			if (SETT.PATH().availability.get(c).player <= 0 || SETT.ENTITIES().hasAtTile(c.x(), c.y())) {
				return false;
			}
		}
		Div adiv = (player ? GAME.ARMIES().player() : GAME.ARMIES().enemy()).divisions().get(wdiv.divID);
		
		adiv.settings().clear();
		adiv.settings().musteringSet(true);
		adiv.settings().fireAtWill = true;
		
		Race race = RACES.all().get(div.race);
		adiv.info.raceSet(race);
		adiv.info.menSet(div.indus.length);
		adiv.info.bannerISet(div.bannerI);
		adiv.info.name().clear().add(div.name);
		
		HTYPE type = player ? HTYPES.SOLDIER() : HTYPES.ENEMY();
		
		int am = div.indus.length;
		
		
		if (am > Config.battle().MEN_PER_DIVISION)
			throw new RuntimeException(div + " " + am);
		
		for (COORDINATE c : tmp) {
			if (men >= div.indus.length)
				break;
			
			Humanoid h = SETT.HUMANOIDS().create(race, c.x(), c.y(), type, CAUSE_ARRIVES.SOLDIER_RETURN());
			if (!h.isRemoved()) {
				STATS.NEEDS().clear(h.indu());
				h.indu().copyFrom(div.indus[men]);
				STATS.BATTLE().basicTraining.setD(h.indu(), 1.0);
				
				
				for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
					m.set(h.indu(), m.get(div.indus[men]));
				}
				
				men ++;
				h.setDivision(adiv);
			}
		}

		
		
		if (men == 0)
			return false;
		
		adiv.info.copySettings(wdiv.g.target);
		for (EquipRange m : STATS.EQUIP().RANGED()) {
			m.ammoClear(adiv);
		}
		d = d.next(-1);
		
		int x1 = tx1*C.TILE_SIZE+C.TILE_SIZEH;
		int y1 = ty1*C.TILE_SIZE+C.TILE_SIZEH;
		int x2 = x1 + d.x()*depthMax*C.TILE_SIZE;
		int y2 = y1 + d.y()*widthMax*C.TILE_SIZE;
		tmpDivs.clear();
		tmpDivs.add(adiv);
		GAME.ARMIES().placer.deploy(tmpDivs, x1, x2, y1, y2);
		GAME.ARMIES().initAndTeleport(tmpDivs);
		
		return true;
		
		
	}
	
	final static class WDivId {
		final int di;
		final DivGeneration gen;
		
		public WDivId(int id, DivGeneration gen) {
			this.di = id;
			this.gen = gen;
		}
		
	}
	
}
