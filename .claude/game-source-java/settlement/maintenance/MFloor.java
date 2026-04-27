package settlement.maintenance;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.GRASS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TWIDTH;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.util.rnd.RND;

final class MFloor extends MType{

	MFloor(){
		
	}
	
	@Override
	public boolean degrade(int tx, int ty, int tile, double rate) {
		if (!FLOOR().getter.is(tx, ty)) {
			return false;
		}
		if (PATH().solidity.is(tx, ty)) {
			return false;
		}
		if (SETT.ROOMS().map.is(tx, ty)) {
			return false;
		}
		if (RND.rFloat() < rate*SETT.MAINTENANCE().tilesPerDay*(1.0-FLOOR().getter.get(tx, ty).durability)) {
			if (FLOOR().degrade(tx, ty) > 0)
				FLOOR().degradeInc(tile, 1+RND.rInt(3));
			else
				FLOOR().degradeInc(tile, 1);
			if (RND.oneIn(2))
				SETT.GRASS().grow(tx, ty);
		}
		return true;
	}
	
	@Override
	public void vandalize(int tx, int ty) {
		FLOOR().degradeInc(tx + TWIDTH*ty, 3+RND.rInt(2));
	}
	
	@Override
	public int shouldPlaceResource(int tx, int ty) {
		Floor f = SETT.FLOOR().getter.get(tx, ty);
		if (RND.rFloat() < 0.25*f.resAmount*SETT.MAINTENANCE().resRate)
			return 1;
		return 0;
	}
		
	@Override
	public boolean validate(int tx, int ty) {
		Floor f = FLOOR().getter.get(tx, ty);
		if (f != null && f.isRoad && !PATH().solidity.is(tx, ty) && !SETT.ROOMS().map.is(tx, ty)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void maintain(int tx, int ty) {
		
		if (FLOOR().getter.is(tx, ty)) {
			int i = tx+ty*TWIDTH;
			FLOOR().degradeInc(i, -FLOOR().degrade(tx, ty));
			GRASS().current.set(tx,  ty, 0);
		}
	}

	@Override
	public RESOURCE res(int tx, int ty, int ri) {
		if (ri > 0)
			return SETT.FLOOR().getter.get(tx, ty).resource;
		return null;
	}

	@Override
	public boolean shouldPlace(int tx, int ty, boolean was) {
		Floor f = FLOOR().getter.get(tx, ty);
		if (f != null && !f.reqs.passes(FACTIONS.player()))
			return false;
		return FLOOR().degrade(tx, ty) > 0;
	}

	@Override
	public double resRate(int tx, int ty, int ri) {
		if (ri != 1) {
			return 0;
		}
		if (!validate(tx, ty))
			return 0;
		Floor f = FLOOR().getter.get(tx, ty);
		return 0.25*f.resAmount*SETT.MAINTENANCE().resRate*(1.0-f.durability);
	}
	
	@Override
	public double degrade(int tx, int ty) {
		return FLOOR().degrade.get(tx, ty);
	}
	
}