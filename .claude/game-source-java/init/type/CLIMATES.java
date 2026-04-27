package init.type;


import java.io.IOException;
import java.util.Arrays;

import game.battle.div.Div;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.paths.PATHS;
import init.sprite.UI.UI;
import init.value.GVALUES;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.data.BOOLEANO;
import util.info.INFO;
import util.keymap.RMAP;
import util.text.D;
import world.WORLD;
import world.map.regions.Region;

public final class CLIMATES {

	public final static String KEY = "CLIMATE";
	private static Data d;
	
	CLIMATES() throws IOException {
		d = new Data();
	}
	
	
	public static CLIMATE COLD() {
		return d.COLD;
	}
	
	public static CLIMATE TEMP() {
		return d.TEMPERATE;
	}
	
	public static CLIMATE HOT() {
		return d.HOT;
	}
	
	public static LIST<CLIMATE> ALL(){
		return d.all;
	}
	
	public static RMAP<CLIMATE> MAP(){
		return d.map;
	}
	
	public static INFO INFO() {
		return d.info;
	}
	
	public static BoostSpecs BONUS() {
		return d.boosters;
	}
	
	public static void pushBonuses(Json json, Boostable bo) {
		if (!json.has(KEY))
			return;
		double vv[] = new double[ALL().size()];
		Arrays.fill(vv, 1.0);
		CLIMATES.MAP().readFill(vv, json, 0, 2000);
		for (CLIMATE c : CLIMATES.ALL()) {
			if (vv[c.index()] == 1.0)
				continue;
			c.boosters.pushPromise(bo, null, vv[c.index()], true);
		}
		
		
	}

	public static BoostSpec pushIfDoesntExist(CLIMATE c, double v, Boostable bo, boolean isMul) {
		String k = bo.key + isMul;
		double none = isMul ? 1 : 0;
		
		if (d.bvmap.containsKey(k) && d.bvmap.get(k).values[c.index()] != none)
			return null;
		

		boolean ret = false;
		if (!d.bvmap.containsKey(k)) {
			ret = true;
			d.bvmap.put(k, new BV(d.boosters, bo, isMul));
		}
		BV bv = d.bvmap.get(k);
		bv.set(c, v);
		c.boosters.push(bo, v, isMul);
		return ret ? bv.spec : null;
	}
	
	
	private static final class Data {

		private final CLIMATE COLD;
		private final CLIMATE TEMPERATE;
		private final CLIMATE HOT;
		private final ArrayList<CLIMATE> all = new ArrayList<>(3);
		private final INFO info;
		private final BoostSpecs boosters;
		private final KeyMap<BV> bvmap = new KeyMap<BV>();
		private final RMAP<CLIMATE> map;
		
		Data() throws IOException{
			D.gInit(CLIMATES.class);
			info = new INFO(D.g("Climate"), D.g("desc", "Climate zones have a range of bonuses and drawbacks. They also have different base temperatures, which can lead to exposure and death for your subjects depending on their natural resilience to hot and cold."));
			d = this;
			Json j = new Json(PATHS.CONFIG().get(KEY));
			COLD = new CLIMATE(
					all, "COLD", 
					D.g("Cold"),
					D.g("cold_desc", "Very cold winters. Unique crops. Low disease rates."),
					j);
			TEMPERATE = new CLIMATE(
					all, "TEMPERATE", 
					D.g("Temperate"),
					D.g("temp_desc", "Varying temperature."),
					j);
			HOT = new CLIMATE(
					all, "HOT", 
					D.g("Warm"),
					D.g("warm_desc", "Hot summers."),
					j);
			
			map = new RMAP<>(KEY, all);
			
			boosters = new BoostSpecs(info.name, UI.icons().s.heat, true);
			
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					
					for (CLIMATE c : CLIMATES.ALL()) {
						
						for (BoostSpec s : c.boosters.all()) {
							String k = s.boostable.key + s.booster.isMul;
							if (!bvmap.containsKey(k)) {
								bvmap.put(k, new BV(boosters, s.boostable, s.booster.isMul));
							}
							bvmap.get(k).set(c, s.booster.to());
						}
					}
					
				}
			};
			BOOSTING.connecter(a);
			
			for (CLIMATE c : all) {
				GVALUES.FACTION.push("CLIMATE_" + c.key, info.name + ": " + c.name, UI.icons().s.heat, new BOOLEANO<Faction>() {
					
					@Override
					public boolean is(Faction t) {
						if (t.capitolRegion() != null)
							return WORLD.CLIMATE().getter.get(t.capitolRegion().cx(), t.capitolRegion().cy()) == c;
						return false;
					}
				});
				
			}
			
		}


		
	}
	
	private static class BV extends Booster{

		private double from;
		private double to;
		private final double[] values = new double[CLIMATES.ALL().size()];
		private final boolean isMul;
		public final BoostSpec spec;
		private final BValue value;
		
		BV(BoostSpecs bos, Boostable target, boolean isMul){
			super(new BSourceInfo(CLIMATES.INFO().name, UI.icons().s.heat), isMul);
			this.isMul = isMul;
			if (isMul)
				Arrays.fill(values, 1.0);
			set();
			spec = bos.push(this, target);
			value = new BValue() {
				
				@Override
				public double vGet(Region reg) {
					if (reg == null)
						return 0;
					double res = 0;
					for (int ci = 0; ci < CLIMATES.ALL().size(); ci++) {
						res += values[ci]*reg.info.climate(CLIMATES.ALL().get(ci));
					}
					
					return res;
				}
				
				@Override
				public double vGet(Faction f) {
					if (f == null)
						return 0;
					return BValue.super.vGet(f);
				}

				@Override
				public double vGet(Induvidual indu) {
					return vGet(indu.faction());
				}

				@Override
				public double vGet(Div div) {
					return vGet(div.faction());
				}

				@Override
				public double vGet(PopTime popTime) {
					if (FACTIONS.player() == null)
						return 0;
					return vGet(FACTIONS.player());
				}

				@Override
				public double vGet(Player f) {
					if (f.capitolRegion() == null)
						return 0;
					return values[WORLD.CLIMATE().getter.get(f.capitolRegion().cx(), f.capitolRegion().cy()).index()];
				}

				@Override
				public double vGet(FactionNPC f) {
					return vGet(f.capitolRegion());
				}
			};
		}
		
		void set(CLIMATE c, double value) {
			values[c.index()] = value;
			set();
		}
		
		private void set() {
			if (isMul) {
				from = 1.0;
				to = 1.0;
			}else {
				from = 0;
				to = 0;
			}
			
			for (double v : values) {
				
				from = Math.min(v, from);
				to = Math.max(v, to);
			}
			

		}
		


		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}

		@Override
		public double getValue(double input) {
			return input;
		}

		@Override
		protected double pget(BOOSTABLE_O o) {
			return o.boostableValue(value);
		}
		
		
	}
	
}
