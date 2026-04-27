package settlement.job;

import settlement.main.SETT;
import settlement.tilemap.floor.Floors.Floor;
import settlement.tilemap.terrain.TBuilding;
import settlement.tilemap.terrain.TFence;
import settlement.tilemap.terrain.TFortification;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.map.MAP_OBJECT;

final class JobGetter implements MAP_OBJECT<Job>{

	@Override
	public Job get(int tile) {
		return get(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
	}

	@Override
	public Job get(int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return null;
		{
			Job j = SETT.JOBS().getter.get(tx, ty);
			if (j != null)
				return j;
		}
		TerrainTile t = SETT.TERRAIN().get(tx, ty);
		if (t instanceof TFortification.Tile){
			return SETT.JOBS().build_fort.all.get(((TFortification.Tile) t).fort.index());
		}
		if (t instanceof TBuilding.BuildingComponent) {
			JobBuildStructure tt = SETT.JOBS().build_structure.get(((TBuilding.BuildingComponent) t).building().structure.index());
			if (t instanceof TBuilding.Ceiling || t instanceof TBuilding.Ceiling.Opening)
				return tt.ceiling;
			return tt.wall;
		}
		if (t instanceof TFence.TFenceTile) {
			return SETT.JOBS().fences.get(((TFence.TFenceTile) t).fence.index());
		}
		if (t instanceof TFortification.Stairs) {
			return SETT.JOBS().build_fort.build_stairs;
		}
		
		if (t == SETT.TERRAIN().MOUNTAIN) {
			return SETT.JOBS().clearss.caveFill;
		}
		
		
		Floor f = SETT.FLOOR().getter.get(tx, ty);
		if (f != null && f.isRoad) {
			return SETT.JOBS().roads.all.get(f.indexRoad());
		}
		if (t == SETT.TERRAIN().CAVE) {
			return SETT.JOBS().clearss.tunnel;
		}
		return null;
	}

	
	
}
