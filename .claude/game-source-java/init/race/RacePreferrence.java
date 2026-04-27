package init.race;

import java.util.Arrays;
import java.util.Comparator;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.resources.ResGDrink;
import init.sprite.UI.Icon;
import init.type.BUILDING_PREF;
import init.type.BUILDING_PREFS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.water.pool.ROOM_POOL;
import settlement.stats.STATS;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.keymap.RMAP;
import util.text.D;
import view.main.VIEW;

public final class RacePreferrence {

	private static CharSequence ¤¤preference = "preference";
	static {
		D.ts(RacePreferrence.class);
	}
	
	public final LIST<ResG> food;
	public final LIST<ResGDrink> drink;
	public final RBIT drinkMask;
	public final RBIT foodMask;
	private final double[] structure = new double[BUILDING_PREFS.ALL().size()];
	private final double[] pools = new double[SETT.ROOMS().POOLS.size()];
	private final double[] work = new double[SETT.ROOMS().employment.ALLS().size()];
	private final double[] others = new double[RACES.all().size()];
	private Json jj;
	private final REN_PREF hov;
	public Json worldBuildingOverride;
	public final LIST<ROOM_RESTHOME> resthomes;
	
	private double[] priceCaps = new double[RESOURCES.ALL().size()]; 
	private double[] priceMuls = new double[RESOURCES.ALL().size()]; 
	
	public final Race mostHated;
	
	RacePreferrence(Json data, Race race) {

		data = data.json("PREFERRED");
		
		{
			food = new ArrayList<>(RESOURCES.EDI().MAP.readManyWarn("FOOD", data));
			if (food.size() == 0)
				data.error("Must have a favorite food!", "FOOD");
			jj = data;
			
			RBITImp m = new RBITImp();
			for (ResG e : food)
				m.or(e.resource);
			foodMask = m;
		}
		{
			drink = new ArrayList<>(RESOURCES.DRINKS().MAP.readManyWarn("DRINK", data));
			if (drink.size() == 0)
				data.error("Must have a favorite drink!", "DRINK");
			jj = data;
			
			RBITImp m = new RBITImp();
			for (ResG e : drink)
				m.or(e.resource);
			drinkMask = m;
		}
		
		
		{
			for (BUILDING_PREF p : BUILDING_PREFS.ALL())
				structure[p.index()] = p.defaultPref[race.index];
			
			BUILDING_PREFS.MAP().readFill(structure, data, 0, 1);
		}
		
		SETT.FLOOR().roadMap.new KJson("ROAD", data) {
			
			@Override
			protected void process(Floor s, Json j, String key, boolean isWeak) {
				s.prefSet(race, j.d(key, 0, 1));
			}
		};
		
		hov = new REN_PREF(race, false, Icon.M*10);
		

		
		{
			
			
			for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS())
				work[e.eindex()] = e.defaultFullfillment;
			
			SETT.ROOMS().collection. new KJson("WORK", data) {
				
				@Override
				protected void process(RoomBlueprint pp, Json j, String key, boolean isWeak) {
					if (pp != null && pp instanceof RoomBlueprintIns<?> && ((RoomBlueprintIns<?>) pp).employment() != null) {
						work[((RoomBlueprintIns<?>) pp).employment().eindex()] = j.d(key, 0, 1);
					}else if (!key.equals(RMAP.WILDCARD)){
						j.errorGet("Not a workable room", key);
					}
				}
			};
			SETT.ROOMS().collection. new KJson("POOL", data) {
				
				@Override
				protected void process(RoomBlueprint pp, Json j, String key, boolean isWeak) {
					if (pp != null && pp instanceof ROOM_POOL) {
						pools[((ROOM_POOL) pp).typeIndex()] = j.d(key, 0, 1);
					}else if (!key.equals(RMAP.WILDCARD)){
						j.errorGet("Not a pool!", key);
					}
				}
			};
			
			
		}
		
		{
			int rh = 0;
			for (ROOM_RESTHOME h : SETT.ROOMS().RESTHOMES) {
				if (work[h.employment().eindex()] > 0) {
					rh++;
				}
			}
			ROOM_RESTHOME[] hh = new ROOM_RESTHOME[rh];
			rh = 0;
			for (ROOM_RESTHOME h : SETT.ROOMS().RESTHOMES) {
				if (work[h.employment().eindex()] > 0) {
					hh[rh++] = h;
				}
			}
			
			Arrays.sort(hh, new Comparator<ROOM_RESTHOME>() {

				@Override
				public int compare(ROOM_RESTHOME o1, ROOM_RESTHOME o2) {
					return work[o1.employment().eindex()] > work[o2.employment().eindex()] ? 1 : -1;
				}
			});
			
			resthomes = new ArrayList<>(hh);
		}
		
		Arrays.fill(others, 1);
//		
//		
//		
//		if (data.has("OTHER_RACES")) {
//			RACES.map().fill("OTHER_RACES", others, data, -100000, 1000000);
//		}
//		
//		
//		double min = Double.MAX_VALUE;
//		double max = Double.MIN_VALUE;
//		
//		for (int i = 0; i < others.length; i++) {
//			min = Math.min(min, others[i]+1);
//			max = Math.max(max, others[i]+1);
//		}
//		
//		double delta = max-min;
//		if (delta == 0)
//			Arrays.fill(othersNor, 1);
//		else {
//			for (int i = 0; i < others.length; i++) {
//				double d = others[i] +1 - min;
//				double dd = max-min;
//				if (dd != 0)
//					d /= dd;
//				othersNor[i] = d;
//			}
//		}
		worldBuildingOverride = data;
		
		
		for (RESOURCE res : RESOURCES.ALL()) {
			priceMuls[res.index()] = res.priceMulDef;
			priceCaps[res.index()] = res.priceCapDef;
		}
		
