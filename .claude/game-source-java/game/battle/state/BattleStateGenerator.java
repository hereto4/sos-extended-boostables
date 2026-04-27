package game.battle.state;

import game.GAME;
import game.battle.state.BattleStateSpec.SpecSide;
import game.battle.util.DivGeneration;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.tilemap.generator.Generator;
import snake2d.Errors;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.GUTIL;
import world.WORLD;
import world.map.regions.centre.WCentre;

final class BattleStateGenerator {

	SpecSide side1;
	SpecSide side2;
	private final LinkedList<ROOM_ARTILLERY> a1 = new LinkedList<>();
	private final LinkedList<ROOM_ARTILLERY> a2 = new LinkedList<>();
	private Race r1;
	private Race r2;
	
	void generate(BattleState s, BattleStateSpec spec, Rec deploymentTiles) {
		
		
		
		side1 = makeSide(spec.player, true);
		side2 = makeSide(spec.enemy, false);
		
		DIR d = DIR.N;
		d = DIR.get(spec.player.wCoo, spec.enemy.wCoo);
		
		if (d == DIR.C)
			d = DIR.N;
		
		if (!d.isOrtho())
			d = d.next(1);
		
		genMap(spec.player.wCoo.x(), spec.player.wCoo.y(), d);
		SETT.ROOMS().THRONE.init.place(SETT.TILE_BOUNDS.cX(), SETT.TILE_BOUNDS.cY(), 0);
		GAME.BATTLE_THREADS().pause();
		genArmies(s, spec.player, spec.enemy, deploymentTiles, d);
		SETT.init();
		Generator.paintMinimap();
		
		

	}
	
	private SpecSide makeSide(SpecSide side, boolean sideA){

		
		
		Race race = getRace(side.divs);
		if (sideA) {
			for (ROOM_ARTILLERY aa : SETT.ROOMS().ARTILLERY) {
				for (int i = 0; i < side.artillery[aa.typeIndex()]; i++)
					a1.add(aa);
			}
			r1 = race;
		}
		else {
			for (ROOM_ARTILLERY aa : SETT.ROOMS().ARTILLERY) {
				for (int i = 0; i < side.artillery[aa.typeIndex()]; i++)
					a2.add(aa);
			}
			r2 = race;
		}
		return side;
	}
	
	private static Race getRace(LIST<DivGeneration> divs) {
		int[] amount = new int[RACES.all().size()];
		for (DivGeneration d : divs) {
			amount[d.race] += d.indus.length;
		}
		
		int best = 0;
		int bestV = 0;
		for (Race r : RACES.all()) {
			if (amount[r.index] > bestV) {
				best = r.index;
				bestV = amount[r.index];
			}
				
		}
		return RACES.all().get(best);
	}
	
	private void genMap(int cx, int cy, DIR eDir) {
		
		cx = CLAMP.i(cx, 0, WORLD.TWIDTH()-1);
		cy = CLAMP.i(cy, 0, WORLD.THEIGHT()-1);
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(cx, cy, 0);
		int ts =0;
		while (GUTIL.flooder().hasMore()) {
			PathTile c = GUTIL.flooder().pollSmallest();
			ts ++;
			if (BattleState.okWorldTile(c.x(), c.y(), eDir)) {
				GUTIL.flooder().done();
				GAME.s().CreateFromWorldMap(c.x()-WCentre.TILE_DIM/2, c.y()-WCentre.TILE_DIM/2, true);
				return;
			}
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.IN_BOUNDS(c, d)) {
					GUTIL.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				}
			}
			
		}
		
		GUTIL.flooder().done();
		throw new Errors.GameError("Unable to find location for battle " + cx + " " + cy + " " + ts);		
		
		
	}
	
	private void genArmies(BattleState s, SpecSide a, SpecSide b, Rec tiles, DIR d) {

		for (int di = 0; di < Config.battle().DIVISIONS_PER_BATTLE; di++)
			GAME.ARMIES().division((short)di).info.menSet(0);
		
		tiles.set(
				SETT.TILE_BOUNDS.cX()+d.next(-2).x()*SETT.TWIDTH/2, SETT.TILE_BOUNDS.cX()+d.next(3).x()*SETT.TWIDTH/2, 
				SETT.TILE_BOUNDS.cY()+d.next(-2).y()*SETT.TWIDTH/2, SETT.TILE_BOUNDS.cY()+d.next(3).y()*SETT.TWIDTH/2);
		tiles.makePositive();
		
		
		
		BattleStateGenArmy.genArmy(side1.divs, true, d.perpendicular(), a1, r1, side1.moraleBase);
		BattleStateGenArmy.genArmy(side2.divs, false, d, a2, r2, side2.moraleBase);
		
		GAME.ARMIES().initAndTeleport(GAME.ARMIES().divisions());
		
		
	}
	
}
