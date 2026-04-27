package world.region.building;

import java.io.IOException;

import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import settlement.main.SETT;
import settlement.tilemap.terrain.TFortification;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tree;
import util.keymap.RMAP;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.RDBoostCache;
import world.region.pop.RDRace;

public final class RDBuildings {

	public final LIST<RDBuilding> all;
	public final LIST<RDBuilding> sorted;
	public final LIST<RDBuildingCat> cats;

	public final RDBoostCache levelRoad;
//	public final RDBoostCache levelFarm;
	public final RDBoostCache levelMine;
	public final RDBoostCache levelWall;
	
	private final RDLevelsTmp tmp;
	
	public final RDBuildPoints costs  = new RDBuildPoints();
	
	
	public RDBuildings(RDInit init) throws IOException{
		

		levelRoad = new RDBoostCache(init, "VISUAL_ROADS", "", "", null);
//		levelFarm = new RDBoostCache(init, "VISUAL_FARMS", "", "", null);
		levelMine = new RDBoostCache(init, "VISUAL_MINE", "", "", null);
		levelWall = new RDBoostCache(init, "VISUAL_WALL", "", "", null) {
			@Override
			protected double pget(Region reg) {
				if (reg == FACTIONS.player().capitolRegion()) {
					double am = 0;
					for (TFortification f : SETT.TERRAIN().FORTIFICATIONS.all()) {
						am += f.tile.count();
					}
					return CLAMP.d(am/(SETT.TWIDTH*4.0), 0, 1);
				}
				return super.pget(reg);
			}
		};
		
		ResFolder f = PATHS.WORLD().folder("building");
		
		Tree<RDBuildingCat> sort = new Tree<RDBuildingCat>(f.init.folders().length) {
			
			@Override
			protected boolean isGreaterThan(RDBuildingCat current, RDBuildingCat cmp) {
				return current.order > cmp.order;
			}
		};
		
		LinkedList<RDBuilding> all = new LinkedList<>();
		
		Creator creator = new Creator(this);
		for (String k : f.init.folders()) {
			sort.add(new RDBuildingCat(creator, all, init, k, f.folder(k)));
		}
		
		ArrayListGrower<RDBuildingCat> cats = new ArrayListGrower<>();
		while(sort.hasMore())
			cats.add(sort.pollSmallest());
		
		this.cats = cats;

		ArrayListGrower<RDBuilding> sorted = new ArrayListGrower<>();
		
		for (RDBuildingCat c : cats)
			for (RDBuilding b : c.all)
				sorted.add(b);
		this.sorted = sorted;
		
		

		this.all = new ArrayList<RDBuilding>(all);
		
		tmp = new RDLevelsTmp(all.size());
		
		init.points = costs;
	}
	
	public void init(RDInit init) {
		RMAP<RDBuilding> MAP = new RMAP<>("WORLD_BUILDING", all);
		
		for (RDRace rdrace : RD.RACES().all) {
			
			if (rdrace.race.pref().worldBuildingOverride == null)
				continue;
			
			Json json = rdrace.race.pref().worldBuildingOverride;
			rdrace.race.pref().worldBuildingOverride = null;
			
			
			MAP.new KJson("WORLD_BUILDING", json) {
				
				@Override
				protected void process(RDBuilding t, Json json, String key, boolean isWeak) {
					double v = json.d(key);
					
					for (int i = 1; i < t.levels.size(); i++) {
						RDBuildingLevel l = t.levels.get(i);
						for (int bi = 0; bi < l.local.all().size(); bi++) {
							BoostSpec sp = l.local.all().get(bi);
							
							if (sp.boostable == rdrace.loyalty.target) {
								replace(l.local, bi, sp, v);
							}else if (sp.boostable == rdrace.pop.dtarget) {
								replace(l.local, bi, sp, v);
							}else if (sp.boostable == rdrace.pop.growth) {
								replace(l.local, bi, sp, v);
							}
							
						}
					}
				}
				
				void replace(BoostSpecs l, int i, BoostSpec sp, double value) {
					
					double from = sp.booster.from();
					double to = sp.booster.to();
					if (sp.booster.isMul) {
						from = (from-1)*value + 1;
						to = (to-1)*value + 1;
					}else {
						from*= value;
						to *= value;
					}
					
					RBooster nn = new RBooster(sp.booster.info, from, to, sp.booster.isMul) {
						
						@Override
						public double get(Region t) {
							return 1.0;
						}
					};
					
					l.replace(i, nn, sp.boostable);
					
				}
			};
			
			
			
			
		}
		for (RDBuilding b : all) {
			b.connect(init);
		}
		
	}
	
	public void update() {
		if (tmp.active > 0) {
			tmp.active--;
		}
	}

	public RDLevelsTmp tmp(boolean init, Region reg) {
		costs.setDirty();
		tmp.active = 2;
		tmp.reg = reg;
		if (init)
			tmp.init(reg);
		return tmp;
	}
	
	public RDLevelsTmp tmp() {
		return tmp;
	}
	
	
}