		RESOURCES.map().new KJson("RESOURCE_RPICE_MUL", data) {
			
			@Override
			protected void process(RESOURCE s, Json j, String key, boolean isWeak) {
				priceMuls[s.index()] = j.d(key, 0, 100);
			}
		};
		
		RESOURCES.map().new KJson("RESOURCE_RPICE_CAP", data) {
			
			@Override
			protected void process(RESOURCE s, Json j, String key, boolean isWeak) {
				priceCaps[s.index()] = j.d(key, 0, 1);
			}
		};
		
		Race w = race;
		double m = Double.MAX_VALUE;
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (r != race && race(r) < m) {
				w = r;
				m = race(r);
			}
		}
		mostHated = w;

		
	}
	
	static void init() {
		
		boolean[][] hits = new boolean[RACES.all().size()][RACES.all().size()];
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race race = RACES.all().get(ri);
			Json j = race.pref().jj;
			
			RACES.map().new KJson("OTHER_RACES_REVERSE", j) {
				
				@Override
				protected void process(Race s, Json j, String key, boolean isWeak) {
					double v = j.d(key, 0, 1);
					if (!isWeak) {
						hits[s.index][race.index()] = true;
						s.pref().others[race.index()] = v;
					}else if (!hits[s.index][race.index()])
						s.pref().others[race.index()] = v;
						
				}
			};
			
			race.pref().others[race.index()] = 1.0;
		}
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race race = RACES.all().get(ri);
			Json j = race.pref().jj;
			race.pref().jj = null;
			RACES.map().new KJson("OTHER_RACES", j) {
				
				@Override
				protected void process(Race s, Json j, String key, boolean isWeak) {
					double v = j.d(key, 0, 1);
					if (!isWeak) {
						hits[s.index][race.index()] = true;
						race.pref().others[s.index()] = v;
					}else if (!hits[s.index][race.index()])
						race.pref().others[s.index()] = v;
					
				}
			};
			
			race.pref().others[race.index()] = 1.0;
		}

		
		
	}
	
	public ResG prefAllowedFood(Humanoid a) {
		RBIT b = STATS.FOOD().fetchMask(a);
		double ma = 0;
		for (ResG f : food) {
			if (b.has(f.resource))
				ma++;
		}
		if (ma == 0)
			return food.rnd();
		ma *= RND.rFloat();
		for (ResG f : food) {
			if (b.has(f.resource)) {
				if (ma <= 1)
					return f;
				ma -= 1;
			}
		}
		return food.rnd();
	}
	
	public double structure(BUILDING_PREF p) {
		return structure[p.index()];
	}
	public double pool(ROOM_POOL p) {
		return pools[p.typeIndex()];
	}
	
	public double getWork(RoomEmploymentSimple e) {
		return work[e.eindex()];
	}
	
	public double race(Race race) {
		return others[race.index];
	}
	
	public double priceMul(RESOURCE res) {
		return priceMuls[res.index()];
	}
	
	public double priceCap(RESOURCE res) {
		return priceCaps[res.index()];
	}

//	public double other(Race race) {
//		return others[race.index];
//	}
//	
//	public double otherNormalized(Race race) {
//		return othersNor[race.index];
//	}
	

	
	public void hoverOther(GUI_BOX box) {
		GBox b = (GBox) box;
		b.textLL(STATS.ENV().OTHERS.info().name);
		b.add(hov);
	}
	

	
	public static class REN_PREF extends HoverableAbs{
		
		private boolean big;
		private final int dim;
		private final Race rr;
		
		public REN_PREF(Race rr, boolean big, int width) {
			this.big = big;
			this.rr = rr;
			dim = big ? Icon.L+8 : Icon.M+6;
			
			int w = Math.min(dim*(width/dim), dim*RACES.all().size());
			
			body.setWidth(w);
			body.setHeight(dim + RACES.all().size()/(w/dim));
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			int x1 = body().x1();
			int y1 = body().y1();
			for (Race ra : RACES.all()) {
				if (ra == rr)
					continue;
				ColorImp col = ColorImp.TMP;
				double l = rr.pref().race(ra);
				GCOLOR.UI().badToGood(col, l);
				col.render(r, x1+1, x1+dim-1, y1+1, y1+dim-1);
				col.shadeSelf(0.5);
				col.renderFrame(r, x1+1, x1+dim-1, y1+1, y1+dim-1, 0, 1);
				SPRITE s = big ? ra.appearance().iconBig : ra.appearance().icon;
				s.renderC(r, x1+dim/2, y1+dim/2);
				x1 += dim;
				if (x1 >= body.x2()) {
					x1 = body.x1();
					y1 +=dim;
				}
			}
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			int tx = VIEW.mouse().x();
			int ty = VIEW.mouse().y();
			int x1 = body().x1();
			int y1 = body().y1();
			for (Race ra : RACES.all()) {
				if (ra == rr)
					continue;
				if (tx >= x1 && tx < x1+dim && ty >= y1 && ty < y1+dim) {
					b.title(ra.info.name);
					b.textLL(¤¤preference);
					b.tab(7);
					b.add(GFORMAT.perc(b.text(), rr.pref().race(ra)));
					b.NL();
				}
				x1 += dim;
				if (x1 >= body.x2()) {
					x1 = body.x1();
					y1 +=dim;
				}
			}
		}
		
		
	}
	
}
